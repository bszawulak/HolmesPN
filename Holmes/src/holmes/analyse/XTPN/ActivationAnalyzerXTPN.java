package holmes.analyse.XTPN;

import holmes.darkgui.GUIManager;
import holmes.petrinet.elements.Arc;
import holmes.petrinet.elements.PlaceXTPN;
import holmes.petrinet.elements.Transition;
import holmes.petrinet.elements.TransitionXTPN;

import javax.swing.JOptionPane;
import java.util.*;

/**
 * Analizator możliwości wykorzystania czasu aktywacji (alfa) przez wskazaną tranzycję xTPN.
 *
 * Wynik: maksymalna liczba kolejnych kroków, w których tranzycja transXTPN była aktywna
 * (tzn. istniały podzbiory aktywujące w każdym prePlace transXTPN).
 *
 * Jeżeli wynik >= alpha^L -> tranzycja "dochodzi" do aktywacji.
 * Jeżeli wynik == alpha^U -> tranzycja może w pełni wykorzystać okno aktywacji (zwracamy natychmiast).
 */
public final class ActivationAnalyzerXTPN {
    private static final GUIManager overlord = GUIManager.getDefaultGUIManager();

    // ====== Ustawienia / status cache ======
    private static TransitionXTPN cachedTrans = null;
    private static boolean cacheInitialized = false;

    private static boolean cachedIncludeCompetitors = true;
    private static int cachedParam = 1; // 1: best, 2: worst
    private static boolean cachedFixZeroTimesL = false;

    private static int cachedAlphaL = 0;
    private static int cachedAlphaU = 0;

    // ====== Lokalny wycinek sieci ======
    private static final List<PrePlaceInfo> cachedPrePlaces = new ArrayList<>();
    private static final List<ProducerEntry> cachedProducers = new ArrayList<>();
    private static final List<CompetitorEntry> cachedCompetitors = new ArrayList<>();

    // ====== Konstruktor prywatny ======
    private ActivationAnalyzerXTPN() {
        // narzędziowa
    }

    // ======================================================================
    //  API PUBLICZNE
    // ======================================================================

