package exceptions;

import wrappers.LogFormat;

public class IncorrectLineFormatException extends RuntimeException {

	public String line;
	
	public IncorrectLineFormatException(String msg, String line) {
		super(msg+" en : \n\r"+line);
		this.line = line;
	}
	public IncorrectLineFormatException(String msg) {
		super(msg);
	}

}
