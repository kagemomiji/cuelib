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
package org.digitalmediaserver.cuelib.id3.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import org.digitalmediaserver.cuelib.util.Utils;


/**
 * The utility class FieldReader.
 */
public class FieldReader {

	private FieldReader() {
		// No need to instantiate.
	}

	/**
	 * Read until null.
	 *
	 * @param input the {@link InputStream}.
	 * @param length the length.
	 * @param charset the {@link Charset}.
	 * @return The read {@link String}.
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static String readUntilNul(InputStream input, int length, Charset charset) throws IOException {
		boolean singleNul;
		if (charset.equals(Charset.forName("ISO-8859-1"))) {
			// Nul is reliably identified as a single 0 byte.
			singleNul = true;
		} else if (charset.equals(Charset.forName("UTF-16"))) {
			// Nul is reliably identified as two 0 bytes. (This encoding can also be read
			// in 2-byte chunks without missing the nul.)
			singleNul = false;
		} else if (charset.equals(Charset.forName("UTF-16BE"))) {
			// Nul is reliably identified as two 0 bytes. (This encoding can also be read
			// in 2-byte chunks without missing the nul.)
			singleNul = false;
		} else if (charset.equals(Charset.forName("UTF-8"))) {
			// Nul is reliably identified as a single 0 byte.
			singleNul = true;
		} else {
			throw new IllegalArgumentException("Encoding not supported: " + charset.toString());
		}

		byte[] b = new byte[length];
		int previousValue = -1;

		for (int index = 0; index < length; index++) {
			byte currentValue = (byte) input.read();
			if (currentValue == 0 && (singleNul || previousValue == 0)) {
				int bytesUntilNul = index - (singleNul ? 0 : 1);
				return new String(b, 0, bytesUntilNul, charset);
			}
			b[index] = currentValue;
			previousValue = currentValue;
		}
		return new String(b, charset);
	}

	/**
	 * Read a field.
	 *
	 * @param input the {@link InputStream}.
	 * @param length the length.
	 * @param charset the {@link Charset}.
	 * @return The read {@link String}.
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static String readField(InputStream input, int length, Charset charset) throws IOException {
		// Read entire field, but throw away everything after first nul character.
		byte[] b = new byte[length];
		Utils.readFully(input, b);
		String rawResult = new String(b, charset);
		int nulPosition = rawResult.indexOf(0);
		return rawResult.substring(0, (nulPosition == -1) ? rawResult.length() : nulPosition);
	}
}
