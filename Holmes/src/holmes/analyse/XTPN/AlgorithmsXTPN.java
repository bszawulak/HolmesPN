package holmes.analyse.XTPN;

import holmes.darkgui.GUIManager;
import holmes.petrinet.elements.Place;
import holmes.petrinet.elements.PlaceXTPN;
import holmes.petrinet.elements.Transition;
import holmes.petrinet.elements.TransitionXTPN;

import java.util.ArrayList;

public class AlgorithmsXTPN {
    private static final GUIManager overlord = GUIManager.getDefaultGUIManager();

    //Alg1:
    ArrayList<TransitionNanoXTPN> preTransitionsNanoA1;
    ArrayList<TransitionNanoXTPN> postTransitionsNanoA1;
    PlaceNanoXTPN placeNanoA1 = null; //obliczanie max liczby tokenów w miejscu, ALG1
    //Alg2:
    TransitionXTPN transLifeA2 = null; //obliczanie żywotności, ALG2
    ArrayList<TransitionNanoXTPN> preTransitionsNanoA2;
    ArrayList<TransitionNanoXTPN> compTransitionsNanoA2;
    ArrayList<PlaceNanoXTPN> prePlacesA2;
    
    long maxSteps; //ile kroków pracy Alg1
    boolean areReadArcs = false; //czy są łuki odczytu w Alg1
    
    boolean Alg1ReadyToProceed;
    
    public class ResultTokensAvg {
        public double avg;
        public double stdDev;
        public double min;
        public double max;
    }

    public AlgorithmsXTPN() {
        preTransitionsNanoA1 = new ArrayList<>();
        postTransitionsNanoA1 = new ArrayList<>();
        maxSteps = 0;
        Alg1ReadyToProceed = false;
    }

    //***************************************************************************************************
    //************************************  ALG1: MAX TOKENS IN PLACE ***********************************
    //***************************************************************************************************
    
    /**
     * Celem tej wspaniałej metody jest ustalenie teoretycznej liczby tokenów w miejscu.
     * @param placeXTPN
     * @return
     */
    public int getTokensPerPlaceAlg1(PlaceXTPN placeXTPN, int digits, int minMaxAvg, boolean considerOutputTransitions) {
        //calculateMaxStepsNumber(placeXTPN, digits, minMaxAvg, considerOutputTransitions);
        if(!Alg1ReadyToProceed)
            return -1;
        
        int result = 0;
        for (long time = 1; time < maxSteps; time++) { //od 1! bo 0 to stan początkowy
            if(!areReadArcs)
                shuffleElements(preTransitionsNanoA1);
            
            if(time == 22) {
                int x =1;
            }

            processInputTransitions(preTransitionsNanoA1, placeNanoA1);
            if(considerOutputTransitions) {
                shuffleElements(preTransitionsNanoA1);
                processOutputTransitions(postTransitionsNanoA1, placeNanoA1);
            }
            
            placeNanoA1.updateTokensSet_N(1);
            if(placeNanoA1.tokens_N.size() > result) {
                result = placeNanoA1.tokens_N.size();
            }
        }
        return result;
    }

