/*
 * Cuelib library for manipulating cue sheets.
 * Copyright (C) 2018 Digital Media Server developers.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package org.digitalmediaserver.cuelib.io;

import java.io.EOFException;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import org.digitalmediaserver.cuelib.CueParser;
import org.digitalmediaserver.cuelib.CueSheet;
import org.digitalmediaserver.cuelib.FileData;
import org.digitalmediaserver.cuelib.Index;
import org.digitalmediaserver.cuelib.Position;
import org.digitalmediaserver.cuelib.TrackData;
import org.digitalmediaserver.cuelib.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This class parses embedded cue sheets from FLAC files. It is designed to be
 * fast when scanning files without cue sheets. No logging is done until a cue
 * sheet is found.
 * <p>
 * The parser uses {@link ReadableByteChannel} which means it will work even on
 * non-seekable sources. Internally, seeking is used for skipping if the source
 * implements {@link SeekableByteChannel} for better performance, but it is not
 * a requirement.
 * <p>
 * The parser also allows a {@link ByteBuffer} to be specified. Since direct
 * allocated byte buffers has a weak implementation for freeing the memory, this
 * allows reuse of the same buffer instance when scanning multiple sources. The
 * parsing will fail if the buffer is very small (less than 128 bytes). A small
 * buffer will mean a lot of buffer rotation and performance will suffer as a
 * result. A very large buffer may also hinder performance, since more data than
 * needed might be read into the buffer. If performance is vital, different
 * buffer sizes should be tested. If is assumed that a buffer somewhere between
 * 2 kB and 4 kB will be optimal taking into consideration the typical size of
 * the FLAC metadata header.
 * <p>
 * To probe FLACs for cue sheets, simply call on of the static
 * {@code getCueSheet()} methods. If {@code null} is returned, no cue sheet was
 * found.
 *
 * @author Nadahar
 */
public class FLACReader {

	private static final Logger LOGGER = LoggerFactory.getLogger(FLACReader.class);

	/** The {@code STREAMINFO} block type */
	public static final byte STREAMINFO = 0;

	/** The {@code PADDING} block type */
	public static final byte PADDING = 1;

	/** The {@code APPLICATION} block type */
	public static final byte APPLICATION = 2;

	/** The {@code SEEKTABLE} block type */
	public static final byte SEEKTABLE = 3;

	/** The {@code VORBIS_COMMENT} block type */
	public static final byte VORBIS_COMMENT = 4;

	/** The {@code CUESHEET} block type */
	public static final byte CUESHEET = 5;

	/** The {@code PICTURE} block type */
	public static final byte PICTURE = 6;

	/** The default file name to use for the {@code FILE} command */
	public static final String DEFAULT_FILENAME = "self.flac";

	/** The input */
	protected final ReadableByteChannel byteChannel;

	/** The FLAC file {@link Path} reference */
	protected final Path file;

	/**
	 * Tries to create a {@link CueSheet} from the FLAC metadata in the
	 * specified {@link Path}.
	 *
	 * @param file the {@link Path} to read from.
	 * @return The new {@link CueSheet} or {@code null} if none was found.
	 * @throws IOException If an error occurs when opening the file.
	 */
	public static CueSheet getCueSheet(Path file) throws IOException {
		if (file == null) {
			return null;
		}
		try (SeekableByteChannel byteChannel = Files.newByteChannel(file)) {
			return new FLACReader(byteChannel, file).extractCueSheet(null);
		}
	}

	/**
	 * Tries to create a {@link CueSheet} from the FLAC metadata in the
	 * specified {@link Path}.
	 * <p>
	 * If a {@link ByteBuffer} is specified, it will be used internally to read
	 * from the file. This allows reuse of the buffer when scanning multiple
	 * sources. All existing data in the {@link ByteBuffer} will be lost.
	 *
	 * @param file the {@link Path} to read from.
	 * @param buffer the reusable {@link ByteBuffer} or {@code null} to have one
	 *            created for this read instance.
	 * @return The new {@link CueSheet} or {@code null} if none was found.
	 * @throws IOException If an error occurs when opening the file.
	 */
	public static CueSheet getCueSheet(Path file, ByteBuffer buffer) throws IOException {
		if (file == null) {
			return null;
		}
		try (SeekableByteChannel byteChannel = Files.newByteChannel(file)) {
			return new FLACReader(byteChannel, file).extractCueSheet(buffer);
		}
	}

