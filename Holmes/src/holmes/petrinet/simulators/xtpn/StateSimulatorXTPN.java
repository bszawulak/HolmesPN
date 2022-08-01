package holmes.petrinet.simulators.xtpn;

import holmes.darkgui.GUIManager;
import holmes.petrinet.elements.*;
import holmes.petrinet.functions.FunctionsTools;
import holmes.petrinet.simulators.QuickSimTools;
import holmes.petrinet.simulators.SimulatorGlobals;
import holmes.windows.managers.ssim.HolmesSim;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Klasa symulatora XTPN.
 */
public class StateSimulatorXTPN {
    private GUIManager overlord;
    private SimulatorXTPN engineXTPN;
    private SimulatorGlobals sg;

    //wewnętrzne obiekty danych symulatora:
    private ArrayList<Transition> transitions;
    private ArrayList<Place> places;
    ArrayList<ArrayList<SimulatorXTPN.NextXTPNstep>> nextXTPNsteps;
    SimulatorXTPN.NextXTPNstep infoNode;
    ArrayList<TransitionXTPN> consumingTokensTransitionsXTPN = new ArrayList<>();
    ArrayList<TransitionXTPN> consumingTokensTransitionsClassical = new ArrayList<>();
    ArrayList<TransitionXTPN> producingTokensTransitionsAll = new ArrayList<>();
    ArrayList<ArrayList<TransitionXTPN>> transitionsAfterSubtracting = new ArrayList<>();


    private int simulationType;	//aktywny tryb symulacji
    private boolean readyToSimulate = false;
    private boolean terminate = false;

    public double simStepsCounter = 0;
    public double simTimeCounter = 0;


    //okna wywołyjące i inne:
    public JProgressBar progressBar;//pasek postępu symulacji
    private HolmesSim boss;	//okno nadrzędne symulatora
    private QuickSimTools quickSim;

    /**
     * Główny konstruktor obiektu klasy StateSimulator.
     */
    public StateSimulatorXTPN() {
        engineXTPN = new SimulatorXTPN();
        overlord = GUIManager.getDefaultGUIManager();
        sg = overlord.simSettings;
    }


    /**
     * Metoda ta musi być wywołana przed każdym startem symulatora. Inicjalizuje początkowe struktury
     * danych dla symulatora.
     * @param ownSettings SimulatorGlobals - jeśli powyższej jest = false, to stąd są brane parametry
     * @return boolean - true, jeśli wszystko się udało
     */
    public boolean initiateSim(SimulatorGlobals ownSettings) {
        transitions = overlord.getWorkspace().getProject().getTransitions();
        places = overlord.getWorkspace().getProject().getPlaces();
        if(transitions == null || places == null) {
            readyToSimulate = false;
            return readyToSimulate;
        }
        if(!(transitions.size() > 0 && places.size() > 0)) {
            readyToSimulate = false;
            return readyToSimulate;
        }
        engineXTPN.setEngine(SimulatorGlobals.SimNetType.XTPN, transitions, places);


        /*
        placesData = new ArrayList<ArrayList<Integer>>();
        placesAvgData = new ArrayList<Double>();
        placesTotalData = new ArrayList<Long>();
        transitionsData = new ArrayList<ArrayList<Integer>>();
        transitionsTotalFiring = new ArrayList<Integer>();
        transitionsAvgData = new ArrayList<Double>();
        for(int t=0; t<transitions.size(); t++) {
            transitionsTotalFiring.add(0);
        }
        for(int p=0; p<places.size(); p++) {
            placesAvgData.add(0.0);
            placesTotalData.add(0L);
        }
         */

        readyToSimulate = true;
        return readyToSimulate;
    }

    /**
     * Metoda ustawiająca obiekty dla symulacji w osobnym wątku.
     * @param simulationType int - typ symulacji: 1 - Node info window 2-qSim 3 - ...
     * @param blackBox Object... - zależy od trybu powyżej
     */
    public void setThreadDetails(int simulationType, Object... blackBox) {
        this.simulationType = simulationType;
        if(simulationType == 1) { //standardowy tryb symulacji
            //this.boss = (HolmesSim)blackBox[0];
            //this.progressBar = (JProgressBar)blackBox[1];
        } else if(simulationType == 2) { //QuickSimReps
            //this.progressBar = (JProgressBar)blackBox[0];
            //this.quickSim = (QuickSimTools)blackBox[1];
        } else if(simulationType == 3) { //QuickSimNoReps
            //this.progressBar = (JProgressBar)blackBox[0];
            //this.quickSim = (QuickSimTools)blackBox[1];
        }
    }

