package holmes.petrinet.elements.containers;

import java.awt.*;

/**
 * Klasa używana do rysowania informacji i grafiki związanej z klastrami. Oraz inwariantami. I MCT. No dobra,
 * mnóstwo algorytmów zaznaczających coś na rysunku sieci wykorzystuje ten kontener. Knockout / Mauritius map też.
 * Dodatkowo jak zauważyłem, KTOŚ zaczął jej używać przy algorytmach dekompozycji.
 */
public class TransitionGraphicsContainer {
    private Color transColorValue = new Color(255, 255, 255);
    private boolean isColorChanged = false;        //zmiana koloru - status
    private boolean valueVisibilityStatus = false;
    private double transNumericalValue = 0.0;        //dodatkowa liczba do wyświetlenia
    private String transAdditionalText = "";
    private boolean showTransitionAddText = false;

    //inne napisy, np MCT (?)
    public int txtXoff = 0;
    public int txtYoff = 0;
    public int valueXoff = 0;
    public int valueYoff = 0;

    protected boolean isGlowedINV = false;
    protected boolean isGlowedMTC = false;
    protected int firingValueInInvariant = 0; // ile razy uruchomiona w ramach niezmiennika

    /**
     * Metoda informuje, czy tramzycja ma być rysowana z innym kolorem wypełnienia
     * @return boolean - true, jeśli ma mieć inny kolor niż domyślny
     */
    public boolean isColorChanged() {
        return isColorChanged;
    }

    /**
     * Metoda zwraca informację, czy ma być wyświetlany dodatkowy tekst obok rysunku tranzycji.
     * @return boolean - true, jeśli tak
     */
    public boolean isShowedAddText() {
        return showTransitionAddText;
    }

    public void setAddText(String txt) {
        showTransitionAddText = true;
        transAdditionalText = txt;
    }

    /**
     * Metoda zwraca dodatkowy tekst do wyświetlenia.
     * @return String - tekst
     */
    public String returnAddText() {
        return transAdditionalText;
    }

    /**
     * Metoda ustawia stan zmiany koloru oraz liczbę do wyświetlenia.
     * @param isColorChanged      boolean - true, jeśli ma rysować się w kolorze
     * @param transColorValue     Color - na jaki kolor
     * @param showNumber          boolean - true, jeśli liczba ma się wyświetlać
     * @param transNumericalValue double - liczba do wyświetlenia
     * @param showText            boolean - czy pokazać dodatkowy tekst
     * @param text                String - dodatkowy tekst do wyświetlenia
     */
    public void setColorWithNumber(boolean isColorChanged, Color transColorValue,
                                   boolean showNumber, double transNumericalValue, boolean showText, String text) {
        this.isColorChanged = isColorChanged;
        this.transColorValue = transColorValue;
        this.valueVisibilityStatus = showNumber;
        this.transNumericalValue = transNumericalValue;
        this.showTransitionAddText = showText;
        this.transAdditionalText = text;
    }

    /**
     * Metoda ustawia stan zmiany koloru oraz liczbę do wyświetlenia.
     * @param isColorChanged      boolean - true, jeśli ma rysować się w kolorze
     * @param transColorValue     Color - na jaki kolor
     * @param showNumber          boolean - true, jeśli liczba ma się wyświetlać
     * @param transNumericalValue double - liczba do wyświetlenia
     * @param showText            boolean - czy pokazać dodatkowy tekst
     * @param text                String - dodatkowy tekst do wyświetlenia
     * @param txtXoff             int - przesunięcie X tekstu
     * @param txtYoff             int - przesunięcie Y tekstu
     * @param valueXoff           int - przesunięcie X liczby
     * @param valueYoff           int - przesunięcie Y liczby
     */
    public void setColorWithNumber(boolean isColorChanged, Color transColorValue,
                                   boolean showNumber, double transNumericalValue, boolean showText, String text,
                                   int txtXoff, int txtYoff, int valueXoff, int valueYoff) {
        this.isColorChanged = isColorChanged;
        this.transColorValue = transColorValue;
        this.valueVisibilityStatus = showNumber;
        this.transNumericalValue = transNumericalValue;
        this.showTransitionAddText = showText;
        this.transAdditionalText = text;

        this.txtXoff = txtXoff;
        this.txtYoff = txtYoff;
        this.valueXoff = valueXoff;
        this.valueYoff = valueYoff;
    }

    /**
     * Reset przesunięć.
     */
    public void resetOffs() {
        this.txtXoff = 0;
        this.txtYoff = 0;
        this.valueXoff = 0;
        this.valueYoff = 0;
    }

    /**
     * Metoda informuje, czy ma się wyświetlać dodatkowa wartośc liczbowa obok rysunku tranzycji.
     * @return boolean - true, jeśli ma się wyświetlać
     */
    public boolean getNumericalValueVisibility() {
        return valueVisibilityStatus;
    }

    /**
     * Zwraca liczbę która ma się wyświetlać obok kwadratu tranzycji.
     * @return double - liczba
     */
    public double getNumericalValueDOUBLE() {
        return transNumericalValue;
    }

    /**
     * Metoda zwraca aktualnie ustawiony kolor dla tranzycji
     * @return Color - kolor
     */
    public Color getTransitionNewColor() {
        return transColorValue;
    }

    /**
     * Metoda informująca, czy tranzycja jest podświetlona kolorem
     * @return boolean - true jeśli świeci; false w przeciwnym wypadku
     */
    public boolean isGlowed() {
        return isGlowedINV;
    }

    /**
     * Metoda pozwala określić, czy tranzycja ma byc podświetlona oraz ile razy
     * występuje ona w ramach niezmiennika.
     * @param isGlowed             boolean - true, jeśli ma świecić
     * @param numericalValueShowed int - liczba uruchomień tranzycji w niezmienniku
     */
    public void setGlowedINV(boolean isGlowed, int numericalValueShowed) {
        this.isGlowedINV = isGlowed;
        this.firingValueInInvariant = numericalValueShowed;
    }

    /**
     * Metoda sprawdza, czy tranzycja świeci będąc częcią zbioru MCT.
     * @return boolean - true jeżeli świeci jako MCT; false w przeciwnym wypadku
     */
    public boolean isGlowed_MTC() {
        return isGlowedMTC;
    }

    /**
     * Metoda ustawia stan świecenia tranzycji jako częci MCT.
     * @param value boolean - true jeżeli ma świecić
     */
    public void setGlowed_MTC(boolean value) {
        this.isGlowedMTC = value;
    }

    /**
     * Metoda zwraca liczbę wystąpień uruchomień tranzycji w ramach niezmiennika.
     * @return int - liczba wystąpień uruchomień tranzycji w niezmienniku z pola firingNumber
     */
    public int getFiring_INV() {
        return this.firingValueInInvariant;
    }
}