    /**
     * Buduje lokalny wycinek sieci wokół transXTPN oraz zwraca horyzont symulacji.
     *
     * @param transXTPN             badana tranzycja (musi mieć alfa aktywne i nie może być knocked out)
     * @param param                1: producers fast, competitors wolno (best dla transXTPN)
     *                             2: producers slow, competitors szybko (worst dla transXTPN)
     * @param includeCompetitors   czy uwzględniać competitors (jeśli false -> lista pusta)
     * @param simplifiedHorizon    jeśli true -> LCM(okresów) + max(gammaU prePlaces)
     *                             jeśli false -> wersja rozszerzona (NOT IMPLEMENTED, fallback + komunikat)
     * @return liczba kroków (>=0); 0 oznacza błąd/wczesne przerwanie
     */
    public static long initialize(TransitionXTPN transXTPN,
                                  int param,
                                  boolean includeCompetitors,
                                  boolean simplifiedHorizon) {
        if (transXTPN == null) {
            throw new IllegalArgumentException("transXTPN must not be null");
        }
        if (param != 1 && param != 2) {
            throw new IllegalArgumentException("param must be 1 or 2");
        }

        // Wymaganie ze specyfikacji:
        // transXTPN musi mieć alfa aktywne i nie może być knocked out.
        if (transXTPN.isKnockedOut()) {
            JOptionPane.showMessageDialog(
                    null,
                    "Selected transition is knocked out. Activation analysis makes no sense.",
                    "Activation analysis error",
                    JOptionPane.ERROR_MESSAGE
            );
            resetCache();
            return 0L;
        }
        if (!transXTPN.isAlphaModeActive()) {
            JOptionPane.showMessageDialog(
                    null,
                    "Selected transition has alpha mode inactive.\n"
                            + "Activation analysis requires alpha functionality to be enabled.",
                    "Activation analysis error",
                    JOptionPane.ERROR_MESSAGE
            );
            resetCache();
            return 0L;
        }

        // Reset cache i ustawienia
        resetCache();
        cachedTrans = transXTPN;
        cachedIncludeCompetitors = includeCompetitors;
        cachedParam = param;

        // 1) Odtworzenie prePlaces + wag łuków prePlace -> transXTPN
        LinkedHashMap<PlaceXTPN, Integer> prePlaceNeedMap = collectPrePlaces(transXTPN);
        if (prePlaceNeedMap.isEmpty()) {
            JOptionPane.showMessageDialog(
                    null,
                    "Selected transition has no NORMAL input places (prePlaces).\n"
                            + "Nothing to analyze.",
                    "Activation analysis error",
                    JOptionPane.ERROR_MESSAGE
            );
            resetCache();
            return 0L;
        }

        // Zapis do cache: prePlaces + gammaL/gammaU + multizbiór początkowy
        int maxGammaU = 0;
        for (Map.Entry<PlaceXTPN, Integer> e : prePlaceNeedMap.entrySet()) {
            PlaceXTPN p = e.getKey();
            int needForTrans = e.getValue();

            int gammaU = (int) Math.round(p.getGammaMaxValue());
            if (gammaU < 0) gammaU = 0;

            int gammaL = (int) Math.round(p.getGammaMinValue());
            if (gammaL < 0) gammaL = 0;
            if (gammaL > gammaU) gammaL = gammaU;

            maxGammaU = Math.max(maxGammaU, gammaU);

            ArrayList<Integer> initialMultiset = extractAndSortInitialMultiset(p, gammaU);
            cachedPrePlaces.add(new PrePlaceInfo(p, gammaL, gammaU, needForTrans, initialMultiset));
        }

        // 2) Odtworzenie producers (tranzycje wchodzące do prePlaces)
        //    oraz competitors (tranzycje wychodzące z prePlaces, poza transXTPN)
        collectProducers();
        if (includeCompetitors) {
            collectCompetitors(transXTPN);
        }

        // 3) Walidacja lokalna (tryby alfa/beta) + ewentualne 0->1 dla części L
        int validation = validateLocalSubnetAndAskForZeroFix(transXTPN, param);
        if (validation < 0) {
            // Stop/błąd
            resetCache();
            return 0L;
        }
        cachedFixZeroTimesL = (validation > 0);

        // 4) Ustalenie alpha^L/alpha^U badanej tranzycji (L może być naprawione 0->1)
        cachedAlphaL = normalizeAlphaL(transXTPN, cachedFixZeroTimesL);
        cachedAlphaU = normalizeAlphaU(transXTPN);

        if (cachedAlphaU < cachedAlphaL) {
            // Dla bezpieczeństwa – model błędny lub niespójny
            cachedAlphaU = cachedAlphaL;
        }

        // 5) Wyznaczenie okresów producentów i competitors (zależnie od param)
        finalizeProducerPeriods(param, cachedFixZeroTimesL);
        finalizeCompetitorTimes(param, cachedFixZeroTimesL);

        // 6) Horyzont
        long steps;
        if (simplifiedHorizon) {
            steps = computeSimplifiedHorizon(maxGammaU);
        } else {
            JOptionPane.showMessageDialog(
                    null,
                    "Extended horizon estimation is not implemented yet.\n"
                            + "Falling back to simplified horizon.",
                    "Activation analysis: horizon",
                    JOptionPane.INFORMATION_MESSAGE
            );
            steps = computeSimplifiedHorizon(maxGammaU);
        }

        cacheInitialized = true;
        return steps;
    }

