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

import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Class for serializing a {@link CueSheet} back to a string representation.
 * Does the inverse job of CueParser.
 *
 * @author jwbroek
 */
public class CueSheetSerializer {

	/**
	 * Character sequence for a single indentation level.
	 */
	private String indentationValue = "  ";

	/**
	 * The logger for this class.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(CueSheetSerializer.class);

	/**
	 * Create a default CueSheetSerializer.
	 */
	public CueSheetSerializer() {
	}

	/**
	 * Create a CueSheetSerializer with the specified indentationValue.
	 *
	 * @param indentationValue This String will be used for indentation.
	 */
	public CueSheetSerializer(String indentationValue) {
		LOGGER.debug("Setting CueSheetSerializer indentation value to: '{}'", indentationValue);
		this.indentationValue = indentationValue;
	}

	/**
	 * Get a textual representation of the cue sheet. If the cue sheet was
	 * parsed, then the output of this method is not necessarily identical to
	 * the parsed sheet, though it will contain the same data. Fields may appear
	 * in a different order, whitespace may change, comments may be gone, etc.
	 *
	 * @param cueSheet The CueSheet to serialize.
	 * @return A textual representation of the cue sheet.
	 */
	public String serializeCueSheet(CueSheet cueSheet) {
		StringBuilder builder = new StringBuilder();

		serializeCueSheet(builder, cueSheet, "");

		return builder.toString();
	}

	/**
	 * Serialize the CueSheet.
	 *
	 * @param builder The StringBuilder to serialize to.
	 * @param cueSheet The CueSheet to serialize.
	 * @param indentation The current indentation.
	 */
	protected void serializeCueSheet(StringBuilder builder, CueSheet cueSheet, String indentation) {
		LOGGER.debug("Serializing cue sheet to cue format.");

		addField(builder, "REM GENRE", indentation, cueSheet.getGenre());
		addField(builder, "REM DATE", indentation, cueSheet.getYear());
		addField(builder, "REM DISCID", indentation, cueSheet.getDiscId());
		addField(builder, "REM COMMENT", indentation, cueSheet.getComment());
		addField(builder, "CATALOG", indentation, cueSheet.getCatalog());
		addField(builder, "PERFORMER", indentation, cueSheet.getPerformer());
		addField(builder, "TITLE", indentation, cueSheet.getTitle());
		addField(builder, "SONGWRITER", indentation, cueSheet.getSongwriter());
		addField(builder, "CDTEXTFILE", indentation, cueSheet.getCdTextFile());

		for (FileData fileData : cueSheet.getFileData()) {
			serializeFileData(builder, fileData, indentation);
		}
	}

	/**
	 * Serialize the FileData.
	 *
	 * @param builder The StringBuilder to serialize to.
	 * @param fileData The FileData to serialize.
	 * @param indentation The current indentation.
	 */
	protected void serializeFileData(StringBuilder builder, FileData fileData, String indentation) {
		builder.append(indentation).append("FILE");

		if (fileData.getFile() != null) {
			builder.append(' ').append(quoteIfNecessary(fileData.getFile()));
		}

		if (fileData.getFileType() != null) {
			builder.append(' ').append(quoteIfNecessary(fileData.getFileType()));
		}

		builder.append('\n');

		for (TrackData trackData : fileData.getTrackData()) {
			serializeTrackData(builder, trackData, indentation + getIndentationValue());
		}
	}

	/**
	 * Serialize the TrackData.
	 *
	 * @param builder The StringBuilder to serialize to.
	 * @param trackData The TrackData to serialize.
	 * @param indentation The current indentation.
	 */
	protected void serializeTrackData(StringBuilder builder, TrackData trackData, String indentation) {
		builder.append(indentation).append("TRACK");

		if (trackData.getNumber() > -1) {
			builder.append(' ').append(String.format("%1$02d", trackData.getNumber()));
		}

		if (trackData.getDataType() != null) {
			builder.append(' ').append(quoteIfNecessary(trackData.getDataType()));
		}

		builder.append('\n');

		String childIndentation = indentation + getIndentationValue();

		addField(builder, "ISRC", childIndentation, trackData.getIsrcCode());
		addField(builder, "PERFORMER", childIndentation, trackData.getPerformer());
		addField(builder, "TITLE", childIndentation, trackData.getTitle());
		addField(builder, "SONGWRITER", childIndentation, trackData.getSongwriter());
		addField(builder, "PREGAP", childIndentation, trackData.getPregap());
		addField(builder, "POSTGAP", childIndentation, trackData.getPostgap());

		if (trackData.getFlags().size() > 0) {
			serializeFlags(builder, trackData.getFlags(), childIndentation);
		}

		for (Index index : trackData.getIndices()) {
			serializeIndex(builder, index, childIndentation);
		}
	}

