package holmes.analyse;

import holmes.analyse.comparison.SubnetComparator;
import holmes.darkgui.GUIManager;
import holmes.windows.HolmesComparisonModule;

import javax.swing.*;
import java.awt.*;

public class DecoCalccalculator implements Runnable {

    private HolmesComparisonModule masterWindow = null;

    public DecoCalccalculator() {
        masterWindow = GUIManager.getDefaultGUIManager().accessComparisonWindow();
    }
    @Override
    public void run() {
        if (SubnetCalculator.adtSubNets == null || SubnetCalculator.adtSubNets.isEmpty()) {
            JOptionPane jpo = new JOptionPane("No ADT sets!");
            GUIManager.getDefaultGUIManager().showDecoWindow();
        } else {
            masterWindow.listOfTablesDec.clear();
            InvariantsCalculator ic = new InvariantsCalculator(masterWindow.secondNet);
            ic.generateInvariantsForTest(masterWindow.secondNet);

            masterWindow.infoPaneDec.append("Second net: Invariants generated.\n");

            masterWindow.secondNet.setT_InvMatrix(ic.getInvariants(true), false);
            masterWindow.seconNetList = SubnetCalculator.generateADTFromSecondNet(masterWindow.secondNet);

            masterWindow.infoPaneDec.append("Second net: ADT generated.\n");

            masterWindow.sc = new SubnetComparator(SubnetCalculator.adtSubNets, masterWindow.seconNetList);
            //rrayList<ArrayList<GreatCommonSubnet>> listofComparedSubnets = sc.compare();

            masterWindow.infoPaneDec.append("Start comparison...\n");
            JComponent result = masterWindow.createResultsPanel();//stofComparedSubnets);//createPartResultTable(listofComparedSubnets);// createResultsPanel(listofComparedSubnets);
            masterWindow.decoResult.add(result, BorderLayout.WEST);
            //this.revalidate();
        }

    }
}
