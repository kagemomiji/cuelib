/*
 * Cuelib library for manipulating cue sheets.
 * Copyright (C) 2007-2009 Jan-Willem van den Broek
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
package org.digitalmediaserver.cuelib.id3.v1;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import org.digitalmediaserver.cuelib.id3.CanonicalFrameType;
import org.digitalmediaserver.cuelib.id3.ID3Reader;
import org.digitalmediaserver.cuelib.id3.ID3Tag;
import org.digitalmediaserver.cuelib.id3.ID3Version;
import org.digitalmediaserver.cuelib.id3.TextFrame;
import org.digitalmediaserver.cuelib.util.Utils;


/**
 * The Class ID3v1Reader.
 */
public class ID3v1Reader implements ID3Reader {

	@Override
	public boolean hasTag(File file) throws IOException {
		try (RandomAccessFile input = new RandomAccessFile(file, "r")) {
			if (input.length() >= 128) {
				input.seek(input.length() - 128);
				if (
					input.readUnsignedByte() == 'T' &&
					input.readUnsignedByte() == 'A' &&
					input.readUnsignedByte() == 'G'
				) {
					return true;
				}
			}
			return false;
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see jwbroek.id3.ID3Reader#read(java.io.File)
	 */
	@Override
	public ID3Tag read(File file) throws IOException {
		ID3Tag tag = new ID3Tag();
		try (RandomAccessFile input = new RandomAccessFile(file, "r")) {
			if (input.length() >= 128) {
				input.seek(input.length() - 128);
				if (
					input.readUnsignedByte() == 'T' &&
					input.readUnsignedByte() == 'A' &&
					input.readUnsignedByte() == 'G'
				) {
					tag.setVersion(ID3Version.ID3v1r0);
					// TODO Don't create frame if field is empty?
					tag.getFrames().add(new TextFrame(CanonicalFrameType.TITLE, ID3v1Reader.getField(input, 30), 30));
					tag.getFrames().add(new TextFrame(CanonicalFrameType.PERFORMER, ID3v1Reader.getField(input, 30), 30));
					tag.getFrames().add(new TextFrame(CanonicalFrameType.ALBUM, ID3v1Reader.getField(input, 30), 30));
					tag.getFrames().add(new TextFrame(CanonicalFrameType.YEAR, ID3v1Reader.getField(input, 4), 4));
					// Remember as we may extract a track number from it.
					TextFrame commentFrame = new TextFrame(CanonicalFrameType.COMMENT, ID3v1Reader.getField(input, 30), 30);
					tag.getFrames().add(commentFrame);
					int rawGenre = input.readUnsignedByte();
					if (rawGenre != 0) {
						// TODO Perhaps a message indicating that genre was not set, if this is the case.
						// TODO Genre is in different form than is the case for v2 tags. Normalise somehow.
						tag.getFrames().add(new TextFrame(CanonicalFrameType.CONTENT_TYPE, "" + rawGenre, 1));
					}
					// ID3 1.1 extension.
					input.seek(input.length() - 3);
					int trackNoMarker = input.readUnsignedByte();
					int rawTrackNo = input.readUnsignedByte();
					if (trackNoMarker == 0) {
						if (rawTrackNo != 0) {
							// TODO Track no is in different form than is the case for v2 tags. Normalise somehow.
							tag.getFrames().add(new TextFrame(CanonicalFrameType.TRACK_NO, "" + rawTrackNo, 1));
							// Comment actually size 28.
							commentFrame.setTotalFrameSize(28);
							tag.setVersion(ID3Version.ID3v1r1);
						}
					}
				} else {
					// Not a valid ID3v1 tag.
					tag = null;
				}
			} else {
				// File too small to contain ID3v1 data.
				tag = null;
			}
		}

		return tag;
	}

	/**
	 * Get a field.
	 *
	 * @param input the {@link RandomAccessFile}.
	 * @param length the length.
	 * @return the field.
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static String getField(RandomAccessFile input, int length) throws IOException {
		StringBuffer result = new StringBuffer();
		for (int index = 0; index < length; index++) {
			int i = input.readUnsignedByte();

			if (i == 0) {
				// End of buffer.
				Utils.skipOrThrow(input, length - index - 1);
				break;
			}
			result.append((char) i);
		}
		// TODO remove trailing spaces if desired.
		return result.toString();
	}
}
