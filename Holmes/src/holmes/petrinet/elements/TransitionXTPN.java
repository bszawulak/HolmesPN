package holmes.petrinet.elements;

import holmes.darkgui.GUIManager;

import java.awt.*;
import java.io.Serial;
import java.util.ArrayList;

public class TransitionXTPN extends Transition {
    @Serial
    private static final long serialVersionUID = 4766270474155264671L;
    //parametry xTPN:
    private boolean alphaMode_xTPN = true;
    private boolean alphaRangeVisibility_XTPN = true;
    private double alphaMin_xTPN = 0.0;
    private double alphaMax_xTPN = 1.0;
    private boolean betaMode_xTPN = true;
    private double betaMin_xTPN = 0.0;
    private double betaMax_xTPN = 1.0;
    private boolean betaRangeVisibility_XTPN = true;
    private double tauAlpha_xTPN = -1.0;
    private double tauBeta_xTPN = -1.0;
    private double timer_Ualfa_XTPN = -1.0;
    private double timer_Vbeta_XTPN = -1.0;
    private boolean tauTimersVisibility_XTPN = true; //czy wyświetlać timery

    //jeśli miejsca wejściowe tracą szybciej tokeny ze starości niż z
    //produkcji, zmniejszamy tau Alfa i Beta (prawdopodobieństwo).
    private boolean massActionKinetics = false;
    private boolean isImmediateXTPN = false;
    private boolean isActivated_xTPN = false;
    private boolean isProducing_xTPN = false;
    //grafika:
    private int fractionDigits = 2;


    //sim:
    public int simInactiveState = 0;
    public int simActiveState = 0;
    public int simProductionState = 0;
    public int simFiredState = 0;
    public double simInactiveTime = 0.0;
    public double simActiveTime = 0.0;
    public double simProductionTime = 0.0;

    public ArrayList<Double> statesHistory = new ArrayList<>();
    public ArrayList<Double> statesTimeHistory = new ArrayList<>();

    public QSimXTPNgraphical qSimXTPN = new QSimXTPNgraphical();

    public boolean showQSimXTPN = false;
    public class QSimXTPNgraphical {
        public int xOff = -70;
        public int yOff = -60;

        public String text1 = "";
        public String text2 = "";
        public String text3 = "";
        public String text4 = "";

        public void clean() {
            xOff = -70;
            yOff = -60;
            text1 = "";
            text2 = "";
            text3 = "";
            text4 = "";
        }
    }

    /**
     * Konstruktor obiektu tranzycji sieci. Używany do wczytywania sieci zewnętrznej, np. ze Snoopy
     * @param transitionId     int - identyfikator tranzycji
     * @param elementLocations ArrayList[ElementLocation] - lista lokalizacji tranzycji
     * @param name             String - nazwa tranzycji
     * @param comment          String - komentarz tranzycji
     */
    public TransitionXTPN(int transitionId, ArrayList<ElementLocation> elementLocations, String name, String comment) {
        super(transitionId, elementLocations, name, comment);
        transType = TransitionType.XTPN;
    }

    /**
     * Konstruktor obiektu tranzycji sieci. Używany przez procedury tworzenia portali.
     * @param transitionId     int - identyfikator tranzycji
     * @param elementLocations ArrayList[ElementLocation] - lista lokalizacji tranzycji
     */
    public TransitionXTPN(int transitionId, ArrayList<ElementLocation> elementLocations) {
        super(transitionId, elementLocations);
        transType = TransitionType.XTPN;
    }

    /**
     * Konstruktor obiektu tranzycji sieci.
     * @param transitionId       int - identyfikator tranzycji
     * @param sheetId            int - identyfikator arkusza
     * @param transitionPosition Point - punkt lokalizacji tranzycji
     */
    public TransitionXTPN(int transitionId, int sheetId, Point transitionPosition) {
        super(transitionId, sheetId, transitionPosition);
        transType = TransitionType.XTPN;
    }

    public TransitionXTPN(String error) {
        super(error);
        transType = TransitionType.XTPN;
    }

    /**
     * Metoda resetuje zegary tranzycji XTPN.
     */
    public void resetTimeVariables_xTPN() {
        tauAlpha_xTPN = -1.0;
        tauBeta_xTPN = -1.0;
        timer_Ualfa_XTPN = -1.0;
        timer_Vbeta_XTPN = -1.0;
        isActivated_xTPN = false;
        isProducing_xTPN = false;
    }

