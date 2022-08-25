package holmes.petrinet.elements;

import holmes.darkgui.GUIManager;
import holmes.petrinet.simulators.IRandomGenerator;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

public class PlaceXTPN extends Place {
    private double gammaMin_xTPN = 0.0;
    private double gammaMax_xTPN = 99;
    private boolean gammaMode_xTPN = true;
    //grafika:
    private boolean showTokenSet_xTPN = false; //czy wyświetlać zbiór tokenów
    public boolean showQSimXTPN = false;
    private boolean gammaRangeVisibility_XTPN = true;
    private int franctionDigits = 2;
    //tokeny:
    private ArrayList<Double> multisetK;

    /**
     * Konstruktor obiektu miejsca sieci.
     * @param nodeId (<b>int</b>) identyfikator wierzchołka.
     * @param sheetId (<b>int</b>) identyfikator arkusza.
     * @param placePosition (<b>Point</b>) punkt lokalizacji.
     */
    public PlaceXTPN(int nodeId, int sheetId, Point placePosition) {
        super(nodeId, sheetId, placePosition);
        this.multisetK = new ArrayList<>();
        placeType = PlaceType.XTPN;
    }

    /**
     * Konstruktor obiektu miejsca sieci - wczytywanie sieci zewnętrznej, np. ze Snoopy.
     * @param nodeId (<b>int</b>) identyfikator wierzchołka.
     * @param elementLocations (<b>ArrayList[ElementLocation]</b>) lista lokalizacji .
     * @param name (<b>String</b>) nazwa miejsca.
     * @param comment (<b>String</b>) komentarz miejsca.
     * @param tokensNumber (<b>int</b>) liczba tokenów.
     */
    public PlaceXTPN(int nodeId, ArrayList<ElementLocation> elementLocations, String name, String comment, int tokensNumber) {
        super(nodeId, elementLocations, name, comment, tokensNumber);
        this.multisetK = new ArrayList<>();
        placeType = PlaceType.XTPN;
    }

    /**
     * Konstruktor obiektu miejsca sieci - tworzenie portali.
     * @param nodeId (<b>int</b>) identyfikator wierzchołka.
     * @param elementLocations (<b>ArrayList[ElementLocation]</b>) lista lokalizacji.
     */
    public PlaceXTPN(int nodeId, ArrayList<ElementLocation> elementLocations) {
        super(nodeId, elementLocations);
        this.multisetK = new ArrayList<>();
        placeType = PlaceType.XTPN;
    }

    /**
     * Metoda ustawia dolną wartość gammaMinimum dla xTPN.
     * @param value (double) czas gammaMinimum (=minimalny czas aktywacji.)
     * @param force (boolean) czy wymusić wartość bez weryfikacji
     */
    public void setGammaMinValue(double value, boolean force) {
        if(force) {
            this.gammaMin_xTPN = value;
            return;
        }
        if (value < 0) {
            this.gammaMin_xTPN = 0.0;
            return;
        }
        if (value > gammaMax_xTPN) { //musi być mniejszy równy niż gammaU
            this.gammaMin_xTPN = gammaMax_xTPN;
            return;
        }
        this.gammaMin_xTPN = value;
    }

    /**
     * Metoda pozwala odczytać dolną wartość gammaMinimum dla xTPN.
     * @return (double) czas gammaMinimum, minimalny czas aktywacji.
     */
    public double getGammaMinValue() {
        return this.gammaMin_xTPN;
    }

    /**
     * Metoda ustawia dolną wartość gammaMaximum dla xTPN.
     * @param value (double) czas gammaMaximum (=token lifetime limit)
     * @param force (boolean) czy wymusić wartość bez weryfikacji
     */
    public void setGammaMaxValue(double value, boolean force) {
        if(value > Integer.MAX_VALUE)
            value = Integer.MAX_VALUE - 1;

        if(force) {
            this.gammaMax_xTPN = value;
            return;
        }
        if (value < 0) {
            this.gammaMax_xTPN = -1.0; //domyślnie do redukcji -> classical Place
            return;
        }
        if (value < gammaMin_xTPN) { //musi być większy równy niż gammaL
            this.gammaMax_xTPN = gammaMin_xTPN;
            return;
        }
        this.gammaMax_xTPN = value;
    }

    /**
     * Metoda pozwala odczytać górną wartość gammaUpper dla xTPN.
     * @return (double) czas gammaUpper.
     */
    public double getGammaMaxValue() {
        return this.gammaMax_xTPN;
    }

