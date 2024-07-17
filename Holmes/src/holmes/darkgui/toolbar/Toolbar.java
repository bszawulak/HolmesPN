package holmes.darkgui.toolbar;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.Serial;
import java.util.ArrayList;
import java.util.Set;

import javax.swing.*;

import holmes.analyse.GraphletsCalculator;
import holmes.petrinet.elements.Node;

import holmes.analyse.MDTSCalculator;
import holmes.darkgui.GUIManager;
import holmes.graphpanel.GraphPanel.DrawModes;
import holmes.petrinet.elements.Transition;
import holmes.petrinet.elements.Transition.TransitionType;
import holmes.petrinet.simulators.IRandomGenerator;
import holmes.petrinet.simulators.GraphicalSimulator.SimulatorMode;
import holmes.petrinet.simulators.StandardRandom;
import holmes.utilities.Tools;
import holmes.varia.NetworkTransformations;
import holmes.windows.HolmesNotepad;
import holmes.windows.managers.HolmesSPNmanager;

/**
 * Klasa odpowiedzialna za tworzenie paska narzędzi zaraz poniżej paska menu programu.
 *
 * @author students
 * @author MR
 */
@SuppressWarnings("unused")
public class Toolbar extends JPanel {
    @Serial
    private static final long serialVersionUID = 640320332920131092L;
    private GUIManager overlord;
    private boolean buttonsDraggable = false;

    // simulator buttons
    ToolbarButtonAction reverseLoopButton, reverseStepButton, loopSimButton,
            singleTransitionLoopSimButton, pauseSimButton, stopSimButton,
            smallStepFwdSimButton, stepFwdSimButton, resetSimButton;

    /**
     * Konstruktor domyślny obiektu klasy Toolbar.
     */
    public Toolbar() {
        overlord = GUIManager.getDefaultGUIManager();
        this.setLayout(new FlowLayout(FlowLayout.LEFT));
        createIObuttons();
        createNetTransformBar();
        createAnalysisBar();
    }

