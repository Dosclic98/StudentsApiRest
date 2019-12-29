package dos.handler;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
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
import dos.studente.searcher.StudSearcher;
import dos.studente.studExc.StudentAlreadyExistingException;
import dos.studente.studExc.StudentNotExistingException;

public class RequestHandler implements HttpHandler {
	
	private final String resNameDB = "studenti";
	
	@Override
	public void handle(HttpExchange he) throws IOException {
		System.out.println("\n****** Request recieved ******");
		
		Headers headers = he.getRequestHeaders();
		String method = he.getRequestMethod();
		
		String response = null;
		int resCode = 0;
		
		if(method.contentEquals("POST")) {
			System.out.println("Method: POST");
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
						// TODO Test della POST XML
					}					
					try {
						if(stud != null && isValidStudent(stud)) {
							System.out.println(stud.toString());
							Studente studWr = new Studente(newId, stud);
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
			System.out.println("Method: GET");
			String resName = null;
			String resType = null;

			try {
				Matcher matcher = parseAccept(headers);
				resName = matcher.group(1);
				resType = matcher.group(2);
				JSONObject stud = null;
				if(resName.equals(resNameDB) && (resType.equals("json") || resType.equals("xml"))) {
					
					stud = getQueryAsJson(he);
					System.out.println("Request query: " + stud.toString());
					if(isValidStudentQuery(stud)) {
						ArrayList<Integer> ids = null;
						ArrayList<Studente> valStuds = new ArrayList<Studente>();
						if(stud.has(StudKey.ID)) {
							ids = new ArrayList<Integer>();
							int qId = stud.getInt(StudKey.ID);
							stud.remove(StudKey.ID);
							ids = StudSearcher.searchListStud(stud);
							removeUnmatchedId(qId, ids);
						} else {
							ids = StudSearcher.searchListStud(stud);
						}
						for(Integer id : ids) {
							valStuds.add(0, new Studente(id));
							valStuds.get(0).loadStudent();
						}
						JSONArray jsonStudentsArr = new JSONArray();
						for(Studente st : valStuds) {
							JSONObject stJsonObj  = st.studentToJsonObj();
							stJsonObj.put(StudKey.ID, st.id);
							jsonStudentsArr.put(stJsonObj);
						}
						
						System.out.println("Valid studs: " + jsonStudentsArr.toString());
						if(resType.equals("json")) {
							response = jsonStudentsArr.toString();
							resCode = 200;
						} else if (resType.equals("xml")) {
							response = XML.toString(jsonStudentsArr, "studente");
							resCode = 200;
						}
						
						if(ids.size() == 0) {
							response = "No student found matching with search\n";
							resCode = 404;
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
			} catch(IOException e3) {
				response = "No student found matching with search\n";
				resCode = 404;							
			}
			
			
		} else if(method.contentEquals("PUT")) {
			System.out.println("Method: PUT");
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
						stud = getJsonStudentFromRequest(he);
						
					} else if (resType.equals("xml")) {
						stud = getXMLStudentFromRequest(he);
					}					
					try {
						if(stud != null && isValidStudentMod(stud)) {
							newId = stud.getInt(StudKey.ID);
							System.out.println("Request body: " + stud.toString());
							Studente studMod = new Studente(newId);
							studMod.loadStudent();
							JSONObject oldStud = studMod.studentToJsonObj();
							modStud(oldStud, stud);
							studMod = new Studente(newId, oldStud);
							studMod.saveModJson();
							response = "Student modified correctly\n";
							resCode = 200;	
						} else {
							response = "Error: student format incorrect\n";
							resCode = 404;
						}
					} catch (IOException e3) {
						response = "Error: Unable to access resource";
						resCode = 404;
					} catch (StudentNotExistingException e) {
						response = "Error: Tryig to modify a not existing student";
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
			
			
		} else if(method.contentEquals("DELETE")) {
			System.out.println("Method: DELETE");
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
						stud = getJsonStudentFromRequest(he);
						
					} else if (resType.equals("xml")) {
						stud = getXMLStudentFromRequest(he);
					}					
					try {
						if(stud != null && isValidStudentDel(stud)) {
							newId = stud.getInt(StudKey.ID);
							System.out.println("Request body: " + stud.toString());
							Studente studDel = new Studente(newId);
							// eliminare studente
							studDel.delete();
							response = "Student deleted correctly\n";
							resCode = 200;	
						} else {
							response = "Error: student format incorrect\n";
							resCode = 404;
						}
					} catch (IOException e3) {
						response = "Error: Unable to delete resource (not existing)";
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
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}		
		System.out.println("Response code: " + resCode);
		System.out.println("Response message: " + response);
		
		he.sendResponseHeaders(resCode, response.getBytes().length);
		OutputStream outputStream = he.getResponseBody();
		outputStream.write(response.getBytes());
		outputStream.close();	
	}
	
	private boolean isValidStudentDel(JSONObject stud) {
		Set<String> keys = stud.keySet();
		for(String key : keys) {
			if(!key.equals(StudKey.ID)) {
				return false;
			}
		}
		return true;
	}
	
	private void modStud(JSONObject oldStud, JSONObject stud) {
		Set<String> keys = stud.keySet();
		for(String key : keys) {
			if(key.equals(StudKey.MATRICOLA)) {
				oldStud.put(StudKey.MATRICOLA, stud.get(StudKey.MATRICOLA));
			} else if(key.equals(StudKey.NOME)) {
				oldStud.put(StudKey.NOME, stud.get(StudKey.NOME));
			} else if(key.equals(StudKey.COGNOME)) {
				oldStud.put(StudKey.COGNOME, stud.get(StudKey.COGNOME));
			} else if(key.equals(StudKey.NASCITA)) {
				oldStud.put(StudKey.NASCITA, stud.get(StudKey.NASCITA));
			} else if(key.equals(StudKey.CDL)) {
				oldStud.put(StudKey.CDL, stud.get(StudKey.CDL));
			} else if(key.equals(StudKey.ANNO)) {
				oldStud.put(StudKey.ANNO, stud.get(StudKey.ANNO));
			} 
			else if(key.equals(StudKey.ID)) {
				// Niente
			} else {
				throw new IllegalArgumentException("Error: unable to modify student");
			}
		}
	}
	
	private void removeUnmatchedId(int qId, ArrayList<Integer> ids) {
		int i = 0; 
		while(i < ids.size()) {
			if(!ids.get(i).equals(qId)) {
				ids.remove(i);
			} else i++;
		}
	}
	
	private JSONObject getQueryAsJson(HttpExchange he) {
		String studentToParse = he.getRequestURI().getQuery();

		return new JSONObject(studentToParse);
	}
	
	private Matcher parseAccept(Headers hd) {
		String rgx = "application/(\\w+)\\+(\\w+)";
		
		String acceptToParseString = hd.getFirst("Accept");
		System.out.println("Accept: " + acceptToParseString);
		
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
		if(studOut.has(StudKey.MATRICOLA)) {
			Integer matr = studOut.getInt(StudKey.MATRICOLA);
			studOut.put(StudKey.MATRICOLA, matr.toString());			
		}
		return studIn.getJSONObject("studente");
		
	}
	
	private boolean isValidStudent(JSONObject studInObj) {
		boolean hasAll = studInObj.has(StudKey.MATRICOLA) && studInObj.has(StudKey.NOME) && studInObj.has(StudKey.COGNOME) &&
				studInObj.has(StudKey.NASCITA) && studInObj.has(StudKey.CDL) && studInObj.has(StudKey.ANNO);
		Set<String> keys = studInObj.keySet();
		boolean notHasDiff = true;
		for(String key : keys) {
			if(!key.equals(StudKey.MATRICOLA) && !key.equals(StudKey.NOME) && 
					!key.equals(StudKey.COGNOME) && !key.equals(StudKey.NASCITA) && 
					!key.equals(StudKey.CDL) && !key.equals(StudKey.ANNO)) {
				notHasDiff = false;
				break;
			}
		}
		return hasAll && notHasDiff;
		
	}
	
	private boolean isValidStudentMod(JSONObject studInObj) {
		if(studInObj.has(StudKey.ID)) {
			boolean hasAtLeast = studInObj.has(StudKey.MATRICOLA) || studInObj.has(StudKey.NOME) || studInObj.has(StudKey.COGNOME) ||
					studInObj.has(StudKey.NASCITA) || studInObj.has(StudKey.CDL) || studInObj.has(StudKey.ANNO);
			Set<String> keys = studInObj.keySet();
			boolean notHasDiff = true;
			for(String key : keys) {
				if(!key.equals(StudKey.MATRICOLA) && !key.equals(StudKey.NOME) && 
						!key.equals(StudKey.COGNOME) && !key.equals(StudKey.NASCITA) && 
						!key.equals(StudKey.CDL) && !key.equals(StudKey.ANNO) &&
						!key.equals(StudKey.ID)) {
					notHasDiff = false;
					break;
				}
			}
			return hasAtLeast && notHasDiff;
		} else return false;
	}
	
	private boolean isValidStudentQuery(JSONObject studInObj) {
		Iterator<String> keysIter = studInObj.keys();
		boolean isValid = true;
		while(keysIter.hasNext() && isValid) {
			String key = keysIter.next();
			if(!key.equals(StudKey.MATRICOLA) && !key.equals(StudKey.NOME) && !key.equals(StudKey.COGNOME) &&
			   !key.equals(StudKey.NASCITA) && !key.equals(StudKey.CDL) && !key.equals(StudKey.ANNO) && !key.equals(StudKey.ID)) {
				isValid = false;
			}
		}
		return isValid;
	}
	
}
