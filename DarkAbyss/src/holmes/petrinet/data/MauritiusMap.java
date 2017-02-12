package holmes.petrinet.data;

import java.util.ArrayList;

import holmes.analyse.InvariantsTools;
import holmes.analyse.MCTCalculator;
import holmes.darkgui.GUIManager;
import holmes.petrinet.elements.Transition;

/**
 * Klasa tworząca Mapę Mauritiusa dla wybranej tranzycji. Publikacja:
 * 
 * "Petri net modelling of gene regulation of the Duchenne muscular dystrophy"
 * Stefanie Grunwald, Astrid Speer, Jorg Ackermann, Ina Koch
 * BioSystems, 2008, 92, pp.189-205
 * 
 * @author MR
 */
public class MauritiusMap {
	BTNode root = null;
	ArrayList<Transition> transitions = null;
	ArrayList<String> transMCTNames = null;
	public enum NodeType {
		ROOT, BRANCH, LEAF, VERTEX
	}

	/**
	 * Konstruktor używany do tworzenia mapy
	 * @param invariants ArrayList[ArrayList[Integer]] - macierz inwariantów
	 * @param rootTransition int - indeks tranzycji bazowej
	 * @param coverageVal int - od 0 do 100. Np. 20 oznacza, że tranzycja musi być obecna w 20% inwariantów zbioru 
	 * 		bazowego lub więcej
	 */
	public MauritiusMap(ArrayList<ArrayList<Integer>> invariants, int rootTransition, int coverageVal) {
		root = new BTNode();
		root.type = NodeType.ROOT;
		transitions = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions();
		
		invariants = addIndexToInvariants(invariants);
		
		ArrayList<ArrayList<Integer>> subInvariants = InvariantsTools.returnT_invWithTransition(invariants, rootTransition);
		ArrayList<ArrayList<Integer>> antiInvariants = InvariantsTools.returnT_invWithoutTransition(invariants, rootTransition);
		ArrayList<Integer> antiVector = InvariantsTools.getFrequency(antiInvariants, true);
		transMCTNames = getMCTNamesVector();
		
		
		ArrayList<ArrayList<Integer>> modInvariants = new ArrayList<>();
		for(int i=0; i<subInvariants.size(); i++) {
			ArrayList<Integer> pseudoInv = new ArrayList<>();
			for(int j=0; j<antiVector.size(); j++) {
				if(antiVector.get(j) == 0)
					pseudoInv.add(1);
				else
					pseudoInv.add(0);
			}
			modInvariants.add(pseudoInv);
		}
		
		/*
		//usuń tranzycje z anty-listy:
		if(antiVector.size() == subInvariants.size()) {
			for(ArrayList<Integer> inv : subInvariants) {
				for(int i=0; i<inv.size()-1; i++) { //!!! ostatnie pole ignorujemy - to indeks inwariantu
					if(antiVector.get(i) > 0) {
						inv.set(i, 0);
					}
				}
			}
		}
		//usuń tranzycje poniżej procentu pokrycia:
		ArrayList<Integer> freqVector = InvariantsTools.getFrequency(subInvariants, true);
		float treshold = (float)coverageVal / (float)100;
		float maxCoverage = freqVector.get(rootTransition);
		ArrayList<Integer> transToKeepVector = new ArrayList<Integer>();
		for(int i=0; i<freqVector.size(); i++) {
			int currentValue = freqVector.get(i);
			if(currentValue == 0) {
				transToKeepVector.add(0);
			} else {
				float coverage = (float)currentValue / maxCoverage;
				if(coverage >= treshold) {
					transToKeepVector.add(1);
				} else {
					transToKeepVector.add(0); //usun tranzycje ze zbioru
				}
			}
		}
		
		for(ArrayList<Integer> inv : subInvariants) {
			for(int i=0; i<inv.size()-1; i++) { //!!! ostatnie pole ignorujemy - to indeks inwariantu
				if(transToKeepVector.get(i) == 0) {
					inv.set(i, 0);
				}
			}
		}
		*/
		
		createMTreeV2(antiInvariants, rootTransition, root);
		//createMTreeV2(modInvariants, rootTransition, root);
		//createMTreeV2(invariants, rootTransition, root);
	}
	
