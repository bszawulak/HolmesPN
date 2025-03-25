package holmes.petrinet.elements.extensions;

import java.io.Serializable;

public class TransitionTimeExtention implements Serializable {
    private double TPN_eft = 0;
    private double TPN_lft = 0;
    private double TPNtimerLimit = -1;
    private double TPNtimer = -1;
    private double DPNduration = 0;
    private double DPNtimer = -1;
    private boolean isTPN = false;
    private boolean isDPN = false;

    /**
     * Metoda ustala dolny limit niezerowego czasu gotowości - EFT.
     * @param value (<b>double</b>) czas EFT.
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
     * @param value (<b>double</b>) nowa wartość EFT.
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
     * @return (<b>double</b>) - czas EFT.
     */
    public double getEFT() {
        return this.TPN_eft;
    }

    /**
     * Metoda ustala górny limit nieujemnego czasu krytycznego - LFT.
     * @param value (<b>double</b>) czas LFT (deadline na uruchomienie).
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
     * @return (<b>double</b>) - czas LFT.
     */
    public double getLFT() {
        return this.TPN_lft;
    }

    /**
     * Metoda pozwala ustawic czas uruchomienia tranzycji.
     * @param value (<b>double</b>) czas uruchomienia tranzycji.
     */
    public void setTPNtimerLimit(double value) {
        TPNtimerLimit = value;
    }

    /**
     * Metoda zwraca aktualny czas uruchomienia.
     * @return (<b>double</b>) - czas uruchomienia - pole FireTime.
     */
    public double getTPNtimerLimit() {
        return TPNtimerLimit;
    }

    /**
     * Metoda zwraca aktualny zegar uruchomienia dla tranzycji.
     * @return (<b>double</b>) - czas uruchomienia - pole FireTime.
     */
    public double getTPNtimer() {
        return TPNtimer;
    }

    /**
     * Metoda pozwala ustawic zegar uruchomienia tranzycji.
     * @param value (<b>double</b>) czas uruchomienia tranzycji.
     */
    public void setTPNtimer(double value) {
        TPNtimer = value;
    }

    /**
     * Metoda ustawia nowy czas trwania odpalenia dla tranzycji DPN.
     * @param value (<b>double</b>) nowy czas DPN.
     */
    public void setDPNduration(double value) {
        if (value < 0)
            DPNduration = 0;
        else
            DPNduration = value;
    }

    /**
     * Metoda zwraca ustawioną dla tranzycji DPN wartość duration.
     * @return (<b>double</b>) - czas trwania odpalenia tranzycji.
     */
    public double getDPNduration() {
        return DPNduration;
    }

    /**
     * Metoda ustawia nowy wewnętrzny timer dla czasu odpalenia dla tranzycji DPN.
     * @param value (<b>double</b>) nowa wartość zegara dla DPN.
     */
    public void setDPNtimer(double value) {
        DPNtimer = value;
    }

    /**
     * Metoda zwraca aktualną wartość zegara odliczającego czas do odpalenia tranzycji DPN (produkcji tokenów).
     * @return (<b>double</b>) - licznik DPNtimer.
     */
    public double getDPNtimer() {
        return DPNtimer;
    }

    /**
     * Metoda pozwalająca stwierdzić, czy tranzycja DPN jest gotowa do produkcji tokenów.
     * @return (<b>boolean</b>) - true, jeśli zegar DPN ma wartość równą ustalonemu czasowi DPN dla tranzycji.
     */
    public boolean isDPNforcedToFire() {
        return DPNtimer >= DPNduration;
    }

    /**
     * Metoda informująca czy tranzycja TPN musi zostać uruchomiona.
     * @return (<b>boolean</b> - true, jeśli wewnętrzny zegar (!= -1) jest równy deadlinowi dla TPN.
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
     * @param status (<b>boolean</b> true, jeśli tryb TPN ma być aktywny.
     */
    public void setTPNstatus(boolean status) {
        isTPN = status;
    }

    /**
     * Metoda zwraca stan aktywności trybu TPN
     * @return (<b>boolean</b> true, jeśli TPN aktywny.
     */
    public boolean isTPN() {
        return isTPN;
    }

    /**
     * Metoda włącza lub wyłącza tryb DPN
     * @param status (<b>boolean</b> true, jeśli tryb DPN ma być aktywny.
     */
    public void setDPNstatus(boolean status) {
        isDPN = status;
    }

    /**
     * Metoda zwraca stan aktywności trybu DPN
     * @return (<b>boolean</b> true, jeśli DPN aktywny.
     */
    public boolean isDPN() {
        return isDPN;
    }
}
