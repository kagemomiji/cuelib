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

import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Parser for cue sheets.
 *
 * @author jwbroek
 */
final public class CueParser {

	/**
	 * Logger for this class.
	 */
	private final static Logger LOGGER = LoggerFactory.getLogger(CueParser.class);

	// Constants for warning texts. Quick and dirty. Should really be a ResourceBundle.
	private final static String WARNING_EMPTY_LINES = "Empty lines not allowed. Will ignore.";
	private final static String WARNING_UNPARSEABLE_INPUT = "Unparseable line. Will ignore.";
	private final static String WARNING_INVALID_CATALOG_NUMBER = "Invalid catalog number.";
	private final static String WARNING_NONCOMPLIANT_FILE_TYPE = "Noncompliant file type.";
	private final static String WARNING_NO_FLAGS = "No flags specified.";
	private final static String WARNING_NONCOMPLIANT_FLAG = "Noncompliant flag(s) specified.";
	private final static String WARNING_WRONG_NUMBER_OF_DIGITS = "Wrong number of digits in number.";
	private final static String WARNING_NONCOMPLIANT_ISRC_CODE = "ISRC code has noncompliant format.";
	private final static String WARNING_FIELD_LENGTH_OVER_80 = "The field is too long to burn as CD-TEXT. The maximum length is 80.";
	private final static String WARNING_NONCOMPLIANT_DATA_TYPE = "Noncompliant data type specified.";
	private final static String WARNING_TOKEN_NOT_UPPERCASE = "Token has wrong case. Uppercase was expected.";
	private final static String WARNING_INVALID_FRAMES_VALUE = "Position has invalid frame value. Should be 00-74.";
	private final static String WARNING_INVALID_SECONDS_VALUE = "Position has invalid seconds value. Should be 00-59.";
	private final static String WARNING_DATUM_APPEARS_TOO_OFTEN = "Datum appears too often.";
	private final static String WARNING_FILE_IN_WRONG_PLACE = "A FILE datum must come before everything else except REM and CATALOG.";
	private final static String WARNING_FLAGS_IN_WRONG_PLACE = "A FLAGS datum must come after a TRACK, but before any INDEX of that TRACK.";
	private final static String WARNING_NO_FILE_SPECIFIED = "Datum must appear in FILE, but no FILE specified.";
	private final static String WARNING_NO_TRACK_SPECIFIED = "Datum must appear in TRACK, but no TRACK specified.";
	private final static String WARNING_INVALID_INDEX_NUMBER = "Invalid index number. First number must be 0 or 1; all next ones sequential.";
	private final static String WARNING_INVALID_FIRST_POSITION = "Invalid position. First index must have position 00:00:00";
	private final static String WARNING_ISRC_IN_WRONG_PLACE = "An ISRC datum must come after TRACK, but before any INDEX of TRACK.";
	private final static String WARNING_PREGAP_IN_WRONG_PLACE = "A PREGAP datum must come after TRACK, but before any INDEX of that TRACK.";
	private final static String WARNING_INDEX_AFTER_POSTGAP = "A POSTGAP datum must come after all INDEX data of a TRACK.";
	private final static String WARNING_INVALID_DISCNUMBER = "Invalid disc number. Should be a number from 1.";
	private final static String WARNING_INVALID_TOTALDISCS = "Invalid total discs. Should be a number from 1.";
	private final static String WARNING_INVALID_TRACK_NUMBER = "Invalid track number. First number must be 1; all next ones sequential.";
	private final static String WARNING_INVALID_YEAR = "Invalid year. Should be a number from 1 to 9999 (inclusive).";

