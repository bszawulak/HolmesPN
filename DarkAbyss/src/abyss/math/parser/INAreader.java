package abyss.math.parser;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import abyss.darkgui.GUIManager;
import abyss.graphpanel.GraphPanel;
import abyss.math.Arc;
import abyss.math.ElementLocation;
import abyss.math.Node;
import abyss.math.PetriNetElement.PetriNetElementType;
import abyss.math.Place;
import abyss.math.Transition;

/*
 * Wyglada na to, ze klasa nie jest juz uzywana!
 * Uzywac INAreader2 !
 * 
 */
public class INAreader {

	private ArrayList<Node> nodeArray = new ArrayList<Node>();
	private ArrayList<Arc> arcArray = new ArrayList<Arc>();
	private ArrayList<ElementLocation> elemArray = new ArrayList<ElementLocation>();
	@SuppressWarnings("unused")
	private String netName = "defaultINA";
	private int globalPlaceNumber = 0; // kolejny ID miejsca (inkrementacja przy czytaniu)
	private int etap = 1;
	private int placeCount = 0;
	private ArrayList<String[]> placeArcListPost = new ArrayList<String[]>();
	private ArrayList<ArrayList<Integer>> placeArcListPostWeight = new ArrayList<ArrayList<Integer>>();
	private ArrayList<String[]> placeArcListPre = new ArrayList<String[]>();
	private ArrayList<ArrayList<Integer>> placeArcListPreWeight = new ArrayList<ArrayList<Integer>>();

	public ArrayList<Node> getNodeArray() {
		return nodeArray;
	}

	public ArrayList<Arc> getArcArray() {
		return arcArray;
	}