	/**
	 * Tries to create a {@link CueSheet} from the FLAC metadata in the
	 * specified {@link ReadableByteChannel}. If no {@code FILE} value is
	 * available in the cue sheet data, {@value #DEFAULT_FILENAME} will be used.
	 *
	 * @param byteChannel the {@link ReadableByteChannel} to read from.
	 * @return The new {@link CueSheet} or {@code null} if none was found.
	 */
	public static CueSheet getCueSheet(ReadableByteChannel byteChannel) {
		return byteChannel == null ? null : new FLACReader(byteChannel).extractCueSheet(null);
	}

	/**
	 * Tries to create a {@link CueSheet} from the FLAC metadata in the
	 * specified {@link ReadableByteChannel}.
	 *
	 * @param byteChannel the {@link ReadableByteChannel} to read from.
	 * @param file the {@link Path} to use for the {@code FILE} command in
	 *            resulting the {@link CueSheet}, and for logging purposes. If
	 *            {@code null}, {@value #DEFAULT_FILENAME} will be used if no
	 *            {@code FILE} value is available in the cue sheet data.
	 * @return The new {@link CueSheet} or {@code null} if none was found.
	 */
	public static CueSheet getCueSheet(ReadableByteChannel byteChannel, Path file) {
		return byteChannel == null ? null : new FLACReader(byteChannel, file).extractCueSheet(null);
	}

	/**
	 * Tries to create a {@link CueSheet} from the FLAC metadata in the
	 * specified {@link ReadableByteChannel}.
	 * <p>
	 * If a {@link ByteBuffer} is specified, it will be used internally to read
	 * from the byte channel. This allows reuse of the buffer when scanning
	 * multiple sources. All existing data in the {@link ByteBuffer} will be
	 * lost. If no {@code FILE} value is available in the cue sheet data,
	 * {@value #DEFAULT_FILENAME} will be used.
	 *
	 * @param byteChannel the {@link ReadableByteChannel} to read from.
	 * @param buffer the reusable {@link ByteBuffer} or {@code null} to have one
	 *            created for this read instance.
	 * @return The new {@link CueSheet} or {@code null} if none was found.
	 */
	public static CueSheet getCueSheet(ReadableByteChannel byteChannel, ByteBuffer buffer) {
		return byteChannel == null ? null : new FLACReader(byteChannel).extractCueSheet(buffer);
	}

	/**
	 * Tries to create a {@link CueSheet} from the FLAC metadata in the
	 * specified {@link ReadableByteChannel}.
	 * <p>
	 * If a {@link ByteBuffer} is specified, it will be used internally to read
	 * from the byte channel. This allows reuse of the buffer when scanning
	 * multiple sources. All existing data in the {@link ByteBuffer} will be
	 * lost.
	 *
	 * @param byteChannel the {@link ReadableByteChannel} to read from.
	 * @param buffer the reusable {@link ByteBuffer} or {@code null} to have one
	 *            created for this read instance.
	 * @param file the {@link Path} to use for the {@code FILE} command in
	 *            resulting the {@link CueSheet}, and for logging purposes. If
	 *            {@code null}, {@value #DEFAULT_FILENAME} will be used if no
	 *            {@code FILE} value is available in the cue sheet data.
	 * @return The new {@link CueSheet} or {@code null} if none was found.
	 */
	public static CueSheet getCueSheet(ReadableByteChannel byteChannel, ByteBuffer buffer, Path file) {
		return byteChannel == null ? null : new FLACReader(byteChannel, file).extractCueSheet(buffer);
	}

