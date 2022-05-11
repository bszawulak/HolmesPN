package holmes.analyse;

import holmes.darkgui.GUIManager;
import holmes.petrinet.data.PetriNet;
import holmes.windows.HolmesComparisonModule;
import holmes.windows.HolmesInvariantsGenerator;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.data.xy.XYSeries;

import javax.swing.table.DefaultTableModel;
import java.util.Arrays;

public class GRDFcalculator implements Runnable {

    private HolmesComparisonModule masterWindow = null;


    public GRDFcalculator() {
        masterWindow = GUIManager.getDefaultGUIManager().accessComparisonWindow();
    }
    @Override
    public void run() {
        int chiisenGraohletSize = masterWindow.getChoosenGraohletSize(masterWindow.graphletSize.getSelectedIndex());

        masterWindow.infoPaneDRGF.append("First net count... \n");
        long[] firstSingleDRGF = calcDRGF(GUIManager.getDefaultGUIManager().getWorkspace().getProject());
        masterWindow.infoPaneDRGF.append("\nSecond net count... \n");
        long[] secondSingleDRGF = calcDRGF(masterWindow.secondNet);

        long firstSum = Arrays.stream(firstSingleDRGF).sum();
        long secondSum = Arrays.stream(secondSingleDRGF).sum();

        long[] distanceDRGF = new long[chiisenGraohletSize];
        double result = 0;

        for (int i = 0; i < chiisenGraohletSize; i++) {
            distanceDRGF[i] = Math.abs(firstSingleDRGF[i] - secondSingleDRGF[i]);
            result += Math.abs(((double) firstSingleDRGF[i] / (double) firstSum) - ((double) secondSingleDRGF[i] / (double) secondSum));
        }
        masterWindow.infoPaneDRGF.append("\nDRGF : " + result);

        XYSeries series1 = new XYSeries("Number of graphlets of net 1");
        XYSeries series2 = new XYSeries("Number of graphlets of net 2");
        masterWindow.dataDRGF = new Object[chiisenGraohletSize + 1][4];
        String[] colNames = new String[4];
        colNames[0] = "Graphlets";
        colNames[1] = "First net";
        colNames[2] = "Second net";
        colNames[3] = "Distance";
        for (int i = 0; i < chiisenGraohletSize; i++) {
            masterWindow.dataDRGF[i][0] = "G " + i;
            masterWindow.dataDRGF[i][1] = firstSingleDRGF[i];
            masterWindow.dataDRGF[i][2] = secondSingleDRGF[i];
            masterWindow.dataDRGF[i][3] = distanceDRGF[i];
            series1.add(i, firstSingleDRGF[i]);
            series2.add(i, secondSingleDRGF[i]);
        }

        DefaultTableModel model = new DefaultTableModel(masterWindow.dataDRGF, colNames);

        masterWindow.drgfTable.setModel(model);
        masterWindow.grdfSeriesDataSet.removeAllSeries();
        masterWindow.grdfSeriesDataSet.addSeries(series1);
        masterWindow.grdfSeriesDataSet.addSeries(series2);
        masterWindow.grdfChartPanel.setVisible(true);

        CategoryPlot chartPlot = masterWindow.grdfChart.getCategoryPlot();
        ValueAxis yAxis = chartPlot.getRangeAxis();
        yAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

        CategoryAxis xAxis = chartPlot.getDomainAxis();
    }

    private long[] calcDRGF(PetriNet project) {
        GraphletsCalculator.cleanAll();
        GraphletsCalculator.generateGraphlets();
        GraphletsCalculator.getFoundServerGraphlets(project,masterWindow.infoPaneDRGF);
        long[] singleDRGF = new long[GraphletsCalculator.graphetsList.size()];

        for (int k = 0; k < GraphletsCalculator.graphetsList.size(); k++) {
            int finalI = k;
            long val = GraphletsCalculator.uniqGraphlets.stream().filter(x -> x.getGraphletID() == finalI).count();
            singleDRGF[k] = val;
        }
        return singleDRGF;
    }
}
