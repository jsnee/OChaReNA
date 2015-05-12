package com.jophus.util;

import java.io.File;
import java.util.logging.Logger;

/**
 * Utility class. Some useful IO utilities.
 * @author Joe Snee
 *
 */
public class JophIOUtils {
	
	/**
	 * Attempts to create directory structure, if missing
	 * @param dir
	 */
	public static void createDir(File dir) {
		LOG.fine("Creating Directory " + dir.toString());
		if (!dir.mkdirs()) { throw new RuntimeException("Can not create directory " + dir); }
	}
	
	/**
	 * Deletes all of the files contained within the specified directory
	 * @param dir
	 */
	public static void deleteFiles(File dir) {
		if (dir.isDirectory()) {
			File[] children = dir.listFiles();
			if (children != null) {
				for (File child : children) {
					deleteFiles(child);
				}
			}
		}
		dir.delete();
	}
	
	private static final Logger LOG = Logger.getLogger(JophIOUtils.class.getName());

}