    public void resetSimVariables_XTPN() {
        simInactiveState = 0;
        simActiveState = 0;
        simProductionState = 0;
        simFiredState = 0;

        simInactiveTime = 0.0;
        simActiveTime = 0.0;
        simProductionTime = 0.0;
    }

    /**
     * Metoda ustawia dolną wartość alfaMin dla xTPN.
     * @param value (<b>double</b>) czas alfaMin (=EFT)
     * @param force (<b>boolean</b>) czy wymusić wartość bez weryfikacji
     */
    public void setAlphaMinValue(double value, boolean force) {
        if(force) {
            this.alphaMin_xTPN = value;
            return;
        }
        if (value < 0) {
            this.alphaMin_xTPN = 0.0;
            return;
        }
        if (value > alphaMax_xTPN) { //musi być mniejszy równy niż alphaU
            this.alphaMin_xTPN = alphaMax_xTPN;
            return;
        }
        this.alphaMin_xTPN = value;
    }

    /**
     * Metoda pozwala odczytać dolną wartość alfaMin dla xTPN.
     * @return (<b>double</b>) - czas alfaMin.
     */
    public double getAlphaMinValue() {
        return this.alphaMin_xTPN;
    }

    /**
     * Metoda ustawia górną wartość alfaMax dla xTPN.
     * @param value (<b>double</b>) czas alfaMax (=LFT)
     * @param force (<b>boolean</b>) czy wymusić wartość bez weryfikacji
     */
    public void setAlphaMaxValue(double value, boolean force) {
        if(force) {
            this.alphaMax_xTPN = value;
            return;
        }
        if (value < 0) {
            this.alphaMax_xTPN = -1.0; //domyślnie do redukcji -> classicalPN
            return;
        }
        if (value < alphaMin_xTPN) { //musi być większy równy niż alphaL
            this.alphaMax_xTPN = alphaMin_xTPN;
            return;
        }
        this.alphaMax_xTPN = value;
    }

    /**
     * Metoda pozwala odczytać górną wartość alfaMax dla xTPN.
     * @return (double) : czas alfaMax.
     */
    public double getAlphaMaxValue() {
        return this.alphaMax_xTPN;
    }

    /**
     * Metoda ustawia dolną wartość betaMin dla xTPN.
     * @param value (<b>double</b>) czas betaMin (=DPN duration lower value)
     * @param force (<b>boolean</b>) czy wymusić wartość bez weryfikacji
     */
    public void setBetaMinValue(double value, boolean force) {
        if(force) {
            this.betaMin_xTPN = value;
            return;
        }
        if (value < 0) {
            this.betaMin_xTPN = 0.0;
            return;
        }
        if (value > betaMax_xTPN) { //musi być mniejszy równy niż betaU
            this.betaMin_xTPN = betaMax_xTPN;
            return;
        }
        this.betaMin_xTPN = value;
    }

    /**
     * Metoda pozwala odczytać dolną wartość betaMin dla xTPN.
     * @return (<b>double</b>) - czas betaMin.
     */
    public double getBetaMinValue() {
        return this.betaMin_xTPN;
    }

    /**
     * Metoda ustawia dolną wartość betaMax dla xTPN.
     * @param value (<b>double</b>) czas betaMax (=DPN duration upper value)
     * @param force (<b>boolean</b>) czy wymusić wartość bez weryfikacji
     */
    public void setBetaMaxValue(double value, boolean force) {
        if(force) {
            this.betaMax_xTPN = value;
            return;
        }
        if (value < 0) {
            this.betaMax_xTPN = 0.0; //domyślnie do redukcji -> classical DPN
            return;
        }
        if (value < betaMin_xTPN) { //musi być większy równy niż betaL
            this.betaMax_xTPN = betaMin_xTPN;
            return;
        }
        this.betaMax_xTPN = value;
    }

    /**
     * Metoda pozwala odczytać górną wartość betaMax dla xTPN.
     * @return (<b>double</b>) - czas betaMax.
     */
    public double getBetaMaxValue() {
        return this.betaMax_xTPN;
    }

    /**
     * Metoda ustawia wartość docelową zegara U - tauAlpha tranzycji.
     * @param value (<b>double</b>) nowa wartość tauAlpha.
     */
    public void setTauAlphaValue(double value) {
        tauAlpha_xTPN = value;
    }

    /**
     * Metoda zwraca wartość docelową zegara U - tauAlfa tranzycji.
     * @return (<b>double</b>) aktualny czas tauAlpha.
     */
    public double getTauAlphaValue() {
        return tauAlpha_xTPN;
    }

