package holmes.analyse.XTPN;

import holmes.petrinet.elements.Arc;
import holmes.petrinet.elements.PlaceXTPN;
import holmes.petrinet.elements.TransitionXTPN;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.CancellationException;
import static holmes.analyse.XTPN.MaxTokensBoundCalculatorV3.Support.*;

/**
 * Local token counts, not complete-net state-space exploration.
 * The V2/article unit-step rules are retained, including endpoint tests for
 * consumer activation. Token age gammaU is available at observation time.
 *
 * prepare() copies CURRENT token ages and parameters, but NOT live transition
 * clocks. Stop the simulator before preparation. compute() uses numerical
 * copies and does not modify the Holmes net.
 *
 * Install BOTH V3 files. Shared support is included below; V2 is not required.
 */
public final class MaxTokensBoundCalculatorV3 {
    private MaxTokensBoundCalculatorV3() {}

    public enum Mode { PRODUCERS_ONLY, WITH_CONSUMERS }
    /** These settings are not proved extremes in the presence of consumers. */
    public enum TimingScenario {
        FAST_PRODUCERS_SLOW_CONSUMERS, SLOW_PRODUCERS_FAST_CONSUMERS
    }
    public enum HorizonMethod {
        NO_PRODUCERS, PRODUCER_PERIOD,
        CONSUMER_RATE_NON_POSITIVE, CONSUMER_RATE_POSITIVE
    }
    public enum TerminationReason { HORIZON_REACHED }

    public static final class HorizonOptions {
        private final int extraBlocks;
        private final long maxStepsCap;
        public HorizonOptions(int extraBlocks,long maxStepsCap) {
            require(extraBlocks>0,"extraBlocks must be positive");
            require(maxStepsCap>=0,"maxStepsCap must not be negative");
            this.extraBlocks=extraBlocks; this.maxStepsCap=maxStepsCap;
        }
        public static HorizonOptions defaults() { return new HorizonOptions(3,100_000L); }
        public static HorizonOptions withoutCap(int blocks) {
            return new HorizonOptions(blocks,Long.MAX_VALUE);
        }
        public int getExtraBlocks() { return extraBlocks; }
        public long getMaxStepsCap() { return maxStepsCap; }
    }

    /** Memory guard, not a model capacity. Exceeding it throws, never drops tokens. */
    public static final class RunOptions {
        private final int maxStoredTokens;
        public RunOptions(int maxStoredTokens) {
            require(maxStoredTokens>0,"maxStoredTokens must be positive");
            this.maxStoredTokens=maxStoredTokens;
        }
        public static RunOptions defaults() { return new RunOptions(1_000_000); }
        public int getMaxStoredTokens() { return maxStoredTokens; }
    }

    @FunctionalInterface
    public interface ObservationListener {
        /** After production, before consumption, including steps zero and H. */
        void observe(long scaledStep,long tokenCount);
    }

    public static final class AnalysisModel {
        private final PlaceXTPN place;
        private final Mode mode;
        private final TimingScenario timing;
        private final FrozenNet frozen;
        private final ScaledNet data;
        private AnalysisModel(PlaceXTPN place,Mode mode,TimingScenario timing,FrozenNet frozen) {
            this.place=place; this.mode=mode; this.timing=timing; this.frozen=frozen;
            data=scaleNet(frozen,timing==TimingScenario.FAST_PRODUCERS_SLOW_CONSUMERS,
                    mode==Mode.WITH_CONSUMERS,null);
        }
        public PlaceXTPN getPlace() { return place; }
        public Mode getMode() { return mode; }
        public TimingScenario getTimingScenario() { return timing; }
        public long getTimeScale() { return data.scale; }
        public long getScaledGammaL() { return data.places.get(0).gammaL; }
        public long getScaledGammaU() { return data.places.get(0).gammaU; }
        public int getInitialTokenCount() { return data.places.get(0).ages.size(); }
        public int getProducerCount() { return data.producers.size(); }
        public int getConsumerCount() { return data.consumers.size(); }
        public List<TransitionXTPN> getProducers() { return transitions(data.producers); }
        public List<TransitionXTPN> getConsumerOrder() { return transitions(data.consumers); }
        public List<BigDecimal> getInitialAges() { return originalAges(data.places.get(0),data.scale); }
        public List<String> getWarnings() { return frozen.warnings; }

        /** All with... methods reuse the snapshot; they do not reread the net. */
        public AnalysisModel withMode(Mode newMode) {
            require(newMode!=null,"mode must not be null");
            return new AnalysisModel(place,newMode,timing,frozen);
        }
        public AnalysisModel withTimingScenario(TimingScenario scenario) {
            require(scenario!=null,"scenario must not be null");
            return new AnalysisModel(place,mode,scenario,frozen);
        }
        public AnalysisModel withConsumerOrder(List<TransitionXTPN> order) {
            require(mode==Mode.WITH_CONSUMERS,"Consumer order requires WITH_CONSUMERS");
            return new AnalysisModel(place,mode,timing,frozen.withConsumers(reorder(frozen.consumers,order)));
        }
    }

