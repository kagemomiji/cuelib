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
import org.digitalmediaserver.cuelib.id3.CanonicalFrameType;
import org.digitalmediaserver.cuelib.id3.TextFrame;
import org.digitalmediaserver.cuelib.id3.util.FieldReader;


/**
 * The Class TextFrameReader.
 */
public class TextFrameReader implements FrameReader {

	private final CanonicalFrameType canonicalFrameType;
	private final int headerSize;

	/**
	 * Instantiates a new text frame reader.
	 *
	 * @param canonicalFrameType the canonical frame type
	 * @param headerSize the header size
	 */
	public TextFrameReader(CanonicalFrameType canonicalFrameType, int headerSize) {
		this.canonicalFrameType = canonicalFrameType;
		this.headerSize = headerSize;
	}

	@Override
	public TextFrame readFrameBody(
		int size,
		InputStream input
	) throws IOException, UnsupportedEncodingException {
		return this.readFrameBody(null, size, input);
	}

	/**
	 * Read frame body.
	 *
	 * @param additionalTypeInfo the additional type info
	 * @param size the size
	 * @param input the input
	 * @return the text frame
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws UnsupportedEncodingException the unsupported encoding exception
	 */
	public TextFrame readFrameBody(
		String additionalTypeInfo,
		int size,
		InputStream input
	) throws IOException, UnsupportedEncodingException {
		TextFrame result = new TextFrame(this.canonicalFrameType);

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

		result.setCharset(charset);

		// Size -1 because we have to count the byte used for encoding.
		result.setText(FieldReader.readField(input, size - 1, charset));
		result.setTotalFrameSize(size + headerSize);

		if (additionalTypeInfo != null) {
			result.setAdditionalTypeInfo(additionalTypeInfo);
		}

		return result;
	}
}