    /**
     * Główna analiza: symuluje lokalny wycinek sieci przez maxSteps kroków
     * i zwraca maksymalny czas ciągłej aktywacji badanej tranzycji.
     *
     * @param transXTPN badana tranzycja (musi zgadzać się z initialize)
     * @param maxSteps  maksymalna liczba kroków
     * @return result (>=0), -1 gdy cache niezgodne / błędne parametry
     */
    public static int analyzeActivationChances(TransitionXTPN transXTPN, long maxSteps) {
        if (transXTPN == null) {
            throw new IllegalArgumentException("transXTPN must not be null");
        }
        if (!cacheInitialized || cachedTrans != transXTPN) {
            return -1;
        }
        if (maxSteps <= 0L) {
            return -1;
        }

        final int alphaL = cachedAlphaL;
        final int alphaU = cachedAlphaU;

        // Kopie multizbiorów dla prePlaces (symulacja nie modyfikuje cache początkowego)
        ArrayList<Integer>[] multisets = new ArrayList[cachedPrePlaces.size()];
        for (int i = 0; i < cachedPrePlaces.size(); i++) {
            multisets[i] = new ArrayList<>(cachedPrePlaces.get(i).initialMultisetSortedDesc);
        }

        // Timery producentów
        int prodCount = cachedProducers.size();
        int[] prodTimer = new int[prodCount];
        int[] prodPeriod = new int[prodCount];
        for (int i = 0; i < prodCount; i++) {
            ProducerEntry pe = cachedProducers.get(i);
            prodPeriod[i] = Math.max(pe.period, 0);
            prodTimer[i] = Math.max(pe.period, 0);
        }

        // Stan competitors
        int compCount = cachedCompetitors.size();
        int[] compActRemaining = new int[compCount];
        int[] compProdRemaining = new int[compCount];
        int[] compActDuration = new int[compCount];
        int[] compProdDuration = new int[compCount];

        for (int i = 0; i < compCount; i++) {
            CompetitorEntry ce = cachedCompetitors.get(i);
            compActDuration[i] = Math.max(ce.activationTime, 0);
            compProdDuration[i] = Math.max(ce.productionTime, 0);
            compActRemaining[i] = compActDuration[i];
            compProdRemaining[i] = 0; // start: gotowy do aktywacji
        }

        int current = 0;
        int result = 0;

        for (long step = 1L; step <= maxSteps; step++) {

            // ------------------------------------------------------------
            // 1) PRODUCERS: cykliczna produkcja niezależna od aktywacji
            // ------------------------------------------------------------
            for (int i = 0; i < prodCount; i++) {
                if (prodPeriod[i] <= 0) {
                    continue; // okres 0 -> ignorujemy (nie chcemy nieskończenie szybkich)
                }
                if (prodTimer[i] > 0) {
                    prodTimer[i]--;
                }
                if (prodTimer[i] == 0) {
                    ProducerEntry pe = cachedProducers.get(i);
                    // dodaj tokeny (zera) do odpowiednich prePlaces (na koniec listy, bo sort desc)
                    for (ProducerOutput out : pe.outputs) {
                        ArrayList<Integer> K = multisets[out.prePlaceIndex];
                        for (int k = 0; k < out.weight; k++) {
                            K.add(0);
                        }
                    }
                    prodTimer[i] = prodPeriod[i];
                }
            }

            // ------------------------------------------------------------
            // 2) COMPETITORS: aktywacja zależna od podzbiorów + konsumpcja
            // ------------------------------------------------------------
            for (int i = 0; i < compCount; i++) {
                CompetitorEntry ce = cachedCompetitors.get(i);

                // jeśli jest w fazie produkcji – tylko odmierzamy czas (nie konsumuje)
                if (compProdRemaining[i] > 0) {
                    compProdRemaining[i]--;
                    continue;
                }

                // poza produkcją: sprawdź, czy competitor ma podzbiory aktywujące we wszystkich swoich prePlaces
                boolean enabled = isTransitionEnabledByLocalPlaces(multisets, ce.inputs);

                if (!enabled) {
                    // reset licznika aktywacji (jak w Twoim extended dla miejsca)
                    compActRemaining[i] = compActDuration[i];
                    continue;
                }

                // enabled == true
                if (compActDuration[i] == 0) {
                    // alfa nieaktywna -> "natychmiastowe uruchomienie" po spełnieniu warunku
                    consumeForCompetitor(multisets, ce.inputs);
                    compProdRemaining[i] = compProdDuration[i];
                    compActRemaining[i] = compActDuration[i];
                    continue;
                }

                if (compActRemaining[i] > 0) {
                    compActRemaining[i]--;
                }
                if (compActRemaining[i] == 0) {
                    // uruchomienie: zabierz tokeny
                    consumeForCompetitor(multisets, ce.inputs);
                    compProdRemaining[i] = compProdDuration[i];
                    compActRemaining[i] = compActDuration[i];
                }
            }

            // ------------------------------------------------------------
            // 3) AKTYWACJA transXTPN: wszystkie prePlaces muszą mieć podzbiory
            // ------------------------------------------------------------
            boolean enabledTrans = true;
            for (int i = 0; i < cachedPrePlaces.size(); i++) {
                PrePlaceInfo pInfo = cachedPrePlaces.get(i);
                if (!hasActivatingSubsetSortedDesc(multisets[i], pInfo.gammaL, pInfo.needForTrans)) {
                    enabledTrans = false;
                    break;
                }
            }

            if (enabledTrans) {
                current++;
                if (current > result) {
                    result = current;
                }
                if (current >= alphaU) {
                    // pełne wykorzystanie okna aktywacji – kończymy wcześniej
                    return alphaU;
                }
            } else {
                current = 0;
            }

            // ------------------------------------------------------------
            // 4) UPŁYW CZASU tokenów w każdym prePlace (i usunięcie > gammaU)
            // ------------------------------------------------------------
            for (int i = 0; i < cachedPrePlaces.size(); i++) {
                PrePlaceInfo pInfo = cachedPrePlaces.get(i);
                updateMultisetSortedDesc(multisets[i], pInfo.gammaU);
            }
        }

        // Jeśli result < alphaL, to w praktyce "nie dochodzi do aktywacji",
        // ale zgodnie ze specyfikacją zwracamy po prostu result.
        return result;
    }

