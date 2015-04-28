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

public class OCHFile {
	private static final Logger logger = Logger.getLogger(OCHFile.class.getName());

	private File ochFile;

	public static final int RETURN_CHAR = 13;
	public static final int NEWLINE_CHAR = 10;
	public static final int SPACE_CHAR = 32;
	public static final int HYPHEN_CHAR = 45;
	public static final String FILE_ENCODING = "UTF-8";

	public OCHFile(String filename) {
		if (isValidOCHDocumentFile(filename)) {
			ochFile = new File(filename);
		}
	}

	public void deleteFile() {
		ochFile.delete();
	}

	public ImagePixels extractImagePixels(MasterSegmentHeader masterHeader) {
		return extractImagePixels(masterHeader, false);
	}

	public ImagePixels extractImagePixels(MasterSegmentHeader masterHeader, boolean muteBlue) {
		try {
			File tempImage = File.createTempFile("Temp_" + System.currentTimeMillis(), "." + FilenameUtils.getExtension(masterHeader.getOriginalImageFilename()));
			tempImage.deleteOnExit();
			ZipFile ochArchiveFile = new ZipFile(ochFile);
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
			return new ImagePixels(1, 1);
		}
	}

	public static boolean isValidOCHDocumentFile(String ochFilename) {
		return (hasOCHFileExtension(ochFilename) && fileExists(ochFilename));
	}

	public static boolean isValidImageFile(String imageFilename) {
		String extension = "." + FilenameUtils.getExtension(imageFilename);
		return (extension.equalsIgnoreCase(".jpg") || extension.equalsIgnoreCase(".jpeg") || extension.equalsIgnoreCase(".png"));
	}

	private static boolean hasOCHFileExtension(String filename) {
		return OcharenaSettings.OCH_FILE_EXTENSION.equalsIgnoreCase("." + FilenameUtils.getExtension(filename));
	}

	private static boolean fileExists(String filename) {
		File file = new File(filename);
		return file.exists();
	}

	public void extractContentsToDirectory(File destinationDirectory) {
		if (destinationDirectory.isDirectory()) {
			try {
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

	public String getName() {
		return ochFile.getName();
	}

	private void unzipEntry(ZipFile zipFile, ZipArchiveEntry entry, File outputDir) throws IOException {

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

	public void addFileToArchive(ZipArchiveOutputStream zipOut, String path, String base) throws IOException {
		File f = new File(path);
		String entryName = base + f.getName();
		ZipArchiveEntry zEntry = new ZipArchiveEntry(f, entryName);

		zipOut.putArchiveEntry(zEntry);

		if (f.isFile()) {
			logger.finer("Writing file to archive: " + f.getName());
			zipOut.write(Files.readAllBytes(Paths.get(f.toURI())));
			zipOut.closeArchiveEntry();
		} else {
			logger.finer("Writing directory to archive: " + f.getName());
			zipOut.closeArchiveEntry();
			File[] children = f.listFiles();

			if (children != null) {
				for (File child : children) {
					addFileToArchive(zipOut, child.getAbsolutePath(), entryName + File.separator);
				}
			}
		}
	}

	public void addFileToArchive(ZipArchiveOutputStream zipOut, File file, String archiveEntryname) throws IOException {
		ZipArchiveEntry zEntry = new ZipArchiveEntry(file, archiveEntryname);

		zipOut.putArchiveEntry(zEntry);

		if (file.isFile()) {
			logger.finer("Writing file to archive: " + file.getName());
			zipOut.write(Files.readAllBytes(Paths.get(file.toURI())));
			zipOut.closeArchiveEntry();
		} else {
			logger.finer("Writing directory to archive: " + file.getName());
			zipOut.closeArchiveEntry();
			File[] children = file.listFiles();

			if (children != null) {
				for (File child : children) {
					addFileToArchive(zipOut, child.getAbsolutePath(), archiveEntryname + File.separator);
				}
			}
		}
	}

	public void addFileToArchive(File file, String archiveEntryname) {
		if (file.exists()) {
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
				addFileToArchive(zipOut, file, archiveEntryname);

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
