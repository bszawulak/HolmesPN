package holmes.darkgui.dockwindows;

import java.awt.*;
import java.io.Serial;
import java.util.ArrayList;

import javax.swing.*;

import holmes.clusters.ClusterDataPackage;
import holmes.darkgui.GUIManager;
import holmes.darkgui.dockwindows.HolmesDockWindowsTable.SubWindow;
import holmes.graphpanel.SelectionActionListener.SelectionActionEvent;
import holmes.graphpanel.SelectionActionListener.SelectionActionEvent.SelectionActionType;
import holmes.petrinet.data.MCSDataMatrix;
import holmes.petrinet.elements.*;
import holmes.petrinet.elements.PetriNetElement.PetriNetElementType;
import holmes.petrinet.elements.Transition.TransitionType;
import holmes.petrinet.simulators.GraphicalSimulator;

import holmes.petrinet.simulators.xtpn.GraphicalSimulatorXTPN;

/**
 * Metoda odpowiedzialna za okno programu, w którym gromadzone są kolejne zakładki
 * jak np. symulator, analizator, edytor, itd. Wychwytuje ona między innymi
 * zdarzenia kliknięcia na jakiś element np. sieci, następnie zlecając utworzenie
 * okna wyświetlającego odpowiednie właściwości, przyciski, opcje, itd.
 *
 * @author students
 */
public class HolmesDockWindow {//extends SingleDock {
    @Serial
    private static final long serialVersionUID = -1966643269924197502L;
    //private Dockable dockable;
    private GUIManager guiManager;
    private HolmesDockWindowsTable dockWindowPanel;
    private SelectionPanel selectionPanel;
    private JScrollPane scrollPane;
    private DockWindowType type;

    /**
     * EDITOR, SIMULATOR, SELECTOR, InvANALYZER, ClusterSELECTOR, MctANALYZER, InvSIMULATOR, MCSselector, Knockout, FIXNET
     */
    public enum DockWindowType {
        EDITOR, SIMULATOR, SELECTOR, T_INVARIANTS, P_INVARIANTS, ClusterSELECTOR, MctANALYZER, MCSselector, Knockout, FIXNET, QuickSim, DECOMPOSITION
    }

