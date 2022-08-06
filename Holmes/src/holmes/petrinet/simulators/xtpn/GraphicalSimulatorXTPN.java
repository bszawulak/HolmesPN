package holmes.petrinet.simulators.xtpn;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JOptionPane;
import javax.swing.Timer;

import holmes.darkgui.GUIManager;
import holmes.petrinet.data.PetriNet;
import holmes.petrinet.elements.*;
import holmes.petrinet.elements.Arc.TypeOfArc;
import holmes.petrinet.functions.FunctionsTools;
import holmes.petrinet.simulators.SimulatorGlobals;
import holmes.windows.HolmesNotepad;

/**
 * Symulator XTPN. Odpowiada na bardzo mądre pytania, na przykład w stylu: czy jak stanę na torach,
 * i chwycę się linii trakcyjnej, to pojadę jak tramwaj?
 * @author MR
 */
public class GraphicalSimulatorXTPN {
    private SimulatorGlobals.SimNetType netSimTypeXTPN;
    private SimulatorModeXTPN simulatorStatusXTPN = SimulatorModeXTPN.STOPPED;
    private SimulatorModeXTPN previousSimStatusXTPN = SimulatorModeXTPN.STOPPED;
    private PetriNet petriNet;
    private SimulatorGlobals sg;
    private int delay = 30;	//opóźnienie
    private boolean simulationActive = false;
    private Timer timer;
    private ArrayList<TransitionXTPN> consumingTokensTransitionsXTPN;
    private ArrayList<TransitionXTPN> consumingTokensTransitionsClassical;
    private ArrayList<TransitionXTPN> producingTokensTransitionsAll;
    //lista tranzycji XTPN i klasycznych które rozpoczęły produkcję
    ArrayList<ArrayList<TransitionXTPN>> transitionsAfterSubtracting;
    private long stepCounter = 0;
    private double simTotalTime = 0.0;
    //private Random generator;

    ArrayList<ArrayList<SimulatorEngineXTPN.NextXTPNstep>> nextXTPNsteps;
    SimulatorEngineXTPN.NextXTPNstep infoNode;

    private SimulatorEngineXTPN engineXTPN;
    private GUIManager overlord;

    public enum SimulatorModeXTPN {
        XTPNLOOP, SINGLE_STEP,LOOP, STOPPED, PAUSED,
    }

    /**
     * Konstruktor obiektu symulatora sieci.
     * @param type NetType - typ sieci
     * @param net PetriNet - sieć do symulacji
     */
    public GraphicalSimulatorXTPN(SimulatorGlobals.SimNetType type, PetriNet net) {
        netSimTypeXTPN = type;
        petriNet = net;
        consumingTokensTransitionsXTPN = new ArrayList<TransitionXTPN>();
        consumingTokensTransitionsClassical = new ArrayList<TransitionXTPN>();
        producingTokensTransitionsAll = new ArrayList<TransitionXTPN>();
        engineXTPN = new SimulatorEngineXTPN();
        nextXTPNsteps = new ArrayList<>();
        overlord = GUIManager.getDefaultGUIManager();
    }

    /**
     * Reset do ustawień domyślnych symulatora XTPN.
     */
    public void resetSimulator() {
        setSimulatorStatus(SimulatorModeXTPN.STOPPED);
        setSimulationActive(true);
        previousSimStatusXTPN = SimulatorModeXTPN.STOPPED;

        stepCounter = 0;
        simTotalTime = 0.0;
        nextXTPNsteps.clear();
        engineXTPN.setEngine(SimulatorGlobals.SimNetType.XTPN, convertTransitionsToXTPN(petriNet.getTransitions()), convertPlacesToXTPN(petriNet.getPlaces()) );
        //engineXTPN.setGraphicalSimulation(true);
    }

