package holmes.petrinet.simulators;

import java.util.ArrayList;
import java.util.Collections;

import holmes.darkgui.GUIManager;
import holmes.petrinet.elements.Node;
import holmes.petrinet.elements.Place;
import holmes.petrinet.elements.Transition;

/**
 * Silnik symulatora XTPN. Procedury odpowiedzialne za symulację modelu
 * opartego na XTPN.
 * @author MR
 */
public class SimulatorXTPN implements IEngine {
    private GUIManager overlord;

    private SimulatorGlobals sg;
    private SimulatorGlobals.SimNetType netSimTypeXTPN = SimulatorGlobals.SimNetType.XTPN;
    private ArrayList<Transition> transitions = null;
    private ArrayList<Place> places = null;
    private ArrayList<Integer> transitionsIndexList = null;
    private ArrayList<Transition> launchableTransitions = null;
    private IRandomGenerator generator = null;

    /**
     * Klasa kontener, informacje o następnej zmianie stanu sieci XTPN.
     */
    public class NextXTPNstep {
        Node element;
        double timeToChange;

        /**
         * 0 - timeToMature, 1 - timeToDie, 2 - transProdStarts, 3 - transProdEnds
         */
        int changeType;

        public NextXTPNstep(Node n, double tau, int change) {
            element = n;
            timeToChange = tau;
            changeType = change;
        }
    }

    /**
     * Konstruktor obiektu klasy SimulatorXTPN.
     */
    public SimulatorXTPN() {
        this.overlord = GUIManager.getDefaultGUIManager();
        generator = new StandardRandom(System.currentTimeMillis());
    }

    /**
     * Ustawianie podstawowych parametrów silnika symulacji.
     * @param simulationType NetType - rodzaj symulowanej sieci
     * @param maxMode boolean - tryb maximum    IGNORED
     * @param singleMode boolean - true, jeśli tylko 1 tranzycja ma odpalić IGNORED
     * @param transitions ArrayList[Transition] - wektor wszystkich tranzycji
     * @param time_transitions ArrayList[Transition] - wektor tranzycji czasowych   IGNORED
     * @param places ArrayList[Place] - wektor miejsc
     */
    public void setEngine(SimulatorGlobals.SimNetType simulationType, boolean maxMode, boolean singleMode,
                          ArrayList<Transition> transitions, ArrayList<Transition> time_transitions,
                          ArrayList<Place> places) {
        this.netSimTypeXTPN = simulationType;
        this.sg = overlord.simSettings;

        if(overlord.simSettings.getGeneratorType() == 1) {
            this.generator = new HighQualityRandom(System.currentTimeMillis());
        } else {
            this.generator = new StandardRandom(System.currentTimeMillis());
        }

        //INIT:
        this.transitions = transitions;
        this.places = places;
        transitionsIndexList = new ArrayList<Integer>();

        launchableTransitions =  new ArrayList<Transition>(); //TODO REMOVE

        for(int t=0; t<transitions.size(); t++) {
            transitionsIndexList.add(t);
        }
    }

    /**
     * Ustawia typ symulacji
     * @param simulationType (<b>SimulatorGlobals.SimNetType</b>) BASIC, TIME, HYBRID, COLOR, XTPN, XTPNfunc, XTPNext, XTPNext_func
     */
    @Override
    public void setNetSimType(SimulatorGlobals.SimNetType simulationType) {
        if(simulationType == SimulatorGlobals.SimNetType.XTPN
                || simulationType == SimulatorGlobals.SimNetType.XTPNfunc
                || simulationType == SimulatorGlobals.SimNetType.XTPNext
                || simulationType == SimulatorGlobals.SimNetType.XTPNext_func) {
            this.netSimTypeXTPN = simulationType;
        } else {
            overlord.log("Wrong simulation type for XTPN simulator: "+simulationType, "error", true);
            //this.netSimTypeXTPN = SimulatorGlobals.SimNetType.XTPN;
        }
    }

