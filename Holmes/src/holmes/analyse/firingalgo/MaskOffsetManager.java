package holmes.analyse.firingalgo;

public class MaskOffsetManager {
    static MaskOffsetManager instance = new MaskOffsetManager();
    private int counter = 0;

    public long getNewMask() {
        counter++;
        //TODO przygotować się na przekroczenie longa
        return 2^counter;
    }
}
