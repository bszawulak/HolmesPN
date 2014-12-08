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
 * Klasa, której zadaniem jest reprezentacja graficzna u¿ywanej w programie
 * sieci Petriego oraz oferowanie interfejsu umo¿liwiaj¹cego interakcje ze
 * strony u¿ytkownika.
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
	 * @param petriNet PetriNet - sieæ Petriego
	 * @param nodesList ArrayList[Node]- lista wierzcho³ków
	 * @param arcsList ArrayList[Arc]- lista ³uków
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

	public GraphPanel getThis() {
		return this;
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
	 * Metoda pozwala na ustawienie zbioru wierzcho³ków oraz ³uków dla danego arkusza.
	 * Zbiór ten jest wspólny dla wszystkich arkuszy i jest przechowywana w obiekcie
	 * PetriNet, o tym gdzie dany element zostanie narysowany decyduje jego lokalizacja
	 * ElementLocation.SheetId.
	 * @param nodes ArrayList[Nodes] - lista wêz³ów przekazywana do danego arkusza
	 * @param arcs ArrayList[Arc] - lista ³uków przekazywana do danego arkusza
	 */
	public void setNodesAndArcs(ArrayList<Node> nodes, ArrayList<Arc> arcs) {
		this.nodes = nodes;
		this.arcs = arcs;
		this.revalidate();
		this.repaint();
	}

	/**
	 * Metoda pozwala na ustawienie zbioru wierzcho³ków dla danego arkusza.
	 * Zbiór ten jest wspólny dla wszystkich arkuszy i jest przechowywana w
	 * obiekcie PetriNet, o tym gdzie dany element zostanie narysowany decyduje
	 * jego lokalizacja
	 * @param nodes ArrayList[Node] - lista wêz³ów przekazywana do danego arkusza
	 */
	public void setNodes(ArrayList<Node> nodes) {
		this.nodes = nodes;
		this.revalidate();
		this.repaint();
	}

	/**
	 * Metoda pozwala na pobrania listy wierzcho³ków przypisanych do danego arkusza.
	 * Lista ta jest wspólna dla wszystkich arkuszy i jest przechowywana w obiekcie PetriNet.
	 * @return ArrayList[Node] - lista wêz³ów
	 */
	public ArrayList<Node> getNodes() {
		return this.nodes;
	}

	/**
	 * Metoda pozwala na ustawienie zbioru ³uków dla danego arkusza. Zbiór ten jest wspólny
	 * dla wszystkich arkuszy, o tym gdzie dany element zostanie narysowany, decyduje jego
	 * lokalizacja startowa i koñcowa (ElementLocation.SheetId).
	 * @param arcs ArrayList[Arc] - lista ³uków przekazywana do danego arkusza
	 */
	public void setArcs(ArrayList<Arc> arcs) {
		this.arcs = arcs;
		this.revalidate();
		this.repaint();
	}

	/**
	 * Metoda pozwala na pobrania listy ³uków przypisanych do danego arkusza. Lista ta jest
	 * wspólna dla wszystkich arkuszy i jest przechowywana w obiekcie PetriNet.
	 * @return ArrayList[Arc] - zwraca listê ³uków
	 */
	public ArrayList<Arc> getArcs() {
		return arcs;
	}

	/**
	 * Metoda pozwala na pobrania aktualnego prostok¹ta zaznaczenia, na podstawie którego
	 * rysowany jest obszar zaznaczenia oraz wybierane s¹ obiekty, które kwalifikuj¹ siê aby
	 * zostaæ zaznaczone. W sytuacji gdy zaznaczenie nie jest rysowane, przyjmuje wartoœæ null.
	 * @return Rectangle - prostok¹d aktualnego zaznaczenia, z pola this.selectingRect
	 */
	public Rectangle getSelectingRect() {
		return selectingRect;
	}

	/**
	 * Metoda pozwala na ustawienie aktualnego prostok¹ta zaznaczenia, na podstawie którego
	 * rysowany jest obszar zaznaczenia oraz wybierane s¹ obiekty, które kwalifikuj¹ siê aby
	 * zostaæ zaznaczone.
	 * @param selectingRect Rectangle - obszar prostok¹tny definiuj¹cy zaznaczenie
	 */
	public void setSelectingRect(Rectangle selectingRect) {
		this.selectingRect = selectingRect;
	}

	/**
	 * Metoda pozwala na pobranie aktualnie u¿ywanego trybu rysowania. Dostêpne tryby definiowane
	 * s¹ przez typ DrawModes.
	 * @return DrawModes - aktualny tryb rysowania, z pola this.drawMode
	 */
	public DrawModes getDrawMode() {
		return this.drawMode;
	}

	/**
	 * Metoda pozwala na ustawienie aktualnego trybu rysowania na bie¿¹cym arkuszu. Tryby
	 * definiowane s¹ przez typ DrawModes. Ustawienie trybu rysowania powoduje zmianê
	 * kursora na arkuszu.
	 * @param newMode DrawModes - nowy tryb rysowania
	 */
	public void setDrawMode(DrawModes newMode) {
		this.drawMode = newMode;
		this.setCursorForMode();
	}

	/**
	 * 
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
	 * 
	 * @return
	 */
	public BufferedImage createImageFromSheet() {
		Rectangle r = getBounds();
		BufferedImage image = new BufferedImage(r.width, r.height,
				BufferedImage.TYPE_INT_RGB);
		Graphics g = image.getGraphics();
		g.setColor(Color.white);
		g.fillRect(0, 0, getWidth(), getHeight());
		drawPetriNet((Graphics2D) g.create());
		return image;
	}

	/**
	 * Metoda pozwala na pobranie stanu symulacji. Jeœli symulacja jest aktywna
	 * (isSimulationActive = true) wszelkie interakcje z arkuszem s¹ zablokowane.
	 * @return boolean - true jeœli symulacja jest aktualnie aktywna;
	 * 		false jeœli symulacja jest zatrzymana
	 */
	public boolean isSimulationActive() {
		return isSimulationActive;
	}

	/**
	 * Metoda pozwala na ustawienie stanu symulacji dla danego arkusza. W sytuacji gdy symulacja
	 * jest aktywna (isSimulationActive = true) wszelkie interakcje z danym arkuszem zostaj¹
	 * zablokowane do momentu jej zakoñczenia.
	 * @param isSimulationActive boolean - true jeœli symulacja ma byæ aktywna
	 */
	public void setSimulationActive(boolean isSimulationActive) {
		this.isSimulationActive = isSimulationActive;
	}

	/**
	 * Metoda pozwala ustawiæ obiekt klasy SelectionManager zarz¹dzaj¹cy zaznaczeniem
	 * na danym arkuszu.
	 * @param selectionManager SelectionManager - nowy manager zaznaczenia
	 */
	public void setSelectionManager(SelectionManager selectionManager) {
		this.selectionManager = selectionManager;
	}

	/**
	 * Metoda pozwala pobraæ obiekt klasy SelectionManager zarz¹dzaj¹cy zaznaczeniem na
	 * danym arkuszu.
	 * @return SelectionManager - manager selekcji
	 */
	public SelectionManager getSelectionManager() {
		return selectionManager;
	}

	/**
	 * Metoda pozwala pobraæ obiekt bêd¹cy menu kontekstowym dla danego arkusza.
	 * @return SheetPopupMenu - obiekt menu kontekstowego
	 */
	public SheetPopupMenu getSheetPopupMenu() {
		return sheetPopupMenu;
	}

	/**
	 * Pozwala ustawiæ obiekt bêd¹cy menu kontekstowym danego arkusza.
	 * @param sheetPopupMenu SheetPopupMenu - obiekt menu kontekstowego
	 */
	public void setSheetPopupMenu(SheetPopupMenu sheetPopupMenu) {
		this.sheetPopupMenu = sheetPopupMenu;
	}

	/**
	 * Metoda pozwala pobraæ obiekt bêd¹cy menu kontekstowym dla ka¿dego miejsca.
	 * @return PlacePopupMenu - obiekt menu kontekstowego
	 */
	public PlacePopupMenu getPlacePopupMenu() {
		return placePopupMenu;
	}

	/**
	 * Metoda pozwala ustawiæ obiekt bêd¹cy menu kontekstowym dla ka¿dego miejsca.
	 * @param placePopupMenu PlacePopupMenu - nowe menu kontekstowe
	 */
	public void setPlacePopupMenu(PlacePopupMenu placePopupMenu) {
		this.placePopupMenu = placePopupMenu;
	}

	/**
	 * Metoda pozwala pobraæ obiekt bêd¹cy menu kontekstowym dla ka¿dej tranzycji.
	 * @return TransitionPopupMenu - obiekt menu kontekstowego tranzycji
	 */
	public TransitionPopupMenu getTransitionPopupMenu() {
		return transitionPopupMenu;
	}

	/**
	 * Metoda pozwala ustawiæ obiekt bêd¹cy menu kontekstowym dla ka¿dej tranzycji.
	 * @param transitionPopupMenu TransitionPopupMenu - nowe menu kontekstowe
	 */
	public void setTransitionPopupMenu(TransitionPopupMenu transitionPopupMenu) {
		this.transitionPopupMenu = transitionPopupMenu;
	}

	/**
	 * Metoda pozwala pobraæ obiekt bêd¹cy menu kontekstowym dla ka¿dego ³uku.
	 * @return ArcPopupMenu - obiekt menu kontekstowego
	 */
	public ArcPopupMenu getArcPopupMenu() {
		return arcPopupMenu;
	}

	/**
	 * Metoda pozwala ustawiæ obiekt bêd¹cy menu kontekstowym dla ka¿dego ³uku.
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
	 * Metoda pozwala na sprawdzenie stanu automatycznego zwiêkszania rozmiaru arkusza
	 * podczas przeci¹gania obiektu poza jego prawy b¹dŸ dolny brzeg. Jeœli przyjmuje
	 * wartoœæ true, to podczas przeci¹gania obiektów poza obszar arkusza, zostanie on
	 * automatycznie zwiêkszony.
	 * @return boolean - true jeœli automatyczna zmiana arkusza jest aktywna; 
	 * 		false w przypadku przeciwnym
	 */
	public boolean isAutoDragScroll() {
		return autoDragScroll;
	}

	/**
	 * Metoda pozwala na ustawienie stanu automatycznego zwiêkszania rozmiaru arkusza podczas
	 * przeci¹gania obiektu poza jego prawy b¹dŸ dolny brzeg. Jeœli przyjmuje wartoœæ true, to
	 * podczas przeci¹gania obiektów poza obszar arkusza, zostanie on automatycznie zwiêkszony.
	 * @param autoDragScroll boolean - nowy stan automatycznego zwiêkszania rozmiaru arkusza
	 */
	public void setAutoDragScroll(boolean autoDragScroll) {
		this.autoDragScroll = autoDragScroll;
	}

	/**
	 * Przeci¹¿ona metoda paintComponent w klasie javax.swing.JComponent. Jej ka¿dorazowe
	 * wywo³anie powoduje wyczyszczenie aktualnego widoku i narysowanie go od nowa. W tym te¿
	 * momencie wybierane s¹ odpowiednie elementy, które maj¹ zastaæ narysowane na danym arkuszu, 
	 * na podstawie ich lokalizacji ElementLocation.sheetId, nastêpnie ka¿demu z obiektów
	 * zakwalifikowanych, zlecane jest narysowanie "siebie" na dostarczonym w parametrze metody
	 * obiekcie Graphics2D. W rysowaniu wykorzystany zosta³ podwójny bufor oraz filtr antyaliasingowy.
	 * @param g Graphics - obiekt zawieraj¹cy prezentowan¹ grafikê
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
	 * Metoda zwracaj¹ca liczbow¹ wartoœæ powiêkszenia.
	 * @return int - zoom
	 */
	public int getZoom() {
		return zoom;
	}

	/**
	 * Metoda s³u¿¹ca do ustawiania skali powiêkszenia.
	 * @param zoom int - nowa wartoœæ powiêkszenia
	 */
	public void setZoom(int zoom) {
		if (getOriginSize() == null)
			setOriginSize(this.getSize());
		if (getOriginSize().width * zoom / 100 < 10)
			return;
		this.zoom = zoom;
		System.out.println(this.getOriginSize().width * zoom / 100);
		this.setSize(this.getOriginSize().width * zoom / 100, this.getOriginSize().height * zoom / 100);
		GUIManager gui = GUIManager.getDefaultGUIManager();
		WorkspaceSheet sheet = gui.getWorkspace().getSheets().get(gui.IDtoIndex(sheetId));
		sheet.revalidate();
		this.invalidate();
		this.repaint();

	}

	/**
	 * Metoda zwracaj¹ca obiekt wymiarów obrazu. Zwi¹zana z metod¹ setZoom().
	 * @return Dimension - obiekt wymiarów
	 */
	public Dimension getOriginSize() {
		return originSize;
	}

	/**
	 * Metoda ustawiaj¹ca wymiary obrazu. Zwi¹zana z metod¹ setZoom().
	 * @param originSize Dimension - obiekt wymiarów
	 */
	public void setOriginSize(Dimension originSize) {
		this.originSize = originSize;
	}

	/**
	 * Metoda przewijania arkusza w poziomie za pomoc¹ wa³ka myszy.
	 * @param delta int - wielkoœæ przewiniêcia
	 */
	public void scrollSheetHorizontal(int delta) {
		GUIManager gui = GUIManager.getDefaultGUIManager();
		WorkspaceSheet sheet = gui.getWorkspace().getSheets().get(gui.IDtoIndex(sheetId));
		sheet.scrollHorizontal(delta);
	}

	/**
	 * Metoda przewijania arkusza w pionie za pomoc¹ wa³ka myszy.
	 * @param delta int - wielkoœæ przewiniêcia
	 */
	public void scrollSheetVertical(int delta) {
		GUIManager gui = GUIManager.getDefaultGUIManager();
		WorkspaceSheet sheet = gui.getWorkspace().getSheets().get(gui.IDtoIndex(sheetId));
		sheet.scrollVertical(delta);
	}

	/**
	 * Metoda realizuje zmianê rozmiaru arkusza, podczas przesuwania po jego lub poza
	 * jego obszar, elementów. Jest ona jedynie wykonywana gdy automatyczne zwiêkszanie
	 * rozmiaru arkusza jest aktywne (isAutoDragScroll = true). Zmiana rozmiaru liczona
	 * jest na podstawie ró¿nicy pomiêdzy wczeœniejsz¹ pozycj¹ przeci¹ganego elementu a aktualn¹.
	 * @param currentPoint Point - aktualna pozycja przeci¹ganego elementu
	 * @param previousPoint Point - wczeœniejsza pozycja przeci¹ganego elementu
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
	 * Metoda zwi¹zana z mousePressed(MouseEvent).
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
	 * Metoda zwi¹zana z mousePressed(MouseEvent).
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
	 * Metoda sprawdza czy podany punkt jest akceptowalny z punktu widzenia rozmiarów arkusza.
	 * Wykorzystywana jets ta metoda podczas przeci¹gania elementów po arkuszu.
	 * @param point Point - punkt, którego poprawnoœæ wspó³rzêdnych bêdzie sprawdzana
	 * @return boolean - true jeœli podany w parametrze punkt jest poprawny; 
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
	 * Usuwanie ³uku - rozkaz z menu kontekstowego na ³uku
	 */
	public void clearDrawnArc() {
		if (this.drawnArc != null) {
			drawnArc.unlinkElementLocations();
			drawnArc = null;
		}
	}

	/**
	 * Prywatna klasa wewn¹trz GraphPanel, zajmuj¹ca siê skrótami klawiaturowymi,
	 * realizowanymi przez GraphPanel.
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
	 * Prywatna klasa wewn¹trz GraphPanel, zajmuj¹ca siê realizacj¹ interakcji
	 * wywo³ywanych ze strony klikniêæ mysz¹. 
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
		 * Metoda aktywowana przez podwójne klikniêcie przycisku myszy.
		 * @param e MouseEvent - obiekt przekazywany w efekcie podwójnego klikniêcia
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
		 * Przeci¹¿ona metoda \textit{mousePressed} w klasie jjava.awt.event.MouseAdapter,
		 * zostaje wywo³ana za ka¿dym razem, gdy którekolwiek z klawiszy myszy zostanie
		 * naciœniêty nad obszarem arkusza. W nastêpstwie tego zdarzenia sprawdzane jest dla
		 * miejsca klikniêcia zosta³y spe³nione warunki przeciêcia z którymkolwiek z lokalizacji
		 * wierzcho³ków ElementLocation lub ³uków znajduj¹cych siê na bie¿¹cym arkuszu. W
		 * zale¿noœci od wyniku tego sprawdzenia oraz trybu rysowania i modyfikatorów klikniêcia
		 * (prawy i lewy przycisk myszy, klawisz Ctrl, Alt, Shift podejmowane s¹ odpowiednie
		 * akcje, w du¿ej mierze przy wykorzystaniu obiektu SelectionManager.
		 * @param e MouseEvent - obiekt klasy przekazywany w efekcie klikniêcia mysz¹
		 */
		public void mousePressed(MouseEvent e) {
			mousePt = e.getPoint();
			mousePt.setLocation(e.getPoint().getX() * 100 / zoom, e.getPoint().getY() * 100 / zoom);
			ElementLocation el = getSelectionManager().getPossiblySelectedElementLocation(mousePt);
			Arc a = getSelectionManager().getPossiblySelectedArc(mousePt);
			// nie kliniêto ani Node ani Arc
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
			// zaznaczone, poniewa¿ to Node jest na wierzchu
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
	 * Prywatna klasa wewn¹trz GraphPanel, realizuj¹ca interakcje ze strony
	 * myszy zwi¹zane z jej poruszaniem siê po arkuszu.
	 * @author students
	 *
	 */
	private class MouseMotionHandler extends MouseMotionAdapter {
		Point delta = new Point();

		/**
		 * Przeci¹¿ona metoda mouseDragged w klasie jjava.awt.event.MouseMotionAdapter,
		 * zostaje wywo³ana za ka¿dym razem, gdy którekolwiek z klawiszy myszy zostanie
		 * naciœniêty w po³¹czeniu z przesuwaniem myszy nad obszarem arkusza. W nastêpstwie
		 * tego zdarzenia sprawdzane jest dla miejsca klikniêcia zosta³y spe³nione warunki
		 * przeciêcia z którymkolwiek z lokalizacji wierzcho³ków ElementLocation lub ³uków
		 * znajduj¹cych siê na bie¿¹cym arkuszu. Jeœli warunki zosta³ spe³nione, wykonywane
		 * jest przesuniêcie pozycji wszystkich zaznaczonych lokalizacji wierzcho³ków, a co
		 * za tym idzie ³uków, o wektor przesuniêcia myszy. W przypadku niespe³nienia tych
		 * warunków oraz gdy aktualnym narzêdziem rysowania jest wskaŸnik, rysowany jest
		 * prostok¹t zaznaczenia, po³¹czony ze sprawdzaniem na bie¿¹co, które elementy
		 * zawieraj¹ siê w obszarze zaznaczenia.
		 * @param e MouseEvent - obiekt klasy przekazywany w efekcie przeci¹gniêcia mysz¹
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
	 * Wewnêtrzna klasa odpowiedzialna za ob³usgê rolki myszy.
	 * @author students
	 *
	 */
	public class MouseWheelHandler implements MouseWheelListener {
		/**
		 * Metoda odpowiedzialna za dzia³anie rozpoczête przez przesuwanie rolki
		 * myszy nad arkusze. W zale¿noœci czy wciœniêtych jest klawisz CTRL czy
		 * te¿ SHIFT czy te¿ ¿aden klawisz - dzia³ania s¹ ró¿ne.
		 * @param e MouseWheelEvent - obiekt klasy przekazywany w efekcie u¿ycia wa³ka myszy
		 */
		public void mouseWheelMoved(MouseWheelEvent e) {
			if (e.isControlDown())
				setZoom(getZoom() - 10 * e.getWheelRotation());
			else if (e.isShiftDown())
				scrollSheetHorizontal(e.getWheelRotation() * e.getScrollAmount() * 2);
			else
				scrollSheetVertical(e.getWheelRotation() * e.getScrollAmount() * 2);
		}
	}
}