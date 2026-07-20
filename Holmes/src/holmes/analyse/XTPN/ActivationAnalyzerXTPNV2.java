package holmes.analyse.XTPN;

import holmes.petrinet.elements.Arc;
import holmes.petrinet.elements.PlaceXTPN;
import holmes.petrinet.elements.TransitionXTPN;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Local analysis of the longest continuous activation time of one xTPN
 * transition.
 *
 * <p>The target transition is observed but never starts production. Producers
 * are assumed to be continuously active. Competitors are checked only in the
 * target pre-places and consume the oldest mature tokens in a fixed order.</p>
 *
 * <p>No repeated-configuration detection is performed. The simulation stops
 * only after the supplied number of scaled time steps or when the complete
 * activation interval of the target has been observed.</p>
 */
public final class ActivationAnalyzerXTPNV2 {
    private static final int DEFAULT_EXTRA_BLOCKS = 3;
    private static final long DEFAULT_MAX_STEPS_CAP = 100_000L;

    private ActivationAnalyzerXTPNV2() {
    }

    /**
     * FAVORABLE_TO_TARGET is the scenario used in the article:
     * producer lower bounds and competitor upper bounds.
     *
     * OPPOSITE_ENDPOINTS is retained for experiments, but it is not guaranteed
     * to be a true worst case because token aging can change synchronization.
     */
    public enum TimingScenario {
        FAVORABLE_TO_TARGET,
        OPPOSITE_ENDPOINTS
    }

    public enum HorizonStrategy {
        BASE,
        PLACE_AWARE
    }

    public enum ActivationStatus {
        NEVER_ACTIVE,
        BELOW_ALPHA_L,
        PARTIAL_WINDOW,
        FULL_WINDOW
    }

    public static final class HorizonOptions {
        private final HorizonStrategy strategy;
        private final int extraBlocks;
        private final long maxStepsCap;

        public HorizonOptions(
                HorizonStrategy strategy,
                int extraBlocks,
                long maxStepsCap) {
            if (strategy == null) {
                throw new IllegalArgumentException("strategy must not be null");
            }
            if (extraBlocks <= 0) {
                throw new IllegalArgumentException(
                        "extraBlocks must be positive");
            }
            if (maxStepsCap < 0L) {
                throw new IllegalArgumentException(
                        "maxStepsCap must not be negative");
            }
            this.strategy = strategy;
            this.extraBlocks = extraBlocks;
            this.maxStepsCap = maxStepsCap;
        }

        public static HorizonOptions defaults() {
            return new HorizonOptions(
                    HorizonStrategy.PLACE_AWARE,
                    DEFAULT_EXTRA_BLOCKS,
                    DEFAULT_MAX_STEPS_CAP);
        }

        public static HorizonOptions baseDefaults() {
            return new HorizonOptions(
                    HorizonStrategy.BASE,
                    DEFAULT_EXTRA_BLOCKS,
                    DEFAULT_MAX_STEPS_CAP);
        }

        public static HorizonOptions withoutCap(
                HorizonStrategy strategy,
                int extraBlocks) {
            return new HorizonOptions(
                    strategy,
                    extraBlocks,
                    Long.MAX_VALUE);
        }

        public HorizonStrategy getStrategy() {
            return strategy;
        }

        public int getExtraBlocks() {
            return extraBlocks;
        }

        public long getMaxStepsCap() {
            return maxStepsCap;
        }
    }

    /**
     * Immutable data extracted from the Holmes net.
     */
    public static final class AnalysisModel {
        private final TransitionXTPN target;
        private final TimingScenario timingScenario;
        private final boolean competitorsIncluded;
        private final long timeScale;
        private final long targetAlphaL;
        private final long targetAlphaU;
        private final List<PrePlaceSpec> prePlaces;
        private final List<ProducerSpec> producers;
        private final List<CompetitorSpec> competitors;

        private AnalysisModel(
                TransitionXTPN target,
                TimingScenario timingScenario,
                boolean competitorsIncluded,
                long timeScale,
                long targetAlphaL,
                long targetAlphaU,
                List<PrePlaceSpec> prePlaces,
                List<ProducerSpec> producers,
                List<CompetitorSpec> competitors) {
            this.target = target;
            this.timingScenario = timingScenario;
            this.competitorsIncluded = competitorsIncluded;
            this.timeScale = timeScale;
            this.targetAlphaL = targetAlphaL;
            this.targetAlphaU = targetAlphaU;
            this.prePlaces = List.copyOf(prePlaces);
            this.producers = List.copyOf(producers);
            this.competitors = List.copyOf(competitors);
        }

        public TransitionXTPN getTarget() {
            return target;
        }

        public TimingScenario getTimingScenario() {
            return timingScenario;
        }

        public boolean areCompetitorsIncluded() {
            return competitorsIncluded;
        }

        /**
         * One original time unit is represented by timeScale iterations.
         */
        public long getTimeScale() {
            return timeScale;
        }

        public long getScaledAlphaL() {
            return targetAlphaL;
        }

        public long getScaledAlphaU() {
            return targetAlphaU;
        }

        public BigDecimal getAlphaL() {
            return unscale(targetAlphaL, timeScale);
        }

        public BigDecimal getAlphaU() {
            return unscale(targetAlphaU, timeScale);
        }

        public int getPrePlaceCount() {
            return prePlaces.size();
        }

        public int getProducerCount() {
            return producers.size();
        }

        public int getCompetitorCount() {
            return competitors.size();
        }

        public List<PlaceXTPN> getPrePlaces() {
            List<PlaceXTPN> result = new ArrayList<>();
            for (PrePlaceSpec place : prePlaces) {
                result.add(place.place);
            }
            return List.copyOf(result);
        }

        public List<TransitionXTPN> getProducers() {
            List<TransitionXTPN> result = new ArrayList<>();
            for (ProducerSpec producer : producers) {
                result.add(producer.transition);
            }
            return List.copyOf(result);
        }

        /**
         * Returns the fixed order used by this model.
         */
        public List<TransitionXTPN> getCompetitorOrder() {
            List<TransitionXTPN> result = new ArrayList<>();
            for (CompetitorSpec competitor : competitors) {
                result.add(competitor.transition);
            }
            return List.copyOf(result);
        }
    }

