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


/**
 * The Enum ID3Version.
 */
public enum ID3Version {

	/** The ID3 v1. */
	ID3v1(1, 0),

	/** The ID3 v1r0. */
	ID3v1r0(1, 0),

	/** The ID3 v1r1. */
	ID3v1r1(1, 1),

	/** The ID3 v2. */
	ID3v2(2, 0),

	/** The ID3 v2r0. */
	ID3v2r0(2, 0),

	/** The ID3 v2r2. */
	ID3v2r2(2, 2),

	/** The ID3 v2r3. */
	ID3v2r3(2, 3),

	/** The ID3 v2r4. */
	ID3v2r4(3, 3);

	private int majorVersion;
	private int minorVersion;

	/**
	 * Instantiates a new ID 3 version.
	 *
	 * @param majorVersion the major version
	 * @param minorVersion the minor version
	 */
	ID3Version(int majorVersion, int minorVersion) {
		this.majorVersion = majorVersion;
		this.minorVersion = minorVersion;
	}

	/**
	 * Gets the major version.
	 *
	 * @return the major version
	 */
	public int getMajorVersion() {
		return this.majorVersion;
	}

	/**
	 * Gets the minor version.
	 *
	 * @return the minor version
	 */
	public int getMinorVersion() {
		return this.minorVersion;
	}

	// TODO Provide better ordering.
}
