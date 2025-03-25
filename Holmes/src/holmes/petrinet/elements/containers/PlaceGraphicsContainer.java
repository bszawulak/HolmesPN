package holmes.petrinet.elements.containers;

import java.awt.*;
import java.io.Serializable;

/**
 * Klasa używana do rysowania informacji i grafiki związanej z miejscami. Odpowiednik TransitionGraphicsContainer.
 * Dodatkowo jak zauważyłem, KTOŚ zaczął jej używać przy algorytmach dekompozycji.
 */
public class PlaceGraphicsContainer implements Serializable {
    private boolean isColorChanged;
    private Color placeColorValue;
    private boolean valueVisibilityStatus;
    private double placeNumericalValue;
    private boolean showPlaceAddText;
    private String placeAdditionalText;
    public int txtXoff;
    public int txtYoff;
    public int valueXoff;
    public int valueYoff;

    /**
     * Metoda ustawia stan zmiany koloru oraz liczbę do wyświetlenia.
     * @param isColorChanged boolean - true, jeśli ma rysować się w kolorze
     * @param placeColorValue Color - na jaki kolor
     * @param showNumber boolean - true, jeśli liczba ma się wyświetlać
     * @param placeNumericalValue double - liczba do wyświetlenia
     * @param showText boolean - czy pokazać dodatkowy tekst
     * @param text String - dodatkowy tekst do wyświetlenia
     * @param txtXoff int - przesunięcie X tekstu
     * @param txtYoff int - przesunięcie Y tekstu
     * @param valueXoff int - przesunięcie X liczby
     * @param valueYoff int - przesunięcie Y liczby
     */
    public void setColorWithNumber(boolean isColorChanged, Color placeColorValue,
                                   boolean showNumber, double placeNumericalValue, boolean showText, String text,
                                   int txtXoff, int txtYoff, int valueXoff, int valueYoff) {
        this.isColorChanged = isColorChanged;
        this.placeColorValue = placeColorValue;
        this.valueVisibilityStatus = showNumber;
        this.placeNumericalValue = placeNumericalValue;
        this.showPlaceAddText = showText;
        this.placeAdditionalText = text;

        this.txtXoff = txtXoff;
        this.txtYoff = txtYoff;
        this.valueXoff = valueXoff;
        this.valueYoff = valueYoff;
    }

    /**
     * Metoda ustawia stan zmiany koloru oraz liczbę do wyświetlenia.
     * @param isColorChanged boolean - true, jeśli ma rysować się w kolorze
     * @param placeColorValue Color - na jaki kolor
     * @param showNumber boolean - true, jeśli liczba ma się wyświetlać
     * @param placeNumericalValue double - liczba do wyświetlenia
     * @param showText boolean - czy pokazać dodatkowy tekst
     * @param text String - dodatkowy tekst do wyświetlenia
     */
    public void setColorWithNumber(boolean isColorChanged, Color placeColorValue,
                                   boolean showNumber, double placeNumericalValue, boolean showText, String text) {
        this.isColorChanged = isColorChanged;
        this.placeColorValue = placeColorValue;
        this.valueVisibilityStatus = showNumber;
        this.placeNumericalValue = placeNumericalValue;
        this.showPlaceAddText = showText;
        this.placeAdditionalText = text;
    }

    /**
     * Metoda informuje, czy ma się wyświetlać dodatkowa wartośc liczbowa obok rysunku miejsca.
     * @return boolean - true, jeśli ma się wyświetlać
     */
    public boolean getNumericalValueVisibility() {
        return valueVisibilityStatus;
    }

    /**
     * Zwraca liczbę która ma się wyświetlać obok miejsca.
     * @return double - liczba
     */
    public double getNumericalValueDOUBLE() {
        return placeNumericalValue;
    }

    /**
     * Metoda zwraca aktualnie ustawiony kolor dla miejsca
     * @return Color - kolor
     */
    public Color getPlaceNewColor() {
        return placeColorValue;
    }

    /**
     * Metoda informuje, czy miejsce ma być rysowane z innym kolorem wypełnienia
     * @return boolean - true, jeśli ma mieć inny kolor niż domyślny
     */
    public boolean isColorChanged() {
        return isColorChanged;
    }

    /**
     * Metoda zwraca informację, czy ma być wyświetlany dodatkowy tekst obok rysunku miejsca.
     * @return boolean - true, jeśli tak
     */
    public boolean showAddText() {
        return showPlaceAddText;
    }

    /**
     * Metoda zwraca dodatkowy tekst do wyświetlenia.
     * @return String - tekst
     */
    public String returnAddText() {
        return placeAdditionalText;
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
}
