package holmes.analyse;

import holmes.darkgui.GUIManager;
import holmes.darkgui.LanguageManager;
import holmes.windows.decompositions.HolmesComparisonModule;

import javax.swing.table.DefaultTableModel;

public class GDDAcalculator implements Runnable {
    private HolmesComparisonModule masterWindow = null;
    private static final GUIManager overlord = GUIManager.getDefaultGUIManager();
    private static final LanguageManager lang = GUIManager.getLanguageManager();

    public GDDAcalculator() {
        masterWindow = GUIManager.getDefaultGUIManager().accessComparisonWindow();
    }

    @Override
    public void run() {
        GraphletsCalculator.GraphletsCalculator();
        masterWindow.infoPaneGDDA.append(lang.getText("GDDA_entry001")); //First net count...
        int[][] firstSingleDGDD = masterWindow.calcDGDD(GUIManager.getDefaultGUIManager().getWorkspace().getProject(), true);
        masterWindow.infoPaneGDDA.append(lang.getText("GDDA_entry002")); //Second net count...
        int[][] secondSingleDGDD = masterWindow.calcDGDD(masterWindow.secondNet, false);

        //long firstSum =  Arrays.stream(firstSingleDGDDA).sum();
        //long secondSum =  Arrays.stream(secondSingleDGDDA).sum();
        /*
        System.out.println("---------------------GDD");
        for(int i = 0 ; i < firstSingleDGDD.length ; i++)
        {
            for(int j = 0 ; j < firstSingleDGDD[i].length ; j++)
            {
                System.out.print(firstSingleDGDD[i][j] + ",");
            }
            System.out.println();
        }
        System.out.println("---------------------");
        for(int i = 0 ; i < secondSingleDGDD.length ; i++)
        {
            for(int j = 0 ; j < secondSingleDGDD[i].length ; j++)
            {
                System.out.print(secondSingleDGDD[i][j] + ",");
            }
            System.out.println();
        }
        */

        long[] distanceDGDD = new long[GraphletsCalculator.globalOrbitMap.size()];
        double result = 0;

        masterWindow.infoPaneGDDA.append(lang.getText("GDDA_entry003")); //Calculate GDDA...
        double DGDDA = masterWindow.calcDGDDA(firstSingleDGDD, secondSingleDGDD, masterWindow.getOrbitsNumber());

        /*
        for(int i = 0 ; i < GraphletsCalculator.globalOrbitMap.size() ; i++)
        {
            distanceDRGF[i] = Math.abs(firstSingleDRGF[i] - secondSingleDRGF[i]);
            //if(firstSingleDRGF[i]!=0 && secondSingleDRGF[i]!=0) {
            result += Math.abs(((double) firstSingleDRGF[i] / (double) firstSum) - ((double) secondSingleDRGF[i] / (double) secondSum));
        }
        */

        masterWindow.infoPaneGDDA.append("\n");
        masterWindow.infoPaneGDDA.append("DGDDA : " + DGDDA + "\n");

        /*
        Object[][] dataDGDD = new Object[GraphletsCalculator.globalOrbitMap.size()+1][4];
        String[] colNames = new String[4];
        int = getK()
        colNames[0] = "Graphlets";
        colNames[1] = "First net";
        colNames[2] = "Second net";
        colNames[3] = "Distance";
        for(int i = 0 ; i < GraphletsCalculator.graphetsList.size(); i++) {
            dataDGDD[i][0] = "G "+i;
            dataDGDD[i][1] = firstSingleDRGF[i];
            dataDGDD[i][2] = secondSingleDRGF[i];
            dataDGDD[i][3] = distanceDRGF[i];
        }
        */

        if (firstSingleDGDD.length != 0) {
            String[] colNames = new String[firstSingleDGDD[0].length + 1];
            for (int i = 1; i < firstSingleDGDD[0].length + 1; i++) {
                colNames[i] = String.valueOf(i);
            }

            DefaultTableModel model = new DefaultTableModel(masterWindow.dataDGDD, colNames);
            masterWindow.dgddTable.setAutoResizeMode(5);
            masterWindow.dgddTable.setModel(model);
            masterWindow.dgddTable.setAutoResizeMode(5);
        }
    }
}
