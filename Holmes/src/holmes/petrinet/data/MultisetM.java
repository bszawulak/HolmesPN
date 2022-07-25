package holmes.petrinet.data;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;

import holmes.darkgui.GUIManager;
import holmes.petrinet.elements.Place;
import holmes.petrinet.elements.PlaceXTPN;

/**
 * Klasa zarządzająca stanem sieci XTPN, tj. liczbą tokenów w miejscach. Zawiera multizbiory reprezentujące
 * tokeny we wszystkich miejsach sieci.
 */
public class MultisetM implements Serializable {
    @Serial
    private static final long serialVersionUID = 2161649872359143583L;
    private final ArrayList<ArrayList<Double>> multisetM_ArrayLists;
    private final ArrayList<Integer> placesGammasVector;
    private String stateType;
    private String stateDescription;

    /**
     * Konstruktor obiektu klasy StatePlacesVectorXTPN.
     */
    public MultisetM() {
        multisetM_ArrayLists = new ArrayList<>();
        placesGammasVector = new ArrayList<>();
        stateType = "XTPN";
        stateDescription = "Default description for XTPN state.";
    }

    /**
     * Dodaje nowe miejsce (a raczej jego multizbiór K) z zadanym zbiorem tokenów do multizbioru M.
     * Używana także przy wczytywaniu sieci z pliku.
     * @param multisetK (<b>ArrayList[Double]</b>) nowy multizbiór tokenów.
     * @param isGammaMode (<b>int</b>) 1 jeśli GAMMA=ON, 0 jeśli GAMMA=OFF
     */
    public void addMultiset_K_toMultiset_M(ArrayList<Double> multisetK, int isGammaMode) {
        multisetM_ArrayLists.add(multisetK);
        placesGammasVector.add(isGammaMode);
    }

    /**
     * Usuwa multizbiór K właśnie kasowanego miejsca z multizbioru M.
     * @param index (<b>int</b>) indeks miejsca.
     * @return boolean - true, jeśli operacja się udała
     */
    public boolean removePlaceFromMultiset_M(int index) {
        if(index >= multisetM_ArrayLists.size())
            return false;

        multisetM_ArrayLists.remove(index);
        placesGammasVector.remove(index);
        return true;
    }

    /**
     * Zwraca wielkość multizbioru M.
     * @return (<b>int</b>) liczba miejsca w wektorze stanu.
     */
    public int getMultiset_M_Size() {
        return multisetM_ArrayLists.size();
    }

    /**
     * Zwraca multizbiór K tokenów dla zadanego miejsca z multizbioru M.
     * @param index (<b>int</b>) indeks miejsca.
     * @return (<b>ArrayList<Double></b>) multizbiór K tokenów dla miejsca.
     */
    public ArrayList<Double> accessMultiset_K(int index) { //było: getTokens
        if(index >= multisetM_ArrayLists.size())
            return null;
        else
            return multisetM_ArrayLists.get(index);
    }

    /**
     * Ustawia przesłany multizbiór K tokenów w multizbiorze M dla danego miejsca.
     * @param index (<b>int</b>) indeks miejsca.
     * @param multisetK (<b>ArrayList<Double></b>) nowy multizbiór K.
     */
    public void setNewMultiset_K(int index, ArrayList<Double> multisetK, int isGammaMode) { //było setTokens
        if(index < multisetM_ArrayLists.size()) {
            multisetM_ArrayLists.set(index, multisetK);
            placesGammasVector.set(index, isGammaMode);
        }
    }

    /**
     * Dodaje multizbiór K tokenów do już istniejącego multizbioru K danego miejsca.
     * @param index (<b>int</b>) indeks miejsca.
     * @param newMultiSetK (<b>ArrayList[Double]</b>) nowe tokeny do dodania.
     * @param sort (<b>boolean</b>) true, jeśli mamy sortować, niepotrzebne, gdy dodajemy zera.
     */
    public void addTokensMultiset_K(int index, ArrayList<Double> newMultiSetK, boolean sort) {
        if(index < multisetM_ArrayLists.size()) {
            ArrayList<Double> oldMultiset = multisetM_ArrayLists.get(index);
            oldMultiset.addAll(newMultiSetK);

            if(sort) {
                Collections.sort(oldMultiset);
                Collections.reverse(oldMultiset);
            }
        }
    }

    public boolean isPlaceStoredAsGammaActive(int placeIndex) {
        return placesGammasVector.get(placeIndex) == 1;
    }

    public void setPlaceGammaStatus(int placeIndex, boolean isGammaMode) {
        if(isGammaMode) {
            placesGammasVector.set(placeIndex, 1);
        } else {
            placesGammasVector.set(placeIndex, 0);
        }
    }

    /**
     * Dodawanie jednego tokenu to multizbiór K na pozycji index..
     * @param index (<b>int</b>) indeks miejsca.
     * @param token (<b>double</b>) wartość tokenu do dodania.
     */
    public void addToken(int index, double token) {
        if(index < multisetM_ArrayLists.size()) {
            ArrayList<Double> oldMultiset = multisetM_ArrayLists.get(index);
            oldMultiset.add(token);
            if(token == 0) {
                Collections.sort(oldMultiset);
                Collections.reverse(oldMultiset);
            }
        }
    }

    /**
     * Uaktualnia cały multizbiór M aktualnym stanem sieci (multizbiorami K tokenów)
     */
    public void overwriteMultiset_M_withNetState() {
        ArrayList<Place> places = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getPlaces();
        int placesNumber = places.size();
        for(int p=0; p<placesNumber; p++) {
            if( !(places.get(p) instanceof PlaceXTPN) ) {
                GUIManager.getDefaultGUIManager().log("Error 26y30923", "error", false);
                return;
            }

            ArrayList<Double> multiset = ((PlaceXTPN)places.get(p)).accessMultiset();
            ArrayList<Double> newMultiset = new ArrayList<>(multiset);

            multisetM_ArrayLists.set(p, newMultiset);
            if(((PlaceXTPN)places.get(p)).isGammaModeActiveXTPN())
                placesGammasVector.set(p, 1);
            else
                placesGammasVector.set(p, 1);
        }
    }

    /**
     * Ustawia nowy opis wektora stanów (liczby tokenów sieci).
     * @param description (<b>String</b>) nowy opis wektora stanów.
     */
    public void setDescription(String description) {
        this.stateDescription = description;
    }

    /**
     * Zwraca opis wektora stanu (liczby tokenów w sieci).
     * @return (<b>String</b>) zwraca opis wektora stanów.
     */
    public String getDescription() {
        return this.stateDescription;
    }

    /**
     * Ustawia typ wektora stanu (liczby tokenów w sieci).
     * @param type (<b>String</b>) nazwa typu wektora stanów XTPN.
     */
    public void setStateType(String type) {
        this.stateType = type;
    }

    /**
     * Zwraca nazwę typu wektora stanu (liczby tokenów sieci).
     * @return (<b>String</b>) nazwa typu.
     */
    public String getStateType() {
        return this.stateType;
    }

    /**
     * Umożliwia dostęp do wektora danych stanu sieci - multizbioru M.
     * @return (<b>ArrayList[ArrayList[Double]]</b>) multizbiór M
     */
    public ArrayList<ArrayList<Double>> accessArrayListSOfMultiset_M() {
        return this.multisetM_ArrayLists;
    }

}
