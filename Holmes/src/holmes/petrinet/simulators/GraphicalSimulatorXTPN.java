package holmes.petrinet.simulators;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JOptionPane;
import javax.swing.Timer;

import holmes.darkgui.GUIManager;
import holmes.petrinet.data.PetriNet;
import holmes.petrinet.elements.Arc;
import holmes.petrinet.elements.Place;
import holmes.petrinet.elements.Transition;
import holmes.petrinet.elements.Arc.TypeOfArc;
import holmes.petrinet.functions.FunctionsTools;
import holmes.windows.HolmesNotepad;

/**
 * Klasa zajmująca się zarządzaniem całym procesem symulacji.
 *
 * @author students - pierwsza wersja, klasyczne PN oraz TPN
 * @author MR - poprawki, zmiany, kolejne rodzaje trubów symulacji
 */
public class GraphicalSimulatorXTPN {
    private SimulatorGlobals.SimNetType netSimTypeXTPN;
    private SimulatorModeXTPN simulatorStatusXTPN = SimulatorModeXTPN.STOPPED;
    private SimulatorModeXTPN previousSimStatusXTPN = SimulatorModeXTPN.STOPPED;
    private PetriNet petriNet;
    private int delay = 30;	//opóźnienie
    private boolean simulationActive = false;
    private Timer timer;
    private ArrayList<Transition> consumingTokensTransitionsXTPN;
    private ArrayList<Transition> consumingTokensTransitionsClassical;
    private ArrayList<Transition> producingTokensTransitionsAll;
    //lista tranzycji XTPN i klasycznych które rozpoczęły produkcję
    ArrayList<ArrayList<Transition>> transitionsAfterSubtracting;
    private long stepCounter = 0;
    private double simTotalTime = 0.0;
    //private Random generator;

    ArrayList<ArrayList<SimulatorXTPN.NextXTPNstep>> nextXTPNsteps;
    SimulatorXTPN.NextXTPNstep infoNode;

    private SimulatorXTPN engineXTPN;
    private GUIManager overlord;

    public enum SimulatorModeXTPN {
        XTPNLOOP, STOPPED, PAUSED,
    }

    /**
     * Konstruktor obiektu symulatora sieci.
     * @param type NetType - typ sieci
     * @param net PetriNet - sieć do symulacji
     */
    public GraphicalSimulatorXTPN(SimulatorGlobals.SimNetType type, PetriNet net) {
        netSimTypeXTPN = type;
        petriNet = net;
        consumingTokensTransitionsXTPN = new ArrayList<Transition>();
        consumingTokensTransitionsClassical = new ArrayList<Transition>();
        producingTokensTransitionsAll = new ArrayList<Transition>();
        engineXTPN = new SimulatorXTPN();
        overlord = GUIManager.getDefaultGUIManager();
    }

    /**
     * Reset do ustawień domyślnych symulatora XTPN.
     */
    public void resetSimulator() {
        simulatorStatusXTPN = SimulatorModeXTPN.STOPPED;
        previousSimStatusXTPN = SimulatorModeXTPN.STOPPED;
        simulationActive = false;
        stepCounter = -1;
        engineXTPN = new SimulatorXTPN();
    }

    /**
     * Dostęp do obiektu silnika symulacji.
     * @return (<b>engineXTPN</b>) silnik symulatora XTPN.
     */
    public SimulatorXTPN accessEngine() {
        return engineXTPN;
    }