	/**
	 * Creates a new instance using the specified {@link ReadableByteChannel}.
	 * The file in the resulting {@link CueSheet} will either be that specified
	 * in the cue sheet data or {@value #DEFAULT_FILENAME}.
	 *
	 * @param byteChannel the {@link ReadableByteChannel} to read from.
	 */
	public FLACReader(ReadableByteChannel byteChannel) {
		this(byteChannel, null);
	}

	/**
	 * Creates a new instance using the specified {@link ReadableByteChannel}.
	 *
	 * @param byteChannel the {@link ReadableByteChannel} to read from.
	 * @param file the file to specify in the resulting {@link CueSheet}. If
	 *            blank and nothing is specified in the cue sheet data itself,
	 *            {@value #DEFAULT_FILENAME} is used.
	 */
	public FLACReader(ReadableByteChannel byteChannel, Path file) {
		this.byteChannel = byteChannel;
		this.file = file;
	}

	/**
	 * Tries to find and parse a {@link CueSheet} in the FLAC metadata.
	 *
	 * @return The new {@link CueSheet} or {@code null} if none was found.
	 */
	public CueSheet extractCueSheet() {
		return extractCueSheet(null);
	}

	/**
	 * Tries to find and parse a {@link CueSheet} in the FLAC metadata.
	 * <p>
	 * If a {@link ByteBuffer} is specified, it will be used internally to read
	 * from the byte channel. This allows reuse of the buffer when scanning
	 * multiple sources. All existing data in the {@link ByteBuffer} will be
	 * lost.
	 *
	 * @param buffer the reusable {@link ByteBuffer} or {@code null} to have one
	 *            created for this read instance.
	 * @return The new {@link CueSheet} or {@code null} if none was found.
	 */
	public CueSheet extractCueSheet(ByteBuffer buffer) {
		boolean selfAllocated;
		if (buffer == null) {
			selfAllocated = true;
			buffer = ByteBuffer.allocateDirect(3);
			buffer.limit(0);
		} else {
			selfAllocated = false;
		}
		buffer.order(ByteOrder.BIG_ENDIAN);
		try {
			ensureAvailable(buffer, 3);
			byte b = buffer.get();
			if (b == 'f' && buffer.get() == 'L' && buffer.get() == 'a') {
				if (selfAllocated) {
					buffer = ByteBuffer.allocateDirect(2048);
					buffer.order(ByteOrder.BIG_ENDIAN);
					buffer.limit(0);
				}
				ensureAvailable(buffer, 1);
				if (buffer.get() != 'C') {
					return null;
				}
				return findCueSheet(buffer);
			} else if (b == 'I' && buffer.get() == 'D' && buffer.get() == '3') {
				if (selfAllocated) {
					buffer = ByteBuffer.allocateDirect(4096);
					buffer.order(ByteOrder.BIG_ENDIAN);
					buffer.limit(0);
				}
				return skipID3v2(buffer) ? extractCueSheet(buffer) : null;
			} else {
				return null;
			}
		} catch (IOException e) {
			if (file == null) {
				LOGGER.error("An error occurred while parsing cue sheet from FLAC metadata: {}", e.getMessage());
			} else {
				LOGGER.error(
					"An error occurred while parsing cue sheet from FLAC metadata in \"{}\": {}",
					file,
					e.getMessage()
				);
			}
			LOGGER.trace("", e);
			return null;
		}
	}

