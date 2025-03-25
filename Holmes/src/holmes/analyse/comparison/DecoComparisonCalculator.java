package holmes.analyse.comparison;

import holmes.analyse.SubnetCalculator;
import holmes.analyse.matrix.IncidenceMatrix;
import holmes.analyse.matrix.InputMatrix;
import holmes.analyse.matrix.OutputMatrix;
import holmes.petrinet.data.PetriNet;

import javax.swing.*;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
;

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

    public void calculate(ArrayList<SubnetCalculator.SubNet> subnetsList, ArrayList<int[][]> secondSubnetsIncidenceList) {

    }

    public static ArrayList<int[][]> voa(int[][] im) {

        boolean[] isbranch = new boolean[im[0].length];
        ArrayList<Integer> branchRows = new ArrayList<>();
        int[] degree = new int[im.length];

        for (int r = 0; r < im.length; r++) {
            int[] row = im[r];
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

        //TODO what if there is no branches

        //if(branchRows.size()>0)
            tranOrder.add(branchRows.get(0));

        //co jeśli są równe
        ///for(int ti = 0 ; ti < maxRows.size(); ti++) {


        ArrayList<VertexOrder> listOfVO = new ArrayList<>();

        //int t = branchRows.get(ti);

        //TODO
        //if(branchRows.size()>0)
            listOfVO.add(new VertexOrder(branchRows.get(0)));

        for (int ti = 0; ti < branchRows.size(); ti++) {
            ArrayList<ArrayList<Integer>> listOfBranches = new ArrayList<>();

            //maxRows
            int t = branchRows.get(ti);

            for (int i = 0; i < im[t].length; i++) {
                //if(branchRows.contains())
                if (im[t][i] != 0) {
                    ArrayList<Integer> branch = new ArrayList<>();
                    branch.add(t);
                    byColmn(im, i, branch, listOfBranches, t);
                }
            }

            //Variant II

            //sortowanie
            Collections.sort(listOfBranches, new LenghtComparator());
            ArrayList<ArrayList<Integer>> tmpLoB = listOfBranches;

            while (tmpLoB.size() > 0) {
                ArrayList<ArrayList<Integer>> sameLengthBranch = new ArrayList<>();
                sameLengthBranch = (ArrayList<ArrayList<Integer>>) tmpLoB.stream().filter(x -> x.size() == tmpLoB.get(0).size()).collect(Collectors.toList());
                //TODO brach case and weight case? weight case 100%

                if (sameLengthBranch.size() > 1) {

                    ArrayList<ArrayList<ArrayList<Integer>>> sortedSameLenght = permute(sameLengthBranch.toArray());
                    ArrayList<VertexOrder> oldlistOfVO = cloneList(listOfVO);
                    listOfVO = new ArrayList<>();
                    for (ArrayList<ArrayList<Integer>> perm : sortedSameLenght) {
                        listOfVO.addAll(calcVertexOrderFromBranch2(im, oldlistOfVO, perm));
                    }
                } else {
                    calcVertexOrderFromBranch(im, listOfVO, sameLengthBranch);
                }
                tmpLoB.removeAll(sameLengthBranch);
            }

        }

        //Varant I
          /*
            Collections.sort(listOfBranches, new LenghtComparator());

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
        */

        ArrayList<int[][]> listOfOrderedMatrixes = new ArrayList<>();

        for (VertexOrder vo : listOfVO) {
            int[][] orderedMatrxi = new int[vo.transitionOrder.size()][vo.placeOrder.size()];


            for (int i = 0; i < vo.transitionOrder.size(); i++) {
                for (int j = 0; j < vo.placeOrder.size(); j++) {
                    orderedMatrxi[i][j] = im[vo.transitionOrder.get(i)][vo.placeOrder.get(j)];
                }
            }
            listOfOrderedMatrixes.add(orderedMatrxi);

            //////// TEST ZAPIS ///////

            try {
                BufferedWriter bw = new BufferedWriter(new FileWriter("orderedMatrix-" + listOfVO.indexOf(vo) + ".txt"));
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
        }

        return listOfOrderedMatrixes;
    }

    /*
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


          ArrayList<VertexOrder> listOfVO = new ArrayList<>();

          //int t = branchRows.get(ti);

          listOfVO.add(new VertexOrder(branchRows.get(0)));

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

              //Variant II

              //sortowanie
              Collections.sort(listOfBranches, new LenghtComparator());
              ArrayList<ArrayList<Integer>> tmpLoB = listOfBranches;

              while (tmpLoB.size() > 0) {
                  ArrayList<ArrayList<Integer>> sameLengthBranch = new ArrayList<>();
                  sameLengthBranch = (ArrayList<ArrayList<Integer>>) tmpLoB.stream().filter(x -> x.size() == tmpLoB.get(0).size()).collect(Collectors.toList());
                  //TODO brach case and weight case? weight case 100%

                  if (sameLengthBranch.size() > 1) {

                      ArrayList<ArrayList<ArrayList<Integer>>> sortedSameLenght = permute(sameLengthBranch.toArray());
                      ArrayList<VertexOrder> oldlistOfVO = cloneList(listOfVO);
                      listOfVO = new ArrayList<>();
                      for (ArrayList<ArrayList<Integer>> perm : sortedSameLenght) {
                          listOfVO.addAll(calcVertexOrderFromBranch2(im, oldlistOfVO, perm));
                      }
                  } else {
                      calcVertexOrderFromBranch(im, listOfVO, sameLengthBranch);
                  }
                  tmpLoB.removeAll(sameLengthBranch);
              }

          }

          //Varant I
            /*
              Collections.sort(listOfBranches, new LenghtComparator());

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
          */
/*
        for (VertexOrder vo : listOfVO) {
            int[][] orderedMatrxi = new int[vo.transitionOrder.size()][vo.placeOrder.size()];

            for (int i = 0; i < vo.transitionOrder.size(); i++) {
                for (int j = 0; j < vo.placeOrder.size(); j++) {
                    orderedMatrxi[i][j] = im.getMatrix()[vo.transitionOrder.get(i)][vo.placeOrder.get(j)];
                }
            }

            //////// TEST ZAPIS ///////

            try {
                BufferedWriter bw = new BufferedWriter(new FileWriter("orderedMatrix-" + listOfVO.indexOf(vo) + ".txt"));
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
        }

        return im;
    }
*/
    private static void calcVertexOrderFromBranch(int[][] im, ArrayList<VertexOrder> listOfVO, ArrayList<ArrayList<Integer>> sameLengthBranch) {
        for (ArrayList<Integer> branch : sameLengthBranch) {
            for (int j = 0; j < listOfVO.size(); j++) {
                for (int i = 1; i < branch.size(); i++) {
                    if (im[branch.get(0)][branch.get(1)] > 0) {
                        if (i % 2 == 0) {
                            listOfVO.get(j).transitionOrder.add(0, branch.get(i));
                        } else {
                            listOfVO.get(j).placeOrder.add(0, branch.get(i));
                        }
                    }
                    if (im[branch.get(0)][branch.get(1)] < 0) {
                        //for (int i = 1; i < branch.size(); i++) {
                        if (i % 2 == 0) {
                            listOfVO.get(j).transitionOrder.add(branch.get(i));
                        } else {
                            listOfVO.get(j).placeOrder.add(branch.get(i));
                        }
                        //}
                    }
                }
            }
        }
    }

    public static ArrayList<VertexOrder> cloneList(ArrayList<VertexOrder> dogList) {
        ArrayList<VertexOrder> clonedList = new ArrayList<VertexOrder>(dogList.size());
        for (VertexOrder dog : dogList) {
            clonedList.add(new VertexOrder(dog));
        }
        return clonedList;
    }

    private static ArrayList<VertexOrder> calcVertexOrderFromBranch2(int[][] im, ArrayList<VertexOrder> newVO, ArrayList<ArrayList<Integer>> sameLengthBranch) {
        ArrayList<VertexOrder> listOfVO = cloneList(newVO);

        for (int j = 0; j < listOfVO.size(); j++) {
            for (ArrayList<Integer> branch : sameLengthBranch) {
                //VertexOrder nvo = new VertexOrder();

                //nvo.transitionOrder = new ArrayList<Integer>(listOfVO.get(j).transitionOrder);
                //nvo.placeOrder = new ArrayList<Integer>(listOfVO.get(j).placeOrder);
                for (int i = 1; i < branch.size(); i++) {
                    if (im[branch.get(0)][branch.get(1)] > 0) {
                        if (i % 2 == 0) {
                            listOfVO.get(j).transitionOrder.add(0, branch.get(i));
                        } else {
                            listOfVO.get(j).placeOrder.add(0, branch.get(i));
                        }
                    }
                    if (im[branch.get(0)][branch.get(1)] < 0) {
                        //for (int i = 1; i < branch.size(); i++) {
                        if (i % 2 == 0) {
                            listOfVO.get(j).transitionOrder.add(branch.get(i));
                        } else {
                            listOfVO.get(j).placeOrder.add(branch.get(i));
                        }
                        //}
                    }
                }
                //newVO.add(nvo);
            }
        }
        return listOfVO;
    }


    ///copy

    public static ArrayList<ArrayList<ArrayList<Integer>>> permute(Object[] nums) {
        ArrayList<ArrayList<ArrayList<Integer>>> result = new ArrayList<>();
        helper(0, nums, result);
        return result;
    }

    private static void helper(int start, Object[] nums, ArrayList<ArrayList<ArrayList<Integer>>> result) {
        if (start == nums.length - 1) {
            ArrayList<ArrayList<Integer>> list = new ArrayList<>();
            for (Object num : nums) {
                list.add((ArrayList<Integer>) num);
            }
            result.add(list);
            return;
        }

        for (int i = start; i < nums.length; i++) {
            swap(nums, i, start);
            helper(start + 1, nums, result);
            swap(nums, i, start);
        }
    }

    private static void swap(Object[] nums, int i, int j) {
        Object temp = nums[i];
        nums[i] = nums[j];
        nums[j] = temp;
    }


    ///


    private static void byColmn(int[][] im, int column, ArrayList<Integer> branch, ArrayList<ArrayList<Integer>> listofBranches, int parent) {
        int degree = 0;

        //detect end
        for (int j = 0; j < im.length; j++) {
            if (im[j][column] != 0) {
                degree++;
            }
        }
        if (degree != 2) {
            branch.add(column);
            listofBranches.add(branch);
            branch = new ArrayList<>();
        } else {

            for (int i = 0; i < im.length; i++) {
                if (i != parent && !tranOrder.contains(i)) {
                    if (im[i][column] != 0) {
                        branch.add(column);
                        byRow(im, i, branch, listofBranches, column);
                        //add go by column
                    }
                }
            }
        }
    }

    private static void byRow(int[][] im, int row, ArrayList<Integer> branch, ArrayList<ArrayList<Integer>> listofBranches, int parent) {
        int degree = 0;

        for (int j = 0; j < im[row].length; j++) {
            if (im[row][j] != 0) {
                degree++;
            }
        }
        if (degree != 2) {
            branch.add(row);
            listofBranches.add(branch);
            branch = new ArrayList<>();
        } else {
            for (int i = 0; i < im[row].length; i++) {
                if (i != parent && !placeOrder.contains(i)) {
                    if (im[row][i] != 0) {
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

    static class VertexOrder {
        public ArrayList<Integer> placeOrder;
        public ArrayList<Integer> transitionOrder;

        public VertexOrder() {
            placeOrder = new ArrayList<>();
            transitionOrder = new ArrayList<>();
        }

        public VertexOrder(int t) {
            placeOrder = new ArrayList<>();
            transitionOrder = new ArrayList<>();
            transitionOrder.add(t);
        }

        public VertexOrder(VertexOrder vo) {
            this.placeOrder = (ArrayList<Integer>) vo.placeOrder.clone();
            this.transitionOrder = (ArrayList<Integer>) vo.transitionOrder.clone();
        }

    }

    static public class GteatCommonSubnet {
        int firstNetId = -1;
        int seconNetId = -1;
        public ArrayList<int[][]> firstSubnetMaps;
        public ArrayList<int[][]> secondSubneMapst;
        //może wciąż zbiór najlepszych?
        public ArrayList<int[][]> gcs;
        int fnType = -1;
        int snType = -1;

        public GteatCommonSubnet(ArrayList<int[][]> fsm, ArrayList<int[][]> ssm, int one, int two) {
            firstNetId = one;
            seconNetId = two;
            firstSubnetMaps = fsm;
            secondSubneMapst = ssm;
            gcs = compareIM();
        }

        private ArrayList<int[][]> compareIM() {
            ArrayList<BranchRow> branchRowsFN = new ArrayList<>();
            ArrayList<BranchRow> branchRowsSN = new ArrayList<>();
            for (int i = 0; i < firstSubnetMaps.size(); i++) {
                branchRowsFN = getBranchRows(firstSubnetMaps.get(i), i);
            }

            for (int i = 0; i < secondSubneMapst.size(); i++) {
                branchRowsSN = getBranchRows(secondSubneMapst.get(i), i);
            }

            //TODO for each map number
            for(int k = 0; k < firstSubnetMaps.size() ; k++){
                int finalK = k;
                ArrayList<BranchRow> branchesOfFirstSubnets = (ArrayList)branchRowsFN.stream().filter(x-> x.mapNumber== finalK).collect(Collectors.toList());

                //multiple branch

                for(int l = 0 ; l < secondSubneMapst.size(); l++){


                    int finall = l;
                    ArrayList<BranchRow> branchesOfSecondSubnets = (ArrayList)branchRowsSN.stream().filter(x-> x.mapNumber== finall).collect(Collectors.toList());
                    int x = Math.max(branchesOfFirstSubnets.size(), 1);//firstSubnetMaps.get(0).length,secondSubneMapst.get(0).length);
                    int y = Math.max(branchesOfSecondSubnets.size(), 1);//firstSubnetMaps.get(0)[0].length,secondSubneMapst.get(0)[0].length);
                    BranchMatching[][] bmMatrix = new BranchMatching[x][y];

                    // n n



                    if (branchesOfFirstSubnets.size() > 0 && branchesOfSecondSubnets.size() > 0) {
                        for (int i = 0; i < branchesOfFirstSubnets.size(); i++) {
                            for (int j = 0; j < branchesOfSecondSubnets.size(); j++) {
                                bmMatrix[i][j] = calcGCS(branchesOfFirstSubnets.get(i), branchesOfSecondSubnets.get(j));
                            }
                        }
                    } else {
                        if (branchesOfFirstSubnets.size() > 0) {
                            for (int i = 0; i < branchesOfFirstSubnets.size(); i++)
                                bmMatrix[i][0] = calcGCSsC(branchesOfFirstSubnets.get(i), new BranchRow(0,l));
                        } else if (branchesOfSecondSubnets.size() > 0) {
                            for (int j = 0; j < branchesOfSecondSubnets.size(); j++)
                                bmMatrix[0][j] = calcGCS( new BranchRow(0,k), branchesOfSecondSubnets.get(j));
                        } else {
                            bmMatrix[0][0] = calcGCS( new BranchRow(0,k),  new BranchRow(0,l));
                            //cykle
                            //bmMatrix[0][0] = calcGCS(breanchRowsF.get(i), breanchRowsS.get(j));
                            //JOptionPane.showMessageDialog(null, "DO zakodzenia przypadek cykliczny", "WARNING MESSAGE", JOptionPane.WARNING_MESSAGE);
                            //TODO dla nie branchowych
                        }
                    }

                    // n 0

                    // 0 n

                    // 0 0




                    double[][] costMatrix = new double[bmMatrix.length][bmMatrix[0].length];

                    for (int i = 0; i < bmMatrix.length; i++)
                        for (int j = 0; j < bmMatrix[0].length; j++)
                            costMatrix[i][j] = 100 - bmMatrix[0][j].matchVal;

                    //czy dodawać puste

                    //TODO coś sensownego za 100

                    //czy dodawać puste
                    Hungarian h = new Hungarian(costMatrix);
                    int[] result = h.execute();

                    //TODO wybierz wspólne grafy o maxymalnych wartościach i zwróć je

                    for(int i = 0 ; i < result.length ; i ++){
                        ArrayList<Integer> row = bmMatrix[i][result[i]].row;
                        //create and add gcs
                    }
                }
            }

            //take only best gcs

            return new ArrayList<int[][]>();
        }

        private BranchMatching calcGCS(BranchRow branchRow, BranchRow branchRow1) {
            int[][] fm = firstSubnetMaps.get(branchRow.mapNumber);
            int[][] sm = secondSubneMapst.get(branchRow1.mapNumber);

            int fi = branchRow.row;
            int si = branchRow1.row;
            int compVal = 0;

            //backtracking!
            int startIndexFN = 0;
            for (int i = 0; i < fm[branchRow.row].length; i++) {
                if (fm[branchRow.row][i] > 0) {
                    startIndexFN = i;
                    //break;
                } else {
                    if (fm[branchRow.row][i] < 0) {
                        break;
                    }
                }
            }
            int startIndexSN = 0;
            for (int i = 0; i < sm[branchRow.row].length; i++) {
                if (sm[branchRow1.row][i] > 0) {
                    startIndexSN = i;
                    //break;
                } else {
                    if (sm[branchRow1.row][i] < 0) {
                        break;
                    }
                }
            }

            int sindex = startIndexSN;
            int findex = startIndexFN;
            HashMap<Integer, Integer> columnMap = new HashMap<Integer, Integer>();
            HashMap<Integer, Integer> rowMap = new HashMap<Integer, Integer>();
            ArrayList<Integer> newRow = new ArrayList<>();
            for (int i = startIndexFN; i >= 0; i--) {
                if(sindex < 0)
                {
                    break;
                }
                if (fm[branchRow.row][i] == sm[branchRow1.row][sindex]) {
                    if (fm[branchRow.row][i] == 0) {
                        //calc sim val by comp diag
                        if (fm[i][i] == sm[sindex][sindex]) {
                            newRow.add(fm[branchRow.row][i]);
                            columnMap.put(i, sindex);
                            sindex--;
                            //sim val
                        } else {
                            newRow.add(fm[branchRow.row][i]);
                            columnMap.put(i, sindex);
                            sindex--;
                            //sim val
                        }
                    } else {
                        newRow.add(fm[branchRow.row][i]);
                        columnMap.put(i, sindex);
                        sindex--;
                        //sim val
                    }
                    //good
                } else if (fm[branchRow.row][i] > sm[branchRow1.row][sindex] && sm[branchRow1.row][sindex] != 0) {
                    newRow.add(sm[branchRow1.row][sindex]);
                    columnMap.put(i, sindex);
                    sindex--;
                    //sim val
                } else if (fm[branchRow.row][i] == 0 && sm[branchRow1.row][sindex] > 0) {
                    //nie dodawaj zatrzymaj sindex
                } else if (fm[branchRow.row][i] > 0 && sm[branchRow1.row][sindex] == 0) {
                    //idź w przód aż znajdziesz 1 lub zakończ
                    while (sindex > 0 && sm[branchRow1.row][sindex] == 0) {
                        sindex--;
                    }
                    newRow.add(fm[branchRow.row][i]);
                    columnMap.put(i, sindex);
                    sindex--;
                } else {

                    JOptionPane.showMessageDialog(null, "Nieprzewidziany przypadek 781", "WARNING MESSAGE", JOptionPane.WARNING_MESSAGE);
                }

                if (sindex < 0)
                    break;
            }

            sindex = startIndexSN+1;
            startIndexFN++;

            for (int i = startIndexFN; i < fm[branchRow.row].length; i++) {
                if(sindex >= sm[branchRow1.row].length)
                {
                    break;
                }

                if (fm[branchRow.row][i] == sm[branchRow1.row][sindex]) {
                    newRow.add(fm[branchRow.row][i]);
                    columnMap.put(i, sindex);
                    sindex++;
                } else if (fm[branchRow.row][i] < sm[branchRow1.row][sindex] && sm[branchRow1.row][sindex] != 0) {
                    newRow.add(sm[branchRow1.row][sindex]);
                    columnMap.put(i, sindex);
                    sindex++;
                } else if (fm[branchRow.row][i] == 0 && sm[branchRow1.row][sindex] < 0) {

                } else if (fm[branchRow.row][i] < 0 && sm[branchRow1.row][sindex] == 0) {
                    while (sindex <= fm[branchRow.row].length && sm[branchRow1.row][sindex] == 0) {
                        sindex++;
                    }
                    newRow.add(fm[branchRow.row][i]);
                    columnMap.put(i, sindex);
                    sindex++;
                } else {
                    JOptionPane.showMessageDialog(null, "Nieprzewidziany przypadek 815", "WARNING MESSAGE", JOptionPane.WARNING_MESSAGE);
                }
            }

            return new BranchMatching(columnMap, newRow, compVal);
        }
        private BranchMatching calcGCSsC(BranchRow branchRow, BranchRow branchRow1) {
            int[][] fm = firstSubnetMaps.get(branchRow.mapNumber);
            int[][] sm = secondSubneMapst.get(branchRow1.mapNumber);

            int fi = branchRow.row;
            int si = branchRow1.row;
            int compVal = 0;

            //backtracking!
            int startIndexFN = 0;
            for (int i = 0; i < fm[branchRow.row].length; i++) {
                if (fm[branchRow.row][i] > 0) {
                    startIndexFN = i;
                    //break;
                } else {
                    if (fm[branchRow.row][i] < 0) {
                        break;
                    }
                }
            }
            int startIndexSN = 0;
            for (int i = 0; i < sm[branchRow.row].length; i++) {
                if (sm[branchRow1.row][i] > 0) {
                    startIndexSN = i;
                    //break;
                } else {
                    if (sm[branchRow1.row][i] < 0) {
                        break;
                    }
                }
            }

            int sindex = startIndexSN;
            int findex = startIndexFN;
            HashMap<Integer, Integer> columnMap = new HashMap<Integer, Integer>();
            HashMap<Integer, Integer> rowMap = new HashMap<Integer, Integer>();
            ArrayList<Integer> newRow = new ArrayList<>();
            for (int i = startIndexFN; i >= 0; i--) {
                if(sindex < 0)
                {
                    break;
                }
                if (fm[branchRow.row][i] == sm[branchRow1.row][sindex]) {
                    if (fm[branchRow.row][i] == 0) {
                        //calc sim val by comp diag
                        if (fm[i][i] == sm[sindex][sindex]) {
                            newRow.add(fm[branchRow.row][i]);
                            columnMap.put(i, sindex);
                            sindex--;
                            //sim val
                        } else {
                            newRow.add(fm[branchRow.row][i]);
                            columnMap.put(i, sindex);
                            sindex--;
                            //sim val
                        }
                    } else {
                        newRow.add(fm[branchRow.row][i]);
                        columnMap.put(i, sindex);
                        sindex--;
                        //sim val
                    }
                    //good
                } else if (fm[branchRow.row][i] > sm[branchRow1.row][sindex] && sm[branchRow1.row][sindex] != 0) {
                    newRow.add(sm[branchRow1.row][sindex]);
                    columnMap.put(i, sindex);
                    sindex--;
                    //sim val
                } else if (fm[branchRow.row][i] == 0 && sm[branchRow1.row][sindex] > 0) {
                    //nie dodawaj zatrzymaj sindex
                } else if (fm[branchRow.row][i] > 0 && sm[branchRow1.row][sindex] == 0) {
                    //idź w przód aż znajdziesz 1 lub zakończ
                    while (sindex > 0 && sm[branchRow1.row][sindex] == 0) {
                        sindex--;
                    }
                    newRow.add(fm[branchRow.row][i]);
                    columnMap.put(i, sindex);
                    sindex--;
                } else {
                    JOptionPane.showMessageDialog(null, "Nieprzewidziany przypadek", "WARNING MESSAGE", JOptionPane.WARNING_MESSAGE);
                }

                if (sindex < 0)
                    break;
            }

            sindex = startIndexSN+1;
            startIndexFN++;

            for (int i = startIndexFN; i < fm[branchRow.row].length; i++) {
                if(sindex >= sm[branchRow1.row].length)
                {
                    break;
                }

                if (fm[branchRow.row][i] == sm[branchRow1.row][sindex]) {
                    newRow.add(fm[branchRow.row][i]);
                    columnMap.put(i, sindex);
                    sindex++;
                } else if (fm[branchRow.row][i] < sm[branchRow1.row][sindex] && sm[branchRow1.row][sindex] != 0) {
                    newRow.add(sm[branchRow1.row][sindex]);
                    columnMap.put(i, sindex);
                    sindex++;
                } else if (fm[branchRow.row][i] == 0 && sm[branchRow1.row][sindex] < 0) {

                } else if (fm[branchRow.row][i] < 0 && sm[branchRow1.row][sindex] == 0) {
                    while (sindex <= fm[branchRow.row].length && sm[branchRow1.row][sindex] == 0) {
                        sindex++;
                    }
                    newRow.add(fm[branchRow.row][i]);
                    columnMap.put(i, sindex);
                    sindex++;
                } else {
                    JOptionPane.showMessageDialog(null, "Nieprzewidziany przypadek", "WARNING MESSAGE", JOptionPane.WARNING_MESSAGE);
                }
            }

            return new BranchMatching(columnMap, newRow, compVal);
        }

        private ArrayList<BranchRow> getBranchRows(int[][] firstSubnetMaps, int mapNumber) {
            ArrayList<BranchRow> branchRows = new ArrayList<>();
            for (int i = 0; i < firstSubnetMaps.length; i++) {
                int branchCounterP = 0;
                int branchCounterM = 0;
                for (int j = 0; j < firstSubnetMaps[i].length; j++) {
                    if (firstSubnetMaps[i][j] > 0)
                        branchCounterP++;
                    if (firstSubnetMaps[i][j] < 0)
                        branchCounterM++;
                }

                if (branchCounterP > 1 || branchCounterM > 1)
                    branchRows.add(new BranchRow(i, mapNumber));

            }

            return branchRows;
        }
    }

    public static class GCS {
        int[][] gcs;
        public Map<Integer, Integer> s1TToGcs = new HashMap<>();
        public Map<Integer, Integer> s2RToGcs = new HashMap<>();


        public Map<Integer, Integer> s1PToGcs = new HashMap<>();
        public Map<Integer, Integer> s2PToGcs = new HashMap<>();
        int value = -1;

        public GCS(int[][] g, int v) {
            this.gcs = g;
            this.value = v;
        }
    }

    public static class BranchRow {
        int row = -1;
        int mapNumber = -1;

        BranchRow(int r, int mn) {
            this.row = r;
            this.mapNumber = mn;
        }
    }

    public static class BranchMatching {
        public HashMap<Integer, Integer> columnMathing;
        public HashMap<Integer, Integer> rowMathing;
        public ArrayList<Integer> row;
        public int matchVal;

        BranchMatching(HashMap<Integer, Integer> cm, ArrayList<Integer> r, int mv) {
            this.columnMathing = cm;
            this.row = r;
            this.matchVal = mv;
        }
    }
}
