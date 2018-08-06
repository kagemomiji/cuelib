/*
 * Cuelib library for manipulating cue sheets.
 * Copyright (C) 2007-2008 Jan-Willem van den Broek
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
package org.digitalmediaserver.cuelib.util.properties;


/**
 * Method intended to be thrown when a {@link PropertyHandler} cannot convert to
 * or from a property.
 *
 * @author jwbroek
 */
public class CannotConvertPropertyException extends RuntimeException {

	/**
	 * Generated UID to comply with the contract of {@link Serializable}.
	 */
	private static final long serialVersionUID = -8158507550259008115L;

	/**
	 * Create a new CannotConvertPropertyException.
	 */
	public CannotConvertPropertyException() {
		super();
	}

	/**
	 * Create a new CannotConvertPropertyException.
	 *
	 * @param message the message.
	 */
	public CannotConvertPropertyException(String message) {
		super(message);
	}

	/**
	 * Create a new CannotConvertPropertyException.
	 *
	 * @param message the message.
	 * @param cause the {@link Throwable}.
	 */
	public CannotConvertPropertyException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Create a new CannotConvertPropertyException.
	 *
	 * @param cause the {@link Throwable}.
	 */
	public CannotConvertPropertyException(Throwable cause) {
		super(cause);
	}
}
