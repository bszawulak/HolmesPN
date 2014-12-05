package abyss.wasteland;

import java.io.PrintWriter;
import java.util.ArrayList;

import abyss.math.Arc;
import abyss.math.Place;
import abyss.math.Transition;

/**
 * Nieuzywana, zawartosc przeniesiona do INAprotocols
 * @author students
 *
 */
public class INAwriter {

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
	
	public String getNazwaPliku(String sciezka){
		
		String[] tablica = sciezka.split("\\\\");
		return tablica[tablica.length-1];
	}
}
