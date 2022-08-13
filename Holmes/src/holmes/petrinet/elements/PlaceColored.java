package holmes.petrinet.elements;


import java.awt.*;
import java.util.ArrayList;

public class PlaceColored extends Place {
    //colors:
    public int token1green = 0;
    public int token2blue = 0;
    public int token3yellow = 0;
    public int token4grey = 0;
    public int token5black = 0;
    public int reserved1green = 0;
    public int reserved2blue = 0;
    public int reserved3yellow = 0;
    public int reserved4grey = 0;
    public int reserved5black = 0;

    /**
     * Konstruktor obiektu miejsca sieci.
     * @param nodeId (<b>int</b>) identyfikator wierzchołka.
     * @param sheetId (<b>int</b>) identyfikator arkusza.
     * @param placePosition (<b>Point</b>) punkt lokalizacji.
     */
    public PlaceColored(int nodeId, int sheetId, Point placePosition) {
        super(nodeId, sheetId, placePosition);
        isColored = true;
        setPlaceType(PlaceType.CPN);
    }

    /**
     * Konstruktor obiektu miejsca sieci - wczytywanie sieci zewnętrznej, np. ze Snoopy.
     * @param nodeId (<b>int</b>) identyfikator wierzchołka.
     * @param elementLocations (<b>ArrayList[ElementLocation]</b>) lista lokalizacji .
     * @param name (<b>String</b>) nazwa miejsca.
     * @param comment (<b>String</b>) komentarz miejsca.
     * @param tokensNumber (<b>int</b>) liczba tokenów.
     */
    public PlaceColored(int nodeId, ArrayList<ElementLocation> elementLocations, String name, String comment, int tokensNumber) {
        super(nodeId, elementLocations, name, comment, tokensNumber);
        isColored = true;
        setPlaceType(PlaceType.CPN);
    }

    /**
     * Konstruktor obiektu miejsca sieci - tworzenie portali.
     * @param nodeId (<b>int</b>) identyfikator wierzchołka.
     * @param elementLocations (<b>ArrayList[ElementLocation]</b>) lista lokalizacji.
     */
    public PlaceColored(int nodeId, ArrayList<ElementLocation> elementLocations) {
        super(nodeId, elementLocations);
        isColored = true;
        setPlaceType(PlaceType.CPN);
    }

    /**
     * Metoda pozwala odczytać aktualną liczbę kolorowych tokenów z miejsca.
     * @param i int - nr porządkowy tokenu, default 0, od 0 do 5
     * @return int - liczba tokenów kolorowych
     */
    public int getColorTokensNumber(int i) {
        return switch (i) {
            case 1 -> token1green;
            case 2 -> token2blue;
            case 3 -> token3yellow;
            case 4 -> token4grey;
            case 5 -> token5black;
            default -> tokensNumber;
        };
    }

    /**
     * Metoda pozwala ustawić wartość liczby kolorowych tokenów dla miejsca.
     * @param tokensNumber int - nowa liczba tokenów
     * @param i int - nr porządkowy tokenu, default 0, od 0 do 1
     */
    public void setColorTokensNumber(int tokensNumber, int i) {
        switch (i) {
            case 1 -> this.token1green = Math.max(tokensNumber, 0);
            case 2 -> this.token2blue = Math.max(tokensNumber, 0);
            case 3 -> this.token3yellow = Math.max(tokensNumber, 0);
            case 4 -> this.token4grey = Math.max(tokensNumber, 0);
            case 5 -> this.token5black = Math.max(tokensNumber, 0);
            default -> this.tokensNumber = Math.max(tokensNumber, 0);
        }
    }

