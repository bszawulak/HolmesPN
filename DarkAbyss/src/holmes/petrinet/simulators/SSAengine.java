package holmes.petrinet.simulators;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

import holmes.petrinet.elements.Node;
import holmes.petrinet.elements.Place;
import holmes.petrinet.elements.Transition;
import holmes.petrinet.simulators.NetSimulator.NetType;

public class SSAengine implements IEngine {
	private Map<Place, ArrayList<Transition>> involvedTransitions;
	private Map<Transition, ArrayList<Place>> involvedPlaces;
	private Transition lastFired;
	private IRandomGenerator generator = null;
	private ArrayList<Transition> launchableTransitions;
	private ArrayList<Transition> transitions;
	private ArrayList<Transition> transitionToUpdate;
	
	public void setEngine(NetType simulationType, boolean maxMode, boolean singleMode,
			ArrayList<Transition> transitions, ArrayList<Transition> time_transitions, ArrayList<Place> places) {
		
		for(Transition transition : transitions) {
			ArrayList<Place> placesVector = new ArrayList<Place>();
			for(Node node : transition.getInNodes()) {
				placesVector.add((Place)node);
			}
			for(Node node : transition.getOutNodes()) {
				if(!placesVector.contains(node))
					placesVector.add((Place)node);
			}
			involvedPlaces.put(transition, placesVector);
		}
		
		for(Place place : places) {
			ArrayList<Transition> transitionsVector = new ArrayList<Transition>();
			for(Node node : place.getInNodes()) {
				transitionsVector.add((Transition)node);
			}
			for(Node node : place.getOutNodes()) {
				if(!transitionsVector.contains(node))
					transitionsVector.add((Transition)node);
			}
			involvedTransitions.put(place, transitionsVector);
		}
		
		this.lastFired = null;
		this.launchableTransitions = new ArrayList<Transition>();
		this.transitionToUpdate = new ArrayList<Transition>();
		
		for(Transition trans : transitions)
			transitionToUpdate.add(trans); //na początku: wszystkie
		
		this.transitions = transitions;
		setGenerator(new StandardRandom(System.currentTimeMillis()));
	}
	
	@Override
	public ArrayList<Transition> getTransLaunchList(boolean emptySteps) {
		//empty steps jest w tym symulatorze ignorowane
		launchableTransitions.clear();
		
		for(Transition trans : transitions) {
			if(!trans.isActive())
				continue;
			
			if(transitionToUpdate.contains(trans)) { //tym sprytnym sposobem aktulizacja dotyczy tylko tych tranzycji, dla
				setSPNtimer(trans); //których zmieniły się warunki startowe (tokeny w miejscach)
				transitionToUpdate.remove(trans);
			}
		}

		
		
		//generateLaunchingTransitions();	
		
		lastFired = launchableTransitions.get(0);
		updateUpdateList(lastFired);
		return launchableTransitions; 
	}

	/**
	 * Metoda aktualizuje listę tranzycji, których wartości prawdopodobieństwa odpalenia muszą ulec zmianie z powodu
	 * zmian w miejsach połączonych (IN/OUT) z tymi tranzycjami.
	 * @param lastFiredTransition Transition - ostatnio (aktualnie) odpalona tranzycja, która spowodowała zmiany
	 */
	private void updateUpdateList(Transition lastFiredTransition) {
		ArrayList<Place> changedPlaces = involvedPlaces.get(lastFiredTransition);
		for(Place place : changedPlaces) {
			for(Transition trans : involvedTransitions.get(place)) {
				if(!transitionToUpdate.contains(trans)) {
					transitionToUpdate.add(trans);
				}
			}
		}
	}

	private void setSPNtimer(Transition transition) {
		// TODO Auto-generated method stub
		
	}



	@Override
	public void setNetSimType(NetType simulationType) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setMaxMode(boolean value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setSingleMode(boolean value) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * Ustawia generator liczb pseudo-losowych.
	 * @param IRandomGenerator - generator implementujący interface
	 */
	@Override
	public void setGenerator(IRandomGenerator generator) {
		this.generator = generator;
	}

}
