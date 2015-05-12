package com.jophus.util;

import java.io.File;

import org.apache.commons.io.FilenameUtils;

/**
 * Utility class. Some useful file methods
 * @author Joe Snee
 *
 */
public class JophFileUtils {
	
	/**
	 * Default Constructor.
	 */
	public JophFileUtils() {
	}
	
	/**
	 * Gets filename without the directory path or file extension
	 * @param targetFile
	 * @return
	 */
	public static String getBaseNameOfFile(File targetFile) {
		return FilenameUtils.getBaseName(targetFile.getName());
	}
	
	/**
	 * Gets the full path of the file
	 * @param targetFile
	 * @return
	 */
	public static String getFullPathOfFile(File targetFile) {
		return FilenameUtils.getFullPath(targetFile.getName());
	}
	
	/**
	 * Gets the extension of the file
	 * @param targetFile
	 * @return
	 */
	public static String getExtensionOfFile(File targetFile) {
		return "." + FilenameUtils.getExtension(targetFile.getName());
	}

}