    public long calculateMaxStepsNumberPreAlg1(PlaceXTPN placeXTPN, int digits, int minMaxAvg, boolean considerOutputTransitions) {
        //minMaxAvg = -1 - wejściowe tranzycje najszybsze, wyjściowe najwolniejsze
        //minMaxAvg = 0 - średnia z U/L alfa i beta
        //minMaxAvg = 1 - wejściowe tranzycje najwolniejsze, wyjściowe najszybsze

        ArrayList<Transition> preTransitions = placeXTPN.getInputTransitions();
        ArrayList<Transition> postTransitions = placeXTPN.getOutputTransitions();
        ArrayList<Transition> transitions = overlord.getWorkspace().getProject().getTransitions();

        //sprawdzamy, czy wartości alfa, beta i gamma są liczbami wymiernymi:
        boolean areRational = areRationalsAlg1(preTransitions, placeXTPN, digits);
        if(!areRational) { //jeśli nie są, to sprawdzamy wartości dla tranzycji wyjściowych
            areRational = areRationalsAlg1(postTransitions, placeXTPN, digits);
        }
        int multiplier = 1;
        if(areRational) {
            multiplier = digits;
        }
        
        //miejsce do pomiaru tokenów:
        placeNanoA1 = new PlaceNanoXTPN(placeXTPN, overlord.getWorkspace().getProject().getPlaces().indexOf(placeXTPN));
        placeNanoA1.gammaL_N = (int) (placeNanoA1.gammaL * multiplier);
        placeNanoA1.gammaU_N = (int) (placeNanoA1.gammaU * multiplier);
        //TODO: add tokens already in place
        
        //tutaj przygotowujemy obiekty TransitionNanoXTPN i PlaceNanoXTPN do obliczeń:
        ArrayList<Integer> lcmCandidates = new ArrayList<Integer>();
        
        for (Transition t : preTransitions) { //tworzenie listy tranzycji wejściowych do miejsca
            TransitionNanoXTPN nt = new TransitionNanoXTPN((TransitionXTPN) t, transitions.indexOf(t));
            if(((TransitionXTPN) t).isAlphaModeActive()) { //czy tranzycja ma aktywny tryb alfa
                nt.isAlphaType = true;
                nt.alphaL_N = (int) (nt.alphaL * multiplier);
                nt.alphaU_N = (int) (nt.alphaU * multiplier);
            } else {
                nt.isAlphaType = false;
                nt.alphaL = 0;
                nt.alphaU = 0;
            }

            if(((TransitionXTPN) t).isBetaModeActive()) { //czy tranzycja ma aktywny tryb beta
                nt.isBetaType = true;
                nt.betaL_N = (int) (nt.betaL * multiplier);
                nt.betaU_N = (int) (nt.betaU * multiplier);
            } else {
                nt.isBetaType = false;
                nt.betaL = 0;
                nt.betaU = 0;
            }
            
            TransitionNanoXTPN.PlaceArcWeight paw = nt.new PlaceArcWeight();
            paw.place = placeNanoA1;
            paw.weight = t.getOutputArcWeightTo(placeXTPN);
            nt.postPlaces.add(paw);
            
            switch(minMaxAvg) {
                case -1: //wejściowe tranzycje najszybsze
                    nt.tauAlphaLimit = nt.alphaL_N;
                    nt.tauBetaLimit = nt.betaL_N;
                    break;
                case 0: //
                    nt.tauAlphaLimit = (nt.alphaL_N + nt.alphaU_N) / 2;
                    nt.tauBetaLimit = (nt.betaL_N + nt.betaU_N) / 2;
                    break;
                case 1: //wejściowe tranzycje najwolniejsze
                    nt.tauAlphaLimit = nt.alphaU_N;
                    nt.tauBetaLimit = nt.betaU_N;
                    break;
            }
            lcmCandidates.add(nt.tauAlphaLimit + nt.tauBetaLimit);

            if(postTransitions.contains(t)) { //jeśli tranzycja jest zarówno wejściowa, jak i wyjściowa
                nt.hasReadArcToPlace = true;   //to ustawiamy flagę
                areReadArcs = true;
                postTransitions.remove(t); //usuwamy z listy tranzycji wyjściowych
            }

            preTransitionsNanoA1.add(nt);
        }

        if(considerOutputTransitions) { //wyjściowe tranzycje badanego miejsca
            for (Transition t : postTransitions) { //tworzenie listy tranzycji wyjściowych
                TransitionNanoXTPN nt = new TransitionNanoXTPN((TransitionXTPN) t, transitions.indexOf(t));
                if(((TransitionXTPN) t).isAlphaModeActive()) { //czy tranzycja ma aktywny tryb alfa
                    nt.isAlphaType = true;
                    nt.alphaL_N = (int) (nt.alphaL * multiplier);
                    nt.alphaU_N = (int) (nt.alphaU * multiplier);
                } else {
                    nt.isAlphaType = false;
                    nt.alphaL = 0;
                    nt.alphaU = 0;
                }

                if(((TransitionXTPN) t).isBetaModeActive()) { //czy tranzycja ma aktywny tryb beta
                    nt.isBetaType = true;
                    nt.betaL_N = (int) (nt.betaL * multiplier);
                    nt.betaU_N = (int) (nt.betaU * multiplier);
                } else {
                    nt.isBetaType = false;
                    nt.betaL = 0;
                    nt.betaU = 0;
                }

                TransitionNanoXTPN.PlaceArcWeight paw = nt.new PlaceArcWeight();
                paw.place = placeNanoA1;
                paw.weight = t.getInputArcWeightFrom(placeXTPN);
                nt.prePlaces.add(paw);

                //nt.weightToPlace = t.getInputArcWeightFrom(placeXTPN); //tyle tokenów zabierają z miejsca
                switch(minMaxAvg) {
                    case -1: //wyjściowe tranzycje najwolniejsze
                        nt.tauAlphaLimit = nt.alphaU_N;
                        nt.tauBetaLimit = nt.betaU_N;
                        break;
                    case 0: //
                        nt.tauAlphaLimit = (nt.alphaL_N + nt.alphaU_N) / 2;
                        nt.tauBetaLimit = (nt.betaL_N + nt.betaU_N) / 2;
                        break;
                    case 1: //wyjściowe tranzycje najszybsze
                        nt.tauAlphaLimit = nt.alphaL_N;
                        nt.tauBetaLimit = nt.betaL_N;
                        break;
                }
                postTransitionsNanoA1.add(nt);
            }
        }
        
        //wyliczamy maksymalny czas obliczeń:
        int[] arr = lcmCandidates.stream().mapToInt(i -> i).toArray();
        maxSteps = MathToolsXTPN.lcm_of_array_elements(arr);
        maxSteps += placeNanoA1.gammaU_N;

        //symulacja, ustawianie zegarów
        for (TransitionNanoXTPN t : preTransitionsNanoA1) {
            if(t.hasReadArcToPlace) {
                t.tauAlpha_N = -1; //z łukiem odczyty niekoniecznie aktywna
            } else {
                t.tauAlpha_N = 0; //zawsze aktywne
            }
            t.tauBeta_N = -1; //nie produkuje na początku
        }
        for(TransitionNanoXTPN t : postTransitionsNanoA1) {
            t.tauAlpha_N = -1; //niekoniecznie aktywna
            t.tauBeta_N = -1; //nie produkuje na początku
        }
        //maxSteps;

        Alg1ReadyToProceed = true;
        return maxSteps;
    }