	/**
	 * Serialize the flags.
	 *
	 * @param builder The StringBuilder to serialize to.
	 * @param flags The flags to serialize.
	 * @param indentation The current indentation.
	 */
	protected static void serializeFlags(StringBuilder builder, Set<String> flags, String indentation) {
		builder.append(indentation).append("FLAGS");
		for (String flag : flags) {
			builder.append(' ').append(quoteIfNecessary(flag));
		}
		builder.append('\n');
	}

	/**
	 * Serialize the index.
	 *
	 * @param builder The StringBuilder to serialize to.
	 * @param index The Index to serialize.
	 * @param indentation The current indentation.
	 */
	protected static void serializeIndex(StringBuilder builder, Index index, String indentation) {
		builder.append(indentation).append("INDEX");
		if (index.getNumber() > -1) {
			builder.append(' ').append(String.format("%1$02d", index.getNumber()));
		}

		if (index.getPosition() != null) {
			builder.append(' ').append(formatPosition(index.getPosition()));
		}

		builder.append('\n');
	}

	/**
	 * Format the specified position.
	 *
	 * @param position The {@link Position}.
	 * @return The formatted position.
	 */
	protected static String formatPosition(Position position) {
		return String.format("%1$02d:%2$02d:%3$02d", position.getMinutes(), position.getSeconds(), position.getFrames());
	}

	/**
	 * Add a field to the builder. The field is only added if the value is !=
	 * null.
	 *
	 * @param cueBuilder The {@link StringBuilder} to use.
	 * @param command The command to add.
	 * @param value The value to add. Will be formatted as per
	 *            formatPosition(Position).
	 * @param indentation The indentation for this field.
	 */
	protected static void addField(StringBuilder cueBuilder, String command, String indentation, Position value) {
		if (value != null) {
			cueBuilder.append(indentation).append(command).append(' ').append(formatPosition(value)).append('\n');
		}
	}

	/**
	 * Add a field to the builder. The field is only added if the value is !=
	 * null.
	 *
	 * @param cueBuilder The {@link StringBuilder} to use.
	 * @param command The command to add.
	 * @param value The value to add.
	 * @param indentation The indentation for this field.
	 */
	protected static void addField(StringBuilder cueBuilder, String command, String indentation, String value) {
		if (value != null) {
			cueBuilder.append(indentation).append(command).append(' ').append(quoteIfNecessary(value)).append('\n');
		}
	}

	/**
	 * Add a field to the builder. The field is only added if the value is
	 * {@code > -1}.
	 *
	 * @param cueBuilder The {@link StringBuilder} to use.
	 * @param command The command to add.
	 * @param value The value to add.
	 * @param indentation The indentation for this field.
	 */
	protected static void addField(StringBuilder cueBuilder, String command, String indentation, int value) {
		if (value > -1) {
			cueBuilder.append(indentation).append(command).append(' ').append("" + value).append('\n');
		}
	}

	/**
	 * Enclose the string in double quotes if it contains whitespace.
	 *
	 * @param input The input {@link String}.
	 * @return The input string, which will be surrounded in double quotes if it
	 *         contains any whitespace.
	 */
	protected static String quoteIfNecessary(String input) {
		// Search for whitespace
		for (int index = 0; index < input.length(); index++) {
			if (Character.isWhitespace(input.charAt(index))) {
				return '"' + input + '"';
			}
		}

		return input;
	}

	/**
	 * Get the character sequence for a single indentation value.
	 *
	 * @return The character sequence for a single indentation value.
	 */
	public String getIndentationValue() {
		return indentationValue;
	}

	/**
	 * Set the character sequence for a single indentation value.
	 *
	 * @param indentationValue The character sequence for a single indentation
	 *            value.
	 */
	public void setIndentationValue(String indentationValue) {
		this.indentationValue = indentationValue;
	}
}
