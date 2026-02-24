package holmes.analyse.XTPN;

import holmes.darkgui.GUIManager;
import holmes.petrinet.elements.PlaceXTPN;
import holmes.petrinet.elements.Transition;
import holmes.petrinet.elements.TransitionXTPN;
import holmes.petrinet.elements.Arc;
import holmes.windows.HolmesNotepad;

import javax.swing.JOptionPane;
import java.util.HashSet;
import java.util.Set;
import java.util.ArrayList;
import java.util.List;

public final class MaxTokensBoundCalculator {
    private static final GUIManager overlord = GUIManager.getDefaultGUIManager();
    // ====== CACHE DLA TRYBU PROSTEGO (tylko wejścia) ======
    private static List<Integer> cachedFastTimesSimple = new ArrayList<>();
    private static List<Integer> cachedArcWeightsSimple = new ArrayList<>();
    private static int cachedGammaUSimple = 0;
    private static PlaceXTPN cachedPlaceSimple = null;
    private static boolean cacheInitializedSimple = false;

    // ====== CACHE DLA TRYBU ROZSZERZONEGO (wejścia + wyjścia) ======

    // Stałe dla heurystyki extended:
    private static final double DRIFT_EPSILON = 1e-6;          // próg "braku dryfu"
    private static final int    EXTENDED_EXTRA_PERIODS_NO_DRIFT = 3; // ile makro-okresów w przypadku braku dryfu
    private static final long   EXTENDED_MAX_ITERATIONS_CAP = 100_000L; // bezpieczny górny limit kroków

    // Producenci (tranzycje wejściowe względem miejsca p_x)
    private static List<Integer> cachedFastInTimesExt = new ArrayList<>();
    private static List<Integer> cachedInWeightsExt   = new ArrayList<>();
    // Konsumenci "bezpieczni" (EXT_SAFE: tylko tranzycje zależne wyłącznie od p_x)
    private static List<Integer> cachedOutSlowTimesSafeExt = new ArrayList<>();
    private static List<Integer> cachedOutWeightsSafeExt   = new ArrayList<>();
    // Konsumenci "wszyscy" (EXT_UNSAFE: wszystkie tranzycje wyjściowe)
    private static List<Integer> cachedOutSlowTimesAllExt = new ArrayList<>();
    private static List<Integer> cachedOutWeightsAllExt   = new ArrayList<>();
    private static int cachedGammaUExt   = 0;   // gamma^U_{p_x}
    private static int cachedGammaLExt   = 0;   // gamma^L_{p_x}
    private static PlaceXTPN cachedPlaceExt = null;
    private static boolean cacheInitializedExt = false;

    // DODAJ w polach klasy:
    private static List<Integer> cachedOutAlphaTimesSafeExt = new ArrayList<>();
    private static List<Integer> cachedOutBetaTimesSafeExt  = new ArrayList<>();
    private static List<Integer> cachedOutAlphaTimesAllExt  = new ArrayList<>();
    private static List<Integer> cachedOutBetaTimesAllExt   = new ArrayList<>();


    private MaxTokensBoundCalculator() {
        // Klasa narzędziowa – brak publicznego konstruktora.
    }

    public static long maxSteps(PlaceXTPN placeXTPN) {
        if (placeXTPN == null) {
            throw new IllegalArgumentException("placeXTPN must not be null");
        }

        // ------------------------------------------------------------
        // KROK 0: Walidacja pod-sieci (tranzycje wejściowe/wyjściowe p_x)
        // ------------------------------------------------------------
        //
        //  wynik < 0  -> błąd lub użytkownik wybrał "Stop" -> zwracamy 0
        //  wynik == 0 -> brak zerowych L, wszystko ok
        //  wynik == 1 -> są zerowe L, użytkownik zgodził się na "min. time = 1"
        //
        int validationResult = validateSubnetCorrectness(placeXTPN);
        if (validationResult < 0) {
            // Nie kontynuujemy obliczeń.
            return 0L;
        }
        boolean fixZeroTimes = (validationResult > 0);

        // ------------------------------------------------------------
        // KROK 1: Reset cache trybu prostego
        // ------------------------------------------------------------
        cachedFastTimesSimple = new ArrayList<>();
        cachedArcWeightsSimple = new ArrayList<>();
        cacheInitializedSimple = false;
        cachedPlaceSimple = placeXTPN;

        int gammaU = (int) Math.round(placeXTPN.getGammaMaxValue());
        if (gammaU < 0) {
            gammaU = 0;
        }
        cachedGammaUSimple = gammaU;

        // ------------------------------------------------------------
        // KROK 2: Zbudowanie listy producentów (tranzycje wejściowe do p_x)
        //         z uwzględnieniem:
        //          - isKnockedOut(),
        //          - trybów alfa/beta,
        //          - ewentualnej zamiany 0 -> 1 dla L.
        // ------------------------------------------------------------
        List<Arc> inputArcs = placeXTPN.getInputArcs(); // łuki: Transition -> Place
        if (inputArcs != null) {
            for (Arc arc : inputArcs) {
                if (arc == null) {
                    continue;
                }

                // Interesują nas tylko klasyczne łuki transportujące tokeny
                if (arc.getArcType() != Arc.TypeOfArc.NORMAL) {
                    continue; // READARC, INHIBITOR, itp. ignorujemy w trybie prostym
                }

                Object startNode = arc.getStartLocation().getParentNode();
                if (!(startNode instanceof TransitionXTPN)) {
                    continue;  // Dla bezpieczeństwa pomijamy, jeśli to nie jest TransitionXTPN
                }

                TransitionXTPN t = (TransitionXTPN) startNode;

                // Jeśli tranzycja jest "knocked out" – traktujemy jakby nie istniała
                if (t.isKnockedOut()) {
                    continue;
                }

                int weight = arc.getWeight();
                if (weight <= 0) {
                    continue;
                }

                boolean alphaActive = t.isAlphaModeActive();
                boolean betaActive  = t.isBetaModeActive();

                // Jeśli oba tryby są nieaktywne – ta tranzycja jest logicznie błędna
                // (powinniśmy już ją złapać w validateSubnetCorrectness), ale dla
                // bezpieczeństwa tu też ją pomijamy.
                if (!alphaActive && !betaActive) {
                    continue;
                }

                double alphaMin = t.getAlphaMinValue();
                double betaMin  = t.getBetaMinValue();

                // Jeśli użytkownik zgodził się na "min. time = 1", zamieniamy zera
                // w aktywnych trybach L na 1. Dzięki temu fast(t) nigdy nie będzie 0.
                if (fixZeroTimes) {
                    if (alphaActive && alphaMin == 0.0) {
                        alphaMin = 1.0;
                    }
                    if (betaActive && betaMin == 0.0) {
                        betaMin = 1.0;
                    }
                }

                // Wyznacz fast(t) w zależności od trybów alfa/beta:
                //  - oba aktywne    -> α^L + β^L (klasyczne xTPN),
                //  - tylko alfa     -> α^L      (tryb TPN),
                //  - tylko beta     -> β^L      (tryb DPN).
                double fastDouble;
                if (alphaActive && betaActive) {
                    fastDouble = alphaMin + betaMin;
                } else if (alphaActive && !betaActive) {
                    fastDouble = alphaMin;
                } else { // !alphaActive && betaActive
                    fastDouble = betaMin;
                }

                int fast = (int) Math.round(fastDouble);
                if (fast <= 0) {
                    // Jeśli mimo wszystko fast <= 0, pomijamy tę tranzycję (nie chcemy
                    // nieskończenie szybkich producentów).
                    continue;
                }
                cachedFastTimesSimple.add(fast);
                cachedArcWeightsSimple.add(weight);
            }
        }

        // ------------------------------------------------------------
        // KROK 3: Wyznaczenie H_simple = tauMaxProd + gammaU
        // ------------------------------------------------------------
        long tauMaxProd = computeLcmOfList(cachedFastTimesSimple);
        long steps;
        if (tauMaxProd <= 0L) {
            // Brak producentów – symulacja co najwyżej do gammaU
            // (tylko wykruszanie tokenów początkowych)
            steps = gammaU;
        } else {
            long tmp = tauMaxProd + (long) gammaU;
            steps = (tmp > Long.MAX_VALUE) ? Long.MAX_VALUE : tmp;
        }
        cacheInitializedSimple = true;
        return steps;
    }


