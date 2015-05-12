package com.jophus.ocharena.logging;

import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Customized Java Logger
 * @author Joe Snee
 *
 */
public class JophLogger {
	
	static private FileHandler fileTxt;
	static private SimpleFormatter formatterTxt;

	static public void setup() throws IOException {
		
		// Grab the global logger to configure
		Logger logger = Logger.getLogger("com.jophus.ocharena");
		
		logger.setLevel(Level.ALL);
		fileTxt = new FileHandler("OChaReNA.log");
		
		// Create txt Formatter
		fileTxt.setFormatter(new SimpleFormatter());
		logger.addHandler(fileTxt);
	}
}
