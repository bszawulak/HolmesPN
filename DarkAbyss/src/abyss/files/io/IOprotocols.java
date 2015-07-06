/**
 * 
 */
package abyss.files.io;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import abyss.darkgui.GUIManager;
import abyss.graphpanel.GraphPanel;
import abyss.math.Arc;
import abyss.math.Arc.TypesOfArcs;
import abyss.math.ElementLocation;
import abyss.math.Node;
import abyss.math.Place;
import abyss.math.Transition;
import abyss.math.PetriNetElement.PetriNetElementType;

/**
 * Klasa odpowiedzialna za protokoły komunikacyjne z programem INA, Charlie, itd. Precyzyjnie,
 * posiada ona metody zapisu i odczytu plików sieci, inwariantów i innych zbiorów analitycznych
 * do/z różnych formatów plików.
 * @author students - pierwsze wersje metod w czterech oddzielnych klasach
 * @author MR - integracja w jedną klasę, writeINV - przeróbka, aby w ogóle działało
 *
 */
public class IOprotocols {
	private ArrayList<ArrayList<Integer>> invariantsList; // = new ArrayList<ArrayList<Integer>>();
	private ArrayList<Integer> nodesList; // = new ArrayList<Integer>();
	private ArrayList<Node> nodeArray; // = new ArrayList<Node>();
	private ArrayList<Arc> arcArray; // = new ArrayList<Arc>();
	private ArrayList<ElementLocation> elemArray; // = new ArrayList<ElementLocation>();
	
	private int MatSiz; // = 99999;
	@SuppressWarnings("unused")
	private String netName; // = "";
	private int globalPlaceNumber; // = 0;
	private int etap; // = 1;
	private int placeCount;//  = 0;
	private ArrayList<String[]> placeArcListPost; // = new ArrayList<String[]>();
	private ArrayList<ArrayList<Integer>> placeArcListPostWeight; // = new ArrayList<ArrayList<Integer>>();
	private ArrayList<String[]> placeArcListPre; // = new ArrayList<String[]>();
	private ArrayList<ArrayList<Integer>> placeArcListPreWeight; // = new ArrayList<ArrayList<Integer>>();
	
	/**
	 * Konstruktor obiektu klasy IOprotocols.
	 */
	public IOprotocols() {
		resetComponents();
	}
	
	/**
	 * Zwraca tablice inwariantow z wczytanego pliku INA
	 * @return invariantsList - lista inwariantów
	 */
	public ArrayList<ArrayList<Integer>> getInvariantsList() {
		return invariantsList;
	}
	
	/**
	 * Metoda resetująca pola klasy.
	 */
	private void resetComponents() {
		invariantsList = new ArrayList<ArrayList<Integer>>();
		nodesList = new ArrayList<Integer>();
		nodeArray = new ArrayList<Node>();
		arcArray = new ArrayList<Arc>();
		elemArray = new ArrayList<ElementLocation>();
		
		MatSiz = 99999;
		netName = "";
		globalPlaceNumber = 0;
		etap = 1;
		placeCount = 0;
		placeArcListPost = new ArrayList<String[]>();
		placeArcListPostWeight = new ArrayList<ArrayList<Integer>>();
		placeArcListPre = new ArrayList<String[]>();
		placeArcListPreWeight = new ArrayList<ArrayList<Integer>>();
	}

