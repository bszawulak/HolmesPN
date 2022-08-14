package holmes.windows.clusters;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.Serial;
import java.util.ArrayList;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import holmes.darkgui.GUIManager;
import holmes.utilities.Tools;

public class HolmesClusterConfig extends JFrame {
	@Serial
	private static final long serialVersionUID = 1694133455242675169L;

	private ArrayList<String> commandsValidate;
	private HolmesClusters boss;

	private JCheckBox corAvgCB;
	private JCheckBox corCentrCB;
	private JCheckBox corComplCB;
	private JCheckBox corMcQCB;
	private JCheckBox corMedCB;
	private JCheckBox corSingCB;
	private JCheckBox corWardCB;
	
	private JCheckBox pearAvgCB;
	private JCheckBox pearCentrCB;
	private JCheckBox pearComplCB;
	private JCheckBox pearMcQCB;
	private JCheckBox pearMedCB;
	private JCheckBox pearSingCB;
	private JCheckBox pearWardCB;
	
	private JCheckBox binAvgCB;
	private JCheckBox binCentrCB;
	private JCheckBox binComplCB;
	private JCheckBox binMcQCB;
	private JCheckBox binMedCB;
	private JCheckBox binSingCB;
	private JCheckBox binWardCB;
	
	private JCheckBox canAvgCB;
	private JCheckBox canCentrCB;
	private JCheckBox canComplCB;
	private JCheckBox canMcQCB;
	private JCheckBox canMedCB;
	private JCheckBox canSingCB;
	private JCheckBox canWardCB;
	
	private JCheckBox eucAvgCB;
	private JCheckBox eucCentrCB;
	private JCheckBox eucComplCB;
	private JCheckBox eucMcQCB;
	private JCheckBox eucMedCB;
	private JCheckBox eucSingCB;
	private JCheckBox eucWardCB;
	
	private JCheckBox manAvgCB;
	private JCheckBox manCentrCB;
	private JCheckBox manComplCB;
	private JCheckBox manMcQCB;
	private JCheckBox manMedCB;
	private JCheckBox manSingCB;
	private JCheckBox manWardCB;
	
	private JCheckBox maxAvgCB;
	private JCheckBox maxCentrCB;
	private JCheckBox maxComplCB;
	private JCheckBox maxMcQCB;
	private JCheckBox maxMedCB;
	private JCheckBox maxSingCB;
	private JCheckBox maxWardCB;
	
	private JCheckBox minAvgCB;
	private JCheckBox minCentrCB;
	private JCheckBox minComplCB;
	private JCheckBox minMcQCB;
	private JCheckBox minMedCB;
	private JCheckBox minSingCB;
	private JCheckBox minWardCB;
	
	/**
	 * Główny konstruktor domyślny okna notatnika.
	 */
	public HolmesClusterConfig(ArrayList<String> comm, HolmesClusters parent) {
		try {
			setIconImage(Tools.getImageFromIcon("/icons/holmesicon.png"));
		} catch (Exception ex) {
			GUIManager.getDefaultGUIManager().log("Error (656756737) | Exception:  "+ex.getMessage(), "error", true);
		}
		commandsValidate = comm;
		boss = parent;
		
		setVisible(false);
		this.setTitle("Clustering config");
		
		setLayout(new BorderLayout());
		setSize(new Dimension(1050, 300));
		setLocation(15, 15);
		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

		JPanel mainPanel = createMainPanel();
		add(mainPanel, BorderLayout.CENTER);
		
		initiateListeners();
	}
	
	/**
	 * Metoda tworząca główny panel okna.
	 * @return JPanel - panel główny
	 */
	private JPanel createMainPanel() {
		JPanel panel = new JPanel(new BorderLayout());

		//Panel wyboru opcji szukania
		JPanel upPanel = createUpPanel(0, 0, 1050, 50);
		JPanel bottomPanel = createBottomPanel(0, 130, 1050, 250);
		
		panel.add(upPanel, BorderLayout.NORTH);
		panel.add(bottomPanel, BorderLayout.CENTER);
		panel.repaint();
		return panel;
	}

	/**
	 * Metoda tworząca górny panel przycisków.
	 * @param x int - współrzędna x
	 * @param y int - współrzędna y
	 * @param width int - szerokość panelu
	 * @param height int - wysokość panelu
	 * @return JPanel - panel przycisków
	 */
	@SuppressWarnings("SameParameterValue")
	private JPanel createUpPanel(int x, int y, int width, int height) {
		JPanel panel = new JPanel();
		panel.setLayout(null);
		panel.setBorder(BorderFactory.createTitledBorder("Search options"));
		//panel.setBounds(x, y, width, height);
		panel.setLocation(x, y);
		panel.setPreferredSize(new Dimension(width, height));
		
		int posX = 10;
		int posY = 20;
		
		JButton cleanAllCB = new JButton("Clean All");
		cleanAllCB.setBounds(posX, posY, 140, 20);
		cleanAllCB.addActionListener(actionEvent -> {
			commandsValidate.clear();
			updateComponents();
		});
		cleanAllCB.setSelected(true);
		panel.add(cleanAllCB);
		
		JButton setAllCB = new JButton("Set All");
		setAllCB.setBounds(posX+150, posY, 140, 20);
		setAllCB.addActionListener(actionEvent -> {
			commandsAddAll();
			updateComponents();
		});
		setAllCB.setSelected(true);
		panel.add(setAllCB);
		
		JButton pearsonsCB = new JButton("PearsonOnly");
		pearsonsCB.setBounds(posX+300, posY, 140, 20);
		pearsonsCB.addActionListener(actionEvent -> {
			selectPearsonOnly();
			updateComponents();
		});
		pearsonsCB.setSelected(true);
		panel.add(pearsonsCB);
		
		return panel;
	}
	
