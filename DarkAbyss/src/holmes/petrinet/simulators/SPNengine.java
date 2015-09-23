package holmes.petrinet.simulators;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import holmes.analyse.InvariantsCalculator;
import holmes.darkgui.GUIManager;
import holmes.petrinet.elements.Node;
import holmes.petrinet.elements.Place;
import holmes.petrinet.elements.Transition;
import holmes.petrinet.simulators.NetSimulator.NetType;

public class SPNengine implements IEngine {
	private GUIManager overlord;
	private Map<Place, ArrayList<Transition>> involvedTransitionsMap;
	private Map<Transition, ArrayList<Place>> involvedPlacesMap;
	private Map<Transition, ArrayList<Place>> prePlacesMap;
	private Transition lastFired;
	private IRandomGenerator generator;
	private ArrayList<Transition> launchableTransitions;
	private ArrayList<Transition> transitions;
	private ArrayList<Transition> transitionToUpdate;
	
	private ArrayList<Transition> affectedTransitions;
	private SimulatorGlobals settings;
	
	HashMap<Place, Integer> placesMap;
	HashMap<Transition, Integer> transitionsMap;
	
	private ArrayList<ArrayList<Integer>> tpIncidenceMatrix;
	
	public void setEngine(NetType simulationType, boolean maxMode, boolean singleMode,
			ArrayList<Transition> transitions, ArrayList<Transition> time_transitions, ArrayList<Place> places) {
		
		this.overlord = GUIManager.getDefaultGUIManager();
		this.lastFired = null;
		this.launchableTransitions = new ArrayList<Transition>();
		this.transitionToUpdate = new ArrayList<Transition>();
		this.involvedPlacesMap = new HashMap<Transition, ArrayList<Place>>();
		this.involvedTransitionsMap = new HashMap<Place, ArrayList<Transition>>();
		this.prePlacesMap = new HashMap<Transition, ArrayList<Place>>();
		this.settings = overlord.simSettings;
		this.placesMap = new HashMap<Place, Integer>();
		this.transitionsMap = new HashMap<Transition, Integer>();
		this.affectedTransitions = new ArrayList<Transition>();
		
		this.transitions = transitions;
		
		if(overlord.simSettings.getGeneratorType() == 1) {
			this.generator = new HighQualityRandom(System.currentTimeMillis());
		} else {
			this.generator = new StandardRandom(System.currentTimeMillis());
		}
		
		for(Transition trans : transitions)
			transitionToUpdate.add(trans); //na początku: wszystkie
		
		for(Transition transition : transitions) {
			ArrayList<Place> placesVector = new ArrayList<Place>();
			for(Node node : transition.getInNodes()) {
				placesVector.add((Place)node);
			}
			ArrayList<Place> prePlacesVector = new ArrayList<Place>(placesVector);
			prePlacesMap.put(transition, prePlacesVector);
			
			for(Node node : transition.getOutNodes()) {
				if(!placesVector.contains(node))
					placesVector.add((Place)node);
			}
			involvedPlacesMap.put(transition, placesVector);
		}
		
		for(Place place : places) {
			ArrayList<Transition> transitionsVector = new ArrayList<Transition>();
			//for(Node node : place.getInNodes()) {
				//transitionsVector.add((Transition)node); //NIE! patrz niżej:
			//}
			//TODO: tylko tranzycje wyjściowe, wejściowe DO miejsca nie mają znaczenia dla funkcji P(t)
			for(Node node : place.getOutNodes()) {
				if(!transitionsVector.contains(node))
					transitionsVector.add((Transition)node);
			}
			involvedTransitionsMap.put(place, transitionsVector);
		}
		
		
		for (int i = 0; i < places.size(); i++) {
			placesMap.put(places.get(i), i);
		}
		for (int i = 0; i < transitions.size(); i++) {
			transitionsMap.put(transitions.get(i), i);
		}
		
		InvariantsCalculator ic = new InvariantsCalculator(true);
		tpIncidenceMatrix = ic.getCMatrix();
		
		for (int t = 0; t < transitions.size(); t++) {
			for (int p = 0; p < places.size(); p++) {
				int value = tpIncidenceMatrix.get(t).get(p);
				value = value < 0 ? -value : value;
				tpIncidenceMatrix.get(t).set(p, value);
			}
		}
	}
	
