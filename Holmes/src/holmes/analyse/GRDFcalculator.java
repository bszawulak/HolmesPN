package holmes.analyse;

import holmes.darkgui.GUIManager;
import holmes.darkgui.LanguageManager;
import holmes.petrinet.data.PetriNet;
import holmes.windows.decompositions.HolmesComparisonModule;
import org.jfree.chart.axis.*;
import org.jfree.chart.plot.DefaultDrawingSupplier;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.data.xy.XYSeries;

import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.Arrays;

public class GRDFcalculator implements Runnable {
    private static LanguageManager lang = GUIManager.getLanguageManager();
    private HolmesComparisonModule masterWindow = null;
    public GRDFcalculator() {
        masterWindow = GUIManager.getDefaultGUIManager().accessComparisonWindow();
    }

    @Override
    public void run() {
        int chiisenGraohletSize = masterWindow.getChoosenGraohletSize(masterWindow.graphletSize.getSelectedIndex());

        masterWindow.infoPaneDRGF.append(lang.getText("GRDF_entry001")); //Comparison process started.
        masterWindow.infoPaneDRGF.append(lang.getText("GRDF_entry002")); //First net count...
        long[] firstSingleDRGF = calcDRGF(GUIManager.getDefaultGUIManager().getWorkspace().getProject());
        masterWindow.infoPaneDRGF.append(lang.getText("GRDF_entry003")); //Second net count...
        long[] secondSingleDRGF = calcDRGF(masterWindow.secondNet);

        long firstSum = Arrays.stream(firstSingleDRGF).sum();
        long secondSum = Arrays.stream(secondSingleDRGF).sum();

        long[] distanceDRGF = new long[chiisenGraohletSize];
        double result = 0;

        for (int i = 0; i < chiisenGraohletSize; i++) {
            distanceDRGF[i] = Math.abs(firstSingleDRGF[i] - secondSingleDRGF[i]);

            double f1 = -Math.log((double) firstSingleDRGF[i] / (double) firstSum);
            double f2 = -Math.log((double) secondSingleDRGF[i] / (double) secondSum);
            if(firstSingleDRGF[i]==0)
                f1=0;
            if(secondSingleDRGF[i]==0)
                f2=0;
            result += Math.abs(f1 - f2);
        }

        int nodeN = masterWindow.graphletSize.getSelectedIndex() + 1;
        masterWindow.infoPaneDRGF.append("\nGRDF (" + nodeN + "-node) : " + result);

        XYSeries series1 = new XYSeries("Number of graphlets of net 1"); //Number of graphlets of net 1
        XYSeries series2 = new XYSeries("Number of graphlets of net 2"); //Number of graphlets of net 2
        masterWindow.dataDRGF = new Object[chiisenGraohletSize ][4];
        String[] colNames = new String[4];
        colNames[0] = "Graphlets"; //Graphlets
        colNames[1] = "First net"; //First net
        colNames[2] = "Second net";//Second net
        colNames[3] = "Distance"; //Distance
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

        XYPlot xyplot = (XYPlot) masterWindow.grdfChart.getPlot();
        xyplot.setForegroundAlpha(0.85F);
        XYBarRenderer xybarrenderer = (XYBarRenderer) xyplot.getRenderer();
        xybarrenderer.setBarPainter(new StandardXYBarPainter());

        Paint[] paintArray = {              //code related to translucent colors begin here
                new Color(0x80ff0000, true),
                new Color(0x800000ff, true)
        };
        String[] axisX = new String[151];
        for (int i=0 ; i< 151 ; i++)
            axisX[i]=String.valueOf(i+1);
        SymbolAxis rangeAxis = new SymbolAxis("Graphlets", axisX); //Graphlets
        rangeAxis.setVerticalTickLabels(true);
        xyplot.setDomainAxis(rangeAxis);

        NumberAxis yAxis = (NumberAxis) xyplot.getRangeAxis();
        DecimalFormat format = new DecimalFormat("0");
        yAxis.setNumberFormatOverride(format);

        double max = series1.getMaxY();
        if(max<series2.getMaxY())
            max=series2.getMaxY();

        int tick = (int)max/10;

        yAxis.setTickUnit(new NumberTickUnit(tick));

        xyplot.setDrawingSupplier(new DefaultDrawingSupplier(
                paintArray,
                DefaultDrawingSupplier.DEFAULT_FILL_PAINT_SEQUENCE,
                DefaultDrawingSupplier.DEFAULT_OUTLINE_PAINT_SEQUENCE,
                DefaultDrawingSupplier.DEFAULT_STROKE_SEQUENCE,
                DefaultDrawingSupplier.DEFAULT_OUTLINE_STROKE_SEQUENCE,
                DefaultDrawingSupplier.DEFAULT_SHAPE_SEQUENCE));

        masterWindow.grdfChartPanel.setVisible(true);
        masterWindow.drgfTable.revalidate();
        masterWindow.infoPaneDRGF.append(lang.getText("GRDF_entry004")); //Comparison process ended.
    }

    private long[] calcDRGF(PetriNet project) {
        GraphletsCalculator.cleanAll();
        GraphletsCalculator.generateGraphlets();
        GraphletsCalculator.getFoundServerGraphlets(project, masterWindow.infoPaneDRGF);
        long[] singleDRGF = new long[GraphletsCalculator.graphetsList.size()];

        for (int k = 0; k < GraphletsCalculator.graphetsList.size(); k++) {
            int finalI = k;
            long val = GraphletsCalculator.uniqGraphlets.stream().filter(x -> x.getGraphletID() == finalI).count();
            singleDRGF[k] = val;
        }
        return singleDRGF;
    }
}