    //***************************************************************************************************
    //************************************  ALG2: TRANSITION LIFENESS ***********************************
    //***************************************************************************************************

    /**
     * Celem tej wspaniałej metody jest ustalenie teoretycznej liczby tokenów w miejscu.
     * @param placeXTPN
     * @return
     */
    public int getTokensPerPlaceAlg2(PlaceXTPN placeXTPN, int digits, int minMaxAvg, boolean considerOutputTransitions) {
        //calculateMaxStepsNumber(placeXTPN, digits, minMaxAvg, considerOutputTransitions);
        if(!Alg1ReadyToProceed)
            return -1;

        int result = 0;
        for (long time = 1; time < maxSteps; time++) { //od 1! bo 0 to stan początkowy
            if(!areReadArcs)
                shuffleElements(preTransitionsNanoA1);

            if(time == 22) {
                int x =1;
            }

            processInputTransitions(preTransitionsNanoA1, placeNanoA1);
            if(considerOutputTransitions) {
                shuffleElements(preTransitionsNanoA1);
                processOutputTransitions(postTransitionsNanoA1, placeNanoA1);
            }

            placeNanoA1.updateTokensSet_N(1);
            if(placeNanoA1.tokens_N.size() > result) {
                result = placeNanoA1.tokens_N.size();
            }
        }
        return result;
    }

