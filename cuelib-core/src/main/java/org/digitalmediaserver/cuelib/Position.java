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
package org.digitalmediaserver.cuelib;

import java.util.Locale;


/**
 * Simple representation for a position field in a cue sheet.
 *
 * @author jwbroek
 */
public class Position {

	/**
	 * The number of minutes in this position. Must be >= 0. Should be < 60.
	 */
	private int minutes = 0;

	/**
	 * The number of seconds in this position. Must be >= 0. Should be < 60.
	 */
	private int seconds = 0;

	/**
	 * The number of frames in this position. Must be >= 0. Should be < 75.
	 */
	private int frames = 0;

	/**
	 * Create a new Position.
	 */
	public Position() {
	}

	/**
	 * Creates a new {@link Position}.
	 *
	 * @param minutes The number of minutes in this position. Must be
	 *            {@code >= 0}. Should be {@code < 60}.
	 * @param seconds The number of seconds in this position. Must be
	 *            {@code >= 0}. Should be {@code < 60}.
	 * @param frames The number of frames in this position. Must be {@code >= 0}
	 *            . Should be {@code < 75}.
	 */
	public Position(int minutes, int seconds, int frames) {
		this.minutes = minutes;
		this.seconds = seconds;
		this.frames = frames;
	}

	/**
	 * Creates a new {@link Position} based on the sample number.
	 *
	 * @param sampleNumber the sample number.
	 * @param sampleRate the sample rate.
	 */
	public Position(long sampleNumber, int sampleRate) {
		int totalSeconds = (int) (sampleNumber / sampleRate);
		minutes = totalSeconds / 60;
		seconds = totalSeconds % 60;
		frames = (int) (((sampleNumber % sampleRate) * 75) / sampleRate);
	}

	/**
	 * Get the total number of frames represented by this position. This is
	 * equal to {@code frames + (75 * (seconds + 60 * minutes))}.
	 *
	 * @return The total number of frames represented by this position.
	 */
	public int getTotalFrames() {
		return frames + (75 * (seconds + 60 * minutes));
	}

	/**
	 * Get the number of frames in this position. Must be {@code >= 0}. Should
	 * be {@code < 75}.
	 *
	 * @return The number of frames in this position. Must be {@code >= 0}.
	 *         Should be {@code < 75}.
	 */
	public int getFrames() {
		return frames;
	}

	/**
	 * Set the number of frames in this position. Must be {@code >= 0}. Should
	 * be {@code < 75}.
	 *
	 * @param frames The number of frames in this position. Must be {@code >= 0}
	 *            . Should be {@code < 75}.
	 */
	public void setFrames(int frames) {
		this.frames = frames;
	}

	/**
	 * Get the number of minutes in this position. Must be {@code >= 0}. Should
	 * be {@code < 60}.
	 *
	 * @return The number of minutes in this position. Must be {@code >= 0}.
	 *         Should be {@code < 60}.
	 */
	public int getMinutes() {
		return minutes;
	}

	/**
	 * Set the number of minutes in this position. Must be {@code >= 0}. Should
	 * be {@code < 60}.
	 *
	 * @param minutes The number of minutes in this position. Must be
	 *            {@code >= 0}. Should be {@code < 60}.
	 */
	public void setMinutes(int minutes) {
		this.minutes = minutes;
	}

	/**
	 * Get the number of seconds in this position. Must be {@code >= 0}. Should
	 * be {@code < 60}.
	 *
	 * @return The seconds of seconds in this position. Must be {@code >= 0}.
	 *         Should be {@code < 60}.
	 */
	public int getSeconds() {
		return seconds;
	}

	/**
	 * Set the number of seconds in this position. Must be {@code >= 0}. Should
	 * be {@code < 60}.
	 *
	 * @param seconds The number of seconds in this position. Must be
	 *            {@code >= 0}. Should be {@code < 60}.
	 */
	public void setSeconds(int seconds) {
		this.seconds = seconds;
	}

	@Override
	public String toString() {
		return String.format(Locale.ROOT, "%d:%02d.%02d", minutes, seconds, frames);
	}
}
