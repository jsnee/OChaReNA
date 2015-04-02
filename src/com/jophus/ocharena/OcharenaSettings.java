package com.jophus.ocharena;

import java.io.File;

public class OcharenaSettings {
	
	public static String defaultWorkingDirectoryPath = "ScannedDocuments" + File.separator;
	public static String temporaryDirectoryPath = "TempDocs" + File.separator;
	public static final String ARCHIVE_FILETYPE_EXTENSION = ".zip";
	public static final String OCH_FILE_EXTENSION = ".och";
	public static final String OCH_HEADER_EXTENSION = ".ochh";

	public static void createDefaultWorkingDirectoryUnlessExists() {
		File defaultWorkingDirectory = new File(defaultWorkingDirectoryPath);
		defaultWorkingDirectory.mkdirs();
	}

}
