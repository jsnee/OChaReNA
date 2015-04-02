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
import com.jophus.util.JophFileUtils;

public class OCHFileFactory {

	public static File buildOCHFromImage(String originalImageFilename) throws IOException, FileNotFoundException {
		File archiveFile, originalImageFile, masterHeaderFile;
		originalImageFile = new File(originalImageFilename);
		if (!originalImageFile.exists()) {
			throw new FileNotFoundException(originalImageFilename);
		}
		String imageBaseNameAndExtension = FilenameUtils.getBaseName(originalImageFilename) + "." + FilenameUtils.getExtension(originalImageFilename);
		ImagePixels image = new ImagePixels(originalImageFilename);
		archiveFile = generateArchive(originalImageFilename);
		OCHFile ochFile = new OCHFile(archiveFile.getAbsolutePath());
		masterHeaderFile = new File(MasterSegmentHeader.masterHeaderFilename);
		if (!masterHeaderFile.createNewFile()) {
			ochFile.deleteFile();
			throw new IOException("Error creating new .och file!");
		} else {
			writeToMasterHeader(masterHeaderFile, imageBaseNameAndExtension, image.getImageWidth(), image.getImageHeight());
		}
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
	
	
	
	private static void writeToMasterHeader(File masterHeaderFile, String imageBaseAndExtension, int imageWidth, int imageHeight) throws FileNotFoundException, UnsupportedEncodingException {
		PrintWriter masterHeaderWriter = new PrintWriter(masterHeaderFile, OCHFile.FILE_ENCODING);
		masterHeaderWriter.println("-n " + imageBaseAndExtension);
		masterHeaderWriter.println("-w " + imageWidth);
		masterHeaderWriter.println("-h " + imageHeight);
		masterHeaderWriter.close();
	}

	private static File generateArchive(String originalImageFilename) throws IOException {
		String archiveFilename = FilenameUtils.getBaseName(originalImageFilename) + OcharenaSettings.OCH_FILE_EXTENSION;
		File archiveFile = new File(archiveFilename);
		if (!archiveFile.createNewFile()) {
			archiveFilename = System.currentTimeMillis() + archiveFilename;
			archiveFile = new File(archiveFilename);
			if (!archiveFile.createNewFile()) {
				throw new IOException("Error while creating new .och file!");
			}
		}
		return archiveFile;
	}

}
