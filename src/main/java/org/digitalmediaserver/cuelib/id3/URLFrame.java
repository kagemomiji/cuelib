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

import java.net.URL;
import java.util.Properties;


/**
 * The Class URLFrame.
 */
public class URLFrame implements ID3Frame {

	// TODO Use proper URL.
	private String additionalTypeInfo = "";
	private String url;
	private int totalFrameSize;
	private CanonicalFrameType canonicalFrameType;
	private Properties flags = new Properties();

	/**
	 * @return the flags
	 */
	@Override
	public Properties getFlags() {
		return flags;
	}

	/**
	 * Instantiates a new URL frame.
	 *
	 * @param canonicalFrameType the canonical frame type
	 */
	public URLFrame(CanonicalFrameType canonicalFrameType) {
		this.canonicalFrameType = canonicalFrameType;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("URL frame: ").append(this.canonicalFrameType.toString())
			.append(' ').append(this.additionalTypeInfo)
			.append(" [").append(this.totalFrameSize).append("]\n")
			.append("Flags: ").append(this.flags.toString()).append('\n')
			.append("URL: ").append(this.url);
		return builder.toString();
	}

	/**
	 * Set the {@link URL}.
	 *
	 * @param url the {@link URL}.
	 */
	public void setUrl(URL url) {
		this.url = url.toString();
	}

	/**
	 * Set the {@link URL}.
	 *
	 * @param url the URL.
	 */
	public void setUrl(String url) {
		this.url = url;
	}

	/**
	 * @return The URL.
	 */
	public String getUrl() {
		return this.url;
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

	@Override
	public CanonicalFrameType getCanonicalFrameType() {
		return this.canonicalFrameType;
	}

	/**
	 * Sets the canonical frame type.
	 *
	 * @param canonicalFrameType the new canonical frame type
	 */
	public void setCanonicalFrameType(CanonicalFrameType canonicalFrameType) {
		this.canonicalFrameType = canonicalFrameType;
	}

	/**
	 * Get the additionalTypeInfo of this URLFrame.
	 *
	 * @return The additionalTypeInfo of this URLFrame.
	 */
	public String getAdditionalTypeInfo() {
		return additionalTypeInfo;
	}

	/**
	 * Set the additionalTypeInfo of this URLFrame.
	 *
	 * @param additionalTypeInfo The additionalTypeInfo of this URLFrame.
	 */
	public void setAdditionalTypeInfo(String additionalTypeInfo) {
		this.additionalTypeInfo = additionalTypeInfo;
	}
}
