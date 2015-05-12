package com.jophus.ocharena.factory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.io.FilenameUtils;

import com.jophus.ocharena.OcharenaSettings;
import com.jophus.ocharena.document.OCHFile;
import com.jophus.ocharena.image.ImagePixels;
import com.jophus.ocharena.ochfile.MasterSegmentHeader;

/**
 * OCHFileFactory class. Generates new OCHFiles from images
 * @author Joe Snee
 *
 */
public class OCHFileFactory {

	/**
	 * Generates a new OCHFile from an image
	 * @param originalImageFilename the image filename
	 * @return the OCHFile
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public static File buildOCHFromImage(String originalImageFilename) throws IOException, FileNotFoundException {
		File archiveFile, originalImageFile, masterHeaderFile;
		originalImageFile = new File(originalImageFilename);
		if (!originalImageFile.exists()) {
			// Error: file doesn't exist
			throw new FileNotFoundException(originalImageFilename);
		}
		String imageBaseNameAndExtension = FilenameUtils.getBaseName(originalImageFilename) + "." + FilenameUtils.getExtension(originalImageFilename);
		ImagePixels image = new ImagePixels(originalImageFilename);
		// Make new archive
		archiveFile = generateArchive(originalImageFilename);
		OCHFile ochFile = new OCHFile(archiveFile.getAbsolutePath());
		
		// Generate new master header
		masterHeaderFile = new File(MasterSegmentHeader.masterHeaderFilename);
		if (!masterHeaderFile.createNewFile()) {
			ochFile.deleteFile();
			throw new IOException("Error creating new .och file!");
		} else {
			// Save master header
			writeToMasterHeader(masterHeaderFile, imageBaseNameAndExtension, image.getImageWidth(), image.getImageHeight());
		}
		
		// Archive new files
		ZipArchiveOutputStream archiveOutputStream = new ZipArchiveOutputStream(archiveFile);
		archiveOutputStream.putArchiveEntry(archiveOutputStream.createArchiveEntry(originalImageFile, imageBaseNameAndExtension));
		archiveOutputStream.write(Files.readAllBytes(Paths.get(originalImageFile.toURI())));
		archiveOutputStream.closeArchiveEntry();
		archiveOutputStream.putArchiveEntry(archiveOutputStream.createArchiveEntry(masterHeaderFile, MasterSegmentHeader.masterHeaderFilename));
		archiveOutputStream.write(Files.readAllBytes(Paths.get(masterHeaderFile.toURI())));
		archiveOutputStream.closeArchiveEntry();
		archiveOutputStream.close();
		masterHeaderFile.delete();
		return archiveFile;
	}
	
	/**
	 * Saves the master header
	 * @param masterHeaderFile the master header file to save to
	 * @param imageBaseAndExtension the image name
	 * @param imageWidth the image width
	 * @param imageHeight the image height
	 * @throws FileNotFoundException
	 * @throws UnsupportedEncodingException
	 */
	private static void writeToMasterHeader(File masterHeaderFile, String imageBaseAndExtension, int imageWidth, int imageHeight) throws FileNotFoundException, UnsupportedEncodingException {
		PrintWriter masterHeaderWriter = new PrintWriter(masterHeaderFile, OCHFile.FILE_ENCODING);
		masterHeaderWriter.println("-n " + imageBaseAndExtension);
		masterHeaderWriter.println("-w " + imageWidth);
		masterHeaderWriter.println("-h " + imageHeight);
		masterHeaderWriter.close();
	}

	/**
	 * Private OCH archive generation
	 * @param originalImageFilename the image name
	 * @return the OCHFile
	 * @throws IOException
	 */
	private static File generateArchive(String originalImageFilename) throws IOException {
		String archiveFilename = FilenameUtils.getBaseName(originalImageFilename) + OcharenaSettings.OCH_FILE_EXTENSION;
		File archiveFile = new File(OcharenaSettings.ochFolder + archiveFilename);
		if (!archiveFile.createNewFile()) {
			archiveFilename = OcharenaSettings.ochFolder + System.currentTimeMillis() + archiveFilename;
			archiveFile = new File(archiveFilename);
			if (!archiveFile.createNewFile()) {
				throw new IOException("Error while creating new .och file!");
			}
		}
		return archiveFile;
	}

}