	public void read(String sciezka) {
		try {

			DataInputStream in = new DataInputStream(new FileInputStream(
					sciezka));
			BufferedReader buffer = new BufferedReader(
					new InputStreamReader(in));
			String wczytanaLinia = buffer.readLine();
			String[] tabWczytanaLinia = wczytanaLinia.split(":");
			netName = tabWczytanaLinia[1];
			int marking[] = new int[999];
			int trans[][] = new int[999][2];
			int mark = 0;

			while ((wczytanaLinia = buffer.readLine()) != null) {
				// Etap I
				// Wczytywanie informacji o Arcach i tokenach
				if (wczytanaLinia.equals("@")) {
					etap++;
				}
				switch (etap) {
				case 1:
					String[] stringDoWarunku = wczytanaLinia.split("");
					// System.out.println(stringDoWarunku[10]);
					if (stringDoWarunku[10].equals(",")) {

						tabWczytanaLinia = wczytanaLinia.split("    , ");
						String tmp1 = tabWczytanaLinia[0];
						String[] t1Array = tmp1.split(" ");
						ArrayList<String> t1List = new ArrayList<String>();
						for (int i = 0; i < t1Array.length; i++) {
							if (t1Array[i].equals("")) {
							} else {
								t1List.add(t1Array[i]);
							}
						}
						marking[mark] = Integer.parseInt(t1List.get(1));
						mark++;
						tmp1 = tabWczytanaLinia[1];
						t1Array = tmp1.split(" ");
						/////////////
						int redukcja = 0;
						for (int k = 0; k < t1Array.length; k++) {
							if (t1Array[k].endsWith(":")) {
								redukcja++;
							}
						}

						String[] forArcListPost = new String[t1Array.length
								- redukcja];
						int red=0;
						ArrayList<Integer> weightArryPost = new ArrayList<Integer>();
						for (int k = 0; k < t1Array.length; k++) {
							if (t1Array[k].endsWith(":")) {
								String[] bezDK = t1Array[k].split(":");
								forArcListPost[k-red] = bezDK[0];
								weightArryPost.add(Integer
										.parseInt(t1Array[k + 1]));
								k++;
								red++;
							} else {
								weightArryPost.add(1);
								forArcListPost[k-red] = t1Array[k];
							}
						}

						placeArcListPostWeight.add(weightArryPost);
						placeArcListPost.add(forArcListPost);
						// ////////////
						String[] pusta = {};
						placeArcListPreWeight.add(new ArrayList<Integer>());
						placeArcListPre.add(pusta);
					} else {
						boolean brakNastepnikow = true;
						for (int i = 0; i < stringDoWarunku.length; i++) {
							if (stringDoWarunku[i].equals(",")) {
								brakNastepnikow = false;
							}
						}
						tabWczytanaLinia = wczytanaLinia.split("     ");
						String tmp1 = tabWczytanaLinia[0];
						String[] t1Array = tmp1.split(" ");
						ArrayList<String> t1List = new ArrayList<String>();
						for (int i = 0; i < t1Array.length; i++) {
							if (t1Array[i].equals("")) {
							} else {
								t1List.add(t1Array[i]);
							}
						}
						marking[mark] = Integer.parseInt(t1List.get(1));
						mark++;

						if (brakNastepnikow) {
							String tmp2 = tabWczytanaLinia[1];
							t1Array = tmp2.split(" ");
							////
							int redukcja = 0;
							for (int k = 0; k < t1Array.length; k++) {
								if (t1Array[k].endsWith(":")) {
									redukcja++;
								}
							}

							String[] forArcListPre = new String[t1Array.length
									- redukcja];
							int red=0;
							ArrayList<Integer> weightArryPre = new ArrayList<Integer>();
							for (int k = 0; k < t1Array.length; k++) {
								if (t1Array[k].endsWith(":")) {
									String[] bezDK = t1Array[k].split(":");
									forArcListPre[k-red] = bezDK[0];
									weightArryPre.add(Integer
											.parseInt(t1Array[k + 1]));
									k++;
									red++;
								} else {
									weightArryPre.add(1);
									forArcListPre[k-red] = t1Array[k];
								}
							}

							placeArcListPreWeight.add(weightArryPre);
							placeArcListPre.add(forArcListPre);
							// //
							String[] pusta = {};
							placeArcListPost.add(pusta);
							placeArcListPostWeight.add(new ArrayList<Integer>());
						} else {
							String tmp2 = tabWczytanaLinia[1];
							String[] t2Array = tmp2.split(", ");
							tmp1 = t2Array[0];
							t1Array = tmp1.split(" ");
							int redukcja = 0;
							

							for (int k = 0; k < t1Array.length; k++) {
								if (t1Array[k].endsWith(":")) {									
									redukcja++;
								}
							}

							String[] forArcListPre = new String[t1Array.length
									- redukcja];
							int red = 0;
							ArrayList<Integer> weightArryPre = new ArrayList<Integer>();
							for (int k = 0; k < t1Array.length; k++) {
								// System.out.println(t1Array[k]);
								if (t1Array[k].endsWith(":")) {
									String[] bezDK = t1Array[k].split(":");
									forArcListPre[k-red] = bezDK[0];
									weightArryPre.add(Integer
											.parseInt(t1Array[k + 1]));
									k++;
									red++;
								} else {
									weightArryPre.add(1);
									forArcListPre[k-red] = t1Array[k];
								}
							}
							placeArcListPreWeight.add(weightArryPre);
							placeArcListPre.add(forArcListPre);
							tmp1 = t2Array[1];
							t1Array = tmp1.split(" ");

							//////
							redukcja = 0;
							for (int k = 0; k < t1Array.length; k++) {
								if (t1Array[k].endsWith(":")) {
									redukcja++;
								}
							}
							
							String[] forArcListPost = new String[t1Array.length
									- redukcja];
							red=0;
							ArrayList<Integer> weightArryPost = new ArrayList<Integer>();
							for (int k = 0; k < t1Array.length; k++) {
								if (t1Array[k].endsWith(":")) {
									String[] bezDK = t1Array[k].split(":");
									forArcListPost[k-red] = bezDK[0];
									weightArryPost.add(Integer
											.parseInt(t1Array[k + 1]));
									k++;
									red++;
								} else {
									weightArryPost.add(1);
									forArcListPost[k-red] = t1Array[k];
								}
							}

							placeArcListPostWeight.add(weightArryPost);
							placeArcListPost.add(forArcListPost);
							// ////
							// placeArcListPost.add(t1Array);

						}
					}
					break;
				case 2:
					// Etap II
					//System.out.println("Etap II");
					// Wczytywanie danych o miejscach
					if (wczytanaLinia.endsWith("name capacity time") || wczytanaLinia.equals("@")) {
					} else {

						tabWczytanaLinia = wczytanaLinia.split(": ");
						//String[] tmp4 = tabWczytanaLinia[0].split(" ");
						int placeNumber = globalPlaceNumber;
						globalPlaceNumber++;
						tabWczytanaLinia = tabWczytanaLinia[1].split(" ");
						String placeName = tabWczytanaLinia[0];
						nodeArray.add(new Place(placeNumber,
							new ArrayList<ElementLocation>(), placeName, "", marking[placeNumber]));
					}
					break;
				case 3:
					// Etap III
					// Wczytywanie danych o tranzycjach
					if (wczytanaLinia.endsWith("name priority time")
							|| wczytanaLinia.equals("@")) {
						placeCount = globalPlaceNumber;
					} else {
						tabWczytanaLinia = wczytanaLinia.split(": ");
						String[] tmp5 = tabWczytanaLinia[0].split(" ");
						for (int i = 0; i < tmp5.length; i++) {
							if (tmp5[i].equals("")) {
							} else {
								globalPlaceNumber++;
								trans[Integer.parseInt(tmp5[i])][0] = Integer
										.parseInt(tmp5[i]);
								trans[Integer.parseInt(tmp5[i])][1] = globalPlaceNumber;
							}
						}

						int transNumber = mark;
						tabWczytanaLinia = tabWczytanaLinia[1].split(" ");
						String transName = tabWczytanaLinia[0];
						nodeArray.add(new Transition(transNumber,
							new ArrayList<ElementLocation>(), transName, ""));
						mark++;
					}
					break;
				case 4:
					// Tworzenie Arc�w, szerokosci okna
					int SID = GUIManager.getDefaultGUIManager().getWorkspace()
							.newTab();
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

					// Arki
					for (int k = 0; k < placeArcListPre.size(); k++) {
						for (int j = 0; j < placeArcListPre.get(k).length; j++) {

							int t1 = trans[Integer.parseInt(placeArcListPre
									.get(k)[j])][1];

//							arcArray.add(new Arc(nodeArray.get(t1 - 1)
//									.getLastLocation(), nodeArray.get(k)
//									.getLastLocation()));
							arcArray.add(new Arc(nodeArray.get(t1 - 1)
									.getLastLocation(), nodeArray.get(k)
									.getLastLocation(), "", placeArcListPreWeight.get(k).get(j)));
						}
					}
					for (int k = 0; k < placeArcListPost.size(); k++) {
						for (int j = 0; j < placeArcListPost.get(k).length; j++) {
							int t2 = trans[Integer.parseInt(placeArcListPost
									.get(k)[j])][1];
//							arcArray.add(new Arc(nodeArray.get(k)
//									.getLastLocation(), nodeArray.get(t2 - 1)
//									.getLastLocation()));
							arcArray.add(new Arc(nodeArray.get(k)
									.getLastLocation(), nodeArray.get(t2 - 1)
									.getLastLocation(), "",placeArcListPostWeight.get(k).get(j)));
							//placeArcListPostWeight.get(k).get(j);
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
}