	// Patterns used for parsing and validation. Quick and dirty. A formal grammar would be nicer.
	private final static Pattern PATTERN_POSITION = Pattern.compile("^(\\d*):(\\d*):(\\d*)$");
	private final static Pattern PATTERN_CATALOG_NUMBER = Pattern.compile("^\\d{13}$");
	private final static Pattern PATTERN_FILE = Pattern.compile("^FILE\\s+((?:\"[^\"]*\")|\\S+)\\s+(\\S+)\\s*$", Pattern.CASE_INSENSITIVE);
	private final static Pattern PATTERN_CDTEXTFILE = Pattern.compile("^CDTEXTFILE\\s+((?:\"[^\"]*\")|\\S+)\\s*$", Pattern.CASE_INSENSITIVE);
	private final static Pattern PATTERN_FLAGS = Pattern.compile("^FLAGS(\\s+\\w+)*\\s*$", Pattern.CASE_INSENSITIVE);
	private final static Pattern PATTERN_INDEX = Pattern.compile("^INDEX\\s+(\\d+)\\s+(\\d*:\\d*:\\d*)\\s*$", Pattern.CASE_INSENSITIVE);
	private final static Pattern PATTERN_ISRC_CODE = Pattern.compile("^\\w{5}\\d{7}$");
	private final static Pattern PATTERN_PERFORMER = Pattern.compile("^PERFORMER\\s+((?:\"[^\"]*\")|\\S+)\\s*$", Pattern.CASE_INSENSITIVE);
	private final static Pattern PATTERN_POSTGAP = Pattern.compile("^POSTGAP\\s+(\\d*:\\d*:\\d*)\\s*$", Pattern.CASE_INSENSITIVE);
	private final static Pattern PATTERN_PREGAP = Pattern.compile("^PREGAP\\s+(\\d*:\\d*:\\d*)\\s*$", Pattern.CASE_INSENSITIVE);
	private final static Pattern PATTERN_REM_COMMENT = Pattern.compile("^(REM\\s+COMMENT)\\s+((?:\"[^\"]*\")|\\S+)\\s*$", Pattern.CASE_INSENSITIVE);
	private final static Pattern PATTERN_REM_DATE = Pattern.compile("^(REM\\s+DATE)\\s+(\\d+)\\s*$", Pattern.CASE_INSENSITIVE);
	private final static Pattern PATTERN_REM_DISCID = Pattern.compile("^(REM\\s+DISCID)\\s+((?:\"[^\"]*\")|\\S+)\\s*$", Pattern.CASE_INSENSITIVE);
	private final static Pattern PATTERN_REM_DISCNUMBER = Pattern.compile("^(REM\\s+DISCNUMBER)\\s+((?:\"[^\"]*\")|\\S+)\\s*$", Pattern.CASE_INSENSITIVE);
	private final static Pattern PATTERN_REM_GENRE = Pattern.compile("^(REM\\s+GENRE)\\s+((?:\"[^\"]*\")|\\S+)\\s*$", Pattern.CASE_INSENSITIVE);
	private final static Pattern PATTERN_REM_TOTALDISCS = Pattern.compile("^(REM\\s+TOTALDISCS)\\s+((?:\"[^\"]*\")|\\S+)\\s*$", Pattern.CASE_INSENSITIVE);
	private final static Pattern PATTERN_SONGWRITER = Pattern.compile("^SONGWRITER\\s+((?:\"[^\"]*\")|\\S+)\\s*$", Pattern.CASE_INSENSITIVE);
	private final static Pattern PATTERN_TITLE = Pattern.compile("^TITLE\\s+((?:\"[^\"]*\")|\\S+)\\s*$", Pattern.CASE_INSENSITIVE);
	private final static Pattern PATTERN_TRACK = Pattern.compile("TRACK\\s+(\\d+)\\s+(\\S+)\\s*$", Pattern.CASE_INSENSITIVE);

	/**
	 * A set of all file types that are allowed by the cue sheet spec.
	 */
	private final static Set<String> COMPLIANT_FILE_TYPES = new TreeSet<String>(Arrays.asList(new String[] {"BINARY", "MOTOROLA", "AIFF", "WAVE", "MP3"}));
	/**
	 * A set of all flags that are allowed by the cue sheet spec.
	 */
	private final static Set<String> COMPLIANT_FLAGS = new TreeSet<String>(Arrays.asList(new String[] {"DCP", "4CH", "PRE", "SCMS", "DATA"}));
	/**
	 * A set of all data types that are allowed by the cue sheet spec.
	 */
	private final static Set<String> COMPLIANT_DATA_TYPES = new TreeSet<String>(Arrays.asList(new String[] {
		"AUDIO",
		"CDG",
		"MODE1/2048",
		"MODE1/2352",
		"MODE2/2336",
		"MODE2/2352",
		"CDI/2336",
		"CDI/2352"
	}));

	/**
	 * Create a CueParser. Should never be used, as all properties and methods
	 * of this class are static.
	 */
	private CueParser() {
	}

	/**
	 * Parse a cue sheet that will be read from the InputStream.
	 *
	 * @param inputStream An {@link java.io.InputStream} that produces a cue
	 *            sheet. The stream will be closed afterward.
	 * @return A representation of the cue sheet.
	 * @throws IOException
	 */
	public static CueSheet parse(final InputStream inputStream) throws IOException {
		LineNumberReader lnReader = new LineNumberReader(new InputStreamReader(inputStream));
		final CueSheet result = CueParser.parse(lnReader);
		lnReader.close();

		return result;
	}

	/**
	 * Parse a cue sheet file.
	 *
	 * @param file A cue sheet file.
	 * @return A representation of the cue sheet.
	 * @throws IOException
	 */
	public static CueSheet parse(final File file) throws IOException {
		final CueSheet result = CueParser.parse(new LineNumberReader(new FileReader(file)));
		return result;
	}

