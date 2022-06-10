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

        switch (masterWindow.decoType.getSelectedIndex()) {
            case 0 -> JOptionPane.showMessageDialog(new JOptionPane(), "Please choose comparison type");
            case 1 -> adtThred();
            case 2 -> functionalThred();
            case 3 -> tnetThred();
            case 4 -> tComponent();
            case 5 -> sComponent();
        }

    }

    private void sComponent() {
    }

    private void tComponent() {
        if (SubnetCalculator.tinvSubNets == null || SubnetCalculator.tinvSubNets.isEmpty()) {
            JOptionPane jpo = new JOptionPane("No t-invariants sets!");
            GUIManager.getDefaultGUIManager().showDecoWindow();
        } else {
            masterWindow.listOfTablesDec.clear();
            InvariantsCalculator ic = new InvariantsCalculator(masterWindow.secondNet);
            ic.generateInvariantsForTest(masterWindow.secondNet);

            masterWindow.infoPaneDec.append("Second net: Invariants generated.\n");

            masterWindow.secondNet.setT_InvMatrix(ic.getInvariants(true), false);
            masterWindow.seconNetList = SubnetCalculator.generateTcomponentFromSecondNet(masterWindow.secondNet);

            masterWindow.infoPaneDec.append("Second net: ADT generated.\n");

            masterWindow.sc = new SubnetComparator(SubnetCalculator.tinvSubNets, masterWindow.seconNetList);
            //rrayList<ArrayList<GreatCommonSubnet>> listofComparedSubnets = sc.compare();

            masterWindow.infoPaneDec.append("Start comparison...\n");
            JComponent result = masterWindow.createResultsPanel();//stofComparedSubnets);//createPartResultTable(listofComparedSubnets);// createResultsPanel(listofComparedSubnets);
            masterWindow.decoResult.add(result, BorderLayout.WEST);
            //this.revalidate();
        }
    }

    private void tnetThred() {
        if (SubnetCalculator.tnetSubNets == null || SubnetCalculator.tnetSubNets.isEmpty()) {
            JOptionPane jpo = new JOptionPane("No t-nets sets!");
            GUIManager.getDefaultGUIManager().showDecoWindow();
        } else {
            masterWindow.listOfTablesDec.clear();
            InvariantsCalculator ic = new InvariantsCalculator(masterWindow.secondNet);
            //ic.generateInvariantsForTest(masterWindow.secondNet);

            //masterWindow.infoPaneDec.append("Second net: Invariants generated.\n");

            //masterWindow.secondNet.setT_InvMatrix(ic.getInvariants(true), false);

            masterWindow.seconNetList = SubnetCalculator.generateTnetFromSecondNet(masterWindow.secondNet);
            masterWindow.infoPaneDec.append("Second net: T-net generated.\n");

            masterWindow.sc = new SubnetComparator(SubnetCalculator.tnetSubNets, masterWindow.seconNetList);
            //rrayList<ArrayList<GreatCommonSubnet>> listofComparedSubnets = sc.compare();

            masterWindow.infoPaneDec.append("Start comparison...\n");
            JComponent result = masterWindow.createResultsPanel();//stofComparedSubnets);//createPartResultTable(listofComparedSubnets);// createResultsPanel(listofComparedSubnets);
            masterWindow.decoResult.add(result, BorderLayout.WEST);
            //this.revalidate();
        }
    }

    private void adtThred() {
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

    private void functionalThred() {
        if (SubnetCalculator.functionalSubNets == null || SubnetCalculator.functionalSubNets.isEmpty()) {
            JOptionPane jpo = new JOptionPane("No ADT sets!");
            GUIManager.getDefaultGUIManager().showDecoWindow();
        } else {
            masterWindow.listOfTablesDec.clear();
            masterWindow.seconNetList = SubnetCalculator.generateFunctionalFromSecondNet(masterWindow.secondNet);// generateADTFromSecondNet(masterWindow.secondNet);
            masterWindow.infoPaneDec.append("Second net: Functional nets generated.\n");
            masterWindow.sc = new SubnetComparator(SubnetCalculator.functionalSubNets, masterWindow.seconNetList);
            masterWindow.infoPaneDec.append("Start comparison...\n");
            JComponent result = masterWindow.createResultsPanel();//stofComparedSubnets);//createPartResultTable(listofComparedSubnets);// createResultsPanel(listofComparedSubnets);
            masterWindow.decoResult.add(result, BorderLayout.WEST);
        }
    }
}