	/**
	 * Iterates through the FLAC metadata blocks looking for potential cue sheet
	 * data. Will try to parse if anything is found.
	 *
	 * @param buffer the {@link ByteBuffer} to use.
	 * @return The new {@link CueSheet} or {@code null} if none was found.
	 * @throws IOException If an error occurred during the operation.
	 */
	protected CueSheet findCueSheet(ByteBuffer buffer) throws IOException {
		byte blockHeader;
		StreamInfo streamInfo = null;
		for (;;) {
			ensureAvailable(buffer, 4);
			blockHeader = buffer.get();
			int blockType = blockHeader & 0x7F;
			int size = ((buffer.get() & 0xFF) << 16) | ((buffer.get() & 0xFF) << 8) | buffer.get() & 0xFF;
			int read = 0;
			if (blockType == STREAMINFO) {
				streamInfo = parseStreamInfoBlock(buffer);
				read = size;
			} else if (blockType == CUESHEET) {
				CueSheetResult result = parseCuesheetBlock(buffer, streamInfo);
				if (result.getCueSheet() != null) {
					if (LOGGER.isDebugEnabled()) {
						if (file == null) {
							LOGGER.debug(
								"Parsed the following cue sheet from the FLAC CUESHEET block:\n{}",
								result.getCueSheet()
							);
						} else {
							LOGGER.debug(
								"Parsed the following cue sheet from the FLAC CUESHEET block in \"{}\":\n{}",
								file,
								result.getCueSheet()
							);
						}
					}
					return result.getCueSheet();
				}
				read = result.getReadLength();
			} else if (blockType == VORBIS_COMMENT) {
				CueSheetResult result = parseVorbisCommentBlock(buffer);
				if (result.getCueSheet() != null) {
					if (LOGGER.isDebugEnabled()) {
						if (file == null) {
							LOGGER.debug(
								"Parsed the following cue sheet from the FLAC VORBIS_COMMENT block:\n{}",
								result.getCueSheet()
							);
						} else {
							LOGGER.debug(
								"Parsed the following cue sheet from the FLAC VORBIS_COMMENT block in \"{}\":\n{}",
								file,
								result.getCueSheet()
							);
						}
					}
					return result.getCueSheet();
				}
				buffer.order(ByteOrder.BIG_ENDIAN);
				read = result.getReadLength();
			}
			if (isLastBlock(blockHeader)) {
				return null;
			}
			skip(buffer, size - read);
		}
	}

	/**
	 * Parses the {@code METADATA_BLOCK_STREAMINFO}.
	 *
	 * @param buffer the {@link ByteBuffer} to use.
	 * @return The resulting {@link StreamInfo}.
	 * @throws IOException If an error occurred during the operation.
	 */
	protected StreamInfo parseStreamInfoBlock(ByteBuffer buffer) throws IOException {
		skip(buffer, 10);
		ensureAvailable(buffer, 4);
		int pos = buffer.position();
		int sampleRate =
			((buffer.get(pos) & 0xFF) << 12) |
			((buffer.get(pos + 1) & 0xFF) << 4) |
			((buffer.get(pos + 2) & 0xF0) >> 4);
		int bitsPerSample = ((buffer.get(pos + 2) & 1) << 4) | ((buffer.get(pos + 3) & 0xF0) >> 4) + 1;
		skip(buffer, 24);
		return new StreamInfo(sampleRate, bitsPerSample);
	}

	/**
	 * Parses the {@code METADATA_BLOCK_CUESHEET}.
	 *
	 * @param buffer the {@link ByteBuffer} to use.
	 * @param streamInfo the {@link StreamInfo} to use.
	 * @return The resulting {@link CueSheetResult}.
	 * @throws IOException If an error occurred during the operation.
	 */
	protected CueSheetResult parseCuesheetBlock(ByteBuffer buffer, StreamInfo streamInfo) throws IOException {
		if (LOGGER.isDebugEnabled()) {
			if (file == null) {
				LOGGER.debug("Parsing FLAC CUESHEET block");
			} else {
				LOGGER.debug("Parsing FLAC CUESHEET block in \"{}\"", file);
			}
		}
		CueSheet result = new CueSheet(file);
		int read = 0;
		result.setCatalog(readString(buffer, StandardCharsets.US_ASCII, 128, true, true));
		read += 128;
		skip(buffer, 267);
		read += 267;
		ensureAvailable(buffer, 1);
		int numTracks = buffer.get() & 0xFF;
		read++;
		if (numTracks > 0) {
			Path fileName = file == null ? null : file.getFileName();
			String fileNameStr = fileName == null ? null : fileName.toString();
			if (Utils.isBlank(fileNameStr)) {
				fileNameStr = DEFAULT_FILENAME;
			}
			FileData fileData = new FileData(result, fileNameStr, "WAVE");
			result.getFileData().add(fileData);
			List<TrackData> tracks = fileData.getTrackData();
			for (int i = 0; i < numTracks; i++) {
				ensureAvailable(buffer, 9);
				long trackOffsetSamples = buffer.getLong();
				read += 8;
				TrackData track = new TrackData(fileData, buffer.get() & 0xFF, "AUDIO");
				read++;
				track.setIsrcCode(readString(buffer, StandardCharsets.US_ASCII, 12, true, true));
				read += 12;
				ensureAvailable(buffer, 1);
				boolean isAudio = (buffer.get() & 0x80) == 0;
				if (!isAudio) {
					track.setDataType("DATA");
				}
				read++;
				skip(buffer, 13);
				read += 13;
				ensureAvailable(buffer, 1);
				int numIndicies = buffer.get() & 0xFF;
				read++;
				for (int j = 0; j < numIndicies; j++) {
					ensureAvailable(buffer, 12);
					long offsetSamples = buffer.getLong();
					int indexNo = buffer.get() & 0xFF;
					skip(buffer, 3);
					read += 12;
					Position position = new Position(trackOffsetSamples + offsetSamples, streamInfo.getSampleRate());
					Index index = new Index(indexNo, position);
					track.getIndices().add(index);
				}
				int trackNo = track.getNumber();
				if (trackNo != 0 && (isAudio && trackNo != 170 || !isAudio && trackNo != 255)) {
					tracks.add(track);
				}
			}
		}

		return new CueSheetResult(result.getAllTrackData().isEmpty() ? null : result, read);
	}

