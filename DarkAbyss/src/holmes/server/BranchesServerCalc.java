package holmes.server;

import holmes.analyse.comparison.structures.BranchVertex;
import holmes.files.io.IOprotocols;
import holmes.petrinet.data.PetriNet;
import holmes.petrinet.elements.Arc;
import holmes.petrinet.elements.Node;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public class BranchesServerCalc {

    public int index_i = 0;
    public int index_j = 0;
    public int index_p = 0;
    public int index_d = 0;

    public BranchesServerCalc() {
    }

    public void Compare() {
        for (int d = 99; d < 302; d++) {
            int i = 40;
            int j = 40;
            for (int p = 0; p < 100; p++) {

                PetriNet pn1 = loadNet("/home/bartek/Eksperyment/Wyniki/d" + index_d + "i40j40/d" + index_d + "i40j40p" + index_p + "/d" + index_d + "i40j40p" + index_p + "-BASE.pnt", 0);
                //PetriNet pn2 = loadNet("/home/Szavislav/i0j0p0-S4VARIANT.pnt", 1);
                PetriNet pn2 = loadNet("/home/bartek/Eksperyment/Wyniki/d" + index_d + "i40j40/d" + index_d + "i40j40p" + index_p + "/d" + index_d + "i40j40p" + index_p + "-1S.pnt", 1);

                ArrayList<BranchVertex> lbv1 = calcBranches(pn1);
                ArrayList<BranchVertex> lbv2 = calcBranches(pn2);


                index_d = d;
                index_p = p;
                //is type, degree and endpoints type the same
                comparison0(lbv1, lbv2);
                comparison1(lbv1, lbv2);
                comparison2(lbv1, lbv2);
                comparison3(lbv1, lbv2);
                //---------------------
                comparison4(lbv1, lbv2);
            }
        }

        // variant I
        /**
         * znajdź największy zawierający się
         * sprawdź typy endpointów
         */

        //comparisonI(lbv1, lbv2);

        // variant II
        /**
         * znajdź największy zawierający się
         * sprawdź typy i stopnie endpointów tak samo zawierające się
         */

        //comparisonII(lbv1, lbv2);

        // variant III
        /**
         * znajdzź idealne dopasowanie bv
         * sprawdź typy i stopnie endpointów t"najlepsze"
         * znajdź najlepsze dopasowanie pozostałych bv
         * sprawdź typy i stopnie endpointów t"najlepsze"
         */

        //comparisonIII(lbv1, lbv2);

        // variant IV
        /**
         * znajdzź idealne dopasowanie bv
         * sprawdź typy i stopnie endpointów t"idealne"
         * znajdź najlepsze dopasowanie pozostałych bv
         * sprawdź typy i stopnie endpointów t"najlepsze"
         */

        //comparisonIV(lbv1, lbv2);

        // variant V
        /**
         * znajdzź idealne dopasowanie bv
         * sprawdź typy i stopnie endpointów t"idealne"
         * znajdź najlepsze dopasowanie pozostałych bv
         * sprawdź typy i stopnie endpointów t"idealne"
         */

        //comparisonV(lbv1, lbv2);

        // variant V
        /**
         * znajdzź idealne dopasowanie bv
         * sprawdź typy i ISTOTNOŚĆ endpointów
         * znajdź najlepsze dopasowanie pozostałych bv
         * sprawdź typy i ISTOTNOŚĆ endpointów
         */

        // variant VI
        /**
         * znajdzź najlepsze dopasowanie bv
         * sprawdź typy i ISTOTNOŚĆ endpointów
         */

    }

    private void comparisonV(ArrayList<BranchVertex> lbv1, ArrayList<BranchVertex> lbv2) {
    }

    private void comparisonIV(ArrayList<BranchVertex> lbv1, ArrayList<BranchVertex> lbv2) {
    }

    private void comparisonIII(ArrayList<BranchVertex> lbv1, ArrayList<BranchVertex> lbv2) {
    }

    private void comparisonII(ArrayList<BranchVertex> lbv1, ArrayList<BranchVertex> lbv2) {
    }

    private void comparisonI(ArrayList<BranchVertex> lbv1, ArrayList<BranchVertex> lbv2) {
        ArrayList<BranchVertex> tlbv1 = new ArrayList<>(lbv1);
        ArrayList<BranchVertex> tlbv2 = new ArrayList<>(lbv2);

        HashMap<BranchVertex, BranchVertex> maping = new HashMap<>();

        //double distance = 0;

        while (tlbv1.size() > 0 && tlbv2.size() > 0) {
            //max from 1
            if (tlbv1.get(0).getDegreeOfBV() > tlbv2.get(0).getDegreeOfBV()) {
                BranchVertex branchVertFirst = tlbv1.get(0);
                tlbv1.remove(branchVertFirst);

                int max = -1;
                int maxIndex = -1;
                double sumDistance = 0;
                for (int i = 0; i < lbv2.size(); i++) {
                    if (branchVertFirst.getTypeOfBV().equals(lbv2.get(i).getTypeOfBV())) {
                        int tempMax = compVB(branchVertFirst, lbv2.get(i));
                        double stepDistance = calcDistance(branchVertFirst, lbv2.get(i));

                        if (max < tempMax) {
                            max = tempMax;
                            maxIndex = i;
                        }
                    }
                }

                if (maxIndex > -1) {
                    maping.put(branchVertFirst, lbv2.get(maxIndex));
                    tlbv2.remove(maxIndex);
                }
                tlbv1.remove(branchVertFirst);
            } else {
                BranchVertex branchVertSecond = tlbv2.get(0);
                tlbv2.remove(branchVertSecond);

                int max = -1;
                int maxIndex = -1;
                for (int i = 0; i < lbv1.size(); i++) {
                    if (branchVertSecond.getTypeOfBV().equals(lbv1.get(i).getTypeOfBV())) {
                        int tempMax = compVB(branchVertSecond, lbv1.get(i));
                        if (max < tempMax) {
                            max = tempMax;
                            maxIndex = i;
                        }
                    }
                }

                if (maxIndex > -1) {
                    maping.put(lbv1.get(maxIndex), branchVertSecond);
                    tlbv1.remove(maxIndex);
                }
                tlbv2.remove(branchVertSecond);
            }
        }

    }

    private void comparison0(ArrayList<BranchVertex> lbv1, ArrayList<BranchVertex> lbv2) {
        ArrayList<BranchVertex> tlbv1 = new ArrayList<>(lbv1);
        ArrayList<BranchVertex> tlbv2 = new ArrayList<>(lbv2);

        HashMap<BranchVertex, BranchVertex> maping = new HashMap<>();

        //double distance = 0;

        while (tlbv1.size() > 0 && tlbv2.size() > 0) {
            if (maping.size() == 43) {
                System.out.println("GDzie jest bład");
            }
            //max from 1
            if (tlbv1.get(0).getDegreeOfBV() >= tlbv2.get(0).getDegreeOfBV()) {
                BranchVertex branchVertFirst = tlbv1.get(0);
                tlbv1.remove(branchVertFirst);

                int max = -1;
                ArrayList<Integer> maxIndex = new ArrayList<>();


                double sumDistance = 0;
                for (int i = 0; i < tlbv2.size(); i++) {
                    if (branchVertFirst.getTypeOfBV().equals(tlbv2.get(i).getTypeOfBV()) && branchVertFirst.getInDegreeOfBV() == tlbv2.get(i).getInDegreeOfBV() && branchVertFirst.getOutDegreeOfBV() == tlbv2.get(i).getOutDegreeOfBV()) {
                        maxIndex.add(i);
                    }
                }

                if (!maxIndex.isEmpty()) {
                    //check endpoints
                    boolean isMatch = false;
                    int index = -1;
                    for (int i = 0; i < maxIndex.size(); i++) {
                        if (branchVertFirst.getNumberOfInPlace() == tlbv2.get(maxIndex.get(i)).getNumberOfInPlace() &&
                                branchVertFirst.getNumberOfOutPlace() == tlbv2.get(maxIndex.get(i)).getNumberOfOutPlace() &&
                                branchVertFirst.getNumberOfInTransitions() == tlbv2.get(maxIndex.get(i)).getNumberOfInTransitions() &&
                                branchVertFirst.getNumberOfOutTransitions() == tlbv2.get(maxIndex.get(i)).getNumberOfOutTransitions()) {
                            index = i;
                        }
                    }

                    if (index != -1) {
                        maping.put(branchVertFirst, tlbv2.get(maxIndex.get(index)));
                        tlbv2.remove(tlbv2.get(maxIndex.get(index)));
                    } else {
                        System.out.println();
                    }
                }
                //tlbv1.remove(branchVertFirst);
            } else {
                BranchVertex branchVertSecond = tlbv2.get(0);
                tlbv2.remove(branchVertSecond);


                int max = -1;
                ArrayList<Integer> maxIndex = new ArrayList<>();


                double sumDistance = 0;
                for (int i = 0; i < tlbv1.size(); i++) {
                    if (branchVertSecond.getTypeOfBV().equals(lbv1.get(i).getTypeOfBV()) && branchVertSecond.getInDegreeOfBV() == tlbv1.get(i).getInDegreeOfBV() && branchVertSecond.getOutDegreeOfBV() == tlbv1.get(i).getOutDegreeOfBV()) {
                        maxIndex.add(i);
                    }
                }

                if (!maxIndex.isEmpty()) {
                    //check endpoints
                    boolean isMatch = false;
                    int index = -1;
                    for (int i = 0; i < maxIndex.size(); i++) {
                        if (branchVertSecond.getNumberOfInPlace() == tlbv1.get(maxIndex.get(i)).getNumberOfInPlace() &&
                                branchVertSecond.getNumberOfOutPlace() == tlbv1.get(maxIndex.get(i)).getNumberOfOutPlace() &&
                                branchVertSecond.getNumberOfInTransitions() == tlbv1.get(maxIndex.get(i)).getNumberOfInTransitions() &&
                                branchVertSecond.getNumberOfOutTransitions() == tlbv1.get(maxIndex.get(i)).getNumberOfOutTransitions()) {
                            index = i;
                        }
                    }

                    if (index != -1) {
                        maping.put(tlbv1.get(maxIndex.get(index)), branchVertSecond);
                        tlbv1.remove(tlbv1.get(maxIndex.get(index)));
                    }
                }
                //tlbv2.remove(branchVertSecond);
            }
        }

        //calc sim
        System.out.println("C 0 ");
        showSimilarity(lbv1, lbv2, maping, "V0");
    }

    private void comparison1(ArrayList<BranchVertex> lbv1, ArrayList<BranchVertex> lbv2) {
        ArrayList<BranchVertex> tlbv1 = new ArrayList<>(lbv1);
        ArrayList<BranchVertex> tlbv2 = new ArrayList<>(lbv2);

        HashMap<BranchVertex, BranchVertex> maping = new HashMap<>();

        //double distance = 0;

        while (tlbv1.size() > 0 && tlbv2.size() > 0) {
            //max from 1
            if (tlbv1.get(0).getDegreeOfBV() >= tlbv2.get(0).getDegreeOfBV()) {
                BranchVertex branchVertFirst = tlbv1.get(0);
                tlbv1.remove(branchVertFirst);

                int max = -1;
                ArrayList<Integer> maxIndex = new ArrayList<>();


                double sumDistance = 0;
                for (int i = 0; i < tlbv2.size(); i++) {
                    if (branchVertFirst.getTypeOfBV().equals(tlbv2.get(i).getTypeOfBV()) && branchVertFirst.getInDegreeOfBV() == tlbv2.get(i).getInDegreeOfBV() && branchVertFirst.getOutDegreeOfBV() == tlbv2.get(i).getOutDegreeOfBV()) {
                        maxIndex.add(i);
                    }
                }

                if (!maxIndex.isEmpty()) {
                    //check endpoints
                    boolean isMatch;
                    int index = -1;
                    for (int i = 0; i < maxIndex.size(); i++) {
                        if (branchVertFirst.getNumberOfInPlace() == tlbv2.get(maxIndex.get(i)).getNumberOfInPlace() &&
                                branchVertFirst.getNumberOfOutPlace() == tlbv2.get(maxIndex.get(i)).getNumberOfOutPlace() &&
                                branchVertFirst.getNumberOfInTransitions() == tlbv2.get(maxIndex.get(i)).getNumberOfInTransitions() &&
                                branchVertFirst.getNumberOfOutTransitions() == tlbv2.get(maxIndex.get(i)).getNumberOfOutTransitions()) {

                            isMatch = compareEndpointsBoolean(branchVertFirst, tlbv2.get(maxIndex.get(i)));
                            if (isMatch)
                                index = i;
                        }
                    }

                    if (index != -1) {
                        maping.put(branchVertFirst, tlbv2.get(maxIndex.get(index)));
                        tlbv2.remove(maxIndex.get(index));
                    }
                }
                tlbv1.remove(branchVertFirst);
            } else {
                BranchVertex branchVertSecond = tlbv2.get(0);
                tlbv2.remove(branchVertSecond);


                int max = -1;
                ArrayList<Integer> maxIndex = new ArrayList<>();


                double sumDistance = 0;
                for (int i = 0; i < tlbv1.size(); i++) {
                    if (branchVertSecond.getTypeOfBV().equals(lbv1.get(i).getTypeOfBV()) && branchVertSecond.getInDegreeOfBV() == tlbv1.get(i).getInDegreeOfBV() && branchVertSecond.getOutDegreeOfBV() == tlbv1.get(i).getOutDegreeOfBV()) {
                        maxIndex.add(i);
                    }
                }

                if (!maxIndex.isEmpty()) {
                    //check endpoints
                    boolean isMatch = false;
                    int index = -1;
                    for (int i = 0; i < maxIndex.size(); i++) {
                        if (branchVertSecond.getNumberOfInPlace() == tlbv1.get(maxIndex.get(i)).getNumberOfInPlace() &&
                                branchVertSecond.getNumberOfOutPlace() == tlbv1.get(maxIndex.get(i)).getNumberOfOutPlace() &&
                                branchVertSecond.getNumberOfInTransitions() == tlbv1.get(maxIndex.get(i)).getNumberOfInTransitions() &&
                                branchVertSecond.getNumberOfOutTransitions() == tlbv1.get(maxIndex.get(i)).getNumberOfOutTransitions()) {
                            index = i;
                        }
                    }

                    if (index != -1) {
                        maping.put(tlbv1.get(maxIndex.get(index)), branchVertSecond);
                        tlbv1.remove(maxIndex.get(index));
                    }
                }
                tlbv2.remove(branchVertSecond);
            }
        }

        //calc sim

        System.out.println("C 1 ");
        showSimilarity(lbv1, lbv2, maping, "V1");
    }

    private void comparison2(ArrayList<BranchVertex> lbv1, ArrayList<BranchVertex> lbv2) {
        ArrayList<BranchVertex> tlbv1 = new ArrayList<>(lbv1);
        ArrayList<BranchVertex> tlbv2 = new ArrayList<>(lbv2);

        HashMap<BranchVertex, BranchVertex> maping = new HashMap<>();

        //double distance = 0;

        while (tlbv1.size() > 0 && tlbv2.size() > 0) {
            //max from 1
            if (tlbv1.get(0).getDegreeOfBV() >= tlbv2.get(0).getDegreeOfBV()) {
                BranchVertex branchVertFirst = tlbv1.get(0);
                tlbv1.remove(branchVertFirst);

                int max = -1;
                ArrayList<Integer> maxIndex = new ArrayList<>();


                double sumDistance = 0;
                for (int i = 0; i < tlbv2.size(); i++) {
                    if (branchVertFirst.getTypeOfBV().equals(tlbv2.get(i).getTypeOfBV()) && branchVertFirst.getInDegreeOfBV() == tlbv2.get(i).getInDegreeOfBV() && branchVertFirst.getOutDegreeOfBV() == tlbv2.get(i).getOutDegreeOfBV()) {
                        maxIndex.add(i);
                    }
                }

                if (!maxIndex.isEmpty()) {
                    //check endpoints
                    boolean isMatch = false;
                    int index = -1;
                    double maxEndpoint = 0;
                    for (int i = 0; i < maxIndex.size(); i++) {
                        //if(branchVertFirst.getNumberOfInPlace()== tlbv2.get(maxIndex.get(i)).getNumberOfInPlace() &&
                        //        branchVertFirst.getNumberOfOutPlace()== tlbv2.get(maxIndex.get(i)).getNumberOfOutPlace() &&
                        //        branchVertFirst.getNumberOfInTransitions()== tlbv2.get(maxIndex.get(i)).getNumberOfInTransitions() &&
                        //        branchVertFirst.getNumberOfOutTransitions()== tlbv2.get(maxIndex.get(i)).getNumberOfOutTransitions()){

                        double tempMaxEndpoint = compareEndpointsDooble(branchVertFirst, tlbv2.get(maxIndex.get(i)));
                        if (tempMaxEndpoint > maxEndpoint) {
                            maxEndpoint = tempMaxEndpoint;
                            index = i;
                        }
                        //}
                    }

                    if (index != -1) {
                        maping.put(branchVertFirst, tlbv2.get(maxIndex.get(index)));
                        tlbv2.remove(maxIndex.get(index));
                    }

                    //System.out.println("jakie podob znalazło " + maxEndpoint);
                }

                tlbv1.remove(branchVertFirst);
            } else {
                BranchVertex branchVertSecond = tlbv2.get(0);
                tlbv2.remove(branchVertSecond);


                int max = -1;
                ArrayList<Integer> maxIndex = new ArrayList<>();


                double sumDistance = 0;
                for (int i = 0; i < tlbv1.size(); i++) {
                    if (branchVertSecond.getTypeOfBV().equals(lbv1.get(i).getTypeOfBV()) && branchVertSecond.getInDegreeOfBV() == tlbv1.get(i).getInDegreeOfBV() && branchVertSecond.getOutDegreeOfBV() == tlbv1.get(i).getOutDegreeOfBV()) {
                        maxIndex.add(i);
                    }
                }

                if (!maxIndex.isEmpty()) {
                    //check endpoints
                    boolean isMatch = false;
                    int index = -1;
                    double maxEndpoint = 0;
                    for (int i = 0; i < maxIndex.size(); i++) {
                        //if(branchVertSecond.getNumberOfInPlace()== tlbv1.get(maxIndex.get(i)).getNumberOfInPlace() &&
                        //       branchVertSecond.getNumberOfOutPlace()== tlbv1.get(maxIndex.get(i)).getNumberOfOutPlace() &&
                        //       branchVertSecond.getNumberOfInTransitions()== tlbv1.get(maxIndex.get(i)).getNumberOfInTransitions() &&
                        //      branchVertSecond.getNumberOfOutTransitions()== tlbv1.get(maxIndex.get(i)).getNumberOfOutTransitions()){
                        //if(isMatched(branchVertSecond,tlbv1.get(maxIndex.get(i)))){
                        //isMatch = compareEndpointsBoolean(tlbv1.get(maxIndex.get(i)),branchVertSecond);
                        //if(isMatch)
                        //    index =i;

                        double tempMaxEndpoint = compareEndpointsDooble(tlbv1.get(maxIndex.get(i)), branchVertSecond);
                        if (tempMaxEndpoint > maxEndpoint) {
                            maxEndpoint = tempMaxEndpoint;
                            index = i;
                        }
                        //}
                    }

                    if (index != -1) {
                        maping.put(tlbv1.get(maxIndex.get(index)), branchVertSecond);
                        tlbv1.remove(maxIndex.get(index));
                    }

                    //System.out.println("jakie podob znalazło " + maxEndpoint);
                }
                tlbv2.remove(branchVertSecond);
            }
        }

        //calc sim

        System.out.println("C 2 ");
        showSimilarity(lbv1, lbv2, maping, "V2");
    }

    private void comparison3(ArrayList<BranchVertex> lbv1, ArrayList<BranchVertex> lbv2) {
        ArrayList<BranchVertex> tlbv1 = new ArrayList<>(lbv1);
        ArrayList<BranchVertex> tlbv2 = new ArrayList<>(lbv2);

        HashMap<BranchVertex, BranchVertex> maping = new HashMap<>();

        //double distance = 0;

        while (tlbv1.size() > 0 && tlbv2.size() > 0) {
            //max from 1
            if (tlbv1.get(0).getDegreeOfBV() >= tlbv2.get(0).getDegreeOfBV()) {
                BranchVertex branchVertFirst = tlbv1.get(0);
                tlbv1.remove(branchVertFirst);

                int max = -1;
                ArrayList<Integer> maxIndex = new ArrayList<>();


                double sumDistance = 0;

                //TODO
                // mniejsze równe i max z tego


                int minDegreeDifference = 99999;

                for (int i = 0; i < tlbv2.size(); i++) {


                    if (branchVertFirst.getTypeOfBV().equals(tlbv2.get(i).getTypeOfBV())
                    ) {

                        int tmpMDD = Math.abs(branchVertFirst.getInDegreeOfBV() - tlbv2.get(i).getInDegreeOfBV()) +
                                Math.abs(branchVertFirst.getOutDegreeOfBV() - tlbv2.get(i).getOutDegreeOfBV());
                        if (tmpMDD < minDegreeDifference) {
                            minDegreeDifference = tmpMDD;
                            maxIndex.clear();
                            maxIndex.add(i);
                        }

                        if (tmpMDD == minDegreeDifference) {
                            maxIndex.add(i);
                        }
                    }
                }

                if (!maxIndex.isEmpty()) {
                    //check endpoints
                    boolean isMatch = false;
                    int index = -1;
                    double maxEndpoint = 0;
                    for (int i = 0; i < maxIndex.size(); i++) {
                        //if(branchVertFirst.getNumberOfInPlace()== tlbv2.get(maxIndex.get(i)).getNumberOfInPlace() &&
                        //       branchVertFirst.getNumberOfOutPlace()== tlbv2.get(maxIndex.get(i)).getNumberOfOutPlace() &&
                        //      branchVertFirst.getNumberOfInTransitions()== tlbv2.get(maxIndex.get(i)).getNumberOfInTransitions() &&
                        //     branchVertFirst.getNumberOfOutTransitions()== tlbv2.get(maxIndex.get(i)).getNumberOfOutTransitions()){

                        double tempMaxEndpoint = compareEndpointsDooble(branchVertFirst, tlbv2.get(maxIndex.get(i)));
                        if (tempMaxEndpoint > maxEndpoint) {
                            maxEndpoint = tempMaxEndpoint;
                            index = i;
                        }
                        //}
                    }

                    if (index != -1) {
                        maping.put(branchVertFirst, tlbv2.get(maxIndex.get(index)));
                        tlbv2.remove(maxIndex.get(index));
                    }

                    //System.out.println("jakie podob znalazło " + maxEndpoint);
                }

                tlbv1.remove(branchVertFirst);
            } else {
                BranchVertex branchVertSecond = tlbv2.get(0);
                tlbv2.remove(branchVertSecond);


                int max = -1;
                ArrayList<Integer> maxIndex = new ArrayList<>();


                int minDegreeDifference = 99999;
                double sumDistance = 0;
                for (int i = 0; i < tlbv1.size(); i++) {
                    //if (branchVertSecond.getTypeOfBV().equals(lbv1.get(i).getTypeOfBV()) && branchVertSecond.getInDegreeOfBV() == tlbv1.get(i).getInDegreeOfBV() && branchVertSecond.getOutDegreeOfBV() == tlbv1.get(i).getOutDegreeOfBV()) {
                    //    maxIndex.add(i);
                    //}

                    if (branchVertSecond.getTypeOfBV().equals(tlbv1.get(i).getTypeOfBV())
                    ) {
                        int tmpMDD = Math.abs(branchVertSecond.getInDegreeOfBV() - tlbv1.get(i).getInDegreeOfBV()) +
                                Math.abs(branchVertSecond.getOutDegreeOfBV() - tlbv1.get(i).getOutDegreeOfBV());
                        if (tmpMDD < minDegreeDifference) {
                            minDegreeDifference = tmpMDD;
                            maxIndex.clear();
                            maxIndex.add(i);
                        }

                        if (tmpMDD == minDegreeDifference) {
                            maxIndex.add(i);
                        }
                    }
                }

                if (!maxIndex.isEmpty()) {
                    //check endpoints
                    boolean isMatch = false;
                    int index = -1;
                    double maxEndpoint = 0;
                    for (int i = 0; i < maxIndex.size(); i++) {
                        //if(branchVertSecond.getNumberOfInPlace()== tlbv1.get(maxIndex.get(i)).getNumberOfInPlace() &&
                        //       branchVertSecond.getNumberOfOutPlace()== tlbv1.get(maxIndex.get(i)).getNumberOfOutPlace() &&
                        //       branchVertSecond.getNumberOfInTransitions()== tlbv1.get(maxIndex.get(i)).getNumberOfInTransitions() &&
                        //      branchVertSecond.getNumberOfOutTransitions()== tlbv1.get(maxIndex.get(i)).getNumberOfOutTransitions()){
                        //if(isMatched(branchVertSecond,tlbv1.get(maxIndex.get(i)))){
                        //isMatch = compareEndpointsBoolean(tlbv1.get(maxIndex.get(i)),branchVertSecond);
                        //if(isMatch)
                        //    index =i;

                        double tempMaxEndpoint = compareEndpointsDooble(tlbv1.get(maxIndex.get(i)), branchVertSecond);
                        if (tempMaxEndpoint > maxEndpoint) {
                            maxEndpoint = tempMaxEndpoint;
                            index = i;
                        }
                        //}
                    }

                    if (index != -1) {
                        maping.put(tlbv1.get(maxIndex.get(index)), branchVertSecond);
                        tlbv1.remove(maxIndex.get(index));
                    }

                    //System.out.println("jakie podob znalazło " + maxEndpoint);
                }
                tlbv2.remove(branchVertSecond);
            }
        }

        //calc sim

        System.out.println("C 3 ");
        showSimilarity(lbv1, lbv2, maping, "V3");
    }


    private void comparison4(ArrayList<BranchVertex> lbv1, ArrayList<BranchVertex> lbv2) {
        ArrayList<BranchVertex> tlbv1 = new ArrayList<>(lbv1);
        ArrayList<BranchVertex> tlbv2 = new ArrayList<>(lbv2);

        HashMap<BranchVertex, BranchVertex> maping = new HashMap<>();

        while (tlbv1.size() > 0 && tlbv2.size() > 0) {
            //max from 1
            if (tlbv1.get(0).getDegreeOfBV() >= tlbv2.get(0).getDegreeOfBV()) {
                BranchVertex branchVertFirst = tlbv1.get(0);
                tlbv1.remove(branchVertFirst);

                int max = -1;
                ArrayList<Integer> maxIndex = new ArrayList<>();

                double sumDistance = 0;
                double maxEndpoint = 0;

                for (int i = 0; i < tlbv2.size(); i++) {
                    if (branchVertFirst.getTypeOfBV().equals(tlbv2.get(i).getTypeOfBV())) {

                        double tempMaxEndpoint = compareEndpointsDooble(branchVertFirst, tlbv2.get(i));
                        if (tempMaxEndpoint > maxEndpoint) {
                            maxEndpoint = tempMaxEndpoint;

                            maxIndex.clear();
                            maxIndex.add(i);
                        }
                        if (tempMaxEndpoint == maxEndpoint) {
                            maxIndex.add(i);
                        }
                    }
                }

                int index = -1;
                int minDegreeDifference = 99999;
                if (!maxIndex.isEmpty()) {
                    //get best match
                    for (int i = 0; i < maxIndex.size(); i++) {
                        int tmpMDD = Math.abs(branchVertFirst.getInDegreeOfBV() - tlbv2.get(maxIndex.get(i)).getInDegreeOfBV()) +
                                Math.abs(branchVertFirst.getOutDegreeOfBV() - tlbv2.get(maxIndex.get(i)).getOutDegreeOfBV());

                        if (tmpMDD < minDegreeDifference) {
                            minDegreeDifference = tmpMDD;
                            index = i;
                        }
                    }

                    if (index != -1) {
                        maping.put(branchVertFirst, tlbv2.get(maxIndex.get(index)));
                        tlbv2.remove(maxIndex.get(index));
                    }
                }

                tlbv1.remove(branchVertFirst);
            } else {
                BranchVertex branchVertSecond = tlbv2.get(0);
                tlbv2.remove(branchVertSecond);

                int max = -1;
                ArrayList<Integer> maxIndex = new ArrayList<>();

                double sumDistance = 0;
                double maxEndpoint = 0;

                for (int i = 0; i < tlbv1.size(); i++) {
                    if (branchVertSecond.getTypeOfBV().equals(tlbv1.get(i).getTypeOfBV())) {

                        double tempMaxEndpoint = compareEndpointsDooble(tlbv1.get(i), branchVertSecond);
                        if (tempMaxEndpoint > maxEndpoint) {
                            maxEndpoint = tempMaxEndpoint;

                            maxIndex.clear();
                            maxIndex.add(i);
                        }
                        if (tempMaxEndpoint == maxEndpoint) {
                            maxIndex.add(i);
                        }
                    }
                }

                int index = -1;
                int minDegreeDifference = 99999;
                if (!maxIndex.isEmpty()) {
                    //get best match
                    for (int i = 0; i < maxIndex.size(); i++) {
                        int tmpMDD = Math.abs(branchVertSecond.getInDegreeOfBV() - tlbv1.get(maxIndex.get(i)).getInDegreeOfBV()) +
                                Math.abs(branchVertSecond.getOutDegreeOfBV() - tlbv1.get(maxIndex.get(i)).getOutDegreeOfBV());

                        if (tmpMDD < minDegreeDifference) {
                            minDegreeDifference = tmpMDD;
                            index = i;
                        }
                    }

                    if (index != -1) {
                        maping.put(tlbv1.get(maxIndex.get(index)), branchVertSecond);
                        tlbv1.remove(maxIndex.get(index));
                    }
                }

                tlbv2.remove(branchVertSecond);


                /*
            }
                BranchVertex branchVertSecond = tlbv2.get(0);
                tlbv2.remove(branchVertSecond);

                int max = -1;
                ArrayList<Integer> maxIndex = new ArrayList<>();

                int minDegreeDifference = 99999;
                double sumDistance = 0;
                for (int i = 0; i < tlbv1.size(); i++) {
                    if (branchVertSecond.getTypeOfBV().equals(tlbv1.get(i).getTypeOfBV())
                    ) {
                        int tmpMDD = Math.abs(branchVertSecond.getInDegreeOfBV() - tlbv1.get(i).getInDegreeOfBV()) +
                                Math.abs(branchVertSecond.getOutDegreeOfBV() - tlbv1.get(i).getOutDegreeOfBV());
                        if (tmpMDD < minDegreeDifference) {
                            minDegreeDifference = tmpMDD;
                            maxIndex.clear();
                            maxIndex.add(i);
                        }

                        if (tmpMDD == minDegreeDifference) {
                            maxIndex.add(i);
                        }
                    }
                }

                if (!maxIndex.isEmpty()) {
                    //check endpoints
                    boolean isMatch = false;
                    int index = -1;
                    double maxEndpoint = 0;
                    for (int i = 0; i < maxIndex.size(); i++) {
                        double tempMaxEndpoint = compareEndpointsDooble(tlbv1.get(maxIndex.get(i)), branchVertSecond);
                        if (tempMaxEndpoint > maxEndpoint) {
                            maxEndpoint = tempMaxEndpoint;
                            index = i;
                        }
                    }

                    if (index != -1) {
                        maping.put(tlbv1.get(maxIndex.get(index)), branchVertSecond);
                        tlbv1.remove(maxIndex.get(index));
                    }
                }
                tlbv2.remove(branchVertSecond);
                */
            }
        }

        //calc sim
        System.out.println("C 4 ");
        showSimilarity(lbv1, lbv2, maping, "V4");
    }


    private void showSimilarity(ArrayList<BranchVertex> lbv1, ArrayList<BranchVertex> lbv2, HashMap<BranchVertex, BranchVertex> maping, String type) {
        int distance = lbv1.size() - maping.size() + lbv2.size() - maping.size();
        //System.out.println("Dystans : " + distance);
        double ssimilarity = (double) (2 * maping.size()) / (lbv1.size() + lbv2.size());
        //System.out.println("Sørensen : " + ssimilarity);
        double jsimilarity = (double) (maping.size()) / (lbv1.size() + lbv2.size() - maping.size());
        //System.out.println("Jackard : " + jsimilarity);
        //System.out.println("d : " + index_d + " p : "+ index_p );


        try {
            writeSimilarity(lbv1, lbv2, maping, type);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void writeSimilarity(ArrayList<BranchVertex> lbv1, ArrayList<BranchVertex> lbv2, HashMap<BranchVertex, BranchVertex> maping, String type) throws IOException {
        FileWriter writer = new FileWriter("/home/bartek/Eksperyment/Wyniki/d" + index_d + "i40j40/d" + index_d + "i40j40p" + index_p + "/branch-similarity-" + type + ".txt");
        writer.append("Type : ").append(type).append("\n");
        int distance = lbv1.size() - maping.size() + lbv2.size() - maping.size();
        writer.append("Dystans : ").append(String.valueOf(distance)).append("\n");
        double ssimilarity = (double) (2 * maping.size()) / (lbv1.size() + lbv2.size());
        writer.append("Sørensen : ").append(String.valueOf(ssimilarity)).append("\n");
        double jsimilarity = (double) (maping.size()) / (lbv1.size() + lbv2.size() - maping.size());
        writer.append("Jackard : ").append(String.valueOf(jsimilarity)).append("\n");
        writer.close();
    }

    private double compareEndpointsDooble(BranchVertex branchVertFirst, BranchVertex branchVertex) {
        double result = 0;
        int count = 0;

        for (int i = 0; i < branchVertFirst.inEndpoints.size(); i++) {
            double max = 0;
            boolean is_match = false;

            for (int j = 0; j < branchVertex.inEndpoints.size(); j++) {
                double tmpMax = matchedValue(getVB(branchVertFirst.inEndpoints.get(i)), getVB(branchVertex.inEndpoints.get(j)));

                if (max < tmpMax) {
                    max = tmpMax;
                }
            }

            result += max;
        }

        for (int i = 0; i < branchVertFirst.outEndpoints.size(); i++) {
            double max = 0;
            boolean is_match = false;

            for (int j = 0; j < branchVertex.outEndpoints.size(); j++) {
                double tmpMax = matchedValue(getVB(branchVertFirst.outEndpoints.get(i)), getVB(branchVertex.outEndpoints.get(j)));

                if (max < tmpMax) {
                    max = tmpMax;
                }
            }

            result += max;
        }

        return result;
    }


    private boolean compareEndpointsBoolean(BranchVertex branchVertFirst, BranchVertex branchVertex) {
        boolean result = false;
        int count = 0;

        for (int i = 0; i < branchVertFirst.inEndpoints.size(); i++) {
            boolean is_match = false;
            for (int j = 0; j < branchVertex.inEndpoints.size(); j++) {
                if (isMatched(getVB(branchVertFirst.inEndpoints.get(i)), getVB(branchVertex.inEndpoints.get(j)))) {
                    is_match = true;
                }
            }
            if (is_match)
                count++;
        }

        for (int i = 0; i < branchVertFirst.outEndpoints.size(); i++) {
            boolean is_match = false;
            for (int j = 0; j < branchVertex.outEndpoints.size(); j++) {
                if (isMatched(getVB(branchVertFirst.outEndpoints.get(i)), getVB(branchVertex.outEndpoints.get(j)))) {
                    is_match = true;
                }
            }
            if (is_match)
                count++;
        }

        if (count == branchVertex.inEndpoints.size() + branchVertex.outEndpoints.size())
            result = true;

        return result;
    }

    private BranchVertex getVB(Node node) {
        return addBV(node);
    }

    private boolean isMatched(BranchVertex branchVertFirst, BranchVertex branchVertex) {
        return branchVertFirst.getNumberOfInPlace() == branchVertex.getNumberOfInPlace() &&
                branchVertFirst.getNumberOfOutPlace() == branchVertex.getNumberOfOutPlace() &&
                branchVertFirst.getNumberOfInTransitions() == branchVertex.getNumberOfInTransitions() &&
                branchVertFirst.getNumberOfOutTransitions() == branchVertex.getNumberOfOutTransitions();
    }

    private double matchedValue(BranchVertex branchVertFirst, BranchVertex branchVertex) {
        //niee przez zero

        long distance = Math.abs(branchVertFirst.getNumberOfInPlace() - branchVertex.getNumberOfInPlace()) +
                Math.abs(branchVertFirst.getNumberOfOutPlace() - branchVertex.getNumberOfOutPlace()) +
                Math.abs(branchVertFirst.getNumberOfInTransitions() - branchVertex.getNumberOfInTransitions()) +
                Math.abs(branchVertFirst.getNumberOfOutTransitions() - branchVertex.getNumberOfOutTransitions());

        return (double) 1 - (distance / (Math.max(branchVertFirst.inEndpoints.size() + branchVertFirst.outEndpoints.size(),
                branchVertex.inEndpoints.size() + branchVertex.outEndpoints.size())));

        //return (double)(branchVertFirst.getNumberOfInPlace()/ branchVertex.getNumberOfInPlace()) +
        //        (double)(branchVertFirst.getNumberOfOutPlace()/ branchVertex.getNumberOfOutPlace()) +
        //        (double)(branchVertFirst.getNumberOfInTransitions()/branchVertex.getNumberOfInTransitions()) +
        //        (double)(branchVertFirst.getNumberOfOutTransitions()/ branchVertex.getNumberOfOutTransitions());
    }

    private double calcDistance(BranchVertex branchVertFirst, BranchVertex branchVertex) {
        int degDistance = compVB(branchVertFirst, branchVertex);

        return 0;
    }


    private int compVB(BranchVertex branchVertFirst, BranchVertex branchVertex) {

        int inDeg = Math.min(branchVertFirst.getInDegreeOfBV(), branchVertex.getDegreeOfBV());
        int outDeg = Math.min(branchVertFirst.getOutDegreeOfBV(), branchVertex.getOutDegreeOfBV());

        return inDeg + outDeg;
    }

    private void comparisonVI(ArrayList<BranchVertex> lbv1, ArrayList<BranchVertex> lbv2) {
    }

    public PetriNet loadNet(String path, int sid) {
        IOprotocols io = new IOprotocols();
        return io.serverReadPNT(path, sid);
    }

    public ArrayList<BranchVertex> calcBranches(PetriNet pn) {
        ArrayList<BranchVertex> listOfBranchVertices = new ArrayList<>();
        for (Node n : pn.getNodes()) {
            if (n.getInArcs().size() > 1 || n.getOutArcs().size() > 1)
                listOfBranchVertices.add(addBV(n));
        }

        listOfBranchVertices.sort(new sortVB());
        return listOfBranchVertices;
    }

    private BranchVertex addBV(Node n) {
        return new BranchVertex(n, calcInEndpoints(n), calcOutEndpoints(n));
    }

    private ArrayList<Node> calcOutEndpoints(Node n) {
        ArrayList<Node> endpoints = new ArrayList<>();
        for (Arc a : n.getOutArcs()) {
            endpoints.add(findOutEndpoint(a.getEndNode()));
        }

        return endpoints;
    }

    private ArrayList<Node> calcInEndpoints(Node n) {
        ArrayList<Node> endpoints = new ArrayList<>();
        for (Arc a : n.getInArcs()) {
            endpoints.add(findInEndpoint(a.getEndNode()));
        }
        return endpoints;
    }

    private Node findInEndpoint(Node n) {
        Node result;
        if (n.getInArcs().size() > 1 || n.getOutArcs().size() > 1) {
            return n;
        } else {
            result = findInEndpoint(n.getOutNodes().get(0));
        }

        return result;
    }

    private Node findOutEndpoint(Node n) {
        Node result;
        if (n.getInArcs().size() > 1 || n.getOutArcs().size() > 1) {
            return n;
        } else {
            result = findInEndpoint(n.getInNodes().get(0));
        }

        return result;
    }


    public void calcSimilarity() {

    }

    public void getData() {

        Double[][][][] result = new Double[5][3][3][204];

        for (int d = 99; d < 302; d++) {
            for (int v = 0; v < 5; v++) {

                Double[][] packtetContent = new Double[100][3];


                double minJack = 9999;
                double minSoren = 9999;
                double minDist = 9999;

                double maxJack = 0;
                double maxSoren = 0;
                double maxDist = 0;

                double sumJack = 0;
                double sumSoren = 0;
                double sumDist = 0;

                for (int p = 0; p < 100; p++) {
                    packtetContent[p] = readData("/home/bartek/Eksperyment/Wyniki/d" + d + "i40j40/d" + d + "i40j40p" + p + "/branch-similarity-V" + v + ".txt");
                    if (packtetContent[p][0] < minDist)
                        minDist = packtetContent[p][0];
                    if (packtetContent[p][1] < minSoren)
                        minSoren = packtetContent[p][1];
                    if (packtetContent[p][2] < minJack)
                        minJack = packtetContent[p][2];

                    if (packtetContent[p][0] > maxDist)
                        maxDist = packtetContent[p][0];
                    if (packtetContent[p][1] > maxSoren)
                        maxSoren = packtetContent[p][1];
                    if (packtetContent[p][2] > maxJack)
                        maxJack = packtetContent[p][2];

                    sumDist += packtetContent[p][0];
                    sumSoren += packtetContent[p][1];
                    sumJack += packtetContent[p][2];
                }


                result[v][0][0][d - 99] = minDist;
                result[v][0][1][d - 99] = maxDist;
                result[v][0][2][d - 99] = sumDist;


                result[v][1][0][d - 99] = minSoren;
                result[v][1][1][d - 99] = maxSoren;
                result[v][1][2][d - 99] = sumSoren;


                result[v][2][0][d - 99] = minJack;
                result[v][2][1][d - 99] = maxJack;
                result[v][2][2][d - 99] = sumJack;

            }
            System.out.println("d = " + d);
        }
        try {
            writeResult("/home/bartek/Eksperyment/resultOfBranchCOmparison.csv", result);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void writeResult(String path, Double[][][][] result) throws IOException {
        FileWriter writer = new FileWriter(path);

        System.out.println("Zapis do pliku");
        for (int v = 0; v < 5; v++) {
            for (int m = 0; m < 3; m++) {
                for (int t = 0; t < 3; t++) {
                    for (int d = 0; d < 204; d++) {
                        writer.append(String.valueOf(result[v][m][t][d]));
                        if (d != 203) {
                            writer.append(",");
                        }
                    }
                    writer.append("\n");
                }
                writer.append("\n");
            }
            writer.append("\n");
        }
        writer.close();
    }

    private Double[] readData(String s) {
        Double[] result = new Double[3];

        DataInputStream in = null;
        try {
            in = new DataInputStream(new FileInputStream(s));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        BufferedReader buffer = new BufferedReader(new InputStreamReader(in));
        String wczytanaLinia;
        try {
            while ((wczytanaLinia = buffer.readLine()) != null) {

                if (wczytanaLinia.contains("Type")) {

                }

                if (wczytanaLinia.contains("Dystans")) {
                    String[] line = wczytanaLinia.split(":");
                    result[0] = Double.parseDouble(line[1]);
                }
                if (wczytanaLinia.contains("Sørensen")) {
                    String[] line = wczytanaLinia.split(":");
                    result[1] = Double.parseDouble(line[1]);
                }
                if (wczytanaLinia.contains("Jackard")) {
                    String[] line = wczytanaLinia.split(":");
                    result[2] = Double.parseDouble(line[1]);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    class sortVB implements Comparator<BranchVertex> {
        // Used for sorting in ascending order of
        // roll number
        public int compare(BranchVertex a, BranchVertex b) {
            return a.getDegreeOfBV() - b.getDegreeOfBV();
        }
    }

}
