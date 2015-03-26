package abyss.graphpanel;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;

import javax.swing.JPanel;

import abyss.math.MauritiusMapBT;
import abyss.math.MauritiusMapBT.BTNode;
import abyss.math.MauritiusMapBT.NodeType;

/**
 * Metoda odpowiedzialna za rysowanie map Mauritiusa.
 * @author MR
 *
 */
public class MauritiusMapPanel extends JPanel {
	private static final long serialVersionUID = 1028800481995974984L;
	
	private MauritiusMapBT mmbt;
	private ArrayList<Location> locationVector = new ArrayList<Location>();
	private int currentVerticalLevel = 0; //AKTUALNY popziom
	private int verticalMulti = 0; //ile poddrzew
	private int offsetVertical = 100;
	private int maxUsedWidth = 0;
	private boolean fullName = true; //czy pełna nazwa tranzycji ma być wyświetlona
	private int baseThickness = 0;
	
	private int sizeX = 0;
	private int sizeY = 0;
	
	/**
	 * Główny konstruktor obiektu klasy MauritiusMapPanel.
	 */
    public MauritiusMapPanel() {
    	sizeX = 800;
    	sizeY = 600;
        setPreferredSize(new Dimension(sizeX, sizeY));
    }
    
    /**
     * Metoda dodająca obiekt mapy. Po tym wystarczy odświeżyć panel aby mapa została narysowana.
     * @param mmbt MauritiusMapBT - Mauritius Map Binary Tree
     */
    public void addMMBT(MauritiusMapBT mmbt) {
    	this.mmbt = mmbt;
    }
    
    @Override
    public Dimension getPreferredSize() {
    	return new Dimension(sizeX, sizeY);
    }
    
    /**
     * Metoda ustawia opcję wyświetlania pełnej nazwy tranzycji.
     * @param full boolean - jeśli true - pełna nazwa; false - format "t_"+lokalizacja w wektorze
     */
    private void setFullName(boolean full) {
    	fullName = full;
    }
    
    /**
     * Aktualizuje szerokość w zależności od rysunku.
     * @param x int - stara wartość szerokości panelu
     */
    private void updateWidth(int x) {
    	if(x>maxUsedWidth)
    		maxUsedWidth = x;
    }
    
    /**
     * Główna metoda rysująca mapę Mauritiusa poprzez rekurencyjne przeglądanie drzewa binarnego danych o mapie.
     * @param node BTNode - aktualny węzeł drzewa
     * @param g Graphics - obiekt rysujący
     * @param x int - pozycja startowa X korzenia
     * @param y int - pozycja startowa Y korzenia
     * @param fullName boolean - jeśli true, wyświetla pełne nazwy reakcji
     */
    private void readTree(BTNode node, Graphics g, int x, int y, boolean fullName) {
    	updateWidth(x);
    	String name = node.transName;
    	int freq = node.transFrequency;
    	int currentMulti = verticalMulti;
    	
    	if(fullName == false) {
    		int loc = node.transLocation;
    		name = "t_"+loc;
    	}
    	//rysowanie okręgu i tekstów:
    	drawCenteredCircle(g, x, y, 40, Color.darkGray);
    	drawRotatedText(g, x+15, y-15, -5, name);
    	drawText(g, x-7, y+4, freq+"", Color.red); //częstość wystąpień w inwariantach DANEJ(rysowanej poziomo) ścieżki
    	
    	if(node.rightChild != null) {
    		drawArrow(g, x+20, y, x+100, y, 3, Color.gray);
    		readTree(node.rightChild, g, x+126, y, fullName); //120+6 (6=offest strzałki)
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
    		
    		drawL_shapeArrow(g, x-45, currentAltitude, x-45, lowerAltitude, 3, Color.gray);

    		readTree(node.leftChild, g, x, lowerAltitude, fullName);
    		verticalMulti++; //powrót z podwęzła, następny +1 odległość
    		currentVerticalLevel--; //powrót na starą wysokość
    		
    	}
    	
    	if(node.type == NodeType.ROOT) {
    		if(sizeY < y+((offsetVertical+10)*verticalMulti))
    			sizeY = y+((offsetVertical+10)*verticalMulti);
    		
    		if(sizeX < 200+maxUsedWidth)
    			sizeX = 700+maxUsedWidth;
    		
    		setPreferredSize(new Dimension(sizeX, sizeY));
    		verticalMulti=0;
    	}
    }
    