    /**
     * Waliduje poprawność "pod-sieci" związanej z miejscem placeXTPN,
     * pod kątem:
     *  - tranzycji z wyłączonym alfa i beta (oba tryby false) – niedozwolone,
     *  - zerowych czasów L (alphaMin == 0 lub betaMin == 0) w trybach aktywnych.
     *
     * Zwraca:
     *  0  -> brak problematycznych tranzycji (nie ma zer w częściach L),
     *  1  -> są zerowe L, użytkownik wybrał "Continue with min. time = 1"
     * -1  -> błąd krytyczny lub użytkownik wybrał "Stop":
     *        * niedozwolone tranzycje (both alphaMode=false i betaMode=false),
     *        * użytkownik nie zgodził się na zamianę zer na 1.
     */
    public static int validateSubnetCorrectness(PlaceXTPN placeXTPN) {
        if (placeXTPN == null) {
            throw new IllegalArgumentException("placeXTPN must not be null");
        }

        // Zbierz wszystkie tranzycje xTPN połączone NORMAL arcs z placeXTPN
        Set<TransitionXTPN> connectedTransitions = new HashSet<>();

        // Łuki wejściowe (Transition -> Place)
        List<Arc> inputArcs = placeXTPN.getInputArcs();
        if (inputArcs != null) {
            for (Arc arc : inputArcs) {
                if (arc == null) continue;
                if (arc.getArcType() != Arc.TypeOfArc.NORMAL) continue;

                Object startNode = arc.getStartLocation().getParentNode();
                if (startNode instanceof TransitionXTPN) {
                    TransitionXTPN t = (TransitionXTPN) startNode;
                    if (!t.isKnockedOut()) { // wyłączone tranzycje ignorujemy całkowicie
                        connectedTransitions.add(t);
                    }
                }
            }
        }

        // Łuki wyjściowe (Place -> Transition)
        List<Arc> outputArcs = placeXTPN.getOutputArcs();
        if (outputArcs != null) {
            for (Arc arc : outputArcs) {
                if (arc == null) continue;
                if (arc.getArcType() != Arc.TypeOfArc.NORMAL) continue;

                Object endNode = arc.getEndLocation().getParentNode();
                if (endNode instanceof TransitionXTPN) {
                    TransitionXTPN t = (TransitionXTPN) endNode;
                    if (!t.isKnockedOut()) {
                        connectedTransitions.add(t);
                    }
                }
            }
        }

        // Listy problematycznych tranzycji
        List<TransitionXTPN> invalidModeTransitions = new ArrayList<>();
        List<TransitionXTPN> zeroTimeTransitions    = new ArrayList<>();

        for (TransitionXTPN t : connectedTransitions) {
            boolean alphaActive = t.isAlphaModeActive();
            boolean betaActive  = t.isBetaModeActive();

            // 1) Niedozwolone: oba tryby nieaktywne (ale tylko jeśli tranzycja nie jest knockedOut)
            if (!alphaActive && !betaActive) {
                invalidModeTransitions.add(t);
                continue; // reszta i tak nas nie obchodzi dla tej tranzycji
            }

            double alphaMin = t.getAlphaMinValue();
            double betaMin  = t.getBetaMinValue();

            boolean hasZeroLow = false;

            // Zera liczymy tylko tam, gdzie tryb jest aktywny
            if (alphaActive && alphaMin == 0.0) {
                hasZeroLow = true;
            }
            if (betaActive && betaMin == 0.0) {
                hasZeroLow = true;
            }

            if (hasZeroLow) {
                zeroTimeTransitions.add(t);
            }
        }

        // Jeśli są niedozwolone tranzycje (oba tryby false) – komunikat błędu i STOP
        if (!invalidModeTransitions.isEmpty()) {
            // Ustal identyfikatory t0, t1, ...
            ArrayList<Transition> allTransitions =
                    overlord.getWorkspace().getProject().getTransitions();

            List<String> ids = new ArrayList<>();
            for (TransitionXTPN t : invalidModeTransitions) {
                int idx = allTransitions.indexOf(t);
                if (idx >= 0) {
                    ids.add("t" + idx);
                } else {
                    ids.add("t?");
                }
            }

            String listStr = String.join(", ", ids);
            String message = "Invalid xTPN transitions (both alpha and beta modes inactive): "
                    + listStr
                    + ".\nSuch transitions are not allowed (unless knocked out).";

            JOptionPane.showMessageDialog(
                    null,
                    message,
                    "Invalid xTPN transitions detected",
                    JOptionPane.ERROR_MESSAGE
            );

            return -1;
        }

        // Jeśli są tranzycje z zerowymi wartościami L (alphaMin lub betaMin)
        if (!zeroTimeTransitions.isEmpty()) {
            ArrayList<Transition> allTransitions =
                    overlord.getWorkspace().getProject().getTransitions();

            List<String> ids = new ArrayList<>();
            for (TransitionXTPN t : zeroTimeTransitions) {
                int idx = allTransitions.indexOf(t);
                if (idx >= 0) {
                    ids.add("t" + idx);
                } else {
                    ids.add("t?");
                }
            }

            String listStr = String.join(", ", ids);
            String message = "Incorrect zero-time L-values in transitions: "
                    + listStr
                    + ".\n"
                    + "One or more alpha^L / beta^L values are equal to 0.\n\n"
                    + "You can:\n"
                    + " - Stop now and fix the model manually,\n"
                    + " - Continue, treating all zero L-values as 1 for this analysis.";

            Object[] options = { "Continue with min. time = 1", "Stop" };
            int n = JOptionPane.showOptionDialog(
                    null,
                    message,
                    "Incorrect zero-time values detected",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    options,
                    options[0]
            );

            if (n == JOptionPane.YES_OPTION) {
                // Użytkownik zgodził się na zamianę 0 -> 1
                return 1;
            } else {
                // Stop lub zamknięcie okna
                return -1;
            }
        }

        // Brak problemów – ani niedozwolonych, ani zerowych L
        return 0;
    }

