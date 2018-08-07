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
import org.digitalmediaserver.cuelib.id3.CanonicalFrameType;
import org.digitalmediaserver.cuelib.id3.URLFrame;
import org.digitalmediaserver.cuelib.id3.util.FieldReader;


/**
 * The Class URLFrameReader.
 */
public class URLFrameReader implements FrameReader {

	private final CanonicalFrameType canonicalFrameType;
	private final int headerSize;

	/**
	 * Instantiates a new URL frame reader.
	 *
	 * @param canonicalFrameType the canonical frame type
	 * @param headerSize the header size
	 */
	public URLFrameReader(CanonicalFrameType canonicalFrameType, int headerSize) {
		this.canonicalFrameType = canonicalFrameType;
		this.headerSize = headerSize;
	}

	@Override
	public URLFrame readFrameBody(
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
	 * @return the URL frame
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws UnsupportedEncodingException the unsupported encoding exception
	 */
	public URLFrame readFrameBody(
		String additionalTypeInfo,
		int size,
		InputStream input
	) throws IOException, UnsupportedEncodingException {
		URLFrame result = new URLFrame(this.canonicalFrameType);
		result.setTotalFrameSize(size + this.headerSize);
		// Read encoding. Should not be there officially.
		input.read();
		result.setUrl(FieldReader.readField(input, size, Charset.forName("ISO-8859-1")));

		if (additionalTypeInfo != null) {
			result.setAdditionalTypeInfo(additionalTypeInfo);
		}
		return result;
	}
}