    public static final class FormulaResult {
        private final BigInteger bProd,bSafe;
        private final boolean emptyInitially;
        private FormulaResult(BigInteger bProd,BigInteger bSafe,boolean empty) {
            this.bProd=bProd; this.bSafe=bSafe; emptyInitially=empty;
        }
        public BigInteger getProducerOnlyMaximumExact() { return bProd; }
        public BigInteger getSafeBoundWithInitialTokensExact() { return bSafe; }
        public long getProducerOnlyMaximum() { return bProd.longValueExact(); }
        public long getSafeBoundWithInitialTokens() { return bSafe.longValueExact(); }
        /** Exactness ONLY for the empty producer-only model, not the complete net. */
        public boolean isExactForEmptyProducerOnlyModel() { return emptyInitially; }
        /** @deprecated The name omits the producer-only qualification. */
        @Deprecated public boolean isExactForCurrentInitialMultiset() { return emptyInitially; }
    }

    public static final class HorizonEstimate {
        private final HorizonMethod method;
        private final BigInteger uncapped,produced,consumed;
        private final long steps,scale;
        private final boolean capped;
        private HorizonEstimate(HorizonMethod method,BigInteger raw,long cap,long scale,
                                BigInteger produced,BigInteger consumed) {
            this.method=method; uncapped=raw; this.scale=scale;
            steps=raw.min(BigInteger.valueOf(cap)).longValueExact();
            capped=raw.compareTo(BigInteger.valueOf(cap))>0;
            this.produced=produced; this.consumed=consumed;
        }
        public HorizonMethod getMethod() { return method; }
        public BigInteger getUncappedSteps() { return uncapped; }
        public long getMaxSteps() { return steps; }
        public boolean isCapped() { return capped; }
        public long getTimeScale() { return scale; }
        public BigDecimal getMaxTime() { return unscale(steps,scale); }
        public BigDecimal getUncappedTime() { return unscale(uncapped,scale); }
        public BigInteger getProducedPerCommonBlock() { return produced; }
        public BigInteger getPotentiallyConsumedPerCommonBlock() { return consumed; }
    }

    public static final class CalculationResult {
        private final long maximum,first,requested,scale;
        private final Mode mode;
        private final boolean completeLocalMaximum;
        private CalculationResult(long maximum,long first,long requested,AnalysisModel model,boolean complete) {
            this.maximum=maximum; this.first=first; this.requested=requested;
            scale=model.data.scale; mode=model.mode; completeLocalMaximum=complete;
        }
        public long getMaxTokens() { return maximum; }
        public long getFirstMaximumStep() { return first; }
        public long getRequestedMaxSteps() { return requested; }
        public long getPerformedSteps() { return requested; }
        public long getTimeScale() { return scale; }
        public Mode getMode() { return mode; }
        public BigDecimal getFirstMaximumTime() { return unscale(first,scale); }
        public BigDecimal getPerformedTime() { return unscale(requested,scale); }
        public TerminationReason getTerminationReason() { return TerminationReason.HORIZON_REACHED; }
        /**
         * Sufficient certificate for THIS local count run: no producers, or
         * no consumers and H >= gammaU + LCM(selected producer periods).
         * Not a certificate of attainability in the complete net.
         */
        public boolean isCompleteLocalMaximumCertified() { return completeLocalMaximum; }
    }

    public static AnalysisModel prepare(PlaceXTPN place,Mode mode) {
        return prepare(place,mode,null);
    }
    public static AnalysisModel prepare(PlaceXTPN place,Mode mode,List<TransitionXTPN> order) {
        return prepare(place,mode,TimingScenario.FAST_PRODUCERS_SLOW_CONSUMERS,order);
    }
    public static AnalysisModel prepare(PlaceXTPN place,Mode mode,TimingScenario scenario,
                                        List<TransitionXTPN> order) {
        require(place!=null && mode!=null && scenario!=null,"place, mode and scenario must not be null");
        LinkedHashSet<String> warnings=new LinkedHashSet<>();
        RawPlace rp=readPlace(place);
        Map<TransitionXTPN,Integer> producers=neighbours(place,true,warnings);
        Map<TransitionXTPN,Integer> consumers=neighbours(place,false,warnings);
        for(TransitionXTPN t:producers.keySet())
            require(!consumers.containsKey(t),"Opposite NORMAL arcs between "+label(t)+" and "+rp.label);
        List<RawFlow> prod=new ArrayList<>(),cons=new ArrayList<>();
        Set<PlaceXTPN> retained=Collections.singleton(place);
        for(Map.Entry<TransitionXTPN,Integer> e:producers.entrySet()) {
            prod.add(new RawFlow(e.getKey(),new int[]{e.getValue()}));
            describeIgnoredInputs(e.getKey(),retained,true,warnings);
        }
        for(Map.Entry<TransitionXTPN,Integer> e:consumers.entrySet()) {
            cons.add(new RawFlow(e.getKey(),new int[]{e.getValue()}));
            describeIgnoredInputs(e.getKey(),retained,false,warnings);
        }
        if(mode==Mode.PRODUCERS_ONLY)
            require(order==null || order.isEmpty(),"No consumer order in PRODUCERS_ONLY mode");
        else if(order!=null) cons=reorder(cons,order);
        return new AnalysisModel(place,mode,scenario,new FrozenNet(List.of(rp),prod,cons,warnings));
    }

