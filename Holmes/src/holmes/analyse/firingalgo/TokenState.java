package holmes.analyse.firingalgo;

class TokenState {
    private TokenSource tokenSource;
    public double multiplier;

    public TokenState(TokenSource tokenSource) {
        this(tokenSource, 1.0);
    }

    public TokenState(TokenSource tokenSource, double multiplier) {
        this.tokenSource = tokenSource;
        this.multiplier = multiplier;
    }

    public boolean isResolved() {
        return tokenSource.isResolved();
    }

    public Double getTokens() {
        if (!isResolved()) {
            return null;
        }
        return tokenSource.firingRate * multiplier;
    }

    public TokenState copyForOtherTransition() {
        return new TokenState(tokenSource, multiplier);
    }

    public void setTokenSourceValue(double value) {
        tokenSource.firingRate = value/multiplier;
    }
}
