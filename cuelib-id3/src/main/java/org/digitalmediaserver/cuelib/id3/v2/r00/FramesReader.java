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
package org.digitalmediaserver.cuelib.id3.v2.r00;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import org.digitalmediaserver.cuelib.id3.CanonicalFrameType;
import org.digitalmediaserver.cuelib.id3.ID3Tag;
import org.digitalmediaserver.cuelib.id3.v2.COMFrameReader;
import org.digitalmediaserver.cuelib.id3.v2.FrameReader;
import org.digitalmediaserver.cuelib.id3.v2.IPLFrameReader;
import org.digitalmediaserver.cuelib.id3.v2.ITunesPodcastFrameReader;
import org.digitalmediaserver.cuelib.id3.v2.MCIFrameReader;
import org.digitalmediaserver.cuelib.id3.v2.MalformedFrameException;
import org.digitalmediaserver.cuelib.id3.v2.PICFrameReader;
import org.digitalmediaserver.cuelib.id3.v2.TXXFrameReader;
import org.digitalmediaserver.cuelib.id3.v2.TextFrameReader;
import org.digitalmediaserver.cuelib.id3.v2.UFIFrameReader;
import org.digitalmediaserver.cuelib.id3.v2.URLFrameReader;
import org.digitalmediaserver.cuelib.id3.v2.UnsupportedEncodingException;
import org.digitalmediaserver.cuelib.id3.v2.WXXFrameReader;
import org.digitalmediaserver.cuelib.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The Class FramesReader.
 */
public class FramesReader {

	private static final Logger LOGGER = LoggerFactory.getLogger(FramesReader.class);

	// TODO Make sure we can handle unexpected EOFs.

	private static FrameDictionary frameDictionary = new FrameDictionary();
	private static Map<String, FrameReader> frameReaders = new HashMap<String, FrameReader>();
	private static final int FRAME_HEADER_LENGTH = 6;

	private static void putTextFrameReader(String frameName) {
		frameReaders.put(frameName, new TextFrameReader(
			frameDictionary.getCanonicalFrameType(frameName),
			FramesReader.FRAME_HEADER_LENGTH
		));
	}

	private static void putURLFrameReader(String frameName) {
		frameReaders.put(frameName, new URLFrameReader(frameDictionary.getCanonicalFrameType(frameName), FramesReader.FRAME_HEADER_LENGTH));
	}

	static {

		frameReaders.put("UFI", new UFIFrameReader(FramesReader.FRAME_HEADER_LENGTH));

		FramesReader.putTextFrameReader("TT1");
		FramesReader.putTextFrameReader("TT2");
		FramesReader.putTextFrameReader("TT3");

		FramesReader.putTextFrameReader("TP1");
		FramesReader.putTextFrameReader("TP2");
		FramesReader.putTextFrameReader("TP3");
		FramesReader.putTextFrameReader("TP4");

		// Composers separated by "/".
		FramesReader.putTextFrameReader("TCM");

		// Textwriters separated by "/".
		FramesReader.putTextFrameReader("TXT");

		FramesReader.putTextFrameReader("TLA");

		// Effectively genre. Free text, but you can also reference an ID3v1
		// genre by encapsulating it in parenthesis, such as (31). You can
		// refine this adding data, such as (4)Eurodisco. If you want a
		// parenthesis in your refinement, use a double opening parenthesis,
		// such as (31)((I think).
		FramesReader.putTextFrameReader("TCO");

		// Unofficial
		// TODO Make switch for this.
		FramesReader.putTextFrameReader("TDS");

		FramesReader.putTextFrameReader("TAL");

		// Effectively disc number. Can use "/" to include total. I.e. 1/3.
		FramesReader.putTextFrameReader("TPA");

		// Can use "/" to include total. I.e. 3/12.
		FramesReader.putTextFrameReader("TRK");

		FramesReader.putTextFrameReader("TRC");

		FramesReader.putTextFrameReader("TYE");

		// Should always be 4 characters long, and of format DDMM.
		FramesReader.putTextFrameReader("TDA");

		// Should always be 4 characters long, and of format HHMM.
		FramesReader.putTextFrameReader("TIM");

		FramesReader.putTextFrameReader("TRD");

		// Are a bunch of predefined values. Those are in parentheses.
		FramesReader.putTextFrameReader("TMT");

		// Default is "MPG". Are a bunch of predefined values. They are not
		// in parentheses.
		FramesReader.putTextFrameReader("TFT");

		// Integer.
		FramesReader.putTextFrameReader("TBP");

		// For the original, not this audio file. Must begin with a year and
		// a space.
		FramesReader.putTextFrameReader("TCR");

		FramesReader.putTextFrameReader("TPB");

		FramesReader.putTextFrameReader("TEN");

		FramesReader.putTextFrameReader("TSS");

		FramesReader.putTextFrameReader("TOF");

		// Numeric
		FramesReader.putTextFrameReader("TLE");

		// Numeric. Excludes ID3 tag.
		FramesReader.putTextFrameReader("TSI");

		// Numeric. Delay in ms between tracks in playlist.
		FramesReader.putTextFrameReader("TDY");

		// The ground keys are represented with "A","B","C","D","E",
		// "F" and "G" and halfkeys represented with "b" and "#". Minor is
		// represented as "m". Example "Cbm". Off key is represented with an "o"
		// only.
		FramesReader.putTextFrameReader("TKE");

		FramesReader.putTextFrameReader("TOT");

		// Artists separated by "/".
		FramesReader.putTextFrameReader("TOA");

		// Textwriters separated by "/".
		FramesReader.putTextFrameReader("TOL");

		// As per TYE.
		FramesReader.putTextFrameReader("TOR");

		// TODO Make switch for this.
		// Unofficial.
		FramesReader.putTextFrameReader("TID");

		// TODO Make switch for this.
		// Unofficial.
		FramesReader.putTextFrameReader("TCT");

		// TODO Make switch for this.
		// Unofficial.
		FramesReader.putTextFrameReader("TDR");

		// TODO Make switch for this.
		// Unofficial.
		FramesReader.putTextFrameReader("TKW");

		// User defined text. Must be only one of these per description.
		frameReaders.put("TXX", new TXXFrameReader(FramesReader.FRAME_HEADER_LENGTH));

		FramesReader.putURLFrameReader("WAF");

		// May be more than one if there is more than one artist.
		FramesReader.putURLFrameReader("WAR");

		FramesReader.putURLFrameReader("WAS");

		FramesReader.putURLFrameReader("WCM");

		FramesReader.putURLFrameReader("WCP");

		FramesReader.putURLFrameReader("WPB");

		// TODO Make switch for this.
		// Unofficial. Name and purpose suggest that its a URL frame, but it's actually a text frame
		// as it contains an encoding byte.
		FramesReader.putTextFrameReader("WFD");

		// User defined URL. Must be only one of these per description.
		frameReaders.put("WXX", new WXXFrameReader(FramesReader.FRAME_HEADER_LENGTH));

		frameReaders.put("IPL", new IPLFrameReader(FramesReader.FRAME_HEADER_LENGTH));

		// Can only be one, and requires present and valid TRK frame.
		frameReaders.put("MCI", new MCIFrameReader(FramesReader.FRAME_HEADER_LENGTH));

		// Can be only one.
		// TODO ETC

		// TODO MLL

		// TODO STC

		// May contain newlines.
		// TODO ULT

		// May contain newlines.
		// TODO SLT

		// Must be only one per language and content description pair. May contain newlines.
		frameReaders.put("COM", new COMFrameReader(FramesReader.FRAME_HEADER_LENGTH));

		// TODO RVA

		// TODO EQU

		// TODO REV

		// Must be only one per description pair. Also only one per icon type allowed.
		frameReaders.put("PIC", new PICFrameReader(FramesReader.FRAME_HEADER_LENGTH, true));

		// TODO GEO

		// TODO CNT

		// TODO POP

		// TODO BUF

		// TODO CRM

		// TODO CRA

		// TODO LNK

		// TODO Make switch for this.
		// Unofficial.
		frameReaders.put("PCS", new ITunesPodcastFrameReader(FramesReader.FRAME_HEADER_LENGTH));
	}