    public static FormulaResult computeProducerOnlyFormula(PlaceXTPN place) {
        return computeProducerOnlyFormula(prepare(place,Mode.PRODUCERS_ONLY));
    }
    /** Uses ORIGINAL LOWER producer bounds even on an opposite-timing model. */
    public static FormulaResult computeProducerOnlyFormula(AnalysisModel model) {
        require(model!=null,"model must not be null");
        BigDecimal life=decimal(model.frozen.places.get(0).gammaU,"gammaU");
        BigInteger sum=BigInteger.ZERO;
        for(RawFlow f:model.frozen.producers) {
            f.validate();
            BigDecimal minimum=decimal(f.alphaLower(),"alphaL").add(decimal(f.betaLower(),"betaL"));
            require(minimum.signum()>0,"B_prod/B_safe require a positive LOWER cycle: "+f.label);
            BigInteger count=life.divide(minimum,0,RoundingMode.FLOOR).toBigIntegerExact().add(BigInteger.ONE);
            sum=sum.add(count.multiply(BigInteger.valueOf(f.weights[0])));
        }
        return new FormulaResult(sum,sum.add(BigInteger.valueOf(model.getInitialTokenCount())),
                model.getInitialTokenCount()==0);
    }
    public static long computeProducerOnlyKBound(PlaceXTPN place) {
        return computeProducerOnlyFormula(place).getSafeBoundWithInitialTokens();
    }
    public static long computeProducerOnlyKBound(AnalysisModel model) {
        return computeProducerOnlyFormula(model).getSafeBoundWithInitialTokens();
    }
    public static HorizonEstimate estimateMaxSteps(AnalysisModel model) {
        return estimateMaxSteps(model,HorizonOptions.defaults());
    }
    public static HorizonEstimate estimateMaxSteps(AnalysisModel model,HorizonOptions options) {
        require(model!=null && options!=null,"model and options must not be null");
        ScaledNet d=model.data;
        RateEstimate e=placeHorizon(d,0,options.extraBlocks,true);
        HorizonMethod method=d.producers.isEmpty()?HorizonMethod.NO_PRODUCERS
                :d.consumers.isEmpty()?HorizonMethod.PRODUCER_PERIOD
                :e.produced.compareTo(e.consumed)>0?HorizonMethod.CONSUMER_RATE_POSITIVE
                :HorizonMethod.CONSUMER_RATE_NON_POSITIVE;
        return new HorizonEstimate(method,e.steps,options.maxStepsCap,d.scale,e.produced,e.consumed);
    }
    public static long stepsForTime(AnalysisModel model,BigDecimal time) {
        require(model!=null,"model must not be null");
        return exactSteps(time,model.data.scale);
    }
    public static CalculationResult computeForTime(AnalysisModel model,BigDecimal time) {
        return compute(model,stepsForTime(model,time));
    }
    public static CalculationResult compute(PlaceXTPN place,Mode mode,long H) {
        return compute(prepare(place,mode),H);
    }
    public static CalculationResult compute(AnalysisModel model,long H) {
        return compute(model,H,RunOptions.defaults(),null);
    }
    public static CalculationResult compute(AnalysisModel model,long H,RunOptions options,
                                            ObservationListener listener) {
        require(model!=null && options!=null,"model and options must not be null");
        require(H>=0,"maxSteps must not be negative");
        Engine engine=new Engine(model.data,options.maxStoredTokens);
        long maximum=-1,first=0,step=0;
        while(true) {
            checkCancelled();
            engine.completeAtCurrentTime();
            long count=engine.ages.get(0).size();
            if(count>maximum) { maximum=count; first=step; }
            if(listener!=null) listener.observe(step,count);
            engine.consumeAtCurrentTime();
            if(step==H) break;
            engine.advanceOneStep();
            step++;
        }
        boolean complete=model.data.producers.isEmpty();
        if(!complete && model.data.consumers.isEmpty())
            complete=BigInteger.valueOf(H).compareTo(BigInteger.valueOf(model.getScaledGammaU())
                    .add(commonPeriods(model.data.producers)))>=0;
        return new CalculationResult(maximum,first,H,model,complete);
    }
    public static String describeModel(AnalysisModel model) {
        require(model!=null,"model must not be null");
        return "TOKEN-COUNT LOCAL MODEL\nMode: "+model.mode+"\nTiming: "+model.timing+"\n"
                +describe(model.frozen,model.data)
                +"Observation: after producer completions, before consumption.\n";
    }
    public static String formatReport(AnalysisModel model,HorizonEstimate h,CalculationResult r) {
        require(model!=null && r!=null,"model and result must not be null");
        StringBuilder s=new StringBuilder(describeModel(model));
        s.append("\nLargest observed token count: ").append(r.maximum)
                .append("\nFirst maximum: step ").append(r.first)
                .append(", time ").append(plain(r.getFirstMaximumTime()))
                .append("\nRequested/performed positive updates: ").append(r.requested)
                .append("\nObserved through time: ").append(plain(r.getPerformedTime()))
                .append("\nTermination: ").append(r.getTerminationReason())
                .append("\nComplete maximum certified for this LOCAL run: ").append(r.completeLocalMaximum).append('\n');
        if(h!=null)
            s.append("Automatic horizon method: ").append(h.method)
                    .append("\nAutomatic H before cap: ").append(h.uncapped)
                    .append(" (time ").append(plain(h.getUncappedTime())).append(")")
                    .append("\nAutomatic H after cap: ").append(h.steps)
                    .append(" (time ").append(plain(h.getMaxTime())).append(")")
                    .append("\nAutomatic horizon capped: ").append(h.capped).append('\n');
        return s.append("No repeated-configuration detection was performed.\n")
                .append("A consumer-aware result is not a proved upper/lower bound for the complete net.\n")
                .append("A local certificate does not establish attainability in the complete net.\n").toString();
    }

