package com.jophus.ocharena.exception;

/**
 * Custom FileException
 * @author Joe Snee
 * unused.
 */
public class OCHFileException extends Exception {
	private static final long serialVersionUID = 1L;
	
	private String message = null;
	
	public OCHFileException() {
		super();
	}
	
	public OCHFileException(String message) {
		super(message);
		this.message = message;
	}
	
	public OCHFileException(Throwable cause) {
		super(cause);
	}
	
	public String getMessage() {
		return message;
	}
	
	public String toString() {
		return "OCHFileException: " + message;
	}

}
