package holmes.analyse.firingalgo;

class TokenSource {
    public Double firingRate;

    public TokenSource(Double firingRate) {
        this.firingRate = firingRate;
    }

    public boolean isResolved() {
        return firingRate != null;
    }
}