    /**
     * Metoda rozpoczyna symulację w odpowiednim trybie. W zależności od wybranego trybu,
     * w oddzielnym wątku symulacji inicjalizowana zostaje odpowiednia klasa dziedzicząca
     * po StepPerformer.
     * @param simulatorMode (<b>SimulatorModeXTPN</b>) wybrany tryb symulacji XTPN.
     */
    public void startSimulation(SimulatorModeXTPN simulatorMode) {
        sg = overlord.simSettings;

        ArrayList<Place> places = petriNet.getPlaces();
        //nsl.logStart(netSimType, writeHistory, simulatorMode, isMaxMode()); //TODO

        HolmesNotepad notepad = new HolmesNotepad(640, 480);
        boolean status = FunctionsTools.validateFunctionNet(notepad, places);
        if(status)
            notepad.setVisible(true);
        else
            notepad.dispose();

        stepCounter = 0;
        simTotalTime = 0.0;
        engineXTPN.setEngine(SimulatorGlobals.SimNetType.XTPN, convertTransitionsToXTPN(petriNet.getTransitions()), convertPlacesToXTPN(places) );
        engineXTPN.setGraphicalSimulation(true);
        //nsl.logBackupCreated(); //TODO

        ArrayList<Arc> arcs = petriNet.getArcs();
        for(Arc arc : arcs) {
            arc.setXTPNactStatus(false);
            arc.setXTPNprodStatus(false);
        }

        previousSimStatusXTPN = getXTPNsimulatorStatus();
        setSimulatorStatus(simulatorMode);
        setSimulationActive(true);
        ActionListener taskPerformer = new SimulationPerformer();

        //ustawiania stanu przycisków symulacji:
        overlord.getSimulatorBox().getCurrentDockWindow().allowOnlySimulationDisruptButtonsXTPN();

        switch (getXTPNsimulatorStatus()) {
            case XTPNLOOP:
                taskPerformer = new StepLoopPerformerXTPN(true); //główny tryb
                break;
            case SINGLE_STEP:
                taskPerformer = new StepLoopPerformerXTPN(false); //główny tryb
                break;
            case PAUSED:
                break;
            case STOPPED:
                break;
            default:
                break;
        }
        setTimer(new Timer(getDelay(), taskPerformer));
        getTimer().start();
    }

    /**
     * Zwraca rodzaj ustawionej sieci do symulacji (BASIC, TIMED, HYBRID).
     * @return NetType - j.w.
     */
    public SimulatorGlobals.SimNetType getSimNetType() {
        return netSimTypeXTPN;
    }

    /**
     * Metoda zatrzymuje symulację po zakończeniu aktualnego kroku (gdy zostaną odpalone
     * wszystkie odpalane w tym momencie tranzycje).
     */
    public void stop() {
        ((SimulationPerformer) getTimer().getActionListeners()[0]).scheduleStop();
    }

