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
import com.jophus.ocharena.image.ImagePixels;
import com.jophus.util.JophIOUtils;

public class ScannedDocument {

	protected ImagePixels doc;
	//protected Path zipFile;
	protected String filename;
	protected File zip;

	/**
	 * Constructor creates the zipped file containing the document and adds the original image to the archive
	 * @param filename The path of the file to be read.
	 */
	public ScannedDocument(String filename) {
		this.filename = filename;
		File imageFile = new File(filename);
		if (imageFile.exists()) {
			this.doc = new ImagePixels(imageFile);
			createDocumentArchive(imageFile);
		} else {
			doc = null;
		}
	}

	/**
	 * Method that initializes the document archive file
	 * 
	 * @param imageFile
	 */
	private void createDocumentArchive(File imageFile) {
		try {
			File dir = new File("ScannedDocuments" + File.separator);
			dir.mkdirs();
			zip = new File(dir + File.separator + imageFile.getName() + ".zip");
			if (zip.exists()) {
				zip = File.createTempFile(imageFile.getName(), ".zip", dir);
			} else {
				zip.createNewFile();
			}
			if (zip.exists()) {
				ZipArchiveOutputStream zipOut = new ZipArchiveOutputStream(zip);
				zipOut.putArchiveEntry(zipOut.createArchiveEntry(imageFile, "image." + FilenameUtils.getExtension(imageFile.getName())));
				zipOut.write(Files.readAllBytes(Paths.get(imageFile.toURI())));
				zipOut.closeArchiveEntry();
				zipOut.close();
				LOG.fine("File is at: " + zip.getAbsolutePath());
			}
		} catch (IOException e) {
			e.printStackTrace();
			LOG.log(Level.SEVERE, null, e);
		}
	}

	/**
	 * Method that gets the BufferedImage object of the document
	 * 
	 * @return returns the currently stored image of the document
	 */
	public BufferedImage getImage() {
		return doc.getBImg();
	}

	/**
	 * Method that adds all of the files in designated directory to the document archive
	 * 
	 * @param dir Path of directory containing files to read
	 */
	public void addDirToArchive(File dir) {
		if (dir.isDirectory()) {
			File outputDir = new File("unzipTEMP" + File.separator);
			try {
				ZipFile zFile = new ZipFile(zip);

				outputDir.deleteOnExit();

				for (Enumeration<ZipArchiveEntry> entries = zFile.getEntries(); entries.hasMoreElements();) {

					ZipArchiveEntry ae = entries.nextElement();
					unzipEntry(zFile, ae, outputDir);
				}

				ZipArchiveOutputStream zipOut = new ZipArchiveOutputStream(zip);

				for (File each : outputDir.listFiles()) {
					addFileToZip(zipOut, each.getAbsolutePath(), "");
				}
				addFileToZip(zipOut, dir.getAbsolutePath(), "");

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
		return this.doc;
	}

	private static Logger LOG = Logger.getLogger(ScannedDocument.class.getName());
}