    public long calculateLifenessPreAlg2(TransitionXTPN t_x, int digits, int inputTransSpeed, int competingTransSpeed) {
        //inputTransSpeed = -1 - WEjściowe tranzycje najszybsze
        //inputTransSpeed = 0 - średnia z U/L alfa i beta
        //inputTransSpeed = 1 - WEjściowe tranzycje najszybsze (default, jak się uda odpalić t_x wtedy, to przy szybszej
        //          produkcji tokenów w jej miejsach wejściowych - tym bardziej
        
        //competingTransSpeed = -1 - konkurencja jest najszybsza (default, jak wyżej)
        //competingTransSpeed = 0 - średnia z U/L alfa i beta
        //competingTransSpeed = 1 - konkurencja jest najwolniejsza
        
        ArrayList<Place> prePlaces = t_x.getInputPlaces();
        ArrayList<Transition> preTransitions = new ArrayList<Transition>();
        ArrayList<Transition> compTransitions = new ArrayList<Transition>();

        ArrayList<Transition> transitions = overlord.getWorkspace().getProject().getTransitions();
        
        for (Place p : prePlaces) {
            for(Transition t : p.getInputTransitions()) {
                if(!preTransitions.contains(t)) {
                    preTransitions.add(t);
                }
            }
        }
        for(Place p : prePlaces) {
            for(Transition t : p.getOutputTransitions()) {
                if(!compTransitions.contains(t) && !t_x.equals(t)) {
                    compTransitions.add(t);
                }
            }
        }
        
        //sprawdzamy, czy wartości alfa, beta i gamma są liczbami wymiernymi:
        ArrayList<Transition> transitionsPrePost = new ArrayList<Transition>();
        transitionsPrePost.addAll(preTransitions);
        transitionsPrePost.addAll(compTransitions);
        transitionsPrePost.add(t_x);
        boolean areRational = areRationalsAlg2(transitionsPrePost, prePlaces, digits);
        int multiplier = 1;
        if(areRational) {
            multiplier = digits;
        }

        //miejsce do pomiaru tokenów:
        
        for(Place p : prePlaces) {
            PlaceNanoXTPN placeNano = new PlaceNanoXTPN((PlaceXTPN) p, overlord.getWorkspace().getProject().getPlaces().indexOf(p));
            placeNano.gammaL_N = (int) (placeNano.gammaL * multiplier);
            placeNano.gammaU_N = (int) (placeNano.gammaU * multiplier);
            //placeNano.addTokens_N(placeNano.gammaL_N); //TODO: good idea
            this.prePlacesA2.add(placeNano);
        }

        //tutaj przygotowujemy obiekty TransitionNanoXTPN do obliczeń:
        ArrayList<Integer> lcmCandidates = new ArrayList<Integer>();

        for (Transition t : preTransitions) { //tworzenie listy tranzycji wejściowych do miejsca
            TransitionNanoXTPN nt = new TransitionNanoXTPN((TransitionXTPN) t, transitions.indexOf(t));
            if(((TransitionXTPN) t).isAlphaModeActive()) { //czy tranzycja ma aktywny tryb alfa
                nt.isAlphaType = true;
                nt.alphaL_N = (int) (nt.alphaL * multiplier);
                nt.alphaU_N = (int) (nt.alphaU * multiplier);
            } else {
                nt.isAlphaType = false;
                nt.alphaL = 0;
                nt.alphaU = 0;
            }

            if(((TransitionXTPN) t).isBetaModeActive()) { //czy tranzycja ma aktywny tryb beta
                nt.isBetaType = true;
                nt.betaL_N = (int) (nt.betaL * multiplier);
                nt.betaU_N = (int) (nt.betaU * multiplier);
            } else {
                nt.isBetaType = false;
                nt.betaL = 0;
                nt.betaU = 0;
            }

            TransitionNanoXTPN.PlaceArcWeight paw = nt.new PlaceArcWeight();
            paw.place = placeNanoA1;
            //paw.weight = t.getOutputArcWeightTo(placeXTPN);
            nt.postPlaces.add(paw);

            //nt.weightToPlace = t.getOutputArcWeightTo(placeXTPN); //tyle tokenów dodają do miejsca

            switch(inputTransSpeed) { //predkość wejściowych tranzycji
                case -1: //wejściowe tranzycje najszybsze
                    nt.tauAlphaLimit = nt.alphaL_N;
                    nt.tauBetaLimit = nt.betaL_N;
                    break;
                case 0: //
                    nt.tauAlphaLimit = (nt.alphaL_N + nt.alphaU_N) / 2;
                    nt.tauBetaLimit = (nt.betaL_N + nt.betaU_N) / 2;
                    break;
                case 1: //wejściowe tranzycje najwolniejsze
                    nt.tauAlphaLimit = nt.alphaU_N;
                    nt.tauBetaLimit = nt.betaU_N;
                    break;
            }
            lcmCandidates.add(nt.tauAlphaLimit + nt.tauBetaLimit);

            if(compTransitions.contains(t)) { //jeśli tranzycja jest zarówno wejściowa, jak i wyjściowa
                nt.hasReadArcToPlace = true;   //to ustawiamy flagę
                areReadArcs = true;
                compTransitions.remove(t); //usuwamy z listy tranzycji wyjściowych
            }

            preTransitionsNanoA1.add(nt);
        }
        
            for (Transition t : compTransitions) { //tworzenie listy tranzycji wyjściowych
                TransitionNanoXTPN nt = new TransitionNanoXTPN((TransitionXTPN) t, transitions.indexOf(t));
                if(((TransitionXTPN) t).isAlphaModeActive()) { //czy tranzycja ma aktywny tryb alfa
                    nt.isAlphaType = true;
                    nt.alphaL_N = (int) (nt.alphaL * multiplier);
                    nt.alphaU_N = (int) (nt.alphaU * multiplier);
                } else {
                    nt.isAlphaType = false;
                    nt.alphaL = 0;
                    nt.alphaU = 0;
                }

                if(((TransitionXTPN) t).isBetaModeActive()) { //czy tranzycja ma aktywny tryb beta
                    nt.isBetaType = true;
                    nt.betaL_N = (int) (nt.betaL * multiplier);
                    nt.betaU_N = (int) (nt.betaU * multiplier);
                } else {
                    nt.isBetaType = false;
                    nt.betaL = 0;
                    nt.betaU = 0;
                }

                TransitionNanoXTPN.PlaceArcWeight paw = nt.new PlaceArcWeight();
                paw.place = placeNanoA1;
                //paw.weight = t.getInputArcWeightFrom(placeXTPN);
                nt.prePlaces.add(paw);

                //nt.weightToPlace = t.getInputArcWeightFrom(placeXTPN); //tyle tokenów zabierają z miejsca
                switch(competingTransSpeed) {
                    case -1: //wyjściowe tranzycje najwolniejsze
                        nt.tauAlphaLimit = nt.alphaU_N;
                        nt.tauBetaLimit = nt.betaU_N;
                        break;
                    case 0: //
                        nt.tauAlphaLimit = (nt.alphaL_N + nt.alphaU_N) / 2;
                        nt.tauBetaLimit = (nt.betaL_N + nt.betaU_N) / 2;
                        break;
                    case 1: //wyjściowe tranzycje najszybsze
                        nt.tauAlphaLimit = nt.alphaL_N;
                        nt.tauBetaLimit = nt.betaL_N;
                        break;
                }
                postTransitionsNanoA1.add(nt);
            }
        

        //wyliczamy maksymalny czas obliczeń:
        int[] arr = lcmCandidates.stream().mapToInt(i -> i).toArray();
        maxSteps = MathToolsXTPN.lcm_of_array_elements(arr);
        maxSteps += placeNanoA1.gammaU_N;

        //symulacja, ustawianie zegarów
        for (TransitionNanoXTPN t : preTransitionsNanoA1) {
            if(t.hasReadArcToPlace) {
                t.tauAlpha_N = -1; //z łukiem odczyty niekoniecznie aktywna
            } else {
                t.tauAlpha_N = 0; //zawsze aktywne
            }
            t.tauBeta_N = -1; //nie produkuje na początku
        }
        for(TransitionNanoXTPN t : postTransitionsNanoA1) {
            t.tauAlpha_N = -1; //niekoniecznie aktywna
            t.tauBeta_N = -1; //nie produkuje na początku
        }
        //maxSteps;

        Alg1ReadyToProceed = true;
        return maxSteps;
    }
    
