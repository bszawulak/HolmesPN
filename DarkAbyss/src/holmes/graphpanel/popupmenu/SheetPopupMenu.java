package holmes.graphpanel.popupmenu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import holmes.darkgui.GUIManager;
import holmes.graphpanel.GraphPanel;
import holmes.petrinet.elements.PetriNetElement.PetriNetElementType;
import holmes.utilities.HolmesFileView;
import holmes.workspace.ExtensionFileFilter;

/**
 * Klasa tworząca obiekty menu kontekstowego dla arkusza z rysunkiem sieci.
 * @author students
 *
 */
public class SheetPopupMenu extends GraphPanelPopupMenu {
	private static final long serialVersionUID = 3206422633820189233L;

	/**
	 * Konstruktor obiektu klasy SheetPopupMenu.
	 * @param graphPanel GraphPanel - panel dla którego powstaje menu
	 */
	public SheetPopupMenu(GraphPanel graphPanel, PetriNetElementType pne) {
		super(graphPanel, pne);

		this.addMenuItem("Select All", "", new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				getGraphPanel().getSelectionManager().selectAllElementLocations();
			}
		});

		// this.addMenuItem("Align to grid", "", new ActionListener() {
		// public void actionPerformed(ActionEvent e) {
		// }
		// });

		this.addSeparator();

		this.add(pasteMenuItem);

		this.addSeparator();

		this.addMenuItem("Refresh", "refresh.png", new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				getGraphPanel().invalidate();
				getGraphPanel().repaint();
			}
		});
		
		this.addMenuItem("Clear colors", "clearColors.png", new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				GUIManager.getDefaultGUIManager().reset.clearGraphColors();
			}
		});

		this.addMenuItem("Save to image file", "picture_save.png",
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						exportToPicture();
					}
					private void exportToPicture() {
						//String lastPath = getGraphPanel().getPetriNet().getWorkspace().getGUI().getLastPath();
						String lastPath = GUIManager.getDefaultGUIManager().getLastPath();
						JFileChooser fc;
						if(lastPath == null)
							fc = new JFileChooser();
						else
							fc = new JFileChooser(lastPath);
						
						fc.setFileView(new HolmesFileView());
						FileFilter pngFilter = new ExtensionFileFilter(".png - Portable Network Graphics", new String[] { "png" });
						FileFilter bmpFilter = new ExtensionFileFilter(".bmp -  Bitmap Image File", new String[] { "bmp" });
						FileFilter jpegFilter = new ExtensionFileFilter(".jpeg - JPEG Image File", new String[] { "jpeg" });
						FileFilter jpgFilter = new ExtensionFileFilter(".jpg - JPEG Image File", new String[] { "jpg" });
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
								if(ext.equals(".png") && !(path.contains(".png"))) ext2 = ".png";
								if(ext.equals(".bmp") && !file.getPath().contains(".bmp")) ext2 = ".bmp";
								if(ext.equals(".jpeg") && !file.getPath().contains(".jpeg")) ext2 = ".jpeg";
								if(ext.equals(".jpeg") && !file.getPath().contains(".jpg")) ext2 = ".jpg";
								
								ImageIO.write(image, ext.substring(1), new File(file.getPath() + ext2));
								
								GUIManager.getDefaultGUIManager().setLastPath(file.getParentFile().getPath());
								
								//getGraphPanel().getPetriNet().getWorkspace().getGUI().setLastPath(
								//		file.getParentFile().getPath()); //  ╯°□°）╯ ︵  ┻━━━┻
							} catch (IOException ex) {
								ex.printStackTrace();
								JOptionPane.showMessageDialog(null,
										"Saving net sheet into picture failed.",
										"Export Picture Error",JOptionPane.ERROR_MESSAGE);
								return;
							}
						}
					}
				});

		this.addSeparator();
		
		this.addMenuItem("Fast zoom reset", "undo.png", new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				getGraphPanel().setZoom(100, getGraphPanel().getZoom());
			}
		});

		JMenu zoomMenu = new JMenu("Zoom");

		this.add(zoomMenu);

		zoomMenu.add(createMenuItem("100%", "", null, new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				getGraphPanel().setZoom(100, getGraphPanel().getZoom());
			}
		}));

		zoomMenu.add(createMenuItem("80%", "", null, new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				getGraphPanel().setZoom(80, getGraphPanel().getZoom());
			}
		}));

		zoomMenu.add(createMenuItem("50%", "", null, new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				getGraphPanel().setZoom(50, getGraphPanel().getZoom());
			}
		}));

		zoomMenu.add(createMenuItem("30%", "", null, new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				getGraphPanel().setZoom(30, getGraphPanel().getZoom());
			}
		}));
		
		this.addSeparator();
		JMenu analMenu = new JMenu("Network Analysis"); // (⌐■_■) 
		this.add(analMenu);
		analMenu.add(createMenuItem("Import t-invariants from file", "invImportPopup.png", null, new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				GUIManager.getDefaultGUIManager().io.loadExternalAnalysis(true);
			}
		}));
		analMenu.add(createMenuItem("Generate t-invariants", "generateINA.png", null, new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				GUIManager.getDefaultGUIManager().io.fastGenerateTinvariants();
			}
		}));
		
		analMenu.add(createMenuItem("Generate MCT sets", "generateMCT.png", null, new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				GUIManager.getDefaultGUIManager().generateMCT();
			}
		}));
		
		JMenu mctSubMenu = new JMenu("MCT Options");
		analMenu.add(mctSubMenu);
		
		JMenuItem mct1 = createMenuItem("Simple MCT file", "", null, new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				GUIManager.getDefaultGUIManager().io.generateSimpleMCTFile();
			}
		});
		mctSubMenu.add(mct1);
		
		JMenuItem mct2 = createMenuItem("Tex output file", "", null, new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				//GUIManager.getDefaultGUIManager().generateMCT();
			}
		});
		mct2.setEnabled(false);
		mctSubMenu.add(mct2);
		
		JMenuItem mct3 = createMenuItem("Other files", "", null, new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				//GUIManager.getDefaultGUIManager().generateMCT();
			}
		});
		mct3.setEnabled(false);
		mctSubMenu.add(mct3);
		
		JMenu netMenu = new JMenu("Network Tools");
		this.add(netMenu);
		netMenu.add(createMenuItem("Show TPN transitions", "", null, new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				GUIManager.getDefaultGUIManager().io.markTransitions(0);
			}
		}));
		netMenu.add(createMenuItem("Show DPN transitions", "", null, new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				GUIManager.getDefaultGUIManager().io.markTransitions(1);
			}
		}));
		netMenu.add(createMenuItem("Show TPN/DPN transitions", "", null, new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				GUIManager.getDefaultGUIManager().io.markTransitions(2);
			}
		}));
		
		netMenu.add(createMenuItem("Fix Snoopy compatibility", "", null, new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				GUIManager.getDefaultGUIManager().subnetsHQ.checkSnoopyCompatibility();
			}
		}));
		
	}
	
	@SuppressWarnings("unused")
	/**
	 * Metoda eksportująca rysunek sieci do pliku - dla klikniętego arkusza.
	 */
	private void exportToFile() {
		JFileChooser fc = new JFileChooser();
		FileFilter pngFilter = new ExtensionFileFilter(
				".png - Portable Network Graphics", new String[] { "png" });
		FileFilter bmpFilter = new ExtensionFileFilter(
				".bmp -  Bitmap Image File", new String[] { "bmp" });
		FileFilter jpegFilter = new ExtensionFileFilter(
				".jpeg - JPEG Image File", new String[] { "jpeg" });
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
				ImageIO.write(image, ext.substring(1),new File(file.getPath() + ext));
				GUIManager.getDefaultGUIManager().log("Network image save to file "+file.getPath() + ext, "text", true);
			} catch (IOException ex) {
				ex.printStackTrace();
				GUIManager.getDefaultGUIManager().log("Error: " + ex.getMessage(), "error", true);
			}
		}
	}
}