    /**
     * Konstruktor obiektu klasy HolmesDockWindow. Tworzy czyste podokienko dokowane
     * do interfejsu programu (wywołanie pochodzi z konstruktora GUIManager).
     * Wypełnianie okna elementami jest już wykonywane zdalnie, na rządanie odpowiednią
     * metodą.
     *
     * @param propertiesType DockWindowType - typ właściwości do dodania
     */
    public HolmesDockWindow(DockWindowType propertiesType) {
        type = propertiesType;
        scrollPane = new JScrollPane();
        guiManager = GUIManager.getDefaultGUIManager();

        /*
        switch (type) {
            case EDITOR -> setDockable(GUIManager.externalWithListener(new DefaultDockable("NetElement", scrollPane,
                    "Net Element"), GUIManager.getDefaultGUIManager().getDockingListener()));
            case SIMULATOR -> setDockable(GUIManager.externalWithListener(new DefaultDockable("Simulator", scrollPane,
                    "Simulator"), GUIManager.getDefaultGUIManager().getDockingListener()));
            case SELECTOR -> setDockable(GUIManager.externalWithListener(new DefaultDockable("Selection", scrollPane,
                    "Selection"), GUIManager.getDefaultGUIManager().getDockingListener()));
            case T_INVARIANTS ->
                    setDockable(GUIManager.externalWithListener(new DefaultDockable("Transition_Invariants", scrollPane,
                            "T-inv"), GUIManager.getDefaultGUIManager().getDockingListener()));
            case P_INVARIANTS ->
                    setDockable(GUIManager.externalWithListener(new DefaultDockable("Place_Invariants", scrollPane,
                            "P-inv"), GUIManager.getDefaultGUIManager().getDockingListener()));
            case MctANALYZER ->
                    setDockable(GUIManager.externalWithListener(new DefaultDockable("MCT_Groups", scrollPane,
                            "MCT"), GUIManager.getDefaultGUIManager().getDockingListener()));
            case ClusterSELECTOR ->
                    setDockable(GUIManager.externalWithListener(new DefaultDockable("Clusters_Selection", scrollPane,
                            "Clusters"), GUIManager.getDefaultGUIManager().getDockingListener()));
            case MCSselector ->
                    setDockable(GUIManager.externalWithListener(new DefaultDockable("MCS_selector", scrollPane,
                            "MCS"), GUIManager.getDefaultGUIManager().getDockingListener()));
            case FIXNET -> setDockable(GUIManager.externalWithListener(new DefaultDockable("Fix_selector", scrollPane,
                    "Fix"), GUIManager.getDefaultGUIManager().getDockingListener()));
            case QuickSim ->
                    setDockable(GUIManager.externalWithListener(new DefaultDockable("Quick_simulator", scrollPane,
                            "qSim"), GUIManager.getDefaultGUIManager().getDockingListener()));
            case Knockout ->
                    setDockable(GUIManager.externalWithListener(new DefaultDockable("Knockout_selector", scrollPane,
                            "Knockout"), GUIManager.getDefaultGUIManager().getDockingListener()));
            case DECOMPOSITION ->
                    setDockable(GUIManager.externalWithListener(new DefaultDockable("Decomposition", scrollPane,
                            "DECOMPOSITION"), GUIManager.getDefaultGUIManager().getDockingListener()));
        }
        */

        Point position = new Point(0, 0);
        //this.addDockable(getDockable(), position, position);

        //immediate creation:
        switch (type) {
            case SIMULATOR -> {
                GraphicalSimulator netSim = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getSimulator();
                GraphicalSimulatorXTPN netSimXTPN = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getSimulatorXTPN();
                setCurrentDockWindow(new HolmesDockWindowsTable(SubWindow.SIMULATOR, netSim, netSimXTPN));
                scrollPane.getViewport().add(getCurrentDockWindow());
            }
            case EDITOR -> {
                setCurrentDockWindow(new HolmesDockWindowsTable(SubWindow.EMPTY));
                scrollPane.getViewport().add(getCurrentDockWindow());
            }
            case SELECTOR -> {
                setSelectionPanel(new SelectionPanel());
                scrollPane.getViewport().add(getSelectionPanel());
            }
            case FIXNET -> {
                setCurrentDockWindow(new HolmesDockWindowsTable(SubWindow.FIXER));
                scrollPane.getViewport().add(getCurrentDockWindow());
            }
            case QuickSim -> {
                setCurrentDockWindow(new HolmesDockWindowsTable(SubWindow.QUICKSIM));
                scrollPane.getViewport().add(getCurrentDockWindow());
            }
            case DECOMPOSITION -> {
                setCurrentDockWindow(new HolmesDockWindowsTable(SubWindow.DECOMPOSITION));
                scrollPane.getViewport().add(getCurrentDockWindow());
            }
            case T_INVARIANTS -> {
                setCurrentDockWindow(new HolmesDockWindowsTable(SubWindow.T_INVARIANTS, GUIManager.getDefaultGUIManager().getWorkspace().getProject().getT_InvMatrix()));
                scrollPane.getViewport().add(getCurrentDockWindow());
            }
            case P_INVARIANTS -> {
                setCurrentDockWindow(new HolmesDockWindowsTable(SubWindow.P_INVARIANTS, GUIManager.getDefaultGUIManager().getWorkspace().getProject().getP_InvMatrix()));
                scrollPane.getViewport().add(getCurrentDockWindow());
            }
            case MctANALYZER -> {
                setCurrentDockWindow(new HolmesDockWindowsTable(SubWindow.MCT, GUIManager.getDefaultGUIManager().getWorkspace().getProject().getMCTMatrix()));
                scrollPane.getViewport().add(getCurrentDockWindow());
            }
            case MCSselector -> {
                setCurrentDockWindow(new HolmesDockWindowsTable(SubWindow.MCS));
                scrollPane.getViewport().add(getCurrentDockWindow());
            }
            case ClusterSELECTOR -> {
                setCurrentDockWindow(new HolmesDockWindowsTable(SubWindow.CLUSTERS, new ClusterDataPackage()));
                scrollPane.getViewport().add(getCurrentDockWindow());
            }
            case Knockout -> {
                setCurrentDockWindow(new HolmesDockWindowsTable(SubWindow.KNOCKOUT, new ArrayList<ArrayList<Integer>>()));
                scrollPane.getViewport().add(getCurrentDockWindow());
            }
        }
    }

    /**
     * Metoda zwracająca podokno dokowalne intefejsu programu.
     *
     * @return Dockable - obiekt dokowalny
     */
    //public Dockable getDockable() {
    //    return dockable;
    //}