    //***************************************************************************************************
    //************************************  GENERAL PURPOSE FUNCTIONS ***********************************
    //***************************************************************************************************

    /**
     * Metoda przetwarza tranzycje wejściowe. W tej metodzie przetwarzany jest zbiór transitions, które MOGĄ mieć
     * łuki odczytu do miejsca place, tak więc są równocześnie wyjściowymi i podlegają zasadom aktywacji. Te tranzycje
     * które nie mają łuków odczytu do miejsca place, są zawsze aktywne.
     * @param transitions ArrayList<TransitionNanoXTPN> lista tranzycji
     * @param place PlaceNanoXTPN miejsce
     */
    private static void processInputTransitions(ArrayList<TransitionNanoXTPN> transitions, PlaceNanoXTPN place) {
        for (TransitionNanoXTPN inputTrans : transitions) {
            if(inputTrans.hasReadArcToPlace) {
                if(inputTrans.tauBeta_N != -1) { //jeżeli produkuje
                    inputTrans.tauBeta_N++; //zwiększ zegar
                    if(inputTrans.tauBeta_N == inputTrans.tauBetaLimit) { //jeżeli skończyła produkcję

                        for(TransitionNanoXTPN.PlaceArcWeight paw : inputTrans.postPlaces) {
                            paw.place.addTokens_N(paw.weight); //dodaj tokeny do miejsca wyjściowego
                        }
                        //place.addTokens_N(inputTrans.weightToPlace); //dodaj tokeny do miejsca
                        
                        inputTrans.tauBeta_N = -1;
                        if(canActivate(inputTrans)) {
                            inputTrans.tauAlpha_N = 0; //aktywuj
                        } else {
                            inputTrans.tauAlpha_N = -1; //dezaktywuj
                        }

                    } else if (inputTrans.tauBeta_N > inputTrans.tauBetaLimit) { //jeżeli przekroczył limit
                        int error = 1; //np gdy aktywacja = 0, ORAZ produkcja = 0
                        error++;
                    }
                } else { //jeżeli nie produkuje, sprawdzić czy aktywna:
                    if(canActivate(inputTrans)) { //sprawdź, czy można ją aktywować
                        if(inputTrans.tauAlpha_N == -1) { //jeżeli nie była aktywna
                            inputTrans.tauAlpha_N = 0; //aktywuj

                            if(inputTrans.tauAlpha_N == inputTrans.tauAlphaLimit) { //jeżeli aktywacja w zerowym czasie
                                inputTrans.tauBeta_N = 0; //rozpocznij produkcję

                                for(TransitionNanoXTPN.PlaceArcWeight paw : inputTrans.prePlaces) {
                                    paw.place.removeTokens_N(paw.weight); //usuń tokeny z każdego miejsca wejściowego
                                }
                                //place.removeTokens_N(inputTrans.weightToPlace); //usuń tokeny z miejsca
                            }
                        } else { //jeżeli już jest aktywna
                            inputTrans.tauAlpha_N++; //zwiększ zegar
                            if(inputTrans.tauAlpha_N == inputTrans.tauAlphaLimit) { //jeżeli skończyła aktywację
                                inputTrans.tauBeta_N = 0; //rozpocznij produkcję

                                for(TransitionNanoXTPN.PlaceArcWeight paw : inputTrans.prePlaces) {
                                    paw.place.removeTokens_N(paw.weight); //usuń tokeny z każdego miejsca wejściowego
                                }
                                //place.removeTokens_N(inputTrans.weightToPlace); //usuń tokeny z miejsca
                            }
                        }
                    } else {
                        inputTrans.tauAlpha_N = -1; //dezaktywuj
                    }
                }
            } else {// IF NOT (inputTrans.hasReadArcToPlace)
                //zawsze aktywna, bo wejściowa, wtedy zegar aktywacji odpowiada za aktywację ORAZ produkcję
                inputTrans.tauAlpha_N++;
                if(inputTrans.tauAlpha_N == inputTrans.tauAlphaLimit + inputTrans.tauBetaLimit) {
                    
                    for(TransitionNanoXTPN.PlaceArcWeight paw : inputTrans.postPlaces) {
                        paw.place.addTokens_N(paw.weight); //dodaj tokeny do miejsca wyjściowego
                    }
                    //place.addTokens_N(inputTrans.weightToPlace); //dodaj tokeny do miejsca
                    
                    inputTrans.tauAlpha_N = 0;
                }
            }
        }
    }

