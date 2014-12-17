package abyss.graphpanel.popupmenu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import abyss.darkgui.GUIManager;
import abyss.graphpanel.GraphPanel;
import abyss.workspace.ExtensionFileFilter;

@SuppressWarnings("serial")
public class SheetPopupMenu extends GraphPanelPopupMenu {

	public SheetPopupMenu(GraphPanel graphPanel) {
		super(graphPanel);

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

		this.addMenuItem("Refresh", "refresh", new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				getGraphPanel().invalidate();
				getGraphPanel().repaint();
			}
		});

		this.addMenuItem("Save to image file", "picture_save",
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
		analMenu.add(createMenuItem("Generate Invariants (INA)", "generateINA", null, new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				GUIManager.getDefaultGUIManager().generateINAinvariants();
				//getGraphPanel().setZoom(30, getGraphPanel().getZoom());
			}
		}));
		analMenu.add(createMenuItem("Generate PN MCT sets", "generateMCT", null, new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				GUIManager.getDefaultGUIManager().generateMCT();
			}
		}));
		JMenu mctSubMenu = new JMenu("MCT Advanced Generation");
		analMenu.add(mctSubMenu);
		
		mctSubMenu.add(createMenuItem("Simple MCT file", "", null, new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				//GUIManager.getDefaultGUIManager().generateMCT();
			}
		}));
		
		mctSubMenu.add(createMenuItem("Tex output file", "", null, new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				//GUIManager.getDefaultGUIManager().generateMCT();
			}
		}));
		
		mctSubMenu.add(createMenuItem("Other file", "", null, new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				//GUIManager.getDefaultGUIManager().generateMCT();
			}
		}));
	}
	
	@SuppressWarnings("unused")
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
				ImageIO.write(image, ext.substring(1),
						new File(file.getPath() + ext));
			} catch (IOException ex) {
				ex.printStackTrace();

			}
		}
	}
}
