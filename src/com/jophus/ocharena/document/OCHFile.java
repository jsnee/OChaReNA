package com.jophus.ocharena.document;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.io.FilenameUtils;

import com.jophus.ocharena.OcharenaSettings;
import com.jophus.ocharena.image.ImagePixels;
import com.jophus.ocharena.ochfile.MasterSegmentHeader;
import com.jophus.util.JophIOUtils;

/**
 * OCHFile class. In charge of managing the archive system and manipulating the actual files.
 * @author Joe Snee
 *
 */
public class OCHFile {
	private static final Logger logger = Logger.getLogger(OCHFile.class.getName());

	// The och archive
	private File ochFile;

	// Some important character codes
	public static final int RETURN_CHAR = 13;
	public static final int NEWLINE_CHAR = 10;
	public static final int SPACE_CHAR = 32;
	public static final int HYPHEN_CHAR = 45;
	
	// File encoding type
	public static final String FILE_ENCODING = "UTF-8";

	/**
	 * Constructor. Asserts the provided filename is valid
	 * @param filename
	 */
	public OCHFile(String filename) {
		if (isValidOCHDocumentFile(filename)) {
			ochFile = new File(filename);
		}
	}

	// Deletes the och archive
	public void deleteFile() {
		ochFile.delete();
	}

	// Loads the image as pixels from the archive, assumes muteBlue = false
	public ImagePixels extractImagePixels(MasterSegmentHeader masterHeader) {
		return extractImagePixels(masterHeader, false);
	}

