package holmes.petrinet.simulators.xtpn;

import java.util.ArrayList;
import java.util.Collections;

import holmes.darkgui.GUIManager;
import holmes.darkgui.LanguageManager;
import holmes.petrinet.elements.*;
import holmes.petrinet.functions.FunctionsTools;
import holmes.petrinet.simulators.*;

/**
 * Silnik symulatora XTPN. Wyobraźmy sobie sieć XTPN. Każda tranzycja ma wartości Alfa (TPN) oraz Beta (DPN), a każde
 * miejsce ma zakres Gamma. A teraz kombinujemy. Są tranzycje, które nie mają Alfy - to tranzycje DPN. Są też w modelu
 * tranzycje, które nie mają Bety - to czyste tranzycje TPN. Są miejsca bez wartości Gamma - miejsca klasyczne. Oraz
 * tranzycje bez Alfa i Beta - tranzycje klasyczne. Te ostatnie moga być typu 'immediate' - natychmiastowe, uruchamiają
 * się kiedy tylko mogą. Sieć XTPN jest symulowana po przeskokach czasu tau. Gdyby tranzycja klasyczna natychmiastowa
 * mogła faktycznie z tau=0 się uruchamiać, sieć nie robiła by nic innego i również NIC innego by w niej nie drgnęło nawet.
 * Wspominałem o łukach odczytu? Mogą być. Łuki hamujące? Też. A teraz czytelniku wyobraź sobie jeden model, który ma
 * miejsca i tranzycje i łuki tutaj opisane: tranzycje klasyczne natychmiastowe lub nie, tranzycje TPN, DPN, XTPN, miejsca
 * czasowe i klasyczne. To właśnie robi ten symulator. Chociaż nie do końca. On przeprowadza najważniejsze obliczenia,
 * ale dycyzje zapadają w innym miejscu, np. w GraphicalSimulatorXTPN->StepLoopPerformerXTPN.actionPerformed().
 * Z resztą nieważne. Chyba wystarczająco nakreśliłem poziom zaawansowania poniższego kodu.
 */
public class SimulatorEngineXTPN implements IEngineXTPN {
    private static final GUIManager overlord = GUIManager.getDefaultGUIManager();
    private static final LanguageManager lang = GUIManager.getLanguageManager();
    private SimulatorGlobals sg;
    private SimulatorGlobals.SimNetType netSimTypeXTPN = SimulatorGlobals.SimNetType.XTPN;
    private ArrayList<TransitionXTPN> transitions;
    private ArrayList<PlaceXTPN> places;
    private IRandomGenerator generator;
    private boolean graphicalSimulation = false;

    private boolean globalMAK = false; //mass action kinetics
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
    public SimulatorEngineXTPN() {
        generator = new StandardRandom(System.currentTimeMillis());
        this.sg = overlord.simSettings;
        globalMAK = overlord.getSettingsManager().getValue("simXTPNmassAction").equals("1");

        transitions = new ArrayList<>();
        places = new ArrayList<>();
    }

