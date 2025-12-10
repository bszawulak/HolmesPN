package holmes.analyse.XTPN;

import holmes.petrinet.elements.PlaceXTPN;
import holmes.petrinet.elements.Transition;
import holmes.petrinet.elements.TransitionXTPN;

import java.util.ArrayList;
import java.util.List;

public final class MaxTokensBoundCalculator {

    // Cache danych przygotowanych przez maxSteps:
    private static List<Integer> cachedFastTimes = new ArrayList<>();
    private static List<Integer> cachedArcWeights = new ArrayList<>();
    private static int cachedGammaU = 0;
    private static PlaceXTPN cachedPlace = null;
    private static boolean cacheInitialized = false;

    private MaxTokensBoundCalculator() {
        // Klasa narzędziowa – brak publicznego konstruktora.
    }

    /**
     * Oblicza maksymalną liczbę kroków symulacji dla danego miejsca p_x.
     * Przy okazji wypełnia cache: cachedFastTimes, cachedArcWeights, cachedGammaU.
     *
     * @param placeXTPN miejsce p_x
     * @return liczba kroków (tau_max = lcm(fast(t)) + gammaU) lub 0, jeśli nie ma producentów i gammaU == 0
     */
    public static long maxSteps(PlaceXTPN placeXTPN) {
        if (placeXTPN == null) {
            throw new IllegalArgumentException("placeXTPN must not be null");
        }

        // Wyczyszczenie i inicjalizacja cache
        cachedFastTimes = new ArrayList<>();
        cachedArcWeights = new ArrayList<>();
        cacheInitialized = false;
        cachedPlace = placeXTPN;

        // gamma^U (skalowane do int)
        int gammaU = (int) Math.round(placeXTPN.getGammaMaxValue());
        if (gammaU < 0) {
            gammaU = 0;
        }
        cachedGammaU = gammaU;

        // Zbierz tranzycje wejściowe i wypełnij fastTimes + arcWeights
        ArrayList<Transition> preTransitions = placeXTPN.getInputTransitions();
        if (preTransitions != null) {
            for (Transition t : preTransitions) {
                if (!(t instanceof TransitionXTPN)) {
                    // Jeśli to nie TransitionXTPN, pomijamy (lub można rzucić wyjątek – zależy od projektu)
                    continue;
                }
                TransitionXTPN xt = (TransitionXTPN) t;

                double alphaMin = xt.getAlphaMinValue();
                double betaMin = xt.getBetaMinValue();
                int fast = (int) Math.round(alphaMin + betaMin);

                int weight = t.getOutputArcWeightTo(placeXTPN);

                // Bierzemy tylko tranzycje, które faktycznie produkują do p_x i mają dodatni fast(t)
                if (fast > 0 && weight > 0) {
                    cachedFastTimes.add(fast);
                    cachedArcWeights.add(weight);
                }
            }
        }

        // lcm dla fast(t_i)
        int tauMaxProd = computeLcmOfList(cachedFastTimes);

        // tau_max = tauMaxProd + gammaU (z ochroną przed przepełnieniem)
        long steps;
        if (tauMaxProd <= 0) {
            // Brak producentów – symulacja co najwyżej do gammaU (tylko "wykruszanie" tokenów początkowych)
            steps = gammaU;
        } else {
            long tmp = (long) tauMaxProd + (long) gammaU;
            steps = (tmp > Long.MAX_VALUE) ? Long.MAX_VALUE : (int) tmp;
        }

        cacheInitialized = true;
        return steps;
    }

