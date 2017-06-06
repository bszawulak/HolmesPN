package holmes.graphpanel;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.swing.*;

import holmes.darkgui.GUIManager;
import holmes.graphpanel.popupmenu.ArcPopupMenu;
import holmes.graphpanel.popupmenu.MetaNodePopupMenu;
import holmes.graphpanel.popupmenu.PlacePopupMenu;
import holmes.graphpanel.popupmenu.SheetPopupMenu;
import holmes.graphpanel.popupmenu.TransitionPopupMenu;
import holmes.petrinet.data.IdGenerator;
import holmes.petrinet.data.PetriNet;
import holmes.petrinet.elements.Arc;
import holmes.petrinet.elements.ElementLocation;
import holmes.petrinet.elements.MetaNode;
import holmes.petrinet.elements.Node;
import holmes.petrinet.elements.Place;
import holmes.petrinet.elements.Transition;
import holmes.petrinet.elements.Arc.TypeOfArc;
import holmes.petrinet.elements.MetaNode.MetaType;
import holmes.petrinet.elements.PetriNetElement.PetriNetElementType;
import holmes.petrinet.elements.Transition.TransitionType;
import holmes.petrinet.simulators.NetSimulator.NetType;
import holmes.petrinet.subnets.SubnetsTools;
import holmes.utilities.Tools;
import holmes.workspace.WorkspaceSheet;

/**
 * Klasa, której zadaniem jest reprezentacja graficzna używanej w programie
 * sieci Petriego oraz oferowanie interfejsu umożliwiającego interakcję ze
 * strony użytkownika.
 * Drobne zmiany
 * 
 * @author students
 * @author MR - zmiany, zmiany, zmiany
 */
public class GraphPanel extends JComponent {
	private static final long serialVersionUID = -5746225670483573975L;
	private static final int meshSize = 20;
	private GUIManager overlord;
	private PetriNet petriNet;
	private ArrayList<Node> nodes = new ArrayList<Node>();
	private ArrayList<Arc> arcs = new ArrayList<Arc>();
	private SelectionManager selectionManager;
	private Point mousePt;// = new Point(WIDE / 2, HIGH / 2);
	private DrawModes drawMode = DrawModes.POINTER;
	private Rectangle selectingRect = null;
	private Arc drawnArc = null;
	private int sheetId;
	private boolean autoDragScroll = false;
	private boolean isSimulationActive = false;
	private int zoom = 100;
	private Dimension originSize;
	private boolean drawMesh = false;
	private boolean snapToMesh = false;
	/** TRANSITION, TIMETRANSITION, FUNCTIONALTRANS, IMMEDIATETRANS, DETERMINISTICTRANS, SCHEDULEDTRANS,
		ARC, ARC_INHIBITOR, ARC_RESET, ARC_EQUAL, READARC, ARC_MODIFIER,
		SUBNET_T, SUBNET_P, SUBNET_PT, CPLACE, CARC */
	public enum DrawModes { POINTER, ERASER, PLACE, 
		TRANSITION, TIMETRANSITION, FUNCTIONALTRANS, STOCHASTICTRANS, IMMEDIATETRANS, DETERMINISTICTRANS, SCHEDULEDTRANS,
		ARC, ARC_INHIBITOR, ARC_RESET, ARC_EQUAL, READARC, ARC_MODIFIER,
		SUBNET_T, SUBNET_P, SUBNET_PT,
		CPLACE, CARC }
	
	/** Jeśli nie jest równy null, to znaczy, że właśnie przesuwamy jakiś punkt łamiący łuk */
	public Point arcBreakPoint = null;
	public Point arcNewBreakPoint = null;

	/**
	 * Konstruktor obiektu klasy GraphPanel
	 * @param sheetId int - nr arkusza
	 * @param petriNet PetriNet - sieć Petriego
	 * @param nodesList ArrayList[Node]- lista wierzchołków
	 * @param arcsList ArrayList[Arc]- lista łuków
	 */
	public GraphPanel(int sheetId, PetriNet petriNet, ArrayList<Node> nodesList, ArrayList<Arc> arcsList) {
		this.overlord = GUIManager.getDefaultGUIManager();
		this.petriNet = petriNet;
		this.sheetId = sheetId;
		this.setNodesAndArcs(nodesList, arcsList);
		this.Initialize();
	}

	/**
	 * Inicjalizacja obiektów dla panelu
	 */
	public void Initialize() {
		this.setBorder(BorderFactory.createLineBorder(Color.lightGray));
		this.setOpaque(true);
		this.addMouseListener(new MouseHandler());
		this.addMouseMotionListener(new MouseMotionHandler());
		this.addKeyListener(new KeyboardHandler());
		this.addMouseWheelListener(new MouseWheelHandler());
		this.setSelectionManager(new SelectionManager(this));
		this.getSelectionManager().setActionListener(petriNet);
	}

	/**
	 * Metoda pozwala na ustawienie zbioru wierzchołków oraz łuków dla danego arkusza.
	 * Zbiór ten jest wspólny dla wszystkich arkuszy i jest przechowywana w obiekcie
	 * PetriNet, o tym gdzie dany element zostanie narysowany decyduje jego lokalizacja
	 * ElementLocation.SheetId.
	 * @param nodes ArrayList[Nodes] - lista węzłów przekazywana do danego arkusza
	 * @param arcs ArrayList[Arc] - lista łuków przekazywana do danego arkusza
	 */
	public void setNodesAndArcs(ArrayList<Node> nodes, ArrayList<Arc> arcs) {
		this.nodes = nodes;
		this.arcs = arcs;
		this.revalidate();
		this.repaint();
	}
	
	public void resetNodesAndArcs(ArrayList<Node> nodes, ArrayList<Arc> arcs) {
		this.nodes = nodes;
		this.arcs = arcs;
	}

	/**
	 * Metoda pozwala na ustawienie zbioru wierzchołków dla danego arkusza.
	 * Zbiór ten jest wspólny dla wszystkich arkuszy i jest przechowywana w
	 * obiekcie PetriNet, o tym gdzie dany element zostanie narysowany decyduje
	 * jego lokalizacja
	 * @param nodes ArrayList[Node] - lista węzłów przekazywana do danego arkusza
	 */
	public void setNodes(ArrayList<Node> nodes) {
		this.nodes = nodes;
		this.revalidate();
		this.repaint();
	}

	/**
	 * Metoda ustawia odpowiedni kursor w zależności od wybranego elementu sieci.
	 */
	public void setCursorForMode() {
		if (this.getDrawMode() == DrawModes.POINTER) {
			setCursor(Cursor.getDefaultCursor());
		} else {
			Toolkit toolkit = Toolkit.getDefaultToolkit();
			Image image = null;
			String modeName = "";
			try {
				modeName = this.getDrawMode().toString();
				image = Tools.getImageFromIcon("/cursors/"+ modeName + ".gif");
			} catch (Exception e ) {
				overlord.log("Critical error, no "+modeName+".gif in jar file. Thank java un-catchable exceptions...", "error", true);
				//i tak nic nie pomoże, jak powyższe się wywali. Taka nasza Java piękna i wesoła.
			}
			Point hotSpot = new Point(0, 0);
			Cursor cursor = toolkit.createCustomCursor(image, hotSpot, this.getDrawMode().toString());
			setCursor(cursor);
		}
	}

	/**
	 * Metoda odpowiedzialna za konwersję części sieci wyświetlanej w komponencie
	 * GraphPanel na obiekt typu BufferedImage.
	 * @return BufferedImage - obraz arkusza sieci
	 */
	public BufferedImage createImageFromSheet() {
		Rectangle r = getBounds();
		BufferedImage image = new BufferedImage(r.width, r.height, BufferedImage.TYPE_INT_RGB);
		Graphics g = image.getGraphics();
		g.setColor(Color.white);
		g.fillRect(0, 0, getWidth(), getHeight());
		drawPetriNet((Graphics2D) g.create());
		return image;
	}

	/**
	 * Metoda pozwala na pobranie numery identyfikacyjnego danego arkusza.
	 * @return int - zwraca id arkusza z pola sheetId
	 */
	public int getSheetId() {
		return sheetId;
	}

	/**
	 * Metoda pozwala na ustawienie nowego identyfikatora dla danego arkusza.
	 * @param sheetID int - id arkusza dla sieci
	 */
	public void setSheetId(int sheetID) {
		this.sheetId = sheetID;
	}
	
	/**
	 * Metoda pozwala na pobrania listy wierzchołków przypisanych do danego arkusza.
	 * Lista ta jest wspólna dla wszystkich arkuszy i jest przechowywana w obiekcie PetriNet.
	 * @return ArrayList[Node] - lista węzłów
	 */
	public ArrayList<Node> getNodes() {
		return this.nodes;
	}

