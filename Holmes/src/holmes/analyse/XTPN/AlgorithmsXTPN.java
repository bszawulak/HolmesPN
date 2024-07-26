package holmes.analyse.XTPN;

import holmes.darkgui.GUIManager;
import holmes.petrinet.elements.PlaceXTPN;
import holmes.petrinet.elements.Transition;
import holmes.petrinet.elements.TransitionXTPN;

import java.util.ArrayList;

public class AlgorithmsXTPN {
    private static final GUIManager overlord = GUIManager.getDefaultGUIManager();

    public class ResultTokensAvg {
        public double avg;
        public double stdDev;
        public double min;
        public double max;
    }

    public AlgorithmsXTPN() {
    }

    /**
     * Celem tej wspaniałej metody jest ustalenie teoretycznej liczby tokenów w miejscu.
     * @param placeXTPN
     * @return
     */
    public static int getTokensPerPlace(PlaceXTPN placeXTPN, int digits, int minMaxAvg, boolean considerOutputTransitions) {
        //minMaxAvg = -1 - wejściowe tranzycje najszybsze, wyjściowe najwolniejsze
        //minMaxAvg = 0 - średnia z U/L alfa i beta
        //minMaxAvg = 1 - wejściowe tranzycje najwolniejsze, wyjściowe najszybsze

        ArrayList<Transition> preTransitions = placeXTPN.getInputTransitions();
        ArrayList<Transition> postTransitions = placeXTPN.getOutputTransitions();
        ArrayList<Transition> transitions = overlord.getWorkspace().getProject().getTransitions();

        //sprawdzamy, czy wartości alfa, beta i gamma są liczbami wymiernymi:
        boolean areRational = areRationals(preTransitions, placeXTPN, digits);
        if(!areRational) { //jeśli nie są, to sprawdzamy wartości dla tranzycji wyjściowych
            areRational = areRationals(postTransitions, placeXTPN, digits);
        }
        int multiplier = 1;
        if(areRational) {
            multiplier = digits;
        }

        //tutaj przygotowujemy obiekty TransitionNanoXTPN i PlaceNanoXTPN do obliczeń:

        boolean areReadArcs = false;
        ArrayList<TransitionNanoXTPN> preTransitionsNano = new ArrayList<>();
        ArrayList<TransitionNanoXTPN> postTransitionsNano = new ArrayList<>();
        PlaceNanoXTPN placeNano = null;
        ArrayList<Integer> lcmCandidates = new ArrayList<Integer>();
        for (Transition t : preTransitions) { //tworzenie listy tranzycji wejściowych
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

            nt.weightToPlace = t.getOutputArcWeightTo(placeXTPN); //tyle tokenów dodają do miejsca

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

            preTransitionsNano.add(nt);
        }

        if(considerOutputTransitions) {
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

                nt.weightToPlace = t.getInputArcWeightFrom(placeXTPN); //tyle tokenów zabierają z miejsca
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

                postTransitionsNano.add(nt);
            }
        }

        placeNano = new PlaceNanoXTPN(placeXTPN, overlord.getWorkspace().getProject().getPlaces().indexOf(placeXTPN));
        placeNano.gammaL_N = (int) (placeNano.gammaL * multiplier);
        placeNano.gammaU_N = (int) (placeNano.gammaU * multiplier);

        //wyliczamy maksymalny czas obliczeń:

        int[] arr = lcmCandidates.stream().mapToInt(i -> i).toArray();
        long maxSteps = MathToolsXTPN.lcm_of_array_elements(arr);
        maxSteps += placeNano.gammaU_N;

        //symulacja, ustawianie zegarów
        for (TransitionNanoXTPN t : preTransitionsNano) {
            if(t.hasReadArcToPlace) {
                t.tauAlpha_N = -1; //z łukiem odczyty niekoniecznie aktywna
            } else {
                t.tauAlpha_N = 0; //zawsze aktywne
            }
            t.tauBeta_N = -1; //nie produkuje na początku
        }
        for(TransitionNanoXTPN t : postTransitionsNano) {
            t.tauAlpha_N = -1; //niekoniecznie aktywna
            t.tauBeta_N = -1; //nie produkuje na początku
        }

        //symulacja:

        int result = 0;
        ArrayList<Integer> Kp = new ArrayList<>();
        for (long time = 0; time < maxSteps; time++) {

            if(!areReadArcs)
                shuffleElements(preTransitionsNano);

            processInputTransitions(preTransitionsNano, placeNano);
            placeNano.updateTokensSet_N(1);

            if(considerOutputTransitions) {
                shuffleElements(preTransitionsNano);
                processOutputTransitions(postTransitionsNano, placeNano);
            }
            if(Kp.size() > result) {
                result = Kp.size();
            }
        }
        return result;
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

