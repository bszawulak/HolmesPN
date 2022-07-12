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
public class NetSimulatorXTPN {
    private SimulatorGlobals.SimNetType netSimTypeXTPN;
    private SimulatorModeXTPN simulatorStatusXTPN = SimulatorModeXTPN.STOPPED;
    private SimulatorModeXTPN previousSimStatusXTPN = SimulatorModeXTPN.STOPPED;
    private PetriNet petriNet;
    private int delay = 30;	//opóźnienie
    private boolean simulationActive = false;
    private Timer timer;
    private ArrayList<Transition> launchingTransitions;
    private long timeCounter = -1;
    //private Random generator;

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
    public NetSimulatorXTPN(SimulatorGlobals.SimNetType type, PetriNet net) {
        netSimTypeXTPN = type;
        petriNet = net;
        launchingTransitions = new ArrayList<Transition>();
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
        timeCounter = -1;
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
     * Metoda sprawdzająca, czy krok jest możliwy - czy istnieje choć jedna aktywna tranzycja.
     * Nie ma znaczenie jaki jest tryb symulacji, jesli żadna tranzycja nie ma odpowiedniej liczby tokenów
     * w miejsach wejściowych, symulacja musi się zakończyć.
     * @return boolean - true jeśli jest choć jedna aktywna tranzycja; false w przeciwnym wypadku
     */
    private boolean isPossibleStep() {
        ArrayList<Transition> transitions = petriNet.getTransitions();
        for (Transition transition : transitions) {
            if (transition.isActive()) {
                return true;
            }
            if(transition.getDPNtimer() >= 0 && transition.getDPNduration() != 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * Zwraca rodzaj ustawionej sieci do symulacji (BASIC, TIMED, HYBRID).
     * @return NetType - j.w.
     */
    public SimulatorGlobals.SimNetType getSimNetType() {
        return netSimTypeXTPN;
    }

    /**
     * Metoda uruchamia fazę odejmowania tokenów z miejsc wejściowych odpalonych tranzycji
     * (lub wyjściowych, dla trybu cofania).
     * @param transitions ArrayList[Transition] - lista uruchamianych tranzycji
     */
    public void launchSubtractPhase(ArrayList<Transition> transitions) {
        ArrayList<Arc> arcs;
        for (Transition transition : transitions) {
            transition.setLaunching(true);
            arcs = transition.getInArcs();

            if(transition.getDPNtimer() > 0) //yeah, trust me, I'm an engineer
                continue;

            // odejmij odpowiednią liczbę tokenów:
            for (Arc arc : arcs) {
                arc.setSimulationForwardDirection(true);
                arc.setTransportingTokens(true);
                Place place = (Place) arc.getStartNode();
                if(arc.getArcType() == TypeOfArc.INHIBITOR) {
                    arc.setTransportingTokens(false);
                }  else {
                    FunctionsTools.functionalExtraction(transition, arc, place);
                }
            }
        }
    }

    /**
     * Metoda uruchamia fazę odejmowania tokenów z miejsc wejściowych jednej odpalonej tranzycji
     * (lub wyjściowych, dla trybu cofania).
     * @param transitions ArrayList[Transition] - lista odpalanych tranzycji
     * @param chosenTransition Transition - wybrana tranzycja, której dotyczy uruchomienie tej metody
     * @return boolean - true, jeśli faza została pomyślnie uruchomiona; false w przeciwnym razie
     */
    public boolean launchSingleSubtractPhase(ArrayList<Transition> transitions, Transition chosenTransition) {
        if (transitions.size() < 1)
            return false;
        else {
            Transition transition= transitions.get(0);
            ArrayList<Arc> arcs = transition.getInArcs();

            transition.setLaunching(true);
            for (Arc arc : arcs) {
                arc.setSimulationForwardDirection(true);
                arc.setTransportingTokens(true);
                Place place = (Place) arc.getStartNode();

                if(arc.getArcType() == TypeOfArc.INHIBITOR) {
                    arc.setTransportingTokens(false);
                } else {
                    FunctionsTools.functionalExtraction(transition, arc, place);
                    //place.modifyTokensNumber(-arc.getWeight());
                }
            }
            return true;
        }
    }

    /**
     * Metoda uruchamia fazę wizualizacji dodawania tokenów do miejsc wyjściowych odpalonych tranzycji
     * (lub wejściowych, dla trybu cofania).
     * @param transitions ArrayList[Transition] - lista odpalanych tranzycji
     */
    public void launchAddPhaseGraphics(ArrayList<Transition> transitions) {
        ArrayList<Arc> arcs;
        for (Transition tran : transitions) {
            tran.setLaunching(true);
            arcs = tran.getOutArcs();

            for (Arc arc : arcs) {
                //if(arc.getArcType() == TypeOfArc.INHIBITOR || arc.getArcType() == TypeOfArc.READARC)
                if(arc.getArcType() == TypeOfArc.INHIBITOR )
                    continue;

                arc.setSimulationForwardDirection(true);
                arc.setTransportingTokens(true);
            }
        }
    }

    /**
     * Metoda uruchamia fazę wizualizacji dodawania tokenów do miejsc wyjściowych dla pojedynczej
     * spośród odpalanych tranzycji (lub wejściowych, dla trybu cofania).
     * @param transitions ArrayList[Transition] - lista odpalanych tranzycji
     * @param chosenTransition Transition - wybrana tranzycja, której dotyczy uruchomienie tej metody
     * @return boolean - true, jeśli faza została pomyślnie uruchomiona; false w przeciwnym razie
     */
    public boolean launchSingleAddPhaseGraphics( ArrayList<Transition> transitions, Transition chosenTransition) {
        if (transitions.size() < 1)
            return false;
        else {
            Transition tran = transitions.get(0);
            ArrayList<Arc> arcs = tran.getOutArcs();
            tran.setLaunching(true);

            for (Arc arc : arcs) {
                //if(arc.getArcType() == TypeOfArc.INHIBITOR || arc.getArcType() == TypeOfArc.READARC)
                if(arc.getArcType() == TypeOfArc.INHIBITOR)
                    continue;

                arc.setSimulationForwardDirection(true);
                arc.setTransportingTokens(true);
            }
            return true;
        }
    }

    /**
     * Metoda uruchamia fazę faktycznego dodawania tokenów do miejsc wyjściowych odpalonych tranzycji
     * @param transitions ArrayList[Transition] - lista odpalanych tranzycji
     */
    public void launchAddPhase(ArrayList<Transition> transitions) {
        ArrayList<Arc> arcs;
        for (Transition transition : transitions) {
            transition.setLaunching(false);  // skoro tutaj dotarliśmy, to znaczy że tranzycja już
            //swoje zrobiła i jej status aktywnej się kończy w tym kroku
            arcs = transition.getOutArcs();

            //dodaj odpowiednią liczbę tokenów do miejsc
            for (Arc arc : arcs) {
                Place place = (Place) arc.getEndNode();

                if(!(arc.getArcType() == TypeOfArc.NORMAL || arc.getArcType() == TypeOfArc.COLOR
                        || arc.getArcType() == TypeOfArc.READARC)) {
                    overlord.log("Error: non-standard arc used to produce tokens: "+place.getName()+
                            " arc: "+arc.toString(), "error", true);
                }

                FunctionsTools.functionalAddition(transition, arc, place);

            }
            transition.resetTimeVariables();
        }
        transitions.clear();  //wyczyść listę tranzycji 'do uruchomienia' (już swoje zrobiły)
    }

    /**
     * Metoda uruchamia fazę faktycznego dodawania tokenów do miejsc wyjściowych dla pojedynczej
     * spośród odpalanych tranzycji (lub wejściowych, dla trybu cofania).
     * @param transitions ArrayList[Transition] - lista odpalanych tranzycji
     * @param chosenTransition Transition - wybrana tranzycja, której dotyczy uruchomienie tej metody
     * @return boolean - true, jeśli faza została pomyślnie uruchomiona; false w przeciwnym razie
     */
    public boolean launchSingleAddPhase(ArrayList<Transition> transitions, Transition chosenTransition) {
        if (transitions.size() < 1)
            return false;
        else {
            Transition transition = transitions.get(0);
            ArrayList<Arc> arcs = transition.getOutArcs();

            for (Arc arc : arcs) {
                if(arc.getArcType() == TypeOfArc.READARC)
                    continue;

                Place place = (Place) arc.getEndNode();
                if(arc.getArcType() == TypeOfArc.NORMAL || arc.getArcType() == TypeOfArc.COLOR
                        || arc.getArcType() == TypeOfArc.READARC) { //!!!!!! było bez drugiego członu po ||
                    ;
                } else {
                    overlord.log("Error: non-standard arc used to produce tokens: "+place.getName()+
                            " arc: "+arc.toString(), "error", true);
                }
                if(arc.getArcType() == TypeOfArc.COLOR && place.isColored) {
                    place.modifyColorTokensNumber(arc.getColorWeight(0), 0);
                    place.modifyColorTokensNumber(arc.getColorWeight(1), 1);
                    place.modifyColorTokensNumber(arc.getColorWeight(2), 2);
                    place.modifyColorTokensNumber(arc.getColorWeight(3), 3);
                    place.modifyColorTokensNumber(arc.getColorWeight(4), 4);
                    place.modifyColorTokensNumber(arc.getColorWeight(5), 5);
                } else {
                    //tylko zwykły łuk
                    FunctionsTools.functionalAddition(transition, arc, place);
                    //place.modifyTokensNumber(arc.getWeight());
                }
            }
            transitions.remove(transition);
            return true;
        }
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
    public long getSimulatorTimeStep() {
        return timeCounter;
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
        protected int transitionDelay = overlord.simSettings.getTransDelay(); // licznik kroków graficznych

        protected boolean stateChangeStarted = false;
        protected boolean subtractPhase = true; // true - subtract, false - add
        protected boolean addPhase = false;
        protected boolean finishedAddPhase = false;
        protected boolean scheduledStop = false;
        protected int remainingTransitionsAmount = launchingTransitions.size();



        //training:
        protected boolean timePassPhase = false;
        protected boolean transActivatedPhase = false;
        protected boolean transProdStartPhase = false;
        protected boolean transProdEndsPhase = false;

        /**
         * Metoda aktualizuje wyświetlanie graficznej części symulacji po wykonaniu każdego kroku.
         */
        protected void updateStepCounter() {
            petriNet.incrementSimulationStep();
        }

        private void resetPhaseOrderXTPN() {
            timePassPhase = false;
            transActivatedPhase = false;
            transProdStartPhase = false;
            transProdEndsPhase = false;

            subtractPhase = true;
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
         * Metoda wykonywana jako kolejny krok przez symulator.
         * @param event ActionEvent - zdarzenie, które spowodowało wykonanie metody
         */
        public void actionPerformed(ActionEvent event) {
            //testDzialaniaSymulacji();
            int DEFAULT_COUNTER = overlord.simSettings.getTransDelay(); //def: 25
            if (scheduledStop) { // jeśli symulacja ma się zatrzymać
                executeScheduledStop();
            }

            if(!stateChangeStarted) { //nowy krok symulacji
                //najpierw aktywacja wszystkiego w danym stanie co da się aktywować

                ArrayList<SimulatorXTPN.NextXTPNstep> nextXTPNsteps = engineXTPN.computeNextState();

                if(nextXTPNsteps.size() == 0) {
                    setSimulationActive(false);
                    stopSimulation();
                    JOptionPane.showMessageDialog(null, "Simulation stopped, no active transitions.",
                            "Simulation stopped", JOptionPane.INFORMATION_MESSAGE);
                    transitionDelay = 0;
                }
                stateChangeStarted = true;
            }


/*
            //updateStepCounter(); // rusz tokeny
            petriNet.incrementSimulationStep();

            if (transitionDelay >= DEFAULT_COUNTER && subtractPhase) { // jeśli trwa faza zabierania tokenów
                //z miejsc wejściowych i oddawania ich tranzycjom
                if (scheduledStop) { // jeśli symulacja ma się zatrzymać
                    executeScheduledStop();
                } else if (isPossibleStep()) { // sprawdzanie, czy są aktywne tranzycje
                    if (remainingTransitionsAmount == 0) {
                        timeCounter++;
                        overlord.io.updateTimeStep(""+timeCounter);
                        overlord.simSettings.currentStep = timeCounter;

                        launchingTransitions = engineXTPN.getTransLaunchList(false);
                        remainingTransitionsAmount = launchingTransitions.size();
                    }

                    launchSubtractPhase(launchingTransitions); //zabierz tokeny poprzez aktywne tranzycje
                    //usuń te tranzycje, które są w I fazie DPN
                    for(int t=0; t<launchingTransitions.size(); t++) {
                        Transition test_t = launchingTransitions.get(t);
                        if(test_t.getDPNstatus()) {
                            if(test_t.getDPNtimer() == 0 && test_t.getDPNduration() != 0) {
                                launchingTransitions.remove(test_t);
                                t--;
                            }
                        }
                    }
                    subtractPhase = false;
                } else {
                    // simulation ends, no possible steps remaining
                    setSimulationActive(false);
                    stopSimulation();
                    JOptionPane.showMessageDialog(null, "Simulation stopped, no active transitions.",
                            "Simulation stopped", JOptionPane.INFORMATION_MESSAGE);
                }
                transitionDelay = 0;
            } else if (transitionDelay >= DEFAULT_COUNTER && !subtractPhase) {
                // koniec fazy zabierania tokenów, tutaj realizowany jest graficzny przepływ tokenów
                launchAddPhaseGraphics(launchingTransitions);
                finishedAddPhase = false;
                transitionDelay = 0;
            } else if (transitionDelay >= DEFAULT_COUNTER - 5 && !finishedAddPhase) {
                boolean detailedLogging = true;
                //nsl.logSimStepFinished(launchingTransitions, detailedLogging);

                // koniec fazy przepływu tokenów, tutaj uaktualniane są wartości tokenów dla miejsc wyjściowych
                launchAddPhase(launchingTransitions);
                finishedAddPhase = true;
                subtractPhase = true;
                remainingTransitionsAmount = 0; // all transitions launched
                // jeśli to nie tryb LOOP, zatrzymaj symulację

                if (!loop)
                    scheduleStop();

                transitionDelay++;
            } else
                transitionDelay++; // empty steps
                */
        }

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

            if(timePassPhase == false && transActivatedPhase == false && transProdStartPhase == false
                && transProdEndsPhase == false) {
                timePassPhase = true;
                transitionDelay = 0;
            }

            if(timePassPhase) {
                if(transitionDelay >= DEFAULT_COUNTER) {
                    transitionDelay = 0;
                    timePassPhase = false;
                    transActivatedPhase = true;
                } else {
                    transitionDelay++;
                    overlord.getSimulatorBox().getCurrentDockWindow().timeStepLabelValue.setText("Time pass...");
                    launchingTransitions = engineXTPN.getTransLaunchList(false);
                    for(Transition t : launchingTransitions) {
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

            if(transActivatedPhase) {
                if(transitionDelay >= DEFAULT_COUNTER) {
                    transitionDelay = 0;
                    transActivatedPhase = false;
                    transProdStartPhase = true;
                } else {
                    transitionDelay++;
                    overlord.getSimulatorBox().getCurrentDockWindow().timeStepLabelValue.setText("Activated...");
                    launchingTransitions = engineXTPN.getTransLaunchList(false);
                    for(Transition t : launchingTransitions) {
                        t.setLaunching(false);
                        t.setGlowedINV(true, transitionDelay);
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

            if(transProdStartPhase) {
                if(transitionDelay >= DEFAULT_COUNTER) {
                    transitionDelay = 0;
                    transProdStartPhase = false;
                    transProdEndsPhase = true;
                } else {
                    transitionDelay++;
                    overlord.getSimulatorBox().getCurrentDockWindow().timeStepLabelValue.setText("Producing...");
                    launchingTransitions = engineXTPN.getTransLaunchList(false);
                    for(Transition t : launchingTransitions) {
                        t.setFunctional(true);
                        t.setGlowedINV(false, transitionDelay);
                    }
                    petriNet.repaintAllGraphPanels();
                }
            }

            if(transProdEndsPhase) {
                if(transitionDelay >= DEFAULT_COUNTER) {
                    transitionDelay = 0;
                    transProdEndsPhase = false;
                } else {
                    transitionDelay++;
                    overlord.getSimulatorBox().getCurrentDockWindow().timeStepLabelValue.setText("Produced...");
                    launchingTransitions = engineXTPN.getTransLaunchList(false);
                    for(Transition t : launchingTransitions) {
                        t.setFunctional(false);
                        t.setInvisibility(true);
                    }
                    petriNet.repaintAllGraphPanels();
                }
            }
        }
    }
}
