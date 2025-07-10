package holmes.analyse.firingalgo;

import java.util.BitSet;

public class MaskOffsetManager {
    static MaskOffsetManager instance = new MaskOffsetManager();
    private int counter = 0;

    public BitSet getNewMask() {
        counter++;
        BitSet bits = new BitSet(counter);
        bits.clear();
        bits.set(counter - 1);
        return bits;
    }
}