	/**
	 * Parse a cue sheet.
	 *
	 * @param reader A reader for the cue sheet. This reader will be closed
	 *            afterward.
	 * @return A representation of the cue sheet.
	 * @throws IOException
	 */
	public static CueSheet parse(final LineNumberReader reader) throws IOException {
		LOGGER.debug("Parsing cue sheet.");

		final CueSheet result = new CueSheet();

		try {
			// Go through all lines of input.
			String inputLine = reader.readLine();

			while (inputLine != null) {
				LOGGER.trace("Processing input line.");

				// Normalize by removing left and right whitespace.
				inputLine = inputLine.trim();

				final LineOfInput input = new LineOfInput(reader.getLineNumber(), inputLine, result);

				// Do some validation. If there are no problems, then parse the
				// line.
				if (inputLine.length() == 0) {
					// File should not contain empty lines.
					addWarning(input, WARNING_EMPTY_LINES);
				} else if (inputLine.length() < 2) {
					// No token in the spec has length smaller than 2. Unknown
					// token.
					addWarning(input, WARNING_UNPARSEABLE_INPUT);
				} else {
					// Use first 1-2 characters to guide parsing. These two
					// characters are enough to determine how to
					// proceed.
					switch (inputLine.charAt(0)) {
						case 'c':
						case 'C':
							switch (inputLine.charAt(1)) {
								case 'a':
								case 'A':
									CueParser.parseCatalog(input);
									break;
								case 'd':
								case 'D':
									CueParser.parseCdTextFile(input);
									break;
								default:
									addWarning(input, WARNING_UNPARSEABLE_INPUT);
									break;
							}
							break;
						case 'f':
						case 'F':
							switch (inputLine.charAt(1)) {
								case 'i':
								case 'I':
									CueParser.parseFile(input);
									break;
								case 'l':
								case 'L':
									CueParser.parseFlags(input);
									break;
								default:
									addWarning(input, WARNING_UNPARSEABLE_INPUT);
									break;
							}
							break;
						case 'i':
						case 'I':
							switch (inputLine.charAt(1)) {
								case 'n':
								case 'N':
									CueParser.parseIndex(input);
									break;
								case 's':
								case 'S':
									CueParser.parseIsrc(input);
									break;
								default:
									addWarning(input, WARNING_UNPARSEABLE_INPUT);
									break;
							}
							break;
						case 'p':
						case 'P':
							switch (inputLine.charAt(1)) {
								case 'e':
								case 'E':
									CueParser.parsePerformer(input);
									break;
								case 'o':
								case 'O':
									CueParser.parsePostgap(input);
									break;
								case 'r':
								case 'R':
									CueParser.parsePregap(input);
									break;
								default:
									addWarning(input, WARNING_UNPARSEABLE_INPUT);
									break;
							}
							break;
						case 'r':
						case 'R':
							CueParser.parseRem(input);
							break;
						case 's':
						case 'S':
							CueParser.parseSongwriter(input);
							break;
						case 't':
						case 'T':
							switch (inputLine.charAt(1)) {
								case 'i':
								case 'I':
									CueParser.parseTitle(input);
									break;
								case 'r':
								case 'R':
									CueParser.parseTrack(input);
									break;
								default:
									addWarning(input, WARNING_UNPARSEABLE_INPUT);
									break;
							}
							break;
						default:
							addWarning(input, WARNING_UNPARSEABLE_INPUT);
							break;
					}
				}

				// And on to the next line...
				inputLine = reader.readLine();
			}
		} finally {
			LOGGER.trace("Closing input reader.");
			reader.close();
		}

		return result;
	}