    public static final class HorizonEstimate {
        private final HorizonStrategy strategy;
        private final BigInteger baseObservationSteps;
        private final BigInteger selectedObservationSteps;
        private final BigInteger alphaUTail;
        private final BigInteger uncappedSteps;
        private final long maxSteps;
        private final boolean capped;
        private final long timeScale;

        private HorizonEstimate(
                HorizonStrategy strategy,
                BigInteger baseObservationSteps,
                BigInteger selectedObservationSteps,
                BigInteger alphaUTail,
                BigInteger uncappedSteps,
                long maxSteps,
                boolean capped,
                long timeScale) {
            this.strategy = strategy;
            this.baseObservationSteps = baseObservationSteps;
            this.selectedObservationSteps = selectedObservationSteps;
            this.alphaUTail = alphaUTail;
            this.uncappedSteps = uncappedSteps;
            this.maxSteps = maxSteps;
            this.capped = capped;
            this.timeScale = timeScale;
        }

        public HorizonStrategy getStrategy() {
            return strategy;
        }

        /**
         * Initial-token part plus the selected number of common time blocks,
         * before the final alphaU observation margin is added.
         */
        public BigInteger getBaseObservationSteps() {
            return baseObservationSteps;
        }

        public BigInteger getSelectedObservationSteps() {
            return selectedObservationSteps;
        }

        public BigInteger getAlphaUTail() {
            return alphaUTail;
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

        public BigDecimal getMaxTime() {
            return unscale(maxSteps, timeScale);
        }
    }

    public static final class CalculationResult {
        private final long maximumActivationSteps;
        private final boolean everActive;
        private final long firstMaximumStep;
        private final long requestedMaxSteps;
        private final long performedSteps;
        private final boolean fullWindowReached;
        private final long alphaL;
        private final long alphaU;
        private final long timeScale;

        private CalculationResult(
                long maximumActivationSteps,
                boolean everActive,
                long firstMaximumStep,
                long requestedMaxSteps,
                long performedSteps,
                boolean fullWindowReached,
                long alphaL,
                long alphaU,
                long timeScale) {
            this.maximumActivationSteps = maximumActivationSteps;
            this.everActive = everActive;
            this.firstMaximumStep = firstMaximumStep;
            this.requestedMaxSteps = requestedMaxSteps;
            this.performedSteps = performedSteps;
            this.fullWindowReached = fullWindowReached;
            this.alphaL = alphaL;
            this.alphaU = alphaU;
            this.timeScale = timeScale;
        }

        public long getMaximumActivationSteps() {
            return maximumActivationSteps;
        }

        public BigDecimal getMaximumActivationTime() {
            return unscale(maximumActivationSteps, timeScale);
        }

        public boolean wasEverActive() {
            return everActive;
        }

        /**
         * First simulation step at which the reported maximum was observed.
         * Returns -1 if the target was never active.
         */
        public long getFirstMaximumStep() {
            return firstMaximumStep;
        }

        public BigDecimal getFirstMaximumTime() {
            if (firstMaximumStep < 0L) {
                return null;
            }
            return unscale(firstMaximumStep, timeScale);
        }

        public long getRequestedMaxSteps() {
            return requestedMaxSteps;
        }

        public long getPerformedSteps() {
            return performedSteps;
        }

        public boolean isFullWindowReached() {
            return fullWindowReached;
        }

        public boolean isIterationLimitReached() {
            return !fullWindowReached && performedSteps == requestedMaxSteps;
        }

        public long getTimeScale() {
            return timeScale;
        }

        public BigDecimal getAlphaL() {
            return unscale(alphaL, timeScale);
        }

        public BigDecimal getAlphaU() {
            return unscale(alphaU, timeScale);
        }

        public ActivationStatus getStatus() {
            if (!everActive) {
                return ActivationStatus.NEVER_ACTIVE;
            }
            if (maximumActivationSteps >= alphaU) {
                return ActivationStatus.FULL_WINDOW;
            }
            if (maximumActivationSteps >= alphaL) {
                return ActivationStatus.PARTIAL_WINDOW;
            }
            return ActivationStatus.BELOW_ALPHA_L;
        }
    }

    public static AnalysisModel prepare(
            TransitionXTPN target,
            TimingScenario timingScenario,
            boolean includeCompetitors) {
        return prepare(
                target,
                timingScenario,
                includeCompetitors,
                null);
    }

