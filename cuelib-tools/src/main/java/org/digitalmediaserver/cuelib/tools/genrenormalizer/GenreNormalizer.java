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
package org.digitalmediaserver.cuelib.tools.genrenormalizer;

import java.util.HashMap;
import java.util.Map;


/**
 * The Class GenreNormalizer.
 */
public class GenreNormalizer {

	/**
	 * Enum for specifying various search modes.
	 */
	public static enum SearchMode {

		/** The strict. */
		STRICT,

		/** The normal. */
		NORMAL,

		/** The heuristic. */
		HEURISTIC
	}

	/**
	 * All supported genres, indexed as per ID3v1.
	 */
	private static final String[] GENRES = {
		"Blues", "Classic Rock", "Country", "Dance", "Disco", "Funk", "Grunge", "Hip-Hop", "Jazz",
		"Metal", "New Age", "Oldies", "Other", "Pop", "R&B", "Rap", "Reggae", "Rock", "Techno", "Industrial", "Alternative", "Ska",
		"Death Metal", "Pranks", "Soundtrack", "Euro-Techno", "Ambient", "Trip-Hop", "Vocal", "Jazz+Funk", "Fusion", "Trance",
		"Classical", "Instrumental", "Acid", "House", "Game", "Sound Clip", "Gospel", "Noise", "Alternative Rock", "Bass", "Punk",
		"Space", "Meditative", "Instrumental Pop", "Instrumental Rock", "Ethnic", "Gothic", "Darkwave", "Techno-Industrial",
		"Electronic", "Pop-Folk", "Eurodance", "Dream", "Southern Rock", "Comedy", "Cult", "Gangsta", "Top 40", "Christian Rap",
		"Pop/Funk", "Jungle", "Native US", "Cabaret", "New Wave", "Psychedelic", "Rave", "Showtunes", "Trailer", "Lo-Fi", "Tribal",
		"Acid Punk", "Acid Jazz", "Polka", "Retro", "Musical", "Rock & Roll", "Hard Rock", "Folk", "Folk-Rock", "National Folk",
		"Swing", "Fast Fusion", "Bebob", "Latin", "Revival", "Celtic", "Bluegrass", "Avantgarde", "Gothic Rock", "Progressive Rock",
		"Psychedelic Rock", "Symphonic Rock", "Slow Rock", "Big Band", "Chorus", "Easy Listening", "Acoustic", "Humour", "Speech",
		"Chanson", "Opera", "Chamber Music", "Sonata", "Symphony", "Booty Bass", "Primus", "Porn Groove", "Satire", "Slow Jam", "Club",
		"Tango", "Samba", "Folklore", "Ballad", "Power Ballad", "Rhythmic Soul", "Freestyle", "Duet", "Punk Rock", "Drum Solo",
		"Acappella", "Euro-House", "Dance Hall", "Goa", "Drum & Bass", "Club-House", "Hardcore", "Terror", "Indie", "BritPop",
		"Negerpunk", "Polsk Punk", "Beat", "Christian Gangsta", "Heavy Metal", "Black Metal", "Crossover", "Contemporary Christian",
		"Christian Rock", "Merengue", "Salsa", "Thrash Metal", "Anime", "JPop", "SynthPop"
	};

	/**
	 * First genre index that is an extension of WinAmp (added to ID3v1). Next
	 * follows a block of such genres. Everything before is original ID3v1.
	 */
	private static int firstWinAmpExtensionIndex = 80;

	/**
	 * First genre index that is an extension of LAME. Next follows a block of
	 * such genres. The indices directly before this are a block of WinAmp
	 * extensions (added to ID3v1).
	 */
	private static int firstLameExtensionIndex = 126;

	/**
	 * Maps the uppercase genre to its ID3 index.
	 */
	private static Map<String, Integer> genreSignatureToID3Index = new HashMap<String, Integer>();

	static {
		for (int index = 0; index < GenreNormalizer.GENRES.length; index++) {
			GenreNormalizer.genreSignatureToID3Index.put(GenreNormalizer.getGenreSignature(GenreNormalizer.GENRES[index]), index);
		}
	}

	/**
	 * Create a new GenreNormalizer. Should never be called.
	 */
	private GenreNormalizer() {
	}

	/**
	 * Get a signature of the genre. Two genres with the same signature are
	 * considered identical in the normal search mode.
	 * <p>
	 * The current implementation simply returns the genre string with all
	 * letters turned to uppercase, and all other characters removed.
	 *
	 * @param genre
	 * @return The signature of the genre.
	 */
	private static String getGenreSignature(String genre) {
		StringBuilder builder = new StringBuilder(genre.length());
		for (int index = 0; index < genre.length(); index++) {
			char currentChar = genre.charAt(index);
			if (Character.isLetter(currentChar)) {
				builder.append(Character.toUpperCase(currentChar));
			}
		}
		String result = builder.toString();
		return result;
	}