    /**
     * Metoda ustawia wartość docelową zegara V - tauBeta tranzycji.
     * @param value (<b>double</b>) nowa wartość tauBeta.
     */
    public void setTauBetaValue(double value) {
        tauBeta_xTPN = value;
    }

    /**
     * Metoda zwraca wartość docelową zegara V - tauBeta tranzycji.
     * @return (<b>double</b>) aktualny czas tauBeta.
     */
    public double getTauBetaValue() {
        return tauBeta_xTPN;
    }

    /**
     * Metoda ustawia status tauAlpha i tauBeta - pokazywać czy nie.
     * @param status (<b>boolean</b>) true, jeśli zegary mają być pokazywane.
     */
    public void setTauTimersVisibility(boolean status) {
        tauTimersVisibility_XTPN = status;
    }

    /**
     * Metoda zwraca status zegarów zegarów alpha - pokazywać czy nie.
     * @return (<b>boolean</b>) - true, jeśli zegary mają być pokazywane.
     */
    public boolean isTauTimerVisible() {
        return tauTimersVisibility_XTPN;
    }

    /**
     * Metoda ustawia status zakresów zegarów alpha - pokazywać czy nie.
     * @param status (<b>boolean</b>) true, jeśli zakresy mają być pokazywane.
     */
    public void setAlphaRangeVisibility(boolean status) {
        alphaRangeVisibility_XTPN = status;
    }

    /**
     * Metoda zwraca status zakresów alpha - pokazywać czy nie.
     * @return (<b>boolean</b>) - true, jeśli zakresy alpha mają być pokazywane.
     */
    public boolean isAlphaRangeVisible() {
        return alphaRangeVisibility_XTPN;
    }

    /**
     * Metoda ustawia status zakresów zegarów beta - pokazywać czy nie.
     * @param status (<b>boolean</b>) true, jeśli zakresy beta mają być pokazywane.
     */
    public void setBetaRangeVisibility(boolean status) {
        betaRangeVisibility_XTPN = status;
    }

    /**
     * Metoda zwraca status zakresów beta - pokazywać czy nie.
     * @return (<b>boolean</b>) - true, jeśli zakresy beta mają być pokazywane.
     */
    public boolean isBetaRangeVisible() {
        return betaRangeVisibility_XTPN;
    }

    /**
     * Metoda ustawia wyświetlaną dokładność po przecinku.
     * @param value (<b>int</b>) nowa wartość liczby cyfr przecinku.
     */
    public void setFraction_xTPN(int value) {
        fractionDigits = value;
    }

    /**
     * Metoda zwraca wyświetlaną dokładność po przecinku.
     * @return (<b>int</b>) - aktualna wartość liczby cyfr przecinku.
     */
    public int getFraction_xTPN() {
        return fractionDigits;
    }

    /**
     * Metoda ustawia wartość docelową zegara U-alfa tranzycji.
     * @param value (<b>double</b>) - nowa wartość zegara U-alfa.
     */
    public void setTimerAlfaValue(double value) {
        timer_Ualfa_XTPN = value;
    }

    /**
     * Metoda modyfikuje wartość docelową zegara U-alfa tranzycji.
     * @param delta (<b>double</b>) o ile zmienić zegar U-alfa.
     */
    public void updateTimerAlfaValue(double delta) {
        timer_Ualfa_XTPN += delta;
    }

    /**
     * Metoda zwraca wartość docelową zegara U-alfa tranzycji.
     * @return (<b>double</b>) aktualny czas U-alfa.
     */
    public double getTimerAlfaValue() {
        return timer_Ualfa_XTPN;
    }

    /**
     * Metoda ustawia wartość docelową zegara V-beta tranzycji.
     * @param value (<b>double</b>) nowa wartość zegara V-beta.
     */
    public void setTimerBetaValue(double value) {
        timer_Vbeta_XTPN = value;
    }

    /**
     * Metoda modyfikuje wartość docelową zegara V-beta tranzycji.
     * @param delta (<b>double</b>) o ile zmienić zegar V-beta.
     */
    public void updateTimerBetaValue(double delta) {
        timer_Vbeta_XTPN += delta;
    }

    /**
     * Metoda zwraca wartość docelową zegara V-beta tranzycji.
     * @return (<b>double</b>) aktualny czas V-beta.
     */
    public double getTimerBetaValue() {
        return timer_Vbeta_XTPN;
    }