    public static int computeUpperBoundForPlace(PlaceXTPN placeXTPN, long iterations) {
        if (placeXTPN == null) {
            throw new IllegalArgumentException("placeXTPN must not be null");
        }
        if (!cacheInitializedSimple || cachedPlaceSimple != placeXTPN) {
            return -1;
        }
        if (iterations <= 0L) {
            return -1;
        }

        final int gammaU = cachedGammaUSimple;

        HolmesNotepad notePad = null;
        notePad = new HolmesNotepad(900,600);
        notePad.addTextLineNL("computeUpperBoundForPlace:", "text");

        ArrayList<Double> initialMultiset = placeXTPN.copyMultiset();
        ArrayList<Integer> multisetK = new ArrayList<>();

        if (initialMultiset != null) {
            for (Double ageDouble : initialMultiset) {
                if (ageDouble == null) continue;
                int age = (int) Math.round(ageDouble);
                if (age >= 0 && age <= gammaU) {
                    multisetK.add(age);
                }
            }
        }

        int numberOfProducers = cachedFastTimesSimple.size();
        int[] fastVector   = new int[numberOfProducers];
        int[] timeVector   = new int[numberOfProducers];
        int[] weightsVector = new int[numberOfProducers];

        for (int i = 0; i < numberOfProducers; i++) {
            int f = cachedFastTimesSimple.get(i);
            int w = cachedArcWeightsSimple.get(i);
            fastVector[i]   = f;
            timeVector[i]   = f;
            weightsVector[i] = w;
        }

        int maxTokens = multisetK.size();

        for (long step = 1L; step <= iterations*3; step++) {

            // Produkcja
            for (int i = 0; i < numberOfProducers; i++) {
                if (timeVector[i] > 0) {
                    timeVector[i]--;
                }
                if (timeVector[i] == 0) {
                    int weight = weightsVector[i];
                    for (int k = 0; k < weight; k++) {
                        multisetK.add(0);
                    }
                    timeVector[i] = fastVector[i];
                }
            }

            //notePad.addTextLine(" :" + step + " |K|=" + multisetK.size() + "   ={ ", "text");
            //for (int age : multisetK)
            //    notePad.addTextLine(" " + age, "text");
            //notePad.addTextLineNL(" }", "text");

            if (multisetK.size() > maxTokens) {
                maxTokens = multisetK.size();
            }

            // Upływ czasu
            updateMultiset(multisetK, gammaU);
        }
        //notePad.setVisible(true);
        //notePad.addTextLineNL("", "text");
        return maxTokens;
    }

    public static long maxStepsExtended(PlaceXTPN placeXTPN) {
        if (placeXTPN == null) {
            throw new IllegalArgumentException("placeXTPN must not be null");
        }

        // ================================================================
        // KROK 0: Prosty algorytm (tylko wejścia) – formalne górne ograniczenie
        // ================================================================
        long simpleSteps = maxSteps(placeXTPN);
        if (simpleSteps <= 0L) {
            // gammaU == 0 albo użytkownik przerwał analizę w walidacji
            return 0L;
        }

        int simpleBound = computeUpperBoundForPlace(placeXTPN, simpleSteps);
        if (simpleBound <= 0) {
            simpleBound = 1;
        }

        // maxSteps(...) już wykonał walidację i ewentualnie wymusił politykę 0->1 dla L.
        // Extended przyjmujemy spójnie (nie pokazujemy drugiego okna).
        boolean fixZeroTimes = true;

        // ================================================================
        // KROK 1: Przygotowanie cache dla trybu rozszerzonego (wejścia + wyjścia)
        // ================================================================
        cachedFastInTimesExt       = new ArrayList<>();
        cachedInWeightsExt         = new ArrayList<>();

        cachedOutSlowTimesSafeExt  = new ArrayList<>();
        cachedOutWeightsSafeExt    = new ArrayList<>();
        cachedOutSlowTimesAllExt   = new ArrayList<>();
        cachedOutWeightsAllExt     = new ArrayList<>();

        // NOWE cache: osobno α^U i β^U dla tranzycji wyjściowych
        cachedOutAlphaTimesSafeExt = new ArrayList<>();
        cachedOutBetaTimesSafeExt  = new ArrayList<>();
        cachedOutAlphaTimesAllExt  = new ArrayList<>();
        cachedOutBetaTimesAllExt   = new ArrayList<>();

        cacheInitializedExt        = false;
        cachedPlaceExt             = placeXTPN;

        int gammaU = (int) Math.round(placeXTPN.getGammaMaxValue());
        if (gammaU < 0) gammaU = 0;
        cachedGammaUExt = gammaU;

        int gammaL = (int) Math.round(placeXTPN.getGammaMinValue());
        if (gammaL < 0) gammaL = 0;
        if (gammaL > gammaU) gammaL = gammaU;
        cachedGammaLExt = gammaL;

        // --- Producenci: łuki NORMAL wejściowe do p_x (Transition -> Place) ---
        List<Arc> inputArcs = placeXTPN.getInputArcs();
        if (inputArcs != null) {
            for (Arc arc : inputArcs) {
                if (arc == null) continue;
                if (arc.getArcType() != Arc.TypeOfArc.NORMAL) continue;

                Object startNode = arc.getStartLocation().getParentNode();
                if (!(startNode instanceof TransitionXTPN)) continue;

                TransitionXTPN t = (TransitionXTPN) startNode;

                if (t.isKnockedOut()) continue;

                int weight = arc.getWeight();
                if (weight <= 0) continue;

                boolean alphaActive = t.isAlphaModeActive();
                boolean betaActive  = t.isBetaModeActive();
                if (!alphaActive && !betaActive) continue; // niedozwolone (powinno już być złapane)

                double alphaMin = t.getAlphaMinValue();
                double betaMin  = t.getBetaMinValue();

                // 0 -> 1 dla części L w aktywnych trybach (spójnie z maxSteps)
                if (fixZeroTimes) {
                    if (alphaActive && alphaMin == 0.0) alphaMin = 1.0;
                    if (betaActive  && betaMin  == 0.0) betaMin  = 1.0;
                }

                // fast(t):
                //  - oba tryby aktywne: α^L + β^L
                //  - tylko alfa:        α^L   (TPN)
                //  - tylko beta:        β^L   (DPN)
                double fastDouble;
                if (alphaActive && betaActive) {
                    fastDouble = alphaMin + betaMin;
                } else if (alphaActive) { // && !betaActive
                    fastDouble = alphaMin;
                } else { // !alphaActive && betaActive
                    fastDouble = betaMin;
                }

                int fast = (int) Math.round(fastDouble);
                if (fast <= 0) continue;

                cachedFastInTimesExt.add(fast);
                cachedInWeightsExt.add(weight);
            }
        }

        // --- Konsumenci: łuki NORMAL wyjściowe z p_x (Place -> Transition) ---
        List<Arc> outputArcs = placeXTPN.getOutputArcs();
        if (outputArcs != null) {
            for (Arc arc : outputArcs) {
                if (arc == null) continue;
                if (arc.getArcType() != Arc.TypeOfArc.NORMAL) continue;

                Object endNode = arc.getEndLocation().getParentNode();
                if (!(endNode instanceof TransitionXTPN)) continue;

                TransitionXTPN t = (TransitionXTPN) endNode;
                if (t.isKnockedOut()) continue;

                int weight = arc.getWeight();
                if (weight <= 0) continue;
                boolean alphaActive = t.isAlphaModeActive();
                boolean betaActive  = t.isBetaModeActive();
                if (!alphaActive && !betaActive) continue; // niedozwolone (powinno już być złapane)

                // Dla konsumentów potrzebujemy OSOBNO α^U i β^U:
                int alphaU = 0;
                int betaU  = 0;
                if (alphaActive) {
                    alphaU = (int) Math.round(t.getAlphaMaxValue());
                }
                if (betaActive) {
                    betaU = (int) Math.round(t.getBetaMaxValue());
                }

                // Dla heurystyki zachowujemy też slow = α^U + β^U
                int slow = alphaU + betaU;
                if (slow <= 0) continue; // praktycznie nie powinno się zdarzyć

                boolean localToPlace = isTransitionLocalToPlace(t, placeXTPN);

                // ALL (EXT_UNSAFE)
                cachedOutSlowTimesAllExt.add(slow);
                cachedOutWeightsAllExt.add(weight);
                cachedOutAlphaTimesAllExt.add(alphaU);
                cachedOutBetaTimesAllExt.add(betaU);
                // SAFE (EXT_SAFE)
                if (localToPlace) {
                    cachedOutSlowTimesSafeExt.add(slow);
                    cachedOutWeightsSafeExt.add(weight);
                    cachedOutAlphaTimesSafeExt.add(alphaU);
                    cachedOutBetaTimesSafeExt.add(betaU);
                }
            }
        }

        // KROK 2: Heurystyka horyzontu – estimateExtendedHorizon
        long extendedSteps = estimateExtendedHorizon(simpleSteps, simpleBound);
        cacheInitializedExt = true;
        return extendedSteps;
    }

