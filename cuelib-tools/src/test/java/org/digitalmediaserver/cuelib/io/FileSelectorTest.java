/*
 * Cuelib library for manipulating cue sheets. Copyright (C) 2007-2008
 * Jan-Willem van den Broek
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 */
package org.digitalmediaserver.cuelib.io;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import org.digitalmediaserver.cuelib.tools.io.FileSelector;
import org.digitalmediaserver.cuelib.tools.io.TemporaryFileCreator;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


/**
 * Unit test for {@link FileSelector}.
 *
 * @author jwbroek
 */
public class FileSelectorTest {

	/**
	 * The root of the testing environment.
	 */
	private File testRoot;

	/**
	 * Maximum number of files to create.
	 */
	private static final int MAX_FILES = 200;

	/**
	 * First file will have a name based on this number.
	 */
	private static final int FIRST_FILE = 1000;

	/**
	 * Maximum number of directories to create.
	 */
	private static final int MAX_DIRS = 30;

	/**
	 * First directories will have a name based on this number.
	 */
	private static final int FIRST_DIR = 2000;

	/**
	 * Prefix for numbered files in the root directory. Numbers range from
	 * {@link FileSelectorTest#FIRST_FILE} to {@link FileSelectorTest#FIRST_FILE}
	 * + {@link FileSelectorTest#MAX_FILES}.
	 */
	private static final String ROOT_FILE_PREFIX = "testFile";

	/**
	 * Prefix for numbered subdirs in the root directory. Numbers range from
	 * {@link FileSelectorTest#FIRST_DIR} to {@link FileSelectorTest#FIRST_DIR} +
	 * {@link FileSelectorTest#MAX_DIRS}.
	 */
	private static final String ROOT_SUBDIR_PREFIX = "testDir";

	/**
	 * Create a testing environment containing various files and directories.
	 * The layout is as follows: ([] is directory; {} is logical name (not actual name))
	 * <ul>
	 *   <li>[{testRoot}]</li>
	 *   <li>
	 *     <ul>
	 *       <li>{rootFilePrefix}{firstFile}</li>
	 *       <li>...</li>
	 *       <li>{rootFilePrefix}{firstFile+maxFiles-1}</li>
	 *       <li>{testRoot}file</li>
	 *       <li>[{rootFilePrefix}{firstDir}]</li>
	 *       <li>[...]</li>
	 *       <li>[testDir{firstDir+maxDirs-1}]</li>
	 *       <li>[{testRoot}]</li>
	 *       <li>[a]</li>
	 *       <li>
	 *         <ul>
	 *           <li>[a]</li>
	 *           <li>
	 *             <ul>
	 *               <li>a</li>
	 *               <li>[b1]</li>
	 *               <li>[b2]</li>
	 *               <li>b3</li>
	 *               <li>b4</li>
	 *             </ul>
	 *           </li>
	 *           <li>[b1]</li>
	 *           <li>[b2]</li>
	 *           <li>b3</li>
	 *           <li>b4</li>
	 *         </ul>
	 *       </li>
	 *     </ul>
	 *   </li>
	 * </ul>
	 *
	 * @throws IOException If an error occurs during the operation.
	 * @throws SecurityException it the test fails.
	 */
	@Before
	public void setUp() throws SecurityException, IOException {
		// File bound
		int fileBound = FIRST_FILE + MAX_FILES;
		// Dir bound
		int dirBound = FIRST_DIR + MAX_DIRS;

		// Create a temporary directory in which to create a test environment.
		this.testRoot = TemporaryFileCreator.createTemporaryDirectory();

		// Create numbered files.
		for (int fileIndex = FIRST_FILE; fileIndex < fileBound; fileIndex++) {
			TemporaryFileCreator.createNamedTemporaryFileOrDirectory(
				FileSelectorTest.ROOT_FILE_PREFIX + fileIndex,
				"",
				testRoot,
				false,
				true
			);
		}
		// Create a file with a name that starts with the name of the root
		// directory and ends in "file".
		TemporaryFileCreator.createNamedTemporaryFileOrDirectory(this.testRoot.getName() + "file", "", this.testRoot, false, true);

		// Create numbered directories.
		for (int dirIndex = FIRST_DIR; dirIndex < dirBound; dirIndex++) {
			TemporaryFileCreator.createNamedTemporaryFileOrDirectory("testDir" + dirIndex, "", this.testRoot, true, true);
		}
		// Create a directory with the same name as the root directory.
		TemporaryFileCreator.createNamedTemporaryFileOrDirectory(this.testRoot.getName(), "", this.testRoot, true, true);

		// Create a directory called "a" in the root.
		File newRootDir = TemporaryFileCreator.createNamedTemporaryFileOrDirectory("a", "", this.testRoot, true, true);
		// Create a subdirectory "a" in directory "a", as well as b1...b4.
		File subDir = TemporaryFileCreator.createNamedTemporaryFileOrDirectory("a", "", newRootDir, true, true);
		TemporaryFileCreator.createNamedTemporaryFileOrDirectory("b1", "", newRootDir, true, true);
		TemporaryFileCreator.createNamedTemporaryFileOrDirectory("b2", "", newRootDir, true, true);
		TemporaryFileCreator.createNamedTemporaryFileOrDirectory("b3", "", newRootDir, false, true);
		TemporaryFileCreator.createNamedTemporaryFileOrDirectory("b4", "", newRootDir, false, true);
		// Create a file "a" in directory "a/a", as well as b1...b4.
		TemporaryFileCreator.createNamedTemporaryFileOrDirectory("a", "", subDir, false, true);
		TemporaryFileCreator.createNamedTemporaryFileOrDirectory("b1", "", subDir, true, true);
		TemporaryFileCreator.createNamedTemporaryFileOrDirectory("b2", "", subDir, true, true);
		TemporaryFileCreator.createNamedTemporaryFileOrDirectory("b3", "", subDir, false, true);
		TemporaryFileCreator.createNamedTemporaryFileOrDirectory("b4", "", subDir, false, true);
	}