    /**
     * Metoda odpowiedzialna za tworzenie konkretnych instancji przycisków głównych.
     */
    private void createIObuttons() {
        JButton addButton = new JButton("", Tools.getResIcon48("/icons/toolbar/add_panel.png"));
        addButton.setPreferredSize(new Dimension(50,50));
        addButton.addActionListener(arg0 -> GUIManager.getDefaultGUIManager().reset.newProjectInitiated());
        addButton.setBorderPainted(false);
        addButton.setContentAreaFilled(false);
        addButton.setFocusPainted(false);
        addButton.setOpaque(false);
        this.add(addButton);

        JButton openButton = new JButton("", Tools.getResIcon48("/icons/toolbar/open.png"));
        openButton.setPreferredSize(new Dimension(50,50));
        openButton.addActionListener(arg0 -> overlord.io.selectAndOpenHolmesProject());
        openButton.setBorderPainted(false);
        openButton.setContentAreaFilled(false);
        openButton.setFocusPainted(false);
        openButton.setOpaque(false);
        this.add(openButton);

        //import projektu ze snoopiego
        JButton importButton = new JButton("", Tools.getResIcon48("/icons/toolbar/import_net.png"));
        importButton.setPreferredSize(new Dimension(50,50));
        importButton.addActionListener(arg0 -> overlord.io.importNetwork());
        importButton.setBorderPainted(false);
        importButton.setContentAreaFilled(false);
        importButton.setFocusPainted(false);
        importButton.setOpaque(false);
        this.add(importButton);

        //zapis jako projekt
        JButton saveProjectButton = new JButton("", Tools.getResIcon48("/icons/toolbar/holmesSave.png"));
        saveProjectButton.setPreferredSize(new Dimension(50,50));
        saveProjectButton.addActionListener(arg0 -> overlord.io.saveAsAbyssFile());
        saveProjectButton.setBorderPainted(false);
        saveProjectButton.setContentAreaFilled(false);
        saveProjectButton.setFocusPainted(false);
        saveProjectButton.setOpaque(false);
        this.add(saveProjectButton);

        //export projektu do snoopiego
        JButton exportButton = new JButton("", Tools.getResIcon48("/icons/toolbar/snoopyExport.png"));
        exportButton.setPreferredSize(new Dimension(50,50));
        exportButton.addActionListener(arg0 -> overlord.io.saveAsGlobal());
        exportButton.setBorderPainted(false);
        exportButton.setContentAreaFilled(false);
        exportButton.setFocusPainted(false);
        exportButton.setOpaque(false);
        this.add(exportButton);

        //zapis obrazu sieci do pliku
        JButton pictureButton = new JButton("", Tools.getResIcon48("/icons/toolbar/save_picture.png"));
        pictureButton.setPreferredSize(new Dimension(50,50));
        pictureButton.addActionListener(arg0 -> overlord.io.exportProjectToImage());
        pictureButton.setBorderPainted(false);
        pictureButton.setContentAreaFilled(false);
        pictureButton.setFocusPainted(false);
        pictureButton.setOpaque(false);
        this.add(pictureButton);

        JButton refreshButton = new JButton("", Tools.getResIcon48("/icons/toolbar/refresh.png"));
        refreshButton.setPreferredSize(new Dimension(50,50));
        refreshButton.addActionListener(arg0 -> overlord.getWorkspace().getProject().repaintAllGraphPanels());
        refreshButton.setBorderPainted(false);
        refreshButton.setContentAreaFilled(false);
        refreshButton.setFocusPainted(false);
        refreshButton.setOpaque(false);
        this.add(refreshButton);

        JButton clearProject = new JButton("", Tools.getResIcon48("/icons/toolbar/clear_project.png"));
        clearProject.setPreferredSize(new Dimension(50,50));
        clearProject.addActionListener(arg0 -> overlord.reset.newProjectInitiated());
        clearProject.setBorderPainted(false);
        clearProject.setContentAreaFilled(false);
        clearProject.setFocusPainted(false);
        clearProject.setOpaque(false);
        this.add(clearProject);
    }

