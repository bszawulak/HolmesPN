/**
 * 
 */
package abyss.math.parser;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;

import abyss.darkgui.GUIManager;
import abyss.graphpanel.GraphPanel;
import abyss.math.Arc;
import abyss.math.ElementLocation;
import abyss.math.Node;
import abyss.math.Place;
import abyss.math.Transition;
import abyss.math.PetriNetElement.PetriNetElementType;

/**
 * @author Rince
 *
 */
public class INAprotocols {
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
	 * @return invariantsList
	 */
	public ArrayList<ArrayList<Integer>> getInvariantsList() {
		return invariantsList;
	}  

	/**
	 * Wczytywanie pliki t-inwariantow INA, wczesniej: INAinvariants.read
	 * @param sciezka
	 */
	public void readINV(String sciezka) {
		try {
			DataInputStream in = new DataInputStream(new FileInputStream(sciezka));
			@SuppressWarnings("resource")
			BufferedReader buffer = new BufferedReader(new InputStreamReader(in));
			String wczytanaLinia = buffer.readLine();
			// invariantow dla miejsc nie ma, bo nie mam na czy sie wzorowac, a INA
			// mnie nie slucha (student)	
			// brzydka, niedobra INA... (MR)
			
			if (wczytanaLinia
					.contains("transition sub/sur/invariants for net 0.t               :")) {
			}
			buffer.readLine();
			if (wczytanaLinia.contains("semipositive transition invariants =")) {
			}
			buffer.readLine();
			//System.out.println("Etap I");
			// Etap I - ilosc tranzycji/miejsc
			while (!(wczytanaLinia = buffer.readLine()).endsWith("~~~~~~~~~~~~~~~~~~~~~~~~")) {
				if(wczytanaLinia.endsWith("~~~~~~~~~~~~~~~~~~~~~~~~")){break;}
				String[] sformatowanaLinia = wczytanaLinia.split(" ");
				//System.out.println(wczytanaLinia);
				for (int j = 0; j < sformatowanaLinia.length; j++) {
					if ((sformatowanaLinia[j].isEmpty())
							|| sformatowanaLinia[j].contains("Nr.")) {
					} else {
						nodesList.add(Integer.parseInt(sformatowanaLinia[j]));
					}
				}
			}
			// Etap II - lista T/P - invariantow
			ArrayList<Integer> tmpInvariant = new ArrayList<Integer>();
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
		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
		}
	}