    // ======================================================================
    //  INIT: budowanie wycinka sieci
    // ======================================================================

    private static LinkedHashMap<PlaceXTPN, Integer> collectPrePlaces(TransitionXTPN transXTPN) {
        LinkedHashMap<PlaceXTPN, Integer> map = new LinkedHashMap<>();
        List<Arc> inArcs = transXTPN.getInputArcs(); // Place -> Transition
        if (inArcs == null) return map;

        for (Arc arc : inArcs) {
            if (arc == null) continue;
            if (arc.getArcType() != Arc.TypeOfArc.NORMAL) continue;

            Object startNode = arc.getStartLocation().getParentNode();
            if (!(startNode instanceof PlaceXTPN)) {
                // W xTPN spodziewamy się PlaceXTPN
                continue;
            }

            PlaceXTPN p = (PlaceXTPN) startNode;
            int w = arc.getWeight();
            if (w <= 0) continue;

            map.merge(p, w, Integer::sum);
        }
        return map;
    }

    private static void collectProducers() {
        // Mapowanie po tranzycji (żeby jeden timer na tranzycję, a wiele wyjść do prePlaces)
        LinkedHashMap<TransitionXTPN, ProducerEntry> prodMap = new LinkedHashMap<>();

        for (int preIdx = 0; preIdx < cachedPrePlaces.size(); preIdx++) {
            PlaceXTPN p = cachedPrePlaces.get(preIdx).place;
            List<Arc> inputArcs = p.getInputArcs(); // Transition -> Place
            if (inputArcs == null) continue;

            for (Arc arc : inputArcs) {
                if (arc == null) continue;
                if (arc.getArcType() != Arc.TypeOfArc.NORMAL) continue;

                Object startNode = arc.getStartLocation().getParentNode();
                if (!(startNode instanceof TransitionXTPN)) continue;

                TransitionXTPN t = (TransitionXTPN) startNode;

                if (t.isKnockedOut()) continue;
                if (t == cachedTrans) continue; // transXTPN nie jest symulowana jako producent

                int w = arc.getWeight();
                if (w <= 0) continue;

                ProducerEntry pe = prodMap.computeIfAbsent(t, ProducerEntry::new);
                pe.outputs.add(new ProducerOutput(preIdx, w));
            }
        }

        cachedProducers.clear();
        cachedProducers.addAll(prodMap.values());
    }

