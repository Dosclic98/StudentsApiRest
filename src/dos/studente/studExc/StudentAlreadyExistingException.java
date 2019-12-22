package dos.studente.studExc;

public class StudentAlreadyExistingException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public StudentAlreadyExistingException() {
		super("Student already existing: cannot overwrite");
	}

}
