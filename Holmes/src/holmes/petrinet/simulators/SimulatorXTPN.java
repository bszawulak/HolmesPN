package holmes.petrinet.simulators;

import java.util.ArrayList;
import java.util.Collections;

import holmes.darkgui.GUIManager;
import holmes.petrinet.elements.*;
import holmes.petrinet.functions.FunctionsTools;

import static holmes.graphpanel.EditorResources.actXTPNcolor;

/**
 * Silnik symulatora XTPN. Procedury odpowiedzialne za symulację modelu
 * opartego na XTPN.
 * @author MR
 */
public class SimulatorXTPN implements IEngine {
    private GUIManager overlord;
    private SimulatorGlobals sg;
    private SimulatorGlobals.SimNetType netSimTypeXTPN = SimulatorGlobals.SimNetType.XTPN;
    private ArrayList<TransitionXTPN> transitions;
    private ArrayList<PlaceXTPN> places;
    private ArrayList<Integer> transitionsIndexList = null;
    private ArrayList<TransitionXTPN> launchableTransitions = null;
    private IRandomGenerator generator;

    private boolean graphicalSimulation = false;

    public void setGraphicalSimulation(boolean status) {
        this.graphicalSimulation = status;
    }

    /**
     * Klasa kontener, informacje o następnej zmianie stanu sieci XTPN.
     */
    public static class NextXTPNstep {
        Node nodeTP;
        double timeToChange;

        /**
         * 0 - timeToMature, 1 - timeToDie, 2 - transProdStarts, 3 - transProdEnds; 4 - classical transition<br>
         * <b>W przypadku InfoNode: liczba obiektów NextXTPNstep w arraylistach.</b>
         */
        int changeType;

        public NextXTPNstep(Node n, double tau, int change) {
            nodeTP = n;
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
        this.sg = overlord.simSettings;

        transitions = new ArrayList<>();
        places = new ArrayList<>();
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
        initiateXTPNtransitions(transitions);
        initiateXTPNplaces(places);
        transitionsIndexList = new ArrayList<Integer>();
        launchableTransitions =  new ArrayList<TransitionXTPN>(); //TODO REMOVE

        for(int t = 0; t<transitions.size(); t++) {
            transitionsIndexList.add(t);
        }
    }

    private void initiateXTPNtransitions(ArrayList<Transition> inputT) {
        transitions.clear();
        for(Transition trans : inputT) {
            if( !(trans instanceof TransitionXTPN)) {
                transitions.clear();
                overlord.log("Error, non-XTPN transitions found in list sent into SimulatorXTPN!", "error", false);
                return;
            }
            transitions.add( (TransitionXTPN) trans);
        }
    }

