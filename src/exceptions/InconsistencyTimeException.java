package exceptions;


public class InconsistencyTimeException extends RuntimeException {

	private static final long serialVersionUID = -5421919331940054545L;

	public InconsistencyTimeException(long timeStamp){
		super("Time-stamp "+timeStamp+" has been already inserted");
	}
}
