package holmes.petrinet.elements.containers;

import java.awt.*;
import java.io.Serializable;

public class ArcQSimContainer implements Serializable {
    public boolean qSimForcedArc = false; //czy łuk ma być wzmocniony
    public Color qSimForcedColor = Color.BLACK; //kolor wzmocnienia
}
