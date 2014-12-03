package abyss.analyzer;

import java.util.ArrayList;

import abyss.analyzer.matrix.IncidenceMatrix;
import abyss.analyzer.matrix.InputMatrix;
import abyss.analyzer.matrix.OutputMatrix;
import abyss.darkgui.GUIManager;
import abyss.math.InvariantTransition;
import abyss.math.PetriNet;
import abyss.math.Transition;

public class DarkAnalyzer {
	private InputMatrix inMatrix;
	private OutputMatrix outMatrix;
	private IncidenceMatrix matrix;

	private PetriNet petriNet;

	private ArrayList<ArrayList<InvariantTransition>> tInvariants = new ArrayList<ArrayList<InvariantTransition>>();

	public DarkAnalyzer(PetriNet petriNet) {
		this.petriNet = petriNet;
	}

	public void initiateData() {
		inMatrix = new InputMatrix(petriNet.getTranstions(),
				petriNet.getPlaces());
		outMatrix = new OutputMatrix(petriNet.getTranstions(),
				petriNet.getPlaces());
		setMatrix(new IncidenceMatrix(petriNet.getTranstions(),
				petriNet.getPlaces(), inMatrix, outMatrix));
	}

	public IncidenceMatrix getMatrix() {
		return matrix;
	}

	private void setMatrix(IncidenceMatrix matrix) {
		this.matrix = matrix;
	}

	public ArrayList<ArrayList<Transition>> generateMCT(
			ArrayList<ArrayList<InvariantTransition>> tInvariantsList) {
		// konwersja z InvariantTransitions na Transitions
		ArrayList<ArrayList<Transition>> invariants = new ArrayList<ArrayList<Transition>>();
		for (ArrayList<InvariantTransition> invariant : tInvariantsList) {
			ArrayList<Transition> currentInvariant = new ArrayList<Transition>();
			for (InvariantTransition invariantTransition : invariant) {
				currentInvariant.add(invariantTransition.getTransition());
			}
			invariants.add(currentInvariant);
		}
		// dodaje do ka¿dej tranzycji liste inwariantow, ktore ja zawieraja...
		ArrayList<Transition> allTransitions = GUIManager
				.getDefaultGUIManager().getWorkspace().getProject()
				.getTranstions();
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
				if (transition.getContainingInvariants().equals(
						anotherTransition.getContainingInvariants())
						&& transition.getContainingInvariants().size() > 0)
					currentMCT.add(anotherTransition);
			}
			if ((currentMCT.size() > 0) && !mctGroups.contains(currentMCT))
				mctGroups.add(currentMCT);
		}
		return mctGroups;
	}

	public ArrayList<ArrayList<InvariantTransition>> gettInvariants() {
		return tInvariants;
	}

	public void settInvariants(
			ArrayList<ArrayList<InvariantTransition>> tInvariants) {
		this.tInvariants = tInvariants;
	}
}
