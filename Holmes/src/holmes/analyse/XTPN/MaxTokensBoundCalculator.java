package holmes.analyse.XTPN;

import holmes.petrinet.elements.PlaceXTPN;
import holmes.petrinet.elements.Transition;
import holmes.petrinet.elements.TransitionXTPN;

import java.util.ArrayList;
import java.util.List;

public class MaxTokensBoundCalculator {
    private MaxTokensBoundCalculator() {
        // Klasa narzędziowa – brak konstruktorów publicznych.
    }
    
    public static int maxSteps(PlaceXTPN placeXTPN) {
        ArrayList<Transition> preTransitions = placeXTPN.getInputTransitions();
        List<Integer> fastTimes = new ArrayList<>();
        if (preTransitions != null) {
            for (Transition t : preTransitions) {
                if (!(t instanceof TransitionXTPN)) {
                    continue;
                }
                TransitionXTPN xt = (TransitionXTPN) t;
                double alphaMin = xt.getAlphaMinValue();
                double betaMin = xt.getBetaMinValue();
                int fast = (int) Math.round(alphaMin + betaMin);
                if (fast > 0) {
                    fastTimes.add(fast);
                }
            }
        }
        // 5. Oblicz okres produkcji: tau_maxProd = lcm(fast(t_i)).
        int tauMaxProd = computeLcmOfList(fastTimes);
        int gammaU = (int) Math.round(placeXTPN.getGammaMaxValue());
        return tauMaxProd + gammaU;
    }

    /**
     * Główna metoda obliczająca górną granicę liczby tokenów w zadanym miejscu xTPN.
     *
     * @param placeXTPN miejsce xTPN, dla którego liczymy oszacowanie z góry
     * @return maksymalna (zawyżona, ale bezpieczna) liczba tokenów,
     *         jaka może się pojawić w tym miejscu w uproszczonym modelu
     */
    public static int computeUpperBoundForPlace(PlaceXTPN placeXTPN) {
        if (placeXTPN == null) {
            throw new IllegalArgumentException("placeXTPN must not be null");
        }

        // 1. Odczyt gamma^U (przyjmujemy, że > 0 i jest liczbą całkowitą po skalowaniu).
        int gammaU = (int) Math.round(placeXTPN.getGammaMaxValue());
        if (gammaU <= 0) {
            // W takim przypadku tokeny praktycznie nie mogą istnieć w miejscu,
            // ale dla bezpieczeństwa potraktujmy to jako "brak życia" tokenów.
            gammaU = 0;
        }

        // 2. Zbierz tranzycje wejściowe względem miejsca p_x.
        //    Zakładamy, że getInputTransitions() zwraca tranzycje z łukiem t -> p_x
        //    (czyli takie, które produkują tokeny do tego miejsca).
        ArrayList<Transition> preTransitions = placeXTPN.getInputTransitions();

        // 3. Zbuduj wektory fast(t) i wag łuków V(t, p_x) dla tych tranzycji,
        //    które rzeczywiście produkują tokeny do p_x (waga > 0 i fast > 0).
        List<Integer> fastTimes = new ArrayList<>();
        List<Integer> arcWeights = new ArrayList<>();

        if (preTransitions != null) {
            for (Transition t : preTransitions) {
                if (!(t instanceof TransitionXTPN)) {
                    // Jeśli trafi się jakaś inna implementacja, można:
                    // - pominąć ją (jak poniżej),
                    // - lub rzucić wyjątek – w zależności od wymagań.
                    continue;
                }
                TransitionXTPN xt = (TransitionXTPN) t;

                // fast(t) = alpha^L_t + beta^L_t, przyjmujemy, że po skalowaniu to int.
                double alphaMin = xt.getAlphaMinValue();
                double betaMin = xt.getBetaMinValue();
                int fast = (int) Math.round(alphaMin + betaMin);

                // Waga łuku z tranzycji do miejsca p_x.
                int weight = t.getOutputArcWeightTo(placeXTPN);

                // Interesują nas tylko tranzycje, które rzeczywiście produkują coś do p_x
                // i mają dodatni czas fast.
                if (weight > 0 && fast > 0) {
                    fastTimes.add(fast);
                    arcWeights.add(weight);
                }
            }
        }

        // 4. Skopiuj tokeny początkowe z miejsca (czasy życia).
        //    accessMultiset() zwraca ArrayList<Double> – czasy życia tokenów.
        ArrayList<Double> initialMultiset = placeXTPN.accessMultiset();
        ArrayList<Integer> multisetK = new ArrayList<>();

        if (initialMultiset != null) {
            for (Double ageDouble : initialMultiset) {
                if (ageDouble == null) {
                    continue;
                }
                int age = (int) Math.round(ageDouble);
                // Tokeny, które już przekroczyły gammaU, i tak zaraz znikną.
                // Możemy je od razu odfiltrować.
                if (age <= gammaU && age >= 0) {
                    multisetK.add(age);
                }
            }
        }

        // 5. Oblicz okres produkcji: tau_maxProd = lcm(fast(t_i)).
        int tauMaxProd = computeLcmOfList(fastTimes);

        // 6. Oblicz łączną liczbę iteracji: tau_max = tau_maxProd + gammaU.
        //    Dodatkowo, jeśli nie ma żadnych tranzycji wejściowych (lcm = 0),
        //    to symulujemy tylko do gammaU (tokeny początkowe po tym czasie znikną).
        int iterations;
        if (tauMaxProd <= 0) {
            iterations = gammaU;
        } else {
            long tmp = (long) tauMaxProd + (long) gammaU;
            // Strzeż się przepełnienia – w razie czego saturacja do Integer.MAX_VALUE.
            iterations = (tmp > Integer.MAX_VALUE) ? Integer.MAX_VALUE : (int) tmp;
        }

        // Jeśli gammaU == 0, nie ma sensu wykonywać pętli – tokeny znikają natychmiast.
        if (iterations <= 0) {
            return multisetK.size(); // tylko tokeny początkowe (jeśli gammaU=0, i tak zaraz znikną)
        }

        // 7. Zainicjalizuj wektor timeVector dla tranzycji wejściowych:
        //    timeVector[i] = fast(t_i) – odliczanie do następnego uruchomienia.
        int numberOfProducers = fastTimes.size();
        int[] timeVector = new int[numberOfProducers];
        int[] fastVector = new int[numberOfProducers];
        int[] weightsVector = new int[numberOfProducers];

        for (int i = 0; i < numberOfProducers; i++) {
            int f = fastTimes.get(i);
            int w = arcWeights.get(i);
            fastVector[i] = f;
            timeVector[i] = f; // pierwsze uruchomienie po fast krokach
            weightsVector[i] = w;
        }

        // 8. Symulacja – szukamy maksimum liczby tokenów w multisetK.
        int maxTokens = multisetK.size();

        for (int step = 1; step <= iterations; step++) {

            // 8.1. Dekrementuj timery tranzycji wejściowych i uruchom te,
            //      które dochodzą do zera.
            for (int i = 0; i < numberOfProducers; i++) {
                if (timeVector[i] > 0) {
                    timeVector[i]--;
                }

                if (timeVector[i] == 0) {
                    int weight = weightsVector[i];
                    // Dodaj "weight" nowych tokenów z czasem życia = 0.
                    for (int k = 0; k < weight; k++) {
                        multisetK.add(0);
                    }
                    // Zrestartuj licznik do kolejnego uruchomienia po fast(t) krokach.
                    timeVector[i] = fastVector[i];
                }
            }

            // 8.2. Zaktualizuj czasy życia tokenów i usuń te, które przekroczyły gammaU.
            updateMultiset(multisetK, gammaU);

            // 8.3. Zaktualizuj zapamiętane maksimum liczby tokenów.
            if (multisetK.size() > maxTokens) {
                maxTokens = multisetK.size();
            }
        }

        return maxTokens;
    }

