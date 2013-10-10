package RQLibrary;

public class SingularMatrixException extends Exception{

	private static final long serialVersionUID = -5498117821525565618L;
	public static final String msg = "Matrix is singular, therefore not invertible.";
	
	public SingularMatrixException(){
		
		super(msg);
	}
	
	public SingularMatrixException(String message){
		
		super(message);
	}
	
	public SingularMatrixException(String message, Throwable cause){
		
		super(message, cause); 
	}
	
	public SingularMatrixException(Throwable cause){
		
		super(cause); 
	}
}