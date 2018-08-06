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

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;


/**
 * Simple representation of a cue sheet.
 *
 * @author jwbroek
 */
public class CueSheet {

	/**
	 * Enumeration of available metadata fields. These can be consulted through
	 * the {@link #getMetaData(MetaDataField)} method.
	 */
	public enum MetaDataField {
		/**
		 * Performer of the album.
		 */
		ALBUMPERFORMER,
		/**
		 * Songwriter of the album.
		 */
		ALBUMSONGWRITER,
		/**
		 * Title of the album.
		 */
		ALBUMTITLE,
		/**
		 * The disc's media catalog number.
		 */
		CATALOG,
		/**
		 * CD-TEXT file.
		 */
		CDTEXTFILE,
		/**
		 * Album comment.
		 */
		COMMENT,
		/**
		 * An id for the disc. Typically the freedb disc id.
		 */
		DISCID,
		/**
		 * Disc number of the album.
		 */
		DISCNUMBER,
		/**
		 * Genre of the album.
		 */
		GENRE,
		/**
		 * ISRC code of a track.
		 */
		ISRCCODE,
		/**
		 * Performer of the album or track.
		 */
		PERFORMER,
		/**
		 * Songwriter of the album or track.
		 */
		SONGWRITER,
		/**
		 * Title of the album or track.
		 */
		TITLE,
		/**
		 * Total Discs of the album.
		 */
		TOTALDISCS,
		/**
		 * Number of a track.
		 */
		TRACKNUMBER,
		/**
		 * Performer of a track.
		 */
		TRACKPERFORMER,
		/**
		 * Songwriter of a track.
		 */
		TRACKSONGWRITER,
		/**
		 * Title of a track.
		 */
		TRACKTITLE,
		/**
		 * Year of the album.
		 */
		YEAR
	}

	/**
	 * The CUE file if any.
	 */
	private final Path file;

	/**
	 * Messages that concern this CueSheet.
	 */
	private final List<Message> messages = new ArrayList<Message>();

	// Various components of a cue sheet.
	/**
	 * The file components of the cue sheet.
	 */
	private final List<FileData> fileData = new ArrayList<FileData>();

	/**
	 * The disc's media catalog number. It should be 13 digits and compliant
	 * with UPC/EAN rules. May be null.
	 */
	private String catalog = null;

	/**
	 * The file containing the cd text data. May be null.
	 */
	private String cdTextFile = null;

	/**
	 * The performer of the album. For using as cd-text, it should be a maximum
	 * of 80 characters long. May be null.
	 */
	private String performer = null;

	/**
	 * The title of the album. For burning as cd-text, it should be a maximum of
	 * 80 characters long. May be null.
	 */
	private String title = null;

	/**
	 * The songwriter of the album. For burning as cd-text, it should be a
	 * maximum of 80 characters long. May be null.
	 */
	private String songwriter = null;

	/**
	 * A comment as is typically copied to ID3 tags. It may be null.
	 */
	private String comment = null;

	/**
	 * The year of the album. -1 signifies that it has not been specified.
	 */
	private int year = -1;

	/**
	 * An id for the disc. Typically the freedb disc id. May be null.
	 */
	private String discId = null;

	/**
	 * The genre of the album. May be null.
	 */
	private String genre = null;

	/**
	 * Total discs of the album. -1 signifies that it has not been specified.
	 */
	private int totalDiscs = -1;

	/**
	 * Disc number of the album. -1 signifies that it has not been specified.
	 */
	private int discNumber = -1;

	/**
	 * Create a new instance that isn't based on a file.
	 */
	public CueSheet() {
		this.file = null;
	}

