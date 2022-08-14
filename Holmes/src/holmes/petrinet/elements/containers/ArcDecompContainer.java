package holmes.petrinet.elements.containers;

import java.awt.*;
import java.util.ArrayList;

public class ArcDecompContainer {
    private boolean isColorChanged;
    private Color arcColorValue;
    public ArrayList<Color> layers = new ArrayList<>();
    private int memoryOfArcWeight = -1;
    //comparison:
    private boolean isBranchEnd = false;


    public void setColor(boolean isColorChanged, Color arcColorValue) {
        this.isColorChanged = isColorChanged;
        this.arcColorValue = arcColorValue;
    }

    public boolean isColorChanged() {
        return isColorChanged;
    }

    public Color getArcNewColor() {
        return arcColorValue;
    }


    @SuppressWarnings("unused")
    public int getMemoryOfArcWeight() {
        return memoryOfArcWeight;
    }

    public void setMemoryOfArcWeight(int memoryOfArcWeight) {
        this.memoryOfArcWeight = memoryOfArcWeight;
    }

    public boolean isBranchEnd() {
        return isBranchEnd;
    }

    public void setBranchEnd(boolean branchEnd) {
        isBranchEnd = branchEnd;
    }
}