    /**
     * Jeśli aktualnie trwa symulacja, metoda ta przerwie ją natychmiast, i zablokuje wszystkie
     * inne funkcje w symulatorze, poza opcją uruchomienia tej metody. Jeśli symulacja została
     * już zatrzymana tą metodą, ponowne jej wywołanie spowoduje kontynuowanie jej od momentu, w
     * którym została przerwana.
     */
    public void pause() {
        if ((getXTPNsimulatorStatus() != SimulatorModeXTPN.PAUSED) && (getXTPNsimulatorStatus() != SimulatorModeXTPN.STOPPED)) {
            pauseSimulation();
        } else if (getXTPNsimulatorStatus() == SimulatorModeXTPN.PAUSED) {
            unpauseSimulation();
        } else if (getXTPNsimulatorStatus() == SimulatorModeXTPN.STOPPED) {
            JOptionPane.showMessageDialog(null,
                    "Can't pause a stopped simulation!", "XTPN simulator is already stopped!", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Metoda uruchamiana przez klasy pomocniczne dziedziczące z SimulationPerformer, odpowiedzialna
     * za zatrzymanie symulacji.
     */
    private void stopSimulation() {
        overlord.getSimulatorBox().getCurrentDockWindow().allowOnlySimulationInitiateButtonsXTPN();
        timer.stop();
        previousSimStatusXTPN = simulatorStatusXTPN;
        setSimulatorStatus(SimulatorModeXTPN.STOPPED);

        stepCounter = 0;
        simTotalTime = 0.0;

        overlord.io.updateTimeStep(true,stepCounter, simTotalTime);
        overlord.simSettings.currentStep = stepCounter;
        overlord.simSettings.currentTime = simTotalTime;
        //nsl.logSimStopped(timeCounter);

        ArrayList<Arc> arcs = petriNet.getArcs();
        for(Arc arc : arcs) {
            arc.setXTPNactStatus(false);
            arc.setXTPNprodStatus(false);
        }
    }

    /**
     * Metoda uruchamiana przez klasy pomocniczne dziedziczące z SimulationPerformer, odpowiedzialna
     * za pauzowanie symulacji.
     */
    private void pauseSimulation() {
        overlord.getSimulatorBox().getCurrentDockWindow().allowOnlyUnpauseButtonXTPN();
        timer.stop();
        previousSimStatusXTPN = simulatorStatusXTPN;
        setSimulatorStatus(SimulatorModeXTPN.PAUSED);

       // nsl.logSimPause(true);
    }

    /**
     * Metoda uruchamiana przez klasy pomocniczne dziedziczące z SimulationPerformer, odpowiedzialna
     * za ponowne uruchomienie (po pauzie) symulacji.
     */
    private void unpauseSimulation() {
        overlord.getSimulatorBox().getCurrentDockWindow().allowOnlySimulationDisruptButtonsXTPN();
        if (previousSimStatusXTPN != SimulatorModeXTPN.STOPPED) {
            timer.start();
            setSimulatorStatus(previousSimStatusXTPN);
        }

       // nsl.logSimPause(false);
    }

    /**
     * Metoda pozwala sprawdzić, czy symulacja jest w tym momencie aktywna.
     * @return boolean - true, jeśli symulacja jest aktywna; false w przeciwnym wypadku
     */
    public boolean isSimulationActive() {
        return simulationActive;
    }

    /**
     * Metoda pozwala ustawić, czy symulacja jest w tym momencie aktywna.
     * @param simulationActive boolean - wartość aktywności symulacji
     */
    public void setSimulationActive(boolean simulationActive) {
        this.simulationActive = simulationActive;
        overlord.getWorkspace().getProject().setSimulationActive(isSimulationActive());
    }

    /**
     * Metoda pozwala pobrać zegar odpowiadający za wątek symulacji.
     * @return Timer - zegar odpowiadający za wątek symulacji
     */
    public Timer getTimer() {
        return timer;
    }

    /**
     * Metoda pozwalająca ustawić nowy zegar dla wątku symulacji.
     * @param timer Timer - zegar
     */
    private void setTimer(Timer timer) {
        this.timer = timer;
    }

    /**
     * Metoda pozwala pobrać interwał czasowy pomiędzy kolejnymi krokami symulacji.
     * @return Integer - interwał czasowy pomiędzy kolejnymi krokami symulacji wyrażony w milisekundach
     */
    public int getDelay() {
        return delay;
    }

    /**
     * Metoda pozwala ustawić interwał czasowy pomiędzy kolejnymi krokami symulacji.
     * @param delay Integer - interwał czasowy pomiędzy kolejnymi krokami symulacji wyrażony w milisekundach
     */
    public void setDelay(Integer delay) {
        if (timer != null)
            timer.setDelay(delay);
        this.delay = delay;
    }

    /**
     * Metoda pozwala pobrać aktualny tryb pracy symulatora XTPN.
     * @return (<b>SimulatorModeXTPN</b>) tryb pracy symulatora XTPN.
     */
    public SimulatorModeXTPN getXTPNsimulatorStatus() {
        return simulatorStatusXTPN;
    }

    /**
     * Metoda ustawiająca nowy tryb pracy dla symulatora XTPN.
     * @param mode (<b>SimulatorModeXTPN</b>) tryb pracy symulatora XTPN.
     */
    private void setSimulatorStatus(SimulatorModeXTPN mode) {
        this.simulatorStatusXTPN = mode;
    }

    /**
     * Metoda zwraca wartość aktualnego kroku symulacji (numer, tj. czas).
     * @return (<b>long</b>) - nr kroku symulacji
     */
    public long getStepsCounterXTPN() {
        return stepCounter;
    }

    public double getTimeCounterXTPN() {
        return simTotalTime;
    }

    // ================================================================================
    // Simulation task performer class
    // ================================================================================

    /**
     * Po tej klasie dziedziczy szereg klas implementujących konkretne tryby pracy symulatora.
     * Metoda actionPerformed() jest wykonywana w każdym kroku symulacji przez timer obiektu
     * NetSimulator.
     * @author students
     *
     */
    private class SimulationPerformer implements ActionListener {
        protected int repaintSteps = overlord.simSettings.getTransitionGraphicDelay(); // licznik kroków graficznych
        protected boolean newStateChangeStarts = true;
        protected boolean subtractPhase = false; // true - subtract, false - add
        protected boolean addPhase = false;
        protected boolean scheduledStop = false;

        /**
         * Metoda aktualizuje wyświetlanie graficznej części symulacji po wykonaniu każdego kroku.
         */
        protected void initiateTokensMoveGraphics() {
            petriNet.incrementGraphicalSimulationStep();
        }

        /**
         * Metoda inicjuje zatrzymanie symulacji po zakończeniu aktualnego kroku. Wywoływana przez przycisk w oknie
         * symulatora, ale pośrednio - poprzez metodę stop() w ramach action listener. Tak, też tego nie ogarniam,
         * jak do tego doszło, nie wiem. Albo i wiem, ale winny jest poza zasięgiem.
         */
        public void scheduleStop() {
            scheduledStop = true;
        }

        /**
         * Metoda natychmiast zatrzymuje symulację. Wywoływana na początku głównej metody działającej
         * w wątku ( actionPerformed(ActionEvent event) ), po tym jak coś ustawiło zmienną scheduledStop
         * na true. Na przykład przycisk okna interfejsu.
         */
        public void executeScheduledStop() {
            stopSimulation();
            scheduledStop = false;
        }

        /**
         * Metoda faktycznie wykonywana jako każdy kolejny krok przez symulator.
         * Szkielet oferowany przez StepPerformer automatycznie aktualizuje graficzną
         * część symulacji na początku każdego kroku.
         * @param event ActionEvent - zdarzenie, które spowodowało wykonanie metody
         */
        public void actionPerformed(ActionEvent event) {
            petriNet.incrementGraphicalSimulationStep();
        }
    }

    private ArrayList<TransitionXTPN> convertTransitionsToXTPN(ArrayList<Transition> transitionsVector) {
        ArrayList<TransitionXTPN> transitions = new ArrayList<>();
        for(Transition trans : transitionsVector) {
            if( !(trans instanceof TransitionXTPN)) {
                transitions.clear();
                overlord.log("Error, non-XTPN transitions found in list sent into SimulatorXTPN!", "error", true);
                return transitions;
            }
            transitions.add( (TransitionXTPN) trans);
        }
        return transitions;
    }

    private ArrayList<PlaceXTPN> convertPlacesToXTPN(ArrayList<Place> placesVector) {
        ArrayList<PlaceXTPN> places = new ArrayList<>();
        for(Place place : placesVector) {
            if( !(place instanceof PlaceXTPN)) {
                places.clear();
                overlord.log("Error, non-XTPN places found in list sent into SimulatorXTPN!", "error", true);
                return places;
            }
            places.add( (PlaceXTPN) place);
        }
        return places;
    }



    //************************************************************************************************
    //************************************************************************************************
    //************************************************************************************************
    //************************************************************************************************
    //************************************************************************************************
    //******************************************            ******************************************
    //****************************************** EngineCore ******************************************
    //******************************************            ******************************************
    //************************************************************************************************
    //************************************************************************************************
    //************************************************************************************************
    //************************************************************************************************
    //************************************************************************************************

    /**
     *  Główna podklasa symulacji. Bo tak.
     */
    private class StepLoopPerformerXTPN extends SimulationPerformer {
        private boolean loop;

        /**
         * Konstruktor bezparametrowy obiektu klasy StepPerformer
         */
        public StepLoopPerformerXTPN() {
            loop = false;
        }

        /**
         * Konstruktor obiektu klasy StepPerformer
         * @param looping (<b>boolean</b>)  true, jeśli działanie ciągłe, false jeśli tylko jeden stan na raz.
         */
        public StepLoopPerformerXTPN(boolean looping) {
            loop = looping;
        }

        /**
         * Metoda wykonywana jako kolejny krok przez symulator. Straszy.
         * @param event (<b>ActionEvent</b>) zdarzenie, które spowodowało wykonanie metody. Nie ma przypadków. Są tylko znaki.
         */
        public void actionPerformed(ActionEvent event) {
            //testDzialaniaSymulacji();
            int DEFAULT_COUNTER = overlord.simSettings.getTransitionGraphicDelay(); //def: 25

            //TUTAJ USTALENIE JAK WYGLĄDA AKTUALNY STAN, I O ILE CZASU IDZIEMY DO PRZODU Z SYMULACJĄ
            if(newStateChangeStarts) { //nowy krok symulacji
                if (scheduledStop) { // jeśli symulacja ma się zatrzymać, np. bo przycisk
                    executeScheduledStop(); // egzorcyzmuj symulator
                    setSimulationActive(false);
                    return;
                }
                //najpierw aktywacja tranzycji wejściowych, jeśli są nieaktywne:
                ArrayList<SimulatorEngineXTPN.NextXTPNstep> classicalInputTransitions = engineXTPN.revalidateNetState();
                //teraz obliczamy minimalną zmianę czasu w sieci:

                nextXTPNsteps = engineXTPN.computeNextState();
                //dodajemy tranzycje wejściowe klasyczne do listy uruchomień w nowym stanie +tau:
                nextXTPNsteps.set(4, classicalInputTransitions);
                nextXTPNsteps.get(6).get(0).changeType += classicalInputTransitions.size();

                //azali, czy jestże coś do rozp... uruchomienia:
                infoNode = nextXTPNsteps.get(6).get(0); //7-ta lista, pierwszy obiekt (8 wrota...)
                if(infoNode.changeType == 0) {
                    setSimulationActive(false);
                    stopSimulation();
                    JOptionPane.showMessageDialog(null, "Simulation stopped, no active transitions.",
                            "Simulation stopped", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }
                //TUTAJ NASTĘPUJE UPDATE STANU O WYLICZONĄ WCZEŚNIEJ WARTOŚĆ +TAU (infoNode.timeToChange)
                //teraz trzeba uaktualnić stan sieci (poza usunięciem tokenów starych, to na samym końcu,
                //może coś je zje przedtem:
                engineXTPN.updateNetTime(infoNode.timeToChange);

                //aktualizacja czasu i kroku symulacji, wyświetlanie w oknie symulatora:
                stepCounter++;
                simTotalTime += infoNode.timeToChange;
                overlord.io.updateTimeStep(true,stepCounter, simTotalTime);
                overlord.simSettings.currentStep = stepCounter;
                overlord.simSettings.currentTime = simTotalTime;

                //tutaj przygotowujemy wektory tranzycji które zabiorą tokeny (dwa, na razie klasyczne w drugiej
                //kolejności po XTPN), oraz wspólny wektor tranzycji, które później wyprodukują tokeny.
                consumingTokensTransitionsXTPN = engineXTPN.returnConsumingTransXTPNVector(nextXTPNsteps);
                consumingTokensTransitionsClassical = engineXTPN.returnConsumingTransClassicalVector(nextXTPNsteps);
                producingTokensTransitionsAll = engineXTPN.returnProducingTransVector(nextXTPNsteps);

                newStateChangeStarts = false;
                repaintSteps = 0;
                if(consumingTokensTransitionsXTPN.size() > 0 || consumingTokensTransitionsClassical.size() > 0) {
                    //faza zabierana tokenów, czyli uruchamianie tranzycji gdy timeAlfa = tauAlfa
                    subtractPhase = true;
                } else if(producingTokensTransitionsAll.size() > 0) { //tylko produkcja tokenów
                    addPhase = true;
                } else { //tylko upływ czasu, co już miało miejsce
                    endThisSimulationStep();
                }
                petriNet.repaintAllGraphPanels();
            }

            //GRAFIKA
            //tutaj faza zabierania tokenów:
            if(subtractPhase) {
                if(repaintSteps == 0) {
                    transitionsAfterSubtracting = prepareSubtractPhaseGraphics();
                }

                if(repaintSteps < DEFAULT_COUNTER) {
                    repaintSteps++;
                    initiateTokensMoveGraphics();
                } else { //zakończenie fazy zabierania tokenów
                    engineXTPN.endSubtractPhase(transitionsAfterSubtracting);

                    subtractPhase = false;
                    repaintSteps = 0;
                    if(producingTokensTransitionsAll.size() > 0) { //jeszcze produkcja
                        addPhase = true;
                    } else { //tylko upływ czasu?
                        endThisSimulationStep();
                    }
                }
            }
            if(addPhase) {
                if(repaintSteps == 0) {
                    prepareProductionPhaseGraphics();
                }
                if(repaintSteps < DEFAULT_COUNTER) {
                    repaintSteps++;
                    initiateTokensMoveGraphics();
                } else { //zakończenie fazy produkcji tokenów
                    engineXTPN.endProductionPhase(producingTokensTransitionsAll);
                    addPhase = false;
                    repaintSteps = 0;
                    endThisSimulationStep();
                }
            }
        }

        /**
         * Metoda kończąca aktualny krok symulacji, gdy już wszystko się wykonało co miało.
         */
        private void endThisSimulationStep() {
            newStateChangeStarts = true;
            if (!loop) {
                scheduleStop();
            }
            clearDataMatrix();
        }

        private void clearDataMatrix() {
            consumingTokensTransitionsXTPN.clear();
            consumingTokensTransitionsClassical.clear();
            producingTokensTransitionsAll.clear();
            nextXTPNsteps.clear();
        }

        /**
         * Metoda uruchamia fazę odejmowania tokenów z miejsc wejściowych tranzycji XTPN oraz osobno zwykłych.
         * [2022-07-15] Na razie osobno, czyli priorytet mają XTPN nad klasycznymi.
         * @return (<b>ArrayList[ArrayList[TransitionXTPN]]</b>) podwójna lista tranzycji (XTPN i klasycznych), które zostały uruchomione
         */
        public ArrayList<ArrayList<TransitionXTPN>> prepareSubtractPhaseGraphics() {
            ArrayList<ArrayList<TransitionXTPN>> launchedTransitions = new ArrayList<>();
            ArrayList<TransitionXTPN> launchedXTPN = new ArrayList<>();
            ArrayList<TransitionXTPN> launchedClassical = new ArrayList<>();
            ArrayList<Arc> arcs;

            //dla : consumingTokensTransitionsXTPN
            //oraz osobno: consumingTokensTransitionsClassical

            for (TransitionXTPN transition : consumingTokensTransitionsXTPN) { //lista tych, które zabierają tokeny
                if(transition.isActiveTransitionXTPN(sg.getCalculationsAccuracy())) { //jeżeli jest aktywna, to zabieramy tokeny
                    transition.setLaunching(true);
                    arcs = transition.getInArcs();
                    for (Arc arc : arcs) {
                        arc.setSimulationForwardDirection(true); //zawsze dla tego symulatora (nie działamy wstecz)
                        arc.setTransportingTokens(true);
                        PlaceXTPN place = (PlaceXTPN) arc.getStartNode(); //miejsce, z którego zabieramy
                        if(arc.getArcType() == TypeOfArc.INHIBITOR) {
                            arc.setTransportingTokens(false);
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
                    transition.deactivateTransitionXTPN(true); // ???
                    transition.setActivationStatusXTPN(false);
                    transition.setProductionStatus_xTPN(false);
                    for(Arc arc : transition.getOutArcs()) {
                        arc.setXTPNprodStatus(false);
                    }
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



            //for (TransitionXTPN transition : consumingTokensTransitionsClassical) { //lista tych, które zabierają tokeny
            for (Iterator<TransitionXTPN> iteratorTrans = consumingTokensTransitionsClassical.iterator(); iteratorTrans.hasNext(); ) {
                TransitionXTPN transition = iteratorTrans.next();

                if(mustFireSOMETHING && fireClassSoFar == 0) {
                    //czyli: MUSIMY coś uruchomić i jeszcze NIC nie uruchomiliśmy:
                    //NIC... najważniejszy, że else zostanie zignorowany, potem już możemy być uczciwi
                } else { //tu bawimy się w bycie uczciwym (chyba, że tranzycja immediate)
                    if ((engineXTPN.getGenerator().nextInt(100) < 50) && !transition.isImmediateXTPN()) {
                        //non immediate classical: 50% chances to fire
                        transition.deactivateTransitionXTPN(true);
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
                        arc.setSimulationForwardDirection(true); //zawsze dla tego symulatora
                        arc.setTransportingTokens(true);
                        PlaceXTPN place = (PlaceXTPN) arc.getStartNode(); //miejsce, z którego zabieramy
                        if(arc.getArcType() == TypeOfArc.INHIBITOR) {
                            arc.setTransportingTokens(false);
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
                    transition.deactivateTransitionXTPN(true);
                    producingTokensTransitionsAll.remove(transition);
                }
            }

            launchedTransitions.add(launchedXTPN);
            launchedTransitions.add(launchedClassical);
            return launchedTransitions;
        }

        /**
         * Metoda uruchamia fazę wizualizacji dodawania tokenów do miejsc wyjściowych odpalonych tranzycji
         * (lub wejściowych, dla trybu cofania).
         */
        public void prepareProductionPhaseGraphics() {
            ArrayList<Arc> arcs;
            for (TransitionXTPN tran : producingTokensTransitionsAll) {
                tran.setLaunching(true);
                arcs = tran.getOutArcs();

                for (Arc arc : arcs) { //read arc...
                    if(arc.getArcType() == TypeOfArc.INHIBITOR )
                        continue;

                    arc.setSimulationForwardDirection(true);
                    arc.setTransportingTokens(true);

                    arc.setXTPNprodStatus(true); //ustaw produkcję tokenów na łuku (kolor)
                }
            }
        }
    }
}
















        /*
        private void testDzialaniaSymulacji() { //for training purposes only, if u dont know how simulator works
            int DEFAULT_COUNTER = overlord.simSettings.getTransDelay(); //def: 25
            if (scheduledStop) { // jeśli symulacja ma się zatrzymać
                executeScheduledStop();
            }

            if (!isPossibleStep()) {
                setSimulationActive(false);
                stopSimulation();
                JOptionPane.showMessageDialog(null, "Simulation stopped, no active transitions.",
                        "Simulation stopped", JOptionPane.INFORMATION_MESSAGE);
            }

            if(!timePassPhase && !subtractPhase && !addPhase
                    && !finishedAddPhase) {
                timePassPhase = true;
                repaintSteps = 0;
            }

            if(timePassPhase) {
                if(repaintSteps >= DEFAULT_COUNTER) {
                    repaintSteps = 0;
                    timePassPhase = false;
                    subtractPhase = true;
                } else {
                    repaintSteps++;
                    overlord.getSimulatorBox().getCurrentDockWindow().timeStepLabelValue.setText("Time pass...");
                    consumingTokensTransitionsXTPN = engineXTPN.getTransLaunchList(false);
                    for(Transition t : consumingTokensTransitionsXTPN) {
                        t.setInvisibility(false);
                        t.setLaunching(true);
                        ArrayList<Arc> arcs = t.getInArcs();

                        // odejmij odpowiednią liczbę tokenów:
                        for (Arc arc : arcs) {
                            arc.setSimulationForwardDirection(true);
                            arc.setTransportingTokens(true);
                        }
                    }
                    petriNet.repaintAllGraphPanels();
                }
            }

            if(subtractPhase) {
                if(repaintSteps >= DEFAULT_COUNTER) {
                    repaintSteps = 0;
                    subtractPhase = false;
                    addPhase = true;
                } else {
                    repaintSteps++;
                    overlord.getSimulatorBox().getCurrentDockWindow().timeStepLabelValue.setText("Activated...");
                    consumingTokensTransitionsXTPN = engineXTPN.getTransLaunchList(false);
                    for(Transition t : consumingTokensTransitionsXTPN) {
                        t.setLaunching(false);
                        t.setGlowedINV(true, repaintSteps);
                        ArrayList<Arc> arcs = t.getInArcs();
                        // odejmij odpowiednią liczbę tokenów:
                        for (Arc arc : arcs) {
                            arc.setSimulationForwardDirection(true);
                            arc.setTransportingTokens(false);
                        }
                    }
                    petriNet.repaintAllGraphPanels();
                }
            }

            if(addPhase) {
                if(repaintSteps >= DEFAULT_COUNTER) {
                    repaintSteps = 0;
                    addPhase = false;
                    finishedAddPhase = true;
                } else {
                    repaintSteps++;
                    overlord.getSimulatorBox().getCurrentDockWindow().timeStepLabelValue.setText("Producing...");
                    consumingTokensTransitionsXTPN = engineXTPN.getTransLaunchList(false);
                    for(Transition t : consumingTokensTransitionsXTPN) {
                        t.setFunctional(true);
                        t.setGlowedINV(false, repaintSteps);
                    }
                    petriNet.repaintAllGraphPanels();
                }
            }

            if(finishedAddPhase) {
                if(repaintSteps >= DEFAULT_COUNTER) {
                    repaintSteps = 0;
                    finishedAddPhase = false;
                } else {
                    repaintSteps++;
                    overlord.getSimulatorBox().getCurrentDockWindow().timeStepLabelValue.setText("Produced...");
                    consumingTokensTransitionsXTPN = engineXTPN.getTransLaunchList(false);
                    for(Transition t : consumingTokensTransitionsXTPN) {
                        t.setFunctional(false);
                        t.setInvisibility(true);
                    }
                    petriNet.repaintAllGraphPanels();
                }
            }
        }
        */