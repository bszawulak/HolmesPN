package holmes.analyse.firingalgo;

import holmes.analyse.firingalgo.petrinetstructure.Transition;

import java.util.BitSet;

class Conflict {
    public Transition transition;
    public double weight;
    public double s;
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
        s = s*weight/sumOfWeights;
        other.s = other.s*other.weight/sumOfWeights;

        mask.or(other.mask);
        other.mask.or(mask);
    }

    public void syncByHalf(Conflict other) {
        s = s/2;
        other.s = other.s/2;

        mask.or(other.mask);
        other.mask.or(mask);
    }

    public boolean isResolved() {
        BitSet masked = (BitSet)mask.clone();
        masked.and(targetMask);
        return masked.equals(targetMask);
    }

    public Conflict copyForOtherTransition(Transition transition) {
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
