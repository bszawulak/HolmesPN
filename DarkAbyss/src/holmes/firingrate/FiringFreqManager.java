package holmes.firingrate;

import holmes.petrinet.data.PetriNet;
import holmes.petrinet.elements.Arc;
import holmes.petrinet.elements.Place;
import holmes.petrinet.elements.Transition;

import java.util.*;

public class FiringFreqManager {
    private PetriNet pn;

    public FiringFreqManager(PetriNet net) {
        this.pn = net;
    }
    /**
     * Metoda zwraca wszytskie tranzycje źródłowe w sieci. Chyba bardziej pasuje do PetriNet.
     * @param transitions ArrayList<Transition>  - lista tranzycji do sprawdzenia
     * @return ArrayList<Transition> - tablica zawierające tranzycje źródłowe
     */
    public ArrayList<Transition> getTsource(ArrayList<Transition> transitions) {
        ArrayList<Transition> firstTransitions = new ArrayList<>();
        for (Transition tr : transitions) {
            if (tr.getPrePlaces().isEmpty()) {
                firstTransitions.add(tr);
            }
        }
        return firstTransitions;
    }

    /**
     * Algorytm slużący do wyznaczenia częstości uruchomienia.
     * Przechodzi po sieci, a na łukach zapisuje ograniczenia wg określonych zasad.
     */
    public void retentionFreeAlgorithm() {
        ArrayList<Transition> transitions, firstTransitions;
        Set<Integer> donePlacesId = new HashSet<>();
        Set<Integer> doneTransitionsId = new HashSet<>();
        ArrayList<Place> nextPlaces = new ArrayList<>();

        transitions = pn.getTransitions();
        for (Arc arc : pn.getArcs()) {
            arc.setConstraints(null);
            arc.setVisited(false);
        }

        firstTransitions = getTsource(transitions);

        for (Transition firstTransition : firstTransitions) {
            for (Arc outArc : firstTransition.getOutArcs()) {
                ArrayList<Constraint> conditionalExpressions = new ArrayList<>();
                Constraint conditionalExpr = new Constraint();
                conditionalExpr.setTransition(firstTransition);
                conditionalExpr.setBeta(outArc.getWeight());
                conditionalExpressions.add(conditionalExpr);
                outArc.setConstraints(conditionalExpressions);
            }
            doneTransitionsId.add(firstTransition.getID());
        }

        for (Transition tr : firstTransitions) {
            if (!tr.getPostPlaces().isEmpty()) {
                nextPlaces.addAll(checkNextPlaces(doneTransitionsId, tr, donePlacesId));
            }
        }

        while (nextPlaces.size() > 0) {
            Place nextPlace = nextPlaces.remove(0);
            ArrayList<Transition> completedTransitions = inspectPlaceRetentionFree(nextPlace);
            donePlacesId.add(nextPlace.getID());
            for (Transition completedTransition : completedTransitions) {
                doneTransitionsId.add(completedTransition.getID());
            }
            for (Transition completedTransition : completedTransitions) {
                nextPlaces.addAll(checkNextPlaces(doneTransitionsId, completedTransition, donePlacesId));
            }
        }
    }

