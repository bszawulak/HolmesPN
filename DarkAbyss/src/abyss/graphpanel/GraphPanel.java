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

	public enum DrawModes {
		POINTER, PLACE, TRANSITION, ARC, ERASER, TIMETRANSITION;
	}

	public GraphPanel(int sheetId, PetriNet petriNet,
			ArrayList<Node> nodesList, ArrayList<Arc> arcsList) {
		this.petriNet = petriNet;
		this.sheetId = sheetId;
		this.setNodesAndArcs(nodesList, arcsList);
		this.Initialize();
	}

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

	public int getSheetId() {
		return sheetId;
	}

	public void setSheetId(int sheetID) {
		this.sheetId = sheetID;
	}

	public void setNodesAndArcs(ArrayList<Node> nodes, ArrayList<Arc> arcs) {
		this.nodes = nodes;
		this.arcs = arcs;
		this.revalidate();
		this.repaint();
	}

	public void setNodes(ArrayList<Node> nodes) {
		this.nodes = nodes;
		this.revalidate();
		this.repaint();
	}

	public ArrayList<Node> getNodes() {
		return this.nodes;
	}

	public void setArcs(ArrayList<Arc> arcs) {
		this.arcs = arcs;
		this.revalidate();
		this.repaint();
	}

	public ArrayList<Arc> getArcs() {
		return arcs;
	}

	public Rectangle getSelectingRect() {
		return selectingRect;
	}

	public void setSelectingRect(Rectangle selectingRect) {
		this.selectingRect = selectingRect;
	}

	public DrawModes getDrawMode() {
		return this.drawMode;
	}

	public void setDrawMode(DrawModes newMode) {
		this.drawMode = newMode;
		this.setCursorForMode();
	}

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

	public boolean isSimulationActive() {
		return isSimulationActive;
	}

	public void setSimulationActive(boolean isSimulationActive) {
		this.isSimulationActive = isSimulationActive;
	}

	public void setSelectionManager(SelectionManager selectionManager) {
		this.selectionManager = selectionManager;
	}

	public SelectionManager getSelectionManager() {
		return selectionManager;
	}

	public SheetPopupMenu getSheetPopupMenu() {
		return sheetPopupMenu;
	}

	public void setSheetPopupMenu(SheetPopupMenu sheetPopupMenu) {
		this.sheetPopupMenu = sheetPopupMenu;
	}

	public PlacePopupMenu getPlacePopupMenu() {
		return placePopupMenu;
	}

	public void setPlacePopupMenu(PlacePopupMenu placePopupMenu) {
		this.placePopupMenu = placePopupMenu;
	}

	public TransitionPopupMenu getTransitionPopupMenu() {
		return transitionPopupMenu;
	}

	public void setTransitionPopupMenu(TransitionPopupMenu transitionPopupMenu) {
		this.transitionPopupMenu = transitionPopupMenu;
	}

	public ArcPopupMenu getArcPopupMenu() {
		return arcPopupMenu;
	}

	public void setArcPopupMenu(ArcPopupMenu arcPopupMenu) {
		this.arcPopupMenu = arcPopupMenu;
	}

	public boolean isDrawMesh() {
		return drawMesh;
	}

	public void setDrawMesh(boolean drawMesh) {
		this.drawMesh = drawMesh;
		this.invalidate();
		this.repaint();
	}

	public boolean isAutoDragScroll() {
		return autoDragScroll;
	}

	public void setAutoDragScroll(boolean autoDragScroll) {
		this.autoDragScroll = autoDragScroll;
	}

	@Override
	public void paintComponent(Graphics g) {
		g.setColor(new Color(0x00f0f0f0));
		g.fillRect(0, 0, getWidth(), getHeight());
		Graphics2D g2d = (Graphics2D) g.create();
		if (isDrawMesh())
			drawMesh(g2d);
		drawPetriNet(g2d);
	}

	private void drawMesh(Graphics2D g2d) {
		g2d.setColor(EditorResources.graphPanelMeshColor);
		for (int i = meshSize; i < this.getWidth(); i += meshSize)
			g2d.drawLine(i, 0, i, this.getHeight());
		for (int i = meshSize; i < this.getHeight(); i += meshSize)
			g2d.drawLine(0, i, this.getWidth(), i);
	}

	public boolean isSnapToMesh() {
		return snapToMesh;
	}

	public void setSnapToMesh(boolean snapToMesh) {
		this.snapToMesh = snapToMesh;
	}

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

	public int getZoom() {
		return zoom;
	}

	public void setZoom(int zoom) {
		if (getOriginSize() == null)
			setOriginSize(this.getSize());
		if (getOriginSize().width * zoom / 100 < 10)
			return;
		this.zoom = zoom;
		System.out.println(this.getOriginSize().width * zoom / 100);
		this.setSize(this.getOriginSize().width * zoom / 100,
				this.getOriginSize().height * zoom / 100);
		GUIManager gui = GUIManager.getDefaultGUIManager();
		WorkspaceSheet sheet = gui.getWorkspace().getSheets()
				.get(gui.IDtoIndex(sheetId));
		sheet.revalidate();
		this.invalidate();
		this.repaint();

	}

	public Dimension getOriginSize() {
		return originSize;
	}

	public void setOriginSize(Dimension originSize) {
		this.originSize = originSize;
	}

	public void scrollSheetHorizontal(int delta) {
		GUIManager gui = GUIManager.getDefaultGUIManager();
		WorkspaceSheet sheet = gui.getWorkspace().getSheets()
				.get(gui.IDtoIndex(sheetId));
		sheet.scrollHorizontal(delta);
	}

	public void scrollSheetVertical(int delta) {
		GUIManager gui = GUIManager.getDefaultGUIManager();
		WorkspaceSheet sheet = gui.getWorkspace().getSheets()
				.get(gui.IDtoIndex(sheetId));
		sheet.scrollVertical(delta);
	}

	// autoscrolluje przy wyjechaniu poza viewport
	public void adjustScroll(Point currentPoint, Point previousPoint) {
		if (!isAutoDragScroll())
			return;
		GUIManager gui = GUIManager.getDefaultGUIManager();
		WorkspaceSheet sheet = gui.getWorkspace().getSheets()
				.get(gui.IDtoIndex(sheetId));
		Dimension viewSize = sheet.getViewport().getSize();
		Point delta = new Point();
		delta.setLocation(currentPoint.x - previousPoint.x, currentPoint.y
				- previousPoint.y);
		JViewport viewport = sheet.getViewport();
		Point viewPoint = new Point(currentPoint.x
				- viewport.getViewPosition().x, currentPoint.y
				- viewport.getViewPosition().y);
		if (isAutoDragScroll()
				&& ((viewSize.width - 20) < viewPoint.x
						|| (viewSize.height - 20) < viewPoint.y || (20 > viewPoint.x || (20 > viewPoint.y)))) {
			sheet.scrollHorizontal(delta.x);
			sheet.scrollVertical(delta.y);
		}
	}

	private void addNewPlace(Point p) {
		if (isLegalLocation(p)) {
			Place n = new Place(IdGenerator.getNextId(), this.sheetId, p);
			this.getSelectionManager().selectOneElementLocation(
					n.getLastLocation());
			getNodes().add(n);
		}
	}

	// TODO z tym te¿ trzeba co zrobic, bo tak to kurwa byc nie mo¿e
	private void addNewTransition(Point p) {
		if (isLegalLocation(p)) {
			Transition n = new Transition(IdGenerator.getNextId(),
					this.sheetId, p);
			this.getSelectionManager().selectOneElementLocation(
					n.getLastLocation());
			getNodes().add(n);
		}
	}
	
	private void addNewTimeTransition(Point p) {
		if (isLegalLocation(p)) {
			TimeTransition n = new TimeTransition(IdGenerator.getNextId(),
					this.sheetId, p);
			this.getSelectionManager().selectOneElementLocation(
					n.getLastLocation());
			getNodes().add(n);
		}
	}

	public boolean isLegalLocation(Point point) {
		if (point.x > 20 && point.y > 20 && point.x < (getSize().width - 20)
				&& point.y < (getSize().height - 20)) {
			return true;
		} else {
			return false;
		}
	}

	public void clearDrawnArc() {
		if (this.drawnArc != null) {
			drawnArc.unlinkElementLocations();
			drawnArc = null;
		}
	}

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

	private class MouseHandler extends MouseAdapter {

		@Override
		public void mouseReleased(MouseEvent e) {
			setSelectingRect(null);
			e.getComponent().repaint();
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			if (e.getClickCount() == 2) {
				if (e.getButton() == MouseEvent.BUTTON1)
					getSelectionManager().increaseTokensNumber();
				// nie dziala, trzeba by jakos ograniczyc sytuacje dla popupmenu
				if (e.getButton() == MouseEvent.BUTTON3)
					getSelectionManager().decreaseTokensNumber();
			}
		}

		@Override
		public void mousePressed(MouseEvent e) {
			mousePt = e.getPoint();
			mousePt.setLocation(e.getPoint().getX() * 100 / zoom, e.getPoint()
					.getY() * 100 / zoom);
			ElementLocation el = getSelectionManager()
					.getPossiblySelectedElementLocation(mousePt);
			Arc a = getSelectionManager().getPossiblySelectedArc(mousePt);
			// nie klinieto ani Node ani Arc
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
						getSelectionManager()
								.toggleElementLocationSelection(el);
					else if (!getSelectionManager().isElementLocationSelected(
							el)) {
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
	}

	private class MouseMotionHandler extends MouseMotionAdapter {

		Point delta = new Point();

		@Override
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
				delta.setLocation(dragPoint.getX() - mousePt.x,
						dragPoint.getY() - mousePt.y);
				for (ElementLocation el : getSelectionManager()
						.getSelectedElementLocations()) {
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
				movePoint.setLocation(e.getX() * 100 / zoom, e.getY() * 100
						/ zoom);
				drawnArc.setEndPoint(movePoint);
				drawnArc.checkIsCorect(getSelectionManager()
						.getPossiblySelectedElementLocation(movePoint));

			} else
				clearDrawnArc();
			e.getComponent().repaint();
		}
	}

	public class MouseWheelHandler implements MouseWheelListener {

		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {
			if (e.isControlDown())
				setZoom(getZoom() - 10 * e.getWheelRotation());
			else if (e.isShiftDown())
				scrollSheetHorizontal(e.getWheelRotation()
						* e.getScrollAmount() * 2);
			else
				scrollSheetVertical(e.getWheelRotation() * e.getScrollAmount()
						* 2);
		}
	}
}