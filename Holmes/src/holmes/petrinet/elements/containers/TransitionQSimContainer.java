package holmes.petrinet.elements.containers;

import java.awt.*;
import java.io.Serializable;

/**
 * Klasa kontener dla rysowania grafiki związanej z modułem qSim.
 */
public class TransitionQSimContainer implements Serializable {
    //quickSim - kolorowanie wyników symulacji
    public boolean qSimDrawed = false; // czy rysować dodatkowe oznaczenie tranzycji - okrąg
    public int qSimOvalSize = 10; //rozmiar okręgu oznaczającego
    public Color qSimOvalColor = Color.RED;
    public Color qSimFillColor = Color.WHITE; //domyślny kolor
    public boolean qSimDrawStats = false; // czy rysować wypełnienie tranzycji
    public int qSimFillValue = 0; //poziom wypełnienia
    public double qSimFired = 0; //ile razy uruchomiona

}