    /**
     * Metoda ustawiająca podokno dokowalne intefejsu programu.
     */
    //private void setDockable(Dockable dockable) {
    //    this.dockable = dockable;
    //}

    /**
     * Metoda odpowiedzialna za wypełnienie sekcji symulatora sieci.
     */
    public void createSimulatorProperties(boolean XTPN) {
        if (type == DockWindowType.SIMULATOR) {
            GraphicalSimulator netSim = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getSimulator();
            GraphicalSimulatorXTPN netSimXTPN = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getSimulatorXTPN();
            //TODO: 29062023 Shavi, zjebałes, wywołanie tej metody powoduje problemy z panelami symulatoró (obu!)
            //a kiedy działała, odpowiadzalna jest za reset paneli symulatorów. Tylko ze jak je teraz zresetujemy
            //to np. nie działa już wygaszanie i właćzanie przycisków stop/pause simulator, zarówno dla
            //clasic jak i XTPN. Zakomentowanie jej to hotfix, który powoduje pewne problemy, ale jest mniejszym
            // złem. Będę naprawiać w weekend.
            //setCurrentDockWindow(new HolmesDockWindowsTable(SubWindow.SIMULATOR, netSim, netSimXTPN, XTPN));

            //hotfix2 30062023: to powinno ustabilizować sytuację:
            dockWindowPanel.setSimulator(netSim, netSimXTPN);

            scrollPane.getViewport().add(getCurrentDockWindow());
        }
    }


    /**
     * Metoda wywoływana po wygenerowaniu t-inwariantów przez program. Zleca wykonanie
     * elementów interfejsu dla pokazywania t-inwariantów.
     *
     * @param t_invariants ArrayList[ArrayList[InvariantTransition]] - t-inwarianty
     */
    public void showT_invBoxWindow(ArrayList<ArrayList<Integer>> t_invariants) {
        if (type == DockWindowType.T_INVARIANTS) {
            setCurrentDockWindow(new HolmesDockWindowsTable(SubWindow.T_INVARIANTS, t_invariants));
            scrollPane.getViewport().add(getCurrentDockWindow());
        }
    }

    /**
     * Metoda wywoływana po wygenerowaniu t-inwariantów przez program. Zleca wykonanie
     * elementów interfejsu dla pokazywania t-inwariantów.
     *
     * @param p_invariants ArrayList[ArrayList[InvariantTransition]] - p-inwarianty
     */
    public void showP_invBoxWindow(ArrayList<ArrayList<Integer>> p_invariants) {
        if (type == DockWindowType.P_INVARIANTS) {
            setCurrentDockWindow(new HolmesDockWindowsTable(SubWindow.P_INVARIANTS, p_invariants));
            scrollPane.getViewport().add(getCurrentDockWindow());
        }
    }

    @SuppressWarnings("unused")
    public void showDecompositionBoxWindows(){
        if (type == DockWindowType.DECOMPOSITION) {
            setCurrentDockWindow(new HolmesDockWindowsTable(SubWindow.DECOMPOSITION));
            scrollPane.getViewport().add(getCurrentDockWindow());
        }
    }

    /**
     * Metoda wywoływana w momencie, kiedy z okna klastrów wpłyną dane o kolorach
     * tranzycji w każdym klastrze. Wtedy tworzy całą resztę elementów podokna klastrów.
     *
     * @param data ArrayList[ArrayList[Color]] - macierz kolorów
     */
    public void showClusterSelector(ClusterDataPackage data) {
        if (type == DockWindowType.ClusterSELECTOR) {
            setCurrentDockWindow(new HolmesDockWindowsTable(SubWindow.CLUSTERS, data));
            scrollPane.getViewport().add(getCurrentDockWindow());
        }
    }

    /**
     * Metoda odpowiedzialna za pokazanie podokna ze zbiorami MCT sieci.
     *
     * @param mctGroups ArrayList[ArrayList[Transition]] - macierz zbiorów MCT
     */
    public void showMCT(ArrayList<ArrayList<Transition>> mctGroups) {
        if (type == DockWindowType.MctANALYZER) {
            setCurrentDockWindow(new HolmesDockWindowsTable(SubWindow.MCT, mctGroups));
            scrollPane.getViewport().add(getCurrentDockWindow());
        }
    }

    /**
     * Metoda odpowiedzialna za pokazanie podokna ze zbiorami MCS sieci.
     */
    public void showMCS() {
        if (type == DockWindowType.MCSselector) {
            MCSDataMatrix mcsData = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getMCSdataCore();
            setCurrentDockWindow(new HolmesDockWindowsTable(SubWindow.MCS, mcsData));
            scrollPane.getViewport().add(getCurrentDockWindow());

        }
    }

