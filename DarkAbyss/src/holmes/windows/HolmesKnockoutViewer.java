package holmes.windows;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;

import holmes.darkgui.GUIManager;
import holmes.graphpanel.MauritiusMapPanel.MapElement;
import holmes.petrinet.data.PetriNet;
import holmes.petrinet.elements.Transition;
import holmes.tables.InvariantsSimpleTableModel;
import holmes.utilities.Tools;

public class HolmesKnockoutViewer extends JFrame {
	private static final long serialVersionUID = -6944527110471274930L;
	private GUIManager overlord;
	@SuppressWarnings("unused")
	private HolmesKnockout parentWindow;
	private PetriNet pn;
	private MapElement data;
	private ArrayList<Transition> transitions;
	
	private InvariantsSimpleTableModel tableModel;
	private JTable table;
	
	public HolmesKnockoutViewer(MapElement data, HolmesKnockout holmesKnockout) {
		this.overlord = GUIManager.getDefaultGUIManager();
		this.pn = overlord.getWorkspace().getProject();
		this.parentWindow = holmesKnockout;
		this.data = data;
		this.transitions = pn.getTransitions();
		//parentWindow.setEnabled(false);
		
		initalizeComponents();
	    initiateListeners();
	    setVisible(true);
	}

	/**
	 * Główna metoda tworząca panele okna.
	 */
	private void initalizeComponents() {
		try {
			setIconImage(Tools.getImageFromIcon("/icons/blackhole.png"));
		} catch (Exception e ) { }
		setLayout(new BorderLayout());
		setSize(new Dimension(640, 400));
		setLocation(50, 50);
		setResizable(false);
		setTitle("Holmes Inv-Knockout Details window");
		setLayout(new BorderLayout());
		JPanel main = new JPanel(new BorderLayout());
		main.add(getUpperPanel(), BorderLayout.NORTH);
		main.add(getBottomPanel(), BorderLayout.CENTER);
		add(main, BorderLayout.CENTER);
	}

	/**
	 * Tworzy panel przycisków bocznych.
	 * @return JPanel - panel
	 */
	public JPanel getUpperPanel() {
		JPanel result = new JPanel(null);
		result.setBorder(BorderFactory.createTitledBorder("Knockout element general information"));
		result.setPreferredSize(new Dimension(640, 130));

		int posXda = 10;
		int posYda = 20;

		JLabel label0 = new JLabel("Element: ");
		label0.setBounds(posXda, posYda, 70, 20);
		result.add(label0);
		
		int t_index = data.node.transLocation;
		ArrayList<Integer> mctSet = checkMCT(t_index);
		
		JLabel labelElement = new JLabel("");
		labelElement.setBounds(posXda+100, posYda, 400, 20);
		result.add(labelElement);

		if(mctSet.size() > 1) {
			labelElement.setText("MCT #"+mctSet.get(0));
			
			JLabel label2 = new JLabel("MCT set:");
			label2.setBounds(posXda+210, posYda, 80, 20);
			result.add(label2);
			
			JComboBox<String> mctTransCombo = new JComboBox<String>();
			for(int i=1; i < mctSet.size(); i++) {
				String transName = "t"+mctSet.get(i)+"_"+transitions.get(mctSet.get(i)).getName();
				mctTransCombo.addItem(transName);
			}
			mctTransCombo.setBounds(posXda+270, posYda, 340, 20);

			result.add(mctTransCombo);
		} else {
			
			labelElement.setText("t"+t_index+"_"+transitions.get(t_index).getName());
		}
		
		JLabel label3 = new JLabel("Dependent invariants:");
		label3.setBounds(posXda, posYda+=20, 150, 20);
		result.add(label3);
	
		JLabel label4 = new JLabel(data.node.transFrequency+"");
		label4.setBounds(posXda+160, posYda, 30, 20);
		result.add(label4);
		
		JLabel label5 = new JLabel("Unaffected invariants:");
		label5.setBounds(posXda+190, posYda, 150, 20);
		result.add(label5);
		
		JLabel label6 = new JLabel(data.node.othersFrequency+"");
		label6.setBounds(posXda+350, posYda, 150, 20);
		result.add(label6);
		
		JLabel label7 = new JLabel("Knockout path:");
		label7.setBounds(posXda, posYda+=20, 150, 20);
		result.add(label7);
		
		JTextArea descriptionTextArea = new JTextArea();
		descriptionTextArea.setLineWrap(true);
		descriptionTextArea.setEditable(false);
        JPanel descPanel = new JPanel();
        descPanel.setLayout(new BorderLayout());
        descPanel.add(new JScrollPane(descriptionTextArea), BorderLayout.CENTER);
        descPanel.setBounds(posXda, posYda+=20, 600, 40);
        result.add(descPanel);
        
        computePath(descriptionTextArea);
		
	    return result;
	}