    /**
     * Pomocnicza metoda przeglądająca dane miejsce dla algorytmu wyżej
     */
    private ArrayList<Transition> inspectPlaceRetentionFree(Place p) {
        ArrayList<Transition> doneTransitions = new ArrayList<>();
        if (!p.getOutNodes().isEmpty()) {
            int outTrans = p.getPostTransitions().size();
            ArrayList<Constraint> constraints = new ArrayList<>();

            //sumowanie T wejściowych  - wzór (2)
            for (Arc inArc : p.getInArcs()) {
                constraints.addAll(inArc.getConstraints());
            }

            for (Arc outArc : p.getOutArcs()) {
                int alpha = outArc.getWeight();
                ArrayList<Constraint> arcConstraint = new ArrayList<>();

                // dodanie wspołczynnika alpha do nierówności
                for (Constraint constraint : constraints) {
                    Constraint cnst = new Constraint();
                    cnst.setTransition(constraint.getTransition());
                    cnst.setBeta(constraint.getBeta());
                    cnst.setAlpha(constraint.getAlpha() * alpha);

                    ArrayList<Arc> probsArc = new ArrayList<>(constraint.getConflictArcs());
                    
                    //konflikt - wzór (4), dodanie prawdopodobieństwa do nierówności;
                    if (outTrans > 1) {
                        probsArc.add(outArc);
                    }
                    cnst.setConflictArcs(probsArc);
                    arcConstraint.add(cnst);
                }
                outArc.setConstraints(arcConstraint);
                outArc.setVisited(true);
            }

            //wyznaczenie zbioru ukończonych tranzycji dla analizowanego miejsca
            for (Transition postTransition : p.getPostTransitions()) {
                ArrayList<Arc> inArcs = postTransition.getInArcs();
                if (inArcs.size() > 1) {
                    ArrayList<Constraint> tempConstraint;
                    boolean readyToProceed = checkIfReady(inArcs);

                    if (readyToProceed) {
                        tempConstraint = inArcs.get(0).getConstraints();
                        for (Arc inArc : inArcs) {
                            if (tempConstraint.size() > inArc.getConstraints().size()) {
                                tempConstraint = inArc.getConstraints();
                            }
                        }
                        for (Arc outArc : postTransition.getOutArcs()) {
                            int beta = outArc.getWeight();
                            ArrayList<Constraint> arcConstraint = new ArrayList<>();
                            // dodanie wspołczynnika beta do nierówności
                            for (Constraint ce : tempConstraint) {
                                Constraint cnst = new Constraint();
                                cnst.setTransition(ce.getTransition());
                                cnst.setAlpha(ce.getAlpha());
                                cnst.setBeta(ce.getBeta() * beta);

                                ArrayList<Arc> probsArc = new ArrayList<>(ce.getConflictArcs());
                                cnst.setConflictArcs(probsArc);

                                arcConstraint.add(cnst);
                            }
                            outArc.setConstraints(arcConstraint);
                        }
                        doneTransitions.add(postTransition);
                    }
                } else {
                    for (Arc outArc : postTransition.getOutArcs()) {
                        ArrayList<Constraint> arcConstraint = new ArrayList<>();
                        for (Constraint constraint : inArcs.get(0).getConstraints()) {
                            Constraint cnst = new Constraint();
                            cnst.setTransition(constraint.getTransition());
                            cnst.setAlpha(constraint.getAlpha());
                            cnst.setBeta(constraint.getBeta() * outArc.getWeight());
                            ArrayList<Arc> probsArc = new ArrayList<>(constraint.getConflictArcs());
                            cnst.setConflictArcs(probsArc);
                            arcConstraint.add(cnst);
                        }
                        outArc.setConstraints(arcConstraint);
                    }
                    doneTransitions.add(postTransition);
                }
            }
        }
        return doneTransitions;
    }


    /**
     * Algorytm slużący do wyznaczenia częstości uruchomienia.
     * Przechodzi po sieci i na bieżąco zapisuje wartości odpaleń. Gorsza wersja poprzedniego.
     */
    public int equilibriumAlgorithm() {
        ArrayList<Transition> transitions = pn.getTransitions();
        ArrayList<Transition> firstTransitions = new ArrayList<>();
        Set<Integer> donePlacesId = new HashSet<>();
        Set<Integer> doneTransitionsId = new HashSet<>();
        ArrayList<Place> nextPlaces = new ArrayList<>();
        for (Transition tr : transitions) {
            if (tr.getPrePlaces().isEmpty()) {
                firstTransitions.add(tr);
                doneTransitionsId.add(tr.getID());
            }
        }
        for (Transition tr : firstTransitions) {
            if (!tr.getPostPlaces().isEmpty()) {
                nextPlaces.addAll(checkNextPlaces(doneTransitionsId, tr, donePlacesId));
            }
        }

        while (nextPlaces.size() > 0) {
            Place nextPlace = nextPlaces.remove(0);
            ArrayList<Transition> completedTransitions;
            try {
                completedTransitions = inspectPlaceEquilibrium(nextPlace);
            } catch (Exception e) {
                //algorithm cannot proceed
                return doneTransitionsId.size();
            }
            donePlacesId.add(nextPlace.getID());
            for (Transition completedTransition : completedTransitions) {
                doneTransitionsId.add(completedTransition.getID());
            }
            for (Transition completedTransition : completedTransitions) {
                nextPlaces.addAll(checkNextPlaces(doneTransitionsId, completedTransition, donePlacesId));
            }
        }
        return doneTransitionsId.size();
    }

