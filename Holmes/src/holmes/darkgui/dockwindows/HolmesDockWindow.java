package holmes.darkgui.dockwindows;

import java.awt.*;
import java.util.ArrayList;

import javax.swing.*;

import holmes.analyse.InvariantsCalculator;
import holmes.analyse.InvariantsTools;
import holmes.clusters.ClusterDataPackage;
import holmes.darkgui.GUIManager;
import holmes.darkgui.LanguageManager;
import holmes.darkgui.dockwindows.HolmesDockWindowsTable.SubWindow;
import holmes.graphpanel.SelectionActionListener.SelectionActionEvent;
import holmes.graphpanel.SelectionActionListener.SelectionActionEvent.SelectionActionType;
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
    //private static final long serialVersionUID = -1966643269924197502L;
    //private Dockable dockable;
    private static final GUIManager overlord = GUIManager.getDefaultGUIManager();
    private static final LanguageManager lang = GUIManager.getLanguageManager();
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

        switch (type) {
            case SIMULATOR -> {
                GraphicalSimulator netSim = overlord.getWorkspace().getProject().getSimulator();
                GraphicalSimulatorXTPN netSimXTPN = overlord.getWorkspace().getProject().getSimulatorXTPN();
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
                setCurrentDockWindow(new HolmesDockWindowsTable(SubWindow.T_INVARIANTS, overlord.getWorkspace().getProject().getT_InvMatrix()));
                scrollPane.getViewport().add(getCurrentDockWindow());
            }
            case P_INVARIANTS -> {
                setCurrentDockWindow(new HolmesDockWindowsTable(SubWindow.P_INVARIANTS, overlord.getWorkspace().getProject().getP_InvMatrix()));
                scrollPane.getViewport().add(getCurrentDockWindow());
            }
            case MctANALYZER -> {
                setCurrentDockWindow(new HolmesDockWindowsTable(SubWindow.MCT, overlord.getWorkspace().getProject().getMCTMatrix()));
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
     * Metoda odpowiedzialna za wypełnienie sekcji symulatora sieci.
     */
    public void createSimulatorProperties(boolean XTPN) {
        if (type == DockWindowType.SIMULATOR) {
            GraphicalSimulator netSim = overlord.getWorkspace().getProject().getSimulator();
            GraphicalSimulatorXTPN netSimXTPN = overlord.getWorkspace().getProject().getSimulatorXTPN();

            //29062023: wywołanie tej metody powoduje problemy z panelami symulatorów (obu!)
            //a kiedy działała, odpowiadzalna jest za reset paneli symulatorów. Tylko ze jak je teraz zresetujemy
            //to np. nie działa już wygaszanie i właćzanie przycisków stop/pause simulator, zarówno dla
            //clasic jak i XTPN. Zakomentowanie jej to hotfix, który powoduje pewne problemy, ale jest mniejszym
            // złem. Będę naprawiać w weekend:
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
            if(t_invariants != null) {
                InvariantsCalculator ic = new InvariantsCalculator(true);
                InvariantsTools.analyseInvariantTypes(ic.getCMatrix(), t_invariants, true);
            }
            overlord.getT_invBox().getCurrentDockWindow().cleanTINVsubwindowFields();
            overlord.getT_invBox().getCurrentDockWindow().setT_invariants(t_invariants);
            overlord.getT_invBox().getCurrentDockWindow().refreshInvariantsComboBox();
            overlord.getT_invBox().getCurrentDockWindow().refreshSubSurCombos();
        }
    }

    /**
     * Metoda odpowiedzialna za pokazanie podokna ze zbiorami MCT sieci.
     * @param mctGroups ArrayList[ArrayList[Transition]] - macierz zbiorów MCT
     */
    public void showMCT(ArrayList<ArrayList<Transition>> mctGroups) {
        if (type == DockWindowType.MctANALYZER) {
            overlord.getMctBox().getCurrentDockWindow().cleanMCtsubwindowFields();
            overlord.getMctBox().getCurrentDockWindow().refreshMCTComboBox(mctGroups);
        }
    }

    /**
     * Metoda wywoływana po wygenerowaniu t-inwariantów przez program. Zleca wykonanie
     * elementów interfejsu dla pokazywania t-inwariantów.
     * @param p_invariants ArrayList[ArrayList[InvariantTransition]] - p-inwarianty
     */
    public void showP_invBoxWindow(ArrayList<ArrayList<Integer>> p_invariants) {
        if (type == DockWindowType.P_INVARIANTS) {
            overlord.getP_invBox().getCurrentDockWindow().cleanPINVsubwindowFields();
            overlord.getP_invBox().getCurrentDockWindow().setP_invariants(p_invariants);
            overlord.getP_invBox().getCurrentDockWindow().refreshP_invComboBox();
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
     * Metoda odpowiedzialna za odświeżenie podokna ze zbiorami MCS sieci.
     */
    public void showMCS() {
        if (type == DockWindowType.MCSselector) {
            overlord.getMCSBox().getCurrentDockWindow().cleanMCScomboBoxes();

            //MCSDataMatrix mcsData = overlord.getWorkspace().getProject().getMCSdataCore();
            //setCurrentDockWindow(new HolmesDockWindowsTable(SubWindow.MCS, mcsData));
            //scrollPane.getViewport().add(getCurrentDockWindow());
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

            if (!e.getElementLocationGroup().isEmpty()) {
                Node n = e.getElementLocation().getParentNode();
                if (n.getType() == PetriNetElementType.PLACE) {
                    if( n instanceof PlaceXTPN ) {
                        setCurrentDockWindow(new HolmesDockWindowsTable(SubWindow.XTPNPLACE, n, e.getElementLocation()));
                        //overlord.setPropertiesBox(this);
                    } else {
                        setCurrentDockWindow(new HolmesDockWindowsTable(SubWindow.PLACE, n, e.getElementLocation()));
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

                        overlord.propericeTMPBox.removeAll();
                        overlord.propericeTMPBox.add(dockWindowPanel.getPanel());
                        overlord.getFrame().revalidate();
                        overlord.getFrame().repaint();
                    }
                }
                scrollPane.getViewport().add(getCurrentDockWindow());
            } else if (!e.getArcGroup().isEmpty()) {
                setCurrentDockWindow(new HolmesDockWindowsTable(SubWindow.ARC, e.getArc()));
                scrollPane.getViewport().add(getCurrentDockWindow());
            }

            overlord.propericeTMPBox.removeAll();
            overlord.propericeTMPBox.add(dockWindowPanel.getPanel());
            overlord.getFrame().revalidate();
            overlord.getFrame().repaint();

        } else if (e.getActionType() == SelectionActionType.SELECTED_SHEET) {
            overlord.getPropertiesBox().setCurrentDockWindow(new HolmesDockWindowsTable(SubWindow.SHEET,
                    overlord.getWorkspace().getSheets().get(overlord.getWorkspace().getIndexOfId(e.getSheetId()))));
            scrollPane.getViewport().add(getCurrentDockWindow());

            overlord.propericeTMPBox.removeAll();
            overlord.propericeTMPBox.add(dockWindowPanel.getPanel());
            overlord.getFrame().revalidate();
            overlord.getFrame().repaint();
            dockWindowPanel.setBackground(Color.BLUE);
        }
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