	/**
	 * Create a new instance that is based on a file.
	 *
	 * @param file the CUE file.
	 */
	public CueSheet(Path file) {
		this.file = file;
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
	 * @throws IllegalArgumentException If the {@link MetaDataField} is
	 *             unsupported.
	 */
	public String getMetaData(MetaDataField metaDataField) throws IllegalArgumentException {
		switch (metaDataField) {
			case CATALOG:
				return getCatalog() == null ? "" : getCatalog();
			case CDTEXTFILE:
				return getCdTextFile() == null ? "" : getCdTextFile();
			case COMMENT:
				return getComment() == null ? "" : getComment();
			case DISCID:
				return getDiscId() == null ? "" : getDiscId();
			case DISCNUMBER:
				return getDiscNumber() == -1 ? "" : "" + getDiscNumber();
			case GENRE:
				return getGenre() == null ? "" : getGenre();
			case PERFORMER:
			case ALBUMPERFORMER:
				return getPerformer() == null ? "" : getPerformer();
			case SONGWRITER:
			case ALBUMSONGWRITER:
				return getSongwriter() == null ? "" : getSongwriter();
			case TITLE:
			case ALBUMTITLE:
				return getTitle() == null ? "" : getTitle();
			case TOTALDISCS:
				return getTotalDiscs() == -1 ? "" : "" + getTotalDiscs();
			case YEAR:
				return getYear() == -1 ? "" : "" + getYear();
			default:
				throw new IllegalArgumentException("Unsupported field: " + metaDataField.toString());
		}
	}

	/**
	 * Add an error message to this cue sheet.
	 *
	 * @param lineOfInput The line of input that caused the error.
	 * @param message A message describing the error.
	 */
	public void addError(LineOfInput lineOfInput, String message) {
		messages.add(new Error(lineOfInput, message));
	}

	/**
	 * Add a warning message to this cue sheet.
	 *
	 * @param lineOfInput The line of input that caused the warning.
	 * @param message A message describing the warning.
	 */
	public void addWarning(LineOfInput lineOfInput, String message) {
		messages.add(new Warning(lineOfInput, message));
	}

	/**
	 * Get all track data described in this cue sheet.
	 *
	 * @return All track data associated described in this cue sheet.
	 */
	public List<TrackData> getAllTrackData() {
		List<TrackData> allTrackData = new ArrayList<TrackData>();

		for (FileData fileDataElement : fileData) {
			allTrackData.addAll(fileDataElement.getTrackData());
		}

		return allTrackData;
	}

	/**
	 * Get the disc's media catalog number. It should be 13 digits and compliant
	 * with UPC/EAN rules. Null signifies that the catalog has not been
	 * specified.
	 *
	 * @return The disc's media catalog number
	 */
	public String getCatalog() {
		return catalog;
	}

	/**
	 * Set the disc's media catalog number. It should be 13 digits and compliant
	 * with UPC/EAN rules. Null signifies that the catalog has not been
	 * specified.
	 *
	 * @param catalog The disc's media catalog number
	 */
	public void setCatalog(String catalog) {
		this.catalog = catalog;
	}

	/**
	 * Get the file containing cd text data. Null signifies that no such file
	 * has been specified.
	 *
	 * @return The file containing cd text data.
	 */
	public String getCdTextFile() {
		return cdTextFile;
	}

	/**
	 * Set the file containing cd text data. Null signifies that no such file
	 * has been specified.
	 *
	 * @param cdTextFile The file containing cd text data
	 */
	public void setCdTextFile(String cdTextFile) {
		this.cdTextFile = cdTextFile;
	}

	/**
	 * Get the performer of the album. For burning as cd-text, it should be a
	 * maximum of 80 characters. May be null.
	 *
	 * @return The performer of the album
	 */
	public String getPerformer() {
		return performer;
	}

	/**
	 * Set the performer of the album. For burning as cd-text, it should be a
	 * maximum of 80 characters. May be null.
	 *
	 * @param performer The performer of the album.
	 */
	public void setPerformer(String performer) {
		this.performer = performer;
	}

	/**
	 * Get the songwriter of the album. For burning as cd-text, it should be a
	 * maximum of 80 characters. May be null.
	 *
	 * @return The songwriter of the album
	 */
	public String getSongwriter() {
		return songwriter;
	}

	/**
	 * Set the songwriter of the album. For burning as cd-text, it should be a
	 * maximum of 80 characters. May be null.
	 *
	 * @param songwriter The songwriter of the album.
	 */
	public void setSongwriter(String songwriter) {
		this.songwriter = songwriter;
	}

	/**
	 * Get the title of the album. For burning as cd-text, it should be a
	 * maximum of 80 characters. May be null.
	 *
	 * @return The title of the album
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * Set the title of the album. For burning as cd-text, it should be a
	 * maximum of 80 characters. May be null.
	 *
	 * @param title The title of the album.
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * Get the id for the disc. Typically the freedb disc ID. May be null.
	 *
	 * @return The ID for the disc.
	 * @deprecated Use {@link #getDiscId()} instead.
	 */
	@Deprecated
	public String getDiscid() {
		return discId;
	}

	/**
	 * Get the id for the disc. Typically the freedb disc ID. May be null.
	 *
	 * @return The ID for the disc.
	 */
	public String getDiscId() {
		return discId;
	}

	/**
	 * Set the ID for the disc. Typically the freedb disc ID. May be null.
	 *
	 * @param discid The ID for the disc.
	 * @deprecated Use {@link #setDiscId(String)} instead.
	 */
	@Deprecated
	public void setDiscid(String discid) {
		this.discId = discid;
	}

	/**
	 * Set the ID for the disc. Typically the freedb disc ID. May be null.
	 *
	 * @param discId The ID for the disc.
	 */
	public void setDiscId(String discId) {
		this.discId = discId;
	}

	/**
	 * Get the genre of the album. May be null.
	 *
	 * @return The genre of the album.
	 */
	public String getGenre() {
		return genre;
	}

	/**
	 * Set the genre of the album. May be null.
	 *
	 * @param genre The genre of the album.
	 */
	public void setGenre(String genre) {
		this.genre = genre;
	}

	/**
	 * Get the year of the album. -1 indicated that no year is set.
	 *
	 * @return The year of the album.
	 */
	public int getYear() {
		return year;
	}

	/**
	 * Set the year of the album. -1 indicated that no year is set.
	 *
	 * @param year The year of the album.
	 */
	public void setYear(int year) {
		this.year = year;
	}

	/**
	 * The comment for the album. May be null.
	 *
	 * @return The comment for the album.
	 */
	public String getComment() {
		return comment;
	}

	/**
	 * Set the comment for the album. May be null.
	 *
	 * @param comment The comment for the album.
	 */
	public void setComment(String comment) {
		this.comment = comment;
	}

	/**
	 * Get the file data for this cue sheet.
	 *
	 * @return The file data for this cue sheet.
	 */
	public List<FileData> getFileData() {
		return fileData;
	}

	/**
	 * Get the parsing messages for this cue sheet.
	 *
	 * @return The parsing messages for the cue sheet.
	 */
	public List<Message> getMessages() {
		return messages;
	}

	/**
	 * Get the total discs of the album. -1 indicated that no value is set.
	 *
	 * @return The total discs of the album.
	 */
	public int getTotalDiscs() {
		return totalDiscs;
	}

	/**
	 * Set the total discs of the album. -1 indicated that no value is set.
	 *
	 * @param totalDiscs The total discs of the album.
	 */
	public void setTotalDiscs(int totalDiscs) {
		this.totalDiscs = totalDiscs;
	}

	/**
	 * Get the total discs of the album. -1 indicated that no value is set.
	 *
	 * @return The total discs of the album.
	 */
	public int getDiscNumber() {
		return discNumber;
	}

	/**
	 * Set the disc number of the album. -1 indicated that no value is set.
	 *
	 * @param discNumber The disc number of the album.
	 */
	public void setDiscNumber(int discNumber) {
		this.discNumber = discNumber;
	}

	/**
	 * Get the CUE {@link Path} for this {@link CueSheet}. Might be {@code null}
	 * if the information wasn't given in the constructor.
	 *
	 * @return The {@link Path} or {@code null}.
	 */
	public Path getFile() {
		return file;
	}
}
