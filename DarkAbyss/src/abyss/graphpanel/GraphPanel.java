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
import abyss.math.Arc;
import abyss.math.ElementLocation;
import abyss.math.Node;
import abyss.math.PetriNet;
import abyss.math.PetriNetElement.PetriNetElementType;
import abyss.math.Place;
import abyss.math.TimeTransition;
import abyss.math.Transition;
import abyss.workspace.WorkspaceSheet;

/**
 * Klasa, kt�rej zadaniem jest reprezentacja graficzna u�ywanej w programie
 * sieci Petriego oraz oferowanie interfejsu umo�liwiaj�cego interakcj� ze
 * strony u�ytkownika.
 * @author students
 *
 */
public class GraphPanel extends JComponent {
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
	public enum DrawModes { POINTER, PLACE, TRANSITION, ARC, ERASER, TIMETRANSITION; }

	/**
	 * Konstruktor obiektu klasy GraphPanel
	 * @param sheetId int - nr arkusza
	 * @param petriNet PetriNet - sie� Petriego
	 * @param nodesList ArrayList[Node]- lista wierzcho�k�w
	 * @param arcsList ArrayList[Arc]- lista �uk�w
	 */
	public GraphPanel(int sheetId, PetriNet petriNet, ArrayList<Node> nodesList, ArrayList<Arc> arcsList) {
		this.petriNet = petriNet;
		this.sheetId = sheetId;
		this.setNodesAndArcs(nodesList, arcsList);
		this.Initialize();
	}

	/**
	 * Inicjalizacja obiekt�w dla panelu
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
	 * Metoda pozwala na ustawienie zbioru wierzcho�k�w oraz �uk�w dla danego arkusza.
	 * Zbi�r ten jest wsp�lny dla wszystkich arkuszy i jest przechowywana w obiekcie
	 * PetriNet, o tym gdzie dany element zostanie narysowany decyduje jego lokalizacja
	 * ElementLocation.SheetId.
	 * @param nodes ArrayList[Nodes] - lista w�z��w przekazywana do danego arkusza
	 * @param arcs ArrayList[Arc] - lista �uk�w przekazywana do danego arkusza
	 */
	public void setNodesAndArcs(ArrayList<Node> nodes, ArrayList<Arc> arcs) {
		this.nodes = nodes;
		this.arcs = arcs;
		this.revalidate();
		this.repaint();
	}

	/**
	 * Metoda pozwala na ustawienie zbioru wierzcho�k�w dla danego arkusza.
	 * Zbi�r ten jest wsp�lny dla wszystkich arkuszy i jest przechowywana w
	 * obiekcie PetriNet, o tym gdzie dany element zostanie narysowany decyduje
	 * jego lokalizacja
	 * @param nodes ArrayList[Node] - lista w�z��w przekazywana do danego arkusza
	 */
	public void setNodes(ArrayList<Node> nodes) {
		this.nodes = nodes;
		this.revalidate();
		this.repaint();
	}

	/**
	 * Metoda ustawia odpowiedni kursor w zale�no�ci od wybranego elementu sieci.
	 */
	public void setCursorForMode() {
		if (this.getDrawMode() == DrawModes.POINTER) {
			setCursor(Cursor.getDefaultCursor());
		} else {
			Toolkit toolkit = Toolkit.getDefaultToolkit();
			Image image = toolkit.getImage("resources/cursors/"
					+ this.getDrawMode().toString() + ".gif");
			Point hotSpot = new Point(0, 0);
			Cursor cursor = toolkit.createCustomCursor(image, hotSpot, this
					.getDrawMode().toString());
			setCursor(cursor);
		}
	}

	/**
	 * Metoda odpowiedzialna za konwersj� cz�ci sieci wy�wietlanej w komponencie
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
	 * Metoda pozwala na pobrania listy wierzcho�k�w przypisanych do danego arkusza.
	 * Lista ta jest wsp�lna dla wszystkich arkuszy i jest przechowywana w obiekcie PetriNet.
	 * @return ArrayList[Node] - lista w�z��w
	 */
	public ArrayList<Node> getNodes() {
		return this.nodes;
	}

	/**
	 * Metoda pozwala na ustawienie zbioru �uk�w dla danego arkusza. Zbi�r ten jest wsp�lny
	 * dla wszystkich arkuszy, o tym gdzie dany element zostanie narysowany, decyduje jego
	 * lokalizacja startowa i ko�cowa (ElementLocation.SheetId).
	 * @param arcs ArrayList[Arc] - lista �uk�w przekazywana do danego arkusza
	 */
	public void setArcs(ArrayList<Arc> arcs) {
		this.arcs = arcs;
		this.revalidate();
		this.repaint();
	}

