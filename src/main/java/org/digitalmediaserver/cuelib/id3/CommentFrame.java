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
import java.nio.charset.StandardCharsets;
import java.util.Properties;


/**
 * The {@link ID3Frame} implementation CommentFrame.
 */
public class CommentFrame implements ID3Frame {

	/** The description. */
	private String description;

	/** The text. */
	private String text;

	/** The language code. */
	private String languageCode;

	/** The total frame size. */
	private int totalFrameSize;

	/** The charset. */
	private Charset charset = StandardCharsets.ISO_8859_1;

	/** The flags. */
	private Properties flags = new Properties();

	/**
	 * Instantiates a new comment frame.
	 */
	public CommentFrame() {
	}

	/**
	 * Instantiates a new comment frame.
	 *
	 * @param totalFrameSize the total frame size.
	 */
	public CommentFrame(int totalFrameSize) {
		this.totalFrameSize = totalFrameSize;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder
			.append("Comment frame: ").append(languageCode).append(" [").append(totalFrameSize).append("] ")
			.append(charset.toString()).append('\n').append("Flags: ").append(flags.toString()).append('\n')
			.append("Description: ").append(description).append('\n').append("Text: ").append(text);
		return builder.toString();
	}

	/**
	 * Sets the {@link Charset}.
	 *
	 * @param charset The {@link Charset} to set.
	 */
	public void setCharset(Charset charset) {
		this.charset = charset;
	}

	/**
	 * @return The {@link Charset}.
	 */
	public Charset getCharset() {
		return this.charset;
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
	 * @return the text
	 */
	public String getText() {
		return text;
	}

	/**
	 * @param text the text to set
	 */
	public void setText(String text) {
		this.text = text;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public CanonicalFrameType getCanonicalFrameType() {
		return CanonicalFrameType.COMMENT;
	}

	/**
	 * @return the languageCode
	 */
	public String getLanguageCode() {
		return languageCode;
	}

	/**
	 * @param languageCode the languageCode to set
	 */
	public void setLanguageCode(String languageCode) {
		this.languageCode = languageCode;
	}

	/**
	 * @return The flags.
	 */
	@Override
	public Properties getFlags() {
		return flags;
	}
}
