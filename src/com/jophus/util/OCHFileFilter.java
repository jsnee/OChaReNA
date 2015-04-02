package com.jophus.util;

import java.io.File;

import javax.swing.filechooser.FileFilter;

import org.apache.commons.io.FilenameUtils;

import com.jophus.ocharena.OcharenaSettings;

public class OCHFileFilter extends FileFilter {

	@Override
	public boolean accept(File file) {
		if (file.isDirectory()) {
			return true;
		}

		String extension = FilenameUtils.getExtension(file.getName());
		if (extension != null) {
			return (OcharenaSettings.OCH_FILE_EXTENSION.equalsIgnoreCase("." + extension));
		}
		return false;
	}

	@Override
	public String getDescription() {
		return "OChaReNA files";
	}

}
