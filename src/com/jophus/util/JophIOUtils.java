package com.jophus.util;

import java.io.File;
import java.util.logging.Logger;

public class JophIOUtils {
	
	public static void createDir(File dir) {
		LOG.fine("Creating Directory " + dir.toString());
		if (!dir.mkdirs()) { throw new RuntimeException("Can not create directory " + dir); }
	}
	
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
