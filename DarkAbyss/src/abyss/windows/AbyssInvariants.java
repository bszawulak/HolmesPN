package abyss.windows;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.DefaultCaret;

import abyss.analyse.InvariantsCalculator;
import abyss.analyse.InvariantsTools;
import abyss.darkgui.GUIManager;
import abyss.files.io.IOprotocols;
import abyss.utilities.Tools;
import abyss.workspace.ExtensionFileFilter;

/**
 * Okno generatora inwariantów i związanych z nimi narzędzi.
 * @author MR
 *
 */
public class AbyssInvariants extends JFrame {
	private static final long serialVersionUID = 5805567123988000425L;
	private boolean tInvCalculation = true;
	private AbyssInvariants ego;
	private JTextArea logField;
	private InvariantsCalculator invGenerator = null;
	public boolean isGeneratorWorking = false;

	/**
	 * Główny konstruktor okna generatora inwariantów.
	 */
	public AbyssInvariants() {
		try {
			setIconImage(Tools.getImageFromIcon("/icons/blackhole.png"));
		} catch (Exception e ) {
			
		}
		setVisible(false);
		this.setTitle("Invariants generator and tools");
		ego = this;
		
		setLayout(new BorderLayout());
		setSize(new Dimension(666, 520));
		setLocation(50, 50);
		setResizable(false);
		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		
		JPanel mainPanel = createMainPanel();
		add(mainPanel, BorderLayout.CENTER);

		
	}

	/**
	 * Metoda tworząca główny panel okna.
	 * @return JPanel - panel główny
	 */
	private JPanel createMainPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(null); /**  ╯°□°）╯︵  ┻━┻   */
		
		//Panel wyboru opcji szukania
		JPanel buttonPanel = createUpperButtonPanel(0, 0, 660, 90);
		JPanel logMainPanel = createLogMainPanel(0, 90, 530, 400);
		JPanel sideButtonPanel = createRightButtonPanel(530, 90, 130, 400);
		
