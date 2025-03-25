package holmes.petrinet.elements.containers;

import java.io.Serializable;

public class ArcXTPNContainer implements Serializable {
    private boolean isXTPN = false;
    private boolean isXTPNinhibitor = false;
    private boolean isXTPNact = false;
    private boolean isXTPNprod = false;
    public boolean showQSimXTPN = false;


    public boolean isXTPN() {
        return isXTPN;
    }

    public void setXTPNstatus(boolean status) {
        isXTPN = status;
    }

    public boolean isXTPNinhibitor() {
        return isXTPNinhibitor;
    }

    public void setXTPNinhibitorStatus(boolean status) {
        isXTPNinhibitor = status;
    }

    public void setXTPNactStatus(boolean value) {
        isXTPNact = value;
    }

    public boolean getXTPNactStatus() {
        return isXTPNact;
    }

    public void setXTPNprodStatus(boolean value) {
        isXTPNprod = value;
    }

    public boolean getXTPNprodStatus() {
        return isXTPNprod;
    }
}
