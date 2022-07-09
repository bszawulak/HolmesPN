package holmes.petrinet.data;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;

import holmes.darkgui.GUIManager;
import holmes.petrinet.elements.Place;

/**
 * Klasa zarządzająca stanem sieci XTPN, tj. liczbą tokenów w miejscach.
 * @author MR
 *
 */
public class StatePlacesVectorXTPN implements Serializable {
    @Serial
    private static final long serialVersionUID = 2161649872359143583L;
    private ArrayList<ArrayList<Double>> stateVectorXTPN;
    private String stateType;
    private String stateDescription;

    /**
     * Konstruktor obiektu klasy StatePlacesVectorXTPN.
     */
    public StatePlacesVectorXTPN() {
        stateVectorXTPN = new ArrayList<>();
        stateType = "XTPN";
        stateDescription = "Default description for XTPN state.";
    }

    /**
     * Dodaje nowe miejsce z zadanym zbiorem tokenów do wektora.
     * @param multisetK (<b>ArrayList[Double]</b>) multizbiór tokenów.
     */
    public void addPlaceXTPN(ArrayList<Double> multisetK) {
        stateVectorXTPN.add(multisetK);
    }

    /**
     * Usuwa multizbiór właśnie kasowanego miejsca z wektora tokenów.
     * @param index (<b>int</b>) indeks miejsca.
     * @return boolean - true, jeśli operacja się udała
     */
    public boolean removePlaceXTPN(int index) {
        if(index >= stateVectorXTPN.size())
            return false;

        stateVectorXTPN.remove(index);
        return true;
    }

    /**
     * Zwraca liczbę miejsc.
     * @return (<b>int</b>) liczba miejsca w wektorze stanu.
     */
    public int getSize() {
        return stateVectorXTPN.size();
    }

    /**
     * Zwraca liczbę tokenów w stanie dla zadanego miejsca.
     * @param index (<b>int</b>) indeks miejsca.
     * @return (<b>ArrayList<Double></b>) multizbiór K tokenów miejsca.
     */
    public ArrayList<Double> getMultisetK(int index) { //było: getTokens
        if(index >= stateVectorXTPN.size())
            return null;
        else
            return stateVectorXTPN.get(index);
    }

    /**
     * Ustawia przesłany multizbiór K tokenów w wektorze stanu dla danego miejsca.
     * @param index (<b>int</b>) indeks miejsca.
     * @param multisetK (<b>ArrayList<Double></b>) nowy multizbiór K.
     */
    public void setNewMultisetK(int index, ArrayList<Double> multisetK) { //było setTokens
        if(index < stateVectorXTPN.size())
            stateVectorXTPN.set(index, multisetK);
    }

    /**
     * Dodaje podzbiór tokenów do już istniejącego multizbioru danego miejsca.
     * @param index (<b>int</b>) indeks miejsca.
     * @param newMultiSetK (<b>ArrayList[Double]</b>) nowe tokeny do dodania.
     * @param sort (<b>boolean</b>) true, jeśli mamy sortować, niepotrzebne, gdy dodajemy zera.
     */
    public void addTokensMultiset(int index, ArrayList<Double> newMultiSetK, boolean sort) {
        if(index < stateVectorXTPN.size()) {
            ArrayList<Double> oldMultiset = stateVectorXTPN.get(index);
            for(double d : newMultiSetK) {
                oldMultiset.add(Double.valueOf(d));
            }
            //stateVectorXTPN.set(index, oldValue+tokens);
            if(sort) {
                Collections.sort(oldMultiset);
                Collections.reverse(oldMultiset);
            }
            int x = 1; //sprawdzić tą metodę!
        }
    }

    /**
     * Dodawanie jednego tokenu.
     * @param index (<b>int</b>) indeks miejsca.
     * @param token (<b>double</b>) wartość tokenu do dodania.
     */
    public void addToken(int index, double token) {
        if(index < stateVectorXTPN.size()) {
            ArrayList<Double> oldMultiset = stateVectorXTPN.get(index);
            oldMultiset.add(Double.valueOf(token));
            //stateVectorXTPN.set(index, oldValue+tokens);
            if(token == 0) {
                Collections.sort(oldMultiset);
                Collections.reverse(oldMultiset);
            }
            int x = 1; //sprawdzić tą metodę!
        }
    }

    /**
     * Uaktualnia cały wektor stanu XTPN aktualnym stanem sieci (multizbiorami K tokenów)
     */
    public void updateWholeVector() {
        ArrayList<Place> places = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getPlaces();
        int placesNumber = places.size();
        for(int p=0; p<placesNumber; p++) {
            ArrayList<Double> multiset = places.get(p).accessMultiset();
            ArrayList<Double> newMultiset = new ArrayList<>();
            for(double d : multiset) {
                newMultiset.add(d);
            }
            stateVectorXTPN.set(p, newMultiset);
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
     * Umożliwia dostęp do wektora danych stanu sieci - multizbioru K.
     * @return (<b>ArrayList[ArrayList[Double]]</b>) wektor stanu sieci XTPN.
     */
    public ArrayList<ArrayList<Double>> accessVector() {
        return this.stateVectorXTPN;
    }
}