    /**
     * Prepares one local model. If explicitCompetitorOrder is null, competitors
     * are kept in their discovery order: target pre-place order followed by arc
     * order in each place.
     */
    public static AnalysisModel prepare(
            TransitionXTPN target,
            TimingScenario timingScenario,
            boolean includeCompetitors,
            List<TransitionXTPN> explicitCompetitorOrder) {

        if (target == null) {
            throw new IllegalArgumentException("target must not be null");
        }
        if (timingScenario == null) {
            throw new IllegalArgumentException(
                    "timingScenario must not be null");
        }
        if (target.isKnockedOut()) {
            throw new IllegalArgumentException(
                    "The target transition is knocked out");
        }
        validateTransitionTiming(target);
        if (!target.isAlphaModeActive()) {
            throw new IllegalArgumentException(
                    "The target transition must have active alpha timing");
        }

        double targetAlphaLValue = requireFiniteNonNegative(
                activeAlphaLower(target),
                "alphaL of target " + target.getName());
        double targetAlphaUValue = requireFiniteNonNegative(
                activeAlphaUpper(target),
                "alphaU of target " + target.getName());

        LinkedHashMap<PlaceXTPN, Integer> targetNeeds =
                collectTargetPrePlaces(target);

        if (targetNeeds.isEmpty()) {
            if (explicitCompetitorOrder != null
                    && !explicitCompetitorOrder.isEmpty()) {
                throw new IllegalArgumentException(
                        "A source target transition has no competitors in this analysis");
            }
            TimeScaler scaler = TimeScaler.from(
                    List.of(targetAlphaLValue, targetAlphaUValue));
            return new AnalysisModel(
                    target,
                    timingScenario,
                    includeCompetitors,
                    scaler.getScale(),
                    scaler.scale(targetAlphaLValue),
                    scaler.scale(targetAlphaUValue),
                    List.of(),
                    List.of(),
                    List.of());
        }

        List<PlaceXTPN> placeOrder =
                new ArrayList<>(targetNeeds.keySet());
        LinkedHashMap<PlaceXTPN, Integer> placeIndices =
                new LinkedHashMap<>();
        for (int i = 0; i < placeOrder.size(); i++) {
            placeIndices.put(placeOrder.get(i), i);
        }

        validateNoTargetReturnArcs(target, placeIndices.keySet());

        LinkedHashMap<TransitionXTPN, LinkedHashMap<Integer, Integer>>
                structuralCompetitorInputs =
                collectCompetitorInputs(target, placeOrder);

        LinkedHashMap<TransitionXTPN, LinkedHashMap<Integer, Integer>>
                allProducerOutputs =
                collectAllProducerOutputs(target, placeOrder);

        validateNoOppositeNormalArcForSamePair(
                structuralCompetitorInputs,
                allProducerOutputs);

        LinkedHashMap<TransitionXTPN, LinkedHashMap<Integer, Integer>>
                producerOutputs = new LinkedHashMap<>();
        for (Map.Entry<TransitionXTPN, LinkedHashMap<Integer, Integer>> entry
                : allProducerOutputs.entrySet()) {
            if (!structuralCompetitorInputs.containsKey(entry.getKey())) {
                producerOutputs.put(entry.getKey(), entry.getValue());
            }
        }

        LinkedHashMap<TransitionXTPN, LinkedHashMap<Integer, Integer>>
                competitorInputs;
        if (includeCompetitors) {
            competitorInputs = structuralCompetitorInputs;
            if (explicitCompetitorOrder != null) {
                competitorInputs = reorderCompetitors(
                        competitorInputs,
                        explicitCompetitorOrder);
            }
        } else {
            if (explicitCompetitorOrder != null
                    && !explicitCompetitorOrder.isEmpty()) {
                throw new IllegalArgumentException(
                        "Competitor order cannot be supplied when competitors "
                                + "are not included");
            }
            competitorInputs = new LinkedHashMap<>();
        }

        List<Double> valuesForScale = new ArrayList<>();
        valuesForScale.add(targetAlphaLValue);
        valuesForScale.add(targetAlphaUValue);

        List<ArrayList<Double>> initialValuesByPlace =
                new ArrayList<>(placeOrder.size());
        for (PlaceXTPN place : placeOrder) {
            if (!place.isGammaModeActive()) {
                throw new IllegalArgumentException(
                        "Target pre-place " + place.getName()
                                + " must have active gamma timing");
            }
            double gammaL = requireFiniteNonNegative(
                    place.getGammaMinValue(),
                    "gammaL of place " + place.getName());
            double gammaU = requireFiniteNonNegative(
                    place.getGammaMaxValue(),
                    "gammaU of place " + place.getName());
            if (!(gammaL < gammaU)) {
                throw new IllegalArgumentException(
                        "Place " + place.getName()
                                + " must satisfy gammaL < gammaU");
            }
            valuesForScale.add(gammaL);
            valuesForScale.add(gammaU);

            ArrayList<Double> initial = place.copyMultiset();
            if (initial == null) {
                initial = new ArrayList<>();
            }
            for (Double age : initial) {
                if (age == null) {
                    throw new IllegalArgumentException(
                            "Initial token multiset of " + place.getName()
                                    + " contains a null age");
                }
                double checked = requireFiniteNonNegative(
                        age,
                        "initial token age in place " + place.getName());
                if (checked > gammaU) {
                    throw new IllegalArgumentException(
                            "Initial token age exceeds gammaU in place "
                                    + place.getName());
                }
                valuesForScale.add(checked);
            }
            initialValuesByPlace.add(initial);
        }

        for (TransitionXTPN transition : producerOutputs.keySet()) {
            validateTransitionTiming(transition);
            valuesForScale.add(requireFiniteNonNegative(
                    selectedProducerAlpha(transition, timingScenario),
                    "selected alpha time of producer "
                            + transition.getName()));
            valuesForScale.add(requireFiniteNonNegative(
                    selectedProducerBeta(transition, timingScenario),
                    "selected beta time of producer "
                            + transition.getName()));
        }
        for (TransitionXTPN transition : competitorInputs.keySet()) {
            validateTransitionTiming(transition);
            valuesForScale.add(requireFiniteNonNegative(
                    selectedCompetitorAlpha(transition, timingScenario),
                    "selected alpha time of competitor "
                            + transition.getName()));
            valuesForScale.add(requireFiniteNonNegative(
                    selectedCompetitorBeta(transition, timingScenario),
                    "selected beta time of competitor "
                            + transition.getName()));
        }

        TimeScaler scaler = TimeScaler.from(valuesForScale);
        long targetAlphaL = scaler.scale(targetAlphaLValue);
        long targetAlphaU = scaler.scale(targetAlphaUValue);

        List<PrePlaceSpec> prePlaces = new ArrayList<>();
        for (int i = 0; i < placeOrder.size(); i++) {
            PlaceXTPN place = placeOrder.get(i);
            long gammaL = scaler.scale(place.getGammaMinValue());
            long gammaU = scaler.scale(place.getGammaMaxValue());

            List<Long> ages = new ArrayList<>();
            for (Double age : initialValuesByPlace.get(i)) {
                ages.add(scaler.scale(age));
            }
            ages.sort(Collections.reverseOrder());

            prePlaces.add(new PrePlaceSpec(
                    place,
                    gammaL,
                    gammaU,
                    targetNeeds.get(place),
                    ages));
        }

        List<ProducerSpec> producers = new ArrayList<>();
        for (Map.Entry<TransitionXTPN, LinkedHashMap<Integer, Integer>> entry
                : producerOutputs.entrySet()) {
            TransitionXTPN transition = entry.getKey();
            long alpha = scaler.scale(
                    selectedProducerAlpha(transition, timingScenario));
            long beta = scaler.scale(
                    selectedProducerBeta(transition, timingScenario));
            long period = addExact(alpha, beta, "producer period");
            if (period <= 0L) {
                throw new IllegalArgumentException(
                        "Producer " + transition.getName()
                                + " has a zero selected cycle. This would allow "
                                + "an unbounded zero-time production loop.");
            }

            List<ProducerOutput> outputs = new ArrayList<>();
            for (Map.Entry<Integer, Integer> output
                    : entry.getValue().entrySet()) {
                outputs.add(new ProducerOutput(
                        output.getKey(),
                        output.getValue()));
            }
            producers.add(new ProducerSpec(
                    transition,
                    period,
                    outputs));
        }

        List<CompetitorSpec> competitors = new ArrayList<>();
        for (Map.Entry<TransitionXTPN, LinkedHashMap<Integer, Integer>> entry
                : competitorInputs.entrySet()) {
            TransitionXTPN transition = entry.getKey();
            long alpha = scaler.scale(
                    selectedCompetitorAlpha(transition, timingScenario));
            long beta = scaler.scale(
                    selectedCompetitorBeta(transition, timingScenario));
            long cycle = addExact(alpha, beta, "competitor cycle");
            if (cycle <= 0L) {
                throw new IllegalArgumentException(
                        "Competitor " + transition.getName()
                                + " has a zero selected cycle. This would allow "
                                + "an unbounded zero-time loop.");
            }

            List<CompetitorInput> inputs = new ArrayList<>();
            for (Map.Entry<Integer, Integer> input
                    : entry.getValue().entrySet()) {
                inputs.add(new CompetitorInput(
                        input.getKey(),
                        input.getValue()));
            }
            competitors.add(new CompetitorSpec(
                    transition,
                    alpha,
                    beta,
                    inputs));
        }

        return new AnalysisModel(
                target,
                timingScenario,
                includeCompetitors,
                scaler.getScale(),
                targetAlphaL,
                targetAlphaU,
                prePlaces,
                producers,
                competitors);
    }

