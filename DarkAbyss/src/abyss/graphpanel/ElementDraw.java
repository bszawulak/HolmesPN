package abyss.graphpanel;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;

import abyss.math.ElementLocation;
import abyss.math.Node;
import abyss.math.Place;
import abyss.math.TimeTransition;
import abyss.math.Transition;

public final class ElementDraw {
	/**
	 * Prywatny konstruktor. To powinno załatwić problem obiektów.
	 */
	private ElementDraw() {

	}
	
	public static Graphics2D drawElement(Node node, Graphics2D g, int sheetId) {
		if(node instanceof Transition || node instanceof TimeTransition) {
			Transition trans = (Transition)node;
			for (ElementLocation el : trans.getNodeLocations(sheetId)) {
				Rectangle nodeBounds = new Rectangle(
					el.getPosition().x - trans.getRadius(), el.getPosition().y - trans.getRadius(),
						trans.getRadius() * 2, trans.getRadius() * 2);
				if (!trans.isLaunching()) { //jeśli nieaktywna
					if (trans.isGlowed_MTC()) { //jeśli ma się świecić jako MCT
						g.setColor(EditorResources.glowMTCTransitonColorLevel1);
						g.setStroke(EditorResources.glowStrokeLevel1);
						g.drawRect(nodeBounds.x, nodeBounds.y, nodeBounds.width, nodeBounds.height);

						g.setColor(EditorResources.glowMTCTransitonColorLevel2);
						g.setStroke(EditorResources.glowStrokeLevel2);
						g.drawRect(nodeBounds.x, nodeBounds.y, nodeBounds.width, nodeBounds.height);

						g.setColor(EditorResources.glowMTCTransitonColorLevel3);
						g.setStroke(EditorResources.glowStrokeLevel3);
						g.drawRect(nodeBounds.x, nodeBounds.y, nodeBounds.width, nodeBounds.height);
					} else if (trans.isGlowed()) {
						g.setColor(EditorResources.glowTransitonColorLevel1);
						g.setStroke(EditorResources.glowStrokeLevel1);
						g.drawRect(nodeBounds.x, nodeBounds.y, nodeBounds.width, nodeBounds.height);

						g.setColor(EditorResources.glowTransitonColorLevel2);
						g.setStroke(EditorResources.glowStrokeLevel2);
						g.drawRect(nodeBounds.x, nodeBounds.y, nodeBounds.width, nodeBounds.height);

						g.setColor(EditorResources.glowTransitonColorLevel3);
						g.setStroke(EditorResources.glowStrokeLevel3);
						g.drawRect(nodeBounds.x, nodeBounds.y, nodeBounds.width, nodeBounds.height);
					} else if (el.isSelected() && !el.isPortalSelected()) {
						g.setColor(EditorResources.selectionColorLevel1);
						g.setStroke(EditorResources.glowStrokeLevel1);
						g.drawRect(nodeBounds.x, nodeBounds.y, nodeBounds.width, nodeBounds.height);

						g.setColor(EditorResources.selectionColorLevel2);
						g.setStroke(EditorResources.glowStrokeLevel2);
						g.drawRect(nodeBounds.x, nodeBounds.y, nodeBounds.width, nodeBounds.height);

						g.setColor(EditorResources.selectionColorLevel3);
						g.setStroke(EditorResources.glowStrokeLevel3);
						g.drawRect(nodeBounds.x, nodeBounds.y, nodeBounds.width, nodeBounds.height);
						
						try {
							BufferedImage img = ImageIO.read(ElementDraw.class.getResource("/icons/selectedSign.png"));
							g.drawImage(img, null, 
									nodeBounds.x-(trans.getRadius()+2), 
									nodeBounds.y-(trans.getRadius()+2));
						} catch (Exception e) {
							
						}
					} else if (el.isPortalSelected()) {
						/*
						g.setColor(EditorResources.glowPortalColorLevel1);
						g.setStroke(EditorResources.glowStrokeLevel1);
						g.drawRect(nodeBounds.x, nodeBounds.y, nodeBounds.width, nodeBounds.height);

						g.setColor(EditorResources.glowPortalColorLevel2);
						g.setStroke(EditorResources.glowStrokeLevel2);
						g.drawRect(nodeBounds.x, nodeBounds.y, nodeBounds.width, nodeBounds.height);

						g.setColor(EditorResources.glowPortalColorLevel3);
						g.setStroke(EditorResources.glowStrokeLevel3);
						g.drawRect(nodeBounds.x, nodeBounds.y, nodeBounds.width, nodeBounds.height);	
						*/
					}
				}
				if (trans.isLaunching()) {
					g.setColor(EditorResources.launchColorLevel1);
					g.setStroke(EditorResources.glowStrokeLevel1);
					g.drawRect(nodeBounds.x, nodeBounds.y, nodeBounds.width, nodeBounds.height);

					g.setColor(EditorResources.launchColorLevel2);
					g.setStroke(EditorResources.glowStrokeLevel2);
					g.drawRect(nodeBounds.x, nodeBounds.y, nodeBounds.width, nodeBounds.height);

					g.setColor(EditorResources.launchColorLevel3);
					g.setStroke(EditorResources.glowStrokeLevel3);
					g.drawRect(nodeBounds.x, nodeBounds.y, nodeBounds.width, nodeBounds.height);
				}
				
				if (trans.isGlowed()) { //inwarianty
					g.setColor(EditorResources.glowTransitonColorLevel3);
				}
				else if(trans.isGlowed_MTC()) { //mct
					g.setColor(EditorResources.glowMTCTransitonColorLevel3);
				}
				else if(trans.isGlowed_Cluster()) { //klaster
					g.setColor(trans.getColor_Cluster());
				} else if(el.isPortalSelected()) { //inny ELoc portalu:
					g.setColor(EditorResources.selectionColorLevel3);
				}
				else {
					g.setColor(new Color(224,224,224));
					if(node instanceof TimeTransition) {
						g.setColor(Color.gray);
					}
				}
				g.fillRect(nodeBounds.x, nodeBounds.y, nodeBounds.width, nodeBounds.height);
				
				g.setColor(Color.DARK_GRAY);
				g.setStroke(new BasicStroke(1.5F));
				g.drawRect(nodeBounds.x, nodeBounds.y, nodeBounds.width, nodeBounds.height);
				if (trans.isPortal()) {
					g.drawRect(nodeBounds.x + 10, nodeBounds.y + 10, nodeBounds.width - 20, nodeBounds.height - 20);
				}
				// -------- do tego miejsca wspólne dla Transition i TimeTransition --------
				
				//TIME TRANSITION
				if(node instanceof TimeTransition) {
					g.setColor(Color.black);
					g.setFont(new Font("TimesRoman", Font.PLAIN, 7));
					String miFT = String.valueOf( ((TimeTransition)node).getMinFireTime() );
					g.drawString(miFT, nodeBounds.x+35, nodeBounds.y + 8);

					g.setColor(Color.black);
					g.setFont(new Font("TimesRoman", Font.PLAIN, 7));
					String mxFT = String.valueOf( ((TimeTransition)node).getMaxFireTime() );
					g.drawString(mxFT, nodeBounds.x +35, nodeBounds.y + 28);

					g.setColor(Color.LIGHT_GRAY);
					g.drawLine(nodeBounds.x + 10, nodeBounds.y + 9, nodeBounds.x + 20, nodeBounds.y + 9);
					g.drawLine(nodeBounds.x + 10, nodeBounds.y + 21, nodeBounds.x + 20, nodeBounds.y + 21);
					g.drawLine(nodeBounds.x + 10, nodeBounds.y + 9, nodeBounds.x + 20, nodeBounds.y + 21);
					g.drawLine(nodeBounds.x + 10, nodeBounds.y + 21, nodeBounds.x + 20, nodeBounds.y + 9);
				}
				
				g.setColor(EditorResources.glowTransitonTextColor);
				
				//WYŚWIETLANIE DANYCH O ODPALENIACH
				if (trans.isGlowed() && trans.getFiring_INV() > 0) {
					int posX = nodeBounds.x + nodeBounds.width / 2 
							- g.getFontMetrics().stringWidth(Integer.toString(trans.getFiring_INV())) / 2;
					int posY = nodeBounds.y + nodeBounds.height / 2 + 5;
					g.drawString(Integer.toString(trans.getFiring_INV()), posX, posY);
				}
				
				//WYŚWIETLANIE DANYCH ODNOŚNIE WYSTĘPOWANIA TRANZYCJI W KLASTRZE:
				if(trans.isGlowed_Cluster() && trans.getFreq_Cluster() > 0) {
					//int posX = nodeBounds.x + (nodeBounds.width / 2)
					//		- (g.getFontMetrics().stringWidth(Integer.toString(this.clNumber)) / 2);
					int posX = nodeBounds.x + nodeBounds.width
							- (g.getFontMetrics().stringWidth(Integer.toString(trans.getFreq_Cluster())) / 2);
					int posY = nodeBounds.y - 1;// + (nodeBounds.height / 2) + 5;
					Font old = g.getFont();
					Color oldC = g.getColor();
					
					g.setFont(new Font("TimesRoman", Font.BOLD, 14)); 
					g.setColor(Color.black);
					g.drawString(Integer.toString(trans.getFreq_Cluster()), posX, posY);
					
					g.setFont(old);
					g.setColor(oldC);
				}
				
			}
			
		} else if(node instanceof Place) { // MIEJSCA
			Place place = (Place)node;
			for (ElementLocation el : node.getNodeLocations(sheetId)) {
				Rectangle nodeBounds = new Rectangle(
						el.getPosition().x - place.getRadius(), el.getPosition().y - place.getRadius(), 
						place.getRadius() * 2, place.getRadius() * 2);
				if (el.isSelected() && !el.isPortalSelected()) {
					g.setColor(EditorResources.selectionColorLevel1);
					g.setStroke(EditorResources.glowStrokeLevel1);
					g.drawOval(nodeBounds.x, nodeBounds.y, nodeBounds.width, nodeBounds.height);

					g.setColor(EditorResources.selectionColorLevel2);
					g.setStroke(EditorResources.glowStrokeLevel2);
					g.drawOval(nodeBounds.x, nodeBounds.y, nodeBounds.width, nodeBounds.height);

					g.setColor(EditorResources.selectionColorLevel3);
					g.setStroke(EditorResources.glowStrokeLevel3);
					g.drawOval(nodeBounds.x, nodeBounds.y, nodeBounds.width, nodeBounds.height);
					
					try {
						BufferedImage img = ImageIO.read(ElementDraw.class.getResource("/icons/selectedSign.png"));
						g.drawImage(img, null, 
								nodeBounds.x-(place.getRadius()-4), 
								nodeBounds.y-(place.getRadius()-4));
					} catch (Exception e) {
						
					}
				} else if (el.isPortalSelected()) {
					/*
					g.setColor(EditorResources.glowPortalColorLevel1);
					g.setStroke(EditorResources.glowStrokeLevel1);
					g.drawOval(nodeBounds.x, nodeBounds.y, nodeBounds.width, nodeBounds.height);

					g.setColor(EditorResources.glowPortalColorLevel2);
					g.setStroke(EditorResources.glowStrokeLevel2);
					g.drawOval(nodeBounds.x, nodeBounds.y, nodeBounds.width, nodeBounds.height);

					g.setColor(EditorResources.glowPortalColorLevel3);
					g.setStroke(EditorResources.glowStrokeLevel3);
					g.drawOval(nodeBounds.x, nodeBounds.y, nodeBounds.width, nodeBounds.height);
					*/
				}
				//wypełnianie kolorem:
				if(el.isPortalSelected()) { //dla wszystkich innych ElLocations portalu właśnie klikniętego
					g.setColor(EditorResources.selectionColorLevel3);
				} else
					g.setColor(Color.white);
				g.fillOval(nodeBounds.x, nodeBounds.y, nodeBounds.width, nodeBounds.height);
				
				g.setColor(Color.DARK_GRAY);
				g.setStroke(new BasicStroke(1.5F));
				g.drawOval(nodeBounds.x, nodeBounds.y, nodeBounds.width, nodeBounds.height);
				if (place.getTokensNumber() > 0)
					g.drawString(
							Integer.toString(place.getTokensNumber()),
							nodeBounds.x + nodeBounds.width / 2 - g.getFontMetrics()
							.stringWidth(Integer.toString(place.getTokensNumber())) / 2, 
							nodeBounds.y + nodeBounds.height / 2 + 5);
				if (place.isPortal()) {
					g.drawOval(nodeBounds.x + 10, nodeBounds.y + 10, nodeBounds.width - 20, nodeBounds.height - 20);
				}
				/*
				if (el.isPortalSelected()) {
					try {
						BufferedImage img = ImageIO.read(ElementDraw.class.getResource("/icons/selectedSignPortal.png"));
						g.drawImage(img, null, 
								nodeBounds.x-(place.getRadius()-4), 
								nodeBounds.y-(place.getRadius()-4));
					} catch (Exception e) { }
				}
				*/
			}
		}
		
		return g;
	}
}
