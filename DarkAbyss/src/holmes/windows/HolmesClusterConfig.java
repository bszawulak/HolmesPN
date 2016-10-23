package holmes.windows;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import holmes.darkgui.GUIManager;
import holmes.utilities.Tools;

public class HolmesClusterConfig extends JFrame {
	private static final long serialVersionUID = 1694133455242675169L;

	private ArrayList<String> commandsValidate;
	private HolmesClusters boss;
	
	private JPanel mainPanel;
	private JPanel upPanel;
	private JPanel bottomPanel;
	
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
		} catch (Exception e ) {
			
		}
		commandsValidate = comm;
		boss = parent;
		boss.setEnabled(false);
		
		setVisible(true);
		this.setTitle("Clustering config");
		
		setLayout(new BorderLayout());
		setSize(new Dimension(1050, 300));
		setLocation(15, 15);
		
		mainPanel = createMainPanel();
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
		upPanel = createUpPanel(0, 0, 1050, 50);
		bottomPanel = createBottomPanel(0, 130, 1050, 250);
		
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
		cleanAllCB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				commandsValidate.clear();
				updateComponents();
			}
		});
		cleanAllCB.setSelected(true);
		panel.add(cleanAllCB);
		
		JButton setAllCB = new JButton("Set All");
		setAllCB.setBounds(posX+150, posY, 140, 20);
		setAllCB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				commandsAddAll();
				updateComponents();
			}
		});
		setAllCB.setSelected(true);
		panel.add(setAllCB);
		
		JButton pearsonsCB = new JButton("PearsonOnly");
		pearsonsCB.setBounds(posX+300, posY, 140, 20);
		pearsonsCB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				pearsonsOnly();
			}
		});
		pearsonsCB.setSelected(true);
		panel.add(pearsonsCB);
		
		JButton aaa = new JButton("Test");
		aaa.setBounds(posX+450, posY, 140, 20);
		aaa.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				//pearsonsOnly();
				int x;
				x=1;
			}
		});
		aaa.setSelected(true);
		panel.add(aaa);
		
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
		corAvgCB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
				if (abstractButton.getModel().isSelected()) {
					if(!commandsValidate.contains("\"correlation\",\"average\""))
						commandsValidate.add("\"correlation\",\"average\"");
				} else {
					commandsValidate.remove("\"correlation\",\"average\"");
				}
			}
		});
		corAvgCB.setSelected(true);
		panel.add(corAvgCB);
		
		corCentrCB = new JCheckBox("CorPear-Centroid");
		corCentrCB.setBounds(posX+140, posY, 140, 20);
		corCentrCB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
				if (abstractButton.getModel().isSelected()) {
					if(!commandsValidate.contains("\"correlation\",\"centroid\""))
						commandsValidate.add("\"correlation\",\"centroid\"");
				} else {
					commandsValidate.remove("\"correlation\",\"centroid\"");
				}
			}
		});
		corCentrCB.setSelected(true);
		panel.add(corCentrCB);
		
		corComplCB = new JCheckBox("CorPear-Complete");
		corComplCB.setBounds(posX+280, posY, 140, 20);
		corComplCB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
				if (abstractButton.getModel().isSelected()) {
					if(!commandsValidate.contains("\"correlation\",\"complete\""))
						commandsValidate.add("\"correlation\",\"complete\"");
				} else {
					commandsValidate.remove("\"correlation\",\"complete\"");
				}
			}
		});
		corComplCB.setSelected(true);
		panel.add(corComplCB);
		
		corMcQCB = new JCheckBox("CorPear-McQuitty");
		corMcQCB.setBounds(posX+420, posY, 140, 20);
		corMcQCB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
				if (abstractButton.getModel().isSelected()) {
					if(!commandsValidate.contains("\"correlation\",\"mcquitty\""))
						commandsValidate.add("\"correlation\",\"mcquitty\"");
				} else {
					commandsValidate.remove("\"correlation\",\"mcquitty\"");
				}
			}
		});
		corMcQCB.setSelected(true);
		panel.add(corMcQCB);
		
		corMedCB = new JCheckBox("CorPear-Median");
		corMedCB.setBounds(posX+560, posY, 140, 20);
		corMedCB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
				if (abstractButton.getModel().isSelected()) {
					if(!commandsValidate.contains("\"correlation\",\"median\""))
						commandsValidate.add("\"correlation\",\"median\"");
				} else {
					commandsValidate.remove("\"correlation\",\"median\"");
				}
			}
		});
		corMedCB.setSelected(true);
		panel.add(corMedCB);
		
		corSingCB = new JCheckBox("CorPear-Single");
		corSingCB.setBounds(posX+700, posY, 140, 20);
		corSingCB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
				if (abstractButton.getModel().isSelected()) {
					if(!commandsValidate.contains("\"correlation\",\"single\""))
						commandsValidate.add("\"correlation\",\"single\"");
				} else {
					commandsValidate.remove("\"correlation\",\"single\"");
				}
			}
		});
		corSingCB.setSelected(true);
		panel.add(corSingCB);
		
		corWardCB = new JCheckBox("CorPear-Ward");
		corWardCB.setBounds(posX+840, posY, 140, 20);
		corWardCB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
				if (abstractButton.getModel().isSelected()) {
					if(!commandsValidate.contains("\"correlation\",\"ward\""))
						commandsValidate.add("\"correlation\",\"ward\"");
				} else {
					commandsValidate.remove("\"correlation\",\"ward\"");
				}
			}
		});
		corWardCB.setSelected(true);
		panel.add(corWardCB);
		
		//-------------------------------------------
		posY+=20;
		
		pearAvgCB = new JCheckBox("Pearson-Average");
		pearAvgCB.setBounds(posX, posY, 140, 20);
		pearAvgCB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
				if (abstractButton.getModel().isSelected()) {
					if(!commandsValidate.contains("\"pearson\",\"average\""))
						commandsValidate.add("\"pearson\",\"average\"");
				} else {
					commandsValidate.remove("\"pearson\",\"average\"");
				}
			}
		});
		pearAvgCB.setSelected(true);
		panel.add(pearAvgCB);
		
		pearCentrCB = new JCheckBox("Pearson-Centroid");
		pearCentrCB.setBounds(posX+140, posY, 140, 20);
		pearCentrCB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
				if (abstractButton.getModel().isSelected()) {
					if(!commandsValidate.contains("\"pearson\",\"centroid\""))
						commandsValidate.add("\"pearson\",\"centroid\"");
				} else {
					commandsValidate.remove("\"pearson\",\"centroid\"");
				}
			}
		});
		pearCentrCB.setSelected(true);
		panel.add(pearCentrCB);
		
		pearComplCB = new JCheckBox("Pearson-Complete");
		pearComplCB.setBounds(posX+280, posY, 140, 20);
		pearComplCB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
				if (abstractButton.getModel().isSelected()) {
					if(!commandsValidate.contains("\"pearson\",\"complete\""))
						commandsValidate.add("\"pearson\",\"complete\"");
				} else {
					commandsValidate.remove("\"pearson\",\"complete\"");
				}
			}
		});
		pearComplCB.setSelected(true);
		panel.add(pearComplCB);
		
		pearMcQCB = new JCheckBox("Pearson-McQuitty");
		pearMcQCB.setBounds(posX+420, posY, 140, 20);
		pearMcQCB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
				if (abstractButton.getModel().isSelected()) {
					if(!commandsValidate.contains("\"pearson\",\"mcquitty\""))
						commandsValidate.add("\"pearson\",\"mcquitty\"");
				} else {
					commandsValidate.remove("\"pearson\",\"mcquitty\"");
				}
			}
		});
		pearMcQCB.setSelected(true);
		panel.add(pearMcQCB);
		
		pearMedCB = new JCheckBox("Pearson-Median");
		pearMedCB.setBounds(posX+560, posY, 140, 20);
		pearMedCB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
				if (abstractButton.getModel().isSelected()) {
					if(!commandsValidate.contains("\"pearson\",\"median\""))
						commandsValidate.add("\"pearson\",\"median\"");
				} else {
					commandsValidate.remove("\"pearson\",\"median\"");
				}
			}
		});
		pearMedCB.setSelected(true);
		panel.add(pearMedCB);
		
		pearSingCB = new JCheckBox("Pearson-Single");
		pearSingCB.setBounds(posX+700, posY, 140, 20);
		pearSingCB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
				if (abstractButton.getModel().isSelected()) {
					if(!commandsValidate.contains("\"pearson\",\"single\""))
						commandsValidate.add("\"pearson\",\"single\"");
				} else {
					commandsValidate.remove("\"pearson\",\"single\"");
				}
			}
		});
		pearSingCB.setSelected(true);
		panel.add(pearSingCB);
		
		pearWardCB = new JCheckBox("Pearson-Ward");
		pearWardCB.setBounds(posX+840, posY, 140, 20);
		pearWardCB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
				if (abstractButton.getModel().isSelected()) {
					if(!commandsValidate.contains("\"pearson\",\"ward\""))
						commandsValidate.add("\"pearson\",\"ward\"");
				} else {
					commandsValidate.remove("\"pearson\",\"ward\"");
				}
			}
		});
		pearWardCB.setSelected(true);
		panel.add(pearWardCB);
		
		//---
		//-------------------------------------------
		posY+=20;
		
		binAvgCB = new JCheckBox("Binary-Average");
		binAvgCB.setBounds(posX, posY, 140, 20);
		binAvgCB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
				if (abstractButton.getModel().isSelected()) {
					if(!commandsValidate.contains("\"binary\",\"average\""))
						commandsValidate.add("\"binary\",\"average\"");
				} else {
					commandsValidate.remove("\"binary\",\"average\"");
				}
			}
		});
		binAvgCB.setSelected(true);
		panel.add(binAvgCB);
		
		binCentrCB = new JCheckBox("Binary-Centroid");
		binCentrCB.setBounds(posX+140, posY, 140, 20);
		binCentrCB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
				if (abstractButton.getModel().isSelected()) {
					if(!commandsValidate.contains("\"binary\",\"centroid\""))
						commandsValidate.add("\"binary\",\"centroid\"");
				} else {
					commandsValidate.remove("\"binary\",\"centroid\"");
				}
			}
		});
		binCentrCB.setSelected(true);
		panel.add(binCentrCB);
		
		binComplCB = new JCheckBox("Binary-Complete");
		binComplCB.setBounds(posX+280, posY, 140, 20);
		binComplCB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
				if (abstractButton.getModel().isSelected()) {
					if(!commandsValidate.contains("\"binary\",\"complete\""))
						commandsValidate.add("\"binary\",\"complete\"");
				} else {
					commandsValidate.remove("\"binary\",\"complete\"");
				}
			}
		});
		binComplCB.setSelected(true);
		panel.add(binComplCB);
		
		binMcQCB = new JCheckBox("Binary-McQuitty");
		binMcQCB.setBounds(posX+420, posY, 140, 20);
		binMcQCB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
				if (abstractButton.getModel().isSelected()) {
					if(!commandsValidate.contains("\"binary\",\"mcquitty\""))
						commandsValidate.add("\"binary\",\"mcquitty\"");
				} else {
					commandsValidate.remove("\"binary\",\"mcquitty\"");
				}
			}
		});
		binMcQCB.setSelected(true);
		panel.add(binMcQCB);
		
		binMedCB = new JCheckBox("Binary-Median");
		binMedCB.setBounds(posX+560, posY, 140, 20);
		binMedCB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
				if (abstractButton.getModel().isSelected()) {
					if(!commandsValidate.contains("\"binary\",\"median\""))
						commandsValidate.add("\"binary\",\"median\"");
				} else {
					commandsValidate.remove("\"binary\",\"median\"");
				}
			}
		});
		binMedCB.setSelected(true);
		panel.add(binMedCB);
		
		binSingCB = new JCheckBox("Binary-Single");
		binSingCB.setBounds(posX+700, posY, 140, 20);
		binSingCB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
				if (abstractButton.getModel().isSelected()) {
					if(!commandsValidate.contains("\"binary\",\"single\""))
						commandsValidate.add("\"binary\",\"single\"");
				} else {
					commandsValidate.remove("\"binary\",\"single\"");
				}
			}
		});
		binSingCB.setSelected(true);
		panel.add(binSingCB);
		
		binWardCB = new JCheckBox("Binary-Ward");
		binWardCB.setBounds(posX+840, posY, 140, 20);
		binWardCB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
				if (abstractButton.getModel().isSelected()) {
					if(!commandsValidate.contains("\"binary\",\"ward.D\""))
						commandsValidate.add("\"binary\",\"ward.D\"");
				} else {
					commandsValidate.remove("\"binary\",\"ward.D\"");
				}
			}
		});
		binWardCB.setSelected(true);
		panel.add(binWardCB);
		
		//-----------------------------
		posY+=20;
		
		canAvgCB = new JCheckBox("Canberra-Average");
		canAvgCB.setBounds(posX, posY, 140, 20);
		canAvgCB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
				if (abstractButton.getModel().isSelected()) {
					if(!commandsValidate.contains("\"canberra\",\"average\""))
						commandsValidate.add("\"canberra\",\"average\"");
				} else {
					commandsValidate.remove("\"canberra\",\"average\"");
				}
			}
		});
		canAvgCB.setSelected(true);
		panel.add(canAvgCB);
		
		canCentrCB = new JCheckBox("Canberra-Centroid");
		canCentrCB.setBounds(posX+140, posY, 140, 20);
		canCentrCB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
				if (abstractButton.getModel().isSelected()) {
					if(!commandsValidate.contains("\"canberra\",\"centroid\""))
						commandsValidate.add("\"canberra\",\"centroid\"");
				} else {
					commandsValidate.remove("\"canberra\",\"centroid\"");
				}
			}
		});
		canCentrCB.setSelected(true);
		panel.add(canCentrCB);
		
		canComplCB = new JCheckBox("Canberra-Complete");
		canComplCB.setBounds(posX+280, posY, 140, 20);
		canComplCB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
				if (abstractButton.getModel().isSelected()) {
					if(!commandsValidate.contains("\"canberra\",\"complete\""))
						commandsValidate.add("\"canberra\",\"complete\"");
				} else {
					commandsValidate.remove("\"canberra\",\"complete\"");
				}
			}
		});
		canComplCB.setSelected(true);
		panel.add(canComplCB);
		
		canMcQCB = new JCheckBox("Canberra-McQuitty");
		canMcQCB.setBounds(posX+420, posY, 140, 20);
		canMcQCB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
				if (abstractButton.getModel().isSelected()) {
					if(!commandsValidate.contains("\"canberra\",\"mcquitty\""))
						commandsValidate.add("\"canberra\",\"mcquitty\"");
				} else {
					commandsValidate.remove("\"canberra\",\"mcquitty\"");
				}
			}
		});
		canMcQCB.setSelected(true);
		panel.add(canMcQCB);
		
		canMedCB = new JCheckBox("Canberra-Median");
		canMedCB.setBounds(posX+560, posY, 140, 20);
		canMedCB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
				if (abstractButton.getModel().isSelected()) {
					if(!commandsValidate.contains("\"canberra\",\"median\""))
						commandsValidate.add("\"canberra\",\"median\"");
				} else {
					commandsValidate.remove("\"canberra\",\"median\"");
				}
			}
		});
		canMedCB.setSelected(true);
		panel.add(canMedCB);
		
		canSingCB = new JCheckBox("Canberra-Single");
		canSingCB.setBounds(posX+700, posY, 140, 20);
		canSingCB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
				if (abstractButton.getModel().isSelected()) {
					if(!commandsValidate.contains("\"canberra\",\"single\""))
						commandsValidate.add("\"canberra\",\"single\"");
				} else {
					commandsValidate.remove("\"canberra\",\"single\"");
				}
			}
		});
		canSingCB.setSelected(true);
		panel.add(canSingCB);
		
		canWardCB = new JCheckBox("Canberra-Ward");
		canWardCB.setBounds(posX+840, posY, 140, 20);
		canWardCB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
				if (abstractButton.getModel().isSelected()) {
					if(!commandsValidate.contains("\"canberra\",\"ward.D\""))
						commandsValidate.add("\"canberra\",\"ward.D\"");
				} else {
					commandsValidate.remove("\"canberra\",\"ward.D\"");
				}
			}
		});
		canWardCB.setSelected(true);
		panel.add(canWardCB);
		
		//--------------
		posY+=20;
		
		eucAvgCB = new JCheckBox("Euclidean-Average");
		eucAvgCB.setBounds(posX, posY, 140, 20);
		eucAvgCB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
				if (abstractButton.getModel().isSelected()) {
					if(!commandsValidate.contains("\"euclidean\",\"average\""))
						commandsValidate.add("\"euclidean\",\"average\"");
				} else {
					commandsValidate.remove("\"euclidean\",\"average\"");
				}
			}
		});
		eucAvgCB.setSelected(true);
		panel.add(eucAvgCB);
		
		eucCentrCB = new JCheckBox("Euclidean-Centroid");
		eucCentrCB.setBounds(posX+140, posY, 140, 20);
		eucCentrCB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
				if (abstractButton.getModel().isSelected()) {
					if(!commandsValidate.contains("\"euclidean\",\"centroid\""))
						commandsValidate.add("\"euclidean\",\"centroid\"");
				} else {
					commandsValidate.remove("\"euclidean\",\"centroid\"");
				}
			}
		});
		eucCentrCB.setSelected(true);
		panel.add(eucCentrCB);
		
		eucComplCB = new JCheckBox("Euclidean-Complete");
		eucComplCB.setBounds(posX+280, posY, 140, 20);
		eucComplCB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
				if (abstractButton.getModel().isSelected()) {
					if(!commandsValidate.contains("\"euclidean\",\"complete\""))
						commandsValidate.add("\"euclidean\",\"complete\"");
				} else {
					commandsValidate.remove("\"euclidean\",\"complete\"");
				}
			}
		});
		eucComplCB.setSelected(true);
		panel.add(eucComplCB);
		
		eucMcQCB = new JCheckBox("Euclidean-McQuitty");
		eucMcQCB.setBounds(posX+420, posY, 140, 20);
		eucMcQCB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
				if (abstractButton.getModel().isSelected()) {
					if(!commandsValidate.contains("\"euclidean\",\"mcquitty\""))
						commandsValidate.add("\"euclidean\",\"mcquitty\"");
				} else {
					commandsValidate.remove("\"euclidean\",\"mcquitty\"");
				}
			}
		});
		eucMcQCB.setSelected(true);
		panel.add(eucMcQCB);
		
		eucMedCB = new JCheckBox("Euclidean-Median");
		eucMedCB.setBounds(posX+560, posY, 140, 20);
		eucMedCB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
				if (abstractButton.getModel().isSelected()) {
					if(!commandsValidate.contains("\"euclidean\",\"median\""))
						commandsValidate.add("\"euclidean\",\"median\"");
				} else {
					commandsValidate.remove("\"euclidean\",\"median\"");
				}
			}
		});
		eucMedCB.setSelected(true);
		panel.add(eucMedCB);
		
		eucSingCB = new JCheckBox("Euclidean-Single");
		eucSingCB.setBounds(posX+700, posY, 140, 20);
		eucSingCB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
				if (abstractButton.getModel().isSelected()) {
					if(!commandsValidate.contains("\"euclidean\",\"single\""))
						commandsValidate.add("\"euclidean\",\"single\"");
				} else {
					commandsValidate.remove("\"euclidean\",\"single\"");
				}
			}
		});
		eucSingCB.setSelected(true);
		panel.add(eucSingCB);
		
		eucWardCB = new JCheckBox("Euclidean-Ward");
		eucWardCB.setBounds(posX+840, posY, 140, 20);
		eucWardCB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
				if (abstractButton.getModel().isSelected()) {
					if(!commandsValidate.contains("\"euclidean\",\"ward.D\""))
						commandsValidate.add("\"euclidean\",\"ward.D\"");
				} else {
					commandsValidate.remove("\"euclidean\",\"ward.D\"");
				}
			}
		});
		eucWardCB.setSelected(true);
		panel.add(eucWardCB);
		
		//-----------------
		posY+=20;
		
		manAvgCB = new JCheckBox("Manhattan-Average");
		manAvgCB.setBounds(posX, posY, 140, 20);
		manAvgCB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
				if (abstractButton.getModel().isSelected()) {
					if(!commandsValidate.contains("\"manhattan\",\"average\""))
						commandsValidate.add("\"manhattan\",\"average\"");
				} else {
					commandsValidate.remove("\"manhattan\",\"average\"");
				}
			}
		});
		manAvgCB.setSelected(true);
		panel.add(manAvgCB);
		
		manCentrCB = new JCheckBox("Manhattan-Centroid");
		manCentrCB.setBounds(posX+140, posY, 140, 20);
		manCentrCB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
				if (abstractButton.getModel().isSelected()) {
					if(!commandsValidate.contains("\"manhattan\",\"centroid\""))
						commandsValidate.add("\"manhattan\",\"centroid\"");
				} else {
					commandsValidate.remove("\"manhattan\",\"centroid\"");
				}
			}
		});
		manCentrCB.setSelected(true);
		panel.add(manCentrCB);
		
		manComplCB = new JCheckBox("Manhattan-Complete");
		manComplCB.setBounds(posX+280, posY, 140, 20);
		manComplCB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
				if (abstractButton.getModel().isSelected()) {
					if(!commandsValidate.contains("\"manhattan\",\"complete\""))
						commandsValidate.add("\"manhattan\",\"complete\"");
				} else {
					commandsValidate.remove("\"manhattan\",\"complete\"");
				}
			}
		});
		manComplCB.setSelected(true);
		panel.add(manComplCB);
		
		manMcQCB = new JCheckBox("Manhattan-McQuitty");
		manMcQCB.setBounds(posX+420, posY, 140, 20);
		manMcQCB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
				if (abstractButton.getModel().isSelected()) {
					if(!commandsValidate.contains("\"manhattan\",\"mcquitty\""))
						commandsValidate.add("\"manhattan\",\"mcquitty\"");
				} else {
					commandsValidate.remove("\"manhattan\",\"mcquitty\"");
				}
			}
		});
		manMcQCB.setSelected(true);
		panel.add(manMcQCB);
		
		manMedCB = new JCheckBox("Manhattan-Median");
		manMedCB.setBounds(posX+560, posY, 140, 20);
		manMedCB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
				if (abstractButton.getModel().isSelected()) {
					if(!commandsValidate.contains("\"manhattan\",\"median\""))
						commandsValidate.add("\"manhattan\",\"median\"");
				} else {
					commandsValidate.remove("\"manhattan\",\"median\"");
				}
			}
		});
		manMedCB.setSelected(true);
		panel.add(manMedCB);
		
		manSingCB = new JCheckBox("Manhattan-Single");
		manSingCB.setBounds(posX+700, posY, 140, 20);
		manSingCB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
				if (abstractButton.getModel().isSelected()) {
					if(!commandsValidate.contains("\"manhattan\",\"single\""))
						commandsValidate.add("\"manhattan\",\"single\"");
				} else {
					commandsValidate.remove("\"manhattan\",\"single\"");
				}
			}
		});
		manSingCB.setSelected(true);
		panel.add(manSingCB);
		
		manWardCB = new JCheckBox("Manhattan-Ward");
		manWardCB.setBounds(posX+840, posY, 140, 20);
		manWardCB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
				if (abstractButton.getModel().isSelected()) {
					if(!commandsValidate.contains("\"manhattan\",\"ward.D\""))
						commandsValidate.add("\"manhattan\",\"ward.D\"");
				} else {
					commandsValidate.remove("\"manhattan\",\"ward.D\"");
				}
			}
		});
		manWardCB.setSelected(true);
		panel.add(manWardCB);
		
		//-------------
		
		posY+=20;
		
		maxAvgCB = new JCheckBox("Maximum-Average");
		maxAvgCB.setBounds(posX, posY, 140, 20);
		maxAvgCB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
				if (abstractButton.getModel().isSelected()) {
					if(!commandsValidate.contains("\"maximum\",\"average\""))
						commandsValidate.add("\"maximum\",\"average\"");
				} else {
					commandsValidate.remove("\"maximum\",\"average\"");
				}
			}
		});
		maxAvgCB.setSelected(true);
		panel.add(maxAvgCB);
		
		maxCentrCB = new JCheckBox("Maximum-Centroid");
		maxCentrCB.setBounds(posX+140, posY, 140, 20);
		maxCentrCB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
				if (abstractButton.getModel().isSelected()) {
					if(!commandsValidate.contains("\"maximum\",\"centroid\""))
						commandsValidate.add("\"maximum\",\"centroid\"");
				} else {
					commandsValidate.remove("\"maximum\",\"centroid\"");
				}
			}
		});
		maxCentrCB.setSelected(true);
		panel.add(maxCentrCB);
		
		maxComplCB = new JCheckBox("Maximum-Complete");
		maxComplCB.setBounds(posX+280, posY, 140, 20);
		maxComplCB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
				if (abstractButton.getModel().isSelected()) {
					if(!commandsValidate.contains("\"maximum\",\"complete\""))
						commandsValidate.add("\"maximum\",\"complete\"");
				} else {
					commandsValidate.remove("\"maximum\",\"complete\"");
				}
			}
		});
		maxComplCB.setSelected(true);
		panel.add(maxComplCB);
		
		maxMcQCB = new JCheckBox("Maximum-McQuitty");
		maxMcQCB.setBounds(posX+420, posY, 140, 20);
		maxMcQCB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
				if (abstractButton.getModel().isSelected()) {
					if(!commandsValidate.contains("\"maximum\",\"mcquitty\""))
						commandsValidate.add("\"maximum\",\"mcquitty\"");
				} else {
					commandsValidate.remove("\"maximum\",\"mcquitty\"");
				}
			}
		});
		maxMcQCB.setSelected(true);
		panel.add(maxMcQCB);
		
		maxMedCB = new JCheckBox("Maximum-Median");
		maxMedCB.setBounds(posX+560, posY, 140, 20);
		maxMedCB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
				if (abstractButton.getModel().isSelected()) {
					if(!commandsValidate.contains("\"maximum\",\"median\""))
						commandsValidate.add("\"maximum\",\"median\"");
				} else {
					commandsValidate.remove("\"maximum\",\"median\"");
				}
			}
		});
		maxMedCB.setSelected(true);
		panel.add(maxMedCB);
		
		maxSingCB = new JCheckBox("Maximum-Single");
		maxSingCB.setBounds(posX+700, posY, 140, 20);
		maxSingCB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
				if (abstractButton.getModel().isSelected()) {
					if(!commandsValidate.contains("\"maximum\",\"single\""))
						commandsValidate.add("\"maximum\",\"single\"");
				} else {
					commandsValidate.remove("\"maximum\",\"single\"");
				}
			}
		});
		maxSingCB.setSelected(true);
		panel.add(maxSingCB);
		
		maxWardCB = new JCheckBox("Maximum-Ward");
		maxWardCB.setBounds(posX+840, posY, 140, 20);
		maxWardCB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
				if (abstractButton.getModel().isSelected()) {
					if(!commandsValidate.contains("\"maximum\",\"ward.D\""))
						commandsValidate.add("\"maximum\",\"ward.D\"");
				} else {
					commandsValidate.remove("\"maximum\",\"ward.D\"");
				}
			}
		});
		maxWardCB.setSelected(true);
		panel.add(maxWardCB);
		
		//--------------
		
		posY+=20;
		
		minAvgCB = new JCheckBox("Minkowski-Average");
		minAvgCB.setBounds(posX, posY, 140, 20);
		minAvgCB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
				if (abstractButton.getModel().isSelected()) {
					if(!commandsValidate.contains("\"minkowski\",\"average\""))
						commandsValidate.add("\"minkowski\",\"average\"");
				} else {
					commandsValidate.remove("\"minkowski\",\"average\"");
				}
			}
		});
		minAvgCB.setSelected(true);
		panel.add(minAvgCB);
		
		minCentrCB = new JCheckBox("Minkowski-Centroid");
		minCentrCB.setBounds(posX+140, posY, 140, 20);
		minCentrCB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
				if (abstractButton.getModel().isSelected()) {
					if(!commandsValidate.contains("\"minkowski\",\"centroid\""))
						commandsValidate.add("\"minkowski\",\"centroid\"");
				} else {
					commandsValidate.remove("\"minkowski\",\"centroid\"");
				}
			}
		});
		minCentrCB.setSelected(true);
		panel.add(minCentrCB);
		
		minComplCB = new JCheckBox("Minkowski-Complete");
		minComplCB.setBounds(posX+280, posY, 140, 20);
		minComplCB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
				if (abstractButton.getModel().isSelected()) {
					if(!commandsValidate.contains("\"minkowski\",\"complete\""))
						commandsValidate.add("\"minkowski\",\"complete\"");
				} else {
					commandsValidate.remove("\"minkowski\",\"complete\"");
				}
			}
		});
		minComplCB.setSelected(true);
		panel.add(minComplCB);
		
		minMcQCB = new JCheckBox("Minkowski-McQuitty");
		minMcQCB.setBounds(posX+420, posY, 140, 20);
		minMcQCB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
				if (abstractButton.getModel().isSelected()) {
					if(!commandsValidate.contains("\"minkowski\",\"mcquitty\""))
						commandsValidate.add("\"minkowski\",\"mcquitty\"");
				} else {
					commandsValidate.remove("\"minkowski\",\"mcquitty\"");
				}
			}
		});
		minMcQCB.setSelected(true);
		panel.add(minMcQCB);
		
		minMedCB = new JCheckBox("Minkowski-Median");
		minMedCB.setBounds(posX+560, posY, 140, 20);
		minMedCB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
				if (abstractButton.getModel().isSelected()) {
					if(!commandsValidate.contains("\"minkowski\",\"median\""))
						commandsValidate.add("\"minkowski\",\"median\"");
				} else {
					commandsValidate.remove("\"minkowski\",\"median\"");
				}
			}
		});
		minMedCB.setSelected(true);
		panel.add(minMedCB);
		
		minSingCB = new JCheckBox("Minkowski-Single");
		minSingCB.setBounds(posX+700, posY, 140, 20);
		minSingCB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
				if (abstractButton.getModel().isSelected()) {
					if(!commandsValidate.contains("\"minkowski\",\"single\""))
						commandsValidate.add("\"minkowski\",\"single\"");
				} else {
					commandsValidate.remove("\"minkowski\",\"single\"");
				}
			}
		});
		minSingCB.setSelected(true);
		panel.add(minSingCB);
		
		minWardCB = new JCheckBox("Minkowski-Ward");
		minWardCB.setBounds(posX+840, posY, 140, 20);
		minWardCB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
				if (abstractButton.getModel().isSelected()) {
					if(!commandsValidate.contains("\"minkowski\",\"ward.D\""))
						commandsValidate.add("\"minkowski\",\"ward.D\"");
				} else {
					commandsValidate.remove("\"minkowski\",\"ward.D\"");
				}
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
  	  	    	updateComponents();
  	  	    }  
    	});
    	
    	addWindowListener(new java.awt.event.WindowAdapter() {
		    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
		    	boss.setEnabled(true);
		    	GUIManager.getDefaultGUIManager().getFrame().setEnabled(true);
		    }
		});
    }
    
    private void updateComponents() {
    	if(commandsValidate.contains("\"correlation\",\"average\""))
    		corAvgCB.setSelected(true);
    	else
    		corAvgCB.setSelected(false);
    	
    	if(commandsValidate.contains("\"correlation\",\"centroid\""))
    		corCentrCB.setSelected(true);
    	else
    		corCentrCB.setSelected(false);
    	
    	if(commandsValidate.contains("\"correlation\",\"complete\""))
    		corComplCB.setSelected(true);
    	else
    		corComplCB.setSelected(false);
    	
    	if(commandsValidate.contains("\"correlation\",\"mcquitty\""))
    		corMcQCB.setSelected(true);
    	else
    		corMcQCB.setSelected(false);
    	
    	if(commandsValidate.contains("\"correlation\",\"median\""))
    		corMedCB.setSelected(true);
    	else
    		corMedCB.setSelected(false);
    	
    	if(commandsValidate.contains("\"correlation\",\"single\""))
    		corSingCB.setSelected(true);
    	else
    		corSingCB.setSelected(false);
    	
    	if(commandsValidate.contains("\"correlation\",\"ward\""))
    		corWardCB.setSelected(true);
    	else
    		corWardCB.setSelected(false);
    	
    	//---
    	
    	if(commandsValidate.contains("\"pearson\",\"average\""))
    		pearAvgCB.setSelected(true);
    	else
    		pearAvgCB.setSelected(false);
    	
    	if(commandsValidate.contains("\"pearson\",\"centroid\""))
    		pearCentrCB.setSelected(true);
    	else
    		pearCentrCB.setSelected(false);
    	
    	if(commandsValidate.contains("\"pearson\",\"complete\""))
    		pearComplCB.setSelected(true);
    	else
    		pearComplCB.setSelected(false);
    		
    	if(commandsValidate.contains("\"pearson\",\"mcquitty\""))
    		pearMcQCB.setSelected(true);
    	else
    		pearMcQCB.setSelected(false);
    	
    	if(commandsValidate.contains("\"pearson\",\"median\""))
    		pearMedCB.setSelected(true);
    	else
    		pearMedCB.setSelected(false);
    	
    	if(commandsValidate.contains("\"pearson\",\"single\""))
    		pearSingCB.setSelected(true);
    	else
    		pearSingCB.setSelected(false);
    	
    	if(commandsValidate.contains("\"pearson\",\"ward\""))
    		pearWardCB.setSelected(true);
    	else
    		pearWardCB.setSelected(false);
    	
    	//------
    	if(commandsValidate.contains("\"binary\",\"average\""))
    		binAvgCB.setSelected(true);
    	else
    		binAvgCB.setSelected(false);
    	
    	if(commandsValidate.contains("\"binary\",\"centroid\""))
    		binCentrCB.setSelected(true);
    	else
    		binCentrCB.setSelected(false);
    	
    	if(commandsValidate.contains("\"binary\",\"complete\""))
    		binComplCB.setSelected(true);
    	else
    		binComplCB.setSelected(false);
    		
    	if(commandsValidate.contains("\"binary\",\"mcquitty\""))
    		binMcQCB.setSelected(true);
    	else
    		binMcQCB.setSelected(false);
    	
    	if(commandsValidate.contains("\"binary\",\"median\""))
    		binMedCB.setSelected(true);
    	else
    		binMedCB.setSelected(false);
    	
    	if(commandsValidate.contains("\"binary\",\"single\""))
    		binSingCB.setSelected(true);
    	else
    		binSingCB.setSelected(false);
    	
    	if(commandsValidate.contains("\"binary\",\"ward.D\""))
    		binWardCB.setSelected(true);
    	else
    		binWardCB.setSelected(false);
    	
    	//-----
    	
    	if(commandsValidate.contains("\"canberra\",\"average\""))
    		canAvgCB.setSelected(true);
    	else
    		canAvgCB.setSelected(false);
    	
    	if(commandsValidate.contains("\"canberra\",\"centroid\""))
    		canCentrCB.setSelected(true);
    	else
    		canCentrCB.setSelected(false);
    	
    	if(commandsValidate.contains("\"canberra\",\"complete\""))
    		canComplCB.setSelected(true);
    	else
    		canComplCB.setSelected(false);
    		
    	if(commandsValidate.contains("\"canberra\",\"mcquitty\""))
    		canMcQCB.setSelected(true);
    	else
    		canMcQCB.setSelected(false);
    	
    	if(commandsValidate.contains("\"canberra\",\"median\""))
    		canMedCB.setSelected(true);
    	else
    		canMedCB.setSelected(false);
    	
    	if(commandsValidate.contains("\"canberra\",\"single\""))
    		canSingCB.setSelected(true);
    	else
    		canSingCB.setSelected(false);
    	
    	if(commandsValidate.contains("\"canberra\",\"ward.D\""))
    		canWardCB.setSelected(true);
    	else
    		canWardCB.setSelected(false);
    		
    	//---
    	
    	if(commandsValidate.contains("\"euclidean\",\"average\""))
    		eucAvgCB.setSelected(true);
    	else
    		eucAvgCB.setSelected(false);
    	
    	if(commandsValidate.contains("\"euclidean\",\"centroid\""))
    		eucCentrCB.setSelected(true);
    	else
    		eucCentrCB.setSelected(false);
    	
    	if(commandsValidate.contains("\"euclidean\",\"complete\""))
    		eucComplCB.setSelected(true);
    	else
    		eucComplCB.setSelected(false);
    		
    	if(commandsValidate.contains("\"euclidean\",\"mcquitty\""))
    		eucMcQCB.setSelected(true);
    	else
    		eucMcQCB.setSelected(false);
    	
    	if(commandsValidate.contains("\"euclidean\",\"median\""))
    		eucMedCB.setSelected(true);
    	else
    		eucMedCB.setSelected(false);
    	
    	if(commandsValidate.contains("\"euclidean\",\"single\""))
    		eucSingCB.setSelected(true);
    	else
    		eucSingCB.setSelected(false);
    	
    	if(commandsValidate.contains("\"euclidean\",\"ward.D\""))
    		eucWardCB.setSelected(true);
    	else
    		eucWardCB.setSelected(false);
    		
    	//----
    	
    	if(commandsValidate.contains("\"manhattan\",\"average\""))
    		manAvgCB.setSelected(true);
    	else
    		manAvgCB.setSelected(false);
    	
    	if(commandsValidate.contains("\"manhattan\",\"centroid\""))
    		manCentrCB.setSelected(true);
    	else
    		manCentrCB.setSelected(false);
    	
    	if(commandsValidate.contains("\"manhattan\",\"complete\""))
    		manComplCB.setSelected(true);
    	else
    		manComplCB.setSelected(false);
    		
    	if(commandsValidate.contains("\"manhattan\",\"mcquitty\""))
    		manMcQCB.setSelected(true);
    	else
    		manMcQCB.setSelected(false);
    	
    	if(commandsValidate.contains("\"manhattan\",\"median\""))
    		manMedCB.setSelected(true);
    	else
    		manMedCB.setSelected(false);
    	
    	if(commandsValidate.contains("\"manhattan\",\"single\""))
    		manSingCB.setSelected(true);
    	else
    		manSingCB.setSelected(false);
    	
    	if(commandsValidate.contains("\"manhattan\",\"ward.D\""))
    		manWardCB.setSelected(true);
    	else
    		manWardCB.setSelected(false);
    		
    	//----
    	
    	if(commandsValidate.contains("\"maximum\",\"average\""))
    		maxAvgCB.setSelected(true);
    	else
    		maxAvgCB.setSelected(false);
    	
    	if(commandsValidate.contains("\"maximum\",\"centroid\""))
    		maxCentrCB.setSelected(true);
    	else
    		maxCentrCB.setSelected(false);
    	
    	if(commandsValidate.contains("\"maximum\",\"complete\""))
    		maxComplCB.setSelected(true);
    	else
    		maxComplCB.setSelected(false);
    		
    	if(commandsValidate.contains("\"maximum\",\"mcquitty\""))
    		maxMcQCB.setSelected(true);
    	else
    		maxMcQCB.setSelected(false);
    	
    	if(commandsValidate.contains("\"maximum\",\"median\""))
    		maxMedCB.setSelected(true);
    	else
    		maxMedCB.setSelected(false);
    	
    	if(commandsValidate.contains("\"maximum\",\"single\""))
    		maxSingCB.setSelected(true);
    	else
    		maxSingCB.setSelected(false);
    	
    	if(commandsValidate.contains("\"maximum\",\"ward.D\""))
    		maxWardCB.setSelected(true);
    	else
    		maxWardCB.setSelected(false);
    		
    	//---
    	
    	if(commandsValidate.contains("\"minkowski\",\"average\""))
    		minAvgCB.setSelected(true);
    	else
    		minAvgCB.setSelected(false);
    	
    	if(commandsValidate.contains("\"minkowski\",\"centroid\""))
    		minCentrCB.setSelected(true);
    	else
    		minCentrCB.setSelected(false);
    	
    	if(commandsValidate.contains("\"minkowski\",\"complete\""))
    		minComplCB.setSelected(true);
    	else
    		minComplCB.setSelected(false);
    		
    	if(commandsValidate.contains("\"minkowski\",\"mcquitty\""))
    		minMcQCB.setSelected(true);
    	else
    		minMcQCB.setSelected(false);
    	
    	if(commandsValidate.contains("\"minkowski\",\"median\""))
    		minMedCB.setSelected(true);
    	else
    		minMedCB.setSelected(false);
    	
    	if(commandsValidate.contains("\"minkowski\",\"single\""))
    		minSingCB.setSelected(true);
    	else
    		minSingCB.setSelected(false);
    	
    	if(commandsValidate.contains("\"minkowski\",\"ward.D\""))
    		minWardCB.setSelected(true);
    	else
    		minWardCB.setSelected(false);
    }
    
    private void pearsonsOnly() {
    	corAvgCB.setSelected(true);
    	corCentrCB.setSelected(true);
    	corComplCB.setSelected(true);
    	corMcQCB.setSelected(true);
    	corMedCB.setSelected(true);
    	corSingCB.setSelected(true);
    	corWardCB.setSelected(true);
    		
    	pearAvgCB.setSelected(true);
    	pearCentrCB.setSelected(true);
    	pearComplCB.setSelected(true);
    	pearMcQCB.setSelected(true);
    	pearMedCB.setSelected(true);
    	pearSingCB.setSelected(true);
    	pearWardCB.setSelected(true);
    		
    	binAvgCB.setSelected(false);
    	binCentrCB.setSelected(false);
    	binComplCB.setSelected(false);
    	binMcQCB.setSelected(false);
    	binMedCB.setSelected(false);
    	binSingCB.setSelected(false);
    	binWardCB.setSelected(false);
    		
    	canAvgCB.setSelected(false);
    	canCentrCB.setSelected(false);
    	canComplCB.setSelected(false);
    	canMcQCB.setSelected(false);
    	canMedCB.setSelected(false);
    	canSingCB.setSelected(false);
    	canWardCB.setSelected(false);
    		
    	eucAvgCB.setSelected(false);
    	eucCentrCB.setSelected(false);
    	eucComplCB.setSelected(false);
    	eucMcQCB.setSelected(false);
    	eucMedCB.setSelected(false);
    	eucSingCB.setSelected(false);
    	eucWardCB.setSelected(false);
    		
    	manAvgCB.setSelected(false);
    	manCentrCB.setSelected(false);
    	manComplCB.setSelected(false);
    	manMcQCB.setSelected(false);
    	manMedCB.setSelected(false);
    	manSingCB.setSelected(false);
    	manWardCB.setSelected(false);
    		
    	maxAvgCB.setSelected(false);
    	maxCentrCB.setSelected(false);
    	maxComplCB.setSelected(false);
    	maxMcQCB.setSelected(false);
    	maxMedCB.setSelected(false);
    	maxSingCB.setSelected(false);
    	maxWardCB.setSelected(false);
    		
    	minAvgCB.setSelected(false);
    	minCentrCB.setSelected(false);
    	minComplCB.setSelected(false);
    	minMcQCB.setSelected(false);
    	minMedCB.setSelected(false);
    	minSingCB.setSelected(false);
    	minWardCB.setSelected(false);
    	
    	commandsValidate.clear();
    	selectPearsonOnly();
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
