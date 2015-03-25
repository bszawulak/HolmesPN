package abyss.graphpanel;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import javax.swing.JComponent;
import javax.swing.JPanel;

import abyss.math.MauritiusMapBT;
import abyss.math.MauritiusMapBT.BTNode;
import abyss.math.MauritiusMapBT.NodeType;

public class MauritiusMapPanel extends JPanel {
	private MauritiusMapBT mmbt;
	ArrayList<Location> locationVector = new ArrayList<Location>();
	int currentVerticalLevel = 0; //AKTUALNY popziom
	int verticalMulti = 0; //ile poddrzew
	int offsetVertical = 50;
	int maxUsedWidth = 0;
	
	int sizeX = 0;
	int sizeY = 0;
	
    public MauritiusMapPanel() {
    	sizeX = 300;
    	sizeY = 700;
        setPreferredSize(new Dimension(sizeX, sizeY));
    }
    
    public void addMMBT(MauritiusMapBT mmbt) {
    	this.mmbt = mmbt;
    }
    
    @Override
    public Dimension getPreferredSize() {
    	return new Dimension(sizeX, sizeY);
    }
    
    private void updateWidth(int x) {
    	if(x>maxUsedWidth)
    		maxUsedWidth = x;
    }
    
    private void readTree(BTNode node, Graphics g, int x, int y) {
    	updateWidth(x);
    	String name = node.transName;
    	drawCenteredCircle(g, x, y, 25, Color.darkGray);
    	int currentMulti = verticalMulti;
    	
    	drawText(g, x+15, y-15, name);
    	if(node.rightChild != null) {
    		drawArrow(g, x+14, y, x+64, y, 3, Color.gray);
    		readTree(node.rightChild, g, x+80, y);
    	}
    	
    	if(node.leftChild != null) {
    		int additionalDown = 0;
    		
    		if(currentVerticalLevel == 0) {
    			additionalDown = offsetVertical * verticalMulti;
    		} else if(currentMulti < verticalMulti) {
    			additionalDown += 50*(verticalMulti-currentMulti);
    			currentMulti++;
    		}
    		
    		currentVerticalLevel++;
    		int currentAltitude = y;
    		int lowerAltitude = y+50+additionalDown;
    		drawArrow2(g, x-25, currentAltitude, x-25, lowerAltitude, 3, Color.gray);

    		readTree(node.leftChild, g, x, lowerAltitude);
    		verticalMulti++; //powrót z podwęzła, następny +1 odległość
    		currentVerticalLevel--; //powrót na starą wysokość
    		
    	}
    	
    	if(node.type == NodeType.ROOT) {
    		if(sizeY < y+(55*verticalMulti))
    			sizeY = y+(55*verticalMulti);
    		
    		if(sizeX < 200+maxUsedWidth)
    			sizeX = 1000+maxUsedWidth;
    		
    		setPreferredSize(new Dimension(sizeX, sizeY));
    		verticalMulti=0;
    	}
    }
    
    @Override
    public void paintComponent(Graphics g) {
		super.paintComponent(g);
    	if(mmbt == null)
			return;
		
    	readTree(mmbt.getRoot(), g, 100, 200);
    	
    	
    	
		//drawArrow(g, 50, 50, 100, 50, 3, Color.gray);
		//drawCenteredCircle(g, 150, 170, 25, Color.gray);
		//drawText(g, 300, 300, "ala ma kota");
		//drawLine(g, 70, 70, 140, 70, 3, Color.red);
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
	
	private void drawArrow2(Graphics graphics, int x1, int y1, int x2, int y2, int width, Color color) {
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
        
        //int offX = 6; // >0 przesunięcie do przodu
        //int offY = 0; // >0 przesunięcie w dół pod kreskę
        //g.fillPolygon(new int[] {len + offX, len-ARR_SIZE+ offX, len-ARR_SIZE+ offX, len+ offX},
        //			new int[] {0 + offY, -ARR_SIZE+ offY, ARR_SIZE+ offY, 0+ offY}, 4);
        g.setPaint(old);
        g.setTransform(atOld);
        
        drawArrow(graphics, x2, y2, x2+9, y2, width, color);
	}
	
	private void drawText(Graphics graphics, int x1, int y1, String text) {
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
    	x = x-(r/2);
    	y = y-(r/2);
    	
    	Color old = g.getColor();
        g.setPaint(color);
    	g.fillOval(x,y,r,r);
    	g.setPaint(old);
    }
    
    public class Location {
    	public Point locXY;
    	
    	Location() {}
    }
}
