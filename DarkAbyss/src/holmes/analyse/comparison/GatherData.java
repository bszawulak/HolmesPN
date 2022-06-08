package holmes.analyse.comparison;

import holmes.analyse.GraphletsCalculator;
import holmes.darkgui.GUIManager;

import javax.swing.table.DefaultTableModel;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Stream;

public class GatherData {

    String directory = "/home/bszawulak/Dokumenty/Eksperyment/Wyniki";
    Double[][][][] branchResult;
    Double[][][][] drgfResults;
    Integer[][][][][] drgfValues;
    Integer[][][][][] branchValues;
    int numberOfGraphlets = 151;


    public GatherData(boolean t) {
        branchValues = new Integer[8][8][100][6][50];
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                for (int p = 0; p < 100; p++) {
                    branchValues[i][j][p][0] = extractedBr(i, j, p, "branch-similarity-V0");
                    branchValues[i][j][p][1] = extractedBr(i, j, p, "branch-similarity-V1");
                    branchValues[i][j][p][2] = extractedBr(i, j, p, "branch-similarity-V2");
                    branchValues[i][j][p][3] = extractedBr(i, j, p, "branch-similarity-V3");
                    branchValues[i][j][p][4] = extractedBr(i, j, p, "branch-similarity-V4");
                    branchValues[i][j][p][5] = extractedBr(i, j, p, "branch-similarity-V5");
                }
            }
        }

        //int type =0;


        branchResult = new Double[8][8][6][3];

        for (int type = 0; type < 6; type++) {
            for (int i = 0; i < 8; i++) {
                for (int j = 0; j < 8; j++) {
                    Double[][] forSingleNet = new Double[100][3];
                    ArrayList<Double> min = new ArrayList<>();
                    ArrayList<Double> max = new ArrayList<>();
                    ArrayList<Double> avg = new ArrayList<>();
                    for (int p1 = 0; p1 < 100; p1++) {
                                //result.add((Arrays.stream(branchValues[i][j][p1][type]).mapToLong(k -> k).toArray(), Arrays.stream(branchValues[i][j][p2][type]).mapToLong(k -> k).toArray()));
                    }

                    branchResult[i][j][type][0] = Collections.max(max);
                    branchResult[i][j][type][1] = Collections.min(min);
                    branchResult[i][j][type][2] = average(avg);
                }
            }
        }
        compressDataToCSV();

    }

    private void compressDataToCSV() {
        FileWriter writer = null;
        try {
            writer = new FileWriter(directory + "/DRGF-RESULTS.txt");

            for (int type = 0; type < 10; type++) {
                writer.append("type" + type + "\n");
                for (int mma = 0; mma < 3; mma++) {
                    writer.append("mma" + mma + "\n");
                    for (int i = 0; i < 8; i++) {
                        for (int j = 0; j < 8; j++) {
                            writer.append(drgfResults[i][j][type][mma].toString());
                            writer.append(",");
                        }
                        writer.append("\n");
                    }
                }
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //-------------------------------------------------------------------------------

    public GatherData() {
        drgfValues = new Integer[8][8][100][10][151];
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                for (int p = 0; p < 100; p++) {
                    drgfValues[i][j][p][0] = extracted(i, j, p, "BASE");
                    drgfValues[i][j][p][1] = extracted(i, j, p, "C6VARIANT");
                    drgfValues[i][j][p][2] = extracted(i, j, p, "E2VARIANT");
                    drgfValues[i][j][p][3] = extracted(i, j, p, "K4LkVARIANT");
                    drgfValues[i][j][p][4] = extracted(i, j, p, "K4LVARIANT");
                    drgfValues[i][j][p][5] = extracted(i, j, p, "P3VARIANT");
                    drgfValues[i][j][p][6] = extracted(i, j, p, "S4VARIANT");
                    drgfValues[i][j][p][7] = extracted(i, j, p, "SS4VARIANT");
                    drgfValues[i][j][p][8] = extracted(i, j, p, "SSS4VARIANT");
                    drgfValues[i][j][p][9] = extracted(i, j, p, "ALLVARIANT");
                }
            }
        }

        //int type =0;


        drgfResults = new Double[8][8][10][3];

        for (int type = 0; type < 10; type++) {
            for (int i = 0; i < 8; i++) {
                for (int j = 0; j < 8; j++) {
                    Double[][] forSingleNet = new Double[100][3];
                    ArrayList<Double> min = new ArrayList<>();
                    ArrayList<Double> max = new ArrayList<>();
                    ArrayList<Double> avg = new ArrayList<>();
                    for (int p1 = 0; p1 < 100; p1++) {
                        //double[] result = new double[100];
                        ArrayList<Double> result = new ArrayList<>();
                        for (int p2 = 0; p2 < 100; p2++) {
                            if (p1 != p2) {
                                result.add(compareDRGF(Arrays.stream(drgfValues[i][j][p1][type]).mapToLong(k -> k).toArray(), Arrays.stream(drgfValues[i][j][p2][type]).mapToLong(k -> k).toArray()));
                            }
                        }
                        //forSingleNet[p1][0] = Collections.max(result);
                        //forSingleNet[p1][1] = Collections.min(result);
                        //forSingleNet[p1][2] = average(result);

                        min.add(Collections.min(result));
                        max.add(Collections.max(result));
                        avg.add(average(result));


                    }

                    drgfResults[i][j][type][0] = Collections.max(max);
                    drgfResults[i][j][type][1] = Collections.min(min);
                    drgfResults[i][j][type][2] = average(avg);
                }
            }
        }
        compressDRGFDataToCSV();

    }

    private void compressDRGFDataToCSV() {
        FileWriter writer = null;
        try {
            writer = new FileWriter(directory + "/DRGF-RESULTS.txt");

            for (int type = 0; type < 10; type++) {
                writer.append("type" + type + "\n");
                for (int mma = 0; mma < 3; mma++) {
                    writer.append("mma" + mma + "\n");
                    for (int i = 0; i < 8; i++) {
                        for (int j = 0; j < 8; j++) {
                            writer.append(drgfResults[i][j][type][mma].toString());
                            writer.append(",");
                        }
                        writer.append("\n");
                    }
                }
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private double compareDRGF(long[] firstSingleDRGF, long[] secondSingleDRGF) {
        long firstSum = Arrays.stream(firstSingleDRGF).sum();
        long secondSum = Arrays.stream(secondSingleDRGF).sum();

        long[] distanceDRGF = new long[numberOfGraphlets];
        double result = 0;
        for (int i = 0; i < numberOfGraphlets; i++) {
            //distanceDRGF[i] = Math.abs(firstSingleDRGF[i] - secondSingleDRGF[i]);

            if(firstSingleDRGF[i] !=0.0 && secondSingleDRGF[i] !=0.0)
            {
                result += Math.abs((Math.log(firstSingleDRGF[i]) / Math.log(firstSum)) - (Math.log(secondSingleDRGF[i]) / Math.log(secondSum)));
            }else if(firstSingleDRGF[i] !=0.0 )
            {
                result += Math.abs((Math.log(firstSingleDRGF[i]) / Math.log(firstSum)) - 0.0 );
            }else if(secondSingleDRGF[i] !=0.0 )
            {
                result += Math.abs(0.0 - (Math.log(secondSingleDRGF[i]) / Math.log(secondSum)));
            }
            else{
                //dwa zera
            }

            /**
             * if(firstSingleDRGF[i] !=0.0 && secondSingleDRGF[i] !=0.0)
             *             {
             *                 //System.out.println("i:" +i+ " "+Math.abs((Math.log(firstSingleDRGF[i]) / Math.log(firstSum)) - (Math.log(secondSingleDRGF[i]) / Math.log(secondSum))));
             *                 result += Math.abs((Math.log((double) firstSingleDRGF[i] / (double)firstSum)) - (Math.log(secondSingleDRGF[i]) / Math.log(secondSum)));
             *             }else if(firstSingleDRGF[i] !=0.0 )
             *             {
             *                 //System.out.println("i:" +i+ " "+Math.abs((Math.log(firstSingleDRGF[i]) / Math.log(firstSum)) - (Math.log(secondSingleDRGF[i]) / Math.log(secondSum))));
             *                 result += Math.abs((Math.log((double) firstSingleDRGF[i] / (double)firstSum))- 0.0 );
             *             }else if(secondSingleDRGF[i] !=0.0 )
             *             {
             *                 //System.out.println("i:" +i+ " "+Math.abs((Math.log(firstSingleDRGF[i]) / Math.log(firstSum)) - (Math.log(secondSingleDRGF[i]) / Math.log(secondSum))));
             *                 result += Math.abs(0.0 - (Math.log((double) secondSingleDRGF[i] / (double) secondSum)));
             *             }
             *             else{
             *                 //dwa zera
             *             }
             */
        }
        //if(result>1.0)
       // {
      //      System.out.println("ToTi");
       // }
        return result;
    }

    private Integer[] extracted(int i, int j, int p, String name) {
        List<String[]> records = new ArrayList<>();
        String tmp = directory + setPath(i * 5, j * 5, p) + "DRGF-" + name+".txt";
        try (BufferedReader br = new BufferedReader(new FileReader(tmp))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                records.add(values);
            }
        } catch (Exception e) {

        }
        Integer[] result = new Integer[records.get(0).length];
        for (int k = 0 ; k < records.get(0).length ; k++)
        {
            result[k]=Integer.parseInt(records.get(0)[k]);
        }

        return result;
    }

    private Integer[] extractedBr(int i, int j, int p, String name) {
        List<String[]> records = new ArrayList<>();
        String tmp = directory + setPath(i * 5, j * 5, p)  + name+".txt";
        try (BufferedReader br = new BufferedReader(new FileReader(tmp))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                records.add(values);
            }
        } catch (Exception e) {

        }
        Integer[] result = new Integer[records.get(0).length];
        for (int k = 0 ; k < records.get(0).length ; k++)
        {
            result[k]=Integer.parseInt(records.get(0)[k]);
        }

        return result;
    }

    private String setPath(int i, int j, int p) {
        return "/i" + i + "j" + j + "/i" + i + "j" + j + "p" + p + "/";
    }

    public double average(ArrayList<Double> array) {
        Double sum = 0.0;

        for (Double d : array
        ) {
            sum += d;
        }
        return sum / array.size();
    }
}
