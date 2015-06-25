package abyss.analyse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import abyss.analyzer.matrix.IncidenceMatrix;
import abyss.analyzer.matrix.InputMatrix;
import abyss.analyzer.matrix.OutputMatrix;
import abyss.darkgui.GUIManager;
import abyss.math.PetriNet;
import abyss.math.Transition;

/**
 * Klasa analizatora, aktualnie odpowiedzialna za generowanie zbiorów MCT.
 * @author students
 * @author MR
 *
 */
public class MCTCalculator {
	private InputMatrix inMatrix;
	private OutputMatrix outMatrix;
	private IncidenceMatrix matrix;
	private PetriNet petriNet;
	//private ArrayList<ArrayList<InvariantTransition>> tInvariants = new ArrayList<ArrayList<InvariantTransition>>();

	/**
	 * Konstruktor obiektu analizatora
	 * @param petriNet
	 */
	public MCTCalculator(PetriNet petriNet) {
		this.petriNet = petriNet;
	}

	/**
	 * Metoda tworząca macierze wejściowe i wyjściowe (in/out arcs) oraz na ich
	 * bazie tworząca nową macierz incydencji
	 */
	public void initiateData() {
		inMatrix = new InputMatrix(petriNet.getTransitions(), petriNet.getPlaces());
		outMatrix = new OutputMatrix(petriNet.getTransitions(), petriNet.getPlaces());
		setMatrix(new IncidenceMatrix(petriNet.getTransitions(), petriNet.getPlaces(), inMatrix, outMatrix));
	}

	/**
	 * Metoda zwracają aktualną macierz incydencji.
	 * @return IncidenceMatrix - obiekt klasy IncidenceMatrix
	 */
	public IncidenceMatrix getMatrix() {
		return matrix;
	}

	/**
	 * Metoda ustawiająca nową macierz incydencji.
	 * @param matrix IncidenceMatrix - nowa macierz
	 */
	private void setMatrix(IncidenceMatrix matrix) {
		this.matrix = matrix;
	}

	/**
	 * Metoda generująca zbiory MCT na bazie macierzy T-inwariantów.
	 * @return ArrayList[ArrayList[Transition]] - macierz wyjściowa
	 */
	public ArrayList<ArrayList<Transition>> generateMCT() {
		ArrayList<Transition> allTransitions = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions();
		ArrayList<ArrayList<Integer>> invariantsTranspose =	InvariantsTools.transposeMatrix(
				GUIManager.getDefaultGUIManager().getWorkspace().getProject().getInvariantsMatrix() );
		
		invariantsTranspose = InvariantsTools.returnBinaryMatrix(invariantsTranspose);
		
		ArrayList<ArrayList<Transition>> mctGroups = new ArrayList<ArrayList<Transition>>();
		int size = allTransitions.size();
		
		
		for(int t1=0; t1<size; t1++) {
			ArrayList<Transition> currentMCT = new ArrayList<Transition>();
			ArrayList<Integer> invVector = invariantsTranspose.get(t1);
			ArrayList<Integer> support = InvariantsTools.getSupport(invVector); //tutaj: niezerowy, jeśli t należy do inw
			for(int t2=0; t2<size; t2++) {
				
				ArrayList<Integer> invVector2 = invariantsTranspose.get(t2);
				if(invVector.equals(invVector2) && support.size()>0 ) {
					currentMCT.add(allTransitions.get(t2));
				}
			}
			if ((currentMCT.size() > 0) && !mctGroups.contains(currentMCT))
				mctGroups.add(currentMCT);
		}
		GUIManager.getDefaultGUIManager().reset.setMCTStatus(true); //status zbiorów MCT: wczytane
		return mctGroups;
	}
	
	/**
	 * Metoda sortuje zbiory MCT
	 * @param mctGroups ArrayList[ArrayList[Transition]] - nieposortowane MCT
	 * @param addTrivial boolean - true, jeżeli w wyniku mają się na końcu listy znaleźć trywialne zbiory MCT
	 * @return ArrayList[ArrayList[Transition]] - posortowane zbiory MCT
	 */
	@SuppressWarnings("unchecked")
	public static ArrayList<ArrayList<Transition>> getSortedMCT(ArrayList<ArrayList<Transition>> mctGroups, boolean addTrivial) {
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
		
		if(addTrivial)
			mctGroups.add(unused); //dodaj wszystkie pojedyncze tranzycje w jeden 'mct'
		
		return mctGroups;
	}
}
