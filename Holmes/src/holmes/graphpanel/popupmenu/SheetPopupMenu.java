package holmes.graphpanel.popupmenu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.Serial;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import holmes.darkgui.GUIManager;
import holmes.files.io.IOprotocols;
import holmes.graphpanel.GraphPanel;
import holmes.petrinet.elements.ElementLocation;
import holmes.petrinet.elements.PetriNetElement.PetriNetElementType;
import holmes.petrinet.elements.Place;
import holmes.petrinet.elements.Transition;
import holmes.utilities.HolmesFileView;
import holmes.workspace.ExtensionFileFilter;

/**
 * Klasa tworząca obiekty menu kontekstowego dla arkusza z rysunkiem sieci.
 */
public class SheetPopupMenu extends GraphPanelPopupMenu {
    @Serial
    private static final long serialVersionUID = 3206422633820189233L;
    public int x;
    public int y;

    public int sheetID = -1;

    /**
     * Konstruktor obiektu klasy SheetPopupMenu.
     * @param graphPanel GraphPanel - panel dla którego powstaje menu.
     */
    public SheetPopupMenu(GraphPanel graphPanel, PetriNetElementType pne) {
        super(graphPanel, pne);
        sheetID = graphPanel.getSheetId();

        //x = GUIManager.getDefaultGUIManager().getWorkspace().getSelectedSheet().getMousePosition().x;
        //y = GUIManager.getDefaultGUIManager().getWorkspace().getSelectedSheet().getMousePosition().y;

        this.addMenuItem("Select All", "", e -> getGraphPanel().getSelectionManager().selectAllElementLocations());

        this.addSeparator();

        this.add(pasteMenuItem);

        this.addSeparator();

        this.addMenuItem("Refresh", "refresh.png", e -> {
            getGraphPanel().invalidate();
            getGraphPanel().repaint();
        });

        this.addMenuItem("Clear colors", "clearColors.png", e -> GUIManager.getDefaultGUIManager().reset.clearGraphColors());

        this.addMenuItem("Save to image file", "picture_save.png",
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    exportToPicture();
                }

                private void exportToPicture() {
                    //String lastPath = getGraphPanel().getPetriNet().getWorkspace().getGUI().getLastPath();
                    String lastPath = GUIManager.getDefaultGUIManager().getLastPath();
                    JFileChooser fc;
                    if (lastPath == null)
                        fc = new JFileChooser();
                    else
                        fc = new JFileChooser(lastPath);

                    fc.setFileView(new HolmesFileView());
                    FileFilter pngFilter = new ExtensionFileFilter(".png - Portable Network Graphics", new String[]{"png"});
                    FileFilter bmpFilter = new ExtensionFileFilter(".bmp -  Bitmap Image File", new String[]{"bmp"});
                    FileFilter jpegFilter = new ExtensionFileFilter(".jpeg - JPEG Image File", new String[]{"jpeg"});
                    FileFilter jpgFilter = new ExtensionFileFilter(".jpg - JPEG Image File", new String[]{"jpg"});
                    fc.setFileFilter(pngFilter);
                    fc.addChoosableFileFilter(pngFilter);
                    fc.addChoosableFileFilter(bmpFilter);
                    fc.addChoosableFileFilter(jpegFilter);
                    fc.addChoosableFileFilter(jpgFilter);
                    fc.setAcceptAllFileFilterUsed(false);
                    if (fc.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
                        File file = fc.getSelectedFile();
                        String ext = "";
                        String extension = fc.getFileFilter().getDescription();
                        if (extension.contains(".png")) ext = ".png";
                        if (extension.contains(".bmp")) ext = ".bmp";
                        if (extension.contains(".jpeg") || extension.contains(".jpg")) ext = ".jpeg";

                        BufferedImage image = getGraphPanel().createImageFromSheet();
                        try {
                            String ext2 = "";
                            String path = file.getPath();
                            if (ext.equals(".png") && !(path.contains(".png"))) ext2 = ".png";
                            if (ext.equals(".bmp") && !file.getPath().contains(".bmp")) ext2 = ".bmp";
                            if (ext.equals(".jpeg") && !file.getPath().contains(".jpeg")) ext2 = ".jpeg";
                            if (ext.equals(".jpeg") && !file.getPath().contains(".jpg")) ext2 = ".jpg";

                            ImageIO.write(image, ext.substring(1), new File(file.getPath() + ext2));

                            GUIManager.getDefaultGUIManager().setLastPath(file.getParentFile().getPath());

                            //getGraphPanel().getPetriNet().getWorkspace().getGUI().setLastPath(
                            //		file.getParentFile().getPath()); //  ╯°□°）╯ ︵  ┻━━━┻
                        } catch (IOException ex) {
                            ex.printStackTrace();
                            JOptionPane.showMessageDialog(null,
                                    "Saving net sheet into picture failed.",
                                    "Export Picture Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            });

        this.addSeparator();

        this.addMenuItem("Fast zoom reset", "undo.png", e -> getGraphPanel().setZoom(100, getGraphPanel().getZoom()));

        JMenu zoomMenu = new JMenu("Zoom");

        this.add(zoomMenu);

        zoomMenu.add(createMenuItem("200%", "", null, arg0 -> getGraphPanel().setZoom(200, getGraphPanel().getZoom())));

        zoomMenu.add(createMenuItem("100%", "", null, arg0 -> getGraphPanel().setZoom(100, getGraphPanel().getZoom())));

        zoomMenu.add(createMenuItem("80%", "", null, arg0 -> getGraphPanel().setZoom(80, getGraphPanel().getZoom())));

        zoomMenu.add(createMenuItem("50%", "", null, arg0 -> getGraphPanel().setZoom(50, getGraphPanel().getZoom())));

        zoomMenu.add(createMenuItem("30%", "", null, arg0 -> getGraphPanel().setZoom(30, getGraphPanel().getZoom())));

        this.addSeparator();

        this.addMenuItem("All Invisibility: OFF", "smallInvisibility.png", e -> {
            if(getGraphPanel().getSelectionManager().getSelectedElementLocations().size() == 0)
                return;

            for(Transition trans : GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions()) {
                trans.setInvisibility(false);
            }
            for(Place place : GUIManager.getDefaultGUIManager().getWorkspace().getProject().getPlaces()) {
                place.setInvisibility(false);
            }
            GUIManager.getDefaultGUIManager().getWorkspace().repaintAllGraphPanels();
        });

        this.addMenuItem("All knockout: OFF", "offlineSmall.png", e -> {
            if(getGraphPanel().getSelectionManager().getSelectedElementLocations().size() == 0)
                return;

            for(Transition trans : GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions()) {
                trans.setKnockout(false);
            }

            GUIManager.getDefaultGUIManager().getWorkspace().repaintAllGraphPanels();
        });

        this.addSeparator();

        JMenu analMenu = new JMenu("Network Analysis"); // (⌐■_■)
        this.add(analMenu);
        analMenu.add(createMenuItem("Import t-invariants from file", "invImportPopup.png", null, arg0 -> GUIManager.getDefaultGUIManager().io.loadExternalAnalysis(true)));
        analMenu.add(createMenuItem("Generate t-invariants", "generateINA.png", null, arg0 -> GUIManager.getDefaultGUIManager().io.fastGenerateTinvariants()));

        analMenu.add(createMenuItem("Generate MCT sets", "generateMCT.png", null, arg0 -> GUIManager.getDefaultGUIManager().generateMCT()));

        JMenu mctSubMenu = new JMenu("MCT Options");
        analMenu.add(mctSubMenu);

        JMenuItem mct1 = createMenuItem("Simple MCT file", "", null, arg0 -> GUIManager.getDefaultGUIManager().io.generateSimpleMCTFile());
        mctSubMenu.add(mct1);

        JMenuItem mct2 = createMenuItem("Tex output file", "", null, arg0 -> {
            //GUIManager.getDefaultGUIManager().generateMCT();
        });
        mct2.setEnabled(false);
        mctSubMenu.add(mct2);

        JMenuItem mct3 = createMenuItem("Other files", "", null, arg0 -> {
            //GUIManager.getDefaultGUIManager().generateMCT();
        });
        mct3.setEnabled(false);
        mctSubMenu.add(mct3);

        JMenu netMenu = new JMenu("Network Tools");
        this.add(netMenu);
        netMenu.add(createMenuItem("Show TPN transitions", "", null, arg0 -> GUIManager.getDefaultGUIManager().io.markTransitions(0)));
        netMenu.add(createMenuItem("Show DPN transitions", "", null, arg0 -> GUIManager.getDefaultGUIManager().io.markTransitions(1)));
        netMenu.add(createMenuItem("Show TPN/DPN transitions", "", null, arg0 -> GUIManager.getDefaultGUIManager().io.markTransitions(2)));
        netMenu.add(createMenuItem("Fix Snoopy compatibility", "", null, arg0 -> GUIManager.getDefaultGUIManager().subnetsHQ.checkSnoopyCompatibility()));

        if(sheetID != 0) {
            netMenu.add(createMenuItem("Remove panel", "", null, arg0 -> GUIManager.getDefaultGUIManager().testRemovePanel(sheetID)  ) );
        }

        //SUBNET IMPORT PROTOTYP
        this.addSeparator();

        JMenu subImpMenu = new JMenu("Subnet import");
        this.add(subImpMenu);

        subImpMenu.add(createMenuItem("Import Subnet with original location ", "", null, arg0 -> {
            JFileChooser fc = new JFileChooser();
            int returnVal = fc.showOpenDialog(SheetPopupMenu.this);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();

                IOprotocols io = new IOprotocols();
                io.importSubnetFromFile(file.getAbsolutePath(), 0, 0);
            }
        }));

        subImpMenu.add(createMenuItem("Import Subnet with point click location ", "", null, arg0 -> {
            //System.out.println("x " + GUIManager.getDefaultGUIManager().getWorkspace().getSelectedSheet().getMousePosition().x  + " y " + GUIManager.getDefaultGUIManager().getWorkspace().getSelectedSheet().getMousePosition().y);
            JFileChooser fc = new JFileChooser();
            int returnVal = fc.showOpenDialog(SheetPopupMenu.this);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();

                IOprotocols io = new IOprotocols();
                //io.importSubnetFromFile(file.getAbsolutePath(), x, y);

                for (ElementLocation el : getGraphPanel().getSelectionManager().getSelectedElementLocations()) {
                    el.setSelected(false);
                }

            }
        }));

        /*
        this.addMenuItem("ImporttSubnetFromFile", "refresh.png", new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                JFileChooser fc = new JFileChooser();
                int returnVal = fc.showOpenDialog(SheetPopupMenu.this);

                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File file = fc.getSelectedFile();

                    IOprotocols io = new IOprotocols();
                    io.importSubnetFromFile(file.getAbsolutePath());
                }
            }
        });
        */
    }





    /**
     * Metoda eksportująca rysunek sieci do pliku - dla klikniętego arkusza.
     */
    private void exportToFile() {
        JFileChooser fc = new JFileChooser();
        FileFilter pngFilter = new ExtensionFileFilter(
                ".png - Portable Network Graphics", new String[]{"png"});
        FileFilter bmpFilter = new ExtensionFileFilter(
                ".bmp -  Bitmap Image File", new String[]{"bmp"});
        FileFilter jpegFilter = new ExtensionFileFilter(
                ".jpeg - JPEG Image File", new String[]{"jpeg"});
        fc.setFileFilter(pngFilter);
        fc.addChoosableFileFilter(pngFilter);
        fc.addChoosableFileFilter(bmpFilter);
        fc.addChoosableFileFilter(jpegFilter);
        fc.setAcceptAllFileFilterUsed(false);
        if (fc.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            String ext = "";
            String extension = fc.getFileFilter()
                    .getDescription();
            if (extension.contains(".png")) {
                ext = ".png";
            }
            if (extension.contains(".bmp")) {
                ext = ".bmp";
            }
            if (extension.contains(".jpeg")) {
                ext = ".jpeg";
            }
            BufferedImage image = getGraphPanel().createImageFromSheet();
            try {
                ImageIO.write(image, ext.substring(1), new File(file.getPath() + ext));
                GUIManager.getDefaultGUIManager().log("Network image save to file " + file.getPath() + ext, "text", true);
            } catch (IOException ex) {
                ex.printStackTrace();
                GUIManager.getDefaultGUIManager().log("Error: " + ex.getMessage(), "error", true);
            }
        }
    }
}
