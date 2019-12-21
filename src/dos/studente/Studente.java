package dos.studente;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.json.JSONObject;
import org.json.XML;

public class Studente {
	public final int id;
	public String matricola;
	public String nome;
	public String cognome;
	public LocalDate nascita;
	public String CDL;
	public int anno;
	
	
	public Studente(int id, String matricola,  String nome, String cognome, 
					LocalDate nascita, String CDL, int anno) {
		this.id = id;
		this.matricola = matricola;
		this.nome = nome;
		this.cognome = cognome;
		this.nascita = nascita;
		this.CDL = CDL;
		this.anno = anno;
	}
	
	public Studente(int id, JSONObject studJson) {
		this(id, studJson.getString(StudKey.MATRICOLA), 
				 studJson.getString(StudKey.NOME), 
				 studJson.getString(StudKey.COGNOME),
				 LocalDate.parse(studJson.getString(StudKey.NASCITA), DateTimeFormatter.ofPattern("dd-MM-yyyy")),
				 studJson.getString(StudKey.CDL),
				 studJson.getInt(StudKey.ANNO));
	}
	
	public Studente(int id, XML studXML) {
		this(id, XML.toJSONObject(studXML.toString()));
	}
	
	public void loadStudent(int id) {
		// TODO Load a student from the give id
	}
	
	public void saveJson() {
		// TODO Save the student to a json file with the same name ad the id
	}
	
	public String jsonDesc() {
		// TODO Return a json description of the student
		return null;
	}

	public String XMLDesc() {
		// TODO Return an XML description of the student
		return null;
	}

}
