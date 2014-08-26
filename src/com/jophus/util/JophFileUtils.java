package com.jophus.util;

import java.io.File;

import org.apache.commons.io.FilenameUtils;

public class JophFileUtils {
	
	public JophFileUtils() {
	}
	
	public static String getBaseNameOfFile(File targetFile) {
		return FilenameUtils.getBaseName(targetFile.getName());
	}
	
	public static String getExtensionOfFile(File targetFile) {
		return FilenameUtils.getBaseName(targetFile.getName());
	}

}
