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
import org.digitalmediaserver.cuelib.id3.ID3Frame;


/**
 * The Interface FrameReader.
 */
public interface FrameReader {

	/**
	 * Read frame body.
	 *
	 * @param size the size.
	 * @param input the {@link InputStream}.
	 * @return the {@link ID3Frame}.
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws UnsupportedEncodingException If the encoding isn't supported.
	 * @throws MalformedFrameException If a malformed frame were encountered.
	 */
	public ID3Frame readFrameBody(
		int size,
		InputStream input
	) throws IOException, UnsupportedEncodingException, MalformedFrameException;
}
