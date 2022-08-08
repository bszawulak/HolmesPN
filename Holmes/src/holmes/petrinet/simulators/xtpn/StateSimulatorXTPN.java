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
import java.util.HashMap;
import java.util.Iterator;

/**
 * Klasa symulatora XTPN.
 */
public class StateSimulatorXTPN {
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
        engineXTPN = new SimulatorEngineXTPN();
        overlord = GUIManager.getDefaultGUIManager();
        sg = overlord.simSettings;
    }


    /**
     * Metoda ta musi być wywołana przed każdym startem symulatora. Inicjalizuje początkowe struktury
     * danych dla symulatora
     * @param ownSettings (<b>SimulatorGlobals</b>) parametry symulacji.
     * @return (<b>boolean</b>) - true, jeśli wszystko się udało.
     */
    public boolean initiateSim(SimulatorGlobals ownSettings) {
        transitions.clear();
        for(Transition trans : overlord.getWorkspace().getProject().getTransitions()) {
            if( !(trans instanceof TransitionXTPN)) {
                transitions.clear();
                overlord.log("Error, non-XTPN transitions found in list sent into SimulatorXTPN!", "error", true);
                return false;
            }
            transitions.add( (TransitionXTPN) trans);
        }

        for(Place place : overlord.getWorkspace().getProject().getPlaces()) {
            if( !(place instanceof PlaceXTPN)) {
                transitions.clear();
                overlord.log("Error, non-XTPN places found in list sent into SimulatorXTPN!", "error", true);
                return false;
            }
            places.add( (PlaceXTPN) place);
        }

        if(transitions == null || places == null) {
            readyToSimulate = false;
            return readyToSimulate;
        }
        if(!(transitions.size() > 0 && places.size() > 0)) {
            readyToSimulate = false;
            return readyToSimulate;
        }

        engineXTPN.setEngine(ownSettings.getNetType(), transitions, places);


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
        } //else if(simulationType == 2) { //QuickSimReps
            //this.progressBar = (JProgressBar)blackBox[0];
            //this.quickSim = (QuickSimTools)blackBox[1];
        //} else if(simulationType == 3) { //QuickSimNoReps
            //this.progressBar = (JProgressBar)blackBox[0];
            //this.quickSim = (QuickSimTools)blackBox[1];
        //}
    }

    /**
     * Uruchamiania zdalnie, zakłada, że wszystko co potrzebne zostało już ustawione za pomocą setThreadDetails(...)
     */
    public void run() {
        this.terminate = false;
        if(simulationType == 1) {
            simulateNet();
            //boss.completeSimulationProcedures();
        } //else if(simulationType == 2) {
            //quickSimGatherData();
            //quickSim.finishedStatsData(quickSimAllStats, transitions, places);
        //} else if(simulationType == 3) {
            //quickSimGatherDataNoReps();
            //quickSim.finishedStatsData(quickSimAllStats, transitions, places);
        //}
        this.terminate = false;
    }

    /**
     * Ustawia status wymuszonego kończenia symulacji.
     * @param status (<b>boolean</b>) true, jeśli symulator ma zakończyć działanie.
     */
    public void setCancelStatus(boolean status) {
        this.terminate = status;
        if(status)
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
        createBackupState();

        for(int i=0; i<sg.simSteps_XTPN; i++) {
            if(terminate)
                break;

            ArrayList<HashMap<TransitionXTPN, Double>> tStatusVectors = processSimStep(true, null);

        }

        overlord.log("Simulation ended. Restoring zero marking.", "text", true);
        readyToSimulate = false;

        //restore p-state here:
        restoreInternalMarkingZero();
    }

    /**
     * Metoda wywoływana przez okno informacji o miejscu XTPN. Zwraca dwa wektory informacji o tokenach w miejscu.
     * @param ownSettings (<b>SimulatorGlobals</b>) ważne informacje: czy symulacja po czasie czy po krokach,
     *                    ile kroków, ile czasu?
     * @param place (<b>PlaceXTPN</b>) obiekt miejsca XTPN, którego tokeny są liczone.
     * @return (<b>ArrayList[ArrayList[Double]]</b>) - dwa wektory, pierwszy liczy tokeny w każdym kroku, drugi
     *          zawiera informację o czasie wykonania kroku.
     */
    public ArrayList<ArrayList<Double>> simulateNetSinglePlace(SimulatorGlobals ownSettings
            , PlaceXTPN place) {
        ArrayList<ArrayList<Double>> resultVectors = new ArrayList<>();
        ArrayList<Double> tokensNumber = new ArrayList<>();
        ArrayList<Double> timeVector = new ArrayList<>();
        resultVectors.add(tokensNumber);
        resultVectors.add(timeVector);

        if(!readyToSimulate) {
            JOptionPane.showMessageDialog(null,"XTPN Simulation cannot start, engine initialization failed.",
                    "Simulation problem",JOptionPane.ERROR_MESSAGE);
            return resultVectors;
        }
        createBackupState(); //zapis p-stanu

        if(ownSettings.simulateTime) {
            while(simTimeCounter < ownSettings.simMaxTime_XTPN) {
                if(terminate)
                    break;

                processSimStep(false, null);
                if(place.isGammaModeActive()) {
                    tokensNumber.add((double) place.accessMultiset().size());
                } else {
                    tokensNumber.add((double) place.getTokensNumber());
                }
                timeVector.add(simTimeCounter);
            }
        } else {
            for(int i=0; i<ownSettings.simSteps_XTPN; i++) {
                if(terminate)
                    break;

                processSimStep(false, null);
                if(place.isGammaModeActive()) {
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
     * @param ownSettings (<b>SimulatorGlobals</b>) ważne informacje: czy symulacja po czasie czy po krokach,
     *                    ile kroków, ile czasu?
     * @param transition (<b>TransitionXTPN</b>) - wybrana tranzycja do testowania.
     * @return (<b>ArrayList[ArrayList[Double]]</b>) - trzy wektory, pierwszy zawiera status tranzycji (0 - nieaktywna,
     *  1 - aktywna, 2 - produkuje, 3 - WYprodukowuje tokeny (w danym momencie), drugi zawiera czas statusu, trzeci
     *  to numer kroku dla statusu.
     */
    public ArrayList<ArrayList<Double>> simulateNetSingleTransition(SimulatorGlobals ownSettings
            , TransitionXTPN transition) {
        ArrayList<ArrayList<Double>> resultVectors = new ArrayList<>();
        ArrayList<Double> statusVector = new ArrayList<>();
        ArrayList<Double> timeVector = new ArrayList<>();
        ArrayList<Double> stepVector = new ArrayList<>();
        resultVectors.add(statusVector);
        resultVectors.add(timeVector);
        resultVectors.add(stepVector);

        if(!readyToSimulate) {
            JOptionPane.showMessageDialog(null,"XTPN Simulation cannot start, engine initialization failed.",
                    "Simulation problem",JOptionPane.ERROR_MESSAGE);
            return resultVectors;
        }
        createBackupState(); //zapis p-stanu

        if(ownSettings.simulateTime) {
            double step = 0;
            while(simTimeCounter < ownSettings.simMaxTime_XTPN) {
                if(terminate)
                    break;

                step++;
                ArrayList<HashMap<TransitionXTPN, Double>> result = processSimStep(true, transition);
                HashMap<TransitionXTPN, Double> transStatusVector = result.get(0);
                HashMap<TransitionXTPN, Double> transTimeVector = result.get(1);

                statusVector.add(transStatusVector.get(transition));
                timeVector.add(transTimeVector.get(transition));
                stepVector.add(step);
            }
        } else {
            for(int i=0; i<ownSettings.simSteps_XTPN; i++) {
                if(terminate)
                    break;

                ArrayList<HashMap<TransitionXTPN, Double>> result = processSimStep(true, transition);
                HashMap<TransitionXTPN, Double> transStatusVector = result.get(0);
                HashMap<TransitionXTPN, Double> transTimeVector = result.get(1);

                statusVector.add(transStatusVector.get(transition));
                //timeVector.add(simTimeCounter);
                timeVector.add(transTimeVector.get(transition));
                stepVector.add((double)i);
            }
        }

        readyToSimulate = false;
        restoreInternalMarkingZero(); //restore p-state
        return resultVectors;
    }

    /**
     * Metoda zamyka w sobie wszystkie elementy pełnego kroku symulacji. Zwraca 2 wektory: dwie HashMapy ze statusem
     * tranzycji (0 - nieaktywna, 1 - aktywowana, 2 - produkuJĄCA, 3 - wyproduKOWAŁA), drugi wektor dla
     * tranzycji zawiera czas w którym odnotowano status. Uwaga: dla aktywnych może być o tau (krok) mniejszy niż
     * dla tych które zmieniły swój stan w danym kroku!
     * @param scanTransitions (<b>boolean</b>) true, jeżeli mają być zapamiętane informacje o tranzycjach.
     * @param singleTransition (<b>TransitionXTPN</b>) jeśli nie (<b>null</b>), to tylko ta tranzycja będzie monitorowana.
     * @return (<b>ArrayList[HashMap[TransitionXTPN, Double]]</b>) dwa wektory ze stanem tranzycji po danym kroku.
     */
    private ArrayList<HashMap<TransitionXTPN, Double>> processSimStep(boolean scanTransitions
            , TransitionXTPN singleTransition) {
        ArrayList<SimulatorEngineXTPN.NextXTPNstep> classicalInputTransitions = engineXTPN.revalidateNetState();
        nextXTPNsteps = engineXTPN.computeNextState();
        nextXTPNsteps.set(4, classicalInputTransitions);
        nextXTPNsteps.get(6).get(0).changeType += classicalInputTransitions.size();
        infoNode = nextXTPNsteps.get(6).get(0);
        if(infoNode.changeType == 0) {
            terminate = true;
        }

        engineXTPN.updateNetTime(infoNode.timeToChange);
        simStepsCounter++;
        double oldTIme = simTimeCounter;
        simTimeCounter += infoNode.timeToChange;

        consumingTokensTransitionsXTPN = engineXTPN.returnConsumingTransXTPNVector(nextXTPNsteps);
        consumingTokensTransitionsClassical = engineXTPN.returnConsumingTransClassicalVector(nextXTPNsteps);
        producingTokensTransitionsAll = engineXTPN.returnProducingTransVector(nextXTPNsteps);


        // WEKTORY STANU I CZASU DLA TRANZYCJI:
        HashMap<TransitionXTPN, Double> transitionsStatusVector = new HashMap<>();
        HashMap<TransitionXTPN, Double> transitionsStatusTimeVector = new HashMap<>();
        ArrayList<HashMap<TransitionXTPN, Double>> resultVectors = new ArrayList<>();
        resultVectors.add(transitionsStatusVector);
        resultVectors.add(transitionsStatusTimeVector);

        if(scanTransitions) {
            for (TransitionXTPN trans : transitions) {
                if( !trans.equals(singleTransition )) { //jak null, to i tak nie przejdzie
                    continue;
                }
                if (trans.isActivated_xTPN()) { //jeśli aktywna:
                    transitionsStatusVector.put(trans, 1.0); //1 - activated
                    transitionsStatusTimeVector.put(trans, oldTIme);
                } else if (trans.isProducing_xTPN()) { //jeśli produkująca
                    transitionsStatusVector.put(trans, 2.0); //2 - producing
                    transitionsStatusTimeVector.put(trans, oldTIme);
                } else { //nieaktywna:
                    transitionsStatusVector.put(trans, 0.0); //0 - nieaktywna
                    transitionsStatusTimeVector.put(trans, oldTIme);
                }
            }
        }

        if(consumingTokensTransitionsXTPN.size() > 0 || consumingTokensTransitionsClassical.size() > 0) {
            //faza zabierana tokenów, czyli uruchamianie tranzycji:
            transitionsAfterSubtracting = consumeTokensSubphase();
            //wszystko co trafia poniżej dostaje status production(true), tranzycje XTPN - nowy timer beta:
            engineXTPN.endSubtractPhase(transitionsAfterSubtracting);
        }

        if(producingTokensTransitionsAll.size() > 0) { //tylko produkcja tokenów
            engineXTPN.endProductionPhase(producingTokensTransitionsAll);
        }

        if(scanTransitions) {
            //UPDATE WEKTORÓW:
            for (TransitionXTPN trans : producingTokensTransitionsAll) {
                if( !trans.equals(singleTransition )) { //jak null, to i tak nie przejdzie
                    continue;
                }

                transitionsStatusVector.put(trans, 3.0); //wyprodukowały coś w tym kroku
                transitionsStatusTimeVector.put(trans, simTimeCounter);
            }
            for (TransitionXTPN trans : transitionsAfterSubtracting.get(2)) { //deaktywowane
                if( !trans.equals(singleTransition )) { //jak null, to i tak nie przejdzie
                    continue;
                }

                transitionsStatusVector.put(trans, 0.0); //tranzycje, które w tym kroku zostały deaktywowane
                transitionsStatusTimeVector.put(trans, simTimeCounter);
            }
            for (TransitionXTPN trans : transitionsAfterSubtracting.get(0)) {
                if( !trans.equals(singleTransition )) { //jak null, to i tak nie przejdzie
                    continue;
                }

                if (trans.isProducing_xTPN()) { //jeśli false, to znaczy, że to była klasyczna
                    // i już zdażyła zabrać/wyprodukować i się wygasić
                    transitionsStatusVector.put(trans, 2.0);
                    transitionsStatusTimeVector.put(trans, simTimeCounter);
                }
            }
        }

        consumingTokensTransitionsXTPN.clear();
        consumingTokensTransitionsClassical.clear();
        producingTokensTransitionsAll.clear();
        nextXTPNsteps.clear();
        return resultVectors;
    }

    /**
     * W zasadzie jest to główna metoda ''odpalania'' tranzycji. Te które są aktywne pobierają tokeny i
     * są umieszczane na listach launchedXTPN oraz (potem) launchedClassical. Te tranzycje dla których
     * zabrakło tokenów są deaktywowane oraz umieszczane w trzeciej tablicy: deactivated.
     * @return (<b>ArrayList[ArrayList[TransitionXTPN]]</b>) - trzy listy:
     *          <b>launchedXTPN</b> (.get(0)), <b>launchedClassical</b> (.get(1)) oraz <b>deactivated</b> (.get(2)).
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
        for(PlaceXTPN place : places) {
            if( (place.isGammaModeActive()) ) {
                backupState_MultisetM.addMultiset_K_toMultiset_M(new ArrayList<>(place.accessMultiset()), 1);
            } else {
                int tokens = place.getTokensNumber();
                ArrayList<Double> fakeMultiset = new ArrayList<>();
                fakeMultiset.add((double) tokens);
                backupState_MultisetM.addMultiset_K_toMultiset_M(fakeMultiset, 0);
            }
        }
        for(TransitionXTPN trans : transitions) {
            trans.deactivateTransitionXTPN(false);
        }
    }

    /**
     * Metoda ta przywraca stan sieci przed rozpoczęciem symulacji.
     */
    public void restoreInternalMarkingZero() {
        for (int placeIndex = 0; placeIndex < places.size(); placeIndex++) {
            PlaceXTPN place = places.get(placeIndex);
            if(backupState_MultisetM.isPlaceStoredAsGammaActive(placeIndex)) {
                place.setGammaModeStatus(true);
                place.replaceMultiset( new ArrayList<>(backupState_MultisetM.accessMultiset_K(placeIndex)) );
                place.setTokensNumber( backupState_MultisetM.accessMultiset_K(placeIndex).size() );
            } else { //jeśli w managerze miejsce jest przechowywane jako klasyczne
                place.setGammaModeStatus(false);
                place.accessMultiset().clear();
                double tokensNo = backupState_MultisetM.accessMultiset_K(placeIndex).get(0);
                place.setTokensNumber( (int)tokensNo );
            }
        }
        for(TransitionXTPN trans : transitions) {
            trans.deactivateTransitionXTPN(false);
        }
    }
}
