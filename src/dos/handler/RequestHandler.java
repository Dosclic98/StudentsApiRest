package dos.handler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.format.DateTimeParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.json.XML;

import dos.studente.StudKey;
import dos.studente.Studente;
import dos.studente.studExc.StudentAlreadyExistingException;

public class RequestHandler implements HttpHandler {
	
	private final String resNameDB = "studenti";
	
	@Override
	public void handle(HttpExchange he) throws IOException {
	System.out.println("QUI");
		Headers headers = he.getRequestHeaders();
		String method = he.getRequestMethod();
		
		String response = null;
		int resCode = 0;
		
		if(method.contentEquals("POST")) {
			String resName = null;
			String resType = null;

			try {
				Matcher matcher = parseAccept(headers);
				resName = matcher.group(1);
				resType = matcher.group(2);
				if(resName.equals(resNameDB)) {
					if(resType.equals("json")) {
						int newId = Studente.gestNextId();
						JSONObject stud = getStudentFromRequest(he);
						
						try {
							if(stud != null && isValidStudent(stud)) {
								System.out.println(newId);
								System.out.println(stud.toString());
								Studente studWr = new Studente(newId, stud);
								System.out.println("QUI STUD");
								studWr.saveNewJson();
								
								response = "Student added correctly\n";
								resCode = 200;	
							} else {
								response = "Error: student format incorrect\n";
								resCode = 404;
							}
						} catch (StudentAlreadyExistingException e) {
							response = "Error: saving student\n";
							resCode = 404;
						} catch (JSONException er) {
							response = "Error: student format incorrect\n";
							resCode = 404;
						} catch (DateTimeParseException er1) {
							response = "Error: unparsable date\n";
							resCode = 404;							
						}
					} else if (resType.equals("xml")) {
						
						// TODO response = XML.toString(studsValid, "students");
					} else {
						response = "Error: Resource format not present\n";
						resCode = 404;
					}	

				} else {
					response = "Error: Invalid resource requested\n";
					resCode = 404;			
				}
				
			} catch (IllegalArgumentException e) {
				response = "Error: Invalid Accept field\n";
				resCode = 404;
			}
			
		}
		
		he.sendResponseHeaders(resCode, response.getBytes().length);
		OutputStream outputStream = he.getResponseBody();
		outputStream.write(response.getBytes());
		outputStream.close();	
	}
	
	private Matcher parseAccept(Headers hd) {
		String rgx = "application/(\\w+)\\+(\\w+)";
		
		String acceptToParseString = hd.getFirst("Accept");
		System.out.println(acceptToParseString);
		
		Pattern pattern = Pattern.compile(rgx);
		Matcher matcher = pattern.matcher(acceptToParseString);
		
		if(!matcher.find()) {
			throw new IllegalArgumentException("Unable to parse the Accept field");
		} else return matcher;
			
	}
	
	private JSONObject getStudentFromRequest(HttpExchange he) {
		JSONObject studIn = null;
		JSONTokener students = null;
		
		try {
			InputStream in = he.getRequestBody();
			students = new JSONTokener(in);
			studIn = new JSONObject(students);
			
		} catch(JSONException er) {
			return null;
		}
		return studIn;
		
	}
	
	private boolean isValidStudent(JSONObject studInObj) {
		return studInObj.has(StudKey.MATRICOLA) && studInObj.has(StudKey.NOME) && studInObj.has(StudKey.COGNOME) &&
				studInObj.has(StudKey.NASCITA) && studInObj.has(StudKey.CDL) && studInObj.has(StudKey.ANNO);
	}
	
}
