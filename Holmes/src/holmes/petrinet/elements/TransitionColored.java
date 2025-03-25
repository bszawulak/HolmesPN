package holmes.petrinet.elements;

import holmes.darkgui.GUIManager;
import holmes.darkgui.LanguageManager;
import holmes.petrinet.functions.FunctionsTools;

import java.awt.*;
import java.util.ArrayList;

/**
 * Nic tu nie ma, proszę się rozejść. Nazwanie tego eksperymentalnym prototypem (2017, 2022) to niedopowiedzenie.
 */
public class TransitionColored extends Transition {
    private static final GUIManager overlord = GUIManager.getDefaultGUIManager();
    private static final LanguageManager lang = GUIManager.getLanguageManager();
    protected int reqT0red = 1;
    protected int reqT1green = 0;
    protected int reqT2blue = 0;
    protected int reqT3yellow = 0;
    protected int reqT4gray = 0;
    protected int reqT5black = 0;

    /**
     * Konstruktor obiektu tranzycji sieci. Używany do wczytywania sieci zewnętrznej, np. ze Snoopy
     * @param transitionId (<b>int</b>) identyfikator tranzycji.
     * @param elementLocations (<b>ArrayList[ElementLocation]</b>) lista lokalizacji tranzycji.
     * @param name (<b>String</b>) nazwa tranzycji.
     * @param comment (<b>String</b>) komentarz tranzycji.
     */
    public TransitionColored(int transitionId, ArrayList<ElementLocation> elementLocations, String name, String comment) {
        super(transitionId, elementLocations, name, comment);
        transType = TransitionType.CPN;
    }

    /**
     * Konstruktor obiektu tranzycji sieci. Używany przez procedury tworzenia portali.
     * @param transitionId (<b>int</b>) identyfikator tranzycji.
     * @param elementLocations (<b>ArrayList[ElementLocation]</b>) lista lokalizacji tranzycji.
     */
    public TransitionColored(int transitionId, ArrayList<ElementLocation> elementLocations) {
        super(transitionId, elementLocations);
        transType = TransitionType.CPN;
    }

    /**
     * Konstruktor obiektu tranzycji sieci.
     * @param transitionId (<b>int</b>) identyfikator tranzycji.
     * @param sheetId (<b>int</b>) identyfikator arkusza.
     * @param transitionPosition (<b>Point</b>) punkt lokalizacji tranzycji.
     */
    public TransitionColored(int transitionId, int sheetId, Point transitionPosition) {
        super(transitionId, sheetId, transitionPosition);
        transType = TransitionType.CPN;
    }

    /**
     * Diabli wiedzą co.
     * @param error (<b>String</b>) parametr, a co?
     */
    public TransitionColored(String error) {
        super(error);
        transType = TransitionType.CPN;
    }

    /**
     * Metoda pozwala sprawdzić, czy tranzycja kolorowana jest aktywna i może zostać odpalona.
     * @return boolean - true, jeśli tranzycja jest aktywna i może zostać odpalona; false w przeciwnym wypadku
     */
    public boolean isColorActive() {
        if (knockoutStatus)
            return false;

        int req0red = 0;
        int req1green = 0;
        int req2blue = 0;
        int req3yellow = 0;
        int req4grey = 0;
        int req5black = 0;

        int tokens0red = 0;
        int tokens1green = 0;
        int tokens2blue = 0;
        int tokens3yellow = 0;
        int tokens4grey = 0;
        int tokens5black = 0;

        for (Arc arc : getInputArcs()) {
            Place arcStartPlace = (Place) arc.getStartNode();
            Arc.TypeOfArc arcType = arc.getArcType();

            if (arcType != Arc.TypeOfArc.COLOR) { //dla zwykłych łuków
                int startPlaceTokens = arcStartPlace.getNonReservedTokensNumber();
                if (arcType == Arc.TypeOfArc.INHIBITOR) {
                    if (startPlaceTokens > arc.getWeight())
                        return false; //nieaktywna
                    else
                        continue; //aktywna (nie jest w danej chwili blokowana)
                } else if (arcType == Arc.TypeOfArc.EQUAL && startPlaceTokens != arc.getWeight()) { //DOKŁADNIE TYLE CO WAGA
                    return false;
                } else {
                    if (fpnExtension.isFunctional()) { //fast, no method
                        boolean status = FunctionsTools.getFunctionDecision(startPlaceTokens, arc, arc.getWeight(), this);
                        if (!status)
                            return false; //zwróc tylko jesli false
                    } else {
                        if (startPlaceTokens < arc.getWeight())
                            return false;
                    }
                }
            }

            req0red += arc.getColorWeight(0);
            req1green += arc.getColorWeight(1);
            req2blue += arc.getColorWeight(2);
            req3yellow += arc.getColorWeight(3);
            req4grey += arc.getColorWeight(4);
            req5black += arc.getColorWeight(5);

            try {
                tokens0red += ((PlaceColored)arcStartPlace).getNonReservedColorTokensNumber(0);
                tokens1green += ((PlaceColored)arcStartPlace).getNonReservedColorTokensNumber(1);
                tokens2blue += ((PlaceColored)arcStartPlace).getNonReservedColorTokensNumber(2);
                tokens3yellow += ((PlaceColored)arcStartPlace).getNonReservedColorTokensNumber(3);
                tokens4grey += ((PlaceColored)arcStartPlace).getNonReservedColorTokensNumber(4);
                tokens5black += ((PlaceColored)arcStartPlace).getNonReservedColorTokensNumber(5);
            } catch (Exception ex) {
                overlord.log(lang.getText("LOGentry00394")+"\n"+ex.getMessage(), "error", false);
            }
        }
        return req0red > tokens0red || req1green > tokens1green || req2blue > tokens2blue || req3yellow > tokens3yellow ||
                req4grey > tokens4grey || req5black > tokens5black;
    }

    /**
     * Metoda zwraca liczbę potrzebnych tokenów do produkcji (z danego koloru)
     *
     * @param i int - nr porzadkowy koloru, default 0, od 0 do 5
     * @return int - wymagana liczba tokenów danego koloru
     */
    public int getRequiredColoredTokens(int i) {
        return switch (i) {
            case 1 -> reqT1green;
            case 2 -> reqT2blue;
            case 3 -> reqT3yellow;
            case 4 -> reqT4gray;
            case 5 -> reqT5black;
            default -> reqT0red;
        };
    }

    /**
     * Metoda ustawia wymaganą liczbę tokenów danego koloru dla aktywacji tranzycji.
     *
     * @param tokens int - liczba tokenów
     * @param i      int - nr porządkowy koloru, default 0, od 0 do 5
     */
    public void setRequiredColoredTokens(int tokens, int i) {
        switch (i) {
            case 1 -> reqT1green = tokens;
            case 2 -> reqT2blue = tokens;
            case 3 -> reqT3yellow = tokens;
            case 4 -> reqT4gray = tokens;
            case 5 -> reqT5black = tokens;
            default -> reqT0red = tokens;
        }
    }
}
