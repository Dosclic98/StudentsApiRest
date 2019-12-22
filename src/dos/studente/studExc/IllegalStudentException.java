package dos.studente.studExc;

public class IllegalStudentException extends IllegalStateException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public IllegalStudentException() {
		super("Missing fields to build student");
	}
	
}