    /**
     * Metoda odpowiedzialna za tworzenie tablicy przycisków analizatora.
     * @return ArrayList[ButtonDockable] - tablica zawierająca obiekty przycisków
     */
    private void createAnalysisBar() {
        JButton clusterButton = new JButton("", Tools.getResIcon48("/icons/toolbar/clusters.png"));
        clusterButton.setPreferredSize(new Dimension(50,50));
        clusterButton.addActionListener(arg0 -> overlord.showClusterWindow());
        clusterButton.setBorderPainted(false);
        clusterButton.setContentAreaFilled(false);
        clusterButton.setFocusPainted(false);
        clusterButton.setOpaque(false);
        this.add(clusterButton);

        JButton netTablesButton = new JButton("", Tools.getResIcon48("/icons/toolbar/netTables.png"));
        netTablesButton.setPreferredSize(new Dimension(50,50));
        netTablesButton.addActionListener(arg0 -> overlord.showNetTablesWindow());
        netTablesButton.setBorderPainted(false);
        netTablesButton.setContentAreaFilled(false);
        netTablesButton.setFocusPainted(false);
        netTablesButton.setOpaque(false);
        this.add(netTablesButton);

        JButton netSimLogButton = new JButton("", Tools.getResIcon48("/icons/toolbar/simLog.png"));
        netSimLogButton.setPreferredSize(new Dimension(50,50));
        netSimLogButton.addActionListener(arg0 -> overlord.showSimLogWindow());
        netSimLogButton.setBorderPainted(false);
        netSimLogButton.setContentAreaFilled(false);
        netSimLogButton.setFocusPainted(false);
        netSimLogButton.setOpaque(false);
        this.add(netSimLogButton);

        JButton consoleButton = new JButton("", Tools.getResIcon48("/icons/toolbar/terminal2.png"));
        consoleButton.setPreferredSize(new Dimension(50,50));
        consoleButton.addActionListener(arg0 -> overlord.showConsole(true));
        consoleButton.setBorderPainted(false);
        consoleButton.setContentAreaFilled(false);
        consoleButton.setFocusPainted(false);
        consoleButton.setOpaque(false);
        this.add(consoleButton);

        JButton cleanButton = new JButton("", Tools.getResIcon48("/icons/toolbar/cleanGraphColors.png"));
        cleanButton.setPreferredSize(new Dimension(50,50));
        cleanButton.addActionListener(arg0 -> overlord.reset.clearGraphColors());
        cleanButton.setBorderPainted(false);
        cleanButton.setContentAreaFilled(false);
        cleanButton.setFocusPainted(false);
        cleanButton.setOpaque(false);
        this.add(cleanButton);

        JButton fireRatesButton = new JButton("", Tools.getResIcon48("/icons/toolbar/firingRates.png"));
        fireRatesButton.setPreferredSize(new Dimension(50,50));
        fireRatesButton.addActionListener(arg0 -> new HolmesSPNmanager(GUIManager.getDefaultGUIManager().getFrame()));
        fireRatesButton.setBorderPainted(false);
        fireRatesButton.setContentAreaFilled(false);
        fireRatesButton.setFocusPainted(false);
        fireRatesButton.setOpaque(false);
        this.add(fireRatesButton);

        //TODO:
        ToolbarButtonAction testButton = new ToolbarButtonAction(this, "Debug1", "Debug", Tools.getResIcon48("/icons/toolbar/aaa.png")) {
            public void actionPerformed(ActionEvent actionEvent) {
                IRandomGenerator generator2 = new StandardRandom();

                for (Transition t : GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions()) {
                    t.setTransType(TransitionType.TPN);
                    int value = (int) generator2.nextLong(10);
                    int eft;
                    int lft;
                    int duration;

                    if (value > 7) {
                        eft = (int) generator2.nextLong(6);
                        lft = (int) generator2.nextLong(eft + 6) + 1;
                        duration = (int) generator2.nextLong(10);

                        t.timeExtension.setDPNstatus(true);
                        t.timeExtension.setTPNstatus(true);
                        t.timeExtension.setLFT(lft);
                        t.timeExtension.setEFT(eft);
                        t.timeExtension.setDPNduration(duration);

                    } else if (value > 3) {
                        duration = (int) generator2.nextLong(10);

                        t.timeExtension.setDPNstatus(true);
                        t.timeExtension.setDPNduration(duration);
                    } else {
                        eft = (int) generator2.nextLong(6);
                        lft = (int) generator2.nextLong(eft + 6) + 1;

                        t.timeExtension.setTPNstatus(true);
                        t.timeExtension.setLFT(lft);
                        t.timeExtension.setEFT(eft);
                    }
                }
                GUIManager.getDefaultGUIManager().getWorkspace().getProject().repaintAllGraphPanels();

				/*
				HolmesNotepad aa = new HolmesNotepad(600, 480);
				aa.setVisible(true);
				
				JEP myParser = new JEP();
				myParser.addStandardFunctions();
				myParser.addVariable("p0", 2.0);
				myParser.addVariable("p2", 2.0);
				String expressionString = "(p1+p0)<1";
				myParser.parseExpression(expressionString);
				double result = myParser.getValue();
				aa.addTextLineNL(expressionString+" = "+result, "text");
				*/
            }
        };
        //testButton.setEnabled(false);
        //analysisDockables.add(createButtonDockable("Testing", testButton));

        //TODO:

        ToolbarButtonAction testButton2 = new ToolbarButtonAction(this, "DEBUG2", "Debug2", Tools.getResIcon48("/icons/toolbar/a.png")) {
            //@SuppressWarnings("unused")
            public void actionPerformed(ActionEvent actionEvent) {
                JTree test = overlord.getToolBox().getTree();
                overlord.getToolBox().selectPointer();
                //test.setSelectionPath(new TreePath());
                int x = 1;

                MDTSCalculator mdts = new MDTSCalculator();
                ArrayList<Set<Integer>> results = mdts.calculateMDTS();

                HolmesNotepad notePad = new HolmesNotepad(900, 600);
                notePad.setVisible(true);

                notePad.addTextLineNL("", "text");
                notePad.addTextLineNL("Maximal Dependend Transition sets:", "text");
                StringBuilder text;
                int setNo = 0;
                for (Set<Integer> set : results) {
                    text = new StringBuilder();
                    setNo++;
                    text.append("Set #").append(setNo).append(": [");
                    for (int i : set) {
                        text.append("t").append(i).append(", ");
                    }
                    text.append("]");
                    text = new StringBuilder(text.toString().replace(", ]", "]"));
                    notePad.addTextLineNL(text.toString(), "text");
                }
            }
        };
        //Test Graphley Button

        ToolbarButtonAction testGraphletButton = new ToolbarButtonAction(this, "Debug1", "Debug", Tools.getResIcon48("/icons/toolbar/aaa.png")) {
            public void actionPerformed(ActionEvent actionEvent) {
                GraphletsCalculator.generateGraphlets();
                ArrayList<int[]> GDV = new ArrayList<>();
                boolean test = false;

                if (test) {
                    System.out.println(GUIManager.getDefaultGUIManager().getWorkspace().getProject().getNodes().get(1));
                    int[] vectorOrbit = GraphletsCalculator.vectorOrbit(GUIManager.getDefaultGUIManager().getWorkspace().getProject().getNodes().get(1), test);
                } else {

                    for (Node startNode : GUIManager.getDefaultGUIManager().getWorkspace().getProject().getNodes()) {
                        int[] vectorOrbit = GraphletsCalculator.vectorOrbit(startNode, test);
                        GDV.add(vectorOrbit);
                    }


                    for (int j = 0; j < GDV.size(); j++) {
                        System.out.print(GUIManager.getDefaultGUIManager().getWorkspace().getProject().getNodes().get(j).getName() + " - ");
                        int[] vector = GDV.get(j);
                        for (int i = 0; i < vector.length; i++) {
                            if (vector[i] < 0) {
                                System.out.print("X, \t");
                            } else {
                                System.out.print(vector[i] + "\t ");
                            }
                        }
                        System.out.println();
                    }
                }
            }
        };
    }

