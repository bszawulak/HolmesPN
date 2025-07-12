package holmes.analyse.firingalgo;

import holmes.petrinet.elements.Transition;

class State {
    public Transition transition;
    public Conflict conflict;
    public TokenState tokenState;

    public State(Transition transition, TokenSource tokenSource) {
        this(transition, null, new TokenState(tokenSource));
    }

    public State(Transition transition, Conflict conflict) {
        this(transition, conflict, null);
    }

    public State(Transition transition, Conflict conflict, TokenState tokenState) {
        this.transition = transition;
        this.conflict = conflict;
        this.tokenState = tokenState;
    }

    public boolean isResolved() {
        return tokenState.isResolved() && conflict.isResolved();
    }

    public Double getResult() {
        if(tokenState.isResolved()) {
            return conflict.getResult() * tokenState.getTokens();
        }
        return null;
    }

    public State copyForOtherTransition(Transition otherTransition) {
        return new State(
                otherTransition,
                conflict.copyForOtherTransition(transition),
                tokenState.copyForOtherTransition());
    }
}