    /**
     * Ustawia miejsce XTPN na czasowe lub klasyczne.
     * @param status (boolean) true, jeśli tryb gamma ma być aktywny - czyli miejsce ma być czasowe.
     */
    public void setGammaModeStatus(boolean status) {
        gammaMode_xTPN = status;
        if(!status)
            setGammaRangeVisibility(status);
    }

    /**
     * Sprawdzenie, czy miejsce jest typu XTPN (z włączonymi zakresami gamma).
     * @return (<b>boolean</b>>) - true, jeśli jest to miejsce czasowe.
     */
    public boolean isGammaModeActive() {
        return gammaMode_xTPN;
    }

    /**
     * Metoda ustawia status zakresów gamma - pokazywać czy nie.
     * @param status (<b>boolean</b>) true, jeśli zakresy gamma mają być pokazywane.
     */
    public void setGammaRangeVisibility(boolean status) {
        gammaRangeVisibility_XTPN = status;
    }

    /**
     * Metoda zwraca status zakresów gamma - pokazywać czy nie.
     * @return (<b>boolean</b>) - true, jeśli zakresy gamma mają być pokazywane.
     */
    public boolean isGammaRangeVisible() {
        return gammaRangeVisibility_XTPN;
    }

    /**
     * Metoda ustawia wyświetlaną dokładność po przecinku.
     * @param value (int) nowa wartość liczby cyfr przecinku.
     */
    public void setFractionForPlaceXTPN(int value) {
        franctionDigits = value;
    }

    /**
     * Metoda zwraca wyświetlaną dokładność po przecinku.
     * @return (int) aktualna wartość liczby cyfr przecinku.
     */
    public int getFractionForPlaceXTPN() {
        return franctionDigits;
    }

    /**
     * Dodawanie nowych tokenów do multizbioru K. Jeśli tranzycja nieczasowa - tylko modyfikuje sumę.
     * @param howMany (int) ile tokenów dodać
     * @param initialTime (double) wartość początkowa
     */
    public void addTokens_XTPN(int howMany, double initialTime) {
        if(isGammaModeActive()) { //tylko gdy XTPN włączone
            for (int i = 0; i < howMany; i++) {
                multisetK.add(initialTime);
            }
            if(initialTime > 0) {
                Collections.sort(multisetK);
                Collections.reverse(multisetK);
            }
        }
        modifyTokensNumber(howMany);
    }

    /**
     * Usuwa tokeny, których czas życia jest większy GammaMax.
     * @return (<b>int</b>) - liczba usuniętych tokenów.
     */
    @SuppressWarnings("UnusedReturnValue")
    public int removeOldTokens_XTPN() {
        int removed = 0;
        if(isGammaModeActive()) { //tylko gdy XTPN włączone
            for (Iterator<Double> iterator = multisetK.iterator(); iterator.hasNext();) {
                Double kappa = iterator.next();
                if(kappa >= gammaMax_xTPN) {
                    iterator.remove(); //metoda remove() iteratora
                    removed++;
                    continue;
                }
                if (Math.abs(gammaMax_xTPN - kappa) < GUIManager.getDefaultGUIManager().simSettings.getCalculationsAccuracy()) {
                    iterator.remove(); //close enough, brakuje 1e-9 lub mniej
                    removed++;
                    continue;
                }
                //czyli jeśli nie jest większy niż limit, ani nawet w okolicy, to kończymy
                //bo cała reszta w kolejności jest jeszcze młodsza:
                break;
            }
			/* //Smuteczek, nie można usuwać w pętli foreach, bo ConcurrentModificationException
			for (Double kappa : multisetK) {
				if(kappa >= gammaMax_xTPN) { //to na pewno
					multisetK.remove(kappa);
					removed++;
					continue;
				}
				if (Math.abs(gammaMax_xTPN - kappa) < GUIManager.getDefaultGUIManager().simSettings.getCalculationsAccuracy()) {
					multisetK.remove(kappa); //close enough, brakuje 1e-9 lub mniej
					removed++;
					continue;
				}
				//czyli jeśli nie jest większy niż limit, ani nawet w okolicy, to kończymy
				//bo cała reszta w kolejności jest jeszcze młodsza:
				break;
			}
			 */
        }
        modifyTokensNumber(-removed);
        return removed;
    }