    public static HorizonEstimate estimateMaxSteps(AnalysisModel model) {
        return estimateMaxSteps(model, HorizonOptions.defaults());
    }

    /**
     * Estimates maxSteps. The estimate is approximate and performs no
     * repeated-configuration detection.
     *
     * <p>The final alphaU term is intentional: it allows an activation period
     * that starts near the end of the observation part to be measured through
     * the complete target activation interval.</p>
     */
    public static HorizonEstimate estimateMaxSteps(
            AnalysisModel model,
            HorizonOptions options) {
        requireModel(model);
        if (options == null) {
            throw new IllegalArgumentException("options must not be null");
        }

        if (model.prePlaces.isEmpty()) {
            return createHorizon(
                    options.strategy,
                    BigInteger.ZERO,
                    BigInteger.ZERO,
                    BigInteger.ZERO,
                    options,
                    model.timeScale);
        }

        long gammaStar = 0L;
        for (PrePlaceSpec place : model.prePlaces) {
            gammaStar = Math.max(gammaStar, place.gammaU);
        }

        List<Long> allPeriods = new ArrayList<>();
        for (ProducerSpec producer : model.producers) {
            allPeriods.add(producer.period);
        }
        for (CompetitorSpec competitor : model.competitors) {
            allPeriods.add(competitor.cycle());
        }

        BigInteger baseObservation;
        if (allPeriods.isEmpty()) {
            baseObservation = BigInteger.valueOf(gammaStar);
        } else {
            BigInteger common = lcmOfPositiveLongs(allPeriods);
            baseObservation = BigInteger.valueOf(gammaStar).add(
                    common.multiply(
                            BigInteger.valueOf(options.extraBlocks)));
        }

        BigInteger selectedObservation = baseObservation;
        if (options.strategy == HorizonStrategy.PLACE_AWARE) {
            for (int placeIndex = 0;
                 placeIndex < model.prePlaces.size();
                 placeIndex++) {
                BigInteger candidate =
                        estimatePlaceObservationSteps(
                                model,
                                placeIndex,
                                options.extraBlocks);
                if (candidate.compareTo(selectedObservation) > 0) {
                    selectedObservation = candidate;
                }
            }
        }

        BigInteger alphaUTail =
                BigInteger.valueOf(model.targetAlphaU);
        return createHorizon(
                options.strategy,
                baseObservation,
                selectedObservation,
                alphaUTail,
                options,
                model.timeScale);
    }

