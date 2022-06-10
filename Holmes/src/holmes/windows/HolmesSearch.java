package holmes.windows;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.ParseException;
import java.util.ArrayList;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.text.DefaultFormatter;

import holmes.darkgui.GUIManager;
import holmes.petrinet.elements.ElementLocation;
import holmes.petrinet.elements.Place;
import holmes.petrinet.elements.Transition;
import holmes.utilities.Tools;
import holmes.workspace.WorkspaceSheet;

/**
 * Klasa implementująca okno wyszukiwania elementów sieci - miejsc lub tranzycji. Można
 * ręcznie wskazać odpowiedni wierzchołek na listach rozwijalnych, względnie wyszukać go
 * po nazwie lub identyfikatorze.
 * @author MR
 *
 */
public class HolmesSearch extends JFrame {
	private static final long serialVersionUID = 8885161841467059860L;
	private JFrame ego;
	private ArrayList<Place> places = new ArrayList<Place>();
	private ArrayList<Transition> transitions = new ArrayList<Transition>();
	//private ArrayList<Node> nodes = new ArrayList<Node>();
	private ArrayList<Integer> foundNodes = new ArrayList<Integer>();
	private ArrayList<Integer> foundType = new ArrayList<Integer>();
	private int selectedFound = -1;
	
	private JComboBox<String> placesCombo = null;
	private JComboBox<String> transitionsCombo = null;
	private ButtonGroup group = new ButtonGroup();
	private JFormattedTextField searchField;
	private JFormattedTextField idField;
	private JRadioButton transitionMode;
	
	private JLabel nodeName;
	private JLabel nodeType;
	private JLabel nodeInArcs;
	private JLabel nodeOutArcs;
	private JLabel nodeIsPortal;
	
	private boolean listenerAllowed = true; //jeśli true, comboBoxy działają
	