    /**
     * Metoda odpowiedzialna za pokazanie podokna ze zbiorami Knockout sieci.
     */
    public void showKnockout(ArrayList<ArrayList<Integer>> knockoutData) {
        if (type == DockWindowType.Knockout) {
            setCurrentDockWindow(new HolmesDockWindowsTable(SubWindow.KNOCKOUT, knockoutData));
            scrollPane.getViewport().add(getCurrentDockWindow());
        }
    }

    /**
     * Metoda odpowiedzialna za uaktualnienie właściwości.
     */
    public void updateSimulatorProperties() {
        if (type == DockWindowType.SIMULATOR) {
            //getCurrentDockWindow().updateSimulatorProperties(); //pusta metoda TODO Sprawdzić czy usunąć
        }
    }

    /**
     * Metoda odpowiedzialna za wykrycie tego, co zostało kliknięte w programie, i o ile
     * to możliwe - wyświetlenie właściwości tego czegoś.
     * Działa dla podokna EDYTOR
     *
     * @param e SelectionActionEvent - zdarzenie wyboru elementów
     */
    public void selectElement(SelectionActionEvent e) {
        //dockWindowPanel.setBackground(Color.BLUE);
        if (e.getActionType() == SelectionActionType.SELECTED_ONE) {

            if (e.getElementLocationGroup().size() > 0) {
                Node n = e.getElementLocation().getParentNode();
                if (n.getType() == PetriNetElementType.PLACE) {
                    if( n instanceof PlaceXTPN ) {
                        setCurrentDockWindow(new HolmesDockWindowsTable(SubWindow.XTPNPLACE, n, e.getElementLocation()));
                        //GUIManager.getDefaultGUIManager().setPropertiesBox(this);
                    } else {
                        setCurrentDockWindow(new HolmesDockWindowsTable(SubWindow.PLACE, n, e.getElementLocation()));
                        //GUIManager.getDefaultGUIManager().getPropertiesBox().dockWindowPanel.setBackground(Color.BLUE);
                        //GUIManager.getDefaultGUIManager().getPropertiesBox().getCurrentDockWindow().setPanel(dockWindowPanel.getPanel());
                        //GUIManager.getDefaultGUIManager().getPropertiesBox().getCurrentDockWindow().getPanel().setBackground(Color.BLUE);
                        //GUIManager.getDefaultGUIManager().setPropertiesBox(this);
                        //GUIManager.getDefaultGUIManager().getPropertiesBox().dockWindowPanel.revalidate();
                        //GUIManager.getDefaultGUIManager().getPropertiesBox().dockWindowPanel.repaint();
                        //GUIManager.getDefaultGUIManager().getPropertiesBox().dockWindowPanel.getPanel().revalidate();
                        //GUIManager.getDefaultGUIManager().getPropertiesBox().dockWindowPanel.getPanel().repaint();
                        //.getPanel().revalidate();
                        //GUIManager.getDefaultGUIManager().getPropertiesBox().dockWindowPanel.getPanel().repaint();
                    }
                } else if (n.getType() == PetriNetElementType.META) {
                    setCurrentDockWindow(new HolmesDockWindowsTable(SubWindow.META, n, e.getElementLocation()));
                } else {
                    if (n.getType().equals(PetriNetElementType.TRANSITION)) {
                        if (((Transition) n).getTransType() == TransitionType.PN) {
                            setCurrentDockWindow(new HolmesDockWindowsTable(SubWindow.TRANSITION, n, e.getElementLocation()));
                        } else if (((Transition) n).getTransType() == TransitionType.TPN) {
                            setCurrentDockWindow(new HolmesDockWindowsTable(SubWindow.TIMETRANSITION, n, e.getElementLocation()));
                        } else if (((Transition) n).getTransType() == TransitionType.SPN) {
                            setCurrentDockWindow(new HolmesDockWindowsTable(SubWindow.SPNTRANSITION, n, e.getElementLocation()));
                        } else if ( n instanceof TransitionXTPN ) {
                            setCurrentDockWindow(new HolmesDockWindowsTable(SubWindow.XTPNTRANSITION, n, e.getElementLocation()));
                        } else if (((Transition) n).getTransType() == TransitionType.CPN) {
                            setCurrentDockWindow(new HolmesDockWindowsTable(SubWindow.CTRANSITION, n, e.getElementLocation()));
                        }

                        GUIManager.getDefaultGUIManager().propericeTMPBox.removeAll();
                        GUIManager.getDefaultGUIManager().propericeTMPBox.add(dockWindowPanel.getPanel());
                        GUIManager.getDefaultGUIManager().getFrame().revalidate();
                        GUIManager.getDefaultGUIManager().getFrame().repaint();
                    }
                }
                scrollPane.getViewport().add(getCurrentDockWindow());
            } else if (e.getArcGroup().size() > 0) {
                setCurrentDockWindow(new HolmesDockWindowsTable(SubWindow.ARC, e.getArc()));
                scrollPane.getViewport().add(getCurrentDockWindow());
            }
            //GUIManager.getDefaultGUIManager().getPropertiesBox().getCurrentDockWindow().getPanel().removeAll();
            //GUIManager.getDefaultGUIManager().getPropertiesBox().getCurrentDockWindow().setPanel(dockWindowPanel.getPanel());
            //GUIManager.getDefaultGUIManager().getPropertiesBox().getCurrentDockWindow().revalidate();
            //GUIManager.getDefaultGUIManager().getPropertiesBox().getCurrentDockWindow().repaint();
            /*JFrame nowe = new JFrame();
            nowe.add(dockWindowPanel.getPanel());
            nowe.setSize(300,300);
            nowe.setVisible(true);*/
            GUIManager.getDefaultGUIManager().propericeTMPBox.removeAll();
            GUIManager.getDefaultGUIManager().propericeTMPBox.add(dockWindowPanel.getPanel());
            GUIManager.getDefaultGUIManager().getFrame().revalidate();
            GUIManager.getDefaultGUIManager().getFrame().repaint();

        } else if (e.getActionType() == SelectionActionType.SELECTED_SHEET) {
            GUIManager.getDefaultGUIManager().getPropertiesBox().setCurrentDockWindow(new HolmesDockWindowsTable(SubWindow.SHEET,
                    guiManager.getWorkspace().getSheets().get(guiManager.getWorkspace().getIndexOfId(e.getSheetId()))));
            scrollPane.getViewport().add(getCurrentDockWindow());



            //GUIManager.getDefaultGUIManager().getPropertiesBox().setCurrentDockWindow(dockWindowPanel.getPanel());
            //GUIManager.getDefaultGUIManager().getPropertiesBox().getCurrentDockWindow().getPanel().revalidate();
            //GUIManager.getDefaultGUIManager().getPropertiesBox().getCurrentDockWindow().getPanel().repaint();
            GUIManager.getDefaultGUIManager().propericeTMPBox.removeAll();
            GUIManager.getDefaultGUIManager().propericeTMPBox.add(dockWindowPanel.getPanel());
            GUIManager.getDefaultGUIManager().getFrame().revalidate();
            GUIManager.getDefaultGUIManager().getFrame().repaint();
            dockWindowPanel.setBackground(Color.BLUE);
        }
        //GUIManager.getDefaultGUIManager().getPropertiesBox().getCurrentDockWindow().getPanel().setBackground(Color.GREEN);


    }

    /**
     * Metoda zwracająca odpowiedni obiekt właściwości, czyli obiekt zawierający komponenty
     * któregoś z podokien programu wyświetlające przyciski, napisy, itd.
     *
     * @return HolmesDockWindowsTable - obiekt podokna z ramach okna właściwości
     */
    public HolmesDockWindowsTable getCurrentDockWindow() {
        return dockWindowPanel;
    }

    /**
     * Metoda ustawiająca odpowiedni obiekt podokna, czyli obiekt zawierający komponenty
     * któregoś z podokien programu wyświetlającego np. przyciski symulatora czy informacje
     * o elementach sieci.
     */
    public void setCurrentDockWindow(HolmesDockWindowsTable properties) {
        this.dockWindowPanel = properties;
    }

    /**
     * Metoda zwracająca obiekt panelu wyświetlającego zaznaczone elementy sieci.
     *
     * @return SelectionPanel - obiekt panelu
     */
    public SelectionPanel getSelectionPanel() {
        return selectionPanel;
    }

    /**
     * Metoda ustawiająca nowy obiekt panelu wyświetlającego zaznaczone elementy sieci.
     */
    private void setSelectionPanel(SelectionPanel selectionPanel) {
        this.selectionPanel = selectionPanel;
    }
}