    /**
     * Runs exactly maxSteps positive unit-time updates unless alphaU is reached
     * earlier. States at steps 0,1,...,maxSteps are examined.
     */
    public static CalculationResult compute(
            AnalysisModel model,
            long maxSteps) {
        requireModel(model);
        if (maxSteps < 0L) {
            throw new IllegalArgumentException(
                    "maxSteps must not be negative");
        }

        if (model.prePlaces.isEmpty()) {
            return new CalculationResult(
                    model.targetAlphaU,
                    true,
                    0L,
                    maxSteps,
                    0L,
                    true,
                    model.targetAlphaL,
                    model.targetAlphaU,
                    model.timeScale);
        }

        List<ArrayList<Long>> multisets =
                new ArrayList<>(model.prePlaces.size());
        for (PrePlaceSpec place : model.prePlaces) {
            multisets.add(new ArrayList<>(place.initialAges));
        }

        long[] producerTimers =
                new long[model.producers.size()];
        for (int i = 0; i < producerTimers.length; i++) {
            producerTimers[i] = model.producers.get(i).period;
        }

        CompetitorState[] competitorStates =
                new CompetitorState[model.competitors.size()];
        for (int i = 0; i < competitorStates.length; i++) {
            competitorStates[i] =
                    new CompetitorState(
                            CompetitorPhase.WAITING,
                            model.competitors.get(i).alpha);
        }

        long activeTime = 0L;
        long result = 0L;
        boolean everActive = false;
        long firstMaximumStep = -1L;
        long step = 0L;

        while (true) {
            // Producer completions at the current time.
            for (int i = 0; i < model.producers.size(); i++) {
                if (producerTimers[i] == 0L) {
                    ProducerSpec producer = model.producers.get(i);
                    produceTokens(multisets, producer.outputs);
                    producerTimers[i] = producer.period;
                }
            }

            // Competitor production completions at the current time.
            for (int i = 0; i < model.competitors.size(); i++) {
                CompetitorState state = competitorStates[i];
                if (state.phase == CompetitorPhase.PRODUCING
                        && state.timer == 0L) {
                    state.phase = CompetitorPhase.WAITING;
                    state.timer = model.competitors.get(i).alpha;
                }
            }

            // The target has priority over competitor starts at this time.
            boolean targetActive =
                    hasTargetActivatingSubsets(model, multisets);
            if (targetActive) {
                if (!everActive) {
                    everActive = true;
                    firstMaximumStep = step;
                }
                if (activeTime > result) {
                    result = activeTime;
                    firstMaximumStep = step;
                }
                if (result >= model.targetAlphaU) {
                    return new CalculationResult(
                            model.targetAlphaU,
                            true,
                            firstMaximumStep,
                            maxSteps,
                            step,
                            true,
                            model.targetAlphaL,
                            model.targetAlphaU,
                            model.timeScale);
                }
            } else {
                activeTime = 0L;
            }

            // Fixed competitor order.
            for (int i = 0; i < model.competitors.size(); i++) {
                CompetitorSpec competitor = model.competitors.get(i);
                CompetitorState state = competitorStates[i];

                if (state.phase != CompetitorPhase.WAITING) {
                    continue;
                }

                boolean active =
                        hasActivatingSubsets(model, multisets, competitor.inputs);
                if (!active) {
                    state.timer = competitor.alpha;
                } else if (state.timer == 0L) {
                    consumeOldest(multisets, competitor.inputs);
                    state.phase = CompetitorPhase.PRODUCING;
                    state.timer = competitor.beta;

                    if (state.timer == 0L) {
                        // Zero production time: completion is immediate.
                        state.phase = CompetitorPhase.WAITING;
                        state.timer = competitor.alpha;
                    }
                }
            }

            // A competitor may remove a target activating subset in this same
            // time instant.
            if (!hasTargetActivatingSubsets(model, multisets)) {
                activeTime = 0L;
            }

            if (step == maxSteps) {
                break;
            }

            boolean[] competitorActiveBefore =
                    new boolean[model.competitors.size()];
            for (int i = 0; i < model.competitors.size(); i++) {
                CompetitorState state = competitorStates[i];
                if (state.phase == CompetitorPhase.WAITING) {
                    competitorActiveBefore[i] =
                            hasActivatingSubsets(
                                    model,
                                    multisets,
                                    model.competitors.get(i).inputs);
                }
            }
            boolean targetActiveBefore =
                    hasTargetActivatingSubsets(model, multisets);

            // One positive scaled unit of time passes.
            for (int i = 0; i < model.prePlaces.size(); i++) {
                updateMultiset(
                        multisets.get(i),
                        model.prePlaces.get(i).gammaU);
            }

            for (int i = 0; i < producerTimers.length; i++) {
                if (producerTimers[i] > 0L) {
                    producerTimers[i]--;
                }
            }

            for (int i = 0; i < model.competitors.size(); i++) {
                CompetitorSpec competitor = model.competitors.get(i);
                CompetitorState state = competitorStates[i];

                if (state.phase == CompetitorPhase.PRODUCING) {
                    if (state.timer > 0L) {
                        state.timer--;
                    }
                } else {
                    boolean activeAfter =
                            hasActivatingSubsets(
                                    model,
                                    multisets,
                                    competitor.inputs);
                    if (competitorActiveBefore[i] && activeAfter) {
                        if (state.timer > 0L) {
                            state.timer--;
                        }
                    } else {
                        state.timer = competitor.alpha;
                    }
                }
            }

            boolean targetActiveAfter =
                    hasTargetActivatingSubsets(model, multisets);
            if (targetActiveBefore && targetActiveAfter) {
                activeTime = addExact(
                        activeTime,
                        1L,
                        "target activation time");
            } else {
                activeTime = 0L;
            }

            step++;
        }

        return new CalculationResult(
                result,
                everActive,
                firstMaximumStep,
                maxSteps,
                step,
                false,
                model.targetAlphaL,
                model.targetAlphaU,
                model.timeScale);
    }

    public static CalculationResult compute(
            TransitionXTPN target,
            TimingScenario timingScenario,
            boolean includeCompetitors,
            long maxSteps) {
        return compute(
                prepare(
                        target,
                        timingScenario,
                        includeCompetitors),
                maxSteps);
    }

    private static LinkedHashMap<PlaceXTPN, Integer>
    collectTargetPrePlaces(TransitionXTPN target) {
        LinkedHashMap<PlaceXTPN, Integer> result =
                new LinkedHashMap<>();
        List<Arc> arcs = target.getInputArcs();
        if (arcs == null) {
            return result;
        }

        for (Arc arc : arcs) {
            if (arc == null || arc.getArcType() != Arc.TypeOfArc.NORMAL) {
                continue;
            }
            Object node = arc.getStartNode();
            if (!(node instanceof PlaceXTPN)) {
                throw new IllegalArgumentException(
                        "A NORMAL input arc of the target does not start "
                                + "in PlaceXTPN");
            }
            mergeWeight(result, (PlaceXTPN) node, arc.getWeight());
        }
        return result;
    }

    private static void validateNoTargetReturnArcs(
            TransitionXTPN target,
            Set<PlaceXTPN> prePlaces) {
        List<Arc> arcs = target.getOutputArcs();
        if (arcs == null) {
            return;
        }
        for (Arc arc : arcs) {
            if (arc == null || arc.getArcType() != Arc.TypeOfArc.NORMAL) {
                continue;
            }
            Object node = arc.getEndNode();
            if (node instanceof PlaceXTPN && prePlaces.contains(node)) {
                throw new IllegalArgumentException(
                        "The target is connected to one analyzed pre-place by "
                                + "NORMAL arcs in both directions, which is "
                                + "outside the supported local subnet");
            }
        }
    }

    private static LinkedHashMap<TransitionXTPN,
            LinkedHashMap<Integer, Integer>> collectCompetitorInputs(
            TransitionXTPN target,
            List<PlaceXTPN> prePlaces) {

        LinkedHashMap<TransitionXTPN,
                LinkedHashMap<Integer, Integer>> result =
                new LinkedHashMap<>();

        for (int placeIndex = 0;
             placeIndex < prePlaces.size();
             placeIndex++) {
            PlaceXTPN place = prePlaces.get(placeIndex);
            List<Arc> arcs = place.getOutputArcs();
            if (arcs == null) {
                continue;
            }

            for (Arc arc : arcs) {
                if (arc == null
                        || arc.getArcType() != Arc.TypeOfArc.NORMAL) {
                    continue;
                }
                Object node = arc.getEndNode();
                if (!(node instanceof TransitionXTPN)) {
                    throw new IllegalArgumentException(
                            "A NORMAL output arc of target pre-place "
                                    + place.getName()
                                    + " does not end in TransitionXTPN");
                }
                TransitionXTPN transition = (TransitionXTPN) node;
                if (transition == target || transition.isKnockedOut()) {
                    continue;
                }

                LinkedHashMap<Integer, Integer> inputs =
                        result.computeIfAbsent(
                                transition,
                                key -> new LinkedHashMap<>());
                mergeWeight(inputs, placeIndex, arc.getWeight());
            }
        }
        return result;
    }