    private void initiateXTPNplaces(ArrayList<Place> inputP) {
        places.clear();
        for(Place place : inputP) {
            if( !(place instanceof PlaceXTPN)) {
                transitions.clear();
                overlord.log("Error, non-XTPN places found in list sent into SimulatorXTPN!", "error", false);
                return;
            }
            places.add( (PlaceXTPN) place);
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
     * Metoda aktywuje wszystkie nieaktywowane i nieprodukujące tranzycje, także wejściowe. Jeśli są
     * typu non-alfa i non-beta, to dodaje taką wejściową do listy uruchomień na później (bo i tak nie ma
     * jak ustawić zegara czasowego). Dodatkowo sprawdza, czy aktywne tranzycje wciąż mogą takie pozostać.
     * Jak nie, to je deaktywuje.
     * @return (<b>ArrayList[NextXTPNstep]</b>) - lista klasycznych, wejściowych i aktywnych tranzycji.
     */
    public ArrayList<NextXTPNstep> revalidateNetState() {
        //czyszczenie miejsc ze starych tokenów:
        for(PlaceXTPN place : places) {
            place.removeOldTokens_XTPN();
        }

        ArrayList<NextXTPNstep> classicalInputOnes = new ArrayList<>(); //klasyczne wejściowe będą uruchamiane osobno 50/50
        //tutaj uruchamiany tranzycje wejściowe, one są niewrażliwe na zmiany czasów w tokenach
        for(TransitionXTPN transition : transitions) {
            if(transition.isProducing_xTPN()) { //produkujące zostawiamy w spokoju
                continue;
            }

            if(!transition.isActivated_xTPN()) { //nieaktywowana
                if(transition.isInputTransition()) { //tranzycja wejściowa, więc może być aktywna, więc aktywujemy poniżej
                    if(transition.isAlphaActiveXTPN()) { //typ alfa, ustaw zegar
                        double min = transition.getAlphaMin_xTPN();
                        double max = transition.getAlphaMax_xTPN();
                        double rand = getSafeRandomValueXTPN(min, max);

                        if(rand < sg.getCalculationsAccuracy()) {
                            // Może tak być, że jest typu alfa, ale ma range alfa = 0, wtedy bety muszą być
                            // niezerowe (zabezpieczenie) - ustawiamy więc ich wartości czasowe i stan tranzycji
                            // na produkujący. Jeśli alfa=OFF i beta=OFF, to tu i tak nie wejdziemy.
                            min = transition.getBetaMin_xTPN();
                            max = transition.getBetaMax_xTPN();
                            rand = getSafeRandomValueXTPN(min, max);
                            assert (rand > sg.getCalculationsAccuracy()) : "Alfy są zerami, beta też?! Jakim cudem?";

                            transition.setTauBeta_xTPN( rand );
                            transition.setTimerBeta_XTPN(0.0);
                            transition.setProductionStatus_xTPN(true);
                        } else { //zakres alfa nie jest zerowy:
                            transition.setTauAlpha_xTPN( rand );
                            transition.setTimerAlfa_XTPN(0.0);
                            transition.setActivationStatusXTPN(true);
                        }
                        continue;
                    } else if(transition.isBetaActiveXTPN()) { //tylko typ beta
                        double min = transition.getBetaMin_xTPN();
                        double max = transition.getBetaMax_xTPN();

                        double rand = getSafeRandomValueXTPN(min, max);
                        transition.setTauBeta_xTPN( rand );
                        transition.setTimerBeta_XTPN(0.0);
                        transition.setProductionStatus_xTPN(true);
                        continue;
                    } else { //ani alfa, ani beta
                        //TODO: immediate or 50/50
                        //if ((generator.nextInt(100) < 50) || transition.isImmediateXTPN()) {
                            //classicalInputOnes.add(new NextXTPNstep(transition, -1, 3));
                            //transition.setProductionStatus_xTPN(true);
                        //}

                        //technicznie typ 3: productionEnds, bo ta tranzycja niczego nie zabierze, ale (być może)
                        //uruchomi się w obliczanym stanie (P=50/50) i wyprodukuje tokeny.
                    }
                } else { // nie jest wejściowa, jest nieaktywna:
                    if(transition.getActiveStatusXTPN(sg.getCalculationsAccuracy())) { //sprawdź zbiór aktywujący, czy może stać się aktywna
                        //dla poprzedniego dużego if'a odpowiednikiem tego było sprawdzanie, czy T jest wejściowa
                        if(transition.isAlphaActiveXTPN()) { //typ alfa
                            double min = transition.getAlphaMin_xTPN();
                            double max = transition.getAlphaMax_xTPN();
                            double rand = getSafeRandomValueXTPN(min, max);

                            if(rand < sg.getCalculationsAccuracy()) {
                                // Może tak być, że jest typu alfa, ale ma range alfa = 0, wtedy bety muszą być
                                // niezerowe (zabezpieczenie) - ustawiamy więc ich wartości czasowe i stan tranzycji
                                // na produkujący. Jeśli alfa=OFF i beta=OFF, to tu i tak nie wejdziemy.
                                min = transition.getBetaMin_xTPN();
                                max = transition.getBetaMax_xTPN();
                                rand = getSafeRandomValueXTPN(min, max);
                                assert (rand > sg.getCalculationsAccuracy()) : "Alfy są zerami, beta też?! Jakim cudem?";

                                transition.setTauBeta_xTPN( rand );
                                transition.setTimerBeta_XTPN(0.0);
                                transition.setProductionStatus_xTPN(true);
                            } else { //zakres alfa nie jest zerowy:
                                transition.setTauAlpha_xTPN( rand );
                                transition.setTimerAlfa_XTPN(0.0);
                                transition.setActivationStatusXTPN(true);
                            }
                        } else if(transition.isBetaActiveXTPN()) { // nie jest alfa, to może beta?
                            double min = transition.getBetaMin_xTPN();
                            double max = transition.getBetaMax_xTPN();
                            double rand = getSafeRandomValueXTPN(min, max);

                            transition.setTauBeta_xTPN( rand );
                            transition.setTimerBeta_XTPN(0.0);
                            transition.setProductionStatus_xTPN(true);
                        } else { //nieaktywna, nie wejściowa, może być aktywna, brak alfa i beta -> klasyczna wewnętrzna
                            //TODO: immediate or 50/50
                            if ((generator.nextInt(100) < 50) || transition.isImmediateXTPN()) {
                                //transition.setActivationStatusXTPN(true);
                            }
                        }

                        //jakikolwiek powyższy scenariusz by nie był, jest to aktywna tranzycja, więc:
                        if(graphicalSimulation) { //ustaw zielony kolor łuku
                            ArrayList<Arc> arcs = transition.getInArcs();
                            for (Arc arc : arcs) {
                                arc.setXTPNactStatus(true);
                            }
                        }
                    } //else nie ma: nie jest wejściowa i nie może być aktywna, bo brakuje tokenów. "niech spierdala".
                }
            } else { //tranzycja była do tej pory aktywna, sprawdzamy, czy wciąż w tym stanie może być
                //tutaj trzeba sprawdzić stan, pod koniec ostatniego mogły zniknąć zbyt stare tokeny:
                if(transition.isInputTransition()) {
                    continue; //wejściowa, jak jest aktywna, to pozostanie
                }

                if(!transition.getActiveStatusXTPN(sg.getCalculationsAccuracy())) {
                    transition.deactivateXTPN(graphicalSimulation);
                }
            }
        }
        return classicalInputOnes;
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
        ArrayList<NextXTPNstep> transOtherClassical = new ArrayList<>();
        double currentMinTime = Double.MAX_VALUE;

        for(PlaceXTPN place : places) { //znajdź najmniejszy czas do zmiany w miejscach
            if(!place.isGammaModeActiveXTPN() || place.accessMultiset().size() == 0) {
                //czyli nie dotyczy miejsc klasycznych, lub pusty multizbiór
                continue;
            }

            double gammaMin = place.getGammaMin_xTPN();
            double gammaMax = place.getGammaMax_xTPN();
            double timeDifference;

            //założenie: na liście tokeny zawsze od największych, do najmniejszych
            boolean closestToGammaMaxSearchMakesNoSense = false;

            for(double kappa : place.accessMultiset()) {
                if(kappa < gammaMin) { //obliczanie minimalnego czasu do dorośnięcia tokenu
                    timeDifference = gammaMin - kappa;
                    assert (timeDifference >= 0);
                    if(timeDifference > currentMinTime) {
                        break; // dalej już tylko mniejsze, koniec dla multizbioru
                        //jeżeli czas do dorośnięcia jest większy niż aktualny najmniejszy czas do zmiany, to
                        //zostawiamy token w spokoju. Ponieważ kolejne tokeny będa tylko mniejsze, to timeDifference
                        //będzie tylko coraz większy względem currentMinTime, więc break.
                    }

                    if(currentMinTime - timeDifference > 0 &&
                            !(Math.abs(currentMinTime - timeDifference) < sg.getCalculationsAccuracy())) {
                        //czyli currentMinTime > timeDifference ALE nieprawdą jest, że różnica między nimi jest mniejsza
                        //niż dokładność obliczeń.
                        //wcześniejsze rekordy, jeśli istnieją, zajdą za późniejszy czas:
                        placesMaturity.clear();
                        placesAging.clear();
                        //transProdStart.clear(); //na tym etapie są jeszcze puste, pozostawić ten komentarz
                        //transProdEnd.clear();
                        currentMinTime = timeDifference;
                    }
                    //token dojrzeje za timeDifference, podtyp 0
                    placesMaturity.add(new NextXTPNstep(place, timeDifference, 0));
                    continue;
                }

                if( (kappa < gammaMax) && !closestToGammaMaxSearchMakesNoSense ) { //obliczanie minimalnego czasu do usunięcia tokenu
                    timeDifference = gammaMax - kappa;
                    assert (timeDifference >= 0);
                    if(timeDifference > currentMinTime) {
                        closestToGammaMaxSearchMakesNoSense = true; //kolejne będą tylko mniejsze
                        continue;
                        //jeżeli czas do usunięcia tokenu jest większy niż aktualny najmniejszy czas do zmiany, to
                        //zostawiamy token w spokoju.
                    }

                    if(currentMinTime - timeDifference > 0 &&
                            !(Math.abs(currentMinTime - timeDifference) < sg.getCalculationsAccuracy())) {
                        //czyli currentMinTime > timeDifference ALE nieprawdą jest, że różnica między nimi jest mniejsza
                        //niż dokładność obliczeń.
                        //wcześniejsze rekordy, jeśli istnieją, zajdą za późniejszy czas:
                        placesMaturity.clear();
                        placesAging.clear();
                        //transProdStart.clear(); //na tym etapie są jeszcze puste, pozostawić ten komentarz
                        //transProdEnd.clear();
                        currentMinTime = timeDifference;
                    }
                    //token zniknie za timeDifference, podtyp 1
                    placesAging.add(new NextXTPNstep(place, timeDifference, 1));
                }
            }
        }

        for(TransitionXTPN transition : transitions) { //znajdź najmniejszy czas do zmiany w tranzycjach
            //podtyp: 0 - timeToMature, 1 - timeToDie, 2 - transProdStarts, 3 - transProdEnds; 4 - classical transition
            //znajdujemy wszystkie tranzycje klasyczne, które są niezależne od czasu:
            if(!transition.isAlphaActiveXTPN() && !transition.isBetaActiveXTPN()) { //tranzycje klasyczne
                if(transition.getActiveStatusXTPN(sg.getCalculationsAccuracy())) { //jeśli jest aktywna
                    transOtherClassical.add(new NextXTPNstep(transition, 0, 4)); //do aktywacji i uruchomienia, podtyp 4
                }
                continue;
            }

            double tauAlpha = transition.getTauAlpha_xTPN();
            double tauBeta = transition.getTauBeta_xTPN();
            double timerAlpha = transition.getTimerAlfa_XTPN();
            double timerBeta = transition.getTimerBeta_XTPN();
            double timeDifference;

            if(transition.isAlphaActiveXTPN() && transition.isActivated_xTPN()) { //tylko dla Alfa-TPN i to tych aktywnych
                timeDifference = tauAlpha - timerAlpha;
                assert (timeDifference >= 0);
                if(timeDifference > currentMinTime) {
                    continue;
                    //jeżeli czas do aktywacji jest większy niż aktualny najmniejszy czas do zmiany, to
                    //zostawiamy tranzycję w spokoju.
                }

                if(currentMinTime - timeDifference > 0 &&
                        !(Math.abs(currentMinTime - timeDifference) < sg.getCalculationsAccuracy())) {
                    //czyli currentMinTime > timeDifference ALE nieprawdą jest, że różnica między nimi jest mniejsza
                    //niż dokładność obliczeń.

                    // wcześniejsze rekordy, jeśli istnieją, zajdą za późniejszy czas, więc reset:
                    placesMaturity.clear();
                    placesAging.clear();
                    transProdStart.clear();
                    transProdEnd.clear();
                    currentMinTime = timeDifference;
                }
                //tranzycja XTPN uruchomi się za timeDifference, podtyp 2
                transProdStart.add(new NextXTPNstep(transition, timeDifference, 2));

                if(!transition.isBetaActiveXTPN()) {
                    //jeśli nie ma trybu beta, tylko Alfa, czyli TPN, dodaj do listy tych, które wyprodukują też tokeny
                    transProdEnd.add(new NextXTPNstep(transition, timeDifference, 3)); //produkcja ASAP
                }
                continue;
            }

            if(transition.isProducing_xTPN()) {
                if(transition.isBetaActiveXTPN() ) { //produkująca tranzycja XTPN
                    timeDifference = tauBeta - timerBeta;
                    assert (timeDifference >= 0);
                    if(timeDifference > currentMinTime) {
                        continue;
                        //jeżeli czas do zakończenia produkcji jest większy niż aktualny najmniejszy czas do zmiany, to
                        //zostawiamy tranzycję w spokoju.
                    }
                    if(currentMinTime - timeDifference > 0 &&
                            !(Math.abs(currentMinTime - timeDifference) < sg.getCalculationsAccuracy())) {
                        //czyli currentMinTime > timeDifference ALE nieprawdą jest, że różnica między nimi jest mniejsza
                        //niż dokładność obliczeń.
                        // wcześniejsze rekordy, jeśli istnieją, zajdą za późniejszy czas:
                        placesMaturity.clear();
                        placesAging.clear();
                        transProdStart.clear();
                        transProdEnd.clear();
                        currentMinTime = timeDifference;
                    }
                    //tranzycja XTPN wyprodukuje tokeny za timeDifference, podtyp 3
                    transProdEnd.add(new NextXTPNstep(transition, timeDifference, 3));
                } else { //czysty TPN, bez bety
                    currentMinTime = 0.0;
                    transProdEnd.add(new NextXTPNstep(transition, 0.0, 3)); //produkcja ASAP
                }
            }
        }
        stateVector.add(placesMaturity);
        stateVector.add(placesAging);
        stateVector.add(transProdStart);
        stateVector.add(transProdEnd);
        stateVector.add(null); //miejsce na klasyczne aktywne wejściowe
        stateVector.add(transOtherClassical); //miejsce na klasyczne aktywne wewnętrzne / wyjściowe

        int elements = placesMaturity.size() + placesAging.size() + transProdStart.size() + transProdEnd.size();
        ArrayList<NextXTPNstep> specialVector = new ArrayList<>();

        if(currentMinTime == Double.MAX_VALUE) {
            currentMinTime = 0;
        }

        if( (currentMinTime > Double.MAX_VALUE - 1) && transOtherClassical.size() > 0) {
            //znaleziono tylko aktywne klasyczne, nic więcej
            currentMinTime = 0;
        }

        specialVector.add(new NextXTPNstep(null, currentMinTime, elements));
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
        for(PlaceXTPN place : places) {
            if(place.isGammaModeActiveXTPN()) { //tylko dla miejsc czasowych
                place.incTokensTime_XTPN(tau);
            }
        }
        for(TransitionXTPN transition : transitions) {
            if(transition.isAlphaActiveXTPN() && transition.isActivated_xTPN()) { //aktywna tranzycja Alfa
                transition.updateTimerAlfa_XTPN(tau);

                //transition.setActivationStatusXTPN(true);
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
     * Tutaj zapewne zmiana stanu niektórych tranzycji...
     * @param launchedTransitions (<b>ArrayList[ArrayList[Transition]]</b>) podwójna lista tranzycji które się uruchomiły: XTPN i klasyczne
     */
    public void endSubtractPhase(ArrayList<ArrayList<TransitionXTPN>> launchedTransitions) {
        ArrayList<TransitionXTPN> launchedXTPN = launchedTransitions.get(0);
        ArrayList<TransitionXTPN> launchedClassical = launchedTransitions.get(1);

        for(TransitionXTPN transition : launchedXTPN) {
            transition.setLaunching(false);
            if(Math.abs(transition.getTauAlpha_xTPN() - transition.getTimerAlfa_XTPN()) < sg.getCalculationsAccuracy()) {
                //jeśli timerAlfa = tauAlfa
                if(transition.isBetaActiveXTPN()) {
                    //czas na przejście w stan Beta
                    transition.setProductionStatus_xTPN(true); //samo zrobi activation=false
                    double min = transition.getBetaMin_xTPN();
                    double max = transition.getBetaMax_xTPN();
                    double rand = getSafeRandomValueXTPN(min, max);
                    transition.setTauBeta_xTPN( rand );
                    transition.setTimerBeta_XTPN(0.0);
                    transition.setTauAlpha_xTPN(-1.0);
                    transition.setTimerAlfa_XTPN(-1.0);
                } else { //czysty TPN
                    transition.setProductionStatus_xTPN(true);
                }
            }
        }

        for(TransitionXTPN transition : launchedClassical) {
            transition.setProductionStatus_xTPN(true); //samo ustawi false na activationStatus
        }
    }

    /**
     * Metoda uruchamia fazę faktycznego dodawania tokenów do miejsc wyjściowych odpalonych tranzycji
     */
    public void endProductionPhase(ArrayList<TransitionXTPN> producingTokensTransitionsAll) {
        ArrayList<Arc> arcs;
        for (TransitionXTPN transition : producingTokensTransitionsAll) {
            transition.setLaunching(false);  // skoro tutaj dotarliśmy, to znaczy że tranzycja już
            //swoje zrobiła i jej status aktywnej się kończy w tym kroku
            arcs = transition.getOutArcs();

            //dodaj odpowiednią liczbę tokenów do miejsc
            for (Arc arc : arcs) {
                if(graphicalSimulation) { //koniec zaznaczania łuku jako produkcyjnego
                    arc.setXTPNprodStatus(false);
                }

                PlaceXTPN place = (PlaceXTPN) arc.getEndNode();
                if(!(arc.getArcType() == Arc.TypeOfArc.NORMAL || arc.getArcType() == Arc.TypeOfArc.READARC)) {
                    overlord.log("Error: non-standard arc used to produce tokens: "+place.getName()+
                            " arc: "+ arc, "error", true);
                }

                int weight = arc.getWeight();
                if(transition.isFunctional()) {
                    weight = FunctionsTools.getFunctionalArcWeight(transition, arc, place);
                }
                place.addTokens_XTPN(weight, 0.0);

            }
            transition.deactivateXTPN(graphicalSimulation);
            transition.setProductionStatus_xTPN(false);
        }
        //producingTokensTransitionsAll.clear();  //wyczyść listę tranzycji 'do uruchomienia' (już swoje zrobiły)
    }

    public ArrayList<TransitionXTPN> returnConsumingTransXTPNVector(ArrayList<ArrayList<NextXTPNstep>> nextXTPNsteps) {
        //nextXTPNsteps : [0] places maturity [1] places aging [2] transProdStart [3] transProdEnd
        //              [4] transInputClassical [5] transOtherClassical
        // konsumujące: 2 oraz 5
        // produkujące: 3, 4 oraz 5
        ArrayList<TransitionXTPN> consumingTokensTransitionsXTPN = new ArrayList<>();
        for(NextXTPNstep element : nextXTPNsteps.get(2)) {
            //if(!((Transition)element.nodeTP).isInputTransition()) {// jeśli NIE jest to tranzycja wejściowa:
            consumingTokensTransitionsXTPN.add((TransitionXTPN) element.nodeTP);
            //}
        }
        Collections.shuffle(consumingTokensTransitionsXTPN);
        return consumingTokensTransitionsXTPN;
    }

    public ArrayList<TransitionXTPN> returnConsumingTransClassicalVector(ArrayList<ArrayList<NextXTPNstep>> nextXTPNsteps) {
        //nextXTPNsteps : [0] places maturity [1] places aging [2] transProdStart [3] transProdEnd
        //              [4] transInputClassical [5] transOtherClassical
        // konsumujące: 2 oraz 5
        // produkujące: 3, 4 oraz 5
        ArrayList<TransitionXTPN> consumingTokensTransitionsClassical = new ArrayList<>();
        for(NextXTPNstep element : nextXTPNsteps.get(5)) {
            consumingTokensTransitionsClassical.add((TransitionXTPN) element.nodeTP);
        }
        Collections.shuffle(consumingTokensTransitionsClassical);
        return consumingTokensTransitionsClassical;
    }

    public ArrayList<TransitionXTPN> returnProducingTransVector(ArrayList<ArrayList<NextXTPNstep>> nextXTPNsteps) {
        //nextXTPNsteps : [0] places maturity [1] places aging [2] transProdStart [3] transProdEnd
        //              [4] transInputClassical [5] transOtherClassical
        // konsumujące: 2 oraz 5
        // produkujące: 3, 4 oraz 5
        ArrayList<TransitionXTPN> producingTokensTransitions = new ArrayList<>();
        for(NextXTPNstep element : nextXTPNsteps.get(3)) {
            producingTokensTransitions.add((TransitionXTPN) element.nodeTP);
        }
        for(NextXTPNstep element : nextXTPNsteps.get(4)) {
            producingTokensTransitions.add((TransitionXTPN) element.nodeTP);
        }
        for(NextXTPNstep element : nextXTPNsteps.get(5)) {
            producingTokensTransitions.add((TransitionXTPN) element.nodeTP);
        }
        return producingTokensTransitions;
        //Collections.shuffle(producingTokensTransitionsAll); //bez sensu, przy produkcji kolejność dowolna
    }

    /**
     * Zwraca wartość losową pomiędzy min a max, jeśli są takie same, wtedy zwraca min. Chodzi o to, że
     * zgadywanie czasu dla double jest problematyczne, i NIE CHCEMY uruchamiać .nextDouble gdy np.
     * min i max są równe.
     * @param min (<b>double</b>) minimalna wartość.
     * @param max (<b>double</b>) maksymalna wartość.
     * @return (<b>double</b>) - wartość losowa [min, max) lub min jeśli są równe.
     */
    private double getSafeRandomValueXTPN(double min, double max) {
        double range =  max - min;
        if(range < sg.getCalculationsAccuracy()) { //alfaMin=Max lub zero
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
    @Override
    public ArrayList<Transition> getTransLaunchList(boolean emptySteps) {
        return null; //TODO: dla stateSimulatorXTPN
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
    @Override
    public IRandomGenerator getGenerator() {
        return this.generator;
    }
}
