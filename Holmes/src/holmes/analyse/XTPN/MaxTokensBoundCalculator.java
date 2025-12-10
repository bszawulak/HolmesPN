package holmes.analyse.XTPN;

import holmes.petrinet.elements.PlaceXTPN;
import holmes.petrinet.elements.Transition;
import holmes.petrinet.elements.TransitionXTPN;
import holmes.petrinet.elements.Arc;

import java.util.ArrayList;
import java.util.List;

public final class MaxTokensBoundCalculator {

    // ====== CACHE DLA TRYBU PROSTEGO (tylko wejścia) ======

    private static List<Integer> cachedFastTimesSimple = new ArrayList<>();
    private static List<Integer> cachedArcWeightsSimple = new ArrayList<>();
    private static int cachedGammaUSimple = 0;
    private static PlaceXTPN cachedPlaceSimple = null;
    private static boolean cacheInitializedSimple = false;

    // ====== CACHE DLA TRYBU ROZSZERZONEGO (wejścia + wyjścia) ======

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

    private MaxTokensBoundCalculator() {
        // Klasa narzędziowa – brak publicznego konstruktora.
    }

    // =======================================================================
    //  CZĘŚĆ 1: PROSTY ALGORYTM – TYLKO TRANZYCJE WEJŚCIOWE (BEZ WYJŚĆ)
    // =======================================================================

    public static long maxSteps(PlaceXTPN placeXTPN) {
        if (placeXTPN == null) {
            throw new IllegalArgumentException("placeXTPN must not be null");
        }

        cachedFastTimesSimple = new ArrayList<>();
        cachedArcWeightsSimple = new ArrayList<>();
        cacheInitializedSimple = false;
        cachedPlaceSimple = placeXTPN;

        int gammaU = (int) Math.round(placeXTPN.getGammaMaxValue());
        if (gammaU < 0) {
            gammaU = 0;
        }
        cachedGammaUSimple = gammaU;

        ArrayList<Transition> preTransitions = placeXTPN.getInputTransitions();
        if (preTransitions != null) {
            for (Transition t : preTransitions) {
                if (!(t instanceof TransitionXTPN)) {
                    continue;
                }
                TransitionXTPN xt = (TransitionXTPN) t;

                double alphaMin = xt.getAlphaMinValue();
                double betaMin  = xt.getBetaMinValue();
                int fast = (int) Math.round(alphaMin + betaMin);

                int weight = t.getOutputArcWeightTo(placeXTPN);

                if (fast > 0 && weight > 0) {
                    cachedFastTimesSimple.add(fast);
                    cachedArcWeightsSimple.add(weight);
                }
            }
        }

        long tauMaxProd = computeLcmOfList(cachedFastTimesSimple);

        long steps;
        if (tauMaxProd <= 0L) {
            steps = gammaU;
        } else {
            long tmp = tauMaxProd + (long) gammaU;
            steps = (tmp > Long.MAX_VALUE) ? Long.MAX_VALUE : tmp;
        }

        cacheInitializedSimple = true;
        return steps;
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

        for (long step = 1L; step <= iterations; step++) {

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

            // Upływ czasu
            updateMultiset(multisetK, gammaU);

            if (multisetK.size() > maxTokens) {
                maxTokens = multisetK.size();
            }
        }

        return maxTokens;
    }

    // =======================================================================
    //  CZĘŚĆ 2: ROZSZERZONY ALGORYTM – WEJŚCIA + WYJŚCIA
    // =======================================================================

    public static long maxStepsExtended(PlaceXTPN placeXTPN) {
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
        return steps;
    }

    public static int computeUpperBoundForPlaceExtended(PlaceXTPN placeXTPN,
                                                        long iterations,
                                                        boolean unsafePlaces) {
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

        // Producenci
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

        // Konsumenci
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

            // 2. Upływ czasu
            updateMultiset(multisetK, gammaU);

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
        }

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
}