	/**
	 * Parses the {@code METADATA_BLOCK_VORBIS_COMMENT} and looks for a
	 * {@code CUESHEET} tag.
	 *
	 * @param buffer the {@link ByteBuffer} to use.
	 * @return The resulting {@link CueSheetResult}.
	 * @throws IOException If an error occurred during the operation.
	 */
	protected CueSheetResult parseVorbisCommentBlock(ByteBuffer buffer) throws IOException {
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		int read = 0;
		ensureAvailable(buffer, 4);
		int length = buffer.getInt();
		read += 4;
		skip(buffer, length);
		read += length;
		ensureAvailable(buffer, 4);
		length = buffer.getInt();
		read += 4;
		int commentLength;
		byte[] commentID = new byte[9];
		for (int i = 0; i < length; i++) {
			ensureAvailable(buffer, 4);
			commentLength = buffer.getInt();
			read += 4 + commentLength;
			if (commentLength > 9) {
				ensureAvailable(buffer, 9);
				buffer.get(commentID);
				if (!"CUESHEET=".equals(new String(commentID, StandardCharsets.US_ASCII).toUpperCase(Locale.ROOT))) {
					skip(buffer, commentLength - 9);
					continue;
				}
				if (LOGGER.isDebugEnabled()) {
					if (file == null) {
						LOGGER.debug("Parsing cue sheet from the FLAC VORBIS_COMMENTCUESHEET block");
					} else {
						LOGGER.debug("Parsing cue sheet from the FLAC VORBIS_COMMENTCUESHEET block in \"{}\"", file);
					}
				}
				try (
					LineNumberReader reader = new LineNumberReader(new StringReader(
						readString(buffer, StandardCharsets.UTF_8, commentLength - 9, false, false)
				))) {
					CueSheet result = CueParser.parse(reader, file);
					if (file != null) {
						Path fileName = file.getFileName();
						if (fileName != null) {
							String fileNameStr = fileName.toString();
							for (FileData fileData : result.getFileData()) {
								fileData.setFile(fileNameStr);
							}
						}
					}
					return new CueSheetResult(result, read);
				}
			}
			skip(buffer, commentLength);
		}

		return new CueSheetResult(null, read);
	}

