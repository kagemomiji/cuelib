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
package org.digitalmediaserver.cuelib.id3.v2;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import org.digitalmediaserver.cuelib.id3.InvolvedPeopleFrame;
import org.digitalmediaserver.cuelib.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The Class IPLFrameReader.
 */
public class IPLFrameReader implements FrameReader {

	private static final Logger LOGGER = LoggerFactory.getLogger(IPLFrameReader.class);

	private final int headerSize;

	/**
	 * Instantiates a new IPL frame reader.
	 *
	 * @param headerSize the header size
	 */
	public IPLFrameReader(int headerSize) {
		this.headerSize = headerSize;
	}

	@Override
	public InvolvedPeopleFrame readFrameBody(
		int size,
		InputStream input
	) throws IOException, UnsupportedEncodingException, MalformedFrameException {
		InvolvedPeopleFrame result = new InvolvedPeopleFrame(this.headerSize + size);
		int encoding = input.read();

		Charset charset;
		switch (encoding) {
			case 0:
				charset = StandardCharsets.ISO_8859_1;
				break;
			case 1:
				charset = StandardCharsets.UTF_16;
				break;
			case 2:
				// TODO Not supported until 2.4. Enable via option and throw exception otherwise.
				charset = StandardCharsets.UTF_16BE;
				break;
			case 3:
				// TODO Not supported until 2.4. Enable via option and throw exception otherwise.
				charset = StandardCharsets.UTF_8;
				break;
			default:
				throw new UnsupportedEncodingException("Encoding not supported: " + encoding);
		}
		LOGGER.trace("Reading InvolvedPeopleFrame with charset {}", charset);

		result.setCharset(charset);

		// Read entire field, then process.
		// Length -1 because of the encoding byte.
		byte[] b = new byte[size - 1];
		Utils.readFully(input, b);
		String rawValue = new String(b, charset);

		int startPosition = 0;
		boolean atInvolvement = true;
		InvolvedPeopleFrame.InvolvedPerson involvedPerson = null;
		while (startPosition < rawValue.length()) {
			int nulPosition = rawValue.indexOf(0, startPosition);
			int endPosition = (nulPosition == -1) ? rawValue.length() : nulPosition;
			String value = rawValue.substring(startPosition, endPosition);
			if (atInvolvement) {
				involvedPerson = new InvolvedPeopleFrame.InvolvedPerson();
				involvedPerson.setInvolvement(value);
				atInvolvement = false;
			} else if (involvedPerson != null) {
				involvedPerson.setInvolvee(value);
				result.getInvolvedPeopleList().add(involvedPerson);
				involvedPerson = null;
				atInvolvement = true;
			}
			// +1 because we don't want the nul character.
			startPosition = endPosition + 1;
		}

		if (involvedPerson != null) {
			// Involvement without involvee found.
			LOGGER.warn("Encountered ID3v2 involvement ({}) without involvee", involvedPerson.getInvolvement());
			involvedPerson.setInvolvee("");
			result.getInvolvedPeopleList().add(involvedPerson);
		}

		return result;
	}
}
