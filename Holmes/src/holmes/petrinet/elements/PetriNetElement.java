package holmes.petrinet.elements;

import java.io.Serial;
import java.io.Serializable;

import org.simpleframework.xml.Element;

/**
 * Wszystkie elementy sieci Petriego, na poziomie logiki programu, są klasami
 * dziedziczącymi po tej klasie. Zapewnia ona im konieczne elementy wspólne -
 * generowanie unikalnych (w obrębie wszystkich elementów, a nie jednej klasy)
 * numery identyfikacyjne, przechowywanie nazw i komentarzy.
 */
public class PetriNetElement implements Serializable {
    @Serial
    private static final long serialVersionUID = 3428968829261305581L;

    /**
     * ARC, PLACE, TRANSITION, META, UNKNOWN
     */
    public enum PetriNetElementType {ARC, PLACE, TRANSITION, META, UNKNOWN}

    @Element
    protected int ID = -1;
    @Element
    private String name = "";
    @Element
    protected String comment = "";
    private PetriNetElementType petriNetElementType;

    private boolean isGlowedSub = false;

    /**
     * Metoda pozwala pobrać typ elementu sieci Petriego.
     * @return (<b>PetriNetElementType</b>) - obiekt elementu sieci.
     */
    public PetriNetElementType getType() {
        return this.petriNetElementType;
    }

    /**
     * Metoda pozwala ustawić typ elementu sieci Petriego.
     * @param petriNetElementType (<b>PetriNetElementType</b>)  typ elementu sieci Petriego.
     */
    public void setType(PetriNetElementType petriNetElementType) {
        this.petriNetElementType = petriNetElementType;
    }

    /**
     * Metoda pozwala pobrać identyfikator elementu sieci Petriego.
     * @return (<b>int</b>) identyfikator przypisany do tego elementu sieci Petriego.
     */
    public int getID() {
        return this.ID;
    }

    /**
     * Metoda pozwala ustawić identyfikator elementu sieci Petriego.
     * @param iD (<b>int</b>) identyfikator elementu sieci Petriego.
     */
    protected void setID(int iD) {
        this.ID = iD;
    }

    public void importOnlySetID(int iD) {
        this.ID = iD;
    }

    /**
     * Metoda pozwala pobrać komentarz do elementu sieci Petriego.
     * @return (<b>String</b>) - tekst komentarza do elementu sieci Petriego.
     */
    public String getComment() {
        return comment;
    }

    /**
     * Metoda pozwala ustawić komentarz do elementu sieci Petriego.
     * @param comment (<b>String</b>) komentarz do elementu sieci Petriego.
     */
    public void setComment(String comment) {
        this.comment = comment;
    }

    /**
     * Metoda pozwala pobrać nazwę elementu sieci Petriego.
     * @return (<b>String</b>) - nazwa elementu sieci Petriego.
     */
    public String getName() {
        return name;
    }

    /**
     * Metoda pozwala ustawić nazwę elementu sieci Petriego.
     * @param name (<b>String</b>) nazwa elementu sieci Petriego.
     */
    public void setName(String name) {
        if (name.isEmpty())
            return;

        this.name = normalizeName(name);
    }

    /**
     * Metoda pomocnicza zapewniająca, że nazwa nie zawiera spacji oraz nie zaczyna się od cyfry.
     * @param name (<b>String</b>) nowa nazwa.
     * @return (<b>String</b>) - znormalizowana nazwa.
     */
    private String normalizeName(String name) {
        name = name.replace(" ", "_");
        String letter = name.substring(0, 1);
        if (letter.matches("\\d+"))
            name = "_" + name;

        return name;
    }

    /**
     * Metoda sprawdza, czy element świeci będąc częcią podsieci.
     * @return (<b>boolean</b>) true, jeżeli świeci jako podsieć; false w przeciwnym wypadku.
     */
    public boolean isGlowed_Sub() {
        return isGlowedSub;
    }

    /**
     * Metoda ustawia stan świecenia elementu jako częci podsieci.
     * @param value (<b>boolean</b>) true, jeżeli ma świecić.
     */
    public void setGlowedSub(boolean value) {
        this.isGlowedSub = value;
    }
}