    /**
     * Heurystyczne oszacowanie horyzontu czasowego dla algorytmu extended.
     *
     * Wykorzystuje:
     *  - wynik prostego algorytmu (simpleSteps, simpleBound),
     *  - cache wejść:  cachedFastInTimesExt, cachedInWeightsExt,
     *  - cache wyjść:  cachedOutSlowTimesAllExt, cachedOutWeightsAllExt,
     *  - cachedGammaUExt (gamma^U).
     *
     * Idea:
     *  1) Obliczamy średnią produkcję:
     *         λ_in = sum_{t in In(p)} V(t,p) / fast(t)
     *  2) Obliczamy maksymalną średnią konsumpcję (gdy wyjścia nie są głodne):
     *         μ_max = sum_{t in Out*(p)} V(p,t) / slow(t)
     *  3) "Dryf" (szacunkowo najgorszy dla extended):
     *         Δ_est = max(λ_in - μ_max, 0)
     *     - jeśli Δ_est ~ 0, system jest zrównoważony albo przepustowość wyjść >= wejść.
     *     - jeśli Δ_est > 0, system powoli się "napełnia".
     *  4) Wyznaczamy makro-okres:
     *         L_all = lcm( fast(t), slow(t) )
     *  5) Gdy Δ_est ≤ epsilon:
     *         H_ext = max( H_simple,  gammaU + EXTRA_PERIODS * L_all )
     *     (czyli kilka pełnych makro-okresów ponad klasyczny horyzont)
     *  6) Gdy Δ_est > epsilon:
     *         Przyrost na makro-okres:
     *             δ_per_macro = Δ_est * L_all
     *         Liczba makro-okresów potrzebna, by dojść do ~B_simple:
     *             k = ceil( B_simple / δ_per_macro )
     *         H_ext = max( H_simple,  gammaU + k * L_all )
     *  7) Nakładamy górny limit:
     *         H_ext = min( H_ext, EXTENDED_MAX_ITERATIONS_CAP )
     */
    private static long estimateExtendedHorizon(long simpleSteps, int simpleBound) {
        final long H_simple = simpleSteps;
        final int  gammaU   = cachedGammaUExt;

        // Jeśli nie ma producentów, to extended nie może podnieść liczby tokenów
        // ponad to, co wyszło z prostego modelu – wystarczy H_simple.
        if (cachedFastInTimesExt == null || cachedFastInTimesExt.isEmpty()) {
            return H_simple;
        }

        // 1. Średnia produkcja λ_in
        double lambdaIn = 0.0;
        for (int i = 0; i < cachedFastInTimesExt.size(); i++) {
            int fast   = cachedFastInTimesExt.get(i);
            int weight = cachedInWeightsExt.get(i);
            if (fast > 0 && weight > 0) {
                lambdaIn += (double) weight / (double) fast;
            }
        }

        // 2. Maksymalna średnia konsumpcja μ_max (używamy pełnego zbioru wyjść – "All")
        double muMax = 0.0;
        if (cachedOutSlowTimesAllExt != null) {
            for (int i = 0; i < cachedOutSlowTimesAllExt.size(); i++) {
                int slow   = cachedOutSlowTimesAllExt.get(i);
                int weight = cachedOutWeightsAllExt.get(i);
                if (slow > 0 && weight > 0) {
                    muMax += (double) weight / (double) slow;
                }
            }
        }
        // 3. Szacunkowy dryf Δ_est
        double deltaEst = lambdaIn - muMax;
        if (deltaEst < 0.0) {
            // Nie dopuszczamy ujemnego dryfu dla extended – w praktyce oznacza
            // to, że wyjścia nigdy nie będą "skuteczniejsze" niż wejścia,
            // bo w realnej sieci często są głodne.
            deltaEst = 0.0;
        }
        // 4. Okresy: P_in i L_all
        long P_in = computeLcmOfList(cachedFastInTimesExt); // okres produkcji wejść
        // Makro-okres – LCM fastów i slowów
        List<Integer> allPeriods = new ArrayList<>();
        allPeriods.addAll(cachedFastInTimesExt);
        if (cachedOutSlowTimesAllExt != null) {
            allPeriods.addAll(cachedOutSlowTimesAllExt);
        }
        long L_all = computeLcmOfList(allPeriods);

        if (L_all <= 0L) {
            // Jeśli z jakiegoś powodu nie udało się wyznaczyć sensownego makro-okresu,
            // zostajemy przy klasycznym H_simple.
            return H_simple;
        }
        long H_ext;

        // 5/6. Dwa przypadki: brak wyraźnego dryfu vs dodatni dryf
        if (deltaEst <= DRIFT_EPSILON) {
            // PRZYPADEK A: Δ_est ≈ 0
            // System jest "zrównoważony" lub wyjścia potencjalnie mają
            // przepustowość >= wejść. Maksimum zwykle pojawia się w fazie
            // przejściowej lub w kilku pierwszych makro-okresach.
            long candidate = gammaU + (long) EXTENDED_EXTRA_PERIODS_NO_DRIFT * L_all;

            // Zabezpieczenie przed przepełnieniem i względem globalnego CAP
            if (candidate < 0L || candidate > EXTENDED_MAX_ITERATIONS_CAP) {
                candidate = EXTENDED_MAX_ITERATIONS_CAP;
            }

            H_ext = Math.max(H_simple, candidate);
        } else {
            // PRZYPADEK B: Δ_est > 0  (dodatni dryf – system powoli się "napełnia")
            // Szacujemy ile netto tokenów przybywa na jeden makro-okres L_all:
            double deltaPerMacro = deltaEst * (double) L_all;

            if (deltaPerMacro <= DRIFT_EPSILON) {
                // Numerycznie coś poszło nie tak – traktujemy jak brak dryfu.
                long candidate = gammaU + (long) EXTENDED_EXTRA_PERIODS_NO_DRIFT * L_all;
                if (candidate < 0L || candidate > EXTENDED_MAX_ITERATIONS_CAP) {
                    candidate = EXTENDED_MAX_ITERATIONS_CAP;
                }
                H_ext = Math.max(H_simple, candidate);
            } else {
                // Liczba makro-okresów potrzebna, by _typowo_ móc dojść
                // z poziomów rzędu 0 do poziomu B_simple:
                //   k ≈ B_simple / (przyrost na jeden makro-okres)
                double kDouble = (double) simpleBound / deltaPerMacro;
                if (kDouble < 1.0) {
                    kDouble = 1.0;
                }
                long k = (long) Math.ceil(kDouble);

                // H_ext ≈ gammaU + k * L_all
                double candidateD = (double) gammaU + (double) k * (double) L_all;

                if (candidateD > (double) EXTENDED_MAX_ITERATIONS_CAP) {
                    H_ext = EXTENDED_MAX_ITERATIONS_CAP;
                } else {
                    long candidate = (long) Math.ceil(candidateD);
                    H_ext = candidate;
                }
                // Nie schodzimy poniżej horyzontu prostego algorytmu
                if (H_ext < H_simple) {
                    H_ext = H_simple;
                }
            }
        }
        return H_ext;
    }

