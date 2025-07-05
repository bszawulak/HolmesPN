package holmes.analyse.firingalgo;

public class FiringDelayConflict {
    public int transactionId;
    public double weight;
    public double s;
    public long mask;
    private long targetMask;

    FiringDelayConflict(int transactionId, double weight, long mask, long targetMask) {
        this.transactionId = transactionId;
        this.weight = weight;
        this.mask = mask;
        this.targetMask = targetMask;
    }

    public double getResult() {
        return weight * s;
    }

    public void sync(FiringDelayConflict other) {
        double sumOfWeights = weight + other.weight;
        s = s*weight/sumOfWeights;
        other.s = other.s*other.weight/sumOfWeights;

        mask |= other.mask;
        other.mask |= mask;
    }

    public boolean isResolved() {
        return (mask & targetMask) == targetMask;
    }
}
