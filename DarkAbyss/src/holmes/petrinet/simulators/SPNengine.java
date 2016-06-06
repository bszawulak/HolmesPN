package holmes.petrinet.simulators;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import holmes.analyse.InvariantsCalculator;
import holmes.darkgui.GUIManager;
import holmes.petrinet.data.SPNdataVector;
import holmes.petrinet.elements.Node;
import holmes.petrinet.elements.Place;
import holmes.petrinet.elements.Transition;
import holmes.petrinet.elements.Transition.StochaticsType;
import holmes.petrinet.simulators.NetSimulator.NetType;

public class SPNengine implements IEngine {
	private GUIManager overlord;
	/** Tylko tranzycje WYJŚCIOWE dla miejsca */
	private Map<Place, ArrayList<Transition>> involvedTransitionsMap;
	/** Mapa miejsc WEJŚCIOWYCH i WYJŚCIOWYCH dla tranzycji */
	private Map<Transition, ArrayList<Place>> involvedPlacesMap;
	/** */
	private Map<Transition, ArrayList<Place>> prePlacesMap;
	private Transition lastFired;
	private IRandomGenerator generator;
	private ArrayList<Transition> transitions;
	
	/** Tranzycja z tej listy zostanie uruchomiona */
	private ArrayList<Transition> launchableTransitions;
	/** Tranzycje z tej listy muszą mieć przeliczone firing rate, dotyczy tranzycji ST */
	private ArrayList<Transition> transitionSTtypeUpdateList;
	/** Tranzycja z tej listy będzie przeniesiona do launchableTransitions dla pętli ST */
	private ArrayList<Transition> activeReadyToFireTransitions;
	/** Tranzycje z tej listy odpalają się jako pierwsze jeśli są aktywne */
	private ArrayList<Transition> immTransitions;
	/** Lista tranzycji IMMEDIATE w trybie sequenced-firing */
	private ArrayList<Transition> immFireListTransitionsOPTION;
	/** Tranzycje z tej listy odpalają się z opóźnieniem */
	private ArrayList<Transition> detTransitions;
	/** Lista tranzycji deterministycznie opóźnionych oczekujących na uruchomienie */
	private ArrayList<Transition> detTransitionsSequence;
	/** W odpowiednim trybie: priorytetowa lista uruchomień tranzycji DET w SPN*/
	private ArrayList<Transition> detPrioritySequence;
	/** Tranzycje z tej listy komplikują algorytm jak jasna cholera */
	private ArrayList<Transition> schTransitions;
	
	private ArrayList<Transition> stochasticTransitions;
	
	private SimulatorGlobals settings;
	private int settingImmediateMode = 2;
	private boolean settingDetRemoval;
	
	private HashMap<Place, Integer> placesMap;
	private HashMap<Transition, Integer> transitionsMap;
	
	private ArrayList<ArrayList<Integer>> tpIncidenceMatrix;
	
	public void setEngine(NetType simulationType, boolean maxMode, boolean singleMode,
			ArrayList<Transition> transitions, ArrayList<Transition> time_transitions, ArrayList<Place> places) {
		
		this.overlord = GUIManager.getDefaultGUIManager();
		this.lastFired = null;
		this.launchableTransitions = new ArrayList<Transition>();
		this.transitionSTtypeUpdateList = new ArrayList<Transition>();
		this.involvedPlacesMap = new HashMap<Transition, ArrayList<Place>>();
		this.involvedTransitionsMap = new HashMap<Place, ArrayList<Transition>>();
		this.prePlacesMap = new HashMap<Transition, ArrayList<Place>>();
		this.settings = overlord.simSettings;
		this.placesMap = new HashMap<Place, Integer>();
		this.transitionsMap = new HashMap<Transition, Integer>();
		this.activeReadyToFireTransitions = new ArrayList<Transition>();
		
		this.immTransitions = new ArrayList<Transition>();
		this.immFireListTransitionsOPTION = new ArrayList<Transition>();
		this.detTransitions = new ArrayList<Transition>();
		this.detTransitionsSequence = new ArrayList<Transition>();
		this.detPrioritySequence = new ArrayList<Transition>();
		this.schTransitions = new ArrayList<Transition>();
		this.stochasticTransitions = new ArrayList<Transition>();
		
		this.transitions = transitions;
		
		if(overlord.simSettings.getGeneratorType() == 1) {
			this.generator = new HighQualityRandom(System.currentTimeMillis());
		} else {
			this.generator = new StandardRandom(System.currentTimeMillis());
		}
		
		for(Transition trans : transitions)
			transitionSTtypeUpdateList.add(trans); //na początku: wszystkie
		
		prepareTransitionsSystem();

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
		
		settingImmediateMode = settings.getSPNimmediateMode();
		settingDetRemoval = settings.isSPNdetRemoveMode();
	}
	
