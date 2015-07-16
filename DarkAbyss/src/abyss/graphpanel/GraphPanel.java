package abyss.graphpanel;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.swing.*;

import abyss.darkgui.GUIManager;
import abyss.graphpanel.popupmenu.ArcPopupMenu;
import abyss.graphpanel.popupmenu.PlacePopupMenu;
import abyss.graphpanel.popupmenu.SheetPopupMenu;
import abyss.graphpanel.popupmenu.TransitionPopupMenu;
import abyss.math.pnElements.Arc;
import abyss.math.pnElements.ElementLocation;
import abyss.math.pnElements.Node;
import abyss.math.pnElements.PetriNet;
import abyss.math.pnElements.Place;
import abyss.math.pnElements.Transition;
import abyss.math.pnElements.Arc.TypesOfArcs;
import abyss.math.pnElements.PetriNetElement.PetriNetElementType;
import abyss.math.pnElements.Transition.TransitionType;
import abyss.utilities.Tools;
import abyss.workspace.WorkspaceSheet;

/**
 * Klasa, której zadaniem jest reprezentacja graficzna używanej w programie
 * sieci Petriego oraz oferowanie interfejsu umożliwiającego interakcję ze
 * strony użytkownika.
 * @author students
 *
 */
public class GraphPanel extends JComponent {
	//BACKUP: -5746225670483573975L; nie ruszać poniższej zmiennej
	private static final long serialVersionUID = -5746225670483573975L;
	private static final int meshSize = 20;
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
	private SheetPopupMenu sheetPopupMenu;
	private PlacePopupMenu placePopupMenu;
	private TransitionPopupMenu transitionPopupMenu;
	private ArcPopupMenu arcPopupMenu;
	private int zoom = 100;
	private Dimension originSize;
	private boolean drawMesh = false;
	private boolean snapToMesh = false;
	//public enum DrawModes { POINTER, PLACE, TRANSITION, ARC, ERASER, TIMETRANSITION; }
	/** POINTER, ERASER, PLACE, TRANSITION, TIMETRANSITION, ARC, ARC_INHIBITOR, ARC_RESET, ARC_EQUAL, READARC */
	public enum DrawModes { POINTER, ERASER, PLACE, TRANSITION, TIMETRANSITION, ARC, ARC_INHIBITOR, ARC_RESET, ARC_EQUAL, READARC }
	
	//private Graphics2D oldState = null;