	/**
	 * Metoda zwraca zmodyfikowaną macierz inwariantów - każdy inwariant otrzymuje pod koniec swojego wektora jego numer
	 * porządkowy w oryginalnej macierzy inwariantów.
	 * @param originalInvariants ArrayList[ArrayList[Integer]] - oryginalna macierz inwariantów
	 * @return ArrayList[ArrayList[Integer]] - zmodyfikowana macierz inwariantów
	 */
	private ArrayList<ArrayList<Integer>> addIndexToInvariants(ArrayList<ArrayList<Integer>> originalInvariants) {
		ArrayList<ArrayList<Integer>> newInvariants = new ArrayList<>();
		int invNumber = originalInvariants.size();
		for(int i=0; i<invNumber; i++) {
			ArrayList<Integer> newInvariant = new ArrayList<Integer>(originalInvariants.get(i));
			newInvariant.add(i);
			newInvariants.add(newInvariant);
		}
		return newInvariants;
	}

	/**
	 * Metoda tworzy znormalizowane nazwy tranzycji z uwzględnieniem ich obecności w zbiorach MCT
	 * @return ArrayList[String] - wektor nazw tranzycji
	 */
	private ArrayList<String> getMCTNamesVector() {
		MCTCalculator analyzer = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getMCTanalyzer();
		ArrayList<ArrayList<Transition>> mct = analyzer.generateMCT();
		ArrayList<String> resultNames = new ArrayList<String>();
		
		for(Transition t : transitions) {
			resultNames.add(t.getName());
		}
		
		if(mct.size() == 0) {
			return resultNames;
		} else {
			//mct = getSortedMCT(mct);
			mct = MCTCalculator.getSortedMCT(mct, false);
			int mctNo = 0;
			for(ArrayList<Transition> arr : mct) {
				mctNo++;
				for(Transition t : arr) {
					int id = transitions.indexOf(t);
					String name = t.getName() + "_MCT"+mctNo;
					resultNames.set(id, name);
				}
			}
			return resultNames;
		}
	}

	/**
	 * Metoda zwraca wierzchołek główny drzewa.
	 * @return BTNode - root
	 */
	public BTNode getRoot() {
		return root;
	}
	
