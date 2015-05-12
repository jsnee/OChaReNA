package com.jophus.ocharena;

import java.io.File;

/**
 * Global Constants and Settings
 * @author Joe Snee
 *
 */
public class OcharenaSettings {
	
	@Deprecated
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
	
	// Total number of combined extracted features
	public static final int NUM_ATTRIBUTES = 120;
	
	// Characters to train on
	public static final String SUPPORTED_CHARS = "0123456789"
			+ "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
			+ "abcdefghijklmnopqrstuvwxyz";

	@Deprecated
	public static void createDefaultWorkingDirectoryUnlessExists() {
		File defaultWorkingDirectory = new File(defaultWorkingDirectoryPath);
		defaultWorkingDirectory.mkdirs();
	}

	// Get the id of the input character
	public static int charIndex(char ch) {
		return SUPPORTED_CHARS.indexOf(ch);
	}
	
	// Get the character with the given id
	public static char charAtIndex(int index) {
		return SUPPORTED_CHARS.charAt(index);
	}
	
	// Generate an empty output for non-training datasets
	public static double[] emptyOutputArray() {
		double[] result = new double[SUPPORTED_CHARS.length()];
		for (int i = 0; i < SUPPORTED_CHARS.length(); i++) result[i] = 0.0d;
		return result;
	}
}