	/**
	 * Wczytywanie pliki t-inwariantów INA, wcześniej: INAinvariants.read
	 * Dodano poprawki oraz drugą ściękę odczytu - jako plik inwariantów Charliego.
	 * @param sciezka String - scieżka do pliku
	 * @return boolean - true, jeśli operacja się powiodła
	 */
	public boolean readINV(String sciezka) {
		try {
			resetComponents();
			DataInputStream in = new DataInputStream(new FileInputStream(sciezka));
			BufferedReader buffer = new BufferedReader(new InputStreamReader(in));
			String readLine = buffer.readLine();
			String backup = readLine;
			
			
			if (readLine.contains("transition sub/sur/invariants for net")) {
				//to znaczy, że wczytujemy plik INA, po prostu
			} else if (readLine.contains("List of all elementary modes")) {
				buffer.close();
				return readMonaLisaINV(sciezka);
			}else if (readLine.contains("minimal semipositive transition")) {
				buffer.close();
				return readCharlieINV(sciezka);
			} else {
				Object[] options = {"Read as INA file", "Read as MonaLisa file", "Read as Charlie file", "Terminate reading",};
				int decision = JOptionPane.showOptionDialog(null,
								"Unknown or corrupted invariants file format. Please choose format for this invariants file?",
								"Error reading file header", JOptionPane.YES_NO_OPTION,
								JOptionPane.WARNING_MESSAGE, null, options, options[0]);
				if (decision == 1) {
					buffer.close();
					return readCharlieINV(sciezka);
				} else if (decision == 2) { //Charlie
					buffer.close();
					return readCharlieINV(sciezka);
				} else if (decision == 3) {
					buffer.close();
					return false;
				}
				//jeśli nie 1, 2 lub 3 to znaczy, że 0, czyli na sieć czytamy dalej jako INA inv.
			}
			
			if(backup.contains("transition invariants basis")) {
				JOptionPane.showMessageDialog(null,"Wrong invariants. Only semipositives are acceptable.",
						"ERROR:readINV",JOptionPane.ERROR_MESSAGE);
				buffer.close();
				return false;
			}
			
			buffer.readLine();
			while (!readLine.contains("semipositive transition invariants =")) {
				readLine = buffer.readLine();
			}
			buffer.readLine();
			nodesList.clear();
			// Etap I - Liczba tranzycji/miejsc
			while (!(readLine = buffer.readLine()).endsWith("~~~~~~~~~~~")) {
				if(readLine.endsWith("~~~~~~~~~~~")){break;}
				String[] formattedLine = readLine.split(" ");
				for (int j = 0; j < formattedLine.length; j++) {
					if (!(formattedLine[j].isEmpty() || formattedLine[j].contains("Nr."))) {
						try {
							nodesList.add(Integer.parseInt(formattedLine[j]));
						} catch (NumberFormatException e) {}
					}
				}
			}
			// Etap II - lista T-inwariantow
			ArrayList<Integer> tmpInvariant = new ArrayList<Integer>();
			invariantsList.clear();
			while ((readLine = buffer.readLine()) != null) {
				if(readLine.contains("@")||readLine.isEmpty()){break;}
				String[] formattedLine = readLine.split("\\|");
				formattedLine = formattedLine[1].split(" ");
				for(int i = 0; i<formattedLine.length;i++)
				{
					if(formattedLine[i].isEmpty()){}else
					{
						tmpInvariant.add(Integer.parseInt(formattedLine[i]));
					}
				}
				if(tmpInvariant.size() == nodesList.size())
				{
					invariantsList.add(tmpInvariant);
					tmpInvariant = new ArrayList<Integer>();
				}
			}
			buffer.close();
			GUIManager.getDefaultGUIManager().log("Invariants from INA file have been read.", "text", true);
			return true;
		} catch (Exception e) {
			//JOptionPane.showMessageDialog(null,e.getMessage(),"ERROR:readINV",JOptionPane.ERROR_MESSAGE);
			GUIManager.getDefaultGUIManager().log("Invariants reading operation failed.", "error", true);
			return false;
		} 
	}
	

	/**
	 * Metoda wczytująca plik inwariantów wygenerowany programem Charlie.
	 * @param sciezka String - ściezka do pliku
	 * @return boolean - true, jeśli operacja się powiodła
	 */
	private boolean readCharlieINV(String sciezka) {
		try {
			DataInputStream in = new DataInputStream(new FileInputStream(sciezka));
			BufferedReader buffer = new BufferedReader(new InputStreamReader(in));
			String readLine = buffer.readLine();

			if (!readLine.contains("minimal semipositive transition")) {
				Object[] options = {"Force read as Charlie file", "Terminate reading",};
				int n = JOptionPane.showOptionDialog(null,
								"Unknown or corrupted invariants file format! Read anyway as Charlie invariants?",
								"Error reading file header", JOptionPane.YES_NO_OPTION,
								JOptionPane.WARNING_MESSAGE, null, options, options[0]);

				if (n == 1) {
					buffer.close();
					return false;
				}
				
			}
			nodesList.clear();
			
			ArrayList<Integer> tmpInvariant = new ArrayList<Integer>();
			boolean firstPass = true;
			
			ArrayList<Transition> namesCheck = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions();
			
			int transSetSize = namesCheck.size();
			for(int t=0; t<transSetSize; t++) //init
				tmpInvariant.add(0);
			
			readLine = buffer.readLine();
			while (readLine != null && readLine.length() > 0) {
				String lineStart = readLine.substring(0, readLine.indexOf("|"));
				lineStart = lineStart.replace(" ", "");
				lineStart = lineStart.replace("\t", "");
				
				if(lineStart.length() > 0 && firstPass == false) { //początek inwariantu
					invariantsList.add(tmpInvariant);

					tmpInvariant = new ArrayList<Integer>();
					for(int t=0; t<transSetSize; t++) // init
						tmpInvariant.add(0);

				} 
				firstPass = false;
				
				readLine = readLine.substring(readLine.indexOf("|")+1);
				readLine = readLine.replace(" ", "");
				readLine = readLine.replace("\t", "");
				
				String tmp =  readLine.substring(0, readLine.indexOf("."));
				int transNumber = Integer.parseInt(tmp); //numer tramzycji w zbiorze
				
				readLine = readLine.substring(readLine.indexOf(".")+1);
				String transName = readLine.substring(0, readLine.indexOf(":"));
				
				String orgName = namesCheck.get(transNumber).getName();
				if(!transName.equals(orgName)) {
					GUIManager.getDefaultGUIManager().log("Transition name and location does not match!"
							+ " Read transition: "+transName+" (loc:"+transNumber+"), while in net: "+orgName, "text", true);
				}
				
				readLine = readLine.substring(readLine.indexOf(":")+1);
				readLine = readLine.replace(",", "");
				int transValue = Integer.parseInt(readLine);
				
				if(transNumber >= transSetSize) {
					GUIManager.getDefaultGUIManager().log("Charlie invariants file has reference to non existing transitions in the current net."
							+ " Operation cancelled.", "text", true);
					buffer.close();
					return false;
				}
				tmpInvariant.set(transNumber, transValue);
				
				readLine = buffer.readLine();
			}
			
			//dodaj ostatni inwariant do listy
			invariantsList.add(tmpInvariant);

			buffer.close();
			GUIManager.getDefaultGUIManager().log("Invariants from Charlie file have been read.", "text", true);
			return true;
		} catch (Exception e) {
			GUIManager.getDefaultGUIManager().log("Charlie invariants reading operation failed.", "text", true);
			return false;
		} 
	}
	