	/**
	 * Determine if the input starts with some string. Will return true if it
	 * matches, regardless of case. If there is a match, but the case differs,
	 * then a "TOKEN NOT UPPERCASE" warning will be added to the cue sheet
	 * associated with the input.
	 *
	 * @param input The input to check.
	 * @param start The starting string to check for. Should be uppercase, or
	 *            else the warning will not make sense.
	 * @return True if there is a match. False otherwise.
	 */
	private static boolean startsWith(final LineOfInput input, final String start) {
		if (input.getInput().startsWith(start)) {
			return true;
		} else if (input.getInput().substring(0, start.length()).equalsIgnoreCase(start)) {
			addWarning(input, WARNING_TOKEN_NOT_UPPERCASE);
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Determine if the input contains the specified pattern. Will return true
	 * if it matches. If there is a match and and there is a capturing group,
	 * then the first such group will be checked for case. If it is not
	 * uppercase, then a "TOKEN NOT UPPERCASE" warning will be added to the cue
	 * sheet associated with the input.
	 *
	 * @param input The input to check.
	 * @param pattern {@link java.util.regex.Pattern} to check for. If it
	 *            contains a capturing group, then on a match, this group will
	 *            be checked for case as per the method description.
	 * @return True if there is a match. False otherwise.
	 */
	private static boolean contains(final LineOfInput input, final Pattern pattern) {
		final Matcher matcher = pattern.matcher(input.getInput());

		if (matcher.find()) {
			if (matcher.groupCount() > 0 && !matcher.group(1).equals(matcher.group(1).toUpperCase())) {
				addWarning(input, WARNING_TOKEN_NOT_UPPERCASE);
			}
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Parse the CATALOG command.
	 *
	 * CATALOG [media-catalog-number] CD catalog number. Code follows UPC/EAN
	 * rules. Usually the first command, but this is not required. Not a
	 * mandatory command.
	 *
	 * @param input
	 */
	private static void parseCatalog(final LineOfInput input) {
		if (startsWith(input, "CATALOG")) {
			String catalogNumber = input.getInput().substring("CATALOG".length()).trim();
			if (!PATTERN_CATALOG_NUMBER.matcher(catalogNumber).matches()) {
				addWarning(input, WARNING_INVALID_CATALOG_NUMBER);
			}

			if (input.getAssociatedSheet().getCatalog() != null) {
				addWarning(input, WARNING_DATUM_APPEARS_TOO_OFTEN);
			}

			input.getAssociatedSheet().setCatalog(catalogNumber);
		} else {
			addWarning(input, WARNING_UNPARSEABLE_INPUT);
		}
	}

	/**
	 * Parse the FILE command.
	 *
	 * FILE [filename] [filetype] File containing data. According to the spec it
	 * must come before every other command except CATALOG. This rule
	 * contradicts the official examples and is often broken in practice. Hence,
	 * we don't raise a warning when this rule is broken.
	 *
	 * @param input
	 */
	private static void parseFile(final LineOfInput input) {
		Matcher fileMatcher = PATTERN_FILE.matcher(input.getInput());

		if (startsWith(input, "FILE") && fileMatcher.matches()) {
			if (!COMPLIANT_FILE_TYPES.contains(fileMatcher.group(2))) {
				if (COMPLIANT_FILE_TYPES.contains(fileMatcher.group(2).toUpperCase())) {
					addWarning(input, WARNING_TOKEN_NOT_UPPERCASE);
				} else {
					addWarning(input, WARNING_NONCOMPLIANT_FILE_TYPE);
				}

			}

			/*
			 * This is a silly rule that is very commonly broken. Hence, we
			 * don't enforce it.
			 *
			 * Check to see if FILE is the first command in the sheet, except
			 * for CATALOG. (Technically, we should also check for REM commands,
			 * but we don't keep track of all of those.)
			 *
			 * if ( input.getAssociatedSheet().getFileData().size()==0 && (
			 * input.getAssociatedSheet().getCdTextFile() != null ||
			 * input.getAssociatedSheet().getPerformer() != null ||
			 * input.getAssociatedSheet().getSongwriter() != null ||
			 * input.getAssociatedSheet().getTitle() != null ||
			 * input.getAssociatedSheet().getComment() != null ||
			 * input.getAssociatedSheet().getDiscid() != null ||
			 * input.getAssociatedSheet().getYear() != -1 ||
			 * input.getAssociatedSheet().getGenre() != null ) ) {
			 * CueParser.logger.warning(WARNING_FILE_IN_WRONG_PLACE);
			 * input.getAssociatedSheet().addWarning(input,
			 * WARNING_FILE_IN_WRONG_PLACE); }
			 */

			// If the file name is enclosed in quotes, remove those.
			String file = fileMatcher.group(1);
			if (file.length() > 0 && file.charAt(0) == '"' && file.charAt(file.length() - 1) == '"') {
				file = file.substring(1, file.length() - 1);
			}

			input.getAssociatedSheet().getFileData()
				.add(new FileData(input.getAssociatedSheet(), file, fileMatcher.group(2).toUpperCase()));
		} else {
			addWarning(input, WARNING_UNPARSEABLE_INPUT);
		}
	}

	/**
	 * Parse the CDTEXTFILE command.
	 *
	 * CDTEXTFILE [filename] File that contains cd text data. Not mandatory.
	 *
	 * @param input
	 */
	private static void parseCdTextFile(final LineOfInput input) {
		Matcher cdTextFileMatcher = PATTERN_CDTEXTFILE.matcher(input.getInput());

		if (startsWith(input, "CDTEXTFILE") && cdTextFileMatcher.matches()) {
			if (input.getAssociatedSheet().getCdTextFile() != null) {
				LOGGER.warn(WARNING_DATUM_APPEARS_TOO_OFTEN);
				input.getAssociatedSheet().addWarning(input, WARNING_DATUM_APPEARS_TOO_OFTEN);
			}

			// If the file name is enclosed in quotes, remove those.
			String file = cdTextFileMatcher.group(1);
			if (file.length() > 0 && file.charAt(0) == '"' && file.charAt(file.length() - 1) == '"') {
				file = file.substring(1, file.length() - 1);
			}

			input.getAssociatedSheet().setCdTextFile(file);
		} else {
			addWarning(input, WARNING_UNPARSEABLE_INPUT);
		}
	}

	/**
	 * Parse the FLAGS command.
	 *
	 * FLAGS [flags] Track subcode flags. Rarely used according to spec.
	 *
	 * @param input
	 */
	private static void parseFlags(final LineOfInput input) {
		Matcher flagsMatcher = PATTERN_FLAGS.matcher(input.getInput());

		if (startsWith(input, "FLAGS") && flagsMatcher.matches()) {
			if (null == flagsMatcher.group(1)) {
				addWarning(input, WARNING_NO_FLAGS);
			} else {
				TrackData trackData = getLastTrackData(input);

				if (trackData.getIndices().size() > 0) {
					addWarning(input, WARNING_FLAGS_IN_WRONG_PLACE);
				}

				Set<String> flagCollection = trackData.getFlags();

				if (!flagCollection.isEmpty()) {
					addWarning(input, WARNING_DATUM_APPEARS_TOO_OFTEN);
				}

				try (Scanner flagScanner = new Scanner(flagsMatcher.group(1))) {
					while (flagScanner.hasNext()) {
						String flag = flagScanner.next();
						if (!COMPLIANT_FLAGS.contains(flag)) {
							addWarning(input, WARNING_NONCOMPLIANT_FLAG);
						}
						flagCollection.add(flag);
					}
				}
			}
		} else {
			addWarning(input, WARNING_UNPARSEABLE_INPUT);
		}
	}

	/**
	 * Parse the INDEX command.
	 *
	 * INDEX [number] [mm:ss:ff] Indexes or subindexes within a track. Relative
	 * w.r.t. beginning of file.
	 *
	 * ff = frames; 75 frames/s First index must be 0 or 1. All others
	 * sequential. First index must be 00:00:00 0 is track pregap. 1 is starting
	 * time of track data. > 1 is subindex within track.
	 *
	 * @param input
	 */
	private static void parseIndex(final LineOfInput input) {
		Matcher indexMatcher = PATTERN_INDEX.matcher(input.getInput());

		if (startsWith(input, "INDEX") && indexMatcher.matches()) {
			if (indexMatcher.group(1).length() != 2) {
				addWarning(input, WARNING_WRONG_NUMBER_OF_DIGITS);
			}

			TrackData trackData = getLastTrackData(input);
			List<Index> trackIndices = trackData.getIndices();

			// Postgap data must come after all index data. Only check for first
			// index. No need to repeat this warning for
			// all indices that follow.
			if (trackIndices.isEmpty() && trackData.getPostgap() != null) {
				addWarning(input, WARNING_INDEX_AFTER_POSTGAP);
			}

			int indexNumber = Integer.parseInt(indexMatcher.group(1));

			// If first index of track, then number must be 0 or 1; if not first
			// index of track, then number must be 1
			// higher than last one.
			if (trackIndices.isEmpty() && indexNumber > 1 || !trackIndices.isEmpty() &&
				trackIndices.get(trackIndices.size() - 1).getNumber() != indexNumber - 1) {
				addWarning(input, WARNING_INVALID_INDEX_NUMBER);
			}

			List<Index> fileIndices = getLastFileData(input).getAllIndices();

			Position position = parsePosition(input, indexMatcher.group(2));

			// Position of first index of file must be 00:00:00.
			if (fileIndices.isEmpty() && !(position.getMinutes() == 0 && position.getSeconds() == 0 && position.getFrames() == 0)) {
				addWarning(input, WARNING_INVALID_FIRST_POSITION);
			}

			trackIndices.add(new Index(indexNumber, position));
		} else {
			addWarning(input, WARNING_UNPARSEABLE_INPUT);
		}
	}

	/**
	 * Parse the ISRC command.
	 *
	 * ISRC [code] International Standard Recording Code of track. Must come
	 * after TRACK, but before INDEX.
	 *
	 * @param input
	 */
	private static void parseIsrc(final LineOfInput input) {
		if (startsWith(input, "ISRC")) {
			String isrcCode = input.getInput().substring("ISRC".length()).trim();
			if (!PATTERN_ISRC_CODE.matcher(isrcCode).matches()) {
				addWarning(input, WARNING_NONCOMPLIANT_ISRC_CODE);
			}

			TrackData trackData = getLastTrackData(input);

			if (trackData.getIndices().size() > 0) {
				addWarning(input, WARNING_ISRC_IN_WRONG_PLACE);
			}

			if (trackData.getIsrcCode() != null) {
				addWarning(input, WARNING_DATUM_APPEARS_TOO_OFTEN);
			}

			trackData.setIsrcCode(isrcCode);
		} else {
			addWarning(input, WARNING_UNPARSEABLE_INPUT);
		}
	}

	/**
	 * Parse the PERFORMER command.
	 *
	 * PERFORMER [performer-string] Performer of album/TRACK.
	 *
	 * [performer-string] should be <= 80 character if you want to burn it to
	 * disc. If used before any TRACK fields, then it is the album artist. If
	 * after a TRACK field, then it is the performer of that track.
	 *
	 * @param input
	 */
	private static void parsePerformer(final LineOfInput input) {
		Matcher performerMatcher = PATTERN_PERFORMER.matcher(input.getInput());

		if (startsWith(input, "PERFORMER") && performerMatcher.matches()) {
			String performer = performerMatcher.group(1);

			if (performer.charAt(0) == '\"') {
				performer = performer.substring(1, performer.length() - 1);
			}

			if (performer.length() > 80) {
				addWarning(input, WARNING_FIELD_LENGTH_OVER_80);
			}

			// First check file data, as getLastFileData will create a FileData
			// instance if there is none
			// and we don't actually want to create such an instance.
			if (input.getAssociatedSheet().getFileData().size() == 0 || getLastFileData(input).getTrackData().size() == 0) {
				// Performer of album.
				if (input.getAssociatedSheet().getPerformer() != null) {
					addWarning(input, WARNING_DATUM_APPEARS_TOO_OFTEN);
				}

				input.getAssociatedSheet().setPerformer(performer);
			} else {
				// Performer of track.
				TrackData trackData = getLastTrackData(input);
				if (trackData.getPerformer() != null) {
					addWarning(input, WARNING_DATUM_APPEARS_TOO_OFTEN);
				}

				trackData.setPerformer(performer);
			}
		} else {
			addWarning(input, WARNING_UNPARSEABLE_INPUT);
		}
	}

	/**
	 * Parse the POSTGAP command.
	 *
	 * POSTGAP [mm:ss:ff] Must come after all INDEX fields for a track. Only one
	 * per track allowed.
	 *
	 * @param input
	 */
	private static void parsePostgap(final LineOfInput input) {
		Matcher postgapMatcher = PATTERN_POSTGAP.matcher(input.getInput());

		if (startsWith(input, "POSTGAP") && postgapMatcher.matches()) {
			TrackData trackData = getLastTrackData(input);
			if (trackData.getPostgap() != null) {
				addWarning(input, WARNING_DATUM_APPEARS_TOO_OFTEN);
			}

			trackData.setPostgap(parsePosition(input, postgapMatcher.group(1)));
		} else {
			addWarning(input, WARNING_UNPARSEABLE_INPUT);
		}
	}

	/**
	 * Parse the PREGAP command.
	 *
	 * PREGAP [mm:ss:ff] Must come after TRACK, but before INDEX fields for that
	 * track.
	 *
	 * @param input
	 */
	private static void parsePregap(final LineOfInput input) {
		Matcher pregapMatcher = PATTERN_PREGAP.matcher(input.getInput());

		if (startsWith(input, "PREGAP") && pregapMatcher.matches()) {
			TrackData trackData = getLastTrackData(input);
			if (trackData.getPregap() != null) {
				addWarning(input, WARNING_DATUM_APPEARS_TOO_OFTEN);
			}

			if (trackData.getIndices().size() > 0) {
				addWarning(input, WARNING_PREGAP_IN_WRONG_PLACE);
			}

			trackData.setPregap(parsePosition(input, pregapMatcher.group(1)));
		} else {
			addWarning(input, WARNING_UNPARSEABLE_INPUT);
		}
	}

	/**
	 * Parse the non-standard REM COMMENT command.
	 *
	 * REM COMMENT [comment]
	 *
	 * @param input
	 */
	private static void parseRemComment(final LineOfInput input) {
		Matcher matcher = PATTERN_REM_COMMENT.matcher(input.getInput());

		if (matcher.find()) {
			String comment = matcher.group(2);
			if (comment.charAt(0) == '"' && comment.charAt(comment.length() - 1) == '"') {
				comment = comment.substring(1, comment.length() - 1);
			}
			input.getAssociatedSheet().setComment(comment);
		} else {
			addWarning(input, WARNING_UNPARSEABLE_INPUT);
		}
	}

	/**
	 * Parse the non-standard REM DATE command.
	 *
	 * REM DATE [year]
	 *
	 * @param input
	 */
	private static void parseRemDate(final LineOfInput input) {
		Matcher matcher = PATTERN_REM_DATE.matcher(input.getInput());

		if (matcher.find()) {
			int year = Integer.parseInt(matcher.group(2));
			if (year < 1 || year > 9999) {
				addWarning(input, WARNING_INVALID_YEAR);
			}
			input.getAssociatedSheet().setYear(year);
		} else {
			addWarning(input, WARNING_UNPARSEABLE_INPUT);
		}
	}

	/**
	 * Parse the non-standard REM DISCID command.
	 *
	 * REM DISCID [discid]
	 *
	 * @param input
	 */
	private static void parseRemDiscid(final LineOfInput input) {
		Matcher matcher = PATTERN_REM_DISCID.matcher(input.getInput());

		if (matcher.find()) {
			String discid = matcher.group(2);
			if (discid.charAt(0) == '"' && discid.charAt(discid.length() - 1) == '"') {
				discid = discid.substring(1, discid.length() - 1);
			}
			input.getAssociatedSheet().setDiscid(discid);
		} else {
			addWarning(input, WARNING_UNPARSEABLE_INPUT);
		}
	}

	/**
	 * Parse the non-standard REM DISCNUMBER command.
	 *
	 * REM DISCNUMBER [discNumber]
	 *
	 * @param input
	 */
	private static void parseRemDiscNumber(final LineOfInput input) {
		Matcher matcher = PATTERN_REM_DISCNUMBER.matcher(input.getInput());

		if (matcher.find()) {
			int discNumber = Integer.parseInt(matcher.group(2));
			if (discNumber < 1) {
				addWarning(input, WARNING_INVALID_DISCNUMBER);
			}
			input.getAssociatedSheet().setDiscNumber(discNumber);
		} else {
			addWarning(input, WARNING_UNPARSEABLE_INPUT);
		}
	}

	/**
	 * Parse the non-standard REM GENRE command.
	 *
	 * REM GENRE [genre]
	 *
	 * @param input
	 */
	private static void parseRemGenre(final LineOfInput input) {
		Matcher matcher = PATTERN_REM_GENRE.matcher(input.getInput());

		if (matcher.find()) {
			String genre = matcher.group(2);
			if (genre.charAt(0) == '"' && genre.charAt(genre.length() - 1) == '"') {
				genre = genre.substring(1, genre.length() - 1);
			}
			input.getAssociatedSheet().setGenre(genre);
		} else {
			addWarning(input, WARNING_UNPARSEABLE_INPUT);
		}
	}

	/**
	 * Parse the non-standard REM TOTALDISCS command.
	 *
	 * REM TOTALDISCS [totalDiscs]
	 *
	 * @param input
	 */
	private static void parseRemTotalDiscs(final LineOfInput input) {
		Matcher matcher = PATTERN_REM_TOTALDISCS.matcher(input.getInput());

		if (matcher.find()) {
			int totalDiscs = Integer.parseInt(matcher.group(2));
			if (totalDiscs < 1) {
				addWarning(input, WARNING_INVALID_TOTALDISCS);
			}
			input.getAssociatedSheet().setTotalDiscs(totalDiscs);
		} else {
			addWarning(input, WARNING_UNPARSEABLE_INPUT);
		}
	}

	/**
	 * Parse the REM command. Will also parse a number of non-standard commands
	 * used by Exact Audio Copy.
	 *
	 * REM [comment]
	 *
	 * Or the non-standard commands:
	 *
	 * REM COMMENT [comment] REM DATE [year] REM DISCID [discid] REM GENRE
	 * [genre]
	 *
	 * @param input
	 */
	private static void parseRem(final LineOfInput input) {
		if (startsWith(input, "REM")) {
			// This is a comment, but popular implementation like Exact Audio
			// Copy may still
			// embed information here. We'll try to parse this, but we'll
			// silently accept anything.
			// There will be no warnings or errors, except for case mismatches.

			String comment = input.getInput().substring("REM".length()).trim();

			switch (comment.charAt(0)) {
				case 'c':
				case 'C':
					if (contains(input, PATTERN_REM_COMMENT)) {
						parseRemComment(input);
					}
					break;
				case 'd':
				case 'D':
					if (contains(input, PATTERN_REM_DATE)) {
						parseRemDate(input);
					} else if (contains(input, PATTERN_REM_DISCID)) {
						parseRemDiscid(input);
					} else if (contains(input, PATTERN_REM_DISCNUMBER)) {
						parseRemDiscNumber(input);
					}
					break;
				case 'g':
				case 'G':
					if (contains(input, PATTERN_REM_GENRE)) {
						parseRemGenre(input);
					}
					break;
				case 't':
				case 'T':
					if (contains(input, PATTERN_REM_TOTALDISCS)) {
						parseRemTotalDiscs(input);
					}
					break;
			}
		} else {
			addWarning(input, WARNING_UNPARSEABLE_INPUT);
		}
	}

	/**
	 * Parse the SONGWRITER command.
	 *
	 * SONGWRITER [songwriter-string] Songwriter of CD/TRACK.
	 * [songwriter-string] should be <= 80 character if you want to burn it to
	 * disc. If used before any TRACK fields, then it is the album writer. If
	 * after a TRACK field, then it is the writer of that track.
	 *
	 * @param input
	 */
	private static void parseSongwriter(final LineOfInput input) {
		Matcher songwriterMatcher = PATTERN_SONGWRITER.matcher(input.getInput());

		if (startsWith(input, "SONGWRITER") && songwriterMatcher.matches()) {
			String songwriter = songwriterMatcher.group(1);

			if (songwriter.charAt(0) == '\"') {
				songwriter = songwriter.substring(1, songwriter.length() - 1);
			}

			if (songwriter.length() > 80) {
				addWarning(input, WARNING_FIELD_LENGTH_OVER_80);
			}

			// First check file data, as getLastFileData will create a FileData
			// instance if there is none
			// and we don't actually want to create such an instance.
			if (input.getAssociatedSheet().getFileData().size() == 0 || getLastFileData(input).getTrackData().size() == 0) {
				// Songwriter of album.
				if (input.getAssociatedSheet().getSongwriter() != null) {
					addWarning(input, WARNING_DATUM_APPEARS_TOO_OFTEN);
				}

				input.getAssociatedSheet().setSongwriter(songwriter);
			} else {
				// Songwriter of track.
				TrackData trackData = getLastTrackData(input);
				if (trackData.getSongwriter() != null) {
					addWarning(input, WARNING_DATUM_APPEARS_TOO_OFTEN);
				}

				trackData.setSongwriter(songwriter);
			}
		} else {
			addWarning(input, WARNING_UNPARSEABLE_INPUT);
		}
	}

	/**
	 * Parse the TITLE command.
	 *
	 * TITLE [title-string] Title of CD/TRACK. [title-string] should be <= 80
	 * character if you want to burn it to disc. If used before any TRACK
	 * fields, then it is the album title. If after a TRACK field, then it is
	 * the title of that track.
	 *
	 * @param input
	 */
	private static void parseTitle(final LineOfInput input) {
		Matcher titleMatcher = PATTERN_TITLE.matcher(input.getInput());

		if (startsWith(input, "TITLE") && titleMatcher.matches()) {
			String title = titleMatcher.group(1);

			if (title.charAt(0) == '\"') {
				title = title.substring(1, title.length() - 1);
			}

			if (title.length() > 80) {
				addWarning(input, WARNING_FIELD_LENGTH_OVER_80);
			}

			// First check file data, as getLastFileData will create a FileData
			// instance if there is none
			// and we don't actually want to create such an instance.
			if (input.getAssociatedSheet().getFileData().size() == 0 || getLastFileData(input).getTrackData().size() == 0) {
				// Title of album.
				if (input.getAssociatedSheet().getTitle() != null) {
					addWarning(input, WARNING_DATUM_APPEARS_TOO_OFTEN);
				}

				input.getAssociatedSheet().setTitle(title);
			} else {
				// Title of track.
				TrackData trackData = getLastTrackData(input);
				if (trackData.getTitle() != null) {
					addWarning(input, WARNING_DATUM_APPEARS_TOO_OFTEN);
				}

				trackData.setTitle(title);
			}
		} else {
			addWarning(input, WARNING_UNPARSEABLE_INPUT);
		}
	}

	/**
	 * Parse the TRACK command.
	 *
	 * TRACK [number] [datatype] Beginning of track data. First track number may
	 * be > 1, but all others must be sequential. Allowed are 1-99 inclusive.
	 *
	 * Modes recognized by the spec. (Others will be parsed, but will also cause
	 * a warning to be raised.) AUDIO - Audio/Music (2352) CDG - Karaoke CD+G
	 * (2448) MODE1/2048 - CDROM Mode1 Data (cooked) MODE1/2352 - CDROM Mode1
	 * Data (raw) MODE2/2336 - CDROM-XA Mode2 Data MODE2/2352 - CDROM-XA Mode2
	 * Data CDI/2336 - CDI Mode2 Data CDI/2352 - CDI Mode2 Data
	 *
	 * @param input
	 */
	private static void parseTrack(final LineOfInput input) {
		Matcher trackMatcher = PATTERN_TRACK.matcher(input.getInput());

		if (startsWith(input, "TRACK") && trackMatcher.matches()) {
			if (trackMatcher.group(1).length() != 2) {
				addWarning(input, WARNING_WRONG_NUMBER_OF_DIGITS);
			}
			int trackNumber = Integer.parseInt(trackMatcher.group(1));

			String dataType = trackMatcher.group(2);
			if (!COMPLIANT_DATA_TYPES.contains(dataType)) {
				addWarning(input, WARNING_NONCOMPLIANT_DATA_TYPE);
			}

			List<TrackData> trackDataList = input.getAssociatedSheet().getAllTrackData();

			// First track must have number 1; all next ones sequential.
			if (trackDataList.isEmpty() && trackNumber != 1 || !trackDataList.isEmpty() &&
				trackDataList.get(trackDataList.size() - 1).getNumber() != trackNumber - 1) {
				addWarning(input, WARNING_INVALID_TRACK_NUMBER);
			}

			FileData lastFileData = getLastFileData(input);
			lastFileData.getTrackData().add(new TrackData(lastFileData, trackNumber, dataType));
		} else {
			addWarning(input, WARNING_UNPARSEABLE_INPUT);
		}
	}

	/**
	 * Parse a position, as used by several commands.
	 *
	 * [mm:ss:ff] mm = minutes ss = seconds ff = frames (75 per second)
	 *
	 * @param input
	 */
	private static Position parsePosition(final LineOfInput input, final String position) {
		Matcher positionMatcher = PATTERN_POSITION.matcher(position);

		if (positionMatcher.matches()) {
			String minutesString = positionMatcher.group(1);
			String secondsString = positionMatcher.group(2);
			String framesString = positionMatcher.group(3);
			int minutes = Integer.parseInt(minutesString);
			int seconds = Integer.parseInt(secondsString);
			int frames = Integer.parseInt(framesString);

			if (!(minutesString.length() == 2 && secondsString.length() == 2 && framesString.length() == 2)) {
				addWarning(input, WARNING_WRONG_NUMBER_OF_DIGITS);
			}

			if (seconds > 59) {
				addWarning(input, WARNING_INVALID_SECONDS_VALUE);
			}

			if (frames > 74) {
				addWarning(input, WARNING_INVALID_FRAMES_VALUE);
			}

			Position result = new Position(minutes, seconds, frames);
			return result;
		} else {
			addWarning(input, WARNING_UNPARSEABLE_INPUT);
			return new Position();
		}
	}

	/**
	 * Get the last {@link org.digitalmediaserver.cuelib.TrackData} element. If
	 * none exist, an empty one is created and a warning added.
	 *
	 * @param input
	 * @return The last {@link org.digitalmediaserver.cuelib.TrackData} element.
	 *         If none exist, an empty one is created and a warning added.
	 */
	private static TrackData getLastTrackData(final LineOfInput input) {
		FileData lastFileData = getLastFileData(input);
		List<TrackData> trackDataList = lastFileData.getTrackData();

		if (trackDataList.size() == 0) {
			trackDataList.add(new TrackData(lastFileData));
			addWarning(input, WARNING_NO_TRACK_SPECIFIED);
		}

		return trackDataList.get(trackDataList.size() - 1);
	}

	/**
	 * Get the last {@link org.digitalmediaserver.cuelib.FileData} element. If
	 * none exist, an empty one is created and a warning added.
	 *
	 * @param input
	 * @return The last {@link org.digitalmediaserver.cuelib.FileData} element.
	 *         If none exist, an empty one is created and a warning added.
	 */
	private static FileData getLastFileData(final LineOfInput input) {
		List<FileData> fileDataList = input.getAssociatedSheet().getFileData();

		if (fileDataList.size() == 0) {
			fileDataList.add(new FileData(input.getAssociatedSheet()));
			addWarning(input, WARNING_NO_FILE_SPECIFIED);
		}

		return fileDataList.get(fileDataList.size() - 1);
	}

	/**
	 * Write a warning to the {@link org.digitalmediaserver.cuelib.CueSheet}
	 * associated with the {@link org.digitalmediaserver.cuelib.LineOfInput}.
	 *
	 * @param input The {@link org.digitalmediaserver.cuelib.LineOfInput} the
	 *            warning pertains to.
	 * @param warning The warning to write.
	 */
	private static void addWarning(final LineOfInput input, final String warning) {
		LOGGER.warn(warning);
		input.getAssociatedSheet().addWarning(input, warning);
	}

	/**
	 * Parse all .cue files in the user's working directory and print any
	 * warnings to standard out.
	 *
	 * @param args
	 */
	public static void main(final String[] args) {
		CueSheet sheet = null;

		try {
			CueSheetToXmlSerializer xmlSerializer = new CueSheetToXmlSerializer();

			FileFilter cueFilter = new FileFilter() {

				@Override
				public boolean accept(final File file) {
					return file.getName().length() >= 4 && file.getName().substring(file.getName().length() - 4).equalsIgnoreCase(".cue");
				}
			};

			List<File> files = new ArrayList<File>();
			File[] filesFound = null;

			File workingDir = new File(System.getProperty("user.dir"));

			filesFound = workingDir.listFiles(cueFilter);
			if (filesFound != null) {
				files.addAll(Arrays.asList(filesFound));
			}

			for (File file : files) {
				CueParser.LOGGER.info("Processing file: '" + file.toString() + "'");
				sheet = CueParser.parse(file);

				for (Message message : sheet.getMessages()) {
					System.out.println(message);
				}

				System.out.println((new CueSheetSerializer()).serializeCueSheet(sheet));
				xmlSerializer.serializeCueSheet(sheet, System.out);
			}
		} catch (Exception e) {
			LOGGER.error("", e);
		}
	}
}