	/**
	 * Metoda pozwala na pobrania listy �uk�w przypisanych do danego arkusza. Lista ta jest
	 * wsp�lna dla wszystkich arkuszy i jest przechowywana w obiekcie PetriNet.
	 * @return ArrayList[Arc] - zwraca list� �uk�w
	 */
	public ArrayList<Arc> getArcs() {
		return arcs;
	}

	/**
	 * Metoda pozwala na pobrania aktualnego prostok�ta zaznaczenia, na podstawie kt�rego
	 * rysowany jest obszar zaznaczenia oraz wybierane s� obiekty, kt�re kwalifikuj� si� aby
	 * zosta� zaznaczone. W sytuacji gdy zaznaczenie nie jest rysowane, przyjmuje warto�� null.
	 * @return Rectangle - prostok�d aktualnego zaznaczenia, z pola this.selectingRect
	 */
	public Rectangle getSelectingRect() {
		return selectingRect;
	}

	/**
	 * Przeci��ona metoda paintComponent w klasie javax.swing.JComponent. Jej ka�dorazowe
	 * wywo�anie powoduje wyczyszczenie aktualnego widoku i narysowanie go od nowa. W tym te�
	 * momencie wybierane s� odpowiednie elementy, kt�re maj� zasta� narysowane na danym arkuszu, 
	 * na podstawie ich lokalizacji ElementLocation.sheetId, nast�pnie ka�demu z obiekt�w
	 * zakwalifikowanych, zlecane jest narysowanie "siebie" na dostarczonym w parametrze metody
	 * obiekcie Graphics2D. W rysowaniu wykorzystany zosta� podw�jny bufor oraz filtr antyaliasingowy.
	 * @param g Graphics - obiekt zawieraj�cy prezentowan� grafik�
	 */
	public void paintComponent(Graphics g) {
		g.setColor(new Color(0x00f0f0f0));
		g.fillRect(0, 0, getWidth(), getHeight());
		Graphics2D g2d = (Graphics2D) g.create();
		if (isDrawMesh())
			drawMesh(g2d);
		drawPetriNet(g2d);
	}

	/**
	 * 
	 * @param g2d Graphics2D
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
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, // Anti-alias!
				RenderingHints.VALUE_ANTIALIAS_ON);
		for (Arc a : getArcs()) {
			a.draw(g2d, this.sheetId, getZoom());
		}
		if (this.isSimulationActive())
			for (Arc a : getArcs()) {
				a.drawSimulationToken(g2d, this.sheetId);
			}
		for (Node n : getNodes()) {
			n.draw(g2d, this.sheetId);
		}
		if (getSelectingRect() != null) {
			g2d.setColor(EditorResources.selectionRectColor);
			g2d.setStroke(EditorResources.selectionRectStroke);
			g2d.drawRoundRect(getSelectingRect().x, getSelectingRect().y,
				getSelectingRect().width, getSelectingRect().height, 3, 3);
			g2d.setColor(EditorResources.selectionRectFill);
			g2d.fillRoundRect(getSelectingRect().x, getSelectingRect().y,
				getSelectingRect().width, getSelectingRect().height, 3, 3);
		}
		if (drawnArc != null)
			drawnArc.draw(g2d, this.sheetId, getZoom());
	}

	/**
	 * Metoda s�u��ca do ustawiania skali powi�kszenia.
	 * @param zoom int - nowa warto�� powi�kszenia
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
	 * Metoda przewijania arkusza w poziomie za pomoc� wa�ka myszy.
	 * @param delta int - wielko�� przewini�cia
	 */
	public void scrollSheetHorizontal(int delta) {
		GUIManager gui = GUIManager.getDefaultGUIManager();
		WorkspaceSheet sheet = gui.getWorkspace().getSheets().get(gui.IDtoIndex(sheetId));
		sheet.scrollHorizontal(delta);
	}

	/**
	 * Metoda przewijania arkusza w pionie za pomoc� wa�ka myszy.
	 * @param delta int - wielko�� przewini�cia
	 */
	public void scrollSheetVertical(int delta) {
		GUIManager gui = GUIManager.getDefaultGUIManager();
		WorkspaceSheet sheet = gui.getWorkspace().getSheets().get(gui.IDtoIndex(sheetId));
		sheet.scrollVertical(delta);
	}

