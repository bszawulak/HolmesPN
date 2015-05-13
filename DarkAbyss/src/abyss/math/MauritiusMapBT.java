package abyss.math;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import abyss.analyse.InvariantsTools;
import abyss.analyse.MCTCalculator;
import abyss.darkgui.GUIManager;

/**
 * Klasa tworząca mapę Mauritiusa dla wybranej tranzycji. Publikacja:
 * 
 * "Petri net modelling of gene regulation of the Duchenne muscular dystrophy"
 * Stefanie Grunwald, Astrid Speer, Jorg Ackermann, Ina Koch
 * BioSystems, 2008, 92, pp.189-205
 * 
 * 
 * @author MR
 *
 */
public class MauritiusMapBT {
	BTNode root = null;
	ArrayList<Transition> transitions = null;
	ArrayList<String> transMCTNames = null;
	boolean testMode = false;
	
	ArrayList<String> transitionsS = null;
	public enum NodeType {
		ROOT, BRANCH, LEAF, VERTEX
	}

	public MauritiusMapBT(ArrayList<ArrayList<Integer>> invariants, int rootTransition) {
		root = new BTNode();
		root.type = NodeType.ROOT;
		transitions = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions();
		ArrayList<ArrayList<Integer>> subInvariants = InvariantsTools.returnInvWithTransition(invariants, rootTransition);
		
		transMCTNames = getMCTNamesVector();
		
		createMTree(subInvariants, rootTransition, root);
	}
	
	public MauritiusMapBT(ArrayList<ArrayList<Integer>> invariants) {
		root = new BTNode();
		root.type = NodeType.ROOT;
		transitions = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions();
		
		//tu wstaw MCT
		transMCTNames = getMCTNamesVector();
		
		createMTree(invariants, -1, root);
	}
	
