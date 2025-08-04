package holmes.analyse.firingalgo;

import holmes.analyse.firingalgo.petrinetstructure.Transition;

class State {
    public boolean marked = false;
    public Transition transition;
    public Conflict conflict;
    public TokenState tokenState;

    public State(Transition transition) {
        this(transition, null, null);
    }

    public State(Transition transition, TokenState tokenState) {
        this(transition, null, tokenState);
    }

    public State(Transition transition, Conflict conflict) {
        this(transition, conflict, null);
    }

    public State(Transition transition, Conflict conflict, TokenState tokenState) {
        this.transition = transition;
        this.conflict = conflict;
        this.tokenState = tokenState;
    }

    public void updateWeights(double multipier) {
        if(conflict != null) {
            conflict.weight /= multipier;
        }
        if (tokenState != null) {
            tokenState.multiplier *= multipier;
        }
    }

    public boolean isResolved() {
        return (tokenState != null && tokenState.isResolved())
                && (conflict == null || conflict.isResolved());
    }

    public Double getResult() {
        if(tokenState != null && tokenState.isResolved()) {
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