	/**
	 * Metoda wczytująca plik inwariantów wygenerowany programem Charlie.
	 * @param sciezka String - ściezka do pliku
	 * @return boolean - true, jeśli operacja się powiodła
	 */
	private boolean readMonaLisaINV(String sciezka) {
		try {
			DataInputStream in = new DataInputStream(new FileInputStream(sciezka));
			BufferedReader buffer = new BufferedReader(new InputStreamReader(in));
			String line = buffer.readLine();

			if (!line.contains("# List of all elementary modes")) {
				Object[] options = {"Force read as Mona Lisa file", "Terminate reading",};
				int n = JOptionPane.showOptionDialog(null,
								"Unknown or corrupted invariants file format! Read anyway as Mona Lisa invariants?",
								"Error reading file header", JOptionPane.YES_NO_OPTION,
								JOptionPane.WARNING_MESSAGE, null, options, options[0]);

				if (n == 1) {
					buffer.close();
					return false;
				}
				
			}
			nodesList.clear();
			
			
			//boolean firstPass = true;
			
			ArrayList<Transition> transitions = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions();
			int transSetSize = transitions.size();
			
			ArrayList<Integer> tmpInvariant = new ArrayList<Integer>();

			line = buffer.readLine();
			while (line != null && line.length() > 0 && line.contains("Elementary")) {
				tmpInvariant = new ArrayList<Integer>();
				for(int t=0; t<transSetSize; t++) //init
					tmpInvariant.add(0);
				
				String lineNumber = line.substring(0, line.indexOf("."));
				
				try {
					line = line.substring(line.indexOf(":")+1);
					line = line.trim();
					String[] tablica = line.split(" ");
					for(String el : tablica) {
						if(el.contains("*")) {
							String valueS = el.substring(0, el.indexOf("*"));
							int value = Integer.parseInt(valueS);
							
							el = el.substring(el.indexOf("*")+1);
							int trans = Integer.parseInt(el);
							trans--; //MonaLisa liczy od 1 tranzycje
							tmpInvariant.set(trans, value);
						} else {
							int trans = Integer.parseInt(el);
							trans--; //MonaLisa liczy od 1 tranzycje
							tmpInvariant.set(trans, 1);
						}
					}
					invariantsList.add(tmpInvariant);
					line = buffer.readLine();
				} catch (Exception e) {
					GUIManager.getDefaultGUIManager().log("Error reading invariant #"+lineNumber, "error", true);
					line = buffer.readLine();
				}
			}
			buffer.close();
			GUIManager.getDefaultGUIManager().log("Invariants from MonaLisa file have been read.", "text", true);
			return true;
		} catch (Exception e) {
			GUIManager.getDefaultGUIManager().log("MonaLisa invariants reading operation failed.", "text", true);
			return false;
		} 
	}