    /**
     * Metoda ustawia status aktywacji tranzycji xTPN. Jeśli na true, to ustawia false dla produkcji.
     * @param status (<b>boolean</b>) true, jeśli tranzycja jest aktywna.
     */
    public void setActivationStatusXTPN(boolean status) {
        isActivated_xTPN = status;

        if(status) { //jeśli true
            isProducing_xTPN = false; //zawsze odwrotność
        }
    }

    /**
     * Metoda zwraca status aktywacji tranzycji xTPN.
     * @return (<b>boolean</b>) - true, jeśli tranzycja jest aktywna.
     */
    public boolean isActivated_xTPN() {
        return isActivated_xTPN;
    }

    /**
     * Metoda ustawia status produkcji tranzycji xTPN. Jeśli na true, to ustawia false dla aktywacji.
     * @param status (<b>boolean</b>) true, jeśli tranzycja rozpoczęła produkcję.
     */
    public void setProductionStatus_xTPN(boolean status) {
        isProducing_xTPN = status;

        if(status) { //jeśli true
            isActivated_xTPN = false; //zawsze odwrotność
        }
    }

    /**
     * Metoda zwraca status status produkcji tranzycji xTPN.
     * @return (<b>boolean</b>) - true, jeśli tranzycja rozpoczęła produkcję.
     */
    public boolean isProducing_xTPN() {
        return isProducing_xTPN;
    }

    /**
     * Metoda włącza tryb alfa-XTPN dla tranzycji.
     * @param status (<b>boolean</b>) true, jeśli tryb alfa-XTPN ma być aktywny
     */
    public void setAlphaModeStatus(boolean status) {
        alphaMode_xTPN = status;
        //setAlphaRangeStatus(status);
    }

    /**
     * Metoda zwraca status alfa tranzycji XTPN.
     * @return (<b>boolean</b>) - true, jeśli tryb alfa-XTPN aktywny.
     */
    public boolean isAlphaModeActive() {
        return alphaMode_xTPN;
    }
    /**
     * Metoda włącza tryb beta-XTPN dla tranzycji.
     * @param status (<b>boolean</b>) true, jeśli tryb beta-XTPN ma być aktywny.
     */
    public void setBetaModeStatus(boolean status) {
        betaMode_xTPN = status;
    }

    /**
     * Metoda zwraca status beta tranzycji XTPN / wszystkie inne.
     * @return (<b>boolean</b>) - true, jeśli tryb beta-XTPN aktywny.
     */
    public boolean isBetaModeActive() {
        return betaMode_xTPN;
    }

    /**
     * Metoda włącza tryb mass-action kinetics XTPN dla tranzycji.
     * @param status (<b>boolean</b>) true, jeśli tryb mass-action kinetics XTPN ma być aktywny.
     */
    public void setMassActionKineticsXTPNstatus(boolean status) {
        massActionKinetics = status;
    }

    /**
     * Metoda zwraca status trybu natychmiastowej tranzycji XTPN.
     * @return (<b>boolean</b>) - true, jeśli tryb natychmiastowy.
     */
    public boolean isImmediateXTPN() {
        return isImmediateXTPN;
    }

    /**
     * Metoda ustawia status trybu natychmiastowej tranzycji XTPN.
     * @param immediateXTPN (<b>boolean</b>) true, jeśli ma być natychmiastowa.
     */
    public void setImmediateStatusXTPN(boolean immediateXTPN) {
        isImmediateXTPN = immediateXTPN;
    }

    /**
     * Metoda zwraca status trybu mass-action kinetics XTPN dla tranzycji.
     * @return (<b>boolean</b>) - true, jeśli tryb mass-action kinetics XTPN ma być aktywny
     */
    public boolean isMassActionKineticsActiveXTPN() {
        return massActionKinetics;
    }

    public boolean isInputTransition() {
        return getInArcs().size() == 0;
    }
    public boolean isOutputTransition() {
        return getOutArcs().size() == 0;
    }

