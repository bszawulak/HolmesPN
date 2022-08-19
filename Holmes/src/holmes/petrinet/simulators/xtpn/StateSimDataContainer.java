package holmes.petrinet.simulators.xtpn;

import java.util.ArrayList;

/**
 * Klasa kontener dla danych z szybkiej symulacji. Macierz tranzycji zawiera tyle list ile kroków symulacji,
 * a każdy wpis to krotka: liczba kroków w fazach: nieaktywna, aktywna, produkująca, uruchomion; czas w fazach:
 * nieaktywności, aktywności, produkcji.
 * Dla miejsc: placesTokensDataMatrix zawiera stan wszystkich miejsc w każdym kroku symulacji.
 */
public class StateSimDataContainer {
    public ArrayList<ArrayList<Double>> transDataMatrix = new ArrayList<>();

    /** Każdy rekord to box z informacją, ile razy (przy powtórzeniach) tranzycja była w danym kroku nieaktywna, aktywna, produkująca, odpalająca */
    public ArrayList<StateSimulatorXTPN.TransitionStepStats> transitionsSimHistory = new ArrayList<>();


    public ArrayList<ArrayList<Double>> placesTokensHistory = new ArrayList<>();
    /** Średnia liczba tokenów w każdym kroku / powtórzenia */
    public ArrayList<Double> avgTokens = new ArrayList<>();
    /** Średnia liczba odpaleń w każdym kroku / powtórzenia */
    public ArrayList<Double> avgFires = new ArrayList<>();
    public ArrayList<Double> avtTimeForStep = new ArrayList<>();


    public double simSteps = 0.0;
    public double simTime = 0.0;
    public double simReps = 0.0;
    public long compTime = 0;
}
