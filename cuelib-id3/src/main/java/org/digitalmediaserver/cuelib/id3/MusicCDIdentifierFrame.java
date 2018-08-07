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

import java.util.Properties;


/**
 * The Class MusicCDIdentifierFrame.
 */
public class MusicCDIdentifierFrame implements ID3Frame {

	// TODO Change to byte array?
	private String hexTOC;
	private int totalFrameSize;
	private Properties flags = new Properties();

	/**
	 * @return the flags
	 */
	@Override
	public Properties getFlags() {
		return flags;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder
			.append("Music CD Identifier frame: [").append(this.totalFrameSize).append("]\n")
			.append("Flags: ").append(flags).append('\n')
			.append("Identifier: ").append(hexTOC);
		return builder.toString();
	}

	/**
	 * @return the declaredSize
	 */
	@Override
	public int getTotalFrameSize() {
		return totalFrameSize;
	}

	/**
	 * @param totalFrameSize the totalFrameSize to set
	 */
	public void setTotalFrameSize(int totalFrameSize) {
		this.totalFrameSize = totalFrameSize;
	}

	/**
	 * Gets the hex TOC.
	 *
	 * @return the hex TOC
	 */
	public String getHexTOC() {
		return this.hexTOC;
	}

	/**
	 * Sets the hex TOC.
	 *
	 * @param hexTOC the new hex TOC
	 */
	public void setHexTOC(String hexTOC) {
		this.hexTOC = hexTOC;
	}

	@Override
	public CanonicalFrameType getCanonicalFrameType() {
		return CanonicalFrameType.MUSIC_CD_IDENTIFIER;
	}
}