	/**
	 * Metoda pozwala na ustawienie zbioru łuków dla danego arkusza. Zbiór ten jest wspólny
	 * dla wszystkich arkuszy, o tym gdzie dany element zostanie narysowany, decyduje jego
	 * lokalizacja startowa i końcowa (ElementLocation.SheetId).
	 * @param arcs ArrayList[Arc] - lista łuków przekazywana do danego arkusza
	 */
	public void setArcs(ArrayList<Arc> arcs) {
		this.arcs = arcs;
		this.revalidate();
		this.repaint();
	}

	/**
	 * Metoda pozwala na pobrania listy łuków przypisanych do danego arkusza. Lista ta jest
	 * wspólna dla wszystkich arkuszy i jest przechowywana w obiekcie PetriNet.
	 * @return ArrayList[Arc] - zwraca listę łuków
	 */
	public ArrayList<Arc> getArcs() {
		return arcs;
	}

	/**
	 * Metoda pozwala na pobrania aktualnego prostokąta zaznaczenia, na podstawie którego
	 * rysowany jest obszar zaznaczenia oraz wybierane są obiekty, które kwalifikują się aby
	 * został zaznaczone. W sytuacji gdy zaznaczenie nie jest rysowane, przyjmuje wartość null.
	 * @return Rectangle - prostokąd aktualnego zaznaczenia, z pola this.selectingRect
	 */
	public Rectangle getSelectingRect() {
		return selectingRect;
	}

	/**
	 * Przeciążona metoda paintComponent w klasie javax.swing.JComponent. Jej każdorazowe
	 * wywołanie powoduje wyczyszczenie aktualnego widoku i narysowanie go od nowa. W tym też
	 * momencie wybierane są odpowiednie elementy, które mają zastać narysowane na danym arkuszu, 
	 * na podstawie ich lokalizacji ElementLocation.sheetId, następnie każdemu z obiektów
	 * zakwalifikowanych, zlecane jest narysowanie "siebie" na dostarczonym w parametrze metody
	 * obiekcie Graphics2D. W rysowaniu wykorzystany został podwójny bufor oraz filtr antyaliasingowy.
	 * @param g Graphics - obiekt zawierający prezentowaną grafikę
	 */
	public void paintComponent(Graphics g) {
		g.setColor(new Color(0x00f0f0f0));
		g.fillRect(0, 0, getWidth(), getHeight());
		Graphics2D g2d = (Graphics2D) g.create();
		if (isDrawMesh())
			drawMesh(g2d);
		try {
			drawPetriNet(g2d);
		} catch (Exception e) {
			overlord.log("CRITICAL unknown and urecoverable error while drawing net. "
					+ "Loaded file probably corrupted. Restarting program.", "error", true);
			overlord.reset.emergencyRestart();
		}
	}

	/**
	 * Metoda rysująca siatkę na panelu.
	 * @param g2d Graphics2D - obiekt rysujący
	 */
	private void drawMesh(Graphics2D g2d) {
		g2d.setColor(EditorResources.graphPanelMeshColor);
		for (int i = meshSize; i < this.getWidth(); i += meshSize)
			g2d.drawLine(i, 0, i, this.getHeight());
		
		for (int i = meshSize; i < this.getHeight(); i += meshSize)
			g2d.drawLine(0, i, this.getWidth(), i);
	}

	/**
	 * Metoda odpowiedzialna za rysowanie sieci.
	 * @param g2d Graphics2D - obiekt grafiki
	 */
	public void drawPetriNet(Graphics2D g2d) {
		g2d.translate(0, 0);
		g2d.scale((float) getZoom() / 100, (float) getZoom() / 100);
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		if(overlord.getSettingsManager().getValue("editorGridLines").equals("1")) {
			g2d.setColor(Color.lightGray);
			
			int maxWidth = (getWidth() * 100)/getZoom();
			int maxHeight = (getHeight() * 100)/getZoom();
			int counterVertical = 1;
			int counterHorizontal = 1;
			for(int i=1; i<(maxWidth/20); i++) {
				g2d.drawLine(counterVertical*20, 0, counterVertical*20, maxHeight);
				counterVertical++;
			}
			
			for(int i=1; i<(maxHeight/20); i++) {
				g2d.drawLine(0, counterHorizontal*20, maxWidth, counterHorizontal*20);
				counterHorizontal++;
			}
		}
		
		ElementDrawSettings eds = new ElementDrawSettings();
		if(GUIManager.getDefaultGUIManager().getWorkspace().getProject().getSimulator().getSimNetType() == NetType.COLOR) {
			eds.color = true;
		}
		//TODO:
		
		for (Arc a : getArcs()) {
			a.draw(g2d, this.sheetId, getZoom(), eds);
		}
		if (this.isSimulationActive()) {
			for (Arc a : getArcs()) {
				a.drawSimulationToken(g2d, this.sheetId);
			}
		}
		
		for (Node n : getNodes()) {
			n.draw(g2d, this.sheetId, eds);	
		}
		
		ArrayList<ArrayList<Node>> elements = petriNet.getPNelements();
		
		for (Node n : getNodes()) {
			n.drawName(g2d, this.sheetId, elements.get(0), elements.get(1), elements.get(2), elements.get(3));
		}
		
		if (getSelectingRect() != null) {
			g2d.setColor(EditorResources.selectionRectColor);
			g2d.setStroke(EditorResources.selectionRectStroke);
			g2d.drawRoundRect(getSelectingRect().x, getSelectingRect().y, getSelectingRect().width, getSelectingRect().height, 3, 3);
			g2d.setColor(EditorResources.selectionRectFill);
			g2d.fillRoundRect(getSelectingRect().x, getSelectingRect().y, getSelectingRect().width, getSelectingRect().height, 3, 3);
		}
		if (drawnArc != null) {
			drawnArc.draw(g2d, this.sheetId, getZoom(), eds);
		}
		
		if(overlord.debug)
			debugInfo(g2d);
	}

	/**
	 * W prawym górnym rogu wyświetla informacje o ustawieniach symulatora.
	 * @param g2d Graphics2D - obiekt rysujący
	 */
	private void debugInfo(Graphics2D g2d) {
		g2d.setColor(Color.BLACK);
		g2d.setFont(new Font("TimesRoman", Font.BOLD, 15));
		
		String status = overlord.getSimulatorBox().getCurrentDockWindow().getSimulator().getSimulatorStatus().toString();
		if(status.equals("LOOP"))
			status = "Status: ACTIVE";
		else
			status = "Status: "+status;
		g2d.drawString(status, 20, 20);
		
		status = overlord.simSettings.getNetType().toString();
		status = "Status: "+status;
		g2d.drawString(status, 20, 40);
		
		boolean max = overlord.simSettings.isMaxMode();
		if(max)
			status = "Maximum mode ON";
		else
			status = "Maximum mode OFF";
		g2d.drawString(status, 20, 60);
		
		boolean single = overlord.simSettings.isSingleMode();
		if(single)
			status = "Single mode ON";
		else
			status = "Single mode OFF";
		g2d.drawString(status, 20, 80);
		
		int arcDelay = overlord.simSettings.getArcDelay();
		status = "Arc delay: "+arcDelay;
		g2d.drawString(status, 20, 100);
		
		int transDelay = overlord.simSettings.getTransDelay();
		status = "Trans. firing delay: "+transDelay;
		g2d.drawString(status, 20, 120);
	}

	/**
	 * Metoda służąca do ustawiania skali powiększenia.
	 * @param zoom int - nowa wartość powiększenia
	 */
	public void setZoom(int zoom, int oldZoom) {
		if (getOriginSize() == null)
		   setOriginSize(this.getSize());
		
		Dimension hidden = getOriginSize();
		int orgHeight = (int) hidden.getHeight();
		int orgWidth = (int) hidden.getWidth();
		
		if (getOriginSize().width * zoom / 100 < 10)
			return;

		this.zoom = zoom;
		//System.out.println(this.getOriginSize().width * zoom / 100);
		//this.setSize(this.getOriginSize().width * zoom / 100, this.getOriginSize().height * zoom / 100);
		int h = orgHeight;
		h = (int) (h * (double)zoom / (double)100);
		int w = orgWidth;
		w = (int) (w * (double)zoom / (double)100);
		this.setSize(w, h);
		WorkspaceSheet sheet = overlord.getWorkspace().getSheets().get(overlord.IDtoIndex(sheetId));
		sheet.revalidate();
		this.invalidate();
		this.repaint();
	}

	/**
	 * Metoda przewijania arkusza w poziomie za pomocą wałka myszy.
	 * @param delta int - wielkość przewinięcia
	 */
	public void scrollSheetHorizontal(int delta) {
		WorkspaceSheet sheet = overlord.getWorkspace().getSheets().get(overlord.IDtoIndex(sheetId));
		sheet.scrollHorizontal(delta);
	}
	