    /**
     * Metoda przetwarza tranzycje wyjściowe. W tej metodzie przetwarzany jest zbiór transitions, które NIE MAJĄ
     * łuków odczytu do miejsca place, tak więc cokolwiek gdziekolwiek produkują, jest ignorowane. Jedyne co robią,
     * to po zakończeniu aktywacji, zabierają tokeny z miejsca place.
     * @param transitions ArrayList<TransitionNanoXTPN> lista tranzycji
     * @param place PlaceNanoXTPN miejsce
     */
    private static void processOutputTransitions(ArrayList<TransitionNanoXTPN> transitions, PlaceNanoXTPN place) {
        for (TransitionNanoXTPN outputTrans : transitions) {
            if (outputTrans.tauBeta_N != -1) { //jeżeli produkuje
                outputTrans.tauBeta_N++; //zwiększ zegar
                if (outputTrans.tauBeta_N == outputTrans.tauBetaLimit) { //jeżeli skończyła produkcję
                    outputTrans.tauBeta_N = -1;
                    if (canActivate(outputTrans)) {
                        outputTrans.tauAlpha_N = 0; //aktywuj
                    } else {
                        outputTrans.tauAlpha_N = -1; //dezaktywuj
                    }
                } else if (outputTrans.tauBeta_N > outputTrans.tauBetaLimit) { //jeżeli przekroczył limit
                    int error = 1; //np gdy aktywacja = 0, ORAZ produkcja = 0
                    error++;
                }
            } else { //jeżeli nie produkuje, sprawdzić czy aktywna:
                if (canActivate(outputTrans)) { //sprawdź, czy można ją aktywować
                    if (outputTrans.tauAlpha_N == -1) { //jeżeli nie była aktywna
                        outputTrans.tauAlpha_N = 0; //aktywuj

                        if (outputTrans.tauAlpha_N == outputTrans.tauAlphaLimit) { //jeżeli aktywacja w zerowym czasie
                            outputTrans.tauBeta_N = 0; //rozpocznij produkcję

                            for(TransitionNanoXTPN.PlaceArcWeight paw : outputTrans.prePlaces) {
                                paw.place.removeTokens_N(paw.weight); //usuń tokeny z każdego miejsca wejściowego
                            }
                            
                            //place.removeTokens_N(outputTrans.weightToPlace); //usuń tokeny z miejsca
                        }
                    } else { //jeżeli już jest aktywna
                        outputTrans.tauAlpha_N++; //zwiększ zegar
                        if (outputTrans.tauAlpha_N == outputTrans.tauAlphaLimit) { //jeżeli skończyła aktywację
                            outputTrans.tauBeta_N = 0; //rozpocznij produkcję
                            outputTrans.tauAlpha_N = -1; //dezaktywuj

                            for(TransitionNanoXTPN.PlaceArcWeight paw : outputTrans.prePlaces) {
                                paw.place.removeTokens_N(paw.weight); //usuń tokeny z każdego miejsca wejściowego
                            }
                            //place.removeTokens_N(outputTrans.weightToPlace); //usuń tokeny z miejsca
                        }
                    }
                } else {
                    outputTrans.tauAlpha_N = -1; //dezaktywuj
                }
            }
        }
    }