    /**
     * Metoda pozwala zmienić liczbę tokenów w miejscu, dodając do niej określoną wartość.
     * @param delta int - wartość o którą zmieni się liczba kolorowych tokenów
     * @param i int - nr porządkowy tokenu, default 0, od 0 do 5
     */
    public void modifyColorTokensNumber(int delta, int i) {
        switch (i) {
            case 1 -> {
                this.token1green += delta;
                if (this.tokensNumber < 0) {
                    this.token1green = 0;
                }
            }
            case 2 -> {
                this.token2blue += delta;
                if (this.tokensNumber < 0) {
                    this.token2blue = 0;
                }
            }
            case 3 -> {
                this.token3yellow += delta;
                if (this.tokensNumber < 0) {
                    this.token3yellow = 0;
                }
            }
            case 4 -> {
                this.token4grey += delta;
                if (this.tokensNumber < 0) {
                    this.token4grey = 0;
                }
            }
            case 5 -> {
                this.token5black += delta;
                if (this.tokensNumber < 0) {
                    this.token5black = 0;
                }
            }
            default -> {
                this.tokensNumber += delta;
                if (this.tokensNumber < 0) {
                    this.tokensNumber = 0;
                }
            }
        }
    }

    /**
     * Metoda zwraca liczbę zarezerwowanych kolorowych tokenów (0-5)
     * @param i (<b>int</b>) nr porządkowy tokenu, default 0, od 0 do 5
     * @return (<b>int</b>) - liczba zarezerwowanych tokenów
     */
    public int getReservedColorTokens(int i) {
        return switch (i) {
            case 1 -> reserved1green;
            case 2 -> reserved2blue;
            case 3 -> reserved3yellow;
            case 4 -> reserved4grey;
            case 5 -> reserved5black;
            default -> reservedTokens;
        };
    }

    /**
     * Metoda pozwala zarezerwować określoną liczbę kolorowych tokenów w miejscu.
     * @param tokensTaken (int) liczba zajmowanych tokenów
     * @param i (int) nr porządkowy kolorowanego tokeny, dafult 0, od 0 do 5
     */
    public void reserveColorTokens(int tokensTaken, int i) {
        switch (i) {
            case 1 -> this.reserved1green += tokensTaken;
            case 2 -> this.reserved2blue += tokensTaken;
            case 3 -> this.reserved3yellow += tokensTaken;
            case 4 -> this.reserved4grey += tokensTaken;
            case 5 -> this.reserved5black += tokensTaken;
            default -> this.reservedTokens += tokensTaken;
        }
    }

    /**
     * Metoda pozwala zwolnić wszystkie zarezerwowane tokeny.
     */
    public void freeReservedTokens() {
        this.reservedTokens = 0;
        this.reserved1green = 0;
        this.reserved2blue = 0;
        this.reserved3yellow = 0;
        this.reserved4grey = 0;
        this.reserved5black = 0;
    }


    /**
     * Metoda zwalnia wszystkie zarezerwowane kolorowe tokeny.
     * @param i int - nr porządkowy tokenu, default 0, od 0 do 5
     */
    public void freeReservedColorTokens(int i) {
        switch (i) {
            case 1 -> this.reserved1green = 0;
            case 2 -> this.reserved2blue = 0;
            case 3 -> this.reserved3yellow = 0;
            case 4 -> this.reserved4grey = 0;
            case 5 -> this.reserved5black = 0;
            default -> this.reservedTokens = 0;
        }

    }

    /**
     * Metoda pobiera zarezerwowane wolne kolorowe tokeny.
     * @param i int - nr porządkowy tokenu, default 0, od 0 do 5
     * @return int - liczba dostępnych tokenów
     */
    public int getNonReservedColorTokensNumber(int i) {
        return switch (i) {
            case 0 -> tokensNumber - getReservedColorTokens(0);
            case 1 -> token1green - getReservedColorTokens(1);
            case 2 -> token2blue - getReservedColorTokens(2);
            case 3 -> token3yellow - getReservedColorTokens(3);
            case 4 -> token4grey - getReservedColorTokens(4);
            case 5 -> token5black - getReservedColorTokens(5);
            default -> tokensNumber - getReservedTokens();
        };
    }
}
