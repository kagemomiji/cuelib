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
package org.digitalmediaserver.cuelib.id3.io;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

// TODO Add option to not treat skipping as reading.

/**
 * Counts the number of bytes that are read or skipped. (Skipping is treated as
 * reading.) A read that returns -1 is not counted.
 *
 * @author jwbroek
 */
public class ByteCountInputStream extends FilterInputStream {

	private long bytesRead = 0;

	/**
	 * Instantiates a new byte count input stream.
	 *
	 * @param in the {@link InputStream}.
	 */
	public ByteCountInputStream(InputStream in) {
		super(in);
	}

	@Override
	public int read() throws IOException {
		int byteRead = in.read(); // Is all that super.read() does.
		if (byteRead >= 0) {
			bytesRead++;
		}
		return byteRead;
	}

	// read(byte[]) is implemented to call read(byte[],int,int), so no need to override.

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		int count = super.read(b, off, len);
		if (count > 0) {
			bytesRead += count;
		}
		return count;
	}

	@Override
	public long skip(long n) throws IOException {
		long bytesSkipped = super.skip(n);
		if (bytesSkipped > 0) {
			this.bytesRead += bytesSkipped;
		}
		return bytesSkipped;
	}

	/**
	 * Get the bytesRead of this ByteCountInputStream.
	 *
	 * @return The bytesRead of this ByteCountInputStream.
	 */
	public long getBytesRead() {
		return bytesRead;
	}

	/**
	 * Reset the number of bytes read count.
	 */
	public void resetBytesRead() {
		this.bytesRead = 0;
	}
}
