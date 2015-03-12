package abyss.analyzer;

import java.util.ArrayList;
import java.util.HashMap;

import abyss.darkgui.GUIManager;
import abyss.math.Arc;
import abyss.math.ElementLocation;
import abyss.math.PetriNetElement.PetriNetElementType;
import abyss.math.PetriNet;
import abyss.math.Place;
import abyss.math.Transition;

/**
 * Metoda stara się liczyć inwarianty. Cosik nie wyszło do końca...<br>
 * 11.03.2015: No więc czas ją naprawić. MR
 * 
 * @author Bartłomiej Szawulak
 * @author MR
 */
public class InvariantsAnalyzer implements Runnable {
	private ArrayList<Arc> arcs = new ArrayList<Arc>();
	private ArrayList<Place> places = new ArrayList<Place>();
	private ArrayList<Transition> transitions = new ArrayList<Transition>();
	private ArrayList<ArrayList<Integer>> invariantsList = new ArrayList<ArrayList<Integer>>();

	private ArrayList<ArrayList<Integer>> globalIncidenceMatrix;
	private ArrayList<ArrayList<Integer>> globalIdentityMatrix;
	@SuppressWarnings("unused")
	private int glebokosc = 0;
	private boolean znalazl = false;
	//private NetPropertiesAnalyzer NPA = new NetPropertiesAnalyzer();
	
	/**
	 * Konstruktor obiektu klasy InvariantsAnalyzer. Zapewnia dostęp do miejsc, tranzycji i łuków sieci.
	 */
	public InvariantsAnalyzer() {
		places = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getPlaces();
		transitions = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions();
		arcs = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getArcs();
	}
	
	/**
	 * Metoda wirtualna - nadpisana, odpowiada za działanie w niezależnym wątku
	 */
	public void run() {
		this.createTPIncidenceAndIdentityMatrix();
		this.searchTInvariants();
		PetriNet project = GUIManager.getDefaultGUIManager().getWorkspace().getProject();
		GUIManager.getDefaultGUIManager().getInvariantsBox().showInvariants(project.getEIAgeneratedInv_old());
		project.setInvariantsMatrix(invariantsList);
	}
	
	private void log(String msg, String type) {
		GUIManager.getDefaultGUIManager().log("InvModule: "+msg, type, true);
	}

	/**
	 * Metoda tworząca macierze: incydencji i jednostkową dla modelu szukania T-inwariantów
	 * (TP-macierz z literatury)
	 */
	public void createTPIncidenceAndIdentityMatrix() {
		//hashmapy do ustalania lokalizacji miejsca/tramzycji. Równie dobrze 
		//działałoby (niżej, gdy są używane): np. places.indexOf(...)
		HashMap<Place, Integer> placesMap = new HashMap<Place, Integer>();
		HashMap<Transition, Integer> transitionsMap = new HashMap<Transition, Integer>();
		for (int i = 0; i < places.size(); i++)
			placesMap.put(places.get(i), i);
		for (int i = 0; i < transitions.size(); i++)
			transitionsMap.put(transitions.get(i), i);
		
		globalIncidenceMatrix = new ArrayList<ArrayList<Integer>>();
		globalIdentityMatrix = new ArrayList<ArrayList<Integer>>();

		//tworzenie macierzy TP - precyzyjnie do obliczeń T-inwariantów
		for (int i = 0; i < transitions.size(); i++) {
			ArrayList<Integer> transRow = new ArrayList<Integer>();
			for (int j = 0; j < places.size(); j++) {
				transRow.add(0);
			}
			globalIncidenceMatrix.add(transRow);
		}
		//wypełnianie macierzy incydencji
		for (Arc oneArc : arcs) {
			int tPosition = 0;
			int pPosition = 0;
			int incidanceValue = 0;

			if (oneArc.getStartNode().getType() == PetriNetElementType.TRANSITION
					|| oneArc.getStartNode().getType() == PetriNetElementType.TIMETRANSITION) {
				tPosition = transitionsMap.get(oneArc.getStartNode());
				pPosition = placesMap.get(oneArc.getEndNode());
				incidanceValue = -1 * oneArc.getWeight();
			} else {
				tPosition = transitionsMap.get(oneArc.getEndNode());
				pPosition = placesMap.get(oneArc.getStartNode());
				incidanceValue = 1 * oneArc.getWeight();
			}
			globalIncidenceMatrix.get(tPosition).set(pPosition, incidanceValue);
		}
		log("TP-class incidence matrix created for "+transitions.size()+" transitions and "+places.size()+" places.","text");
		
		//macierz jednostkowa
		for (int i = 0; i < transitions.size(); i++) {
			ArrayList<Integer> identRow = new ArrayList<Integer>();
			for (int j = 0; j < transitions.size(); j++) {
				if (i == j) identRow.add(1);
				else identRow.add(0);
			}
			globalIdentityMatrix.add(identRow);
		}
		
		log("Identity matrix created for "+transitions.size()+" transitions","text");
	}

