package holmes.analyse.XTPN;

import holmes.petrinet.elements.Arc;
import holmes.petrinet.elements.PlaceXTPN;
import holmes.petrinet.elements.TransitionXTPN;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Local token-count analysis for one xTPN place.
 *
 * <p>The class implements the two variants described in the article:</p>
 * <ul>
 *   <li>{@link Mode#PRODUCERS_ONLY}: consumers are ignored;</li>
 *   <li>{@link Mode#WITH_CONSUMERS}: all NORMAL output arcs of the place are
 *       represented by consumers processed in a fixed order.</li>
 * </ul>
 *
 * <p>No repeated-configuration detection is performed here.  The simulation
 * always runs through the supplied number of scaled time steps.</p>
 */
public final class MaxTokensBoundCalculatorV2 {
    private static final int DEFAULT_EXTRA_BLOCKS = 3;
    private static final long DEFAULT_MAX_STEPS_CAP = 100_000L;

    private MaxTokensBoundCalculatorV2() {
    }

    public enum Mode {
        PRODUCERS_ONLY,
        WITH_CONSUMERS
    }

    public enum HorizonMethod {
        NO_PRODUCERS,
        PRODUCER_PERIOD,
        CONSUMER_RATE_NON_POSITIVE,
        CONSUMER_RATE_POSITIVE
    }

    /**
     * Parameters used only when maxSteps is estimated.  A user-supplied
     * maxSteps can always be passed directly to {@link #compute}.
     */
    public static final class HorizonOptions {
        private final int extraBlocks;
        private final long maxStepsCap;

        public HorizonOptions(int extraBlocks, long maxStepsCap) {
            if (extraBlocks <= 0) {
                throw new IllegalArgumentException("extraBlocks must be positive");
            }
            if (maxStepsCap < 0L) {
                throw new IllegalArgumentException("maxStepsCap must not be negative");
            }
            this.extraBlocks = extraBlocks;
            this.maxStepsCap = maxStepsCap;
        }

        public static HorizonOptions defaults() {
            return new HorizonOptions(DEFAULT_EXTRA_BLOCKS, DEFAULT_MAX_STEPS_CAP);
        }

        public static HorizonOptions withoutCap(int extraBlocks) {
            return new HorizonOptions(extraBlocks, Long.MAX_VALUE);
        }

        public int getExtraBlocks() {
            return extraBlocks;
        }

        public long getMaxStepsCap() {
            return maxStepsCap;
        }
    }

    /**
     * Immutable data extracted from the Holmes net.  Preparing the model once
     * and using it for both maxSteps estimation and simulation prevents stale
     * static caches and guarantees that both operations use the same scale and
     * the same consumer order.
     */
    public static final class AnalysisModel {
        private final PlaceXTPN place;
        private final Mode mode;
        private final long timeScale;
        private final long gammaL;
        private final long gammaU;
        private final List<Long> initialAges;
        private final List<ProducerSpec> producers;
        private final List<ConsumerSpec> consumers;

        private AnalysisModel(
                PlaceXTPN place,
                Mode mode,
                long timeScale,
                long gammaL,
                long gammaU,
                List<Long> initialAges,
                List<ProducerSpec> producers,
                List<ConsumerSpec> consumers) {
            this.place = place;
            this.mode = mode;
            this.timeScale = timeScale;
            this.gammaL = gammaL;
            this.gammaU = gammaU;
            this.initialAges = List.copyOf(initialAges);
            this.producers = List.copyOf(producers);
            this.consumers = List.copyOf(consumers);
        }

        public PlaceXTPN getPlace() {
            return place;
        }

        public Mode getMode() {
            return mode;
        }

        /**
         * One original time unit is represented by {@code timeScale}
         * simulation steps.
         */
        public long getTimeScale() {
            return timeScale;
        }

        public long getScaledGammaL() {
            return gammaL;
        }

        public long getScaledGammaU() {
            return gammaU;
        }

        public int getInitialTokenCount() {
            return initialAges.size();
        }

        public int getProducerCount() {
            return producers.size();
        }

        public int getConsumerCount() {
            return consumers.size();
        }
    }

    /**
     * Result of the direct producer-only formula.
     *
     * <p>{@code producerOnlyMaximum} is B_prod(p).  It is exact for the
     * producer-only model when the initial multiset is empty.
     * {@code safeBoundWithInitialTokens} is |K_p^0| + B_prod(p), hence it is a
     * safe but possibly loose bound for an arbitrary initial multiset.</p>
     */
    public static final class FormulaResult {
        private final long producerOnlyMaximum;
        private final long safeBoundWithInitialTokens;
        private final boolean exactForCurrentInitialMultiset;

        private FormulaResult(
                long producerOnlyMaximum,
                long safeBoundWithInitialTokens,
                boolean exactForCurrentInitialMultiset) {
            this.producerOnlyMaximum = producerOnlyMaximum;
            this.safeBoundWithInitialTokens = safeBoundWithInitialTokens;
            this.exactForCurrentInitialMultiset = exactForCurrentInitialMultiset;
        }

        public long getProducerOnlyMaximum() {
            return producerOnlyMaximum;
        }

        public long getSafeBoundWithInitialTokens() {
            return safeBoundWithInitialTokens;
        }

        public boolean isExactForCurrentInitialMultiset() {
            return exactForCurrentInitialMultiset;
        }
    }

    public static final class HorizonEstimate {
        private final HorizonMethod method;
        private final BigInteger uncappedSteps;
        private final long maxSteps;
        private final boolean capped;
        private final long timeScale;
        private final BigInteger producedPerCommonBlock;
        private final BigInteger potentiallyConsumedPerCommonBlock;

        private HorizonEstimate(
                HorizonMethod method,
                BigInteger uncappedSteps,
                long maxSteps,
                boolean capped,
                long timeScale,
                BigInteger producedPerCommonBlock,
                BigInteger potentiallyConsumedPerCommonBlock) {
            this.method = method;
            this.uncappedSteps = uncappedSteps;
            this.maxSteps = maxSteps;
            this.capped = capped;
            this.timeScale = timeScale;
            this.producedPerCommonBlock = producedPerCommonBlock;
            this.potentiallyConsumedPerCommonBlock = potentiallyConsumedPerCommonBlock;
        }

        public HorizonMethod getMethod() {
            return method;
        }

        public BigInteger getUncappedSteps() {
            return uncappedSteps;
        }

        public long getMaxSteps() {
            return maxSteps;
        }

        public boolean isCapped() {
            return capped;
        }

        public long getTimeScale() {
            return timeScale;
        }

        public BigInteger getProducedPerCommonBlock() {
            return producedPerCommonBlock;
        }

        public BigInteger getPotentiallyConsumedPerCommonBlock() {
            return potentiallyConsumedPerCommonBlock;
        }
    }

    public static final class CalculationResult {
        private final long maxTokens;
        private final long firstMaximumStep;
        private final long performedSteps;
        private final long timeScale;
        private final Mode mode;

        private CalculationResult(
                long maxTokens,
                long firstMaximumStep,
                long performedSteps,
                long timeScale,
                Mode mode) {
            this.maxTokens = maxTokens;
            this.firstMaximumStep = firstMaximumStep;
            this.performedSteps = performedSteps;
            this.timeScale = timeScale;
            this.mode = mode;
        }

        public long getMaxTokens() {
            return maxTokens;
        }

        public long getFirstMaximumStep() {
            return firstMaximumStep;
        }

        public long getPerformedSteps() {
            return performedSteps;
        }

        public long getTimeScale() {
            return timeScale;
        }

        public Mode getMode() {
            return mode;
        }

        public BigDecimal getFirstMaximumTime() {
            return BigDecimal.valueOf(firstMaximumStep)
                    .divide(BigDecimal.valueOf(timeScale), 12, RoundingMode.HALF_UP)
                    .stripTrailingZeros();
        }
    }

    /**
     * Uses the order of NORMAL output arcs returned by the place as the fixed
     * consumer order.
     */
    public static AnalysisModel prepare(PlaceXTPN place, Mode mode) {
        return prepare(place, mode, null);
    }

    /**
     * Prepares one immutable local model.
     *
     * @param explicitConsumerOrder optional permutation of all consumers
     *                              discovered through NORMAL output arcs;
     *                              null selects the arc-list order
     */
    public static AnalysisModel prepare(
            PlaceXTPN place,
            Mode mode,
            List<TransitionXTPN> explicitConsumerOrder) {

        if (place == null) {
            throw new IllegalArgumentException("place must not be null");
        }
        if (mode == null) {
            throw new IllegalArgumentException("mode must not be null");
        }
        if (!place.isGammaModeActive()) {
            throw new IllegalArgumentException(
                    "The analyzed place must have active gamma timing");
        }

        double gammaLValue = requireFiniteNonNegative(
                place.getGammaMinValue(), "gammaL of place " + place.getName());
        double gammaUValue = requireFiniteNonNegative(
                place.getGammaMaxValue(), "gammaU of place " + place.getName());
        if (!(gammaLValue < gammaUValue)) {
            throw new IllegalArgumentException(
                    "The analyzed place must satisfy gammaL < gammaU");
        }

        LinkedHashMap<TransitionXTPN, Integer> producerWeights =
                collectProducers(place);
        LinkedHashMap<TransitionXTPN, Integer> allConsumerWeights =
                collectConsumers(place);

        Set<TransitionXTPN> bothRoles = new HashSet<>(producerWeights.keySet());
        bothRoles.retainAll(allConsumerWeights.keySet());
        if (!bothRoles.isEmpty()) {
            throw new IllegalArgumentException(
                    "A transition connected to the analyzed place by NORMAL arcs "
                            + "in both directions is outside the supported local subnet");
        }

        LinkedHashMap<TransitionXTPN, Integer> consumerWeights =
                mode == Mode.WITH_CONSUMERS
                        ? allConsumerWeights
                        : new LinkedHashMap<>();

        if (mode == Mode.PRODUCERS_ONLY
                && explicitConsumerOrder != null
                && !explicitConsumerOrder.isEmpty()) {
            throw new IllegalArgumentException(
                    "Consumer order cannot be supplied in PRODUCERS_ONLY mode");
        }
        if (mode == Mode.WITH_CONSUMERS && explicitConsumerOrder != null) {
            consumerWeights = reorderConsumers(consumerWeights, explicitConsumerOrder);
        }

        ArrayList<Double> initialValues = place.copyMultiset();
        if (initialValues == null) {
            initialValues = new ArrayList<>();
        }

        List<Double> valuesForScale = new ArrayList<>();
        valuesForScale.add(gammaLValue);
        valuesForScale.add(gammaUValue);

        for (Double age : initialValues) {
            if (age == null) {
                throw new IllegalArgumentException(
                        "The initial token multiset contains a null age");
            }
            valuesForScale.add(requireFiniteNonNegative(
                    age, "initial token age in place " + place.getName()));
        }

        for (TransitionXTPN transition : producerWeights.keySet()) {
            validateTransitionTiming(transition);
            valuesForScale.add(activeAlphaLower(transition));
            valuesForScale.add(activeBetaLower(transition));
        }
        for (TransitionXTPN transition : consumerWeights.keySet()) {
            validateTransitionTiming(transition);
            valuesForScale.add(activeAlphaUpper(transition));
            valuesForScale.add(activeBetaUpper(transition));
        }

        TimeScaler scaler = TimeScaler.from(valuesForScale);
        long gammaL = scaler.scale(gammaLValue);
        long gammaU = scaler.scale(gammaUValue);

        List<Long> initialAges = new ArrayList<>(initialValues.size());
        for (Double ageValue : initialValues) {
            long age = scaler.scale(ageValue);
            if (age > gammaU) {
                throw new IllegalArgumentException(
                        "Initial token age exceeds gammaU in place " + place.getName());
            }
            initialAges.add(age);
        }
        initialAges.sort(Collections.reverseOrder());

        List<ProducerSpec> producers = new ArrayList<>();
        for (Map.Entry<TransitionXTPN, Integer> entry : producerWeights.entrySet()) {
            TransitionXTPN transition = entry.getKey();
            long alphaL = scaler.scale(activeAlphaLower(transition));
            long betaL = scaler.scale(activeBetaLower(transition));
            long period = addExact(alphaL, betaL, "producer period");
            if (period <= 0L) {
                throw new IllegalArgumentException(
                        "Producer " + transition.getName()
                                + " has fast(t)=0. At least one active phase must "
                                + "have a positive lower time bound.");
            }
            producers.add(new ProducerSpec(
                    transition, period, entry.getValue()));
        }

        List<ConsumerSpec> consumers = new ArrayList<>();
        for (Map.Entry<TransitionXTPN, Integer> entry : consumerWeights.entrySet()) {
            TransitionXTPN transition = entry.getKey();
            long alphaU = scaler.scale(activeAlphaUpper(transition));
            long betaU = scaler.scale(activeBetaUpper(transition));
            long slow = addExact(alphaU, betaU, "consumer cycle");
            if (slow <= 0L) {
                throw new IllegalArgumentException(
                        "Consumer " + transition.getName()
                                + " has alphaU+betaU=0. This would allow an "
                                + "unbounded zero-time loop.");
            }
            consumers.add(new ConsumerSpec(
                    transition, alphaU, betaU, entry.getValue()));
        }

        return new AnalysisModel(
                place,
                mode,
                scaler.getScale(),
                gammaL,
                gammaU,
                initialAges,
                producers,
                consumers);
    }

    /**
     * Fast K-bound calculation without running the simulation.  The returned
     * value is exact for the producer-only model when K_p^0 is empty and is
     * the safe value |K_p^0| + B_prod(p) otherwise.
     */
    public static long computeProducerOnlyKBound(PlaceXTPN place) {
        return computeProducerOnlyFormula(place)
                .getSafeBoundWithInitialTokens();
    }

    public static long computeProducerOnlyKBound(AnalysisModel model) {
        return computeProducerOnlyFormula(model)
                .getSafeBoundWithInitialTokens();
    }

    public static FormulaResult computeProducerOnlyFormula(PlaceXTPN place) {
        return computeProducerOnlyFormula(prepare(place, Mode.PRODUCERS_ONLY));
    }

    public static FormulaResult computeProducerOnlyFormula(AnalysisModel model) {
        requireModel(model);
        long bProd = producerOnlyMaximum(model);
        long bSafe = addExact(bProd, model.initialAges.size(), "safe producer-only bound");
        return new FormulaResult(bProd, bSafe, model.initialAges.isEmpty());
    }

    /**
     * Estimates maxSteps.  For PRODUCERS_ONLY this gives gammaU plus one
     * common producer period so that the simulation can be compared with the
     * direct formula.  For WITH_CONSUMERS it uses the two rate cases from the
     * article.  The estimate does not detect repeated configurations.
     */
    public static HorizonEstimate estimateMaxSteps(AnalysisModel model, HorizonOptions options) {
        requireModel(model);
        if (options == null) {
            throw new IllegalArgumentException("options must not be null");
        }

        if (model.producers.isEmpty()) {
            return createHorizon(
                    HorizonMethod.NO_PRODUCERS,
                    BigInteger.ZERO,
                    options,
                    model.timeScale,
                    BigInteger.ZERO,
                    BigInteger.ZERO);
        }

        if (model.mode == Mode.PRODUCERS_ONLY || model.consumers.isEmpty()) {
            BigInteger producerLcm = lcmOfProducerPeriods(model.producers);
            BigInteger raw = BigInteger.valueOf(model.gammaU).add(producerLcm);
            return createHorizon(
                    HorizonMethod.PRODUCER_PERIOD,
                    raw,
                    options,
                    model.timeScale,
                    BigInteger.ZERO,
                    BigInteger.ZERO);
        }

        List<Long> periods = new ArrayList<>();
        for (ProducerSpec producer : model.producers) {
            periods.add(producer.period);
        }
        for (ConsumerSpec consumer : model.consumers) {
            periods.add(consumer.slow());
        }

        BigInteger commonBlock = lcmOfPositiveLongs(periods);
        BigInteger produced = BigInteger.ZERO;
        for (ProducerSpec producer : model.producers) {
            BigInteger occurrences =
                    commonBlock.divide(BigInteger.valueOf(producer.period));
            produced = produced.add(
                    occurrences.multiply(BigInteger.valueOf(producer.weight)));
        }

        BigInteger potentiallyConsumed = BigInteger.ZERO;
        for (ConsumerSpec consumer : model.consumers) {
            BigInteger occurrences =
                    commonBlock.divide(BigInteger.valueOf(consumer.slow()));
            potentiallyConsumed = potentiallyConsumed.add(
                    occurrences.multiply(BigInteger.valueOf(consumer.weight)));
        }

        BigInteger difference = produced.subtract(potentiallyConsumed);
        BigInteger blocks = BigInteger.valueOf(options.extraBlocks);
        HorizonMethod method = HorizonMethod.CONSUMER_RATE_NON_POSITIVE;

        if (difference.signum() > 0) {
            BigInteger bProd = BigInteger.valueOf(producerOnlyMaximum(model));
            BigInteger neededBlocks = ceilDivide(bProd, difference);
            if (neededBlocks.compareTo(blocks) > 0) {
                blocks = neededBlocks;
            }
            method = HorizonMethod.CONSUMER_RATE_POSITIVE;
        }

        BigInteger raw = BigInteger.valueOf(model.gammaU)
                .add(commonBlock.multiply(blocks));

        return createHorizon(
                method,
                raw,
                options,
                model.timeScale,
                produced,
                potentiallyConsumed);
    }

    public static HorizonEstimate estimateMaxSteps(AnalysisModel model) {
        return estimateMaxSteps(model, HorizonOptions.defaults());
    }

    public static CalculationResult compute(
            PlaceXTPN place,
            Mode mode,
            long maxSteps) {
        return compute(prepare(place, mode), maxSteps);
    }

    /**
     * Runs exactly maxSteps positive unit-time updates and examines states at
     * steps 0,1,...,maxSteps.  No repeated-configuration stopping is used.
     */
    public static CalculationResult compute(
            AnalysisModel model,
            long maxSteps) {
        requireModel(model);
        if (maxSteps < 0L) {
            throw new IllegalArgumentException("maxSteps must not be negative");
        }

        ArrayList<Long> multiset = new ArrayList<>(model.initialAges);

        long[] producerTimers = new long[model.producers.size()];
        for (int i = 0; i < producerTimers.length; i++) {
            producerTimers[i] = model.producers.get(i).period;
        }

        ConsumerState[] consumerStates =
                new ConsumerState[model.consumers.size()];
        for (int i = 0; i < consumerStates.length; i++) {
            ConsumerSpec consumer = model.consumers.get(i);
            consumerStates[i] =
                    new ConsumerState(ConsumerPhase.WAITING, consumer.alphaU);
        }

        long maxTokens = multiset.size();
        long firstMaximumStep = 0L;
        long step = 0L;

        while (true) {
            // Producer completions at the current time.
            for (int i = 0; i < model.producers.size(); i++) {
                if (producerTimers[i] == 0L) {
                    ProducerSpec producer = model.producers.get(i);
                    appendZeroAges(multiset, producer.weight);
                    producerTimers[i] = producer.period;
                }
            }

            // The article records occupancy after producer completions and
            // before any consumer starts production at the same time.
            if ((long) multiset.size() > maxTokens) {
                maxTokens = multiset.size();
                firstMaximumStep = step;
            }

            // Fixed consumer order.
            for (int i = 0; i < model.consumers.size(); i++) {
                ConsumerSpec consumer = model.consumers.get(i);
                ConsumerState state = consumerStates[i];

                if (state.phase == ConsumerPhase.PRODUCING
                        && state.timer == 0L) {
                    state.phase = ConsumerPhase.WAITING;
                    state.timer = consumer.alphaU;
                }

                if (state.phase == ConsumerPhase.WAITING) {
                    boolean active = hasActivatingSubset(
                            multiset, model.gammaL, consumer.weight);

                    if (!active) {
                        state.timer = consumer.alphaU;
                    } else if (state.timer == 0L) {
                        consumeOldest(multiset, consumer.weight);
                        state.phase = ConsumerPhase.PRODUCING;
                        state.timer = consumer.betaU;

                        if (state.timer == 0L) {
                            // Zero production time: completion is immediate.
                            state.phase = ConsumerPhase.WAITING;
                            state.timer = consumer.alphaU;
                        }
                    }
                }
            }

            if (step == maxSteps) {
                break;
            }

            boolean[] activeBefore = new boolean[model.consumers.size()];
            for (int i = 0; i < model.consumers.size(); i++) {
                ConsumerState state = consumerStates[i];
                if (state.phase == ConsumerPhase.WAITING) {
                    ConsumerSpec consumer = model.consumers.get(i);
                    activeBefore[i] = hasActivatingSubset(
                            multiset, model.gammaL, consumer.weight);
                }
            }

            updateMultiset(multiset, model.gammaU);

            for (int i = 0; i < producerTimers.length; i++) {
                if (producerTimers[i] > 0L) {
                    producerTimers[i]--;
                }
            }

            for (int i = 0; i < model.consumers.size(); i++) {
                ConsumerSpec consumer = model.consumers.get(i);
                ConsumerState state = consumerStates[i];

                if (state.phase == ConsumerPhase.PRODUCING) {
                    if (state.timer > 0L) {
                        state.timer--;
                    }
                } else {
                    boolean activeAfter = hasActivatingSubset(
                            multiset, model.gammaL, consumer.weight);
                    if (activeBefore[i] && activeAfter) {
                        if (state.timer > 0L) {
                            state.timer--;
                        }
                    } else {
                        state.timer = consumer.alphaU;
                    }
                }
            }

            step++;
        }

        return new CalculationResult(
                maxTokens,
                firstMaximumStep,
                maxSteps,
                model.timeScale,
                model.mode);
    }

    private static LinkedHashMap<TransitionXTPN, Integer> collectProducers(
            PlaceXTPN place) {
        LinkedHashMap<TransitionXTPN, Integer> result = new LinkedHashMap<>();
        List<Arc> arcs = place.getInputArcs();
        if (arcs == null) {
            return result;
        }

        for (Arc arc : arcs) {
            if (arc == null || arc.getArcType() != Arc.TypeOfArc.NORMAL) {
                continue;
            }
            Object node = arc.getStartNode();
            if (!(node instanceof TransitionXTPN)) {
                throw new IllegalArgumentException(
                        "A NORMAL input arc of the analyzed xTPN place does "
                                + "not start in TransitionXTPN");
            }
            TransitionXTPN transition = (TransitionXTPN) node;
            if (transition.isKnockedOut()) {
                continue;
            }
            mergeWeight(result, transition, arc.getWeight());
        }
        return result;
    }

    private static LinkedHashMap<TransitionXTPN, Integer> collectConsumers(
            PlaceXTPN place) {
        LinkedHashMap<TransitionXTPN, Integer> result = new LinkedHashMap<>();
        List<Arc> arcs = place.getOutputArcs();
        if (arcs == null) {
            return result;
        }

        for (Arc arc : arcs) {
            if (arc == null || arc.getArcType() != Arc.TypeOfArc.NORMAL) {
                continue;
            }
            Object node = arc.getEndNode();
            if (!(node instanceof TransitionXTPN)) {
                throw new IllegalArgumentException(
                        "A NORMAL output arc of the analyzed xTPN place does "
                                + "not end in TransitionXTPN");
            }
            TransitionXTPN transition = (TransitionXTPN) node;
            if (transition.isKnockedOut()) {
                continue;
            }
            mergeWeight(result, transition, arc.getWeight());
        }
        return result;
    }

    private static void mergeWeight(
            LinkedHashMap<TransitionXTPN, Integer> map,
            TransitionXTPN transition,
            int weight) {
        if (weight <= 0) {
            throw new IllegalArgumentException(
                    "NORMAL arc weight must be positive");
        }
        Integer old = map.get(transition);
        if (old == null) {
            map.put(transition, weight);
        } else {
            try {
                map.put(transition, Math.addExact(old, weight));
            } catch (ArithmeticException exception) {
                throw new IllegalArgumentException(
                        "Combined arc weight exceeds int range", exception);
            }
        }
    }

    private static LinkedHashMap<TransitionXTPN, Integer> reorderConsumers(
            LinkedHashMap<TransitionXTPN, Integer> discovered,
            List<TransitionXTPN> order) {
        LinkedHashMap<TransitionXTPN, Integer> reordered = new LinkedHashMap<>();
        Set<TransitionXTPN> seen = new HashSet<>();

        for (TransitionXTPN transition : order) {
            if (transition == null
                    || !discovered.containsKey(transition)
                    || !seen.add(transition)) {
                throw new IllegalArgumentException(
                        "Explicit consumer order must contain every discovered "
                                + "consumer exactly once");
            }
            reordered.put(transition, discovered.get(transition));
        }

        if (reordered.size() != discovered.size()) {
            throw new IllegalArgumentException(
                    "Explicit consumer order must contain every discovered "
                            + "consumer exactly once");
        }
        return reordered;
    }

    private static void validateTransitionTiming(TransitionXTPN transition) {
        if (transition.isMassActionKineticsActiveXTPN()) {
            throw new IllegalArgumentException(
                    "Transition " + transition.getName()
                            + " uses mass-action kinetics, which is outside "
                            + "the endpoint-time scenario of this algorithm");
        }
        if (transition.isImmediateXTPN()) {
            throw new IllegalArgumentException(
                    "Immediate transition " + transition.getName()
                            + " is outside the supported endpoint-time scenario");
        }

        boolean alpha = transition.isAlphaModeActive();
        boolean beta = transition.isBetaModeActive();

        if (!alpha && !beta) {
            throw new IllegalArgumentException(
                    "Transition " + transition.getName()
                            + " has both alpha and beta modes disabled");
        }

        if (alpha) {
            double lower = requireFiniteNonNegative(
                    transition.getAlphaMinValue(),
                    "alphaL of transition " + transition.getName());
            double upper = requireFiniteNonNegative(
                    transition.getAlphaMaxValue(),
                    "alphaU of transition " + transition.getName());
            if (lower > upper) {
                throw new IllegalArgumentException(
                        "Transition " + transition.getName()
                                + " has alphaL > alphaU");
            }
        }
        if (beta) {
            double lower = requireFiniteNonNegative(
                    transition.getBetaMinValue(),
                    "betaL of transition " + transition.getName());
            double upper = requireFiniteNonNegative(
                    transition.getBetaMaxValue(),
                    "betaU of transition " + transition.getName());
            if (lower > upper) {
                throw new IllegalArgumentException(
                        "Transition " + transition.getName()
                                + " has betaL > betaU");
            }
        }
    }

    private static double activeAlphaLower(TransitionXTPN transition) {
        return transition.isAlphaModeActive()
                ? requireFiniteNonNegative(
                transition.getAlphaMinValue(),
                "alphaL of transition " + transition.getName())
                : 0.0;
    }

    private static double activeBetaLower(TransitionXTPN transition) {
        return transition.isBetaModeActive()
                ? requireFiniteNonNegative(
                transition.getBetaMinValue(),
                "betaL of transition " + transition.getName())
                : 0.0;
    }

    private static double activeAlphaUpper(TransitionXTPN transition) {
        return transition.isAlphaModeActive()
                ? requireFiniteNonNegative(
                transition.getAlphaMaxValue(),
                "alphaU of transition " + transition.getName())
                : 0.0;
    }

    private static double activeBetaUpper(TransitionXTPN transition) {
        return transition.isBetaModeActive()
                ? requireFiniteNonNegative(
                transition.getBetaMaxValue(),
                "betaU of transition " + transition.getName())
                : 0.0;
    }

    private static double requireFiniteNonNegative(
            double value,
            String description) {
        if (!Double.isFinite(value) || value < 0.0) {
            throw new IllegalArgumentException(
                    description + " must be a finite non-negative number");
        }
        return value;
    }

    private static long producerOnlyMaximum(AnalysisModel model) {
        long result = 0L;
        for (ProducerSpec producer : model.producers) {
            long occurrences = model.gammaU / producer.period + 1L;
            long contribution;
            try {
                contribution = Math.multiplyExact(
                        occurrences, (long) producer.weight);
                result = Math.addExact(result, contribution);
            } catch (ArithmeticException exception) {
                throw new ArithmeticException(
                        "Producer-only token bound exceeds long range");
            }
        }
        return result;
    }

    private static HorizonEstimate createHorizon(
            HorizonMethod method,
            BigInteger raw,
            HorizonOptions options,
            long timeScale,
            BigInteger produced,
            BigInteger consumed) {
        BigInteger cap = BigInteger.valueOf(options.maxStepsCap);
        boolean capped = raw.compareTo(cap) > 0;
        BigInteger used = capped ? cap : raw;

        if (used.signum() < 0 || used.compareTo(
                BigInteger.valueOf(Long.MAX_VALUE)) > 0) {
            throw new ArithmeticException(
                    "Selected maxSteps does not fit in long");
        }

        return new HorizonEstimate(
                method,
                raw,
                used.longValueExact(),
                capped,
                timeScale,
                produced,
                consumed);
    }

    private static BigInteger lcmOfProducerPeriods(
            List<ProducerSpec> producers) {
        List<Long> values = new ArrayList<>();
        for (ProducerSpec producer : producers) {
            values.add(producer.period);
        }
        return lcmOfPositiveLongs(values);
    }

    private static BigInteger lcmOfPositiveLongs(List<Long> values) {
        if (values.isEmpty()) {
            return BigInteger.ZERO;
        }
        BigInteger lcm = BigInteger.ONE;
        for (Long value : values) {
            if (value == null || value <= 0L) {
                throw new IllegalArgumentException(
                        "LCM values must be positive");
            }
            BigInteger current = BigInteger.valueOf(value);
            lcm = lcm.divide(lcm.gcd(current)).multiply(current);
        }
        return lcm;
    }

    private static BigInteger ceilDivide(
            BigInteger numerator,
            BigInteger denominator) {
        if (numerator.signum() < 0 || denominator.signum() <= 0) {
            throw new IllegalArgumentException(
                    "ceilDivide requires a non-negative numerator and "
                            + "a positive denominator");
        }
        if (numerator.signum() == 0) {
            return BigInteger.ZERO;
        }
        return numerator.add(denominator).subtract(BigInteger.ONE)
                .divide(denominator);
    }

    private static boolean hasActivatingSubset(
            List<Long> multiset,
            long gammaL,
            int need) {
        return need > 0
                && multiset.size() >= need
                && multiset.get(need - 1) >= gammaL;
    }

    private static void consumeOldest(
            ArrayList<Long> multiset,
            int count) {
        if (count <= 0 || multiset.size() < count) {
            throw new IllegalStateException(
                    "Cannot consume the requested number of tokens");
        }
        multiset.subList(0, count).clear();
    }

    private static void appendZeroAges(
            ArrayList<Long> multiset,
            int count) {
        if (count < 0 || count > Integer.MAX_VALUE - multiset.size()) {
            throw new IllegalStateException(
                    "Token multiset would exceed ArrayList capacity");
        }
        for (int i = 0; i < count; i++) {
            multiset.add(0L);
        }
    }

    /**
     * Increases every age by one and removes only ages greater than gammaU.
     * A token whose new age is exactly gammaU remains present.
     */
    private static void updateMultiset(
            ArrayList<Long> multiset,
            long gammaU) {
        int expiredPrefix = 0;
        while (expiredPrefix < multiset.size()) {
            long age = multiset.get(expiredPrefix);
            if (age == Long.MAX_VALUE || age + 1L > gammaU) {
                expiredPrefix++;
            } else {
                break;
            }
        }
        if (expiredPrefix > 0) {
            multiset.subList(0, expiredPrefix).clear();
        }
        for (int i = 0; i < multiset.size(); i++) {
            multiset.set(i, multiset.get(i) + 1L);
        }
    }

    private static long addExact(long a, long b, String description) {
        try {
            return Math.addExact(a, b);
        } catch (ArithmeticException exception) {
            throw new ArithmeticException(description + " exceeds long range");
        }
    }

    private static void requireModel(AnalysisModel model) {
        if (model == null) {
            throw new IllegalArgumentException("model must not be null");
        }
    }

    private static final class ProducerSpec {
        private final TransitionXTPN transition;
        private final long period;
        private final int weight;

        private ProducerSpec(
                TransitionXTPN transition,
                long period,
                int weight) {
            this.transition = transition;
            this.period = period;
            this.weight = weight;
        }
    }

    private static final class ConsumerSpec {
        private final TransitionXTPN transition;
        private final long alphaU;
        private final long betaU;
        private final int weight;

        private ConsumerSpec(
                TransitionXTPN transition,
                long alphaU,
                long betaU,
                int weight) {
            this.transition = transition;
            this.alphaU = alphaU;
            this.betaU = betaU;
            this.weight = weight;
        }

        private long slow() {
            return addExact(alphaU, betaU, "consumer cycle");
        }
    }

    private enum ConsumerPhase {
        WAITING,
        PRODUCING
    }

    private static final class ConsumerState {
        private ConsumerPhase phase;
        private long timer;

        private ConsumerState(ConsumerPhase phase, long timer) {
            this.phase = phase;
            this.timer = timer;
        }
    }

    private static final class Rational {
        private final BigInteger numerator;
        private final BigInteger denominator;

        private Rational(BigInteger numerator, BigInteger denominator) {
            this.numerator = numerator;
            this.denominator = denominator;
        }

        private static Rational from(double value) {
            BigDecimal decimal = BigDecimal.valueOf(value)
                    .stripTrailingZeros();
            BigInteger numerator = decimal.unscaledValue();
            int decimalScale = decimal.scale();
            BigInteger denominator;

            if (decimalScale < 0) {
                numerator = numerator.multiply(
                        BigInteger.TEN.pow(-decimalScale));
                denominator = BigInteger.ONE;
            } else {
                denominator = BigInteger.TEN.pow(decimalScale);
            }

            BigInteger gcd = numerator.abs().gcd(denominator);
            if (gcd.signum() != 0) {
                numerator = numerator.divide(gcd);
                denominator = denominator.divide(gcd);
            }
            return new Rational(numerator, denominator);
        }
    }

    /**
     * Exact common scaling for the finite decimal values stored by Holmes.
     * Denominators are reduced before their least common multiple is taken.
     */
    private static final class TimeScaler {
        private final BigInteger scale;

        private TimeScaler(BigInteger scale) {
            this.scale = scale;
        }

        private static TimeScaler from(List<Double> values) {
            BigInteger common = BigInteger.ONE;
            for (Double value : values) {
                if (value == null) {
                    throw new IllegalArgumentException(
                            "Time value must not be null");
                }
                Rational rational = Rational.from(value);
                common = common.divide(common.gcd(rational.denominator))
                        .multiply(rational.denominator);
            }
            if (common.compareTo(
                    BigInteger.valueOf(Long.MAX_VALUE)) > 0) {
                throw new ArithmeticException(
                        "Common time scale exceeds long range");
            }
            return new TimeScaler(common);
        }

        private long getScale() {
            return scale.longValueExact();
        }

        private long scale(double value) {
            Rational rational = Rational.from(value);
            BigInteger[] division =
                    scale.divideAndRemainder(rational.denominator);
            if (division[1].signum() != 0) {
                throw new IllegalStateException(
                        "Internal error: value does not fit common time scale");
            }
            BigInteger result =
                    rational.numerator.multiply(division[0]);
            if (result.signum() < 0
                    || result.compareTo(
                    BigInteger.valueOf(Long.MAX_VALUE)) > 0) {
                throw new ArithmeticException(
                        "Scaled time value exceeds long range");
            }
            return result.longValueExact();
        }
    }
}
