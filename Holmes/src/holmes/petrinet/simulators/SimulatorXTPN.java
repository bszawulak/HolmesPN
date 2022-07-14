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
    private IRandomGenerator generator;

    /**
     * Klasa kontener, informacje o następnej zmianie stanu sieci XTPN.
     */
    public class NextXTPNstep {
        Node element;
        double timeToChange;

        /**
         * 0 - timeToMature, 1 - timeToDie, 2 - transProdStarts, 3 - transProdEnds;<br>
         * <b>W przypadku InfoNode: liczba obiektów NextXTPNstep w arraylistach.</b>
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
    public ArrayList<ArrayList<NextXTPNstep>> computeNextState() {
        ArrayList<ArrayList<NextXTPNstep>> stateVector = new ArrayList<>();
        ArrayList<NextXTPNstep> placesMaturity = new ArrayList<>();
        ArrayList<NextXTPNstep> placesAging = new ArrayList<>();
        ArrayList<NextXTPNstep> transProdStart = new ArrayList<>();
        ArrayList<NextXTPNstep> transProdEnd = new ArrayList<>();
        //ArrayList<NextXTPNstep> transInputClassical = null; //new ArrayList<>(); //wypełnione później, poza metodą
        //ArrayList<NextXTPNstep> transOtherClassical = null; //new ArrayList<>(); //wypełnione jeszcze później, po aktualizacji czasów
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
                        placesMaturity.add(new NextXTPNstep(place, timeDifference, 0)); //token dojrzeje za timeDifference
                    } else if (timeDifference < currentMinTime) {
                        // wcześniejsze rekordy, jeśli istnieją, zajdą za późniejszy czas:
                        placesMaturity.clear();
                        placesAging.clear();
                        //transProdStart.clear(); //na tym etapie są jeszcze puste, pozostawić ten komentarz
                        //transProdEnd.clear();

                        placesMaturity.add(new NextXTPNstep(place, timeDifference, 0)); //token dojrzeje za timeDifference
                        currentMinTime = timeDifference;
                    }
                }

                if(kappa < gammaMax) { //obliczanie minimalnego czasu do usunięcia tokenu
                    timeDifference = gammaMax - kappa;
                    if(Math.abs(timeDifference - currentMinTime) < sg.calculationsAccuracy) { //uznajmy, że to ten sam czas
                        placesAging.add(new NextXTPNstep(place, timeDifference, 1)); //token zniknie za timeDifference
                    } else if (timeDifference < currentMinTime) {
                        // wcześniejsze rekordy, jeśli istnieją, zajdą za późniejszy czas:
                        placesMaturity.clear();
                        placesAging.clear();
                        //transProdStart.clear(); //na tym etapie są jeszcze puste, pozostawić ten komentarz
                        //transProdEnd.clear();

                        placesAging.add(new NextXTPNstep(place, timeDifference, 1));  //token zniknie za timeDifference
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
                    transProdStart.add(new NextXTPNstep(transition, timeDifference, 2)); //tranzycja się uruchomi za timeDifference
                } else if (timeDifference < currentMinTime) {
                    // wcześniejsze rekordy, jeśli istnieją, zajdą za późniejszy czas:
                    placesMaturity.clear();
                    placesAging.clear();
                    transProdStart.clear();
                    transProdEnd.clear();

                    transProdStart.add(new NextXTPNstep(transition, timeDifference, 2));  //tranzycja się uruchomi za timeDifference
                    currentMinTime = timeDifference;
                }
            }

            if(transition.isBetaActiveXTPN() && transition.isProducing_xTPN()) { //tylko dla Beta-DPN i to tych produkujących tokeny
                timeDifference = tauBeta - timerBeta;
                if(Math.abs(timeDifference - currentMinTime) < sg.calculationsAccuracy) { //uznajmy, że to ten sam czas
                    transProdEnd.add(new NextXTPNstep(transition, timeDifference, 3)); //tranzycja skończy produkcję za timeDifference
                } else if (timeDifference < currentMinTime) {
                    // wcześniejsze rekordy, jeśli istnieją, zajdą za późniejszy czas:
                    placesMaturity.clear();
                    placesAging.clear();
                    transProdStart.clear();
                    transProdEnd.clear();

                    transProdEnd.add(new NextXTPNstep(transition, timeDifference, 3));  //tranzycja skończy produkcję za timeDifference
                    currentMinTime = timeDifference;
                }
            }
        }
        stateVector.add(placesMaturity);
        stateVector.add(placesAging);
        stateVector.add(transProdStart);
        stateVector.add(transProdEnd);
        stateVector.add(null); //miejsce na klasyczne aktywne wejściowe
        stateVector.add(null); //miejsce na klasyczne aktywne wewnętrzne / wyjściowe

        int elements = placesMaturity.size() + placesAging.size() + transProdStart.size() + transProdEnd.size();
        ArrayList<NextXTPNstep> specialVector = new ArrayList<>();
        specialVector.add( new NextXTPNstep(null, currentMinTime, elements) );
        //ostatnia, piąta lista zawiera tylko jeden wpis, informujący ile obiektów
        //NextXTPNstep łącznie wpisano do tej pory oraz jaki jest minimalny czas.
        stateVector.add(specialVector);
        return stateVector;
    }

    /**
     * Metoda zwiększa czas wszystkich czasowych komponentów sieci XTPN o zadaną wielkość.
     * @param tau (<b>double</b>) wartość czasu.
     */
    public void updateNetTime(double tau) {
        for(Place place : places) {
            if(place.isGammaModeActiveXTPN()) { //tylko dla miejsc czasowych
                place.incTokensTime_XTPN(tau);
            }
        }
        for(Transition transition : transitions) {
            if(transition.isAlphaActiveXTPN() && transition.isActivated_xTPN()) { //aktywna tranzycja Alfa
                transition.updateTimerAlfa_XTPN(tau);
                transition.setActivationStatusXTPN(true);
                continue;
            }

            if(transition.isBetaActiveXTPN() && transition.isProducing_xTPN()) { //produkująca tranzycja Beta
                transition.updateTimerBeta_XTPN(tau);
                transition.setProductionStatus_xTPN(true);
                continue;
            }
        }
    }

    /**
     * Aktywacja tranzycji XTPN. W przypadku gdy aktywna jest tranzycja z alfa=beta=OFF, wtedy
     * dodawana jest do listy zwracanej jako wynik.
     * @return (<b>ArrayList[NextXTPNstep]</b>) - lista tranzycji klasycznych aktywnych.
     */
    public ArrayList<NextXTPNstep> activateTransitionsInNewTimeState() {
        ArrayList<NextXTPNstep> classicalInternalTransitions = new ArrayList<>();

        for(Transition transition : transitions) {
            //produkujące nas nie interesują, ani też już aktywne:
            if(transition.isProducing_xTPN() || transition.isActivated_xTPN())
                continue;

            if(transition.getActiveStatusXTPN()) { //czy tranzycja jest aktywna?
                /**
                 * Tutaj do rozważenia: technicznie mogą istnieć tokeny, które są maks. stare, ale jeszcze są w multizbiorze
                 * i jakakolwiek tranzycja aktywna dzięki nim, może przestać być aktywna w nowym stanie...
                 * Z drugiej strony, aktywowane tutaj tranzycje długo takimi nie będą, a klasyczne mają szansę się jeszcze odpalić (time=0).
                 */
                if(transition.isAlphaActiveXTPN()) { //typ alfa
                    double min = transition.getAlphaMin_xTPN();
                    double max = transition.getAlphaMax_xTPN();
                    double rand = getRandomValue(min, max);

                    transition.setTauAlpha_xTPN( rand );
                    transition.setTimerAlfa_XTPN(0.0);
                    transition.setActivationStatusXTPN(true);
                    continue;
                } else if(transition.getActiveStatusXTPN()) { //tylko typ beta
                    double min = transition.getBetaMin_xTPN();
                    double max = transition.getBetaMax_xTPN();
                    double rand = getRandomValue(min, max);

                    transition.setTauBeta_xTPN( rand );
                    transition.setTimerBeta_XTPN(0.0);
                    transition.setProductionStatus_xTPN(true);
                    continue;
                } else { //ani alfa, ani beta:
                    classicalInternalTransitions.add( new NextXTPNstep(transition, -1, 2));
                    //technicznie typ 2 i 3: tak tranzycja jeśli się uruchomi zabierze tokeny oraz wyprodukuje
                    //natychmiast nowe
                }
            }
        }
        return classicalInternalTransitions;
    }

    /**
     * Metoda aktywuje wszystkie nieaktywowane i nieprodukujące tranzycje wejściowe. Jeśli są
     * typu non-alfa i non-beta, to dodaje taką do listy uruchomień na później (bo i tak nie ma
     * jak ustawić zegara czasowego).
     * Dodatkowo sprawdza, czy aktywne tranzycje wciąż mogą takie pozostać. Jak nie - deaktywuje.
     *
     * @return (<b>ArrayList[NextXTPNstep]</b>) - lista klasycznych, wejściowych i aktywnych tranzycji.
     */
    public ArrayList<NextXTPNstep> revalidateNetState() {
        ArrayList<NextXTPNstep> classicalInputOnes = new ArrayList<>(); //klasyczne wejściowe będą uruchamiane osobno 50/50
        //tutaj uruchamiany tranzycje wejściowe, one są niewrażliwe na zmiany w tokenach
        for(Transition transition : transitions) {
            if(!transition.isActivated_xTPN() && !transition.isProducing_xTPN()) { //nieaktywowana, nieprodukująca
                if(transition.getInArcs().size() == 0) { //tranzycja wejściowa, więc aktywna teoretycznie
                    if(transition.isAlphaActiveXTPN()) { //typ alfa, ustaw zegar
                        double min = transition.getAlphaMin_xTPN();
                        double max = transition.getAlphaMax_xTPN();

                        double rand = getRandomValue(min, max);
                        transition.setTauAlpha_xTPN( rand );
                        transition.setTimerAlfa_XTPN(0.0);
                        transition.setActivationStatusXTPN(true);
                        continue;
                    } else if(transition.getActiveStatusXTPN()) { //tylko typ beta
                        double min = transition.getBetaMin_xTPN();
                        double max = transition.getBetaMax_xTPN();

                        double rand = getRandomValue(min, max);
                        transition.setTauBeta_xTPN( rand );
                        transition.setTimerBeta_XTPN(0.0);
                        transition.setProductionStatus_xTPN(true);
                        continue;
                    } else { //ani alfa, ani beta
                        classicalInputOnes.add( new NextXTPNstep(transition, -1, 3));
                        //technicznie typ 3: productionEnds, bo ta tranzycja niczego nie zabierze, ale (być może)
                        //uruchomi się w obliczanym stanie (P=50/50) i wyprodukuje tokeny.
                    }
                }
            }
            //tutaj trzeba jednak sprawdzić globalny stan, pod koniec ostatniego mogły zniknąć zbyt stare tokeny:
            if(transition.isActivated_xTPN()) {
                if(transition.getInArcs().size() == 0)
                    continue; //wejściowa, jak jest aktywna, to pozostanie

                if(!transition.getActiveStatusXTPN()) {
                    transition.deactivateXTPN();
                }
            }

        }
        return classicalInputOnes;
    }

    /**
     * Zwraca wartość losową pomiędzy min a max, jeśli są takie same, wtedy zwraca min. Chodzi o to, że
     * zgadywanie czasu dla double jest problematyczne, i NIE CHCEMY uruchamiać .nextDouble gdy np.
     * min i max są równe.
     * @param min (<b>double</b>) minimalna wartość.
     * @param max (<b>double</b>) maksymalna wartość.
     * @return (<b>double</b>) - wartość losowa [min, max) lub min jeśli są równe.
     */
    private double getRandomValue(double min, double max) {
        double range =  max - min;
        if(range < sg.calculationsAccuracy) { //alfaMin=Max lub zero
            return min;
        } else {
            return generator.nextDouble(min, max);
        }
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
                            GUIManager.getDefaultGUIManager().log("Error, no active transition but option: generateValidLaunchingTransitions "
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
