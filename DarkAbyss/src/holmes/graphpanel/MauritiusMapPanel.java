package holmes.graphpanel;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;

import javax.swing.JPanel;

import holmes.darkgui.GUIManager;
import holmes.petrinet.data.MauritiusMap;
import holmes.petrinet.data.MauritiusMap.BTNode;

/**
 * Metoda odpowiedzialna za rysowanie map Mauritiusa.
 * @author MR
 *
 */
public class MauritiusMapPanel extends JPanel {
	private static final long serialVersionUID = 1028800481995974984L;
	
	private MauritiusMap mmbt;
	//private ArrayList<MapNodeInfo> locationVector = new ArrayList<MapNodeInfo>();
	private int currentVerticalLevel = 0; //AKTUALNY popziom
	private int verticalMulti = 0; //ile poddrzew
	private int offsetVertical = 100;
	private int maxUsedWidth = 0;
	private boolean fullName = true; //czy pełna nazwa tranzycji ma być wyświetlona
	private int baseThickness = 0;
	private int zoom = 100;
	
	private int panelWidth = 0;
	private int panelHeigth = 0;
	private Dimension originSize; //oryginalny rozmiar
	
	public boolean originalSizeKnown = false;
	private boolean contractedMode = false;
	
	private Point mousePt;
	
	/**
	 * Główny konstruktor obiektu klasy MauritiusMapPanel.
	 */
    public MauritiusMapPanel() {
    	panelWidth = 800;
    	panelHeigth = 600;
        setPreferredSize(new Dimension(panelWidth, panelHeigth));
        
        this.addMouseWheelListener(new MouseMapWheelHandler());
        this.addMouseListener(new MouseMapHandler());
    }
    
    /**
     * Metoda dodająca obiekt mapy. Po tym wystarczy odświeżyć panel aby mapa została narysowana.
     * @param mmbt MauritiusMapBT - Mauritius Map Binary Tree
     */
    public void addNewMap(MauritiusMap mmbt, boolean contractedMode) {
    	this.mmbt = mmbt;
    	this.originalSizeKnown = false;
    	this.contractedMode = contractedMode;
    }
  
    /**
     * Metoda ustawia opcję wyświetlania pełnej nazwy tranzycji.
     * @param full boolean - jeśli true - pełna nazwa; false - format "t_"+lokalizacja w wektorze
     */
    public void setFullName(boolean full) {
    	fullName = full;
    }