    /**
     * Uruchamiania zdalnie, zakłada, że wszystko co potrzebne zostało już ustawione za pomocą setThreadDetails(...)
     */
    public void run() {
        this.terminate = false;
        if(simulationType == 1) {
            simulateNet();
            //boss.completeSimulationProcedures();
        } else if(simulationType == 2) {
            //quickSimGatherData();
            //quickSim.finishedStatsData(quickSimAllStats, transitions, places);
        } else if(simulationType == 3) {
            //quickSimGatherDataNoReps();
            //quickSim.finishedStatsData(quickSimAllStats, transitions, places);
        }
        this.terminate = false;
    }

    /**
     * Ustawia status wymuszonego kończenia symulacji.
     * @param val boolean - true, jeśli symulator ma zakończyć działanie
     */
    public void setCancelStatus(boolean val) {
        this.terminate = val;
        if(val)
            readyToSimulate = false;
    }


    /**
     * Metoda pracująca w wątku. Metoda symuluje podaną liczbę kroków sieci Petriego dla wybranego wcześniej trybu, tj.
     * jeśli maximumMode = true, wtedy każda aktywna tranzycja musi się uruchomić.
     */
    public void simulateNet() {
        if(!readyToSimulate) {
            JOptionPane.showMessageDialog(null,"XTPN Simulation cannot start, engine initialization failed.",
                    "Simulation problem",JOptionPane.ERROR_MESSAGE);
            return;
        }


        //tutaj zapisujemy p-stan
        prepareNetM0();

        for(int i=0; i<sg.simSteps_XTPN; i++) {
            if(terminate)
                break;

            ArrayList<SimulatorXTPN.NextXTPNstep> classicalInputTransitions = engineXTPN.revalidateNetState();
            nextXTPNsteps = engineXTPN.computeNextState();
            nextXTPNsteps.set(4, classicalInputTransitions);
            nextXTPNsteps.get(6).get(0).changeType += classicalInputTransitions.size();
            infoNode = nextXTPNsteps.get(6).get(0);
            if(infoNode.changeType == 0) {
                terminate = true;
                break;
            }
            engineXTPN.updateNetTime(infoNode.timeToChange);
            simStepsCounter++;
            simTimeCounter += infoNode.timeToChange;

            consumingTokensTransitionsXTPN = engineXTPN.returnConsumingTransXTPNVector(nextXTPNsteps);
            consumingTokensTransitionsClassical = engineXTPN.returnConsumingTransClassicalVector(nextXTPNsteps);
            producingTokensTransitionsAll = engineXTPN.returnProducingTransVector(nextXTPNsteps);

            if(consumingTokensTransitionsXTPN.size() > 0 || consumingTokensTransitionsClassical.size() > 0) {
                //faza zabierana tokenów, czyli uruchamianie tranzycji gdy timeAlfa = tauAlfa
                transitionsAfterSubtracting = prepareSubtractPhase();
                engineXTPN.endSubtractPhase(transitionsAfterSubtracting);
            }

            if(producingTokensTransitionsAll.size() > 0) { //tylko produkcja tokenów
                engineXTPN.endProductionPhase(producingTokensTransitionsAll);
            }

            consumingTokensTransitionsXTPN.clear();
            consumingTokensTransitionsClassical.clear();
            producingTokensTransitionsAll.clear();
            nextXTPNsteps.clear();

            //zbierz informacje o tokenach w miejsach:
        }



        overlord.log("Simulation ended. Restoring zero marking.", "text", true);
        readyToSimulate = false;

        //restore p-state here:
        //restoreInternalMarkingZero();
    }