    /**
     * Analogicznie -  pomocnicza metoda przeglądająca dane miejsce dla algorytmu wyżej
     */
    private ArrayList<Transition> inspectPlaceEquilibrium(Place p) throws Exception {
        ArrayList<Transition> doneTransitions = new ArrayList<>();
        if (!p.getOutNodes().isEmpty()) {
            double betaCount = 0;
            double alphaSquared = 0;

            for (Transition tr : p.getPreTransitions()) {
                betaCount += tr.getOutArcWeightTo(p) * tr.getFiringRate();
            }
            for (Transition tr : p.getPostTransitions()) {
                int weight = tr.getInArcWeightFrom(p);
                alphaSquared += weight * weight;
            }

            double singleAlpha = betaCount / alphaSquared;
            for (Transition postTransition : p.getPostTransitions()) {
                ArrayList<Arc> inArcs = postTransition.getInArcs();

                if (inArcs.size() > 1) {
                    boolean readyToCalculateFlag = true;
                    for (Arc inArc : inArcs) {
                        if (inArc.getStartNode().getID() == p.getID()) {
                            inArc.setTempFiringFreq(singleAlpha * postTransition.getInArcWeightFrom(p));
                        } else {
                            if (inArc.getTempFiringFreq() == 0) {
                                readyToCalculateFlag = false;
                            }
                        }
                    }
                    if (readyToCalculateFlag) {
                        double firingRate = inArcs.get(0).getTempFiringFreq();
                        for (int i = 1; i < inArcs.size(); i++) {
                            if (firingRate != inArcs.get(i).getTempFiringFreq()) {
                                throw new Exception("Cannot proceed");
                            }
                        }
                        postTransition.setFiringRate(firingRate);
                        doneTransitions.add(postTransition);
                    }
                } else {
                    postTransition.setFiringRate(singleAlpha * postTransition.getInArcWeightFrom(p));
                    doneTransitions.add(postTransition);
                }
            }
        }
        return doneTransitions;

    }

    /**
     * Metoda służąca do wyznaczenia kolejnych miejsc do przeglądnięcia
     */
    public ArrayList<Place> checkNextPlaces(Set<Integer> doneTransitionsId, Transition tr, Set<Integer> donePlacesId) {
        ArrayList<Place> nextPlaces = new ArrayList<>();
        for (Place postPlace : tr.getPostPlaces()) {
            Set<Integer> preTransitionsId = new HashSet<>();
            for (Transition preTransition : postPlace.getPreTransitions()) {
                preTransitionsId.add(preTransition.getID());
            }
            if (doneTransitionsId.containsAll(preTransitionsId) && !donePlacesId.contains(postPlace.getID())) {
                nextPlaces.add(postPlace);
            }
        }
        return nextPlaces;
    }

    /**
     * Czy wszystkie łuki wejściowe dla tranzycji zostały sprawdzone
     */
    private boolean checkIfReady(ArrayList<Arc> inArcs){
        for (Arc inArc : inArcs) {
            if (!inArc.isVisited()) {
                return false;
            }
        }
        return true;
    }
}



