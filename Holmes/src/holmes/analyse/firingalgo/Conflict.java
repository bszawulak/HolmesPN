package holmes.analyse.firingalgo;

import holmes.analyse.firingalgo.petrinetstructure.Transition;

import java.util.BitSet;

class Conflict {
    public Transition transition;
    public double weight;
    public double s;
    public double multiplierForPropagation = 1;
    private BitSet mask;
    private BitSet targetMask;

    Conflict(Transition transition, double weight, BitSet mask, BitSet targetMask) {
        this(transition, weight, mask, targetMask, 1);
    }

    Conflict(Transition transition, double weight, BitSet mask, BitSet targetMask, double s) {
        this.transition = transition;
        this.weight = weight;
        this.mask = (BitSet)mask.clone();
        this.targetMask = (BitSet)targetMask.clone();
        this.s = s;
    }

    public double getResult() {
        return s;
    }

    public void sync(Conflict other) {
        double sumOfWeights = weight + other.weight;

        multiplierForPropagation = weight/sumOfWeights;
        s = s*multiplierForPropagation;

        other.multiplierForPropagation = other.weight/sumOfWeights;
        other.s = other.s* other.multiplierForPropagation;

        mask.or(other.mask);
        other.mask.or(mask);
    }

    public void syncByHalf(Conflict other) {
        s = s/2;
        other.s = other.s/2;

        multiplierForPropagation = 0.5;
        other.multiplierForPropagation = 0.5;

        mask.or(other.mask);
        other.mask.or(mask);
    }

    public void forceResolve(Double newS) {
        if(newS != null && newS > 0 && newS < 1) {
            s = newS;
            multiplierForPropagation = newS;
            mask = targetMask;
            return;
        }
        s = 1d/targetMask.cardinality();
        multiplierForPropagation = 1d/targetMask.cardinality();
        mask = targetMask;
    }

    public boolean isResolved() {
        BitSet masked = (BitSet)mask.clone();
        masked.and(targetMask);
        return masked.equals(targetMask);
    }

    public Conflict copyForOtherTransition(Transition transition) {
        return new Conflict(transition, weight, mask, targetMask, s);
    }

    public Conflict copy() {
        return new Conflict(transition, weight, mask, targetMask, s);
    }

    public BitSet getMask() {
        return (BitSet)mask.clone();
    }

    public void setMask(BitSet mask) {
        this.mask = (BitSet)mask.clone();
    }

    public BitSet getTargetMask() {
        return (BitSet)targetMask.clone();
    }
}
