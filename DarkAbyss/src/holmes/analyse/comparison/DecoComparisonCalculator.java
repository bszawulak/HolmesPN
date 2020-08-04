package holmes.analyse.comparison;

import holmes.analyse.SubnetCalculator;
import holmes.analyse.matrix.IncidenceMatrix;
import holmes.analyse.matrix.InputMatrix;
import holmes.analyse.matrix.OutputMatrix;
import holmes.petrinet.data.PetriNet;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class DecoComparisonCalculator {
    private PetriNet firstNet;
    private PetriNet secondNet;
    //private InputMatrix inMatrix;
    //private OutputMatrix outMatrix;
    private IncidenceMatrix matrix;
    //private SubnetCalculator.SubNet subNet;


    public static ArrayList<Integer> tranOrder = new ArrayList<Integer>();
    public static ArrayList<Integer> placeOrder = new ArrayList<Integer>();

    public DecoComparisonCalculator() {
        //load IM from s1 or generate IM from s2

        //load IM from s2

        //VOA s1

        //VOA s2

        //for each pair GCCS

        //index
    }

    public static IncidenceMatrix vertexOrderAlgorithm(IncidenceMatrix im) {

        boolean[] isbranch = new boolean[im.getMatrix()[0].length];
        ArrayList<Integer> branchRows = new ArrayList<>();
        int[] degree = new int[im.getMatrix().length];

        for (int r = 0; r < im.getMatrix().length; r++) {
            int[] row = im.getMatrix()[r];
            int in = 0;
            int out = 0;
            int deg = 0;
            for (int i = 0; i < row.length; i++) {
                if (row[i] > 0) {
                    in++;
                }
                if (row[i] < 0) {
                    out++;
                }
                if (row[i] != 0)
                    deg++;
            }
            if (deg > 1 && (in > 1 || out > 1)) {
                isbranch[r] = true;
                branchRows.add(r);
            }
            degree[r] = deg;
        }


        //osobna metoda
        int max = 0;
        ArrayList<Integer> maxRows = new ArrayList<Integer>();
        for (int i = 0; i < degree.length; i++) {
            if (degree[i] > max) {
                maxRows.clear();
                maxRows.add(i);
                max = degree[i];
            } else if (degree[i] == max) {
                maxRows.add(i);
            } else if (degree[i] < max) {

            }
        }

        //maxRow
        tranOrder.add(branchRows.get(0));

        //co jeśli są równe
        ///for(int ti = 0 ; ti < maxRows.size(); ti++) {
        for (int ti = 0; ti < branchRows.size(); ti++) {
            ArrayList<ArrayList<Integer>> listOfBranches = new ArrayList<>();

            //maxRows
            int t = branchRows.get(ti);

            for (int i = 0; i < im.getMatrix()[t].length; i++) {
                //if(branchRows.contains())
                if (im.getMatrix()[t][i] != 0) {
                    ArrayList<Integer> branch = new ArrayList<>();
                    branch.add(t);
                    byColmn(im, i, branch, listOfBranches, t);
                }
            }


            //lenght of vertex in branch

            Collections.sort(listOfBranches, new LenghtComparator());

            //Czy końćzy branchową

            //

            ArrayList<ArrayList<ArrayList<Integer>>> conflictGroups = new ArrayList<>();


            for (ArrayList<Integer> branch : listOfBranches) {
                if (im.getMatrix()[branch.get(0)][branch.get(1)] > 0) {
                    for (int i = 1; i < branch.size(); i++) {
                        if (i % 2 == 0) {
                            tranOrder.add(0, branch.get(i));
                        } else {
                            placeOrder.add(0, branch.get(i));
                        }
                    }
                }
                if (im.getMatrix()[branch.get(0)][branch.get(1)] < 0) {
                    for (int i = 1; i < branch.size(); i++) {
                        if (i % 2 == 0) {
                            tranOrder.add(branch.get(i));
                        } else {
                            placeOrder.add(branch.get(i));
                        }
                    }
                }
            }
        }

        int[][] orderedMatrxi = new int[tranOrder.size()][placeOrder.size()];

        for (int i = 0; i < tranOrder.size(); i++) {
            for (int j = 0; j < placeOrder.size(); j++) {
                orderedMatrxi[i][j] = im.getMatrix()[tranOrder.get(i)][placeOrder.get(j)];
            }
        }


        //////// TEST ZAPIS ///////

        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter("orderedMatrix.txt"));
            //for (int i = 0 ; i < orderedMatrxi.size(); i++){
            //    bw.write("sub: "+i);
            //   bw.newLine();

            for (int i = 0; i < orderedMatrxi.length; i++) {
                for (int j = 0; j < orderedMatrxi[i].length; j++) {
                    if (j == orderedMatrxi.length - 1) {
                        bw.write(orderedMatrxi[i][j] + "");
                    } else {
                        bw.write(orderedMatrxi[i][j] + " ");
                    }
                }
                bw.newLine();
            }

            bw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }


        return im;
    }


    private static void byColmn(IncidenceMatrix im, int column, ArrayList<Integer> branch, ArrayList<ArrayList<Integer>> listofBranches, int parent) {
        int degree = 0;

        //detect end
        for (int j = 0; j < im.getMatrix().length; j++) {
            if (im.getMatrix()[j][column] != 0) {
                degree++;
            }
        }
        if (degree != 2) {
            branch.add(column);
            listofBranches.add(branch);
            branch = new ArrayList<>();
        } else {

            for (int i = 0; i < im.getMatrix().length; i++) {
                if (i != parent && !tranOrder.contains(i)) {
                    if (im.getMatrix()[i][column] != 0) {
                        branch.add(column);
                        byRow(im, i, branch, listofBranches, column);
                        //add go by column
                    }
                }
            }
        }
    }

    private static void byRow(IncidenceMatrix im, int row, ArrayList<Integer> branch, ArrayList<ArrayList<Integer>> listofBranches, int parent) {
        int degree = 0;

        for (int j = 0; j < im.getMatrix()[row].length; j++) {
            if (im.getMatrix()[row][j] != 0) {
                degree++;
            }
        }
        if (degree != 2) {
            branch.add(row);
            listofBranches.add(branch);
            branch = new ArrayList<>();
        } else {
            for (int i = 0; i < im.getMatrix()[row].length; i++) {
                if (i != parent && !placeOrder.contains(i)) {
                    if (im.getMatrix()[row][i] != 0) {
                        branch.add(row);
                        byColmn(im, i, branch, listofBranches, row);
                        //add go by column
                    }
                }
            }
        }
        //stop

        //dodanie

    }

    public void greatCommonConnectedSubgraphAlgorithm() {

    }


    public static IncidenceMatrix subIncMat(SubnetCalculator.SubNet subNet) {
        InputMatrix inMatrix = new InputMatrix(subNet.getSubTransitions(), subNet.getSubPlaces());
        OutputMatrix outMatrix = new OutputMatrix(subNet.getSubTransitions(), subNet.getSubPlaces());
        return new IncidenceMatrix(subNet.getSubTransitions(), subNet.getSubPlaces(), inMatrix, outMatrix);
    }

    private void setMatrix(IncidenceMatrix matrix) {
        this.matrix = matrix;
    }

    static class LenghtComparator implements Comparator<ArrayList<Integer>> {
        public int compare(ArrayList<Integer> l1, ArrayList<Integer> l2) {
            return l2.size() - l1.size();
        }
    }
}