    private static LinkedHashMap<TransitionXTPN,
            LinkedHashMap<Integer, Integer>> collectAllProducerOutputs(
            TransitionXTPN target,
            List<PlaceXTPN> prePlaces) {

        LinkedHashMap<TransitionXTPN,
                LinkedHashMap<Integer, Integer>> result =
                new LinkedHashMap<>();

        for (int placeIndex = 0;
             placeIndex < prePlaces.size();
             placeIndex++) {
            PlaceXTPN place = prePlaces.get(placeIndex);
            List<Arc> arcs = place.getInputArcs();
            if (arcs == null) {
                continue;
            }

            for (Arc arc : arcs) {
                if (arc == null
                        || arc.getArcType() != Arc.TypeOfArc.NORMAL) {
                    continue;
                }
                Object node = arc.getStartNode();
                if (!(node instanceof TransitionXTPN)) {
                    throw new IllegalArgumentException(
                            "A NORMAL input arc of target pre-place "
                                    + place.getName()
                                    + " does not start in TransitionXTPN");
                }
                TransitionXTPN transition = (TransitionXTPN) node;
                if (transition == target || transition.isKnockedOut()) {
                    continue;
                }

                LinkedHashMap<Integer, Integer> outputs =
                        result.computeIfAbsent(
                                transition,
                                key -> new LinkedHashMap<>());
                mergeWeight(outputs, placeIndex, arc.getWeight());
            }
        }
        return result;
    }

    private static void validateNoOppositeNormalArcForSamePair(
            LinkedHashMap<TransitionXTPN,
                    LinkedHashMap<Integer, Integer>> competitorInputs,
            LinkedHashMap<TransitionXTPN,
                    LinkedHashMap<Integer, Integer>> producerOutputs) {
        for (Map.Entry<TransitionXTPN,
                LinkedHashMap<Integer, Integer>> competitor
                : competitorInputs.entrySet()) {
            LinkedHashMap<Integer, Integer> outputs =
                    producerOutputs.get(competitor.getKey());
            if (outputs == null) {
                continue;
            }
            for (Integer placeIndex : competitor.getValue().keySet()) {
                if (outputs.containsKey(placeIndex)) {
                    throw new IllegalArgumentException(
                            "Transition "
                                    + competitor.getKey().getName()
                                    + " is connected to the same analyzed "
                                    + "pre-place by NORMAL arcs in both "
                                    + "directions");
                }
            }
        }
    }

    private static LinkedHashMap<TransitionXTPN,
            LinkedHashMap<Integer, Integer>> reorderCompetitors(
            LinkedHashMap<TransitionXTPN,
                    LinkedHashMap<Integer, Integer>> discovered,
            List<TransitionXTPN> order) {

        LinkedHashMap<TransitionXTPN,
                LinkedHashMap<Integer, Integer>> result =
                new LinkedHashMap<>();
        Set<TransitionXTPN> seen = new HashSet<>();

        for (TransitionXTPN transition : order) {
            if (transition == null
                    || !discovered.containsKey(transition)
                    || !seen.add(transition)) {
                throw new IllegalArgumentException(
                        "Explicit competitor order must contain every "
                                + "discovered competitor exactly once");
            }
            result.put(transition, discovered.get(transition));
        }

        if (result.size() != discovered.size()) {
            throw new IllegalArgumentException(
                    "Explicit competitor order must contain every discovered "
                            + "competitor exactly once");
        }
        return result;
    }

    private static <K> void mergeWeight(
            LinkedHashMap<K, Integer> map,
            K key,
            int weight) {
        if (weight <= 0) {
            throw new IllegalArgumentException(
                    "NORMAL arc weight must be positive");
        }
        Integer old = map.get(key);
        if (old == null) {
            map.put(key, weight);
        } else {
            try {
                map.put(key, Math.addExact(old, weight));
            } catch (ArithmeticException exception) {
                throw new IllegalArgumentException(
                        "Combined arc weight exceeds int range",
                        exception);
            }
        }
    }