	/**
	 * Główna metoda klasy odpowiedzialna za wyszukiwania T-inwariantów
	 */
	public void searchTInvariants() {
		ArrayList<ArrayList<Integer>> incMatrix = new ArrayList<ArrayList<Integer>>();
		ArrayList<ArrayList<Integer>> identMatrix = new ArrayList<ArrayList<Integer>>();
		ArrayList<ArrayList<ArrayList<Integer>>> incMatrixList = new ArrayList<ArrayList<ArrayList<Integer>>>();
		ArrayList<ArrayList<ArrayList<Integer>>> identMatrixList = new ArrayList<ArrayList<ArrayList<Integer>>>();

		incMatrixList.add(globalIncidenceMatrix); //macierz incydencji
		identMatrixList.add(globalIdentityMatrix);
		incMatrix = incMatrixList.get(incMatrixList.size() - 1);
		identMatrix = identMatrixList.get(identMatrixList.size() - 1);
		
		ArrayList<ArrayList<Integer>> rowsPhaseI = new ArrayList<ArrayList<Integer>>();

		System.out.println("------->Czysta nie naruszona siec<---------");
		System.out.println("------->Macierz incydencji<---------");
		for (int it = 0; it < incMatrix.size(); it++) {
			System.out.println();
			for (int jo = 0; jo < places.size(); jo++)
				System.out.print(incMatrix.get(it).get(jo) + " ");
		}
		System.out.println();
		System.out.println("------->Macierz TxT<---------");
		for (int it = 0; it < identMatrix.size(); it++) {
			System.out.println();
			for (int jo = 0; jo < transitions.size(); jo++)
				System.out.print(identMatrix.get(it).get(jo) + " ");
		}

		// Etap I z artykulu
		log("Phase I inititated. Performing only for all 1-in/1-out places.","text");
		for (int p = 0; p < places.size(); p++) {
			// wystepuje tylko jedno wejscie i wyjscie z miejsca
			if (checkEtapI(incMatrix, p)) {
				findAndCreateNewRowL(incMatrix, rowsPhaseI, p);
				sumRowsForIEtap(incMatrix, identMatrix, rowsPhaseI);

			}
			rowsPhaseI.clear();
			incMatrixList.clear();
			incMatrixList.add(incMatrix);
			identMatrixList.clear();
			identMatrixList.add(identMatrix);
		}

		System.out.println("------->EtapI - zakonczony<---------");
		System.out.println("------->Macierz incydencji<---------");
		for (int it = 0; it < incMatrix.size(); it++) {
			System.out.println();
			for (int jo = 0; jo < places.size(); jo++)
				System.out.print(incMatrix.get(it).get(jo) + " ");
		}
		System.out.println();
		System.out.println("------->Macierz TxT<---------");
		for (int it = 0; it < identMatrix.size(); it++) {
			System.out.println();
			for (int jo = 0; jo < transitions.size(); jo++)
				System.out.print(identMatrix.get(it).get(jo) + " ");
		}

		incMatrix = incMatrixList.get(incMatrixList.size() - 1);
		incMatrixList.remove(incMatrixList.size() - 1);
		identMatrix = identMatrixList.get(identMatrixList.size() - 1);
		identMatrixList.remove(identMatrixList.size() - 1);

		ArrayList<ArrayList<ArrayList<Integer>>> iLL = new ArrayList<ArrayList<ArrayList<Integer>>>();
		ArrayList<ArrayList<ArrayList<Integer>>> tLL = new ArrayList<ArrayList<ArrayList<Integer>>>();
		ArrayList<ArrayList<Integer>> newRowL = new ArrayList<ArrayList<Integer>>();

		// Etap II z artykulu

		ArrayList<ArrayList<Integer>> im = new ArrayList<ArrayList<Integer>>();

		for (int i = 0; i < incMatrix.size(); i++) {
			ArrayList<Integer> tmp = new ArrayList<Integer>();
			for (int j = 0; j < incMatrix.get(i).size(); j++)
				tmp.add(incMatrix.get(i).get(j));
			im.add(tmp);
		}

		ArrayList<ArrayList<Integer>> txt = new ArrayList<ArrayList<Integer>>();

		for (int i = 0; i < identMatrix.size(); i++) {
			ArrayList<Integer> tmp = new ArrayList<Integer>();
			for (int j = 0; j < identMatrix.get(i).size(); j++)
				tmp.add(identMatrix.get(i).get(j));
			txt.add(tmp);
		}

		etapII(im, txt, newRowL);
		setFoundedInvariantsL(im, txt);
		getMinimal(invariantsList);

		System.out.print("Koniec pracy analizatora");
	}

	@SuppressWarnings("unused")
	private int chceckInv(ArrayList<Integer> in1, ArrayList<Integer> in2) {
		int zgodnosc1 = 0;
		int zgodnosc2 = 0;
		int zgodnosc3 = 0;
		// czy in1 jest bardziej polak... minimalny niz nv2
		for (int i = 0; i < in1.size(); i++) {

			if (in1.get(i) > in2.get(i))
				zgodnosc1++;
			if (in1.get(i) < in2.get(i))
				zgodnosc2++;
			if (in1.get(i) == 0 && in2.get(i) > 0)
				zgodnosc3++;
		}

		if (zgodnosc1 > 0 && zgodnosc2 == 0)
			return 1;
		if (zgodnosc1 > 0 && zgodnosc2 > 0)
			return 2;
		else
			return 0;
	}

	private void etapII(ArrayList<ArrayList<Integer>> incidanceMatrixTMPL,
			ArrayList<ArrayList<Integer>> TxTMatrixTMPL,
			ArrayList<ArrayList<Integer>> newRowL) {
		for (int p = 0; p < places.size(); p++) {
			if (!znalazl) {

				if (checkEtapII(incidanceMatrixTMPL, p)) {

					ArrayList<ArrayList<Integer>> im = new ArrayList<ArrayList<Integer>>();

					for (int i = 0; i < incidanceMatrixTMPL.size(); i++) {
						ArrayList<Integer> tmp = new ArrayList<Integer>();
						for (int j = 0; j < incidanceMatrixTMPL.get(i).size(); j++)
							tmp.add(incidanceMatrixTMPL.get(i).get(j));
						im.add(tmp);
					}

					ArrayList<ArrayList<Integer>> txt = new ArrayList<ArrayList<Integer>>();

					for (int i = 0; i < TxTMatrixTMPL.size(); i++) {
						ArrayList<Integer> tmp = new ArrayList<Integer>();
						for (int j = 0; j < TxTMatrixTMPL.get(i).size(); j++)
							tmp.add(TxTMatrixTMPL.get(i).get(j));
						txt.add(tmp);
					}

					findAndCreateNewRowL(im, newRowL, p);

					if (newRowL.size() > 0) {
						addRows(im, txt, newRowL, true);
					}
					newRowL.clear();

					boolean pusty = true;
					pusty = checkIfHasFinishL(im, pusty);
					if (pusty) {
						setFoundedInvariantsL(im, txt);
						znalazl = true;
					} else {

						glebokosc++;

						if (p + 1 == places.size()) {

						} else {
							etapII(im, txt, newRowL);
						}
						glebokosc--;
					}

					setFoundedInvariantsL(im, txt);
				}
			}
		}
	}

	private void sumRowsForIEtap(ArrayList<ArrayList<Integer>> incidanceMatrixTMPL,
			ArrayList<ArrayList<Integer>> TxTMatrixTMPL,
			ArrayList<ArrayList<Integer>> newRowL) {
		if (newRowL.size() > 0) {
			ArrayList<ArrayList<Integer>> incidanceMatrixTML = new ArrayList<ArrayList<Integer>>();
			ArrayList<ArrayList<Integer>> TxTMatrixTML = new ArrayList<ArrayList<Integer>>();

			for (int l = 0; l < newRowL.size(); l++) {
				int tr = newRowL.get(l).get(0);
				int pl = newRowL.get(l).get(4);

				if (incidanceMatrixTMPL.get(tr).get(pl) < 0)
					addNewRowVerIL(incidanceMatrixTMPL, TxTMatrixTMPL, newRowL,
							incidanceMatrixTML, TxTMatrixTML, l);
				else
					addNewRowVerIIL(incidanceMatrixTMPL, TxTMatrixTMPL,
							newRowL, incidanceMatrixTML, TxTMatrixTML, l);

			}

			addOldRowsL(incidanceMatrixTMPL, TxTMatrixTMPL, newRowL, incidanceMatrixTML, TxTMatrixTML);

			incidanceMatrixTMPL.clear();
			for (int i = 0; i < incidanceMatrixTML.size(); i++) {
				ArrayList<Integer> tmp = new ArrayList<Integer>();
				for (int j = 0; j < incidanceMatrixTML.get(i).size(); j++)
					tmp.add(incidanceMatrixTML.get(i).get(j));
				incidanceMatrixTMPL.add(tmp);
			}
			// incidanceMatrixTMPL.add(incidanceMatrixTML.get(i));
			TxTMatrixTMPL.clear();
			for (int i = 0; i < TxTMatrixTML.size(); i++) {
				ArrayList<Integer> tmp = new ArrayList<Integer>();
				for (int j = 0; j < TxTMatrixTML.get(i).size(); j++)
					tmp.add(TxTMatrixTML.get(i).get(j));
				TxTMatrixTMPL.add(tmp);
			}
			// TxTMatrixTMPL.add(TxTMatrixTML.get(i));
		}
	}