	public HolmesSearch() {
		ego = this;
		ego.setTitle("Net nodes search window");
		try {
			setIconImage(Tools.getImageFromIcon("/icons/holmesicon.png"));
		} catch (Exception e ) {
			
		}
		
		setLayout(new BorderLayout());
		setSize(new Dimension(506, 248));
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
	 * Absolute positioning, deal with it.
	 * @return JPanel - panel okna
	 */
	private JPanel createMainPanel() {	
		JPanel panel = new JPanel();
		panel.setLayout(null); 
		
		//Panel wyboru opcji szukania
		JPanel choicePanel = new JPanel();
		choicePanel.setLayout(null);
		choicePanel.setBorder(BorderFactory.createTitledBorder("Search options"));
		choicePanel.setBounds(0, 0, 500, 150); // -6pikseli do rozmiaru <---> (650)
		
		int choiceColPx = 10;
		int choiceRowPx = 15;
		
		// SIMULATION MODE
		JLabel label1 = new JLabel("Places:");
		label1.setBounds(choiceColPx, choiceRowPx, 70, 20);
		choicePanel.add(label1);
		
		String[] dataP = { "---" };
		placesCombo = new JComboBox<String>(dataP); //final, aby listener przycisku odczytał wartość
		placesCombo.setLocation(choiceColPx + 75, choiceRowPx+2);
		placesCombo.setSize(400, 20);
		placesCombo.setSelectedIndex(0);
		placesCombo.setMaximumRowCount(6);
		placesCombo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				if(listenerAllowed == false) 
					return;
				int selected = placesCombo.getSelectedIndex();
				if(selected > 0) {
					listenerAllowed = false;
					transitionsCombo.setSelectedIndex(0);
					listenerAllowed = true;
					centerOnElement("place", selected -1, null);
				} else {
					clearSubPanel();
				}
			}
			
		});
		choiceRowPx += 25;
		choicePanel.add(placesCombo);
		
		JLabel label2 = new JLabel("Transitions:");
		label2.setBounds(choiceColPx, choiceRowPx, 70, 20);
		choicePanel.add(label2);
		
		String[] dataT = { "---" };
		transitionsCombo = new JComboBox<String>(dataT); //final, aby listener przycisku odczytał wartość
		transitionsCombo.setLocation(choiceColPx + 75, choiceRowPx+2);
		transitionsCombo.setSize(400, 20);
		transitionsCombo.setSelectedIndex(0);
		transitionsCombo.setMaximumRowCount(6);
		transitionsCombo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				if(listenerAllowed == false) 
					return;
				
				int selected = transitionsCombo.getSelectedIndex();
				if(selected > 0) {
					listenerAllowed = false;
					placesCombo.setSelectedIndex(0);
					listenerAllowed = true;
					centerOnElement("transition", selected -1, null);
				} else {
					clearSubPanel();
				}
			}
			
		});
		choiceRowPx += 25;
		choicePanel.add(transitionsCombo);
		
		//Fraza do szukania:
		JLabel label3 = new JLabel("Search for:");
		label3.setBounds(choiceColPx, choiceRowPx, 80, 20);
		choicePanel.add(label3);
		
		DefaultFormatter format = new DefaultFormatter();
	    format.setOverwriteMode(false);
	    searchField = new JFormattedTextField(format);
		searchField.setLocation(choiceColPx + 75, choiceRowPx);
		searchField.setSize(270, 20);
		searchField.setValue("");
		searchField.addPropertyChangeListener("value", new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent e) {
				JFormattedTextField field = (JFormattedTextField) e.getSource();
				try {
					field.commitEdit();
					String newName = (String) field.getText();
					if(newName.length() > 0)
						searchForString(newName);
				} catch (ParseException ex) {
				}
				
			}
		});
		choicePanel.add(searchField);
		
		//ID do szukania:
		JLabel label4 = new JLabel("Search ID:");
		label4.setBounds(choiceColPx+350, choiceRowPx, 80, 20);
		choicePanel.add(label4);
		
		DefaultFormatter formatID = new DefaultFormatter();
		formatID.setOverwriteMode(false);
		idField = new JFormattedTextField(format);
		idField.setLocation(choiceColPx + 415, choiceRowPx);
		idField.setSize(60, 20);
		idField.setValue("");
		idField.addPropertyChangeListener("value", new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent e) {
				JFormattedTextField field = (JFormattedTextField) e.getSource();
				try {
					field.commitEdit();
					String IDstr = (String) field.getText();
					if(IDstr.length() == 0)
						return;
					int id = Integer.parseInt(IDstr);
					selectByID(id);
				} catch (Exception ex) {
				}

			}
		});
		choicePanel.add(idField);	
		choiceRowPx += 20;
		
		JRadioButton placesMode = new JRadioButton("Search places");
		placesMode.setBounds(choiceColPx, choiceRowPx, 120, 20);
		placesMode.setLocation(choiceColPx, choiceRowPx);
		placesMode.setActionCommand("0");
		ActionListener placesModeActionListener = new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				AbstractButton aButton = (AbstractButton) actionEvent.getSource();
				if(aButton.isSelected() == true) {
					String newName = (String) searchField.getText();
					idField.setText("");
					if(newName.length() > 0)
						searchForString(newName);
					//System.out.println("Selected: " + aButton.getText());
				}
			}
		};
		placesMode.addActionListener(placesModeActionListener);
		choicePanel.add(placesMode);
		group.add(placesMode);
		
		transitionMode = new JRadioButton("Search transitions");
		transitionMode.setBounds(choiceColPx+140, choiceRowPx, 140, 20);
		transitionMode.setLocation(choiceColPx+140, choiceRowPx);
		transitionMode.setActionCommand("1");
		ActionListener transitionModeActionListener = new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				AbstractButton aButton = (AbstractButton) actionEvent.getSource();
				if(aButton.isSelected() == true) {
					String newName = (String) searchField.getText();
					idField.setText("");
					if(newName.length() > 0)
						searchForString(newName);
					//System.out.println("Selected: " + aButton.getText());
				}
			}
		};
		transitionMode.addActionListener(transitionModeActionListener);
		choicePanel.add(transitionMode);
		group.add(transitionMode);
		
		JRadioButton bothMode = new JRadioButton("Search places and transitions");
		bothMode.setBounds(choiceColPx+280, choiceRowPx, 200, 20);
		bothMode.setLocation(choiceColPx+280, choiceRowPx);
		bothMode.setActionCommand("2");
		bothMode.setVisible(false);
		choicePanel.add(bothMode);
		group.add(bothMode);
		group.setSelected(transitionMode.getModel(), true);

		choiceRowPx += 20;
		
		JButton prevButton = new JButton("Previous");
		prevButton.setBounds(choiceColPx, choiceRowPx, 120, 32);
		prevButton.setIcon(Tools.getResIcon32("/icons/searchWindow/prev.png"));
		prevButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				showFound("prev");
			}
		});
		choicePanel.add(prevButton);
		
		JButton nextButton = new JButton("Next");
		nextButton.setBounds(choiceColPx + 130, choiceRowPx, 120, 32);
		nextButton.setIcon(Tools.getResIcon32("/icons/searchWindow/next.png"));
		nextButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				showFound("next");
			}
		});
		choicePanel.add(nextButton);
		
		panel.add(choicePanel);
		
		JPanel infoPanel = new JPanel();
		infoPanel.setLayout(null);
		infoPanel.setBorder(BorderFactory.createTitledBorder("Selected node info"));
		infoPanel.setBounds(0, 150, 500, 70);
		
		int infoCol = 10;
		int infoRow = 20;
		JLabel label11 = new JLabel("Type:");
		label11.setBounds(infoCol, infoRow, 50, 20);
		infoPanel.add(label11);
		
		nodeType = new JLabel("---");
		nodeType.setBounds(infoCol+60, infoRow, 70, 20);
		infoPanel.add(nodeType);
		
		nodeName = new JLabel("---");
		nodeName.setBounds(infoCol+130, infoRow, 355, 20);
		infoPanel.add(nodeName);
		
		infoRow += 20;
		
		JLabel label12 = new JLabel("Portal:");
		label12.setBounds(infoCol, infoRow, 50, 20);
		infoPanel.add(label12);
		
		nodeIsPortal = new JLabel("---");
		nodeIsPortal.setBounds(infoCol+60, infoRow, 30, 20);
		infoPanel.add(nodeIsPortal);
		
		JLabel label13 = new JLabel("In-arcs:");
		label13.setBounds(infoCol+130, infoRow, 50, 20);
		infoPanel.add(label13);
		
		nodeInArcs = new JLabel("0");
		nodeInArcs.setBounds(infoCol+180, infoRow, 30, 20);
		infoPanel.add(nodeInArcs);
		
		JLabel label14 = new JLabel("Out-arcs:");
		label14.setBounds(infoCol+210, infoRow, 60, 20);
		infoPanel.add(label14);
		
		nodeOutArcs = new JLabel("0");
		nodeOutArcs.setBounds(infoCol+280, infoRow, 30, 20);
		infoPanel.add(nodeOutArcs);
		
		
		panel.add(infoPanel);
		panel.repaint();
		return panel;
	}

	protected void clearSubPanel() {
		nodeName.setText("---");
		nodeType.setText("---");
		nodeInArcs.setText("---");
		nodeOutArcs.setText("---");
		nodeIsPortal.setText("---");
	}

	/**
	 * Metoda odpowiedzialna za wypełnianie okna danymi, tj. aktualizuje listy rozwijalne
	 * wypełniając je aktualnymi nazwami miejsc i trazycji
	 */
	public void fillComboBoxesData() {
		places = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getPlaces();
		transitions = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions();
		
		//nodes = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getNodes();
		//int newPlaceNumber = places.size();
		//int newTransitionsNumber = transitions.size();
		//fill the data:
		//doJump = false;
		placesCombo.removeAllItems();
		placesCombo.addItem("---");
		for(int p=0; p < places.size(); p++) {
			placesCombo.addItem("p"+(p)+"."+places.get(p).getName());
		}
		transitionsCombo.removeAllItems();
		transitionsCombo.addItem("---");
		for(int t=0; t < transitions.size(); t++) {
			transitionsCombo.addItem("t"+(t)+"."+transitions.get(t).getName());
		}
		
		nodeName.setText("---");
		nodeType.setText("---");
		nodeInArcs.setText("---");
		nodeOutArcs.setText("---");
		nodeIsPortal.setText("---");
		
		if(places.size() == 0 || transitions.size() ==0)
			return; //nothing else to do here
	}
	
	/**
	 * Metoda odpowiedzialna za pokazanie na rysunku sieci wybranego elementu
	 * @param type String - place / transition
	 * @param index int - nr elementu w tablicach
	 */
	protected void centerOnElement(String type, int index, ElementLocation portalLoc) {
		/**
		// THE HOLY CODE, ZOSTAWIĆ JAKO KOMENTARZ, NIERAZ SIĘ JESZCZE PRZYDA
		ArrayList<Node> nod = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getNodes();
		if(nod.size() > 0) {
		ArrayList<ElementLocation> el = nod.get(0).getElementLocations();
		ElementLocation clicked = el.get(0);
		WorkspaceSheet ws = GUIManager.getDefaultGUIManager().getWorkspace().getSelectedSheet();
		ws.getGraphPanel().getSelectionManager().selectOneElementLocation(clicked);
						
		JScrollBar barHor =  ws.getHorizontalScrollBar();
		JScrollBar barVer =  ws.getVerticalScrollBar();
		String msg = "Horizontal: "+barHor.getValue()+ " Vertical: "+barVer.getValue();
		JOptionPane.showMessageDialog(null, msg, "Info",JOptionPane.INFORMATION_MESSAGE);
		}
		 */
		if(portalLoc == null) { //normalny tryb	
			ElementLocation loc1st = null;
			int sheetID = 0;
			if(type.equals("place")) {
				Place x = places.get(index);
				loc1st = x.getElementLocations().get(0);
				sheetID = loc1st.getSheetID();
				
				nodeName.setText(x.getName());
				nodeType.setText("place");
				nodeInArcs.setText(x.getInArcs().size()+"");
				nodeOutArcs.setText(x.getOutArcs().size()+"");
				if(x.isPortal())
					nodeIsPortal.setText("yes");
				else
					nodeIsPortal.setText("no");
			} else { //"transition"
				Transition x = transitions.get(index);
				loc1st = x.getElementLocations().get(0);
				sheetID = loc1st.getSheetID();
				
				nodeName.setText(x.getName());
				nodeType.setText("place");
				nodeInArcs.setText(x.getInArcs().size()+"");
				nodeOutArcs.setText(x.getOutArcs().size()+"");
				if(x.isPortal())
					nodeIsPortal.setText("yes");
				else
					nodeIsPortal.setText("no");
			}

			//Ustawienie zoomu na normalne 100% wyświetlania
			WorkspaceSheet ws = GUIManager.getDefaultGUIManager().getWorkspace().getSheets().get(sheetID);
			ws.getGraphPanel().setZoom(100, ws.getGraphPanel().getZoom()); //zoom na normal
			
			ws.getGraphPanel().getSelectionManager().selectOneElementLocation(loc1st); //zaznacz element
			
			//Przewinięcie na zaznaczony element:
			//CompositeTabDock xxx = GUIManager.getDefaultGUIManager().getWorkspace().getWorkspaceDock();
			//int visibleX = xxx.getWidth(); //widocza na ekranie szerokość panelu rysowania (+24)
			//int visibleY = xxx.getHeight(); //j.w. wysokość (+63)
			
			int visibleX = ws.getWidth(); 
			int visibleY = ws.getHeight(); //tyle pikseli dokładnie widać na ekranie

			int barHorX =  ws.getHorizontalScrollBar().getValue(); // aktualna wartość przesunięcia 
			int barVerY =  ws.getVerticalScrollBar().getValue();
			
			ws.scrollHorizontal(-barHorX); //przewiń na x=0
			ws.scrollVertical(-barVerY); //przewiń na y=0
			
			int locX = loc1st.getPosition().x;
			int locY = loc1st.getPosition().y;
			
			if(locX > (visibleX-100)) { //jeśli nie widać "w poziomie"
				int pushX = (locX - (visibleX-100)) + (visibleX/2);
				ws.scrollHorizontal(pushX);
			}
			if(locY > (visibleY-160)) { //jeśli nie widać "w poziomie"
				int pushY = (locY - (visibleY-160)) + (visibleY/2);
				ws.scrollVertical(pushY);
			}
			
		} else { 
			//TODO: skakanie po portalach
		}
	}
	
	/**
	 * Metoda wybierająca i wskazująca element sieci po jego ID
	 * @param id int - id wierzchołka
	 */
	private void selectByID(int id) {
		int mode = Integer.valueOf(group.getSelection().getActionCommand());
		if(mode == 0) {
			int maxPlaces = places.size();
			if(id < maxPlaces) {
				placesCombo.setSelectedIndex(id+1);
			}
		} else if (mode == 1) {
			int maxTransitions = transitions.size();
			if(id < maxTransitions) {
				transitionsCombo.setSelectedIndex(id+1);
			}
		} else {
			group.setSelected(transitionMode.getModel(), true);
			selectByID(id);
		}
	}
	
	/**
	 * Metoda wyszukuje miejsce lub tranzycję zawierającą dany podciąg znaków.
	 * @param searchString String - podciąg znaków
	 */
	private void searchForString(String searchString) {
		int mode = Integer.valueOf(group.getSelection().getActionCommand());
		searchString = searchString.toLowerCase();
		selectedFound = -1;
		foundNodes.clear();
		foundType.clear();
		if(mode == 0) {
			int id = 99999;
			int maxPlaces = places.size();
			foundNodes.clear();
			for(int i=0; i<maxPlaces; i++) {
				if(places.get(i).getName().toLowerCase().contains(searchString)) {
					if(id == 99999)
						id = i;
					foundNodes.add(i);
					foundType.add(mode);
					selectedFound = 0;
				}
			}
			
			if(id < maxPlaces) {
				placesCombo.setSelectedIndex(id+1);
			}
		} else if (mode == 1) {
			int maxTransitions = transitions.size();
			int id = 99999;;
			foundNodes.clear();
			for(int i=0; i<maxTransitions; i++) {
				if(transitions.get(i).getName().toLowerCase().contains(searchString)) {
					if(id == 99999)
						id = i;
					foundNodes.add(i);
					foundType.add(mode);
					selectedFound = 0;
				}
			}
			
			if(id < maxTransitions) {
				transitionsCombo.setSelectedIndex(id+1);
			}
		} else {
			group.setSelected(transitionMode.getModel(), true);
			searchForString(searchString);
		}
	}
	
	/**
	 * Metoda odpowiedzialna za przeskakiwanie po miejsach lub tranzycjach znalezionych
	 * wcześniej.
	 * @param mode String - prev lub next, czyli poprzednie/następne znalezione
	 */
	private void showFound(String mode) {
		try {
		if(mode.equals("prev")) {
			if(foundNodes.size() > 0 && selectedFound > 0) {
				selectedFound--;
				int id = foundNodes.get(selectedFound);
				if(foundType.get(selectedFound)==0) { //jeśli to miejsce
					placesCombo.setSelectedIndex(id+1);
				} else {
					transitionsCombo.setSelectedIndex(id+1);
				}
			}
		} else if(mode.equals("next")) {
			if(foundNodes.size() > 0 && selectedFound + 1 < foundNodes.size()) {
				selectedFound++;
				int id = foundNodes.get(selectedFound);
				if(foundType.get(selectedFound)==0) { //jeśli to miejsce
					placesCombo.setSelectedIndex(id+1);
				} else {
					transitionsCombo.setSelectedIndex(id+1);
				}
			}
		}
		} catch (Exception e) {
			return;
		}
	}
	
	/**
	 * Metoda ustawia odpowiedni comboBox i powoduje przeskok w oknie programu na miejsce lub tranzycję.
	 * Wywoływana z far far away, np. z okna symulatora stanów.
	 * @param place boolean - true, jeśli miejsca, false - tranzycje
	 * @param index int - indeks na comboBox
	 */
	public void selectedManually(boolean place, int index) {
		try {
			if(place) {
				placesCombo.setSelectedIndex( index+1);
			} else {
				transitionsCombo.setSelectedIndex(index+1);
			}
		} catch (Exception e) {
			@SuppressWarnings("unused")
			int x=1;
		}
	}
	
	/**
	 * Inicjalizacja agentów nasłuchujących różnych zdarzeń dla okna poszukiwania.
	 */
    private void initiateListeners() {
    	addWindowListener(new WindowAdapter() {
  	  	    public void windowActivated(WindowEvent e) {
  	  	    	fillComboBoxesData();
  	  	    	searchField.requestFocusInWindow();
  	  	    }  
    	});
    }
}
