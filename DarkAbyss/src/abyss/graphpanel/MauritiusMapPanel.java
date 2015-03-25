package abyss.graphpanel;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

import javax.swing.JComponent;
import javax.swing.JPanel;

import abyss.math.MauritiusMapBT;

public class MauritiusMapPanel extends JPanel {
	private MauritiusMapBT mmbt;
	
    public MauritiusMapPanel() {
        setPreferredSize(new Dimension(1600, 1200));
    }
    
    public void addMMBT(MauritiusMapBT mmbt) {
    	this.mmbt = mmbt;
    }
    
    private Graphics readTree(MauritiusMapBT mmbt) {
    	
    	return null;
    }
    
    @Override
    public void paintComponent(Graphics g) {
		super.paintComponent(g);
    	if(mmbt == null)
			return;
		
        Graphics2D g2 = (Graphics2D)g;
        //g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth();
        int h = getHeight();
        //g2.setPaint(Color.red);
        //g2.draw(new Rectangle2D.Double(w/16, h/16, w/4, h*3/8));

        g2.setPaint(Color.blue);
       // g2.draw(new Ellipse2D.Double(w/6, h*2/3, w/4, h/4));
        
        g2.drawRect(10, 10, 50, 50);
		g2.fillRoundRect(500, 500, 100, 100, 80, 80);

		
		drawArrow(g, 50, 50, 200,200);
    }
    
    void drawArrow(Graphics g1, int x1, int y1, int x2, int y2) {
        Graphics2D g = (Graphics2D) g1.create();
        
        int ARR_SIZE = 10;

        double dx = x2 - x1, dy = y2 - y1;
        double angle = Math.atan2(dy, dx);
        int len = (int) Math.sqrt(dx*dx + dy*dy);
        AffineTransform at = AffineTransform.getTranslateInstance(x1, y1);
        at.concatenate(AffineTransform.getRotateInstance(angle));
        g.transform(at);

        // Draw horizontal arrow starting in (0, 0)
        g.setStroke(new BasicStroke(3));
        g.drawLine(0, 0, len, 0);
        g.fillPolygon(new int[] {len, len-ARR_SIZE, len-ARR_SIZE, len},
                      new int[] {0, -ARR_SIZE, ARR_SIZE, 0}, 4);
    }
}