    /**
     * Główna metoda rysująca mapę Mauritiusa poprzez rekurencyjne przeglądanie drzewa binarnego danych o mapie.
     * @param node BTNode - aktualny węzeł drzewa
     * @param g Graphics - obiekt rysujący
     * @param x int - pozycja startowa X korzenia
     * @param y int - pozycja startowa Y korzenia
     * @param fullName boolean - jeśli true, wyświetla pełne nazwy reakcji
     */
    private void readAndPaintTree(BTNode node, Graphics2D g2d, int x, int y, boolean fullName) {
    	updateWidth(x);
    	String name = node.transName;
    	if(name == null) name = "N/A";
    	
    	int freq = node.transFrequency;
    	int currentMulti = verticalMulti;
    	
    	if(fullName == false) {
    		int loc = node.transLocation;
    		name = "t_"+loc;
    	}
    	
    	//MCT:
    	boolean mctDetected = false;
    	if(name.contains("_MCT")) {
    		int ind = name.indexOf("_MCT");
    		String mctName = name.substring(ind+1);
    		drawRotatedText(g2d, x+8, y+35, 0, "("+mctName+")", false);
    		
    		name = name.substring(0, ind);
    		mctDetected = true;
    	}
    	
    	//rysowanie okręgu i nazwy:
    	if(mctDetected==true && contractedMode)
    		drawCenteredSquare(g2d, x, y, 40, Color.lightGray);
    	else
    		drawCenteredCircle(g2d, x, y, 40, Color.darkGray);
    	
    	//drawCenteredSquare
    	
    	if(mctDetected==true && contractedMode) 
    		;
    	else
    		drawRotatedText(g2d, x+15, y-15, -8, name, false);

    	//ID:
    	String t_id = "t"+node.transLocation;
    	drawRotatedText(g2d, x-27, y+35, 0, t_id, true);
    	
    	
    	if(freq < 10)
    		drawText(g2d, x-4, y+5, freq+"", Color.red); //częstość wystąpień w inwariantach DANEJ(rysowanej poziomo) ścieżki
    	else if(freq < 100)
    		drawText(g2d, x-8, y+5, freq+"", Color.red); 
    	else
    		drawText(g2d, x-14, y+5, freq+"", Color.red);
    	
    	
    	if(node.rightChild != null) {
    		if(contractedMode) {
    			int currentVal = node.transFrequency;
    			
    			while(node.rightChild != null && currentVal == node.rightChild.transFrequency) {
    				node.rightChild = node.rightChild.rightChild;
    			}
    			
    		}
    		
    		if(node.rightChild != null) {
    			drawArrow(g2d, x+20, y, x+100, y, 3, Color.gray);
    			readAndPaintTree(node.rightChild, g2d, x+126, y, fullName); //120+6 (6=offest strzałki)
    		}
    	}
    	
    	if(node.leftChild != null) {
    		int additionalDown = 0;
    		
    		if(currentVerticalLevel == 0) {
    			additionalDown = offsetVertical * verticalMulti;
    		} else if(currentMulti < verticalMulti) {
    			additionalDown += offsetVertical*(verticalMulti-currentMulti);
    			currentMulti++;
    		}
    		
    		currentVerticalLevel++;
    		int currentAltitude = y;
    		int lowerAltitude = y+offsetVertical+additionalDown;
    		
    		drawL_shapeArrow(g2d, x-45, currentAltitude, x-45, lowerAltitude, 3, Color.gray);

    		readAndPaintTree(node.leftChild, g2d, x, lowerAltitude, fullName);
    		verticalMulti++; //powrót z podwęzła, następny +1 odległość
    		currentVerticalLevel--; //powrót na starą wysokość
    	}
    }

    /**
     * Metoda ustala ostateczne rozmiary panelu po narysowaniu drzewa.
     */
	private void normalizeSize() {
		if(panelHeigth < 200+((offsetVertical+10)*verticalMulti))
			panelHeigth = 200+((offsetVertical+10)*verticalMulti);
		
		if(panelWidth < 200+maxUsedWidth)
			panelWidth = 700+maxUsedWidth;
		
		double zoomMod = (double)zoom / (double)100;
		
		//i jedno i drugie potrzebne, aby scrollPanel działa i się aktualizował. No i borderlayout.
		
		setPreferredSize(new Dimension((int)(panelWidth*zoomMod), (int)(panelHeigth*zoomMod)));
		setSize(new Dimension((int)(panelWidth*zoomMod), (int)(panelHeigth*zoomMod)));
	}
    
