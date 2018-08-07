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
import org.digitalmediaserver.cuelib.id3.PictureFrame;
import org.digitalmediaserver.cuelib.id3.io.ByteCountInputStream;
import org.digitalmediaserver.cuelib.id3.util.FieldReader;
import org.digitalmediaserver.cuelib.util.Utils;


/**
 * The Class PICFrameReader.
 */
public class PICFrameReader implements FrameReader {

	private final int headerSize;
	private int imageTypeSize = -1; // -1 Stands for unlimited.

	/**
	 * Instantiates a new PIC frame reader.
	 *
	 * @param headerSize the header size
	 */
	public PICFrameReader(int headerSize) {
		this.headerSize = headerSize;
	}

	/**
	 * Instantiates a new PIC frame reader.
	 *
	 * @param headerSize the header size
	 * @param v2r00Mode the v 2 r 00 mode
	 */
	public PICFrameReader(int headerSize, @SuppressWarnings("unused") boolean v2r00Mode) {
		this.headerSize = headerSize;
		this.imageTypeSize = 3;
	}

	@Override
	public PictureFrame readFrameBody(
		int size,
		InputStream input
	) throws IOException, UnsupportedEncodingException, MalformedFrameException {
		try (ByteCountInputStream countingInput = new ByteCountInputStream(input)) {
			PictureFrame result = new PictureFrame();
			result.setTotalFrameSize(size + headerSize);

			int encoding = countingInput.read();

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

			if (this.imageTypeSize > 0) {
				result.setImageType(FieldReader.readField(countingInput, this.imageTypeSize, Charset.forName("ISO-8859-1")));
			} else {
				result.setImageType(FieldReader.readUntilNul(countingInput, size - 1, Charset.forName("ISO-8859-1")));
			}

			result.setPictureNumber(countingInput.read());

			// TODO Size is actually a maximum of 64 in 2.2 and 2.3.
			result.setDescription(FieldReader.readUntilNul(countingInput, size, charset));

			// Remainder of frame is data.
			byte[] imageData = new byte[size - (int) countingInput.getBytesRead()];
			Utils.readFully(countingInput, imageData);
			result.setImageData(imageData);

			return result;
		}
	}

}