    /**
     * Usuwa tokeny na potrzeby produkcji tranzycji XTPN.
     * @param howMany (<b>int</b>) ile usunąć.
     * @param mode (<b>int</b>) tryb: 0 - najstarsze, 1 - najmłodsze, 2 - losowe.
     * @param generator (<b>Random</b>) generator dla mode=2.
     * @return (<b>ArrayList[Double]</b>) - zbiór usuniętych tokenów.
     */
    public ArrayList<Double> removeTokensForProduction_XTPN(int howMany, int mode, IRandomGenerator generator) {
        ArrayList<Double> removedTokens = new ArrayList<>();

        if(!isGammaModeActive()) { //gdy XTPN wyłączone, tylko usuwamy liczbę
            modifyTokensNumber(-howMany);
            return removedTokens;
        }

        int counter = howMany;
        if(howMany > multisetK.size()) {
            GUIManager.getDefaultGUIManager().log("Error, trying to remove more tokens ("+howMany+") than\n" +
                    "the multiset size ("+multisetK.size()+")", "error", true);
            removedTokens.addAll(multisetK);
            multisetK.clear();
            return removedTokens;
        }

        if(mode == 0) { //najstarsze
            double oldOne = Double.MAX_VALUE;
            for (Iterator<Double> iterator = multisetK.iterator(); iterator.hasNext();) {  //zakładamy, że posortowany od największych
                Double kappa = iterator.next();
                removedTokens.add(kappa);
                iterator.remove();
                counter--;

                assert (oldOne >= kappa);
                oldOne = kappa;

                if(counter == 0)
                    break;
            }
        } else if (mode == 1) { //najmłodsze
            Collections.reverse(multisetK);
            double oldOne = -1.0;
            for (Iterator<Double> iterator = multisetK.iterator(); iterator.hasNext();) {  //zakładamy, że posortowany od największych
                Double kappa = iterator.next();
                removedTokens.add(kappa);
                iterator.remove();
                counter--;

                assert (oldOne <= kappa);
                oldOne = kappa;

                if(counter == 0) {
                    break;
                }
            }
            Collections.reverse(multisetK);
        } else { //losowo
            for(int i=0; i<howMany; i++) {
                int index = generator.nextInt(multisetK.size());
                removedTokens.add(multisetK.get(index));
                multisetK.remove(index);
            }
        }
        modifyTokensNumber(-howMany);
        return removedTokens;
    }

    /**
     * Zwiększanie czasu życia wszystkich tokenów na liście.
     * @param tau (double) o ile zwiększyć czas życia tokenów.
     */
    public void incTokensTime_XTPN(double tau) {
        if(isGammaModeActive()) {
            multisetK.replaceAll(aDouble -> aDouble + tau);
        } else {
            JOptionPane.showMessageDialog(null, "Critical error - tokens time update when XTPN status OFF" +
                            "\nfor place"+this.getName(),
                    "Error 587654", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Aktualizacja wartości czasowej tokenu. Potem sortowanie multizbioru.
     * @param ID (<b>ID</b>) indeks tokenu.
     * @param value (<b>value</b>) nowa wartość tokenu.
     */
    public void updateToken_XTPN(int ID, Double value) {
        if(ID > -1 && ID < multisetK.size()) {
            multisetK.set(ID, value);
        }
        Collections.sort(multisetK);
        Collections.reverse(multisetK);
    }

    /**
     * Usuwanie tokenu po indeksie.
     * @param index (<b>int</b>) indeks tokenu.
     */
    public void removeTokenByID_XTPN(int index) {
        if(index > -1 && index < multisetK.size()) {
            multisetK.remove(index);
        }
        modifyTokensNumber(-1);
    }


    /**
     * Metoda umożliwia dostęp do multizbioru K tokenów.
     * @return (<b>ArrayList[Double]</b>) - multizbiór K miejsca XTPN.
     */
    public ArrayList<Double> accessMultiset() {
        return multisetK;
    }

    /**
     * Podmienia multizbiór na nowy (np. przy zmianie stanu na jeden z przechowywanych).
     * @param newMultiset (<b>ArrayList[Double]</b>) nowy multizbiór.
     */
    public void replaceMultiset(ArrayList<Double> newMultiset) {
        multisetK = newMultiset;
        //reservedMultisetK.clear(); // ?
    }

    /**
     * Metoda kasuje multizbiór K, pozostawia tylko liczbę tokenów jako int.
     */
    public void transformXTPNintoPNpace() {
        setGammaModeStatus(false);
        multisetK.clear();
    }

    /**
     * Na podstawie liczby tokenów metoda wypełnia zerami nowy multizbiór K.
     */
    public void transformIntoXTPNplace() {
        setGammaModeStatus(true);
        for(int i=0; i<tokensNumber; i++) {
            multisetK.add(0.0);
        }
    }
}
