package holmes.firingrate;

import holmes.petrinet.elements.Arc;
import holmes.petrinet.elements.Place;
import holmes.petrinet.elements.Transition;

import java.util.*;

public class ExpressionBuilder {

    public ArrayList<ArrayList<String>> printWithParams(ArrayList<Transition> transitionsList,
                                                        ArrayList<Arc> conflictArcs) {

        ArrayList<ArrayList<String>> rowVectors = new ArrayList<ArrayList<String>>();
        ArrayList<ArrayList<String>> equalitiesSet = new ArrayList<ArrayList<String>>();
        for (Transition transition : transitionsList) {
            HashSet<String> tSyncCheck = new HashSet<>();
            HashSet<String> inequalitySet = new HashSet<>();
            for (Arc inputArc : transition.getInArcs()) {
                ArrayList<Constraint> constraints = inputArc.getConstraints();
                if (constraints != null) {
                    String leftValue = functionWithParams(constraints, transitionsList, conflictArcs);
                    String inequality = leftValue + " <= f" + transitionsList.lastIndexOf(inputArc.getEndNode());
                    inequalitySet.add(inequality);
                    tSyncCheck.add(leftValue);
                }
            }
            for (String s : inequalitySet) {
                rowVectors.add(new ArrayList<>(Arrays.asList(s)));
            }
            if (equalitiesSet.size() == 0 && tSyncCheck.size() > 1) {
                String main = "";
                for (String s : tSyncCheck) {
                    if (main.equals("")) {
                        main = s;
                    } else {
                        equalitiesSet.add(new ArrayList<String>(Arrays.asList(main, s)));
                    }
                }
            }
            String main = "";
            for (String s : tSyncCheck) {
                if (main.equals("")) {
                    main = s;
                } else {
                    ArrayList<String> vector = new ArrayList<>(Arrays.asList(main, s));
                    if (isUnique(equalitiesSet, vector)) {
                        equalitiesSet.add(vector);
                    }
                }
            }
        }
        for (ArrayList<String> strings : equalitiesSet) {
            rowVectors.add(new ArrayList<>(Arrays.asList(String.join(" = ", strings))));
        }
        return rowVectors;
    }

    private boolean isUnique(ArrayList<ArrayList<String>> equalitiesSet, ArrayList<String> vector) {
        for (int i = 0; i < equalitiesSet.size(); i++) {
            if (equalitiesSet.get(i).containsAll(vector)) {
                return false;
            }
        }
        return true;
    }

    private String functionWithParams(ArrayList<Constraint> constraints, ArrayList<Transition> transitionsList,
                                      ArrayList<Arc> conflictArcs) {
        ArrayList<String> functionsToAdd = new ArrayList<>();

        for (int i = 0; i < constraints.size(); i++) {
            double deno = constraints.get(i).getAlpha();
            double num = constraints.get(i).getBeta();
            int Tsource = transitionsList.lastIndexOf(constraints.get(i).getTransition());

            ArrayList<String> probs = new ArrayList<>();
            for (Arc conflictArc : constraints.get(i).getConflictArcs()) {
                for (int j = 0; j < conflictArcs.size(); j++) {
                    if (conflictArcs.get(j).getID() == conflictArc.getID()) {
                        probs.add("s" + j);
                        break;
                    }
                }
            }
            String res = "";
            double ratio = num / deno;
            res += ratio == 1 ? "" : ratio + " * ";
            res += probs.size() == 0 ? "" : String.join(" * ", probs) + " * ";
            res += "f" + Tsource;
            functionsToAdd.add(res);
        }

        return String.join(" + ", functionsToAdd);
    }

    /*
    Zwraca indeks nierówności
     */
    private Integer isFunctionInEquations(HashMap<String, ArrayList<Constraint>> sameEquals,
                                          ArrayList<HashMap<String, ArrayList<Constraint>>> equations
    ) {
        Set<String> functions = sameEquals.keySet();
        for (int i = 0; i < equations.size(); i++) {
            HashMap<String, ArrayList<Constraint>> setOfEq = equations.get(i);
            for (String function : functions) {
                if (setOfEq.containsKey(function)) {
                    return i;
                }
            }
        }
        return -1;
    }