	private void addRows(ArrayList<ArrayList<Integer>> incidanceMatrixTMPL,
			ArrayList<ArrayList<Integer>> TxTMatrixTMPL,
			ArrayList<ArrayList<Integer>> newRowL, boolean etap) {
		if (newRowL.size() > 0) {
			ArrayList<ArrayList<Integer>> incidanceMatrixTML = new ArrayList<ArrayList<Integer>>();
			ArrayList<ArrayList<Integer>> TxTMatrixTML = new ArrayList<ArrayList<Integer>>();

			for (int l = 0; l < newRowL.size(); l++) {

				if (incidanceMatrixTMPL.get(newRowL.get(l).get(0)).get(
						newRowL.get(l).get(4)) < 0)
					addNewRowVerIL(incidanceMatrixTMPL, TxTMatrixTMPL, newRowL,
							incidanceMatrixTML, TxTMatrixTML, l);
				else
					addNewRowVerIIL(incidanceMatrixTMPL, TxTMatrixTMPL,
							newRowL, incidanceMatrixTML, TxTMatrixTML, l);

			}

			addOldRowsL(incidanceMatrixTMPL, TxTMatrixTMPL, newRowL, incidanceMatrixTML, TxTMatrixTML);

			incidanceMatrixTMPL.clear();
			for (int i = 0; i < incidanceMatrixTML.size(); i++) {
				ArrayList<Integer> tmp = new ArrayList<Integer>();
				for (int j = 0; j < incidanceMatrixTML.get(i).size(); j++)
					tmp.add(incidanceMatrixTML.get(i).get(j));
				incidanceMatrixTMPL.add(tmp);
			}

			TxTMatrixTMPL.clear();
			for (int i = 0; i < TxTMatrixTML.size(); i++) {
				ArrayList<Integer> tmp = new ArrayList<Integer>();
				for (int j = 0; j < TxTMatrixTML.get(i).size(); j++)
					tmp.add(TxTMatrixTML.get(i).get(j));
				TxTMatrixTMPL.add(tmp);
			}
		}
	}

	private void setFoundedInvariantsL(ArrayList<ArrayList<Integer>> iL, ArrayList<ArrayList<Integer>> tL) {
		for (int it = 0; it < iL.size(); it++) {
			boolean zero = true;
			for (int jo = 0; jo < places.size(); jo++) {
				if (iL.get(it).get(jo) != 0)
					zero = false;
			}
			if (zero)
			{
				if (checkIfExist(invariantsList, tL.get(it)) == 0) {
					invariantsList.add(tL.get(it));
					System.out.println("!!!Wstawiam!!!");
				}
				/*
				if (checkIfExist(listaInvatianow, tL.get(it),x) == 2) {
					listaInvatianow.set(x, tL.get(it));
					System.out.println("!!!Wstawiam2!!!");
				}*/
				
			}
		}
	}
	
	private void getMinimal(ArrayList<ArrayList<Integer>> iL) {
		ArrayList<ArrayList<Integer>> minList = new ArrayList<ArrayList<Integer>>();
		
		for (int it = 0; it < iL.size(); it++) {
			boolean isSimp = true;
			for (int it2 = 0; it2 < iL.size(); it2++) {
				if(it!=it2)
				{
					boolean tmp = isSimpler(iL,it,it2);
					if(isSimp==false&&tmp==true)
					{
						
					}
					else
					{
						isSimp = tmp;
					}
				}				
			}
			
			if(isSimp)
				minList.add(iL.get(it));
		}
		invariantsList = minList;
	}
	
	@SuppressWarnings("unused")
	private boolean isSimpler(ArrayList<ArrayList<Integer>> iL, int it1, int it2)
	{
		int u = 0;
		int n = 0;
		int r = 0;
		
		for (int i = 0; i < iL.get(0).size(); i++)
		{
			if(iL.get(it1).get(i)>iL.get(it2).get(i))
				n++;
			if(iL.get(it1).get(i)<iL.get(it2).get(i))
				u++;
			if(iL.get(it1).get(i)==iL.get(it2).get(i))
				r++;
		}
		
		if(u==0&&n>0)
			return false;
		
		return true;
	}

	@SuppressWarnings("unused")
	private int checkIfExist(ArrayList<ArrayList<Integer>> LI,
			ArrayList<Integer> INV) {
		//Tu wsadzi minimalny
		
		int exist = 0;
		for (int i = 0; i < LI.size(); i++) {
			ArrayList<Integer> oldinv = LI.get(i);
			boolean check = true;
			boolean min = true;
			for (int j = 0; j < oldinv.size(); j++) {
				if (oldinv.get(j) != INV.get(j))
					check = false;
				//if (oldinv.get(j) > INV.get(j)&&INV.get(j)!=0)
				//	min = false;//spr
				//TODO
				
			}
			if (check)
				exist = 1;
		}

		return exist;
	}

	private boolean checkIfHasFinishL(ArrayList<ArrayList<Integer>> list,
			boolean pusty) {
		pusty = true;
		for (int t = 0; t < list.size(); t++)
			for (int p = 0; p < list.get(t).size(); p++)
				if (list.get(t).get(p) != 0)
					pusty = false;

		return pusty;
	}

	private void addOldRowsL(ArrayList<ArrayList<Integer>> incidanceMatrixTMP,
			ArrayList<ArrayList<Integer>> TxTMatrixTMP,
			ArrayList<ArrayList<Integer>> newRow,
			ArrayList<ArrayList<Integer>> incidanceMatrixTM,
			ArrayList<ArrayList<Integer>> TxTMatrixTM) {

		for (int i = 0; i < TxTMatrixTMP.size(); i++) {
			boolean kist = false;

			for (int k = 0; k < newRow.size(); k++)
				if ((i == newRow.get(k).get(0)) || i == newRow.get(k).get(1)) {
					kist = true;
				}

			if (kist == false) {
				ArrayList<Integer> NR = new ArrayList<Integer>();

				for (int b = 0; b < places.size(); b++) {
					NR.add(incidanceMatrixTMP.get(i).get(b));
				}
				incidanceMatrixTM.add(NR);
				NR = new ArrayList<Integer>();
				for (int b = 0; b < transitions.size(); b++) {
					NR.add(TxTMatrixTMP.get(i).get(b));
				}
				TxTMatrixTM.add(NR);
			}
		}
	}

