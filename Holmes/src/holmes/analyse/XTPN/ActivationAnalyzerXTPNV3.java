package holmes.analyse.XTPN;

import holmes.petrinet.elements.Arc;
import holmes.petrinet.elements.PlaceXTPN;
import holmes.petrinet.elements.TransitionXTPN;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import static holmes.analyse.XTPN.MaxTokensBoundCalculatorV3.Support.*;

/**
 * Local endpoint-tested activation counter, retaining the V2/article updates.
 * The target never fires. The counter is NOT generally a continuous activation
 * duration and is not a reachability or liveness certificate.
 *
 * All direct target inputs must be NORMAL. Read/inhibitor inputs are rejected
 * before recognition of a source target. Producer requirements, external
 * competitor inputs and competitor outputs remain omitted and are reported.
 *
 * Requires MaxTokensBoundCalculatorV3.java, which contains shared support.
 * Neither class uses V2 or mutates the Holmes net.
 */
public final class ActivationAnalyzerXTPNV3 {
    private ActivationAnalyzerXTPNV3() {}
    public enum TimingScenario {
        /** Historical name; not a proved best case. */
        FAVORABLE_TO_TARGET,
        /** Not a proved worst case. */
        OPPOSITE_ENDPOINTS
    }
    public enum HorizonStrategy { BASE, PLACE_AWARE }
    public enum TerminationReason { HORIZON_REACHED, COUNTER_CAP_REACHED, SOURCE_TARGET_ANALYTIC }
    public enum ActivationStatus {
        NEVER_ACTIVE_OBSERVED, BELOW_ALPHA_L, ALPHA_L_REACHED, ALPHA_U_REACHED, SOURCE_TARGET_ANALYTIC
    }
    public static final class HorizonOptions {
        private final HorizonStrategy strategy;
        private final int extraBlocks;
        private final long maxStepsCap;
        public HorizonOptions(HorizonStrategy strategy,int extraBlocks,long cap) {
            require(strategy!=null,"strategy must not be null");
            require(extraBlocks>0,"extraBlocks must be positive");
            require(cap>=0,"maxStepsCap must not be negative");
            this.strategy=strategy; this.extraBlocks=extraBlocks; maxStepsCap=cap;
        }
        public static HorizonOptions defaults() {
            return new HorizonOptions(HorizonStrategy.PLACE_AWARE,3,100_000L);
        }
        public static HorizonOptions baseDefaults() {
            return new HorizonOptions(HorizonStrategy.BASE,3,100_000L);
        }
        public static HorizonOptions withoutCap(HorizonStrategy strategy,int blocks) {
            return new HorizonOptions(strategy,blocks,Long.MAX_VALUE);
        }
        public HorizonStrategy getStrategy() { return strategy; }
        public int getExtraBlocks() { return extraBlocks; }
        public long getMaxStepsCap() { return maxStepsCap; }
    }
    @FunctionalInterface
    public interface ObservationListener {
        /** Before competitor starts; counter is in scaled steps. */
        void observe(long scaledStep,long counter,boolean active);
    }
    public static final class AnalysisModel {
        private final RawFlow target;
        private final TimingScenario scenario;
        private final boolean includeCompetitors;
        private final FrozenNet frozen;
        private final ScaledNet data;
        private AnalysisModel(RawFlow target,TimingScenario scenario,boolean include,FrozenNet frozen) {
            this.target=target; this.scenario=scenario; includeCompetitors=include; this.frozen=frozen;
            data=scaleNet(frozen,scenario==TimingScenario.FAVORABLE_TO_TARGET,include,target);
        }
        public TransitionXTPN getTarget() { return target.transition; }
        public TimingScenario getTimingScenario() { return scenario; }
        public boolean areCompetitorsIncluded() { return includeCompetitors; }
        public long getTimeScale() { return data.scale; }
        public long getScaledAlphaL() { return data.targetAlphaL; }
        public long getScaledAlphaU() { return data.targetAlphaU; }
        public BigDecimal getAlphaL() { return unscale(data.targetAlphaL,data.scale); }
        public BigDecimal getAlphaU() { return unscale(data.targetAlphaU,data.scale); }
        public int getPrePlaceCount() { return data.places.size(); }
        public int getProducerCount() { return data.producers.size(); }
        public int getCompetitorCount() { return data.consumers.size(); }
        public List<PlaceXTPN> getPrePlaces() {
            List<PlaceXTPN> result=new ArrayList<>();
            for(RawPlace p:frozen.places) result.add(p.place);
            return List.copyOf(result);
        }
        public List<TransitionXTPN> getProducers() { return transitions(data.producers); }
        public List<TransitionXTPN> getCompetitorOrder() { return transitions(data.consumers); }
        public List<String> getWarnings() { return frozen.warnings; }
        public Map<PlaceXTPN,List<BigDecimal>> getInitialAges() {
            Map<PlaceXTPN,List<BigDecimal>> result=new LinkedHashMap<>();
            for(PlaceData p:data.places) result.put(p.raw.place,originalAges(p,data.scale));
            return Collections.unmodifiableMap(result);
        }
        /** Reuses the frozen snapshot, including captured names; no live reads. */
        public AnalysisModel withTimingScenario(TimingScenario timing) {
            require(timing!=null,"scenario must not be null");
            return new AnalysisModel(target,timing,includeCompetitors,frozen);
        }
        public AnalysisModel withCompetitors(boolean included) {
            return new AnalysisModel(target,scenario,included,frozen);
        }
        public AnalysisModel withCompetitorOrder(List<TransitionXTPN> order) {
            require(includeCompetitors,"Competitors are disabled");
            return new AnalysisModel(target,scenario,true,frozen.withConsumers(reorder(frozen.consumers,order)));
        }
    }
    public static final class HorizonEstimate {
        private final HorizonStrategy strategy;
        private final BigInteger base,selected,tail,raw;
        private final long steps,scale;
        private final boolean capped;
        private HorizonEstimate(HorizonOptions o,BigInteger base,BigInteger selected,long tail,long scale) {
            strategy=o.strategy; this.base=base; this.selected=selected; this.tail=BigInteger.valueOf(tail);
            raw=selected.add(this.tail);
            steps=raw.min(BigInteger.valueOf(o.maxStepsCap)).longValueExact();
            capped=raw.compareTo(BigInteger.valueOf(o.maxStepsCap))>0; this.scale=scale;
        }
        public HorizonStrategy getStrategy() { return strategy; }
        public BigInteger getBaseObservationSteps() { return base; }
        public BigInteger getSelectedObservationSteps() { return selected; }
        public BigInteger getAlphaUTail() { return tail; }
        public BigInteger getUncappedSteps() { return raw; }
        public long getMaxSteps() { return steps; }
        public boolean isCapped() { return capped; }
        public long getTimeScale() { return scale; }
        public BigDecimal getMaxTime() { return unscale(steps,scale); }
        public BigDecimal getUncappedTime() { return unscale(raw,scale); }
    }
    public static final class CalculationResult {
        private final long maximum,first,requested,performed,scale,aL,aU;
        private final boolean everActive;
        private final TerminationReason reason;
        private CalculationResult(long maximum,boolean ever,long first,long requested,
                                  long performed,ScaledNet d,TerminationReason reason) {
            this.maximum=maximum; everActive=ever; this.first=first; this.requested=requested;
            this.performed=performed; scale=d.scale; aL=d.targetAlphaL; aU=d.targetAlphaU; this.reason=reason;
        }
        public long getMaximumActivationCounterSteps() { return maximum; }
        public BigDecimal getMaximumActivationCounterTimeUnits() { return unscale(maximum,scale); }
        /** @deprecated Use getMaximumActivationCounterSteps(). */
        @Deprecated public long getMaximumActivationSteps() { return maximum; }
        /** @deprecated This is a sampled counter, not a continuous activation duration. */
        @Deprecated public BigDecimal getMaximumActivationTime() { return getMaximumActivationCounterTimeUnits(); }
        /** Also true when activity is established analytically for a source. */
        public boolean wasEverActive() { return everActive; }
        public boolean wasActiveObserved() { return everActive && !isAnalyticalSourceResult(); }
        public boolean isAnalyticalSourceResult() { return reason==TerminationReason.SOURCE_TARGET_ANALYTIC; }
        /** -1 for no active observation, including the analytical source case. */
        public long getFirstMaximumStep() { return first; }
        public BigDecimal getFirstMaximumTime() { return first<0?null:unscale(first,scale); }
        public long getRequestedMaxSteps() { return requested; }
        public long getPerformedSteps() { return performed; }
        public BigDecimal getRequestedTime() { return unscale(requested,scale); }
        public BigDecimal getPerformedTime() { return unscale(performed,scale); }
        public boolean isCounterCapReached() { return reason==TerminationReason.COUNTER_CAP_REACHED; }
        /** @deprecated No continuous window is certified. False for an analytical source result. */
        @Deprecated public boolean isFullWindowReached() { return isCounterCapReached(); }
        public boolean isIterationLimitReached() { return reason==TerminationReason.HORIZON_REACHED; }
        public long getTimeScale() { return scale; }
        public BigDecimal getAlphaL() { return unscale(aL,scale); }
        public BigDecimal getAlphaU() { return unscale(aU,scale); }
        public TerminationReason getTerminationReason() { return reason; }
        /** About the counter, not global firing feasibility. */
        public boolean reachesLowerThreshold() { return everActive && maximum>=aL; }
        public ActivationStatus getStatus() {
            if(isAnalyticalSourceResult()) return ActivationStatus.SOURCE_TARGET_ANALYTIC;
            if(!everActive) return ActivationStatus.NEVER_ACTIVE_OBSERVED;
            if(maximum>=aU) return ActivationStatus.ALPHA_U_REACHED;
            if(maximum>=aL) return ActivationStatus.ALPHA_L_REACHED;
            return ActivationStatus.BELOW_ALPHA_L;
        }
    }
    public static AnalysisModel prepare(TransitionXTPN target,TimingScenario scenario,boolean include) {
        return prepare(target,scenario,include,null);
    }
    public static AnalysisModel prepare(TransitionXTPN target,TimingScenario scenario,
                                        boolean include,List<TransitionXTPN> order) {
        require(target!=null && scenario!=null,"target and scenario must not be null");
        require(!target.isKnockedOut(),"Target is knocked out");
        Map<PlaceXTPN,Integer> needs=new LinkedHashMap<>();
        // Crucial: check ALL target inputs BEFORE recognizing a source.
        for(Arc a:arcs(target.getInputArcs())) {
            require(a!=null && a.getArcType()==Arc.TypeOfArc.NORMAL,
                    "Target "+label(target)+" has a non-NORMAL or invalid input. "
                            +"Read/inhibitor/other special inputs are unsupported and must not be silently ignored");
            require(a.getStartNode() instanceof PlaceXTPN,"Target NORMAL input must start in PlaceXTPN");
            merge(needs,(PlaceXTPN)a.getStartNode(),a.getWeight());
        }
        for(Arc a:arcs(target.getOutputArcs())) {
            require(a!=null && a.getArcType()==Arc.TypeOfArc.NORMAL,
                    "Special target output/read counterpart is outside this algorithm");
            require(!needs.containsKey(a.getEndNode()),"Opposite NORMAL arcs between target and a retained place");
        }
        if(!include) require(order==null || order.isEmpty(),"Explicit competitor order requires competitors enabled");
        List<PlaceXTPN> places=new ArrayList<>(needs.keySet());
        Set<PlaceXTPN> retained=new LinkedHashSet<>(places);
        List<RawPlace> rawPlaces=new ArrayList<>();
        LinkedHashSet<String> warnings=new LinkedHashSet<>();
        Map<TransitionXTPN,int[]> prod=new LinkedHashMap<>(),cons=new LinkedHashMap<>();
        int[] targetWeights=new int[places.size()];
        for(int p=0;p<places.size();p++) {
            PlaceXTPN place=places.get(p); rawPlaces.add(readPlace(place)); targetWeights[p]=needs.get(place);
            addNeighbours(neighbours(place,true,warnings),prod,p,places.size(),target);
            addNeighbours(neighbours(place,false,warnings),cons,p,places.size(),target);
        }
        for(Map.Entry<TransitionXTPN,int[]> e:cons.entrySet()) {
            int[] output=prod.get(e.getKey());
            if(output!=null) for(int p=0;p<output.length;p++) {
                require(!(output[p]>0 && e.getValue()[p]>0),
                        label(e.getKey())+" has opposite NORMAL arcs to "+rawPlaces.get(p).label);
                if(output[p]>0) warnings.add("Competitor production into retained place is omitted: "
                        +label(e.getKey())+" -> "+rawPlaces.get(p).label+", weight="+output[p]);
            }
        }
        List<RawFlow> producers=new ArrayList<>(),competitors=new ArrayList<>();
        for(Map.Entry<TransitionXTPN,int[]> e:prod.entrySet()) {
            // Structural competitors are excluded even if their consumption is disabled.
            if(cons.containsKey(e.getKey())) continue;
            producers.add(new RawFlow(e.getKey(),e.getValue()));
            describeIgnoredInputs(e.getKey(),retained,true,warnings);
        }
        for(Map.Entry<TransitionXTPN,int[]> e:cons.entrySet()) {
            competitors.add(new RawFlow(e.getKey(),e.getValue()));
            describeIgnoredInputs(e.getKey(),retained,false,warnings);
        }
        if(include && order!=null) competitors=reorder(competitors,order);
        return new AnalysisModel(new RawFlow(target,targetWeights),scenario,include,
                new FrozenNet(rawPlaces,producers,competitors,warnings));
    }
    private static void addNeighbours(Map<TransitionXTPN,Integer> neighbours,Map<TransitionXTPN,int[]> map,
                                      int p,int n,TransitionXTPN target) {
        for(Map.Entry<TransitionXTPN,Integer> e:neighbours.entrySet()) {
            if(e.getKey()==target) continue;
            int[] weights=map.computeIfAbsent(e.getKey(),k->new int[n]);
            weights[p]=e.getValue();
        }
    }
    public static HorizonEstimate estimateMaxSteps(AnalysisModel model) {
        return estimateMaxSteps(model,HorizonOptions.defaults());
    }
    public static HorizonEstimate estimateMaxSteps(AnalysisModel model,HorizonOptions o) {
        require(model!=null && o!=null,"model and options must not be null");
        ScaledNet d=model.data;
        if(d.places.isEmpty()) return new HorizonEstimate(o,BigInteger.ZERO,BigInteger.ZERO,0,d.scale);
        long life=0;
        for(PlaceData p:d.places) life=Math.max(life,p.gammaU);
        BigInteger base=BigInteger.valueOf(life);
        if(!d.producers.isEmpty() || !d.consumers.isEmpty())
            base=base.add(lcm(commonPeriods(d.producers),commonPeriods(d.consumers))
                    .multiply(BigInteger.valueOf(o.extraBlocks)));
        BigInteger selected=base;
        if(o.strategy==HorizonStrategy.PLACE_AWARE)
            for(int p=0;p<d.places.size();p++) selected=selected.max(placeHorizon(d,p,o.extraBlocks,false).steps);
        return new HorizonEstimate(o,base,selected,d.targetAlphaU,d.scale);
    }
    public static long stepsForTime(AnalysisModel model,BigDecimal time) {
        require(model!=null,"model must not be null");
        return exactSteps(time,model.data.scale);
    }
    public static CalculationResult computeForTime(AnalysisModel model,BigDecimal time) {
        return compute(model,stepsForTime(model,time));
    }
    public static CalculationResult compute(TransitionXTPN target,TimingScenario scenario,boolean include,long H) {
        return compute(prepare(target,scenario,include),H);
    }
    public static CalculationResult compute(AnalysisModel model,long H) {
        return compute(model,H,MaxTokensBoundCalculatorV3.RunOptions.defaults(),null);
    }
    public static CalculationResult compute(AnalysisModel model,long H,
                                            MaxTokensBoundCalculatorV3.RunOptions options,
                                            ObservationListener listener) {
        require(model!=null && options!=null,"model and options must not be null");
        require(H>=0,"maxSteps must not be negative"); checkCancelled();
        ScaledNet d=model.data;
        if(d.places.isEmpty())
            return new CalculationResult(d.targetAlphaU,true,-1,H,0,d,TerminationReason.SOURCE_TARGET_ANALYTIC);
        Engine engine=new Engine(d,options.getMaxStoredTokens());
        long counter=0,maximum=0,first=-1,step=0;
        boolean ever=false;
        while(true) {
            checkCancelled(); engine.completeAtCurrentTime();
            boolean active=engine.active(model.target.weights);
            if(active) {
                if(!ever) { ever=true; first=step; }
                if(counter>maximum) { maximum=counter; first=step; }
            } else counter=0;
            if(listener!=null) listener.observe(step,counter,active);
            if(active && maximum>=d.targetAlphaU)
                return new CalculationResult(d.targetAlphaU,true,first,H,step,d,TerminationReason.COUNTER_CAP_REACHED);
            engine.consumeAtCurrentTime();
            boolean before=engine.active(model.target.weights);
            if(!before) counter=0;
            if(step==H) return new CalculationResult(maximum,ever,first,H,step,d,TerminationReason.HORIZON_REACHED);
            engine.advanceOneStep();
            counter=before && engine.active(model.target.weights)?Math.addExact(counter,1):0;
            step++;
        }
    }
    public static String describeModel(AnalysisModel model) {
        require(model!=null,"model must not be null");
        StringBuilder s=new StringBuilder("ACTIVATION-COUNTER LOCAL MODEL\nTarget: ");
        s.append(model.target.label).append("\nTiming: ").append(model.scenario)
                .append("\nCompetitors included: ").append(model.includeCompetitors)
                .append("\nTarget alpha: [").append(plain(model.getAlphaL())).append(", ")
                .append(plain(model.getAlphaU())).append("]\n").append(describe(model.frozen,model.data))
                .append("Target NORMAL requirements:\n");
        for(int p=0;p<model.target.weights.length;p++)
            s.append("  ").append(model.frozen.places.get(p).label).append(" -> target: ")
                    .append(model.target.weights[p]).append('\n');
        s.append("Target is held from firing and checked before competitor starts.\n");
        if(!model.includeCompetitors)
            s.append("Structural competitors remain excluded from producers even when consumption is disabled.\n");
        return s.toString();
    }
    public static String formatReport(AnalysisModel model,HorizonEstimate h,CalculationResult r) {
        require(model!=null && r!=null,"model and result must not be null");
        StringBuilder s=new StringBuilder(describeModel(model));
        s.append('\n').append(r.isAnalyticalSourceResult()
                        ?"Analytical counter cap (no target input requirements): "
                        :"Largest observed activation counter (original time units): ")
                .append(plain(r.getMaximumActivationCounterTimeUnits()))
                .append("\nCounter in scaled steps: ").append(r.maximum)
                .append("\neverActive: ").append(r.everActive)
                .append("\nActive observed in simulation: ").append(r.wasActiveObserved())
                .append("\nLocal status: ").append(r.getStatus()).append("\nTermination: ").append(r.reason)
                .append("\nFirst maximum observed: ").append(r.first<0?"not observed"
                        :"step "+r.first+", time "+plain(r.getFirstMaximumTime()))
                .append("\nRequested updates: ").append(r.requested)
                .append(" (time ").append(plain(r.getRequestedTime())).append(")")
                .append("\nPerformed updates: ").append(r.performed)
                .append(" (time ").append(plain(r.getPerformedTime())).append(")\n");
        if(h!=null)
            s.append("Automatic horizon: ").append(h.strategy)
                    .append("\nAutomatic H before cap: ").append(h.raw)
                    .append(" (time ").append(plain(h.getUncappedTime())).append(")")
                    .append("\nAutomatic H after cap: ").append(h.steps)
                    .append(" (time ").append(plain(h.getMaxTime())).append(")")
                    .append("\nAutomatic horizon capped: ").append(h.capped).append('\n');
        return s.append("No repeated-configuration detection was performed.\n")
                .append("This counter is not generally a continuous activation duration.\n")
                .append("Neither reaching nor missing alphaL proves global firing feasibility/infeasibility.\n")
                .append("The timing settings are not guaranteed upper/lower extremes.\n").toString();
    }
}
