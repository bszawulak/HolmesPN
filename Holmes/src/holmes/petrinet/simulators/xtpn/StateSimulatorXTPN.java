package holmes.petrinet.simulators.xtpn;

import holmes.darkgui.GUIManager;
import holmes.petrinet.data.MultisetM;
import holmes.petrinet.elements.*;
import holmes.petrinet.functions.FunctionsTools;
import holmes.petrinet.simulators.QuickSimTools;
import holmes.petrinet.simulators.SimulatorGlobals;
import holmes.windows.xtpn.HolmesSimXTPN;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;

/**
 * Klasa symulatora XTPN.
 */
public class StateSimulatorXTPN implements Runnable {
    private GUIManager overlord;
    private SimulatorEngineXTPN engineXTPN;
    private SimulatorGlobals sg;

    //wewnętrzne obiekty danych symulatora:
    private ArrayList<TransitionXTPN> transitions = new ArrayList<>();
    private ArrayList<PlaceXTPN> places = new ArrayList<>();

    private MultisetM backupState_MultisetM = new MultisetM();
    ArrayList<ArrayList<SimulatorEngineXTPN.NextXTPNstep>> nextXTPNsteps;
    SimulatorEngineXTPN.NextXTPNstep infoNode;
    ArrayList<TransitionXTPN> consumingTokensTransitionsXTPN = new ArrayList<>();
    ArrayList<TransitionXTPN> consumingTokensTransitionsClassical = new ArrayList<>();
    ArrayList<TransitionXTPN> producingTokensTransitionsAll = new ArrayList<>();
    ArrayList<ArrayList<TransitionXTPN>> transitionsAfterSubtracting = new ArrayList<>();


    private int simulationType;    //aktywny tryb symulacji
    private boolean readyToSimulate = false;
    private boolean terminate = false;
    public double simStepsCounter = 0;
    public double simTimeCounter = 0;
    public double simLastTimeChange = 0.0;


    //okna wywołyjące i inne:
    public JProgressBar progressBar;//pasek postępu symulacji
    private HolmesSimXTPN boss;    //okno nadrzędne symulatora
    private QuickSimTools quickSim;

    /**
     * Główny konstruktor obiektu klasy StateSimulator.
     */
    public StateSimulatorXTPN() {
        engineXTPN = new SimulatorEngineXTPN();
        overlord = GUIManager.getDefaultGUIManager();
        sg = overlord.simSettings;
    }

    public static final class TransitionStepStats {
        public double inactive = 0;
        public double active = 0;
        public double producing = 0;
        public double fired = 0;
    }

    /**
     * Metoda ta musi być wywołana przed każdym startem symulatora. Inicjalizuje początkowe struktury
     * danych dla symulatora
     * @param ownSettings (<b>SimulatorGlobals</b>) parametry symulacji.
     * @return (< b > boolean < / b >) - true, jeśli wszystko się udało.
     */
    public boolean initiateSim(SimulatorGlobals ownSettings) {
        transitions.clear();
        for (Transition trans : overlord.getWorkspace().getProject().getTransitions()) {
            if (!(trans instanceof TransitionXTPN)) {
                transitions.clear();
                overlord.log("Error, non-XTPN transitions found in list sent into SimulatorXTPN!", "error", true);
                return false;
            }
            transitions.add((TransitionXTPN) trans);
        }
        for (Place place : overlord.getWorkspace().getProject().getPlaces()) {
            if (!(place instanceof PlaceXTPN)) {
                transitions.clear();
                overlord.log("Error, non-XTPN places found in list sent into SimulatorXTPN!", "error", true);
                return false;
            }
            places.add((PlaceXTPN) place);
        }

        if (transitions == null || places == null) {
            readyToSimulate = false;
            return readyToSimulate;
        }
        if (!(transitions.size() > 0 && places.size() > 0)) {
            readyToSimulate = false;
            return readyToSimulate;
        }

        engineXTPN.setEngine(ownSettings.getNetType(), transitions, places);
        readyToSimulate = true;
        return readyToSimulate;
    }

    public void restartEngine() {
        terminate = false;
        simStepsCounter = 0;
        simTimeCounter = 0.0;
        simLastTimeChange = 0.0;
        readyToSimulate = true;
    }

    /**
     * Metoda ustawiająca obiekty dla symulacji w osobnym wątku.
     * @param simulationType int - typ symulacji: 1 - Node info window 2-qSim 3 - ...
     * @param blackBox       Object... - zależy od trybu powyżej
     */
    public void setThreadDetails(int simulationType, Object... blackBox) {
        this.simulationType = simulationType;
        if (simulationType == 1) { //qSim
            this.quickSim = (QuickSimTools) blackBox[0];
            this.sg = (SimulatorGlobals) blackBox[1];
            this.progressBar = (JProgressBar) blackBox[2];
        } else if (simulationType == 2) { //qSim + repetitions
            this.quickSim = (QuickSimTools) blackBox[0];
            this.sg = (SimulatorGlobals) blackBox[1];
            this.progressBar = (JProgressBar) blackBox[2];
        } else if (simulationType == 3) { //qSim + repetitions + knockout
            this.quickSim = (QuickSimTools) blackBox[0];
            this.sg = (SimulatorGlobals) blackBox[1];
            this.progressBar = (JProgressBar) blackBox[2];
        } else if (simulationType == 4) { //qSim + repetitions + knockout
            this.boss = (HolmesSimXTPN) blackBox[0];
            this.sg = (SimulatorGlobals) blackBox[1];
            this.progressBar = (JProgressBar) blackBox[2];
        }
    }

    /**
     * Uruchamiania zdalnie, zakłada, że wszystko co potrzebne zostało już ustawione za pomocą setThreadDetails(...)
     */
    public void run() {
        this.terminate = false;
        if (simulationType == 1) { //qSim
            StateSimDataContainer result = quickSimGatherData();
            quickSim.finishedStatsDataXTPN(result, transitions, places);
        } else if (simulationType == 2) { //qSim + repetitions
            StateSimDataContainer result = quickSimGatherDataRepetitions(false, 0);
            quickSim.finishedStatsDataXTPN(result, transitions, places);
        } else if (simulationType == 3) { //qSim + repetitions + knockout
            ArrayList<StateSimDataContainer> result = quickSimKnockout();
            quickSim.finishedStatsDataXTPN_Knockout(result, transitions, places);
        } else if (simulationType == 4) { //state simulator XTPN
            StateSimDataContainer result = simulateNet();
            boss.completeSimulationProcedures_Mk1(result);
        }
        this.terminate = false;
    }

    /**
     * Ustawia status wymuszonego kończenia symulacji.
     * @param status (<b>boolean</b>) true, jeśli symulator ma zakończyć działanie.
     */
    public void setCancelStatus(boolean status) {
        this.terminate = status;
        if (status)
            readyToSimulate = false;
    }

