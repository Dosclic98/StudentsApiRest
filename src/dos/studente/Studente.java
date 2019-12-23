package dos.studente;

import java.io.File;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.json.JSONObject;
import org.json.XML;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONTokener;

import dos.studente.studExc.IllegalStudentException;
import dos.studente.studExc.StudentAlreadyExistingException;
import dos.studente.studExc.StudentNotExistingException;

public class Studente {
	private static String folderName = "studenti/";
	
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
	
	public Studente(int id) {
		this.id = id;
	}
	
	public Studente(int id, XML studXML) {
		this(id, XML.toJSONObject(studXML.toString()));
	}
	
	public void loadStudent() throws IOException {
		String fileName = genFilePath();
		JSONObject studJson = getStudentContent(fileName);
		
		this.matricola = studJson.getString(StudKey.MATRICOLA); 
		this.nome = studJson.getString(StudKey.NOME);
		this.cognome = studJson.getString(StudKey.COGNOME);
		this.nascita = LocalDate.parse(studJson.getString(StudKey.NASCITA), DateTimeFormatter.ofPattern("dd-MM-yyyy"));
		this.CDL = studJson.getString(StudKey.CDL);
		this.anno = studJson.getInt(StudKey.ANNO);
	}
	
	public void saveNewJson() throws StudentAlreadyExistingException, IOException {
		File tmpFile = new File(genFilePath());
		if(!tmpFile.exists()) {
			JSONObject studToWrite = studentToJsonObj();
			writeStudentOnFile(genFilePath(), studToWrite);
		} else throw new StudentAlreadyExistingException();
	}
	
	public void saveModJson() throws IOException, StudentNotExistingException {
		File tmpFile = new File(genFilePath());
		if(tmpFile.exists()) {
			JSONObject studToWrite = studentToJsonObj();
			writeStudentOnFile(genFilePath(), studToWrite);
		} else throw new StudentNotExistingException();
	}
	
	public String jsonDesc() {
		JSONObject stud = studentToJsonObj();
		return stud.toString();
	}

	public String XMLDesc() {
		JSONObject studJson = studentToJsonObj();
		return XML.toString(studJson, "studente");
	}
	
	private String genFilePath() {
		Integer tmpId = id;
		return folderName + tmpId + ".json";
	}

	/*	
	private String getFileContent(String fileName) throws IOException {
		File file = new File(fileName);
		FileInputStream fis = new FileInputStream(file);
		byte[] data = new byte[(int) file.length()];
		fis.read(data);
		fis.close();
		String str = new String(data, "UTF-8");
		
		return str;
	}
*/	
	private JSONObject getStudentContent(String fileName) throws FileNotFoundException {
		JSONTokener studFile = new JSONTokener(new FileReader(fileName));
		return new JSONObject(studFile);
	}
	
	private JSONObject studentToJsonObj() {
		if(id == 0 || matricola == null || nome == null || cognome == null ||
		   nascita == null || CDL == null || anno == 0) {
			throw new IllegalStudentException();
		} else {
			JSONObject stud = new JSONObject();
			stud.put(StudKey.MATRICOLA, matricola);
			stud.put(StudKey.NOME, nome);
			stud.put(StudKey.COGNOME, cognome);
			stud.put(StudKey.NASCITA, nascita.toString());
			stud.put(StudKey.CDL, CDL);
			stud.put(StudKey.ANNO, anno);
			
			return stud;
		}
	}
	
	private void writeStudentOnFile(String fileName, JSONObject stud) throws IOException {
		FileWriter wr = new FileWriter(fileName);
		Gson gsonFile = new GsonBuilder()
				.setPrettyPrinting()
				.create();
		JsonObject arr = JsonParser.parseString(stud.toString()).getAsJsonObject();
		gsonFile.toJson(arr, wr);
		wr.flush();
        wr.close();

	}
	
	public static int gestNextId() {
		List<String> result = getListOfFiles();
		int arrId[];
		if(result != null) {
			if(result.isEmpty()) return 1;
			arrId = getSortedArrOfId(result);
			
			return arrId[arrId.length - 1] + 1;
		}
		else return 0;
	}
	
	private static List<String> getListOfFiles() {
		List<String> result = null;
		try (Stream<Path> walk = Files.walk(Paths.get(folderName))) {

			result = walk.filter(Files::isRegularFile)
					.map(x -> x.toString()).collect(Collectors.toList());

			result.forEach(System.out::println);

		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return result;
	}
	
	private static int[] getSortedArrOfId(List<String> res) {
		String rgx = folderName + "(\\d+).json";
				
		Pattern pattern = Pattern.compile(rgx);
		Matcher matcher = null;

		ArrayList<Integer> arrListId = new ArrayList<Integer>();
		
		for(String fileName : res) {
			matcher = pattern.matcher(fileName);
			if(matcher.find()) {
				arrListId.add(Integer.parseInt(matcher.group(1)));
			}
		}
		
		Collections.sort(arrListId);
		arrListId.forEach(System.out::println);
		
		int[] arrId = new int[arrListId.size()]; 
		
		for(int i = 0; i < arrListId.size(); i++) {
			arrId[i] = arrListId.get(i);
		} 
		
		return arrId;
	}

}