    /**
     * Oblicza górną (zawyżoną, ale bezpieczną) granicę liczby tokenów w miejscu p_x,
     * korzystając z liczby kroków wyznaczonej wcześniej przez maxSteps.
     *
     * Uwaga: zakładamy, że wcześniej wywołano maxSteps(placeXTPN), a parametr 'iterations'
     * jest wartością zwróconą przez maxSteps dla TEGO SAMEGO miejsca.
     *
     * @param placeXTPN miejsce p_x
     * @param iterations liczba kroków symulacji (tau_max)
     * @return maksymalna liczba tokenów lub -1, jeśli cache nie został przygotowany lub parametry są niepoprawne
     */
    public static int computeUpperBoundForPlace(PlaceXTPN placeXTPN, long iterations) {
        if (placeXTPN == null) {
            throw new IllegalArgumentException("placeXTPN must not be null");
        }

        // Sprawdzenie, czy cache został przygotowany i dotyczy tego samego miejsca
        if (!cacheInitialized || cachedPlace != placeXTPN) {
            // nie wywołano maxSteps() lub dla innego miejsca
            return -1;
        }

        if (iterations <= 0) {
            // Brak sensu symulować – albo parametry są błędne,
            // albo gammaU == 0 i brak producentów; zwracamy kod błędu
            return -1;
        }

        final int gammaU = cachedGammaU;

        // Skopiuj tokeny początkowe z miejsca:
        // Używamy copyMultiset(), żeby nie dotykać oryginalnego multizbioru.
        ArrayList<Double> initialMultiset = placeXTPN.copyMultiset();
        ArrayList<Integer> multisetK = new ArrayList<>();

        if (initialMultiset != null) {
            for (Double ageDouble : initialMultiset) {
                if (ageDouble == null) {
                    continue;
                }
                int age = (int) Math.round(ageDouble);
                if (age >= 0 && age <= gammaU) {
                    multisetK.add(age);
                }
            }
        }

        // Przygotowanie wektorów fast/time/weights z cache
        int numberOfProducers = cachedFastTimes.size();
        int[] fastVector = new int[numberOfProducers];
        int[] timeVector = new int[numberOfProducers];
        int[] weightsVector = new int[numberOfProducers];

        for (int i = 0; i < numberOfProducers; i++) {
            int f = cachedFastTimes.get(i);
            int w = cachedArcWeights.get(i);
            fastVector[i] = f;
            timeVector[i] = f; // pierwsze uruchomienie po fast(t) krokach
            weightsVector[i] = w;
        }

        int maxTokens = multisetK.size();

        // Główna pętla symulacji
        for (int step = 1; step <= iterations; step++) {

            // 1. Obsługa producentów: odliczanie i produkcja nowych tokenów
            for (int i = 0; i < numberOfProducers; i++) {
                if (timeVector[i] > 0) {
                    timeVector[i]--;
                }

                if (timeVector[i] == 0) {
                    int weight = weightsVector[i];
                    for (int k = 0; k < weight; k++) {
                        multisetK.add(0); // nowy token z czasem życia 0
                    }
                    timeVector[i] = fastVector[i];
                }
            }

            // 2. Upływ czasu: postarzenie tokenów i usunięcie tych, które przekraczają gammaU
            updateMultiset(multisetK, gammaU);

            // 3. Aktualizacja maksimum
            if (multisetK.size() > maxTokens) {
                maxTokens = multisetK.size();
            }
        }

        return maxTokens;
    }

    /**
     * Zwiększa wiek wszystkich tokenów o 1 i usuwa te, których wiek po zwiększeniu
     * przekracza gammaU.
     */
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

    /**
     * LCM dla listy dodatnich intów. Jeśli lista jest pusta lub nie ma dodatnich wartości – zwraca 0.
     */
    private static int computeLcmOfList(List<Integer> values) {
        if (values == null || values.isEmpty()) {
            return 0;
        }

        int lcm = 0;
        for (int v : values) {
            if (v <= 0) {
                continue;
            }
            if (lcm == 0) {
                lcm = v;
            } else {
                lcm = lcm(lcm, v);
                if (lcm == Integer.MAX_VALUE) {
                    break; // saturacja
                }
            }
        }
        return lcm;
    }

    private static int gcd(int a, int b) {
        a = Math.abs(a);
        b = Math.abs(b);
        if (a == 0) return b;
        if (b == 0) return a;

        while (b != 0) {
            int tmp = a % b;
            a = b;
            b = tmp;
        }
        return a;
    }

    private static int lcm(int a, int b) {
        if (a <= 0 || b <= 0) return 0;
        int gcd = gcd(a, b);
        long result = (long) a / (long) gcd * (long) b;
        if (result > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        return (int) result;
    }
}