	/**
	 * Zapis inwariantow w formacie INA.
	 * @param path - scieżka do pliku
	 * @param invariants ArrayList[ArrayList[Integer]] - lista niezmienników
	 * @param transitions ArrayList[Transition] - lista tranzycji
	 */
	public void writeINV(String path, ArrayList<ArrayList<Integer>> invariants, ArrayList<Transition> transitions) {
		try {
			String extension = "";
			if(!path.contains(".inv"))
				extension = ".inv";
			PrintWriter pw = new PrintWriter(path + extension);

			pw.print("transition sub/sur/invariants for net 0.t\r\n");
			pw.print("\r\n");
			pw.print("semipositive transition invariants =\r\n");
			pw.print("\r\n");
			pw.print("Nr.      ");

			int delimiter = 13;
			if(transitions.size() < 100)
				delimiter = 17;
			int multipl = 1;
			int transNo = invariants.get(0).size();
			
			for (int i = 0; i < transitions.size(); i++) {
				if(transNo >= 100)
					pw.print(convertIntToStr(true,i));
				else
					pw.print(convertIntToStr(false,i));
				
				if (i == (multipl*delimiter) - 1) {
					pw.print("\r\n");
					pw.print("        ");
					multipl++;
				}
			}
			pw.print("\r\n");
			pw.print("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
			pw.print("\r\n");

			for (int i = 0; i < invariants.size(); i++) { //po wszystkich inwariantach
				
				if(transNo >= 100) {
					pw.print(convertIntToStr(true,i) + " |   ");
				} else
					pw.print(convertIntToStr(false,i) + " |   ");
				
				multipl = 1;
				for (int t = 0; t < invariants.get(i).size(); t++) { //po wszystkich tranzycja inwariantu
					int tr = invariants.get(i).get(t); // nr tranzycji
					if (transNo >= 100)
						pw.print(convertIntToStr(true, tr)); //tutaj wstawiamy wartość dla tranz. w inw.
					else
						pw.print(convertIntToStr(false, tr)); //tutaj wstawiamy wartość dla tranz. w inw.
					
					if (t == (multipl*delimiter)-1 ) { //rozdzielnik wierszy
						pw.print("\r\n");
						if(transNo>=100)
							pw.print("      |   ");
						else
							pw.print("     |   ");
						multipl++;
					}
				}
				pw.print("\r\n");
			}
			pw.print("\r\n");
			pw.print("@");
			pw.close();
			GUIManager.getDefaultGUIManager().log("Invariants in INA file format saved to "+path, "text", true);
		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
			JOptionPane.showMessageDialog(null,e.getMessage(),"ERROR: writeINV",JOptionPane.ERROR_MESSAGE);
			GUIManager.getDefaultGUIManager().log("Error: " + e.getMessage(), "error", true);
		}
	}
	
	/**
	 * Metoda pomocnicza zwracająca liczbę w formie String z odpowiednią liczbą spacji.
	 * Metoda ta jest niezbędna do zapisu pliku inwariantów w formacie programu INA
	 * @param large boolean - true, jeśli dla dużej sieci.
	 * @param tr int - liczba do konwersji
	 * @return String - liczba po konwersji
	 */
	private String convertIntToStr(boolean large, int tr) {
		//String result = "";
		if(large) {
			if(tr<10)
				return "    "+tr;
			if(tr<100)
				return "   "+tr;
			if(tr<1000)
				return "  "+tr;
			else
				return " "+tr;
		} else { //smaller
			if(tr<10)
				return "   "+tr;
			if(tr<100)
				return "  "+tr;
			else
				return " "+tr;
		}
		//return result = " "+tr;
	}

	/**
	 * Metoda zwraca nazwę pliku.
	 * @param sciezka - scieżka do pliku
	 * @return String - nazwa pliku
	 */
	public String getFileName(String sciezka) {
		String[] tablica = sciezka.split("\\\\");
		return tablica[tablica.length - 1];
	}
	
	/**
	 * Metoda zwraca listę wezłów sieci po tym jak readPNT przeczyta plik INY
	 * @return nodeArray[Node] - tablica węzłów sieci
	 */
	public ArrayList<Node> getNodeArray() {
		return nodeArray;
	}

	/**
	 * Zwraca liste krawedzi sieci po tym jak readPNT przeczyta plik INY
	 * @return arcArray[Arc] - lista łuków sieci
	 */
	public ArrayList<Arc> getArcArray() {
		return arcArray;
	}

	/**
	 * Czyta plik sieci petriego w formacie PNT (INA)
	 * @param scieżka String - scieżka do pliku
	 */
	public void readPNT(String sciezka) {
		try {
			resetComponents();
			int SID = GUIManager.getDefaultGUIManager().getWorkspace().getProject().returnCleanSheetID();	
			DataInputStream in = new DataInputStream(new FileInputStream(sciezka));
			BufferedReader buffer = new BufferedReader(new InputStreamReader(in));
			String wczytanaLinia = buffer.readLine();
			String[] tabWczytanaLinia = wczytanaLinia.split(":");
			netName = tabWczytanaLinia[1];
			int trans[][] = new int[MatSiz][2];
			int ID = 0;
			String[] wID = new String[MatSiz];
			int[] wMark = new int[MatSiz];
			ArrayList<Integer> wagiWej = new ArrayList<Integer>();
			ArrayList<Integer> wagiWyj = new ArrayList<Integer>();

			while ((wczytanaLinia = buffer.readLine()) != null) {
				// Etap I

				// Wczytywanie informacji o Arcach i tokenach
				if (wczytanaLinia.equals("@")) {
					etap++;
				}
				switch (etap) {

				case 1:
					ArrayList<String> tmpStringWej = new ArrayList<String>();
					ArrayList<String> tmpStringWyj = new ArrayList<String>();

					wczytanaLinia = wczytanaLinia.replace(",", " , ");
					wczytanaLinia = wczytanaLinia.replace(":", " : ");
					String[] WczytanyString = wczytanaLinia.split(" ");
					int poz = 0;
					int poZap = 0;
					for (int j = 0; j < WczytanyString.length; j++) {
						if (!WczytanyString[j].isEmpty()) {
							if (!Character.isWhitespace(WczytanyString[j].charAt(0))) 
							{
								if (WczytanyString[j].contains(",")) {
									poZap = poz;
									poz = 5;
								}
								if (WczytanyString[j].contains(":")) {
									poZap = poz;
									poz = 4;
								}

								switch (poz) {
								// numer miejsca
								case 0:
									wID[ID] = WczytanyString[j];
									poz++;
									break;
								// ilosc tokenow
								case 1:
									wMark[ID] = Integer.parseInt(WczytanyString[j]);
									ID++;
									poz++;
									break;
								// wchodzace
								case 2:
									tmpStringWej.add(WczytanyString[j]);
									wagiWej.add(1);
									break;
								// wychodzace
								case 3:
									tmpStringWyj.add(WczytanyString[j]);
									wagiWyj.add(1);
									break;
								case 4:
									if (WczytanyString[j].contains(":")) {
									} else {
										if (poZap == 2) {
											wagiWej.remove(wagiWej.size() - 1);
											wagiWej.add(Integer.parseInt(WczytanyString[j]));
										} else {
											wagiWyj.remove(wagiWyj.size() - 1);
											wagiWyj.add(Integer.parseInt(WczytanyString[j]));
										}
										poz = poZap;
									}
									break;
								case 5:
									poz = poZap;
									poz++;
									break;
								}
							}
						}
					}
					String[] a = new String[tmpStringWej.size()];
					placeArcListPre.add(tmpStringWej.toArray(a));
					placeArcListPreWeight.add(wagiWej);
					a = new String[tmpStringWyj.size()];
					placeArcListPost.add(tmpStringWyj.toArray(a));
					placeArcListPostWeight.add(wagiWyj);

					break;
				case 2:
					// Etap II
					// Wczytywanie danych o miejscach

					if ((wczytanaLinia.contains("capacity") && wczytanaLinia.contains("time") 
							&& wczytanaLinia.contains("name")) || wczytanaLinia.equals("@")) {
					} else {
						tabWczytanaLinia = wczytanaLinia.split(": ");
						//String[] tmp4 = tabWczytanaLinia[0].split(" ");
						int placeNumber = globalPlaceNumber;
						globalPlaceNumber++;
						tabWczytanaLinia = tabWczytanaLinia[1].split(" ");
						String placeName = tabWczytanaLinia[0];
						Place tmpPlace = new Place(placeNumber, new ArrayList<ElementLocation>(), placeName, "", wMark[placeNumber]);
						ArrayList<ElementLocation> namesLoc = new ArrayList<ElementLocation>();
						namesLoc.add(new ElementLocation(0, new Point(0, 0), null));
						tmpPlace.setNamesLocations(namesLoc);
						
						nodeArray.add(tmpPlace);
					}
					break;
				case 3:
					// Etap III
					// Wczytywanie danych o tranzycjach
					if ((wczytanaLinia.contains("priority") && wczytanaLinia.contains("time") 
							&& wczytanaLinia.contains("name")) || wczytanaLinia.equals("@")) {
						placeCount = globalPlaceNumber;
					} else {
						tabWczytanaLinia = wczytanaLinia.split(": ");
						String[] tmp5 = tabWczytanaLinia[0].split(" ");
						for (int i = 0; i < tmp5.length; i++) {
							if (tmp5[i].equals("")) {
							} else {
								globalPlaceNumber++;
								trans[Integer.parseInt(tmp5[i])][0] = Integer.parseInt(tmp5[i]);
								trans[Integer.parseInt(tmp5[i])][1] = globalPlaceNumber;
							}
						}

						int transNumber = globalPlaceNumber;
						tabWczytanaLinia = tabWczytanaLinia[1].split(" ");
						String transName = tabWczytanaLinia[0];
						Transition tmpTrans = new Transition(transNumber, new ArrayList<ElementLocation>(), transName, "");
						ArrayList<ElementLocation> namesLoc = new ArrayList<ElementLocation>();
						namesLoc.add(new ElementLocation(0, new Point(0, 0), null));
						tmpTrans.setNamesLocations(namesLoc);
						nodeArray.add(tmpTrans);
						//mark++;
					}
					break;
				case 4:
					// Tworzenie Arców, szerokosci okna
					// tworzenie dla kazdego noda element location
					for (int j = 0; j < nodeArray.size(); j++) {
						if (nodeArray.get(j).getType() == PetriNetElementType.PLACE) {
							elemArray.add(new ElementLocation(SID, new Point(80, 30 + j * 60), nodeArray.get(j)));
							ArrayList<ElementLocation> tempElementLocationArry = new ArrayList<ElementLocation>();
							tempElementLocationArry.add(elemArray.get(j));
							nodeArray.get(j).setElementLocations(tempElementLocationArry);
						}

						if (nodeArray.get(j).getType() == PetriNetElementType.TRANSITION) {
							elemArray.add(new ElementLocation(SID, new Point(280, 30 + (j - placeCount) * 60), nodeArray.get(j)));
							ArrayList<ElementLocation> tempElementLocationArry = new ArrayList<ElementLocation>();
							tempElementLocationArry.add(elemArray.get(j));
							nodeArray.get(j).setElementLocations(tempElementLocationArry);
						}
					}

					int pozycja_a = 0;
					// Arki
					for (int k = 0; k < placeArcListPre.size(); k++) {
						for (int j = 0; j < placeArcListPre.get(k).length; j++) {
							int t1 = trans[Integer.parseInt(placeArcListPre.get(k)[j])][1];
							arcArray.add(new Arc(nodeArray.get(t1 - 1).getLastLocation(), 
									nodeArray.get(k).getLastLocation(), "", 
									placeArcListPreWeight.get(0).get(pozycja_a), TypesOfArcs.NORMAL));
							pozycja_a++;
						}
					}
					pozycja_a = 0;
					for (int k = 0; k < placeArcListPost.size(); k++) {
						for (int j = 0; j < placeArcListPost.get(k).length; j++) {
							int t2 = trans[Integer.parseInt(placeArcListPost.get(k)[j])][1];
							arcArray.add(new Arc(nodeArray.get(k).getLastLocation(), 
									nodeArray.get(t2 - 1).getLastLocation(), "",
									placeArcListPostWeight.get(0).get(pozycja_a), TypesOfArcs.NORMAL));
							pozycja_a++;

						}
					}

					int wid = Toolkit.getDefaultToolkit().getScreenSize().width - 20;
					int hei = Toolkit.getDefaultToolkit().getScreenSize().height - 20;
					int SIN = GUIManager.getDefaultGUIManager().IDtoIndex(SID);
					int tmpX = 0;
					int tmpY = 0;
					boolean xFound = false;
					boolean yFound = false;
					GraphPanel graphPanel = GUIManager.getDefaultGUIManager()
							.getWorkspace().getSheets().get(SIN).getGraphPanel();
					for (int l = 0; l < elemArray.size(); l++) {
						if (elemArray.get(l).getPosition().x > wid) {
							tmpX = l;
							xFound = true;
							wid = elemArray.get(l).getPosition().x;
						}
						if (elemArray.get(l).getPosition().y > hei) {
							tmpY = l;
							yFound = true;
							hei = elemArray.get(l).getPosition().y;
						}
					}
					if (xFound == true && yFound == false) {
						graphPanel.setSize(new Dimension(elemArray.get(tmpX)
								.getPosition().x + 90,graphPanel.getSize().height));
					}
					if (yFound == true && xFound == false) {
						graphPanel.setSize(new Dimension(
								graphPanel.getSize().width, elemArray.get(tmpY).getPosition().y + 90));
					}
					if (xFound == true && yFound == true) {
						graphPanel.setSize(new Dimension(elemArray.get(tmpX)
								.getPosition().x + 90, elemArray.get(tmpY).getPosition().y + 90));
					}
					graphPanel.setOriginSize(graphPanel.getSize());
					break;
				}
			}
			
			in.close();
			GUIManager.getDefaultGUIManager().log("Petri net from INA .pnt file successfully read.", "text", true);
		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
			JOptionPane.showMessageDialog(null,e.getMessage(),"ERROR: readPNT",JOptionPane.ERROR_MESSAGE);
			GUIManager.getDefaultGUIManager().log("Error: " + e.getMessage(), "error", true);
		}

	}

	/**
	 * Metoda służąca do zapisywaniu pliku sieci Petriego w formacie PNT (INA).
	 * @param path - ścieżka zapisu pliku
	 * @param placeList ArrayList[Place] - lista miejsc sieci
	 * @param transitionList ArrayList[Transition] - lista tranzycji sieci
	 * @param arcList ArrayList[Arc] - lista łuków sieci
	 * @return boolean - status operacji: true jeśli nie było problemów
	 */
	public boolean writePNT(String path, ArrayList<Place> placeList, ArrayList<Transition> transitionList, ArrayList<Arc> arcList) {
		String fileBuffer = "P   M   PRE,POST  NETZ 0:";
		try {
			PrintWriter writerObject = new PrintWriter(path);
			fileBuffer += getFileName(path);
			fileBuffer += "\r\n";
			//int[] tabPlace = new int[placeList.size()];

			for (int i = 0; i < placeList.size(); i++) {
				if (i < 9) {
					fileBuffer += " ";
				}
				if (i < 99) {
					fileBuffer += " ";
				}
				fileBuffer += i;
				fileBuffer += " ";
				fileBuffer += placeList.get(i).getTokensNumber();
				fileBuffer += "    ";

				// łuki
				if (placeList.get(i).getInArcs().isEmpty()
						&& placeList.get(i).getOutArcs().isEmpty()) {
					fileBuffer += " ";
				}
				if (placeList.get(i).getInArcs().size() > 0 && placeList.get(i).getOutArcs().isEmpty()) {
					for (int j = 0; j < placeList.get(i).getInArcs().size(); j++) {
						fileBuffer += " ";
						fileBuffer += transitionList.indexOf(placeList.get(i).getInArcs().get(j).getStartNode());
						if (placeList.get(i).getInArcs().get(j).getWeight() > 1) {
							fileBuffer += ": "+ placeList.get(i).getInArcs().get(j).getWeight();
						}
					}
				}
				if (placeList.get(i).getInArcs().size() > 0 && placeList.get(i).getOutArcs().size() > 0) {
					for (int j = 0; j < placeList.get(i).getInArcs().size(); j++) {
						fileBuffer += " ";
						fileBuffer += transitionList.indexOf(placeList.get(i).getInArcs().get(j).getStartNode());
						if (placeList.get(i).getInArcs().get(j).getWeight() > 1) {
							fileBuffer += ": "+ placeList.get(i).getInArcs().get(j).getWeight();
						}
					}
					fileBuffer += ",";
					for (int j = 0; j < placeList.get(i).getOutArcs().size(); j++) {
						fileBuffer += " ";
						fileBuffer += transitionList.indexOf(placeList.get(i).getOutArcs().get(j).getEndNode());
						if (placeList.get(i).getOutArcs().get(j).getWeight() > 1) {
							fileBuffer += ": "+ placeList.get(i).getOutArcs().get(j).getWeight();
						}
					}
				}
				if (placeList.get(i).getInArcs().isEmpty() && placeList.get(i).getOutArcs().size() > 0) {
					fileBuffer += ",";
					for (int j = 0; j < placeList.get(i).getOutArcs().size(); j++) {
						fileBuffer += " ";
						fileBuffer += transitionList.indexOf(placeList.get(i).getOutArcs().get(j).getEndNode());
						if (placeList.get(i).getOutArcs().get(j).getWeight() > 1) {
							fileBuffer += ": "+ placeList.get(i).getOutArcs().get(j).getWeight();
						}
					}
				}
				fileBuffer += "\r\n";
			}
			fileBuffer += "@\r\n";
			fileBuffer += "place nr.             name capacity time\r\n";

			for (int i = 0; i < placeList.size(); i++) {
				fileBuffer += "     ";
				if (i < 9) {
					fileBuffer += " ";
				}
				if (i < 99) {
					fileBuffer += " ";
				}
				fileBuffer += i;
				fileBuffer += ": ";
				fileBuffer += placeList.get(i).getName() + "                  ";
				fileBuffer += "65535    0";
				fileBuffer += "\r\n";
			}

			fileBuffer += "@\r\n";
			fileBuffer += "trans nr.             name priority time\r\n";
			for (int i = 0; i < transitionList.size(); i++) {
				fileBuffer += "     ";
				if (i <= 9) {
					fileBuffer += " ";
				}
				if (i <= 99) {
					fileBuffer += " ";
				}
				fileBuffer += i;
				fileBuffer += ": ";
				/*
				if (transitionList.get(i).getName().length() > 16) {
					tmpNazwy = transitionList.get(i).getName();
					
					tmpNazwy = tmpNazwy.substring(0, 16);
					
				} else {
					tmpNazwy = transitionList.get(i).getName();
					wielkoscNazwy = tmpNazwy.length();
					for (int k = wielkoscNazwy; k < 16; k++) {
						przerwa += " ";
					}
				}*/
				
				fileBuffer += transitionList.get(i).getName() + "                      ";
				fileBuffer += "0    0";
				fileBuffer += "\r\n";
			}
			
			fileBuffer += "@";
			writerObject.println(fileBuffer);
			writerObject.close();
			GUIManager.getDefaultGUIManager().log("Petri net exported as .pnt INA format. File: "+path, "text", true);
			GUIManager.getDefaultGUIManager().markNetSaved();
			return true;
		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
			JOptionPane.showMessageDialog(null,e.getMessage(),"ERROR: writePNT", JOptionPane.ERROR_MESSAGE);
			GUIManager.getDefaultGUIManager().log("Error: " + e.getMessage(), "error", true);
			return false;
		}
	}
	
	/**
	 * Metoda zapisująca inwarianty w pliku w formacie programu Charlie.
	 * @param path String - ścieżka do pliku
	 * @param invariants ArrayList[ArrayList[Integer]] - macierz inwariantów
	 * @param transitions ArrayList[Transition] - wektor tranzycji
	 */
	public void writeCharlieInv(String path, ArrayList<ArrayList<Integer>> invariants, ArrayList<Transition> transitions) {
		try {
			String extension = "";
			if(!path.contains(".inv"))
				extension = ".inv";
			PrintWriter pw = new PrintWriter(path + extension);
			pw.print("minimal semipositive transition invariants=");

			for (int i = 0; i < invariants.size(); i++) { //po wszystkich inwariantach
				//pw.print(i+1);
				boolean nrPlaced = false;
				for (int t = 0; t < invariants.get(i).size(); t++) { //po wszystkich tranzycjach
					int value = invariants.get(i).get(t);
					if(value == 0) {
						continue;
					}
					
					if(nrPlaced == false) {
						pw.print("\r\n"+(i+1)+"\t|\t");
						nrPlaced = true;
					} else {
						pw.print(",\r\n");
						pw.print("\t|\t");
					}
					String name = transitions.get(t).getName();
					pw.print(t+"."+name+"\t\t:"+value);
				}
			}
			pw.close();
			GUIManager.getDefaultGUIManager().log("Invariants in Charlie file format saved to "+path, "text", true);
		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
			JOptionPane.showMessageDialog(null,e.getMessage(),"ERROR:writeCharlieInv",JOptionPane.ERROR_MESSAGE);
			GUIManager.getDefaultGUIManager().log("Error: " + e.getMessage(), "error", true);
		}
	}
	
	/**
	 * Metoda zapisująca inwarianty w formacie Comma Separated Value.
	 * @param path String - ścieżka do pliku zapisu
	 * @param invariants ArrayList[ArrayList[Integer]] - macierz inwariantów
	 * @param transitions ArrayList[Transition] - wektor tranzycji
	 */
	public void writeInvToCSV(String path, ArrayList<ArrayList<Integer>> invariants, ArrayList<Transition> transitions) {
		try {
			String extension = "";
			if(!path.contains(".csv"))
				extension = ".csv";
			PrintWriter pw = new PrintWriter(path + extension);
			//pw.print(";");
			for (int i = 0; i < transitions.size(); i++) {
				pw.print(";"+i+"."+transitions.get(i).getName());
			}
			pw.print("\r\n");

			boolean crazyMode = GUIManager.getDefaultGUIManager().accessClusterWindow().crazyMode;
			if(crazyMode) {
				for (int i = 0; i < invariants.size(); i++) { //po wszystkich inwariantach
					pw.print(i+1);
					for (int t = 0; t < invariants.get(i).size(); t++) { //po wszystkich tranzycjach
						int value = invariants.get(i).get(t);
						if(value>0)
							value = 1;
						pw.print(";"+value);
					}
					pw.print("\r\n");
				}
			} else {
				for (int i = 0; i < invariants.size(); i++) { //po wszystkich inwariantach
					pw.print(i+1);
					for (int t = 0; t < invariants.get(i).size(); t++) { //po wszystkich tranzycjach
						int value = invariants.get(i).get(t);
						pw.print(";"+value);
					}
					pw.print("\r\n");
				}
			}
			pw.close();
			GUIManager.getDefaultGUIManager().log("Invariants saved as CSV file "+path, "text", true);
		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
			JOptionPane.showMessageDialog(null,e.getMessage(),"ERROR:writeInvToCSV",JOptionPane.ERROR_MESSAGE);
			GUIManager.getDefaultGUIManager().log("Error: " + e.getMessage(), "error", true);
		}
	}
}
