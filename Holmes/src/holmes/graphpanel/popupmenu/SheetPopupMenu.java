package holmes.graphpanel.popupmenu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.Serial;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;

import holmes.darkgui.GUIManager;
import holmes.darkgui.LanguageManager;
import holmes.files.io.IOprotocols;
import holmes.graphpanel.GraphPanel;
import holmes.petrinet.elements.*;
import holmes.petrinet.elements.PetriNetElement.PetriNetElementType;
import holmes.petrinet.subnets.SubnetsActions;
import holmes.utilities.HolmesFileView;
import holmes.workspace.ExtensionFileFilter;

/**
 * Klasa tworząca obiekty menu kontekstowego dla arkusza z rysunkiem sieci.
 */
public class SheetPopupMenu extends GraphPanelPopupMenu {
    @Serial
    private static final long serialVersionUID = 3206422633820189233L;
    private static final GUIManager overlord = GUIManager.getDefaultGUIManager();
    private static final LanguageManager lang = GUIManager.getLanguageManager();
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

        //x = overlord.getWorkspace().getSelectedSheet().getMousePosition().x;
        //y = overlord.getWorkspace().getSelectedSheet().getMousePosition().y;

        this.addMenuItem(lang.getText("SPM_entry001"), "", e -> getGraphPanel().getSelectionManager().selectAllElementLocations());

        this.addSeparator();

        this.add(pasteMenuItem);

        this.addSeparator();

        this.addMenuItem(lang.getText("SPM_entry002"), "refresh.png", e -> {
            getGraphPanel().invalidate();
            getGraphPanel().repaint();
        });

        this.addMenuItem(lang.getText("SPM_entry003"), "clearColors.png", e -> overlord.reset.clearGraphColors());

        this.addMenuItem(lang.getText("SPM_entry004"), "picture_save.png", //Save to image file
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    exportToPicture();
                }