	/**
	 * Skips over an {@code ID3v2} tag.
	 *
	 * @param buffer the {@link ByteBuffer} to use.
	 * @return {@code true} if an {@code ID3v2} tag was skipped, {@code false}
	 *         if it failed.
	 * @throws IOException If an error occurred during the operation.
	 */
	protected boolean skipID3v2(ByteBuffer buffer) throws IOException {
		ensureAvailable(buffer, 7);
		if (buffer.get() == -1 || buffer.get() == -1) {
			return false;
		}
		skip(buffer, 1);
		byte[] bytes = new byte[4];
		buffer.get(bytes);

		// ID3 32/28 bit synchsafe integer
		int tagSize = ((bytes[0] & 0xff) << 21) + ((bytes[1] & 0xff) << 14) + ((bytes[2] & 0xff) << 7) + ((bytes[3]) & 0xff);
		skip(buffer, tagSize);
		return true;
	}

	/**
	 * Makes sure that at least the specified number of bytes is available for
	 * reading from the specified buffer.
	 *
	 * @param buffer the {@link ByteBuffer}.
	 * @param length the number of bytes to make sure is available.
	 * @throws EOFException If the source doesn't contain the required number of
	 *             bytes.
	 * @throws IOException If an error occurs during the operation.
	 * @throws IllegalStateException If the number of bytes required is larger
	 *             than the size of {@code buffer}.
	 */
	protected void ensureAvailable(ByteBuffer buffer, int length) throws IOException {
		if (length > buffer.capacity()) {
			throw new IllegalStateException(
				"Impossible to make " + length + " bytes available in a buffer of size " + buffer.capacity()
			);
		}
		int available = buffer.remaining();
		if (available >= length) {
			return;
		}
		buffer.compact();
		int count;
		while (available < length) {
			count = byteChannel.read(buffer);
			if (count < 0) {
				throw new EOFException("The required number of bytes (" + length + " ) isn't available");
			}
			available += count;
		}
		buffer.flip();
	}

	/**
	 * Skips the specified number of bytes. The buffer will be rotated if
	 * necessary. This method operates differently depending on whether or not
	 * the underlying {@link ReadableByteChannel} also is a
	 * {@link SeekableByteChannel}. If seek is available and the skip is larger
	 * than what remains in the buffer, the position in the underlying
	 * {@link SeekableByteChannel} is changed and the buffer is emptied. If seek
	 * isn't available, the buffer will be rotated and skipped as needed.
	 * <p>
	 * If skipping beyond the end of the byte channel, the behavior differs
	 * between the two internal implementations. If seek is available, no
	 * {@link EOFException} will be thrown by this method. It will however be
	 * thrown at the first attempt to read from the underlying
	 * {@link ReadableByteChannel} after the skip. If seek isn't available, this
	 * method will throw an {@link EOFException} if trying to read beyond the
	 * end of the byte stream.
	 *
	 * @param buffer the {@link ByteBuffer} to use.
	 * @param count the number of bytes to skip.
	 * @throws EOFException If the source doesn't contain the required number of
	 *             bytes.
	 * @throws IOException If an error occurs during the operation.
	 */
	protected void skip(ByteBuffer buffer, long count) throws IOException {
		if (count < 1) {
			return;
		}

		if (count <= buffer.remaining()) {
			buffer.position(buffer.position() + (int) count);
			return;
		}

		if (byteChannel instanceof SeekableByteChannel) {
			// The fast way
			SeekableByteChannel seekable = (SeekableByteChannel) byteChannel;
			seekable.position(seekable.position() - buffer.remaining() + count);
			buffer.position(0);
			buffer.limit(0);
			return;
		}

		// The slow way
		for (long remainingSkip = count; remainingSkip > 0;) {
			if (remainingSkip <= buffer.remaining()) {
				buffer.position(buffer.position() + (int) remainingSkip);
				return;
			}
			if (buffer.remaining() > 0) {
				remainingSkip -= buffer.remaining();
				buffer.position(buffer.limit());
			}
			if (remainingSkip > 0) {
				buffer.clear();
				if (byteChannel.read(buffer) == -1) {
					throw new EOFException("The required number of bytes (" + count + " ) isn't available");
				}
				buffer.flip();
			}
		}
	}

