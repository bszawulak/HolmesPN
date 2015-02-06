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
	//dawniej: pola klas INAinvariants
	private ArrayList<ArrayList<Integer>> invariantsList = new ArrayList<ArrayList<Integer>>();
	private ArrayList<Integer> nodesList = new ArrayList<Integer>();
	//dawniej: pola klasy INAreader
	private ArrayList<Node> nodeArray = new ArrayList<Node>();
	private ArrayList<Arc> arcArray = new ArrayList<Arc>();
	private ArrayList<ElementLocation> elemArray = new ArrayList<ElementLocation>();
	private int MatSiz = 999999;
	@SuppressWarnings("unused")
	private String netName = "";
	private int globalPlaceNumber = 0;
	private int etap = 1;
	private int placeCount = 0;
	private ArrayList<String[]> placeArcListPost = new ArrayList<String[]>();
	private ArrayList<ArrayList<Integer>> placeArcListPostWeight = new ArrayList<ArrayList<Integer>>();
	private ArrayList<String[]> placeArcListPre = new ArrayList<String[]>();
	private ArrayList<ArrayList<Integer>> placeArcListPreWeight = new ArrayList<ArrayList<Integer>>();
	
	/**
	 * Zwraca tablice inwariantow z wczytanego pliku INA
	 * @return invariantsList - lista inwariantów
	 */
	public ArrayList<ArrayList<Integer>> getInvariantsList() {
		return invariantsList;
	}  

	/**
	 * Wczytywanie pliki t-inwariantów INA, wcześniej: INAinvariants.read
	 * Dodano poprawki oraz drugą ściękę odczytu - jako plik inwariantów Charliego.
	 * @param sciezka String - scieżka do pliku
	 */
	public void readINV(String sciezka) {
		try {
			DataInputStream in = new DataInputStream(new FileInputStream(sciezka));
			BufferedReader buffer = new BufferedReader(new InputStreamReader(in));
			String wczytanaLinia = buffer.readLine();
			String backup = wczytanaLinia;
			// invariantow dla miejsc nie ma, bo nie mam na czy sie wzorowac, a INA
			// mnie nie slucha (student)	
			// brzydka, niedobra INA... (MR)
			//transition invariants basis
			if (wczytanaLinia.contains("transition sub/sur/invariants for net")) {
				//to znaczy, że wczytujemy plik INA, po prostu
			} else if (wczytanaLinia.contains("minimal semipositive transition")) {
				buffer.close();
				readCharlieINV(sciezka);
				return;
			} else {
				Object[] options = {"Force read as INA file", "Force read as Charlie file", "Terminate reading",};
				int decision = JOptionPane.showOptionDialog(null,
								"Unknown or corrupted invariants file format. Force read as INA or Charlie invariants?",
								"Error reading file header", JOptionPane.YES_NO_OPTION,
								JOptionPane.WARNING_MESSAGE, null, options, options[0]);
				if (decision == 2) {
					buffer.close();
					return;
				} else if (decision == 1) { //Charlie
					buffer.close();
					readCharlieINV(sciezka);
					return;
				}
				//jeśli nie 2 i nie 1, to znaczy, że 0, czyli na sieć czytamy dalej jako INA inv.
			}
			
			if(backup.contains("transition invariants basis")) {
				JOptionPane.showMessageDialog(null,"Wrong invariants. Only semipositives are acceptable.",
						"ERROR:readINV",JOptionPane.ERROR_MESSAGE);
				buffer.close();
				return;
			}
			
			buffer.readLine();
			while (!wczytanaLinia.contains("semipositive transition invariants =")) {
				wczytanaLinia = buffer.readLine();
			}
			buffer.readLine();
			nodesList.clear();
			// Etap I - Liczba tranzycji/miejsc
			while (!(wczytanaLinia = buffer.readLine()).endsWith("~~~~~~~~~~~")) {
				if(wczytanaLinia.endsWith("~~~~~~~~~~~")){break;}
				String[] sformatowanaLinia = wczytanaLinia.split(" ");
				for (int j = 0; j < sformatowanaLinia.length; j++) {
					if (!(sformatowanaLinia[j].isEmpty() || sformatowanaLinia[j].contains("Nr."))) {
						try {
							nodesList.add(Integer.parseInt(sformatowanaLinia[j]));
						} catch (NumberFormatException e) {}
					}
				}
			}
			// Etap II - lista T-inwariantow
			ArrayList<Integer> tmpInvariant = new ArrayList<Integer>();
			invariantsList.clear();
			while ((wczytanaLinia = buffer.readLine()) != null) {
				if(wczytanaLinia.contains("@")||wczytanaLinia.isEmpty()){break;}
				String[] sformatowanaLinia = wczytanaLinia.split("\\|");
				sformatowanaLinia = sformatowanaLinia[1].split(" ");
				for(int i = 0; i<sformatowanaLinia.length;i++)
				{
					if(sformatowanaLinia[i].isEmpty()){}else
					{
						tmpInvariant.add(Integer.parseInt(sformatowanaLinia[i]));
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
		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
			JOptionPane.showMessageDialog(null,e.getMessage(),"ERROR:readINV",JOptionPane.ERROR_MESSAGE);
			GUIManager.getDefaultGUIManager().log("Error: "+e.getMessage(), "error", true);
		} 
	}
	
//TODO
	
	private void readCharlieINV(String sciezka) {
		if(sciezka!=null)
			return; //trololo
		
		
		try {
			DataInputStream in = new DataInputStream(new FileInputStream(sciezka));
			BufferedReader buffer = new BufferedReader(new InputStreamReader(in));
			String wczytanaLinia = buffer.readLine();

			if (!wczytanaLinia.contains("minimal semipositive transition")) {
				Object[] options = {"Force read as Charlie file", "Terminate reading",};
				int n = JOptionPane.showOptionDialog(null,
								"Unknown or corrupted invariants file format! Read anyway as INA invariants?",
								"Error reading file header", JOptionPane.YES_NO_OPTION,
								JOptionPane.WARNING_MESSAGE, null, options, options[0]);

				if (n == 1) {
					buffer.close();
					return;
				}
				
			}
			
			buffer.readLine();
			while (!wczytanaLinia.contains("semipositive transition invariants =")) {
				wczytanaLinia = buffer.readLine();
			}
			buffer.readLine();
			nodesList.clear();
			// Etap I - Liczba tranzycji/miejsc
			while (!(wczytanaLinia = buffer.readLine()).endsWith("~~~~~~~~~~~")) {
				if(wczytanaLinia.endsWith("~~~~~~~~~~~")){break;}
				String[] sformatowanaLinia = wczytanaLinia.split(" ");
				for (int j = 0; j < sformatowanaLinia.length; j++) {
					if (!(sformatowanaLinia[j].isEmpty() || sformatowanaLinia[j].contains("Nr."))) {
						try {
							nodesList.add(Integer.parseInt(sformatowanaLinia[j]));
						} catch (NumberFormatException e) {}
					}
				}
			}
			// Etap II - lista T-inwariantow
			ArrayList<Integer> tmpInvariant = new ArrayList<Integer>();
			invariantsList.clear();
			while ((wczytanaLinia = buffer.readLine()) != null) {
				if(wczytanaLinia.contains("@")||wczytanaLinia.isEmpty()){break;}
				String[] sformatowanaLinia = wczytanaLinia.split("\\|");
				sformatowanaLinia = sformatowanaLinia[1].split(" ");
				for(int i = 0; i<sformatowanaLinia.length;i++)
				{
					if(sformatowanaLinia[i].isEmpty()){}else
					{
						tmpInvariant.add(Integer.parseInt(sformatowanaLinia[i]));
					}
				}
				if(tmpInvariant.size() == nodesList.size())
				{
					invariantsList.add(tmpInvariant);
					tmpInvariant = new ArrayList<Integer>();
				}
			}
			buffer.close();
			GUIManager.getDefaultGUIManager().log("Invariants from Charlie file have been read.", "text", true);
		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
			JOptionPane.showMessageDialog(null,e.getMessage(),"ERROR:readINV",JOptionPane.ERROR_MESSAGE);
		} 
		
	}

	/**
	 * Zapis inwariantow w formacie INA.
	 * @param path - scieśka do pliku
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
					pw.print(conIntToStr(true,i));
				else
					pw.print(conIntToStr(false,i));
				
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
					pw.print(conIntToStr(true,i) + " |   ");
				} else
					pw.print(conIntToStr(false,i) + " |   ");
				
				multipl = 1;
				for (int t = 0; t < invariants.get(i).size(); t++) { //po wszystkich tranzycja inwariantu
					int tr = invariants.get(i).get(t); // nr tranzycji
					if (transNo >= 100)
						pw.print(conIntToStr(true, tr)); //tutaj wstawiamy wartość dla tranz. w inw.
					else
						pw.print(conIntToStr(false, tr)); //tutaj wstawiamy wartość dla tranz. w inw.
					
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
	private String conIntToStr(boolean large, int tr) {
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
	public String getNazwaPliku(String sciezka) {
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
	 * Czyta plik PNT w formacie INA z siecia standardowa
	 * @param scieżka String - scieżka do pliku
	 */
	public void readPNT(String sciezka) {
		try {
			int SID = GUIManager.getDefaultGUIManager().getWorkspace().getProject().checkSheetID();	
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
						/*
						  bylo: 
						  if (WczytanyString[j].isEmpty()) {

						} else {
						
						 */
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
						nodeArray.add(new Place(placeNumber, new ArrayList<ElementLocation>(), 
								placeName, "", wMark[placeNumber]));
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
						nodeArray.add(new Transition(transNumber,
							new ArrayList<ElementLocation>(), transName, ""));
						//mark++;
					}
					break;
				case 4:
					// Tworzenie Arców, szerokosci okna
					// tworzenie dla kazdego noda element location
					for (int j = 0; j < nodeArray.size(); j++) {
						if (nodeArray.get(j).getType() == PetriNetElementType.PLACE) {
							elemArray.add(new ElementLocation(SID, new Point(
									80, 30 + j * 60), nodeArray.get(j)));
							ArrayList<ElementLocation> tempElementLocationArry = new ArrayList<ElementLocation>();
							tempElementLocationArry.add(elemArray.get(j));
							nodeArray.get(j).setNodeLocations(
									tempElementLocationArry);
						}

						if (nodeArray.get(j).getType() == PetriNetElementType.TRANSITION) {
							elemArray.add(new ElementLocation(SID, new Point(
									280, 30 + (j - placeCount) * 60), nodeArray.get(j)));
							ArrayList<ElementLocation> tempElementLocationArry = new ArrayList<ElementLocation>();
							tempElementLocationArry.add(elemArray.get(j));
							nodeArray.get(j).setNodeLocations(
									tempElementLocationArry);
						}
					}

					int pozycja_a = 0;
					// Arki
					for (int k = 0; k < placeArcListPre.size(); k++) {
						for (int j = 0; j < placeArcListPre.get(k).length; j++) {
							int t1 = trans[Integer.parseInt(placeArcListPre.get(k)[j])][1];
							arcArray.add(new Arc(nodeArray.get(t1 - 1).getLastLocation(), 
									nodeArray.get(k).getLastLocation(), "", 
									placeArcListPreWeight.get(0).get(pozycja_a)));
							pozycja_a++;
						}
					}
					pozycja_a = 0;
					for (int k = 0; k < placeArcListPost.size(); k++) {
						for (int j = 0; j < placeArcListPost.get(k).length; j++) {
							int t2 = trans[Integer.parseInt(placeArcListPost.get(k)[j])][1];
							arcArray.add(new Arc(nodeArray.get(k).getLastLocation(), 
									nodeArray.get(t2 - 1).getLastLocation(), "",
									placeArcListPostWeight.get(0).get(pozycja_a)));
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
	 * Metoda służąca do zapisywaniu pliku sieci Petriego w formacie programu INA.
	 * @param path - ścieżka zapisu pliku
	 * @param placeList ArrayList[Place] - lista miejsc sieci
	 * @param transitionList ArrayList[Transition] - lista tranzycji sieci
	 * @param arcList ArrayList[Arc] - lista łuków sieci
	 */
	public void writePNT(String path, ArrayList<Place> placeList,
			ArrayList<Transition> transitionList, ArrayList<Arc> arcList) {
		String zawartoscPliku = "P   M   PRE,POST  NETZ 0:";
		try {
			PrintWriter zapis = new PrintWriter(path);
			zawartoscPliku += getNazwaPliku(path);
			zawartoscPliku += "\r\n";
			//int[] tabPlace = new int[placeList.size()];

			for (int i = 0; i < placeList.size(); i++) {
				if (i < 9) {
					zawartoscPliku += " ";
				}
				if (i < 99) {
					zawartoscPliku += " ";
				}
				zawartoscPliku += i;
				zawartoscPliku += " ";
				zawartoscPliku += placeList.get(i).getTokensNumber();
				zawartoscPliku += "    ";

				// łuki
				if (placeList.get(i).getInArcs().isEmpty()
						&& placeList.get(i).getOutArcs().isEmpty()) {
					zawartoscPliku += " ";
				}
				if (placeList.get(i).getInArcs().size() > 0
						&& placeList.get(i).getOutArcs().isEmpty()) {
					for (int j = 0; j < placeList.get(i).getInArcs().size(); j++) {
						zawartoscPliku += " ";
						zawartoscPliku += transitionList.indexOf(placeList
								.get(i).getInArcs().get(j).getStartNode());
						if (placeList.get(i).getInArcs().get(j).getWeight() > 1) {
							zawartoscPliku += ": "
									+ placeList.get(i).getInArcs().get(j)
											.getWeight();
						}
					}
				}
				if (placeList.get(i).getInArcs().size() > 0
						&& placeList.get(i).getOutArcs().size() > 0) {
					for (int j = 0; j < placeList.get(i).getInArcs().size(); j++) {
						zawartoscPliku += " ";
						zawartoscPliku += transitionList.indexOf(placeList
								.get(i).getInArcs().get(j).getStartNode());
						if (placeList.get(i).getInArcs().get(j).getWeight() > 1) {
							zawartoscPliku += ": "
									+ placeList.get(i).getInArcs().get(j)
											.getWeight();
						}
					}
					zawartoscPliku += ",";
					for (int j = 0; j < placeList.get(i).getOutArcs().size(); j++) {
						zawartoscPliku += " ";
						zawartoscPliku += transitionList.indexOf(placeList
								.get(i).getOutArcs().get(j).getEndNode());
						if (placeList.get(i).getOutArcs().get(j).getWeight() > 1) {
							zawartoscPliku += ": "
									+ placeList.get(i).getOutArcs().get(j)
											.getWeight();
						}
					}
				}
				if (placeList.get(i).getInArcs().isEmpty()
						&& placeList.get(i).getOutArcs().size() > 0) {
					zawartoscPliku += ",";
					for (int j = 0; j < placeList.get(i).getOutArcs().size(); j++) {
						zawartoscPliku += " ";
						zawartoscPliku += transitionList.indexOf(placeList
								.get(i).getOutArcs().get(j).getEndNode());
						if (placeList.get(i).getOutArcs().get(j).getWeight() > 1) {
							zawartoscPliku += ": "
									+ placeList.get(i).getOutArcs().get(j)
											.getWeight();
						}
					}
				}
				// zawartoscPliku
				zawartoscPliku += "\r\n";
			}
			zawartoscPliku += "@\r\n";
			zawartoscPliku += "place nr.             name capacity time\r\n";

			for (int i = 0; i < placeList.size(); i++) {
				zawartoscPliku += "     ";
				if (i < 9) {
					zawartoscPliku += " ";
				}
				if (i < 99) {
					zawartoscPliku += " ";
				}
				zawartoscPliku += i;
				zawartoscPliku += ": ";
				zawartoscPliku += placeList.get(i).getName() + "                  ";
				zawartoscPliku += "65535    0";
				zawartoscPliku += "\r\n";
			}

			zawartoscPliku += "@\r\n";
			zawartoscPliku += "trans nr.             name priority time\r\n";
			for (int i = 0; i < transitionList.size(); i++) {
				zawartoscPliku += "     ";
				if (i <= 9) {
					zawartoscPliku += " ";
				}
				if (i <= 99) {
					zawartoscPliku += " ";
				}
				zawartoscPliku += i;
				zawartoscPliku += ": ";
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
				
				zawartoscPliku += transitionList.get(i).getName() + "                      ";
				zawartoscPliku += "0    0";
				zawartoscPliku += "\r\n";
			}
			zawartoscPliku += "@";
			zapis.println(zawartoscPliku);
			zapis.close();
			GUIManager.getDefaultGUIManager().log("Petri net exported as .pnt INA format. File: "+path, "text", true);
		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
			JOptionPane.showMessageDialog(null,e.getMessage(),"ERROR: writePNT",JOptionPane.ERROR_MESSAGE);
			GUIManager.getDefaultGUIManager().log("Error: " + e.getMessage(), "error", true);
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
					
					//if(!(t == invariants.size() - 1))
					//	pw.print(",");
					
					//pw.print("\r\n");
				}
				//pw.print("\r\n");
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

			for (int i = 0; i < invariants.size(); i++) { //po wszystkich inwariantach
				pw.print(i+1);
				for (int t = 0; t < invariants.get(i).size(); t++) { //po wszystkich tranzycjach
					int value = invariants.get(i).get(t);
					pw.print(";"+value);
				}
				pw.print("\r\n");
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
