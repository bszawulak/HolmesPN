package holmes.petrinet.elements.containers;

import java.awt.*;

/**
 * Klasa kontener dla rysowania grafiki związanej z modułem qSim.
 */
public class PlaceQSimContainer {
    //quickSim
    public boolean qSimDrawed = false; //czy rysować dodatkowe oznaczenie miejsca - okrąg
    public int qSimOvalSize = 10; //rozmiar okręgu oznaczającego
    public Color qSimOvalColor = Color.RED;
    public Color qSimFillColor = Color.WHITE; //kolor oznaczenia
    public boolean qSimDrawStats = false; //czy rysować dodatkowe dane statystyczne
    public int qSimFillValue = 0; //poziom wypełnienia danych
    public double qSimTokens = 0; //ile średnio tokenów w symulacji
}