    private boolean allArcsCorrect(ArrayList<Arc> conflictArcs) {
        HashMap<Integer,Double> probCheck = new HashMap<>();
        double accuracy = 0.05;
        for (Arc conflictArc : conflictArcs) {
            if (conflictArc.getfProbability() == 0) {
                return false;
            }
            int id = conflictArc.getStartNode().getID();
            if(probCheck.containsKey(id)) {
                probCheck.put(id,probCheck.get(id)+conflictArc.getfProbability());
            } else {
                probCheck.put(id,conflictArc.getfProbability());
            }
        }
        for (Double value : probCheck.values()) {
            if (value < 1 - accuracy || value > 1 + accuracy) {
                return false;
            }
        }
        return true;
    }

    public ArrayList<ArrayList<String>> calculateAndPrint(ArrayList<Transition> transitionsList,
                                                          ArrayList<Arc> conflictArcs) throws ConstraintException {

        ArrayList<ArrayList<String>> rowVectors = new ArrayList<ArrayList<String>>();

        HashMap<Integer, Double> transitionsValue = new HashMap<>();
        ArrayList<Transition> firstTransitions = new ArrayList<>();
        HashSet<Integer> toCalculate = new HashSet<>();
        HashSet<Integer> calculated = new HashSet<>();
        ArrayList<HashMap<String, ArrayList<Constraint>>> equations = new ArrayList<>();

        for (Transition transition : transitionsList) {

            HashMap<String, ArrayList<Constraint>> sameEquals = new HashMap<>();
            if (transition.getInArcs().size() > 1) {
                for (Arc inputArc : transition.getInArcs()) {
                    ArrayList<Constraint> constraints = inputArc.getConstraints();
                    if (constraints != null) {
                        String leftValue = functionWithParams(constraints, transitionsList, conflictArcs);
                        sameEquals.put(leftValue, constraints);
                    }
                }
                if (equations.size() == 0) {
                    equations.add(sameEquals);
                } else {
                    int index = isFunctionInEquations(sameEquals, equations);
                    if (index != -1) {
                        equations.get(index).putAll(sameEquals);
                    } else {
                        equations.add(sameEquals);
                    }
                }
            }
            if (transition.getPrePlaces().isEmpty() && transition.getFiringRate() == 0) {
                firstTransitions.add(transition);
            }
        }
        if (allArcsCorrect(conflictArcs)) {
            ArrayList<Double> results = new ArrayList<Double>(Collections.nCopies(equations.size(), 0.0));
            boolean checkAgain = true;
            while (checkAgain) {
                checkAgain = false;
                for (int i = 0; i < equations.size(); i++) {
                    HashMap<String, ArrayList<Constraint>> localEquations = equations.get(i);
                    Collection<ArrayList<Constraint>> equationToCheck = localEquations.values();

                    for (ArrayList<Constraint> constraints : equationToCheck) {
                        double sumOfFunctions = 0;
                        HashSet<Integer> noInfo = new HashSet<>();
                        ArrayList<Constraint> noInfoConstraint = new ArrayList<>();
                        for (int k = 0; k < constraints.size(); k++) {
                            double sourceFiringRate = constraints.get(k).getTransition().getFiringRate();
                            if (sourceFiringRate == 0) {
                                noInfo.add(constraints.get(k).getTransition().getID());
                                noInfoConstraint.add(constraints.get(k));
                                toCalculate.add(constraints.get(k).getTransition().getID());
                            } else {
                                sumOfFunctions += calcWithFRate(constraints.get(k), conflictArcs);
                            }
                        }
                        if (noInfo.isEmpty()) {
                            if (results.get(i) == 0) {
                                results.set(i, sumOfFunctions);
                                checkAgain = true;
                            }
                            if (results.get(i) != 0 && results.get(i) != sumOfFunctions) {
                                throw new ConstraintException("Values do not match!");
                            }
                        } else {
                            if (results.get(i) != 0 && noInfo.size() == 1) {
                                double missingValues = 0;
                                for (int i1 = 0; i1 < noInfoConstraint.size(); i1++) {
                                    missingValues += calcNotFRate(noInfoConstraint.get(i1), conflictArcs);
                                }
                                double diff = results.get(i) - sumOfFunctions;
                                if (diff <= 0) {
                                    throw new ConstraintException("Values do not match!");
                                }
                                noInfoConstraint.get(0).getTransition().setFiringRate(diff / missingValues);
                                checkAgain = true;
                                calculated.add(noInfoConstraint.get(0).getTransition().getID());
                            }
                        }
                    }
                }
            }
        } else {
            throw new ConstraintException("Probabilities are not correct!");
        }
        if (!calculated.equals(toCalculate)) {
            throw new ConstraintException("Provide more values!");
        }

        for (Transition transition : transitionsList) {
            HashSet<String> inequalitySet = new HashSet<>();
            for (Arc inputArc : transition.getInArcs()) {
                double sumOfFunctions = 0;
                ArrayList<Constraint> constraints = inputArc.getConstraints();
                ArrayList<String> functionsToAdd = new ArrayList<>();
                if (constraints != null) {
                    for (int i = 0; i < constraints.size(); i++) {
                        if(constraints.get(i).getTransition().getFiringRate() == 0 ){
                            double ratio = calcNotFRate(constraints.get(i), conflictArcs);
                            String res = "";
                            res += ratio == 1 ? "" : ratio + " * ";
                            res += "f" + transitionsList.lastIndexOf(constraints.get(i).getTransition());
                            functionsToAdd.add(res);
                        } else {
                            sumOfFunctions += calcWithFRate(constraints.get(i), conflictArcs);
                        }
                    }
                    String inequality;
                    if(!functionsToAdd.isEmpty()) {
                        if (sumOfFunctions != 0 ) {
                            functionsToAdd.add(String.valueOf(sumOfFunctions));
                        }

                        inequality = String.join(" + ", functionsToAdd) + " <= f" + transitionsList.lastIndexOf(inputArc.getEndNode());
                    } else {
                        inequality = sumOfFunctions + " <= f" + transitionsList.lastIndexOf(inputArc.getEndNode());
                        transitionsValue.put(transition.getID(), sumOfFunctions);
                    }

                    inequalitySet.add(inequality);
                }
            }

            for (String s : inequalitySet) {
                ArrayList<String> rowVector = new ArrayList<>();
                rowVector.add(s);
                rowVectors.add(rowVector);
            }


        }

        for (Transition firstTransition : firstTransitions) {
            if(calculated.contains(firstTransition.getID())) {
                String s = "f" + transitionsList.lastIndexOf(firstTransition) + " = " + firstTransition.getFiringRate();
                ArrayList<String> rowVector = new ArrayList<>();
                rowVector.add(s);
                rowVectors.add(rowVector);
            }
        }



        for (Transition transition : transitionsList) {
            if (transitionsValue.get(transition.getID()) != null) {
                transition.setFiringRate(transitionsValue.get(transition.getID()));
            }
        }

        return rowVectors;
    }