    private static boolean canActivate(TransitionNanoXTPN outputTransition) {
        for (TransitionNanoXTPN.PlaceArcWeight paw : outputTransition.prePlaces) {
            if (paw.place.tokens_N.isEmpty() || paw.place.tokens_N.size() < paw.weight) { //!!! do wewnatrz
                return false;
            } else {
                boolean placeOK = false;
                int neededTokens = paw.weight;
                for (int token : paw.place.tokens_N) {
                    if (token >= paw.place.gammaL_N) {
                        neededTokens--;
                    }
                    if (neededTokens == 0) {
                        placeOK = true;
                        break;
                    }
                }
                if(!placeOK) {
                    return false; //brak tokenów w miejscu
                }
            } 
        }
        return true; //jeśli tu doszliśmy, to znaczy że każde miejsce wejściowe ma tokeny
    }

    /**
     * Metoda sprawdza, czy grupa tranzycji oraz miejsce mają wartości alfa, beta i gamma, które są liczbami wymiernymi.
     * Zmienna digits, np. 100 oznacza, że liczby będą zaokrąglane do 2 miejsc po przecinku.
     * @param transitions ArrayList<Transition> lista tranzycji
     * @param placeXTPN PlaceXTPN miejsce
     * @param digits int liczba cyfr po przecinku
     * @return boolean czy wartości są liczbami wymiernymi
     */
    private static boolean areRationalsAlg1(ArrayList<Transition> transitions, PlaceXTPN placeXTPN, int digits) {
        long checkIfRationals = 0;
        boolean isRational = false;
        for (Transition t : transitions) {
            checkIfRationals = (long) (((TransitionXTPN)t).getAlphaMinValue() * digits);
            if (checkIfRationals % digits != 0) {
                isRational = true;
                break;
            }
            checkIfRationals = (long) (((TransitionXTPN)t).getAlphaMaxValue() * digits);
            if (checkIfRationals % digits != 0) {
                isRational = true;
                break;
            }
            checkIfRationals = (long) (((TransitionXTPN)t).getAlphaMinValue() * digits);
            if (checkIfRationals % digits != 0) {
                isRational = true;
                break;
            }
            checkIfRationals = (long) (((TransitionXTPN)t).getAlphaMaxValue() * digits);
            if (checkIfRationals % digits != 0) {
                isRational = true;
                break;
            }
        }

        checkIfRationals = (long) (placeXTPN.getGammaMaxValue() * digits);
        if (checkIfRationals % digits != 0) {
            isRational = true;
        }
        checkIfRationals = (long) (placeXTPN.getGammaMinValue() * digits);
        if (checkIfRationals % digits != 0) {
            isRational = true;
        }
        return isRational;
    }