	/**
	 * Metoda zmiany lokalizacji nazwy wskazanego wierzchołka w poziomie.
	 * @param delta int - wielkość przewinięcia
	 * @return Point - współrzędne po zmianie
	 */
	public Point nameLocationChangeHorizontal(int delta) {
		Node n = overlord.getNameLocChangeNode();
		ElementLocation el = overlord.getNameLocChangeEL();
		
		int nameLocIndex = n.getElementLocations().indexOf(el);
		
		int oldX = n.getNamesLocations().get(nameLocIndex).getPosition().x;
		int oldY = n.getNamesLocations().get(nameLocIndex).getPosition().y;
		oldX += delta;
		
		int x = oldX+el.getPosition().x;
		int y = oldY+el.getPosition().y;
		
		if(isLegalLocation(new Point(x, y)) == true)
			n.getNamesLocations().get(nameLocIndex).getPosition().setLocation(oldX+delta, oldY);
	
		return n.getNamesLocations().get(nameLocIndex).getPosition();
	}

	/**
	 * Metoda przewijania arkusza w pionie za pomocą wałka myszy.
	 * @param delta int - wielkość przewinięcia
	 */
	public void scrollSheetVertical(int delta) {
		WorkspaceSheet sheet = overlord.getWorkspace().getSheets().get(overlord.IDtoIndex(sheetId));
		sheet.scrollVertical(delta);
	}
	
	/**
	 * Metoda zmiany lokalizacji nazwy wskazanego wierzchołka w pionie.
	 * @param delta int - wielkość przewinięcia
	 * @return Point - współrzędne po zmianie
	 */
	public Point nameLocationChangeVertical(int delta) {
		Node n = overlord.getNameLocChangeNode();
		ElementLocation el = overlord.getNameLocChangeEL();
		
		int nameLocIndex = n.getElementLocations().indexOf(el);
		
		int oldX = n.getNamesLocations().get(nameLocIndex).getPosition().x;
		int oldY = n.getNamesLocations().get(nameLocIndex).getPosition().y;
		
		oldY += delta;
		
		int x = oldX+el.getPosition().x;
		int y = oldY+el.getPosition().y;
		
		if(isLegalLocation(new Point(x, y)) == true)
			n.getNamesLocations().get(nameLocIndex).getPosition().setLocation(oldX, oldY);
		
		return n.getNamesLocations().get(nameLocIndex).getPosition();
	}

	/**
	 * Metoda realizuje zmianę rozmiaru arkusza, podczas przesuwania po jego lub poza
	 * jego obszar, elementów. Jest ona jedynie wykonywana gdy automatyczne zwiększanie
	 * rozmiaru arkusza jest aktywne (isAutoDragScroll = true). Zmiana rozmiaru liczona
	 * jest na podstawie różnicy pomiędzy wcześniejszą pozycją przeciąganego elementu a aktualną.
	 * @param currentPoint Point - aktualna pozycja przeciąganego elementu
	 * @param previousPoint Point - wcześniejsza pozycja przeciąganego elementu
	 */
	public void adjustScroll(Point currentPoint, Point previousPoint) {
		if (!isAutoDragScroll())
			return;
		WorkspaceSheet sheet = overlord.getWorkspace().getSheets().get(overlord.IDtoIndex(sheetId));
		Dimension viewSize = sheet.getViewport().getSize();
		Point delta = new Point();
		delta.setLocation(currentPoint.x - previousPoint.x, currentPoint.y - previousPoint.y);
		JViewport viewport = sheet.getViewport();
		Point viewPoint = new Point(currentPoint.x - viewport.getViewPosition().x, currentPoint.y
				- viewport.getViewPosition().y);
		if (isAutoDragScroll() && ((viewSize.width - 20) < viewPoint.x
				|| (viewSize.height - 20) < viewPoint.y || (20 > viewPoint.x || (20 > viewPoint.y)))) {
			sheet.scrollHorizontal(delta.x);
			sheet.scrollVertical(delta.y);
		}
	}

	/**
	 * Dodawanie miejsca - menu kontekstowe.
	 * @param p Point - punkt dodawania miejsca
	 */
	private void addNewPlace(Point p) {
		if (isLegalLocation(p)) {
			Place n = new Place(IdGenerator.getNextId(), this.sheetId, p);
			this.getSelectionManager().selectOneElementLocation(n.getLastLocation());
			getNodes().add(n);
			overlord.getWorkspace().getProject().accessStatesManager().addPlace();
			overlord.getWorkspace().getProject().accessSSAmanager().addPlace();
		}
	}
	
	/**
	 * Dodawanie miejsca dla sieci kolorowanej - menu kontekstowe.
	 * @param p Point - punkt dodawania miejsca
	 */
	private void addNewCPlace(Point p) {
		if (isLegalLocation(p)) {
			Place n = new Place(IdGenerator.getNextId(), this.sheetId, p);
			n.isColored = true;
			this.getSelectionManager().selectOneElementLocation(n.getLastLocation());
			getNodes().add(n);
			overlord.getWorkspace().getProject().accessStatesManager().addPlace();
			overlord.getWorkspace().getProject().accessSSAmanager().addPlace();
		}
	}

	/**
	 * Metoda związana z mousePressed(MouseEvent).
	 * @param p Point - punkt dodawania tranzycji
	 */
	private void addNewTransition(Point p) {
		if (isLegalLocation(p)) {
			Transition n = new Transition(IdGenerator.getNextId(), this.sheetId, p);
			this.getSelectionManager().selectOneElementLocation(n.getLastLocation());
			getNodes().add(n);
			overlord.getWorkspace().getProject().accessFiringRatesManager().addTrans();
		}
	}
	
	/**
	 * Metoda związana z mousePressed(MouseEvent).
	 * @param p Point - punkt dodawania tranzycji czasowej
	 */
	private void addNewTimeTransition(Point p) {
		if (isLegalLocation(p)) {
			Transition n = new Transition(IdGenerator.getNextId(),this.sheetId, p);
			n.setTransType(TransitionType.TPN);
			n.setTPNstatus(true);
			this.getSelectionManager().selectOneElementLocation(n.getLastLocation());
			getNodes().add(n);
			overlord.getWorkspace().getProject().accessFiringRatesManager().addTrans();
		}
	}
	
	/**
	 * Metoda związana z mousePressed(MouseEvent).
	 * @param p Point - punkt dodawania tranzycji funkcyjnej
	 */
	private void addNewFunctionalTransition(Point p) {
		if (isLegalLocation(p)) {
			Transition n = new Transition(IdGenerator.getNextId(),this.sheetId, p);
			n.setFunctional(true);
			this.getSelectionManager().selectOneElementLocation(n.getLastLocation());
			getNodes().add(n);
			overlord.getWorkspace().getProject().accessFiringRatesManager().addTrans();
		}
	}

	/**
	 * Metoda dodaje nowy meta-węzeł (i podsieć typu P) we wskazane miejsce.
	 * @param p Point - lokalizacja
	 */
	private void addNewSubnetP(Point p) {
		if (isLegalLocation(p)) {
			overlord.getWorkspace().newTab(true, p, this.sheetId, MetaType.SUBNETPLACE);
		}
	}
	
	/**
	 * Metoda dodaje nowy meta-węzeł (i podsieć typu T) we wskazane miejsce.
	 * @param p Point - lokalizacja
	 */
	private void addNewSubnetT(Point p) {
		if (isLegalLocation(p)) {
			overlord.getWorkspace().newTab(true, p, this.sheetId, MetaType.SUBNETTRANS);
		}
	}

