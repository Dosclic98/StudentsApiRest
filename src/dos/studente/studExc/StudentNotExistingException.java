package dos.studente.studExc;

public class StudentNotExistingException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public StudentNotExistingException() {
		super("Student not existing: cannot modify it");
	}

}