                private void exportToPicture() {
                    //String lastPath = getGraphPanel().getPetriNet().getWorkspace().getGUI().getLastPath();
                    String lastPath = overlord.getLastPath();
                    JFileChooser fc;
                    if (lastPath == null)
                        fc = new JFileChooser();
                    else
                        fc = new JFileChooser(lastPath);

                    fc.setFileView(new HolmesFileView());
                    FileFilter pngFilter = new ExtensionFileFilter(".png - Portable Network Graphics", new String[]{"png"});
                    FileFilter bmpFilter = new ExtensionFileFilter(".bmp -  Bitmap Image", new String[]{"bmp"});
                    FileFilter jpegFilter = new ExtensionFileFilter(".jpeg - JPEG Image", new String[]{"jpeg"});
                    FileFilter jpgFilter = new ExtensionFileFilter(".jpg - JPEG Image", new String[]{"jpg"});
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
                            overlord.setLastPath(file.getParentFile().getPath());
                        } catch (IOException ex) {
                            overlord.log(lang.getText("LOGentry00371exception")+ "\n" + ex.getMessage(), "error", true);
                        }
                    }
                }
            });

        this.addSeparator();

        this.addMenuItem(lang.getText("SPM_entry005"), "undo.png", e -> getGraphPanel().setZoom(100, getGraphPanel().getZoom()));

        JMenu zoomMenu = new JMenu(lang.getText("SPM_entry006")); //zoom

        this.add(zoomMenu);

        zoomMenu.add(createMenuItem("200%", "", null, arg0 -> getGraphPanel().setZoom(200, getGraphPanel().getZoom())));

        zoomMenu.add(createMenuItem("100%", "", null, arg0 -> getGraphPanel().setZoom(100, getGraphPanel().getZoom())));

        zoomMenu.add(createMenuItem("80%", "", null, arg0 -> getGraphPanel().setZoom(80, getGraphPanel().getZoom())));

        zoomMenu.add(createMenuItem("50%", "", null, arg0 -> getGraphPanel().setZoom(50, getGraphPanel().getZoom())));

        zoomMenu.add(createMenuItem("30%", "", null, arg0 -> getGraphPanel().setZoom(30, getGraphPanel().getZoom())));

        this.addSeparator();

        this.addMenuItem(lang.getText("SPM_entry007"), "smallInvisibility.png", e -> {
            if(getGraphPanel().getSelectionManager().getSelectedElementLocations().isEmpty())
                return;

            for(Transition trans : overlord.getWorkspace().getProject().getTransitions()) {
                trans.setInvisibility(false);
            }
            for(Place place : overlord.getWorkspace().getProject().getPlaces()) {
                place.setInvisibility(false);
            }
            overlord.getWorkspace().repaintAllGraphPanels();
        });

        this.addMenuItem(lang.getText("SPM_entry008"), "offlineSmall.png", e -> {
            if(getGraphPanel().getSelectionManager().getSelectedElementLocations().isEmpty())
                return;

            for(Transition trans : overlord.getWorkspace().getProject().getTransitions()) {
                trans.setKnockout(false);
            }

            overlord.getWorkspace().repaintAllGraphPanels();
        });

        this.addSeparator();

        JMenu analMenu = new JMenu(lang.getText("SPM_entry009")); // (⌐■_■) //Network Analysis
        this.add(analMenu);
        analMenu.add(createMenuItem(lang.getText("SPM_entry010"), "invImportPopup.png", null, arg0 -> overlord.io.loadExternalAnalysis(true)));
        analMenu.add(createMenuItem(lang.getText("SPM_entry011"), "generateINA.png", null, arg0 -> overlord.io.fastGenerateTinvariants()));

        analMenu.add(createMenuItem(lang.getText("SPM_entry012"), "generateMCT.png", null, arg0 -> overlord.generateMCT()));

        JMenu mctSubMenu = new JMenu(lang.getText("SPM_entry013"));    //MCT Options
        analMenu.add(mctSubMenu);

        JMenuItem mct1 = createMenuItem(lang.getText("SPM_entry014"), "", null, arg0 -> overlord.io.generateSimpleMCTFile()); //Simple MCT file
        mctSubMenu.add(mct1);

        JMenuItem mct2 = createMenuItem(lang.getText("SPM_entry015"), "", null, arg0 -> { //Tex output file
            //overlord.generateMCT();
        });
        mct2.setEnabled(false);
        mctSubMenu.add(mct2);

        JMenuItem mct3 = createMenuItem(lang.getText("SPM_entry016"), "", null, arg0 -> { //Other files
            //overlord.generateMCT();
        });
        mct3.setEnabled(false);
        mctSubMenu.add(mct3);

        JMenu netMenu = new JMenu(lang.getText("SPM_entry017")); //Network Tools
        this.add(netMenu);
        netMenu.add(createMenuItem(lang.getText("SPM_entry018"), "", null, arg0 -> overlord.io.markTransitions(0))); //Show TPN transitions
        netMenu.add(createMenuItem(lang.getText("SPM_entry019"), "", null, arg0 -> overlord.io.markTransitions(1))); //Show DPN transitions
        netMenu.add(createMenuItem(lang.getText("SPM_entry020"), "", null, arg0 -> overlord.io.markTransitions(2))); //Show TPN/DPN transitions
        netMenu.add(createMenuItem(lang.getText("SPM_entry021"), "", null, arg0 -> overlord.subnetsHQ.checkSnoopyCompatibility())); //Fix Snoopy compatibility

        if(sheetID != 0) {
            netMenu.add(createMenuItem(lang.getText("SPM_entry022"), "", null, arg0 -> overlord.testRemovePanel(sheetID)  ) ); //Remove panel
        }

        //SUBNET IMPORT PROTOTYP
        this.addSeparator();

        JMenu subImpMenu = new JMenu(lang.getText("SPM_entry023")); //Subnet import
        this.add(subImpMenu);

        subImpMenu.add(createMenuItem(lang.getText("SPM_entry024"), "", null, arg0 -> { //Import Subnet with original location
            JFileChooser fc = new JFileChooser();
            int returnVal = fc.showOpenDialog(SheetPopupMenu.this);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();

                IOprotocols io = new IOprotocols();
                io.importSubnetFromFile(file.getAbsolutePath(), 0, 0);
            }
        }));

        subImpMenu.add(createMenuItem(lang.getText("SPM_entry025"), "", null, arg0 -> { //Import Subnet with point click location
            JFileChooser fc = new JFileChooser();
            int returnVal = fc.showOpenDialog(SheetPopupMenu.this);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                IOprotocols io = new IOprotocols();

                for (ElementLocation el : getGraphPanel().getSelectionManager().getSelectedElementLocations()) {
                    el.setSelected(false);
                }

            }
        }));

        if (getGraphPanel().getSheetId() != 0) {
            this.addMenuItem(lang.getText("SPM_entry026"), "", e ->SubnetsActions.addExistingElement(graphPanel) ); //Add existing node
        }
    }

    /**
     * Metoda eksportująca rysunek sieci do pliku - dla klikniętego arkusza.
     */
    private void exportToFile() {
        JFileChooser fc = new JFileChooser();
        FileFilter pngFilter = new ExtensionFileFilter(".png - Portable Network Graphics", new String[]{"png"});
        FileFilter bmpFilter = new ExtensionFileFilter(".bmp -  Bitmap Image", new String[]{"bmp"});
        FileFilter jpegFilter = new ExtensionFileFilter(".jpeg - JPEG Image", new String[]{"jpeg"});
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
                overlord.log(lang.getText("LOGentry00372")+ " " + file.getPath() + ext, "text", true);
            } catch (IOException ex) {
                overlord.log(lang.getText("LOGentry00373exception")+ " " + ex.getMessage(), "error", true);
            }
        }
    }
}