	/**
	 * Reads a string from the specified {@link ByteBuffer}. Will rotate the
	 * buffer as needed, so the whole string need not be available in the buffer
	 * at the start.
	 *
	 * @param buffer the {@link ByteBuffer} to read from.
	 * @param charset the {@link Charset} to use for conversion.
	 * @param byteLength the number of <i>bytes</i> (not characters) to read.
	 * @param nullTerminated if {@code true}, trailing {@code null} characters
	 *            will be stripped from the resulting {@link String}.
	 * @param returnNull if {@code true}, {@code null} will be returned instead
	 *            of an empty string.
	 * @return The resulting {@link String} or {@code null} if the string is
	 *         empty and {@code returnNull} is {@code true}.
	 * @throws IOException If an error occurs during the operation.
	 */
	protected String readString(
		ByteBuffer buffer,
		Charset charset,
		int byteLength,
		boolean nullTerminated,
		boolean returnNull
	) throws IOException {
		if (byteLength < 1) {
			return returnNull ? null : "";
		}
		byte[] bytes = new byte[byteLength];
		int pos = 0;
		for (int remainingBytes = byteLength; remainingBytes > 0;) {
			if (remainingBytes <= buffer.remaining()) {
				buffer.get(bytes, pos, remainingBytes);
				break;
			}
			if (buffer.remaining() > 0) {
				int available = buffer.remaining();
				remainingBytes -= available;
				buffer.get(bytes, pos, available);
				pos += available;
			}
			if (remainingBytes > 0) {
				buffer.clear();
				byteChannel.read(buffer);
				buffer.flip();
			}
		}
		if (nullTerminated) {
			for (int i = bytes.length; i > 0; i--) {
				if (bytes[i - 1] != 0) {
					return new String(bytes, 0, i, charset);
				}
			}
			return returnNull ? null : "";
		}

		return new String(bytes, charset);
	}

	/**
	 * Determines if the specified {@code METADATA_BLOCK_HEADER} indicates
	 * that this is the last {@code METADATA_BLOCK}.
	 *
	 * @param blockHeader the {@code METADATA_BLOCK_HEADER} to evaluate.
	 * @return {@code true} the last {@code METADATA_BLOCK} is indicated,
	 *         {@code false} otherwise.
	 */
	protected static boolean isLastBlock(byte blockHeader) {
		return blockHeader < 0;
	}

	/**
	 * This class represents a subset of {@code METADATA_BLOCK_STREAMINFO}.
	 *
	 * @author Nadahar
	 */
	protected static class StreamInfo {

		private final int sampleRate;
		private final int bitsPerSample;

		/**
		 * Creates a new instance using the specified values.
		 *
		 * @param sampleRate the number of samples per second.
		 * @param bitsPerSample the number of bits per sample.
		 */
		public StreamInfo(int sampleRate, int bitsPerSample) {
			this.sampleRate = sampleRate;
			this.bitsPerSample = bitsPerSample;
		}

		/**
		 * @return The number of samples per second.
		 */
		public int getSampleRate() {
			return sampleRate;
		}

		/**
		 * @return The number of bits per sample.
		 */
		public int getBitsPerSample() {
			return bitsPerSample;
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder
				.append("StreamInfo [sampleRate=").append(sampleRate)
				.append(", bitsPerSample=").append(bitsPerSample).append("]");
			return builder.toString();
		}
	}

	/**
	 * This class is a simple wrapper to be able to return both a
	 * {@link CueSheet} and the number of bytes read.
	 *
	 * @author Nadahar
	 */
	protected static class CueSheetResult {

		private final CueSheet cueSheet;
		private final int readLength;

		/**
		 * Creates a new instance using the specified values.
		 *
		 * @param cueSheet the {@link CueSheet} or {@code null}.
		 * @param readLength the number of bytes read from the buffer.
		 */
		public CueSheetResult(CueSheet cueSheet, int readLength) {
			this.cueSheet = cueSheet;
			this.readLength = readLength;
		}

		/**
		 * @return The {@link CueSheet} or {@code null}.
		 */
		public CueSheet getCueSheet() {
			return cueSheet;
		}

		/**
		 * @return The number of bytes read from the buffer.
		 */
		public int getReadLength() {
			return readLength;
		}
	}
}
