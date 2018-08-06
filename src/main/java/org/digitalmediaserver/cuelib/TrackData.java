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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.digitalmediaserver.cuelib.CueSheet.MetaDataField;


/**
 * Simple representation of a TRACK block of a cue sheet.
 *
 * @author jwbroek
 */
public class TrackData {

	/**
	 * The indices in this track,
	 */
	private final List<Index> indices = new ArrayList<Index>();

	/**
	 * The flags for this track.
	 */
	private final Set<String> flags = new TreeSet<String>();

	/**
	 * The track number. -1 signifies that it has not been set.
	 */
	private int number = -1;

	/**
	 * The data type of this track. Null signifies that it has not been set.
	 */
	private String dataType = null;

	/**
	 * The ISRC code of this track. Null signifies that it has not been set.
	 */
	private String isrcCode = null;

	/**
	 * The performer of this track. Null signifies that it has not been set.
	 * Should be a maximum of 80 characters if you want to burn to CD-TEXT.
	 */
	private String performer = null;

	/**
	 * The title of this track. Null signifies that it has not been set. Should
	 * be a maximum of 80 characters if you want to burn to CD-TEXT.
	 */
	private String title = null;

	/**
	 * The pregap of this track. Null signifies that it has not been set.
	 */
	private Position pregap = null;

	/**
	 * The postgap of this track. Null signifies that it has not been set.
	 */
	private Position postgap = null;

	/**
	 * The songwriter of this track. Null signifies that it has not been set.
	 * Should be a maximum of 80 characters if you want to burn to CD-TEXT.
	 */
	private String songwriter = null;

	/**
	 * The file data that this track data belongs to.
	 */
	private FileData parent;

	/**
	 * Create a new TrackData instance.
	 *
	 * @param parent The file data that this track data belongs to. Should not
	 *            be null.
	 */
	public TrackData(FileData parent) {
		this.parent = parent;
	}

	/**
	 * Create a new TrackData instance.
	 *
	 * @param parent The file data that this track data belongs to. Should not
	 *            be null.
	 * @param number The track number. -1 signifies that it has not been set.
	 * @param dataType The data type of this track. Null signifies that it has
	 *            not been set.
	 */
	public TrackData(FileData parent, int number, String dataType) {
		this.parent = parent;
		this.number = number;
		this.dataType = dataType;
	}

	/**
	 * Convenience method for getting metadata from the cue sheet. If a certain
	 * metadata field is not set, the method will return the empty string. When
	 * a field is ambiguous (such as the track number on a cue sheet instead of
	 * on a specific track), an IllegalArgumentException will be thrown.
	 * Otherwise, this method will attempt to give a sensible answer, possibly
	 * by searching through the cue sheet.
	 *
	 * @param metaDataField The {@link MetaDataField}.
	 * @return The specified metadata.
	 * @throws IllegalArgumentException If a field is ambiguous.
	 */
	public String getMetaData(MetaDataField metaDataField) throws IllegalArgumentException {
		switch (metaDataField) {
			case ISRCCODE:
				return getIsrcCode() == null ? "" : getIsrcCode();
			case PERFORMER:
				return getPerformer() == null ? getParent().getParent().getPerformer() : getPerformer();
			case TRACKPERFORMER:
				return getPerformer() == null ? "" : getPerformer();
			case SONGWRITER:
				return getSongwriter() == null ? getParent().getParent().getSongwriter() : getSongwriter();
			case TRACKSONGWRITER:
				return getSongwriter();
			case TITLE:
				return getTitle() == null ? getParent().getParent().getTitle() : getTitle();
			case TRACKTITLE:
				return getTitle();
			case TRACKNUMBER:
				return Integer.toString(getNumber());
			default:
				return getParent().getParent().getMetaData(metaDataField);
		}
	}

	/**
	 * Get the data type of this track. Null signifies that it has not been set.
	 *
	 * @return The data type of this track. Null signifies that it has not been
	 *         set.
	 */
	public String getDataType() {
		return dataType;
	}

	/**
	 * Set the data type of this track. Null signifies that it has not been set.
	 *
	 * @param dataType The data type of this track. Null signifies that it has
	 *            not been set.
	 */
	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	/**
	 * Get the ISRC code of this track. Null signifies that it has not been set.
	 *
	 * @return The ISRC code of this track. Null signifies that it has not been
	 *         set.
	 */
	public String getIsrcCode() {
		return isrcCode;
	}

	/**
	 * Set the ISRC code of this track. Null signifies that it has not been set.
	 *
	 * @param isrcCode The ISRC code of this track. Null signifies that it has
	 *            not been set.
	 */
	public void setIsrcCode(String isrcCode) {
		this.isrcCode = isrcCode;
	}

	/**
	 * Get the track number. -1 signifies that it has not been set.
	 *
	 * @return The track number. -1 signifies that it has not been set.
	 */
	public int getNumber() {
		return number;
	}

	/**
	 * Set the track number. -1 signifies that it has not been set.
	 *
	 * @param number The track number. -1 signifies that it has not been set.
	 */
	public void setNumber(int number) {
		this.number = number;
	}

