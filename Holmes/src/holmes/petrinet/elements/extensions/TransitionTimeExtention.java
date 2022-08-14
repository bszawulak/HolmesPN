package holmes.petrinet.elements.extensions;

public class TransitionTimeExtention {
    private double TPN_eft = 0;
    private double TPN_lft = 0;
    private double TPNtimerLimit = -1;
    private double TPNtimer = -1;
    private double DPNduration = 0;
    private double DPNtimer = -1;
    private boolean TPNactive = false;
    private boolean DPNactive = false;

    /**
     * Metoda ustala dolny limit niezerowego czasu gotowości - EFT.
     * @param value double - czas EFT
     */
    public void setEFT(double value) {
        if (value < 0) {
            this.TPN_eft = 0;
            return;
        }
        if (value > TPN_lft) {
            this.TPN_eft = TPN_lft;
            return;
        }
        this.TPN_eft = value;
    }

    /**
     * Na potrzeby wczytywania pliku projektu, bez porownania z LFT
     * @param value - double
     */
    public void forceSetEFT(double value) {
        if (value < 0) {
            this.TPN_eft = 0;
            return;
        }
        this.TPN_eft = value;
    }

    /**
     * Metoda pozwala odczytać przypisany czas EFT tranzycji.
     * @return double - czas EFT
     */
    public double getEFT() {
        return this.TPN_eft;
    }

    /**
     * Metoda ustala górny limit nieujemnego czasu krytycznego - LFT.
     * @param value double - czas LFT (deadline na uruchomienie)
     */
    public void setLFT(double value) {
        if (value < TPN_eft) {
            this.TPN_lft = TPN_eft;
            return;
        }

        this.TPN_lft = value;
    }

    /**
     * Metoda pozwala odczytać przypisany czas LFT tranzycji.
     * @return double - czas LFT
     */
    public double getLFT() {
        return this.TPN_lft;
    }

    /**
     * Metoda pozwala ustawic czas uruchomienia tranzycji.
     * @param value double - czas uruchomienia tranzycji
     */
    public void setTPNtimerLimit(double value) {
        TPNtimerLimit = value;
    }

    /**
     * Metoda zwraca aktualny czas uruchomienia.
     * @return double - czas uruchomienia - pole FireTime
     */
    public double getTPNtimerLimit() {
        return TPNtimerLimit;
    }

    /**
     * Metoda zwraca aktualny zegar uruchomienia dla tranzycji.
     * @return double - czas uruchomienia - pole FireTime
     */
    public double getTPNtimer() {
        return TPNtimer;
    }

    /**
     * Metoda pozwala ustawic zegar uruchomienia tranzycji.
     * @param value double - czas uruchomienia tranzycji
     */
    public void setTPNtimer(double value) {
        TPNtimer = value;
    }

    /**
     * Metoda ustawia nowy czas trwania odpalenia dla tranzycji DPN.
     * @param value double - nowy czas
     */
    public void setDPNduration(double value) {
        if (value < 0)
            DPNduration = 0;
        else
            DPNduration = value;
    }

    /**
     * Metoda zwraca ustawioną dla tranzycji DPN wartość duration.
     * @return double - czas trwania odpalenia tranzycji
     */
    public double getDPNduration() {
        return DPNduration;
    }

    /**
     * Metoda ustawia nowy wewnętrzny timer dla czasu odpalenia dla tranzycji DPN.
     * @param value double - nowa wartość zegara dla DPN
     */
    public void setDPNtimer(double value) {
        DPNtimer = value;
    }

    /**
     * Metoda zwraca aktualną wartość zegara odliczającego czas do odpalenia tranzycji DPN (produkcji tokenów).
     * @return double durationTimer -
     */
    public double getDPNtimer() {
        return DPNtimer;
    }

    /**
     * Metoda pozwalająca stwierdzić, czy tranzycja DPN jest gotowa do produkcji tokenów.
     * @return boolean - true, jeśli zegar DPN ma wartość równą ustalonemu czasowi DPN dla tranzycji
     */
    public boolean isDPNforcedToFire() {
        return DPNtimer >= DPNduration;
    }

    /**
     * Metoda informująca czy tranzycja TPN musi zostać uruchomiona.
     * @return boolean - true, jeśli wewnętrzny zegar (!= -1) jest równy deadlinowi dla TPN
     */
    public boolean isTPNforcedToFired() {
        if (TPNtimerLimit != -1) {
            return TPNtimerLimit == TPNtimer;
        } else {
            return false; //nieaktywna
        }
    }

    /**
     * Metoda resetuje zegary tranzycji, powinna być używana przez symulatory po tym, jak wyprodukowano
     * tokeny (faza II: AddTokens symulacji)
     */
    public void resetTimeVariables() {
        TPNtimerLimit = -1;
        TPNtimer = -1;
        DPNtimer = -1;
    }

    /**
     * Metoda włącza lub wyłącza tryb TPN
     * @param status boolean - true, jeśli tryb TPN ma być aktywny
     */
    public void setTPNstatus(boolean status) {
        TPNactive = status;
    }

    /**
     * Metoda zwraca stan aktywności trybu TPN
     * @return boolean - true, jeśli TPN aktywny
     */
    public boolean getTPNstatus() {
        return TPNactive;
    }

    /**
     * Metoda włącza lub wyłącza tryb DPN
     * @param status boolean - true, jeśli tryb DPN ma być aktywny
     */
    public void setDPNstatus(boolean status) {
        DPNactive = status;
    }

    /**
     * Metoda zwraca stan aktywności trybu DPN
     * @return boolean - true, jeśli DPN aktywny
     */
    public boolean getDPNstatus() {
        return DPNactive;
    }
}