	private boolean checkEtapI(ArrayList<ArrayList<Integer>> incidanceMatrixTMP, int place) {
		int input = 0;
		int output = 0;
		for (int t = 0; t < incidanceMatrixTMP.size(); t++) {
			if (incidanceMatrixTMP.get(t).get(place) > 0)
				input++;
			if (incidanceMatrixTMP.get(t).get(place) < 0)
				output++;
			
			if(input == 2 || output == 2)
				return false;
		}
		if (input == 1 && output == 1)
			return true;
		else
			return false;
	}

	private boolean checkEtapII(ArrayList<ArrayList<Integer>> incidanceMatrixTMP, int place) {
		int input = 0;
		int output = 0;
		for (int t = 0; t < incidanceMatrixTMP.size(); t++) {
			if (incidanceMatrixTMP.get(t).get(place) > 0)
				input++;
			if (incidanceMatrixTMP.get(t).get(place) < 0)
				output++;
		}
		if (input > 0 && output > 0)
			return true;
		else
			return false;
	}

	private void addNewRowVerIL(
			ArrayList<ArrayList<Integer>> incidanceMatrixTMP,
			ArrayList<ArrayList<Integer>> TxTMatrixTMP,
			ArrayList<ArrayList<Integer>> newRow,
			ArrayList<ArrayList<Integer>> incidanceMatrixTM,
			ArrayList<ArrayList<Integer>> TxTMatrixTM, int l) {

		ArrayList<Integer> NR = new ArrayList<Integer>();
		for (int b = 0; b < places.size(); b++)
			NR.add(-(incidanceMatrixTMP.get(newRow.get(l).get(0)).get(b) * newRow
					.get(l).get(2))
					+ (incidanceMatrixTMP.get(newRow.get(l).get(1)).get(b) * newRow
							.get(l).get(3)));

		incidanceMatrixTM.add(NR);
		NR = new ArrayList<Integer>();

		for (int b = 0; b < transitions.size(); b++)
			NR.add(bezwzgledna(-(TxTMatrixTMP.get(newRow.get(l).get(0)).get(b) * newRow
					.get(l).get(2))
					+ (TxTMatrixTMP.get(newRow.get(l).get(1)).get(b) * newRow
							.get(l).get(3))));

		TxTMatrixTM.add(NR);
	}

	private void addNewRowVerIIL(
			ArrayList<ArrayList<Integer>> incidanceMatrixTMP,
			ArrayList<ArrayList<Integer>> TxTMatrixTMP,
			ArrayList<ArrayList<Integer>> newRow,
			ArrayList<ArrayList<Integer>> incidanceMatrixTM,
			ArrayList<ArrayList<Integer>> TxTMatrixTM, int l) {

		ArrayList<Integer> NR = new ArrayList<Integer>();
		for (int b = 0; b < places.size(); b++)
			NR.add((incidanceMatrixTMP.get(newRow.get(l).get(0)).get(b) * newRow
					.get(l).get(2))
					- (incidanceMatrixTMP.get(newRow.get(l).get(1)).get(b) * newRow
							.get(l).get(3)));

		incidanceMatrixTM.add(NR);
		NR = new ArrayList<Integer>();

		for (int b = 0; b < transitions.size(); b++)
			NR.add(bezwzgledna((TxTMatrixTMP.get(newRow.get(l).get(0)).get(b) * newRow
					.get(l).get(2))
					- (TxTMatrixTMP.get(newRow.get(l).get(1)).get(b) * newRow
							.get(l).get(3))));

		TxTMatrixTM.add(NR);
	}

	private void findAndCreateNewRowL(ArrayList<ArrayList<Integer>> incidanceMatrixTMP, ArrayList<ArrayList<Integer>> newRow, int j) {
		int sizeM = incidanceMatrixTMP.size();
		for (int t1 = 0; t1 < sizeM; t1++) {
			int val1 = incidanceMatrixTMP.get(t1).get(j); // #
			//if (incidanceMatrixTMP.get(t1).get(j) != 0) {
			if (val1 != 0) { //#
				for (int t2 = t1; t2 < sizeM; t2++) {
					int val2 = incidanceMatrixTMP.get(t2).get(j); // #
					//if (incidanceMatrixTMP.get(t2).get(j) != 0)
					if (val2 != 0) {
						if (t2 != t1) {
							//if ((incidanceMatrixTMP.get(t1).get(j) > 0 && incidanceMatrixTMP.get(t2).get(j) < 0)
							//		|| (incidanceMatrixTMP.get(t1).get(j) < 0 && incidanceMatrixTMP.get(t2).get(j) > 0)) {
							//	int l1 = bezwzgledna(incidanceMatrixTMP.get(t1).get(j));
							//	int l2 = bezwzgledna(incidanceMatrixTMP.get(t2).get(j));
							if ((val1 > 0 && val2 < 0) || (val1 < 0 && val2 > 0)) {
								//int l1 = bezwzgledna(incidanceMatrixTMP.get(t1).get(j));
								//int l2 = bezwzgledna(incidanceMatrixTMP.get(t2).get(j));
								int l1 = bezwzgledna(val1);
								int l2 = bezwzgledna(val2);
								
								// ((x*y)/nwd(x,y))
								int nwd = (l1 * l2) / nwd(l1, l2);
								ArrayList<Integer> tab = new ArrayList<Integer>();
								tab.add(t1);
								tab.add(t2);
								tab.add(nwd / incidanceMatrixTMP.get(t1).get(j));
								tab.add(nwd / incidanceMatrixTMP.get(t2).get(j));
								tab.add(j);
								newRow.add(tab);
							}
						}
					}
				}
			}
		}
	}

	public void SetArcForAnalization(ArrayList<Arc> a) {
		arcs = a;

	}

	public static int nwd(int x, int y) {
		while (x != y) {
			if (x > y)
				x -= y;
			else
				y -= x;
		}
		return x;
	}

	public int silnia(int i) {
		if (i == 0)
			return 1;
		else
			return i * silnia(i - 1);
	}

	public int bezwzgledna(int i) {
		if (i < 0)
			return -i;
		else
			return i;
	}

	public ArrayList<ArrayList<Integer>> getListaInvatianow() {
		return invariantsList;
	}

	public void setListaInvatianow(ArrayList<ArrayList<Integer>> listaInvatianow) {
		this.invariantsList = listaInvatianow;
	}
}