	/**
	 * Metoda dodaje nowy meta-węzeł (i podsieć typu PT) we wskazane miejsce.
	 * @param p Point - lokalizacja
	 */
	private void addNewSubnetPT(Point p) {
		if (isLegalLocation(p)) {
			if(overlord.getSettingsManager().getValue("editorSnoopyCompatibleMode").equals("1")) {
					JOptionPane.showMessageDialog(null, "Snoopy compatibility mode is activated in program options.\n"
							+ "Dual interface (PT) subnetworks are not allowed.", 
							"Compatibility issue", JOptionPane.INFORMATION_MESSAGE);
					return;
			}
			overlord.getWorkspace().newTab(true, p, this.sheetId, MetaType.SUBNET);
		}
	}
	/**
	 * Metoda sprawdza czy podany punkt jest akceptowalny z punktu widzenia rozmiarów arkusza.
	 * Wykorzystywana jest ta metoda podczas przeciągania elementów po arkuszu.
	 * @param point Point - punkt, którego poprawność współrzędnych będzie sprawdzana
	 * @return boolean - true jeśli podany w parametrze punkt jest poprawny; 
	 * 		false w przypadku przeciwnym
	 */
	public boolean isLegalLocation(Point point) {
		Dimension orgSize = getOriginSize();
		int panelWidht = orgSize.width;
		int panelHeight = orgSize.height;
		
		//if(point.x > 20 && point.y > 20 && point.x < (getSize().width - 20) && point.y < (getSize().height - 20)) {
		if(point.x > 20 && point.y > 20 && point.x < (panelWidht - 20) && point.y < (panelHeight - 20)) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Usuwanie łuku - rozkaz z menu kontekstowego na łuku
	 */
	public void clearDrawnArc() {
		if (this.drawnArc != null) {
			drawnArc.unlinkElementLocations();
			drawnArc = null;
		}
	}
	
	/**
	 * Metoda zwraca obiekt przechowujący dane o całej rysowanej sieci.
	 * @return PetriNet - reprezentacja sieci w programie
	 */
	public PetriNet getPetriNet() {
		return petriNet;
	}
	
	/**
	 * Metoda pozwala na ustawienie aktualnego prostokąta zaznaczenia, na podstawie którego
	 * rysowany jest obszar zaznaczenia oraz wybierane są obiekty, które kwalifikują się aby
	 * został zaznaczone.
	 * @param selectingRect Rectangle - obszar prostokątny definiujący zaznaczenie
	 */
	public void setSelectingRect(Rectangle selectingRect) {
		this.selectingRect = selectingRect;
	}

	/**
	 * Metoda pozwala na pobranie aktualnie używanego trybu rysowania. Dostępne tryby definiowane
	 * są przez typ DrawModes.
	 * @return DrawModes - aktualny tryb rysowania, z pola this.drawMode:  TRANSITION, TIMETRANSITION, 
	 * FUNCTIONALTRANS, IMMEDIATETRANS, DETERMINISTICTRANS, SCHEDULEDTRANS, ARC, ARC_INHIBITOR, ARC_RESET, 
	 * ARC_EQUAL, READARC, ARC_MODIFIER, SUBNET_T, SUBNET_P, SUBNET_PT, CPLACE, CARC
	 */
	public DrawModes getDrawMode() {
		return this.drawMode;
	}

	/**
	 * Metoda pozwala na ustawienie aktualnego trybu rysowania na bieżącym arkuszu. Tryby
	 * definiowane są przez typ DrawModes. Ustawienie trybu rysowania powoduje zmianę
	 * kursora na arkuszu.
	 * @param newMode DrawModes - nowy tryb rysowania : TRANSITION, TIMETRANSITION, FUNCTIONALTRANS, IMMEDIATETRANS, 
	 * DETERMINISTICTRANS, CHEDULEDTRANS, ARC, ARC_INHIBITOR, ARC_RESET, ARC_EQUAL, READARC, ARC_MODIFIER, SUBNET_T, 
	 * SUBNET_P, SUBNET_PT
	 */
	public void setDrawMode(DrawModes newMode) {
		this.drawMode = newMode;
		this.setCursorForMode();
	}
	
	/**
	 * Metoda pozwala na pobranie stanu symulacji. Jeśli symulacja jest aktywna
	 * (isSimulationActive = true) wszelkie interakcje z arkuszem są zablokowane.
	 * @return boolean - true jeśli symulacja jest aktualnie aktywna;
	 * 		false jeśli symulacja jest zatrzymana
	 */
	public boolean isSimulationActive() {
		return isSimulationActive;
	}

	/**
	 * Metoda pozwala na ustawienie stanu symulacji dla danego arkusza. W sytuacji gdy symulacja
	 * jest aktywna (isSimulationActive = true) wszelkie interakcje z danym arkuszem zostają
	 * zablokowane do momentu jej zakończenia.
	 * @param isSimulationActive boolean - true jeśli symulacja ma być aktywna
	 */
	public void setSimulationActive(boolean isSimulationActive) {
		this.isSimulationActive = isSimulationActive;
	}

	/**
	 * Metoda pozwala ustawić obiekt klasy SelectionManager zarządzający zaznaczeniem
	 * na danym arkuszu.
	 * @param selectionManager SelectionManager - nowy manager zaznaczenia
	 */
	public void setSelectionManager(SelectionManager selectionManager) {
		this.selectionManager = selectionManager;
	}

	/**
	 * Metoda pozwala pobrać obiekt klasy SelectionManager zarządzający zaznaczeniem na
	 * danym arkuszu.
	 * @return SelectionManager - manager selekcji
	 */
	public SelectionManager getSelectionManager() {
		return selectionManager;
	}

	/**
	 * Metoda pozwala pobrać nowy obiekt będący menu kontekstowym dla danego arkusza.
	 * @return SheetPopupMenu - obiekt menu kontekstowego
	 */
	public SheetPopupMenu getSheetPopupMenu(PetriNetElementType pne) {
		return new SheetPopupMenu(this, pne);
	}

	/**
	 * Metoda pozwala pobrać nowy obiekt będący menu kontekstowym dla każdego miejsca.
	 * @param el ElementLocation - kliknięty element
	 * @param pne PetriNetElementType - typ klikniętego elementu
	 * @return PlacePopupMenu - obiekt menu kontekstowego
	 */
	public PlacePopupMenu getPlacePopupMenu(ElementLocation el, PetriNetElementType pne) {
		return new PlacePopupMenu(this, el, pne);
	}

	/**
	 * Metoda pozwala pobrać nowy obiekt będący menu kontekstowym dla każdej tranzycji.
	 * @param el ElementLocation - kliknięty element
	 * @param pne PetriNetElementType - typ klikniętego elementu
	 * @return TransitionPopupMenu - obiekt menu kontekstowego tranzycji
	 */
	public TransitionPopupMenu getTransitionPopupMenu(ElementLocation el, PetriNetElementType pne) {
		return new TransitionPopupMenu(this, el, pne);
	}
	
	/**
	 * Metoda pozwala pobrać nowy obiekt będący menu kontekstowym dla meta-węzła.
	 * @param el ElementLocation - kliknięty element
	 * @param pne PetriNetElementType - typ klikniętego elementu
	 * @return MetaNodePopupMenu - obiekt menu kontekstowego dla meta-węzła
	 */
	public MetaNodePopupMenu getMetaNodePopupMenu(ElementLocation el, PetriNetElementType pne) {
		return new MetaNodePopupMenu(this, el, pne);
	}

	/**
	 * Metoda pozwala pobrać nowy obiekt będący menu kontekstowym dla każdego łuku.
	 * @param arc Arc - obiekt klikniętego łuku
	 * @param pne PetriNetElementType - typ klikniętego elementu
	 * @param mousePt2  Point - kliknięty punkt
	 * @return ArcPopupMenu - obiekt menu kontekstowego
	 */
	public ArcPopupMenu getArcPopupMenu(Arc arc, PetriNetElementType pne, Point mousePt2) {
		arcNewBreakPoint = (Point) mousePt2.clone(); //będzie potrzebne (opcjonalnie) w oknie kontekstowycm
		return new ArcPopupMenu(this, arc, pne);
	}
		
	/**
	 * 
	 * @return
	 */
	public boolean isDrawMesh() {
		return drawMesh;
	}

	/**
	 * 
	 * @param drawMesh
	 */
	public void setDrawMesh(boolean drawMesh) {
		this.drawMesh = drawMesh;
		this.invalidate();
		this.repaint();
	}

	/**
	 * Metoda pozwala na sprawdzenie stanu automatycznego zwiększania rozmiaru arkusza
	 * podczas przeciągania obiektu poza jego prawy bądź dolny brzeg. Jeśli przyjmuje
	 * wartość true, to podczas przeciągania obiektów poza obszar arkusza, zostanie on
	 * automatycznie zwiększony.
	 * @return boolean - true jeśli automatyczna zmiana arkusza jest aktywna; 
	 * 		false w przypadku przeciwnym
	 */
	public boolean isAutoDragScroll() {
		return autoDragScroll;
	}

	/**
	 * Metoda pozwala na ustawienie stanu automatycznego zwiększania rozmiaru arkusza podczas
	 * przeciągania obiektu poza jego prawy bądź dolny brzeg. Jeśli przyjmuje wartość true, to
	 * podczas przeciągania obiektów poza obszar arkusza, zostanie on automatycznie zwiększony.
	 * @param autoDragScroll boolean - nowy stan automatycznego zwiększania rozmiaru arkusza
	 */
	public void setAutoDragScroll(boolean autoDragScroll) {
		this.autoDragScroll = autoDragScroll;
	}
	
	/**
	 * Nastarsi górole nie pamietajo, co to robi.
	 * @return boolean - jakaś wartość logiczna, byle dobra bo się softłyr wywoli
	 */
	public boolean isSnapToMesh() {
		return snapToMesh;
	}

	/**
	 * Patrz komentorz wyżyj. Ło Jezusicku, ale tu dziwnie.
	 * @param snapToMesh boolean - wartość logiczna, to chyba logiczne
	 */
	public void setSnapToMesh(boolean snapToMesh) {
		this.snapToMesh = snapToMesh;
	}

	/**
	 * Metoda zwracająca liczbową wartość powiększenia.
	 * @return int - zoom
	 */
	public int getZoom() {
		return zoom;
	}

	/**
	 * Metoda zwracająca obiekt wymiarów obrazu. Związana z metodą setZoom().
	 * @return Dimension - obiekt wymiarów
	 */
	public Dimension getOriginSize() {
		return originSize;
	}

	/**
	 * Metoda ustawiająca wymiary obrazu. Związana z metodą setZoom().
	 * @param originSize Dimension - obiekt wymiarów
	 */
	public void setOriginSize(Dimension originSize) {
		this.originSize = originSize;
	}
	
	//***********************************************************************************
	//***********************************************************************************
	//***********************************************************************************
	//***********************************************************************************
	//***********************************************************************************

	/**
	 * Prywatna klasa wewnątrz GraphPanel, zajmująca się skrótami klawiatury,
	 * obsługiwanymi przez obiekt klasy GraphPanel.
	 * @author students
	 *
	 */
	private class KeyboardHandler implements KeyListener {
		@Override
		public void keyTyped(KeyEvent e) {
			System.out.println(e.getKeyChar());
		}

		@Override
		public void keyPressed(KeyEvent e) {
			System.out.println(e.getKeyChar());
		}

		@Override
		public void keyReleased(KeyEvent e) {
			System.out.println(e.getKeyChar());
		}
	}

	/**
	 * Prywatna klasa wewnątrz GraphPanel, zajmująca się realizacją funkcji programu
	 * wywoływanych za pomocą myszy. 
	 * @author students
	 * @author MR
	 */
	private class MouseHandler extends MouseAdapter {
		/**
		 * Metoda aktywowana w momencie puszczenia przycisku myszy.
		 */
		public void mouseReleased(MouseEvent e) {
			if(arcBreakPoint != null) {
				mousePt = e.getPoint();
				mousePt.setLocation(e.getPoint().getX() * 100 / zoom, e.getPoint().getY() * 100 / zoom);
				arcBreakPoint.setLocation(mousePt.x, mousePt.y);
				arcBreakPoint = null;
				setSelectingRect(null);
				e.getComponent().repaint();
			} else {
			
				setSelectingRect(null);
				e.getComponent().repaint();
			}
		}

		/**
		 * Metoda aktywowana przez podwójne kliknięcie przycisku myszy.
		 * @param e MouseEvent - obiekt przekazywany w efekcie podwójnego kliknięcia
		 */
		public void mouseClicked(MouseEvent e) {
			if (e.getClickCount() == 2) {
				if (e.getButton() == MouseEvent.BUTTON1 && e.isShiftDown() == false)
					getSelectionManager().doubleClickReactionHandler();
				if (e.getButton() == MouseEvent.BUTTON1 && e.isShiftDown() == true)
					getSelectionManager().decreaseTokensNumber();
			} else if (e.getClickCount() == 1) {
				if (e.getButton() == MouseEvent.BUTTON1 && e.isControlDown() == true) {
					getSelectionManager().doubleClickReactionHandler();
				}
			}
		}

		/**
		 * Przeciążona metoda mousePressed w klasie java.awt.event.MouseAdapter,
		 * zostaje wywołana za każdym razem, gdy którekolwiek z klawiszy myszy zostanie
		 * naciśnięty nad obszarem arkusza. W następstwie tego zdarzenia sprawdzane jest dla
		 * miejsca kliknięcia czy zostały spełnione warunki przecięcia z którymkolwiek z lokalizacji
		 * wierzchołków ElementLocation lub łuków znajdujących się na bieżącym arkuszu. W
		 * zależności od wyniku tego sprawdzenia oraz trybu rysowania i modyfikatorów kliknięcia
		 * (prawy i lewy przycisk myszy, klawisz Ctrl, Alt, Shift podejmowane są odpowiednie
		 * akcje, w dużej mierze przy wykorzystaniu obiektu SelectionManager.
		 * @param e MouseEvent - obiekt klasy przekazywany w efekcie kliknięcia myszą
		 */
		public void mousePressed(MouseEvent e) {
			//reset trybu przesuwania napisu:
			overlord.setNameLocationChangeMode(null, null, false);
			
			mousePt = e.getPoint();
			mousePt.setLocation(e.getPoint().getX() * 100 / zoom, e.getPoint().getY() * 100 / zoom);
			ElementLocation el = getSelectionManager().getPossiblySelectedElementLocation(mousePt);
			Arc a = getSelectionManager().getPossiblySelectedArc(mousePt);
			
			if(a != null && el == null && e.getButton() == MouseEvent.BUTTON1) {
				arcBreakPoint = a.checkBreakIntersection(mousePt);
				if(arcBreakPoint != null)
					return;
			}
			
			// nie kliknięto ani w Node ani w Arc
			if (el == null && a == null) {
				if (e.getButton() == MouseEvent.BUTTON3) { //menu kontekstowe
					if (getDrawMode() == DrawModes.POINTER)
						getSheetPopupMenu(PetriNetElementType.UNKNOWN).show(e);
					
					setDrawMode(DrawModes.POINTER);
					overlord.getToolBox().selectPointer(); //przywraca tryb wybierania z JTree po lewej
				}
				if (!e.isShiftDown() && !e.isControlDown()) {
					overlord.getWorkspace().globalDeselection();
					getSelectionManager().deselectAllElements();
					clearSelectionColors();
				}

				if(e.isAltDown()) //wycentruj ekran
					centerOnPoint(mousePt);
				
				switch (getDrawMode()) {
					case POINTER:
						getSelectionManager().selectSheet();
						setSelectingRect(new Rectangle(mousePt.x, mousePt.y, 0, 0));
						clearDrawnArc();
						break;
					case PLACE:
						overlord.getWorkspace().getProject().restoreMarkingZero();
						
						addNewPlace(mousePt);
						overlord.reset.reset2ndOrderData(true);
						overlord.markNetChange();
						break;
					case TRANSITION:
						overlord.getWorkspace().getProject().restoreMarkingZero();
						
						addNewTransition(mousePt);
						overlord.reset.reset2ndOrderData(true);
						overlord.markNetChange();
						break;
					case STOCHASTICTRANS:
						overlord.getWorkspace().getProject().restoreMarkingZero();
						
						addNewTransition(mousePt);
						overlord.reset.reset2ndOrderData(true);
						overlord.markNetChange();
						break;
					case FUNCTIONALTRANS:
						overlord.getWorkspace().getProject().restoreMarkingZero();
						
						addNewFunctionalTransition(mousePt);
						overlord.reset.reset2ndOrderData(true);
						overlord.markNetChange();
						break;
					case ARC:
						overlord.getWorkspace().getProject().restoreMarkingZero();
						
						clearDrawnArc();
						break;
					case TIMETRANSITION:
						overlord.getWorkspace().getProject().restoreMarkingZero();
						
						addNewTimeTransition(mousePt);
						overlord.reset.reset2ndOrderData(true);
						overlord.markNetChange();
						break;
					case CPLACE:
						overlord.getWorkspace().getProject().restoreMarkingZero();
						
						addNewCPlace(mousePt);
						overlord.reset.reset2ndOrderData(true);
						overlord.markNetChange();
						break;
					case SUBNET_P:
						addNewSubnetP(mousePt);
						break;
					case SUBNET_T:
						addNewSubnetT(mousePt);
						break;
					case SUBNET_PT:
						addNewSubnetPT(mousePt);
						break;
					default:
						break;
				}
			} else if (el != null) {
				// kliknięto w Node, możliwe ze też w łuk, ale nie zostanie on
				// zaznaczony, ponieważ to Node jest na wierzchu
				if (getDrawMode() == DrawModes.ARC 
						|| getDrawMode() == DrawModes.READARC 
						|| getDrawMode() == DrawModes.ARC_INHIBITOR 
						|| getDrawMode() == DrawModes.ARC_RESET 
						|| getDrawMode() == DrawModes.ARC_EQUAL) {
					handleArcsDrawing(el, getDrawMode());
					
				} else if (getDrawMode() == DrawModes.ERASER) { //kasowanie czegoś
					if(overlord.reset.isSimulatorActiveWarning(
							"Operation impossible when simulator is working.", "Warning") == true)
						return;
					
					Object[] options = {"Delete", "Cancel",};
					int n = JOptionPane.showOptionDialog(null, "Do you want to delete selected elements?",
							"Deletion warning?", JOptionPane.YES_NO_OPTION,
							JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
					if (n == 0) {
						overlord.getWorkspace().getProject().restoreMarkingZero();
						
						getSelectionManager().deleteElementLocation(el);
						overlord.reset.reset2ndOrderData(true);
						overlord.markNetChange();
					}
					
				} else {
					if (e.isShiftDown())
						getSelectionManager().selectElementLocation(el);
					else if (e.isControlDown())
						getSelectionManager().toggleElementLocationSelection(el);
					else if (!getSelectionManager().isElementLocationSelected(el)) {
						getSelectionManager().selectOneElementLocation(el);
						getSelectionManager().deselectAllArcs();
					}
					clearDrawnArc();
				}
				if (e.getButton() == MouseEvent.BUTTON3) { //menu kontekstowe węzła
					if (el.getParentNode().getType() == PetriNetElementType.PLACE) {
						getPlacePopupMenu(el, PetriNetElementType.PLACE).show(e);
					} else if (el.getParentNode().getType() == PetriNetElementType.TRANSITION) {
						getTransitionPopupMenu(el, PetriNetElementType.TRANSITION).show(e);
					} else if (el.getParentNode().getType() == PetriNetElementType.META) {
						getMetaNodePopupMenu(el, PetriNetElementType.META).show(e);
					}
				}
			} else if (a != null) { // kliknięto w łuk, więc zostanie on zaznaczony
				if (getDrawMode() == DrawModes.ERASER) {
					if(overlord.reset.isSimulatorActiveWarning(
							"Operation impossible when simulator is working.", "Warning") == true)
						return;
					
					Object[] options = {"Delete", "Cancel",};
					int n = JOptionPane.showOptionDialog(null,
							"Do you want to delete selected elements?",
							"Deletion warning?", JOptionPane.YES_NO_OPTION,
							JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
					if (n == 0) {
						overlord.getWorkspace().getProject().restoreMarkingZero();
						
						getSelectionManager().deleteArc(a);
						overlord.reset.reset2ndOrderData(true);
						overlord.markNetChange();
					}	
				} else {
					if (e.isShiftDown()) {
						a.setSelected(true);
					} else if (e.isControlDown()) {
						a.setSelected(!a.getSelected());
					} else if (!getSelectionManager().isArcSelected(a)) {
						getSelectionManager().deselectAllElementLocations();
						getSelectionManager().selectOneArc(a);
					}
				}
				clearDrawnArc();
				if (e.getButton() == MouseEvent.BUTTON3) { // menu konteksowe łuku
					getArcPopupMenu(a, PetriNetElementType.ARC, mousePt).show(e);
				}
			}
			e.getComponent().repaint();
		}

		/**
		 * Metoda odpowiedzialna za rysowanie łuków między wierzchołkami sieci.
		 * @param clickedLocation ElementLocation - gdzie kliknięto
		 * @param arcType DrawModes - tryb rysowania, tj. rodzaj rysowanego łuku
		 */
		//TODO
		private void handleArcsDrawing(ElementLocation clickedLocation, DrawModes arcType) {
			getSelectionManager().deselectAllElements();
			
			Node node = clickedLocation.getParentNode();
			if(node.isInvisible() == true) {
				JOptionPane.showMessageDialog(null, "Cannot draw arc to invisible node!", 
						"Problem", JOptionPane.WARNING_MESSAGE);
				return;
			}
			
			
			if(drawnArc == null && node instanceof MetaNode) {
				if(arcType == DrawModes.ARC) {
					drawnArc = new Arc(clickedLocation, TypeOfArc.NORMAL);
					return;
				} else {
					JOptionPane.showMessageDialog(null, "Only normal arc allowed from meta-node!", 
							"Problem", JOptionPane.WARNING_MESSAGE);
					return;
				}
			}
			
			if (drawnArc == null) {
				if(arcType == DrawModes.ARC)
					drawnArc = new Arc(clickedLocation, TypeOfArc.NORMAL);
				else if(arcType == DrawModes.READARC)
					drawnArc = new Arc(clickedLocation, TypeOfArc.READARC);
				else if(arcType == DrawModes.ARC_INHIBITOR)
					drawnArc = new Arc(clickedLocation, TypeOfArc.INHIBITOR);
				else if(arcType == DrawModes.ARC_RESET)
					drawnArc = new Arc(clickedLocation, TypeOfArc.RESET);
				else if(arcType == DrawModes.ARC_EQUAL)
					drawnArc = new Arc(clickedLocation, TypeOfArc.EQUAL);
			} else { 
				
				if(clickedLocation.getParentNode() instanceof MetaNode) { //kończymy w meta-node
					if(drawnArc.getStartLocation().getParentNode() instanceof MetaNode) {
						JOptionPane.showMessageDialog(null, "Direct connection between two meta-nodes not possible.", 
								"Problem", JOptionPane.WARNING_MESSAGE);
						clearDrawnArc();
						return;
					}
					if(drawnArc.getArcType() != TypeOfArc.NORMAL) {
						JOptionPane.showMessageDialog(null, "Only normal arc can be connected with meta-node.", 
								"Problem", JOptionPane.WARNING_MESSAGE);
						clearDrawnArc();
						return;
					}
					
					MetaNode n = (MetaNode) clickedLocation.getParentNode();
					
					if(drawnArc.getStartLocation().getParentNode() instanceof Place && n.getMetaType() == MetaType.SUBNETPLACE ) {
						JOptionPane.showMessageDialog(null, "Meta-node type P (transitions-interfaced) can get connection only from transitions!", 
								"Problem", JOptionPane.WARNING_MESSAGE);
						clearDrawnArc();
						return;
					}
					if(drawnArc.getStartLocation().getParentNode() instanceof Transition && n.getMetaType() == MetaType.SUBNETTRANS ) {
						JOptionPane.showMessageDialog(null, "Meta-node type T (places-interfaced) can get connection only from places!", 
								"Problem", JOptionPane.WARNING_MESSAGE);
						clearDrawnArc();
						return;
					}

					//dodaj połączenie z T lub P do Meta
					//MetaNode metanode = (MetaNode)clickedLocation.getParentNode();
					overlord.subnetsHQ.addArcToMetanode(drawnArc.getStartLocation(), clickedLocation, drawnArc);
					clearDrawnArc();
					return;
				}
				
				if(drawnArc.getStartLocation().getParentNode() instanceof MetaNode) { // połączenie z METANODE
					// skoro tu jesteśmy, to znaczy że kliknięto w miejsce lub tranzycję, 
					// ale nie meta-node bo poprzedni if by to wyłowił
					MetaNode n = (MetaNode) drawnArc.getStartLocation().getParentNode();
					
					if(clickedLocation.getParentNode() instanceof Place && n.getMetaType() == MetaType.SUBNETPLACE ) {
						JOptionPane.showMessageDialog(null, "Meta-node type P (transitions-interfaced) can get connection only from transitions!", 
								"Problem", JOptionPane.WARNING_MESSAGE);
						clearDrawnArc();
						return;
					}
					if(clickedLocation.getParentNode() instanceof Transition && n.getMetaType() == MetaType.SUBNETTRANS ) {
						JOptionPane.showMessageDialog(null, "Meta-node type T (places-interfaced) can get connection only from places!", 
								"Problem", JOptionPane.WARNING_MESSAGE);
						clearDrawnArc();
						return;
					}
					
					
					overlord.subnetsHQ.addArcFromMetanode(clickedLocation, drawnArc.getStartLocation(), drawnArc);
					clearDrawnArc();
					return;
				}
				
				//zwykły łuk poza meta-nodeami:
				if (drawnArc.checkIsCorect(clickedLocation)) {
					boolean proceed = true;
					
					if(isArcDuplicated(drawnArc.getStartLocation(), clickedLocation)) {
						JOptionPane.showMessageDialog(null,  "Arc going in this direction already exists.", 
								"Problem", JOptionPane.WARNING_MESSAGE);
						proceed = false;
					} else if(isReverseArcPresent(drawnArc.getStartLocation(), clickedLocation) == true) {
						if(arcType == DrawModes.ARC) {
							//JOptionPane.showMessageDialog(null, "Please use Read Arc drawing mode to draw a read-arc!", "Problem", JOptionPane.WARNING_MESSAGE);
							proceed = true;
						} else if(arcType == DrawModes.READARC) {
							JOptionPane.showMessageDialog(null, "Please remove arc between these two nodes in order to create a read-arc.", "Problem", 
									JOptionPane.WARNING_MESSAGE);
							proceed = false;
						} else {
							//JOptionPane.showMessageDialog(null, "Non-standard arc leading in reverse direction!", "Problem", 
							//	JOptionPane.WARNING_MESSAGE);
							//proceed = false;
						}
					}

					if(clickedLocation.getSheetID() > 0) {
						ArrayList<MetaNode> metas = overlord.getWorkspace().getProject().getMetaNodes();
						int first = SubnetsTools.isInterface(drawnArc.getStartLocation(), metas);
						int second = SubnetsTools.isInterface(clickedLocation, metas);
						
						if(first > 0 && second > 0) {
							JOptionPane.showMessageDialog(null, "Two interfaces cannot be linked directly within single subnet.", 
									"Don't cross the streams!",  JOptionPane.WARNING_MESSAGE);
								proceed = false;
						}
					}
					
					if(proceed) { //dokończ rysowanie łuku, dodaj do listy
						if ((arcType == DrawModes.ARC_INHIBITOR || arcType == DrawModes.ARC_RESET || arcType == DrawModes.ARC_EQUAL) 
								&& clickedLocation.getParentNode() instanceof Place) {
							JOptionPane.showMessageDialog(null,  "This type of arc can only go FROM place TO transition!", "Problem", 
									JOptionPane.WARNING_MESSAGE);
							clearDrawnArc();
						} else {
							overlord.getWorkspace().getProject().restoreMarkingZero();
							TypeOfArc thisArc = convertType(arcType);
							
							Arc arc = new Arc(IdGenerator.getNextId(), drawnArc.getStartLocation(), clickedLocation, thisArc);
							
							if(arcType == DrawModes.ARC) {
								if(arc.getArcType() != TypeOfArc.READARC) //ważne dla tworzenia read-arc poprzez nałożenie ręczne 2 łuków!
									arc.setArcType(TypeOfArc.NORMAL); 
								getArcs().add(arc);
							} else if(arcType == DrawModes.READARC) {
								//arc.setArcType(TypesOfArcs.INHIBITOR);
								getArcs().add(arc);
								Arc arc2 = new Arc(IdGenerator.getNextId(), clickedLocation, drawnArc.getStartLocation(), TypeOfArc.READARC);
								getArcs().add(arc2);
								arc.setArcType(TypeOfArc.READARC);
								arc2.setArcType(TypeOfArc.READARC);
							} else if(arcType == DrawModes.ARC_INHIBITOR) {
								arc.setArcType(TypeOfArc.INHIBITOR);
								getArcs().add(arc);
							} else if(arcType == DrawModes.ARC_RESET) {
								arc.setArcType(TypeOfArc.RESET);
								getArcs().add(arc);
							} else if(arcType == DrawModes.ARC_EQUAL) {
								arc.setArcType(TypeOfArc.EQUAL);
								getArcs().add(arc);
							}
							clearDrawnArc();

							int arcSheet = arc.getStartLocation().getSheetID();
							if(arcSheet > 0) {
								overlord.subnetsHQ.addMetaArc(arc);
							}
							
							overlord.reset.reset2ndOrderData(true);
							overlord.markNetChange();
						}
					}
					else {
						clearDrawnArc();
					}
				} //if (drawnArc.checkIsCorect(clickedLocation)) {
			}
		}
	} //end class MouseHandler

	/**
	 * Prywatna klasa wewnątrz GraphPanel, realizująca interakcje ze strony
	 * myszy związane z jej poruszaniem się po arkuszu.
	 * @author students
	 * @author MR
	 *
	 */
	private class MouseMotionHandler extends MouseMotionAdapter {
		Point delta = new Point();

		/**
		 * Przeciążona metoda mouseDragged w klasie java.awt.event.MouseMotionAdapter,
		 * zostaje wywołana za każdym razem, gdy którekolwiek z klawiszy myszy zostanie
		 * naciśnięty w połączeniu z przesuwaniem myszy nad obszarem arkusza. W następstwie
		 * tego zdarzenia sprawdzane jest dla miejsca kliknięcia zostały spełnione warunki
		 * przecięcia z którymkolwiek z lokalizacji wierzchołków ElementLocation lub łuków
		 * znajdujących się na bieżącym arkuszu. Jeśli warunki zostaną spełnione, wykonywane
		 * jest przesunięcie pozycji wszystkich zaznaczonych lokalizacji wierzchołków, a co
		 * za tym idzie łuków, o wektor przesunięcia myszy. W przypadku niespełnienia tych
		 * warunków oraz gdy aktualnym narzędziem rysowania jest wskaźnik, rysowany jest
		 * prostokąt zaznaczenia, połączony ze sprawdzaniem na bieżąco, które elementy
		 * zawierają się w obszarze zaznaczenia.
		 * @param e MouseEvent - obiekt klasy przekazywany w efekcie przeciągnięcia myszą
		 */
		public void mouseDragged(MouseEvent e) {
			Point dragPoint = e.getPoint();
			dragPoint.setLocation(e.getX() * 100 / zoom, e.getY() * 100 / zoom);
			if(arcBreakPoint != null) {
				int x = dragPoint.x < 10 ? 10 : dragPoint.x;
				int y = dragPoint.y < 10 ? 10 : dragPoint.y;
				arcBreakPoint.setLocation(x, y);
				e.getComponent().repaint();
				return;
			}
			
			if ((getDrawMode() == DrawModes.ARC || getDrawMode() == DrawModes.READARC || getDrawMode() == DrawModes.ARC_INHIBITOR 
					|| getDrawMode() == DrawModes.ARC_RESET || getDrawMode() == DrawModes.ARC_EQUAL)  
					&& drawnArc != null)
				return;
			if (getSelectingRect() != null) {
				getSelectingRect().setBounds(Math.min(mousePt.x, dragPoint.x),
						Math.min(mousePt.y, dragPoint.y),
						Math.abs(mousePt.x - dragPoint.x),
						Math.abs(mousePt.y - dragPoint.y));
				getSelectionManager().selectInRect(getSelectingRect());
			} else {
				delta.setLocation(dragPoint.getX() - mousePt.x, dragPoint.getY() - mousePt.y);
				for (ElementLocation el : getSelectionManager().getSelectedElementLocations()) {
					if (isSnapToMesh())
						el.updateLocationWithMeshSnap(delta, meshSize);
					else
						el.updateLocation(delta);
					getSelectionManager().dragSelected();
				}
				for (Arc a : getSelectionManager().getSelectedArcs()) {
					a.updateAllBreakPointsLocations(delta);
				}
				
				adjustScroll(dragPoint, mousePt);
				mousePt = dragPoint;
			}
			e.getComponent().repaint();
		}

		/**
		 * Metoda pochłaniająca zasoby jak czarna dziura. Wywoływana za każdym razem, gdy kursor
		 * znajdzie się nad panelem rysowania. Nie trzeba mówić, że przerysowywanie sieci w każdej
		 * takiej chwili nie jest najlepszym pomysłem. A to tu było zanim przyszedłem. Cud, że program
		 * się uruchamiał w ogóle...
		 * MR
		 */
		@Override
		public void mouseMoved(MouseEvent e) { 
			if (e.getButton() == MouseEvent.BUTTON3) {
				return;
			}
			
			if ((getDrawMode() == DrawModes.ARC || getDrawMode() == DrawModes.READARC ||getDrawMode() == DrawModes.ARC_INHIBITOR 
					|| getDrawMode() == DrawModes.ARC_RESET || getDrawMode() == DrawModes.ARC_EQUAL) && drawnArc != null) {
				Point movePoint = e.getPoint();
				movePoint.setLocation(e.getX() * 100 / zoom, e.getY() * 100 / zoom);
				drawnArc.setEndPoint(movePoint);
				drawnArc.checkIsCorect(getSelectionManager().getPossiblySelectedElementLocation(movePoint));
				e.getComponent().repaint();
			} else {
				//clearDrawnArc(); //WTH?!
				//e.getComponent().repaint(); //ŁO JEZU!!!!!!!!!!!!
			}
			//e.getComponent().repaint(); //WTF?! NA CZYM TO MA DZIAŁAĆ, NA KOMPACH NASA???
		}
	} //end class MouseMotionHandler

	/**
	 * Wewnątrzna klasa odpowiedzialna za obługę rolki myszy.
	 * @author students
	 * @author MR
	 *
	 */
	public class MouseWheelHandler implements MouseWheelListener {
		/**
		 * Metoda odpowiedzialna za działanie rozpoczęte przez przesuwanie rolki
		 * myszy nad arkusze. W zależności czy wciśniętych jest klawisz CTRL czy
		 * też SHIFT czy też żaden klawisz - działania są różne.
		 * @param e MouseWheelEvent - obiekt klasy przekazywany w efekcie użycia wałka myszy
		 */
		@SuppressWarnings("unused")
		public void mouseWheelMoved(MouseWheelEvent e) {
			if (e.isControlDown()) { //zoom
				double oldZoom = getZoom();
				setZoom(getZoom() - 10 * e.getWheelRotation(), getZoom());
				double newZoom = getZoom();
	
				//double deviation = newZoom - oldZoom;
				Point dragPoint = e.getPoint();
				Point newPoint = new Point();
				newPoint.setLocation(e.getX() * 100 / newZoom, e.getY() * 100 / newZoom);
				
				centerOnPoint(newPoint);
				
				try {
					//Robot robot = new Robot();
					//robot.mouseMove(dragPoint.x, dragPoint.y);
				} catch (Exception e1) {
					//e1.printStackTrace();
				}
			} else if (e.isShiftDown()) {  // przewijanie lewo/prawo
				if(overlord.getNameLocChangeMode() == true) {
					Point newP = nameLocationChangeHorizontal(e.getWheelRotation() * e.getScrollAmount());
					e.getComponent().repaint(); // bo samo się nie wywoła (P.S. NIE. Nie kombinuj. NIE!)
					
					if(overlord.getPropertiesBox().getCurrentDockWindow().nameLocationXSpinnerModel != null) {
						overlord.getPropertiesBox().getCurrentDockWindow().doNotUpdate = true;
						overlord.getPropertiesBox().getCurrentDockWindow().nameLocationXSpinnerModel.setValue(newP.x);
						overlord.getPropertiesBox().getCurrentDockWindow().nameLocationYSpinnerModel.setValue(newP.y);
						overlord.getPropertiesBox().getCurrentDockWindow().doNotUpdate = false;
					}
				} else {
					scrollSheetHorizontal(e.getWheelRotation() * e.getScrollAmount() * 30);
				}
			} else { // przewijanie góra/dół
				if(overlord.getNameLocChangeMode() == true) {
					Point newP =  nameLocationChangeVertical(e.getWheelRotation() * e.getScrollAmount());
					e.getComponent().repaint(); // bo samo się nie wywoła (P.S. NIE. Nie kombinuj. NIE!)
					
					if(overlord.getPropertiesBox().getCurrentDockWindow().nameLocationXSpinnerModel != null) {
						overlord.getPropertiesBox().getCurrentDockWindow().doNotUpdate = true;
						overlord.getPropertiesBox().getCurrentDockWindow().nameLocationXSpinnerModel.setValue(newP.x);
						overlord.getPropertiesBox().getCurrentDockWindow().nameLocationYSpinnerModel.setValue(newP.y);
						overlord.getPropertiesBox().getCurrentDockWindow().doNotUpdate = false;
					}
				} else {
					scrollSheetVertical(e.getWheelRotation() * e.getScrollAmount() * 30);
				}
			}
		}
	} // end class MouseWheelHandler

	/**
	 * Zadaniem tej metody jest wycentrowanie ekranu na klikniętych współrzędnych.
	 * @param mousePt Point - współrzędne centrowania
	 */
	public void centerOnPoint(Point mousePt) {
		//CompositeTabDock xxx = overlord.getWorkspace().getWorkspaceDock();
		WorkspaceSheet ws = overlord.getWorkspace().getSelectedSheet();
		if(ws == null) {
			overlord.log("Unable to obtaint WorkspaceSheet object. Net sheet panel probably externized outside "
					+ "program bounds.", "warning", true);
			return;
		}
		
		int visibleX = ws.getWidth(); 
		int visibleY = ws.getHeight(); //tyle pikseli dokładnie widać na ekranie
		//jeśli odejmiemy powyższe od getSize otrzymamy dane ile w pionie i w poziomie nie widać
		int clickedX = mousePt.x;
		int clickedY = mousePt.y;
		
		int centerX = visibleX / 2;
		int centerY = visibleY / 2; //współrzedne środka panelu
		
		int barHorX =  ws.getHorizontalScrollBar().getValue(); // aktualna wartość przesunięcia 
		int barVerY =  ws.getVerticalScrollBar().getValue();
		
		centerX += barHorX;
		centerY += barVerY;
		
		double zoom = getZoom();
		zoom = 100/zoom;
		centerX *= zoom;
		centerY *= zoom;
		
		if(clickedX <= centerX && clickedY <= centerY) { //I cwiartka, przesuwanie w lewo/góra
			scrollSheetHorizontal(-(centerX - clickedX)); // w lewo
			scrollSheetVertical(-(centerY - clickedY)); //w górę
		} else if(clickedX > centerX && clickedY <= centerY) { //II cwiartka, przesuwanie w prawo/góra
			scrollSheetHorizontal(clickedX - centerX); //w prawo
			scrollSheetVertical(-(centerY - clickedY)); //w górę
		} else if(clickedX > centerX && clickedY > centerY) { //III cwiartka, przesuwanie w prawo/dół
			scrollSheetHorizontal(clickedX - centerX); //w prawo
			scrollSheetVertical(clickedY - centerY); //w dół
		} else if(clickedX <= centerX && clickedY > centerY) { //IV cwiartka, przesuwanie w lewo/dół
			scrollSheetHorizontal(-(centerX - clickedX)); // w lewo
			scrollSheetVertical(clickedY - centerY); //w dół
		} 
	}

	public TypeOfArc convertType(DrawModes arcType) {
		if(arcType == DrawModes.ARC_INHIBITOR)
			return TypeOfArc.INHIBITOR;
		else if(arcType == DrawModes.ARC_EQUAL)
			return TypeOfArc.EQUAL;
		else if(arcType == DrawModes.ARC_RESET)
			return TypeOfArc.RESET;
		else if(arcType == DrawModes.READARC)
			return TypeOfArc.READARC;
		else
			return TypeOfArc.NORMAL;
	}

	/**
	 * Metoda usuwa status selected dla wszystkich portali.
	 */
	public void clearSelectionColors() {
		ArrayList<Node> nodes = overlord.getWorkspace().getProject().getNodes();
		for(Node n : nodes) {
			if(n.isPortal())
				for(ElementLocation el : n.getElementLocations()) {
					el.setSelected(false);
					el.setPortalSelected(false);
				}
		}
	}

	/**
	 * Metoda sprawdza, czy z wierzchołka DO którego prowadzimy łuk nie wychodzi już inny łuk skierowany tam,
	 * skąd właśnie wychodzi łuk aktualnie rysowany - wtedy zwraca true jako sygnał, że nie można dodawać.
	 * @param startLocation ElementLocation - lokalizacja wierzchołka startowego
	 * @param endLocation ElementLocation - lokalizacja wierzchołka docelowego
	 * @return boolean - true, jeśli nie można dodać z uwagi na obecność nieprawidłowego typu łuku skierowanego z 
	 * 		wierzchołka docelowego do wierzchołka startowego z którego prowadzony jest właśnie nowy łuk.
	 */
	public boolean isReverseArcPresent(ElementLocation startLocation, ElementLocation endLocation) {
		Node node = endLocation.getParentNode();
		for(ElementLocation el : node.getElementLocations()) {
			ArrayList<Arc> candidates = el.getOutArcs();
			for (Arc a : candidates) {
				if (locationFamily(a.getEndLocation(), startLocation) == true) {
					//if(a.getArcType() != TypesOfArcs.NORMAL) {
					return true;
					//}
				}
			}
		}
		return false;
	}
	
	/**
	 * Metoda sprawdza, czy testLoc należy do zbioru lokalizacji wierzchołka, do którego na pewno należy obiekt 'location'
	 * @param location ElementLocation - sprawdzamy cały zbiór do którego on należy
	 * @param testLoc ElementLocation - element testowany
	 * @return boolean - true, jeśli testLoc należy do zbioru lokalizacji wierzchołka który na pewno zawiera lokalizację 
	 * 		przesłaną jako 'location'
	 */
	private boolean locationFamily(ElementLocation location, ElementLocation testLoc ) {
		Node node = location.getParentNode();
		for(ElementLocation el : node.getElementLocations()) { //wszystkie, nie tylko samo location!
			if(el == testLoc)
				return true;
		}
		return false;
	}

	/**
	 * Metoda pomocnicza, sprawdza czy rysowany właśnie łuk już istnieje.
	 * @param startLocation ElementLocation - lokalizacja startowa nowego łuku
	 * @param endLocation ElementLocation - lokalizacja wierzchołka docelowego
	 * @return boolean - true, jeśli istnieje już łuk pomiędzy lokalizacjami
	 */
	public boolean isArcDuplicated(ElementLocation startLocation, ElementLocation endLocation) {
		boolean result = false;
		for(ElementLocation el : startLocation.getParentNode().getElementLocations()) {
			ArrayList<Arc> outArcs = el.getOutArcs(); //wszystkie wychodzące ze start EL
			for(Arc a : outArcs) { //dla każdego łuku:
				ElementLocation arcEndLocation = a.getEndLocation();
				if(arcEndLocation == null)
					continue;
				
				Node endNode = arcEndLocation.getParentNode();
				if(endLocation.getParentNode().equals(endNode)) {
					result = true;
					break;
				}
			}
		}
		return result;
	}
}
