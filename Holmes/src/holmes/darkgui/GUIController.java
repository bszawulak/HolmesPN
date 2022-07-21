package holmes.darkgui;

import holmes.petrinet.data.PetriNet;

/**
 * Klasa kontroler, dla krytycznych zmiennych i stanów programu. Pierwsze udokumentowane [2022-07-20] użycie
 * wzorca projektowego, w tym wypadku Singleton. Po 9 latach i 146 000 linijek kodu. Because I'm a fucking genius
 * i kompiluję to wszystko w głowie. Ale mi się znudziło. Tak, tego się trzymajmy. Insynuacje, że już nikt nad
 * niczym w kodzie Holmesa nie panuje to kalumnie i rosyjska propaganda.
 */
public class GUIController {
    private static final GUIController theOneRing = new GUIController();

    private PetriNet.GlobalNetType currentNetType = PetriNet.GlobalNetType.PN;

    private GUIController() { //access denied, może pan pana Singletona w d*** pocałować
        //Tu jest konstruktor. Spróbujcie kanalie się tu dostać z zewnątrz. No dalej.
        //P.S. jeśli ktoś mi użyje mechanizmu refleksji, znajdę i dopadnę. Ostrzegam.
    }

    /**
     * Metoda dostępu do kontrolera.
     * @return
     */
    public static GUIController access() {
        return theOneRing;
    }


    public PetriNet.GlobalNetType getCurrentNetType() {
        return currentNetType;
    }

    public void setCurrentNetType(PetriNet.GlobalNetType currentNetType) {
        this.currentNetType = currentNetType;
    }
}
