package holmes.petrinet.simulators.xtpn;

import holmes.darkgui.GUIManager;
import holmes.petrinet.data.MultisetM;
import holmes.petrinet.elements.*;
import holmes.petrinet.functions.FunctionsTools;
import holmes.petrinet.simulators.QuickSimTools;
import holmes.petrinet.simulators.SimulatorGlobals;
import holmes.windows.managers.ssim.HolmesSim;

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
    private HolmesSim boss;    //okno nadrzędne symulatora
    private QuickSimTools quickSim;

    //TRYBY SYMULACJI:

    /**
     * Główny konstruktor obiektu klasy StateSimulator.
     */
    public StateSimulatorXTPN() {
        engineXTPN = new SimulatorEngineXTPN();
        overlord = GUIManager.getDefaultGUIManager();
        sg = overlord.simSettings;
    }

    /**
     * Klasa kontener dla danych z szybkiej symulacji. Macierz tranzycji zawiera tyle list ile kroków symulacji,
     * a każdy wpis to krotka: liczba kroków w fazach: nieaktywna, aktywna, produkująca, uruchomion; czas w fazach:
     * nieaktywności, aktywności, produkcji.
     * Dla miejsc: placesTokensDataMatrix zawiera stan wszystkich miejsc w każdym kroku symulacji.
     */
    public final class QuickSimMatrix {
        public ArrayList<ArrayList<Double>> transDataMatrix = new ArrayList<>();
        public ArrayList<ArrayList<Double>> placesTokensDataMatrix = new ArrayList<>();
        public ArrayList<Double> placesTimeDataVector = new ArrayList<>();

        public ArrayList<Double> avgTokens = new ArrayList<>();

        public double simSteps = 0.0;
        public double simTime = 0.0;
        public double simReps = 0.0;
        public long compTime = 0;
    }


    /**
     * Metoda ta musi być wywołana przed każdym startem symulatora. Inicjalizuje początkowe struktury
     * danych dla symulatora
     *
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
            this.progressBar = (JProgressBar) blackBox[1];
            this.sg = (SimulatorGlobals) blackBox[2];
        } else if (simulationType == 2) { //qSim + repetitions
            this.quickSim = (QuickSimTools) blackBox[0];
            this.progressBar = (JProgressBar) blackBox[1];
            this.sg = (SimulatorGlobals) blackBox[2];
        } else if (simulationType == 3) { //qSim + repetitions + knockout
            this.quickSim = (QuickSimTools) blackBox[0];
            this.progressBar = (JProgressBar) blackBox[1];
            this.sg = (SimulatorGlobals) blackBox[2];
        }
    }

    /**
     * Uruchamiania zdalnie, zakłada, że wszystko co potrzebne zostało już ustawione za pomocą setThreadDetails(...)
     */
    public void run() {
        this.terminate = false;
        if (simulationType == 1) { //qSim
            QuickSimMatrix result = quickSimGatherData();
            quickSim.finishedStatsDataXTPN(result, transitions, places);
        } else if (simulationType == 2) { //qSim + repetitions
            QuickSimMatrix result = quickSimGatherDataRepetitions(false, 0);
            quickSim.finishedStatsDataXTPN(result, transitions, places);
        } else if (simulationType == 3) { //qSim + repetitions + knockout
            ArrayList<QuickSimMatrix> result = quickSimKnockout();
            quickSim.finishedStatsDataXTPN_Knockout(result, transitions, places);
        }
        this.terminate = false;
    }

    /**
     * Ustawia status wymuszonego kończenia symulacji.
     *
     * @param status (<b>boolean</b>) true, jeśli symulator ma zakończyć działanie.
     */
    public void setCancelStatus(boolean status) {
        this.terminate = status;
        if (status)
            readyToSimulate = false;
    }


    /**
     * Metoda pracująca w wątku. Metoda symuluje podaną liczbę kroków sieci Petriego dla wybranego wcześniej trybu, tj.
     * jeśli maximumMode = true, wtedy każda aktywna tranzycja musi się uruchomić.
     */
    public void simulateNet() {
        if (!readyToSimulate) {
            JOptionPane.showMessageDialog(null, "XTPN Simulation cannot start, engine initialization failed.",
                    "Simulation problem", JOptionPane.ERROR_MESSAGE);
            return;
        }

        //tutaj zapisujemy p-stan
        createBackupState();

        for (int i = 0; i < sg.simSteps_XTPN; i++) {
            if (terminate)
                break;

            break;
        }

        overlord.log("Simulation ended. Restoring zero marking.", "text", true);
        readyToSimulate = false;
        restoreInternalMarkingZero();
    }

    /**
     * Metoda wywoływana przez okno informacji o miejscu XTPN. Zwraca dwa wektory informacji o tokenach w miejscu.
     *
     * @param ownSettings (<b>SimulatorGlobals</b>) ważne informacje: czy symulacja po czasie czy po krokach, ile kroków, ile czasu?
     * @param place       (<b>PlaceXTPN</b>) obiekt miejsca XTPN, którego tokeny są liczone.
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
            trans.storeHistory = false;
        }

        if (ownSettings.simulateTime) {
            while (simTimeCounter < ownSettings.simMaxTime_XTPN) {
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
            for (int i = 0; i < ownSettings.simSteps_XTPN; i++) {
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
     *
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
            trans.storeHistory = true;
        }

        if (ownSettings.simulateTime) {
            int step = 0;
            while (simTimeCounter < ownSettings.simMaxTime_XTPN) {
                if (terminate)
                    break;

                processSimStep();
                step++;
            }
            statsVector.add((double) step);
        } else {
            for (int step = 0; step < ownSettings.simSteps_XTPN; step++) {
                if (terminate)
                    break;

                processSimStep();
            }
            statsVector.add((double) ownSettings.simSteps_XTPN);
        }
        statsVector.add(simTimeCounter);

        for (TransitionXTPN trans : transitions) {
            if (trans.equals(transition)) { //zachowaj historię, zanim skasujemy:
                statusVector = new ArrayList<>(trans.statesHistory);
                timeVector = new ArrayList<>(trans.statesTimeHistory);

                statsVector.add((double) trans.simInactiveState);
                statsVector.add((double) trans.simActiveState);
                statsVector.add((double) trans.simProductionState);
                statsVector.add((double) trans.simFiredState);
                statsVector.add(trans.simInactiveTime);
                statsVector.add(trans.simActiveTime);
                statsVector.add(trans.simProductionTime);

                resultVectors.add(statusVector);
                resultVectors.add(timeVector);
                resultVectors.add(statsVector);
            }
            trans.cleanHistoryVectors();
        }

        readyToSimulate = false;
        restoreInternalMarkingZero(); //restore p-state
        return resultVectors;
    }

    /**
     * Metoda symuluje podaną liczbę kroków sieci Petriego dla i zwraca statystyki dla wybranej tranzycji.
     *
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
            trans.storeHistory = false;
        }

        int step = 0;
        if (ownSettings.simulateTime) {
            while (simTimeCounter < ownSettings.simMaxTime_XTPN) {
                if (terminate)
                    break;
                processSimStep();
                step++;
            }
        } else {
            for (step = 0; step < ownSettings.simSteps_XTPN; step++) {
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
            dataVector.add((double) trans.simInactiveState);
            dataVector.add((double) trans.simActiveState);
            dataVector.add((double) trans.simProductionState);
            dataVector.add((double) trans.simFiredState);
            dataVector.add(trans.simInactiveTime);
            dataVector.add(trans.simActiveTime);
            dataVector.add(trans.simProductionTime);
            break;
        }

        readyToSimulate = false;
        restoreInternalMarkingZero(); //restore p-state
        return dataVector;
    }

    /**
     * Główna metoda przetwarzająca krok symulacji oraz zapisująca historię stanów tranzycji.
     *
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
            trans.simFiredState++; //wyprodukowały coś w tym kroku, state=3
            if (trans.isBetaModeActive()) { // wcześniej była w fazie produkcji (DPN/XTPN)
                trans.simProductionState++;
                trans.simProductionTime += infoNode.timeToChange;

                if (trans.storeHistory)
                    trans.addHistoryMoment(2.0, infoNode.timeToChange);
            } else if (trans.isAlphaModeActive()) { // wcześniej była w fazie aktywności (TPN)
                trans.simActiveState++;
                trans.simActiveTime += infoNode.timeToChange;

                if (trans.storeHistory)
                    trans.addHistoryMoment(1.0, infoNode.timeToChange);
            } else { // klasyczna, ale żeby odpalić musiała być przecież aktywna...
                trans.simActiveState++;
                trans.simActiveTime += infoNode.timeToChange;

                if (trans.storeHistory)
                    trans.addHistoryMoment(1.0, infoNode.timeToChange);
            }
            if (trans.storeHistory)
                trans.addHistoryMoment(3.0, infoNode.timeToChange);
            stateChangedTransitions.add(trans);
        }
        if (transitionsAfterSubtracting.size() == 3) {
            for (TransitionXTPN trans : transitionsAfterSubtracting.get(2)) { //deaktywowane przed pobraniem tokenów
                trans.simActiveState++; //deaktywowana, ale do tego momentu musiała być  aktywna
                trans.simActiveTime += infoNode.timeToChange; // musiała być wcześniej aktywna

                stateChangedTransitions.add(trans);

                if (trans.storeHistory)
                    trans.addHistoryMoment(1.0, infoNode.timeToChange);
            }
            for (TransitionXTPN trans : transitionsAfterSubtracting.get(0)) {
                //wciąż produkują (XTPN), get(1) to klasyczne i one już były przerobione dla producingTokensTransitionsAll
                if (producingTokensTransitionsAll.contains(trans))
                    continue;

                if (trans.isBetaModeActive()) { //DPN? XTPN?
                    trans.simActiveState++; //rozpoczęła produkcję, ale do tego momentu musiała być aktywna
                    trans.simActiveTime += infoNode.timeToChange; // musiała być do tego momentu aktywna

                    if (trans.storeHistory)
                        trans.addHistoryMoment(1.0, infoNode.timeToChange);
                }
                stateChangedTransitions.add(trans);
            }
        }


        for (TransitionXTPN trans : transitions) {
            if (stateChangedTransitions.contains(trans)) //już przetworzona
                continue;

            if (trans.isActivated_xTPN()) { //jeśli aktywna:
                trans.simActiveState++;
                trans.simActiveTime += infoNode.timeToChange;

                if (trans.storeHistory)
                    trans.addHistoryMoment(1.0, infoNode.timeToChange);
            } else if (trans.isProducing_xTPN()) { //jeśli produkująca
                trans.simProductionState++;
                trans.simProductionTime += infoNode.timeToChange;

                if (trans.storeHistory)
                    trans.addHistoryMoment(2.0, infoNode.timeToChange);
            } else { //nieaktywna:
                trans.simInactiveState++;
                trans.simInactiveTime += infoNode.timeToChange;

                if (trans.storeHistory)
                    trans.addHistoryMoment(0.0, infoNode.timeToChange);
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
     *
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
                        if (transition.fpnFunctions.isFunctional()) {
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
                        if (transition.fpnFunctions.isFunctional()) {
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
            trans.cleanHistoryVectors();
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
            trans.resetSimVariables_XTPN();
        }
    }


    //***************************************************************************************************************
    //****************************************   quickSim   *********************************************************
    //****************************************              *********************************************************
    //****************************************     XTPN     *********************************************************
    //***************************************************************************************************************

    /**
     * Metoda używana przez moduł quickSim, zbiera dane o średniej liczbie uruchomień tranzycji oraz tokenach
     * w miejscach. Powtarza symulacje maksymalnie 20 razy.
     *
     * @return QuickSimMatrix - macierz wektorów danych
     */
    public QuickSimMatrix quickSimGatherData() {
        QuickSimMatrix resMatrix = new QuickSimMatrix();
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

        if (sg.simulateTime) {
            progressBar.setValue(0);
            progressBar.setMaximum((int) sg.simMaxTime_XTPN);

            int tenth = (int) (sg.simMaxTime_XTPN / 10);

            while (simTimeCounter < sg.simMaxTime_XTPN) {
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
            progressBar.setValue((int) sg.simMaxTime_XTPN);
            progressBar.update(progressBar.getGraphics());
            resMatrix.simSteps = step;

        } else {
            progressBar.setValue(0);
            progressBar.setMaximum((int) sg.simSteps_XTPN - 1);
            int tenth = (int) sg.simSteps_XTPN / 10;

            for (step = 0; step < sg.simSteps_XTPN; step++) {
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
            progressBar.setValue((int) sg.simSteps_XTPN - 1);
            progressBar.update(progressBar.getGraphics());
            resMatrix.simSteps = sg.simSteps_XTPN;
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
            dataVector.add((double) trans.simInactiveState);
            dataVector.add((double) trans.simActiveState);
            dataVector.add((double) trans.simProductionState);
            dataVector.add((double) trans.simFiredState);
            dataVector.add(trans.simInactiveTime);
            dataVector.add(trans.simActiveTime);
            dataVector.add(trans.simProductionTime);
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
     *
     * @return QuickSimMatrix - macierz wektorów danych
     */
    public QuickSimMatrix quickSimGatherDataRepetitions(boolean knockoutSubSim, int startingProgress) {
        QuickSimMatrix resMatrix = new QuickSimMatrix();
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
            if (sg.simulateTime) {
                int tenth = ((int) sg.simMaxTime_XTPN) / 10;
                int counter = 1;
                while (simTimeCounter < sg.simMaxTime_XTPN) {
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
                int tenth = (int) sg.simSteps_XTPN / 10;
                int maxUpdate = 0;
                for (step = 0; step < sg.simSteps_XTPN; step++) {
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
                resMatrix.simSteps += sg.simSteps_XTPN;
            }
            //koniec jednego powtórzenia:
            //uśrednianie liczby tokenów:
            for (int pID = 0; pID < tokensAvg.size(); pID++) { //uśrednij sumę tokenów po krokach symulacji i dodaj do wektora wyników
                double avg = tokensAvg.get(pID) / step;
                tokensAvgFinal.set(pID, tokensAvgFinal.get(pID) + avg);
                tokensAvg.set(pID, 0.0);
            }

            for (int tID = 0; tID < transitions.size(); tID++) {
                transStatsFinal.get(tID).set(0, transStatsFinal.get(tID).get(0) + transitions.get(tID).simInactiveState);
                transStatsFinal.get(tID).set(1, transStatsFinal.get(tID).get(1) + transitions.get(tID).simActiveState);
                transStatsFinal.get(tID).set(2, transStatsFinal.get(tID).get(2) + transitions.get(tID).simProductionState);
                transStatsFinal.get(tID).set(3, transStatsFinal.get(tID).get(3) + transitions.get(tID).simFiredState);
                transStatsFinal.get(tID).set(4, transStatsFinal.get(tID).get(4) + transitions.get(tID).simInactiveTime);
                transStatsFinal.get(tID).set(5, transStatsFinal.get(tID).get(5) + transitions.get(tID).simActiveTime);
                transStatsFinal.get(tID).set(6, transStatsFinal.get(tID).get(6) + transitions.get(tID).simProductionTime);
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


    public ArrayList<QuickSimMatrix> quickSimKnockout() {
        ArrayList<QuickSimMatrix> resultSets = new ArrayList<>();

        progressBar.setValue(0);
        progressBar.setMaximum(sg.simRepetitions_XTPN * 10 * 2);

        QuickSimMatrix knockoutSet = quickSimGatherDataRepetitions(true, 0);

        restartEngine();
        ArrayList<Boolean> disabledVector = new ArrayList<>();
        for(TransitionXTPN trans : transitions) {
            disabledVector.add(trans.isKnockedOut());
            trans.setKnockout(false);
        }

        QuickSimMatrix referenceSet = quickSimGatherDataRepetitions(true, sg.simRepetitions_XTPN * 10);

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