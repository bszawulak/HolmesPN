package abyss.graphpanel;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;

import javax.imageio.ImageIO;

import abyss.math.ElementLocation;
import abyss.math.Node;
import abyss.math.Place;
import abyss.math.Transition;
import abyss.math.Transition.TransitionType;

/**
 * Klasa pomocnicza, odpowiedzialna za operacje rysowania grafiki w panelach dla sieci.
 * @author MR
 *
 */
public final class ElementDraw {
	private static Font f_plain = new Font("TimesRoman", Font.PLAIN, 10);
	private static Font f_bold = new Font("TimesRoman", Font.BOLD, 12);
	
	/**
	 * Prywatny konstruktor. To powinno załatwić problem obiektów.
	 */
	private ElementDraw() {

	}
	
	public static Graphics2D drawElement(Node node, Graphics2D g, int sheetId) {
		//if(node instanceof Transition || node instanceof TimeTransition) {
		//TODO:
		if(node instanceof Transition) {
			Transition trans = (Transition)node;
			for (ElementLocation el : trans.getNodeLocations(sheetId)) {
				int radius = trans.getRadius();
				//radius = 30;
				Rectangle nodeBounds = new Rectangle(el.getPosition().x - radius, el.getPosition().y - radius, 
						radius * 2, radius * 2);

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
						} catch (Exception e) { }
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
				else if(trans.isColorChanged()) { //klaster lub inny powód
					g.setColor(trans.getTransitionNewColor());
					
				} else if(el.isPortalSelected()) { //inny ELoc portalu:
					g.setColor(EditorResources.selectionColorLevel3);
				}
				else {
					g.setColor(new Color(224,224,224));
					//if(node instanceof TimeTransition) {
					if( ((Transition)node).getTransType() == TransitionType.TPN) {
						g.setColor(Color.gray);
					}
				}
				g.fillRect(nodeBounds.x, nodeBounds.y, nodeBounds.width, nodeBounds.height);
				
				g.setColor(Color.DARK_GRAY);
				g.setStroke(new BasicStroke(1.5F));
				g.drawRect(nodeBounds.x, nodeBounds.y, nodeBounds.width, nodeBounds.height);
				
				
				if (trans.isPortal()) {
					if( trans.getTransType() == TransitionType.TPN) {
						g.drawRect(nodeBounds.x + 5, nodeBounds.y + 5, nodeBounds.width - 10, nodeBounds.height - 10);
					} else {
						g.drawRect(nodeBounds.x + 10, nodeBounds.y + 10, nodeBounds.width - 20, nodeBounds.height - 20);
					}
				}
				// -------- do tego miejsca wspólne dla Transition i TimeTransition --------
				
				//TIME TRANSITION
				if( trans.getTransType() == TransitionType.TPN) {
					g.setColor(Color.black);
					g.setFont(f_plain);
					String eft = String.valueOf( trans.getMinFireTime() );
					g.drawString(eft, nodeBounds.x+35, nodeBounds.y + 8);

					String lft = String.valueOf( trans.getMaxFireTime() );
					g.drawString(lft, nodeBounds.x +35, nodeBounds.y + 28);

					int intTimer = (int) trans.getInternalTPN_Timer();
					int intFireTime = (int) trans.getInternalFireTime();
					String timeInfo = ""+intTimer+"  /  "+intFireTime;
					
					if(!trans.isActive())
						timeInfo = "# / #";
					
					int offset = -9;
					if(timeInfo.length() < 7)
						offset = 4;
					else if(timeInfo.length() < 9)
						offset = 1;
					else if(timeInfo.length() < 10)
						offset = -3;
					else if(timeInfo.length() < 11)
						offset = -5;
					else if(timeInfo.length() < 12)
						offset = -7;
					
					g.drawString(timeInfo, nodeBounds.x + offset, nodeBounds.y + -4);
					
					if(trans.getDPNstatus()) {
						String dur = String.valueOf( trans.getDurationTime() );
						if(trans.getInternalDPN_Timer() >= 0) {
							dur = String.valueOf( trans.getInternalDPN_Timer() ) + " / "+dur;
						} else {
							dur = " # / "+dur;
						}
						dur = "("+dur+")";
						offset = -9;
						if(dur.length() < 7)
							offset = 2;
						else if(dur.length() < 9)
							offset = -1;
						else if(dur.length() < 10)
							offset = -3;
						else if(dur.length() < 11)
							offset = -7;
						else if(dur.length() < 12)
							offset = -10;
						else
							offset = -13;
						
						g.setFont(f_bold);
						g.setColor(Color.red);
						g.drawString(dur, nodeBounds.x +offset, nodeBounds.y + -15);
						g.setColor(Color.black);
						g.setFont(f_plain);
					}

					g.setColor(Color.LIGHT_GRAY);
					g.drawLine(nodeBounds.x + 10, nodeBounds.y + 9, nodeBounds.x + 20, nodeBounds.y + 9);
					g.drawLine(nodeBounds.x + 10, nodeBounds.y + 21, nodeBounds.x + 20, nodeBounds.y + 21);
					g.drawLine(nodeBounds.x + 10, nodeBounds.y + 9, nodeBounds.x + 20, nodeBounds.y + 21);
					g.drawLine(nodeBounds.x + 10, nodeBounds.y + 21, nodeBounds.x + 20, nodeBounds.y + 9);
					
					g.setColor(Color.black);
					g.setFont(new Font("TimesRoman", Font.PLAIN, 7));
				}
				
				g.setColor(EditorResources.glowTransitonTextColor);
				
				//WYŚWIETLANIE DANYCH O ODPALENIACH
				if (trans.isGlowed() && trans.getFiring_INV() > 0) {
					int posX = nodeBounds.x + nodeBounds.width / 2 - g.getFontMetrics().stringWidth(Integer.toString(trans.getFiring_INV())) / 2;
					int posY = nodeBounds.y + nodeBounds.height / 2 + 5;
					g.drawString(Integer.toString(trans.getFiring_INV()), posX, posY);
				}
				
				//WYŚWIETLANIE DANYCH ODNOŚNIE WYSTĘPOWANIA TRANZYCJI W KLASTRZE:
				//if(trans.isColorChanged() && trans.getNumericalValueDOUBLE() > 0) {
				if(trans.isColorChanged() && trans.getNumericalValueVisibility()) {
					String clNumber = formatD(trans.getNumericalValueDOUBLE());

					int posX = nodeBounds.x + nodeBounds.width - (g.getFontMetrics().stringWidth(clNumber) / 2);
					int posY = nodeBounds.y - 1;// + (nodeBounds.height / 2) + 5;
					Font old = g.getFont();
					Color oldC = g.getColor();
					
					g.setFont(new Font("TimesRoman", Font.BOLD, 14)); 
					g.setColor(Color.black);
					g.drawString(clNumber, posX, posY);
					
					g.setFont(old);
					g.setColor(oldC);
				}
				
				if(trans.isOffline() == true) {
					try {
						BufferedImage img = ImageIO.read(ElementDraw.class.getResource("/icons/offlineTransition2.png"));
						g.drawImage(img, null, 
								nodeBounds.x-(trans.getRadius()+2), 
								nodeBounds.y-(trans.getRadius()+2));
					} catch (Exception e) { }
				}
				
				if(trans.showAddText() == true) {
					String txt = trans.returnAddText();
					
					int posX = nodeBounds.x + nodeBounds.width - (g.getFontMetrics().stringWidth(txt) / 2);
					int posY = nodeBounds.y - 1;// + (nodeBounds.height / 2) + 5;
					Font old = g.getFont();
					Color oldC = g.getColor();
					
					g.setFont(new Font("TimesRoman", Font.BOLD, 14)); 
					g.setColor(Color.black);
					g.drawString(txt, posX, posY);
					
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
				} else {
					g.setColor(Color.white);
				}
				g.fillOval(nodeBounds.x, nodeBounds.y, nodeBounds.width, nodeBounds.height);
				
				g.setColor(Color.DARK_GRAY);
				g.setStroke(new BasicStroke(1.5F));
				g.drawOval(nodeBounds.x, nodeBounds.y, nodeBounds.width, nodeBounds.height);

				drawTokens(g, place, nodeBounds);
				
				//RYSOWANIE PORTALU - OKRĄG W ŚRODKU
				g.setColor(Color.BLACK);
				g.setStroke(new BasicStroke(1.5F));
				if (place.isPortal()) {
					g.drawOval(nodeBounds.x + 8, nodeBounds.y + 8, nodeBounds.width - 16, nodeBounds.height - 16);
				}
				
				
			}
		}
		
