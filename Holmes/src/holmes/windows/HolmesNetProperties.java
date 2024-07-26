package holmes.windows;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.Serial;
import java.text.ParseException;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.filechooser.FileFilter;

import holmes.analyse.InvariantsCalculator;
import holmes.analyse.InvariantsTools;
import holmes.analyse.NetPropertiesAnalyzer;
import holmes.darkgui.GUIManager;
import holmes.darkgui.LanguageManager;
import holmes.petrinet.elements.Arc;
import holmes.petrinet.elements.Node;
import holmes.petrinet.elements.Place;
import holmes.petrinet.elements.Transition;
import holmes.utilities.Tools;
import holmes.varia.Check;
import holmes.workspace.ExtensionFileFilter;

/**
 * Klasa odpowiedzialna za wyświetlanie okna informacji o sieci, jej właściwościach, itd.
 * @author MR
 *
 */
public class HolmesNetProperties extends JFrame {
	@Serial
	private static final long serialVersionUID = -4382182770445745847L;
	private static final GUIManager overlord = GUIManager.getDefaultGUIManager();
	private static final LanguageManager lang = GUIManager.getLanguageManager();
	private JFrame ego;
	
	//Wiemy wszystko o wszystkim:
	private ArrayList<Arc> arcs = new ArrayList<Arc>();
	private ArrayList<Place> places = new ArrayList<Place>();
	private ArrayList<Transition> transitions = new ArrayList<Transition>();
	private ArrayList<Node> nodes = new ArrayList<Node>();
	private ArrayList<ArrayList<Integer>> invariantsMatrix;
	private ArrayList<ArrayList<Object>> properties = new ArrayList<ArrayList<Object>>();
	
	//komponenty do ustawienia:
	private JFormattedTextField nameField;
	private JLabel label_nodesNumber;
	private JLabel label_transitionsNumber;
	private JLabel label_placesNumber;
	private JLabel label_arcNumber;
	private JLabel label_invNumber;
	
	private JLabel label_arcNormal;
	private JLabel label_arcReadarc;
	private JLabel label_arcInhibitor;
	private JLabel label_arcReset;
	private JLabel label_arcEqual;
	
	JPanel staticPropertiesPanel; //panel właściwości
	JTextArea textField;
	
	/**
	 * Konstruktor domyślny okna klasy HolmesProperties.
	 */
	public HolmesNetProperties() {
		ego = this;
		try {
			setIconImage(Tools.getImageFromIcon("/icons/holmesicon.png"));
		} catch (Exception ex) {
			overlord.log(lang.getText("LOGentry00470exception")+" "+ex.getMessage(), "error", true);
		}
		this.setTitle(lang.getText("HNPwin_entry001title"));

		setLayout(new BorderLayout());
		setSize(new Dimension(650, 550));
		setLocation(50, 50);
		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		setResizable(false);
		
		JPanel main = createMainPanel();
		add(main, BorderLayout.CENTER);

		addWindowListener(new java.awt.event.WindowAdapter() {
		    @Override
		    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
		    	//parentFrame.setEnabled(true);
		    }
		});
		
