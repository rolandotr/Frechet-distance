package exceptions;


public class InconsistencyTimeException extends RuntimeException {

	public long time;
	
	public InconsistencyTimeException(long time){
		super("La fecha "+time+" ya esta en el arbol y por tanto eso" +
						"significa que hay dos fechas para un mismo punto");
		time = time;
	}
}
