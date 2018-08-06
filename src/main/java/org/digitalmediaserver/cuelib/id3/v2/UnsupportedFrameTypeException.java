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


/**
 * An {@link Exception} that indicates an unsupported frame type.
 */
public class UnsupportedFrameTypeException extends RuntimeException {

	private static final long serialVersionUID = -1545143968507887751L;

	/**
	 * Instantiates a new unsupported frame type exception.
	 */
	public UnsupportedFrameTypeException() {
		super();
	}

	/**
	 * Instantiates a new unsupported frame type exception.
	 *
	 * @param e the {@link Exception}.
	 */
	public UnsupportedFrameTypeException(final Exception e) {
		super(e);
	}

	/**
	 * Instantiates a new unsupported frame type exception.
	 *
	 * @param message the message.
	 */
	public UnsupportedFrameTypeException(final String message) {
		super(message);
	}

	/**
	 * Instantiates a new unsupported frame type exception.
	 *
	 * @param message the message.
	 * @param e the {@link Exception}.
	 */
	public UnsupportedFrameTypeException(final String message, final Exception e) {
		super(message, e);
	}
}