	/**
	 * Clean up the testing environment.
	 */
	@After
	public void cleanUp() {
		// Delete the temporary file. First check if creation went OK.
		if (testRoot != null && testRoot.exists()) {
			Path testFolder = testRoot.toPath();

			try {
				Files.walkFileTree(testFolder, new SimpleFileVisitor<Path>() {

					@Override
					public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
						Files.delete(file);
						return FileVisitResult.CONTINUE;
					}

					@Override
					public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
						Files.delete(dir);
						return FileVisitResult.CONTINUE;
					}
				});
			} catch (IOException e) {
				System.err.println("Couldn't delete test folder \"" + testRoot + "\": " + e.getMessage());
			}
		}
	}

	/**
	 * Test for {@link FileSelector#getDirsFilter()}.
	 *
	 * @throws IOException if the test fails.
	 */
	@Test
	public void testDirsFileFilter() throws IOException {
		FileFilter filter = FileSelector.getDirsFilter();
		Set<File> prediction = new HashSet<File>();
		int bound = FileSelectorTest.FIRST_DIR + FileSelectorTest.MAX_DIRS;
		for (int index = FileSelectorTest.FIRST_DIR; index < bound; index++) {
			prediction.add(new File(this.testRoot, FileSelectorTest.ROOT_SUBDIR_PREFIX + index));
		}
		prediction.add(new File(this.testRoot, this.testRoot.getName()));
		prediction.add(new File(this.testRoot, "a"));
		testFileFilter(this.testRoot, filter, prediction, "FileSelector.getDirsFilter()");
	}

	/**
	 * Test for {@link FileSelector#getFilesFilter()}.
	 *
	 * @throws IOException if the test fails.
	 */
	@Test
	public void testFilesFileFilter() throws IOException {
		FileFilter filter = FileSelector.getFilesFilter();
		Set<File> prediction = new HashSet<File>();
		int bound = FileSelectorTest.FIRST_FILE + FileSelectorTest.MAX_FILES;
		for (int index = FileSelectorTest.FIRST_FILE; index < bound; index++) {
			prediction.add(new File(this.testRoot, FileSelectorTest.ROOT_FILE_PREFIX + index));
		}
		prediction.add(new File(this.testRoot, this.testRoot.getName() + "file"));
		testFileFilter(this.testRoot, filter, prediction, "FileSelector.testFilesFileFilter()");
	}

	/**
	 * Test for {@link FileSelector#getFileNamePatternFilter(Pattern)}.
	 */
	@Test
	public void testFileNamePatternFilter() {
		// Create a FileNamePatternFilter that must match any file of which the
		// name begins with "test", then either
		// "File" or "Dir", then 3 digits, and then a "1" or "2". No other files
		// must be matched.
		FileFilter filter = FileSelector.getFileNamePatternFilter(Pattern.compile("test(?:File|Dir)\\d{3}[12].*"));
		Set<File> prediction = new HashSet<File>();

		// We can be clever and optimize these loops by using "%", greater
		// increments and some "if" statements. This is
		// much clearer though, and speed isn't much of an issue, so I'll leave
		// it like this.

		// Add all qualifying files.
		int fileBound = FileSelectorTest.FIRST_FILE + FileSelectorTest.MAX_FILES;
		for (int index = FileSelectorTest.FIRST_FILE; index < fileBound; index++) {
			if (index > 1000 && index < 10000 && (index % 10 == 1 || index % 10 == 2)) {
				prediction.add(new File(this.testRoot, FileSelectorTest.ROOT_FILE_PREFIX + index));
			}
		}

		// Add all qualifying directories.
		int dirBound = FileSelectorTest.FIRST_DIR + FileSelectorTest.MAX_DIRS;
		for (int index = FileSelectorTest.FIRST_DIR; index < dirBound; index++) {
			if (index > 1000 && index < 10000 && (index % 10 == 1 || index % 10 == 2)) {
				prediction.add(new File(this.testRoot, FileSelectorTest.ROOT_SUBDIR_PREFIX + index));
			}
		}
		testFileFilter(this.testRoot, filter, prediction, "FileSelector.getFileNamePatternFilter(Pattern)");
	}

	/**
	 * Create a Pattern that must match any file that starts with the name of
	 * its parent directory.
	 *
	 * @return A Pattern that must match any file that starts with the name of
	 *         its parent directory.
	 */
	public static Pattern getParentDirNameAsFileNamePattern() {
		String patternDirSeparator = Pattern.quote(File.separator);
		StringBuilder patternHelper = new StringBuilder(".*")
			.append(patternDirSeparator).append("([^").append(patternDirSeparator).append("]*)").append(patternDirSeparator)
			.append("\\1[^").append(patternDirSeparator).append("]*").append(patternDirSeparator).append("?$");
		return Pattern.compile(patternHelper.toString());
	}

	/**
	 * Test for {@link FileSelector#getPathPatternFilter(Pattern)}.
	 */
	@Test
	public void testPathPatternFilter() {
		// Create a FileNamePatternFilter that must match any file that starts
		// with the name of its parent directory.
		FileFilter filter = FileSelector.getPathPatternFilter(FileSelectorTest.getParentDirNameAsFileNamePattern());
		Set<File> prediction = new HashSet<File>();
		prediction.add(new File(this.testRoot, this.testRoot.getName() + "file"));
		prediction.add(new File(this.testRoot, this.testRoot.getName()));
		testFileFilter(this.testRoot, filter, prediction, "FileSelector.getPathPatternFilter(Pattern)");
	}

	/**
	 * Test for {@link FileSelector#getCombinedFileFilter(FileFilter[])} and
	 * {@link FileSelector#getCombinedFileFilter(Iterable)}.
	 */
	@SuppressWarnings("deprecation")
	@Test
	public void testCombinedFileFilterFromArray() {
		// Create a CombinedFileFilter that must match only files (not
		// directories) that start with the name
		// of their parent directory.
		FileFilter filesFilter = FileSelector.getFilesFilter();
		FileFilter pathPatternFilter = FileSelector.getPathPatternFilter(FileSelectorTest.getParentDirNameAsFileNamePattern());
		FileFilter combinedFileFilter = FileSelector.getCombinedFileFilter(filesFilter, pathPatternFilter);
		Set<File> prediction = new HashSet<File>();
		prediction.add(new File(this.testRoot, this.testRoot.getName() + "file"));
		testFileFilter(this.testRoot, combinedFileFilter, prediction, "FileSelector.getCombinedFileFilter(FileFilter[])");
	}

	/**
	 * Test for {@link FileSelector#getCombinedFileFilter(Iterable)}.
	 */
	@SuppressWarnings("deprecation")
	@Test
	public void testCombinedFileFilterFromIterable() {
		// Create a CombinedFileFilter that must match only files (not
		// directories) that start with the name
		// of their parent directory.
		FileFilter filesFilter = FileSelector.getFilesFilter();
		FileFilter pathPatternFilter = FileSelector.getPathPatternFilter(FileSelectorTest.getParentDirNameAsFileNamePattern());
		List<FileFilter> fileFilterList = new ArrayList<FileFilter>();
		fileFilterList.add(filesFilter);
		fileFilterList.add(pathPatternFilter);
		FileFilter combinedFileFilter = FileSelector.getCombinedFileFilter(fileFilterList);
		Set<File> prediction = new HashSet<File>();
		prediction.add(new File(this.testRoot, this.testRoot.getName() + "file"));
		testFileFilter(this.testRoot, combinedFileFilter, prediction, "FileSelector.getCombinedFileFilter(Iterable)");
	}

	/**
	 * Test for {@link FileSelector#getIntersectionFileFilter(FileFilter[])} and
	 * {@link FileSelector#getIntersectionFileFilter(Iterable)}.
	 */
	@Test
	public void testIntersectionFileFilterFromArray() {
		// Create an IntersectionFileFilter that must match only files (not
		// directories) that start with the name
		// of their parent directory.
		FileFilter filesFilter = FileSelector.getFilesFilter();
		FileFilter pathPatternFilter = FileSelector.getPathPatternFilter(FileSelectorTest.getParentDirNameAsFileNamePattern());
		FileFilter intersectionFileFilter = FileSelector.getIntersectionFileFilter(filesFilter, pathPatternFilter);
		Set<File> prediction = new HashSet<File>();
		prediction.add(new File(this.testRoot, this.testRoot.getName() + "file"));
		testFileFilter(this.testRoot, intersectionFileFilter, prediction, "FileSelector.getIntersectionFileFilter(FileFilter[])");
	}

	/**
	 * Test for {@link FileSelector#getIntersectionFileFilter(Iterable)}.
	 */
	@Test
	public void testIntersectionFileFilterFromIterable() {
		// Create an IntersectionFileFilter that must match only files (not
		// directories) that start with the name
		// of their parent directory.
		FileFilter filesFilter = FileSelector.getFilesFilter();
		FileFilter pathPatternFilter = FileSelector.getPathPatternFilter(FileSelectorTest.getParentDirNameAsFileNamePattern());
		List<FileFilter> fileFilterList = new ArrayList<FileFilter>();
		fileFilterList.add(filesFilter);
		fileFilterList.add(pathPatternFilter);
		FileFilter intersectionFileFilter = FileSelector.getIntersectionFileFilter(fileFilterList);
		Set<File> prediction = new HashSet<File>();
		prediction.add(new File(this.testRoot, this.testRoot.getName() + "file"));
		testFileFilter(this.testRoot, intersectionFileFilter, prediction, "FileSelector.getIntersectionFileFilter(Iterable)");
	}

	/**
	 * Test for {@link FileSelector#getUnionFileFilter(FileFilter[])} and
	 * {@link FileSelector#getUnionFileFilter(Iterable)}.
	 */
	@Test
	public void testUnionFileFilterFromArray() {
		// Create an UnionFileFilter that must match only files or directories
		// named "b1" or "b3".
		FileFilter unionFileFilter = FileSelector.getUnionFileFilter(
			FileSelector.getFileNamePatternFilter("b1"),
			FileSelector.getFileNamePatternFilter("b3")
		);
		Set<File> prediction = new HashSet<File>();
		prediction.add(new File(this.testRoot, "a/b1"));
		prediction.add(new File(this.testRoot, "a/b3"));
		testFileFilter(new File(this.testRoot, "a"), unionFileFilter, prediction, "FileSelector.getUnionFileFilter(FileFilter[])");
	}

	/**
	 * Test for {@link FileSelector#getUnionFileFilter(Iterable)}.
	 */
	@Test
	public void testUnionFileFilterFromIterable() {
		// Create an UnionFileFilter that must match only files or directories
		// named "b1" or "b3".
		FileFilter nameFilterB1 = FileSelector.getFileNamePatternFilter("b1");
		FileFilter nameFilterB3 = FileSelector.getFileNamePatternFilter("b3");
		List<FileFilter> fileFilterList = new ArrayList<FileFilter>();
		fileFilterList.add(nameFilterB1);
		fileFilterList.add(nameFilterB3);
		FileFilter unionFileFilter = FileSelector.getUnionFileFilter(fileFilterList);
		Set<File> prediction = new HashSet<File>();
		prediction.add(new File(this.testRoot, "a/b1"));
		prediction.add(new File(this.testRoot, "a/b3"));
		testFileFilter(new File(this.testRoot, "a"), unionFileFilter, prediction, "FileSelector.getUnionFileFilter(Iterable)");
	}

	/**
	 * Test for
	 * {@link FileSelector#selectFiles(File, Pattern, long, boolean, boolean)}
	 * with negative depth.
	 */
	@Test
	public void testSelectFilesWithNegativeDepth() {
		List<File> matchedFiles;
		Set<File> prediction = new HashSet<File>();

		// Should match no files due to depth of -1.
		matchedFiles = FileSelector.selectFiles(this.testRoot, Pattern.compile(".*"), -1, true, true);
		testFilesAgainstPrediction(matchedFiles, prediction, "FileSelector.selectFiles(File,Pattern,boolean,boolean); negative depth");
	}

	/**
	 * Test for
	 * {@link FileSelector#selectFiles(File, Pattern, long, boolean, boolean)}
	 * with depth zero.
	 */
	@Test
	public void testSelectFilesWithDepthZero() {
		List<File> matchedFiles;
		Set<File> prediction = new HashSet<File>();

		// Should match only root due to depth of 0.
		matchedFiles = FileSelector.selectFiles(this.testRoot, Pattern.compile(".*"), 0, true, true);
		prediction.add(this.testRoot);
		testFilesAgainstPrediction(matchedFiles, prediction, "FileSelector.selectFiles(File,Pattern,boolean,boolean); depth zero");
	}

	/**
	 * Test for
	 * {@link FileSelector#selectFiles(File, Pattern, long, boolean, boolean)}
	 * with depth zero and no consideration of base file.
	 */
	@Test
	public void testSelectFilesWithDepthZeroNoBaseFile() {
		List<File> matchedFiles;
		Set<File> prediction = new HashSet<File>();

		// Should match nothing due to considerBaseFile=false and depth = 0.
		matchedFiles = FileSelector.selectFiles(this.testRoot, Pattern.compile(".*"), 0, false, true);
		testFilesAgainstPrediction(matchedFiles, prediction,
			"FileSelector.selectFiles(File,Pattern,boolean,boolean); depth zero, no consideration of base file");
	}

	/**
	 * Test for
	 * {@link FileSelector#selectFiles(File, Pattern, long, boolean, boolean)}
	 * for all files at depth one of which the name starts with the name of the
	 * root.
	 */
	@Test
	public void testSelectFilesWithDepthOneAndNameStartingWithRootName() {
		List<File> matchedFiles;
		Set<File> prediction;

		// Match all files and directories in the root directory that have the
		// same name as the root.
		matchedFiles = FileSelector.selectFiles(this.testRoot, FileSelectorTest.getParentDirNameAsFileNamePattern(), 1, false, true);
		prediction = new HashSet<File>();
		prediction.add(new File(this.testRoot, this.testRoot.getName() + "file"));
		prediction.add(new File(this.testRoot, this.testRoot.getName()));
		testFilesAgainstPrediction(
			matchedFiles,
			prediction,
			"FileSelector.selectFiles(File,Pattern,boolean,boolean);" +
			" all files at depth one of which the name starts with the name of the root"
		);
	}

	/**
	 * Test for
	 * {@link FileSelector#selectFiles(File, Pattern, long, boolean, boolean)}
	 * for all files of which the name starts with the name of their parent.
	 */
	@Test
	public void testSelectFilesWithNameStartingWithParentName() {
		List<File> matchedFiles;
		Set<File> prediction;

		// Match all files and directories in the entire tree minus the root
		// that have a name that starts with
		// the name of their parent.
		matchedFiles = FileSelector.selectFiles(this.testRoot, FileSelectorTest.getParentDirNameAsFileNamePattern(), Integer.MAX_VALUE,
			false, true);
		prediction = new HashSet<File>();
		prediction.add(new File(this.testRoot, "a" + File.separator + "a"));
		prediction.add(new File(this.testRoot, "a" + File.separator + "a" + File.separator + "a"));
		prediction.add(new File(this.testRoot, this.testRoot.getName()));
		prediction.add(new File(this.testRoot, this.testRoot.getName() + "file"));
		testFilesAgainstPrediction(
			matchedFiles,
			prediction,
			"FileSelector.selectFiles(File,Pattern,boolean,boolean); " +
			"all files of which the name starts with the name of the root"
		);
	}

	/**
	 * Test for
	 * {@link FileSelector#selectFiles(File, FileFilter, List, long, boolean, boolean)}
	 * .
	 */
	@Test
	public void testSelectFilesFromFileFilter() {
		FileFilter filter = FileSelector.getFilesFilter();
		List<File> matchedFiles = new ArrayList<File>();
		Set<File> prediction = new HashSet<File>();
		int bound = FileSelectorTest.FIRST_FILE + FileSelectorTest.MAX_FILES;
		for (int index = FileSelectorTest.FIRST_FILE; index < bound; index++) {
			prediction.add(new File(this.testRoot, FileSelectorTest.ROOT_FILE_PREFIX + index));
		}
		prediction.add(new File(this.testRoot, this.testRoot.getName() + "file"));
		prediction.add(new File(this.testRoot, "a" + File.separator + "b3"));
		prediction.add(new File(this.testRoot, "a" + File.separator + "b4"));
		prediction.add(new File(this.testRoot, "a" + File.separator + "a" + File.separator + "a"));
		prediction.add(new File(this.testRoot, "a" + File.separator + "a" + File.separator + "b3"));
		prediction.add(new File(this.testRoot, "a" + File.separator + "a" + File.separator + "b4"));

		// Match all files.
		FileSelector.selectFiles(this.testRoot, filter, matchedFiles, Integer.MAX_VALUE, false, true);
		testFilesAgainstPrediction(
			matchedFiles,
			prediction,
			"FileSelector.selectFiles(File, FileFilter, List, long, boolean, boolean); " +
			"all files, but not directories"
		);
	}

	/**
	 * Test the specified {@link FileFilter}.
	 *
	 * @param directory The directory where the test must take place.
	 * @param filter The filter to test.
	 * @param predictedResult The files and directories predicted to pass the
	 *            filter.
	 * @param filterName The name of the filter. Is used for reporting purposes.
	 */
	private static void testFileFilter(File directory, FileFilter filter, Set<File> predictedResult, String filterName) {
		// Get the files that matched the filter.
		File[] matchedFiles = directory.listFiles(filter);

		Assert.assertNotNull(matchedFiles);
		testFilesAgainstPrediction(Arrays.asList(matchedFiles), predictedResult, filterName);
	}

	/**
	 * Test the specified set of {@link File} instances against the predicted
	 * set of instances.
	 *
	 * @param filesFound The files and directories that were found.
	 * @param predictedResult The files and directories predicted to be found.
	 * @param methodDescription The method that was used to find the files. Is
	 *            used for reporting purposes.
	 */
	private static void testFilesAgainstPrediction(List<File> filesFound, Set<File> predictedResult, String methodDescription) {
		// Set of files that have not (yet) been matched, but have been
		// predicted.
		Set<File> unmatchedFiles = new HashSet<File>(predictedResult);
		Set<File> matchedFiles = new HashSet<File>();
		matchedFiles.addAll(filesFound);

		// There should be no duplicate files matched. While this is not
		// explicitly guaranteed, it would certainly be
		// highly undesirable.
		Assert.assertEquals("List of files found by '" + methodDescription + "' contains duplicates.", filesFound.size(),
			matchedFiles.size());

		// All matched files must be in the prediction.
		for (File acceptedFile : filesFound) {
			Assert.assertTrue(methodDescription + " accepted the unpredicted file: '" + acceptedFile.toString() + "'",
				unmatchedFiles.contains(acceptedFile));
			unmatchedFiles.remove(acceptedFile);
		}

		for (File unmatchedFile : unmatchedFiles) {
			Assert.assertTrue(methodDescription + " did not match predicted file: '" + unmatchedFile.toString() + "'", false);
		}
	}
}
