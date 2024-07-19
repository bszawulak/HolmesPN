package holmes.analyse.XTPN;

import holmes.darkgui.GUIManager;
import holmes.petrinet.elements.PlaceXTPN;
import holmes.petrinet.elements.Transition;
import holmes.petrinet.elements.TransitionXTPN;

import java.util.ArrayList;

public class AlgorithmsXTPN {
    private static GUIManager overlord = GUIManager.getDefaultGUIManager();

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
    public static ArrayList<Integer> getTokensPerPlace(PlaceXTPN placeXTPN, int digits, int minMaxAvg, boolean ignoreOutputTransitions) {
        //minMaxAvg = -1 - wejściowe tranzycje najszybsze, wyjściowe najwolniejsze
        //minMaxAvg = 0 - średnia z U/L alfa i beta
        //minMaxAvg = 1 - wejściowe tranzycje najwolniejsze, wyjściowe najszybsze

        ArrayList<Integer> tokensPerPlace = new ArrayList<>();
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
        ArrayList<TransitionNanoXTPN> preTransitionsNano = new ArrayList<>();
        ArrayList<TransitionNanoXTPN> postTransitionsNano = new ArrayList<>();
        PlaceNanoXTPN placeNano = null;

        for (Transition t : preTransitions) { //tworzenie listy tranzycji wejściowych
            TransitionNanoXTPN nt = new TransitionNanoXTPN((TransitionXTPN) t, transitions.indexOf(t));
            nt.alphaL_N = (int) (nt.alphaL * multiplier);
            nt.alphaU_N = (int) (nt.alphaU * multiplier);
            nt.betaL_N = (int) (nt.betaL * multiplier);
            nt.betaU_N = (int) (nt.betaU * multiplier);
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
            preTransitionsNano.add(nt);
        }

        for (Transition t : postTransitions) { //tworzenie listy tranzycji wyjściowych
            TransitionNanoXTPN nt = new TransitionNanoXTPN((TransitionXTPN) t, transitions.indexOf(t));
            nt.alphaL_N = (int) (nt.alphaL * multiplier);
            nt.alphaU_N = (int) (nt.alphaU * multiplier);
            nt.betaL_N = (int) (nt.betaL * multiplier);
            nt.betaU_N = (int) (nt.betaU * multiplier);
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
        }
        placeNano = new PlaceNanoXTPN(placeXTPN, overlord.getWorkspace().getProject().getPlaces().indexOf(placeXTPN));
        placeNano.gammaL_N = (int) (placeNano.gammaL * multiplier);
        placeNano.gammaU_N = (int) (placeNano.gammaU * multiplier);


        //calculating the tokens for the place:


        return tokensPerPlace;
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