    //TODO przyciski
    private void createNetTransformBar() {
        JButton extendNetButton = new JButton("", Tools.getResIcon48("/icons/toolbar/resizeMax.png"));
        extendNetButton.setPreferredSize(new Dimension(50,50));
        extendNetButton.addActionListener(actionEvent -> {
                NetworkTransformations nt = new NetworkTransformations();
                nt.extendNetwork(true);
        });
        extendNetButton.setBorderPainted(false);
        extendNetButton.setContentAreaFilled(false);
        extendNetButton.setFocusPainted(false);
        extendNetButton.setOpaque(false);
        this.add(extendNetButton);

        JButton shrinkNetButton = new JButton("", Tools.getResIcon48("/icons/toolbar/resizeMin.png"));
        shrinkNetButton.setPreferredSize(new Dimension(50,50));
        shrinkNetButton.addActionListener(actionEvent -> {
            NetworkTransformations nt = new NetworkTransformations();
            nt.extendNetwork(false);
        });
        shrinkNetButton.setBorderPainted(false);
        shrinkNetButton.setContentAreaFilled(false);
        shrinkNetButton.setFocusPainted(false);
        shrinkNetButton.setOpaque(false);
        this.add(shrinkNetButton);

        JButton gridButton = new JButton("", Tools.getResIcon48("/icons/toolbar/grid.png"));
        gridButton.setPreferredSize(new Dimension(50,50));
        gridButton.addActionListener(actionEvent -> {
            if (overlord.getSettingsManager().getValue("editorGridLines").equals("1"))
                overlord.getSettingsManager().setValue("editorGridLines", "0", true);
            else
                overlord.getSettingsManager().setValue("editorGridLines", "1", true);

            overlord.getWorkspace().getProject().repaintAllGraphPanels();
        });
        gridButton.setBorderPainted(false);
        gridButton.setContentAreaFilled(false);
        gridButton.setFocusPainted(false);
        gridButton.setOpaque(false);
        this.add(gridButton);

        JButton gridAlignButton = new JButton("", Tools.getResIcon48("/icons/toolbar/gridAlign.png"));
        gridAlignButton.setPreferredSize(new Dimension(50,50));
        gridAlignButton.addActionListener(actionEvent -> {
            NetworkTransformations nt = new NetworkTransformations();
            nt.alignNetToGrid();
            overlord.getWorkspace().getProject().repaintAllGraphPanels();
        });
        gridAlignButton.setBorderPainted(false);
        gridAlignButton.setContentAreaFilled(false);
        gridAlignButton.setFocusPainted(false);
        gridAlignButton.setOpaque(false);
        this.add(gridAlignButton);
    }