	/**
	 * Metoda zwraca JEDNĄ tranzycję (model symulacji: SSA), która ma zostać odpalona.
	 * @param emptySteps boolean - ignorowane niejako z definicji
	 * @return ArrayList[Transition] - wektor (jednoelementowy w SSA, zgodność z interfejsem IEngine)
	 */
	@Override
	public ArrayList<Transition> getTransLaunchList(boolean emptySteps) {
		//empty steps jest w tym symulatorze ignorowane
		//launchableTransitions.clear();
		
		affectedTransitions.clear();
		for(Transition trans : transitions) {
			if(!trans.isActive())
				continue;
			
			if(transitionToUpdate.contains(trans)) { //tym sprytnym sposobem aktulizacja dotyczy tylko tych tranzycji, dla
				setProbFunction(trans); //których zmieniły się warunki startowe (tokeny w miejscach)
				transitionToUpdate.remove(trans);
			}
			affectedTransitions.add(trans);
		}
		
		//tu wybieramy tranzycje z najnizszym czasie / najwiekszym P odpalenia
		
		
		double compProbValue = affectedTransitions.get(0).getSSAprobTime(); //TODO: zero?
		ArrayList<Transition> toFire = new ArrayList<Transition>();
		for(Transition trans : affectedTransitions) {
			double nextProbValue = trans.getSSAprobTime();
			
			if(compProbValue == nextProbValue) {
				toFire.add(trans);
			} else {
				if(nextProbValue < compProbValue) {
					toFire.clear();
					toFire.add(trans);
					compProbValue = nextProbValue;
				}
			}
		}
		
		int selected = generator.nextInt(toFire.size());
		lastFired = toFire.get(selected);

		//lastFired = launchableTransitions.get(0);
		updateUpdateList(lastFired);
		launchableTransitions.clear();
		launchableTransitions.add(lastFired);
		return launchableTransitions; 
	}

	/**
	 * Metoda aktualizuje listę tranzycji, których wartości prawdopodobieństwa odpalenia muszą ulec zmianie z powodu
	 * zmian w miejsach połączonych (IN/OUT) z tymi tranzycjami.
	 * @param lastFiredTransition Transition - ostatnio (aktualnie) odpalona tranzycja, która spowodowała zmiany
	 */
	private void updateUpdateList(Transition lastFiredTransition) {
		transitionToUpdate.add(lastFiredTransition); //ważne dla wejściowych, gdyż one nie zostałyby tutaj
		//dodane przez kod poniżej, więc dodajemy ręcznie tym poleceniem
		
		ArrayList<Place> changedPlaces = involvedPlacesMap.get(lastFiredTransition);
		for(Place place : changedPlaces) {
			for(Transition trans : involvedTransitionsMap.get(place)) {
				if(!transitionToUpdate.contains(trans)) {
					transitionToUpdate.add(trans);
				}
			}
		}
	}

	/**
	 * Metoda ustala dla danej tranzycji wartość funkcji prawdopodobieństwa odpalenia w modelu symulacji
	 * SSA, odpowiadająca w ogólności czasowi odpalenia (im mniejszy, tym lepiej).
	 * @param transition Transition - tranzycja
	 */
	private void setProbFunction(Transition transition) {
		long massActionKineticModifier = 1;
		if(settings.isSSAMassAction()) {
			
			ArrayList<Place> prePlaces = prePlacesMap.get(transition);
			if(prePlaces.size() == 0) {
				massActionKineticModifier = 1;
			} else {
				massActionKineticModifier = Long.MAX_VALUE;
				for (Place prePlace : prePlaces) {
					int placeLoc = placesMap.get(prePlace);
					int transLoc = transitionsMap.get(transition);
                    long firingNumber = prePlace.getTokensNumber() / tpIncidenceMatrix.get(transLoc).get(placeLoc);
                    massActionKineticModifier = massActionKineticModifier < firingNumber ? massActionKineticModifier : firingNumber;
                }
			}
		}

		double probTime = -(Math.log(1 - generator.nextDouble()) / (massActionKineticModifier * transition.getFiringRate()) );
		transition.setSSAprobTime(probTime);
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
	 * @param IRandomGenerator - generator implementujący interface IRandomGenerator
	 */
	//@Override
	//public void setGenerator(IRandomGenerator generator) {
	//	this.generator = generator;
	//}

	/**
	 * Zwraca aktualnie ustawiony generator liczb pseudo-losowych.
	 * @return IRandomGenerator
	 */
	@Override
	public IRandomGenerator getGenerator() {
		return this.generator;
	}
}