	/**
	 * Tworzenia mapy mauritiusa czy jak to naprawdę się nazywa. Innymi słowy tworzy drzewo knockout na bazie
	 * zbioru inwariantów. W tym miejscu zbiór już jest 'przycięty' i nie zawiera inwariantów które nie miały
	 * tranzycji-korzenia (start) lub (rekurencja) poprzednio dodanej do drzewa tranzycji.
	 * @param subInvariants ArrayList[ArrayList[Integer]] - podmacierz inwariantów
	 * @param chosenTrans int - indeks wybranej tranzycji
	 * @param currentNode BTNode - obiekt opakowujący dane (Binary Tree Node)
	 */
	private void createMTreeV2(ArrayList<ArrayList<Integer>> subInvariants, int chosenTrans, BTNode currentNode) {
		int maxTransition = -1; //pierwsza tranzycja z największą # wystąpień w inwariantach
		int howManyLeft = 0; 
		ArrayList<Integer> transFrequency;
		if(chosenTrans == -1) { //sam wybierz tranzycję z max(inv), tryb: rekurencja
			transFrequency = InvariantsTools.getFrequency(subInvariants, true);
			maxTransition = getPositionOfMostImportantTransition(transFrequency);
			howManyLeft = getSupportSize(transFrequency); //liczba zer
		} else { // wybrana tranzycja, tylko na starcie
			maxTransition = chosenTrans;			
			transFrequency = InvariantsTools.getFrequency(subInvariants, true);
			howManyLeft = getSupportSize(transFrequency);
		}
		
		if(maxTransition == -1) {
			currentNode.transFrequency = -1;
			return;
		}
			
		//dla danej tranzycji wyznacz: jej inwarianty i całą resztę
		ArrayList<ArrayList<Integer>> invsWithCurrentNode = InvariantsTools.returnT_invWithTransition(subInvariants, maxTransition);
		ArrayList<ArrayList<Integer>> invsWithoutCurrentNode = InvariantsTools.returnT_invWithoutTransition(subInvariants, maxTransition);
		
		if(invsWithoutCurrentNode.size() == 0 || howManyLeft == 0) {
			// brak inwariantów bez tranzycji maxTransition: węzeł typu Data
			// czyli: brak poddrzewa invsWithoutCurrentNode
			
			currentNode.transName = transMCTNames.get(maxTransition);
			currentNode.transLocation = maxTransition;
			currentNode.transFrequency = invsWithCurrentNode.size();
			currentNode.myInvariantsIDs = addInvsIndices(invsWithCurrentNode);	
			currentNode.othersFrequency = invsWithoutCurrentNode.size();
			currentNode.theRestInvariantsIDs = addInvsIndices(invsWithoutCurrentNode);
			
			currentNode.leftChild = null;
			if(howManyLeft > 1) {
				if(currentNode.type != NodeType.ROOT)  //ale nie root
					currentNode.type = NodeType.BRANCH;
				
				BTNode rightNode = new BTNode();
				currentNode.rightChild = rightNode;
				
				cleanTransDataInInv(invsWithCurrentNode, maxTransition);
				createMTreeV2(invsWithCurrentNode, -1, rightNode); //rekurencja
				
				if(rightNode.transLocation == -1) //emergency
					currentNode.rightChild = null;
				
			} else { //dodano ostanią tranzycję
				if(currentNode.type != NodeType.ROOT)  //ale nie root
					currentNode.type = NodeType.LEAF;
				
				currentNode.rightChild = null;
				//wracamy wyżej
			} 
		} else { //jeśli jest poddrzewo invsWithoutCurrentNode, howManyLeft MUSI być większe od zera
			if(currentNode.type != NodeType.ROOT) //ale nie root
				currentNode.type = NodeType.BRANCH;
			
			currentNode.transName = transMCTNames.get(maxTransition);
			currentNode.transLocation = maxTransition;
			currentNode.transFrequency = invsWithCurrentNode.size();
			currentNode.myInvariantsIDs = addInvsIndices(invsWithCurrentNode);	
			currentNode.othersFrequency = invsWithoutCurrentNode.size();
			currentNode.theRestInvariantsIDs = addInvsIndices(invsWithoutCurrentNode);
			
			BTNode rightNode = new BTNode();
			currentNode.rightChild = rightNode;
			BTNode leftNode = new BTNode();
			currentNode.leftChild = leftNode;
			
			cleanTransDataInInv(invsWithCurrentNode, maxTransition);
			createMTreeV2(invsWithCurrentNode, -1, rightNode); //rekurencja
			
			if(rightNode.transLocation == -1) //emergency
				currentNode.rightChild = null;
			
			//cleanTransDataInInv(leftInvariants, maxTransition); // niemożliwe z definicji ?
			createMTreeV2(invsWithoutCurrentNode, -1, leftNode); //rekurencja
			
			if(leftNode.transLocation == -1) //emergency
				currentNode.leftChild = null;
		}
		
	}
	
	/**
	 * Zwraca wektor identyfikatorów inwariantów, które w tej klasie na ostatniej pozycji go zawierają.
	 * @param invsWithCurrentNode ArrayList[ArrayList[Integer]] - podmacierz inwariantów
	 * @return ArrayList[Integer] - macierz ID inwariantów
	 */
	private ArrayList<Integer> addInvsIndices(ArrayList<ArrayList<Integer>> invsWithCurrentNode) {
		ArrayList<Integer> idVector = new ArrayList<Integer>();
		if(invsWithCurrentNode.size() == 0) {
			return idVector;
		}
		
		int invSize = invsWithCurrentNode.get(0).size();
		for(ArrayList<Integer> inv : invsWithCurrentNode) {
			idVector.add(inv.get(invSize-1)); //ostatnia pozycja to indeks
		}
		return idVector;
	}

	/**
	 * Metoda zwraca liczność podzbioru wsparcia dla wektora.
	 * @param vector ArrayList[Integer] - wektor liczb
	 * @return int - liczność wsparcia
	 */
	private int getSupportSize(ArrayList<Integer> vector) {
		int res = 0;
		for(int el : vector) {
			if(el > 0)
				res++;
		}
		return res;
	}
	
