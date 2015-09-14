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
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.filechooser.FileFilter;

import holmes.analyse.NetPropertiesAnalyzer;
import holmes.darkgui.GUIManager;
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
	private static final long serialVersionUID = -4382182770445745847L;
	private JFrame ego;
	
	//Wiemy wszystko o wszystkim:
	private ArrayList<Arc> arcs = new ArrayList<Arc>();
	private ArrayList<Place> places = new ArrayList<Place>();
	private ArrayList<Transition> transitions = new ArrayList<Transition>();
	private ArrayList<Node> nodes = new ArrayList<Node>();
	private ArrayList<ArrayList<Integer>> invariantsMatrix;
	private ArrayList<ArrayList<Object>> properties = new ArrayList<ArrayList<Object>>();
	
	//komponenty do ustawienia:
	private JLabel label_netName;
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
			setIconImage(Tools.getImageFromIcon("/icons/blackhole.png"));
		} catch (Exception e ) {
			
		}
		this.setTitle("Petri net general information and properties");
		
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
		//PetriNetData data = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getData();
		places = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getPlaces();
		transitions = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions();
		arcs = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getArcs();
		nodes = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getNodes();
		invariantsMatrix = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getT_InvMatrix();
		
		ArrayList<Integer> arcClasses = Check.getArcClassCount();
		
		label_netName.setText(GUIManager.getDefaultGUIManager().getWorkspace().getProject().getName()+"");
		label_nodesNumber.setText(nodes.size()+"");
		label_transitionsNumber.setText(transitions.size()+"");
		label_placesNumber.setText(places.size()+"");
		int readArcs = arcClasses.get(1) / 2;
		label_arcNumber.setText((arcs.size()-readArcs)+"");
		int inv_number = 0;
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
		
		int transInNoInvariant = 0;
		if(inv_number > 0) {
			for(int inv=0; inv < invariantsMatrix.size(); inv++) { // po wszystkich inwariantach
				for(int trans=0; trans < invariantsMatrix.get(0).size(); trans++) { //po wszystkich tranzycjach
					if(invariantsMatrix.get(inv).get(trans) > 0) {
						int oldVal = transInInv.get(trans);
						oldVal++;
						transInInv.set(trans, oldVal);
						
						oldVal = transFiresInInv.get(trans);
						oldVal += invariantsMatrix.get(inv).get(trans);
						transFiresInInv.set(trans, oldVal);
					}
				}
			}
			for(int trans=0; trans<transitions.size(); trans++) { //policz nieaktywne tranzycje
				if(transInInv.get(trans) == 0) {
					idTransNoInv.add(trans);
					transInNoInvariant++;
				}
			}
			textField.setText("");
			
			//WYŚWIETLANIE DANYCH:
			if(transInNoInvariant==0) {
				textField.append("The net is covered by t-invariants.\n");
				textField.append("\n");
			} else {
				textField.append("The net is not covered by t-invariants. Transitions outside invariants set:\n");
				for(int i=0; i<idTransNoInv.size(); i++) {
					int tNumber = idTransNoInv.get(i);
					String txt1 = Tools.setToSize("t"+tNumber, 5, false);
					textField.append(txt1+" "+(tNumber)+transitions.get(tNumber).getName()+"\n");
				}
				textField.append("\n");
			}
			
			textField.append("Transitions data:\n");
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
	 * @author MR - tak się kiedyś, dzieci, programowało. Tak powstał Unix. I Linux. Więc mordy w kubeł.
	 */
	private JPanel createMainPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(null);  /** (ノಠ益ಠ)ノ彡┻━━━┻ |   */
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
		label_netName = new JLabel("N/A");
		label_netName.setBounds(xPos+label1.getWidth()+10, label1.getLocation().y, 400, 20);
		panel.add(label_netName);
		//NET NODES:
		JLabel label2 = new JLabel("Nodes:");
		label2.setBounds(xPos, yPosA+=spacing, 100, 20);
		panel.add(label2);
		label_nodesNumber = new JLabel("N/A");
		label_nodesNumber.setBounds(xPos+label2.getWidth()+10, label2.getLocation().y, numberLabelWidth, 20);
		panel.add(label_nodesNumber);
		//NET TRANSITION:
		JLabel label3 = new JLabel("Transitions:");
		label3.setBounds(xPos, yPosA+=spacing, 100, 20);
		panel.add(label3);
		label_transitionsNumber = new JLabel("N/A");
		label_transitionsNumber.setBounds(xPos+label3.getWidth()+10, label3.getLocation().y, numberLabelWidth, 20);
		panel.add(label_transitionsNumber);
		//NET PLACES:
		JLabel label4 = new JLabel("Places:");
		label4.setBounds(xPos, yPosA+=spacing, 100, 20);
		panel.add(label4);
		label_placesNumber = new JLabel("N/A");
		label_placesNumber.setBounds(xPos+label4.getWidth()+10, label4.getLocation().y, numberLabelWidth, 20);
		panel.add(label_placesNumber);
		//NET PLACES:
		JLabel label5 = new JLabel("Arcs:");
		label5.setBounds(xPos, yPosA+=spacing, 100, 20);
		panel.add(label5);
		label_arcNumber = new JLabel("N/A");
		label_arcNumber.setBounds(xPos+label5.getWidth()+10, label5.getLocation().y, numberLabelWidth, 20);
		panel.add(label_arcNumber);
		//NET INVARIANTS:
		JLabel label6 = new JLabel("Invariants:");
		label6.setBounds(xPos, yPosA+=spacing, 100, 20);
		panel.add(label6);
		label_invNumber = new JLabel("N/A");
		label_invNumber.setBounds(xPos+label6.getWidth()+10, label6.getLocation().y, numberLabelWidth, 20);
		panel.add(label_invNumber);
		
		//II KOLUMNA:
		JLabel label11 = new JLabel("Normal arc:");
		label11.setBounds(xPos+150, yPosB+=spacing, 80, 20);
		panel.add(label11);
		label_arcNormal = new JLabel("0");
		label_arcNormal.setBounds(xPos+240, yPosB, 60, 20);
		panel.add(label_arcNormal);
		
		JLabel label12 = new JLabel("Read-arc:");
		label12.setBounds(xPos+150, yPosB+=spacing, 80, 20);
		panel.add(label12);
		label_arcReadarc = new JLabel("0");
		label_arcReadarc.setBounds(xPos+240, yPosB, 60, 20);
		panel.add(label_arcReadarc);
		
		JLabel label13 = new JLabel("Inhibitor arc:");
		label13.setBounds(xPos+150, yPosB+=spacing, 80, 20);
		panel.add(label13);
		label_arcInhibitor = new JLabel("0");
		label_arcInhibitor.setBounds(xPos+240, yPosB, 60, 20);
		panel.add(label_arcInhibitor);
		
		JLabel label14 = new JLabel("Reset arc:");
		label14.setBounds(xPos+150, yPosB+=spacing, 80, 20);
		panel.add(label14);
		label_arcReset = new JLabel("0");
		label_arcReset.setBounds(xPos+240, yPosB, 60, 20);
		panel.add(label_arcReset);
		
		JLabel label15 = new JLabel("Equal arc:");
		label15.setBounds(xPos+150, yPosB+=spacing, 80, 20);
		panel.add(label15);
		label_arcEqual = new JLabel("0");
		label_arcEqual.setBounds(xPos+240, yPosB, 60, 20);
		panel.add(label_arcEqual);
		
		//Panel właściwości
		staticPropertiesPanel = new JPanel(new FlowLayout());
		staticPropertiesPanel.setBorder(BorderFactory.createTitledBorder("Net static properties:"));
		staticPropertiesPanel.setBounds(330, 35, 300, 100);
		panel.add(staticPropertiesPanel);
		
		yPosA += 25;
		
		JButton saveButton = new JButton("Save to file");
		saveButton.setMargin(new Insets(0, 0, 0, 0));
		saveButton.setIcon(Tools.getResIcon22("/icons/quickSave.png"));
		saveButton.setBounds(xPos, yPosA, 130, 30);
		saveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				saveToFile();
			}
		});
		panel.add(saveButton);
		
		yPosA += 10;
		//Panel informacji o invariantach
		JPanel invInfoPanel = new JPanel(new BorderLayout());
		invInfoPanel.setBorder(BorderFactory.createTitledBorder("Invariants details:"));
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
				if ((Boolean) pr.get(1) == true) {
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
				    			"Petri net meaning:\n" + yesWeCan[1]
				    			+ "\n\nBiological interpretation:\n" + yesWeCan[2],
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
		String lastPath = GUIManager.getDefaultGUIManager().getLastPath();
		filters[0] = new ExtensionFileFilter("Normal text file (.txt)",  new String[] { "TXT" });
		String selectedFile = Tools.selectFileDialog(lastPath, filters, "Save", "", "");
		
		if(!selectedFile.equals("")) { //jeśli wskazano plik
			String fileName = selectedFile.substring(selectedFile.lastIndexOf(File.separator)+1);
			if(!fileName.contains(".txt")) //(⌐■_■)
				selectedFile += ".txt";
			
			try {
				BufferedWriter bw = new BufferedWriter(new FileWriter(selectedFile));
				
				bw.write("Petri net name: "+label_netName.getText()+"\n");
				bw.write("Number of nodes: "+label_nodesNumber.getText()+"\n");
				bw.write("Number of transitions: "+label_transitionsNumber.getText()+"\n");
				bw.write("Number of places: "+label_placesNumber.getText()+"\n");
				bw.write("Number of arcs: "+label_arcNumber.getText()+"\n");
				
				bw.write("\n");
				bw.write("Net static properties: \n");
				int count = 1;
				for(int i=0; i<properties.size(); i++) {
					boolean isNet = (boolean)properties.get(i).get(1);
					if(isNet) {
						String[] data = (String[]) properties.get(i).get(2);
						bw.write("Property "+count+": "+data[0]+"\n");
						String txt = data[1];
						txt = txt.replace("\n", "");
						bw.write("Meaning: "+txt+"\n");
						txt = data[2];
						txt = txt.replace("\n", "");
						bw.write("Biological: "+data[2]+"\n\n");
						count++;
					}
				}
				bw.write("\n");
				if(invariantsMatrix != null) {
					int inv_number = invariantsMatrix.size();
					bw.write("Number of invariants: "+inv_number+"\n");
					bw.write(textField.getText());
				}
				bw.close();
			} catch (Exception e) {
				
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