    /**
     * Metoda rozpoczyna symulację w odpowiednim trybie. W zależności od wybranego trybu,
     * w oddzielnym wątku symulacji inicjalizowana zostaje odpowiednia klasa dziedzicząca
     * po StepPerformer.
     * @param simulatorMode (<b>SimulatorModeXTPN</b>) wybrany tryb symulacji XTPN.
     */
    public void startSimulation(SimulatorModeXTPN simulatorMode) {
        ArrayList<Transition> transitions = petriNet.getTransitions();
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

        engineXTPN.setEngine(SimulatorGlobals.SimNetType.XTPN, false, false, transitions, null, places);

        //nsl.logBackupCreated(); //TODO

        previousSimStatusXTPN = getXTPNsimulatorStatus();
        setSimulatorStatus(simulatorMode);
        setSimulationActive(true);
        ActionListener taskPerformer = new SimulationPerformer();

        //ustawiania stanu przycisków symulacji:
        overlord.getSimulatorBox().getCurrentDockWindow().allowOnlySimulationDisruptButtons();

        switch (getXTPNsimulatorStatus()) {
            case XTPNLOOP:
                taskPerformer = new StepLoopPerformerXTPN(true); //główny tryb
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
        overlord.getSimulatorBox().getCurrentDockWindow().allowOnlySimulationInitiateButtons();
        timer.stop();
        previousSimStatusXTPN = simulatorStatusXTPN;
        setSimulatorStatus(SimulatorModeXTPN.STOPPED);

        stepCounter = 0;
        simTotalTime = 0.0;

        //nsl.logSimStopped(timeCounter);
    }

    /**
     * Metoda uruchamiana przez klasy pomocniczne dziedziczące z SimulationPerformer, odpowiedzialna
     * za pauzowanie symulacji.
     */
    private void pauseSimulation() {
        overlord.getSimulatorBox().getCurrentDockWindow().allowOnlyUnpauseButton();
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
        overlord.getSimulatorBox().getCurrentDockWindow().allowOnlySimulationDisruptButtons();
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
    public long getStepCounterXTPN() {
        return stepCounter;
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
        protected int repaintSteps = overlord.simSettings.getTransDelay(); // licznik kroków graficznych

        protected boolean newStateChangeStarts = true;
        protected boolean timePassPhase = false;
        protected boolean subtractPhase = false; // true - subtract, false - add
        protected boolean addPhase = false;
        protected boolean finishedAddPhase = false;
        protected boolean scheduledStop = false;
        protected int remainingTransitionsAmount = consumingTokensTransitionsXTPN.size();

        /**
         * Metoda aktualizuje wyświetlanie graficznej części symulacji po wykonaniu każdego kroku.
         */
        protected void updateStepCounter() {
            petriNet.incrementSimulationStep();
        }

        private void resetPhaseOrderXTPN() {
            timePassPhase = false;

            subtractPhase = false;
            addPhase = false;
            finishedAddPhase = false;
        }

        /**
         * Metoda inicjuje zatrzymanie symulacji po zakończeniu aktualnego kroku.
         */
        public void scheduleStop() {
            scheduledStop = true;
        }

        /**
         * Metoda natychmiast zatrzymuje symulację.
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
            petriNet.incrementSimulationStep();
        }
    }

    /**
     *  @author MR
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
         * @param looping boolean - true, jeśli działanie w pętli
         */
        public StepLoopPerformerXTPN(boolean looping) {
            loop = looping;
        }

        /**
         * Metoda wykonywana jako kolejny krok przez symulator. Straszy.
         * @param event ActionEvent - zdarzenie, które spowodowało wykonanie metody. Nie ma przypadków. Są tylko znaki.
         */
        public void actionPerformed(ActionEvent event) {
            //testDzialaniaSymulacji();
            int DEFAULT_COUNTER = overlord.simSettings.getTransDelay(); //def: 25

            //TUTAJ USTALENIE JAK WYGLĄDA AKTUALNY STAN, I O ILE CZASU IDZIEMY DO PRZODU Z SYMULACJĄ
            if(newStateChangeStarts) { //nowy krok symulacji
                if (scheduledStop) { // jeśli symulacja ma się zatrzymać
                    executeScheduledStop(); // egzorcyzmuj symulator
                }
                //najpierw aktywacja tranzycji wejściowych, jeśli są nieaktywne:
                ArrayList<SimulatorXTPN.NextXTPNstep> classicalInputTransitions = engineXTPN.revalidateNetState();
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
                    subtractPhase = true;
                } else if(producingTokensTransitionsAll.size() > 0) { //tylko produkcja
                    addPhase = true;
                } else { //tylko upływ czasu?
                    newStateChangeStarts = true;
                }
                petriNet.repaintAllGraphPanels();
            }

            //tutaj faza zabierana tokenów:
            if(subtractPhase) {
                if(repaintSteps == 0) {
                    transitionsAfterSubtracting = prepareSubtractPhaseGraphics();
                }

                if(repaintSteps < DEFAULT_COUNTER) {
                    repaintSteps++;
                    updateStepCounter();
                } else { //zakończenie fazy zabierania tokenów
                    engineXTPN.endSubtractPhase(transitionsAfterSubtracting);

                    subtractPhase = false;
                    if(producingTokensTransitionsAll.size() > 0) { //tylko produkcja
                        addPhase = true;
                    } else { //tylko upływ czasu?
                        newStateChangeStarts = true;
                    }
                }
            }

            if(addPhase) {
                if(repaintSteps == 0) {
                    prepareProductionPhaseGraphics();
                }

                if(repaintSteps < DEFAULT_COUNTER) {
                    repaintSteps++;
                    updateStepCounter();
                } else { //zakończenie fazy produkcji tokenów
                    engineXTPN.endProductionPhase(producingTokensTransitionsAll);
                    addPhase = false;
                    newStateChangeStarts = true;
                }
            }
        }

        /**
         * Metoda uruchamia fazę odejmowania tokenów z miejsc wejściowych tranzycji XTPN oraz osobno zwykłych.
         * [2022-07-15] Na razie osobno, czyli priorytet mają XTPN nad klasycznymi.
         * @return (<b>ArrayList[ArrayList[Transition]]</b>) podwójna lista tranzycji (XTPN i klasycznych), które zostały uruchomione
         */
        public ArrayList<ArrayList<Transition>> prepareSubtractPhaseGraphics() {
            ArrayList<ArrayList<Transition>> launchedTransitions = new ArrayList<>();
            ArrayList<Transition> launchedXTPN = new ArrayList<>();
            ArrayList<Transition> launchedClassical = new ArrayList<>();
            ArrayList<Arc> arcs;

            //dla : consumingTokensTransitionsXTPN
            //oraz osobno: consumingTokensTransitionsClassical

            for (Transition transition : consumingTokensTransitionsXTPN) { //lista tych, które zabierają tokeny
                if(transition.getActiveStatusXTPN()) { //jeżeli jest aktywna, to zabieramy tokeny
                    transition.setLaunching(true);
                    arcs = transition.getInArcs();
                    for (Arc arc : arcs) {
                        arc.setSimulationForwardDirection(true); //zawsze dla tego symulatora
                        arc.setTransportingTokens(true);
                        Place place = (Place) arc.getStartNode(); //miejsce, z którego zabieramy
                        if(arc.getArcType() == TypeOfArc.INHIBITOR) {
                            arc.setTransportingTokens(false);
                        }  else { //teraz określamy ile
                            int weight = arc.getWeight();
                            if(transition.isFunctional()) {
                                weight = FunctionsTools.getFunctionalArcWeight(transition, arc, place);
                            }
                            place.removeTokensForProduction(weight, 0, engineXTPN.getGenerator());
                        }
                    }
                    launchedXTPN.add(transition);
                } else {
                    transition.deactivateXTPN(); // ???
                }
            }

            for (Transition transition : consumingTokensTransitionsClassical) { //lista tych, które zabierają tokeny
                if(transition.getActiveStatusXTPN()) { //jeżeli jest aktywna, to zabieramy tokeny
                    transition.setLaunching(true);
                    arcs = transition.getInArcs();
                    for (Arc arc : arcs) {
                        arc.setSimulationForwardDirection(true); //zawsze dla tego symulatora
                        arc.setTransportingTokens(true);
                        Place place = (Place) arc.getStartNode(); //miejsce, z którego zabieramy
                        if(arc.getArcType() == TypeOfArc.INHIBITOR) {
                            arc.setTransportingTokens(false);
                        }  else { //teraz określamy ile
                            int weight = arc.getWeight();
                            if(transition.isFunctional()) {
                                weight = FunctionsTools.getFunctionalArcWeight(transition, arc, place);
                            }
                            place.removeTokensForProduction(weight, 0, engineXTPN.getGenerator());
                        }
                    }
                    launchedClassical.add(transition);
                } else {
                    transition.deactivateXTPN();
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
            for (Transition tran : producingTokensTransitionsAll) {
                tran.setLaunching(true);
                arcs = tran.getOutArcs();

                for (Arc arc : arcs) { //read arc...
                    if(arc.getArcType() == TypeOfArc.INHIBITOR )
                        continue;

                    arc.setSimulationForwardDirection(true);
                    arc.setTransportingTokens(true);
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
    }
}
