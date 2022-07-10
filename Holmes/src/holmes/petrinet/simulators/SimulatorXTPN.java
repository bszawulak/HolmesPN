package holmes.petrinet.simulators;

import java.util.ArrayList;
import java.util.Collections;

import holmes.darkgui.GUIManager;
import holmes.petrinet.elements.Place;
import holmes.petrinet.elements.Transition;

/**
 * Silnik symulatora XTPN. Procedury odpowiedzialne za tworzenie
 * listy tranzycji, które mają odpalić w kolejnym kroku symulacji.
 *
 * @author MR
 */
public class SimulatorXTPN implements IEngine {
    private GUIManager overlord;
    private SimulatorGlobals.SimNetType netSimTypeXTPN = SimulatorGlobals.SimNetType.XTPN;
    private ArrayList<Transition> transitions = null;
    private ArrayList<Integer> transitionsIndexList = null;
    private ArrayList<Transition> launchableTransitions = null;
    private IRandomGenerator generator = null;

    //jesli true, wtedy TDPN dziala tak, że clock liczy do EFT i zaraz potem wchodzi DPN
    private boolean TDPNdecision1 = false;

    /**
     * Konstruktor obiektu klasy SimulatorEngine.
     */
    public SimulatorXTPN() {
        this.overlord = GUIManager.getDefaultGUIManager();
        generator = new StandardRandom(System.currentTimeMillis());
    }

    /**
     * Ustawianie podstawowych parametrów silnika symulacji.
     * @param simulationType NetType - rodzaj symulowanej sieci
     * @param maxMode boolean - tryb maximum    IGNORED
     * @param singleMode boolean - true, jeśli tylko 1 tranzycja ma odpalić IGNORED
     * @param transitions ArrayList[Transition] - wektor wszystkich tranzycji
     * @param time_transitions ArrayList[Transition] - wektor tranzycji czasowych   IGNORED
     * @param places ArrayList[Place] - wektor miejsc
     */
    public void setEngine(SimulatorGlobals.SimNetType simulationType, boolean maxMode, boolean singleMode,
                          ArrayList<Transition> transitions, ArrayList<Transition> time_transitions,
                          ArrayList<Place> places) {
        this.netSimTypeXTPN = simulationType;

        if(overlord.simSettings.getGeneratorType() == 1) {
            this.generator = new HighQualityRandom(System.currentTimeMillis());
        } else {
            this.generator = new StandardRandom(System.currentTimeMillis());
        }

        if(overlord.getSettingsManager().getValue("simTDPNrunWhenEft").equals("1"))
            TDPNdecision1 = true;
        else
            TDPNdecision1 = false;

        //INIT:
        this.transitions = transitions;
        transitionsIndexList = new ArrayList<Integer>();
        launchableTransitions =  new ArrayList<Transition>();

        for(int t=0; t<transitions.size(); t++) {
            transitionsIndexList.add(t);
        }
    }

    @Override
    public void setNetSimType(SimulatorGlobals.SimNetType simulationType) {

    }

    /**
     * Metoda generuje zbiór tranzycji do uruchomienia w ramach symulatora.
     * @param emptySteps boolean - true, jeśli może być wygenerowany krok bez odpalania tranzycji
     * @return ArrayList[Transition] - zbiór tranzycji do uruchomienia
     */
    public ArrayList<Transition> getTransLaunchList(boolean emptySteps) {
        if(emptySteps == true) {
            generateNormal();
        } else {
            generateWithoutEmptySteps();
        }
        return launchableTransitions;
    }

    /**
     * Metoda generowania nowych tranzycji do odpalenia bez pustych kroków (pusty wektor tranzycji
     * do odpalenia możliwy tylko w przypadku sieci czasowych).
     * @return ArrayList[Transition] - wektor tranzycji do odpalenia
     */
    private ArrayList<Transition> generateWithoutEmptySteps() {
        boolean generated = false;
        int safetyCounter = 0;
        while (!generated) {
            generateLaunchingTransitions(netSimTypeXTPN);
            if (launchableTransitions.size() > 0) {
                generated = true;
            } else {
                if (netSimTypeXTPN == SimulatorGlobals.SimNetType.XTPN) { //TODO
                    return launchableTransitions;
                } else {
                    safetyCounter++;
                    if(safetyCounter == 9) { // safety measure
                        if(isPossibleStep(transitions) == false) {
                            GUIManager.getDefaultGUIManager().log("Error, no active transition, yet generateValidLaunchingTransitions "
                                    + "has been activated. Please advise authors if this error show up frequently.", "error", true);
                            generated = true;
                            //return launchableTransitions;
                        }
                    }
                }
            }
        }
        return launchableTransitions; //bez tej linii będzie błąd, tak, wiem, że to vs. powyższe jest bez sensu.
    }

    /**
     * Metoda generowania nowych tranzycji do odpalenia dopuszczający puste kroki.
     * @return ArrayList[Transition] - wektor tranzycji do odpalenia
     */
    private ArrayList<Transition> generateNormal() {
        generateLaunchingTransitions(netSimTypeXTPN);
        return launchableTransitions;
    }

    /**
     * Metoda pomocnicza dla generateValidLaunchingTransitions(), odpowiedzialna za sprawdzenie
     * które tranzycje nadają się do uruchomienia. Aktualnie działa dla modelu klasycznego PN
     * oraz czasowego.
     */
    private void generateLaunchingTransitions(SimulatorGlobals.SimNetType simulationType) {
        launchableTransitions.clear();

        if (simulationType == SimulatorGlobals.SimNetType.XTPN) {
            Collections.shuffle(transitionsIndexList);

            for (int i = 0; i < transitionsIndexList.size(); i++) {
                Transition transition = transitions.get(transitionsIndexList.get(i));
                if (transition.isActive() ) {
                    if ((generator.nextInt(100) < 50) ) { // 50% 0-4 / 5-9
                        transition.bookRequiredTokens();
                        launchableTransitions.add(transition);
                    }
                }
            }
        }

        for (Transition transition : launchableTransitions) {
            transition.returnBookedTokens();
        }
    }

    /**
     * Metoda sprawdzająca, czy krok jest możliwy - czy istnieje choć jedna aktywna tranzycja.
     * @return boolean - true jeśli jest choć jedna aktywna tranzycja; false w przeciwnym wypadku
     */
    private boolean isPossibleStep(ArrayList<Transition> transitions) {
        for (Transition transition : transitions) {
            if (transition.isActive())
                return true;
        }
        return false;
    }

    /**
     * Metoda zwraca liczbę losową typu int z podanego zakresu.
     * @param min int - dolna granica
     * @param max int - górna granica
     * @return int - liczba z zakresu [min, max]
     */
    private int getRandomInt(int min, int max) {
        if(min == 0 && max == 0)
            return 0;
        if(min == max)
            return min;

        return generator.nextInt((max - min) + 1) + min; //OK, zakres np. 3do6 daje: 3,4,5,6 (graniczne obie też!)
    }

    @Override
    public void setMaxMode(boolean value) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setSingleMode(boolean value) {
        // TODO Auto-generated method stub

    }

    /**
     * Zwraca aktualnie ustawiony generator liczb pseudo-losowych.
     * @return IRandomGenerator
     */
    public IRandomGenerator getGenerator() {
        return this.generator;
    }
}