    private static void validateTransitionTiming(
            TransitionXTPN transition) {
        if (transition.isMassActionKineticsActiveXTPN()) {
            throw new IllegalArgumentException(
                    "Transition " + transition.getName()
                            + " uses mass-action kinetics, which is outside "
                            + "this endpoint-time analysis");
        }
        if (transition.isImmediateXTPN()) {
            throw new IllegalArgumentException(
                    "Immediate transition " + transition.getName()
                            + " is outside this endpoint-time analysis");
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
            double upper = requireNonNegativeUpper(
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
            double upper = requireNonNegativeUpper(
                    transition.getBetaMaxValue(),
                    "betaU of transition " + transition.getName());
            if (lower > upper) {
                throw new IllegalArgumentException(
                        "Transition " + transition.getName()
                                + " has betaL > betaU");
            }
        }
    }

    private static double selectedProducerAlpha(
            TransitionXTPN transition,
            TimingScenario scenario) {
        if (!transition.isAlphaModeActive()) {
            return 0.0;
        }
        return scenario == TimingScenario.FAVORABLE_TO_TARGET
                ? transition.getAlphaMinValue()
                : transition.getAlphaMaxValue();
    }

    private static double selectedProducerBeta(
            TransitionXTPN transition,
            TimingScenario scenario) {
        if (!transition.isBetaModeActive()) {
            return 0.0;
        }
        return scenario == TimingScenario.FAVORABLE_TO_TARGET
                ? transition.getBetaMinValue()
                : transition.getBetaMaxValue();
    }

    private static double selectedCompetitorAlpha(
            TransitionXTPN transition,
            TimingScenario scenario) {
        if (!transition.isAlphaModeActive()) {
            return 0.0;
        }
        return scenario == TimingScenario.FAVORABLE_TO_TARGET
                ? transition.getAlphaMaxValue()
                : transition.getAlphaMinValue();
    }

    private static double selectedCompetitorBeta(
            TransitionXTPN transition,
            TimingScenario scenario) {
        if (!transition.isBetaModeActive()) {
            return 0.0;
        }
        return scenario == TimingScenario.FAVORABLE_TO_TARGET
                ? transition.getBetaMaxValue()
                : transition.getBetaMinValue();
    }

    private static double activeAlphaLower(
            TransitionXTPN transition) {
        return transition.isAlphaModeActive()
                ? transition.getAlphaMinValue()
                : 0.0;
    }

    private static double activeAlphaUpper(
            TransitionXTPN transition) {
        return transition.isAlphaModeActive()
                ? transition.getAlphaMaxValue()
                : 0.0;
    }

    private static double requireFiniteNonNegative(
            double value,
            String description) {
        if (!Double.isFinite(value) || value < 0.0) {
            throw new IllegalArgumentException(
                    description
                            + " must be a finite non-negative number");
        }
        return value;
    }

    private static double requireNonNegativeUpper(
            double value,
            String description) {
        if (Double.isNaN(value)
                || value == Double.NEGATIVE_INFINITY
                || value < 0.0) {
            throw new IllegalArgumentException(
                    description
                            + " must be non-negative or positive infinity");
        }
        return value;
    }

    private static BigInteger estimatePlaceObservationSteps(
            AnalysisModel model,
            int placeIndex,
            int extraBlocks) {

        PrePlaceSpec place = model.prePlaces.get(placeIndex);
        List<ProducerContribution> producerContributions =
                new ArrayList<>();
        List<CompetitorContribution> competitorContributions =
                new ArrayList<>();
        List<Long> periods = new ArrayList<>();

        for (ProducerSpec producer : model.producers) {
            int weight = producer.weightForPlace(placeIndex);
            if (weight > 0) {
                producerContributions.add(
                        new ProducerContribution(
                                producer.period,
                                weight));
                periods.add(producer.period);
            }
        }

        if (producerContributions.isEmpty()) {
            return BigInteger.valueOf(place.gammaU);
        }

        for (CompetitorSpec competitor : model.competitors) {
            int weight = competitor.weightForPlace(placeIndex);
            if (weight > 0) {
                competitorContributions.add(
                        new CompetitorContribution(
                                competitor.cycle(),
                                weight));
                periods.add(competitor.cycle());
            }
        }

        BigInteger common = lcmOfPositiveLongs(periods);

        /*
         * Without a competitor in this place there is no slow accumulation
         * caused by a small production-consumption difference.  The base
         * estimate already observes several producer periods.
         */
        if (competitorContributions.isEmpty()) {
            return BigInteger.valueOf(place.gammaU)
                    .add(common.multiply(
                            BigInteger.valueOf(extraBlocks)));
        }

        BigInteger produced = BigInteger.ZERO;
        for (ProducerContribution producer : producerContributions) {
            produced = produced.add(
                    common.divide(
                                    BigInteger.valueOf(producer.period))
                            .multiply(
                                    BigInteger.valueOf(producer.weight)));
        }

        BigInteger consumed = BigInteger.ZERO;
        for (CompetitorContribution competitor
                : competitorContributions) {
            consumed = consumed.add(
                    common.divide(
                                    BigInteger.valueOf(competitor.cycle))
                            .multiply(
                                    BigInteger.valueOf(competitor.weight)));
        }

        BigInteger blocks = BigInteger.valueOf(extraBlocks);
        BigInteger difference = produced.subtract(consumed);
        if (difference.signum() > 0) {
            BigInteger bProd =
                    producerOnlyMaximumForPlace(
                            place,
                            producerContributions);
            BigInteger required =
                    ceilDivide(bProd, difference);
            if (required.compareTo(blocks) > 0) {
                blocks = required;
            }
        }

        return BigInteger.valueOf(place.gammaU)
                .add(common.multiply(blocks));
    }

    private static BigInteger producerOnlyMaximumForPlace(
            PrePlaceSpec place,
            List<ProducerContribution> producers) {
        BigInteger result = BigInteger.ZERO;
        for (ProducerContribution producer : producers) {
            long occurrences =
                    place.gammaU / producer.period + 1L;
            result = result.add(
                    BigInteger.valueOf(occurrences)
                            .multiply(
                                    BigInteger.valueOf(producer.weight)));
        }
        return result;
    }

    private static HorizonEstimate createHorizon(
            HorizonStrategy strategy,
            BigInteger baseObservation,
            BigInteger selectedObservation,
            BigInteger alphaUTail,
            HorizonOptions options,
            long timeScale) {

        BigInteger raw =
                selectedObservation.add(alphaUTail);
        BigInteger cap =
                BigInteger.valueOf(options.maxStepsCap);
        boolean capped = raw.compareTo(cap) > 0;
        BigInteger used = capped ? cap : raw;

        if (used.signum() < 0
                || used.compareTo(
                BigInteger.valueOf(Long.MAX_VALUE)) > 0) {
            throw new ArithmeticException(
                    "Selected maxSteps does not fit in long");
        }

        return new HorizonEstimate(
                strategy,
                baseObservation,
                selectedObservation,
                alphaUTail,
                raw,
                used.longValueExact(),
                capped,
                timeScale);
    }

    private static boolean hasTargetActivatingSubsets(
            AnalysisModel model,
            List<ArrayList<Long>> multisets) {
        for (int i = 0; i < model.prePlaces.size(); i++) {
            PrePlaceSpec place = model.prePlaces.get(i);
            if (!hasActivatingSubset(
                    multisets.get(i),
                    place.gammaL,
                    place.targetWeight)) {
                return false;
            }
        }
        return true;
    }

    private static boolean hasActivatingSubset(
            List<Long> multiset,
            long gammaL,
            int need) {
        return need > 0
                && multiset.size() >= need
                && multiset.get(need - 1) >= gammaL;
    }

    private static boolean hasActivatingSubsets(
            AnalysisModel model,
            List<ArrayList<Long>> multisets,
            List<CompetitorInput> inputs) {
        for (CompetitorInput input : inputs) {
            PrePlaceSpec place =
                    model.prePlaces.get(input.prePlaceIndex);
            if (!hasActivatingSubset(
                    multisets.get(input.prePlaceIndex),
                    place.gammaL,
                    input.weight)) {
                return false;
            }
        }
        return true;
    }

    private static void consumeOldest(
            List<ArrayList<Long>> multisets,
            List<CompetitorInput> inputs) {
        for (CompetitorInput input : inputs) {
            ArrayList<Long> multiset =
                    multisets.get(input.prePlaceIndex);
            if (input.weight <= 0
                    || multiset.size() < input.weight) {
                throw new IllegalStateException(
                        "Cannot consume the requested number of tokens");
            }
            multiset.subList(0, input.weight).clear();
        }
    }

    private static void produceTokens(
            List<ArrayList<Long>> multisets,
            List<ProducerOutput> outputs) {
        for (ProducerOutput output : outputs) {
            ArrayList<Long> multiset =
                    multisets.get(output.prePlaceIndex);
            if (output.weight > Integer.MAX_VALUE - multiset.size()) {
                throw new IllegalStateException(
                        "Token multiset would exceed ArrayList capacity");
            }
            for (int i = 0; i < output.weight; i++) {
                multiset.add(0L);
            }
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

    private static long addExact(
            long first,
            long second,
            String description) {
        try {
            return Math.addExact(first, second);
        } catch (ArithmeticException exception) {
            throw new ArithmeticException(
                    description + " exceeds long range");
        }
    }

    private static BigInteger lcmOfPositiveLongs(
            List<Long> values) {
        if (values.isEmpty()) {
            return BigInteger.ZERO;
        }
        BigInteger result = BigInteger.ONE;
        for (Long value : values) {
            if (value == null || value <= 0L) {
                throw new IllegalArgumentException(
                        "LCM values must be positive");
            }
            BigInteger current = BigInteger.valueOf(value);
            result = result.divide(result.gcd(current))
                    .multiply(current);
        }
        return result;
    }

    private static BigInteger ceilDivide(
            BigInteger numerator,
            BigInteger denominator) {
        if (numerator.signum() < 0
                || denominator.signum() <= 0) {
            throw new IllegalArgumentException(
                    "ceilDivide requires a non-negative numerator "
                            + "and a positive denominator");
        }
        if (numerator.signum() == 0) {
            return BigInteger.ZERO;
        }
        return numerator.add(denominator)
                .subtract(BigInteger.ONE)
                .divide(denominator);
    }

    private static BigDecimal unscale(
            long scaledValue,
            long timeScale) {
        return BigDecimal.valueOf(scaledValue)
                .divide(BigDecimal.valueOf(timeScale))
                .stripTrailingZeros();
    }

    private static void requireModel(AnalysisModel model) {
        if (model == null) {
            throw new IllegalArgumentException(
                    "model must not be null");
        }
    }

    private static final class PrePlaceSpec {
        private final PlaceXTPN place;
        private final long gammaL;
        private final long gammaU;
        private final int targetWeight;
        private final List<Long> initialAges;

        private PrePlaceSpec(
                PlaceXTPN place,
                long gammaL,
                long gammaU,
                int targetWeight,
                List<Long> initialAges) {
            this.place = place;
            this.gammaL = gammaL;
            this.gammaU = gammaU;
            this.targetWeight = targetWeight;
            this.initialAges = List.copyOf(initialAges);
        }
    }

    private static final class ProducerSpec {
        private final TransitionXTPN transition;
        private final long period;
        private final List<ProducerOutput> outputs;

        private ProducerSpec(
                TransitionXTPN transition,
                long period,
                List<ProducerOutput> outputs) {
            this.transition = transition;
            this.period = period;
            this.outputs = List.copyOf(outputs);
        }

        private int weightForPlace(int placeIndex) {
            for (ProducerOutput output : outputs) {
                if (output.prePlaceIndex == placeIndex) {
                    return output.weight;
                }
            }
            return 0;
        }
    }

    private static final class ProducerOutput {
        private final int prePlaceIndex;
        private final int weight;

        private ProducerOutput(
                int prePlaceIndex,
                int weight) {
            this.prePlaceIndex = prePlaceIndex;
            this.weight = weight;
        }
    }

    private static final class CompetitorSpec {
        private final TransitionXTPN transition;
        private final long alpha;
        private final long beta;
        private final List<CompetitorInput> inputs;

        private CompetitorSpec(
                TransitionXTPN transition,
                long alpha,
                long beta,
                List<CompetitorInput> inputs) {
            this.transition = transition;
            this.alpha = alpha;
            this.beta = beta;
            this.inputs = List.copyOf(inputs);
        }

        private long cycle() {
            return addExact(alpha, beta, "competitor cycle");
        }

        private int weightForPlace(int placeIndex) {
            for (CompetitorInput input : inputs) {
                if (input.prePlaceIndex == placeIndex) {
                    return input.weight;
                }
            }
            return 0;
        }
    }

    private static final class CompetitorInput {
        private final int prePlaceIndex;
        private final int weight;

        private CompetitorInput(
                int prePlaceIndex,
                int weight) {
            this.prePlaceIndex = prePlaceIndex;
            this.weight = weight;
        }
    }

    private enum CompetitorPhase {
        WAITING,
        PRODUCING
    }

    private static final class CompetitorState {
        private CompetitorPhase phase;
        private long timer;

        private CompetitorState(
                CompetitorPhase phase,
                long timer) {
            this.phase = phase;
            this.timer = timer;
        }
    }

    private static final class ProducerContribution {
        private final long period;
        private final int weight;

        private ProducerContribution(
                long period,
                int weight) {
            this.period = period;
            this.weight = weight;
        }
    }

    private static final class CompetitorContribution {
        private final long cycle;
        private final int weight;

        private CompetitorContribution(
                long cycle,
                int weight) {
            this.cycle = cycle;
            this.weight = weight;
        }
    }

    private static final class Rational {
        private final BigInteger numerator;
        private final BigInteger denominator;

        private Rational(
                BigInteger numerator,
                BigInteger denominator) {
            this.numerator = numerator;
            this.denominator = denominator;
        }

        private static Rational from(double value) {
            BigDecimal decimal =
                    BigDecimal.valueOf(value).stripTrailingZeros();
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

            BigInteger gcd =
                    numerator.abs().gcd(denominator);
            if (gcd.signum() != 0) {
                numerator = numerator.divide(gcd);
                denominator = denominator.divide(gcd);
            }
            return new Rational(numerator, denominator);
        }
    }

    /**
     * Exact common scaling for finite decimal values stored by Holmes.
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
                common = common.divide(
                                common.gcd(rational.denominator))
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
                        "Internal error: value does not fit "
                                + "the common time scale");
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