    private static boolean areRationalsAlg2(ArrayList<Transition> transitions, ArrayList<Place> places, int digits) {
        long checkIfRationals = 0;
        boolean isRational = false;
        for (Transition t : transitions) {
            checkIfRationals = (long) (((TransitionXTPN)t).getAlphaMinValue() * digits);
            if (checkIfRationals % digits != 0) {
                isRational = true;
                break;
            }
            checkIfRationals = (long) (((TransitionXTPN)t).getAlphaMaxValue() * digits);
            if (checkIfRationals % digits != 0) {
                isRational = true;
                break;
            }
            checkIfRationals = (long) (((TransitionXTPN)t).getAlphaMinValue() * digits);
            if (checkIfRationals % digits != 0) {
                isRational = true;
                break;
            }
            checkIfRationals = (long) (((TransitionXTPN)t).getAlphaMaxValue() * digits);
            if (checkIfRationals % digits != 0) {
                isRational = true;
                break;
            }
        }
        
        for(Place p : places) {
            checkIfRationals = (long) (((PlaceXTPN)p).getGammaMaxValue() * digits);
            if (checkIfRationals % digits != 0) {
                isRational = true;
                break;
            }
            checkIfRationals = (long) (((PlaceXTPN)p).getGammaMinValue() * digits);
            if (checkIfRationals % digits != 0) {
                isRational = true;
                break;
            }
        }
        return isRational;
    }

    /**
     * Metoda przetasowuje elementy w liście tranzycji.
     * @param preTransitionsNano ArrayList<TransitionNanoXTPN> lista tranzycji
     */
    private static void shuffleElements(ArrayList<TransitionNanoXTPN> preTransitionsNano) {
        for(int i = 0; i < preTransitionsNano.size(); i++) {
            int randomIndex = (int) (Math.random() * preTransitionsNano.size());
            TransitionNanoXTPN temp = preTransitionsNano.get(i);
            preTransitionsNano.set(i, preTransitionsNano.get(randomIndex));
            preTransitionsNano.set(randomIndex, temp);
        }
    }
}
