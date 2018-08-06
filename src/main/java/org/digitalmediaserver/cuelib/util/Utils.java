/*
 * Cuelib library for manipulating cue sheets.
 * Copyright (C) 2018 Digital Media Server
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
package org.digitalmediaserver.cuelib.util;

import java.io.DataInput;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;


/**
 * A utility class with a few utility methods.
 *
 * @author Nadahar
 */
public class Utils {

	/**
	 * Not to be instantiated.
	 */
	private Utils() {
	}

	/**
	 * Fully reads the target byte array or throws an {@link EOFException}.
	 *
	 * @param inputStream the {@link InputStream} to read.
	 * @param target the byte array to read to.
	 * @throws EOFException If the stream ends before the target byte array has
	 *             been filled.
	 * @throws IOException If an error occurs during the operation.
	 */
	public static void readFully(InputStream inputStream, byte[] target) throws IOException {
		if (inputStream == null) {
			return;
		}
		if (target == null) {
			throw new IllegalArgumentException("target cannot be null");
		}

		int count = 0;
		int length = target.length;
		while (count < length) {
			int read = inputStream.read(target, count, length - count);
			if (read == -1) {
				throw new EOFException("InputStream ended prematurely");
			}
			count += read;
		}
	}

	/**
	 * Skips the specified number of bytes or throws an {@link EOFException}.
	 *
	 * @param inputStream the {@link InputStream} to read.
	 * @param n the number of bytes to skip.
	 * @return The number of bytes skipped.
	 * @throws EOFException If the stream ends before the intended number of
	 *             bytes have been skipped.
	 * @throws IOException If an error occurs during the operation.
	 */
	public static long skipOrThrow(InputStream inputStream, long n) throws IOException {
		if (inputStream == null || n < 1) {
			return 0;
		}
		long result = inputStream.skip(n);

		if (result < n) {
			throw new EOFException("InputStream ended prematurely");
		}
		return result;
	}

	/**
	 * Skips the specified number of bytes or throws an {@link EOFException}.
	 *
	 * @param input the {@link DataInput} to read.
	 * @param n the number of bytes to skip.
	 * @return The number of bytes skipped.
	 * @throws EOFException If the input ends before the intended number of
	 *             bytes have been skipped.
	 * @throws IOException If an error occurs during the operation.
	 */
	public static int skipOrThrow(DataInput input, int n) throws IOException {
		if (input == null || n < 1) {
			return 0;
		}
		int result = input.skipBytes(n);

		if (result < n) {
			throw new EOFException("\"" + input + "\" ended prematurely");
		}
		return result;
	}
}