	/**
	 * Zapis inwariantow w formacie INA
	 * @param path
	 * @param invariants
	 * @param transitions
	 */
	public void writeINV(String path, ArrayList<ArrayList<Integer>> invariants,
			ArrayList<Transition> transitions) {
		String buffor = "transition sub/sur/invariants for net 0.";
		try {
			String extension = "";
			if(!path.contains(".inv"))
				extension = ".inv";
			PrintWriter pw = new PrintWriter(path + extension);

			buffor += getNazwaPliku(path);
			buffor += "\r\n";
			buffor += "\r\n";
			// Dod pokrycie
			buffor += "semipositive transition invariants =\r\n";
			buffor += "\r\n";
			buffor += "Nr.      ";

			//int[] tabTransitions = new int[transitions.size()];
			for (int i = 0; i < transitions.size(); i++) {
				
				if (i <= 9)
					buffor += " ";
				if (i <= 99)
					buffor += " ";
				if (i <= 999)
					buffor += " ";
				buffor += i;

				if (i == 16) {
					buffor += "\r\n";
					buffor += "        ";
				}
			}
			buffor += "\r\n";
			// if(transitions.size()>=17)
			// {
			// buffor +=
			// "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~";

			// }else
			buffor += "~~~~~~~~~";
			for (int t = 0; t < transitions.size(); t++)
				buffor += "~~~~";
			buffor += "\r\n";

			for (int i = 0; i < invariants.size(); i++) {
				if (i <= 9)
					buffor += " ";
				if (i <= 99)
					buffor += " ";
				if (i <= 999)
					buffor += " ";
				buffor += i;
				buffor += " |   ";

				for (int t = 0; t < invariants.get(i).size(); t++) {
					int tr = invariants.get(i).get(t);
					if (tr <= 9)
						buffor += " ";
					if (tr <= 99)
						buffor += " ";
					if (tr <= 999)
						buffor += " ";
					buffor += tr;
					if (t == 16 && invariants.size() > 16) {
						buffor += "\r\n";
						buffor += "     |   ";
					}
				}
				buffor += "\r\n";

			}

			//
			buffor += "\r\n";
			buffor += "@";
			//System.out.println(buffor);
			
			pw.println(buffor);
			pw.close();
			
		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());

		}
	}

	public String getNazwaPliku(String sciezka) {
		String[] tablica = sciezka.split("\\\\");
		return tablica[tablica.length - 1];
	}
	
	/**
	 * Zwraca liste wezlow sieci po tym jak readPNT przeczyta plik INY
	 * @return nodeArray
	 */
	public ArrayList<Node> getNodeArray() {
		return nodeArray;
	}

	/**
	 * Zwraca liste krawedzi sieci po tym jak readPNT przeczyta plik INY
	 * @return arcArray
	 */
	public ArrayList<Arc> getArcArray() {
		return arcArray;
	}

	/**
	 * Czyta plik PNT w formacie INA z siecia standardowa
	 * @param sciezka
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
			// ArrayList<ArrayList<String>> wWej = new
			// ArrayList<ArrayList<String>>();
			// ArrayList<ArrayList<String>> wWyj = new
			// ArrayList<ArrayList<String>>();

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
									wMark[ID] = Integer
											.parseInt(WczytanyString[j]);
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
											wagiWej.add(Integer
													.parseInt(WczytanyString[j]));
										} else {
											wagiWyj.remove(wagiWyj.size() - 1);
											wagiWyj.add(Integer
													.parseInt(WczytanyString[j]));
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

					if ((wczytanaLinia.contains("capacity")
							&& wczytanaLinia.contains("time") && wczytanaLinia
								.contains("name")) || wczytanaLinia.equals("@")) {
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
					if ((wczytanaLinia.contains("priority")
							&& wczytanaLinia.contains("time") && wczytanaLinia
								.contains("name")) || wczytanaLinia.equals("@")) {
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

					// Tworzenie Arc�w, szerokosci okna
					
					// tworzenie dla ka�dego noda element location
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
									280, 30 + (j - placeCount) * 60), nodeArray
									.get(j)));
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

							int t1 = trans[Integer.parseInt(placeArcListPre
									.get(k)[j])][1];

							arcArray.add(new Arc(nodeArray.get(t1 - 1).getLastLocation(), nodeArray.get(k).getLastLocation(), "",placeArcListPreWeight.get(0).get(pozycja_a)));
							pozycja_a++;
						}
					}
					pozycja_a = 0;
					for (int k = 0; k < placeArcListPost.size(); k++) {
						for (int j = 0; j < placeArcListPost.get(k).length; j++) {

							int t2 = trans[Integer.parseInt(placeArcListPost
									.get(k)[j])][1];
							arcArray.add(new Arc(nodeArray.get(k)
									.getLastLocation(), nodeArray.get(t2 - 1)
									.getLastLocation(), "",
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
							.getWorkspace().getSheets().get(SIN)
							.getGraphPanel();
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
								.getPosition().x + 90,
								graphPanel.getSize().height));
					}
					if (yFound == true && xFound == false) {
						graphPanel.setSize(new Dimension(
								graphPanel.getSize().width, elemArray.get(tmpY)
										.getPosition().y + 90));
					}
					if (xFound == true && yFound == true) {
						graphPanel.setSize(new Dimension(elemArray.get(tmpX)
								.getPosition().x + 90, elemArray.get(tmpY)
								.getPosition().y + 90));
					}
					break;
				}
			}
			in.close();
		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
		}

	}

	//INAwriter:
	public void write(String sciezka, ArrayList<Place> placeList,
			ArrayList<Transition> transitionList, ArrayList<Arc> arcList) {
		String zawartoscPliku = "P   M   PRE,POST  NETZ 0:";
		try {
			PrintWriter zapis = new PrintWriter(sciezka + ".pnt");

			
			zawartoscPliku += getNazwaPliku(sciezka);
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

				// Arcki
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
				String tmpNazwy = "";
				int wielkoscNazwy = 0;
				String przerwa = "";
				zawartoscPliku += "     ";
				if (i < 9) {
					zawartoscPliku += " ";
				}
				if (i < 99) {
					zawartoscPliku += " ";
				}
				zawartoscPliku += i;
				zawartoscPliku += ": ";
				if (placeList.get(i).getName().length() > 16) {
					tmpNazwy = placeList.get(i).getName();

					tmpNazwy = tmpNazwy.substring(0, 16);

				} else {
					tmpNazwy = placeList.get(i).getName();
					wielkoscNazwy = tmpNazwy.length();
					for (int k = wielkoscNazwy; k < 16; k++) {
						przerwa += " ";
					}
				}
				zawartoscPliku += tmpNazwy + przerwa;
				zawartoscPliku += "       oo    0";
				zawartoscPliku += "\r\n";
			}

			zawartoscPliku += "@\r\n";
			zawartoscPliku += "trans nr.             name priority time\r\n";
			for (int i = 0; i < transitionList.size(); i++) {
				String tmpNazwy = "";
				int wielkoscNazwy = 0;
				String przerwa = "";
				zawartoscPliku += "     ";
				if (i <= 9) {
					zawartoscPliku += " ";
				}
				if (i <= 99) {
					zawartoscPliku += " ";
				}
				zawartoscPliku += i;
				zawartoscPliku += ": ";
				if (transitionList.get(i).getName().length() > 16) {
					tmpNazwy = transitionList.get(i).getName();
					
					tmpNazwy = tmpNazwy.substring(0, 16);
					
				} else {
					tmpNazwy = transitionList.get(i).getName();
					wielkoscNazwy = tmpNazwy.length();
					for (int k = wielkoscNazwy; k < 16; k++) {
						przerwa += " ";
					}
				}
				zawartoscPliku += tmpNazwy + przerwa;
				zawartoscPliku += "        0    0";
				zawartoscPliku += "\r\n";
			}
			zawartoscPliku += "@";
			zapis.println(zawartoscPliku);
			zapis.close();

		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
		}

	}
	

}