    /**
     * Ustawianie podstawowych parametrów silnika symulatora XTPN.
     * @param simulationType (<b>NetType</b>) - rodzaj symulowanej sieci (XTPN domyślnie).
     * @param transitions (<b>ArrayList[TransitionXTPN]</b>) - wektor tranzycji XTPN.
     * @param places (<b>ArrayList[PlaceXTPN]</b>) - wektor miejsc XTPN.
     */
    public void setEngine(SimulatorGlobals.SimNetType simulationType, ArrayList<TransitionXTPN> transitions
            , ArrayList<PlaceXTPN> places) {
        this.netSimTypeXTPN = simulationType;
        this.sg = overlord.simSettings;
        this.transitions = transitions;
        this.places = places;

        globalMAK = overlord.getSettingsManager().getValue("simXTPNmassAction").equals("1");

        boolean readArcPreserveTime = overlord.getSettingsManager().getValue("simXTPNreadArcTokens").equals("1");
        sg.setXTPNreadArcPreserveTokensLifetime(readArcPreserveTime);
        boolean readArcDontTakeTokens = overlord.getSettingsManager().getValue("simXTPNreadArcDoNotTakeTokens").equals("1");
        sg.setXTPNreadArcDontTakeTokens(readArcDontTakeTokens);

        if(overlord.simSettings.getGeneratorType() == 1) {
            this.generator = new HighQualityRandom(System.currentTimeMillis());
        } else {
            this.generator = new StandardRandom(System.currentTimeMillis());
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
            overlord.log(lang.getText("LOGentry00399")+" "+simulationType, "error", true);
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
        ArrayList<NextXTPNstep> classicalInputOnes = new ArrayList<>(); //klasyczne wejściowe będą uruchamiane osobno 50/50
        for(PlaceXTPN place : places) { //czyszczenie miejsc ze starych tokenów:
            place.removeOldTokens_XTPN();
        }

        //tutaj uruchamiany tranzycje wejściowe, one są niewrażliwe na zmiany czasów w tokenach
        for(TransitionXTPN transition : transitions) {
            if(transition.isProducing_xTPN()) { //produkujące zostawiamy w spokoju
                continue;
            }
            if(!transition.isActivated_xTPN()) { //nieaktywowana
                if(transition.isInputTransition()) { //tranzycja wejściowa, więc może być aktywna, więc aktywujemy poniżej
                    if(transition.isAlphaModeActive()) { //typ alfa, ustaw zegar
                        double min = transition.getAlphaMinValue();
                        double max = transition.getAlphaMaxValue();
                        double rand = getSafeRandomValueXTPN(transition, min, max);

                        if(rand < sg.getCalculationsAccuracy()) { //jeśli wylosujemy coś poniżej 1e-9
                            // Może tak być, że jest typu alfa, ale ma range alfa = 0, wtedy bety muszą być
                            // niezerowe (zabezpieczenie) - ustawiamy więc ich wartości czasowe i stan tranzycji
                            // na produkujący. Jeśli alfa=OFF i beta=OFF, to tu i tak nie wejdziemy.
                            min = transition.getBetaMinValue();
                            max = transition.getBetaMaxValue();
                            rand = getSafeRandomValueXTPN(transition, min, max);
                            assert (rand > sg.getCalculationsAccuracy()) : "Alfy są zerami, beta też?! Jakim cudem?";

                            transition.setTauBetaValue( rand );
                            transition.setTimerBetaValue(0.0);
                            transition.setProductionStatus_xTPN(true);
                        } else { //zakres alfa nie jest zerowy:
                            transition.setTauAlphaValue( rand );
                            transition.setTimerAlfaValue(0.0);
                            transition.setActivationStatusXTPN(true);
                        }
                    } else if(transition.isBetaModeActive()) { // NIE JEST typu alfa, jest typu beta
                        double min = transition.getBetaMinValue();
                        double max = transition.getBetaMaxValue();
                        double rand = getSafeRandomValueXTPN(transition, min, max);
                        transition.setTauBetaValue( rand );
                        transition.setTimerBetaValue(0.0);
                        transition.setProductionStatus_xTPN(true);

                        if(graphicalSimulation) {
                            for(Arc arc : transition.getOutputArcs()) { //ustaw łuki w tryb produkcji
                                arc.arcXTPNbox.setXTPNprodStatus(true);
                            }
                        }
                    } else { //klasyczna wejściowa
                        classicalInputOnes.add(new NextXTPNstep(transition, -1, 3));
                        //transition.setProductionStatus_xTPN(true);
                        //technicznie typ 3: productionEnds, bo ta tranzycja niczego nie zabierze, ale (być może)
                        //uruchomi się w obliczanym stanie (P=50/50) i wyprodukuje tokeny.
                    }
                } else { // nie jest wejściowa, jest nieaktywna:
                    //sprawdź zbiór aktywujący, czy może stać się aktywna
                    if(transition.isActiveTransitionXTPN(sg.getCalculationsAccuracy())) {
                        // (dla poprzedniego dużego if'a odpowiednikiem tego było sprawdzanie, czy T jest wejściowa)
                        if(transition.isAlphaModeActive()) { //typ alfa
                            double min = transition.getAlphaMinValue();
                            double max = transition.getAlphaMaxValue();
                            double rand = getSafeRandomValueXTPN(transition, min, max);

                            if(rand < sg.getCalculationsAccuracy()) { //jeśli wylosowano < 1e-9
                                // Może tak być, że jest typu alfa, ale ma range alfa = 0, wtedy bety muszą być
                                // niezerowe (zabezpieczenie) - ustawiamy więc ich wartości czasowe i stan tranzycji
                                // na produkujący. Jeśli alfa=OFF i beta=OFF, to tu i tak nie wejdziemy.
                                min = transition.getBetaMinValue();
                                max = transition.getBetaMaxValue();
                                rand = getSafeRandomValueXTPN(transition, min, max);
                                assert (rand > sg.getCalculationsAccuracy()) : "Alfy są zerami, beta też?! Jakim cudem?";
                                transition.setTauBetaValue( rand );
                                transition.setTimerBetaValue(0.0);
                                transition.setProductionStatus_xTPN(true);

                                if(graphicalSimulation) { //ustaw łuki w tryb produkcji
                                    for(Arc arc : transition.getOutputArcs()) {
                                        arc.arcXTPNbox.setXTPNprodStatus(true);
                                    }
                                }
                            } else { //zakres alfa nie jest zerowy:
                                transition.setTauAlphaValue( rand );
                                transition.setTimerAlfaValue(0.0);
                                transition.setActivationStatusXTPN(true);
                            }
                        } else if(transition.isBetaModeActive()) { // nie jest alfa, to może beta?
                            double min = transition.getBetaMinValue();
                            double max = transition.getBetaMaxValue();
                            double rand = getSafeRandomValueXTPN(transition, min, max);
                            transition.setTauBetaValue( rand );
                            transition.setTimerBetaValue(0.0);

                            if(graphicalSimulation) {
                                for(Arc arc : transition.getOutputArcs()) { //ustaw łuki w tryb produkcji
                                    arc.arcXTPNbox.setXTPNprodStatus(true);
                                }
                            }

                            transition.setActivationStatusXTPN(true);
                        }
                        //tu jest else, którego nie ma :) -> nieaktywna, nie wejściowa, może być aktywna,
                        //  brak alfa i beta -> czyli tranzycja klasyczna, wewnętrzna
                        //będzie dodana do listy produkcji w computeNextState() tak czy owak
                        //jakikolwiek powyższy scenariusz by nie był, jest to jednak aktywna tranzycja, więc:
                        if(graphicalSimulation) { //ustaw zielony kolor łuku - tryb potrzymywania aktywacji
                            ArrayList<Arc> arcs = transition.getInputArcs();
                            for (Arc arc : arcs) {
                                arc.arcXTPNbox.setXTPNactStatus(true);
                            }
                        }
                    } //else nie ma: nie jest wejściowa i nie może być aktywna, bo brakuje tokenów. "niech spierdala".
                }
            } else { //tranzycja była do tej pory aktywna, sprawdzamy, czy wciąż w tym stanie może być
                //tutaj trzeba sprawdzić stan, pod koniec ostatniego mogły zniknąć zbyt stare tokeny:
                if(transition.isInputTransition()) {
                    continue; //wejściowa, jak jest aktywna, to pozostanie
                }

                if(!transition.isActiveTransitionXTPN(sg.getCalculationsAccuracy())) {
                    transition.deactivateTransitionXTPN(graphicalSimulation);
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
            if(!place.isGammaModeActive() || place.accessMultiset().isEmpty()) {
                //czyli nie dotyczy miejsc klasycznych, lub pusty multizbiór
                continue;
            }

            double gammaMin = place.getGammaMinValue();
            double gammaMax = place.getGammaMaxValue();
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
            if(!transition.isAlphaModeActive() && !transition.isBetaModeActive()) { //tranzycje klasyczne
                if(transition.isActiveTransitionXTPN(sg.getCalculationsAccuracy())) { //jeśli jest aktywna
                    transOtherClassical.add(new NextXTPNstep(transition, 0, 4)); //do aktywacji i uruchomienia, podtyp 4
                }
                continue;
            }

            double tauAlpha = transition.getTauAlphaValue();
            double tauBeta = transition.getTauBetaValue();
            double timerAlpha = transition.getTimerAlfaValue();
            double timerBeta = transition.getTimerBetaValue();
            double timeDifference;

            //special case: pure-DPN:
            if(!transition.isAlphaModeActive() && transition.isActivated_xTPN()) {
                //czyli w revalidateNetState() stwierdzono że: nie jest wejściowa, nie była aktywna, ale już jest (tokeny)
                //oraz nie ma Alfy - ustawiono status Activated ale jeszcze bez timerów
                timeDifference = tauBeta - timerBeta; //"maximum firing mode DPN"
                if(currentMinTime - timeDifference > 0 &&
                        !(Math.abs(currentMinTime - timeDifference) < sg.getCalculationsAccuracy())) {
                    placesMaturity.clear();
                    placesAging.clear();
                    transProdStart.clear();
                    transProdEnd.clear();
                    currentMinTime = timeDifference;
                }
                transProdStart.add(new NextXTPNstep(transition, timeDifference, 2));
                continue;
            }

            //tylko dla Alfa-TPN i to tych aktywnych:
            if(transition.isAlphaModeActive() && transition.isActivated_xTPN()) {
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

                if(!transition.isBetaModeActive()) {
                    //jeśli nie ma trybu beta, tylko Alfa, czyli TPN, dodaj do listy tych, które wyprodukują też tokeny
                    transProdEnd.add(new NextXTPNstep(transition, timeDifference, 3)); //produkcja ASAP
                }
                continue;
            }

            if(transition.isProducing_xTPN()) {
                if(transition.isBetaModeActive() ) { //produkująca tranzycja XTPN
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

        if( (currentMinTime > Double.MAX_VALUE - 1) && !transOtherClassical.isEmpty()) {
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
     * @param tau (<b>double</b>) wartość czasu o którą aktualizowana jest cała sieć.
     */
    public void updateNetTime(double tau) {
        for(PlaceXTPN place : places) {
            if(place.isGammaModeActive()) { //tylko dla miejsc czasowych
                place.incTokensTime_XTPN(tau);
            }
        }
        for(TransitionXTPN transition : transitions) {
            if(transition.getTimerAlfaValue() >= 0) {
                transition.updateTimerAlfaValue(tau);
            }
            if(transition.getTimerBetaValue() >= 0) {
                transition.updateTimerBetaValue(tau);
            }
        }
    }

    /**
     * Każda tranzycja, która tu trafia zmienia status na rozpoczęcie produkcji. Tranzycje XTPN dostają nowy czasu
     * tau-beta, wszystkie inne tylko: transition.setProductionStatus_xTPN(true);
     * @param launchedTransitions (<b>ArrayList[ArrayList[Transition]]</b>) podwójna lista tranzycji które się uruchomiły: XTPN i klasyczne
     */
    public void endSubtractPhase(ArrayList<ArrayList<TransitionXTPN>> launchedTransitions) {
        ArrayList<TransitionXTPN> launchedXTPN = launchedTransitions.get(0);
        ArrayList<TransitionXTPN> launchedClassical = launchedTransitions.get(1);

        for(TransitionXTPN transition : launchedXTPN) {
            transition.setLaunching(false);
            if(Math.abs(transition.getTauAlphaValue() - transition.getTimerAlfaValue()) < sg.getCalculationsAccuracy()) {
                //jeśli timerAlfa = tauAlfa
                if(transition.isBetaModeActive()) {
                    if(transition.getTimerBetaValue() >= 0) {
                        transition.setProductionStatus_xTPN(true); //samo zrobi activation=false
                        transition.setTauAlphaValue(-1.0);
                        transition.setTimerAlfaValue(-1.0);
                    } else {
                        //czas na przejście w stan Beta
                        transition.setProductionStatus_xTPN(true); //samo zrobi activation=false
                        double min = transition.getBetaMinValue();
                        double max = transition.getBetaMaxValue();
                        double rand = getSafeRandomValueXTPN(transition, min, max);
                        transition.setTauBetaValue(rand);
                        transition.setTimerBetaValue(0.0);
                        transition.setTauAlphaValue(-1.0);
                        transition.setTimerAlfaValue(-1.0);
                    }
                    if(graphicalSimulation) {
                        for(Arc arc : transition.getOutputArcs()) {
                            arc.arcXTPNbox.setXTPNprodStatus(true);
                        }
                    }
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
     * Metoda uruchamia fazę dodawania tokenów do miejsc wyjściowych odpalonych tranzycji. Następnie deaktywuje
     * tranzycje oraz ustawia ich status produkcji na false.
     * @param producingTokensTransitionsAll (<b>ArrayList[TransitionXTPN]</b>) lista tranzycji które mają coś wyprodukować.
     */
    public void endProductionPhase(ArrayList<TransitionXTPN> producingTokensTransitionsAll) {
        ArrayList<Arc> arcs;
        for (TransitionXTPN transition : producingTokensTransitionsAll) {
            transition.setLaunching(false);
            // skoro tutaj dotarliśmy, to znaczy że tranzycja już swoje zrobiła
            // i jej status aktywnej się skończy w tym kroku.
            arcs = transition.getOutputArcs();

            //dodaj odpowiednią liczbę tokenów do miejsc
            for (Arc arc : arcs) {
                if(graphicalSimulation) { //koniec zaznaczania łuku jako produkcyjnego
                    arc.arcXTPNbox.setXTPNprodStatus(false);
                }
                PlaceXTPN place = (PlaceXTPN) arc.getEndNode();
                if(!(arc.getArcType() == Arc.TypeOfArc.NORMAL || arc.getArcType() == Arc.TypeOfArc.READARC)) {
                    String strB = "err.";
                    try {
                        strB = String.format(lang.getText("LOGentry00400"), place.getName(), arc);
                    } catch (Exception e) {
                        overlord.log(lang.getText("LOGentryLNGexc")+" "+"LOGentry00400", "error", true);
                    }
                    overlord.log(strB, "warning", true);
                }

                if(arc.getArcType() == Arc.TypeOfArc.READARC && sg.isXTPNreadArcPreserveTokensLifetime() && place.isGammaModeActive()) {
                    continue; //jeśli sg.isXTPNreadArcActive() == true i readarc, to zwrotem zajmie się pętla niżej
                    //która zwraca tokeny do miejsc ze zmodyfikowanym czasem życia.
                }
                
                if(arc.getArcType() == Arc.TypeOfArc.READARC && sg.isXTPNreadArcDontTakeTokens())  {
                    continue; //tylko jeżeli w ogóle nie pobieramy tokenów z miejsc, to i ich nie zwracamy...
                }

                int weight = arc.getWeight();
                if(transition.fpnExtension.isFunctional()) {
                    weight = FunctionsTools.getFunctionalArcWeight(transition, arc, place);
                }
                place.addTokens_XTPN(weight, 0.0);
            }

            //Zwrot tokenów dla XTPN-read arc
            if(!sg.isXTPNreadArcDontTakeTokens()) { //jeżeli zabieramy tokeny łukiem odczytu
                double tau = transition.getTauBetaValue();
                if(sg.isXTPNreadArcPreserveTokensLifetime()) { //jeśli tryb włączony
                    if(tau < 0)
                        tau = 0.0;

                    for(TransitionXTPN.TokensBack box : transition.readArcReturnVector) {
                        ArrayList<Double> returnedTokens = new ArrayList<>();
                        double gammaMax = box.placeBack.getGammaMaxValue();
                        for(int i=0; i<box.multisetBack.size(); i++) {
                            if(box.multisetBack.get(i) + tau <= gammaMax) {
                                returnedTokens.add(box.multisetBack.get(i) + tau);
                            }
                        }
                        box.placeBack.accessMultiset().addAll(returnedTokens);
                        Collections.sort(box.placeBack.accessMultiset());
                        Collections.reverse(box.placeBack.accessMultiset());

                        box.placeBack.addTokensNumber(returnedTokens.size());
                        box.Clear();
                    }
                }
            } 
            
            
            transition.deactivateTransitionXTPN(graphicalSimulation);
            transition.setActivationStatusXTPN(false);
            transition.setProductionStatus_xTPN(false);
            transition.readArcReturnVector.clear();
        }
    }

    /**
     * Metoda tworzy listę tranzycji XTPN, które w tym kroku muszą pobrać tokeny.
     * @param nextXTPNsteps (<b>ArrayList[ArrayList[NextXTPNstep]]</b>) lista potencjalnych tranzycji.
     * @return (<b>ArrayList[NextXTPNstep]</b>) lista tranzycji XTPN konsumujących tokeny.
     */
    public ArrayList<TransitionXTPN> returnConsumingTransXTPNVector(ArrayList<ArrayList<NextXTPNstep>> nextXTPNsteps) {
        //nextXTPNsteps : [0] places maturity [1] places aging [2] transProdStart [3] transProdEnd
        //              [4] transInputClassical [5] transOtherClassical
        // konsumujące: 2 oraz 5
        // produkujące: 3, 4 oraz 5
        ArrayList<TransitionXTPN> consumingTokensTransitionsXTPN = new ArrayList<>();
        for(NextXTPNstep element : nextXTPNsteps.get(2)) {
            consumingTokensTransitionsXTPN.add((TransitionXTPN) element.nodeTP);
        }

        Collections.shuffle(consumingTokensTransitionsXTPN);
        return consumingTokensTransitionsXTPN;
    }

    /**
     * Metoda tworzy listę tranzycji klasycznych, które w tym kroku muszą pobrać tokeny.
     * @param nextXTPNsteps (<b>ArrayList[ArrayList[NextXTPNstep]]</b>) lista potencjalnych tranzycji.
     * @return (<b>ArrayList[NextXTPNstep]</b>) lista tranzycji klasycznych konsumujących tokeny.
     */
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

    /**
     * Metoda tworzy listę tranzycji XTPN oraz klasycznych, które w tym kroku wyprodukują tokeny.
     * @param nextXTPNsteps (<b>ArrayList[ArrayList[NextXTPNstep]]</b>) lista potencjalnych tranzycji.
     * @return (<b>ArrayList[NextXTPNstep]</b>) lista tranzycji XTPN oraz klasycznych, które wyprodukują tokeny.
     */
    public ArrayList<TransitionXTPN> returnProducingTransVector(ArrayList<ArrayList<NextXTPNstep>> nextXTPNsteps) {
        //nextXTPNsteps : [0] places maturity [1] places aging [2] transProdStart [3] transProdEnd
        //              [4] transInputClassical [5] transOtherClassical
        // konsumujące: 2 oraz 5
        // produkujące: 3, 4 oraz 5
        ArrayList<TransitionXTPN> producingTokensTransitions = new ArrayList<>();
        for(NextXTPNstep element : nextXTPNsteps.get(3)) {
            producingTokensTransitions.add((TransitionXTPN) element.nodeTP);
        }
        //poniższy kod już niepotrzebny, tranzycja i tak jest pod .get(5), a wektor spod .get(4) służy tylko
        //do tego, aby wiedzieć czy to nie koniec symulacji i jest cokolwiek do uruchomienia
        //for(NextXTPNstep element : nextXTPNsteps.get(4)) {
        //    producingTokensTransitions.add((TransitionXTPN) element.nodeTP);
        //}
        for(NextXTPNstep element : nextXTPNsteps.get(5)) {
            producingTokensTransitions.add((TransitionXTPN) element.nodeTP);
        }

        for(NextXTPNstep element : nextXTPNsteps.get(2)) { //dla czystych DPN które są w .get(2) czyli zabierają tokeny
            if( !((TransitionXTPN)element.nodeTP).isAlphaModeActive() && ((TransitionXTPN)element.nodeTP).isBetaModeActive() ) {
                if(((TransitionXTPN)element.nodeTP).getTimerBetaValue() == ((TransitionXTPN)element.nodeTP).getTauBetaValue()) {
                    producingTokensTransitions.add((TransitionXTPN) element.nodeTP);
                }
            }
        }

        return producingTokensTransitions;
        //Collections.shuffle(producingTokensTransitionsAll); //bez sensu, przy produkcji kolejność dowolna
    }

    /**
     * Zwraca wartość losową pomiędzy min a max, jeśli są takie same, wtedy zwraca min. Chodzi o to, że
     * zgadywanie czasu dla double jest problematyczne, i NIE CHCEMY uruchamiać .nextDouble gdy np.
     * min i max są równe.
     * @param transition (<b>TransitionXTPN</b>) obiekt tranzycji.
     * @param min (<b>double</b>) minimalna wartość.
     * @param max (<b>double</b>) maksymalna wartość.
     * @return (<b>double</b>) - wartość losowa [min, max) lub min jeśli są równe.
     */
    private double getSafeRandomValueXTPN(TransitionXTPN transition, double min, double max) {
        double range =  max - min;
        if(range < sg.getCalculationsAccuracy()) { //alfaMin=Max lub zero
            return min;
        } else {
            if(transition.isMassActionKineticsActiveXTPN() || globalMAK) {
                double denominator = transition.maxFiresPossible();
                return generator.nextDouble(min, max) / denominator;
            } else
                return generator.nextDouble(min, max);
        }
    }

    /**
     * Zwraca aktualnie ustawiony generator liczb pseudolosowych.
     * @return IRandomGenerator
     */
    @Override
    public IRandomGenerator getGenerator() {
        return this.generator;
    }
}
