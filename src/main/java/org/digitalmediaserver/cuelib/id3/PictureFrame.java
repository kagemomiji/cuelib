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

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;


/**
 * The Class PictureFrame.
 */
public class PictureFrame implements ID3Frame {

	private int totalFrameSize;
	private Charset charset = Charset.forName("ISO-8859-1");
	private Properties flags = new Properties();
	private PictureType pictureType = PictureType.OTHER;
	private int pictureNumber = pictureType.getNumber();
	private String description;
	// "PNG", "JPG" for 2.0, MIME for later version. Can also be "-->", in which case the payload is a hyperlink to the image.
	// TODO Tidy up for usecase when hyperlink is used.
	private String imageType;
	private byte[] imageData;

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Picture frame: [").append(this.totalFrameSize).append("] ").append(this.charset.toString()).append('\n')
			.append("Flags: ").append(this.flags.toString()).append('\n').append("Type: ").append(this.pictureType.toString()).append(" (")
			.append(this.pictureNumber).append(")\n").append("Format: ").append(this.imageType).append(")\n").append("Description: ")
			.append(this.description);
		return builder.toString();
	}

	/**
	 * Sets the picture type.
	 *
	 * @param pictureType the new picture type
	 */
	public void setPictureType(PictureType pictureType) {
		this.pictureType = pictureType;
		this.pictureNumber = pictureType.getNumber();
	}

	/**
	 * Gets the picture number.
	 *
	 * @return the picture number
	 */
	public int getPictureNumber() {
		return this.pictureNumber;
	}

	/**
	 * Sets the picture number.
	 *
	 * @param number the new picture number
	 */
	public void setPictureNumber(int number) {
		this.pictureNumber = number;
		this.pictureType = PictureType.getPictureType(number);
	}

	@Override
	public CanonicalFrameType getCanonicalFrameType() {
		return CanonicalFrameType.PICTURE;
	}

	@Override
	public Properties getFlags() {
		return this.flags;
	}

	@Override
	public int getTotalFrameSize() {
		return this.totalFrameSize;
	}

	/**
	 * Sets the total frame size.
	 *
	 * @param totalFrameSize the new total frame size
	 */
	public void setTotalFrameSize(int totalFrameSize) {
		this.totalFrameSize = totalFrameSize;
	}

	/**
	 * @return the charset
	 */
	public Charset getCharset() {
		return charset;
	}

	/**
	 * @param charset the charset to set
	 */
	public void setCharset(Charset charset) {
		this.charset = charset;
	}

	/**
	 * Get the description of this PictureFrame.
	 *
	 * @return The description of this PictureFrame.
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Set the description of this PictureFrame.
	 *
	 * @param description The description of this PictureFrame.
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Get the imageType of this PictureFrame.
	 *
	 * @return The imageType of this PictureFrame.
	 */
	public String getImageType() {
		return imageType;
	}

	/**
	 * Set the imageType of this PictureFrame.
	 *
	 * @param imageType The imageType of this PictureFrame.
	 */
	public void setImageType(String imageType) {
		this.imageType = imageType;
	}

	/**
	 * Get the imageData of this PictureFrame.
	 *
	 * @return The imageData of this PictureFrame.
	 */
	public byte[] getImageData() {
		return imageData;
	}

	/**
	 * Set the imageData of this PictureFrame.
	 *
	 * @param imageData The imageData of this PictureFrame.
	 */
	public void setImageData(byte[] imageData) {
		this.imageData = imageData;
	}

	/**
	 * The Enum PictureType.
	 */
	public enum PictureType {

		/** Other/Unknown. */
		OTHER(0),

		/** The file icon 32x32. */
		FILE_ICON_32X32(1),

		/** Other file icon. */
		OTHER_FILE_ICON(2),

		/** The front cover. */
		FRONT_COVER(3),

		/** The back cover. */
		BACK_COVER(4),

		/** The leaflet page. */
		LEAFLET_PAGE(5),

		/** The media. */
		MEDIA(6),

		/** The lead performer. */
		LEAD_PERFORMER(7),

		/** The performer. */
		PERFORMER(8),

		/** The conductor. */
		CONDUCTOR(9),

		/** The band or orchestra. */
		BAND_OR_ORCHESTRA(10),

		/** The composer. */
		COMPOSER(11),

		/** The lyricist. */
		LYRICIST(12),

		/** The recording location. */
		RECORDING_LOCATION(13),

		/** The during recording. */
		DURING_RECORDING(14),

		/** The during performance. */
		DURING_PERFORMANCE(15),

		/** The movie capture. */
		MOVIE_CAPTURE(16),

		/** A bright colored fish. Not a joke, actually in spec. */
		A_BRIGHT_COLOURED_FISH(17),

		/** The illustration. */
		ILLUSTRATION(18),

		/** The band or artist logo type. */
		BAND_OR_ARTIST_LOGOTYPE(19),

		/** The publisher or studio logo type. */
		PUBLISHER_OR_STUDIO_LOGOTYPE(20),

		/** Unofficial. */
		UNOFFICIAL(21);  // Needs additional info.

		private static Map<Integer, PictureType> numberToType = new HashMap<Integer, PictureType>();

		static {
			for (int index = 0; index < PictureType.values().length; index++) {
				PictureType.numberToType.put(PictureType.values()[index].getNumber(), PictureType.values()[index]);
			}
		}

		private int number;

		/**
		 * Instantiates a new picture type.
		 *
		 * @param number the number
		 */
		PictureType(int number) {
			this.number = number;
		}

		/**
		 * Gets the number.
		 *
		 * @return the number
		 */
		public int getNumber() {
			return this.number;
		}

		/**
		 * Gets the picture type.
		 *
		 * @param number the number
		 * @return the picture type
		 */
		public static PictureType getPictureType(int number) {
			PictureType result = PictureType.numberToType.get(number);
			if (result == null) {
				return PictureType.UNOFFICIAL;
			}
			return result;
		}
	}
}