    private static void collectCompetitors(TransitionXTPN transXTPN) {
        LinkedHashMap<TransitionXTPN, CompetitorEntry> compMap = new LinkedHashMap<>();

        for (int preIdx = 0; preIdx < cachedPrePlaces.size(); preIdx++) {
            PlaceXTPN p = cachedPrePlaces.get(preIdx).place;
            List<Arc> outArcs = p.getOutputArcs(); // Place -> Transition
            if (outArcs == null) continue;

            for (Arc arc : outArcs) {
                if (arc == null) continue;
                if (arc.getArcType() != Arc.TypeOfArc.NORMAL) continue;

                Object endNode = arc.getEndLocation().getParentNode();
                if (!(endNode instanceof TransitionXTPN)) continue;

                TransitionXTPN t = (TransitionXTPN) endNode;

                if (t.isKnockedOut()) continue;
                if (t == transXTPN) continue; // nie dodajemy badanej tranzycji jako competitor

                int w = arc.getWeight();
                if (w <= 0) continue;

                CompetitorEntry ce = compMap.computeIfAbsent(t, CompetitorEntry::new);

                // competitor zależy tylko od prePlaces i tylko stąd pobiera tokeny:
                ce.inputs.add(new CompetitorInput(preIdx, w, cachedPrePlaces.get(preIdx).gammaL));
            }
        }

        cachedCompetitors.clear();
        cachedCompetitors.addAll(compMap.values());
    }

    // ======================================================================
    //  WALIDACJA + 0->1
    // ======================================================================

    /**
     * Waliduje tryby alfa/beta w tranzycjach lokalnych. Dodatkowo wykrywa zera w częściach L
     * (alpha^L/beta^L) dla aktywnych trybów i pyta użytkownika o 0->1.
     *
     * Zwraca:
     *  0  -> ok, brak zer L
     *  1  -> użytkownik zgodził się na 0->1
     * -1  -> błąd / stop
     */
    private static int validateLocalSubnetAndAskForZeroFix(TransitionXTPN transXTPN, int param) {
        // Zbierz tranzycje: trans + producers + competitors
        LinkedHashSet<TransitionXTPN> transitions = new LinkedHashSet<>();
        transitions.add(transXTPN);

        for (ProducerEntry pe : cachedProducers) {
            transitions.add(pe.transition);
        }
        for (CompetitorEntry ce : cachedCompetitors) {
            transitions.add(ce.transition);
        }

        List<TransitionXTPN> invalidMode = new ArrayList<>();
        List<TransitionXTPN> zeroL = new ArrayList<>();

        for (TransitionXTPN t : transitions) {
            if (t == null) continue;
            if (t.isKnockedOut()) continue;

            boolean alphaActive = t.isAlphaModeActive();
            boolean betaActive = t.isBetaModeActive();

            if (!alphaActive && !betaActive) {
                invalidMode.add(t);
                continue;
            }

            // Zera w częściach L są problematyczne w szczególności gdy param == 1/2?
            // U Ciebie w poprzednim algorytmie pytanie dotyczyło zawsze L,
            // więc tu robimy tak samo: wykryj alpha^L/beta^L == 0 w aktywnych trybach.
            double alphaMin = t.getAlphaMinValue();
            double betaMin = t.getBetaMinValue();

            boolean hasZeroLow = false;
            if (alphaActive && alphaMin == 0.0) hasZeroLow = true;
            if (betaActive && betaMin == 0.0) hasZeroLow = true;

            if (hasZeroLow) {
                zeroL.add(t);
            }
        }

        if (!invalidMode.isEmpty()) {
            String message = "Invalid xTPN transitions (both alpha and beta modes inactive): "
                    + joinTransitionIds(invalidMode)
                    + ".\nSuch transitions are not allowed (unless knocked out).";

            JOptionPane.showMessageDialog(
                    null,
                    message,
                    "Invalid xTPN transitions detected",
                    JOptionPane.ERROR_MESSAGE
            );
            return -1;
        }

        if (!zeroL.isEmpty()) {
            String message = "Incorrect zero-time L-values in transitions: "
                    + joinTransitionIds(zeroL)
                    + ".\n"
                    + "One or more alpha^L / beta^L values are equal to 0.\n\n"
                    + "You can:\n"
                    + " - Stop now and fix the model manually,\n"
                    + " - Continue, treating all zero L-values as 1 for this analysis.";

            Object[] options = {"Continue with min. time = 1", "Stop"};
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
                return 1;
            }
            return -1;
        }