    //***************************************************************************************************
    //***************************************************************************************************

    /**
     * Metoda ta ustawia stan wszystkich przycisków symulatora poza dwoma: pauzą
     * i przyciskiem zatrzymania symulacji.
     *
     * @param enabled boolean - true, jeśli mają być aktywne
     */
    public void setEnabledSimulationInitiateButtons(boolean enabled) {
        //for (int i = 0; i < simulationDockables.size(); i++) {
        //	if (i != 4 && i != 5) //4 i 5 to pauza i stop
        //simulationDockables.get(i).getContent().setEnabled(enabled);
        //}
    }

    /**
     * Metoda ta uaktywnia przyciski Pauza i Stop dla symulacji.
     * @param enabled boolean - true jeśli Pauza i Stop mają być aktywne
     */
    public void setEnabledSimulationDisruptButtons(boolean enabled) {
        //simulationDockables.get(4).getContent().setEnabled(enabled);
        //simulationDockables.get(5).getContent().setEnabled(enabled);
    }

    /**
     * Metoda odpowiedzialna za to, że aktywne są wszystkie przyciski
     * poza dwoma: Pauza i Stop dla symulatora
     */
    public void allowOnlySimulationInitiateButtons() {
        setEnabledSimulationInitiateButtons(true);
        setEnabledSimulationDisruptButtons(false);
    }

    /**
     * Metoda ta uaktywnia tylko przyciski Pauzy i Stopu, reszta nieaktywna - gdy działa symulacja.
     */
    public void allowOnlySimulationDisruptButtons() {
        setEnabledSimulationInitiateButtons(false);
        setEnabledSimulationDisruptButtons(true);
    }

    /**
     * Metoda ustawia na aktywny tylko przycisk przerwania trwającej pauzy.
     */
    public void allowOnlyUnpauseButton() {
        allowOnlySimulationDisruptButtons();
        //simulationDockables.get(5).getContent().setEnabled(false);
    }