    @Override
    public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
    	if(mmbt != null) {
    		baseThickness = mmbt.getRoot().transFrequency;
    		//normalizeBaseThickness();
    		
			readTree(mmbt.getRoot(), g, 100, 200, fullName);
    	}
    }
    
    private int normalizeThickness(int transFrequency) {
    	double tFr = transFrequency;
    	double base = baseThickness;
    	double result = (tFr/base)*10;
    	return (int)result;
	}

	/**
     * Metoda rysuje linię skierowaną w dowolną stronę.
     * @param graphics Graphics - obiekt grafiki
     * @param x1 int - x1
     * @param y1 int - y1
     * @param x2 int - x2 (grot)
     * @param y2 int - y2 (grot)
     * @param width int - szerokość
     * @param color Color - kolor
     */
    private void drawLine(Graphics graphics, int x1, int y1, int x2, int y2, int width, Color color) {
    	 Graphics2D g = (Graphics2D) graphics.create();
         double dx = x2 - x1, dy = y2 - y1;
         double angle = Math.atan2(dy, dx);
         int len = (int) Math.sqrt(dx*dx + dy*dy);
         
         AffineTransform atOld = g.getTransform();
         AffineTransform at = AffineTransform.getTranslateInstance(x1, y1);
         at.concatenate(AffineTransform.getRotateInstance(angle));
         g.transform(at);

         Color old = g.getColor();
         g.setPaint(color);
         g.setStroke(new BasicStroke(width));
         g.drawLine(0, 0, len, 0);
         g.setPaint(old);
         g.setTransform(atOld);
    }
    
    /**
     * Metoda rysuje strzałkę skierowaną w dowolną stronę.
     * @param graphics Graphics - obiekt grafiki
     * @param x1 int - x1
     * @param y1 int - y1
     * @param x2 int - x2 (grot)
     * @param y2 int - y2 (grot)
     * @param width int - szerokość
     * @param color Color - kolor
     */
	private void drawArrow(Graphics graphics, int x1, int y1, int x2, int y2, int width, Color color) {
		Graphics2D g = (Graphics2D) graphics.create();
        int ARR_SIZE = 6+width;
        
        double dx = x2 - x1, dy = y2 - y1;
        double angle = Math.atan2(dy, dx);
        int len = (int) Math.sqrt(dx*dx + dy*dy);
        AffineTransform atOld = g.getTransform();
        AffineTransform at = AffineTransform.getTranslateInstance(x1, y1);
        at.concatenate(AffineTransform.getRotateInstance(angle));
        g.transform(at);
        
        Color old = g.getColor();
        g.setPaint(color);
        g.setStroke(new BasicStroke(width));
        g.drawLine(0, 0, len, 0);
        int offX = 6; // >0 przesunięcie do przodu
        int offY = 0; // >0 przesunięcie w dół pod kreskę
        g.fillPolygon(new int[] {len + offX, len-ARR_SIZE+ offX, len-ARR_SIZE+ offX, len+ offX},
        			new int[] {0 + offY, -ARR_SIZE+ offY, ARR_SIZE+ offY, 0+ offY}, 4);
        
        g.setPaint(old);
        g.setTransform(atOld);
	}
	
	 /**
     * Metoda rysuje strzałkę skierowaną w dół a potem w prawo (wykorzystuje drawArrow)
     * @param graphics Graphics - obiekt grafiki
     * @param x1 int - x1
     * @param y1 int - y1
     * @param x2 int - x2 (grot)
     * @param y2 int - y2 (grot)
     * @param width int - szerokość
     * @param color Color - kolor
     */
	private void drawL_shapeArrow(Graphics graphics, int x1, int y1, int x2, int y2, int width, Color color) {
		Graphics2D g = (Graphics2D) graphics.create();
        double dx = x2 - x1, dy = y2 - y1;
        double angle = Math.atan2(dy, dx);
        int len = (int) Math.sqrt(dx*dx + dy*dy);
        AffineTransform atOld = g.getTransform();
        AffineTransform at = AffineTransform.getTranslateInstance(x1, y1);
        at.concatenate(AffineTransform.getRotateInstance(angle));
        g.transform(at);
        
        Color old = g.getColor();
        g.setPaint(color);
        g.setStroke(new BasicStroke(width));
        g.drawLine(0, 0, len, 0);
        
        g.setPaint(old);
        g.setTransform(atOld);
        
        drawArrow(graphics, x2, y2, x2+20, y2, width, color);
	}
	
	public void drawRotatedText(Graphics graphics, double x, double y, int angle, String text) 
	{    
		Graphics2D g2d = (Graphics2D) graphics.create();
	    g2d.translate((float)x,(float)y);
	    g2d.rotate(Math.toRadians(angle));
	    g2d.drawString(text,0,0);
	    g2d.rotate(-Math.toRadians(angle));
	    g2d.translate(-(float)x,-(float)y);
	}    
	
	private void drawText(Graphics graphics, int x, int y, String text, Color color) {
		Graphics2D g = (Graphics2D) graphics.create();
        Color oldColor = g.getColor();
        g.setColor(color);
        g.setFont(new Font("Tahoma", Font.BOLD, 14));
        g.drawString(text, x, y);
        g.setColor(oldColor);

    }
	
	private void drawTextOld(Graphics graphics, int x1, int y1, String text) {
		Graphics2D g = (Graphics2D) graphics.create();
		
        Color oldColor = g.getColor();
        AffineTransform atOld = g.getTransform();
        
        g.setColor(Color.black);
        g.setFont(new Font("Tahoma", Font.PLAIN, 14));
        int name_width = g.getFontMetrics().stringWidth(text);
        
        int x2 = x1 + name_width;
        int y2 = y1 - name_width;
        double dx = x2 - x1, dy = y2 - y1;
        double angle = Math.atan2(dy, dx);
        int len = (int) Math.sqrt(dx*dx + dy*dy);
        
        AffineTransform at = AffineTransform.getTranslateInstance(x1, y1);
        at.concatenate(AffineTransform.getRotateInstance(angle));
        
        g.transform(at);
        g.drawString(text, 0, len);
        g.setColor(oldColor);
        g.setTransform(atOld);
    }
    
    
    /**
     * Metoda rysuje wypełniony kolorem okrąg.
     * @param graphics Graphics - obiekt grafiki
     * @param x int - współrzędna x środka
     * @param y int - współrzędna y środka
     * @param r int - promień
     * @param color Color - kolor
     */
    private void drawCenteredCircle(Graphics graphics, int x, int y, int r, Color color) {
    	Graphics2D g = (Graphics2D)graphics.create();
    	int xPos = x-(r/2);
    	int yPos = y-(r/2);
    	
    	Color old = g.getColor();
    	Stroke oldStroke = g.getStroke();
    	
        g.setPaint(color);
        
        g.setStroke(new BasicStroke(3));
        g.drawOval(xPos, yPos, r, r);
        
        xPos = x-(r/2) +5;
    	yPos = y-(r/2) +5;
    	
    	g.drawOval(xPos, yPos, r-10, r-10);
    	//g.fillOval(x,y,r,r);
    	
    	g.setStroke(oldStroke);
    	g.setPaint(old);
    }
    
    public class Location {
    	public Point locXY;
    	
    	Location() {}
    }
}
