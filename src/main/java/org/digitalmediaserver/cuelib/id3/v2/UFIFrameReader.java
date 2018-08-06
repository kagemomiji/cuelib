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
import java.util.ArrayList;
import java.util.List;
import org.digitalmediaserver.cuelib.id3.UniqueFileIdentifierFrame;


/**
 * The Class UFIFrameReader.
 */
public class UFIFrameReader implements FrameReader {

	private final int headerSize;

	/**
	 * Instantiates a new UFI frame reader.
	 *
	 * @param headerSize the header size
	 */
	public UFIFrameReader(int headerSize) {
		this.headerSize = headerSize;
	}

	@Override
	public UniqueFileIdentifierFrame readFrameBody(
		int size,
		InputStream input
	) throws IOException, UnsupportedEncodingException {
		UniqueFileIdentifierFrame result = new UniqueFileIdentifierFrame();
		result.setTotalFrameSize(size + this.headerSize);

		StringBuilder owner = new StringBuilder();
		List<Integer> identifier = new ArrayList<Integer>();
		boolean haveNul = false;

		for (int index = 0; index < size; index++) {
			int i = input.read();
			if (haveNul) {
				identifier.add(i);
			} else {
				if (i == 0) {
					haveNul = true;
				} else {
					owner.append((char) i);
				}
			}
		}
		result.setOwnerIdentifier(owner.toString());

		StringBuilder hexIdentifier = new StringBuilder();
		for (Integer i : identifier) {
			hexIdentifier.append(Integer.toHexString(i));
		}
		result.setHexIdentifier(hexIdentifier.toString());

		return result;
	}
}