	/**
	 * Konstruktor obiektu klasy GraphPanel
	 * @param sheetId int - nr arkusza
	 * @param petriNet PetriNet - sieć Petriego
	 * @param nodesList ArrayList[Node]- lista wierzchołków
	 * @param arcsList ArrayList[Arc]- lista łuków
	 */
	public GraphPanel(int sheetId, PetriNet petriNet, ArrayList<Node> nodesList, ArrayList<Arc> arcsList) {
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
		this.setSheetPopupMenu(new SheetPopupMenu(this));
		this.setPlacePopupMenu(new PlacePopupMenu(this));
		this.setTransitionPopupMenu(new TransitionPopupMenu(this));
		this.setArcPopupMenu(new ArcPopupMenu(this));
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
				GUIManager.getDefaultGUIManager().log("Critical error, no "+modeName+".gif in jar file. Thank java un-catchable exceptions...", "error", true);
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
		drawPetriNet(g2d);
		
		//oldState = g2d;
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
		
		if(GUIManager.getDefaultGUIManager().getSettingsManager().getValue("gridLines").equals("1")) {
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
		
		
		for (Arc a : getArcs()) {
			int sizeS = Integer.parseInt(GUIManager.getDefaultGUIManager().getSettingsManager().getValue("graphArcLineSize"));
			g2d.setStroke(new BasicStroke(sizeS));
			a.draw(g2d, this.sheetId, getZoom());
		}
		if (this.isSimulationActive()) {
			for (Arc a : getArcs()) {
				a.drawSimulationToken(g2d, this.sheetId);
			}
		}
		
		for (Node n : getNodes()) {
			n.draw(g2d, this.sheetId);	
		}
		
		ArrayList<Place> places_tmp = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getPlaces();
		ArrayList<Transition> transitions_tmp = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions();
		for (Node n : getNodes()) {
			n.drawName(g2d, this.sheetId, places_tmp, transitions_tmp);
		}
		
		if (getSelectingRect() != null) {
			g2d.setColor(EditorResources.selectionRectColor);
			g2d.setStroke(EditorResources.selectionRectStroke);
			g2d.drawRoundRect(getSelectingRect().x, getSelectingRect().y, getSelectingRect().width, getSelectingRect().height, 3, 3);
			g2d.setColor(EditorResources.selectionRectFill);
			g2d.fillRoundRect(getSelectingRect().x, getSelectingRect().y, getSelectingRect().width, getSelectingRect().height, 3, 3);
		}
		if (drawnArc != null)
			drawnArc.draw(g2d, this.sheetId, getZoom());
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
		GUIManager gui = GUIManager.getDefaultGUIManager();
		WorkspaceSheet sheet = gui.getWorkspace().getSheets().get(gui.IDtoIndex(sheetId));
		sheet.revalidate();
		this.invalidate();
		this.repaint();
	}

	/**
	 * Metoda przewijania arkusza w poziomie za pomocą wałka myszy.
	 * @param delta int - wielkość przewinięcia
	 */
	public void scrollSheetHorizontal(int delta) {
		GUIManager gui = GUIManager.getDefaultGUIManager();
		WorkspaceSheet sheet = gui.getWorkspace().getSheets().get(gui.IDtoIndex(sheetId));
		sheet.scrollHorizontal(delta);
	}
	
	/**
	 * Metoda zmiany lokalizacji nazwy wskazanego wierzchołka w poziomie.
	 * @param delta int - wielkość przewinięcia
	 * @return Point - współrzędne po zmianie
	 */
	public Point nameLocationChangeHorizontal(int delta) {
		Node n = GUIManager.getDefaultGUIManager().getNameLocChangeNode();
		ElementLocation el = GUIManager.getDefaultGUIManager().getNameLocChangeEL();
		
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
		GUIManager gui = GUIManager.getDefaultGUIManager();
		WorkspaceSheet sheet = gui.getWorkspace().getSheets().get(gui.IDtoIndex(sheetId));
		sheet.scrollVertical(delta);
	}
	
	/**
	 * Metoda zmiany lokalizacji nazwy wskazanego wierzchołka w pionie.
	 * @param delta int - wielkość przewinięcia
	 * @return Point - współrzędne po zmianie
	 */
	public Point nameLocationChangeVertical(int delta) {
		Node n = GUIManager.getDefaultGUIManager().getNameLocChangeNode();
		ElementLocation el = GUIManager.getDefaultGUIManager().getNameLocChangeEL();
		
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
		GUIManager gui = GUIManager.getDefaultGUIManager();
		WorkspaceSheet sheet = gui.getWorkspace().getSheets().get(gui.IDtoIndex(sheetId));
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
	 * @return DrawModes - aktualny tryb rysowania, z pola this.drawMode
	 */
	public DrawModes getDrawMode() {
		return this.drawMode;
	}

	/**
	 * Metoda pozwala na ustawienie aktualnego trybu rysowania na bieżącym arkuszu. Tryby
	 * definiowane są przez typ DrawModes. Ustawienie trybu rysowania powoduje zmianę
	 * kursora na arkuszu.
	 * @param newMode DrawModes - nowy tryb rysowania
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
	 * Metoda pozwala ustawić obiekt klasy SelectionManager zarzśdzajścy zaznaczeniem
	 * na danym arkuszu.
	 * @param selectionManager SelectionManager - nowy manager zaznaczenia
	 */
	public void setSelectionManager(SelectionManager selectionManager) {
		this.selectionManager = selectionManager;
	}

	/**
	 * Metoda pozwala pobrać obiekt klasy SelectionManager zarzśdzajścy zaznaczeniem na
	 * danym arkuszu.
	 * @return SelectionManager - manager selekcji
	 */
	public SelectionManager getSelectionManager() {
		return selectionManager;
	}

	/**
	 * Metoda pozwala pobrać obiekt będący menu kontekstowym dla danego arkusza.
	 * @return SheetPopupMenu - obiekt menu kontekstowego
	 */
	public SheetPopupMenu getSheetPopupMenu() {
		return sheetPopupMenu;
	}

	/**
	 * Pozwala ustawić obiekt będący menu kontekstowym danego arkusza.
	 * @param sheetPopupMenu SheetPopupMenu - obiekt menu kontekstowego
	 */
	public void setSheetPopupMenu(SheetPopupMenu sheetPopupMenu) {
		this.sheetPopupMenu = sheetPopupMenu;
	}

	/**
	 * Metoda pozwala pobrać obiekt będący menu kontekstowym dla każdego miejsca.
	 * @return PlacePopupMenu - obiekt menu kontekstowego
	 */
	public PlacePopupMenu getPlacePopupMenu() {
		return placePopupMenu;
	}

	/**
	 * Metoda pozwala ustawić obiekt będący menu kontekstowym dla każdego miejsca.
	 * @param placePopupMenu PlacePopupMenu - nowe menu kontekstowe
	 */
	public void setPlacePopupMenu(PlacePopupMenu placePopupMenu) {
		this.placePopupMenu = placePopupMenu;
	}

	/**
	 * Metoda pozwala pobrać obiekt będący menu kontekstowym dla każdej tranzycji.
	 * @return TransitionPopupMenu - obiekt menu kontekstowego tranzycji
	 */
	public TransitionPopupMenu getTransitionPopupMenu() {
		return transitionPopupMenu;
	}

	/**
	 * Metoda pozwala ustawić obiekt będący menu kontekstowym dla każdej tranzycji.
	 * @param transitionPopupMenu TransitionPopupMenu - nowe menu kontekstowe
	 */
	public void setTransitionPopupMenu(TransitionPopupMenu transitionPopupMenu) {
		this.transitionPopupMenu = transitionPopupMenu;
	}

	/**
	 * Metoda pozwala pobrać obiekt będący menu kontekstowym dla każdego łuku.
	 * @return ArcPopupMenu - obiekt menu kontekstowego
	 */
	public ArcPopupMenu getArcPopupMenu() {
		return arcPopupMenu;
	}

	/**
	 * Metoda pozwala ustawić obiekt będący menu kontekstowym dla każdego łuku.
	 * @param arcPopupMenu ArcPopupMenu - nowe menu kontekstowe
	 */
	public void setArcPopupMenu(ArcPopupMenu arcPopupMenu) {
		this.arcPopupMenu = arcPopupMenu;
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
	 * 
	 * @return boolean
	 */
	public boolean isSnapToMesh() {
		return snapToMesh;
	}

	/**
	 * 
	 * @param snapToMesh boolean
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
			setSelectingRect(null);
			e.getComponent().repaint();
		}

		/**
		 * Metoda aktywowana przez podwójne kliknięcie przycisku myszy.
		 * @param e MouseEvent - obiekt przekazywany w efekcie podwójnego kliknięcia
		 */
		public void mouseClicked(MouseEvent e) {
			if (e.getClickCount() == 2) {
				if (e.getButton() == MouseEvent.BUTTON1 && e.isShiftDown() == false)
					getSelectionManager().increaseTokensNumber();
				if (e.getButton() == MouseEvent.BUTTON1 && e.isShiftDown() == true)
					getSelectionManager().decreaseTokensNumber();
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
			GUIManager.getDefaultGUIManager().setNameLocationChangeMode(null, null, false);
			
			mousePt = e.getPoint();
			mousePt.setLocation(e.getPoint().getX() * 100 / zoom, e.getPoint().getY() * 100 / zoom);
			ElementLocation el = getSelectionManager().getPossiblySelectedElementLocation(mousePt);
			Arc a = getSelectionManager().getPossiblySelectedArc(mousePt);
			// nie kliknięto ani w Node ani w Arc
			if (el == null && a == null) {
				if (e.getButton() == MouseEvent.BUTTON3) { //menu kontekstowe
					if (getDrawMode() == DrawModes.POINTER)
						getSheetPopupMenu().show(e);
					
					setDrawMode(DrawModes.POINTER);
					GUIManager.getDefaultGUIManager().getToolBox().selectPointer(); //przywraca tryb wybierania z JTree po lewej
				}
				if (!e.isShiftDown() && !e.isControlDown()) {
					getSelectionManager().deselectAllElements();
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
						if(GUIManager.getDefaultGUIManager().getWorkspace().getProject().isBackup == true) {
							GUIManager.getDefaultGUIManager().getWorkspace().getProject().restoreMarkingZero();
						}
						
						addNewPlace(mousePt);
						GUIManager.getDefaultGUIManager().reset.reset2ndOrderData();
						GUIManager.getDefaultGUIManager().markNetChange();
						break;
					case TRANSITION:
						if(GUIManager.getDefaultGUIManager().getWorkspace().getProject().isBackup == true) {
							GUIManager.getDefaultGUIManager().getWorkspace().getProject().restoreMarkingZero();
						}
						
						addNewTransition(mousePt);
						GUIManager.getDefaultGUIManager().reset.reset2ndOrderData();
						GUIManager.getDefaultGUIManager().markNetChange();
						break;
					case ARC:
						if(GUIManager.getDefaultGUIManager().getWorkspace().getProject().isBackup == true) {
							GUIManager.getDefaultGUIManager().getWorkspace().getProject().restoreMarkingZero();
						}
						
						clearDrawnArc();
						break;
					case TIMETRANSITION:
						if(GUIManager.getDefaultGUIManager().getWorkspace().getProject().isBackup == true) {
							GUIManager.getDefaultGUIManager().getWorkspace().getProject().restoreMarkingZero();
						}
						
						addNewTimeTransition(mousePt);
						GUIManager.getDefaultGUIManager().reset.reset2ndOrderData();
						GUIManager.getDefaultGUIManager().markNetChange();
						break;
					default:
						break;
				}
			} else if (el != null) {
				// kliknięto w Node, możliwe ze też w łuk, ale nie zostanie on
				// zaznaczony, ponieważ to Node jest na wierzchu
				if (getDrawMode() == DrawModes.ARC || getDrawMode() == DrawModes.READARC ||getDrawMode() == DrawModes.ARC_INHIBITOR 
						|| getDrawMode() == DrawModes.ARC_RESET || getDrawMode() == DrawModes.ARC_EQUAL) {
					handleArcsDrawing(el, getDrawMode());
					
				} else if (getDrawMode() == DrawModes.ERASER) { //kasowanie czegoś
					if(GUIManager.getDefaultGUIManager().reset.isSimulatorActiveWarning(
							"Operation impossible when simulator is working."
							, "Warning") == true)
						return;
					
					Object[] options = {"Delete", "Cancel",};
					int n = JOptionPane.showOptionDialog(null,
							"Do you want to delete selected elements?",
							"Deletion warning?", JOptionPane.YES_NO_OPTION,
							JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
					if (n == 0) {
						if(GUIManager.getDefaultGUIManager().getWorkspace().getProject().isBackup == true) {
							GUIManager.getDefaultGUIManager().getWorkspace().getProject().restoreMarkingZero();
						}
						
						getSelectionManager().deleteElementLocation(el);
						GUIManager.getDefaultGUIManager().reset.reset2ndOrderData();
						GUIManager.getDefaultGUIManager().markNetChange();
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
					if (el.getParentNode().getType() == PetriNetElementType.PLACE)
						getPlacePopupMenu().show(e);
					else
						getTransitionPopupMenu().show(e);
				}
			}
			
			else if (a != null) { // kliknięto w łuk, więc zostanie on zaznaczony
				if (getDrawMode() == DrawModes.ERASER) {
					if(GUIManager.getDefaultGUIManager().reset.isSimulatorActiveWarning(
							"Operation impossible when simulator is working."
							, "Warning") == true)
						return;
					
					Object[] options = {"Delete", "Cancel",};
					int n = JOptionPane.showOptionDialog(null,
							"Do you want to delete selected elements?",
							"Deletion warning?", JOptionPane.YES_NO_OPTION,
							JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
					if (n == 0) {
						if(GUIManager.getDefaultGUIManager().getWorkspace().getProject().isBackup == true) {
							GUIManager.getDefaultGUIManager().getWorkspace().getProject().restoreMarkingZero();
						}
						
						getSelectionManager().deleteArc(a);
						GUIManager.getDefaultGUIManager().reset.reset2ndOrderData();
						GUIManager.getDefaultGUIManager().markNetChange();
					}	
				} else {
					if (e.isShiftDown())
						a.setSelected(true);
					else if (e.isControlDown())
						a.setSelected(!a.getSelected());
					else if (!getSelectionManager().isArcSelected(a)) {
						getSelectionManager().deselectAllElementLocations();
						getSelectionManager().selectOneArc(a);
					}
				}
				clearDrawnArc();
				if (e.getButton() == MouseEvent.BUTTON3) { // menu konteksowe łuku
					getArcPopupMenu().show(e);
				}
			}
			e.getComponent().repaint();
		}

		/**
		 * Metoda odpowiedzialna za rysowanie łuków między wierzchołkami sieci.
		 * @param clickedLocation ElementLocation - gdzie kliknięto
		 * @param arcType DrawModes - tryb rysowania, tj. rodzaj rysowanego łuku
		 */
		private void handleArcsDrawing(ElementLocation clickedLocation, DrawModes arcType) {
			getSelectionManager().deselectAllElements();
			if (drawnArc == null) {
				if(arcType == DrawModes.ARC)
					drawnArc = new Arc(clickedLocation, TypesOfArcs.NORMAL);
				else if(arcType == DrawModes.READARC)
					drawnArc = new Arc(clickedLocation, TypesOfArcs.NORMAL);
				else if(arcType == DrawModes.ARC_INHIBITOR)
					drawnArc = new Arc(clickedLocation, TypesOfArcs.INHIBITOR);
				else if(arcType == DrawModes.ARC_RESET)
					drawnArc = new Arc(clickedLocation, TypesOfArcs.RESET);
				else if(arcType == DrawModes.ARC_EQUAL)
					drawnArc = new Arc(clickedLocation, TypesOfArcs.EQUAL);
			} else {
				if (drawnArc.checkIsCorect(clickedLocation)) {
					//TODO: ??
					if(isArcDuplicated(drawnArc.getStartLocation(), clickedLocation)) {
						JOptionPane.showMessageDialog(null,  "Arc going in this direction already exists.", 
								"Problem", JOptionPane.WARNING_MESSAGE);
					} else if(isReverseArcPresent(drawnArc.getStartLocation(), clickedLocation) == true) {
						if(arcType == DrawModes.ARC) {
							JOptionPane.showMessageDialog(null, "Please use Read Arc drawing mode to draw a read-arc!", "Problem", 
									JOptionPane.WARNING_MESSAGE);
						} else if(arcType == DrawModes.READARC) {
							JOptionPane.showMessageDialog(null, "Please remove arc between these two nodes in order to create a read-arc.", "Problem", 
									JOptionPane.WARNING_MESSAGE);
						} else {
							JOptionPane.showMessageDialog(null, "Non-standard arc leading in reverse direction!", "Problem", 
								JOptionPane.WARNING_MESSAGE);
						}
					} else { //dokończ rysowanie łuku, dodaj do listy
						
						
						if ((arcType == DrawModes.ARC_INHIBITOR || arcType == DrawModes.ARC_RESET || arcType == DrawModes.ARC_EQUAL) 
								&& clickedLocation.getParentNode() instanceof Place) {
							JOptionPane.showMessageDialog(null,  "This type of arc can only go FROM place TO transition!", "Problem", 
									JOptionPane.WARNING_MESSAGE);
							
						} else {
							if(GUIManager.getDefaultGUIManager().getWorkspace().getProject().isBackup == true) {
								GUIManager.getDefaultGUIManager().getWorkspace().getProject().restoreMarkingZero();
							}
							
							Arc arc = new Arc(IdGenerator.getNextId(), drawnArc.getStartLocation(), clickedLocation, TypesOfArcs.NORMAL);
							
							if(arcType == DrawModes.ARC) {
								arc.setArcType(TypesOfArcs.NORMAL);
								getArcs().add(arc);
							} else if(arcType == DrawModes.READARC) {
								//arc.setArcType(TypesOfArcs.INHIBITOR);
								getArcs().add(arc);
								Arc arc2 = new Arc(IdGenerator.getNextId(), clickedLocation, drawnArc.getStartLocation(), TypesOfArcs.NORMAL);
								getArcs().add(arc2);
								
								arc.setArcType(TypesOfArcs.READARC);
								arc2.setArcType(TypesOfArcs.READARC);
							} else if(arcType == DrawModes.ARC_INHIBITOR) {
								arc.setArcType(TypesOfArcs.INHIBITOR);
								getArcs().add(arc);
							} else if(arcType == DrawModes.ARC_RESET) {
								arc.setArcType(TypesOfArcs.RESET);
								getArcs().add(arc);
							} else if(arcType == DrawModes.ARC_EQUAL) {
								arc.setArcType(TypesOfArcs.EQUAL);
								getArcs().add(arc);
							}

							
							GUIManager.getDefaultGUIManager().reset.reset2ndOrderData();
							GUIManager.getDefaultGUIManager().markNetChange();
						}
					}
				}
				clearDrawnArc();
			}
		}
		
		
	} //end class MouseHandler

	/**
	 * Prywatna klasa wewnątrz GraphPanel, realizująca interakcje ze strony
	 * myszy związane z jej poruszaniem się po arkuszu.
	 * @author students
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
				//e.getComponent().repaint(); //DZYZYS!!!!!!!!!!!!
			}
			//e.getComponent().repaint(); //WTF?!
		}
	} //end class MouseMotionHandler

	/**
	 * Wewnątrzna klasa odpowiedzialna za obługę rolki myszy.
	 * @author students
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
				if(GUIManager.getDefaultGUIManager().getNameLocChangeMode() == true) {
					GUIManager gui = GUIManager.getDefaultGUIManager();
					Point newP = nameLocationChangeHorizontal(e.getWheelRotation() * e.getScrollAmount());
					e.getComponent().repaint(); // bo samo się nie wywoła (P.S. NIE. Nie kombinuj. NIE!)
					
					if(gui.getPropertiesBox().getCurrentDockWindow().nameLocationXSpinnerModel != null) {
						gui.getPropertiesBox().getCurrentDockWindow().doNotUpdate = true;
						gui.getPropertiesBox().getCurrentDockWindow().nameLocationXSpinnerModel.setValue(newP.x);
						gui.getPropertiesBox().getCurrentDockWindow().nameLocationYSpinnerModel.setValue(newP.y);
						gui.getPropertiesBox().getCurrentDockWindow().doNotUpdate = false;
					}
				} else {
					scrollSheetHorizontal(e.getWheelRotation() * e.getScrollAmount() * 30);
				}
			} else { // przewijanie góra/dół
				if(GUIManager.getDefaultGUIManager().getNameLocChangeMode() == true) {
					GUIManager gui = GUIManager.getDefaultGUIManager();
					Point newP =  nameLocationChangeVertical(e.getWheelRotation() * e.getScrollAmount());
					e.getComponent().repaint(); // bo samo się nie wywoła (P.S. NIE. Nie kombinuj. NIE!)
					
					if(gui.getPropertiesBox().getCurrentDockWindow().nameLocationXSpinnerModel != null) {
						gui.getPropertiesBox().getCurrentDockWindow().doNotUpdate = true;
						gui.getPropertiesBox().getCurrentDockWindow().nameLocationXSpinnerModel.setValue(newP.x);
						gui.getPropertiesBox().getCurrentDockWindow().nameLocationYSpinnerModel.setValue(newP.y);
						gui.getPropertiesBox().getCurrentDockWindow().doNotUpdate = false;
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
		//CompositeTabDock xxx = GUIManager.getDefaultGUIManager().getWorkspace().getWorkspaceDock();
		WorkspaceSheet ws = GUIManager.getDefaultGUIManager().getWorkspace().getSelectedSheet();
		if(ws == null) {
			GUIManager.getDefaultGUIManager().log("Unable to obtaint WorkspaceSheet object. Net sheet panel probably externized outside "
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