	/**
	 * Tworzy dolny panel okna - tabeli.
	 * @return JPanel - panel
	 */
	public JPanel getBottomPanel() {
		JPanel result = new JPanel(new BorderLayout());
		result.setBorder(BorderFactory.createTitledBorder("Tables"));
		result.setPreferredSize(new Dimension(640, 320));
		
		tableModel = new InvariantsSimpleTableModel();
		table = new JTable(tableModel);
		table.setName("Invariant description");
		
		table.getColumnModel().getColumn(0).setHeaderValue("ID");
		table.getColumnModel().getColumn(0).setPreferredWidth(60);
		table.getColumnModel().getColumn(0).setMinWidth(60);
		table.getColumnModel().getColumn(0).setMaxWidth(60);
		table.getColumnModel().getColumn(1).setHeaderValue("Invariant description");
		table.getColumnModel().getColumn(1).setPreferredWidth(800);
		table.getColumnModel().getColumn(1).setMinWidth(100);
		
		table.addMouseListener(new MouseAdapter() {
        	public void mouseClicked(MouseEvent e) {
          	    if (e.getClickCount() == 1) {
          	    	if(e.isControlDown() == false)
          	    		cellClickAction();
          	    }
          	 }
      	});
		
		table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		JScrollPane tableScrollPane = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		result.add(tableScrollPane, BorderLayout.CENTER);

		ArrayList<Integer> invariants = data.node.myInvariantsIDs;
		for(Integer invIndex : invariants) {
			tableModel.addNew(invIndex, pn.getT_InvDescription(invIndex));
		}
		
	    return result;
	}
	
	/**
	 * Metoda odpowiedzialna za obsługę kliknięcia elementu tabeli inwariantów.
	 */
	protected void cellClickAction() {
		try {
			int row = table.getSelectedRow();
			if(table.getName().equals("Invariant description")) {
				String id = table.getValueAt(row, 0).toString();
				int invID = Integer.parseInt(id);
				new HolmesInvariantsViewer(invID);
			}

		} catch (Exception e) {
			
		}
	}
	
	/**
	 * Metoda przetwarza i wyświetla ścieżkę wyłączeń do punktu, który reprezentuje to okno.
	 * @param descriptionTextArea JTextArea - pole tekstowe
	 */
	private void computePath(JTextArea descriptionTextArea) {
		ArrayList<Integer> path = data.disabledHistory;
		ArrayList<Integer> processed = new ArrayList<>();
		
		String resultPath = "";
		for(Integer trans : path) {
			if(processed.contains(trans))
				continue;
			
			ArrayList<Integer> mct = checkMCT(trans);
			if(mct.size() > 1) {
				resultPath += "MCT#"+mct.get(0)+" ==> ";
				
				for(int i=1; i<mct.size(); i++) {
					processed.add(mct.get(i));
				}
			} else {
				resultPath += "t"+trans+" ==> ";
				processed.add(trans);
			}
		}
		
		if(resultPath.length() > 5) 
			resultPath = resultPath.substring(0, resultPath.length()-4);
		
		descriptionTextArea.setText(resultPath);
	}

	/**
	 * Zwraca tablicę, w której znajdują się wszystkie elementy zbioru MCT w którym występuje tranzycja o indeksie
	 * przesłanym do tej metody. Wektor ma tylko element -1, jeśli nie znaleziono nietrywialy MCT z tranzycją.
	 * @param t_index int - indeks poszukiwanej w mct tranzycji
	 * @return ArrayList[Integer] - indeksy tranzycji zbioru MCT z t_index
	 */
	private ArrayList<Integer> checkMCT(int t_index) {
		ArrayList<Integer> result = new ArrayList<Integer>();
		boolean found = false;
		ArrayList<ArrayList<Transition>> mcts = pn.getMCTMatrix();
		int mctNumber = mcts.size() - 1; //ostatnie to trywialne
		Transition target = transitions.get(t_index);
		
		for(int m=0; m<mctNumber; m++) {
			if(mcts.get(m).contains(target)) {
				ArrayList<Transition> mct = mcts.get(m);
				result.add(m+1);
				
				for(Transition trans : mct) {
					result.add(transitions.indexOf(trans));
				}
				
				found = true;
				break;
			}
		}
		
		if(!found)
			result.add(-1);
		
		return result;
	}

	/**
	 * Inicjalizacja agentów nasłuchujących różnych zdarzeń dla okna poszukiwania.
	 */
    private void initiateListeners() { //HAIL SITHIS
    	addWindowListener(new java.awt.event.WindowAdapter() {
		    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
		    	//parentWindow.setEnabled(true);
		    }
		});
    }
}
