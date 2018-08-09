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
import java.util.Collection;


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
	 * Evaluates if the specified character sequence is {@code null}, empty or
	 * only consists of whitespace.
	 *
	 * @param cs the {@link CharSequence} to evaluate.
	 * @return true if {@code cs} is {@code null}, empty or only consists of
	 *         whitespace, {@code false} otherwise.
	 */
	public static boolean isBlank(CharSequence cs) {
		if (cs == null) {
			return true;
		}
		int strLen = cs.length();
		if (strLen == 0) {
			return true;
		}
		for (int i = 0; i < strLen; i++) {
			if (!Character.isWhitespace(cs.charAt(i))) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Appends {@code ", "} to the specified {@link StringBuilder} only if
	 * {@code first} is false.
	 *
	 * @param sb the {@link StringBuilder} to append to.
	 * @param first the "first" flag.
	 * @return Always {@code false}.
	 */
	public static boolean appendSeparator(StringBuilder sb, boolean first) {
		return appendSeparator(sb, first, ", ");
	}

	/**
	 * Appends the specified separator sequence to the specified
	 * {@link StringBuilder} only if {@code first} is false.
	 *
	 * @param sb the {@link StringBuilder} to append to.
	 * @param first the "first" flag.
	 * @param separator the separator sequence.
	 * @return Always {@code false}.
	 */
	public static boolean appendSeparator(StringBuilder sb, boolean first, String separator) {
		if (!first && sb != null) {
			sb.append(separator);
		}
		return false;
	}

	/**
	 * Formats a {@link Collection} as a comma separated {@link String} wrapped
	 * in curly brackets, unless the specified {@link Collection} only contains
	 * one element, in which case only the element itself is returned.
	 *
	 * @param collection the {@link Collection}.
	 * @return The formatted {@link String}.
	 */
	public static String collectionToString(Collection<?> collection) {
		return collectionToString(collection, false, 0);
	}

	/**
	 * Formats a {@link Collection} as indented newline separated {@link String}
	 * unless the specified {@link Collection} only contains one element, in
	 * which case only the element itself is returned.
	 *
	 * @param collection the {@link Collection}.
	 * @param indent the level if {@code lines} is {@code true}.
	 * @return The formatted {@link String}.
	 */
	public static String collectionToString(Collection<?> collection, int indent) {
		return collectionToString(collection, true, indent);
	}

	/**
	 * Formats a {@link Collection} as a comma or indented newline separated
	 * {@link String}, unless the specified {@link Collection} only contains one
	 * element, in which case only the element itself is returned. If
	 * {@code lines} is {@code false} or there are zero elements, the result is
	 * wrapped in curly brackets,
	 *
	 * @param collection the {@link Collection}.
	 * @param lines if {@code true} a newline is inserted between each entry, if
	 *            {@code false} a comma is used.
	 * @param indent the level if {@code lines} is {@code true}.
	 * @return The formatted {@link String}.
	 */
	public static String collectionToString(Collection<?> collection, boolean lines, int indent) {
		if (collection == null) {
			return "Null";
		}
		if (collection.isEmpty()) {
			return "{}";
		}
		if (collection.size() == 1) {
			return collection.iterator().next().toString();
		}
		StringBuilder sb = new StringBuilder();
		String indentStr = null;
		if (lines) {
			if (indent > 0) {
				for (int i = 0; i < indent; i++) {
					sb.append(' ');
				}
				indentStr = sb.toString();
				sb.setLength(0);
			} else {
				indentStr = "";
			}
		} else {
			sb.append('{');
		}
		boolean first = true;
		for (Object object : collection) {
			if (lines) {
				sb.append('\n').append(indentStr);
			} else {
				first = appendSeparator(sb, first);
			}
			sb.append(object);
		}
		if (!lines) {
			sb.append('}');
		}
		return sb.toString();
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
