package abyss.math;

import java.util.ArrayList;

import abyss.analyse.InvariantsTools;
import abyss.darkgui.GUIManager;

/**
 * Klasa tworząca mapę Mauritiusa dla wybranej tranzycji.
 * @author MR
 *
 */
public class MauritiusMap {
	BTNode root = null;
	ArrayList<Transition> transitions = null;
	public enum NodeType {
		ROOT, BRANCH, LEAF, VERTEX
	}

	public MauritiusMap(ArrayList<ArrayList<Integer>> invariants, int rootTransition) {
		root = new BTNode();
		root.type = NodeType.ROOT;
		transitions = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions();
		ArrayList<ArrayList<Integer>> subInvariants = InvariantsTools.returnInvWithTransition(invariants, rootTransition);
		
		createMTree(subInvariants, rootTransition, root);
	}
	
	public MauritiusMap(ArrayList<ArrayList<Integer>> invariants) {
		root = new BTNode();
		root.type = NodeType.ROOT;
		transitions = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions();
		
		createMTree(invariants, -1, root);
	}
	
	/**
	 * 
	 * @param rootT
	 */
	private void createMTree(ArrayList<ArrayList<Integer>> subInvariants, int chosenTrans, BTNode currentNode) {
		int maxTransition = -1; //pierwsza tranzycja z największą # wystąpień w inwariantach
		int howManyLeft = 0; 
		if(chosenTrans == -1) { //sam wybierz tranzycję z max(inv)
			ArrayList<Integer> transFrequency = InvariantsTools.getFrequency(subInvariants);
			maxTransition = getMaximumPosition(transFrequency);
			howManyLeft = getSupportSize(transFrequency);
		} else { // wybrana tranzycja
			maxTransition = chosenTrans;
			
			ArrayList<Integer> transFrequency = InvariantsTools.getFrequency(subInvariants);
			howManyLeft = getSupportSize(transFrequency);
		}
		
		//dla danej tranzycji wyznacz: jej inwarianty i całą resztę
		ArrayList<ArrayList<Integer>> rightInvariants = InvariantsTools.returnInvWithTransition(subInvariants, maxTransition);
		ArrayList<ArrayList<Integer>> leftInvariants = InvariantsTools.returnInvWithoutTransition(subInvariants, maxTransition);
		
		if(leftInvariants.size() == 0) {
			// brak inwariantów bez tranzycji maxTransition: węzeł typu Data
			// czyli: brak lewego podrzewa
			
			currentNode.transName = transitions.get(maxTransition).getName();
			currentNode.transLocation = maxTransition;
			currentNode.transFrequency = rightInvariants.size();
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
			
			currentNode.transName = transitions.get(maxTransition).getName();
			currentNode.transLocation = maxTransition;
			currentNode.transFrequency = rightInvariants.size();
			
			BTNode rightNode = new BTNode();
			currentNode.rightChild = rightNode;
			
			BTNode leftNode = new BTNode();
			currentNode.leftChild = rightNode;
			
			cleanTransDataInInv(rightInvariants, maxTransition);
			createMTree(rightInvariants, -1, rightNode); //rekurencja
			
			//cleanTransDataInInv(leftInvariants, maxTransition); // niemożliwe z definicji ?
			createMTree(leftInvariants, -1, leftNode); //rekurencja
		}
		
	}
	
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
	
	class BTNode {
		public NodeType type = NodeType.VERTEX;
		public String transName;
		public int transLocation;
		public int transFrequency;
		
		public BTNode leftChild;
		public BTNode rightChild;
	}
}