	/**
	 * Get the performer of this track. Null signifies that it has not been set.
	 *
	 * @return The performer of this track. Null signifies that it has not been
	 *         set.
	 */
	public String getPerformer() {
		return performer;
	}

	/**
	 * Set the performer of this track. Null signifies that it has not been set.
	 *
	 * @param performer The performer of this track. Null signifies that it has
	 *            not been set. Should be a maximum of 80 characters if you want
	 *            to burn to CD-TEXT.
	 */
	public void setPerformer(String performer) {
		this.performer = performer;
	}

	/**
	 * Get the postgap of this track. Null signifies that it has not been set.
	 *
	 * @return The postgap of this track. Null signifies that it has not been
	 *         set.
	 */
	public Position getPostgap() {
		return postgap;
	}

	/**
	 * Set the postgap of this track. Null signifies that it has not been set.
	 *
	 * @param postgap The postgap of this track. Null signifies that it has not
	 *            been set.
	 */
	public void setPostgap(Position postgap) {
		this.postgap = postgap;
	}

	/**
	 * Get the pregap of this track. Null signifies that it has not been set.
	 *
	 * @return The pregap of this track. Null signifies that it has not been
	 *         set.
	 */
	public Position getPregap() {
		return pregap;
	}

	/**
	 * Set the pregap of this track. Null signifies that it has not been set.
	 *
	 * @param pregap The pregap of this track. Null signifies that it has not
	 *            been set.
	 */
	public void setPregap(Position pregap) {
		this.pregap = pregap;
	}

	/**
	 * Get the songwriter of this track. Null signifies that it has not been
	 * set.
	 *
	 * @return The songwriter of this track. Null signifies that it has not been
	 *         set.
	 */
	public String getSongwriter() {
		return songwriter;
	}

	/**
	 * Set the songwriter of this track. Null signifies that it has not been
	 * set. Should be a maximum of 80 characters if you want to burn to CD-TEXT.
	 *
	 * @param songwriter The songwriter of this track. Null signifies that it
	 *            has not been set. Should be a maximum of 80 characters if you
	 *            want to burn to CD-TEXT.
	 */
	public void setSongwriter(String songwriter) {
		this.songwriter = songwriter;
	}

	/**
	 * Get the title of this track. Null signifies that it has not been set.
	 *
	 * @return The title of this track. Null signifies that it has not been set.
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * Set the title of this track. Null signifies that it has not been set.
	 * Should be a maximum of 80 characters if you want to burn to CD-TEXT.
	 *
	 * @param title The title of this track. Null signifies that it has not been
	 *            set. Should be a maximum of 80 characters if you want to burn
	 *            to CD-TEXT.
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * Get the index with the specified number, or null if there is no such
	 * index.
	 *
	 * @param number The number of the desired index.
	 * @return The index with the specified number, or null if there is no such
	 *         index.
	 */
	public Index getIndex(int number) {
		// Note: we have to pass all indices until we've found the right one, as
		// we don't enforce that indices are sorted.
		// Normally, this shouldn't be a problem, as there are generally very
		// few indices. (Only rarely more than 2).
		for (Index index : indices) {
			if (index.getNumber() == number) {
				return index;
			}
		}
		return null;
	}

	/**
	 * Get the indices for this {@link TrackData}.
	 *
	 * @return The {@link List} of {@link Index} entries for this
	 *         {@link TrackData}.
	 */
	public List<Index> getIndices() {
		return indices;
	}

	/**
	 * Get the first {@link Index} for this {@link TrackData}.
	 *
	 * @return The first {@link Index} or {@code null}.
	 */
	public Index getFirstIndex() {
		return indices.isEmpty() ? null : indices.get(0);
	}

	/**
	 * Get the last {@link Index} for this {@link TrackData}.
	 *
	 * @return The last {@link Index} or {@code null}.
	 */
	public Index getLastIndex() {
		return indices.isEmpty() ? null : indices.get(indices.size() - 1);
	}

	/**
	 * Get the {@link Index} that indicates that start of the track. This is per
	 * definition index number 1. It there is no such index, index 0 is
	 * returned. If index 0 doesn't exist either, {@code null} is returned.
	 *
	 * @return The start {@link Index} or {@code null}.
	 */
	public Index getStartIndex() {
		if (indices.isEmpty()) {
			return null;
		}
		Index index0 = null;
		for (Index idx : indices) {
			if (idx.getNumber() == 1) {
				return idx;
			}
			if (idx.getNumber() == 0) {
				index0 = idx;
			}
		}
		return index0;
	}

	/**
	 * Get the flags for this track data.
	 *
	 * @return The flags for this track data.
	 */
	public Set<String> getFlags() {
		return flags;
	}

	/**
	 * Get the file data that this track data belong to..
	 *
	 * @return The file data that this track data belong to..
	 */
	public FileData getParent() {
		return parent;
	}

	/**
	 * Set the file data that this track data belong to..
	 *
	 * @param parent The file data that this track data belong to..
	 */
	public void setParent(FileData parent) {
		this.parent = parent;
	}
}
