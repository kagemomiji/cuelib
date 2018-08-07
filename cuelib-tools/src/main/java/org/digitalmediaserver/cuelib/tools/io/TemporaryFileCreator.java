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
package org.digitalmediaserver.cuelib.tools.io;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Utility class for creating temporary files.
 *
 * @author jwbroek
 */
public class TemporaryFileCreator {

	/**
	 * Not to be instantiated.
	 */
	private TemporaryFileCreator() {
	}

	/**
	 * The logger for this class.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(TemporaryFileCreator.class);

	/**
	 * Counter for added to the names of temporary files and directories.
	 */
	private static final AtomicInteger COUNTER = new AtomicInteger();

	/**
	 * Create a temporary directory based on the information provided. The
	 * directory will be deleted when the VM ends. An effort is made to avoid
	 * naming conflicts, but such a conflict cannot be guaranteed to be avoided.
	 * When a conflict occurs, an exception will be thrown.
	 *
	 * @return A temporary directory, as specified.
	 * @throws IOException Thrown when the directory could not be created.
	 * @throws SecurityException Thrown when a {@link SecurityManager} does not
	 *             allow the temporary directory to be created.
	 */
	public static File createTemporaryDirectory() throws IOException, SecurityException {
		return createTemporaryDirectory(null);
	}

	/**
	 * Create a temporary directory based on the information provided. The
	 * directory will be deleted when the VM ends. An effort is made to avoid
	 * naming conflicts, but such a conflict cannot be guaranteed to be avoided.
	 * When a conflict occurs, an exception will be thrown.
	 *
	 * @param baseDir The directory to create the temporary directory in.
	 * @return A temporary directory, as specified.
	 * @throws IOException Thrown when the directory could not be created.
	 * @throws SecurityException Thrown when a {@link SecurityManager} does not
	 *             allow the temporary directory to be created.
	 */
	public static File createTemporaryDirectory(File baseDir) throws IOException, SecurityException {
		return createTemporaryFileOrDirectory("TemporaryFileCreator", null, baseDir, true, false, 5);
	}

	/**
	 * Create a temporary file based on the information provided. The file will
	 * be deleted when the VM ends. An effort is made to avoid naming conflicts,
	 * but such a conflict cannot be guaranteed to be avoided. When a conflict
	 * occurs, an exception will be thrown. It is not guaranteed that the name
	 * conforms exactly to what is specified.
	 *
	 * @return A temporary file, as specified.
	 * @throws IOException Thrown when the file could not be created.
	 * @throws SecurityException Thrown when a {@link SecurityManager} does not
	 *             allow the temporary file to be created.
	 */
	public static File createTemporaryFile() throws IOException, SecurityException {
		return createTemporaryFile(null);
	}

	/**
	 * Create a temporary file based on the information provided. The file will
	 * be deleted when the VM ends. An effort is made to avoid naming conflicts,
	 * but such a conflict cannot be guaranteed to be avoided. When a conflict
	 * occurs, an exception will be thrown. It is not guaranteed that the name
	 * conforms exactly to what is specified.
	 *
	 * @param baseDir The directory to create the temporary directory in.
	 * @return A temporary file, as specified.
	 * @throws IOException Thrown when the file could not be created.
	 * @throws SecurityException Thrown when a {@link SecurityManager} does not
	 *             allow the temporary file to be created.
	 */
	public static File createTemporaryFile(File baseDir) throws IOException, SecurityException {
		return createTemporaryFileOrDirectory("TemporaryFileCreator", null, baseDir, false, false, 5);
	}