	/**
	 * Metoda tworzy znormalizowane nazwy tranzycji z uwzględnieniem ich obecności w zbiorach MCT
	 * @return ArrayList[String] - wektor nazw tranzycji
	 */
	private ArrayList<String> getMCTNamesVector() {
		MCTCalculator analyzer = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getAnalyzer();
		ArrayList<ArrayList<Transition>> mct = analyzer.generateMCT();
		ArrayList<String> resultNames = new ArrayList<String>();
		
		for(Transition t : transitions) {
			resultNames.add(t.getName());
		}
		
		if(mct.size() == 0) {
			return resultNames;
		} else {
			mct = getSortedMCT(mct);
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
	 * Metoda usuwa trywialne MCT oraz ustawia ich macierz w kolejności malejacej liczności zbiorów
	 * @param mctGroups ArrayList[ArrayList[Transition]] - macierz zbiorów MCT
	 * @return ArrayList[ArrayList[Transition]] - posortowana, przycięta macierz MCT (bez trywialnych)
	 */
	@SuppressWarnings("unchecked")
	private ArrayList<ArrayList<Transition>> getSortedMCT(ArrayList<ArrayList<Transition>> mctGroups) {
		//ogranicz MCT do nietrywialnych
		ArrayList<Transition> unused = new ArrayList<Transition>();
		for(int i=0; i<mctGroups.size(); i++) {
			ArrayList<Transition> mctRow = mctGroups.get(i);
			if(mctRow.size()==1) {
				unused.add(mctRow.get(0));
				mctGroups.set(i, null);
			}
		}
		for(int i=0; i<mctGroups.size(); i++) {
			ArrayList<Transition> mctRow = mctGroups.get(i);
			if(mctRow == null) {
				mctGroups.remove(i);
				i--;
			}
		}
		Object [] temp = mctGroups.toArray();
		Arrays.sort(temp, new Comparator<Object>() {
			public int compare(Object o1, Object o2) {
		        ArrayList<Transition> temp1 = (ArrayList<Transition>)o1;
		        ArrayList<Transition> temp2 = (ArrayList<Transition>)o2;

		        if(temp1.size() > temp2.size())
		        	return -1;
		        else if(temp1.size() == temp2.size()) {
		        	return 0;
		        } else
		        	return 1;
		    }
		});
		
		mctGroups.clear();
		for(Object o: temp) {
			mctGroups.add((ArrayList<Transition>)o);
		}
		//mctGroups.add(unused); //dodaj wszystkie pojedzyncze tranzycje w jeden 'mct'
		
		return mctGroups;
	}
	
	/**
	 * Metoda zwraca wierzchołek główny drzewa.
	 * @return BTNode - root
	 */
	public BTNode getRoot() {
		return root;
	}
	
	/**
	 * TEST ONLY
	 */
	public MauritiusMapBT() {
		Integer[] t1 = { 1, 0, 1, 1, 1, 0, 1, 0, 1 };
		Integer[] t2 = { 0, 0, 1, 1, 1, 1, 1, 1, 0 };
		Integer[] t3 = { 1, 0, 0, 1, 1, 0, 1, 0, 1 };
		Integer[] t4 = { 0, 0, 1, 1, 1, 0, 0, 1, 0 };
		Integer[] t5 = { 1, 0, 1, 1, 0, 1, 0, 1, 1 };
		
		ArrayList<Integer> x1 = new ArrayList<Integer>(Arrays.asList(t1));
		ArrayList<Integer> x2 = new ArrayList<Integer>(Arrays.asList(t2));
		ArrayList<Integer> x3 = new ArrayList<Integer>(Arrays.asList(t3));
		ArrayList<Integer> x4 = new ArrayList<Integer>(Arrays.asList(t4));
		ArrayList<Integer> x5 = new ArrayList<Integer>(Arrays.asList(t5));
		
		ArrayList<ArrayList<Integer>> invariants = new ArrayList<ArrayList<Integer>>();
		invariants.add(x1);
		invariants.add(x2);
		invariants.add(x3);
		invariants.add(x4);
		invariants.add(x5);
		String[] s = {"t1", "t2", "t3", "t4", "t5", "t6", "t7", "t8", "t9"};
		
		transitionsS = new ArrayList<String>(Arrays.asList(s));
		root = new BTNode();
		root.type = NodeType.ROOT;
		testMode = true;
		createMTree(invariants, -1, root);
	}
	
	/**
	 * Główna metoda rekurencyjnie tworząca drzewo przechowujące mapę Mauritiusa dla wybranej tranzycji sieci.
	 * @param subInvariants ArrayList[ArrayList[Integer]] - podmacierz inwariantów
	 * @param chosenTrans int - id wierzchołka początkowego
	 * @param currentNode BTNode - aktualnie przetwarzany węzeł drzewa
	 */
	private void createMTree(ArrayList<ArrayList<Integer>> subInvariants, int chosenTrans, BTNode currentNode) {
		int maxTransition = -1; //pierwsza tranzycja z największą # wystąpień w inwariantach
		int howManyLeft = 0; 
		ArrayList<Integer> transFrequency;
		if(chosenTrans == -1) { //sam wybierz tranzycję z max(inv)
			transFrequency = InvariantsTools.getFrequency(subInvariants);
			maxTransition = getMaximumPosition(transFrequency);
			howManyLeft = getSupportSize(transFrequency);
		} else { // wybrana tranzycja
			maxTransition = chosenTrans;
			
			transFrequency = InvariantsTools.getFrequency(subInvariants);
			howManyLeft = getSupportSize(transFrequency);
		}
			
		//dla danej tranzycji wyznacz: jej inwarianty i całą resztę
		ArrayList<ArrayList<Integer>> rightInvariants = InvariantsTools.returnInvWithTransition(subInvariants, maxTransition);
		ArrayList<ArrayList<Integer>> leftInvariants = InvariantsTools.returnInvWithoutTransition(subInvariants, maxTransition);
		
		if(leftInvariants.size() == 0 || howManyLeft == 0) {
			// brak inwariantów bez tranzycji maxTransition: węzeł typu Data
			// czyli: brak lewego podrzewa
			
			if(testMode == false) {
				//currentNode.transName = transitions.get(maxTransition).getName();
				currentNode.transName = transMCTNames.get(maxTransition);
			} else
				currentNode.transName = transitionsS.get(maxTransition);
			
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
				createMTree(rightInvariants, -1, rightNode); //rekurencja
				
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
			
			if(testMode == false) {
				//currentNode.transName = transitions.get(maxTransition).getName();
				currentNode.transName = transMCTNames.get(maxTransition);
			} else
				currentNode.transName = transitionsS.get(maxTransition);
			
			currentNode.transLocation = maxTransition;
			currentNode.transFrequency = rightInvariants.size();
			currentNode.othersFrequency = leftInvariants.size();
			
			BTNode rightNode = new BTNode();
			currentNode.rightChild = rightNode;
			
			BTNode leftNode = new BTNode();
			currentNode.leftChild = leftNode;
			
			cleanTransDataInInv(rightInvariants, maxTransition);
			createMTree(rightInvariants, -1, rightNode); //rekurencja
			
			//cleanTransDataInInv(leftInvariants, maxTransition); // niemożliwe z definicji ?
			createMTree(leftInvariants, -1, leftNode); //rekurencja
		}
		
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
	 * Metoda zwraca pozycję w wektorze z największą wartością.
	 * @param vector ArrayList[Integer] - wektor liczb
	 * @return int - pozycja z największą wartością
	 */
	private int getMaximumPosition(ArrayList<Integer> vector) {
		int res = 0;
		int pos = 0;
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
	 * Metoda czyści w tablicy inwariantów dane tranzycji (pola na zero)
	 * @param invariants ArrayList[ArrayList[Integer]] - macierz inwariantów
	 * @param trans int - kolumna tranzycji
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
		public int transLocation;
		public int transFrequency;
		public int othersFrequency;
		
		public BTNode leftChild;
		public BTNode rightChild;
	}
}