	/**
	 * Metoda realizuje zmian� rozmiaru arkusza, podczas przesuwania po jego lub poza
	 * jego obszar, element�w. Jest ona jedynie wykonywana gdy automatyczne zwi�kszanie
	 * rozmiaru arkusza jest aktywne (isAutoDragScroll = true). Zmiana rozmiaru liczona
	 * jest na podstawie r�nicy pomi�dzy wcze�niejsz� pozycj� przeci�ganego elementu a aktualn�.
	 * @param currentPoint Point - aktualna pozycja przeci�ganego elementu
	 * @param previousPoint Point - wcze�niejsza pozycja przeci�ganego elementu
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
	 * Metoda zwi�zana z mousePressed(MouseEvent).
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
	 * Metoda zwi�zana z mousePressed(MouseEvent).
	 * @param p Point - punkt dodawania tranzycji czasowej
	 */
	private void addNewTimeTransition(Point p) {
		if (isLegalLocation(p)) {
			TimeTransition n = new TimeTransition(IdGenerator.getNextId(),this.sheetId, p);
			this.getSelectionManager().selectOneElementLocation(n.getLastLocation());
			getNodes().add(n);
		}
	}

	/**
	 * Metoda sprawdza czy podany punkt jest akceptowalny z punktu widzenia rozmiar�w arkusza.
	 * Wykorzystywana jets ta metoda podczas przeci�gania element�w po arkuszu.
	 * @param point Point - punkt, kt�rego poprawno�� wsp�rz�dnych b�dzie sprawdzana
	 * @return boolean - true je�li podany w parametrze punkt jest poprawny; 
	 * 		false w przypadku przeciwnym
	 */
	public boolean isLegalLocation(Point point) {
		if (point.x > 20 && point.y > 20 && point.x < (getSize().width - 20)
				&& point.y < (getSize().height - 20)) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Usuwanie �uku - rozkaz z menu kontekstowego na �uku
	 */
	public void clearDrawnArc() {
		if (this.drawnArc != null) {
			drawnArc.unlinkElementLocations();
			drawnArc = null;
		}
	}
	
	/**
	 * Metoda zwraca obiekt przechowuj�cy dane o ca�ej rysowanej sieci.
	 * @return PetriNet - reprezentacja sieci w programie
	 */
	public PetriNet getPetriNet() {
		return petriNet;
	}
	
	/**
	 * Metoda pozwala na ustawienie aktualnego prostok�ta zaznaczenia, na podstawie kt�rego
	 * rysowany jest obszar zaznaczenia oraz wybierane s� obiekty, kt�re kwalifikuj� si� aby
	 * zosta� zaznaczone.
	 * @param selectingRect Rectangle - obszar prostok�tny definiuj�cy zaznaczenie
	 */
	public void setSelectingRect(Rectangle selectingRect) {
		this.selectingRect = selectingRect;
	}

	/**
	 * Metoda pozwala na pobranie aktualnie u�ywanego trybu rysowania. Dost�pne tryby definiowane
	 * s� przez typ DrawModes.
	 * @return DrawModes - aktualny tryb rysowania, z pola this.drawMode
	 */
	public DrawModes getDrawMode() {
		return this.drawMode;
	}

	/**
	 * Metoda pozwala na ustawienie aktualnego trybu rysowania na bie��cym arkuszu. Tryby
	 * definiowane s� przez typ DrawModes. Ustawienie trybu rysowania powoduje zmian�
	 * kursora na arkuszu.
	 * @param newMode DrawModes - nowy tryb rysowania
	 */
	public void setDrawMode(DrawModes newMode) {
		this.drawMode = newMode;
		this.setCursorForMode();
	}
	
	/**
	 * Metoda pozwala na pobranie stanu symulacji. Je�li symulacja jest aktywna
	 * (isSimulationActive = true) wszelkie interakcje z arkuszem s� zablokowane.
	 * @return boolean - true je�li symulacja jest aktualnie aktywna;
	 * 		false je�li symulacja jest zatrzymana
	 */
	public boolean isSimulationActive() {
		return isSimulationActive;
	}

	/**
	 * Metoda pozwala na ustawienie stanu symulacji dla danego arkusza. W sytuacji gdy symulacja
	 * jest aktywna (isSimulationActive = true) wszelkie interakcje z danym arkuszem zostaj�
	 * zablokowane do momentu jej zako�czenia.
	 * @param isSimulationActive boolean - true je�li symulacja ma by� aktywna
	 */
	public void setSimulationActive(boolean isSimulationActive) {
		this.isSimulationActive = isSimulationActive;
	}

	/**
	 * Metoda pozwala ustawi� obiekt klasy SelectionManager zarz�dzaj�cy zaznaczeniem
	 * na danym arkuszu.
	 * @param selectionManager SelectionManager - nowy manager zaznaczenia
	 */
	public void setSelectionManager(SelectionManager selectionManager) {
		this.selectionManager = selectionManager;
	}

	/**
	 * Metoda pozwala pobra� obiekt klasy SelectionManager zarz�dzaj�cy zaznaczeniem na
	 * danym arkuszu.
	 * @return SelectionManager - manager selekcji
	 */
	public SelectionManager getSelectionManager() {
		return selectionManager;
	}

	/**
	 * Metoda pozwala pobra� obiekt b�d�cy menu kontekstowym dla danego arkusza.
	 * @return SheetPopupMenu - obiekt menu kontekstowego
	 */
	public SheetPopupMenu getSheetPopupMenu() {
		return sheetPopupMenu;
	}

	/**
	 * Pozwala ustawi� obiekt b�d�cy menu kontekstowym danego arkusza.
	 * @param sheetPopupMenu SheetPopupMenu - obiekt menu kontekstowego
	 */
	public void setSheetPopupMenu(SheetPopupMenu sheetPopupMenu) {
		this.sheetPopupMenu = sheetPopupMenu;
	}

	/**
	 * Metoda pozwala pobra� obiekt b�d�cy menu kontekstowym dla ka�dego miejsca.
	 * @return PlacePopupMenu - obiekt menu kontekstowego
	 */
	public PlacePopupMenu getPlacePopupMenu() {
		return placePopupMenu;
	}

	/**
	 * Metoda pozwala ustawi� obiekt b�d�cy menu kontekstowym dla ka�dego miejsca.
	 * @param placePopupMenu PlacePopupMenu - nowe menu kontekstowe
	 */
	public void setPlacePopupMenu(PlacePopupMenu placePopupMenu) {
		this.placePopupMenu = placePopupMenu;
	}

	/**
	 * Metoda pozwala pobra� obiekt b�d�cy menu kontekstowym dla ka�dej tranzycji.
	 * @return TransitionPopupMenu - obiekt menu kontekstowego tranzycji
	 */
	public TransitionPopupMenu getTransitionPopupMenu() {
		return transitionPopupMenu;
	}

	/**
	 * Metoda pozwala ustawi� obiekt b�d�cy menu kontekstowym dla ka�dej tranzycji.
	 * @param transitionPopupMenu TransitionPopupMenu - nowe menu kontekstowe
	 */
	public void setTransitionPopupMenu(TransitionPopupMenu transitionPopupMenu) {
		this.transitionPopupMenu = transitionPopupMenu;
	}

	/**
	 * Metoda pozwala pobra� obiekt b�d�cy menu kontekstowym dla ka�dego �uku.
	 * @return ArcPopupMenu - obiekt menu kontekstowego
	 */
	public ArcPopupMenu getArcPopupMenu() {
		return arcPopupMenu;
	}

	/**
	 * Metoda pozwala ustawi� obiekt b�d�cy menu kontekstowym dla ka�dego �uku.
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
	 * Metoda pozwala na sprawdzenie stanu automatycznego zwi�kszania rozmiaru arkusza
	 * podczas przeci�gania obiektu poza jego prawy b�d� dolny brzeg. Je�li przyjmuje
	 * warto�� true, to podczas przeci�gania obiekt�w poza obszar arkusza, zostanie on
	 * automatycznie zwi�kszony.
	 * @return boolean - true je�li automatyczna zmiana arkusza jest aktywna; 
	 * 		false w przypadku przeciwnym
	 */
	public boolean isAutoDragScroll() {
		return autoDragScroll;
	}

	/**
	 * Metoda pozwala na ustawienie stanu automatycznego zwi�kszania rozmiaru arkusza podczas
	 * przeci�gania obiektu poza jego prawy b�d� dolny brzeg. Je�li przyjmuje warto�� true, to
	 * podczas przeci�gania obiekt�w poza obszar arkusza, zostanie on automatycznie zwi�kszony.
	 * @param autoDragScroll boolean - nowy stan automatycznego zwi�kszania rozmiaru arkusza
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
	 * Metoda zwracaj�ca liczbow� warto�� powi�kszenia.
	 * @return int - zoom
	 */
	public int getZoom() {
		return zoom;
	}

	/**
	 * Metoda zwracaj�ca obiekt wymiar�w obrazu. Zwi�zana z metod� setZoom().
	 * @return Dimension - obiekt wymiar�w
	 */
	public Dimension getOriginSize() {
		return originSize;
	}

	/**
	 * Metoda ustawiaj�ca wymiary obrazu. Zwi�zana z metod� setZoom().
	 * @param originSize Dimension - obiekt wymiar�w
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
	 * Prywatna klasa wewn�trz GraphPanel, zajmuj�ca si� skr�tami klawiatury,
	 * obs�ugiwanymi przez obiekt klasy GraphPanel.
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
	 * Prywatna klasa wewn�trz GraphPanel, zajmuj�ca si� realizacj� interakcji
	 * wywo�ywanych ze strony klikni�� mysz�. 
	 * @author students
	 *
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
		 * Metoda aktywowana przez podw�jne klikni�cie przycisku myszy.
		 * @param e MouseEvent - obiekt przekazywany w efekcie podw�jnego klikni�cia
		 */
		public void mouseClicked(MouseEvent e) {
			if (e.getClickCount() == 2) {
				if (e.getButton() == MouseEvent.BUTTON1)
					getSelectionManager().increaseTokensNumber();
				// nie dziala, trzeba by jakos ograniczyc sytuacje dla popupmenu
				if (e.getButton() == MouseEvent.BUTTON3)
					getSelectionManager().decreaseTokensNumber();
			}
		}

		/**
		 * Przeci��ona metoda \textit{mousePressed} w klasie jjava.awt.event.MouseAdapter,
		 * zostaje wywo�ana za ka�dym razem, gdy kt�rekolwiek z klawiszy myszy zostanie
		 * naci�ni�ty nad obszarem arkusza. W nast�pstwie tego zdarzenia sprawdzane jest dla
		 * miejsca klikni�cia zosta�y spe�nione warunki przeci�cia z kt�rymkolwiek z lokalizacji
		 * wierzcho�k�w ElementLocation lub �uk�w znajduj�cych si� na bie��cym arkuszu. W
		 * zale�no�ci od wyniku tego sprawdzenia oraz trybu rysowania i modyfikator�w klikni�cia
		 * (prawy i lewy przycisk myszy, klawisz Ctrl, Alt, Shift podejmowane s� odpowiednie
		 * akcje, w du�ej mierze przy wykorzystaniu obiektu SelectionManager.
		 * @param e MouseEvent - obiekt klasy przekazywany w efekcie klikni�cia mysz�
		 */
		public void mousePressed(MouseEvent e) {
			mousePt = e.getPoint();
			mousePt.setLocation(e.getPoint().getX() * 100 / zoom, e.getPoint().getY() * 100 / zoom);
			ElementLocation el = getSelectionManager().getPossiblySelectedElementLocation(mousePt);
			Arc a = getSelectionManager().getPossiblySelectedArc(mousePt);
			// nie klini�to ani Node ani Arc
			if (el == null && a == null) {
				if (e.getButton() == MouseEvent.BUTTON3) {
					if (getDrawMode() == DrawModes.POINTER)
						getSheetPopupMenu().show(e);
					setDrawMode(DrawModes.POINTER);
				}
				if (!e.isShiftDown() && !e.isControlDown()) {
					getSelectionManager().deselectAllElements();
				}
				switch (getDrawMode()) {
				case POINTER:
					getSelectionManager().selectSheet();
					setSelectingRect(new Rectangle(mousePt.x, mousePt.y, 0, 0));
					clearDrawnArc();
					break;
				case PLACE:
					addNewPlace(mousePt);
					break;
				case TRANSITION:
					addNewTransition(mousePt);
					break;
				case ARC:
					clearDrawnArc();
					break;
				case TIMETRANSITION:
					addNewTimeTransition(mousePt);
					break;
				default:
					break;
				}
			}
			// klinieto w Node, mozliwe ze tez a Arc, ale nie zostanie ono
			// zaznaczone, poniewa� to Node jest na wierzchu
			else if (el != null) {
				if (getDrawMode() == DrawModes.ARC) {
					getSelectionManager().deselectAllElements();
					if (drawnArc == null) {
						drawnArc = new Arc(el);
					} else {
						if (drawnArc.checkIsCorect(el)) {
							Arc arc = new Arc(IdGenerator.getNextId(),
									drawnArc.getStartLocation(), el);
							getArcs().add(arc);
						}
						clearDrawnArc();
					}
				} else if (getDrawMode() == DrawModes.ERASER) {
					getSelectionManager().deleteElementLocation(el);
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
				if (e.getButton() == MouseEvent.BUTTON3) {
					if (el.getParentNode().getType() == PetriNetElementType.PLACE)
						getPlacePopupMenu().show(e);
					else
						getTransitionPopupMenu().show(e);
				}
			}
			// klinieto w Arc, wiec zostanie ono zaznaczone
			else if (a != null) {
				if (getDrawMode() == DrawModes.ERASER) {
					getSelectionManager().deleteArc(a);
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
				if (e.getButton() == MouseEvent.BUTTON3) {
					getArcPopupMenu().show(e);
				}
			}
			e.getComponent().repaint();
		}
	} //end class MouseHandler

	/**
	 * Prywatna klasa wewn�trz GraphPanel, realizuj�ca interakcje ze strony
	 * myszy zwi�zane z jej poruszaniem si� po arkuszu.
	 * @author students
	 *
	 */
	private class MouseMotionHandler extends MouseMotionAdapter {
		Point delta = new Point();

		/**
		 * Przeci��ona metoda mouseDragged w klasie jjava.awt.event.MouseMotionAdapter,
		 * zostaje wywo�ana za ka�dym razem, gdy kt�rekolwiek z klawiszy myszy zostanie
		 * naci�ni�ty w po��czeniu z przesuwaniem myszy nad obszarem arkusza. W nast�pstwie
		 * tego zdarzenia sprawdzane jest dla miejsca klikni�cia zosta�y spe�nione warunki
		 * przeci�cia z kt�rymkolwiek z lokalizacji wierzcho�k�w ElementLocation lub �uk�w
		 * znajduj�cych si� na bie��cym arkuszu. Je�li warunki zosta� spe�nione, wykonywane
		 * jest przesuni�cie pozycji wszystkich zaznaczonych lokalizacji wierzcho�k�w, a co
		 * za tym idzie �uk�w, o wektor przesuni�cia myszy. W przypadku niespe�nienia tych
		 * warunk�w oraz gdy aktualnym narz�dziem rysowania jest wska�nik, rysowany jest
		 * prostok�t zaznaczenia, po��czony ze sprawdzaniem na bie��co, kt�re elementy
		 * zawieraj� si� w obszarze zaznaczenia.
		 * @param e MouseEvent - obiekt klasy przekazywany w efekcie przeci�gni�cia mysz�
		 */
		public void mouseDragged(MouseEvent e) {
			Point dragPoint = e.getPoint();
			dragPoint.setLocation(e.getX() * 100 / zoom, e.getY() * 100 / zoom);
			if (getDrawMode() == DrawModes.ARC && drawnArc != null)
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

		@Override
		public void mouseMoved(MouseEvent e) {
			if (e.getButton() == MouseEvent.BUTTON3)
				return;
			if (getDrawMode() == DrawModes.ARC && drawnArc != null) {
				Point movePoint = e.getPoint();
				movePoint.setLocation(e.getX() * 100 / zoom, e.getY() * 100 / zoom);
				drawnArc.setEndPoint(movePoint);
				drawnArc.checkIsCorect(getSelectionManager().getPossiblySelectedElementLocation(movePoint));
			} else
				clearDrawnArc();
			e.getComponent().repaint();
		}
	} //end class MouseMotionHandler

	/**
	 * Wewn�trzna klasa odpowiedzialna za ob�usg� rolki myszy.
	 * @author students
	 *
	 */
	public class MouseWheelHandler implements MouseWheelListener {
		/**
		 * Metoda odpowiedzialna za dzia�anie rozpocz�te przez przesuwanie rolki
		 * myszy nad arkusze. W zale�no�ci czy wci�ni�tych jest klawisz CTRL czy
		 * te� SHIFT czy te� �aden klawisz - dzia�ania s� r�ne.
		 * @param e MouseWheelEvent - obiekt klasy przekazywany w efekcie u�ycia wa�ka myszy
		 */
		public void mouseWheelMoved(MouseWheelEvent e) {
			if (e.isControlDown())
				setZoom(getZoom() - 10 * e.getWheelRotation(), getZoom());
			else if (e.isShiftDown())
				scrollSheetHorizontal(e.getWheelRotation() * e.getScrollAmount() * 2);
			else
				scrollSheetVertical(e.getWheelRotation() * e.getScrollAmount() * 2);
		}
	}
}