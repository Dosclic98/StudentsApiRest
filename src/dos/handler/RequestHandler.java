package dos.handler;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.time.format.DateTimeParseException;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

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
				int newId = 0;
				JSONObject stud = null;
				if(resName.equals(resNameDB) && (resType.equals("json") || resType.equals("xml"))) {
					if(resType.equals("json")) {
						newId = Studente.gestNextId();
						stud = getJsonStudentFromRequest(he);
						
					} else if (resType.equals("xml")) {
						newId = Studente.gestNextId();
						stud = getXMLStudentFromRequest(he);
						System.out.println(stud.toString());
						// TODO Test della POST XML
					}					
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
					} catch(IllegalArgumentException er2) {
						response = er2.getMessage();
						resCode = 404;
					}
				} else if(!resName.equals(resNameDB)) {
					response = "Error: Invalid resource requested\n";
					resCode = 404;			
				} else if(!resType.equals("json") && !resType.equals("xml")) {
					response = "Error: Resource format not present\n";
					resCode = 404;
				}
				
			} catch (IllegalArgumentException e) {
				response = "Error: Invalid Accept field\n";
				resCode = 404;
			} catch(JSONException e1) {
				response = "Error: Invalid student inserted\n";
				resCode = 404;
			}
			
		} else if(method.contentEquals("GET")) {
			System.out.println("QUIii");
			String resName = null;
			String resType = null;

			try {
				Matcher matcher = parseAccept(headers);
				resName = matcher.group(1);
				resType = matcher.group(2);
				int newId = 0;
				JSONObject stud = null;
				if(resName.equals(resNameDB) && (resType.equals("json") || resType.equals("xml"))) {
					
					stud = getQueryAsJson(he);
					if(isValidStudentQuery(stud)) {
						// Creare una classe che dato l'oggetto JSON
						// ritorna un array di id di studenti che matchano
						if(resType.equals("json")) {
							// TODO Ritorna i dati in formato JSON
						} else if (resType.equals("xml")) {
							// TODO Ritorna i dati in formato XML
						}						
					} else {
						response = "Error: Invalid fields inserted\n";
						resCode = 404;
					}				
				} else if(!resName.equals(resNameDB)) {
					response = "Error: Invalid resource requested\n";
					resCode = 404;			
				} else if(!resType.equals("json") && !resType.equals("xml")) {
					response = "Error: Resource format not present\n";
					resCode = 404;
				}
				
			} catch (IllegalArgumentException e) {
				response = "Error: Invalid Accept field\n";
				resCode = 404;
			} catch(JSONException e1) {
				response = "Error: Invalid student inserted\n";
				resCode = 404;
			} catch(NullPointerException e2) {
				response = "Error: Query not inserted\n";
				resCode = 404;				
			}
			
			
		}		
		he.sendResponseHeaders(resCode, response.getBytes().length);
		OutputStream outputStream = he.getResponseBody();
		outputStream.write(response.getBytes());
		outputStream.close();	
	}
	
	private JSONObject getQueryAsJson(HttpExchange he) {
		String studentToParse = he.getRequestURI().getQuery();

		return new JSONObject(he.getRequestURI().getQuery());
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
	
	private JSONObject getJsonStudentFromRequest(HttpExchange he) {
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
	
	private JSONObject getXMLStudentFromRequest(HttpExchange he) {
		JSONObject studIn = null;
		
		try {
			InputStream in = he.getRequestBody();
			studIn = XML.toJSONObject(new InputStreamReader(in));
			
		} catch(JSONException er) {
			return null;
		}
		JSONObject studOut = studIn.getJSONObject("studente");
		Integer matr = studOut.getInt(StudKey.MATRICOLA);
		studOut.put(StudKey.MATRICOLA, matr.toString());
		return studIn.getJSONObject("studente");
		
	}
	
	private boolean isValidStudent(JSONObject studInObj) {
		return studInObj.has(StudKey.MATRICOLA) && studInObj.has(StudKey.NOME) && studInObj.has(StudKey.COGNOME) &&
				studInObj.has(StudKey.NASCITA) && studInObj.has(StudKey.CDL) && studInObj.has(StudKey.ANNO);
	}
	
	private boolean isValidStudentQuery(JSONObject studInObj) {
		Iterator<String> keysIter = studInObj.keys();
		boolean isValid = true;
		while(keysIter.hasNext() && isValid) {
			String key = keysIter.next();
			if(!key.equals(StudKey.MATRICOLA) && !key.equals(StudKey.NOME) && !key.equals(StudKey.COGNOME) &&
			   !key.equals(StudKey.NASCITA) && !key.equals(StudKey.CDL) && !key.equals(StudKey.ANNO)) {
				isValid = false;
			}
		}
		return isValid;
	}
	
}
