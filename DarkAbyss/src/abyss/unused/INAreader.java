package abyss.unused;

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

/**
 * Nieuzywana, zawartosc przeniesiona do INAprotocols
 * @author students
 *
 */
public class INAreader {
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

	public ArrayList<Node> getNodeArray() {
		return nodeArray;
	}

	public ArrayList<Arc> getArcArray() {
		return arcArray;
	}

	public void read(String sciezka) {
		try {
			int SID = GUIManager.getDefaultGUIManager().getWorkspace().getProject().returnCleanSheetID();

			DataInputStream in = new DataInputStream(new FileInputStream(sciezka));
			BufferedReader buffer = new BufferedReader(new InputStreamReader(in));
			String wczytanaLinia = buffer.readLine();
			String[] tabWczytanaLinia = wczytanaLinia.split(":");
			netName = tabWczytanaLinia[1];
			// int marking[] = new int[MatSiz];
			int trans[][] = new int[MatSiz][2];
			//int mark = 0;
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
						graphPanel.setSize(new Dimension(elemArray.get(tmpX).getPosition().x + 90,
								graphPanel.getSize().height));
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
		} catch (Exception e) {
			GUIManager.getDefaultGUIManager().log(e.getMessage(), "error", true);
		}
	}
}
