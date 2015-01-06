package abyss.analyzer;

import java.util.ArrayList;

import abyss.analyzer.matrix.IncidenceMatrix;
import abyss.analyzer.matrix.InputMatrix;
import abyss.analyzer.matrix.OutputMatrix;
import abyss.darkgui.GUIManager;
import abyss.math.InvariantTransition;
import abyss.math.PetriNet;
import abyss.math.Transition;

/**
 * Klasa analizatora, aktualnie odpowiedzialna za generowanie zbiorów MCT.
 * @author students
 *
 */
public class DarkAnalyzer {
	private InputMatrix inMatrix;
	private OutputMatrix outMatrix;
	private IncidenceMatrix matrix;
	private PetriNet petriNet;
	private ArrayList<ArrayList<InvariantTransition>> tInvariants = new ArrayList<ArrayList<InvariantTransition>>();

	/**
	 * Konstruktor obiektu analizatora
	 * @param petriNet
	 */
	public DarkAnalyzer(PetriNet petriNet) {
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
	 * Metoda generująca zbiory MCT na bazie t-inwariantów
	 * @param tInvariantsList ArrayList[ArrayList[InvariantTransition]] - macierz inwariantów
	 * @return ArrayList[ArrayList[Transition]] - macierz wyjściowa
	 */
	public ArrayList<ArrayList<Transition>> generateMCT(ArrayList<ArrayList<InvariantTransition>> tInvariantsList) {
		// konwersja z InvariantTransitions na Transitions
		ArrayList<ArrayList<Transition>> invariants = new ArrayList<ArrayList<Transition>>();
		for (ArrayList<InvariantTransition> invariant : tInvariantsList) {
			ArrayList<Transition> currentInvariant = new ArrayList<Transition>();
			for (InvariantTransition invariantTransition : invariant) {
				currentInvariant.add(invariantTransition.getTransition());
			}
			invariants.add(currentInvariant);
		}
		// dodaje do każdej tranzycji liste inwariantow, ktore ja zawieraja...
		ArrayList<Transition> allTransitions = GUIManager
				.getDefaultGUIManager().getWorkspace().getProject().getTransitions();
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
		return mctGroups;
	}

	/**
	 * Metoda zwracająca macierz t-inwariantów
	 * @return ArrayList[ArrayList[InvariantTransition]] - macierz inwariantów
	 */
	public ArrayList<ArrayList<InvariantTransition>> gettInvariants() {
		return tInvariants;
	}

	/**
	 * Metoda ustawiająca nową macierz t-inwariantów
	 * @return ArrayList[ArrayList[InvariantTransition]] - nowa macierz inwariantów
	 */
	public void settInvariants(ArrayList<ArrayList<InvariantTransition>> tInvariants) {
		this.tInvariants = tInvariants;
	}
}
