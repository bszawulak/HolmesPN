package holmes.analyse.firingalgo;

import holmes.petrinet.elements.Transition;

import java.util.BitSet;

class State {
    public boolean marked = false;
    public Transition transition;
    public Conflict conflict;
    public TokenState tokenState;

    public State(Transition transition) {
        this(transition, null, null);
    }

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
        return (tokenState == null || tokenState.isResolved())
                && (conflict == null || conflict.isResolved());
    }

    public Double getResult() {
        if(tokenState.isResolved()) {
            var conflictResult = conflict == null ? 1 : conflict.getResult();
            var tokenResult = tokenState == null ? 1 : tokenState.getTokens();
            return conflictResult * tokenResult;
        }
        return null;
    }

    public State copyForOtherTransition(Transition otherTransition) {
        var copiedConflict = conflict != null ? conflict.copyForOtherTransition(otherTransition) : null;
        var copiedTokenState = tokenState != null ? tokenState.copyForOtherTransition() : null;
        return new State(
                otherTransition,
                copiedConflict,
                copiedTokenState);
    }
}