	/**
	 * Read next frame.
	 *
	 * @param tag the tag
	 * @param input the input
	 * @return the int
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws UnsupportedEncodingException the unsupported encoding exception
	 * @throws MalformedFrameException the malformed frame exception
	 */
	public int readNextFrame(ID3Tag tag, InputStream input) throws IOException, UnsupportedEncodingException,
		MalformedFrameException {
		StringBuilder frameNameBuilder = new StringBuilder(3);
		frameNameBuilder.append((char) input.read());
		frameNameBuilder.append((char) input.read());
		frameNameBuilder.append((char) input.read());
		String frameName = frameNameBuilder.toString();
		int frameSize = input.read() * 65536 + input.read() * 256 + input.read();
		FrameReader reader = FramesReader.frameReaders.get(frameName);
		if (reader == null) {
			if ("\u0000\u0000\u0000".equals(frameName)) {
				// End of frames.
				return FramesReader.FRAME_HEADER_LENGTH;
			} else if (frameName.charAt(0) == 'T') {
				// TODO: Add option to enable/disable this behavior.
				LOGGER.warn("Encountered unknown text frame: \"{}\"", frameName);
				tag.getFrames().add(
					new TextFrameReader(
						CanonicalFrameType.USER_DEFINED_TEXT,
						FramesReader.FRAME_HEADER_LENGTH).readFrameBody(frameName, frameSize, input)
					);
			} else if (frameName.charAt(0) == 'W') {
				// TODO: Add option to enable/disable this behavior.
				LOGGER.warn("Encountered unknown URL frame: \"{}\"", frameName);
				tag.getFrames().add(
					new URLFrameReader(
						CanonicalFrameType.USER_DEFINED_URL,
						FramesReader.FRAME_HEADER_LENGTH).readFrameBody(frameName, frameSize, input)
					);
			} else {
				LOGGER.warn("Encountered unsupported frame type: \"{}\" of length {}", frameName, frameSize);
				Utils.skipOrThrow(input, frameSize);
				// TODO Handle
			}
		} else {
			tag.getFrames().add(reader.readFrameBody(frameSize, input));
		}
		return frameSize + FramesReader.FRAME_HEADER_LENGTH; // Size + header size.
	}

	/**
	 * Read frames.
	 *
	 * @param tag the {@link ID3Tag}.
	 * @param input the {@link InputStream}.
	 * @param length the length.
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws UnsupportedEncodingException the unsupported encoding exception.
	 * @throws MalformedFrameException the malformed frame exception.
	 */
	public void readFrames(
		ID3Tag tag,
		InputStream input,
		int length
	) throws IOException, UnsupportedEncodingException, MalformedFrameException {
		int bytesLeft = length;
		while (bytesLeft >= FramesReader.FRAME_HEADER_LENGTH) {
			int bytesRead = readNextFrame(tag, input);
			bytesLeft -= bytesRead;
			if (bytesRead == FramesReader.FRAME_HEADER_LENGTH) {
				Utils.skipOrThrow(input, bytesLeft);
				bytesLeft = 0;
			}
		}
		Utils.skipOrThrow(input, bytesLeft);
	}
}
