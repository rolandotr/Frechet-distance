package exceptions;


public class IncorrectLineFormatException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -922285232814953087L;
	public String line;
	
	public IncorrectLineFormatException(String msg, String line) {
		super(msg+" en : \n\r"+line);
		this.line = line;
	}
	public IncorrectLineFormatException(String msg) {
		super(msg);
	}

}