    /**
     * [2022-07-12] Dła łuków: normal, inhibitor
     * Metoda pozwala sprawdzić, czy tranzycja XTPN może być aktywowana i w jaki sposób. Pomija produkujące - wtedy zwraca false.
     * @param accuracy (<b>double</b>) dokładność obliczeń.
     * @return (<b>boolean</b>) true, jeżeli aktywna.
     */
    public boolean isActiveTransitionXTPN(double accuracy) {
        if (offline || isProducing_xTPN()) {
            return false;
        }

        // accuracy = SimulatorGlobals.calculationsAccuracy;

        for (Arc arc : getInArcs()) { //jeśli brak, to aktywna wejściowa
            if( !(arc.getStartNode() instanceof PlaceXTPN) ) {
                GUIManager.getDefaultGUIManager().log("Error, non-XTPN place found! Place: "+arc.getStartNode().getName(), "error", true);
                break;
            }

            PlaceXTPN arcStartPlace = (PlaceXTPN) arc.getStartNode();

            Arc.TypeOfArc arcType = arc.getArcType();
            int arcWeight = arc.getWeight();

            if (arcStartPlace.isGammaModeActive()) { //czy miejsce typu XTPN
                if (arcStartPlace.accessMultiset().size() < arcWeight) {
                    //jeśli nie istnieje podzbiór aktywujący (bo sam multizbiór jest mniejszy niż waga łuku), to:
                    if(arcType == Arc.TypeOfArc.INHIBITOR) { //to dobrze, że nie ma zbioru, tranzycja wciąż aktywna
                        //czyli to nic nie znaczy, że za mało tokenów: przynajmniej inhibitor nie blokuje
                    } else {
                        return false;
                    }
                } else { //multizbiór ma przynajmniej tyle tokenów ile wynosi waga łuku, sprawdzamy podzbiór aktywujący:
                    if (!isActivationMultiset(arcWeight, arcStartPlace.getGammaMinValue(), arcStartPlace.accessMultiset(), accuracy)) {
                        //jeśli nie istnieje podzbiór aktywujący (powyżej), to:
                        if(arcType != Arc.TypeOfArc.INHIBITOR) { //jeśli to inhibitor, to ok, jeżeli nie false
                            return false; //brak multizbioru aktywującego, nieaktywna
                        }
                    } else { //istnieje zbiór aktywujący
                        if(arcType == Arc.TypeOfArc.INHIBITOR) {
                            return false;
                        }
                    }
                }
            } else { //miejsce traktowane jak klasyczne
                int startPlaceTokens = arcStartPlace.getNonReservedTokensNumber();
                if (arcWeight > startPlaceTokens) { //większa waga niż liczba tokenów w miejscu
                    if(arcType != Arc.TypeOfArc.INHIBITOR) {
                        return false; //tylko gdy to nie inhibitor
                    }
                } else { //waga mniejsza lub równa tokenom
                    if(arcType == Arc.TypeOfArc.INHIBITOR) {
                        return false;
                    }
                }
            }
        }
        return true;
        //jeśli wciąż tutaj dojdziemy, to znaczy, że aktywna
    }

    /**
     * Deaktywacja tranzycji XTPN. Kiedy przestaje być aktywna z powodu utraty tokenów LUB po wyprodukowaniu
     * @param graphics (<b>boolean</b>) true, jeżeli działamy w trybie graficznym, wtedy czyści kolor łuków wejściowych
     */
    public void deactivateTransitionXTPN(boolean graphics) {
        setActivationStatusXTPN(false);
        setProductionStatus_xTPN(false);
        if(alphaMode_xTPN) {
            setTimerAlfaValue(-1.0);
            setTauAlphaValue(-1.0);
        }
        if(betaMode_xTPN) {
            setTimerBetaValue(-1.0);
            setTauBetaValue(-1.0);
        }

        if(graphics) {
            ArrayList<Arc> arcs = getInArcs();
            for (Arc arc : arcs) {
                arc.setXTPNactStatus(false);
            }
        }
    }

    /**
     * Metoda sprawdza czy istnieje podzbiór aktywujący o zadanych parametrach.
     * @param size (<b>int</b>) minimalny rozmiar zbioru.
     * @param gammaMin (<b>double</b>) gammaMinimum dla tranzycji.
     * @param multiset (<b>ArrayList[Double]</b>)
     * @param accuracy (<b>double</b>) dokładność obliczeń.
     * @return (<b>double</b>) - true, jeżeli istnieje podzbiór aktywujący
     */
    private boolean isActivationMultiset(int size, double gammaMin, ArrayList<Double> multiset, double accuracy) {
        int counter = 0;
        for(Double kappa : multiset) {
            if(kappa + accuracy > gammaMin) {
                counter++;
                if(counter == size)
                    return true;
            }
        }
        return false;
    }

    public void cleanHistoryVectors() {
        statesHistory.clear();
        statesTimeHistory.clear();
    }

    public void addHistoryMoment(double state, double time) {
        statesHistory.add(state);
        statesTimeHistory.add(time);
    }
}