		panel.add(buttonPanel);
		panel.add(logMainPanel);
		panel.add(sideButtonPanel);
		panel.repaint();
		return panel;
	}

	/**
	 * Metoda tworząca górny panel przycisków.
	 * @param x int - pozycja X
	 * @param y int - pozycja Y
	 * @param width int - szerokość panelu
	 * @param height - wysokość panelu
	 * @return JPanel - utworzony panel
	 */
	private JPanel createUpperButtonPanel(int x, int y, int width, int height) {
		JPanel panel = new JPanel();
		panel.setLayout(null);
		panel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		panel.setBounds(x, y, width, height);
		
		int posX = 10;
		int posY = 10;
		
		// przycisk generatora inwariantów
		JButton generateButton = new JButton("Generate");
		generateButton.setBounds(posX, posY, 110, 60);
		generateButton.setMargin(new Insets(0, 0, 0, 0));
		generateButton.setIcon(Tools.getResIcon32("/icons/stateSim/computeData.png"));
		generateButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				if(tInvCalculation == true) {
					if(isGeneratorWorking == true) {
						JOptionPane.showMessageDialog(null, "Invariants generation already in progress.", 
								"Generator working",JOptionPane.WARNING_MESSAGE);
					} else {
						setGeneratorStatus(true);
						
						invGenerator = new InvariantsCalculator(true);
						Thread myThread = new Thread(invGenerator);
						myThread.start();
					}
				} else {
					JOptionPane.showMessageDialog(ego, "Not implementet yet! Sorry!", "Warning", JOptionPane.INFORMATION_MESSAGE);
				}
			}
		});
		generateButton.setFocusPainted(false);
		panel.add(generateButton);

		// USTAWIANIE T/P INWARIANTÓW
		ButtonGroup group = new ButtonGroup();
		JRadioButton tInvariantsMode = new JRadioButton("T-invariants (EM)");
		tInvariantsMode.setBounds(posX+120, posY-3, 120, 20);
		tInvariantsMode.setActionCommand("0");
		ActionListener tInvariantsModeActionListener = new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				tInvCalculation = true;
			}
		};
		tInvariantsMode.addActionListener(tInvariantsModeActionListener);
		panel.add(tInvariantsMode);
		group.add(tInvariantsMode);
		group.setSelected(tInvariantsMode.getModel(), true);
		
		JRadioButton pInvariantsMode = new JRadioButton("P-invariants");
		pInvariantsMode.setBounds(posX+120, posY+15, 120, 20);
		pInvariantsMode.setActionCommand("0");
		ActionListener pInvariantsModeActionListener = new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				tInvCalculation = false;
			}
		};
		pInvariantsMode.addActionListener(pInvariantsModeActionListener);
		panel.add(pInvariantsMode);
		group.add(pInvariantsMode);
		
		// INA GENERATOR
		JButton INAgenerateButton = new JButton();
		INAgenerateButton.setText("<html>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;INA <br />generator</html>");
		INAgenerateButton.setBounds(posX+250, posY, 120, 32);
		INAgenerateButton.setMargin(new Insets(0, 0, 0, 0));
		INAgenerateButton.setIcon(Tools.getResIcon22("/icons/invWindow/inaGenerator.png"));
		INAgenerateButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				setGeneratorStatus(true);
				GUIManager.getDefaultGUIManager().io.generateINAinvariants();
				
			}
		});
		INAgenerateButton.setFocusPainted(false);
		panel.add(INAgenerateButton);
		
		JButton showInvariantsButton = new JButton();
		showInvariantsButton.setText("<html>&nbsp;&nbsp;&nbsp;&nbsp;Show <br />invariants</html>");
		showInvariantsButton.setBounds(posX+250, posY+36, 120, 32);
		showInvariantsButton.setMargin(new Insets(0, 0, 0, 0));
		showInvariantsButton.setIcon(Tools.getResIcon22("/icons/invWindow/showInvariants.png"));
		showInvariantsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				//fillData();
			}
		});
		showInvariantsButton.setFocusPainted(false);
		
		showInvariantsButton.setEnabled(false);
		panel.add(showInvariantsButton);
		
		
		JButton loadInvariantsButton = new JButton();
		loadInvariantsButton.setText("<html>&nbsp;&nbsp;&nbsp;&nbsp;Load <br />invariants</html>");
		loadInvariantsButton.setBounds(posX+380, posY, 120, 32);
		loadInvariantsButton.setMargin(new Insets(0, 0, 0, 0));
		loadInvariantsButton.setIcon(Tools.getResIcon22("/icons/invWindow/loadInvariants.png"));
		loadInvariantsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				GUIManager.getDefaultGUIManager().io.loadExternalAnalysis();
				//TODO: komunikat do logu
			}
		});
		loadInvariantsButton.setFocusPainted(false);
		panel.add(loadInvariantsButton);
		
		JButton saveInvariantsButton = new JButton();
		saveInvariantsButton.setText("<html>&nbsp;&nbsp;&nbsp;Export <br />invariants</html>");
		saveInvariantsButton.setBounds(posX+510, posY, 120, 32);
		saveInvariantsButton.setMargin(new Insets(0, 0, 0, 0));
		saveInvariantsButton.setIcon(Tools.getResIcon22("/icons/invWindow/saveInvariants.png"));
		saveInvariantsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				GUIManager.getDefaultGUIManager().io.exportGeneratedInvariants();
				//TODO: komunikat do logu
			}
		});
		saveInvariantsButton.setFocusPainted(false);
		panel.add(saveInvariantsButton);

		return panel;
	}
	
	/**
	 * Metoda tworząca środkowy panel okna logów.
	 * @param x int - pozycja X
	 * @param y int - pozycja Y
	 * @param width int - szerokość panelu
	 * @param height - wysokość panelu
	 * @return JPanel - utworzony panel
	 */
	private JPanel createLogMainPanel(int x, int y, int width, int height) {
		JPanel panel = new JPanel();
		panel.setLayout(null);
		panel.setBorder(BorderFactory.createTitledBorder("Log"));
		panel.setBounds(x, y, width, height);
		
		logField = new JTextArea();
		logField.setLineWrap(true);
		logField.setEditable(false);
		DefaultCaret caret = (DefaultCaret)logField.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		
        JPanel logFieldPanel = new JPanel();
        logFieldPanel.setLayout(new BorderLayout());
        logFieldPanel.add(new JScrollPane(logField), BorderLayout.CENTER);
        logFieldPanel.setBounds(10, 20, width-20, height-25);
        panel.add(logFieldPanel);
        
		return panel;
	}
	
	/**
	 * Metoda tworząca prawy panel przycisków.
	 * @param x int - pozycja X
	 * @param y int - pozycja Y
	 * @param width int - szerokość panelu
	 * @param height - wysokość panelu
	 * @return JPanel - utworzony panel
	 */
	private JPanel createRightButtonPanel(int x, int y, int width, int height) {
		JPanel panel = new JPanel();
		panel.setLayout(null);
		panel.setBorder(BorderFactory.createTitledBorder("Tools"));
		panel.setBounds(x, y, width, height);
		
		int posX = 10;
		int posY = 18;
		
		JButton cardinalityButton = new JButton();
		cardinalityButton.setText("<html>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Check<br />&nbsp;&nbsp;&nbsp;canonity</html>");
		cardinalityButton.setBounds(posX, posY, 110, 32);
		cardinalityButton.setMargin(new Insets(0, 0, 0, 0));
		cardinalityButton.setIcon(Tools.getResIcon22("/icons/invWindow/test_canon.png"));
		cardinalityButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				ArrayList<ArrayList<Integer>> invariants = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getInvariantsMatrix();
				if(invariants == null || invariants.size() == 0) {
					JOptionPane.showMessageDialog(null, "No invarians to analyze.", 
							"No invariants",JOptionPane.INFORMATION_MESSAGE);
				} else {
					logField.append("\n");
					logField.append("=====================================================================\n");
					logField.append("Checking canonicality for "+invariants.size()+" invariants.\n");
					int card = InvariantsTools.checkCanonity(invariants);
					logField.append("Non canonical invariants: "+card+"\n");
					logField.append("=====================================================================\n");
				}
				
				
			}
		});
		cardinalityButton.setFocusPainted(false);
		panel.add(cardinalityButton);
		
		JButton minSuppButton = new JButton();
		minSuppButton.setText("<html>Check sup.<br />&nbsp;minimality</html>");
		minSuppButton.setBounds(posX, posY+38, 110, 32);
		minSuppButton.setMargin(new Insets(0, 0, 0, 0));
		minSuppButton.setIcon(Tools.getResIcon22("/icons/invWindow/test_minsup.png"));
		minSuppButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				ArrayList<ArrayList<Integer>> invariants = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getInvariantsMatrix();
				if(invariants == null || invariants.size() == 0) {
					JOptionPane.showMessageDialog(null, "No invarians to analyze.", 
							"No invariants",JOptionPane.INFORMATION_MESSAGE);
				} else {
					logField.append("\n");
					logField.append("=====================================================================\n");
					logField.append("Checking support minimality for "+invariants.size()+" invariants.\n");
					int value = InvariantsTools.checkSupportMinimality(invariants);
					logField.append("Non support-minimal invariants: "+value+"\n");
					logField.append("=====================================================================\n");
				}
			}
		});
		minSuppButton.setFocusPainted(false);
		panel.add(minSuppButton);
		
		JButton checkMatrixZeroButton = new JButton();
		checkMatrixZeroButton.setText("<html>&nbsp;&nbsp;&nbsp;&nbsp;Check<br />&nbsp;&nbsp;&nbsp;&nbsp;C&nbsp;&middot;&nbsp;x = 0</html>");
		checkMatrixZeroButton.setBounds(posX, posY+76, 110, 32);
		checkMatrixZeroButton.setMargin(new Insets(0, 0, 0, 0));
		checkMatrixZeroButton.setIcon(Tools.getResIcon22("/icons/invWindow/test_invC.png"));
		checkMatrixZeroButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				ArrayList<ArrayList<Integer>> invariants = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getInvariantsMatrix();
				if(invariants == null || invariants.size() == 0) {
					JOptionPane.showMessageDialog(null, "No invarians to analyze.", 
							"No invariants",JOptionPane.INFORMATION_MESSAGE);
				} else {
					logField.append("\n");
					logField.append("=====================================================================\n");
					logField.append("Checking invariants correctness for "+invariants.size()+" invariants.\n");
					InvariantsCalculator ic = new InvariantsCalculator(true);
					
					int value =  InvariantsTools.countNonInvariants(ic.getCMatrix(), invariants);
					logField.append("Non-invariants: "+value+"\n");
					logField.append("=====================================================================\n");
				}
			}
		});
		checkMatrixZeroButton.setFocusPainted(false);
		panel.add(checkMatrixZeroButton);
		
		JButton loadRefButton = new JButton();
		loadRefButton.setText("<html>&nbsp;&nbsp;&nbsp;Ref. set<br />&nbsp;&nbsp;&nbsp;compare</html>");
		loadRefButton.setBounds(posX, posY+114, 110, 32);
		loadRefButton.setMargin(new Insets(0, 0, 0, 0));
		loadRefButton.setIcon(Tools.getResIcon22("/icons/invWindow/test_ref.png"));
		loadRefButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				testReference();
			}
		});
		loadRefButton.setFocusPainted(false);
		panel.add(loadRefButton);

		return panel;
	}
	
	protected void testReference() {
		ArrayList<ArrayList<Integer>> invariants = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getInvariantsMatrix();
		if(invariants == null || invariants.size() < 1) {
			JOptionPane.showMessageDialog(null, "Invariants matrix empty! Operation cannot start.", 
					"Warning", JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		
		String lastPath = GUIManager.getDefaultGUIManager().getLastPath();
		FileFilter[] filters = new FileFilter[1];
		filters[0] = new ExtensionFileFilter("INA Invariants file (.inv)", new String[] { "INV" });
		String selectedFile = Tools.selectFileDialog(lastPath, filters, "Load invariants", "Select invariant file");
		if(selectedFile.equals(""))
			return;
		
		File file = new File(selectedFile);
		if(!file.exists()) return;
		
		IOprotocols io = new IOprotocols();
				//GUIManager.getDefaultGUIManager().getWorkspace().getProject().getCommunicator();
		boolean status = io.readINV(file.getPath());
		if(status == false) {
			return;
		}
		refTest(invariants, io.getInvariantsList());
		
	}

	private void refTest(ArrayList<ArrayList<Integer>> invRefMatrix, ArrayList<ArrayList<Integer>> invLoadedMatrix) {
		//InvariantsTools.finalSupportMinimalityTest(getInvariants());
		
		if(invRefMatrix != null) {
			
			ArrayList<ArrayList<Integer>> res =  InvariantsTools.compareInv(invRefMatrix, invLoadedMatrix);
			logField.append("\n");
			logField.append("=====================================================================\n");
			logField.append("Prev. computed set size:   "+invRefMatrix.size()+"\n");
			logField.append("Loaded (now) set size:    "+invLoadedMatrix.size()+"\n");
			logField.append("Common set size (load & ref): "+res.get(0).size()+"\n");
			logField.append("Loaded invariants not in a computed set:  "+res.get(1).size()+"\n");
			logField.append("Computed invariants not in a loaded set:  "+res.get(2).size()+"\n");
			logField.append("Repetitions in common set: "+res.get(3).get(0)+"\n");
			logField.append("Total repetitions in loaded:"+res.get(3).get(1)+"\n");
			logField.append("\n");
			logField.append("Inititating further tests for the loaded set of "+invLoadedMatrix.size()+" invariants.\n");
			int card = InvariantsTools.checkCanonity(invLoadedMatrix);
			logField.append("-> Non canonical invariants found : "+card+"\n");
			int value = InvariantsTools.checkSupportMinimality(invLoadedMatrix);
			logField.append("-> Non support-minimal inv. found: "+value+"\n");
			InvariantsCalculator ic = new InvariantsCalculator(true);
			value =  InvariantsTools.countNonInvariants(ic.getCMatrix(), invLoadedMatrix);
			logField.append("-> Not-invariant vectors (Cx=0 test): "+value+"\n");
			logField.append("=====================================================================\n");
			logField.append("\n");
			/*
			System.out.println();
			System.out.println("Computed set size:   "+invRefMatrix.size());
			System.out.println("Loaded set size:    "+invCoreMatrix.size());
			System.out.println("Common set size:      "+res.get(0).size());
			System.out.println("Not in computed set: "+res.get(1).size());
			System.out.println("Not in loaded set:  "+res.get(2).size());
			
			System.out.println("Repeated in common set: "+res.get(3).get(0));
			System.out.println("Repeated not in computed set:"+res.get(3).get(1));
			*/
		}
	}
	
	/**
	 * Metoda umożliwia dostęp do kompontentu wyświetlania logów generatora inwariantów.
	 * @return JTextArea - obiekt logów
	 */
	public JTextArea accessLogField() {
		return logField;
	}
	
	/**
	 * Metoda resetuje połączenie z wątkiem generatora.
	 */
	public void resetInvariantGenerator() {
		invGenerator = null;
		setGeneratorStatus(false);
	}
	
	/**
	 * Metoda ustawia status generatora MCS.
	 * @param status boolean - true, jeśli właśnie działa w tle
	 */
	public void setGeneratorStatus(boolean status) {
		isGeneratorWorking = status;
	}
}