	/**
	 * Przygotowuje wektory danych rozszerzonego modelu SPN
	 */
	private void prepareTransitionsSystem() {
		SPNdataVector SPNvector = overlord.getWorkspace().getProject().accessFiringRatesManager().getCurrentSPNdataVector();

		for (int t = 0; t < transitions.size(); t++) {
			Transition transition = transitions.get(t);
			//transition.setS
			transition.setFiringRate(SPNvector.getFiringRate(t));
			transition.setSPNbox(SPNvector.getSPNtransitionContainer(t));
			transition.setSPNtype(SPNvector.getStochasticType(t));
			
			if(transition.getSPNtype() == StochaticsType.IM)
				immTransitions.add(transition);
			else if(transition.getSPNtype() == StochaticsType.DT)
				detTransitions.add(transition);
			else if(transition.getSPNtype() == StochaticsType.SchT)
				schTransitions.add(transition);
			else
				stochasticTransitions.add(transition);
		}

		//sortuj immediate po priorytecie, prymitywnie bo O(n^2), ale ile ich może być? 10? 100? 1000? wciąż ułamek sekundy działania
		//wspołczesnego komputera, i tylko raz na symulację
		int imSize = immTransitions.size();
		if(imSize > 1) {
			for(int t=0; t<imSize; t++) {
				Transition trans1 = immTransitions.get(t);
				int swapT2 = t;
				for(int t2=t+1; t2<imSize; t2++) {
					if(immTransitions.get(t2).getSPNbox().IM_priority > trans1.getSPNbox().IM_priority) {
						trans1 = immTransitions.get(t2);
						swapT2 = t2;
					}
				}
				if(swapT2 != t)
					Collections.swap(immTransitions, swapT2, t);
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

		if(!settingDetRemoval) { //priorytetowa lista tranzycji opóźnionych, które nie mogły odpalic o czasie
			for(Transition trans : detPrioritySequence) {
				if(trans.isActive()) { //jak już w końcu jest aktywna...
					detPrioritySequence.remove(trans);
					
					lastFired = trans;
					updateSTtransitionsList(lastFired);
					launchableTransitions.clear();
					launchableTransitions.add(lastFired);
					return launchableTransitions; 
				}
			}
			
		}
		
		Transition immFired = immediateFireSubsystem();
		if(immFired != null) {
			lastFired = immFired;
			updateSTtransitionsList(lastFired);
			launchableTransitions.clear();
			launchableTransitions.add(lastFired);
			return launchableTransitions; 
		}
		
		Transition detFired = deterministicDelayFireSubsystem();
		if(detFired != null) {
			lastFired = detFired;
			updateSTtransitionsList(lastFired);
			launchableTransitions.clear();
			launchableTransitions.add(lastFired);
			return launchableTransitions; 
		}
		
		activeReadyToFireTransitions.clear();
		for(Transition trans : stochasticTransitions) {
			if(!trans.isActive())
				continue;
			
			//nie rezerwujemy tokenów (brak wywołania bookTokens, bo i tak tylko 1 zostanie wybrana)
			if(transitionSTtypeUpdateList.contains(trans)) { //tym sprytnym sposobem aktulizacja dotyczy tylko tych tranzycji, dla
				setProbFunction(trans); //których zmieniły się warunki startowe (tokeny w miejscach)
				transitionSTtypeUpdateList.remove(trans);
			}
			activeReadyToFireTransitions.add(trans);
		}
		
		if(activeReadyToFireTransitions.size() > 0) {
			//tu wybieramy tranzycje z najnizszym czasie / najwiekszym P odpalenia
			double compProbValue = activeReadyToFireTransitions.get(0).getSPNprobTime(); //zero?
			ArrayList<Transition> toFire = new ArrayList<Transition>();
			for(Transition trans : activeReadyToFireTransitions) {
				double nextProbValue = trans.getSPNprobTime();
				
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
			updateSTtransitionsList(lastFired);
			launchableTransitions.clear();
			launchableTransitions.add(lastFired);
			if(launchableTransitions.size() > 1) {
				@SuppressWarnings("unused")
				int wtf = 1;
			}
			return launchableTransitions; 
		} else {
			return null;
		}
	}

	/**
	 * Podsystem obsługi tranzycji typu IMMEDIATE w algorytmie.
	 * @return Transition - tranzycja do uruchomienia lub null
	 */
	private Transition immediateFireSubsystem() {
		if(immTransitions.size() == 0)
			return null;
		
		if(settingImmediateMode == 0) { //tylko 1, pierwsza aktywna (najwyższy priorytet)
			shuffleSamePriority();
			
			for(Transition trans : immTransitions) { //są już ustawione po priorytecie
				if(trans.isActive())
					return trans;
			}
			return null;
		} else if(settingImmediateMode == 1) { //po priorytecie, ale wszystkie po kolei
			//sprawdź, czy jest coś na liscie sekwencyjnego uruchamiania:
			if(immFireListTransitionsOPTION.size() > 0) {
				//Transition nowFiring
				Transition youAreFired = immFireListTransitionsOPTION.get(0);
				immFireListTransitionsOPTION.remove(0);
				return youAreFired;
			} else {
				shuffleSamePriority(); //działa na immTransitions
				
				for(Transition trans : immTransitions) { //są już ustawione po priorytecie
					if(!trans.isActive())
						continue;
					//ustaw listę uruchomień wszystkich aktywnych w danym kroku, jedna po drugiej
					trans.bookRequiredTokens();
					immFireListTransitionsOPTION.add(trans);
				}
				for(Transition trans : immFireListTransitionsOPTION) {
					trans.returnBookedTokens(); //zwolnij tokeny
				}
				//uruchom pierwszą
				if(immFireListTransitionsOPTION.size() > 0) {
					Transition youAreFired = immFireListTransitionsOPTION.get(0);
					immFireListTransitionsOPTION.remove(0);
					return youAreFired;
				} else {
					return null;
				}
			}
		} else { //uruchamianie z prawdopodobieństwem określonym przez priorytet
			ArrayList<Transition> roulette = new ArrayList<Transition>();
			int prioritySum = 0;
			for(Transition trans : immTransitions) {
				if(!trans.isActive())
					continue;
				
				trans.bookRequiredTokens(); //rezerwuj tokeny
				roulette.add(trans);
				prioritySum += trans.getSPNbox().IM_priority;
			}
			for(Transition trans : roulette) {
				trans.returnBookedTokens(); //zwolnij tokeny
			}
			
			if(prioritySum == 0 && roulette.size() > 0) {
				@SuppressWarnings("unused")
				int xxx=12;
				//dziwne, nigdy nie powinno wystąpić
				return roulette.get(0);
			}
			int ball = (int) generator.nextLong(prioritySum);
			int cumulativeProbability = 0;
			for (Transition trans : roulette) {
			    cumulativeProbability += trans.getSPNbox().IM_priority;
			    if (ball < cumulativeProbability) {
			        return trans;
			    }
			}
		}
		return null;
	}
	
	/**
	 * Metoda sortuje tranzycji IMMEDIATE o tym samym priorytecie względem siebie. Globalnie, lista wciąż pozostaje
	 * posortowana jako całość.
	 */
	private void shuffleSamePriority() {
		int size = immTransitions.size();
		for(int i=0; i<size; i++) {
			int rangeJ = i;
			for(int j=i+1; j<size; j++) {
				if(immTransitions.get(i).getSPNbox().IM_priority == immTransitions.get(j).getSPNbox().IM_priority) {
					rangeJ++;
				} else 
					continue; //ważne! szybciej.
			}
			
			if(rangeJ != i) {//w tym zakresie mieszamy
				int diff = rangeJ - i;
				
				for(int k=i; k<rangeJ; k++) {
					int r = generator.nextInt(diff+1);
					Collections.swap(immTransitions, k, i+r);//omg...
				}
			}
		}
		
	}

	/**
	 * Dla tranzycji deterministycznie opóźnianych w modelu SPN.
	 * @return Transition - następna do uruchomienia
	 */
	private Transition deterministicDelayFireSubsystem() {
		if(detTransitionsSequence.size() > 0) { //obsługa kolejki
			for(Transition trans : detTransitionsSequence) {
				trans.getSPNbox().tmp_DET_counter--;
				if(trans.getSPNbox().tmp_DET_counter==0) {
					if(trans.isActive()) {
						detTransitionsSequence.remove(trans); //uruchamiaj (w końcu)
						return trans;
					} else {
						detTransitionsSequence.remove(trans); //usuń z listy
						if(!settingDetRemoval) {
							detPrioritySequence.add(trans); //dodaj do listy priorytetowej
						}
					}
				}
			}
		}
		int detSize;
		for(Transition trans : detTransitions) {
			if(detTransitionsSequence.contains(trans))
				continue;
			
			if(!trans.isActive())
				continue;
			
			trans.bookRequiredTokens();
			if((detSize = detTransitionsSequence.size()) > 0) {
				detTransitionsSequence.add(generator.nextInt(detSize), trans); //losowe wstawianie
			} else {
				detTransitionsSequence.add(trans); //każda ma delay min 1, dodaj do kolejki
			}
			trans.getSPNbox().tmp_DET_counter =  trans.getSPNbox().DET_delay;
		}
		
		for(Transition trans : detTransitionsSequence) {
			trans.returnBookedTokens();
		}		
		return null; //dokładnie tak właśnie, rozpatrujemy następne tranzycje, bo na liście detTransitionsSequence
		//są same oczekujące na swój czas uruchomienia
	}


	/**
	 * Metoda aktualizuje listę tranzycji, których wartości prawdopodobieństwa / czasu uruchomienia muszą ulec zmianie z powodu
	 * zmian w miejsach połączonych (IN/OUT) z tymi tranzycjami.
	 * @param lastFiredTransition Transition - ostatnio (aktualnie) odpalona tranzycja, która spowodowała zmiany
	 */
	private void updateSTtransitionsList(Transition lastFiredTransition) {
		if(lastFiredTransition.getSPNtype() == StochaticsType.ST)
			transitionSTtypeUpdateList.add(lastFiredTransition); //ważne dla wejściowych, gdyż one nie zostałyby tutaj
			//dodane przez kod poniżej, więc dodajemy ręcznie tym poleceniem
		

		ArrayList<Place> changedPlaces = involvedPlacesMap.get(lastFiredTransition);
		for(Place place : changedPlaces) {
			for(Transition trans : involvedTransitionsMap.get(place)) {
				if(!transitionSTtypeUpdateList.contains(trans) && trans.getSPNtype() == StochaticsType.ST) {
					transitionSTtypeUpdateList.add(trans);
				}
			}
		}
		
		for(Transition t : transitions) {
			if(t.getOutArcs().size() == 0) {
				if(!transitionSTtypeUpdateList.contains(t) && t.getSPNtype() == StochaticsType.ST) {
					transitionSTtypeUpdateList.add(t);
				}
			}
			if(t.getInArcs().size() == 0) {
				if(!transitionSTtypeUpdateList.contains(t) && t.getSPNtype() == StochaticsType.ST) {
					transitionSTtypeUpdateList.add(t);
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
			
			if(transition.getID() == 68) {
				int y=1;
				y=2;
			}
			
			if(prePlaces.size() == 0) {
				massActionKineticModifier = 1;
			} else {
				massActionKineticModifier = Long.MAX_VALUE;
				if(prePlaces.isEmpty()) {
					int x=1;
				}
				
				for (Place prePlace : prePlaces) {
					int placeLoc = placesMap.get(prePlace);
					int transLoc = transitionsMap.get(transition);
					int arc = tpIncidenceMatrix.get(transLoc).get(placeLoc);
					if(arc==0) {
						arc = 1; //readarc
						arc = prePlace.getTokensNumber();
					}
                    long firingNumber = prePlace.getTokensNumber() / arc;
                    massActionKineticModifier = massActionKineticModifier < firingNumber ? massActionKineticModifier : firingNumber;
                }
			}
		}
		double denominator = (massActionKineticModifier * transition.getFiringRate());
		if(denominator == 0)
			denominator = 1e-23;
		
		double probTime = -(Math.log(1 - generator.nextDouble()) / denominator );
		transition.setSPNprobTime(probTime);
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