	/**
	 * Metoda zwraca największą wartość w wektorze. 
	 * @param vector ArrayList[Integer] - wektor liczb
	 * @return int - największa wartość
	 */
	@SuppressWarnings("unused")
	private int getMaximumValue(ArrayList<Integer> vector) {
		int maxVal = 0;
		int size = vector.size();
		for(int i=0; i<size; i++) {
			if(vector.get(i) > maxVal) {
				maxVal = vector.get(i);
			}
		}
		return maxVal;
	}
	
	/**
	 * Metoda zwraca tranzycję (jej pozycję) w wektorze z największą wartością uruchomień.
	 * @param vector ArrayList[Integer] - wektor liczb
	 * @return int - pozycja z największą wartością
	 */
	private int getPositionOfMostImportantTransition(ArrayList<Integer> vector) {
		int res = 0;
		int pos = -1;
		int size = vector.size();
		for(int i=0; i<size; i++) {
			if(vector.get(i) > res) {
				res = vector.get(i);
				pos = i;
			}
		}
		return pos;
	}
	
	/**
	 * Metoda zwraca pozycje w wektorze, na których występuje okreslona wartość.
	 * @param freq ArrayList[Integer] - wektor liczb
	 * @param max int - szukana wartość
	 * @return ArrayList[Integer] - wektor pozycji na których we 'freq' występuje 'max'
	 */
	@SuppressWarnings("unused")
	private ArrayList<Integer> getMaximalPositions(ArrayList<Integer> freq, int max) {
		ArrayList<Integer> result = new ArrayList<Integer>();
		int size = freq.size();
		for(int i=0; i<size; i++) {
			if(freq.get(i) == max)
				result.add(i);
		}
		return result;
	}
	
	/**
	 * Metoda czyści w tablicy inwariantów kolumnę podanej tranzycji (pola ustawiane na 0)
	 * @param invariants ArrayList[ArrayList[Integer]] - macierz inwariantów
	 * @param trans int - kolumna tranzycji do wyczyszczenia
	 */
	private void cleanTransDataInInv(ArrayList<ArrayList<Integer>> invariants, int trans) {
		for(ArrayList<Integer> vector : invariants) {
			vector.set(trans, 0);
		}
	}
	
	/**
	 * Klasa wewnętrzna - węzeł drzewa BT
	 * @author MR
	 *
	 */
	public class BTNode {
		public NodeType type = NodeType.VERTEX;
		public String transName;
		public int transLocation = -1;
		public int transFrequency;
		public int othersFrequency;
		
		public BTNode leftChild;
		public BTNode rightChild;
		
		public ArrayList<Integer> myInvariantsIDs;
		public ArrayList<Integer> theRestInvariantsIDs;
	}
	
	//***********************************************************************************************************************************
	//***********************************************************************************************************************************
	//***********************************************************************************************************************************
	//***********************************************************************************************************************************
	//***********************************************************************************************************************************
	
	//UNUSED
	public MauritiusMap(ArrayList<ArrayList<Integer>> invariants) {
		root = new BTNode();
		root.type = NodeType.ROOT;
		transitions = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions();
		//tu wstaw MCT
		transMCTNames = getMCTNamesVector();
		createMTree(invariants, -1, root, null);
	}
	
