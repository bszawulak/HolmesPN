package holmes.petrinet.simulators;

import holmes.darkgui.GUIManager;
import holmes.petrinet.elements.Node;
import holmes.petrinet.elements.Place;
import holmes.petrinet.elements.Transition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SSAengine implements IEngine {
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
    private SimulatorGlobals settings;
    private int settingImmediateMode = 2;
    private boolean settingDetRemoval;
    private HashMap<Place, Integer> placesMap;
    private HashMap<Transition, Integer> transitionsMap;

    private ArrayList<ArrayList<Integer>> tpIncidenceMatrix;


    @Override
    public void setEngine(SimulatorGlobals.SimNetType simulationType, boolean maxMode, boolean singleMode, ArrayList<Transition> transitions, ArrayList<Transition> time_transitions, ArrayList<Place> places) {
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


        this.transitions = transitions;

        if(overlord.simSettings.getGeneratorType() == 1) {
            this.generator = new HighQualityRandom(System.currentTimeMillis());
        } else {
            this.generator = new StandardRandom(System.currentTimeMillis());
        }

        for(Transition transition : transitions) {
            ArrayList<Place> placesVector = new ArrayList<Place>();
            for(Node node : transition.getInputNodes()) {
                placesVector.add((Place)node);
            }
            ArrayList<Place> prePlacesVector = new ArrayList<Place>(placesVector);
            prePlacesMap.put(transition, prePlacesVector);

            for(Node node : transition.getOutputNodes()) {
                if(!placesVector.contains((Place)node))
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
            for(Node node : place.getOutputNodes()) {
                if(!transitionsVector.contains((Transition) node))
                    transitionsVector.add((Transition) node);

            }
            involvedTransitionsMap.put(place, transitionsVector);
        }
    }

    @Override
    public void setNetSimType(SimulatorGlobals.SimNetType simulationType) {

    }

    @Override
    public void setMaxMode(boolean value) {

    }

    @Override
    public void setSingleMode(boolean value) {

    }

    @Override
    public ArrayList<Transition> getTransLaunchList(boolean emptySteps) {
        return null;
    }

    @Override
    public IRandomGenerator getGenerator() {
        return null;
    }
}