    public ArrayList<ArrayList<TransitionXTPN>> prepareSubtractPhase() {
        ArrayList<ArrayList<TransitionXTPN>> launchedTransitions = new ArrayList<>();
        ArrayList<TransitionXTPN> launchedXTPN = new ArrayList<>();
        ArrayList<TransitionXTPN> launchedClassical = new ArrayList<>();
        ArrayList<Arc> arcs;

        //dla : consumingTokensTransitionsXTPN
        //oraz osobno: consumingTokensTransitionsClassical

        for (TransitionXTPN transition : consumingTokensTransitionsXTPN) { //lista tych, które zabierają tokeny
            if(transition.isActiveTransitionXTPN(sg.getCalculationsAccuracy())) { //jeżeli jest aktywna, to zabieramy tokeny
                //transition.setLaunching(true);
                arcs = transition.getInArcs();
                for (Arc arc : arcs) {
                    //arc.setSimulationForwardDirection(true); //zawsze dla tego symulatora (nie działamy wstecz)
                    //arc.setTransportingTokens(true);
                    PlaceXTPN place = (PlaceXTPN) arc.getStartNode(); //miejsce, z którego zabieramy
                    if(arc.getArcType() == Arc.TypeOfArc.INHIBITOR) {
                        //arc.setTransportingTokens(false);
                    }  else { //teraz określamy ile zabrać
                        int weight = arc.getWeight();
                        if(transition.isFunctional()) {
                            weight = FunctionsTools.getFunctionalArcWeight(transition, arc, place);
                        }
                        place.removeTokensForProduction_XTPN(weight, 0, engineXTPN.getGenerator());
                    }
                }
                launchedXTPN.add(transition);
            } else {
                transition.deactivateTransitionXTPN(false);
                transition.setActivationStatusXTPN(false);
                transition.setProductionStatus_xTPN(false);
                producingTokensTransitionsAll.remove(transition);
            }
        }

        //mechanizm bezpieczeństwa, COŚ musi się uruchomić w tym kroku, jeżeli tutaj jesteśmy:
        boolean mustFireSOMETHING = false;
        int activatedXTPN = consumingTokensTransitionsXTPN.size();
        int producedXTPN = producingTokensTransitionsAll.size() - 1;
        //minus 1, bo ta sama tranzycja w consumingTokensTransitionsClassical na pewno jest w producingTokensTransitionsAll
        //skoro to klasyczna (NAWET, gdy jest wejściowa)
        double time = infoNode.timeToChange;

        if(activatedXTPN == 0 && producedXTPN == 0 && time == 0.0) { //XTPN components - dead
            mustFireSOMETHING = true;
        }

        int fireClassSoFar = 0;
        for (Iterator<TransitionXTPN> iteratorTrans = consumingTokensTransitionsClassical.iterator(); iteratorTrans.hasNext(); ) {
            TransitionXTPN transition = iteratorTrans.next();

            if(mustFireSOMETHING && fireClassSoFar == 0) {
                //czyli: MUSIMY coś uruchomić i jeszcze NIC nie uruchomiliśmy:
                //NIC... najważniejszy, że else zostanie zignorowany, potem już możemy być uczciwi
            } else { //tu bawimy się w bycie uczciwym (chyba, że tranzycja immediate)
                if ((engineXTPN.getGenerator().nextInt(100) < 50) && !transition.isImmediateXTPN()) {
                    //non immediate classical: 50% chances to fire
                    transition.deactivateTransitionXTPN(false);
                    producingTokensTransitionsAll.remove(transition);
                    iteratorTrans.remove();
                    continue;
                }
            }

            if(transition.isActiveTransitionXTPN(sg.getCalculationsAccuracy())) { //jeżeli jest aktywna, to zabieramy tokeny
                fireClassSoFar++;
                transition.setLaunching(true);
                arcs = transition.getInArcs();
                for (Arc arc : arcs) {
                    //arc.setSimulationForwardDirection(true); //zawsze dla tego symulatora
                    //arc.setTransportingTokens(true);
                    PlaceXTPN place = (PlaceXTPN) arc.getStartNode(); //miejsce, z którego zabieramy
                    if(arc.getArcType() == Arc.TypeOfArc.INHIBITOR) {
                        //arc.setTransportingTokens(false);
                    }  else { //teraz określamy ile
                        int weight = arc.getWeight();
                        if(transition.isFunctional()) {
                            weight = FunctionsTools.getFunctionalArcWeight(transition, arc, place);
                        }
                        place.removeTokensForProduction_XTPN(weight, 0, engineXTPN.getGenerator());
                    }
                }
                launchedClassical.add(transition);
            } else {
                transition.deactivateTransitionXTPN(false);
            }
        }
        launchedTransitions.add(launchedXTPN);
        launchedTransitions.add(launchedClassical);
        return launchedTransitions;
    }


    /**
     * Metoda przygotowuje backup stanu sieci
     */
    public void prepareNetM0() {
        //overlord.getWorkspace().getProject().restoreMarkingZeroFast(transitions);
        //saveInternalMarkingZero(); //zapis aktualnego stanu jako m0
        //clearTransitionsValues();
    }

    /**
     * Metoda ta przywraca stan sieci przed rozpoczęciem symulacji. Liczba tokenów jest przywracana
     * z wektora danych pamiętających ostatni backup, tranzycje są resetowane wewnętrznie.
     */
    public void restoreInternalMarkingZero() {
        // state manager

        //oraz reset tranzycji
        //clearTransitionsValues();
    }
}