    /**
     * Metoda odpowiedzialna za tworzenie tablicy przycisków symulatora.
     *///TODO przyciski
    private void createSimulationBar() {
        reverseLoopButton = new ToolbarButtonAction(this, "LoopBack", "Loop back to oldest action saved",
                Tools.getResIcon48("/icons/toolbar/sim_back.png")) {
            public void actionPerformed(ActionEvent actionEvent) {
                overlord.getWorkspace().setGraphMode(DrawModes.POINTER);
                overlord.getWorkspace().getProject().getSimulator().startSimulation(SimulatorMode.LOOP_BACK);
            }
        };

        reverseStepButton = new ToolbarButtonAction(this, "StepBack", "Single action back simulation",
                Tools.getResIcon48("/icons/toolbar/sim_back_step.png")) {
            public void actionPerformed(ActionEvent actionEvent) {
                overlord.getWorkspace().setGraphMode(DrawModes.POINTER);
                overlord.getWorkspace().getProject().getSimulator().startSimulation(SimulatorMode.ACTION_BACK);
            }
        };

        loopSimButton = new ToolbarButtonAction(this, "Loop", "Loop simulation",
                Tools.getResIcon48("/icons/toolbar/sim_start.png")) {
            public void actionPerformed(ActionEvent actionEvent) {
                overlord.getWorkspace().setGraphMode(DrawModes.POINTER);
                overlord.getWorkspace().getProject().getSimulator().startSimulation(SimulatorMode.LOOP);
            }
        };

        singleTransitionLoopSimButton = new ToolbarButtonAction(this, "LoopSingleTrans", "Loop single transition simulation",
                Tools.getResIcon48("/icons/toolbar/sim_start_single.png")) {
            public void actionPerformed(ActionEvent actionEvent) {
                overlord.getWorkspace().setGraphMode(DrawModes.POINTER);
                overlord.getWorkspace().getProject().getSimulator().startSimulation(SimulatorMode.SINGLE_TRANSITION_LOOP);
            }
        };

        pauseSimButton = new ToolbarButtonAction(this, "Pause", "Pause simulation",
                Tools.getResIcon48("/icons/toolbar/sim_pause.png")) {
            public void actionPerformed(ActionEvent actionEvent) {
                overlord.getWorkspace().setGraphMode(DrawModes.POINTER);
                overlord.getWorkspace().getProject().getSimulator().pause();
            }
        };

        stopSimButton = new ToolbarButtonAction(this, "Stop", "Schedule a stop for the simulation",
                Tools.getResIcon48("/icons/toolbar/sim_stop.png")) {
            public void actionPerformed(ActionEvent actionEvent) {
                overlord.getWorkspace().getProject().getSimulator().stop();
            }
        };

        smallStepFwdSimButton = new ToolbarButtonAction(this, "SingleForward", "Single transition forward simulation",
                Tools.getResIcon48("/icons/toolbar/sim_forward_step.png")) {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                overlord.getWorkspace().setGraphMode(DrawModes.POINTER);
                overlord.getWorkspace().getProject().getSimulator().startSimulation(SimulatorMode.SINGLE_TRANSITION);
            }
        };

        stepFwdSimButton = new ToolbarButtonAction(this, "StepForward", "Step forward simulation",
                Tools.getResIcon48("/icons/toolbar/sim_forward.png")) {
            public void actionPerformed(ActionEvent actionEvent) {
                overlord.getWorkspace().setGraphMode(DrawModes.POINTER);
                overlord.getWorkspace().getProject().getSimulator().startSimulation(SimulatorMode.STEP);
            }
        };

        resetSimButton = new ToolbarButtonAction(this, "Reset", "Reset simulator",
                Tools.getResIcon48("/icons/toolbar/sim_reset.png")) {
            public void actionPerformed(ActionEvent actionEvent) {
                overlord.getWorkspace().getProject().restoreMarkingZero();
            }
        };
    }

    private JPanel createSubtoolsPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEADING));
        panel.setPreferredSize(new Dimension(200, 60));
        panel.setMaximumSize(new Dimension(200, 60));

        JButton button1 = new JButton("Ext.Net");
        button1.setName("extNet");
        button1.setPreferredSize(new Dimension(60, 40));
        //button1.setBounds(0, 0, 60, 60);
        button1.setToolTipText("Extend net elements");
        button1.addActionListener(actionEvent -> {

        });
        panel.add(button1);

        return panel;
    }
}
