package abyss.windows;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.filechooser.FileFilter;

import abyss.analyzer.NetPropertiesAnalyzer;
import abyss.darkgui.GUIManager;
import abyss.math.Arc;
import abyss.math.Node;
import abyss.math.Place;
import abyss.math.Transition;
import abyss.utilities.Tools;
import abyss.workspace.ExtensionFileFilter;

/**
 * Klasa odpowiedzialna za wyświetlanie okna informacji o sieci, jej właściwościach, itd.
 * @author MR
 *
 */
public class AbyssNetProperties extends JFrame {
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
	JLabel label_netName;
	JLabel label_nodesNumber;
	JLabel label_transitionsNumber;
	JLabel label_placesNumber;
	JLabel label_arcNumber;
	JLabel label_invNumber;
	
	JPanel staticPropertiesPanel; //panel właściwości
	JTextArea textField;
	
	/**
	 * Konstruktor domyślny okna klasy AbyssProperties.
	 */
	public AbyssNetProperties() {
		ego = this;
		try {
			setIconImage(Tools.getImageFromIcon("/icons/blackhole.png"));
		} catch (Exception e ) {
			
		}
		this.setTitle("Petri net general information and properties");
		
		setLayout(new BorderLayout());
		setSize(new Dimension(650, 500));
		setLocation(50, 50);
		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		setResizable(false);
		
		JPanel main = createMainPanel();
		JPanel buttonsPanel = createButtonsPanel();
		
		add(main, BorderLayout.CENTER);
		add(buttonsPanel, BorderLayout.EAST);

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
		invariantsMatrix = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getInvariantsMatrix();
		
		label_netName.setText(GUIManager.getDefaultGUIManager().getWorkspace().getProject().getName()+"");
		label_nodesNumber.setText(nodes.size()+"");
		label_transitionsNumber.setText(transitions.size()+"");
		label_placesNumber.setText(places.size()+"");
		label_arcNumber.setText(arcs.size()+"");
		int inv_number = 0;
		if(invariantsMatrix == null) {
			inv_number = 0;
		}
		else {
			inv_number = invariantsMatrix.size();
		}
		label_invNumber.setText(inv_number+"");
		
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
		
		int currentXAxis = 10;
		int currentYAxis = 15;
		int spacing = 20;
		int numberLabelWidth = 70;
		
		//Poniższe idą od lewego górnego rogu panelu
		//JLabel label1 = new JLabel("General Petri net informations:");
		//label1.setBounds(currentXAxis, currentYAxis, 200, 20);
		//panel.add(label1);
		//NET NAME:
		JLabel label1 = new JLabel("Project name:");
		label1.setBounds(currentXAxis, currentYAxis, 100, 20);
		panel.add(label1);
		label_netName = new JLabel("N/A");
		label_netName.setBounds(currentXAxis+label1.getWidth()+10, label1.getLocation().y, 400, 20);
		panel.add(label_netName);
		//NET NODES:
		JLabel label2 = new JLabel("Nodes:");
		label2.setBounds(currentXAxis, currentYAxis+=spacing, 100, 20);
		panel.add(label2);
		label_nodesNumber = new JLabel("N/A");
		label_nodesNumber.setBounds(currentXAxis+label2.getWidth()+10, label2.getLocation().y, numberLabelWidth, 20);
		panel.add(label_nodesNumber);
		//NET TRANSITION:
		JLabel label3 = new JLabel("Transitions:");
		label3.setBounds(currentXAxis, currentYAxis+=spacing, 100, 20);
		panel.add(label3);
		label_transitionsNumber = new JLabel("N/A");
		label_transitionsNumber.setBounds(currentXAxis+label3.getWidth()+10, label3.getLocation().y, numberLabelWidth, 20);
		panel.add(label_transitionsNumber);
		//NET PLACES:
		JLabel label4 = new JLabel("Places:");
		label4.setBounds(currentXAxis, currentYAxis+=spacing, 100, 20);
		panel.add(label4);
		label_placesNumber = new JLabel("N/A");
		label_placesNumber.setBounds(currentXAxis+label4.getWidth()+10, label4.getLocation().y, numberLabelWidth, 20);
		panel.add(label_placesNumber);
		//NET PLACES:
		JLabel label5 = new JLabel("Arcs:");
		label5.setBounds(currentXAxis, currentYAxis+=spacing, 100, 20);
		panel.add(label5);
		label_arcNumber = new JLabel("N/A");
		label_arcNumber.setBounds(currentXAxis+label5.getWidth()+10, label5.getLocation().y, numberLabelWidth, 20);
		panel.add(label_arcNumber);
		//NET INVARIANTS:
		JLabel label6 = new JLabel("Invariants:");
		label6.setBounds(currentXAxis, currentYAxis+=spacing, 100, 20);
		panel.add(label6);
		label_invNumber = new JLabel("N/A");
		label_invNumber.setBounds(currentXAxis+label6.getWidth()+10, label6.getLocation().y, numberLabelWidth, 20);
		panel.add(label_invNumber);
		
		//Panel właściwości
		staticPropertiesPanel = new JPanel(new FlowLayout());
		staticPropertiesPanel.setBorder(BorderFactory.createTitledBorder("Net static properties:"));
		staticPropertiesPanel.setBounds(label2.getLocation().x+200, label2.getLocation().y, 300, 100);
		panel.add(staticPropertiesPanel);
		
		//Panel informacji o invariantach
		JPanel invInfoPanel = new JPanel(new BorderLayout());
		invInfoPanel.setBorder(BorderFactory.createTitledBorder("Invariants details:"));
		invInfoPanel.setBounds(currentXAxis-5, currentYAxis+=spacing, 506, 325);
		
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
				    			"Petri net meaning:\n"
				    			+yesWeCan[1]
				    			+"\n\nBiological interpretation:\n"
				    			+yesWeCan[2],
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
	 * Metoda odpowiedzialna za tworzenia panelu bocznego z przyciskami okna.
	 * @return JPanel - panel boczny przycisków
	 */
	private JPanel createButtonsPanel() {
		int buttonX = 100;
		int buttonY = 40;
		int spaceY = 10;
		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createTitledBorder("Options"));
		JPanel inPanel = new JPanel();
		inPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5)); //panel z odstępami
		inPanel.setLayout(new BoxLayout(inPanel, BoxLayout.Y_AXIS));
		
		
		JButton refreshButton = new JButton("Refresh");
		refreshButton.setMinimumSize(new Dimension(buttonX, buttonY)); 
		refreshButton.setMaximumSize(new Dimension(buttonX, buttonY));
		//refreshButton.setPreferredSize(new Dimension(80,40)); //BoxLayout ma to gdzieś, dlatego patrz wyżej
		refreshButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				fillData();
			}
		});
		inPanel.add(refreshButton);
		inPanel.add(Box.createVerticalStrut(spaceY));
		
		JButton saveButton = new JButton("Save to file");
		saveButton.setMinimumSize(new Dimension(buttonX, buttonY)); 
		saveButton.setMaximumSize(new Dimension(buttonX, buttonY));
		saveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				saveToFile();
			}
		});
		inPanel.add(saveButton);
		inPanel.add(Box.createVerticalStrut(spaceY));
		
		panel.add(inPanel);
		return panel;
	}
	
	/**
	 * Metoda zapisuje aktualną wersję właściwości sieci do wskazanego pliku tekstowego.
	 */
	private void saveToFile() {
		FileFilter[] filters = new FileFilter[1];
		String lastPath = GUIManager.getDefaultGUIManager().getLastPath();
		filters[0] = new ExtensionFileFilter("Normal text file (.txt)",  new String[] { "TXT" });
		String selectedFile = Tools.selectFileDialog(lastPath, filters, "Save", "");
		
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