	/**
	 * Create a temporary file or directory based on the information provided.
	 * The file or directory will be deleted when the VM ends. An effort is made
	 * to avoid naming conflicts, but such a conflict cannot be guaranteed to be
	 * avoided. When a conflict occurs, an exception will be thrown.
	 *
	 * @param prefix The prefix for the temporary file.
	 * @param suffix The suffix for the temporary file. May be null, in which
	 *            case it will default to ".tmp".
	 * @param directory The directory to create the file in. May be null, in
	 *            which case the default directory for temporary files will be
	 *            used.
	 * @param maxAttempts The maximum number of attempts to create a temporary
	 *            file. Whenever the temporary file cannot be created due to an
	 *            IOException (most likely caused by a naming conflict), another
	 *            attempt with a new name is made, up to the maximum number of
	 *            attempts.
	 * @param createDirectory If true, then a directory will be created,
	 *            otherwise a file will be created.
	 * @param exactName Whether or not to guarantee that the exact name as
	 *            specified is used for the temporary file. When false, this
	 *            method is more likely to succeed.
	 * @return A temporary file, as specified.
	 * @throws IOException Thrown when the file or directory could not be
	 *             created, even after the specified number of attempts.
	 * @throws IllegalArgumentException Thrown when maxAttempts is smaller than
	 *             1.
	 * @throws SecurityException Thrown when a {@link SecurityManager} does not
	 *             allow the temporary file or directory to be created.
	 */
	public static File createTemporaryFileOrDirectory(
		String prefix,
		String suffix,
		File directory,
		boolean createDirectory,
		boolean exactName,
		int maxAttempts
	) throws IOException, IllegalArgumentException, SecurityException {

		IOException ioException = null;
		File result = null;

		if (maxAttempts < 1) {
			// TODO This error message should come from a ResourceBundle.
			throw new IllegalArgumentException("maxAttempts must be at least 1.");
		}

		// The filename consists of the prefix, a random number in hex, and a number from the counter.
		// This is probably a little overkill, but you never know...
		StringBuilder nameBuilder = new StringBuilder(prefix)
			.append(Double.toHexString(Math.random()))
			.append(COUNTER.incrementAndGet());

		// Make the specified number of attempt to create a temporary file.
		for (int attempt = 0; result == null && attempt < maxAttempts; attempt++) {
			try {
				result = createNamedTemporaryFileOrDirectory(
					nameBuilder.toString(),
					suffix,
					directory,
					createDirectory,
					exactName
				);
			} catch (IOException e) {
				// Save the exception, in case this is the last allowed attempt.
				ioException = e;
				LOGGER.trace("", e);
			}
		}

		// If we have no result, then that must be because an exception was thrown. We'll rethrow it.
		if (result == null && ioException != null) {
			throw ioException;
		}

		return result;
	}

	/**
	 * Create a temporary file or directory based on the information provided.
	 * The file or directory will be deleted when the VM ends.
	 *
	 * @param name The name for the temporary file or directory.
	 * @param suffix The suffix for the temporary file. May be null, in which
	 *            case it will default to ".tmp" for files and empty string for
	 *            directories.
	 * @param directory The directory to create the file in. May be null, in
	 *            which case the default directory for temporary files will be
	 *            used.
	 * @param createDirectory If true, then a directory will be created,
	 *            otherwise a file will be created.
	 * @param exactName Whether or not to guarantee that the exact name as
	 *            specified is used for the temporary file. When false, this
	 *            method is more likely to succeed.
	 * @return A temporary file, as specified.
	 * @throws IOException Thrown when the file or directory could not be
	 *             created.
	 * @throws SecurityException Thrown when a {@link SecurityManager} does not
	 *             allow the temporary file or directory to be created.
	 * @throws IllegalArgumentException Under unknown circumstances.
	 * @throws SecurityException If a security violation occurs.
	 */
	public static File createNamedTemporaryFileOrDirectory(
		String name,
		String suffix,
		File directory,
		boolean createDirectory,
		boolean exactName
	) throws IOException, IllegalArgumentException, SecurityException {

		File result = null;

		// Determine directory to create the file or directory in.
		File parentDirectory;
		if (directory == null) {
			parentDirectory = new File(System.getProperty("java.io.tmpdir"));
		} else {
			parentDirectory = directory;
		}

		if (createDirectory) {
			// We need to create a temporary directory.
			result = new File(parentDirectory, name + (suffix == null ? "" : suffix));
			if (!result.mkdir()) {
				// There was a problem creating the file.
				// TODO This error message should come from a ResourceBundle.
				throw new IOException("Could not create directory: '" + result.toString() + "'");
			}
		} else {
			// We need to create a temporary file.
			if (exactName) {
				result = new File(parentDirectory, name + (suffix == null ? ".tmp" : suffix));
				if (!result.createNewFile()) {
					throw new IOException("Failed to create \"" + result + "\"");
				}
			} else {
				result = File.createTempFile(name + (suffix == null ? ".tmp" : suffix), suffix, parentDirectory);
			}
		}

		// Request that the file is deleted after the VM ends.
		result.deleteOnExit();

		return result;
	}
}
