package com.jophus.ocharena;

import java.io.File;

public class OcharenaSettings {
	
	public static String defaultWorkingDirectoryPath = "ScannedDocuments" + File.separator;
	public static String temporaryDirectoryPath = "TempDocs" + File.separator;
	public static String dataFolder = "data" + File.separator;
	public static String neuralNetworkFile = dataFolder + "OChaReNA.nnet";
	public static String ochFolder = dataFolder + "och" + File.separator;
	public static final String ARCHIVE_FILETYPE_EXTENSION = ".zip";
	public static final String OCH_FILE_EXTENSION = ".och";
	public static final String OCH_HEADER_EXTENSION = ".ochh";
	public static final String CSV_EXTENSION = ".csv";
	public static final String NEURAL_NET_EXTENSION = ".nnet";
	
	public static final int NUM_ATTRIBUTES = 120;
	
	public static final String SUPPORTED_CHARS = "0123456789"
			+ "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
			+ "abcdefghijklmnopqrstuvwxyz";

	public static void createDefaultWorkingDirectoryUnlessExists() {
		File defaultWorkingDirectory = new File(defaultWorkingDirectoryPath);
		defaultWorkingDirectory.mkdirs();
	}

	public static int charIndex(char ch) {
		return SUPPORTED_CHARS.indexOf(ch);
	}
	
	public static char charAtIndex(int index) {
		return SUPPORTED_CHARS.charAt(index);
	}
	
	public static double[] emptyOutputArray() {
		double[] result = new double[SUPPORTED_CHARS.length()];
		for (int i = 0; i < SUPPORTED_CHARS.length(); i++) result[i] = 0.0d;
		return result;
	}
}
