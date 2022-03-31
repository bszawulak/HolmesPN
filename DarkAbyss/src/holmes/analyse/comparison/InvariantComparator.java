package holmes.analyse.comparison;

import holmes.petrinet.data.PetriNet;
import holmes.petrinet.elements.Node;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class InvariantComparator {

    public PetriNet pn1;
    public PetriNet pn2;
    public HashMap<Node, Node> matchedVertices = new HashMap<>();

    public InvariantComparator(PetriNet p1, PetriNet p2) {
        this.pn1 = p1;
        this.pn2 = p2;
    }

    public HashMap<Integer, Integer> idealInvariantMatching() {
        HashMap<Integer, Integer> ideallyMatchedInvariants = new HashMap<>();

        for (int in1 = 0; in1 < pn1.getT_InvMatrix().size(); in1++) {
            ArrayList<Integer> invariant1 = pn1.getT_InvMatrix().get(in1);
            for (int in2 = 0; in2 < pn2.getT_InvMatrix().size(); in2++) {
                if (!ideallyMatchedInvariants.values().contains(in2)) {
                    ArrayList<Integer> invariant2 = pn2.getT_InvMatrix().get(in2);
                    int invariantSize = 0;
                    int invariantOverlap = 0;
                    for (int i = 0; i < invariant1.size(); i++) {
                        Node matched = matchedVertices.get(pn1.getTransitions().get(i));
                        int index = pn2.getTransitions().indexOf(matched);
                        if (invariant1.get(i) != 0) {
                            invariantSize++;
                            if (index != -1 && invariant2.get(index) == invariant1.get(i)) {
                                invariantOverlap++;
                            }
                        }
                    }
                    if (invariantSize == invariantOverlap && invariantSize != 0 && invariantOverlap != 0) {
                        ideallyMatchedInvariants.put(in1, in2);

                    }

                }
            }
        }
        return ideallyMatchedInvariants;
    }

    public HashMap<Integer, Integer> bestInvariantMatching() {
        //TODO do testu/porawy

        HashMap<Integer, Integer> bestMatchedInvariants = new HashMap<>();

        for (int in1 = 0; in1 < pn1.getT_InvMatrix().size(); in1++) {
            ArrayList<Integer> invariant1 = pn1.getT_InvMatrix().get(in1);

            ArrayList<Integer> resultsForOverlaping = new ArrayList<>();
            for (int in2 = 0; in2 < pn2.getT_InvMatrix().size(); in2++) {
                if (!bestMatchedInvariants.values().contains(in2)) {
                    ArrayList<Integer> invariant2 = pn2.getT_InvMatrix().get(in2);
                    int invariantSize = 0;
                    int invariantOverlap = 0;
                    for (int i = 0; i < invariant1.size(); i++) {
                        Node matched = matchedVertices.get(pn1.getTransitions().get(i));
                        int index = pn2.getTransitions().indexOf(matched);
                        if (invariant1.get(i) != 0) {
                            invariantSize++;
                            if (index != -1 && invariant2.get(index) == invariant1.get(i)) {
                                invariantOverlap++;
                            }
                        }
                    }
                    resultsForOverlaping.add(invariantOverlap);
                }
            }
            int bestMatch = resultsForOverlaping.indexOf(Collections.max(resultsForOverlaping));
            bestMatchedInvariants.put(in1, bestMatch);
        }
        return bestMatchedInvariants;
    }

    public HashMap<Node, Node> matchVertices(int type) {

        boolean dupication = false;
        for (Node n : pn1.getTransitions())
            for (Node m : pn1.getTransitions())
                if (n.getID() != m.getID() && n.getName().equals(m.getName())) {
                    JOptionPane.showMessageDialog(null, "In the net 1 there exists at least 2 nodes with the same name", "ErrorBox: name duplication", JOptionPane.ERROR_MESSAGE);
                }

        for (Node n : pn2.getTransitions())
            for (Node m : pn2.getTransitions())
                if (n.getID() != m.getID() && n.getName().equals(m.getName())) {
                    JOptionPane.showMessageDialog(null, "In the net 2 there exists at least 2 nodes with the same name", "ErrorBox: name duplication", JOptionPane.ERROR_MESSAGE);
                }

        if (!dupication)
            if (type == 0) {
                for (Node n : pn1.getTransitions()) {
                    for (Node m : pn2.getTransitions()) {
                        if (n.getName().equals(m.getName())) {
                            matchedVertices.put(n, m);
                        }
                    }
                }
            } else if (type == 1) {
                for (Node n : pn1.getTransitions()) {
                    ArrayList<Integer> distance = new ArrayList<>();
                    for (Node m : pn2.getTransitions()) {
                        distance.add(LevenshteinDistanceDP.compute_Levenshtein_distanceDP(n.getName(), m.getName()));
                    }

                    int index = distance.indexOf(Collections.min(distance));

                    matchedVertices.put(n, pn2.getTransitions().get(index));

                }
            }

        return matchedVertices;
    }
}