    /**
     * Funkcja pomocnicza do aktualizacji multizbioru K:
     * - zwiększa wiek każdego tokenu o 1,
     * - usuwa tokeny, których wiek po zwiększeniu przekracza gammaU.
     *
     * @param multiset lista czasów życia tokenów
     * @param gammaU   maksymalny dopuszczalny czas życia tokenu
     */
    private static void updateMultiset(List<Integer> multiset, int gammaU) {
        // Iteracja po indeksach z możliwością usuwania w locie.
        int i = 0;
        while (i < multiset.size()) {
            int age = multiset.get(i);
            age += 1; // upływ czasu o 1 jednostkę

            if (age > gammaU) {
                // Token przekroczył maksymalny czas – usuń go.
                multiset.remove(i);
                // Nie zwiększamy i, bo lista się "zsunęła".
            } else {
                // Token nadal żyje – zaktualizuj jego wiek.
                multiset.set(i, age);
                i++;
            }
        }
    }

    /**
     * Oblicza najmniejszą wspólną wielokrotność (LCM) listy dodatnich liczb całkowitych.
     * Jeśli lista jest pusta, zwraca 0.
     */
    private static int computeLcmOfList(List<Integer> values) {
        if (values == null || values.isEmpty()) {
            return 0;
        }

        int lcm = 0;
        for (int v : values) {
            if (v <= 0) {
                continue; // ignorujemy niepoprawne wartości fast(t) <= 0
            }
            if (lcm == 0) {
                lcm = v;
            } else {
                lcm = lcm(lcm, v);
                if (lcm == Integer.MAX_VALUE) {
                    // Saturacja – dalej już nie zwiększamy, ale zachowujemy "ogromny" okres.
                    break;
                }
            }
        }
        return lcm;
    }

    /**
     * Największy wspólny dzielnik (GCD) dla nieujemnych liczb całkowitych.
     */
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

    /**
     * Najmniejsza wspólna wielokrotność (LCM) dla dwóch dodatnich liczb całkowitych.
     * Chroni przed przepełnieniem – w razie czego saturacja do Integer.MAX_VALUE.
     */
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