    /**
     * Główna metoda przetwarzająca krok symulacji oraz zapisująca historię stanów tranzycji.
     * @return (< b > ArrayList[ArrayList[Double]] < / b >) - dwa wektory: placesTokensVector oraz placesTimeVector.
     */
    private ArrayList<ArrayList<Double>> processSimStep() {
        ArrayList<Double> placesTokensVector = new ArrayList<>();
        ArrayList<Double> placesTimeVector = new ArrayList<>();
        ArrayList<ArrayList<Double>> placesResultVectors = new ArrayList<>();
        placesResultVectors.add(placesTokensVector);
        placesResultVectors.add(placesTimeVector);

        ArrayList<SimulatorEngineXTPN.NextXTPNstep> classicalInputTransitions = engineXTPN.revalidateNetState();
        nextXTPNsteps = engineXTPN.computeNextState();
        nextXTPNsteps.set(4, classicalInputTransitions);
        nextXTPNsteps.get(6).get(0).changeType += classicalInputTransitions.size();
        infoNode = nextXTPNsteps.get(6).get(0);
        if (infoNode.changeType == 0) {
            terminate = true;
        }

        engineXTPN.updateNetTime(infoNode.timeToChange);
        simStepsCounter++;
        double oldTIme = simTimeCounter;
        simTimeCounter += infoNode.timeToChange;

        consumingTokensTransitionsXTPN = engineXTPN.returnConsumingTransXTPNVector(nextXTPNsteps);
        consumingTokensTransitionsClassical = engineXTPN.returnConsumingTransClassicalVector(nextXTPNsteps);
        producingTokensTransitionsAll = engineXTPN.returnProducingTransVector(nextXTPNsteps);

        if (consumingTokensTransitionsXTPN.size() > 0 || consumingTokensTransitionsClassical.size() > 0) {
            //faza zabierana tokenów, czyli uruchamianie tranzycji:
            transitionsAfterSubtracting = consumeTokensSubphase();
            //wszystko co trafia poniżej dostaje status production(true), tranzycje XTPN - nowy timer beta:
            engineXTPN.endSubtractPhase(transitionsAfterSubtracting);
        }

        if (producingTokensTransitionsAll.size() > 0) { //tylko produkcja tokenów
            engineXTPN.endProductionPhase(producingTokensTransitionsAll);
        }

        //STATYSTYKI (PAMIĘTANE W OBIEKCIE TRANZYCJI / MIEJSCA):
        ArrayList<TransitionXTPN> stateChangedTransitions = new ArrayList<>();

        for (TransitionXTPN trans : producingTokensTransitionsAll) {
            trans.simHistoryXTPN.simFiredState++; //wyprodukowały coś w tym kroku, state=3
            if (trans.isBetaModeActive()) { // wcześniej była w fazie produkcji (DPN/XTPN)
                trans.simHistoryXTPN.simProductionState++;
                trans.simHistoryXTPN.simProductionTime += infoNode.timeToChange;

                if (trans.simHistoryXTPN.storeHistory)
                    trans.simHistoryXTPN.addHistoryMoment(2.0, infoNode.timeToChange);
            } else if (trans.isAlphaModeActive()) { // wcześniej była w fazie aktywności (TPN)
                trans.simHistoryXTPN.simActiveState++;
                trans.simHistoryXTPN.simActiveTime += infoNode.timeToChange;

                if (trans.simHistoryXTPN.storeHistory)
                    trans.simHistoryXTPN.addHistoryMoment(1.0, infoNode.timeToChange);
            } else { // klasyczna, ale żeby odpalić musiała być przecież aktywna...
                trans.simHistoryXTPN.simActiveState++;
                trans.simHistoryXTPN.simActiveTime += infoNode.timeToChange;

                if (trans.simHistoryXTPN.storeHistory)
                    trans.simHistoryXTPN.addHistoryMoment(1.0, infoNode.timeToChange);
            }
            if (trans.simHistoryXTPN.storeHistory)
                trans.simHistoryXTPN.addHistoryMoment(3.0, infoNode.timeToChange);
            stateChangedTransitions.add(trans);
        }
        if (transitionsAfterSubtracting.size() == 3) {
            for (TransitionXTPN trans : transitionsAfterSubtracting.get(2)) { //deaktywowane przed pobraniem tokenów
                trans.simHistoryXTPN.simActiveState++; //deaktywowana, ale do tego momentu musiała być  aktywna
                trans.simHistoryXTPN.simActiveTime += infoNode.timeToChange; // musiała być wcześniej aktywna

                stateChangedTransitions.add(trans);

                if (trans.simHistoryXTPN.storeHistory)
                    trans.simHistoryXTPN.addHistoryMoment(1.0, infoNode.timeToChange);
            }
            for (TransitionXTPN trans : transitionsAfterSubtracting.get(0)) {
                //wciąż produkują (XTPN), get(1) to klasyczne i one już były przerobione dla producingTokensTransitionsAll
                if (producingTokensTransitionsAll.contains(trans))
                    continue;

                if (trans.isBetaModeActive()) { //DPN? XTPN?
                    trans.simHistoryXTPN.simActiveState++; //rozpoczęła produkcję, ale do tego momentu musiała być aktywna
                    trans.simHistoryXTPN.simActiveTime += infoNode.timeToChange; // musiała być do tego momentu aktywna

                    if (trans.simHistoryXTPN.storeHistory)
                        trans.simHistoryXTPN.addHistoryMoment(1.0, infoNode.timeToChange);
                }
                stateChangedTransitions.add(trans);
            }
        }


        for (TransitionXTPN trans : transitions) {
            if (stateChangedTransitions.contains(trans)) //już przetworzona
                continue;

            if (trans.isActivated_xTPN()) { //jeśli aktywna:
                trans.simHistoryXTPN.simActiveState++;
                trans.simHistoryXTPN.simActiveTime += infoNode.timeToChange;

                if (trans.simHistoryXTPN.storeHistory)
                    trans.simHistoryXTPN.addHistoryMoment(1.0, infoNode.timeToChange);
            } else if (trans.isProducing_xTPN()) { //jeśli produkująca
                trans.simHistoryXTPN.simProductionState++;
                trans.simHistoryXTPN.simProductionTime += infoNode.timeToChange;

                if (trans.simHistoryXTPN.storeHistory)
                    trans.simHistoryXTPN.addHistoryMoment(2.0, infoNode.timeToChange);
            } else { //nieaktywna:
                trans.simHistoryXTPN.simInactiveState++;
                trans.simHistoryXTPN.simInactiveTime += infoNode.timeToChange;

                if (trans.simHistoryXTPN.storeHistory)
                    trans.simHistoryXTPN.addHistoryMoment(0.0, infoNode.timeToChange);
            }
        }

        for (PlaceXTPN place : places) {
            if (place.isGammaModeActive()) {
                placesTokensVector.add((double) place.accessMultiset().size());
            } else {
                placesTokensVector.add((double) place.getTokensNumber());
            }
        }
        placesTimeVector.add(simTimeCounter);
        //KONIEC LICZENIA STATYSTYK

        consumingTokensTransitionsXTPN.clear();
        consumingTokensTransitionsClassical.clear();
        producingTokensTransitionsAll.clear();
        transitionsAfterSubtracting.clear();
        nextXTPNsteps.clear();

        simLastTimeChange = infoNode.timeToChange;

        return placesResultVectors;
    }