        return 0;
    }

    private static String joinTransitionIds(List<TransitionXTPN> list) {
        ArrayList<Transition> allTransitions = overlord.getWorkspace().getProject().getTransitions();
        List<String> ids = new ArrayList<>();
        for (TransitionXTPN t : list) {
            int idx = allTransitions.indexOf(t);
            ids.add(idx >= 0 ? "t" + idx : "t?");
        }
        return String.join(", ", ids);
    }

    // ======================================================================
    //  WYZNACZANIE CZASÓW (period producers, alpha/beta competitors)
    // ======================================================================

    private static int normalizeAlphaL(TransitionXTPN t, boolean fixZeroTimesL) {
        double v = t.getAlphaMinValue();
        if (fixZeroTimesL && t.isAlphaModeActive() && v == 0.0) {
            v = 1.0;
        }
        int aL = (int) Math.round(v);
        if (aL < 0) aL = 0;
        return aL;
    }

    private static int normalizeAlphaU(TransitionXTPN t) {
        double v = t.getAlphaMaxValue();
        int aU = (int) Math.round(v);
        if (aU < 0) aU = 0;
        return aU;
    }

    private static void finalizeProducerPeriods(int param, boolean fixZeroTimesL) {
        for (ProducerEntry pe : cachedProducers) {
            pe.period = computeProducerPeriod(pe.transition, param, fixZeroTimesL);
        }
    }

    private static int computeProducerPeriod(TransitionXTPN t, int param, boolean fixZeroTimesL) {
        if (t == null) return 0;
        if (t.isKnockedOut()) return 0;

        boolean alphaActive = t.isAlphaModeActive();
        boolean betaActive = t.isBetaModeActive();
        if (!alphaActive && !betaActive) return 0;

        double a = 0.0;
        double b = 0.0;

        if (param == 1) { // producers fast
            if (alphaActive) {
                a = t.getAlphaMinValue();
                if (fixZeroTimesL && a == 0.0) a = 1.0;
            }
            if (betaActive) {
                b = t.getBetaMinValue();
                if (fixZeroTimesL && b == 0.0) b = 1.0;
            }
        } else { // param == 2, producers slow
            if (alphaActive) a = t.getAlphaMaxValue();
            if (betaActive) b = t.getBetaMaxValue();
        }

        int period = (int) Math.round(a + b);
        if (period <= 0) {
            // zabezpieczenie: nie chcemy nieskończenie szybkich producentów
            return 0;
        }
        return period;
    }

    private static void finalizeCompetitorTimes(int param, boolean fixZeroTimesL) {
        for (CompetitorEntry ce : cachedCompetitors) {
            TransitionXTPN t = ce.transition;
            if (t == null || t.isKnockedOut()) {
                ce.activationTime = 0;
                ce.productionTime = 0;
                continue;
            }

            boolean alphaActive = t.isAlphaModeActive();
            boolean betaActive = t.isBetaModeActive();

            // activation
            int act;
            if (!alphaActive) {
                act = 0; // alfa wyłączona -> natychmiastowe uruchomienie po spełnieniu warunku
            } else {
                double a = (param == 1) ? t.getAlphaMaxValue() : t.getAlphaMinValue();
                if (param == 2 && fixZeroTimesL && a == 0.0) a = 1.0;
                act = (int) Math.round(a);
                if (act < 0) act = 0;
            }

            // production
            int prod;
            if (!betaActive) {
                prod = 0; // beta wyłączona -> brak fazy produkcji
            } else {
                double b = (param == 1) ? t.getBetaMaxValue() : t.getBetaMinValue();
                if (param == 2 && fixZeroTimesL && b == 0.0) b = 1.0;
                prod = (int) Math.round(b);
                if (prod < 0) prod = 0;
            }

            ce.activationTime = act;
            ce.productionTime = prod;
        }
    }

    // ======================================================================
    //  HORYZONT UPROSZCZONY
    // ======================================================================

    private static long computeSimplifiedHorizon(int maxGammaU) {
        List<Integer> periods = new ArrayList<>();

        for (ProducerEntry pe : cachedProducers) {
            if (pe.period > 0) periods.add(pe.period);
        }

        for (CompetitorEntry ce : cachedCompetitors) {
            int cycle = ce.activationTime + ce.productionTime;
            if (cycle > 0) periods.add(cycle);
        }

        long lcm = computeLcmOfList(periods);
        if (lcm <= 0L) {
            // brak okresów -> tylko starzenie tokenów
            return Math.max(0, maxGammaU);
        }
        return lcm + (long) Math.max(0, maxGammaU);
    }

    // ======================================================================
    //  SYMULACJA: warunki aktywacji i operacje na multizbiorach
    // ======================================================================

    /**
     * Warunek istnienia podzbioru aktywującego w posortowanym malejąco multizbiorze:
     * istnieje co najmniej need tokenów i need-ty token ma wiek >= gammaL.
     */
    private static boolean hasActivatingSubsetSortedDesc(List<Integer> multiset, int gammaL, int need) {
        if (need <= 0) return true;
        if (multiset == null) return false;
        if (multiset.size() < need) return false;
        return multiset.get(need - 1) >= gammaL;
    }

    private static boolean isTransitionEnabledByLocalPlaces(ArrayList<Integer>[] multisets, List<CompetitorInput> inputs) {
        for (CompetitorInput in : inputs) {
            ArrayList<Integer> K = multisets[in.prePlaceIndex];
            if (!hasActivatingSubsetSortedDesc(K, in.gammaL, in.weight)) {
                return false;
            }
        }
        return true;
    }

    private static void consumeForCompetitor(ArrayList<Integer>[] multisets, List<CompetitorInput> inputs) {
        for (CompetitorInput in : inputs) {
            ArrayList<Integer> K = multisets[in.prePlaceIndex];
            consumeOldestEligibleSortedDesc(K, in.gammaL, in.weight);
        }
    }

    /**
     * Zdejmuje dokładnie 'count' najstarszych tokenów o wieku >= gammaL.
     * Zakładamy, że wywołanie następuje tylko gdy istnieje podzbiór aktywujący.
     */
    private static void consumeOldestEligibleSortedDesc(ArrayList<Integer> multiset, int gammaL, int count) {
        if (multiset == null || count <= 0) return;

        for (int i = 0; i < count; i++) {
            if (multiset.isEmpty()) break;
            int age = multiset.get(0);
            if (age < gammaL) break; // nie powinno się zdarzyć przy poprawnej logice
            multiset.remove(0);
        }
    }

    /**
     * Upływ czasu w multizbiorze posortowanym malejąco.
     * Usuwa tokeny, które po inkrementacji przekroczyły gammaU.
     */
    private static void updateMultisetSortedDesc(ArrayList<Integer> multiset, int gammaU) {
        if (multiset == null) return;
        if (gammaU < 0) gammaU = 0;

        // najstarsze tokeny są na początku, więc te które "przepadną" są też na początku
        while (!multiset.isEmpty()) {
            int age = multiset.get(0);
            if (age + 1 > gammaU) {
                multiset.remove(0);
            } else {
                break;
            }
        }

        for (int i = 0; i < multiset.size(); i++) {
            multiset.set(i, multiset.get(i) + 1);
        }
    }

    // ======================================================================
    //  MULTISET POCZĄTKOWY
    // ======================================================================

    private static ArrayList<Integer> extractAndSortInitialMultiset(PlaceXTPN p, int gammaU) {
        ArrayList<Integer> result = new ArrayList<>();
        ArrayList<Double> initial = p.copyMultiset();
        if (initial != null) {
            for (Double d : initial) {
                if (d == null) continue;
                int age = (int) Math.round(d);
                if (age >= 0 && age <= gammaU) {
                    result.add(age);
                }
            }
        }
        // sort malejąco: najstarsze na początku
        result.sort(Comparator.reverseOrder());
        return result;
    }

    // ======================================================================
    //  LCM/GCD
    // ======================================================================

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
        long g = gcd(a, b);
        long result = (a / g) * b;
        if (result < 0L || result > Long.MAX_VALUE) {
            return Long.MAX_VALUE;
        }
        return result;
    }

    // ======================================================================
    //  CACHE / STRUKTURY
    // ======================================================================

    private static void resetCache() {
        cacheInitialized = false;
        cachedTrans = null;
        cachedIncludeCompetitors = true;
        cachedParam = 1;
        cachedFixZeroTimesL = false;
        cachedAlphaL = 0;
        cachedAlphaU = 0;

        cachedPrePlaces.clear();
        cachedProducers.clear();
        cachedCompetitors.clear();
    }

    private static final class PrePlaceInfo {
        final PlaceXTPN place;
        final int gammaL;
        final int gammaU;
        final int needForTrans;
        final ArrayList<Integer> initialMultisetSortedDesc;

        PrePlaceInfo(PlaceXTPN place, int gammaL, int gammaU, int needForTrans, ArrayList<Integer> initialMultisetSortedDesc) {
            this.place = place;
            this.gammaL = gammaL;
            this.gammaU = gammaU;
            this.needForTrans = needForTrans;
            this.initialMultisetSortedDesc = (initialMultisetSortedDesc == null) ? new ArrayList<>() : initialMultisetSortedDesc;
        }
    }

    private static final class ProducerEntry {
        final TransitionXTPN transition;
        int period = 0;
        final List<ProducerOutput> outputs = new ArrayList<>();

        ProducerEntry(TransitionXTPN t) {
            this.transition = t;
        }
    }

    private static final class ProducerOutput {
        final int prePlaceIndex;
        final int weight;

        ProducerOutput(int prePlaceIndex, int weight) {
            this.prePlaceIndex = prePlaceIndex;
            this.weight = weight;
        }
    }

    private static final class CompetitorEntry {
        final TransitionXTPN transition;
        int activationTime = 0;
        int productionTime = 0;
        final List<CompetitorInput> inputs = new ArrayList<>();

        CompetitorEntry(TransitionXTPN t) {
            this.transition = t;
        }
    }

    private static final class CompetitorInput {
        final int prePlaceIndex;
        final int weight;
        final int gammaL;

        CompetitorInput(int prePlaceIndex, int weight, int gammaL) {
            this.prePlaceIndex = prePlaceIndex;
            this.weight = weight;
            this.gammaL = gammaL;
        }
    }
}