    /**
     * Metoda oblicza minimalny czas do zmiany stanu sieci. W liście zwraca informacje o tych
     * miejscach/tranzycjach, w których w tym minimalnym czasie następuje jakaś zmiana.
     * @return (<b>ArrayList[NextXTPNstep]</b>) wektor elementów do zmiany w następnym stanie.
     */
    public ArrayList<NextXTPNstep> computeNextState() {
        ArrayList<NextXTPNstep> stateVector = new ArrayList<>();
        double currentMinTime = Double.MAX_VALUE;

        for(Place place : places) { //znajdź najmniejszy czas do zmiany w miejscach
            double gammaMin = place.getGammaMin_xTPN();
            double gammaMax = place.getGammaMax_xTPN();
            double timeDifference;

            for(double kappa : place.accessMultiset()) {
                if(!place.isGammaModeActiveXTPN()) //nie dotyczy miejsc klasycznych
                    continue;

                if(kappa < gammaMin) { //obliczanie minimalnego czasu do dorośnięcia tokenu
                    timeDifference = gammaMin - kappa;
                    if(Math.abs(timeDifference - currentMinTime) < sg.calculationsAccuracy) { //uznajmy, że to ten sam czas
                        stateVector.add(new NextXTPNstep(place, timeDifference, 0)); //token dojrzeje za timeDifference
                    } else if (timeDifference < currentMinTime) {
                        stateVector.clear(); // wcześniejsze rekordy, jeśli istnieją, zajdą za późniejszy czas
                        stateVector.add(new NextXTPNstep(place, timeDifference, 0)); //token dojrzeje za timeDifference

                        currentMinTime = timeDifference;
                    }
                }

                if(kappa < gammaMax) { //obliczanie minimalnego czasu do usunięcia tokenu
                    timeDifference = gammaMax - kappa;
                    if(Math.abs(timeDifference - currentMinTime) < sg.calculationsAccuracy) { //uznajmy, że to ten sam czas
                        stateVector.add(new NextXTPNstep(place, timeDifference, 1)); //token zniknie za timeDifference
                    } else if (timeDifference < currentMinTime) {
                        stateVector.clear(); // wcześniejsze rekordy, jeśli istnieją, zajdą za późniejszy czas
                        stateVector.add(new NextXTPNstep(place, timeDifference, 1));  //token zniknie za timeDifference

                        currentMinTime = timeDifference;
                    }
                }
            }
        }

        for(Transition transition : transitions) { //znajdź najmniejszy czas do zmiany w tranzycjach
            double tauAlpha = transition.getTauAlpha_xTPN();
            double tauBeta = transition.getTauBeta_xTPN();
            double timerAlpha = transition.getTimerAlfa_XTPN();
            double timerBeta = transition.getTimerBeta_XTPN();
            double timeDifference;

            if(transition.isAlphaActiveXTPN() && transition.isActivated_xTPN()) { //tylko dla Alfa-TPN i to tych aktywnych
                timeDifference = tauAlpha - timerAlpha;
                if(Math.abs(timeDifference - currentMinTime) < sg.calculationsAccuracy) { //uznajmy, że to ten sam czas
                    stateVector.add(new NextXTPNstep(transition, timeDifference, 2)); //tranzycja się uruchomi za timeDifference
                } else if (timeDifference < currentMinTime) {
                    stateVector.clear(); // wcześniejsze rekordy, jeśli istnieją, zajdą za późniejszy czas
                    stateVector.add(new NextXTPNstep(transition, timeDifference, 2));  //tranzycja się uruchomi za timeDifference

                    currentMinTime = timeDifference;
                }
            }
            if(transition.isBetaActiveXTPN() && transition.isProducing_xTPN()) { //tylko dla Beta-DPN i to tych produkujących tokeny
                timeDifference = tauBeta - timerBeta;
                if(Math.abs(timeDifference - currentMinTime) < sg.calculationsAccuracy) { //uznajmy, że to ten sam czas
                    stateVector.add(new NextXTPNstep(transition, timeDifference, 3)); //tranzycja skończy produkcję za timeDifference
                } else if (timeDifference < currentMinTime) {
                    stateVector.clear(); // wcześniejsze rekordy, jeśli istnieją, zajdą za późniejszy czas
                    stateVector.add(new NextXTPNstep(transition, timeDifference, 3));  //tranzycja skończy produkcję za timeDifference

                    currentMinTime = timeDifference;
                }
            }
        }
        return stateVector;
    }




