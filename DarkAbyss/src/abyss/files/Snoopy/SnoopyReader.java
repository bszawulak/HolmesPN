package abyss.files.Snoopy;

import java.awt.Point;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import abyss.darkgui.GUIManager;
import abyss.graphpanel.IdGenerator;
import abyss.math.Arc;
import abyss.math.Node;
import abyss.math.Place;

/**
 * Klasa odpowiedzialna za wczytywanie tego całego syfu, jaki w pliki ładuje niemiecki geszeft 
 * (baczność!) Snoopy (spocznij!).
 * 
 * @author MR
 *
 */
public class SnoopyReader {
	private boolean classPN = false;
	private boolean TPN = false;
	private boolean extPN = false;
	private boolean otherPN = false;
	
	private ArrayList<Arc> arcList = new ArrayList<Arc>();
	private ArrayList<Node> nodesList = new ArrayList<Node>();
	private ArrayList<Integer> snoopyNodesIdList = new ArrayList<Integer>();
	
	
	/**
	 * Główny konstruktor klasy SnoopyReader.
	 * @param type int - typ sieci do wczytania
	 * @param path String - ścieżka do pliku
	 */
	public SnoopyReader(int type, String path) {
		if(type == 1) { //extended
			extPN = true;
		} else if(type == 2) { //TPN
			TPN = true;
		} else if(type == 3) { //other
			otherPN = true;
		} else { // classic
			classPN = true;
		}
		
		coreReader(path);
	}
	
	/**
	 * Metoda główna, delegująca zadania czytania odpowiednich bloków danych w pliku do metod
	 * pomocniczych.
	 * @param filepath String - ścieżka do pliku
	 */
	private void coreReader(String filepath) {
		BufferedReader buffer = null;
		try {
			DataInputStream dis = new DataInputStream(new FileInputStream(filepath));
			buffer = new BufferedReader(new InputStreamReader(dis));
			GUIManager.getDefaultGUIManager().log("Reading Snoopy file: "+filepath, "text", true);	
			
			String line = buffer.readLine();
			// PLACES
			while(!((line = buffer.readLine()).contains("<nodeclass count="))) //przewiń do miejsc
				;
			if(!line.contains("<nodeclass count=\"0\"") && line.contains("name=\"Place\"")) { //są jakieś miejsca
				readSnoopyPlaces(buffer, line);
			}
			
			// TRANSITIONS
			while(!((line = buffer.readLine()).contains("<nodeclass count="))) //przewiń do tranzycji
				;
			if(!line.contains("<nodeclass count=\"0\"") && line.contains("name=\"Transition\"")) { //są jakieś tranzycje
				readSnoopyTransitions(buffer, line);
			}
			
			// COARSE PLACES
			while(!((line = buffer.readLine()).contains("<nodeclass count="))) //przewiń do c-miejsc
				;
			if(!line.contains("<nodeclass count=\"0\"")) { //są jakieś c-miejsca
				readSnoopyCoarsePlaces(buffer, line);
			}
			
			// COARSE TRANSITIONS
			while(!((line = buffer.readLine()).contains("<nodeclass count="))) //przewiń do c-tranzycji
				;
			if(!line.contains("<nodeclass count=\"0\"")) { //są jakieś c-tranzycje
				readSnoopyCoarseTransitions(buffer, line);
			}
			
			//buffer.close();
		} catch (Exception e) {
			GUIManager.getDefaultGUIManager().log("Reading Snoopy net failed.", "error", true);
		} finally {
			try { buffer.close(); }
			catch (Exception e2) {}
		}
	}
	
