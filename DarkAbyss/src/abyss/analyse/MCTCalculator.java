package abyss.analyse;

import java.util.ArrayList;

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
}

/*
	public ArrayList<ArrayList<Transition>> generateMCT() {
		//ArrayList<ArrayList<InvariantTransition>> tInvariantsList;
		// konwersja z InvariantTransitions na Transitions
		ArrayList<ArrayList<Transition>> invariants = new ArrayList<ArrayList<Transition>>();	
		ArrayList<ArrayList<InvariantTransition>> invTr2nd; // = analyzer.gettInvariants();
		
		invTr2nd = InvariantsTools.compute2ndFormInv(GUIManager.getDefaultGUIManager().getWorkspace().getProject().getInvariantsMatrix());
		if(invTr2nd == null) {
			GUIManager.getDefaultGUIManager().log("Unable to calculate 2nd order invariant matrix in generateMCT()", "error", true);
			return null;
		}
		
		for (ArrayList<InvariantTransition> invariant : invTr2nd) {
			ArrayList<Transition> currentInvariant = new ArrayList<Transition>();
			for (InvariantTransition invariantTransition : invariant) {
				currentInvariant.add(invariantTransition.getTransition());
			}
			invariants.add(currentInvariant);
		}
		// dodaje do każdej tranzycji liste inwariantow, ktore ja zawieraja...
		ArrayList<Transition> allTransitions = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions();
		
		for (Transition transition : allTransitions) {
			for (ArrayList<Transition> currentInvariant : invariants) {
				if (currentInvariant.contains(transition))
					transition.getContainingInvariants().add(currentInvariant);
			}
		}
	
		// tworzenie mct
		ArrayList<ArrayList<Transition>> mctGroups = new ArrayList<ArrayList<Transition>>();
		for (Transition transition : allTransitions) {
			ArrayList<Transition> currentMCT = new ArrayList<Transition>();
			for (Transition anotherTransition : allTransitions) {
				if (transition.getContainingInvariants().equals(anotherTransition.getContainingInvariants())
						&& transition.getContainingInvariants().size() > 0)
					currentMCT.add(anotherTransition);
			}
			if ((currentMCT.size() > 0) && !mctGroups.contains(currentMCT))
				mctGroups.add(currentMCT);
		}
		GUIManager.getDefaultGUIManager().reset.setMCTStatus(true); //status zbiorów MCT: wczytane
		return mctGroups;
	}
}
*/