	/**
	 * Metoda tworząca górny panel przycisków.
	 * @param x int - współrzędna x
	 * @param y int - współrzędna y
	 * @param width int - szerokość panelu
	 * @param height int - wysokość panelu
	 * @return JPanel - panel przycisków
	 */
	@SuppressWarnings("SameParameterValue")
	private JPanel createBottomPanel(int x, int y, int width, int height) {
		JPanel panel = new JPanel();
		panel.setLayout(null);
		panel.setBorder(BorderFactory.createTitledBorder("Cluster options"));
		//panel.setBounds(x, y, width, height);
		panel.setLocation(x, y);
		panel.setPreferredSize(new Dimension(width, height));
		
		int posX = 10;
		int posY = 20;
		
		corAvgCB = new JCheckBox("CorPear-Average");
		corAvgCB.setBounds(posX, posY, 140, 20);
		corAvgCB.addActionListener(actionEvent -> {
			AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
			if (abstractButton.getModel().isSelected()) {
				if(!commandsValidate.contains("\"correlation\",\"average\""))
					commandsValidate.add("\"correlation\",\"average\"");
			} else {
				commandsValidate.remove("\"correlation\",\"average\"");
			}
		});
		corAvgCB.setSelected(true);
		panel.add(corAvgCB);
		
		corCentrCB = new JCheckBox("CorPear-Centroid");
		corCentrCB.setBounds(posX+140, posY, 140, 20);
		corCentrCB.addActionListener(actionEvent -> {
			AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
			if (abstractButton.getModel().isSelected()) {
				if(!commandsValidate.contains("\"correlation\",\"centroid\""))
					commandsValidate.add("\"correlation\",\"centroid\"");
			} else {
				commandsValidate.remove("\"correlation\",\"centroid\"");
			}
		});
		corCentrCB.setSelected(true);
		panel.add(corCentrCB);
		
		corComplCB = new JCheckBox("CorPear-Complete");
		corComplCB.setBounds(posX+280, posY, 140, 20);
		corComplCB.addActionListener(actionEvent -> {
			AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
			if (abstractButton.getModel().isSelected()) {
				if(!commandsValidate.contains("\"correlation\",\"complete\""))
					commandsValidate.add("\"correlation\",\"complete\"");
			} else {
				commandsValidate.remove("\"correlation\",\"complete\"");
			}
		});
		corComplCB.setSelected(true);
		panel.add(corComplCB);
		
		corMcQCB = new JCheckBox("CorPear-McQuitty");
		corMcQCB.setBounds(posX+420, posY, 140, 20);
		corMcQCB.addActionListener(actionEvent -> {
			AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
			if (abstractButton.getModel().isSelected()) {
				if(!commandsValidate.contains("\"correlation\",\"mcquitty\""))
					commandsValidate.add("\"correlation\",\"mcquitty\"");
			} else {
				commandsValidate.remove("\"correlation\",\"mcquitty\"");
			}
		});
		corMcQCB.setSelected(true);
		panel.add(corMcQCB);
		
		corMedCB = new JCheckBox("CorPear-Median");
		corMedCB.setBounds(posX+560, posY, 140, 20);
		corMedCB.addActionListener(actionEvent -> {
			AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
			if (abstractButton.getModel().isSelected()) {
				if(!commandsValidate.contains("\"correlation\",\"median\""))
					commandsValidate.add("\"correlation\",\"median\"");
			} else {
				commandsValidate.remove("\"correlation\",\"median\"");
			}
		});
		corMedCB.setSelected(true);
		panel.add(corMedCB);
		
		corSingCB = new JCheckBox("CorPear-Single");
		corSingCB.setBounds(posX+700, posY, 140, 20);
		corSingCB.addActionListener(actionEvent -> {
			AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
			if (abstractButton.getModel().isSelected()) {
				if(!commandsValidate.contains("\"correlation\",\"single\""))
					commandsValidate.add("\"correlation\",\"single\"");
			} else {
				commandsValidate.remove("\"correlation\",\"single\"");
			}
		});
		corSingCB.setSelected(true);
		panel.add(corSingCB);
		
		corWardCB = new JCheckBox("CorPear-Ward");
		corWardCB.setBounds(posX+840, posY, 140, 20);
		corWardCB.addActionListener(actionEvent -> {
			AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
			if (abstractButton.getModel().isSelected()) {
				if(!commandsValidate.contains("\"correlation\",\"ward\""))
					commandsValidate.add("\"correlation\",\"ward\"");
			} else {
				commandsValidate.remove("\"correlation\",\"ward\"");
			}
		});
		corWardCB.setSelected(true);
		panel.add(corWardCB);
		
		//-------------------------------------------
		posY+=20;
		
		pearAvgCB = new JCheckBox("Pearson-Average");
		pearAvgCB.setBounds(posX, posY, 140, 20);
		pearAvgCB.addActionListener(actionEvent -> {
			AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
			if (abstractButton.getModel().isSelected()) {
				if(!commandsValidate.contains("\"pearson\",\"average\""))
					commandsValidate.add("\"pearson\",\"average\"");
			} else {
				commandsValidate.remove("\"pearson\",\"average\"");
			}
		});
		pearAvgCB.setSelected(true);
		panel.add(pearAvgCB);
		
		pearCentrCB = new JCheckBox("Pearson-Centroid");
		pearCentrCB.setBounds(posX+140, posY, 140, 20);
		pearCentrCB.addActionListener(actionEvent -> {
			AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
			if (abstractButton.getModel().isSelected()) {
				if(!commandsValidate.contains("\"pearson\",\"centroid\""))
					commandsValidate.add("\"pearson\",\"centroid\"");
			} else {
				commandsValidate.remove("\"pearson\",\"centroid\"");
			}
		});
		pearCentrCB.setSelected(true);
		panel.add(pearCentrCB);
		
		pearComplCB = new JCheckBox("Pearson-Complete");
		pearComplCB.setBounds(posX+280, posY, 140, 20);
		pearComplCB.addActionListener(actionEvent -> {
			AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
			if (abstractButton.getModel().isSelected()) {
				if(!commandsValidate.contains("\"pearson\",\"complete\""))
					commandsValidate.add("\"pearson\",\"complete\"");
			} else {
				commandsValidate.remove("\"pearson\",\"complete\"");
			}
		});
		pearComplCB.setSelected(true);
		panel.add(pearComplCB);
		
		pearMcQCB = new JCheckBox("Pearson-McQuitty");
		pearMcQCB.setBounds(posX+420, posY, 140, 20);
		pearMcQCB.addActionListener(actionEvent -> {
			AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
			if (abstractButton.getModel().isSelected()) {
				if(!commandsValidate.contains("\"pearson\",\"mcquitty\""))
					commandsValidate.add("\"pearson\",\"mcquitty\"");
			} else {
				commandsValidate.remove("\"pearson\",\"mcquitty\"");
			}
		});
		pearMcQCB.setSelected(true);
		panel.add(pearMcQCB);
		
		pearMedCB = new JCheckBox("Pearson-Median");
		pearMedCB.setBounds(posX+560, posY, 140, 20);
		pearMedCB.addActionListener(actionEvent -> {
			AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
			if (abstractButton.getModel().isSelected()) {
				if(!commandsValidate.contains("\"pearson\",\"median\""))
					commandsValidate.add("\"pearson\",\"median\"");
			} else {
				commandsValidate.remove("\"pearson\",\"median\"");
			}
		});
		pearMedCB.setSelected(true);
		panel.add(pearMedCB);
		
		pearSingCB = new JCheckBox("Pearson-Single");
		pearSingCB.setBounds(posX+700, posY, 140, 20);
		pearSingCB.addActionListener(actionEvent -> {
			AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
			if (abstractButton.getModel().isSelected()) {
				if(!commandsValidate.contains("\"pearson\",\"single\""))
					commandsValidate.add("\"pearson\",\"single\"");
			} else {
				commandsValidate.remove("\"pearson\",\"single\"");
			}
		});
		pearSingCB.setSelected(true);
		panel.add(pearSingCB);
		
		pearWardCB = new JCheckBox("Pearson-Ward");
		pearWardCB.setBounds(posX+840, posY, 140, 20);
		pearWardCB.addActionListener(actionEvent -> {
			AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
			if (abstractButton.getModel().isSelected()) {
				if(!commandsValidate.contains("\"pearson\",\"ward\""))
					commandsValidate.add("\"pearson\",\"ward\"");
			} else {
				commandsValidate.remove("\"pearson\",\"ward\"");
			}
		});
		pearWardCB.setSelected(true);
		panel.add(pearWardCB);
		
		//---
		//-------------------------------------------
		posY+=20;
		
		binAvgCB = new JCheckBox("Binary-Average");
		binAvgCB.setBounds(posX, posY, 140, 20);
		binAvgCB.addActionListener(actionEvent -> {
			AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
			if (abstractButton.getModel().isSelected()) {
				if(!commandsValidate.contains("\"binary\",\"average\""))
					commandsValidate.add("\"binary\",\"average\"");
			} else {
				commandsValidate.remove("\"binary\",\"average\"");
			}
		});
		binAvgCB.setSelected(true);
		panel.add(binAvgCB);
		
		binCentrCB = new JCheckBox("Binary-Centroid");
		binCentrCB.setBounds(posX+140, posY, 140, 20);
		binCentrCB.addActionListener(actionEvent -> {
			AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
			if (abstractButton.getModel().isSelected()) {
				if(!commandsValidate.contains("\"binary\",\"centroid\""))
					commandsValidate.add("\"binary\",\"centroid\"");
			} else {
				commandsValidate.remove("\"binary\",\"centroid\"");
			}
		});
		binCentrCB.setSelected(true);
		panel.add(binCentrCB);
		
		binComplCB = new JCheckBox("Binary-Complete");
		binComplCB.setBounds(posX+280, posY, 140, 20);
		binComplCB.addActionListener(actionEvent -> {
			AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
			if (abstractButton.getModel().isSelected()) {
				if(!commandsValidate.contains("\"binary\",\"complete\""))
					commandsValidate.add("\"binary\",\"complete\"");
			} else {
				commandsValidate.remove("\"binary\",\"complete\"");
			}
		});
		binComplCB.setSelected(true);
		panel.add(binComplCB);
		
		binMcQCB = new JCheckBox("Binary-McQuitty");
		binMcQCB.setBounds(posX+420, posY, 140, 20);
		binMcQCB.addActionListener(actionEvent -> {
			AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
			if (abstractButton.getModel().isSelected()) {
				if(!commandsValidate.contains("\"binary\",\"mcquitty\""))
					commandsValidate.add("\"binary\",\"mcquitty\"");
			} else {
				commandsValidate.remove("\"binary\",\"mcquitty\"");
			}
		});
		binMcQCB.setSelected(true);
		panel.add(binMcQCB);
		
		binMedCB = new JCheckBox("Binary-Median");
		binMedCB.setBounds(posX+560, posY, 140, 20);
		binMedCB.addActionListener(actionEvent -> {
			AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
			if (abstractButton.getModel().isSelected()) {
				if(!commandsValidate.contains("\"binary\",\"median\""))
					commandsValidate.add("\"binary\",\"median\"");
			} else {
				commandsValidate.remove("\"binary\",\"median\"");
			}
		});
		binMedCB.setSelected(true);
		panel.add(binMedCB);
		
		binSingCB = new JCheckBox("Binary-Single");
		binSingCB.setBounds(posX+700, posY, 140, 20);
		binSingCB.addActionListener(actionEvent -> {
			AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
			if (abstractButton.getModel().isSelected()) {
				if(!commandsValidate.contains("\"binary\",\"single\""))
					commandsValidate.add("\"binary\",\"single\"");
			} else {
				commandsValidate.remove("\"binary\",\"single\"");
			}
		});
		binSingCB.setSelected(true);
		panel.add(binSingCB);
		
		binWardCB = new JCheckBox("Binary-Ward");
		binWardCB.setBounds(posX+840, posY, 140, 20);
		binWardCB.addActionListener(actionEvent -> {
			AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
			if (abstractButton.getModel().isSelected()) {
				if(!commandsValidate.contains("\"binary\",\"ward.D\""))
					commandsValidate.add("\"binary\",\"ward.D\"");
			} else {
				commandsValidate.remove("\"binary\",\"ward.D\"");
			}
		});
		binWardCB.setSelected(true);
		panel.add(binWardCB);
		
		//-----------------------------
		posY+=20;
		
		canAvgCB = new JCheckBox("Canberra-Average");
		canAvgCB.setBounds(posX, posY, 140, 20);
		canAvgCB.addActionListener(actionEvent -> {
			AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
			if (abstractButton.getModel().isSelected()) {
				if(!commandsValidate.contains("\"canberra\",\"average\""))
					commandsValidate.add("\"canberra\",\"average\"");
			} else {
				commandsValidate.remove("\"canberra\",\"average\"");
			}
		});
		canAvgCB.setSelected(true);
		panel.add(canAvgCB);
		
		canCentrCB = new JCheckBox("Canberra-Centroid");
		canCentrCB.setBounds(posX+140, posY, 140, 20);
		canCentrCB.addActionListener(actionEvent -> {
			AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
			if (abstractButton.getModel().isSelected()) {
				if(!commandsValidate.contains("\"canberra\",\"centroid\""))
					commandsValidate.add("\"canberra\",\"centroid\"");
			} else {
				commandsValidate.remove("\"canberra\",\"centroid\"");
			}
		});
		canCentrCB.setSelected(true);
		panel.add(canCentrCB);
		
		canComplCB = new JCheckBox("Canberra-Complete");
		canComplCB.setBounds(posX+280, posY, 140, 20);
		canComplCB.addActionListener(actionEvent -> {
			AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
			if (abstractButton.getModel().isSelected()) {
				if(!commandsValidate.contains("\"canberra\",\"complete\""))
					commandsValidate.add("\"canberra\",\"complete\"");
			} else {
				commandsValidate.remove("\"canberra\",\"complete\"");
			}
		});
		canComplCB.setSelected(true);
		panel.add(canComplCB);
		
		canMcQCB = new JCheckBox("Canberra-McQuitty");
		canMcQCB.setBounds(posX+420, posY, 140, 20);
		canMcQCB.addActionListener(actionEvent -> {
			AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
			if (abstractButton.getModel().isSelected()) {
				if(!commandsValidate.contains("\"canberra\",\"mcquitty\""))
					commandsValidate.add("\"canberra\",\"mcquitty\"");
			} else {
				commandsValidate.remove("\"canberra\",\"mcquitty\"");
			}
		});
		canMcQCB.setSelected(true);
		panel.add(canMcQCB);
		
		canMedCB = new JCheckBox("Canberra-Median");
		canMedCB.setBounds(posX+560, posY, 140, 20);
		canMedCB.addActionListener(actionEvent -> {
			AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
			if (abstractButton.getModel().isSelected()) {
				if(!commandsValidate.contains("\"canberra\",\"median\""))
					commandsValidate.add("\"canberra\",\"median\"");
			} else {
				commandsValidate.remove("\"canberra\",\"median\"");
			}
		});
		canMedCB.setSelected(true);
		panel.add(canMedCB);
		
		canSingCB = new JCheckBox("Canberra-Single");
		canSingCB.setBounds(posX+700, posY, 140, 20);
		canSingCB.addActionListener(actionEvent -> {
			AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
			if (abstractButton.getModel().isSelected()) {
				if(!commandsValidate.contains("\"canberra\",\"single\""))
					commandsValidate.add("\"canberra\",\"single\"");
			} else {
				commandsValidate.remove("\"canberra\",\"single\"");
			}
		});
		canSingCB.setSelected(true);
		panel.add(canSingCB);
		
		canWardCB = new JCheckBox("Canberra-Ward");
		canWardCB.setBounds(posX+840, posY, 140, 20);
		canWardCB.addActionListener(actionEvent -> {
			AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
			if (abstractButton.getModel().isSelected()) {
				if(!commandsValidate.contains("\"canberra\",\"ward.D\""))
					commandsValidate.add("\"canberra\",\"ward.D\"");
			} else {
				commandsValidate.remove("\"canberra\",\"ward.D\"");
			}
		});
		canWardCB.setSelected(true);
		panel.add(canWardCB);
		
		//--------------
		posY+=20;
		
		eucAvgCB = new JCheckBox("Euclidean-Average");
		eucAvgCB.setBounds(posX, posY, 140, 20);
		eucAvgCB.addActionListener(actionEvent -> {
			AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
			if (abstractButton.getModel().isSelected()) {
				if(!commandsValidate.contains("\"euclidean\",\"average\""))
					commandsValidate.add("\"euclidean\",\"average\"");
			} else {
				commandsValidate.remove("\"euclidean\",\"average\"");
			}
		});
		eucAvgCB.setSelected(true);
		panel.add(eucAvgCB);
		
		eucCentrCB = new JCheckBox("Euclidean-Centroid");
		eucCentrCB.setBounds(posX+140, posY, 140, 20);
		eucCentrCB.addActionListener(actionEvent -> {
			AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
			if (abstractButton.getModel().isSelected()) {
				if(!commandsValidate.contains("\"euclidean\",\"centroid\""))
					commandsValidate.add("\"euclidean\",\"centroid\"");
			} else {
				commandsValidate.remove("\"euclidean\",\"centroid\"");
			}
		});
		eucCentrCB.setSelected(true);
		panel.add(eucCentrCB);
		
		eucComplCB = new JCheckBox("Euclidean-Complete");
		eucComplCB.setBounds(posX+280, posY, 140, 20);
		eucComplCB.addActionListener(actionEvent -> {
			AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
			if (abstractButton.getModel().isSelected()) {
				if(!commandsValidate.contains("\"euclidean\",\"complete\""))
					commandsValidate.add("\"euclidean\",\"complete\"");
			} else {
				commandsValidate.remove("\"euclidean\",\"complete\"");
			}
		});
		eucComplCB.setSelected(true);
		panel.add(eucComplCB);
		
		eucMcQCB = new JCheckBox("Euclidean-McQuitty");
		eucMcQCB.setBounds(posX+420, posY, 140, 20);
		eucMcQCB.addActionListener(actionEvent -> {
			AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
			if (abstractButton.getModel().isSelected()) {
				if(!commandsValidate.contains("\"euclidean\",\"mcquitty\""))
					commandsValidate.add("\"euclidean\",\"mcquitty\"");
			} else {
				commandsValidate.remove("\"euclidean\",\"mcquitty\"");
			}
		});
		eucMcQCB.setSelected(true);
		panel.add(eucMcQCB);
		
		eucMedCB = new JCheckBox("Euclidean-Median");
		eucMedCB.setBounds(posX+560, posY, 140, 20);
		eucMedCB.addActionListener(actionEvent -> {
			AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
			if (abstractButton.getModel().isSelected()) {
				if(!commandsValidate.contains("\"euclidean\",\"median\""))
					commandsValidate.add("\"euclidean\",\"median\"");
			} else {
				commandsValidate.remove("\"euclidean\",\"median\"");
			}
		});
		eucMedCB.setSelected(true);
		panel.add(eucMedCB);
		
		eucSingCB = new JCheckBox("Euclidean-Single");
		eucSingCB.setBounds(posX+700, posY, 140, 20);
		eucSingCB.addActionListener(actionEvent -> {
			AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
			if (abstractButton.getModel().isSelected()) {
				if(!commandsValidate.contains("\"euclidean\",\"single\""))
					commandsValidate.add("\"euclidean\",\"single\"");
			} else {
				commandsValidate.remove("\"euclidean\",\"single\"");
			}
		});
		eucSingCB.setSelected(true);
		panel.add(eucSingCB);
		
		eucWardCB = new JCheckBox("Euclidean-Ward");
		eucWardCB.setBounds(posX+840, posY, 140, 20);
		eucWardCB.addActionListener(actionEvent -> {
			AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
			if (abstractButton.getModel().isSelected()) {
				if(!commandsValidate.contains("\"euclidean\",\"ward.D\""))
					commandsValidate.add("\"euclidean\",\"ward.D\"");
			} else {
				commandsValidate.remove("\"euclidean\",\"ward.D\"");
			}
		});
		eucWardCB.setSelected(true);
		panel.add(eucWardCB);
		
		//-----------------
		posY+=20;
		
		manAvgCB = new JCheckBox("Manhattan-Average");
		manAvgCB.setBounds(posX, posY, 140, 20);
		manAvgCB.addActionListener(actionEvent -> {
			AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
			if (abstractButton.getModel().isSelected()) {
				if(!commandsValidate.contains("\"manhattan\",\"average\""))
					commandsValidate.add("\"manhattan\",\"average\"");
			} else {
				commandsValidate.remove("\"manhattan\",\"average\"");
			}
		});
		manAvgCB.setSelected(true);
		panel.add(manAvgCB);
		
		manCentrCB = new JCheckBox("Manhattan-Centroid");
		manCentrCB.setBounds(posX+140, posY, 140, 20);
		manCentrCB.addActionListener(actionEvent -> {
			AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
			if (abstractButton.getModel().isSelected()) {
				if(!commandsValidate.contains("\"manhattan\",\"centroid\""))
					commandsValidate.add("\"manhattan\",\"centroid\"");
			} else {
				commandsValidate.remove("\"manhattan\",\"centroid\"");
			}
		});
		manCentrCB.setSelected(true);
		panel.add(manCentrCB);
		
		manComplCB = new JCheckBox("Manhattan-Complete");
		manComplCB.setBounds(posX+280, posY, 140, 20);
		manComplCB.addActionListener(actionEvent -> {
			AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
			if (abstractButton.getModel().isSelected()) {
				if(!commandsValidate.contains("\"manhattan\",\"complete\""))
					commandsValidate.add("\"manhattan\",\"complete\"");
			} else {
				commandsValidate.remove("\"manhattan\",\"complete\"");
			}
		});
		manComplCB.setSelected(true);
		panel.add(manComplCB);
		
		manMcQCB = new JCheckBox("Manhattan-McQuitty");
		manMcQCB.setBounds(posX+420, posY, 140, 20);
		manMcQCB.addActionListener(actionEvent -> {
			AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
			if (abstractButton.getModel().isSelected()) {
				if(!commandsValidate.contains("\"manhattan\",\"mcquitty\""))
					commandsValidate.add("\"manhattan\",\"mcquitty\"");
			} else {
				commandsValidate.remove("\"manhattan\",\"mcquitty\"");
			}
		});
		manMcQCB.setSelected(true);
		panel.add(manMcQCB);
		
		manMedCB = new JCheckBox("Manhattan-Median");
		manMedCB.setBounds(posX+560, posY, 140, 20);
		manMedCB.addActionListener(actionEvent -> {
			AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
			if (abstractButton.getModel().isSelected()) {
				if(!commandsValidate.contains("\"manhattan\",\"median\""))
					commandsValidate.add("\"manhattan\",\"median\"");
			} else {
				commandsValidate.remove("\"manhattan\",\"median\"");
			}
		});
		manMedCB.setSelected(true);
		panel.add(manMedCB);
		
		manSingCB = new JCheckBox("Manhattan-Single");
		manSingCB.setBounds(posX+700, posY, 140, 20);
		manSingCB.addActionListener(actionEvent -> {
			AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
			if (abstractButton.getModel().isSelected()) {
				if(!commandsValidate.contains("\"manhattan\",\"single\""))
					commandsValidate.add("\"manhattan\",\"single\"");
			} else {
				commandsValidate.remove("\"manhattan\",\"single\"");
			}
		});
		manSingCB.setSelected(true);
		panel.add(manSingCB);
		
		manWardCB = new JCheckBox("Manhattan-Ward");
		manWardCB.setBounds(posX+840, posY, 140, 20);
		manWardCB.addActionListener(actionEvent -> {
			AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
			if (abstractButton.getModel().isSelected()) {
				if(!commandsValidate.contains("\"manhattan\",\"ward.D\""))
					commandsValidate.add("\"manhattan\",\"ward.D\"");
			} else {
				commandsValidate.remove("\"manhattan\",\"ward.D\"");
			}
		});
		manWardCB.setSelected(true);
		panel.add(manWardCB);
		
		//-------------
		
		posY+=20;
		
		maxAvgCB = new JCheckBox("Maximum-Average");
		maxAvgCB.setBounds(posX, posY, 140, 20);
		maxAvgCB.addActionListener(actionEvent -> {
			AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
			if (abstractButton.getModel().isSelected()) {
				if(!commandsValidate.contains("\"maximum\",\"average\""))
					commandsValidate.add("\"maximum\",\"average\"");
			} else {
				commandsValidate.remove("\"maximum\",\"average\"");
			}
		});
		maxAvgCB.setSelected(true);
		panel.add(maxAvgCB);
		
		maxCentrCB = new JCheckBox("Maximum-Centroid");
		maxCentrCB.setBounds(posX+140, posY, 140, 20);
		maxCentrCB.addActionListener(actionEvent -> {
			AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
			if (abstractButton.getModel().isSelected()) {
				if(!commandsValidate.contains("\"maximum\",\"centroid\""))
					commandsValidate.add("\"maximum\",\"centroid\"");
			} else {
				commandsValidate.remove("\"maximum\",\"centroid\"");
			}
		});
		maxCentrCB.setSelected(true);
		panel.add(maxCentrCB);
		
		maxComplCB = new JCheckBox("Maximum-Complete");
		maxComplCB.setBounds(posX+280, posY, 140, 20);
		maxComplCB.addActionListener(actionEvent -> {
			AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
			if (abstractButton.getModel().isSelected()) {
				if(!commandsValidate.contains("\"maximum\",\"complete\""))
					commandsValidate.add("\"maximum\",\"complete\"");
			} else {
				commandsValidate.remove("\"maximum\",\"complete\"");
			}
		});
		maxComplCB.setSelected(true);
		panel.add(maxComplCB);
		
		maxMcQCB = new JCheckBox("Maximum-McQuitty");
		maxMcQCB.setBounds(posX+420, posY, 140, 20);
		maxMcQCB.addActionListener(actionEvent -> {
			AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
			if (abstractButton.getModel().isSelected()) {
				if(!commandsValidate.contains("\"maximum\",\"mcquitty\""))
					commandsValidate.add("\"maximum\",\"mcquitty\"");
			} else {
				commandsValidate.remove("\"maximum\",\"mcquitty\"");
			}
		});
		maxMcQCB.setSelected(true);
		panel.add(maxMcQCB);
		
		maxMedCB = new JCheckBox("Maximum-Median");
		maxMedCB.setBounds(posX+560, posY, 140, 20);
		maxMedCB.addActionListener(actionEvent -> {
			AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
			if (abstractButton.getModel().isSelected()) {
				if(!commandsValidate.contains("\"maximum\",\"median\""))
					commandsValidate.add("\"maximum\",\"median\"");
			} else {
				commandsValidate.remove("\"maximum\",\"median\"");
			}
		});
		maxMedCB.setSelected(true);
		panel.add(maxMedCB);
		
		maxSingCB = new JCheckBox("Maximum-Single");
		maxSingCB.setBounds(posX+700, posY, 140, 20);
		maxSingCB.addActionListener(actionEvent -> {
			AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
			if (abstractButton.getModel().isSelected()) {
				if(!commandsValidate.contains("\"maximum\",\"single\""))
					commandsValidate.add("\"maximum\",\"single\"");
			} else {
				commandsValidate.remove("\"maximum\",\"single\"");
			}
		});
		maxSingCB.setSelected(true);
		panel.add(maxSingCB);
		
		maxWardCB = new JCheckBox("Maximum-Ward");
		maxWardCB.setBounds(posX+840, posY, 140, 20);
		maxWardCB.addActionListener(actionEvent -> {
			AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
			if (abstractButton.getModel().isSelected()) {
				if(!commandsValidate.contains("\"maximum\",\"ward.D\""))
					commandsValidate.add("\"maximum\",\"ward.D\"");
			} else {
				commandsValidate.remove("\"maximum\",\"ward.D\"");
			}
		});
		maxWardCB.setSelected(true);
		panel.add(maxWardCB);
		
		//--------------
		
		posY+=20;
		
		minAvgCB = new JCheckBox("Minkowski-Average");
		minAvgCB.setBounds(posX, posY, 140, 20);
		minAvgCB.addActionListener(actionEvent -> {
			AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
			if (abstractButton.getModel().isSelected()) {
				if(!commandsValidate.contains("\"minkowski\",\"average\""))
					commandsValidate.add("\"minkowski\",\"average\"");
			} else {
				commandsValidate.remove("\"minkowski\",\"average\"");
			}
		});
		minAvgCB.setSelected(true);
		panel.add(minAvgCB);
		
		minCentrCB = new JCheckBox("Minkowski-Centroid");
		minCentrCB.setBounds(posX+140, posY, 140, 20);
		minCentrCB.addActionListener(actionEvent -> {
			AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
			if (abstractButton.getModel().isSelected()) {
				if(!commandsValidate.contains("\"minkowski\",\"centroid\""))
					commandsValidate.add("\"minkowski\",\"centroid\"");
			} else {
				commandsValidate.remove("\"minkowski\",\"centroid\"");
			}
		});
		minCentrCB.setSelected(true);
		panel.add(minCentrCB);
		
		minComplCB = new JCheckBox("Minkowski-Complete");
		minComplCB.setBounds(posX+280, posY, 140, 20);
		minComplCB.addActionListener(actionEvent -> {
			AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
			if (abstractButton.getModel().isSelected()) {
				if(!commandsValidate.contains("\"minkowski\",\"complete\""))
					commandsValidate.add("\"minkowski\",\"complete\"");
			} else {
				commandsValidate.remove("\"minkowski\",\"complete\"");
			}
		});
		minComplCB.setSelected(true);
		panel.add(minComplCB);
		
		minMcQCB = new JCheckBox("Minkowski-McQuitty");
		minMcQCB.setBounds(posX+420, posY, 140, 20);
		minMcQCB.addActionListener(actionEvent -> {
			AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
			if (abstractButton.getModel().isSelected()) {
				if(!commandsValidate.contains("\"minkowski\",\"mcquitty\""))
					commandsValidate.add("\"minkowski\",\"mcquitty\"");
			} else {
				commandsValidate.remove("\"minkowski\",\"mcquitty\"");
			}
		});
		minMcQCB.setSelected(true);
		panel.add(minMcQCB);
		
		minMedCB = new JCheckBox("Minkowski-Median");
		minMedCB.setBounds(posX+560, posY, 140, 20);
		minMedCB.addActionListener(actionEvent -> {
			AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
			if (abstractButton.getModel().isSelected()) {
				if(!commandsValidate.contains("\"minkowski\",\"median\""))
					commandsValidate.add("\"minkowski\",\"median\"");
			} else {
				commandsValidate.remove("\"minkowski\",\"median\"");
			}
		});
		minMedCB.setSelected(true);
		panel.add(minMedCB);
		
		minSingCB = new JCheckBox("Minkowski-Single");
		minSingCB.setBounds(posX+700, posY, 140, 20);
		minSingCB.addActionListener(actionEvent -> {
			AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
			if (abstractButton.getModel().isSelected()) {
				if(!commandsValidate.contains("\"minkowski\",\"single\""))
					commandsValidate.add("\"minkowski\",\"single\"");
			} else {
				commandsValidate.remove("\"minkowski\",\"single\"");
			}
		});
		minSingCB.setSelected(true);
		panel.add(minSingCB);
		
		minWardCB = new JCheckBox("Minkowski-Ward");
		minWardCB.setBounds(posX+840, posY, 140, 20);
		minWardCB.addActionListener(actionEvent -> {
			AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
			if (abstractButton.getModel().isSelected()) {
				if(!commandsValidate.contains("\"minkowski\",\"ward.D\""))
					commandsValidate.add("\"minkowski\",\"ward.D\"");
			} else {
				commandsValidate.remove("\"minkowski\",\"ward.D\"");
			}
		});
		minWardCB.setSelected(true);
		panel.add(minWardCB);
		
		
		return panel;
	}
	
