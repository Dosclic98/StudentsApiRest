package dos.studente.searcher;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Set;

import org.json.JSONObject;

import dos.studente.StudKey;

public class StudSearcher {
	
	private static ProcessBuilder process = null;
	private static String cmdNamePar = "grep -l -w ";
	
	public static ArrayList<Integer> searchListStud(JSONObject studIn) {
		ArrayList<String> files = new ArrayList<String>();
		try {
			process = new ProcessBuilder();
			String cmd = genCmd(studIn);
			if(cmd == null) return new ArrayList<Integer>(0);
			
			process.directory(new File("/home/dosclic98/Scrivania/Materiale_Uni/Terzo_anno/Primo_semestre/Progettazione e implementazione di software in rete/Esercizio_API_Rest/studenti"));
			process.command("bash", "-c", cmd);
			Process proc = process.start();

			System.out.println(process.command().toString());
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			String line;
			while ((line = reader.readLine()) != null) {
				files.add(line);
			}
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return parseFilesAsId(files);
	}
	
	private static ArrayList<Integer> parseFilesAsId(ArrayList<String> files) {
		ArrayList<Integer> ids = new ArrayList<Integer>();
		for(String file : files) {
			String[] spl = file.split("\\.");
			assert(spl.length == 2);
			ids.add(Integer.parseInt(spl[0]));
		}
		return ids;
	}
	
	private static String genCmd(JSONObject studIn) {
		Set<String> keySet = studIn.keySet();
		Object[] keyArr = keySet.toArray();
		String cmd = null;
		if(keyArr.length == 0) return null;
		else if(keyArr.length == 1) {
			cmd = cmdNamePar + "\"" + genMatching((String) keyArr[0], studIn) + "\"" + " *";
		} else {
			for(int i = 0; i < keyArr.length; i++) {
				if(i == 0) {
					cmd = cmdNamePar + "\"" + genMatching((String) keyArr[i], studIn) + "\"" + " *" + " > tmp.txt && ";
				} else if(i == keyArr.length - 1) {
					cmd += cmdNamePar + "\"" + genMatching((String) keyArr[i], studIn) + "\"" + " $(cat tmp.txt)";
				} else {
					cmd += cmdNamePar + "\"" + genMatching((String) keyArr[i], studIn) + "\"" + " $(cat tmp.txt) > tmp.txt && ";
				}
			}	
		}
		return cmd;
	}
	
	private static String genMatching(String key, JSONObject studIn) {
		if(key.equals(StudKey.MATRICOLA)) {
			return "\\\"" + StudKey.MATRICOLA + "\\\": \\\"" + studIn.getString(key) + "\\\"";
		} else if(key.equals(StudKey.NOME)) {
			return "\\\"" + StudKey.NOME + "\\\": \\\"" + studIn.getString(key) + "\\\"";
		} else if(key.equals(StudKey.COGNOME)) {
			return "\\\"" + StudKey.COGNOME + "\\\": \\\"" + studIn.getString(key) + "\\\"";
		} else if(key.equals(StudKey.NASCITA)) {
			return "\\\"" + StudKey.NASCITA + "\\\": \\\"" + studIn.getString(key) + "\\\"";
		} else if(key.equals(StudKey.CDL)) {
			return "\\\"" + StudKey.CDL + "\\\": \\\"" + studIn.getString(key) + "\\\"";
		} else if(key.equals(StudKey.ANNO)) {
			return "\\\"" + StudKey.ANNO + "\\\":" + studIn.getInt(key) + "";
		} else {
			throw new IllegalArgumentException("Error generating command");
		}
	}
	
}