    public static int computeUpperBoundForPlaceExtended(PlaceXTPN placeXTPN, long iterations, boolean unsafePlaces) {
        if (placeXTPN == null) {
            throw new IllegalArgumentException("placeXTPN must not be null");
        }
        if (!cacheInitializedExt || cachedPlaceExt != placeXTPN) {
            return -1;
        }
        if (iterations <= 0L) {
            return -1;
        }

        final int gammaU = cachedGammaUExt;
        final int gammaL = cachedGammaLExt;

        ArrayList<Double> initialMultiset = placeXTPN.copyMultiset();
        ArrayList<Integer> multisetK = new ArrayList<>();

        if (initialMultiset != null) {
            for (Double ageDouble : initialMultiset) {
                if (ageDouble == null) continue;
                int age = (int) Math.round(ageDouble);
                if (age >= 0 && age <= gammaU) {
                    multisetK.add(age);
                }
            }
        }

        // Ważne: hasActivatingSubsetSorted + consumeTokensFromPlace zakładają porządek malejący wieku.
        // Jeśli copyMultiset() nie gwarantuje kolejności, musimy ją narzucić na starcie.
        multisetK.sort(java.util.Collections.reverseOrder());

        // PRODUCENCI (wejściowi)
        int numberOfProducers = cachedFastInTimesExt.size();
        int[] fastProd   = new int[numberOfProducers];
        int[] timeProd   = new int[numberOfProducers];
        int[] weightProd = new int[numberOfProducers];

        for (int i = 0; i < numberOfProducers; i++) {
            int f = cachedFastInTimesExt.get(i);
            int w = cachedInWeightsExt.get(i);
            fastProd[i]   = f;
            timeProd[i]   = f;
            weightProd[i] = w;
        }

        // KONSUMENCI (wyjściowi) — TERAZ: α/β osobno
        List<Integer> alphaList;
        List<Integer> betaList;
        List<Integer> weightList;

        if (unsafePlaces) {
            alphaList  = cachedOutAlphaTimesAllExt;
            betaList   = cachedOutBetaTimesAllExt;
            weightList = cachedOutWeightsAllExt;
        } else {
            alphaList  = cachedOutAlphaTimesSafeExt;
            betaList   = cachedOutBetaTimesSafeExt;
            weightList = cachedOutWeightsSafeExt;
        }

        int numberOfConsumers = weightList.size();

        int[] alphaCons  = new int[numberOfConsumers];
        int[] betaCons   = new int[numberOfConsumers];
        int[] weightCons = new int[numberOfConsumers];

        // Stany: 0=WAITING, 1=ACTIVATING, 2=PRODUCING
        byte[] state = new byte[numberOfConsumers];
        int[] actRem  = new int[numberOfConsumers];
        int[] prodRem = new int[numberOfConsumers];

        for (int i = 0; i < numberOfConsumers; i++) {
            int a = alphaList.get(i);
            int b = betaList.get(i);
            int w = weightList.get(i);

            if (a < 0) a = 0;
            if (b < 0) b = 0;

            alphaCons[i]  = a;
            betaCons[i]   = b;
            weightCons[i] = w;

            state[i] = 0;           // WAITING
            actRem[i] = a;          // przygotowane α
            prodRem[i] = 0;
        }

        int maxTokens = multisetK.size();
        HolmesNotepad notePad = null;
        notePad = new HolmesNotepad(900,600);
        // ============================
        // PĘTLA SYMULACJI
        // ============================
        for (long step = 1L; step <= iterations; step++) {

            // 1) Produkcja (producenci wejściowi) – bez sprawdzania aktywacji
            for (int i = 0; i < numberOfProducers; i++) {
                if (timeProd[i] > 0) {
                    timeProd[i]--;
                }
                if (timeProd[i] == 0) {
                    int weight = weightProd[i];
                    for (int k = 0; k < weight; k++) {
                        // Nowe tokeny mają wiek 0, a multisetK trzymamy malejąco,
                        // więc dodajemy na koniec.
                        multisetK.add(0);
                    }
                    timeProd[i] = fastProd[i];
                }
            }

            // 2) Konsumpcja (konsumenci wyjściowi) – PRAWIDŁOWA semantyka xTPN (α potem β)
            for (int i = 0; i < numberOfConsumers; i++) {
                int need = weightCons[i];
                if (need <= 0) continue;

                // Aktywność (podzbiór aktywujący) liczy się TYLKO w fazie α (ACTIVATING).
                // W fazie β (PRODUCING) nie sprawdzamy aktywności.
                boolean hasSubset = hasActivatingSubsetSorted(multisetK, gammaL, need);

                if (state[i] == 0) {
                    // WAITING: czeka na możliwość startu aktywacji
                    if (hasSubset) {
                        int a = alphaCons[i];

                        if (a <= 0) {
                            // α=0: natychmiastowe pobranie tokenów i wejście w β
                            consumeTokensFromPlace(multisetK, gammaL, need);

                            int b = betaCons[i];
                            if (b > 0) {
                                state[i] = 2;      // PRODUCING
                                prodRem[i] = b;
                            } else {
                                // β=0: kończy się od razu, wraca do WAITING
                                state[i] = 0;
                                actRem[i] = alphaCons[i];
                                prodRem[i] = 0;
                            }
                        } else {
                            // Start fazy aktywacji α
                            state[i] = 1;     // ACTIVATING
                            actRem[i] = a;
                            // W tym samym kroku, skoro tranzycja jest aktywna, "tyka" jedna jednostka α.
                            actRem[i]--;
                            if (actRem[i] <= 0) {
                                // Koniec α: pobranie tokenów i wejście w β
                                consumeTokensFromPlace(multisetK, gammaL, need);

                                int b = betaCons[i];
                                if (b > 0) {
                                    state[i] = 2;
                                    prodRem[i] = b;
                                } else {
                                    state[i] = 0;
                                    actRem[i] = alphaCons[i];
                                    prodRem[i] = 0;
                                }
                            }
                        }
                    }

                } else if (state[i] == 1) {
                    // ACTIVATING: odlicza α, ale tylko jeśli pozostaje aktywna
                    if (!hasSubset) {
                        // Utrata aktywności → reset i powrót do WAITING
                        state[i] = 0;
                        actRem[i] = alphaCons[i];
                    } else {
                        // Nadal aktywna → α tyka
                        actRem[i]--;
                        if (actRem[i] <= 0) {
                            // Koniec α: pobranie tokenów i wejście w β
                            consumeTokensFromPlace(multisetK, gammaL, need);

                            int b = betaCons[i];
                            if (b > 0) {
                                state[i] = 2;
                                prodRem[i] = b;
                            } else {
                                state[i] = 0;
                                actRem[i] = alphaCons[i];
                                prodRem[i] = 0;
                            }
                        }
                    }

                } else {
                    // PRODUCING: odliczamy β bez sprawdzania aktywności
                    if (prodRem[i] > 0) {
                        prodRem[i]--;
                    }
                    if (prodRem[i] <= 0) {
                        // Koniec β: wraca do WAITING
                        state[i] = 0;
                        actRem[i] = alphaCons[i];
                        prodRem[i] = 0;
                    }
                }
            }

            // 3) Aktualizacja maksimum (jak w Twojej wersji: przed "upływem czasu")
            if (multisetK.size() > maxTokens) {
                maxTokens = multisetK.size();
            }

            //notePad.addTextLine(" :" + step + " |K|=" + multisetK.size() + "   ={ ", "text");
           // for (int age : multisetK)
            //    notePad.addTextLine(" " + age, "text");
            //notePad.addTextLineNL(" }", "text");

            // 4) Upływ czasu (starzenie i śmierć po gammaU)
            updateMultiset(multisetK, gammaU);

            //notePad.addTextLine("*:" + step + " |K|=" + multisetK.size() + "   ={ ", "text");
            //for (int age : multisetK)
            //    notePad.addTextLine(" " + age, "text");
            //notePad.addTextLineNL(" }", "text");
            // updateMultiset nie psuje porządku malejącego: wszyscy starzeją się o 1,
            // a usuwamy tylko przekroczone gammaU.
        }

        //notePad.setVisible(true);
        //notePad.addTextLineNL("", "text");

        return maxTokens;
    }