    /** Shared copied-data engine for the two V3 classes; not a complete-net simulator. */
    static final class Support {
        private Support() {}
        static void require(boolean ok,String message) {
            if(!ok) throw new IllegalArgumentException(message);
        }
        static void checkCancelled() {
            if(Thread.currentThread().isInterrupted())
                throw new CancellationException("Local analysis interrupted; no complete result");
        }
        static String label(PlaceXTPN p) { return p.getName()+" [node ID="+p.getID()+"]"; }
        static String label(TransitionXTPN t) { return t.getName()+" [node ID="+t.getID()+"]"; }
        static String nodeLabel(Object n) {
            if(n instanceof PlaceXTPN) return label((PlaceXTPN)n);
            if(n instanceof TransitionXTPN) return label((TransitionXTPN)n);
            return n==null?"<null>":n.getClass().getSimpleName();
        }
        static BigDecimal decimal(double v,String what) {
            require(Double.isFinite(v) && v>=0,what+" must be finite and non-negative");
            return BigDecimal.valueOf(v).stripTrailingZeros();
        }
        static void validUpper(double lo,double hi,String what) {
            decimal(lo,what+" lower");
            require(!Double.isNaN(hi) && hi>=lo,what+": invalid upper bound");
        }
        static BigDecimal unscale(long v,long scale) { return unscale(BigInteger.valueOf(v),scale); }
        static BigDecimal unscale(BigInteger v,long scale) {
            // Exact: the scale has only factors 2 and 5 from decimal inputs.
            return new BigDecimal(v).divide(BigDecimal.valueOf(scale)).stripTrailingZeros();
        }
        static String plain(BigDecimal n) { return n==null?"-":n.toPlainString(); }
        static long exactSteps(BigDecimal time,long scale) {
            require(time!=null && time.signum()>=0,"Original-time horizon must be non-negative");
            try { return time.multiply(BigDecimal.valueOf(scale)).longValueExact(); }
            catch(ArithmeticException e) {
                throw new IllegalArgumentException("Horizon must fit long and be an exact multiple of 1/"
                        +scale+"; no rounding or change of sampling grid is applied",e);
            }
        }
        static BigInteger lcm(BigInteger a,BigInteger b) {
            require(a.signum()>0 && b.signum()>0,"LCM arguments must be positive");
            return a.divide(a.gcd(b)).multiply(b);
        }
        static BigInteger ceilDivide(BigInteger n,BigInteger d) {
            require(n.signum()>=0 && d.signum()>0,"Invalid ceiling division");
            return n.add(d).subtract(BigInteger.ONE).divide(d);
        }
        static final class Scaler {
            final long scale;
            Scaler(List<BigDecimal> values) {
                BigInteger s=BigInteger.ONE;
                for(BigDecimal value:values) {
                    BigDecimal x=value.stripTrailingZeros();
                    int k=Math.max(0,x.scale());
                    BigInteger den=BigInteger.TEN.pow(k);
                    BigInteger num=x.movePointRight(k).toBigIntegerExact();
                    den=den.divide(den.gcd(num.abs()));
                    s=lcm(s,den);
                }
                try { scale=s.longValueExact(); }
                catch(ArithmeticException e) {
                    throw new IllegalArgumentException("Exact time scale exceeds long. Check decimal "
                            +"precision; V3 does not silently round parameters or live token ages",e);
                }
            }
            long of(double value) {
                return decimal(value,"time").multiply(BigDecimal.valueOf(scale)).longValueExact();
            }
        }
        static final class RawPlace {
            final PlaceXTPN place;
            final String label;
            final double gammaL,gammaU;
            final List<Double> ages;
            RawPlace(PlaceXTPN p,double l,double u,List<Double> ages) {
                place=p; label=Support.label(p); gammaL=l; gammaU=u; this.ages=List.copyOf(ages);
            }
        }
        static RawPlace readPlace(PlaceXTPN p) {
            require(p!=null,"Place must not be null");
            require(p.isGammaModeActive(),label(p)+" is timeless (gamma disabled). "
                    +"Finite active gamma is required; infinity is NOT replaced by a large number");
            double l=p.getGammaMinValue(),u=p.getGammaMaxValue();
            decimal(l,"gammaL"); decimal(u,"gammaU");
            require(l<u,label(p)+" must satisfy gammaL < gammaU");
            List<Double> copy=p.copyMultiset();
            require(copy!=null,"Missing token multiset in "+label(p));
            List<Double> ages=new ArrayList<>(copy);
            for(Double age:ages) {
                require(age!=null,"Null token age in "+label(p));
                decimal(age,"Token age in "+label(p));
                require(age<=u,"Token age exceeds gammaU in "+label(p));
            }
            ages.sort(Collections.reverseOrder());
            return new RawPlace(p,l,u,ages);
        }
        static final class RawFlow {
            final TransitionXTPN transition;
            final String label;
            final boolean alphaMode,betaMode,massAction,immediate;
            final double aL,aU,bL,bU;
            final int[] weights;
            RawFlow(TransitionXTPN t,int[] weights) {
                transition=t; label=Support.label(t);
                alphaMode=t.isAlphaModeActive(); betaMode=t.isBetaModeActive();
                massAction=t.isMassActionKineticsActiveXTPN(); immediate=t.isImmediateXTPN();
                aL=t.getAlphaMinValue(); aU=t.getAlphaMaxValue();
                bL=t.getBetaMinValue(); bU=t.getBetaMaxValue();
                this.weights=weights.clone();
            }
            void validate() {
                require(!massAction && !immediate,label+": immediate/mass-action timing is unsupported");
                require(alphaMode || betaMode,label+": alpha and beta are both disabled");
                if(alphaMode) validUpper(aL,aU,label+" alpha");
                if(betaMode) validUpper(bL,bU,label+" beta");
            }
            double alphaLower() { return alphaMode?aL:0; }
            double betaLower() { return betaMode?bL:0; }
            double alpha(boolean lower) { return alphaMode?(lower?aL:aU):0; }
            double beta(boolean lower) { return betaMode?(lower?bL:bU):0; }
        }
        static final class FrozenNet {
            final List<RawPlace> places;
            final List<RawFlow> producers,consumers;
            final List<String> warnings;
            FrozenNet(List<RawPlace> p,List<RawFlow> r,List<RawFlow> c,Iterable<String> w) {
                places=List.copyOf(p); producers=List.copyOf(r); consumers=List.copyOf(c);
                List<String> copy=new ArrayList<>();
                for(String s:w) copy.add(s);
                warnings=List.copyOf(copy);
            }
            FrozenNet withConsumers(List<RawFlow> order) {
                return new FrozenNet(places,producers,order,warnings);
            }
        }
        static final class PlaceData {
            final RawPlace raw;
            final long gammaL,gammaU;
            final List<Long> ages;
            PlaceData(RawPlace p,Scaler s) {
                raw=p; gammaL=s.of(p.gammaL); gammaU=s.of(p.gammaU);
                List<Long> copy=new ArrayList<>();
                for(double a:p.ages) copy.add(s.of(a));
                ages=List.copyOf(copy);
            }
        }
        static final class Flow {
            final RawFlow raw;
            final long alpha,beta,period;
            final int[] weights;
            Flow(RawFlow r,Scaler s,boolean lower) {
                raw=r; alpha=s.of(r.alpha(lower)); beta=s.of(r.beta(lower));
                period=Math.addExact(alpha,beta);
                require(period>0,r.label+": selected alpha+beta is zero. "
                        +"This variant is unsupported, not a negative analysis result");
                weights=r.weights.clone();
            }
        }
        static final class ScaledNet {
            final long scale,targetAlphaL,targetAlphaU;
            final List<PlaceData> places;
            final List<Flow> producers,consumers;
            ScaledNet(long scale,List<PlaceData> p,List<Flow> r,List<Flow> c,long aL,long aU) {
                this.scale=scale; places=List.copyOf(p); producers=List.copyOf(r);
                consumers=List.copyOf(c); targetAlphaL=aL; targetAlphaU=aU;
            }
        }
        /** Selected endpoints ONLY: unselected parameters must not refine V2's grid. */
        static ScaledNet scaleNet(FrozenNet net,boolean fastProducers,boolean includeConsumers,RawFlow target) {
            List<BigDecimal> values=new ArrayList<>();
            for(RawPlace p:net.places) {
                values.add(decimal(p.gammaL,"gammaL")); values.add(decimal(p.gammaU,"gammaU"));
                for(double a:p.ages) values.add(decimal(a,"initial age"));
            }
            if(target!=null) {
                target.validate();
                require(target.alphaMode,"Target must have alpha timing enabled");
                values.add(decimal(target.aL,"target alphaL"));
                values.add(decimal(target.aU,"target alphaU"));
            }
            for(RawFlow f:net.producers) addSelected(values,f,fastProducers);
            if(includeConsumers)
                for(RawFlow f:net.consumers) addSelected(values,f,!fastProducers);
            Scaler s=new Scaler(values);
            List<PlaceData> ps=new ArrayList<>();
            List<Flow> rs=new ArrayList<>(),cs=new ArrayList<>();
            for(RawPlace p:net.places) ps.add(new PlaceData(p,s));
            for(RawFlow r:net.producers) rs.add(new Flow(r,s,fastProducers));
            if(includeConsumers)
                for(RawFlow c:net.consumers) cs.add(new Flow(c,s,!fastProducers));
            return new ScaledNet(s.scale,ps,rs,cs,target==null?0:s.of(target.aL),
                    target==null?0:s.of(target.aU));
        }
        static void addSelected(List<BigDecimal> values,RawFlow f,boolean lower) {
            f.validate();
            values.add(decimal(f.alpha(lower),"Selected alpha of "+f.label));
            values.add(decimal(f.beta(lower),"Selected beta of "+f.label));
        }
        static List<Arc> arcs(List<Arc> list) {
            return list==null?List.of():new ArrayList<>(list);
        }
        static <K> void merge(Map<K,Integer> map,K key,int weight) {
            require(weight>0,"NORMAL arc weight must be positive");
            map.put(key,Math.addExact(map.getOrDefault(key,0),weight));
        }
        static Map<TransitionXTPN,Integer> neighbours(PlaceXTPN p,boolean incoming,Set<String> warnings) {
            Map<TransitionXTPN,Integer> result=new LinkedHashMap<>();
            for(Arc a:arcs(incoming?p.getInputArcs():p.getOutputArcs())) {
                require(a!=null && a.getArcType()!=null,"Null arc/type at "+label(p));
                if(a.getArcType()==Arc.TypeOfArc.READARC || a.getArcType()==Arc.TypeOfArc.INHIBITOR) {
                    warnings.add("Read/inhibitor connections of "+label(p)
                            +" are not token flows and are not converted into consumption/production.");
                    continue;
                }
                require(a.getArcType()==Arc.TypeOfArc.NORMAL,"Unsupported "+a.getArcType()+" arc at "+label(p));
                Object n=incoming?a.getStartNode():a.getEndNode();
                require(n instanceof TransitionXTPN,"NORMAL neighbour of "+label(p)+" must be TransitionXTPN");
                TransitionXTPN t=(TransitionXTPN)n;
                if(t.isKnockedOut()) { warnings.add("Knocked-out neighbour omitted: "+label(t)); continue; }
                merge(result,t,a.getWeight());
            }
            return result;
        }
        static void describeIgnoredInputs(TransitionXTPN t,Set<PlaceXTPN> retained,
                                          boolean producer,Set<String> warnings) {
            for(Arc a:arcs(t.getInputArcs())) {
                require(a!=null && a.getArcType()!=null,"Null arc/type at "+label(t));
                if(producer || a.getArcType()!=Arc.TypeOfArc.NORMAL || !retained.contains(a.getStartNode()))
                    warnings.add("Ignored input requirement of "+(producer?"producer ":"consumer/competitor ")
                            +label(t)+": "+nodeLabel(a.getStartNode())+" --"+a.getArcType()
                            +", weight="+a.getWeight()+"--> transition");
            }
        }
        static List<RawFlow> reorder(List<RawFlow> flows,List<TransitionXTPN> order) {
            require(order!=null && order.size()==flows.size(),"Order must contain every consumer/competitor exactly once");
            Map<TransitionXTPN,RawFlow> index=new LinkedHashMap<>();
            for(RawFlow f:flows) index.put(f.transition,f);
            Set<TransitionXTPN> seen=new HashSet<>();
            List<RawFlow> result=new ArrayList<>();
            for(TransitionXTPN t:order) {
                require(t!=null && index.containsKey(t) && seen.add(t),"Duplicate, missing or unrelated transition in order");
                result.add(index.get(t));
            }
            return result;
        }
        static List<TransitionXTPN> transitions(List<Flow> fs) {
            List<TransitionXTPN> result=new ArrayList<>();
            for(Flow f:fs) result.add(f.raw.transition);
            return List.copyOf(result);
        }
        static List<BigDecimal> originalAges(PlaceData p,long scale) {
            List<BigDecimal> result=new ArrayList<>();
            for(long a:p.ages) result.add(unscale(a,scale));
            return List.copyOf(result);
        }
        static BigInteger commonPeriods(List<Flow> fs) {
            BigInteger d=BigInteger.ONE;
            for(Flow f:fs) d=lcm(d,BigInteger.valueOf(f.period));
            return d;
        }
        static BigInteger selectedProducerMaximum(ScaledNet d,int p) {
            BigInteger b=BigInteger.ZERO;
            for(Flow f:d.producers) {
                BigInteger n=BigInteger.valueOf(d.places.get(p).gammaU)
                        .divide(BigInteger.valueOf(f.period)).add(BigInteger.ONE);
                b=b.add(n.multiply(BigInteger.valueOf(f.weights[p])));
            }
            return b;
        }
        static final class RateEstimate {
            final BigInteger steps,produced,consumed;
            RateEstimate(BigInteger s,BigInteger p,BigInteger c) { steps=s; produced=p; consumed=c; }
        }
        static RateEstimate placeHorizon(ScaledNet d,int p,int extraBlocks,boolean countHorizon) {
            BigInteger period=BigInteger.ONE;
            boolean hasProducer=false,hasConsumer=false;
            for(Flow f:d.producers) if(f.weights[p]>0) {
                period=lcm(period,BigInteger.valueOf(f.period)); hasProducer=true;
            }
            for(Flow f:d.consumers) if(f.weights[p]>0) {
                period=lcm(period,BigInteger.valueOf(f.period)); hasConsumer=true;
            }
            BigInteger life=BigInteger.valueOf(d.places.get(p).gammaU);
            if(!hasProducer)
                return new RateEstimate(countHorizon?BigInteger.ZERO:life,BigInteger.ZERO,BigInteger.ZERO);
            BigInteger produced=BigInteger.ZERO,consumed=BigInteger.ZERO;
            for(Flow f:d.producers) if(f.weights[p]>0)
                produced=produced.add(period.divide(BigInteger.valueOf(f.period))
                        .multiply(BigInteger.valueOf(f.weights[p])));
            for(Flow f:d.consumers) if(f.weights[p]>0)
                consumed=consumed.add(period.divide(BigInteger.valueOf(f.period))
                        .multiply(BigInteger.valueOf(f.weights[p])));
            BigInteger blocks=BigInteger.valueOf(countHorizon && !hasConsumer?1:extraBlocks);
            BigInteger delta=produced.subtract(consumed);
            if(hasConsumer && delta.signum()>0)
                blocks=blocks.max(ceilDivide(selectedProducerMaximum(d,p),delta));
            return new RateEstimate(life.add(period.multiply(blocks)),produced,consumed);
        }