	private void readSnoopyPlaces(BufferedReader buffer, String line) {
		String buffLine = line;
		int placesCounter = 0;
		int placesLimit = 0;
		try {
			//policz teoretyczną liczbę miejsc
			buffLine = buffLine.substring(buffLine.indexOf("count=")+7); //<nodeclass count="44" name="Place">
			buffLine = buffLine.substring(0, buffLine.indexOf("\""));
			placesLimit = Integer.parseInt(buffLine);
			
			boolean readForward = true;
			boolean goBoldly = true;
			
			while(readForward) { 
				buffLine = buffer.readLine(); //na początku: <node id="226" net="1">
				if(line.contains("</nodeclass>")) {
					readForward = false;
					continue;
				}
				
				int nodeSnoopyID = (int) getAttributeValue(buffLine, " id=", -1);
				if(nodeSnoopyID == -1) {
					GUIManager.getDefaultGUIManager().log("Catastrophic error: could not read Snoopy Place ID from line: "+buffLine, "error", true);
					break;
				}
				int subNet = (int) getAttributeValue(buffLine, " net=", 0);
				
				Place place = new Place(IdGenerator.getNextId(), subNet, new Point(100,100));
				
				goBoldly = true;
				while(goBoldly) { //czytanie właściwości miejsca
					buffLine = buffer.readLine();
					if(line.contains("</node>")) {
						readForward = false;
						continue;
					}
					
					
				}
			}
			
		} catch (Exception e) {
			GUIManager.getDefaultGUIManager().log("Reading Snoopy places failed in line:", "error", true);
			GUIManager.getDefaultGUIManager().log(buffLine, "error", true);
		}
	}

	private void readSnoopyTransitions(BufferedReader buffer, String line) {
		String buffLine = line;
		try {
			
		} catch (Exception e) {
			GUIManager.getDefaultGUIManager().log("Reading Snoopy transitions failed in line:", "error", true);
			GUIManager.getDefaultGUIManager().log(buffLine, "error", true);
		}
	}

	private void readSnoopyCoarsePlaces(BufferedReader buffer, String line) {
		String buffLine = line;
		try {
			
		} catch (Exception e) {
			GUIManager.getDefaultGUIManager().log("Reading Snoopy coarse places failed in line:", "error", true);
			GUIManager.getDefaultGUIManager().log(buffLine, "error", true);
		}
	}
	
	private void readSnoopyCoarseTransitions(BufferedReader buffer, String line) {
		String buffLine = line;
		try {

			
		} catch (Exception e) {
			GUIManager.getDefaultGUIManager().log("Reading Snoopy coarse transitions failed in line: ", "error", true);
			GUIManager.getDefaultGUIManager().log(buffLine, "error", true);
		}
	}
	
	/**
	 * Metoda wycina wartość liczbową zadanego parametru z linii.
	 * @param line String - linia z pliku
	 * @param signature String - atrybut (format: nazwa=" value ")
	 * @param defaultVal double - wartość domyślna, jeśli nie da się przeczytać
	 * @return double - wartość atrybutu
	 */
	private double getAttributeValue(String line, String signature, double defaultVal) {
		double result = defaultVal;
		try {
			int location = line.indexOf(signature);
			String tmp = line.substring(location + signature.length() + 1);
			location = line.indexOf("\"");
			tmp = line.substring(0, location);
			result = Double.parseDouble(tmp);
		} catch (Exception e) {	
		}
		return result;
	}
	
	private String readStrCDATA(BufferedReader buffer, String line, String defaultVal) {
		try {
			while(line.contains("")) {
				
			}
			//TODO: wczytywanie wielu linii
			//e.g.:
			//          <![CDATA[DNA containing an APE-bound AP site
			//
			//do tego miejsca wchodza wszystkie monofunkcyjne]]>
			String result = "";
			int location = line.indexOf("[CDATA[");
			
			
			return result;
		} catch (Exception e) {	
			return defaultVal;
		}
	}
	
	/**
	 * Metoda pozwala pobrać listę łuków.
	 * @return arcList - lista z łukami sieci
	 */
	public ArrayList<Arc> getArcList() {
		return arcList;
	}

	/**
	 * Metoda pozwala pobrać wierzchołków.
	 * @return nodesList - zwraca listę z wierzchołkami sieci
	 */
	public ArrayList<Node> getNodesList() {
		return nodesList;
	}
}