		return g;
	}

	private static void drawTokens(Graphics2D g, Place place, Rectangle nodeBounds) {
		g.setColor(Color.black);
		g.setFont(new Font("TimesRoman", Font.BOLD, 14));
		
		if (place.getTokensNumber() == 1) {
			int x = nodeBounds.x + nodeBounds.width / 2;
			int y = nodeBounds.y + nodeBounds.height / 2;
			
			g.setColor(EditorResources.tokenDefaultColor);
			g.fillOval(x-5, y-5, 10, 10);
			g.setColor(Color.black);
			g.setStroke(EditorResources.tokenDefaultStroke);
			g.drawOval(x-5, y-5, 10, 10);	
		} else if (place.getTokensNumber() == 2) {
			int x = nodeBounds.x + nodeBounds.width / 2;
			int y = nodeBounds.y + nodeBounds.height / 2;
			
			g.setColor(EditorResources.tokenDefaultColor); //lewy
			g.fillOval(x-12, y-5, 10, 10);
			g.setColor(Color.black);
			g.setStroke(EditorResources.tokenDefaultStroke);
			g.drawOval(x-12, y-5, 10, 10);
			
			g.setColor(EditorResources.tokenDefaultColor); //prawy
			g.fillOval(x+2, y-5, 10, 10);
			g.setColor(Color.black);
			g.setStroke(EditorResources.tokenDefaultStroke);
			g.drawOval(x+2, y-5, 10, 10);
		} else if (place.getTokensNumber() == 3) {
			int x = nodeBounds.x + nodeBounds.width / 2;
			int y = nodeBounds.y + nodeBounds.height / 2;
			
			g.setColor(EditorResources.tokenDefaultColor); //środkowy górny
			g.fillOval(x-5, y-10, 10, 10);
			g.setColor(Color.black);
			g.setStroke(EditorResources.tokenDefaultStroke);
			g.drawOval(x-5, y-10, 10, 10);	
			
			g.setColor(EditorResources.tokenDefaultColor); //lewy dolny
			g.fillOval(x-12, y+1, 10, 10);
			g.setColor(Color.black);
			g.setStroke(EditorResources.tokenDefaultStroke);
			g.drawOval(x-12, y+1, 10, 10);
			
			g.setColor(EditorResources.tokenDefaultColor); //prawy dolny
			g.fillOval(x+2, y+1, 10, 10);
			g.setColor(Color.black);
			g.setStroke(EditorResources.tokenDefaultStroke);
			g.drawOval(x+2, y+1, 10, 10);
		} else if (place.getTokensNumber() == 4) {
			int x = nodeBounds.x + nodeBounds.width / 2;
			int y = nodeBounds.y + nodeBounds.height / 2;
			
			g.setColor(EditorResources.tokenDefaultColor); //lewy górny
			g.fillOval(x-12, y-11, 10, 10);
			g.setColor(Color.black);
			g.setStroke(EditorResources.tokenDefaultStroke);
			g.drawOval(x-12, y-11, 10, 10);
			
			g.setColor(EditorResources.tokenDefaultColor); //prawy górny
			g.fillOval(x+2, y-11, 10, 10);
			g.setColor(Color.black);
			g.setStroke(EditorResources.tokenDefaultStroke);
			g.drawOval(x+2, y-11, 10, 10);
			
			g.setColor(EditorResources.tokenDefaultColor); //lewy dolny
			g.fillOval(x-12, y+1, 10, 10);
			g.setColor(Color.black);
			g.setStroke(EditorResources.tokenDefaultStroke);
			g.drawOval(x-12, y+1, 10, 10);
			
			g.setColor(EditorResources.tokenDefaultColor); //prawy dolny
			g.fillOval(x+2, y+1, 10, 10);
			g.setColor(Color.black);
			g.setStroke(EditorResources.tokenDefaultStroke);
			g.drawOval(x+2, y+1, 10, 10);
		} else if (place.getTokensNumber() > 4) {
			g.drawString(
					Integer.toString(place.getTokensNumber()), 
					nodeBounds.x + nodeBounds.width / 2 - g.getFontMetrics().stringWidth(Integer.toString(place.getTokensNumber())) / 2, 
					nodeBounds.y + nodeBounds.height / 2 + 5);
		}
	}
	
	/**
	 * Metoda zmienia liczbę double na formatowany ciąg znaków.
	 * @param value double - liczba
	 * @return String - ciąg znaków
	 */
	private static String formatD(double value) {
        DecimalFormat df = new DecimalFormat("#.####");
        String txt = "";
        txt += df.format(value);
        txt = txt.replace(",", ".");
		return txt;
	}
}