/*

public class EarlyInvariantsAnalyzer implements Runnable {
	private ArrayList<Arc> arcs = new ArrayList<Arc>();
	private ArrayList<Place> places = new ArrayList<Place>();
	private ArrayList<Transition> transitions = new ArrayList<Transition>();

	private HashMap<Place, Integer> placesMap = new HashMap<Place, Integer>();
	private HashMap<Transition, Integer> transitionsMap = new HashMap<Transition, Integer>();
	private ArrayList<ArrayList<Integer>> invariantsList = new ArrayList<ArrayList<Integer>>();

	private ArrayList<ArrayList<Integer>> incidanceMatrixL;
	private ArrayList<ArrayList<Integer>> TxTMatrixL;
	@SuppressWarnings("unused")
	private int glebokosc = 0;
	private boolean znalazl = false;
	//private NetPropertiesAnalyzer NPA = new NetPropertiesAnalyzer();

	@Override
	public void run() {
		this.CreateIncidanceMatrixAndTxTMatrix();
		this.Analyze();
		PetriNet project = GUIManager.getDefaultGUIManager().getWorkspace().getProject();
		GUIManager.getDefaultGUIManager().getInvariantsBox().showInvariants(project.getEIAgeneratedInv_old());
		project.setInvariantsMatrix(invariantsList);
	}

	public EarlyInvariantsAnalyzer() {
		places = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getPlaces();
		transitions = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions();
		arcs = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getArcs();
		createDictionary();
	}

	private void createDictionary() {
		for (int i = 0; i < places.size(); i++)
			placesMap.put(places.get(i), i);
		for (int i = 0; i < transitions.size(); i++)
			transitionsMap.put(transitions.get(i), i);
	}

	public void CreateIncidanceMatrixAndTxTMatrix() {
		incidanceMatrixL = new ArrayList<ArrayList<Integer>>();
		TxTMatrixL = new ArrayList<ArrayList<Integer>>();

		for (int i = 0; i < transitions.size(); i++) {
			ArrayList<Integer> tmpList = new ArrayList<Integer>();
			for (int j = 0; j < places.size(); j++) {
				tmpList.add(0);
			}
			incidanceMatrixL.add(tmpList);
		}

		for (Arc oneArc : arcs) {
			int tPosition = 0;
			int pPosition = 0;
			int incidanceValue = 0;

			if (oneArc.getStartNode().getType() == PetriNetElementType.TRANSITION
					|| oneArc.getStartNode().getType() == PetriNetElementType.TIMETRANSITION) {
				tPosition = transitionsMap.get(oneArc.getStartNode());
				pPosition = placesMap.get(oneArc.getEndNode());
				incidanceValue = -1 * oneArc.getWeight(); // sprawd�
			} else {
				tPosition = transitionsMap.get(oneArc.getEndNode());
				pPosition = placesMap.get(oneArc.getStartNode());
				incidanceValue = 1 * oneArc.getWeight(); // sprawd�
			}

			incidanceMatrixL.get(tPosition).set(pPosition, incidanceValue);
		}

		for (int i = 0; i < transitions.size(); i++) {
			ArrayList<Integer> tmpList = new ArrayList<Integer>();
			for (int j = 0; j < transitions.size(); j++) {
				if (i == j) {
					tmpList.add(1);
				} else {
					tmpList.add(0);
				}
			}
			TxTMatrixL.add(tmpList);
		}
	}

	@SuppressWarnings("unused")
	public void Analyze() {

		ArrayList<ArrayList<Integer>> incidanceMatrixTMPL = new ArrayList<ArrayList<Integer>>();
		ArrayList<ArrayList<Integer>> TxTMatrixTMPL = new ArrayList<ArrayList<Integer>>();
		ArrayList<ArrayList<ArrayList<Integer>>> incidanceListL = new ArrayList<ArrayList<ArrayList<Integer>>>();
		ArrayList<ArrayList<ArrayList<Integer>>> tListL = new ArrayList<ArrayList<ArrayList<Integer>>>();

		incidanceListL.add(incidanceMatrixL);
		tListL.add(TxTMatrixL);

		incidanceMatrixTMPL = incidanceListL.get(incidanceListL.size() - 1);
		TxTMatrixTMPL = tListL.get(tListL.size() - 1);
		ArrayList<ArrayList<Integer>> rowsForEtapI = new ArrayList<ArrayList<Integer>>();

		System.out.println("------->Czysta nie naruszona siec<---------");
		System.out.println("------->Macierz incydencji<---------");
		for (int it = 0; it < incidanceMatrixTMPL.size(); it++) {
			System.out.println();
			for (int jo = 0; jo < places.size(); jo++)
				System.out.print(incidanceMatrixTMPL.get(it).get(jo) + " ");
		}
		System.out.println();
		System.out.println("------->Macierz TxT<---------");
		for (int it = 0; it < TxTMatrixTMPL.size(); it++) {
			System.out.println();
			for (int jo = 0; jo < transitions.size(); jo++)
				System.out.print(TxTMatrixTMPL.get(it).get(jo) + " ");
		}

		// Etap I z artykulu
		for (int p = 0; p < places.size(); p++) {
			// Wystepuje tylko jedno wejscie i wyjscie z miejsca
			if (checkEtapI(incidanceMatrixTMPL, p)) {
				findAndCreateNewRowL(incidanceMatrixTMPL, rowsForEtapI, p);
				sumRowsForIEtap(incidanceMatrixTMPL, TxTMatrixTMPL, rowsForEtapI);

			}
			rowsForEtapI.clear();
			incidanceListL.clear();
			incidanceListL.add(incidanceMatrixTMPL);
			tListL.clear();
			tListL.add(TxTMatrixTMPL);
		}

		System.out.println("------->EtapI - zakonczony<---------");
		System.out.println("------->Macierz incydencji<---------");
		for (int it = 0; it < incidanceMatrixTMPL.size(); it++) {
			System.out.println();
			for (int jo = 0; jo < places.size(); jo++)
				System.out.print(incidanceMatrixTMPL.get(it).get(jo) + " ");
		}
		System.out.println();
		System.out.println("------->Macierz TxT<---------");
		for (int it = 0; it < TxTMatrixTMPL.size(); it++) {
			System.out.println();
			for (int jo = 0; jo < transitions.size(); jo++)
				System.out.print(TxTMatrixTMPL.get(it).get(jo) + " ");
		}

		incidanceMatrixTMPL = incidanceListL.get(incidanceListL.size() - 1);
		incidanceListL.remove(incidanceListL.size() - 1);
		TxTMatrixTMPL = tListL.get(tListL.size() - 1);
		tListL.remove(tListL.size() - 1);

		ArrayList<ArrayList<ArrayList<Integer>>> iLL = new ArrayList<ArrayList<ArrayList<Integer>>>();
		ArrayList<ArrayList<ArrayList<Integer>>> tLL = new ArrayList<ArrayList<ArrayList<Integer>>>();
		ArrayList<ArrayList<Integer>> newRowL = new ArrayList<ArrayList<Integer>>();

		// Etap II z artykulu

		ArrayList<ArrayList<Integer>> im = new ArrayList<ArrayList<Integer>>();

		for (int i = 0; i < incidanceMatrixTMPL.size(); i++) {
			ArrayList<Integer> tmp = new ArrayList<Integer>();
			for (int j = 0; j < incidanceMatrixTMPL.get(i).size(); j++)
				tmp.add(incidanceMatrixTMPL.get(i).get(j));
			im.add(tmp);
		}

		ArrayList<ArrayList<Integer>> txt = new ArrayList<ArrayList<Integer>>();

		for (int i = 0; i < TxTMatrixTMPL.size(); i++) {
			ArrayList<Integer> tmp = new ArrayList<Integer>();
			for (int j = 0; j < TxTMatrixTMPL.get(i).size(); j++)
				tmp.add(TxTMatrixTMPL.get(i).get(j));
			txt.add(tmp);
		}

		etapII(im, txt, newRowL);
		setFoundedInvariantsL(im, txt);
		getMinimal(invariantsList);

		System.out.print("Koniec pracy analizatora");
	}
	
	@SuppressWarnings("unused")
	private void propAnalyze()
	{
		//PUR - pure net
		boolean isPure = true;
		for(Transition t : transitions)
		{
			boolean arcIn = false;
			boolean arcOut = false;
			for(ElementLocation el : t.getElementLocations())
			{
				if(!el.getInArcs().isEmpty()&&arcIn==false)
					arcIn = true;
				if(!el.getOutArcs().isEmpty()&&arcOut==false)
					arcOut = true;
			}
			if(arcIn==false||arcOut==false)
				isPure = false;
		}		
		for(Place p : places)
		{
			boolean arcIn = false;
			boolean arcOut = false;
			for(ElementLocation el : p.getElementLocations())
			{
				if(!el.getInArcs().isEmpty()&&arcIn==false)
					arcIn = true;
				if(!el.getOutArcs().isEmpty()&&arcOut==false)
					arcOut = true;
			}
			if(arcIn==false||arcOut==false)
				isPure = false;
		}
		
		//ORD - ordinary net
		boolean isOrdinary = true;
		for(Arc a : arcs)
			if(a.getWeight()!=1)
				isOrdinary = false;
		
		//HOM - homogenous net
		boolean isHomogenous = true;
		for(Place p : places)
		{
			int val = 0;
			for(ElementLocation el : p.getElementLocations())
				for(Arc a : el.getOutArcs())
					if(val==0)
						val = a.getWeight();
					else if(val!=a.getWeight())
						isHomogenous = false;			
		}
		//NBM - non blocking multiplicity net
		boolean isNonBlockingMulti = true;
		for(Place p : places)
		{
			int valIn = Integer.MAX_VALUE;
			int valOut = 0;
			for(ElementLocation el : p.getElementLocations())
			{
				for(Arc a : el.getInArcs())
					if(a.getWeight() < valIn)
						valIn = a.getWeight(); 
				for(Arc a : el.getOutArcs())
					if(a.getWeight() > valOut)
						valOut = a.getWeight();
				
			}
			if(valOut>valIn)
				isNonBlockingMulti = false;				
		}
		
		ArrayList<Boolean> NetProps = new ArrayList<Boolean>();
		NetProps.add(isPure);
		NetProps.add(isOrdinary);
		NetProps.add(isHomogenous);
		NetProps.add(isNonBlockingMulti);
		//GUIManager.getDefaultGUIManager()
	}

	@SuppressWarnings("unused")
	private int chceckInv(ArrayList<Integer> in1, ArrayList<Integer> in2) {
		int zgodnosc1 = 0;
		int zgodnosc2 = 0;
		int zgodnosc3 = 0;
		// czy in1 jest bardziej polak... minimalny niz nv2
		for (int i = 0; i < in1.size(); i++) {

			if (in1.get(i) > in2.get(i))
				zgodnosc1++;
			if (in1.get(i) < in2.get(i))
				zgodnosc2++;
			if (in1.get(i) == 0 && in2.get(i) > 0)
				zgodnosc3++;
		}

		if (zgodnosc1 > 0 && zgodnosc2 == 0)
			return 1;
		if (zgodnosc1 > 0 && zgodnosc2 > 0)
			return 2;
		else
			return 0;
	}

	private void etapII(ArrayList<ArrayList<Integer>> incidanceMatrixTMPL,
			ArrayList<ArrayList<Integer>> TxTMatrixTMPL,
			ArrayList<ArrayList<Integer>> newRowL) {
		for (int p = 0; p < places.size(); p++) {
			if (!znalazl) {

				if (checkEtapII(incidanceMatrixTMPL, p)) {

					ArrayList<ArrayList<Integer>> im = new ArrayList<ArrayList<Integer>>();

					for (int i = 0; i < incidanceMatrixTMPL.size(); i++) {
						ArrayList<Integer> tmp = new ArrayList<Integer>();
						for (int j = 0; j < incidanceMatrixTMPL.get(i).size(); j++)
							tmp.add(incidanceMatrixTMPL.get(i).get(j));
						im.add(tmp);
					}

					ArrayList<ArrayList<Integer>> txt = new ArrayList<ArrayList<Integer>>();

					for (int i = 0; i < TxTMatrixTMPL.size(); i++) {
						ArrayList<Integer> tmp = new ArrayList<Integer>();
						for (int j = 0; j < TxTMatrixTMPL.get(i).size(); j++)
							tmp.add(TxTMatrixTMPL.get(i).get(j));
						txt.add(tmp);
					}

					findAndCreateNewRowL(im, newRowL, p);

					if (newRowL.size() > 0) {
						addRows(im, txt, newRowL, true);
					}
					newRowL.clear();

					boolean pusty = true;
					pusty = checkIfHasFinishL(im, pusty);
					if (pusty) {
						setFoundedInvariantsL(im, txt);
						znalazl = true;
					} else {

						glebokosc++;

						if (p + 1 == places.size()) {

						} else {
							etapII(im, txt, newRowL);
						}
						glebokosc--;
					}

					setFoundedInvariantsL(im, txt);
				}
			}
		}
	}

	private void sumRowsForIEtap(ArrayList<ArrayList<Integer>> incidanceMatrixTMPL,
			ArrayList<ArrayList<Integer>> TxTMatrixTMPL,
			ArrayList<ArrayList<Integer>> newRowL) {
		if (newRowL.size() > 0) {
			ArrayList<ArrayList<Integer>> incidanceMatrixTML = new ArrayList<ArrayList<Integer>>();
			ArrayList<ArrayList<Integer>> TxTMatrixTML = new ArrayList<ArrayList<Integer>>();

			for (int l = 0; l < newRowL.size(); l++) {
				int tr = newRowL.get(l).get(0);
				int pl = newRowL.get(l).get(4);

				if (incidanceMatrixTMPL.get(tr).get(pl) < 0)
					addNewRowVerIL(incidanceMatrixTMPL, TxTMatrixTMPL, newRowL,
							incidanceMatrixTML, TxTMatrixTML, l);
				else
					addNewRowVerIIL(incidanceMatrixTMPL, TxTMatrixTMPL,
							newRowL, incidanceMatrixTML, TxTMatrixTML, l);

			}

			addOldRowsL(incidanceMatrixTMPL, TxTMatrixTMPL, newRowL, incidanceMatrixTML, TxTMatrixTML);

			incidanceMatrixTMPL.clear();
			for (int i = 0; i < incidanceMatrixTML.size(); i++) {
				ArrayList<Integer> tmp = new ArrayList<Integer>();
				for (int j = 0; j < incidanceMatrixTML.get(i).size(); j++)
					tmp.add(incidanceMatrixTML.get(i).get(j));
				incidanceMatrixTMPL.add(tmp);
			}
			// incidanceMatrixTMPL.add(incidanceMatrixTML.get(i));
			TxTMatrixTMPL.clear();
			for (int i = 0; i < TxTMatrixTML.size(); i++) {
				ArrayList<Integer> tmp = new ArrayList<Integer>();
				for (int j = 0; j < TxTMatrixTML.get(i).size(); j++)
					tmp.add(TxTMatrixTML.get(i).get(j));
				TxTMatrixTMPL.add(tmp);
			}
			// TxTMatrixTMPL.add(TxTMatrixTML.get(i));
		}
	}

	private void addRows(ArrayList<ArrayList<Integer>> incidanceMatrixTMPL,
			ArrayList<ArrayList<Integer>> TxTMatrixTMPL,
			ArrayList<ArrayList<Integer>> newRowL, boolean etap) {
		if (newRowL.size() > 0) {
			ArrayList<ArrayList<Integer>> incidanceMatrixTML = new ArrayList<ArrayList<Integer>>();
			ArrayList<ArrayList<Integer>> TxTMatrixTML = new ArrayList<ArrayList<Integer>>();

			for (int l = 0; l < newRowL.size(); l++) {

				if (incidanceMatrixTMPL.get(newRowL.get(l).get(0)).get(
						newRowL.get(l).get(4)) < 0)
					addNewRowVerIL(incidanceMatrixTMPL, TxTMatrixTMPL, newRowL,
							incidanceMatrixTML, TxTMatrixTML, l);
				else
					addNewRowVerIIL(incidanceMatrixTMPL, TxTMatrixTMPL,
							newRowL, incidanceMatrixTML, TxTMatrixTML, l);

			}

			addOldRowsL(incidanceMatrixTMPL, TxTMatrixTMPL, newRowL, incidanceMatrixTML, TxTMatrixTML);

			incidanceMatrixTMPL.clear();
			for (int i = 0; i < incidanceMatrixTML.size(); i++) {
				ArrayList<Integer> tmp = new ArrayList<Integer>();
				for (int j = 0; j < incidanceMatrixTML.get(i).size(); j++)
					tmp.add(incidanceMatrixTML.get(i).get(j));
				incidanceMatrixTMPL.add(tmp);
			}

			TxTMatrixTMPL.clear();
			for (int i = 0; i < TxTMatrixTML.size(); i++) {
				ArrayList<Integer> tmp = new ArrayList<Integer>();
				for (int j = 0; j < TxTMatrixTML.get(i).size(); j++)
					tmp.add(TxTMatrixTML.get(i).get(j));
				TxTMatrixTMPL.add(tmp);
			}
		}
	}

	private void setFoundedInvariantsL(ArrayList<ArrayList<Integer>> iL, ArrayList<ArrayList<Integer>> tL) {
		for (int it = 0; it < iL.size(); it++) {
			boolean zero = true;
			for (int jo = 0; jo < places.size(); jo++) {
				if (iL.get(it).get(jo) != 0)
					zero = false;
			}
			if (zero)
			{
				if (checkIfExist(invariantsList, tL.get(it)) == 0) {
					invariantsList.add(tL.get(it));
					System.out.println("!!!Wstawiam!!!");
				}
				
				//if (checkIfExist(listaInvatianow, tL.get(it),x) == 2) {
				//	listaInvatianow.set(x, tL.get(it));
				//	System.out.println("!!!Wstawiam2!!!");
				//}
				
			}
		}
	}
	
	private void getMinimal(ArrayList<ArrayList<Integer>> iL) {
		ArrayList<ArrayList<Integer>> minList = new ArrayList<ArrayList<Integer>>();
		
		for (int it = 0; it < iL.size(); it++) {
			boolean isSimp = true;
			for (int it2 = 0; it2 < iL.size(); it2++) {
				if(it!=it2)
				{
					boolean tmp = isSimpler(iL,it,it2);
					if(isSimp==false&&tmp==true)
					{
						
					}
					else
					{
						isSimp = tmp;
					}
				}				
			}
			
			if(isSimp)
				minList.add(iL.get(it));
		}
		invariantsList = minList;
	}
	
	@SuppressWarnings("unused")
	private boolean isSimpler(ArrayList<ArrayList<Integer>> iL, int it1, int it2)
	{
		int u = 0;
		int n = 0;
		int r = 0;
		
		for (int i = 0; i < iL.get(0).size(); i++)
		{
			if(iL.get(it1).get(i)>iL.get(it2).get(i))
				n++;
			if(iL.get(it1).get(i)<iL.get(it2).get(i))
				u++;
			if(iL.get(it1).get(i)==iL.get(it2).get(i))
				r++;
		}
		
		if(u==0&&n>0)
			return false;
		
		return true;
	}

	@SuppressWarnings("unused")
	private int checkIfExist(ArrayList<ArrayList<Integer>> LI,
			ArrayList<Integer> INV) {
		//Tu wsadzi minimalny
		
		int exist = 0;
		for (int i = 0; i < LI.size(); i++) {
			ArrayList<Integer> oldinv = LI.get(i);
			boolean check = true;
			boolean min = true;
			for (int j = 0; j < oldinv.size(); j++) {
				if (oldinv.get(j) != INV.get(j))
					check = false;
				//if (oldinv.get(j) > INV.get(j)&&INV.get(j)!=0)
				//	min = false;//spr
				//TODO
				
			}
			if (check)
				exist = 1;
		}

		return exist;
	}

	private boolean checkIfHasFinishL(ArrayList<ArrayList<Integer>> list,
			boolean pusty) {
		pusty = true;
		for (int t = 0; t < list.size(); t++)
			for (int p = 0; p < list.get(t).size(); p++)
				if (list.get(t).get(p) != 0)
					pusty = false;

		return pusty;
	}

	private void addOldRowsL(ArrayList<ArrayList<Integer>> incidanceMatrixTMP,
			ArrayList<ArrayList<Integer>> TxTMatrixTMP,
			ArrayList<ArrayList<Integer>> newRow,
			ArrayList<ArrayList<Integer>> incidanceMatrixTM,
			ArrayList<ArrayList<Integer>> TxTMatrixTM) {

		for (int i = 0; i < TxTMatrixTMP.size(); i++) {
			boolean kist = false;

			for (int k = 0; k < newRow.size(); k++)
				if ((i == newRow.get(k).get(0)) || i == newRow.get(k).get(1)) {
					kist = true;
				}

			if (kist == false) {
				ArrayList<Integer> NR = new ArrayList<Integer>();

				for (int b = 0; b < places.size(); b++) {
					NR.add(incidanceMatrixTMP.get(i).get(b));
				}
				incidanceMatrixTM.add(NR);
				NR = new ArrayList<Integer>();
				for (int b = 0; b < transitions.size(); b++) {
					NR.add(TxTMatrixTMP.get(i).get(b));
				}
				TxTMatrixTM.add(NR);
			}
		}
	}

	private boolean checkEtapI(ArrayList<ArrayList<Integer>> incidanceMatrixTMP, int place) {
		int input = 0;
		int output = 0;
		for (int t = 0; t < incidanceMatrixTMP.size(); t++) {
			if (incidanceMatrixTMP.get(t).get(place) > 0)
				input++;
			if (incidanceMatrixTMP.get(t).get(place) < 0)
				output++;
		}
		if (input == 1 && output == 1)
			return true;
		else
			return false;
	}

	private boolean checkEtapII(ArrayList<ArrayList<Integer>> incidanceMatrixTMP, int place) {
		int input = 0;
		int output = 0;
		for (int t = 0; t < incidanceMatrixTMP.size(); t++) {
			if (incidanceMatrixTMP.get(t).get(place) > 0)
				input++;
			if (incidanceMatrixTMP.get(t).get(place) < 0)
				output++;
		}
		if (input > 0 && output > 0)
			return true;
		else
			return false;
	}

	private void addNewRowVerIL(
			ArrayList<ArrayList<Integer>> incidanceMatrixTMP,
			ArrayList<ArrayList<Integer>> TxTMatrixTMP,
			ArrayList<ArrayList<Integer>> newRow,
			ArrayList<ArrayList<Integer>> incidanceMatrixTM,
			ArrayList<ArrayList<Integer>> TxTMatrixTM, int l) {

		ArrayList<Integer> NR = new ArrayList<Integer>();
		for (int b = 0; b < places.size(); b++)
			NR.add(-(incidanceMatrixTMP.get(newRow.get(l).get(0)).get(b) * newRow
					.get(l).get(2))
					+ (incidanceMatrixTMP.get(newRow.get(l).get(1)).get(b) * newRow
							.get(l).get(3)));

		incidanceMatrixTM.add(NR);
		NR = new ArrayList<Integer>();

		for (int b = 0; b < transitions.size(); b++)
			NR.add(bezwzgledna(-(TxTMatrixTMP.get(newRow.get(l).get(0)).get(b) * newRow
					.get(l).get(2))
					+ (TxTMatrixTMP.get(newRow.get(l).get(1)).get(b) * newRow
							.get(l).get(3))));

		TxTMatrixTM.add(NR);
	}

	private void addNewRowVerIIL(
			ArrayList<ArrayList<Integer>> incidanceMatrixTMP,
			ArrayList<ArrayList<Integer>> TxTMatrixTMP,
			ArrayList<ArrayList<Integer>> newRow,
			ArrayList<ArrayList<Integer>> incidanceMatrixTM,
			ArrayList<ArrayList<Integer>> TxTMatrixTM, int l) {

		ArrayList<Integer> NR = new ArrayList<Integer>();
		for (int b = 0; b < places.size(); b++)
			NR.add((incidanceMatrixTMP.get(newRow.get(l).get(0)).get(b) * newRow
					.get(l).get(2))
					- (incidanceMatrixTMP.get(newRow.get(l).get(1)).get(b) * newRow
							.get(l).get(3)));

		incidanceMatrixTM.add(NR);
		NR = new ArrayList<Integer>();

		for (int b = 0; b < transitions.size(); b++)
			NR.add(bezwzgledna((TxTMatrixTMP.get(newRow.get(l).get(0)).get(b) * newRow
					.get(l).get(2))
					- (TxTMatrixTMP.get(newRow.get(l).get(1)).get(b) * newRow
							.get(l).get(3))));

		TxTMatrixTM.add(NR);
	}

	private void findAndCreateNewRowL(ArrayList<ArrayList<Integer>> incidanceMatrixTMP, ArrayList<ArrayList<Integer>> newRow, int j) {
		int sizeM = incidanceMatrixTMP.size();
		for (int t1 = 0; t1 < sizeM; t1++) {
			int val1 = incidanceMatrixTMP.get(t1).get(j); // #
			//if (incidanceMatrixTMP.get(t1).get(j) != 0) {
			if (val1 != 0) { //#
				for (int t2 = t1; t2 < sizeM; t2++) {
					int val2 = incidanceMatrixTMP.get(t2).get(j); // #
					//if (incidanceMatrixTMP.get(t2).get(j) != 0)
					if (val2 != 0) {
						if (t2 != t1) {
							//if ((incidanceMatrixTMP.get(t1).get(j) > 0 && incidanceMatrixTMP.get(t2).get(j) < 0)
							//		|| (incidanceMatrixTMP.get(t1).get(j) < 0 && incidanceMatrixTMP.get(t2).get(j) > 0)) {
							//	int l1 = bezwzgledna(incidanceMatrixTMP.get(t1).get(j));
							//	int l2 = bezwzgledna(incidanceMatrixTMP.get(t2).get(j));
							if ((val1 > 0 && val2 < 0) || (val1 < 0 && val2 > 0)) {
								//int l1 = bezwzgledna(incidanceMatrixTMP.get(t1).get(j));
								//int l2 = bezwzgledna(incidanceMatrixTMP.get(t2).get(j));
								int l1 = bezwzgledna(val1);
								int l2 = bezwzgledna(val2);
								
								// ((x*y)/nwd(x,y))
								int nwd = (l1 * l2) / nwd(l1, l2);
								ArrayList<Integer> tab = new ArrayList<Integer>();
								tab.add(t1);
								tab.add(t2);
								tab.add(nwd / incidanceMatrixTMP.get(t1).get(j));
								tab.add(nwd / incidanceMatrixTMP.get(t2).get(j));
								tab.add(j);
								newRow.add(tab);
							}
						}
					}
				}
			}
		}
	}

	public void SetArcForAnalization(ArrayList<Arc> a) {
		arcs = a;

	}

	public static int nwd(int x, int y) {
		while (x != y) {
			if (x > y)
				x -= y;
			else
				y -= x;
		}
		return x;
	}

	public int silnia(int i) {
		if (i == 0)
			return 1;
		else
			return i * silnia(i - 1);
	}

	public int bezwzgledna(int i) {
		if (i < 0)
			return -i;
		else
			return i;
	}

	public ArrayList<ArrayList<Integer>> getListaInvatianow() {
		return invariantsList;
	}

	public void setListaInvatianow(ArrayList<ArrayList<Integer>> listaInvatianow) {
		this.invariantsList = listaInvatianow;
	}
}

*/