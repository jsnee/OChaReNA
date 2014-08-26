package com.jophus.ocharena.document;

import java.awt.image.BufferedImage;
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

import com.jophus.ocharena.Ocharena;
import com.jophus.ocharena.OcharenaSettings;
import com.jophus.ocharena.image.ImagePixels;
import com.jophus.util.JophFileUtils;
import com.jophus.util.JophIOUtils;

public class ScannedDocument {

	private ImagePixels documentPixelData;
	//protected Path zipFile;
	protected String filepath;
	protected File archivedOCRDocumentFile;

	/**
	 * Constructor creates the zipped file containing the document and adds the original image to the archive
	 * @param fullFilepath The path of the file to be read.
	 */
	public ScannedDocument(String fullFilepath) {
		this.filepath = fullFilepath;
		File originalImageFile = new File(fullFilepath);
		if (originalImageFile.exists()) {
			this.documentPixelData = new ImagePixels(originalImageFile);
			createAndInitializeDocumentArchive(originalImageFile);
		} else {
			documentPixelData = null;
		}
	}

	/**
	 * Method that initializes the document archive file
	 * 
	 * @param originalImageFile
	 */
	private void createAndInitializeDocumentArchive(File originalImageFile) {
		try {
			createUniqueDocumentArchiveFile(JophFileUtils.getBaseNameOfFile(originalImageFile));
			zipArchiveOriginalDocumentImage(originalImageFile);
			LOG.fine("File is at: " + archivedOCRDocumentFile.getAbsolutePath());
		} catch (IOException e) {
			e.printStackTrace();
			LOG.log(Level.SEVERE, null, e);
		}
	}

	private void createUniqueDocumentArchiveFile(String originalImageFilename) throws IOException {
		archivedOCRDocumentFile = new File(getRelativePathnameWithExtension(generateUniqueArchiveFilename(originalImageFilename)));
		archivedOCRDocumentFile.createNewFile();
	}

	private String generateUniqueArchiveFilename(String originalImageFilename) {
		String attemptedArchiveFilename = originalImageFilename;

		while (archiveNameIsNotUnique(attemptedArchiveFilename)) {
			attemptedArchiveFilename = generateTimestampArchiveFilename(originalImageFilename);
		}
		return attemptedArchiveFilename;
	}

	private boolean archiveNameIsNotUnique(String filenameWithoutExtension) {
		return new File(getRelativePathnameWithExtension(filenameWithoutExtension)).exists();
	}

	private String generateTimestampArchiveFilename(String originalImageFilename) {
		return originalImageFilename + System.currentTimeMillis();
	}

	private static String getRelativePathnameWithExtension(String filename) {
		return OcharenaSettings.defaultWorkingDirectoryPath + filename + OcharenaSettings.ARCHIVE_FILETYPE_EXTENSION;
	}

	private void zipArchiveOriginalDocumentImage(File originalImageFile) throws IOException {
		ZipArchiveOutputStream archiveOutputStream = new ZipArchiveOutputStream(archivedOCRDocumentFile);
		archiveOutputStream.putArchiveEntry(archiveOutputStream.createArchiveEntry(originalImageFile, "image." + JophFileUtils.getExtensionOfFile(originalImageFile)));
		archiveOutputStream.write(Files.readAllBytes(Paths.get(originalImageFile.toURI())));
		archiveOutputStream.closeArchiveEntry();
		archiveOutputStream.close();
	}

	/**
	 * Method that gets the BufferedImage object of the document
	 * 
	 * @return returns the currently stored image of the document
	 */
	public BufferedImage getDocumentPixelDataImage() {
		return documentPixelData.getImageAsBufferedImage();
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
				ZipFile zFile = new ZipFile(archivedOCRDocumentFile);

				outputDir.deleteOnExit();

				for (Enumeration<ZipArchiveEntry> entries = zFile.getEntries(); entries.hasMoreElements();) {

					ZipArchiveEntry archiveEntry = entries.nextElement();
					unzipEntry(zFile, archiveEntry, outputDir);
				}

				ZipArchiveOutputStream zipOut = new ZipArchiveOutputStream(archivedOCRDocumentFile);

				for (File each : outputDir.listFiles()) {
					addFileToZip(zipOut, each.getAbsolutePath(), "");
				}
				addFileToZip(zipOut, targetDirectory.getAbsolutePath(), "");

				zipOut.close();
				zFile.close();


			} catch (IOException e) {
				e.printStackTrace();
				LOG.log(Level.SEVERE, null, e);
			} finally {
				JophIOUtils.deleteFiles(outputDir);
			}

		}
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

		LOG.finer("Extracting; " + entry.toString());
		BufferedInputStream inputStream = new BufferedInputStream(zipFile.getInputStream(entry));
		BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outputFile));

		try {
			IOUtils.copy(inputStream,  outputStream);
		} finally {
			outputStream.close();
			inputStream.close();
		}
	}



	private void addFileToZip(ZipArchiveOutputStream zipOut, String path, String base) throws IOException {
		File f = new File(path);
		String entryName = base + f.getName();
		ZipArchiveEntry zEntry = new ZipArchiveEntry(f, entryName);

		zipOut.putArchiveEntry(zEntry);

		if (f.isFile()) {
			LOG.finer("Writing file to archive: " + f.getName());
			zipOut.write(Files.readAllBytes(Paths.get(f.toURI())));
			zipOut.closeArchiveEntry();
		} else {
			LOG.finer("Writing directory to archive: " + f.getName());
			zipOut.closeArchiveEntry();
			File[] children = f.listFiles();

			if (children != null) {
				for (File child : children) {
					addFileToZip(zipOut, child.getAbsolutePath(), entryName + File.separator);
				}
			}
		}
	}

	public ImagePixels getImagePixels() {
		return this.documentPixelData;
	}

	private static Logger LOG = Logger.getLogger(ScannedDocument.class.getName());
}