    /**
     * W zasadzie jest to główna metoda ''odpalania'' tranzycji. Te które są aktywne pobierają tokeny i
     * są umieszczane na listach launchedXTPN oraz (potem) launchedClassical. Te tranzycje dla których
     * zabrakło tokenów są deaktywowane oraz umieszczane w trzeciej tablicy: deactivated.
     * @return (< b > ArrayList[ArrayList[TransitionXTPN]] < / b >) - trzy listy:
     * <b>launchedXTPN</b> (.get(0)), <b>launchedClassical</b> (.get(1)) oraz <b>deactivated</b> (.get(2)).
     */
    public ArrayList<ArrayList<TransitionXTPN>> consumeTokensSubphase() {
        ArrayList<ArrayList<TransitionXTPN>> launchedTransitions = new ArrayList<>();
        ArrayList<TransitionXTPN> launchedXTPN = new ArrayList<>();
        ArrayList<TransitionXTPN> launchedClassical = new ArrayList<>();
        ArrayList<TransitionXTPN> deactivated = new ArrayList<>();
        ArrayList<Arc> arcs;

        //dla : consumingTokensTransitionsXTPN
        //oraz osobno: consumingTokensTransitionsClassical

        for (TransitionXTPN transition : consumingTokensTransitionsXTPN) { //lista tych, które zabierają tokeny
            if (transition.isActiveTransitionXTPN(sg.getCalculationsAccuracy())) { //jeżeli jest aktywna, to zabieramy tokeny
                //transition.setLaunching(true);
                arcs = transition.getInArcs();
                for (Arc arc : arcs) {
                    //arc.setSimulationForwardDirection(true); //zawsze dla tego symulatora (nie działamy wstecz)
                    //arc.setTransportingTokens(true);
                    PlaceXTPN place = (PlaceXTPN) arc.getStartNode(); //miejsce, z którego zabieramy
                    if (arc.getArcType() == Arc.TypeOfArc.INHIBITOR) {
                        //arc.setTransportingTokens(false);
                    } else { //teraz określamy ile zabrać
                        int weight = arc.getWeight();
                        if (transition.fpnExtension.isFunctional()) {
                            weight = FunctionsTools.getFunctionalArcWeight(transition, arc, place);
                        }
                        place.removeTokensForProduction_XTPN(weight, 0, engineXTPN.getGenerator());
                    }
                }
                launchedXTPN.add(transition);
            } else {
                deactivated.add(transition);

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

        if (activatedXTPN == 0 && producedXTPN == 0 && time == 0.0) { //XTPN components - dead
            mustFireSOMETHING = true;
        }

        int fireClassSoFar = 0;
        for (Iterator<TransitionXTPN> iteratorTrans = consumingTokensTransitionsClassical.iterator(); iteratorTrans.hasNext(); ) {
            TransitionXTPN transition = iteratorTrans.next();

            if (mustFireSOMETHING && fireClassSoFar == 0) {
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
            if (transition.isActiveTransitionXTPN(sg.getCalculationsAccuracy())) { //jeżeli jest aktywna, to zabieramy tokeny
                fireClassSoFar++;
                transition.setLaunching(true);
                arcs = transition.getInArcs();
                for (Arc arc : arcs) {
                    //arc.setSimulationForwardDirection(true); //zawsze dla tego symulatora
                    //arc.setTransportingTokens(true);
                    PlaceXTPN place = (PlaceXTPN) arc.getStartNode(); //miejsce, z którego zabieramy
                    if (arc.getArcType() == Arc.TypeOfArc.INHIBITOR) {
                        //arc.setTransportingTokens(false);
                    } else { //teraz określamy ile
                        int weight = arc.getWeight();
                        if (transition.fpnExtension.isFunctional()) {
                            weight = FunctionsTools.getFunctionalArcWeight(transition, arc, place);
                        }
                        place.removeTokensForProduction_XTPN(weight, 0, engineXTPN.getGenerator());
                    }
                }
                launchedClassical.add(transition);
            } else {
                deactivated.add(transition);
                transition.deactivateTransitionXTPN(false);
                producingTokensTransitionsAll.remove(transition);
            }
        }
        launchedTransitions.add(launchedXTPN);
        launchedTransitions.add(launchedClassical);
        launchedTransitions.add(deactivated);
        return launchedTransitions;
    }

    /**
     * Metoda przygotowuje backup stanu sieci przed symulacją.
     */
    public void createBackupState() {
        backupState_MultisetM.clearMultiset();
        for (PlaceXTPN place : places) {
            if ((place.isGammaModeActive())) {
                backupState_MultisetM.addMultiset_K_toMultiset_M(new ArrayList<>(place.accessMultiset()), 1);
            } else {
                int tokens = place.getTokensNumber();
                ArrayList<Double> fakeMultiset = new ArrayList<>();
                fakeMultiset.add((double) tokens);
                backupState_MultisetM.addMultiset_K_toMultiset_M(fakeMultiset, 0);
            }
        }
        for (TransitionXTPN trans : transitions) {
            trans.deactivateTransitionXTPN(false);
            trans.simHistoryXTPN.cleanHistoryVectors();
        }
    }

    /**
     * Metoda ta przywraca stan sieci przed rozpoczęciem symulacji.
     */
    public void restoreInternalMarkingZero() {
        for (int placeIndex = 0; placeIndex < places.size(); placeIndex++) {
            PlaceXTPN place = places.get(placeIndex);
            if (backupState_MultisetM.isPlaceStoredAsGammaActive(placeIndex)) {
                place.setGammaModeStatus(true);
                place.replaceMultiset(new ArrayList<>(backupState_MultisetM.accessMultiset_K(placeIndex)));
                place.setTokensNumber(backupState_MultisetM.accessMultiset_K(placeIndex).size());
            } else { //jeśli w managerze miejsce jest przechowywane jako klasyczne
                place.setGammaModeStatus(false);
                place.accessMultiset().clear();
                double tokensNo = backupState_MultisetM.accessMultiset_K(placeIndex).get(0);
                place.setTokensNumber((int) tokensNo);
            }
        }
        for (TransitionXTPN trans : transitions) {
            trans.deactivateTransitionXTPN(false);
            trans.simHistoryXTPN.resetSimVariables_XTPN();

            trans.simHistoryXTPN.cleanHistoryVectors(); //TODO
        }
    }

    /**
     * Metoda wywoływana przez okno informacji o miejscu XTPN. Zwraca dwa wektory informacji o tokenach w miejscu.
     * @param ownSettings (<b>SimulatorGlobals</b>) ważne informacje: czy symulacja po czasie czy po krokach, ile kroków, ile czasu?
     * @param place (<b>PlaceXTPN</b>) obiekt miejsca XTPN, którego tokeny są liczone.
     * @return (< b > ArrayList[ArrayList[Double]] < / b >) - dwa wektory, pierwszy liczy tokeny w każdym kroku, drugi
     * zawiera informację o czasie wykonania kroku.
     */
    public ArrayList<ArrayList<Double>> simulateNetSinglePlace(SimulatorGlobals ownSettings, PlaceXTPN place) {
        ArrayList<ArrayList<Double>> resultVectors = new ArrayList<>();
        ArrayList<Double> tokensNumber = new ArrayList<>();
        ArrayList<Double> timeVector = new ArrayList<>();
        resultVectors.add(tokensNumber);
        resultVectors.add(timeVector);

        if (!readyToSimulate) {
            JOptionPane.showMessageDialog(null, "XTPN Simulation cannot start, engine initialization failed.",
                    "Simulation problem", JOptionPane.ERROR_MESSAGE);
            return resultVectors;
        }
        createBackupState(); //zapis p-stanu

        for (TransitionXTPN trans : transitions) {
            trans.simHistoryXTPN.storeHistory = false;
        }

        if (ownSettings.getSimulateTime()) {
            while (simTimeCounter < ownSettings.getSimMaxTime_XTPN()) {
                if (terminate)
                    break;

                processSimStep();
                if (place.isGammaModeActive()) {
                    tokensNumber.add((double) place.accessMultiset().size());
                } else {
                    tokensNumber.add((double) place.getTokensNumber());
                }
                timeVector.add(simTimeCounter);
            }
        } else {
            for (int i = 0; i < ownSettings.getSimSteps_XTPN(); i++) {
                if (terminate)
                    break;

                processSimStep();
                if (place.isGammaModeActive()) {
                    tokensNumber.add((double) place.accessMultiset().size());
                } else {
                    tokensNumber.add((double) place.getTokensNumber());
                }
                timeVector.add(simTimeCounter);
            }
        }
        readyToSimulate = false;
        restoreInternalMarkingZero(); //restore p-state
        return resultVectors;
    }

    /**
     * Metoda symuluje podaną liczbę kroków sieci Petriego dla i sprawdza wybraną tranzycję.
     * @param ownSettings (<b>SimulatorGlobals</b>) ważne informacje: czy symulacja po czasie czy po krokach, ile kroków, ile czasu?
     * @param transition  (<b>TransitionXTPN</b>) - wybrana tranzycja do testowania.
     * @return (< b > ArrayList[ArrayList[Double]] < / b >) - trzy wektory, pierwszy zawiera status tranzycji (0 - nieaktywna,
     * 1 - aktywna, 2 - produkuje, 3 - WYprodukowuje tokeny (w danym momencie), drugi zawiera czas statusu, trzeci
     * to numer kroku dla statusu.
     */
    public ArrayList<ArrayList<Double>> simulateNetSingleTransition(SimulatorGlobals ownSettings, TransitionXTPN transition) {
        ArrayList<ArrayList<Double>> resultVectors = new ArrayList<>();
        ArrayList<Double> statusVector;
        ArrayList<Double> timeVector;
        ArrayList<Double> statsVector = new ArrayList<>();

        if (!readyToSimulate) {
            JOptionPane.showMessageDialog(null, "XTPN Simulation cannot start, engine initialization failed.",
                    "Simulation problem", JOptionPane.ERROR_MESSAGE);
            return resultVectors;
        }
        createBackupState(); //zapis p-stanu

        for (TransitionXTPN trans : transitions) {
            trans.simHistoryXTPN.storeHistory = true;
        }

        if (ownSettings.getSimulateTime()) {
            int step = 0;
            while (simTimeCounter < ownSettings.getSimMaxTime_XTPN()) {
                if (terminate)
                    break;

                processSimStep();
                step++;
            }
            statsVector.add((double) step);
        } else {
            for (int step = 0; step < ownSettings.getSimSteps_XTPN(); step++) {
                if (terminate)
                    break;

                processSimStep();
            }
            statsVector.add((double) ownSettings.getSimSteps_XTPN());
        }
        statsVector.add(simTimeCounter);

        for (TransitionXTPN trans : transitions) {
            if (trans.equals(transition)) { //zachowaj historię, zanim skasujemy:
                statusVector = new ArrayList<>(trans.simHistoryXTPN.statesHistory);
                timeVector = new ArrayList<>(trans.simHistoryXTPN.statesTimeHistory);

                statsVector.add((double) trans.simHistoryXTPN.simInactiveState);
                statsVector.add((double) trans.simHistoryXTPN.simActiveState);
                statsVector.add((double) trans.simHistoryXTPN.simProductionState);
                statsVector.add((double) trans.simHistoryXTPN.simFiredState);
                statsVector.add(trans.simHistoryXTPN.simInactiveTime);
                statsVector.add(trans.simHistoryXTPN.simActiveTime);
                statsVector.add(trans.simHistoryXTPN.simProductionTime);

                resultVectors.add(statusVector);
                resultVectors.add(timeVector);
                resultVectors.add(statsVector);
            }
            trans.simHistoryXTPN.cleanHistoryVectors();
        }

        readyToSimulate = false;
        restoreInternalMarkingZero(); //restore p-state
        return resultVectors;
    }

    /**
     * Metoda symuluje podaną liczbę kroków sieci Petriego dla i zwraca statystyki dla wybranej tranzycji.
     * @param ownSettings (<b>SimulatorGlobals</b>) ważne informacje: czy symulacja po czasie czy po krokach, ile kroków, ile czasu?
     * @param transition  (<b>TransitionXTPN</b>) - wybrana tranzycja do testowania.
     * @return (< b > ArrayList[Double] < / b >) - wektor danych zebranych w symulacji
     */
    public ArrayList<Double> simulateNetSingleTransitionStatistics(SimulatorGlobals ownSettings, TransitionXTPN transition) {
        ArrayList<Double> dataVector = new ArrayList<>();
        if (!readyToSimulate) {
            JOptionPane.showMessageDialog(null, "XTPN Simulation cannot start, engine initialization failed.",
                    "Simulation problem", JOptionPane.ERROR_MESSAGE);
            return dataVector;
        }
        createBackupState(); //zapis p-stanu

        for (TransitionXTPN trans : transitions) {
            trans.simHistoryXTPN.storeHistory = false;
        }

        int step = 0;
        if (ownSettings.getSimulateTime()) {
            while (simTimeCounter < ownSettings.getSimMaxTime_XTPN()) {
                if (terminate)
                    break;
                processSimStep();
                step++;
            }
        } else {
            for (step = 0; step < ownSettings.getSimSteps_XTPN(); step++) {
                if (terminate)
                    break;
                processSimStep();
            }
        }

        for (TransitionXTPN trans : transitions) {
            if (!trans.equals(transition))
                continue;

            dataVector.add((double) step);
            dataVector.add(simTimeCounter);
            dataVector.add((double) trans.simHistoryXTPN.simInactiveState);
            dataVector.add((double) trans.simHistoryXTPN.simActiveState);
            dataVector.add((double) trans.simHistoryXTPN.simProductionState);
            dataVector.add((double) trans.simHistoryXTPN.simFiredState);
            dataVector.add(trans.simHistoryXTPN.simInactiveTime);
            dataVector.add(trans.simHistoryXTPN.simActiveTime);
            dataVector.add(trans.simHistoryXTPN.simProductionTime);
            break;
        }

        readyToSimulate = false;
        restoreInternalMarkingZero(); //restore p-state
        return dataVector;
    }


    /**
     * Główny symulator programu dla wykresów. Rozdziela zadanie sumulacji na jedną z dwóch metod, zależnie czy
     * symulacja ma być po liczbie kroków, czy po czasie.
     * @return (<b>QuickSimMatrix</b>) obiekt danych z wynikami.
     */
    public StateSimDataContainer simulateNet() {
        StateSimDataContainer resMatrix = new StateSimDataContainer();
        if (!readyToSimulate) {
            JOptionPane.showMessageDialog(null, "XTPN Simulation cannot start, engine initialization failed.",
                    "Simulation problem", JOptionPane.ERROR_MESSAGE);
            return resMatrix;
        }

        createBackupState(); //zapis p-stanu

        if(sg.getSimulateTime()) {
            resMatrix = simulateNetTime();
        } else {
            resMatrix = simulateNetSteps();
        }

        SimulatorGlobals ssg = overlord.simSettings;

        readyToSimulate = false;
        restoreInternalMarkingZero(); //restore p-state
        return resMatrix;
    }

    /**
     * Metoda symulacji po liczbie kroków.
     * @return (<b>QuickSimMatrix</b>) obiekt danych z wynikami.
     */
    public StateSimDataContainer simulateNetSteps() {
        StateSimDataContainer resMatrix = new StateSimDataContainer();
        ArrayList<Double> avgFires = new ArrayList<>();
        ArrayList<ArrayList<Double>> tokensInSteps = new ArrayList<>();
        ArrayList<Double> avgTimeForStep = new ArrayList<>();
        Date dateStart = new Date();

        for (TransitionXTPN trans : transitions) {
            trans.simHistoryXTPN.storeHistory = true;
            avgFires.add(0.0);
        }

        //inicjalizacja wektora tokenów miejsc
        ArrayList<Double> tokensAvg = new ArrayList<>();
        ArrayList<Double> tokensAvgFinal = new ArrayList<>();
        for (PlaceXTPN ignored : places) {
            tokensAvg.add(0.0);
            tokensAvgFinal.add(0.0);
        }
        //tutaj są statystyki tranzycji zbiorcze:
        ArrayList<ArrayList<Double>> transStatsFinal = new ArrayList<>();
        for (TransitionXTPN ignored : transitions) {
            transStatsFinal.add(new ArrayList<>(Arrays.asList(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0)));
        }
        progressBar.setValue(0);
        progressBar.setMaximum(sg.simRepetitions_XTPN * 10);
        int progress = 0;
        resMatrix.simReps = sg.simRepetitions_XTPN;


        for (int rep = 0; rep < sg.simRepetitions_XTPN; rep++) {
            int tenth = (int) sg.getSimSteps_XTPN() / 10;
            int maxUpdate = 0;
            for (int step = 0; step < sg.getSimSteps_XTPN(); step++) {
                if (terminate)
                    break;

                ArrayList<ArrayList<Double>> placesStatusVectors = processSimStep();

                for (int placeID = 0; placeID < placesStatusVectors.get(0).size(); placeID++) { //policz sumę tokenów
                    tokensAvg.set(placeID, tokensAvg.get(placeID) + placesStatusVectors.get(0).get(placeID));
                }

                if(rep == 0) {
                    tokensInSteps.add(placesStatusVectors.get(0));

                    avgTimeForStep.add(placesStatusVectors.get(1).get(0)) ; //i tak jest tylko jedna wartość po kroku
                } else if (rep < sg.simRepetitions_XTPN - 1){
                    for (int placeID = 0; placeID < placesStatusVectors.get(0).size(); placeID++) {
                        tokensInSteps.get(step).set(placeID, tokensInSteps.get(step).get(placeID) + placesStatusVectors.get(0).get(placeID) );
                    }

                    //średni czas wykonania kroku step:
                    avgTimeForStep.set(step, avgTimeForStep.get(step) + placesStatusVectors.get(1).get(0) );
                } else {
                    for (int placeID = 0; placeID < placesStatusVectors.get(0).size(); placeID++) {
                        tokensInSteps.get(step).set(placeID, tokensInSteps.get(step).get(placeID) + placesStatusVectors.get(0).get(placeID) );
                        tokensInSteps.get(step).set(placeID, tokensInSteps.get(step).get(placeID) / sg.simRepetitions_XTPN );
                    }

                    //średni czas wykonania kroku step:
                    avgTimeForStep.set(step, avgTimeForStep.get(step) + placesStatusVectors.get(1).get(0) );
                    avgTimeForStep.set(step, avgTimeForStep.get(step) / sg.simRepetitions_XTPN );
                }

                if (step % tenth == 0 && maxUpdate < 10) {
                    progressBar.setValue(progress++);
                    progressBar.update(progressBar.getGraphics());
                    maxUpdate++;
                }
            }
            resMatrix.simSteps += sg.getSimSteps_XTPN();

            //koniec jednego powtórzenia, uśrednianie liczby tokenów:
            for (int pID = 0; pID < tokensAvg.size(); pID++) { //uśrednij sumę tokenów po krokach symulacji i dodaj do wektora wyników
                double avg = tokensAvg.get(pID) / sg.getSimSteps_XTPN();
                tokensAvgFinal.set(pID, tokensAvgFinal.get(pID) + avg);
                tokensAvg.set(pID, 0.0);
            }

            for (int tID = 0; tID < transitions.size(); tID++) {
                TransitionXTPN trans = transitions.get(tID);

                double fired = 0;
                if(rep == 0) {
                    for(int s=0; s<trans.simHistoryXTPN.statesHistory.size(); s++) {
                        TransitionStepStats stepHist = new TransitionStepStats();
                        double state = trans.simHistoryXTPN.statesHistory.get(s);
                        if(state == 0) {
                            stepHist.inactive++;
                        } else if(state == 1) {
                            stepHist.active++;
                        } else if(state == 2) {
                            stepHist.producing++;
                        } else if(state == 3) {
                            stepHist.fired++;
                            fired++;
                        }
                        resMatrix.transitionsSimHistory.add(stepHist);
                    }
                } else {
                    for(int s=0; s<trans.simHistoryXTPN.statesHistory.size(); s++) {
                        double state = trans.simHistoryXTPN.statesHistory.get(s);
                        if(state == 0) {
                            resMatrix.transitionsSimHistory.get(s).inactive++;
                        } else if(state == 1) {
                            resMatrix.transitionsSimHistory.get(s).active++;
                        } else if(state == 2) {
                            resMatrix.transitionsSimHistory.get(s).producing++;
                        } else if(state == 3) {
                            resMatrix.transitionsSimHistory.get(s).fired++;
                            fired++;
                        }
                    }
                }
                avgFires.set(tID, avgFires.get(tID) + fired); //tyle razy została uruchomiana w krokach symulacji tego powtórzenia

                transStatsFinal.get(tID).set(0, transStatsFinal.get(tID).get(0) + transitions.get(tID).simHistoryXTPN.simInactiveState);
                transStatsFinal.get(tID).set(1, transStatsFinal.get(tID).get(1) + transitions.get(tID).simHistoryXTPN.simActiveState);
                transStatsFinal.get(tID).set(2, transStatsFinal.get(tID).get(2) + transitions.get(tID).simHistoryXTPN.simProductionState);
                transStatsFinal.get(tID).set(3, transStatsFinal.get(tID).get(3) + transitions.get(tID).simHistoryXTPN.simFiredState);
                transStatsFinal.get(tID).set(4, transStatsFinal.get(tID).get(4) + transitions.get(tID).simHistoryXTPN.simInactiveTime);
                transStatsFinal.get(tID).set(5, transStatsFinal.get(tID).get(5) + transitions.get(tID).simHistoryXTPN.simActiveTime);
                transStatsFinal.get(tID).set(6, transStatsFinal.get(tID).get(6) + transitions.get(tID).simHistoryXTPN.simProductionTime);

                trans.simHistoryXTPN.cleanHistoryVectors();
            }

            resMatrix.simTime += simTimeCounter;
            simTimeCounter = 0.0;
            restoreInternalMarkingZero(); //restore p-state
        }
        resMatrix.simSteps /= sg.simRepetitions_XTPN;
        resMatrix.simTime /= sg.simRepetitions_XTPN;

        progressBar.setValue(sg.simRepetitions_XTPN * 10);
        progressBar.update(progressBar.getGraphics());

        for (int tID = 0; tID < transitions.size(); tID++) {
            transStatsFinal.get(tID).set(0, transStatsFinal.get(tID).get(0) / sg.simRepetitions_XTPN);
            transStatsFinal.get(tID).set(1, transStatsFinal.get(tID).get(1) / sg.simRepetitions_XTPN);
            transStatsFinal.get(tID).set(2, transStatsFinal.get(tID).get(2) / sg.simRepetitions_XTPN);
            transStatsFinal.get(tID).set(3, transStatsFinal.get(tID).get(3) / sg.simRepetitions_XTPN);
            transStatsFinal.get(tID).set(4, transStatsFinal.get(tID).get(4) / sg.simRepetitions_XTPN);
            transStatsFinal.get(tID).set(5, transStatsFinal.get(tID).get(5) / sg.simRepetitions_XTPN);
            transStatsFinal.get(tID).set(6, transStatsFinal.get(tID).get(6) / sg.simRepetitions_XTPN);

            for(int s=0; s<resMatrix.transitionsSimHistory.size(); s++) {
                resMatrix.transitionsSimHistory.get(s).inactive = resMatrix.transitionsSimHistory.get(s).inactive / sg.simRepetitions_XTPN;
                resMatrix.transitionsSimHistory.get(s).active = resMatrix.transitionsSimHistory.get(s).active / sg.simRepetitions_XTPN;
                resMatrix.transitionsSimHistory.get(s).producing = resMatrix.transitionsSimHistory.get(s).producing / sg.simRepetitions_XTPN;
                resMatrix.transitionsSimHistory.get(s).fired = resMatrix.transitionsSimHistory.get(s).fired / sg.simRepetitions_XTPN;
            }

            avgFires.set(tID, avgFires.get(tID) / (sg.simRepetitions_XTPN * sg.getSimSteps_XTPN()) ); //tyle razy została uruchomiana w krokach symulacji tego powtórzenia
        }

        resMatrix.transDataMatrix = transStatsFinal; //główne statystyki symulacji

        for (int pID = 0; pID < places.size(); pID++) {
            //wcześniej pod kożdą pozycja była SUMA średnich po krokach symulacji, teraz dzielimy przez liczbę powtórzeń symulacji
            tokensAvgFinal.set(pID, tokensAvgFinal.get(pID) / sg.simRepetitions_XTPN);
        }
        resMatrix.avgTokens = tokensAvgFinal; //średnia liczba tokenów
        resMatrix.avgFires = avgFires; //średnia liczba uruchomień tranzycji po wszystkich krokach i powtórzeniach
        resMatrix.avtTimeForStep = avgTimeForStep; //średni czas wykonania się danego kroku
        resMatrix.placesTokensHistory = tokensInSteps;
        Date dateEnd = new Date();
        resMatrix.compTime = dateEnd.getTime() - dateStart.getTime(); //czas trwania oblicze

        return resMatrix;
    }

    /**
     * Metoda symulacji po czasie. Działa w ramach powtórzeń tak, że piersze powtórzenie wyznacza maks. liczbę kroków symulacji.
     * @return (<b>QuickSimMatrix</b>) obiekt danych z wynikami.
     */
    public StateSimDataContainer simulateNetTime() {
        StateSimDataContainer resMatrix = new StateSimDataContainer();
        ArrayList<Double> avgFires = new ArrayList<>();
        ArrayList<ArrayList<Double>> tokensInSteps = new ArrayList<>();
        Date dateStart = new Date();

        //średni czas w którym nastepował dany krok symulacji
        ArrayList<Double> avgTimeForStep = new ArrayList<>();
        //int firstRepMaxStep = 0; //ile kroków trwało pierwsze powtórzenie

        for (TransitionXTPN trans : transitions) {
            trans.simHistoryXTPN.storeHistory = true;
            avgFires.add(0.0);
        }

        //inicjalizacja wektora tokenów miejsc
        ArrayList<Double> tokensAvg = new ArrayList<>();
        ArrayList<Double> tokensAvgFinal = new ArrayList<>();
        for (PlaceXTPN ignored : places) {
            tokensAvg.add(0.0);
            tokensAvgFinal.add(0.0);
        }
        //tutaj są statystyki tranzycji zbiorcze:
        ArrayList<ArrayList<Double>> transStatsFinal = new ArrayList<>();
        for (TransitionXTPN ignored : transitions) {
            transStatsFinal.add(new ArrayList<>(Arrays.asList(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0)));
        }
        progressBar.setValue(0);
        progressBar.setMaximum(sg.simRepetitions_XTPN * 10);
        int progress = 0;
        resMatrix.simReps = sg.simRepetitions_XTPN;


        for (int rep = 0; rep < sg.simRepetitions_XTPN; rep++) {
            int tenth = (int) sg.getSimMaxTime_XTPN() / 10;
            int maxUpdate = 0;
            int step = 0;

            boolean rep2forceContinue = false;
            if(rep > 0)
                rep2forceContinue = true; //do skutku, tj. do osiągnięcia poprzedniej liczby kroków

            while(simTimeCounter < sg.getSimMaxTime_XTPN() || rep2forceContinue) {
                if (terminate)
                    break;

                if(rep > 0) { //nie więcej niż liczba kroków pierwszego powtórzenia
                    if(step >= avgTimeForStep.size() ) {
                        break;
                    }
                }

                ArrayList<ArrayList<Double>> placesStatusVectors = processSimStep();
                //dwa wektory: placesTokensVector (.get(0) ) oraz placesTimeVector (.get(1) )

                for (int placeID = 0; placeID < placesStatusVectors.get(0).size(); placeID++) { //policz sumę tokenów
                    tokensAvg.set(placeID, tokensAvg.get(placeID) + placesStatusVectors.get(0).get(placeID));
                }


                if(rep == 0) {
                    tokensInSteps.add(placesStatusVectors.get(0));

                    //średni czas wykonania kroku step:
                    avgTimeForStep.add(placesStatusVectors.get(1).get(0)) ; //i tak jest tylko jedna wartość po kroku
                } else if (rep < sg.simRepetitions_XTPN - 1){
                    for (int placeID = 0; placeID < placesStatusVectors.get(0).size(); placeID++) {
                        tokensInSteps.get(step).set(placeID, tokensInSteps.get(step).get(placeID) + placesStatusVectors.get(0).get(placeID) );
                    }

                    //średni czas wykonania kroku step:
                    avgTimeForStep.set(step, avgTimeForStep.get(step) + placesStatusVectors.get(1).get(0) );
                } else { //ostatnie powtórzenie
                    for (int placeID = 0; placeID < placesStatusVectors.get(0).size(); placeID++) {
                        tokensInSteps.get(step).set(placeID, tokensInSteps.get(step).get(placeID) + placesStatusVectors.get(0).get(placeID) );
                        tokensInSteps.get(step).set(placeID, tokensInSteps.get(step).get(placeID) / sg.simRepetitions_XTPN );
                    }

                    //średni czas wykonania kroku step:
                    avgTimeForStep.set(step, avgTimeForStep.get(step) + placesStatusVectors.get(1).get(0) );
                    avgTimeForStep.set(step, avgTimeForStep.get(step) / sg.simRepetitions_XTPN );
                }

                if (step % tenth == 0 && maxUpdate < 10) {
                    progressBar.setValue(progress++);
                    progressBar.update(progressBar.getGraphics());
                    maxUpdate++;
                }

                step++;
            }
            resMatrix.simSteps += sg.getSimSteps_XTPN();

            //koniec jednego powtórzenia, uśrednianie liczby tokenów:
            for (int pID = 0; pID < tokensAvg.size(); pID++) { //uśrednij sumę tokenów po krokach symulacji i dodaj do wektora wyników
                double avg = tokensAvg.get(pID) / sg.getSimSteps_XTPN();
                tokensAvgFinal.set(pID, tokensAvgFinal.get(pID) + avg);
                tokensAvg.set(pID, 0.0);
            }

            for (int tID = 0; tID < transitions.size(); tID++) {
                TransitionXTPN trans = transitions.get(tID);

                double fired = 0;
                if(rep == 0) {
                    for(int s=0; s<trans.simHistoryXTPN.statesHistory.size(); s++) {
                        TransitionStepStats stepHist = new TransitionStepStats();
                        double state = trans.simHistoryXTPN.statesHistory.get(s);
                        if(state == 0) {
                            stepHist.inactive++;
                        } else if(state == 1) {
                            stepHist.active++;
                        } else if(state == 2) {
                            stepHist.producing++;
                        } else if(state == 3) {
                            stepHist.fired++;
                            fired++;
                        }
                        resMatrix.transitionsSimHistory.add(stepHist);
                    }
                } else {
                    for(int s=0; s<trans.simHistoryXTPN.statesHistory.size(); s++) {
                        double state = trans.simHistoryXTPN.statesHistory.get(s);
                        if(state == 0) {
                            resMatrix.transitionsSimHistory.get(s).inactive++;
                        } else if(state == 1) {
                            resMatrix.transitionsSimHistory.get(s).active++;
                        } else if(state == 2) {
                            resMatrix.transitionsSimHistory.get(s).producing++;
                        } else if(state == 3) {
                            resMatrix.transitionsSimHistory.get(s).fired++;
                            fired++;
                        }
                    }
                }
                avgFires.set(tID, avgFires.get(tID) + fired); //tyle razy została uruchomiana w krokach symulacji tego powtórzenia

                transStatsFinal.get(tID).set(0, transStatsFinal.get(tID).get(0) + transitions.get(tID).simHistoryXTPN.simInactiveState);
                transStatsFinal.get(tID).set(1, transStatsFinal.get(tID).get(1) + transitions.get(tID).simHistoryXTPN.simActiveState);
                transStatsFinal.get(tID).set(2, transStatsFinal.get(tID).get(2) + transitions.get(tID).simHistoryXTPN.simProductionState);
                transStatsFinal.get(tID).set(3, transStatsFinal.get(tID).get(3) + transitions.get(tID).simHistoryXTPN.simFiredState);
                transStatsFinal.get(tID).set(4, transStatsFinal.get(tID).get(4) + transitions.get(tID).simHistoryXTPN.simInactiveTime);
                transStatsFinal.get(tID).set(5, transStatsFinal.get(tID).get(5) + transitions.get(tID).simHistoryXTPN.simActiveTime);
                transStatsFinal.get(tID).set(6, transStatsFinal.get(tID).get(6) + transitions.get(tID).simHistoryXTPN.simProductionTime);

                trans.simHistoryXTPN.cleanHistoryVectors();
            }

            resMatrix.simTime += simTimeCounter;
            simTimeCounter = 0.0;
            restoreInternalMarkingZero(); //restore p-state
        }
        resMatrix.simSteps /= sg.simRepetitions_XTPN;
        resMatrix.simTime /= sg.simRepetitions_XTPN;

        progressBar.setValue(sg.simRepetitions_XTPN * 10);
        progressBar.update(progressBar.getGraphics());

        for (int tID = 0; tID < transitions.size(); tID++) {
            transStatsFinal.get(tID).set(0, transStatsFinal.get(tID).get(0) / sg.simRepetitions_XTPN);
            transStatsFinal.get(tID).set(1, transStatsFinal.get(tID).get(1) / sg.simRepetitions_XTPN);
            transStatsFinal.get(tID).set(2, transStatsFinal.get(tID).get(2) / sg.simRepetitions_XTPN);
            transStatsFinal.get(tID).set(3, transStatsFinal.get(tID).get(3) / sg.simRepetitions_XTPN);
            transStatsFinal.get(tID).set(4, transStatsFinal.get(tID).get(4) / sg.simRepetitions_XTPN);
            transStatsFinal.get(tID).set(5, transStatsFinal.get(tID).get(5) / sg.simRepetitions_XTPN);
            transStatsFinal.get(tID).set(6, transStatsFinal.get(tID).get(6) / sg.simRepetitions_XTPN);

            for(int s=0; s<resMatrix.transitionsSimHistory.size(); s++) {
                resMatrix.transitionsSimHistory.get(s).inactive = resMatrix.transitionsSimHistory.get(s).inactive / sg.simRepetitions_XTPN;
                resMatrix.transitionsSimHistory.get(s).active = resMatrix.transitionsSimHistory.get(s).active / sg.simRepetitions_XTPN;
                resMatrix.transitionsSimHistory.get(s).producing = resMatrix.transitionsSimHistory.get(s).producing / sg.simRepetitions_XTPN;
                resMatrix.transitionsSimHistory.get(s).fired = resMatrix.transitionsSimHistory.get(s).fired / sg.simRepetitions_XTPN;
            }

            avgFires.set(tID, avgFires.get(tID) / (sg.simRepetitions_XTPN * sg.getSimSteps_XTPN()) ); //tyle razy została uruchomiana w krokach symulacji tego powtórzenia
        }

        resMatrix.transDataMatrix = transStatsFinal; //główne statystyki symulacji

        for (int pID = 0; pID < places.size(); pID++) {
            //wcześniej pod kożdą pozycja była SUMA średnich po krokach symulacji, teraz dzielimy przez liczbę powtórzeń symulacji
            tokensAvgFinal.set(pID, tokensAvgFinal.get(pID) / sg.simRepetitions_XTPN);
        }
        resMatrix.avgTokens = tokensAvgFinal; //średnia liczba tokenów
        resMatrix.avgFires = avgFires; //średnia liczba uruchomień tranzycji po wszystkich krokach i powtórzeniach
        resMatrix.avtTimeForStep = avgTimeForStep; //średni czas wykonania się danego kroku
        resMatrix.placesTokensHistory = tokensInSteps;
        Date dateEnd = new Date();
        resMatrix.compTime = dateEnd.getTime() - dateStart.getTime(); //czas trwania oblicze

        return resMatrix;
    }

    //***************************************************************************************************************
    //****************************************   quickSim   *********************************************************
    //****************************************              *********************************************************
    //****************************************     XTPN     *********************************************************
    //***************************************************************************************************************

    /**
     * Metoda używana przez moduł quickSim, zbiera dane o średniej liczbie uruchomień tranzycji oraz tokenach
     * w miejscach. Powtarza symulacje maksymalnie 20 razy.
     * @return QuickSimMatrix - macierz wektorów danych
     */
    public StateSimDataContainer quickSimGatherData() {
        StateSimDataContainer resMatrix = new StateSimDataContainer();
        Date dateStart = new Date();
        resMatrix.simReps = 0;

        if (!readyToSimulate) {
            JOptionPane.showMessageDialog(null, "XTPN Simulation cannot start, engine initialization failed.",
                    "Simulation problem", JOptionPane.ERROR_MESSAGE);
            return resMatrix;
        }
        createBackupState(); //zapis p-stanu
        int step = 0;

        ArrayList<Double> tokensAvg = new ArrayList<>();
        for (PlaceXTPN ignored : places) {
            tokensAvg.add(0.0);
        }

        if (sg.getSimulateTime()) {
            progressBar.setValue(0);
            progressBar.setMaximum((int) sg.getSimMaxTime_XTPN());

            int tenth = (int) (sg.getSimMaxTime_XTPN() / 10);

            while (simTimeCounter < sg.getSimMaxTime_XTPN()) {
                if (terminate)
                    break;

                ArrayList<ArrayList<Double>> placesStatusVectors = processSimStep();
                //resMatrix.placesTokensDataMatrix.add(placesStatusVectors.get(0));
                //resMatrix.placesTimeDataVector.add(placesStatusVectors.get(1).get(0)); //ten sam czas dla każdego

                for (int pID = 0; pID < placesStatusVectors.get(0).size(); pID++) { //policz sumę tokenów
                    tokensAvg.set(pID, tokensAvg.get(pID) + placesStatusVectors.get(0).get(pID));
                }

                if ((int) simTimeCounter % tenth == 0) {
                    progressBar.setValue((int) simTimeCounter);
                    progressBar.update(progressBar.getGraphics());
                }

                step++;
            }
            progressBar.setValue((int) sg.getSimMaxTime_XTPN());
            progressBar.update(progressBar.getGraphics());
            resMatrix.simSteps = step;

        } else {
            progressBar.setValue(0);
            progressBar.setMaximum((int) sg.getSimSteps_XTPN() - 1);
            int tenth = (int) sg.getSimSteps_XTPN() / 10;

            for (step = 0; step < sg.getSimSteps_XTPN(); step++) {
                if (terminate)
                    break;

                ArrayList<ArrayList<Double>> placesStatusVectors = processSimStep();
                //resMatrix.placesTokensDataMatrix.add(placesStatusVectors.get(0));
                //resMatrix.placesTimeDataVector.add(placesStatusVectors.get(1).get(0)); //ten sam czas dla każdego

                for (int pID = 0; pID < placesStatusVectors.get(0).size(); pID++) { //policz sumę tokenów
                    tokensAvg.set(pID, tokensAvg.get(pID) + placesStatusVectors.get(0).get(pID));
                }

                if (step % tenth == 0) {
                    progressBar.setValue(step);
                    progressBar.update(progressBar.getGraphics());
                }
            }
            progressBar.setValue((int) sg.getSimSteps_XTPN() - 1);
            progressBar.update(progressBar.getGraphics());
            resMatrix.simSteps = sg.getSimSteps_XTPN();
        }
        resMatrix.simTime = simTimeCounter;

        for (int pID = 0; pID < tokensAvg.size(); pID++) { //uśrednij sumę tokenów po krokach symulacji
            double avg = tokensAvg.get(pID) / step;
            tokensAvg.set(pID, avg);
        }
        resMatrix.avgTokens = tokensAvg;

        //STATYSTYKI TRANZYCJI:
        for (TransitionXTPN trans : transitions) {
            ArrayList<Double> dataVector = new ArrayList<>();
            dataVector.add((double) trans.simHistoryXTPN.simInactiveState);
            dataVector.add((double) trans.simHistoryXTPN.simActiveState);
            dataVector.add((double) trans.simHistoryXTPN.simProductionState);
            dataVector.add((double) trans.simHistoryXTPN.simFiredState);
            dataVector.add(trans.simHistoryXTPN.simInactiveTime);
            dataVector.add(trans.simHistoryXTPN.simActiveTime);
            dataVector.add(trans.simHistoryXTPN.simProductionTime);
            resMatrix.transDataMatrix.add(dataVector);
        }

        Date dateEnd = new Date();
        resMatrix.compTime = dateEnd.getTime() - dateStart.getTime();

        readyToSimulate = false;
        restoreInternalMarkingZero(); //restore p-state
        return resMatrix;
    }

    /**
     * Metoda używana przez moduł quickSim, zbiera dane o średniej liczbie uruchomień tranzycji oraz tokenach
     * w miejscach. Powtarza symulacje maksymalnie 20 razy.
     * @return QuickSimMatrix - macierz wektorów danych
     */
    public StateSimDataContainer quickSimGatherDataRepetitions(boolean knockoutSubSim, int startingProgress) {
        StateSimDataContainer resMatrix = new StateSimDataContainer();
        Date dateStart = new Date();

        if (!readyToSimulate) {
            JOptionPane.showMessageDialog(null, "XTPN Simulation cannot start, engine initialization failed.",
                    "Simulation problem", JOptionPane.ERROR_MESSAGE);
            return resMatrix;
        }
        createBackupState(); //zapis p-stanu


        //inicjalizacja wektora tokenów miejsc
        ArrayList<Double> tokensAvg = new ArrayList<>();
        ArrayList<Double> tokensAvgFinal = new ArrayList<>();
        for (PlaceXTPN ignored : places) {
            tokensAvg.add(0.0);
            tokensAvgFinal.add(0.0);
        }

        ArrayList<ArrayList<Double>> transStatsFinal = new ArrayList<>();
        for (TransitionXTPN ignored : transitions) {
            transStatsFinal.add(new ArrayList<>(Arrays.asList(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0)));
        }

        if(!knockoutSubSim) { //jeśli to symulacja dla obliczenia ref/knockout set, zignoruj inicjalizację progressBar
            progressBar.setValue(0);
            progressBar.setMaximum(sg.simRepetitions_XTPN * 10);
        }

        int progress = startingProgress;
        resMatrix.simReps = sg.simRepetitions_XTPN;

        for (int rep = 0; rep < sg.simRepetitions_XTPN; rep++) {
            int step = 0;
            if (sg.getSimulateTime()) {
                int tenth = ((int) sg.getSimMaxTime_XTPN()) / 10;
                int counter = 1;
                while (simTimeCounter < sg.getSimMaxTime_XTPN()) {
                    if (terminate)
                        break;

                    ArrayList<ArrayList<Double>> placesStatusVectors = processSimStep();

                    for (int pID = 0; pID < placesStatusVectors.get(0).size(); pID++) { //policz sumę tokenów
                        tokensAvg.set(pID, tokensAvg.get(pID) + placesStatusVectors.get(0).get(pID));
                    }
                    step++;

                    if (((int) simTimeCounter) >= counter * tenth) {
                        progressBar.setValue(progress++);
                        progressBar.update(progressBar.getGraphics());
                        counter++;
                    }

                }
                resMatrix.simSteps += step;
            } else {
                int tenth = (int) sg.getSimSteps_XTPN() / 10;
                int maxUpdate = 0;
                for (step = 0; step < sg.getSimSteps_XTPN(); step++) {
                    if (terminate)
                        break;

                    ArrayList<ArrayList<Double>> placesStatusVectors = processSimStep();

                    for (int pID = 0; pID < placesStatusVectors.get(0).size(); pID++) { //policz sumę tokenów
                        tokensAvg.set(pID, tokensAvg.get(pID) + placesStatusVectors.get(0).get(pID));
                    }

                    if (step % tenth == 0 && maxUpdate < 10) {
                        progressBar.setValue(progress++);
                        progressBar.update(progressBar.getGraphics());
                        maxUpdate++;
                    }
                }
                resMatrix.simSteps += sg.getSimSteps_XTPN();
            }
            //koniec jednego powtórzenia:
            //uśrednianie liczby tokenów:
            for (int pID = 0; pID < tokensAvg.size(); pID++) { //uśrednij sumę tokenów po krokach symulacji i dodaj do wektora wyników
                double avg = tokensAvg.get(pID) / step;
                tokensAvgFinal.set(pID, tokensAvgFinal.get(pID) + avg);
                tokensAvg.set(pID, 0.0);
            }

            for (int tID = 0; tID < transitions.size(); tID++) {
                transStatsFinal.get(tID).set(0, transStatsFinal.get(tID).get(0) + transitions.get(tID).simHistoryXTPN.simInactiveState);
                transStatsFinal.get(tID).set(1, transStatsFinal.get(tID).get(1) + transitions.get(tID).simHistoryXTPN.simActiveState);
                transStatsFinal.get(tID).set(2, transStatsFinal.get(tID).get(2) + transitions.get(tID).simHistoryXTPN.simProductionState);
                transStatsFinal.get(tID).set(3, transStatsFinal.get(tID).get(3) + transitions.get(tID).simHistoryXTPN.simFiredState);
                transStatsFinal.get(tID).set(4, transStatsFinal.get(tID).get(4) + transitions.get(tID).simHistoryXTPN.simInactiveTime);
                transStatsFinal.get(tID).set(5, transStatsFinal.get(tID).get(5) + transitions.get(tID).simHistoryXTPN.simActiveTime);
                transStatsFinal.get(tID).set(6, transStatsFinal.get(tID).get(6) + transitions.get(tID).simHistoryXTPN.simProductionTime);
            }

            resMatrix.simTime += simTimeCounter;
            simTimeCounter = 0.0;
            restoreInternalMarkingZero(); //restore p-state
        }
        resMatrix.simSteps /= sg.simRepetitions_XTPN;
        resMatrix.simTime /= sg.simRepetitions_XTPN;

        if(!knockoutSubSim) { //jeśli to symulacja dla obliczenia ref/knockout set, zignoruj inicjalizację progressBar
            progressBar.setValue(sg.simRepetitions_XTPN * 10);
            progressBar.update(progressBar.getGraphics());
        }

        for (int tID = 0; tID < transitions.size(); tID++) {
            transStatsFinal.get(tID).set(0, transStatsFinal.get(tID).get(0) / sg.simRepetitions_XTPN);
            transStatsFinal.get(tID).set(1, transStatsFinal.get(tID).get(1) / sg.simRepetitions_XTPN);
            transStatsFinal.get(tID).set(2, transStatsFinal.get(tID).get(2) / sg.simRepetitions_XTPN);
            transStatsFinal.get(tID).set(3, transStatsFinal.get(tID).get(3) / sg.simRepetitions_XTPN);
            transStatsFinal.get(tID).set(4, transStatsFinal.get(tID).get(4) / sg.simRepetitions_XTPN);
            transStatsFinal.get(tID).set(5, transStatsFinal.get(tID).get(5) / sg.simRepetitions_XTPN);
            transStatsFinal.get(tID).set(6, transStatsFinal.get(tID).get(6) / sg.simRepetitions_XTPN);
        }
        resMatrix.transDataMatrix = transStatsFinal;

        for (int pID = 0; pID < places.size(); pID++) {
            tokensAvgFinal.set(pID, tokensAvgFinal.get(pID) / sg.simRepetitions_XTPN);
        }
        resMatrix.avgTokens = tokensAvgFinal;

        Date dateEnd = new Date();
        resMatrix.compTime = dateEnd.getTime() - dateStart.getTime();

        readyToSimulate = false;
        restoreInternalMarkingZero(); //restore p-state
        return resMatrix;
    }


    public ArrayList<StateSimDataContainer> quickSimKnockout() {
        ArrayList<StateSimDataContainer> resultSets = new ArrayList<>();

        progressBar.setValue(0);
        progressBar.setMaximum(sg.simRepetitions_XTPN * 10 * 2);

        StateSimDataContainer knockoutSet = quickSimGatherDataRepetitions(true, 0);

        restartEngine();
        ArrayList<Boolean> disabledVector = new ArrayList<>();
        for(TransitionXTPN trans : transitions) {
            disabledVector.add(trans.isKnockedOut());
            trans.setKnockout(false);
        }

        StateSimDataContainer referenceSet = quickSimGatherDataRepetitions(true, sg.simRepetitions_XTPN * 10);

        for(int tID=0; tID < disabledVector.size(); tID++) { //przywróć poprzedni status tranzycji
            transitions.get(tID).setKnockout( disabledVector.get(tID) );
        }

        progressBar.setValue(sg.simRepetitions_XTPN * 10 * 2);
        progressBar.update(progressBar.getGraphics());

        resultSets.add(referenceSet);
        resultSets.add(knockoutSet);
        return resultSets;
    }
}