		//setLocationRelativeTo(null);
		setVisible(false);
		initiateListeners();
	}
	
	/**
	 * Metoda ta odświeża okno o najnowsze informacje na temat załadowanej sieci.
	 */
	private void fillData() {
		//PetriNetData data = petriNet.getData();
		places = overlord.getWorkspace().getProject().getPlaces();
		transitions = overlord.getWorkspace().getProject().getTransitions();
		arcs = overlord.getWorkspace().getProject().getArcs();
		nodes = overlord.getWorkspace().getProject().getNodes();
		invariantsMatrix = overlord.getWorkspace().getProject().getT_InvMatrix();
		
		ArrayList<Integer> arcClasses = Check.getArcClassCount();
		
		nameField.setText(overlord.getWorkspace().getProject().getName());
		label_nodesNumber.setText(nodes.size()+"");
		label_transitionsNumber.setText(transitions.size()+"");
		label_placesNumber.setText(places.size()+"");
		int readArcs = arcClasses.get(1) / 2;
		label_arcNumber.setText((arcs.size()-readArcs)+"");
		int inv_number;
		if(invariantsMatrix == null) {
			inv_number = 0;
		}
		else {
			inv_number = invariantsMatrix.size();
		}
		label_invNumber.setText(inv_number+"");
		
		
		label_arcNormal.setText(arcClasses.get(0)+"");
		label_arcReadarc.setText(arcClasses.get(1)/2+"");
		label_arcInhibitor.setText(arcClasses.get(2)+"");
		label_arcReset.setText(arcClasses.get(3)+"");
		label_arcEqual.setText(arcClasses.get(4)+"");
		
		fillStaticProperties();
		
		ArrayList<Integer> idTransNoInv = new ArrayList<Integer>();
		ArrayList<Integer> transInInv = new ArrayList<Integer>();
		ArrayList<Integer> transFiresInInv = new ArrayList<Integer>();
		for(int trans=0; trans<transitions.size(); trans++) { //zerowanie wektorów
			transInInv.add(0);
			transFiresInInv.add(0);
		}
		
		int sur = 0;
		int sub = 0;
		int none = 0;
		int normal = 0;
		if(invariantsMatrix != null) {
			//sprawdza czy określono typy inwariantów, jeśli nie - wymusza przeliczenie 
			if(!overlord.getWorkspace().getProject().getT_invTypesComputed()) {
				textField.append(lang.getText("HNPwin_entry002")); //Computing t-invariants types vector
				InvariantsCalculator ic = new InvariantsCalculator(true);
				InvariantsTools.analyseInvariantTypes(ic.getCMatrix(), invariantsMatrix, true);
			}
			ArrayList<Integer> invTypes = overlord.getWorkspace().getProject().accessT_InvTypesVector();

			for (Integer invType : invTypes) {
				if (invType == 0) {
					normal++;
				} else if (invType == -1) {
					sub++;
				} else if (invType == 1) {
					sur++;
				} else {
					none++;
				}
			}
		}
		
		textField.setText("");
		int transInNoInvariant = 0;
		if(inv_number > 0) {
			ArrayList<Integer> invTypes = overlord.getWorkspace().getProject().accessT_InvTypesVector();
			for(int inv=0; inv < invariantsMatrix.size(); inv++) { // po wszystkich inwariantach
				if(invTypes.get(inv) != 0) //tylko prawdziwe t-inv
					continue;
				
				for(int t=0; t < invariantsMatrix.get(0).size(); t++) { //po wszystkich tranzycjach
					if(invariantsMatrix.get(inv).get(t) > 0) {
						int oldVal = transInInv.get(t);
						oldVal++;
						transInInv.set(t, oldVal);
						
						oldVal = transFiresInInv.get(t);
						oldVal += invariantsMatrix.get(inv).get(t);
						transFiresInInv.set(t, oldVal);
					}
				}
			}
			for(int trans=0; trans<transitions.size(); trans++) { //policz nieaktywne tranzycje
				if(transInInv.get(trans) == 0) {
					idTransNoInv.add(trans);
					transInNoInvariant++;
				}
			}
			textField.append(lang.getText("HNPwin_entry003")+" "+normal+"\n");
			textField.append(lang.getText("HNPwin_entry004")+" "+sur+"\n");
			textField.append(lang.getText("HNPwin_entry005")+" "+sub+"\n");
			textField.append(lang.getText("HNPwin_entry006")+" "+none+"\n");
			
			//WYŚWIETLANIE DANYCH:
			if(transInNoInvariant==0) {
				textField.append("\n");
				textField.append(lang.getText("HNPwin_entry007"));
				textField.append("\n");
			} else {
				textField.append("\n");
				textField.append(lang.getText("HNPwin_entry008"));
				String strB = String.format(lang.getText("HNPwin_entry008"), normal);
				textField.append(strB);
				textField.append("\n");
				for (int tNumber : idTransNoInv) {
					String txt1 = Tools.setToSize("t" + tNumber, 5, false);
					textField.append(txt1 + " " + (tNumber) + transitions.get(tNumber).getName() + "\n");
				}
				textField.append("\n");
			}
			
			String strB = String.format(lang.getText("HNPwin_entry009"), normal);
			textField.append(strB);
			for(int i=0; i<transitions.size(); i++) {
				if(transInInv.get(i) > 0) { //dla tranz. w inwariantach
					int transInv = transInInv.get(i);
					int transFire = transFiresInInv.get(i);
					String transName = transitions.get(i).getName();
					
					String txt1 = Tools.setToSize("t"+i, 5, false);
					String txt2 = Tools.setToSize("Inv:"+transInv, 10, false);
					String txt3 = Tools.setToSize("Fired:"+transFire, 12, false);
					textField.append(txt1 + txt2 + txt3 + "  t"+(i)+"_"+transName+"\n");
					//textField.append("ID: "+(i)+" INV: "+transInv+" Fired: "+transFire);
					//textField.append("    | t_"+(i)+" "+transName+"\n");
				}
			}
			textField.setCaretPosition(0);
		}
	}
	
	/**
	 * Absolute positioning. Of absolute everything here. 
	 * Nie obchodzi mnie, co o tym myślisz. Idź w layout i nie wracaj. ┌∩┐(◣_◢)┌∩┐
	 * @return JPanel - panel z danymi
	 * @author MR - tak się kiedyś, dzieci, programowało. Tak powstał Unix. I Linux. Więc mordki w kubeł. <br>
	 * 			P.S. No i co z tego, że mamy XXI wiek?! :D
	 */
	private JPanel createMainPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(null);  // (ノಠ益ಠ)ノ彡┻━━━┻ |
		panel.setBorder(BorderFactory.createTitledBorder("General Petri net informations:"));
		
		int xPos = 10;
		int yPosA = 15;
		int yPosB = 15;
		int spacing = 20;
		int numberLabelWidth = 70;
		
		//NET NAME:
		JLabel label1 = new JLabel("Project name:");
		label1.setBounds(xPos, yPosA, 100, 20);
		panel.add(label1);
		
		nameField = new JFormattedTextField();
		nameField.setBounds(xPos+110, yPosA+2, 500, 20);
		nameField.addPropertyChangeListener("value", e -> {
			JFormattedTextField field = (JFormattedTextField) e.getSource();
			try {
				field.commitEdit();
			} catch (ParseException ex) {
				overlord.log(lang.getText("LOGentry00471exception")+" "+ex.getMessage(), "error", true);
			}
			String newName = field.getText();
			overlord.getWorkspace().getProject().setName(newName);
		});
		panel.add(nameField);
		
		//NET NODES:
		JLabel label2 = new JLabel(lang.getText("HNPwin_entry010")); //Nodes
		label2.setBounds(xPos, yPosA+=spacing, 100, 20);
		panel.add(label2);
		label_nodesNumber = new JLabel(lang.getText("HNPwin_entry011"));
		label_nodesNumber.setBounds(xPos+label2.getWidth()+10, label2.getLocation().y, numberLabelWidth, 20);
		panel.add(label_nodesNumber);
		//NET TRANSITION:
		JLabel label3 = new JLabel(lang.getText("HNPwin_entry012"));//Transitions
		label3.setBounds(xPos, yPosA+=spacing, 100, 20);
		panel.add(label3);
		label_transitionsNumber = new JLabel(lang.getText("HNPwin_entry011"));
		label_transitionsNumber.setBounds(xPos+label3.getWidth()+10, label3.getLocation().y, numberLabelWidth, 20);
		panel.add(label_transitionsNumber);
		//NET PLACES:
		JLabel label4 = new JLabel(lang.getText("HNPwin_entry013")); //Places
		label4.setBounds(xPos, yPosA+=spacing, 100, 20);
		panel.add(label4);
		label_placesNumber = new JLabel(lang.getText("HNPwin_entry011"));
		label_placesNumber.setBounds(xPos+label4.getWidth()+10, label4.getLocation().y, numberLabelWidth, 20);
		panel.add(label_placesNumber);
		//NET PLACES:
		JLabel label5 = new JLabel(lang.getText("HNPwin_entry014")); //Arcs
		label5.setBounds(xPos, yPosA+=spacing, 100, 20);
		panel.add(label5);
		label_arcNumber = new JLabel(lang.getText("HNPwin_entry011"));
		label_arcNumber.setBounds(xPos+label5.getWidth()+10, label5.getLocation().y, numberLabelWidth, 20);
		panel.add(label_arcNumber);
		//NET INVARIANTS:
		JLabel label6 = new JLabel(lang.getText("HNPwin_entry015")); //Invariants
		label6.setBounds(xPos, yPosA+=spacing, 100, 20);
		panel.add(label6);
		label_invNumber = new JLabel("N/A");
		label_invNumber.setBounds(xPos+label6.getWidth()+10, label6.getLocation().y, numberLabelWidth, 20);
		panel.add(label_invNumber);
		
		//II KOLUMNA:
		JLabel label11 = new JLabel(lang.getText("HNPwin_entry016")); //Normal arc
		label11.setBounds(xPos+150, yPosB+=spacing, 80, 20);
		panel.add(label11);
		label_arcNormal = new JLabel("0");
		label_arcNormal.setBounds(xPos+240, yPosB, 60, 20);
		panel.add(label_arcNormal);
		
		JLabel label12 = new JLabel(lang.getText("HNPwin_entry017")); //Read arc
		label12.setBounds(xPos+150, yPosB+=spacing, 80, 20);
		panel.add(label12);
		label_arcReadarc = new JLabel("0");
		label_arcReadarc.setBounds(xPos+240, yPosB, 60, 20);
		panel.add(label_arcReadarc);
		
		JLabel label13 = new JLabel(lang.getText("HNPwin_entry018")); //Inhibitor arc
		label13.setBounds(xPos+150, yPosB+=spacing, 80, 20);
		panel.add(label13);
		label_arcInhibitor = new JLabel("0");
		label_arcInhibitor.setBounds(xPos+240, yPosB, 60, 20);
		panel.add(label_arcInhibitor);
		
		JLabel label14 = new JLabel(lang.getText("HNPwin_entry019")); //Reset arc
		label14.setBounds(xPos+150, yPosB+=spacing, 80, 20);
		panel.add(label14);
		label_arcReset = new JLabel("0");
		label_arcReset.setBounds(xPos+240, yPosB, 60, 20);
		panel.add(label_arcReset);
		
		JLabel label15 = new JLabel(lang.getText("HNPwin_entry020")); //Equal arc
		label15.setBounds(xPos+150, yPosB+=spacing, 80, 20);
		panel.add(label15);
		label_arcEqual = new JLabel("0");
		label_arcEqual.setBounds(xPos+240, yPosB, 60, 20);
		panel.add(label_arcEqual);
		
		//Panel właściwości
		staticPropertiesPanel = new JPanel(new FlowLayout());
		staticPropertiesPanel.setBorder(BorderFactory.createTitledBorder(lang.getText("HNPwin_entry021")));
		staticPropertiesPanel.setBounds(330, 35, 300, 100);
		panel.add(staticPropertiesPanel);
		
		yPosA += 25;
		
		JButton saveButton = new JButton(lang.getText("HNPwin_entry022")); //Save to file
		saveButton.setMargin(new Insets(0, 0, 0, 0));
		saveButton.setIcon(Tools.getResIcon22("/icons/quickSave.png"));
		saveButton.setBounds(xPos, yPosA, 130, 30);
		saveButton.addActionListener(actionEvent -> saveToFile());
		panel.add(saveButton);
		
		yPosA += 10;
		//Panel informacji o invariantach
		JPanel invInfoPanel = new JPanel(new BorderLayout());
		invInfoPanel.setBorder(BorderFactory.createTitledBorder(lang.getText("HNPwin_entry023"))); //Invariants details
		invInfoPanel.setBounds(xPos-5, yPosA+=spacing, 635, 345);
		
		textField = new JTextArea();
		//textField.setLineWrap(true);
		textField.setEditable(false);
		textField.setFont(new Font("Monospaced", Font.PLAIN, 12));
		JScrollPane areaScrollPane = new JScrollPane(textField);
		areaScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		invInfoPanel.add(areaScrollPane);
		
		panel.add(invInfoPanel);
		panel.repaint();
		return panel;
	}
	
	/**
	 * Metoda odpowiedzialna za wyświetlenie (w formie przycisków) właściwości statycznych
	 * sieci. Celem ich obliczenia wywoływany jest obiekt klasy NetPropertiesAnalyzer.
	 */
	private void fillStaticProperties() {
		staticPropertiesPanel.removeAll();
		staticPropertiesPanel.revalidate();
		
		NetPropertiesAnalyzer analyzer = new NetPropertiesAnalyzer();
		properties = analyzer.propAnalyze();
		
		for(ArrayList<Object> pr : properties) { //for each property
			JButton pButton = new JButton();
			pButton.setPreferredSize(new Dimension(66,20));
			if (pr.size() < 2) {
				pButton.setBackground(Color.GRAY);
			} else {
				if ((Boolean) pr.get(1)) {
					pButton.setBackground(Color.green);
					pButton.setForeground(Color.WHITE);
				} else {
					pButton.setBackground(Color.red);
				}
				String[] dataTxt = (String[])pr.get(2);
				pButton.setToolTipText(dataTxt[0]);
				
				// anonimowy action listener przyjmujący zmienną non-final (⌐■_■)
				pButton.addActionListener(new ActionListener() {
				    private String[] yesWeCan;
				    public void actionPerformed(ActionEvent e) {
				    	JOptionPane.showMessageDialog(ego, 
				    			lang.getText("HNPwin_entry024a") + yesWeCan[1]
				    			+ lang.getText("HNPwin_entry024b") + yesWeCan[2],
				    			yesWeCan[0], JOptionPane.INFORMATION_MESSAGE);
				    }
				    private ActionListener goForthMyMinion(String[] codeInjection){
				    	yesWeCan = codeInjection;
				        return this;
				    }
				}.goForthMyMinion(dataTxt) );

			}
			pButton.setText(pr.get(0).toString());
			pButton.setVisible(true);
			staticPropertiesPanel.add(pButton);
		}
	}

	/**
	 * Metoda zapisuje aktualną wersję właściwości sieci do wskazanego pliku tekstowego.
	 */
	private void saveToFile() {
		FileFilter[] filters = new FileFilter[1];
		String lastPath = overlord.getLastPath();
		filters[0] = new ExtensionFileFilter("Normal text file (.txt)",  new String[] { "TXT" });
		String selectedFile = Tools.selectFileDialog(lastPath, filters, "Save", "", "");
		
		if(!selectedFile.isEmpty()) { //jeśli wskazano plik
			String fileName = selectedFile.substring(selectedFile.lastIndexOf(File.separator)+1);
			if(!fileName.contains(".txt")) //(⌐■_■)
				selectedFile += ".txt";
			
			try {
				BufferedWriter bw = new BufferedWriter(new FileWriter(selectedFile));
				
				bw.write(lang.getText("HNPwin_entry025")+" "+nameField.getText()+"\n");
				bw.write(lang.getText("HNPwin_entry026")+" "+label_nodesNumber.getText()+"\n");
				bw.write(lang.getText("HNPwin_entry027")+" "+label_transitionsNumber.getText()+"\n");
				bw.write(lang.getText("HNPwin_entry028")+" "+label_placesNumber.getText()+"\n");
				bw.write(lang.getText("HNPwin_entry029")+" "+label_arcNumber.getText()+"\n");
				
				bw.write("\n");
				bw.write(lang.getText("HNPwin_entry030"));
				int count = 1;
				for (ArrayList<Object> property : properties) {
					boolean isNet = (boolean) property.get(1);
					if (isNet) {
						String[] data = (String[]) property.get(2);
						bw.write(lang.getText("HNPwin_entry031")+" " + count + ": " + data[0] + "\n");
						String txt = data[1];
						txt = txt.replace("\n", "");
						bw.write(lang.getText("HNPwin_entry032")+" " + txt + "\n");
						txt = data[2];
						txt = txt.replace("\n", "");
						bw.write(lang.getText("HNPwin_entry033")+" " + data[2] + "\n\n");
						count++;
					}
				}
				bw.write("\n");
				if(invariantsMatrix != null) {
					int inv_number = invariantsMatrix.size();
					bw.write(lang.getText("HNPwin_entry034")+" "+inv_number+"\n");
					bw.write(textField.getText());
				}
				bw.close();
			} catch (Exception ex) {
				overlord.log(lang.getText("LOGentry00472exception")+" "+ex.getMessage(), "error", true);
			}
		}
	}
	
	/**
	 * Inicjalizacja agentów nasłuchujących różnych zdarzeń dla okna właściwości.
	 */
    private void initiateListeners() {
    	addWindowListener(new WindowAdapter() {
  	  	    public void windowActivated(WindowEvent e) {
  	  	    	fillData();
  	  	    }  
    	});
    }
}
