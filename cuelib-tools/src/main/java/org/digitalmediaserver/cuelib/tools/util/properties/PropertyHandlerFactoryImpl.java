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
package org.digitalmediaserver.cuelib.tools.util.properties;

import java.io.File;
import javax.sound.sampled.AudioFileFormat;


/**
 * Implementation of PropertyHandlerFactory that supports the various
 * PropertyHandlers in jwbroek.util.properties.
 *
 * @author jwbroek
 */
public class PropertyHandlerFactoryImpl implements PropertyHandlerFactory {

	private static final long serialVersionUID = 1L;

	/**
	 * The singleton instance of this class.
	 */
	private static final PropertyHandlerFactoryImpl INSTANCE = new PropertyHandlerFactoryImpl();

	/**
	 * This constructor is only meant to be called by PropertyHandlerFactoryImpl
	 * itself, as PropertyHandlerFactoryImpl is a singleton class.
	 */
	private PropertyHandlerFactoryImpl() {
		super();
	}

	/**
	 * Get an instance of PropertyHandlerFactoryImpl.
	 *
	 * @return An instance of PropertyHandlerFactoryImpl.
	 */
	public static PropertyHandlerFactoryImpl getInstance() {
		return PropertyHandlerFactoryImpl.INSTANCE;
	}

	/**
	 * Get a PropertyHandler for the specified type.
	 *
	 * @param propertyType the property type {@link Class}.
	 * @return A PropertyHandler for the specified type.
	 * @throws UnsupportedOperationException When the specified type is not
	 *             supported by this factory.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T> PropertyHandler<T> getPropertyHandler(Class<T> propertyType) throws UnsupportedOperationException {

		// Unsafe operation, but there is no way around this (apart from doing lots of safe casts in the "if" blocks).
		if (propertyType.equals(AudioFileFormat.Type.class)) {
			return (PropertyHandler<T>) AudioFileFormatTypePropertyHandler.getInstance();
		} else if (propertyType.equals(Boolean.class)) {
			return (PropertyHandler<T>) BooleanPropertyHandler.getInstance();
		} else if (propertyType.equals(File.class)) {
			return (PropertyHandler<T>) FilePropertyHandler.getInstance();
		} else if (propertyType.equals(Long.class)) {
			return (PropertyHandler<T>) LongPropertyHandler.getInstance();
		} else {
			throw new UnsupportedOperationException("Unsupported type: '" + propertyType.toString() + "'");
		}
	}

}