	/**
	 * Metoda główna odpowiedzialna za rysowanie mapy w ramach odświeżania ekranu.
	 */
    @Override
    public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
    	if(mmbt != null) {
    		baseThickness = mmbt.getRoot().transFrequency;
    		//normalizeBaseThickness();

    		Graphics2D g2d = (Graphics2D) g.create();
    		g2d.scale((float) getZoom() / 100, (float) getZoom() / 100);
    		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    		
			readAndPaintTree(mmbt.getRoot(), g2d, 100, 200, fullName);
			normalizeSize();
			verticalMulti = 0;
			
			if(originalSizeKnown == false) {
				setOriginSize(new Dimension(panelWidth, panelHeigth));
				originalSizeKnown = true;
			}
    	}
    }
    
    //TODO ?
    @SuppressWarnings("unused")
	private int normalizeThickness(int transFrequency) {
    	double tFr = transFrequency;
    	double base = baseThickness;
    	double result = (tFr/base)*10;
    	return (int)result;
	}

	/**
     * Metoda rysuje linię skierowaną w dowolną stronę.
     * @param g2d Graphics2D - obiekt grafiki
     * @param x1 int - x1
     * @param y1 int - y1
     * @param x2 int - x2 (grot)
     * @param y2 int - y2 (grot)
     * @param width int - szerokość
     * @param color Color - kolor
     */
    @SuppressWarnings("unused")
	private void drawLine(Graphics2D g2d, int x1, int y1, int x2, int y2, int width, Color color) {
         double dx = x2 - x1, dy = y2 - y1;
         double angle = Math.atan2(dy, dx);
         int len = (int) Math.sqrt(dx*dx + dy*dy);
         
         AffineTransform atOld = g2d.getTransform();
         AffineTransform at = AffineTransform.getTranslateInstance(x1, y1);
         at.concatenate(AffineTransform.getRotateInstance(angle));
         g2d.transform(at);

         Color old = g2d.getColor();
         g2d.setPaint(color);
         g2d.setStroke(new BasicStroke(width));
         g2d.drawLine(0, 0, len, 0);
         g2d.setPaint(old);
         g2d.setTransform(atOld);
    }
    
    /**
     * Metoda rysuje strzałkę skierowaną w dowolną stronę.
     * @param g2d Graphics2D - obiekt grafiki
     * @param x1 int - x1
     * @param y1 int - y1
     * @param x2 int - x2 (grot)
     * @param y2 int - y2 (grot)
     * @param width int - szerokość
     * @param color Color - kolor
     */
	private void drawArrow(Graphics2D g2d, int x1, int y1, int x2, int y2, int width, Color color) {
        int ARR_SIZE = 6+width;
        double dx = x2 - x1, dy = y2 - y1;
        double angle = Math.atan2(dy, dx);
        
        int len = (int) Math.sqrt(dx*dx + dy*dy);
        AffineTransform atOld = g2d.getTransform();
        AffineTransform at = AffineTransform.getTranslateInstance(x1, y1);
        at.concatenate(AffineTransform.getRotateInstance(angle));
        g2d.transform(at);
        
        Color old = g2d.getColor();
        g2d.setPaint(color);
        g2d.setStroke(new BasicStroke(width));
        g2d.drawLine(0, 0, len, 0);
        int offX = 6; // >0 przesunięcie do przodu
        int offY = 0; // >0 przesunięcie w dół pod kreskę
        g2d.fillPolygon(new int[] {len + offX, len-ARR_SIZE+ offX, len-ARR_SIZE+ offX, len+ offX},
        			new int[] {0 + offY, -ARR_SIZE+ offY, ARR_SIZE+ offY, 0+ offY}, 4);
        
        g2d.setPaint(old);
        g2d.setTransform(atOld);
	}
	
	 /**
     * Metoda rysuje strzałkę skierowaną w dół a potem w prawo (wykorzystuje drawArrow)
     * @param g2d Graphics2D - obiekt grafiki
     * @param x1 int - x1
     * @param y1 int - y1
     * @param x2 int - x2 (grot)
     * @param y2 int - y2 (grot)
     * @param width int - szerokość
     * @param color Color - kolor
     */
	private void drawL_shapeArrow(Graphics2D g2d, int x1, int y1, int x2, int y2, int width, Color color) {
        double dx = x2 - x1, dy = y2 - y1;
        double angle = Math.atan2(dy, dx);
        int len = (int) Math.sqrt(dx*dx + dy*dy);
        AffineTransform atOld = g2d.getTransform();
        AffineTransform at = AffineTransform.getTranslateInstance(x1, y1);
        at.concatenate(AffineTransform.getRotateInstance(angle));
        g2d.transform(at);
        
        Color old = g2d.getColor();
        g2d.setPaint(color);
        g2d.setStroke(new BasicStroke(width));
        g2d.drawLine(0, 0, len, 0);
        
        g2d.setPaint(old);
        g2d.setTransform(atOld);
        
        drawArrow(g2d, x2, y2, x2+20, y2, width, color);
	}
	
	/**
	 * Metoda rysuje tekst obrócony o pewien kąt.
	 * @param g2d Graphics2D - obiekt rysujący
	 * @param x int - współrzędna x pierwszej litery
	 * @param y int - współrzędna y pierwszej litery
	 * @param angle int - kąt (zamiana na radiany)
	 * @param text String - tekst do wpisania
	 * @param bold boolean - true, jeśli pogrubiona
	 */
	public void drawRotatedText(Graphics2D g2d, double x, double y, int angle, String text, boolean bold) 
	{    
		int baseSize = 14;
		float zoom = getZoom();
		if(zoom < 50) {
			baseSize = 10;
			baseSize *= (100/zoom);
		} else {
			//baseSize *= (100/zoom);
		}
		
		Font oldFont = g2d.getFont();
		Color oldColor = g2d.getColor();
		if(bold)
			g2d.setFont(new Font("Tahoma", Font.BOLD, baseSize));
		else
			g2d.setFont(new Font("Tahoma", Font.PLAIN, baseSize));
		
		g2d.setColor(Color.black);
		
	    g2d.translate((float)x,(float)y);
	    g2d.rotate(Math.toRadians(angle));
	    g2d.drawString(text, 0, 0);
	    g2d.rotate(-Math.toRadians(angle));
	    g2d.translate(-(float)x,-(float)y);
	    
	    g2d.setFont(oldFont);
	    g2d.setColor(oldColor);
	}    
	
	/**
	 * Metoda wypisuje tekst na rysunku.
	 * @param g2d Graphics2D - obiekt rysujący
	 * @param x int - współrzędna x pierwszej litery
	 * @param y int - współrzędna y pierwszej litery
	 * @param text String - teks do wpisania
	 * @param color Color - kolor tekstu
	 */
	private void drawText(Graphics2D g2d, int x, int y, String text, Color color) {
        Color oldColor = g2d.getColor();
        g2d.setColor(color);
        g2d.setFont(new Font("Tahoma", Font.BOLD, 14));
        g2d.drawString(text, x, y);
        g2d.setColor(oldColor);
    }

    /**
     * Metoda rysuje wypełniony kolorem okrąg.
     * @param g2d Graphics2D - obiekt grafiki
     * @param x int - współrzędna x środka
     * @param y int - współrzędna y środka
     * @param r int - promień
     * @param color Color - kolor
     */
    private void drawCenteredCircle(Graphics2D g2d, int x, int y, int r, Color color) {
    	int xPos = x-(r/2);
    	int yPos = y-(r/2);
    	
    	Color old = g2d.getColor();
    	Stroke oldStroke = g2d.getStroke();
    	
        g2d.setPaint(color);
        
        g2d.setStroke(new BasicStroke(3));
        g2d.drawOval(xPos, yPos, r, r);
        
        xPos = x-(r/2) +5;
    	yPos = y-(r/2) +5;
    	
    	g2d.drawOval(xPos, yPos, r-10, r-10);
    	//g.fillOval(x,y,r,r);
    	
    	g2d.setStroke(oldStroke);
    	g2d.setPaint(old);
    }
    
    /**
     * Metoda rysuje kwadrat.
     * @param g2d Graphics2D - obiekt grafiki
     * @param x int - współrzędna x środka
     * @param y int - współrzędna y środka
     * @param r int - promień
     * @param color Color - kolor
     */
    private void drawCenteredSquare(Graphics2D g2d, int x, int y, int r, Color color) {
    	int xPos = x-(r/2);
    	int yPos = y-(r/2);
    	
    	Color old = g2d.getColor();
    	Stroke oldStroke = g2d.getStroke();
    	
        g2d.setPaint(color);
        
        g2d.setStroke(new BasicStroke(3));
        g2d.drawRect(xPos, yPos, r, r);
        //g2d.drawOval(xPos, yPos, r, r);
        
        xPos = x-(r/2) +5;
    	yPos = y-(r/2) +5;
    	
    	g2d.drawOval(xPos-2, yPos-2, r-6, r-6);
    	//g.fillOval(x,y,r,r);
    	
    	g2d.setStroke(oldStroke);
    	g2d.setPaint(old);
    }
    
    //***********************************************************************************************************
  	//***********************************************************************************************************
  	//***********************************************************************************************************

    public Dimension getOriginSize() {
		return originSize;
	}
    
    public void setOriginSize(Dimension originSize) {
		this.originSize = originSize;
	}
    
    /**
     * Metoda zapamiętuje maksymalną szerokość panelu w miarę tworzenia mapy.
     * @param x int - stara wartość szerokości panelu
     */
    private void updateWidth(int x) {
    	if(x>maxUsedWidth) {
    		maxUsedWidth = x;
    	}
    }
    
    /**
     * Metoda zwraca poziom powiększenia panelu.
     * @return int - zoom (0, inf)
     */
    public int getZoom() {
		return zoom;
	}
    
    /**
     * Metoda ustala nowy poziom powiększenia.
     * @param zoom int - nowy poziom
     */
    public void setZoom(int zoom) {
		if (getOriginSize().width * zoom / 100 < 10)
			return;

		this.zoom = zoom;
		
		this.invalidate();
		this.repaint();
		
		/*
		Dimension hidden = getOriginSize();
		int orgHeight = (int) hidden.getHeight();
		int orgWidth = (int) hidden.getWidth();
		int h = orgHeight;
		h = (int) (h * (double)zoom / (double)100);
		int w = orgWidth;
		w = (int) (w * (double)zoom / (double)100);
		this.setSize(w, h);
		
		*/
	}
    
    
    
    public class MapNodeInfo {
    	public Point locationXY;
    	public int transLoc;
    	public int frequency1;
    	
    	MapNodeInfo() {}
    }
    
    /**
	 * Wewnątrzna klasa odpowiedzialna za obługę rolki myszy.
	 * @author MR
	 *
	 */
	public class MouseMapWheelHandler implements MouseWheelListener {
		/**
		 * Metoda odpowiedzialna za działanie rozpoczęte przez przesuwanie rolki
		 * myszy nad arkusze. W zależności czy wciśniętych jest klawisz CTRL czy
		 * też SHIFT czy też żaden klawisz - działania są różne.
		 * @param e MouseWheelEvent - obiekt klasy przekazywany w efekcie użycia wałka myszy
		 */
		public void mouseWheelMoved(MouseWheelEvent e) {
			if (e.isControlDown()) { //zoom
				setZoom(getZoom() - 10 * e.getWheelRotation());
			} 
		}
	}
	
	private class MouseMapHandler extends MouseAdapter {
		/**
		 * Metoda aktywowana w momencie puszczenia przycisku myszy.
		 */
		public void mouseReleased(MouseEvent e) {
			//setSelectingRect(null);
			e.getComponent().repaint();
		}

		/**
		 * Metoda aktywowana przez podwójne kliknięcie przycisku myszy.
		 * @param e MouseEvent - obiekt przekazywany w efekcie podwójnego kliknięcia
		 */
		public void mouseClicked(MouseEvent e) {
			if (e.getClickCount() == 2) {
				if (e.getButton() == MouseEvent.BUTTON1 && e.isShiftDown() == false)
					; //getSelectionManager().doubleClickReactionHandler();
				if (e.getButton() == MouseEvent.BUTTON1 && e.isShiftDown() == true)
					; //getSelectionManager().decreaseTokensNumber();
			}
		}

		public void mousePressed(MouseEvent e) {
			//reset trybu przesuwania napisu:
			GUIManager.getDefaultGUIManager().setNameLocationChangeMode(null, null, false);
			
			Point mousePt = e.getPoint();
			mousePt.setLocation(e.getPoint().getX() * 100 / zoom, e.getPoint().getY() * 100 / zoom);
			
			
			//ElementLocation el = getSelectionManager().getPossiblySelectedElementLocation(mousePt);
			//Arc a = getSelectionManager().getPossiblySelectedArc(mousePt);
			// nie kliknięto ani w Node ani w Arc
			
			e.getComponent().repaint();
		}	
	} //end class MouseHandler
}
