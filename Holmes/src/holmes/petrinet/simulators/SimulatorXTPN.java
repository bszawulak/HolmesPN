package holmes.petrinet.simulators;

import java.util.ArrayList;
import java.util.Collections;

import holmes.darkgui.GUIManager;
import holmes.petrinet.elements.Arc;
import holmes.petrinet.elements.Node;
import holmes.petrinet.elements.Place;
import holmes.petrinet.elements.Transition;
import holmes.petrinet.functions.FunctionsTools;

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
     * Metoda aktywuje wszystkie nieaktywowane i nieprodukujące tranzycje, także wejściowe. Jeśli są
     * typu non-alfa i non-beta, to dodaje taką wejściową do listy uruchomień na później (bo i tak nie ma
     * jak ustawić zegara czasowego). Dodatkowo sprawdza, czy aktywne tranzycje wciąż mogą takie pozostać.
     * Jak nie, to je deaktywuje.
     * @return (<b>ArrayList[NextXTPNstep]</b>) - lista klasycznych, wejściowych i aktywnych tranzycji.
     */
    public ArrayList<NextXTPNstep> revalidateNetState() {
        //czyszczenie miejsc ze starych tokenów:
        for(Place place : places) {
            place.removeOldTokens_XTPN();
        }

        ArrayList<NextXTPNstep> classicalInputOnes = new ArrayList<>(); //klasyczne wejściowe będą uruchamiane osobno 50/50
        //tutaj uruchamiany tranzycje wejściowe, one są niewrażliwe na zmiany czasów w tokenach
        for(Transition transition : transitions) {
            if(transition.isProducing_xTPN()) { //produkujące zostawiamy w spokoju
                continue;
            }

            if(!transition.isActivated_xTPN()) { //nieaktywowana
                if(transition.isInputTransition()) { //tranzycja wejściowa, więc zawsze aktywna
                    if(transition.isAlphaActiveXTPN()) { //typ alfa, ustaw zegar
                        double min = transition.getAlphaMin_xTPN();
                        double max = transition.getAlphaMax_xTPN();
                        double rand = getSafeRandomValueXTPN(min, max);

                        if(rand < sg.calculationsAccuracy) {
                            // Może tak być, że jest typu alfa, ale ma range alfa = 0, wtedy bety muszą być
                            // niezerowe (zabezpieczenie) - ustawiamy więc ich wartości czasowe i stan tranzycji
                            // na produkujący. Jeśli alfa=OFF i beta=OFF, to tu i tak nie wejdziemy.
                            min = transition.getBetaMin_xTPN();
                            max = transition.getBetaMax_xTPN();
                            rand = getSafeRandomValueXTPN(min, max);
                            assert (rand > sg.calculationsAccuracy) : "Alfy są zerami, beta też?! Jakim cudem?";

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
                        classicalInputOnes.add(new NextXTPNstep(transition, -1, 3));
                        transition.setProductionStatus_xTPN(true);
                        //technicznie typ 3: productionEnds, bo ta tranzycja niczego nie zabierze, ale (być może)
                        //uruchomi się w obliczanym stanie (P=50/50) i wyprodukuje tokeny.
                    }
                } else { //nieaktywna, ale nie jest wejściowa
                    if(transition.getActiveStatusXTPN()) { //sprawdź zbiór aktywujący, czy może stać się aktywna
                        if(transition.isAlphaActiveXTPN()) { //typ alfa
                            double min = transition.getAlphaMin_xTPN();
                            double max = transition.getAlphaMax_xTPN();
                            double rand = getSafeRandomValueXTPN(min, max);

                            if(rand < sg.calculationsAccuracy) {
                                // Może tak być, że jest typu alfa, ale ma range alfa = 0, wtedy bety muszą być
                                // niezerowe (zabezpieczenie) - ustawiamy więc ich wartości czasowe i stan tranzycji
                                // na produkujący. Jeśli alfa=OFF i beta=OFF, to tu i tak nie wejdziemy.
                                min = transition.getBetaMin_xTPN();
                                max = transition.getBetaMax_xTPN();
                                rand = getSafeRandomValueXTPN(min, max);
                                assert (rand > sg.calculationsAccuracy) : "Alfy są zerami, beta też?! Jakim cudem?";

                                transition.setTauBeta_xTPN( rand );
                                transition.setTimerBeta_XTPN(0.0);
                                transition.setProductionStatus_xTPN(true);
                            } else { //zakres alfa nie jest zerowy:
                                transition.setTauAlpha_xTPN( rand );
                                transition.setTimerAlfa_XTPN(0.0);
                                transition.setActivationStatusXTPN(true);
                            }
                        } else if(transition.getActiveStatusXTPN()) { //tylko typ beta
                            double min = transition.getBetaMin_xTPN();
                            double max = transition.getBetaMax_xTPN();
                            double rand = getSafeRandomValueXTPN(min, max);

                            transition.setTauBeta_xTPN( rand );
                            transition.setTimerBeta_XTPN(0.0);
                            transition.setProductionStatus_xTPN(true);
                        }

                        //jeśli alfa i beta=OFF, to ich "aktywacją" zajmie się inna metoda
                    }
                }
            } else { //tranzycja była do tej pory aktywna, sprawdzamy, czy wciąż w tym stanie może być
                //tutaj trzeba sprawdzić stan, pod koniec ostatniego mogły zniknąć zbyt stare tokeny:
                if(transition.isInputTransition()) {
                    /*
                    if(Math.abs(transition.getTauAlpha_xTPN() - transition.getTimerAlfa_XTPN()) < sg.calculationsAccuracy) {
                        //jeśli timerAlfa = tauAlfa
                        if(transition.isBetaActiveXTPN()) {
                            //czas na przejście w stan Beta
                            transition.setProductionStatus_xTPN(true); //samo zrobi activation=false
                            double min = transition.getBetaMin_xTPN();
                            double max = transition.getBetaMax_xTPN();
                            double rand = getSafeRandomValueXTPN(min, max);

                            transition.setTauBeta_xTPN( rand );
                            transition.setTimerBeta_XTPN(0.0);
                            transition.setProductionStatus_xTPN(true);
                        } else { //czysty TPN
                            transition.setProductionStatus_xTPN(true);
                        }
                    }
                    */
                    continue; //wejściowa, jak jest aktywna, to pozostanie
                }

                if(!transition.getActiveStatusXTPN()) {
                    transition.deactivateXTPN();
                } else { //czy zmiana stanu Alfa -> Beta
                    /*
                    if(Math.abs(transition.getTauAlpha_xTPN() - transition.getTimerAlfa_XTPN()) < sg.calculationsAccuracy) {
                        if(transition.isBetaActiveXTPN()) {
                            //czas na przejście w stan Beta
                            transition.setProductionStatus_xTPN(true); //samo zrobi activation=false
                            double min = transition.getBetaMin_xTPN();
                            double max = transition.getBetaMax_xTPN();
                            double rand = getSafeRandomValueXTPN(min, max);

                            transition.setTauBeta_xTPN( rand );
                            transition.setTimerBeta_XTPN(0.0);
                            transition.setProductionStatus_xTPN(true);
                        } else { //czysty TPN
                            transition.setProductionStatus_xTPN(true);
                        }
                    }
                    */
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

        for(Place place : places) { //znajdź najmniejszy czas do zmiany w miejscach
            double gammaMin = place.getGammaMin_xTPN();
            double gammaMax = place.getGammaMax_xTPN();
            double timeDifference;

            for(double kappa : place.accessMultiset()) {
                if(!place.isGammaModeActiveXTPN()) //nie dotyczy miejsc klasycznych
                    continue;

                if(kappa < gammaMin) { //obliczanie minimalnego czasu do dorośnięcia tokenu
                    timeDifference = gammaMin - kappa;
                    if(Math.abs(currentMinTime - timeDifference) > sg.calculationsAccuracy) { //czyli timeDifference << currentMinTime
                        // wcześniejsze rekordy, jeśli istnieją, zajdą za późniejszy czas:
                        placesMaturity.clear();
                        placesAging.clear();
                        //transProdStart.clear(); //na tym etapie są jeszcze puste, pozostawić ten komentarz
                        //transProdEnd.clear();
                        currentMinTime = timeDifference;
                    }
                    //token dojrzeje za timeDifference, podtyp 0
                    placesMaturity.add(new NextXTPNstep(place, timeDifference, 0));
                }

                if(kappa < gammaMax) { //obliczanie minimalnego czasu do usunięcia tokenu
                    timeDifference = gammaMax - kappa;
                    if(Math.abs(currentMinTime - timeDifference) > sg.calculationsAccuracy) { //czyli timeDifference << currentMinTime
                        // wcześniejsze rekordy, jeśli istnieją, zajdą za późniejszy czas:
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

        for(Transition transition : transitions) { //znajdź najmniejszy czas do zmiany w tranzycjach
            //podtyp: 0 - timeToMature, 1 - timeToDie, 2 - transProdStarts, 3 - transProdEnds; 4 - classical transition

            //znajdujemy wszystkie tranzycje klasyczne, które są niezależne od czasu:
            if(!transition.isAlphaActiveXTPN() && !transition.isBetaActiveXTPN()) { //tranzycje klasyczne
                if(transition.getActiveStatusXTPN()) { //jeśli jest aktywna
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
                if(Math.abs(currentMinTime - timeDifference) > sg.calculationsAccuracy) { //czyli timeDifference << currentMinTime
                    // wcześniejsze rekordy, jeśli istnieją, zajdą za późniejszy czas, więc reset:
                    placesMaturity.clear();
                    placesAging.clear();
                    transProdStart.clear();
                    transProdEnd.clear();
                    currentMinTime = timeDifference;
                }
                //tranzycja XTPN uruchomi się za timeDifference, podtyp 2
                transProdStart.add(new NextXTPNstep(transition, timeDifference, 2));
            }

            if(transition.isProducing_xTPN()) {
                if(transition.isBetaActiveXTPN() ) { //produkująca tranzycja XTPN
                    timeDifference = tauBeta - timerBeta;
                    if(Math.abs(currentMinTime - timeDifference) > sg.calculationsAccuracy) { //czyli timeDifference << currentMinTime
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
        for(Place place : places) {
            if(place.isGammaModeActiveXTPN()) { //tylko dla miejsc czasowych
                place.incTokensTime_XTPN(tau);
            }
        }
        for(Transition transition : transitions) {
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
    public void endSubtractPhase(ArrayList<ArrayList<Transition>> launchedTransitions) {
        ArrayList<Transition> launchedXTPN = launchedTransitions.get(0);
        ArrayList<Transition> launchedClassical = launchedTransitions.get(1);

        for(Transition transition : launchedXTPN) {
            if(Math.abs(transition.getTauAlpha_xTPN() - transition.getTimerAlfa_XTPN()) < sg.calculationsAccuracy) {
                //jeśli timerAlfa = tauAlfa
                if(transition.isBetaActiveXTPN()) {
                    //czas na przejście w stan Beta
                    transition.setProductionStatus_xTPN(true); //samo zrobi activation=false
                    double min = transition.getBetaMin_xTPN();
                    double max = transition.getBetaMax_xTPN();
                    double rand = getSafeRandomValueXTPN(min, max);
                    transition.setTauBeta_xTPN( rand );
                    transition.setTimerBeta_XTPN(0.0);
                } else { //czysty TPN
                    transition.setProductionStatus_xTPN(true);
                }
            }
        }
        //TODO:
    }

    /**
     * Metoda uruchamia fazę faktycznego dodawania tokenów do miejsc wyjściowych odpalonych tranzycji
     */
    public void endProductionPhase(ArrayList<Transition> producingTokensTransitionsAll) {
        ArrayList<Arc> arcs;
        for (Transition transition : producingTokensTransitionsAll) {
            transition.setLaunching(false);  // skoro tutaj dotarliśmy, to znaczy że tranzycja już
            //swoje zrobiła i jej status aktywnej się kończy w tym kroku
            arcs = transition.getOutArcs();

            //dodaj odpowiednią liczbę tokenów do miejsc
            for (Arc arc : arcs) {
                Place place = (Place) arc.getEndNode();
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
            transition.deactivateXTPN();
            transition.setProductionStatus_xTPN(false);
        }
        //producingTokensTransitionsAll.clear();  //wyczyść listę tranzycji 'do uruchomienia' (już swoje zrobiły)
    }

    public ArrayList<Transition> returnConsumingTransXTPNVector(ArrayList<ArrayList<NextXTPNstep>> nextXTPNsteps) {
        //nextXTPNsteps : [0] places maturity [1] places aging [2] transProdStart [3] transProdEnd
        //              [4] transInputClassical [5] transOtherClassical
        // konsumujące: 2 oraz 5
        // produkujące: 3, 4 oraz 5
        ArrayList<Transition> consumingTokensTransitionsXTPN = new ArrayList<>();
        for(NextXTPNstep element : nextXTPNsteps.get(2)) {
            //if(!((Transition)element.nodeTP).isInputTransition()) {// jeśli NIE jest to tranzycja wejściowa:
            consumingTokensTransitionsXTPN.add((Transition) element.nodeTP);
            //}
        }
        Collections.shuffle(consumingTokensTransitionsXTPN);
        return consumingTokensTransitionsXTPN;
    }

    public ArrayList<Transition> returnConsumingTransClassicalVector(ArrayList<ArrayList<NextXTPNstep>> nextXTPNsteps) {
        //nextXTPNsteps : [0] places maturity [1] places aging [2] transProdStart [3] transProdEnd
        //              [4] transInputClassical [5] transOtherClassical
        // konsumujące: 2 oraz 5
        // produkujące: 3, 4 oraz 5
        ArrayList<Transition> consumingTokensTransitionsClassical = new ArrayList<>();
        for(NextXTPNstep element : nextXTPNsteps.get(5)) {
            consumingTokensTransitionsClassical.add((Transition) element.nodeTP);
        }
        Collections.shuffle(consumingTokensTransitionsClassical);
        return consumingTokensTransitionsClassical;
    }

    public ArrayList<Transition> returnProducingTransVector(ArrayList<ArrayList<NextXTPNstep>> nextXTPNsteps) {
        //nextXTPNsteps : [0] places maturity [1] places aging [2] transProdStart [3] transProdEnd
        //              [4] transInputClassical [5] transOtherClassical
        // konsumujące: 2 oraz 5
        // produkujące: 3, 4 oraz 5
        ArrayList<Transition> producingTokensTransitions = new ArrayList<>();
        for(NextXTPNstep element : nextXTPNsteps.get(3)) {
            producingTokensTransitions.add((Transition) element.nodeTP);
        }
        for(NextXTPNstep element : nextXTPNsteps.get(4)) {
            producingTokensTransitions.add((Transition) element.nodeTP);
        }
        for(NextXTPNstep element : nextXTPNsteps.get(5)) {
            producingTokensTransitions.add((Transition) element.nodeTP);
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
                        if(!isPossibleStep(transitions)) {
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


    /**
     * Aktywacja tranzycji XTPN. W przypadku gdy aktywna jest tranzycja z alfa=beta=OFF, wtedy
     * dodawana jest do listy zwracanej jako wynik.
     */
    public void activateTransitionsInNewTimeState() {
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
                    double rand = getSafeRandomValueXTPN(min, max);

                    transition.setTauAlpha_xTPN( rand );
                    transition.setTimerAlfa_XTPN(0.0);
                    transition.setActivationStatusXTPN(true);
                    continue;
                } else if(transition.getActiveStatusXTPN()) { //tylko typ beta
                    double min = transition.getBetaMin_xTPN();
                    double max = transition.getBetaMax_xTPN();
                    double rand = getSafeRandomValueXTPN(min, max);

                    transition.setTauBeta_xTPN( rand );
                    transition.setTimerBeta_XTPN(0.0);
                    transition.setProductionStatus_xTPN(true);
                    continue;
                }
            }
        }
    }
}