    private double calcNotFRate(Constraint constraint, ArrayList<Arc> conflictArcs) {
        ArrayList<Double> res = paramsValue(constraint, conflictArcs);
        return res.get(1) / res.get(0);
    }

    private double calcWithFRate(Constraint constraint, ArrayList<Arc> conflictArcs) {
        ArrayList<Double> res = paramsValue(constraint, conflictArcs);
        double sourceFiringRate = constraint.getTransition().getFiringRate();
        double deno = res.get(0);
        double num = res.get(1);
        num *= sourceFiringRate;
        return num / deno;
    }


    private ArrayList<Double> paramsValue(Constraint constraint, ArrayList<Arc> conflictArcs) {
        double deno = constraint.getAlpha();
        double num = constraint.getBeta();

        ArrayList<Double> probResult = probValues(constraint, conflictArcs);
        deno *= probResult.get(0);
        num *= probResult.get(1);

        return new ArrayList<>(Arrays.asList(deno, num));
    }

    private ArrayList<Double> probValues(Constraint constraint, ArrayList<Arc> conflictArcs) {
        double num = 1, deno = 1;
        for (Arc conflictArc : constraint.getConflictArcs()) {
            for (int j = 0; j < conflictArcs.size(); j++) {
                if (conflictArcs.get(j).getID() == conflictArc.getID()) {
                    Place pl = (Place) conflictArcs.get(j).getStartNode();
                    double sum = 0;
                    for (Arc outArc : pl.getOutArcs()) {
                        sum += outArc.getWeight() * outArc.getfProbability();
                    }
                    deno *= sum;
                    num *= conflictArc.getfProbability() * conflictArc.getWeight();
                    break;
                }
            }
        }
        return new ArrayList<>(Arrays.asList(deno, num));
    }

}
