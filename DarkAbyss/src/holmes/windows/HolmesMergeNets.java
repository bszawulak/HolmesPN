package holmes.windows;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javax.xml.parsers.SAXParser;

import com.sun.scenario.effect.Merge;

import holmes.darkgui.GUIManager;
import holmes.files.io.Snoopy.NetHandler_Classic;
import holmes.files.io.Snoopy.NetHandler_Colored;
import holmes.files.io.Snoopy.NetHandler_Extended;
import holmes.files.io.Snoopy.NetHandler_Time;
import holmes.files.io.Snoopy.SnoopyReader;
import holmes.graphpanel.GraphPanel;
import holmes.petrinet.data.IdGenerator;
import holmes.petrinet.data.PetriNet;
import holmes.petrinet.elements.Arc;
import holmes.petrinet.elements.ElementLocation;
import holmes.petrinet.elements.Node;
import holmes.petrinet.elements.Place;
import holmes.petrinet.elements.Transition;
import holmes.tables.MergeNodesTableModel;
import holmes.utilities.Tools;
import holmes.workspace.ExtensionFileFilter;
import holmes.workspace.Workspace;

/**
 * Okno łączenia sieci.
 * 
 * @author MR
 */
public class HolmesMergeNets extends JFrame {
	private static final long serialVersionUID = -1099324397672549971L;
	private GUIManager overlord;
	private PetriNet pn;
	private JFrame ego;
	private ArrayList<Transition> transitions;
	private ArrayList<Place> places;
	private ArrayList<Arc> arcs;
	private ArrayList<Node> nodes;
	
	private ArrayList<Transition> newTransitions;
	private ArrayList<Place> newPlaces;
	private ArrayList<Node> newNodes;
	private ArrayList<Arc> newArcs;
	
	private JTable projectTable;
	private JTable mergeTable;
	private JTable importTable;
	private MergeNodesTableModel projectTableModel;
	private MergeNodesTableModel mergeTableModel;
	private MergeNodesTableModel importTableModel;
	
	private boolean transitionView = true;
	
	private class DataTuple{
		int n_org;
		int n_imp;
		public DataTuple(int x, int y) {
			n_org = x;
			n_imp = y;
		}
	}

	/**
	 * Konstruktor okna łączenia sieci
	 */
	public HolmesMergeNets() {
		this.overlord = GUIManager.getDefaultGUIManager();
		this.pn = overlord.getWorkspace().getProject();
		this.ego = this;
		
		places = pn.getPlaces();
		transitions = pn.getTransitions();
		arcs = pn.getArcs();
		nodes = pn.getNodes();
		
		initalizeComponents();
    	initiateListeners();
    	setVisible(true);
    	
    	fillProjectTable();
    	
    	overlord.getFrame().setEnabled(false);
	}

	/**
	 * Główna metoda tworząca panele okna.
	 */
	private void initalizeComponents() {
		try {
			setIconImage(Tools.getImageFromIcon("/icons/holmesicon.png"));
		} catch (Exception e ) { 
			
		}
		setLayout(new BorderLayout());
		setSize(new Dimension(1024, 800));
		setLocation(50, 50);
		setResizable(true);
		setTitle("Holmes net joining tool");
		setLayout(new BorderLayout());
		JPanel main = new JPanel(new BorderLayout());
		main.add(getUpperPanel(), BorderLayout.NORTH);
		main.add(getMainPanel(), BorderLayout.CENTER);
		add(main, BorderLayout.CENTER);
	}
	
