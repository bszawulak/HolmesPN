package holmes.analyse.firingalgo;

import holmes.petrinet.elements.Transition;

import java.util.BitSet;

public class FiringDelayConflict {
    public int transactionId;
    public double weight;
    public double s;
    private BitSet mask;
    private BitSet targetMask;

    FiringDelayConflict(int transactionId, double weight, BitSet mask, BitSet targetMask) {
        this(transactionId, weight, mask, targetMask, 1);
    }

    FiringDelayConflict(int transactionId, double weight, BitSet mask, BitSet targetMask, double s) {
        this.transactionId = transactionId;
        this.weight = weight;
        this.mask = (BitSet)mask.clone();
        this.targetMask = (BitSet)targetMask.clone();
        this.s = s;
    }

    public double getResult() {
        return weight * s;
    }

    public void sync(FiringDelayConflict other) {
        double sumOfWeights = weight + other.weight;
        s = s*weight/sumOfWeights;
        other.s = other.s*other.weight/sumOfWeights;

        mask.or(other.mask);
        other.mask.or(mask);
    }

    public boolean isResolved() {
        BitSet masked = (BitSet)mask.clone();
        masked.and(targetMask);
        return masked.equals(targetMask);
    }

    public FiringDelayConflict copyForOtherTransaction(Transition transition) {
        return new FiringDelayConflict(transition.getID(), weight, mask, targetMask, s);
    }

    public BitSet getMask() {
        return (BitSet)mask.clone();
    }

    public void setMask(BitSet mask) {
        this.mask = (BitSet)mask.clone();
    }
}
