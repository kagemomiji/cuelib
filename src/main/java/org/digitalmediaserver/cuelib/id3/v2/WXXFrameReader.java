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
import org.digitalmediaserver.cuelib.id3.UserDefinedURLFrame;
import org.digitalmediaserver.cuelib.id3.util.FieldReader;


/**
 * The Class WXXFrameReader.
 */
public class WXXFrameReader implements FrameReader {

	private final int headerSize;

	/**
	 * Instantiates a new WXX frame reader.
	 *
	 * @param headerSize the header size
	 */
	public WXXFrameReader(int headerSize) {
		this.headerSize = headerSize;
	}

	@Override
	public UserDefinedURLFrame readFrameBody(
		int size,
		InputStream input
	) throws IOException, UnsupportedEncodingException, MalformedFrameException {
		UserDefinedURLFrame result = new UserDefinedURLFrame();
		result.setTotalFrameSize(size + this.headerSize);

		int encoding = input.read();

		Charset charset;
		int charLength;
		switch (encoding) {
			case 0:
				charset = StandardCharsets.ISO_8859_1;
				charLength = 1;
				break;
			case 1:
				charset = StandardCharsets.UTF_16;
				charLength = 2;
				break;
			case 2:
				// TODO Not supported until 2.4. Enable via option and throw exception otherwise.
				charset = StandardCharsets.UTF_16BE;
				charLength = 2;
				break;
			case 3:
				// TODO Not supported until 2.4. Enable via option and throw exception otherwise.
				charset = StandardCharsets.UTF_8;
				charLength = 1; // TODO Actually variable.
				break;
			default:
				throw new UnsupportedEncodingException("Encoding not supported: " + encoding);
		}
		result.setCharset(charset);

		// First read variable length field with user-defined encoding. Then
		// read rest of field with ISO-8859-1 encoding.
		// Size -1 because of the encoding byte.
		String description = FieldReader.readUntilNul(input, size - 1, charset);
		result.setDescription(description);
		// Length is what remains after the description and encoding byte. Length
		// of description is length in characters + 1 (for the nul) times the
		// character length.
		// TODO Fixme. Method doesn't work reliably for any of the UTF encodings.
		String url = FieldReader.readField(input, size - (description.length() + 1) * charLength - 1, charset);
		result.setUrl(url);

		return result;
	}
}