    /**
     * Metoda przetwarza tranzycje wejściowe. W tej metodzie przetwarzany jest zbiór transitions, które MOGĄ mieć
     * łuki odczytu do miejsca place, tak więc są równocześnie wyjściowymi i podlegają zasadom aktywacji. Te tranzycje
     * które nie mają łyków odczytu do miejsca place, są zawsze aktywne.
     * @param transitions ArrayList<TransitionNanoXTPN> lista tranzycji
     * @param place PlaceNanoXTPN miejsce
     */
    private static void processInputTransitions(ArrayList<TransitionNanoXTPN> transitions, PlaceNanoXTPN place) {
        for (TransitionNanoXTPN inputTrans : transitions) {
            if(inputTrans.hasReadArcToPlace) {
                if(inputTrans.tauBeta_N != -1) { //jeżeli produkuje
                    inputTrans.tauBeta_N++; //zwiększ zegar
                    if(inputTrans.tauBeta_N == inputTrans.tauBetaLimit) { //jeżeli skończyła produkcję

                        place.addTokens_N(inputTrans.weightToPlace); //dodaj tokeny do miejsca
                        inputTrans.tauBeta_N = -1;
                        if(canActivate(inputTrans, place)) {
                            inputTrans.tauAlpha_N = 0; //aktywuj
                        } else {
                            inputTrans.tauAlpha_N = -1; //dezaktywuj
                        }

                    } else if (inputTrans.tauBeta_N > inputTrans.tauBetaLimit) { //jeżeli przekroczył limit
                        int error = 1; //np gdy aktywacja = 0, ORAZ produkcja = 0
                        error++;
                    }
                } else { //jeżeli nie produkuje, sprawdzić czy aktywna:
                    if(canActivate(inputTrans, place)) { //sprawdź, czy można ją aktywować
                        if(inputTrans.tauAlpha_N == -1) { //jeżeli nie była aktywna
                            inputTrans.tauAlpha_N = 0; //aktywuj

                            if(inputTrans.tauAlpha_N == inputTrans.tauAlphaLimit) { //jeżeli aktywacja w zerowym czasie
                                inputTrans.tauBeta_N = 0; //rozpocznij produkcję
                                place.removeTokens_N(inputTrans.weightToPlace); //usuń tokeny z miejsca
                            }
                        } else { //jeżeli już jest aktywna
                            inputTrans.tauAlpha_N++; //zwiększ zegar
                            if(inputTrans.tauAlpha_N == inputTrans.tauAlphaLimit) { //jeżeli skończyła aktywację
                                inputTrans.tauBeta_N = 0; //rozpocznij produkcję
                                place.removeTokens_N(inputTrans.weightToPlace); //usuń tokeny z miejsca
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
                    place.addTokens_N(inputTrans.weightToPlace); //dodaj tokeny do miejsca
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
                    place.removeTokens_N(outputTrans.weightToPlace); //usuń tokeny z miejsca
                    outputTrans.tauBeta_N = -1;
                    if (canActivate(outputTrans, place)) {
                        outputTrans.tauAlpha_N = 0; //aktywuj
                    } else {
                        outputTrans.tauAlpha_N = -1; //dezaktywuj
                    }
                } else if (outputTrans.tauBeta_N > outputTrans.tauBetaLimit) { //jeżeli przekroczył limit
                    int error = 1; //np gdy aktywacja = 0, ORAZ produkcja = 0
                    error++;
                }
            } else { //jeżeli nie produkuje, sprawdzić czy aktywna:
                if (canActivate(outputTrans, place)) { //sprawdź, czy można ją aktywować
                    if (outputTrans.tauAlpha_N == -1) { //jeżeli nie była aktywna
                        outputTrans.tauAlpha_N = 0; //aktywuj

                        if (outputTrans.tauAlpha_N == outputTrans.tauAlphaLimit) { //jeżeli aktywacja w zerowym czasie
                            outputTrans.tauBeta_N = 0; //rozpocznij produkcję
                            place.removeTokens_N(outputTrans.weightToPlace); //usuń tokeny z miejsca
                        }
                    } else { //jeżeli już jest aktywna
                        outputTrans.tauAlpha_N++; //zwiększ zegar
                        if (outputTrans.tauAlpha_N == outputTrans.tauAlphaLimit) { //jeżeli skończyła aktywację
                            outputTrans.tauBeta_N = 0; //rozpocznij produkcję
                            place.removeTokens_N(outputTrans.weightToPlace); //usuń tokeny z miejsca
                        }
                    }
                } else {
                    outputTrans.tauAlpha_N = -1; //dezaktywuj
                }
            }
        }
    }

    private static boolean canActivate(TransitionNanoXTPN outputTransition, PlaceNanoXTPN p) {
        if(!p.tokens_N.isEmpty()) {
            int neededTokens = outputTransition.weightToPlace;
            for(int token : p.tokens_N) {
                if(token > outputTransition.alphaL_N) {
                    neededTokens--;
                }
                if(neededTokens == 0) {
                    return true;
                }
            }
        }
        return false;
    }





    /**
     * Metoda sprawdza, czy grupa tranzycji oraz miejsce mają wartości alfa, beta i gamma, które są liczbami wymiernymi.
     * Zmienna digits, np. 100 oznacza, że liczby będą zaokrąglane do 2 miejsc po przecinku.
     * @param transitions ArrayList<Transition> lista tranzycji
     * @param placeXTPN PlaceXTPN miejsce
     * @param digits int liczba cyfr po przecinku
     * @return boolean czy wartości są liczbami wymiernymi
     */
    private static boolean areRationals(ArrayList<Transition> transitions, PlaceXTPN placeXTPN, int digits) {
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
}