	/**
	 * Główna metoda rekurencyjnie tworząca drzewo przechowujące mapę Mauritiusa dla wybranej tranzycji sieci.
	 * @param subInvariants ArrayList[ArrayList[Integer]] - podmacierz inwariantów
	 * @param chosenTrans int - id wierzchołka początkowego
	 * @param currentNode BTNode - aktualnie przetwarzany węzeł drzewa
	 * @param antiInvariants ArrayList[ArrayList[Integer]] - podmacierz inwariantów w których nie występuje RootTransition
	 */
	@SuppressWarnings("unused")
	private void createMTree(ArrayList<ArrayList<Integer>> subInvariants, int chosenTrans, BTNode currentNode, 
			ArrayList<Integer> antiVector) {
		int maxTransition = -1; //pierwsza tranzycja z największą # wystąpień w inwariantach
		int howManyLeft = 0; 
		ArrayList<Integer> transFrequency;
		if(chosenTrans == -1) { //sam wybierz tranzycję z max(inv)
			transFrequency = InvariantsTools.getFrequency(subInvariants, true);
			
			//minus anti-inv:
			ArrayList<Integer> tmpFreq = new ArrayList<Integer>(transFrequency);
			for(int i=0; i<tmpFreq.size(); i++) {
				if(antiVector.get(i) > 0)
					tmpFreq.set(i, 0);
			}
			//tylko te, które w antiVector mają zera:
			maxTransition = getPositionOfMostImportantTransition(tmpFreq);
			howManyLeft = antiVector.size() - getSupportSize(antiVector); //liczba zer
			if(maxTransition != -1)
				antiVector.set(maxTransition, 99);
			
			//maxTransition = getPositionOfMostImportantTransition(transFrequency);
			//howManyLeft = getSupportSize(transFrequency);
		} else { // wybrana tranzycja
			maxTransition = chosenTrans;
			howManyLeft = antiVector.size() - getSupportSize(antiVector); //liczba zer
			
			if(maxTransition != -1)
				antiVector.set(maxTransition, 99);
			//transFrequency = InvariantsTools.getFrequency(subInvariants);
			//howManyLeft = getSupportSize(transFrequency);
		}
		
		if(maxTransition == -1) {
			currentNode.transFrequency = -1;
			return;
		}
			
		//dla danej tranzycji wyznacz: jej inwarianty i całą resztę
		ArrayList<ArrayList<Integer>> rightInvariants = InvariantsTools.returnT_invWithTransition(subInvariants, maxTransition);
		ArrayList<ArrayList<Integer>> leftInvariants = InvariantsTools.returnT_invWithoutTransition(subInvariants, maxTransition);
		
		if(leftInvariants.size() == 0 || howManyLeft == 0) {
			// brak inwariantów bez tranzycji maxTransition: węzeł typu Data
			// czyli: brak lewego podrzewa
			
			currentNode.transName = transMCTNames.get(maxTransition);
			currentNode.transLocation = maxTransition;
			currentNode.transFrequency = rightInvariants.size();
			
			if(currentNode.transFrequency == 0) {
				int x = 1;
			}
			
			currentNode.othersFrequency = leftInvariants.size();
			currentNode.leftChild = null;
			if(howManyLeft > 1) {
				if(currentNode.type != NodeType.ROOT)  //ale nie root
					currentNode.type = NodeType.BRANCH;
				
				BTNode rightNode = new BTNode();
				currentNode.rightChild = rightNode;
				
				cleanTransDataInInv(rightInvariants, maxTransition);
				createMTree(rightInvariants, -1, rightNode, antiVector); //rekurencja
				
				if(rightNode.transLocation == -1) //emergency
					currentNode.rightChild = null;
				
			} else { //dodano ostanią tranzycję
				if(currentNode.type != NodeType.ROOT)  //ale nie root
					currentNode.type = NodeType.LEAF;
				
				currentNode.rightChild = null;
				//wracamy wyżej
			} 
		} else { //jeśli jest lewe podrzewo, howManyLeft MUSI być większe od zera
			// oba poddrzewa
			if(currentNode.type != NodeType.ROOT) //ale nie root
				currentNode.type = NodeType.BRANCH;
			
			currentNode.transName = transMCTNames.get(maxTransition);
			currentNode.transLocation = maxTransition;
			currentNode.transFrequency = rightInvariants.size();
			currentNode.othersFrequency = leftInvariants.size();
			
			BTNode rightNode = new BTNode();
			currentNode.rightChild = rightNode;
			BTNode leftNode = new BTNode();
			currentNode.leftChild = leftNode;
			
			cleanTransDataInInv(rightInvariants, maxTransition);
			createMTree(rightInvariants, -1, rightNode, antiVector); //rekurencja
			
			if(rightNode.transLocation == -1) //emergency
				currentNode.rightChild = null;
			
			//cleanTransDataInInv(leftInvariants, maxTransition); // niemożliwe z definicji ?
			createMTree(leftInvariants, -1, leftNode, antiVector); //rekurencja
			
			if(leftNode.transLocation == -1) //emergency
				currentNode.leftChild = null;
		}
	}
}