    /**
     * Metoda generuje zbiór tranzycji do uruchomienia w ramach symulatora.
     * @param emptySteps boolean - true, jeśli może być wygenerowany krok bez odpalania tranzycji
     * @return ArrayList[Transition] - zbiór tranzycji do uruchomienia
     */
    public ArrayList<Transition> getTransLaunchList(boolean emptySteps) {
        if(emptySteps == true) {
            generateNormal();
        } else {
            generateWithoutEmptySteps();
        }
        return launchableTransitions;
    }

    /**
     * Metoda generowania nowych tranzycji do odpalenia bez pustych kroków (pusty wektor tranzycji
     * do odpalenia możliwy tylko w przypadku sieci czasowych).
     * @return ArrayList[Transition] - wektor tranzycji do odpalenia
     */
    private ArrayList<Transition> generateWithoutEmptySteps() {
        boolean generated = false;
        int safetyCounter = 0;
        while (!generated) {
            generateLaunchingTransitions(netSimTypeXTPN);
            if (launchableTransitions.size() > 0) {
                generated = true;
            } else {
                if (netSimTypeXTPN == SimulatorGlobals.SimNetType.XTPN) { //TODO
                    return launchableTransitions;
                } else {
                    safetyCounter++;
                    if(safetyCounter == 9) { // safety measure
                        if(isPossibleStep(transitions) == false) {
                            GUIManager.getDefaultGUIManager().log("Error, no active transition, yet generateValidLaunchingTransitions "
                                    + "has been activated. Please advise authors if this error show up frequently.", "error", true);
                            generated = true;
                            //return launchableTransitions;
                        }
                    }
                }
            }
        }
        return launchableTransitions; //bez tej linii będzie błąd, tak, wiem, że to vs. powyższe jest bez sensu.
    }

    /**
     * Metoda generowania nowych tranzycji do odpalenia dopuszczający puste kroki.
     * @return ArrayList[Transition] - wektor tranzycji do odpalenia
     */
    private ArrayList<Transition> generateNormal() {
        generateLaunchingTransitions(netSimTypeXTPN);
        return launchableTransitions;
    }

    /**
     * Metoda pomocnicza dla generateValidLaunchingTransitions(), odpowiedzialna za sprawdzenie
     * które tranzycje nadają się do uruchomienia. Aktualnie działa dla modelu klasycznego PN
     * oraz czasowego.
     */
    private void generateLaunchingTransitions(SimulatorGlobals.SimNetType simulationType) {
        launchableTransitions.clear();

        if (simulationType == SimulatorGlobals.SimNetType.XTPN) {
            Collections.shuffle(transitionsIndexList);

            for (int i = 0; i < transitionsIndexList.size(); i++) {
                Transition transition = transitions.get(transitionsIndexList.get(i));
                if (transition.isActive() ) {
                    if ((generator.nextInt(100) < 50) ) { // 50% 0-4 / 5-9
                        transition.bookRequiredTokens();
                        launchableTransitions.add(transition);
                    }
                }
            }
        }

        for (Transition transition : launchableTransitions) {
            transition.returnBookedTokens();
        }
    }

    /**
     * Metoda sprawdzająca, czy krok jest możliwy - czy istnieje choć jedna aktywna tranzycja.
     * @return boolean - true jeśli jest choć jedna aktywna tranzycja; false w przeciwnym wypadku
     */
    private boolean isPossibleStep(ArrayList<Transition> transitions) {
        for (Transition transition : transitions) {
            if (transition.isActive())
                return true;
        }
        return false;
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
     * Zwraca aktualnie ustawiony generator liczb pseudo-losowych.
     * @return IRandomGenerator
     */
    public IRandomGenerator getGenerator() {
        return this.generator;
    }
}