	/**
	 * Tworzy panel przycisków górnych.
	 * @return JPanel - panel
	 */
	public JPanel getUpperPanel() {
		JPanel result = new JPanel(null);
		result.setBorder(BorderFactory.createTitledBorder("Buttons"));
		result.setPreferredSize(new Dimension(800, 80));

		int posXda = 10;
		int posYda = 25;

		
		JButton loadSnoopyButton = new JButton("<html>Load Snoopy<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;file&nbsp;&nbsp;&nbsp;&nbsp;</html>");
		loadSnoopyButton.setBounds(posXda, posYda, 150, 36);
		loadSnoopyButton.setMargin(new Insets(0, 0, 0, 0));
		loadSnoopyButton.setFocusPainted(false);
		loadSnoopyButton.setIcon(Tools.getResIcon32("/icons/mergeWindow/importFromSnoopy.png"));
		loadSnoopyButton.setToolTipText("Load net from Snoopy file to merge with current one");
		loadSnoopyButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				handleSnoopyImport();
			}
		});
		result.add(loadSnoopyButton);
		
		JButton loadHolmesButton = new JButton("<html>Load Holmes<br>&nbsp;&nbsp;&nbsp;project&nbsp;</html>");
		loadHolmesButton.setBounds(posXda+160, posYda, 150, 36);
		loadHolmesButton.setMargin(new Insets(0, 0, 0, 0));
		loadHolmesButton.setFocusPainted(false);
		loadHolmesButton.setIcon(Tools.getResIcon32("/icons/mergeWindow/importFromHolmes.png"));
		loadHolmesButton.setToolTipText("Load net from Holmes project file to merge with current one");
		loadHolmesButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				;
			}
		});
		result.add(loadHolmesButton);
		
		JButton mergeButton = new JButton("<html>Merge nets</html>");
		mergeButton.setBounds(posXda+320, posYda, 150, 36);
		mergeButton.setMargin(new Insets(0, 0, 0, 0));
		mergeButton.setFocusPainted(false);
		mergeButton.setIcon(Tools.getResIcon32("/icons/mergeWindow/mergeNet.png"));
		mergeButton.setToolTipText("Integrate project net and imported net");
		mergeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				mergeNets();
			}
		});
		result.add(mergeButton);

	    return result;
	}

	/**
	 * Tworzy dolny panel okna - tabele.
	 * @return JPanel - panel
	 */
	public JPanel getMainPanel() {
		JPanel results = new JPanel(new BorderLayout());
		results.setPreferredSize(new Dimension(1000, 800));
		
		JPanel tablesPanel = new JPanel();
		tablesPanel.setPreferredSize(new Dimension(1000, 600));
		BoxLayout boxLayout = new BoxLayout(tablesPanel, BoxLayout.X_AXIS);
		tablesPanel.setLayout(boxLayout);

		JPanel leftTablePanel = new JPanel(new BorderLayout());
		leftTablePanel.setBorder(BorderFactory.createTitledBorder("Active project net"));
		leftTablePanel.setPreferredSize(new Dimension(440, 800));
		
		JPanel middleTablePanel = new JPanel(new BorderLayout());
		middleTablePanel.setBorder(BorderFactory.createTitledBorder("Interface elements"));
		middleTablePanel.setPreferredSize(new Dimension(150, 800));
		middleTablePanel.setMinimumSize(new Dimension(150, 800));
		middleTablePanel.setMaximumSize(new Dimension(150, 800));
		
		JPanel rightTablePanel = new JPanel(new BorderLayout());
		rightTablePanel.setBorder(BorderFactory.createTitledBorder("Loaded net"));
		rightTablePanel.setPreferredSize(new Dimension(440, 800));
		

		//tabelka węzłow projektu
		projectTableModel = new MergeNodesTableModel();
		projectTable = new JTable(projectTableModel);
		projectTable.getColumnModel().getColumn(0).setHeaderValue("ID");
		projectTable.getColumnModel().getColumn(0).setPreferredWidth(30);
		projectTable.getColumnModel().getColumn(0).setMinWidth(30);
		projectTable.getColumnModel().getColumn(0).setMaxWidth(30);
		projectTable.getColumnModel().getColumn(0).setResizable(false);
		projectTable.getColumnModel().getColumn(1).setHeaderValue("Name:");
		//projectTable.getColumnModel().getColumn(1).setPreferredWidth(300);
		projectTable.getColumnModel().getColumn(1).setMinWidth(100);
    	projectTable.getColumnModel().getColumn(2).setHeaderValue("Pre:");
    	projectTable.getColumnModel().getColumn(2).setPreferredWidth(40);
    	projectTable.getColumnModel().getColumn(2).setMinWidth(40);
    	projectTable.getColumnModel().getColumn(2).setMaxWidth(40);
    	projectTable.getColumnModel().getColumn(3).setHeaderValue("Post:");
    	projectTable.getColumnModel().getColumn(3).setPreferredWidth(40);
    	projectTable.getColumnModel().getColumn(3).setMinWidth(40);
    	projectTable.getColumnModel().getColumn(3).setMaxWidth(40);
    	projectTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
    	projectTable.setFillsViewportHeight(true);
    	JScrollPane tableScrollPane = new JScrollPane(projectTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		leftTablePanel.add(tableScrollPane, BorderLayout.CENTER);
		
		//tabela łącząca
		mergeTableModel = new MergeNodesTableModel();
		mergeTable = new JTable(mergeTableModel);

		mergeTable.getColumnModel().getColumn(1).setHeaderValue("Merge:");
		mergeTable.getColumnModel().getColumn(1).setPreferredWidth(130);
		
		mergeTable.removeColumn(mergeTable.getColumnModel().getColumn(3));
		mergeTable.removeColumn(mergeTable.getColumnModel().getColumn(2));
		mergeTable.removeColumn(mergeTable.getColumnModel().getColumn(0));

    	mergeTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
    	mergeTable.setFillsViewportHeight(true);
		JScrollPane tableScrollPaneRightMiddle = new JScrollPane(mergeTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		middleTablePanel.add(tableScrollPaneRightMiddle, BorderLayout.CENTER);
		
		InputMap im = mergeTable.getInputMap(JTable.WHEN_FOCUSED);
		ActionMap am = mergeTable.getActionMap();
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "DeleteRow");
		am.put("DeleteRow", new AbstractAction() {
			private static final long serialVersionUID = 7224127781074461772L;

			public void actionPerformed(ActionEvent e) {
		        int row = mergeTable.getSelectedRow();
		        if (row > -1) {
		        	String value = (String) mergeTable.getValueAt(row, 0);
		            ((MergeNodesTableModel) mergeTable.getModel()).removeRow(value);
		        }
		        ((MergeNodesTableModel) mergeTable.getModel()).fireTableDataChanged();
		    }
		});
		
		//tabela zaimportowanej sieci
		importTableModel = new MergeNodesTableModel();
		importTable = new JTable(importTableModel);
		importTable.getColumnModel().getColumn(0).setHeaderValue("ID");
		importTable.getColumnModel().getColumn(0).setPreferredWidth(30);
		importTable.getColumnModel().getColumn(0).setMinWidth(30);
		importTable.getColumnModel().getColumn(0).setMaxWidth(30);
		importTable.getColumnModel().getColumn(1).setHeaderValue("Name:");
		importTable.getColumnModel().getColumn(1).setPreferredWidth(300);
		importTable.getColumnModel().getColumn(1).setMinWidth(100);
		importTable.getColumnModel().getColumn(2).setHeaderValue("Pre:");
		importTable.getColumnModel().getColumn(2).setPreferredWidth(40);
    	importTable.getColumnModel().getColumn(2).setMinWidth(40);
    	importTable.getColumnModel().getColumn(2).setMaxWidth(40);
    	importTable.getColumnModel().getColumn(3).setHeaderValue("Post:");
    	importTable.getColumnModel().getColumn(3).setPreferredWidth(40);
    	importTable.getColumnModel().getColumn(3).setMinWidth(40);
    	importTable.getColumnModel().getColumn(3).setMaxWidth(40);
    	importTable.setFillsViewportHeight(true);
    	importTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		JScrollPane tableScrollPaneRight = new JScrollPane(importTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		rightTablePanel.add(tableScrollPaneRight, BorderLayout.CENTER);
		
		projectTable.getTableHeader().setReorderingAllowed(false);
		mergeTable.getTableHeader().setReorderingAllowed(false);
		importTable.getTableHeader().setReorderingAllowed(false);
		
		TableRowSorter<TableModel> projectSorter  = new TableRowSorter<TableModel>(projectTable.getModel());
		projectTable.setRowSorter(projectSorter);
		TableRowSorter<TableModel> mergeSorter  = new TableRowSorter<TableModel>(mergeTable.getModel());
		mergeTable.setRowSorter(mergeSorter);
		TableRowSorter<TableModel> importSorter  = new TableRowSorter<TableModel>(importTable.getModel());
		importTable.setRowSorter(importSorter);
		
		
		//top panel button:
		JPanel topPanel = new JPanel(null);
		topPanel.setBorder(BorderFactory.createTitledBorder("Options"));
		topPanel.setPreferredSize(new Dimension(1000, 60));
		
		JCheckBox placesViewCheckBox = new JCheckBox("Transition view");
		placesViewCheckBox.setBounds(10, 15, 150, 20);
		placesViewCheckBox.setSelected(true);
		placesViewCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
				if (abstractButton.getModel().isSelected())
					transitionView = true;
				else
					transitionView = false;
				
				fillProjectTable();
				fillImportTable(newNodes);
			}
		});
		topPanel.add(placesViewCheckBox);
		
		JButton mergeButton = new JButton("<html>Merge nodes</html>");
		mergeButton.setBounds(160, 15, 150, 36);
		mergeButton.setMargin(new Insets(0, 0, 0, 0));
		mergeButton.setFocusPainted(false);
		mergeButton.setIcon(Tools.getResIcon32("/icons/mergeWindow/mergeNodes.png"));
		mergeButton.setToolTipText("Set nodes for joining (imported net node will become another\ngraphical element location of a node from the project)");
		mergeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				addMergeLine();
			}
		});
		topPanel.add(mergeButton);
		
		
		JButton removeButton = new JButton("<html>Remove merge</html>");
		removeButton.setBounds(320, 15, 150, 36);
		removeButton.setMargin(new Insets(0, 0, 0, 0));
		removeButton.setFocusPainted(false);
		removeButton.setIcon(Tools.getResIcon32("/icons/mergeWindow/removeMergeLine.png"));
		removeButton.setToolTipText("Remove merge line");
		removeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				int row = mergeTable.getSelectedRow();
		        if (row > -1) {
		        	String value = (String) mergeTable.getValueAt(row, 0);
		            ((MergeNodesTableModel) mergeTable.getModel()).removeRow(value);
		        }
		        ((MergeNodesTableModel) mergeTable.getModel()).fireTableDataChanged();
			}
		});
		topPanel.add(removeButton);
		
		
		
		
		results.add(topPanel, BorderLayout.NORTH);
		results.add(tablesPanel, BorderLayout.CENTER);
		
		tablesPanel.add(leftTablePanel);
		tablesPanel.add(middleTablePanel);
		tablesPanel.add(rightTablePanel);
		
	    return results;
	}
	
	/**
	 * Przeprowadza procedurę łączenia sieci.
	 */
	private void mergeNets() {
		if(newNodes == null || newNodes.size() == 0)
			return;
		
		Dimension dimSheet0 = getSheet0NetSize(); //maksymalne rozmiary oryginalnej sieci
		Dimension dimImportedNet = getImportedNetSize(); //maksymalne rozmiary wczytanej sieci

		ArrayList<ArrayList<DataTuple>> vectors = getNodesMerge(); //pobierz dane o łączeniach
		ArrayList<DataTuple> dataTrans = vectors.get(0);
		ArrayList<DataTuple> dataPlaces = vectors.get(1);
		
		//zmień dane sieci importowanej
		ArrayList<Place> importedPlacesToRemove = new ArrayList<Place>();
		ArrayList<Transition> importedTransitionsToRemove = new ArrayList<Transition>();
		
		for(DataTuple pData : dataPlaces) {
			Place projectPlace = places.get(pData.n_org);
			Place importedPlace = newPlaces.get(pData.n_imp);
			importedPlacesToRemove.add(importedPlace);
			
			projectPlace.setPortal(true);
			for(ElementLocation el : importedPlace.getElementLocations()) {
				projectPlace.getElementLocations().add(el);
				el.setParentNode(projectPlace);
				Point oldPos = el.getPosition();
				oldPos.setLocation(oldPos.x, oldPos.y + dimSheet0.height);
				//oldPos = el.getPosition(); //check
				el.setSheetID(0);
			}
			for(ElementLocation el : importedPlace.getNamesLocations()) {
				projectPlace.getNamesLocations().add(el);
				el.setParentNode(projectPlace);
				el.setSheetID(0);
			}
		}
		for(DataTuple tData : dataTrans) {
			Transition projectTrans = transitions.get(tData.n_org);
			Transition importedTrans = newTransitions.get(tData.n_imp);
			importedTransitionsToRemove.add(importedTrans);
			
			projectTrans.setPortal(true);
			for(ElementLocation el : importedTrans.getElementLocations()) {
				projectTrans.getElementLocations().add(el);
				el.setParentNode(projectTrans);
				Point oldPos = el.getPosition();
				oldPos.setLocation(oldPos.x, oldPos.y + dimSheet0.height);
				//oldPos = el.getPosition(); //check
				el.setSheetID(0);
			}
			for(ElementLocation el : importedTrans.getNamesLocations()) {
				projectTrans.getNamesLocations().add(el);
				el.setParentNode(projectTrans);
				el.setSheetID(0);
			}
		}
		for(Place place : importedPlacesToRemove) {
			newNodes.remove(place);
		}
		for(Transition trans : importedTransitionsToRemove) {
			newNodes.remove(trans);
		}
		
		//dodaj siec importowaną do głownej:
		for(Node n : newNodes) {
			n.importOnlySetID(IdGenerator.getNextId());
			for(ElementLocation el : n.getElementLocations()) {
				Point oldPos = el.getPosition();
				oldPos.setLocation(oldPos.x, oldPos.y + dimSheet0.height);
			}
			nodes.add(n);
			if(n instanceof Place) {
				pn.accessStatesManager().addPlace();
				pn.accessSSAmanager().addPlace();
			} else if(n instanceof Transition) {
				pn.accessFiringRatesManager().addTrans();
			}
		}
		
		for(Arc a : newArcs) {
			a.importOnlySetID(IdGenerator.getNextId());
			arcs.add(a);
		}
		
		//powieksz panel:
		try {
			GraphPanel graphPanel = overlord.getWorkspace().getSheets().get(0).getGraphPanel();
			int width = dimSheet0.width;
			int height = dimSheet0.height;
			int addW = dimImportedNet.width;
			int addHeight = dimImportedNet.height;
			graphPanel.setSize(new Dimension(width+addW+200, height+addHeight+100));
			graphPanel.setOriginSize(new Dimension(width+addW+200, height+addHeight+100));
			
		} catch (Exception e) {
			GUIManager.getDefaultGUIManager().log("Error: cannot resize sheets.", "error", true);	
		}
		
		overlord.reset.reset2ndOrderData(true);
		overlord.markNetChange();
		
		pn.repaintAllGraphPanels();
	}
	
	/**
	 * Zwraca 2 wektory danych liczowych: t_x <== t_y; p_x <== p_y
	 * @return ArrayList[ArrayList[DataTuple]] - wektory
	 */
	private ArrayList<ArrayList<DataTuple>> getNodesMerge() {
		try {
			ArrayList<ArrayList<DataTuple>> result = new ArrayList<>();
			ArrayList<String> tableData = mergeTableModel.getMergeVector();
			ArrayList<DataTuple> dataPlaces = new ArrayList<DataTuple>();
			ArrayList<DataTuple> dataTrans = new ArrayList<DataTuple>();
			
			for(String str : tableData) {
				String firstInt = str.substring(1, str.indexOf(" "));
				String tmp = str.substring(str.indexOf(" <== ")+6);
				String secondInt = tmp;
				DataTuple newP = new DataTuple(Integer.parseInt(firstInt), Integer.parseInt(secondInt));
				
				if(str.substring(0,1).equals("p")) {
					dataPlaces.add(newP);
				} else {
					dataTrans.add(newP);
				}
			}
			result.add(dataTrans);
			result.add(dataPlaces);
			return result;
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Zwraca rozmiary sieci z arkusza 0.
	 * @return Dimension - maksymalne x i y elementLocation's z sheet0
	 */
	private Dimension getSheet0NetSize() {
		Dimension dimSheet0 = new Dimension(0, 0);
		for(Node n : nodes) {
			for(ElementLocation el: n.getElementLocations()) {
				int subNet = el.getSheetID();
				if(subNet != 0) //tylko głowny arkusz
					continue;
				
				if(dimSheet0.getWidth() < el.getPosition().x)
					dimSheet0.setSize(el.getPosition().x, dimSheet0.getHeight());
			
				if(dimSheet0.getHeight() < el.getPosition().y)
					dimSheet0.setSize(dimSheet0.getWidth(), el.getPosition().y);
			}
		} //teraz mamy maksymalne rozmiary oryginalnej sieci
		return dimSheet0;
	}
	
	/**
	 * Zwraca maksymalne rozmiary sieci wczytanej
	 * @return
	 */
	private Dimension getImportedNetSize() {
		Dimension dimSheet0 = new Dimension(0, 0);
		
		for(Node n : newNodes) {
			for(ElementLocation el: n.getElementLocations()) {
				int subNet = el.getSheetID();
				if(subNet != 0) //tylko głowny arkusz
					continue;
				if(dimSheet0.getWidth() < el.getPosition().x)
					dimSheet0.setSize(el.getPosition().x, dimSheet0.getHeight());
			
				if(dimSheet0.getHeight() < el.getPosition().y)
					dimSheet0.setSize(dimSheet0.getWidth(), el.getPosition().y);
			}
		} //teraz mamy maksymalne rozmiary oryginalnej sieci
		return dimSheet0;
	}

	/**
	 * Wczytywanie pliku ze Snoopiego do importu.
	 */
	protected void handleSnoopyImport() {
		String lastPath = overlord.getLastPath();
		FileFilter[] filters = new FileFilter[5];
		filters[0] = new ExtensionFileFilter("All supported Snoopy files", new String[] { "SPPED", "SPEPT", "SPTPT" });
		filters[1] = new ExtensionFileFilter("Snoopy Petri Net file (.spped)", new String[] { "SPPED" });
		filters[2] = new ExtensionFileFilter("Snoopy Extended PN file (.spept)", new String[] { "SPEPT" });
		filters[3] = new ExtensionFileFilter("Snoopy Time PN file (.sptpt)", new String[] { "SPTPT" });
		filters[4] = new ExtensionFileFilter(".pnt - INA PNT file (.pnt)", new String[] { "PNT" });
		String selectedFile = Tools.selectFileDialog(lastPath, filters,  "Select PN", "Select petri net file", "");
		if(selectedFile.equals(""))
			return;
		
		File file = new File(selectedFile);
		if(!file.exists())
			return;
		
		String path = file.getPath();
		if (path.endsWith(".spped") || path.endsWith(".spept") || path.endsWith(".colpn") || path.endsWith(".sptpt")) {
			SnoopyReader reader = new SnoopyReader(0, path);
			newNodes = reader.getNodesList();
			newArcs = reader.getArcList();
		}
		
		newTransitions = new ArrayList<Transition>();
		newPlaces = new ArrayList<Place>();
		for(Node n : newNodes) {
			if(n instanceof Transition)
				newTransitions.add((Transition) n);
			else if(n instanceof Place)
				newPlaces.add((Place) n);
		}
		
		if(newNodes.size() > 0 && newArcs.size() > 0) {
			fillImportTable(newNodes);
			
		} else {
			JOptionPane.showMessageDialog(ego,"Unable to continue. Loaded nodes and/or arcs sets are empty.", 
					"Error: no data", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	/**
	 * Dodaje linię łącząca miejsca i tranzycje
	 */
	protected void addMergeLine() {
		int leftSelection = projectTable.getSelectedRow();
		int rightSelection = importTable.getSelectedRow();
		if(leftSelection < 0 || rightSelection < 0)
			return;
		
		String leftTableValue = ""+ projectTable.getValueAt(leftSelection, 0);
		String rightTableValue = ""+ importTable.getValueAt(rightSelection, 0);
		
		String prefix = "p";
		if(transitionView)
			prefix = "t";
		
		String rowString = prefix+leftTableValue + " <== " + prefix+rightTableValue;
		
		if(mergeTableModel.elementIndex(rowString) == -1) {
			mergeTableModel.addNew(-1, rowString, 0, 0);
			((MergeNodesTableModel) mergeTable.getModel()).fireTableDataChanged();
		} else {
			JOptionPane.showMessageDialog(ego, "Row already present in the merge table!", 
					"Value present", JOptionPane.INFORMATION_MESSAGE);
		}
	}
	
	/**
	 * Wepełnia lewą tabelę projektu
	 */
	private void fillProjectTable() {
		if(transitionView) {
			projectTableModel.clearModel();
	    	
			int id = 0;
			for(Transition trans : transitions) {
				projectTableModel.addNew(id++, trans.getName(), trans.getPrePlaces().size(), trans.getPostPlaces().size());
			}
		} else {
			projectTableModel.clearModel();
	    	
			int id = 0;
			for(Place place : places) {
				projectTableModel.addNew(id++, place.getName(), place.getPreTransitions().size(), place.getPostTransitions().size());
			}
		}
		projectTable.setFillsViewportHeight(true); //żadnych update cośtam i innych!
		projectTable.validate();
	}
	
	/**
	 * Wypełnianie tabeli elementów importowanych.
	 * @param nodes ArrayList[Node] - wczytane z pliku
	 */
	private void fillImportTable(ArrayList<Node> nodes) {
		if(nodes == null || nodes.size() == 0)
			return;
		
		if(transitionView) {
			importTableModel.clearModel();
	    	
			int id = 0;
			for(Node trans : nodes) {
				if(trans instanceof Transition) {
					importTableModel.addNew(id++, trans.getName(), ((Transition)trans).getPrePlaces().size(), ((Transition)trans).getPostPlaces().size());
				}
			}
		} else {
			importTableModel.clearModel();
	    	
			int id = 0;
			for(Node place : nodes) {
				if(place instanceof Place) {
					importTableModel.addNew(id++, place.getName(), ((Place)place).getPreTransitions().size(), ((Place)place).getPostTransitions().size());
				}
			}
		}
		importTable.setFillsViewportHeight(true); //żadnych update cośtam i innych!
		importTable.validate();
	}
	
	/**
	 * Inicjalizacja agentów nasłuchujących różnych zdarzeń dla okna poszukiwania.
	 */
    private void initiateListeners() { //HAIL SITHIS
    	addWindowListener(new java.awt.event.WindowAdapter() {
		    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
		    	setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		    	overlord.getFrame().setEnabled(true);
		    }
		});
    }
}