	// Loads the image pixels from the archive, muting blue if indicated
	public ImagePixels extractImagePixels(MasterSegmentHeader masterHeader, boolean muteBlue) {
		try {
			// Extract the image as a temporary file, so the pixels may be read
			File tempImage = File.createTempFile("Temp_" + System.currentTimeMillis(), "." + FilenameUtils.getExtension(masterHeader.getOriginalImageFilename()));
			tempImage.deleteOnExit();
			ZipFile ochArchiveFile = new ZipFile(ochFile);
			// Extract the image from the archive
			for (Enumeration<ZipArchiveEntry> entries = ochArchiveFile.getEntries(); entries.hasMoreElements(); ) {
				ZipArchiveEntry archiveEntry = entries.nextElement();
				if (archiveEntry.getName().equals(masterHeader.getOriginalImageFilename())) {
					BufferedInputStream inputStream = new BufferedInputStream(ochArchiveFile.getInputStream(archiveEntry));
					BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(tempImage));

					try {
						IOUtils.copy(inputStream,  outputStream);
					} finally {
						outputStream.close();
						inputStream.close();
					}
				}
			}
			ochArchiveFile.close();
			return new ImagePixels(tempImage.getAbsolutePath(), muteBlue);
		} catch (IOException ioException) {
			logger.severe(ioException.getMessage());
			// Exception: return an empty ImagePixels
			return new ImagePixels(1, 1);
		}
	}

	// Verify the file exists and has the correct extension
	public static boolean isValidOCHDocumentFile(String ochFilename) {
		return (hasOCHFileExtension(ochFilename) && fileExists(ochFilename));
	}

	// Verify the image filename provided is a supported extension
	public static boolean isValidImageFile(String imageFilename) {
		String extension = "." + FilenameUtils.getExtension(imageFilename);
		return (extension.equalsIgnoreCase(".jpg") || extension.equalsIgnoreCase(".jpeg") || extension.equalsIgnoreCase(".png"));
	}

	// Verify the filename has the correct OCH extension
	private static boolean hasOCHFileExtension(String filename) {
		return OcharenaSettings.OCH_FILE_EXTENSION.equalsIgnoreCase("." + FilenameUtils.getExtension(filename));
	}

	// Verify the file exists
	private static boolean fileExists(String filename) {
		File file = new File(filename);
		return file.exists();
	}

	// Extract all of the contents of the OCHFile to a specified directory
	public void extractContentsToDirectory(File destinationDirectory) {
		// Ensure the filepath provided points to a directory, not a file
		if (destinationDirectory.isDirectory()) {
			try {
				// Unzip all of the contents
				ZipFile ochArchiveFile = new ZipFile(ochFile);
				for (Enumeration<ZipArchiveEntry> entries = ochArchiveFile.getEntries(); entries.hasMoreElements(); ) {
					ZipArchiveEntry archiveEntry = entries.nextElement();
					unzipEntry(ochArchiveFile, archiveEntry, destinationDirectory);
				}
			} catch (IOException ioException) {
				logger.severe(ioException.getMessage());
			}
		}
	}

	// Gets the name of the archive file
	public String getName() {
		return ochFile.getName();
	}

	// Private method extracts a specific arhive entry into the specified directory
	private static void unzipEntry(ZipFile zipFile, ZipArchiveEntry entry, File outputDir) throws IOException {

		if (entry.isDirectory()) {
			JophIOUtils.createDir(new File(outputDir, entry.getName()));
			return;
		}

		File outputFile  = new File(outputDir, entry.getName());
		if (!outputFile.getParentFile().exists()) {
			JophIOUtils.createDir(outputFile.getParentFile());
		}

		logger.finer("Extracting; " + entry.toString());
		BufferedInputStream inputStream = new BufferedInputStream(zipFile.getInputStream(entry));
		BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outputFile));

		try {
			IOUtils.copy(inputStream,  outputStream);
		} finally {
			outputStream.close();
			inputStream.close();
		}
	}

	// Static method adds the specified filepath to the specified archiveStream to be located within the archive as specified by base
	public static void addFileToArchive(ZipArchiveOutputStream zipOut, String path, String base) throws IOException {
		File f = new File(path);
		String entryName = base + f.getName();
		ZipArchiveEntry zEntry = new ZipArchiveEntry(f, entryName);

		// Append the entry
		zipOut.putArchiveEntry(zEntry);

		// Write file to archive
		if (f.isFile()) {
			logger.finer("Writing file to archive: " + f.getName());
			zipOut.write(Files.readAllBytes(Paths.get(f.toURI())));
			zipOut.closeArchiveEntry();
		} else {
			// Current file is directory, recursively add all of the children of specified directory
			logger.finer("Writing directory to archive: " + f.getName());
			zipOut.closeArchiveEntry();
			File[] children = f.listFiles();

			if (children != null) {
				for (File child : children) {
					// Recursive call to add children files
					addFileToArchive(zipOut, child.getAbsolutePath(), entryName + File.separator);
				}
			}
		}
	}

	// Static method adds the specified filepath to the specified archiveStream to be named as specified
	public static void addFileToArchive(ZipArchiveOutputStream zipOut, File file, String archiveEntryname) throws IOException {
		ZipArchiveEntry zEntry = new ZipArchiveEntry(file, archiveEntryname);

		// Append the entry
		zipOut.putArchiveEntry(zEntry);

		// Write file to archive
		if (file.isFile()) {
			logger.finer("Writing file to archive: " + file.getName());
			zipOut.write(Files.readAllBytes(Paths.get(file.toURI())));
			zipOut.closeArchiveEntry();
		} else {
			// Current file is directory, recursively add all of the children of specified directory
			logger.finer("Writing directory to archive: " + file.getName());
			zipOut.closeArchiveEntry();
			File[] children = file.listFiles();

			if (children != null) {
				for (File child : children) {
					// Recursive call to add children files
					addFileToArchive(zipOut, child.getAbsolutePath(), archiveEntryname + File.separator);
				}
			}
		}
	}

	// Adds the specified file to the och archive
	public void addFileToArchive(File file, String archiveEntryname) {
		// Verify the file exists
		if (file.exists()) {
			// Unzip the archive, add the new files and zip it all up again
			File outputDir = new File("unzipTEMP" + File.separator);
			try {
				ZipFile zFile = new ZipFile(ochFile);

				outputDir.deleteOnExit();

				for (Enumeration<ZipArchiveEntry> entries = zFile.getEntries(); entries.hasMoreElements();) {

					ZipArchiveEntry archiveEntry = entries.nextElement();
					unzipEntry(zFile, archiveEntry, outputDir);
				}

				ZipArchiveOutputStream zipOut = new ZipArchiveOutputStream(ochFile);

				// Add all of the preexisting archive entries back in
				for (File each : outputDir.listFiles()) {
					addFileToArchive(zipOut, each.getAbsolutePath(), "");
				}
				// Add the new archive entry
				addFileToArchive(zipOut, file, archiveEntryname);

				zipOut.close();
				zFile.close();


			} catch (IOException e) {
				e.printStackTrace();
				logger.log(Level.SEVERE, null, e);
			} finally {
				// Delete the temporary extraction directory
				JophIOUtils.deleteFiles(outputDir);
			}

		}
	}

	/**
	 * Method that adds all of the files in designated directory to the document archive
	 * 
	 * @param targetDirectory Path of directory containing files to read
	 */
	public void addDirectoryToArchive(File targetDirectory) {
		if (targetDirectory.isDirectory()) {
			File outputDir = new File("unzipTEMP" + File.separator);
			try {
				ZipFile zFile = new ZipFile(ochFile);

				outputDir.deleteOnExit();

				for (Enumeration<ZipArchiveEntry> entries = zFile.getEntries(); entries.hasMoreElements();) {

					ZipArchiveEntry archiveEntry = entries.nextElement();
					unzipEntry(zFile, archiveEntry, outputDir);
				}

				ZipArchiveOutputStream zipOut = new ZipArchiveOutputStream(ochFile);

				for (File each : outputDir.listFiles()) {
					addFileToArchive(zipOut, each.getAbsolutePath(), "");
				}
				addFileToArchive(zipOut, targetDirectory.getAbsolutePath(), "");

				zipOut.close();
				zFile.close();


			} catch (IOException e) {
				e.printStackTrace();
				logger.log(Level.SEVERE, null, e);
			} finally {
				JophIOUtils.deleteFiles(outputDir);
			}

		}
	}

}