	/**
	 * Get the code for the specified genre. This is a number as per ID3v1. If
	 * no matching code can be found, then -1 is returned.
	 *
	 * @param genreDescription The genre description.
	 * @param allowWinAmpExtensions Allow the WinAmp genre extensions. These
	 *            were later incorporated in ID3v1.
	 * @param allowLameExtensions Allow the LAME genre extensions. These are not
	 *            part of ID3v1.
	 * @param searchMode How to search for a matching code. STRICT requires the
	 *            genre description to be exactly as specified; NORMAL will
	 *            tolerate differences in case and characters other than
	 *            letters; HEURISTIC will try to find an appropriate code if the
	 *            NORMAL method fails.
	 * @return The code for the specified genre, or -1 if no matching code can
	 *         be found.
	 */
	public static int getGenreCode(
		String genreDescription,
		boolean allowWinAmpExtensions,
		boolean allowLameExtensions,
		SearchMode searchMode
	) {
		int result = -1;

		String inputGenreSignature = getGenreSignature(genreDescription);
		Integer index = GenreNormalizer.genreSignatureToID3Index.get(inputGenreSignature);

		if (index == null) {
			// No match. Try a heuristic, provided we're allowed to.
			if (searchMode == SearchMode.HEURISTIC) {
				// See if the signature of this genre contains the signature of a known genre, or vice versa. If so,
				// treat as that genre.
				// Go backward through the list, as the later numbers are generally more specific.
				int maxGenre;
				if (!allowWinAmpExtensions) {
					maxGenre = GenreNormalizer.firstWinAmpExtensionIndex - 1;
				} else if (!allowLameExtensions) {
					maxGenre = GenreNormalizer.firstLameExtensionIndex - 1;
				} else {
					maxGenre = GenreNormalizer.GENRES.length - 1;
				}

				for (int heuristicGenreIndex = maxGenre; heuristicGenreIndex >= 0 && result == -1; heuristicGenreIndex--) {
					if (
						inputGenreSignature.contains(getGenreSignature(GenreNormalizer.GENRES[heuristicGenreIndex])) ||
						getGenreSignature(GenreNormalizer.GENRES[heuristicGenreIndex]).contains(inputGenreSignature)
					) {
						result = heuristicGenreIndex;
					}
				}
			}
		} else {
			// We have a match.
			result = index;

			// If the search mode is strict, then make sure the genre name is completely identical.
			if (searchMode == SearchMode.STRICT && !genreDescription.equals(GenreNormalizer.GENRES[index])) {
				result = -1;
			}

			// Check if we're not returning an index that isn't allowed.
			if (!allowWinAmpExtensions && result >= GenreNormalizer.firstWinAmpExtensionIndex) {
				result = -1;
			}

			if (!allowLameExtensions && result >= GenreNormalizer.firstLameExtensionIndex) {
				result = -1;
			}
		}

		// If we still have no result, and we're using the heuristic search, then use code 12: "Other".
		if (result == -1 && searchMode == SearchMode.HEURISTIC) {
			result = 12;
		}

		return result;
	}

	/**
	 * Get the description for the specified genre. This is a string as per
	 * ID3v1. If no matching description can be found, then null is returned.
	 *
	 * @param genreCode The genre code.
	 * @param allowWinAmpExtensions Allow the WinAmp genre extensions. These
	 *            were later incorporated in ID3v1.
	 * @param allowLameExtensions Allow the LAME genre extensions. These are not
	 *            part of ID3v1.
	 * @return The description for the specified genre, or null if no matching
	 *         description can be found.
	 */
	public static String getGenreDescription(int genreCode, boolean allowWinAmpExtensions, boolean allowLameExtensions) {
		String result = null;

		// Make sure genreCode is a valid array index, and it doesn't go into extension blocks it isn't allowed into.
		if (
			genreCode >= 0 && genreCode < GenreNormalizer.GENRES.length &&
			(allowWinAmpExtensions || genreCode < GenreNormalizer.firstWinAmpExtensionIndex) &&
			(allowLameExtensions || genreCode < GenreNormalizer.firstLameExtensionIndex)
		) {
			result = GenreNormalizer.GENRES[genreCode];
		}

		return result;
	}

	/**
	 * Normalize the genre description as per the ID3/LAME specification. If no
	 * matching genre can be found, then null is returned.
	 *
	 * @param genreDescription The genre description.
	 * @param allowWinAmpExtensions Allow the WinAmp genre extensions. These
	 *            were later incorporated in ID3v1.
	 * @param allowLameExtensions Allow the LAME genre extensions. These are not
	 *            part of ID3v1.
	 * @return The normalized description for the specified genre, or null if no
	 *         matching code can be found.
	 */
	public static String normalizeGenreDescription(
		String genreDescription,
		boolean allowWinAmpExtensions,
		boolean allowLameExtensions
	) {
		return normalizeGenreDescription(
			genreDescription,
			allowWinAmpExtensions,
			allowLameExtensions,
			GenreNormalizer.SearchMode.HEURISTIC
		);
	}

	/**
	 * Normalize the genre description as per the ID3/LAME specification. If no
	 * matching genre can be found, then null is returned.
	 *
	 * @param genreDescription The genre description.
	 * @param allowWinAmpExtensions Allow the WinAmp genre extensions. These
	 *            were later incorporated in ID3v1.
	 * @param allowLameExtensions Allow the LAME genre extensions. These are not
	 *            part of ID3v1.
	 * @param searchMode How to search for a matching code. STRICT requires the
	 *            genre description to be exactly as specified; NORMAL will
	 *            tolerate differences in case and characters other than
	 *            letters; HEURISTIC will try to find an appropriate code if the
	 *            NORMAL method fails. It is generally advised to use HEURISTIC.
	 * @return The normalized description for the specified genre, or null if no
	 *         matching code can be found.
	 */
	public static String normalizeGenreDescription(
		String genreDescription,
		boolean allowWinAmpExtensions,
		boolean allowLameExtensions,
		SearchMode searchMode
	) {
		return getGenreDescription(
			GenreNormalizer.getGenreCode(
				genreDescription,
				allowWinAmpExtensions,
				allowLameExtensions,
				searchMode),
			allowWinAmpExtensions,
			allowLameExtensions
		);
	}
}
