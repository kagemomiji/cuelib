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
import org.digitalmediaserver.cuelib.id3.CommentFrame;
import org.digitalmediaserver.cuelib.util.Utils;


/**
 * The Class COMFrameReader.
 */
public class COMFrameReader implements FrameReader {

	private final int headerSize;

	/**
	 * Instantiates a new COM frame reader.
	 *
	 * @param headerSize the header size.
	 */
	public COMFrameReader(int headerSize) {
		this.headerSize = headerSize;
	}

	@Override
	public CommentFrame readFrameBody(
		int size,
		InputStream input
	) throws IOException, UnsupportedEncodingException, MalformedFrameException {
		CommentFrame result = new CommentFrame();

		int encoding = input.read();

		Charset charset;
		switch (encoding) {
			case 0:
				charset = Charset.forName("ISO-8859-1");
				break;
			case 1:
				charset = Charset.forName("UTF-16");
				break;
			case 2:
				// TODO Not supported until 2.4. Enable via option and throw exception otherwise.
				charset = Charset.forName("UTF-16BE");
				break;
			case 3:
				// TODO Not supported until 2.4. Enable via option and throw exception otherwise.
				charset = Charset.forName("UTF-8");
				break;
			default:
				throw new UnsupportedEncodingException("Encoding not supported: " + encoding);
		}

		result.setCharset(charset);

		StringBuilder languageBuilder = new StringBuilder();
		languageBuilder.append((char) input.read()).append((char) input.read()).append((char) input.read());

		// Read entire field, then process.
		// Length -4 because of the encoding byte and 3 language bytes.
		byte[] b = new byte[size - 4];
		Utils.readFully(input, b);
		String rawResult = new String(b, charset);
		int nulPosition = rawResult.indexOf(0);
		if (nulPosition < 0) {
			throw new MalformedFrameException("Description not terminated in COM frame.");
		}
		String description = rawResult.substring(0, nulPosition);
		String rawText = rawResult.substring(nulPosition + 1);
		nulPosition = rawText.indexOf(0);
		String value = rawText.substring(0, (nulPosition == -1) ? rawText.length() : nulPosition);
		result.setLanguageCode(languageBuilder.toString());
		result.setDescription(description);
		result.setText(value);
		result.setTotalFrameSize(size + headerSize);

		return result;
	}
}