    // =======================================================================
    //  FUNKCJE POMOCNICZE
    // =======================================================================

    private static boolean isTransitionLocalToPlace(TransitionXTPN t, PlaceXTPN placeXTPN) {
        List<Arc> inputArcs = t.getInputArcs();
        if (inputArcs == null) {
            return true;
        }
        for (Arc arcIn : inputArcs) {
            if (arcIn == null) continue;
            Object srcNode = arcIn.getStartLocation().getParentNode();
            if (srcNode instanceof PlaceXTPN) {
                if (srcNode != placeXTPN) {
                    return false;
                }
            } else if (srcNode instanceof holmes.petrinet.elements.Place) {
                if (srcNode != placeXTPN) {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean hasActivatingSubsetSorted(List<Integer> multiset, int gammaL, int need) {
        if (need <= 0) {
            return false;
        }
        if (multiset.size() < need) {
            return false;
        }
        // lista posortowana malejąco
        return multiset.get(need - 1) >= gammaL;
    }


    private static void updateMultiset(List<Integer> multiset, int gammaU) {
        int i = 0;
        while (i < multiset.size()) {
            int age = multiset.get(i) + 1;
            if (age > gammaU) {
                multiset.remove(i);
            } else {
                multiset.set(i, age);
                i++;
            }
        }
    }

    private static int countEligibleTokens(List<Integer> multiset, int gammaL) {
        int count = 0;
        for (int age : multiset) {
            if (age >= gammaL) {
                count++;
            }
        }
        return count;
    }

    /**
     * Usuwa z multizbioru dokładnie 'count' najstarszych tokenów o wieku >= gammaL.
     * Zakładamy, że hasActivatingSubsetSorted(multiset, gammaL, count) == true
     * w momencie wywołania (czyli pierwsze 'count' tokenów są dorosłe).
     */
    private static void consumeTokensFromPlace(List<Integer> multiset, int gammaL, int count) {
        for (int i = 0; i < count; i++) {
            if (multiset.isEmpty()) {
                // Teoretycznie nie powinno się zdarzyć, jeśli wcześniej sprawdziliśmy podzbiór aktywujący.
                break;
            }
            // Dla bezpieczeństwa możemy asertywnie sprawdzić warunek gammaL:
            int age = multiset.get(0);
            if (age < gammaL) {
                // Znów: w poprawnej logice nie powinno się zdarzyć.
                break;
            }
            multiset.remove(0);
        }
    }

    private static long computeLcmOfList(List<Integer> values) {
        if (values == null || values.isEmpty()) {
            return 0L;
        }
        long lcm = 0L;
        for (int v : values) {
            if (v <= 0) continue;
            if (lcm == 0L) {
                lcm = v;
            } else {
                lcm = lcm(lcm, (long) v);
                if (lcm >= Long.MAX_VALUE) {
                    return Long.MAX_VALUE;
                }
            }
        }
        return lcm;
    }

    private static long gcd(long a, long b) {
        a = Math.abs(a);
        b = Math.abs(b);
        if (a == 0L) return b;
        if (b == 0L) return a;

        while (b != 0L) {
            long tmp = a % b;
            a = b;
            b = tmp;
        }
        return a;
    }

    private static long lcm(long a, long b) {
        if (a <= 0L || b <= 0L) return 0L;
        long gcd = gcd(a, b);
        long result = (a / gcd) * b;
        if (result < 0L || result > Long.MAX_VALUE) {
            return Long.MAX_VALUE;
        }
        return result;
    }


    //******************************************* OLD:
    // =======================================================================
    //  CZĘŚĆ 2: ROZSZERZONY ALGORYTM – WEJŚCIA + WYJŚCIA  OLD
    // =======================================================================
    public static long maxStepsExtendedOldest(PlaceXTPN placeXTPN) {
        if (placeXTPN == null) {
            throw new IllegalArgumentException("placeXTPN must not be null");
        }

        cachedFastInTimesExt       = new ArrayList<>();
        cachedInWeightsExt         = new ArrayList<>();
        cachedOutSlowTimesSafeExt  = new ArrayList<>();
        cachedOutWeightsSafeExt    = new ArrayList<>();
        cachedOutSlowTimesAllExt   = new ArrayList<>();
        cachedOutWeightsAllExt     = new ArrayList<>();
        cacheInitializedExt        = false;
        cachedPlaceExt             = placeXTPN;

        int gammaU = (int) Math.round(placeXTPN.getGammaMaxValue());
        if (gammaU < 0) gammaU = 0;
        cachedGammaUExt = gammaU;

        int gammaL = (int) Math.round(placeXTPN.getGammaMinValue());
        if (gammaL < 0) gammaL = 0;
        if (gammaL > gammaU) gammaL = gammaU;
        cachedGammaLExt = gammaL;

        // --- Producenci: łuki NORMAL wejściowe do p_x (Transition -> Place) ---

        List<Arc> inputArcs = placeXTPN.getInputArcs();
        if (inputArcs != null) {
            for (Arc arc : inputArcs) {
                if (arc == null) continue;

                if (arc.getArcType() != Arc.TypeOfArc.NORMAL)
                    continue; // ignorujemy READARC, INHIBITOR itd.

                Object startNode = arc.getStartLocation().getParentNode();
                if (!(startNode instanceof TransitionXTPN))
                    continue;

                TransitionXTPN t = (TransitionXTPN) startNode;

                int weight = arc.getWeight();
                if (weight <= 0) continue;

                double alphaMin = t.getAlphaMinValue();
                double betaMin  = t.getBetaMinValue();
                int fast = (int) Math.round(alphaMin + betaMin);
                if (fast <= 0) continue;

                cachedFastInTimesExt.add(fast);
                cachedInWeightsExt.add(weight);
            }
        }

        // --- Konsumenci: łuki NORMAL wyjściowe z p_x (Place -> Transition) ---

        List<Arc> outputArcs = placeXTPN.getOutputArcs();
        if (outputArcs != null) {
            for (Arc arc : outputArcs) {
                if (arc == null) continue;

                if (arc.getArcType() != Arc.TypeOfArc.NORMAL)
                    continue; // ignorujemy READARC itd.

                Object endNode = arc.getEndLocation().getParentNode();
                if (!(endNode instanceof TransitionXTPN))
                    continue;

                TransitionXTPN t = (TransitionXTPN) endNode;

                int weight = arc.getWeight();
                if (weight <= 0) continue;

                double alphaMax = t.getAlphaMaxValue();
                double betaMax  = t.getBetaMaxValue();
                int slow = (int) Math.round(alphaMax + betaMax);
                if (slow <= 0) continue;

                boolean localToPlace = isTransitionLocalToPlace(t, placeXTPN);

                // wszyscy konsumenci
                cachedOutSlowTimesAllExt.add(slow);
                cachedOutWeightsAllExt.add(weight);

                // tylko lokalni konsumenci (EXT_SAFE)
                if (localToPlace) {
                    cachedOutSlowTimesSafeExt.add(slow);
                    cachedOutWeightsSafeExt.add(weight);
                }
            }
        }

        long tauMaxProd = computeLcmOfList(cachedFastInTimesExt);
        long steps;
        if (tauMaxProd <= 0L) {
            steps = gammaU;
        } else {
            long tmp = tauMaxProd + (long) gammaU;
            steps = (tmp > Long.MAX_VALUE) ? Long.MAX_VALUE : tmp;
        }

        cacheInitializedExt = true;

        if(steps < 15000)
            steps = 15000; // minimum 3 sekundy na notatniku
        return steps;
    }



    public static long maxStepsExtendedOld(PlaceXTPN placeXTPN) {
        if (placeXTPN == null) {
            throw new IllegalArgumentException("placeXTPN must not be null");
        }
        // ================================================================
        // KROK 0: Prosty algorytm (tylko wejścia) – formalne górne ograniczenie
        // ================================================================
        //
        // maxSteps(place) wywoła validateSubnetCorrectness(placeXTPN),
        // pokaże ewentualne komunikaty, a jeśli użytkownik wybierze "Stop"
        // lub są niedozwolone tranzycje, zwróci 0.
        //
        long simpleSteps = maxSteps(placeXTPN);
        if (simpleSteps <= 0L) {
            // Brak sensownego horyzontu (gammaU == 0, brak życia tokenów
            // lub użytkownik przerwał analizę w walidacji).
            return 0L;
        }

        int simpleBound = computeUpperBoundForPlace(placeXTPN, simpleSteps);
        if (simpleBound <= 0) {
            // Bez sensownego B_simple heurystyka dryfu byłaby dziwna.
            // Dla bezpieczeństwa przyjmujemy 1 (minimalny poziom).
            simpleBound = 1;
        }

        // W tym miejscu wiemy, że:
        //  - jeśli były zerowe wartości L (alpha^L / beta^L),
        //    użytkownik już zobaczył okno dialogowe i zgodził się na
        //    "min. time = 1" w maxSteps(...),
        //  - jeśli ich nie było, to i tak traktowanie 0 -> 1 nie zmieni nic,
        //    bo nie ma zer.
        //
        // Dlatego extended może bezpiecznie stosować tę samą zasadę 0 -> 1
        // dla części L, bez kolejnych okien dialogowych.
        boolean fixZeroTimes = true;

        // ================================================================
        // KROK 1: Przygotowanie cache dla trybu rozszerzonego (wejścia + wyjścia)
        // ================================================================

        cachedFastInTimesExt       = new ArrayList<>();
        cachedInWeightsExt         = new ArrayList<>();
        cachedOutSlowTimesSafeExt  = new ArrayList<>();
        cachedOutWeightsSafeExt    = new ArrayList<>();
        cachedOutSlowTimesAllExt   = new ArrayList<>();
        cachedOutWeightsAllExt     = new ArrayList<>();
        cacheInitializedExt        = false;
        cachedPlaceExt             = placeXTPN;

        int gammaU = (int) Math.round(placeXTPN.getGammaMaxValue());
        if (gammaU < 0) gammaU = 0;
        cachedGammaUExt = gammaU;

        int gammaL = (int) Math.round(placeXTPN.getGammaMinValue());
        if (gammaL < 0) gammaL = 0;
        if (gammaL > gammaU) gammaL = gammaU;
        cachedGammaLExt = gammaL;

        // --- Producenci: łuki NORMAL wejściowe do p_x (Transition -> Place) ---

        List<Arc> inputArcs = placeXTPN.getInputArcs();
        if (inputArcs != null) {
            for (Arc arc : inputArcs) {
                if (arc == null) continue;

                if (arc.getArcType() != Arc.TypeOfArc.NORMAL) {
                    // READARC, INHIBITOR itd. – ignorujemy w kontekście transportu tokenów
                    continue;
                }

                Object startNode = arc.getStartLocation().getParentNode();
                if (!(startNode instanceof TransitionXTPN)) {
                    continue;
                }

                TransitionXTPN t = (TransitionXTPN) startNode;

                // Tranzycje permanentnie wyłączone pomijamy całkowicie
                if (t.isKnockedOut()) {
                    continue;
                }

                int weight = arc.getWeight();
                if (weight <= 0) continue;

                boolean alphaActive = t.isAlphaModeActive();
                boolean betaActive  = t.isBetaModeActive();

                // Jeśli oba tryby są nieaktywne – ta tranzycja jest logicznie błędna.
                // validateSubnetCorrectness powinna już to złapać, ale dla bezpieczeństwa pomijamy.
                if (!alphaActive && !betaActive) {
                    continue;
                }

                double alphaMin = t.getAlphaMinValue();
                double betaMin  = t.getBetaMinValue();

                // Ewentualna zamiana 0 -> 1 dla części L w aktywnych trybach,
                // zgodnie z decyzją użytkownika podjętą przy maxSteps(...).
                if (fixZeroTimes) {
                    if (alphaActive && alphaMin == 0.0) {
                        alphaMin = 1.0;
                    }
                    if (betaActive && betaMin == 0.0) {
                        betaMin = 1.0;
                    }
                }

                // Wyznacz fast(t) w zależności od trybów:
                //  - oba aktywne    -> α^L + β^L (klasyczne xTPN),
                //  - tylko alfa     -> α^L      (tryb TPN),
                //  - tylko beta     -> β^L      (tryb DPN).
                double fastDouble;
                if (alphaActive && betaActive) {
                    fastDouble = alphaMin + betaMin;
                } else if (alphaActive && !betaActive) {
                    fastDouble = alphaMin;
                } else { // !alphaActive && betaActive
                    fastDouble = betaMin;
                }

                int fast = (int) Math.round(fastDouble);
                if (fast <= 0) {
                    // Dla bezpieczeństwa pomijamy, nie chcemy producentów z fast <= 0.
                    continue;
                }

                cachedFastInTimesExt.add(fast);
                cachedInWeightsExt.add(weight);
            }
        }

        // --- Konsumenci: łuki NORMAL wyjściowe z p_x (Place -> Transition) ---

        List<Arc> outputArcs = placeXTPN.getOutputArcs();
        if (outputArcs != null) {
            for (Arc arc : outputArcs) {
                if (arc == null) continue;

                if (arc.getArcType() != Arc.TypeOfArc.NORMAL) {
                    // READARC (czyste łuki odczytu) i inne typy pomijamy
                    continue;
                }

                Object endNode = arc.getEndLocation().getParentNode();
                if (!(endNode instanceof TransitionXTPN)) {
                    continue;
                }

                TransitionXTPN t = (TransitionXTPN) endNode;

                // Wyłączone tranzycje ignorujemy
                if (t.isKnockedOut()) {
                    continue;
                }

                int weight = arc.getWeight();
                if (weight <= 0) continue;

                boolean alphaActive = t.isAlphaModeActive();
                boolean betaActive  = t.isBetaModeActive();

                if (!alphaActive && !betaActive) {
                    // Niedozwolona konfiguracja xTPN (powinna już być zasygnalizowana),
                    // ale nie bierzemy takiej tranzycji jako konsumenta.
                    continue;
                }

                double alphaMax = t.getAlphaMaxValue();
                double betaMax  = t.getBetaMaxValue();

                // Wyznacz slow(t) w zależności od trybów:
                //  - oba aktywne    -> α^U + β^U,
                //  - tylko alfa     -> α^U,
                //  - tylko beta     -> β^U.
                double slowDouble;
                if (alphaActive && betaActive) {
                    slowDouble = alphaMax + betaMax;
                } else if (alphaActive && !betaActive) {
                    slowDouble = alphaMax;
                } else { // !alphaActive && betaActive
                    slowDouble = betaMax;
                }

                int slow = (int) Math.round(slowDouble);
                if (slow <= 0) {
                    // Dla bezpieczeństwa pomijamy konsumentów z slow <= 0.
                    continue;
                }

                boolean localToPlace = isTransitionLocalToPlace(t, placeXTPN);

                // Wszyscy konsumenci (EXT_UNSAFE)
                cachedOutSlowTimesAllExt.add(slow);
                cachedOutWeightsAllExt.add(weight);

                // Konsumenci lokalni (EXT_SAFE)
                if (localToPlace) {
                    cachedOutSlowTimesSafeExt.add(slow);
                    cachedOutWeightsSafeExt.add(weight);
                }
            }
        }

        // ================================================================
        // KROK 2: Heurystyka horyzontu – estimateExtendedHorizon
        // ================================================================
        //
        // W tym miejscu mamy już:
        //  - cachedFastInTimesExt, cachedInWeightsExt    (wejścia),
        //  - cachedOutSlowTimesAllExt, cachedOutWeightsAllExt (wyjścia – pełny zbiór),
        //  - cachedGammaUExt (gamma^U),
        //  - simpleSteps       = H_simple,
        //  - simpleBound       = B_simple.
        //
        long extendedSteps = estimateExtendedHorizon(simpleSteps, simpleBound);

        cacheInitializedExt = true;
        return extendedSteps;
    }

    public static int computeUpperBoundForPlaceExtendedOld(PlaceXTPN placeXTPN, long iterations, boolean unsafePlaces) {
        if (placeXTPN == null) {
            throw new IllegalArgumentException("placeXTPN must not be null");
        }
        if (!cacheInitializedExt || cachedPlaceExt != placeXTPN) {
            return -1;
        }
        if (iterations <= 0L) {
            return -1;
        }
        final int gammaU = cachedGammaUExt;
        final int gammaL = cachedGammaLExt;
        HolmesNotepad notePad = null;
        notePad = new HolmesNotepad(900,600);

        ArrayList<Double> initialMultiset = placeXTPN.copyMultiset();
        ArrayList<Integer> multisetK = new ArrayList<>();
        if (initialMultiset != null) {
            for (Double ageDouble : initialMultiset) {
                if (ageDouble == null) continue;
                int age = (int) Math.round(ageDouble);
                if (age >= 0 && age <= gammaU) {
                    multisetK.add(age);
                }
            }
        }
        // Tranzycje produkujące (wejściowe)
        int numberOfProducers = cachedFastInTimesExt.size();
        int[] fastProd   = new int[numberOfProducers];
        int[] timeProd   = new int[numberOfProducers];
        int[] weightProd = new int[numberOfProducers];

        for (int i = 0; i < numberOfProducers; i++) {
            int f = cachedFastInTimesExt.get(i);
            int w = cachedInWeightsExt.get(i);
            fastProd[i]   = f;
            timeProd[i]   = f;
            weightProd[i] = w;
        }

        // Tranzycje konsumujące (wyjściowe)
        List<Integer> slowList;
        List<Integer> weightList;
        if (unsafePlaces) {
            slowList   = cachedOutSlowTimesAllExt;
            weightList = cachedOutWeightsAllExt;
        } else {
            slowList   = cachedOutSlowTimesSafeExt;
            weightList = cachedOutWeightsSafeExt;
        }

        int numberOfConsumers = slowList.size();
        int[] slowCons   = new int[numberOfConsumers];
        int[] timeCons   = new int[numberOfConsumers];
        int[] weightCons = new int[numberOfConsumers];

        for (int i = 0; i < numberOfConsumers; i++) {
            int s = slowList.get(i);
            int w = weightList.get(i);
            slowCons[i]   = s;
            timeCons[i]   = s; // start od slow(t)
            weightCons[i] = w;
        }

        int maxTokens = multisetK.size();

        for (long step = 1L; step <= iterations; step++) {

            // 1. Produkcja
            for (int i = 0; i < numberOfProducers; i++) {
                if (timeProd[i] > 0) {
                    timeProd[i]--;
                }
                if (timeProd[i] == 0) {
                    int weight = weightProd[i];
                    for (int k = 0; k < weight; k++) {
                        multisetK.add(0);
                    }
                    timeProd[i] = fastProd[i];
                }
            }

            // 3. Konsumpcja – z uwzględnieniem aktywującego podzbioru
            for (int i = 0; i < numberOfConsumers; i++) {
                int need = weightCons[i];
                if (need <= 0) continue;

                // Czy istnieje aktywujący podzbiór w aktualnym stanie?
                boolean hasSubset = hasActivatingSubsetSorted(multisetK, gammaL, need);

                if (hasSubset) {
                    // Tranzycja "ma z czego" się uruchamiać – zegar biegnie
                    if (timeCons[i] > 0) {
                        timeCons[i]--;
                    }
                    if (timeCons[i] == 0) {
                        // Uruchomienie: zabierz 'need' najstarszych tokenów (pierwsze elementy listy)
                        consumeTokensFromPlace(multisetK, gammaL, need);
                        timeCons[i] = slowCons[i];
                    }
                } else {
                    // Brak podzbioru aktywującego:
                    // jeśli zegar już tykał (czyli timeCons < slowCons), resetujemy
                    if (timeCons[i] < slowCons[i]) {
                        timeCons[i] = slowCons[i];
                    }
                    // jeśli timeCons == slowCons, nic nie robimy – tranzycja "czeka"
                }
            }

            if (multisetK.size() > maxTokens) {
                maxTokens = multisetK.size();
            }

            // 2. Upływ czasu
            updateMultiset(multisetK, gammaU);

        }
        return maxTokens;
    }
}