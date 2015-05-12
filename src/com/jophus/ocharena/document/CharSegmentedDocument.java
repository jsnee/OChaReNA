package com.jophus.ocharena.document;

import java.io.File;
import java.util.logging.Logger;

@Deprecated
public class CharSegmentedDocument implements OCRDocumentHandler {

	private static final Logger LOG = Logger.getLogger(CharSegmentedDocument.class.getName());

	protected String filepath;
	protected File archivedOCRDocumentFile;
	
	public CharSegmentedDocument(String filepath) {
		loadArchivedDocumentFromFile(filepath);
	}

	@Override
	public void loadArchivedDocumentFromFile(String archivedDocumentFilepath) {
		archivedOCRDocumentFile = new File(archivedDocumentFilepath);
		if (archivedOCRDocumentFile.exists()) {
			this.filepath = archivedDocumentFilepath;
			
		}
	}
}
