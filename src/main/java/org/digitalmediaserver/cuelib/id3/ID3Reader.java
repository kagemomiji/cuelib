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
package org.digitalmediaserver.cuelib.id3;

import java.io.File;
import java.io.IOException;
import org.digitalmediaserver.cuelib.id3.v2.MalformedFrameException;
import org.digitalmediaserver.cuelib.id3.v2.UnsupportedEncodingException;


/**
 * The Interface ID3Reader.
 */
public interface ID3Reader {

	/**
	 * Checks for tag.
	 *
	 * @param file the {@link File}.
	 * @return {@code true} if the specified File has a tag, {@code false} otherwise.
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public boolean hasTag(File file) throws IOException;

	/**
	 * Read the specified {@link File}.
	 *
	 * @param file the {@link File}.
	 * @return the {@link ID3Tag}.
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws UnsupportedEncodingException If the encoding is unsupported.
	 * @throws MalformedFrameException If a malformed frame is encountered.
	 */
	public ID3Tag read(File file) throws IOException, UnsupportedEncodingException, MalformedFrameException;
}