	/**
	 * Inicjalizacja agentów nasłuchujących.
	 */
    private void initiateListeners() { //HAIL SITHIS
    	addWindowListener(new WindowAdapter() {
  	  	    public void windowActivated(WindowEvent e) {
  	  	    	boss.setEnabled(false);
  	  	    	GUIManager.getDefaultGUIManager().getFrame().setEnabled(false);
  	  	    	updateComponents();
  	  	    }  
    	});
    	
    	addWindowListener(new java.awt.event.WindowAdapter() {
		    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
		    	if(commandsValidate.size() == 0) {
		    		JOptionPane.showMessageDialog(null, "At least one clustering method must be chosen."
		    				+ "\nSelecting CorrelatedPearson-Average(UPGMA)","Problem", JOptionPane.WARNING_MESSAGE);
		    	}
		    	commandsValidate.add("\"correlation\",\"average\"");
		    	
		    	boss.setEnabled(true);
		    	GUIManager.getDefaultGUIManager().getFrame().setEnabled(true);
		    }
		});
    }
    
    private void updateComponents() {
		corAvgCB.setSelected(commandsValidate.contains("\"correlation\",\"average\""));
		corCentrCB.setSelected(commandsValidate.contains("\"correlation\",\"centroid\""));
		corComplCB.setSelected(commandsValidate.contains("\"correlation\",\"complete\""));
		corMcQCB.setSelected(commandsValidate.contains("\"correlation\",\"mcquitty\""));
		corMedCB.setSelected(commandsValidate.contains("\"correlation\",\"median\""));
		corSingCB.setSelected(commandsValidate.contains("\"correlation\",\"single\""));
		corWardCB.setSelected(commandsValidate.contains("\"correlation\",\"ward\""));
    	//---
		pearAvgCB.setSelected(commandsValidate.contains("\"pearson\",\"average\""));
		pearCentrCB.setSelected(commandsValidate.contains("\"pearson\",\"centroid\""));
		pearComplCB.setSelected(commandsValidate.contains("\"pearson\",\"complete\""));
		pearMcQCB.setSelected(commandsValidate.contains("\"pearson\",\"mcquitty\""));
		pearMedCB.setSelected(commandsValidate.contains("\"pearson\",\"median\""));
		pearSingCB.setSelected(commandsValidate.contains("\"pearson\",\"single\""));
		pearWardCB.setSelected(commandsValidate.contains("\"pearson\",\"ward\""));
    	//------
		binAvgCB.setSelected(commandsValidate.contains("\"binary\",\"average\""));
		binCentrCB.setSelected(commandsValidate.contains("\"binary\",\"centroid\""));
		binComplCB.setSelected(commandsValidate.contains("\"binary\",\"complete\""));
		binMcQCB.setSelected(commandsValidate.contains("\"binary\",\"mcquitty\""));
		binMedCB.setSelected(commandsValidate.contains("\"binary\",\"median\""));
		binSingCB.setSelected(commandsValidate.contains("\"binary\",\"single\""));
		binWardCB.setSelected(commandsValidate.contains("\"binary\",\"ward.D\""));
    	//-----
		canAvgCB.setSelected(commandsValidate.contains("\"canberra\",\"average\""));
		canCentrCB.setSelected(commandsValidate.contains("\"canberra\",\"centroid\""));
		canComplCB.setSelected(commandsValidate.contains("\"canberra\",\"complete\""));
		canMcQCB.setSelected(commandsValidate.contains("\"canberra\",\"mcquitty\""));
		canMedCB.setSelected(commandsValidate.contains("\"canberra\",\"median\""));
		canSingCB.setSelected(commandsValidate.contains("\"canberra\",\"single\""));
		canWardCB.setSelected(commandsValidate.contains("\"canberra\",\"ward.D\""));
    	//---
		eucAvgCB.setSelected(commandsValidate.contains("\"euclidean\",\"average\""));
		eucCentrCB.setSelected(commandsValidate.contains("\"euclidean\",\"centroid\""));
		eucComplCB.setSelected(commandsValidate.contains("\"euclidean\",\"complete\""));
		eucMcQCB.setSelected(commandsValidate.contains("\"euclidean\",\"mcquitty\""));
		eucMedCB.setSelected(commandsValidate.contains("\"euclidean\",\"median\""));
		eucSingCB.setSelected(commandsValidate.contains("\"euclidean\",\"single\""));
		eucWardCB.setSelected(commandsValidate.contains("\"euclidean\",\"ward.D\""));
    	//----
		manAvgCB.setSelected(commandsValidate.contains("\"manhattan\",\"average\""));
		manCentrCB.setSelected(commandsValidate.contains("\"manhattan\",\"centroid\""));
		manComplCB.setSelected(commandsValidate.contains("\"manhattan\",\"complete\""));
		manMcQCB.setSelected(commandsValidate.contains("\"manhattan\",\"mcquitty\""));
		manMedCB.setSelected(commandsValidate.contains("\"manhattan\",\"median\""));
		manSingCB.setSelected(commandsValidate.contains("\"manhattan\",\"single\""));
		manWardCB.setSelected(commandsValidate.contains("\"manhattan\",\"ward.D\""));
    	//----
		maxAvgCB.setSelected(commandsValidate.contains("\"maximum\",\"average\""));
		maxCentrCB.setSelected(commandsValidate.contains("\"maximum\",\"centroid\""));
		maxComplCB.setSelected(commandsValidate.contains("\"maximum\",\"complete\""));
		maxMcQCB.setSelected(commandsValidate.contains("\"maximum\",\"mcquitty\""));
		maxMedCB.setSelected(commandsValidate.contains("\"maximum\",\"median\""));
		maxSingCB.setSelected(commandsValidate.contains("\"maximum\",\"single\""));
		maxWardCB.setSelected(commandsValidate.contains("\"maximum\",\"ward.D\""));
    	//---
		minAvgCB.setSelected(commandsValidate.contains("\"minkowski\",\"average\""));
		minCentrCB.setSelected(commandsValidate.contains("\"minkowski\",\"centroid\""));
		minComplCB.setSelected(commandsValidate.contains("\"minkowski\",\"complete\""));
		minMcQCB.setSelected(commandsValidate.contains("\"minkowski\",\"mcquitty\""));
		minMedCB.setSelected(commandsValidate.contains("\"minkowski\",\"median\""));
		minSingCB.setSelected(commandsValidate.contains("\"minkowski\",\"single\""));
		minWardCB.setSelected(commandsValidate.contains("\"minkowski\",\"ward.D\""));
    }
    
    private void commandsAddAll() {
    	commandsValidate.add("\"correlation\",\"average\"");
    	commandsValidate.add("\"correlation\",\"centroid\"");
    	commandsValidate.add("\"correlation\",\"complete\"");
    	commandsValidate.add("\"correlation\",\"mcquitty\"");
    	commandsValidate.add("\"correlation\",\"median\"");
    	commandsValidate.add("\"correlation\",\"single\"");
    	commandsValidate.add("\"correlation\",\"ward\"");
    	
    	commandsValidate.add("\"pearson\",\"average\"");
    	commandsValidate.add("\"pearson\",\"centroid\"");
    	commandsValidate.add("\"pearson\",\"complete\"");
    	commandsValidate.add("\"pearson\",\"mcquitty\"");
    	commandsValidate.add("\"pearson\",\"median\"");
    	commandsValidate.add("\"pearson\",\"single\"");
    	commandsValidate.add("\"pearson\",\"ward\"");
    	
    	commandsValidate.add("\"binary\",\"average\"");
    	commandsValidate.add("\"binary\",\"centroid\"");
    	commandsValidate.add("\"binary\",\"complete\"");
    	commandsValidate.add("\"binary\",\"mcquitty\"");
    	commandsValidate.add("\"binary\",\"median\"");
    	commandsValidate.add("\"binary\",\"single\"");
    	commandsValidate.add("\"binary\",\"ward.D\"");
    	
    	commandsValidate.add("\"canberra\",\"average\"");
    	commandsValidate.add("\"canberra\",\"centroid\"");
    	commandsValidate.add("\"canberra\",\"complete\"");
    	commandsValidate.add("\"canberra\",\"mcquitty\"");
    	commandsValidate.add("\"canberra\",\"median\"");
    	commandsValidate.add("\"canberra\",\"single\"");
    	commandsValidate.add("\"canberra\",\"ward.D\"");
    	
    	commandsValidate.add("\"euclidean\",\"average\"");
    	commandsValidate.add("\"euclidean\",\"centroid\"");
    	commandsValidate.add("\"euclidean\",\"complete\"");
    	commandsValidate.add("\"euclidean\",\"mcquitty\"");
    	commandsValidate.add("\"euclidean\",\"median\"");
    	commandsValidate.add("\"euclidean\",\"single\"");
    	commandsValidate.add("\"euclidean\",\"ward.D\"");
    	
    	commandsValidate.add("\"manhattan\",\"average\"");
    	commandsValidate.add("\"manhattan\",\"centroid\"");
    	commandsValidate.add("\"manhattan\",\"complete\"");
    	commandsValidate.add("\"manhattan\",\"mcquitty\"");
    	commandsValidate.add("\"manhattan\",\"median\"");
    	commandsValidate.add("\"manhattan\",\"single\"");
    	commandsValidate.add("\"manhattan\",\"ward.D\"");
    	
    	commandsValidate.add("\"maximum\",\"average\"");
    	commandsValidate.add("\"maximum\",\"centroid\"");
    	commandsValidate.add("\"maximum\",\"complete\"");
    	commandsValidate.add("\"maximum\",\"mcquitty\"");
    	commandsValidate.add("\"maximum\",\"median\"");
    	commandsValidate.add("\"maximum\",\"single\"");
    	commandsValidate.add("\"maximum\",\"ward.D\"");
    	
    	commandsValidate.add("\"minkowski\",\"average\"");
    	commandsValidate.add("\"minkowski\",\"centroid\"");
    	commandsValidate.add("\"minkowski\",\"complete\"");
    	commandsValidate.add("\"minkowski\",\"mcquitty\"");
    	commandsValidate.add("\"minkowski\",\"median\"");
    	commandsValidate.add("\"minkowski\",\"single\"");
    	commandsValidate.add("\"minkowski\",\"ward.D\"");
    }
    
    private void selectPearsonOnly() {
    	commandsValidate.clear();
    	
    	commandsValidate.add("\"correlation\",\"average\"");
    	commandsValidate.add("\"correlation\",\"centroid\"");
    	commandsValidate.add("\"correlation\",\"complete\"");
    	commandsValidate.add("\"correlation\",\"mcquitty\"");
    	commandsValidate.add("\"correlation\",\"median\"");
    	commandsValidate.add("\"correlation\",\"single\"");
    	commandsValidate.add("\"correlation\",\"ward\"");
    	
    	commandsValidate.add("\"pearson\",\"average\"");
    	commandsValidate.add("\"pearson\",\"centroid\"");
    	commandsValidate.add("\"pearson\",\"complete\"");
    	commandsValidate.add("\"pearson\",\"mcquitty\"");
    	commandsValidate.add("\"pearson\",\"median\"");
    	commandsValidate.add("\"pearson\",\"single\"");
    	commandsValidate.add("\"pearson\",\"ward\"");
    }
    
    
}