        /** Endpoint tests intentionally retain V2; they do not certify continuous activation. */
        static final class Engine {
            final ScaledNet data;
            final List<ArrayList<Long>> ages=new ArrayList<>();
            final long[] producerTimers,timers;
            final boolean[] producing;
            final int tokenLimit;
            long totalTokens;
            Engine(ScaledNet d,int tokenLimit) {
                data=d; this.tokenLimit=tokenLimit;
                for(PlaceData p:d.places) totalTokens+=p.ages.size();
                checkTokenCapacity(0);
                for(PlaceData p:d.places) ages.add(new ArrayList<>(p.ages));
                producerTimers=new long[d.producers.size()];
                for(int i=0;i<producerTimers.length;i++) producerTimers[i]=d.producers.get(i).period;
                timers=new long[d.consumers.size()]; producing=new boolean[timers.length];
                for(int i=0;i<timers.length;i++) timers[i]=d.consumers.get(i).alpha;
            }
            void checkTokenCapacity(long added) {
                if(added>tokenLimit-totalTokens)
                    throw new IllegalStateException("Local token storage limit ("+tokenLimit
                            +") exceeded. No complete result returned; increase RunOptions explicitly.");
            }
            void completeAtCurrentTime() {
                for(int i=0;i<producerTimers.length;i++) if(producerTimers[i]==0) {
                    Flow f=data.producers.get(i);
                    long added=0;
                    for(int w:f.weights) added=Math.addExact(added,w);
                    checkTokenCapacity(added);
                    for(int p=0;p<ages.size();p++)
                        for(int k=0;k<f.weights[p];k++) ages.get(p).add(0L);
                    totalTokens+=added; producerTimers[i]=f.period;
                }
                for(int i=0;i<timers.length;i++) if(producing[i] && timers[i]==0) {
                    producing[i]=false; timers[i]=data.consumers.get(i).alpha;
                }
            }
            boolean active(int[] needs) {
                for(int p=0;p<needs.length;p++) if(needs[p]>0) {
                    List<Long> k=ages.get(p);
                    if(k.size()<needs[p] || k.get(needs[p]-1)<data.places.get(p).gammaL) return false;
                }
                return true;
            }
            void consumeAtCurrentTime() {
                for(int i=0;i<timers.length;i++) {
                    if(producing[i]) continue;
                    Flow f=data.consumers.get(i);
                    if(!active(f.weights)) timers[i]=f.alpha;
                    else if(timers[i]==0) {
                        for(int p=0;p<f.weights.length;p++) {
                            int w=f.weights[p];
                            if(w>0) { ages.get(p).subList(0,w).clear(); totalTokens-=w; }
                        }
                        producing[i]=f.beta>0;
                        timers[i]=producing[i]?f.beta:f.alpha;
                    }
                }
            }
            void advanceOneStep() {
                boolean[] before=new boolean[timers.length];
                for(int i=0;i<before.length;i++)
                    before[i]=!producing[i] && active(data.consumers.get(i).weights);
                for(int p=0;p<ages.size();p++) {
                    ArrayList<Long> k=ages.get(p);
                    long limit=data.places.get(p).gammaU;
                    int remove=0;
                    // At the preceding observation equality was still allowed.
                    while(remove<k.size() && k.get(remove)>=limit) remove++;
                    if(remove>0) { k.subList(0,remove).clear(); totalTokens-=remove; }
                    for(int j=0;j<k.size();j++) k.set(j,k.get(j)+1);
                }
                for(int i=0;i<producerTimers.length;i++) producerTimers[i]--;
                for(int i=0;i<timers.length;i++) {
                    Flow f=data.consumers.get(i);
                    if(producing[i] || (before[i] && active(f.weights))) {
                        if(timers[i]>0) timers[i]--;
                    } else timers[i]=f.alpha;
                }
            }
        }
        static String formatAges(PlaceData p,long scale) {
            Map<Long,Integer> counts=new LinkedHashMap<>();
            for(long a:p.ages) counts.merge(a,1,Integer::sum);
            StringBuilder s=new StringBuilder("{");
            boolean first=true;
            for(Map.Entry<Long,Integer> e:counts.entrySet()) {
                if(!first) s.append(", ");
                first=false; s.append(plain(unscale(e.getKey(),scale)));
                if(e.getValue()>1) s.append(" x ").append(e.getValue());
            }
            return s.append("}").toString();
        }
        static String describe(FrozenNet net,ScaledNet d) {
            StringBuilder s=new StringBuilder();
            s.append("Input: copied CURRENT token ages; local transition counters start afresh.\n")
                    .append("Not continuation of the current complete simulator state.\n")
                    .append("Time scale: ").append(d.scale).append(" step(s) per original time unit.\n")
                    .append("Names/IDs captured at prepare(). Node IDs are not t_i/p_i indices.\n");
            for(PlaceData p:d.places)
                s.append(p.raw.label).append(": J=(").append(plain(unscale(p.gammaL,d.scale)))
                        .append(", ").append(plain(unscale(p.gammaU,d.scale)))
                        .append("), K=").append(formatAges(p,d.scale)).append('\n');
            describeFlows(s,"Producers",d.producers,d);
            describeFlows(s,"Consumers/competitors in processing order",d.consumers,d);
            s.append("Producer requirements and consumer/competitor outputs are ignored.\n")
                    .append("Only retained NORMAL inputs constrain consumers/competitors.\n")
                    .append("Activation across unit updates is checked only at the endpoints.\n");
            for(String w:net.warnings) s.append("NOTE: ").append(w).append('\n');
            return s.toString();
        }
        static void describeFlows(StringBuilder s,String title,List<Flow> fs,ScaledNet d) {
            s.append(title).append(":\n");
            if(fs.isEmpty()) s.append("  (none)\n");
            int i=1;
            for(Flow f:fs) {
                s.append("  ").append(i++).append(". ").append(f.raw.label)
                        .append(": selected alpha=").append(plain(unscale(f.alpha,d.scale)))
                        .append(", beta=").append(plain(unscale(f.beta,d.scale)))
                        .append(", cycle=").append(plain(unscale(f.period,d.scale)))
                        .append("; retained weights: ");
                for(int p=0;p<f.weights.length;p++) if(f.weights[p]>0)
                    s.append(d.places.get(p).raw.label).append('=').append(f.weights[p]).append("; ");
                s.append('\n');
            }
        }
    }
}
