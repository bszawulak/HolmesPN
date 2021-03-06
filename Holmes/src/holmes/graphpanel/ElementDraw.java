package holmes.graphpanel;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.font.TextLayout;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import holmes.darkgui.GUIManager;
import holmes.petrinet.elements.Arc;
import holmes.petrinet.elements.ElementLocation;
import holmes.petrinet.elements.MetaNode;
import holmes.petrinet.elements.Node;
import holmes.petrinet.elements.Place;
import holmes.petrinet.elements.Transition;
import holmes.petrinet.elements.Arc.TypeOfArc;
import holmes.petrinet.elements.MetaNode.MetaType;
import holmes.petrinet.elements.Transition.TransitionType;
import holmes.utilities.Tools;

/**
 * Klasa pomocnicza, odpowiedzialna za operacje rysowania grafiki w panelach dla sieci.
 * @author MR
 *
 */
public final class ElementDraw {
	private static Font f_plain = new Font("TimesRoman", Font.PLAIN, 10);
	private static Font f_bold = new Font("TimesRoman", Font.BOLD, 12);
	
	private static Color cRed = Color.red;
	private static Color cGreen = Color.green;
	private static Color cBlue = Color.blue;
	private static Color cYellow = new Color(255,155,0) ;
	private static Color cGrey = Color.gray;
	private static Color cBlack = Color.black;

	/**
	 * Prywatny konstruktor. To powinno załatwić problem obiektów.
	 */
	private ElementDraw() {

	}
	
	private static void drawCrossHair(Graphics2D g, int x, int y, Color color){
	    g.setColor(color);
	    g.setStroke(new BasicStroke(4.0f));
	    
	    g.drawOval(x, y, 60, 60);
	    g.fillArc(x+20, y + 41 , 20, 20, -45, -90);
	    g.fillArc(x - 1, y + 20, 20, 20, -135, -90);
	    g.fillArc(x + 20, y - 1, 20, 20, -225, -90);
	    g.fillArc(x + 41, y + 20, 20, 20, -315, -90);

	    g.fillArc(x+35, y + 36 , 20, 20, 0, -90);
	    g.fillArc(x+5, y + 36 , 20, 20, -90, -90);
	    g.fillArc(x+5, y + 5 , 20, 20, -180, -90);
	    g.fillArc(x+35, y + 5 , 20, 20, -270, -90);
	}

	//TODO: znacznik tranzycji
	/**
	 * Główna metoda statyczna odpowiedzialna za rysowanie węzłów sieci.
	 * @param node Node - obiekt węzła
	 * @param g Graphics2D - obiekt rysujący
	 * @param sheetId int - numer arkusza
	 * @param eds ElementDrawSettings - opcje rysowania
	 * @return Graphics2D - obiekt rysujący
	 */
	@SuppressWarnings("unused")
	public static Graphics2D drawElement(Node node, Graphics2D g, int sheetId, ElementDrawSettings eds) {
		if(node instanceof Transition) { 
			Transition trans = (Transition)node;
			Color portalColor = new Color(224,224,224);
			Color portalSelColor = EditorResources.selectionColorLevel3;
			Color normalColor = new Color(224,224,224);
			Color tpnNormalColor = Color.GRAY;
			
			if(eds.nonDefColors) {
				normalColor = trans.defColor;
				portalColor = trans.defColor;
			}
			if(eds.snoopyMode) {
				normalColor = Color.WHITE;
				if(!eds.nonDefColors) {
					portalColor = Color.LIGHT_GRAY;
				} else {
					if(portalColor.equals(new Color(224, 224, 224))) {
						portalColor = Color.LIGHT_GRAY;
					}
					if(!trans.defColor.equals(new Color(224, 224, 224))) {
						normalColor = trans.defColor;
					}
				}
			}
			
			for (ElementLocation el : trans.getNodeLocations(sheetId)) {
				int radius = trans.getRadius();
				g.setColor(Color.WHITE);
				Rectangle nodeBounds = new Rectangle(el.getPosition().x - radius, el.getPosition().y - radius, radius * 2, radius * 2);
				
				if(eds.view3d) {
					Color backup = g.getColor();
					g.setColor(Color.DARK_GRAY);
					g.fillRect(nodeBounds.x+1, nodeBounds.y+1, nodeBounds.width, nodeBounds.height);
					g.fillRect(nodeBounds.x+2, nodeBounds.y+2, nodeBounds.width, nodeBounds.height);
					g.fillRect(nodeBounds.x+3, nodeBounds.y+3, nodeBounds.width, nodeBounds.height);
					g.setColor(Color.WHITE);
					g.fillRect(nodeBounds.x, nodeBounds.y, nodeBounds.width, nodeBounds.height);
					g.setColor(backup);
				}
				
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
					} else if (trans.isGlowed_Sub()) { //jeśli ma się świecić jako podsieć
						g.setColor(EditorResources.glowMTCTransitonColorLevel1);
						g.setStroke(EditorResources.glowStrokeLevel1);
						g.drawRect(nodeBounds.x, nodeBounds.y, nodeBounds.width, nodeBounds.height);

						g.setColor(EditorResources.glowMTCTransitonColorLevel2);
						g.setStroke(EditorResources.glowStrokeLevel2);
						g.drawRect(nodeBounds.x, nodeBounds.y, nodeBounds.width, nodeBounds.height);

						g.setColor(EditorResources.glowMTCTransitonColorLevel3);
						g.setStroke(EditorResources.glowStrokeLevel3);
						g.drawRect(nodeBounds.x, nodeBounds.y, nodeBounds.width, nodeBounds.height);
					}
					
					//NIGDY ELSE!:
					if (el.isSelected() && !el.isPortalSelected()) {
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
							//BufferedImage img = ImageIO.read(ElementDraw.class.getResource("/icons/selectedSign.png"));
							//g.drawImage(img, null, nodeBounds.x-(trans.getRadius()+2), nodeBounds.y-(trans.getRadius()+2));
							
							drawCrossHair(g, nodeBounds.x-(trans.getRadius()), nodeBounds.y-(trans.getRadius()), Color.black);
						} catch (Exception e) { }
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
					int row = 4;
					g.fillRect(nodeBounds.x-row, nodeBounds.y-row, nodeBounds.width+(2*row), nodeBounds.height+(2*row));
				} else if(trans.isGlowed_MTC()) { //mct
					g.setColor(EditorResources.glowMTCTransitonColorLevel3);
				} else if(trans.isColorChanged()) { //klaster lub inny powód
					g.setColor(trans.getTransitionNewColor());
				} else if(el.isPortalSelected()) { //inny ELoc portalu:
					g.setColor(EditorResources.selectionColorLevel3);
				} else {
					g.setColor(normalColor);
					if( ((Transition)node).getTransType() == TransitionType.TPN) {
						g.setColor(tpnNormalColor);
					}
				}
				Color back = g.getColor(); // te 4 linie: lekki trojwymiar, ladniejsza tranzycja
				g.setColor(Color.white);
				g.fillRect(nodeBounds.x, nodeBounds.y, nodeBounds.width, nodeBounds.height);
				g.setColor(back);
				
				
				
				if(trans.qSimDrawed && el.qSimDrawed) {
					int w = nodeBounds.width;
					int h = nodeBounds.height;
					if(trans.qSimFired == 0) {
						
						if(trans.qSimDrawStats) {
							try {
								//BufferedImage img = ImageIO.read(ElementDraw.class.getResource("/icons/transDead.png"));
								//g.drawImage(img, null, nodeBounds.x, nodeBounds.y+8);
								g.setColor(new Color(96,96,96));
								g.fillRect(nodeBounds.x+2, nodeBounds.y+10, 23, 10);
								g.setColor(Color.BLACK);
								g.drawRect(nodeBounds.x+2, nodeBounds.y+10, 23, 10);
								
								g.fillRect(nodeBounds.x+25, nodeBounds.y+12, 3, 6);
								g.setColor(Color.RED);
								//g.fillRect(nodeBounds.x+3, nodeBounds.y+11, 3, 8);
								g.drawLine(nodeBounds.x+15, nodeBounds.y+11, nodeBounds.x+13, nodeBounds.y+15);
								g.drawLine(nodeBounds.x+13, nodeBounds.y+15, nodeBounds.x+16, nodeBounds.y+14);
								g.drawLine(nodeBounds.x+16, nodeBounds.y+14, nodeBounds.x+14, nodeBounds.y+19);
							} catch (Exception e) { }
						}
						
						
						g.setColor(trans.qSimOvalColor);
						g.setStroke(new BasicStroke(2.5F));
						
						int os = trans.qSimOvalSize;
						g.drawOval(nodeBounds.x-os, nodeBounds.y-os, nodeBounds.width +(2*os), nodeBounds.height +(2*os));
						g.drawOval(nodeBounds.x-(os+1), nodeBounds.y-(os+1), nodeBounds.width +(2*os+2), nodeBounds.height +(2*os+2));

					} else {
						if(trans.qSimDrawStats) {
							g.setColor(trans.qSimFillColor);
							g.fillRect(nodeBounds.x, nodeBounds.y, nodeBounds.width, nodeBounds.height);
							
							g.setColor(Color.white);
							g.fillRect(nodeBounds.x, nodeBounds.y, nodeBounds.width, nodeBounds.height-(trans.qSimFillValue-2));
						}
					}
					//g.fillRect(nodeBounds.x+2, nodeBounds.y+2, nodeBounds.width-3, nodeBounds.height-3);
				} else {
					g.fillRect(nodeBounds.x+2, nodeBounds.y+2, nodeBounds.width-3, nodeBounds.height-3);
				}
				
				
				if(trans.getTransType() == TransitionType.CPNbasic) {
					g.setColor(Color.BLUE);
				} else {
					g.setColor(Color.DARK_GRAY);
				}
				g.setStroke(new BasicStroke(1.5F));
				g.drawRect(nodeBounds.x, nodeBounds.y, nodeBounds.width, nodeBounds.height);

				if (trans.isPortal()) {
					if( trans.getTransType() == TransitionType.TPN  ) {//|| trans.getTransType() == TransitionType.DPN ) {
						g.drawOval(nodeBounds.x + 4, nodeBounds.y + 4, nodeBounds.width - 8, nodeBounds.height - 8);
						g.drawOval(nodeBounds.x + 3, nodeBounds.y + 3, nodeBounds.width - 6, nodeBounds.height - 6);
					} else {
						if(eds.snoopyMode) {
							g.setColor(normalColor);
							g.fillRect(nodeBounds.x+1, nodeBounds.y+1, nodeBounds.width-2, nodeBounds.height-2);
						} else {
							g.setColor(Color.BLACK);
							g.drawOval(nodeBounds.x + 4, nodeBounds.y + 4, nodeBounds.width - 8, nodeBounds.height - 8);
							g.drawOval(nodeBounds.x + 5, nodeBounds.y + 5, nodeBounds.width - 10, nodeBounds.height - 10);
						}
					}
				}

				//RAMKA dodać wyłączenie
				/*
				if(trans.borderFrame){
					g.drawRect(nodeBounds.x+1, nodeBounds.y+1, nodeBounds.width-2, nodeBounds.height-2);
					g.drawRect(nodeBounds.x+2, nodeBounds.y+2, nodeBounds.width-4, nodeBounds.height-4);
					g.drawRect(nodeBounds.x+3, nodeBounds.y+3, nodeBounds.width-6, nodeBounds.height-6);
				}
				*/

				// -------- do tego miejsca wspólne dla Transition i TimeTransition --------
				
				//TIME TRANSITION
				if(trans.getTransType() == TransitionType.TPN) {
					int dpnTextOffset = -5;
					if(trans.getTPNstatus()) {
						dpnTextOffset = -15;
						g.setColor(Color.black);
						g.setFont(f_plain);
						String eft = String.valueOf( trans.getEFT() );
						g.drawString(eft, nodeBounds.x+35, nodeBounds.y + 8);

						String lft = String.valueOf( trans.getLFT() );
						g.drawString(lft, nodeBounds.x +35, nodeBounds.y + 28);

						int intTimer = (int) trans.getTPNtimer();
						int intFireTime = (int) trans.getTPNtimerLimit();
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
					}
					
					if(trans.getDPNstatus()) {
						String dur = String.valueOf( trans.getDPNduration() );
						if(trans.getDPNtimer() >= 0) {
							dur = String.valueOf( trans.getDPNtimer() ) + " / "+dur;
						} else {
							dur = " # / "+dur;
						}
						dur = "("+dur+")";
						int offset = -9;
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
						g.drawString(dur, nodeBounds.x +offset, nodeBounds.y + dpnTextOffset);
						g.setColor(Color.black);
						g.setFont(f_plain);
					}

					g.setColor(Color.LIGHT_GRAY);
					g.drawLine(nodeBounds.x + 8, nodeBounds.y + 7, nodeBounds.x + 22, nodeBounds.y + 7);
					g.drawLine(nodeBounds.x + 8, nodeBounds.y + 23, nodeBounds.x + 22, nodeBounds.y + 23);
					g.drawLine(nodeBounds.x + 9, nodeBounds.y + 8, nodeBounds.x + 21, nodeBounds.y + 22);
					g.drawLine(nodeBounds.x + 9, nodeBounds.y + 22, nodeBounds.x + 21, nodeBounds.y + 8);
					g.setColor(Color.black);
					g.setFont(new Font("TimesRoman", Font.PLAIN, 7));
				}

				//SYMBOL TRANZYCJI FUNKCYJNEJ
				if(trans.isFunctional()) {
					int posX = nodeBounds.x + nodeBounds.width / 2 - g.getFontMetrics().stringWidth("f") / 2 - 3;
					int posY = nodeBounds.y + nodeBounds.height / 2 + 5;
					Font old = g.getFont();
					Color oldC = g.getColor();
					g.setFont(new Font("Garamond", Font.BOLD + Font.ITALIC, 22));
					g.setColor(Color.RED);
					g.drawString("f", posX, posY);	
					g.setFont(old);
					g.setColor(oldC);
				}
				
				//SYMBOL TRANZYCJI KOLOROWANEJ
				if(trans.getTransType() == TransitionType.CPNbasic) {
					int posX = nodeBounds.x + nodeBounds.width / 2 - g.getFontMetrics().stringWidth("C") / 2 - 3;
					int posY = nodeBounds.y + nodeBounds.height / 2 + 8;
					Font old = g.getFont();
					Color oldC = g.getColor();
					g.setFont(new Font("Garamond", Font.BOLD, 22));
					g.setColor(Color.RED);
					g.drawString("C", posX, posY);	
					g.setFont(old);
					g.setColor(oldC);
				}

				//WYŚWIETLANIE DANYCH O ODPALENIACH
				if (trans.getFiring_INV() > 0) {
					int posX = nodeBounds.x + nodeBounds.width / 2 - g.getFontMetrics().stringWidth(Integer.toString(trans.getFiring_INV())) / 2;
					int posY = nodeBounds.y + nodeBounds.height / 2 + 5;
					g.setColor(Color.BLACK);
					g.drawString(Integer.toString(trans.getFiring_INV()), posX, posY);
				}
				
				//WYŚWIETLANIE DANYCH ODNOŚNIE WYSTĘPOWANIA TRANZYCJI W KLASTRZE:
				if(trans.isColorChanged() && trans.getNumericalValueVisibility()) {
					String clNumber = formatD(trans.getNumericalValueDOUBLE());

					int posX = nodeBounds.x + nodeBounds.width - (g.getFontMetrics().stringWidth(clNumber) / 2);
					int posY = nodeBounds.y - 1;// + (nodeBounds.height / 2) + 5;
					Font old = g.getFont();
					Color oldC = g.getColor();
					
					g.setFont(new Font("TimesRoman", Font.BOLD, 14));
					g.setColor(Color.black);
					g.drawString(clNumber, posX-5+trans.valueXoff, posY+trans.valueYoff);
					
					g.setFont(old);
					g.setColor(oldC);
				}
				
				if(trans.isOffline() == true) {
					try {
						BufferedImage img = ImageIO.read(ElementDraw.class.getResource("/icons/offlineTransition2.png"));
						g.drawImage(img, null, nodeBounds.x-(trans.getRadius()+2), 
								nodeBounds.y-(trans.getRadius()+2));
					} catch (Exception e) { }
				}
				
				if(trans.isInvisible() == true) {
					try {
						BufferedImage img = ImageIO.read(ElementDraw.class.getResource("/icons/invisibility.png"));
						g.drawImage(img, null, nodeBounds.x-(trans.getRadius()+2), 
								nodeBounds.y-(trans.getRadius()+2));
					} catch (Exception e) { }
				}
				
				//dodatkowy tekst nad tranzycją
				if(trans.isShowedAddText() == true) {
					String txt = trans.returnAddText();
					
					int posX = nodeBounds.x + nodeBounds.width - (g.getFontMetrics().stringWidth(txt) / 2);
					int posY = nodeBounds.y - 1;// + (nodeBounds.height / 2) + 5;
					Font old = g.getFont();
					Color oldC = g.getColor();
					
					g.setFont(new Font("TimesRoman", Font.BOLD, 14)); 
					g.setColor(Color.BLACK);
					g.drawString(txt, posX+trans.txtXoff, posY+trans.txtYoff);
					
					g.setFont(old);
					g.setColor(oldC);
				}
				
				if (el.isPortalSelected() && !el.isSelected()) {
					g.setColor(Color.BLACK);
					g.drawRect(nodeBounds.x, nodeBounds.y, nodeBounds.width, nodeBounds.height);
	
					g.setColor(EditorResources.selectionColorLevel1);
					g.setStroke(EditorResources.glowStrokeLevel1);
					g.drawRect(nodeBounds.x, nodeBounds.y, nodeBounds.width, nodeBounds.height);

					g.setColor(EditorResources.selectionColorLevel2);
					g.setStroke(EditorResources.glowStrokeLevel2);
					g.drawRect(nodeBounds.x, nodeBounds.y, nodeBounds.width, nodeBounds.height);
					drawCrossHair(g, nodeBounds.x-(trans.getRadius()), nodeBounds.y-(trans.getRadius()), Color.cyan);
					
					if(eds.snoopyMode) {
						g.setColor(portalSelColor);
						g.fillRect(nodeBounds.x+1, nodeBounds.y+1, nodeBounds.width-2, nodeBounds.height-2);
					} else {
						g.setColor(Color.BLACK);
						g.setStroke(new BasicStroke(1.5F));
						g.drawOval(nodeBounds.x + 4, nodeBounds.y + 4, nodeBounds.width - 8, nodeBounds.height - 8);
						g.drawOval(nodeBounds.x + 5, nodeBounds.y + 5, nodeBounds.width - 10, nodeBounds.height - 10);
					}
				}

				if(trans.branchColor != null){
					Color oldColor = g.getColor();
					g.setColor(trans.branchColor);
					g.drawRect(nodeBounds.x+1, nodeBounds.y+1, nodeBounds.width-1, nodeBounds.height-1);
					g.setColor(oldColor);
					trans.setColorWithNumber(true, trans.branchColor, false, 0, true, "");
				}

				if(!trans.branchBorderColors.isEmpty()){
					int x = 0;
					int y = 0;
					for (Color c: trans.branchBorderColors) {
						g.setColor(Color.black);
						g.fillOval(nodeBounds.x+40+x, nodeBounds.y+y, 6, 6);
						g.setColor(c);
						g.fillOval(nodeBounds.x+40+x, nodeBounds.y+y, 5, 5);


						y=y+6;
						if(y==42)
						{
							y=0;
							x=x+6;
						}
					}
				}

				//dubel
				if (el.isPortalSelected() && !el.isSelected()) {
					g.setColor(Color.BLACK);
					g.drawRect(nodeBounds.x, nodeBounds.y, nodeBounds.width, nodeBounds.height);

					g.setColor(EditorResources.selectionColorLevel1);
					g.setStroke(EditorResources.glowStrokeLevel1);
					g.drawRect(nodeBounds.x, nodeBounds.y, nodeBounds.width, nodeBounds.height);

					g.setColor(EditorResources.selectionColorLevel2);
					g.setStroke(EditorResources.glowStrokeLevel2);
					g.drawRect(nodeBounds.x, nodeBounds.y, nodeBounds.width, nodeBounds.height);
					drawCrossHair(g, nodeBounds.x-(trans.getRadius()), nodeBounds.y-(trans.getRadius()), Color.cyan);

					if(eds.snoopyMode) {
						g.setColor(portalSelColor);
						g.fillRect(nodeBounds.x+1, nodeBounds.y+1, nodeBounds.width-2, nodeBounds.height-2);
					} else {
						g.setColor(Color.BLACK);
						g.setStroke(new BasicStroke(1.5F));
						g.drawOval(nodeBounds.x + 4, nodeBounds.y + 4, nodeBounds.width - 8, nodeBounds.height - 8);
						g.drawOval(nodeBounds.x + 5, nodeBounds.y + 5, nodeBounds.width - 10, nodeBounds.height - 10);
					}
				}

				/*
				if (eds.color == true || trans.getTransType() == TransitionType.CPNbasic) {
					Font currentFont = g.getFont();
					Font newFont = currentFont.deriveFont(currentFont.getSize() * 1.4F);
					g.setFont(newFont);
					
					int r0 = trans.getRequiredColoredTokens(0);
					int r1 = trans.getRequiredColoredTokens(1);
					int r2 = trans.getRequiredColoredTokens(2);
					int r3 = trans.getRequiredColoredTokens(3);
					int r4 = trans.getRequiredColoredTokens(4);
					int r5 = trans.getRequiredColoredTokens(5);
					String txtT0 = "" +r0;
					String txtT1 = "" +r1;
					String txtT2 = "" +r2;
					String txtT3 = "" +r3;
					String txtT4 = "" +r4;
					String txtT5 = "" +r5;
					int txtW0 = g.getFontMetrics().stringWidth(txtT0);
					int txtW1 = g.getFontMetrics().stringWidth(txtT1);
					int txtW2 = g.getFontMetrics().stringWidth(txtT2);
					int txtW3 = g.getFontMetrics().stringWidth(txtT3);
					int txtW4 = g.getFontMetrics().stringWidth(txtT4);
					int txtW5 = g.getFontMetrics().stringWidth(txtT5);
					
					
					
					g.setColor(Color.black);
					String txt = "<";
					int posX = nodeBounds.x - (g.getFontMetrics().stringWidth(txt) / 2 ) - 15;
					int posY = nodeBounds.y - 1;
					g.drawString(txt, posX, posY);
					int lastSize = g.getFontMetrics().stringWidth(txt);
					
					if(r0 > 0) {
						posX += (txtW0 - 6*txtT0.length() + lastSize);
						g.setColor(cRed);
						g.drawString(txtT0, posX, posY);
						lastSize = txtW0;
					}
					
					if(r1 > 0) {
						posX += (txtW1 - 6*txtT1.length()  + lastSize);
						g.setColor(cGreen);
						g.drawString(txtT1, posX, posY);
						lastSize = txtW1;
					}
					
					if(r2 > 0) {
						posX += (txtW2 - 6*txtT2.length()  + lastSize);
						g.setColor(cBlue);
						g.drawString(txtT2, posX, posY);
						lastSize = txtW2;
					}
					
					if(r3 > 0) {
						posX += (txtW3 - 6*txtT3.length()  + lastSize);
						g.setColor(cYellow);
						g.drawString(txtT3, posX, posY);
						lastSize = txtW3;
					}
					
					if(r4 > 0) {
						posX += (txtW4 - 6*txtT4.length()  + lastSize);
						g.setColor(cGrey);
						g.drawString(txtT4, posX, posY);
						lastSize = txtW4;
					}
					
					if(r5 > 0) {
						posX += (txtW5 - 6*txtT5.length()  + lastSize);
						g.setColor(cBlack);
						g.drawString(txtT5, posX, posY);
						lastSize = txtW5;
					}
					
					txt = ">";
					posX += (g.getFontMetrics().stringWidth(txt) );
					g.setColor(cBlack);
					g.drawString(txt, posX, posY);
					
					g.setFont(currentFont);
				}
				*/
			}
		} else if(node instanceof Place) { // MIEJSCA  //TODO: znacznik miejsc
			Place place = (Place)node;
			Color portalColor = Color.WHITE;
			Color portalSelColor = EditorResources.selectionColorLevel3;
            Color subNetColor = EditorResources.glowMTCTransitonColorLevel3;
			Color normalColor = Color.WHITE;
			
			if(eds.nonDefColors) {
				normalColor = place.defColor;
				portalColor = place.defColor;
			}
			if(eds.snoopyMode) {
				if(!eds.nonDefColors) {
					portalColor = Color.LIGHT_GRAY;
				} else {
					if(portalColor.equals(Color.WHITE))
						portalColor = Color.LIGHT_GRAY;
				}
			}
			
			for (ElementLocation el : node.getNodeLocations(sheetId)) {
				Rectangle nodeBounds = new Rectangle(el.getPosition().x - place.getRadius(), el.getPosition().y - place.getRadius(), 
						place.getRadius() * 2, place.getRadius() * 2);
				
				if(eds.view3d) {
					Color backup = g.getColor();	
					g.setColor(Color.DARK_GRAY);
					g.fillOval(nodeBounds.x+1, nodeBounds.y+1, nodeBounds.width+1, nodeBounds.height+1);
					g.fillOval(nodeBounds.x+2, nodeBounds.y+2, nodeBounds.width+1, nodeBounds.height+1);
					g.setColor(normalColor); //kolor
					g.fillOval(nodeBounds.x, nodeBounds.y, nodeBounds.width, nodeBounds.height);
					g.setColor(backup);
				}

				if (place.isGlowed_Sub()) { //jeśli ma się świecić jako podsieć
					g.setColor(EditorResources.glowMTCTransitonColorLevel1);
					g.setStroke(EditorResources.glowStrokeLevel1);
					g.drawOval(nodeBounds.x, nodeBounds.y, nodeBounds.width, nodeBounds.height);

					g.setColor(EditorResources.glowMTCTransitonColorLevel2);
					g.setStroke(EditorResources.glowStrokeLevel2);
					g.drawOval(nodeBounds.x, nodeBounds.y, nodeBounds.width, nodeBounds.height);

					g.setColor(EditorResources.glowMTCTransitonColorLevel3);
					g.setStroke(EditorResources.glowStrokeLevel3);
					g.drawOval(nodeBounds.x, nodeBounds.y, nodeBounds.width, nodeBounds.height);
				}

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
						//BufferedImage img = ImageIO.read(ElementDraw.class.getResource("/icons/selectedSign.png"));
						//g.drawImage(img, null, nodeBounds.x-(place.getRadius()-4), nodeBounds.y-(place.getRadius()-4));
						drawCrossHair(g, nodeBounds.x-(place.getRadius()-6), nodeBounds.y-(place.getRadius()-6), Color.black);
					} catch (Exception e) {
						
					}
				} else if (el.isPortalSelected()) {
					g.setColor(EditorResources.selectionColorLevel1);
					g.setStroke(EditorResources.glowStrokeLevel1);
					g.drawOval(nodeBounds.x, nodeBounds.y, nodeBounds.width, nodeBounds.height);

					g.setColor(EditorResources.selectionColorLevel2);
					g.setStroke(EditorResources.glowStrokeLevel2);
					g.drawOval(nodeBounds.x, nodeBounds.y, nodeBounds.width, nodeBounds.height);
				}

				//wypełnianie kolorem:
				if(el.isPortalSelected()) { //dla wszystkich innych ElLocations portalu właśnie klikniętego
					g.setColor(portalSelColor);
				} else if (place.isGlowed_Sub()){
					g.setColor(subNetColor);
				} else{
					g.setColor(normalColor);
				}
				g.fillOval(nodeBounds.x, nodeBounds.y, nodeBounds.width, nodeBounds.height);
				
				if(eds.nonDefColors == false) {
					Color back = g.getColor(); // te 4 linie: lekki trojwymiar, ladniejsza
					g.setColor(new Color(249,249,249));
					g.fillOval(nodeBounds.x+3, nodeBounds.y+3, nodeBounds.width-3, nodeBounds.height-3);
					g.setColor(back);
				}

				if(place.isColored) {
					g.setColor(Color.BLUE);
				} else {
					g.setColor(Color.DARK_GRAY);
				}
				
				g.setStroke(new BasicStroke(1.5F));
				g.drawOval(nodeBounds.x, nodeBounds.y, nodeBounds.width, nodeBounds.height);

				if(eds.crazyColors) {
					g.setColor(getColor(place.getTokensNumber()));
					g.fillOval(nodeBounds.x, nodeBounds.y, nodeBounds.width, nodeBounds.height);
					//g.fillOval(nodeBounds.x, nodeBounds.y, nodeBounds.width, nodeBounds.height);
					//g.fillOval(nodeBounds.x, nodeBounds.y, nodeBounds.width, nodeBounds.height);
					g.setColor(Color.DARK_GRAY);
					g.setStroke(new BasicStroke(1.5F));
					g.drawOval(nodeBounds.x, nodeBounds.y, nodeBounds.width, nodeBounds.height);
				}
				
				if(place.isInvisible() == true) {
					try {
						BufferedImage img = ImageIO.read(ElementDraw.class.getResource("/icons/invisibility.png"));
						g.drawImage(img, null, nodeBounds.x-(place.getRadius()-4), 
								nodeBounds.y-(place.getRadius()-3));
					} catch (Exception e) { }
				}

				
				
				//RYSOWANIE PORTALU - OKRĄG W ŚRODKU
				if (place.isPortal()) {
					if(eds.snoopyMode) {
						g.setColor(portalColor);
						g.fillOval(nodeBounds.x+1, nodeBounds.y+1, nodeBounds.width-2, nodeBounds.height-2);
					} else {
						g.setColor(Color.BLACK);
						g.setStroke(new BasicStroke(1.5F));
						g.drawOval(nodeBounds.x + 6, nodeBounds.y + 6, nodeBounds.width - 12, nodeBounds.height - 12);
						g.drawOval(nodeBounds.x + 7, nodeBounds.y + 7, nodeBounds.width - 14, nodeBounds.height - 14);
					}
				}
				
				if(place.qSimDrawed && el.qSimDrawed) {
					if(place.qSimTokens == 0) {
						g.setColor(place.qSimOvalColor);
						g.setStroke(new BasicStroke(2.5F));
						int os = place.qSimOvalSize;
						g.drawOval(nodeBounds.x-os, nodeBounds.y-os, nodeBounds.width +(2*os), nodeBounds.height +(2*os));
						g.drawOval(nodeBounds.x-(os+1), nodeBounds.y-(os+1), nodeBounds.width +(2*os+2), nodeBounds.height +(2*os+2));
					} else {
						if(place.qSimDrawStats) {
							g.setStroke(new BasicStroke(1F));
							
							g.setColor(place.qSimFillColor);
							g.fillRect(nodeBounds.x+35, nodeBounds.y-25, 10, nodeBounds.height);
							
							g.setColor(Color.white);
							g.fillRect(nodeBounds.x+35, nodeBounds.y-25, 10, nodeBounds.height-(place.qSimFillValue-2));
							
							g.setColor(Color.BLACK);
							g.drawRect(nodeBounds.x+35, nodeBounds.y-25, 10, nodeBounds.height);
						}
					}
				}
				
				// KOLOROWANKI
				if(place.isColorChanged() ) {
					Color oldColor = g.getColor();
					g.setColor(place.getPlaceNewColor());
					g.fillOval(nodeBounds.x+1, nodeBounds.y+1, nodeBounds.width-1, nodeBounds.height-1);
					g.setColor(oldColor);
				}
				
				if(place.getNumericalValueVisibility()) {
					String clNumber = formatD(place.getNumericalValueDOUBLE());

					int posX = nodeBounds.x + nodeBounds.width - (g.getFontMetrics().stringWidth(clNumber) / 2);
					int posY = nodeBounds.y - 1;// + (nodeBounds.height / 2) + 5;
					Font old = g.getFont();
					Color oldC = g.getColor();
					g.setFont(new Font("TimesRoman", Font.BOLD, 14)); 
					g.setColor(Color.black);
					g.drawString(clNumber, posX-5+place.valueXoff, posY+place.valueYoff);
					g.setFont(old);
					g.setColor(oldC);
				}

				//dodatkowy tekst nad miejscem
				if(place.showAddText() == true) {
					String txt = place.returnAddText();
					int posX = nodeBounds.x + nodeBounds.width - (g.getFontMetrics().stringWidth(txt) / 2);
					int posY = nodeBounds.y - 1;// + (nodeBounds.height / 2) + 5;
					Font old = g.getFont();
					Color oldC = g.getColor();
					g.setFont(new Font("TimesRoman", Font.BOLD, 14)); 
					g.setColor(Color.BLACK);
					g.drawString(txt, posX+place.txtXoff, posY+place.txtYoff);
					g.setFont(old);
					g.setColor(oldC);
				}

				if (el.isPortalSelected() && !el.isSelected()) {
					g.setColor(normalColor);
					g.fillOval(nodeBounds.x+1, nodeBounds.y+1, nodeBounds.width-1, nodeBounds.height-1);
					
					g.setColor(EditorResources.selectionColorLevel1);
					g.setStroke(EditorResources.glowStrokeLevel1);
					g.drawOval(nodeBounds.x, nodeBounds.y, nodeBounds.width, nodeBounds.height);

					g.setColor(EditorResources.selectionColorLevel2);
					g.setStroke(EditorResources.glowStrokeLevel2);
					g.drawOval(nodeBounds.x, nodeBounds.y, nodeBounds.width, nodeBounds.height);
					
					if(eds.snoopyMode) {
						g.setColor(portalSelColor); 
						g.fillOval(nodeBounds.x+1, nodeBounds.y+1, nodeBounds.width-2, nodeBounds.height-2);
					} else {
						g.setColor(portalSelColor);
						g.fillOval(nodeBounds.x+1, nodeBounds.y+1, nodeBounds.width-2, nodeBounds.height-2);
						
						g.setColor(Color.BLACK);
						g.setStroke(new BasicStroke(1.5F));
						g.drawOval(nodeBounds.x + 6, nodeBounds.y + 6, nodeBounds.width - 12, nodeBounds.height - 12);
						g.drawOval(nodeBounds.x + 7, nodeBounds.y + 7, nodeBounds.width - 14, nodeBounds.height - 14);
						
					}
					
					drawCrossHair(g, nodeBounds.x-(place.getRadius()-6), nodeBounds.y-(place.getRadius()-6), Color.blue);
				}
				
				drawTokens(g, place, nodeBounds);

				if(place.branchColor != null){
					Color oldColor = g.getColor();
					g.setColor(place.branchColor);
					g.fillOval(nodeBounds.x+1, nodeBounds.y+1, nodeBounds.width-1, nodeBounds.height-1);
					g.setColor(oldColor);
				}

				if(!place.branchBorderColors.isEmpty()){
					int x = 0;
					int y = 0;
					for (Color c: place.branchBorderColors) {
						g.setColor(Color.black);
						g.fillOval(nodeBounds.x+40+x, nodeBounds.y+y, 6, 6);
						g.setColor(c);
						g.fillOval(nodeBounds.x+40+x, nodeBounds.y+y, 5, 5);


						y=y+6;
						if(y==42)
						{
							y=0;
							x=x+6;
						}
					}
				}

				//dubel
				if (place.isPortal()) {
					if(eds.snoopyMode) {
						g.setColor(portalColor);
						g.fillOval(nodeBounds.x+1, nodeBounds.y+1, nodeBounds.width-2, nodeBounds.height-2);
					} else {
						g.setColor(Color.BLACK);
						g.setStroke(new BasicStroke(1.5F));
						g.drawOval(nodeBounds.x + 6, nodeBounds.y + 6, nodeBounds.width - 12, nodeBounds.height - 12);
						g.drawOval(nodeBounds.x + 7, nodeBounds.y + 7, nodeBounds.width - 14, nodeBounds.height - 14);
					}
				}
				
				//TODO: COLORS
				if (eds.color == true || place.isColored) {
					Font currentFont = g.getFont();
					Font newFont = currentFont.deriveFont(currentFont.getSize() * 1.4F);
					g.setFont(newFont);
					
					String txtT0 = "" +place.getColorTokensNumber(0);
					String txtT1 = "" +place.getColorTokensNumber(1);
					String txtT2 = "" +place.getColorTokensNumber(2);
					String txtT3 = "" +place.getColorTokensNumber(3);
					String txtT4 = "" +place.getColorTokensNumber(4);
					String txtT5 = "" +place.getColorTokensNumber(5);
					int txtW0 = g.getFontMetrics().stringWidth(txtT0);
					int txtW1 = g.getFontMetrics().stringWidth(txtT1);
					int txtW2 = g.getFontMetrics().stringWidth(txtT2);
					int txtW3 = g.getFontMetrics().stringWidth(txtT3);
					int txtW4 = g.getFontMetrics().stringWidth(txtT4);
					int txtW5 = g.getFontMetrics().stringWidth(txtT5);
					
					g.setColor(Color.black);
					String txt = "[";
					int posX = nodeBounds.x - (g.getFontMetrics().stringWidth(txt) / 2 ) - 15;
					int posY = nodeBounds.y - 1;// + (nodeBounds.height / 2) + 5;
					g.drawString(txt, posX, posY);
					
					posX += (txtW0 - 8*txtT0.length() );
					g.setColor(cRed);
					g.drawString(txtT0, posX, posY);
					
					posX += (txtW1 - 10*txtT1.length()  + txtW0);
					g.setColor(cGreen);
					g.drawString(txtT1, posX, posY);
					
					posX += (txtW2 - 10*txtT2.length()  + txtW1);
					g.setColor(cBlue);
					g.drawString(txtT2, posX, posY);
					
					posX += (txtW3 - 10*txtT3.length()  + txtW2);
					g.setColor(cYellow);
					g.drawString(txtT3, posX, posY);
					
					posX += (txtW4 - 10*txtT4.length()  + txtW3);
					g.setColor(cGrey);
					g.drawString(txtT4, posX, posY);
					
					posX += (txtW5 - 10*txtT5.length()  + txtW4);
					g.setColor(cBlack);
					g.drawString(txtT5, posX, posY);
					
					txt = "]";
					posX += (g.getFontMetrics().stringWidth(txt) + txtW5 -5);
					g.setColor(cBlack);
					g.drawString(txt, posX, posY);
					
					g.setFont(currentFont);
				}
			}
		} else if(node instanceof MetaNode) { //TODO: znacznik meta
			MetaNode metanode = (MetaNode)node;
			for (ElementLocation el : metanode.getNodeLocations(sheetId)) {
				int radius = metanode.getRadius();
				//radius = 30;
				Rectangle nodeBounds = new Rectangle(el.getPosition().x - radius, el.getPosition().y - radius, radius * 2, radius * 2);
				
				g.setColor(new Color(224,224,224));
				g.setColor(Color.DARK_GRAY);
				g.setStroke(new BasicStroke(1.5F));
					
				if(true) {
					g.drawRect(nodeBounds.x, nodeBounds.y, nodeBounds.width, nodeBounds.height);
					g.drawRect(nodeBounds.x+1, nodeBounds.y+1, nodeBounds.width, nodeBounds.height);
					g.drawRect(nodeBounds.x+2, nodeBounds.y+2, nodeBounds.width, nodeBounds.height);
					g.setColor(Color.LIGHT_GRAY);
					g.fillRect(nodeBounds.x, nodeBounds.y, nodeBounds.width, nodeBounds.height);
				} 
				
				if(((MetaNode)node).getMetaType() == MetaType.SUBNETTRANS) {
					g.setStroke(new BasicStroke(2F));
					g.setColor(Color.RED);
					g.drawLine(nodeBounds.x + 7, nodeBounds.y + 7, nodeBounds.x + 23, nodeBounds.y + 7);
					g.drawLine(nodeBounds.x + 7, nodeBounds.y + 23, nodeBounds.x + 23, nodeBounds.y + 23);
					g.drawLine(nodeBounds.x + 7, nodeBounds.y + 7, nodeBounds.x + 7, nodeBounds.y + 23);
					g.drawLine(nodeBounds.x + 23, nodeBounds.y + 7, nodeBounds.x + 23, nodeBounds.y + 23);
				} else if(((MetaNode)node).getMetaType() == MetaType.SUBNETPLACE) {
					g.setStroke(new BasicStroke(2F));
					g.setColor(Color.RED);
					g.drawOval(nodeBounds.x + 6, nodeBounds.y + 6, 18, 18);
				} else if(((MetaNode)node).getMetaType() == MetaType.SUBNET) {
					g.setStroke(new BasicStroke(2F));
					g.setColor(Color.RED);
					g.drawOval(nodeBounds.x + 6, nodeBounds.y + 6, 18, 18);
					
					g.drawLine(nodeBounds.x + 10, nodeBounds.y + 10, nodeBounds.x + 20, nodeBounds.y + 10);
					g.drawLine(nodeBounds.x + 10, nodeBounds.y + 20, nodeBounds.x + 20, nodeBounds.y + 20);
					g.drawLine(nodeBounds.x + 10, nodeBounds.y + 10, nodeBounds.x + 10, nodeBounds.y + 20);
					g.drawLine(nodeBounds.x + 20, nodeBounds.y + 10, nodeBounds.x + 20, nodeBounds.y + 20);
				}
				
				g.setColor(Color.black);
				g.setFont(new Font("TimesRoman", Font.PLAIN, 7));
				g.setColor(EditorResources.glowTransitonTextColor);
				
			}
		}
		return g;
	}
	
	/**
	 * Główna metoda statyczna odpowiedzialna za rysowanie łuku sieci.
	 * @param arc Arc - obiekt łuku
	 * @param g Graphics2D - obiekt rysujący
	 * @param sheetId int - numer arkusza
	 * @param zoom int - zoom
	 * @param eds ElementDrawSettings - ustawienia rysowania
	 * @return Graphics2D - obiekt rysujący
	 */
	public static Graphics2D drawArc(Arc arc, Graphics2D g, int sheetId, int zoom, ElementDrawSettings eds) { //TODO: metoda drawArc
		if (arc.getLocationSheetId() != sheetId)
			return g;

		Stroke sizeStroke = g.getStroke();
		ArrayList<Point> breakPoints = arc.accessBreaks();
		int breaks = breakPoints.size();

		Point startP = new Point((Point)arc.getStartLocation().getPosition());
		Point endP = new Point();
		int endRadius = 0;
		if (arc.getEndLocation() == null) {
			endP = (Point)arc.getTempEndPoint();
		} else {
			endP = (Point)arc.getEndLocation().getPosition().clone();
			endRadius = arc.getEndLocation().getParentNode().getRadius();// * zoom / 100;
		}
		
		int distX = Tools.absolute(startP.x - endP.x);
		int distY = Tools.absolute(startP.y - endP.y);
		
		if(distX == distY) {
			startP.setLocation(startP.x+1, startP.y); //yes, this magic is essential
		}

		int incFactorM = 0;
		int incFactorRadius = 0;
		if(arc.qSimForcedArc) {
			incFactorM = 6;
			incFactorRadius = 15;
		}
		
		Point tmpStart = (Point)startP.clone();
		if(breaks>0) {
			tmpStart = breakPoints.get(breaks-1);
		}
		
		double alfa = endP.x - tmpStart.x + endP.y - tmpStart.y == 0 ? 0 : Math.atan(((double) endP.y - (double) tmpStart.y) / ((double) endP.x - (double) tmpStart.x));
		double alfaCos = Math.cos(alfa);
		double alfaSin = Math.sin(alfa);
		//double sign = endP.x < arc.getStartLocation().getPosition().x ? 1 : -1;
		double sign = endP.x < tmpStart.x ? 1 : -1;
		double M = 4 + incFactorM;
		double xp = endP.x + endRadius * alfaCos * sign;
		double yp = endP.y + endRadius * alfaSin * sign;
		double xl = endP.x + (endRadius + 10 + incFactorRadius) * alfaCos * sign + M * alfaSin;
		double yl = endP.y + (endRadius + 10 + incFactorRadius) * alfaSin * sign - M * alfaCos;
		double xk = endP.x + (endRadius + 10 + incFactorRadius) * alfaCos * sign - M * alfaSin;
		double yk = endP.y + (endRadius + 10 + incFactorRadius) * alfaSin * sign + M * alfaCos;
	
		if (arc.getSelected()) {
			g.setColor(EditorResources.selectionColorLevel3);
			g.setStroke(EditorResources.glowStrokeArc);
			g.drawLine(startP.x, startP.y, endP.x, endP.y);
			g.drawPolygon(new int[] { (int) xp, (int) xl, (int) xk },
					new int[] { (int) yp, (int) yl, (int) yk }, 3);
		}

        if (arc.isGlowed_Sub() && breaks == 0) {
            g.setColor(EditorResources.glowMTCTransitonColorLevel3);
            g.setStroke(EditorResources.glowStrokeArc);
            g.drawLine(startP.x, startP.y, endP.x, endP.y);
            g.drawPolygon(new int[] { (int) xp, (int) xl, (int) xk },
                    new int[] { (int) yp, (int) yl, (int) yk }, 3);
        }

		g.setStroke(new BasicStroke(1.0f));
		if (arc.getIsCorect()) {
			if(arc.getArcType() == TypeOfArc.COLOR) {
				g.setStroke(new BasicStroke(2.0f));
				g.setColor(Color.blue);
			} else
				g.setColor(Color.darkGray);
		} else
			g.setColor(new Color(176, 23, 31));

		//NIE-KLIKNIĘTY ŁUK
		if (arc.getPairedArc() == null || arc.isMainArcOfPair()) { 
			//czyli nie rysuje kreski tylko wtedy, jeśli to podrzędny łuk w ramach read-arc - żeby nie dublować
			if(arc.getArcType() == TypeOfArc.META_ARC) {
				g.setColor( new Color(30, 144, 255, 150));
				Stroke backup = g.getStroke();
				g.setStroke(new BasicStroke(4));
				g.drawLine(startP.x, startP.y, (int) xp, (int) yp);
				g.drawLine(startP.x, startP.y, (int) xp, (int) yp);
				g.drawLine(startP.x, startP.y, (int) xp, (int) yp);
				g.setStroke(backup);
			} else {
				//int sizeS = Integer.parseInt(GUIManager.getDefaultGUIManager().getSettingsManager().getValue("editorGraphArcLineSize"));
				g.setStroke(new BasicStroke(eds.arcSize));
				if(breaks > 0)
					drawBreaks(g, arc, startP, (int)xp, (int)yp, breakPoints, breaks);
				else {

					if(!arc.layers.isEmpty())
					{
						int move=0;
						for (Color color : arc.layers) {
							g.setColor(Color.black);
							Stroke backup = g.getStroke();
							g.setStroke(new BasicStroke(3));
							g.drawLine(startP.x+move, startP.y+move, (int) xp+move, (int) yp+move);
							g.setColor(color);
							g.setStroke(new BasicStroke(2));
							g.drawLine(startP.x+move, startP.y+move, (int) xp+move, (int) yp+move);
							g.drawLine(startP.x+move, startP.y+move, (int) xp+move, (int) yp+move);
							g.drawLine(startP.x+move, startP.y+move, (int) xp+move, (int) yp+move);
							g.setStroke(backup);
							move=move+3;
						}
						//arc.layers.clear();
					}else{
						g.drawLine(startP.x, startP.y, (int) xp, (int) yp);
					}
				}
			}

			///TODO ZMIENIĆ LOKALIZACJE



		} else {
			g.setStroke(new BasicStroke(eds.arcSize));
			if(breaks > 0)
				drawBreaks(g, arc, startP, (int)xp, (int)yp, breakPoints, breaks);
			else {
				if(!arc.layers.isEmpty())
				{
					int move=0;
					for (Color color : arc.layers) {
						g.setColor(Color.black);
						Stroke backup = g.getStroke();
						g.setStroke(new BasicStroke(3));
						g.drawLine(startP.x+move, startP.y+move, (int) xp+move, (int) yp+move);
						g.setColor(color);
						g.setStroke(new BasicStroke(2));
						g.drawLine(startP.x+move, startP.y+move, (int) xp+move, (int) yp+move);
						g.drawLine(startP.x+move, startP.y+move, (int) xp+move, (int) yp+move);
						g.drawLine(startP.x+move, startP.y+move, (int) xp+move, (int) yp+move);
						g.setStroke(backup);
						move=move+3;
					}
					//arc.layers.clear();
				}else{
					g.drawLine(startP.x, startP.y, (int) xp, (int) yp);
				}
			}
		}



		if(arc.isColorChanged() && breaks == 0) {
			Color oldColor = g.getColor();
			g.setColor(arc.getArcNewColor());
			g.setStroke(EditorResources.glowStrokeArc);
			g.drawLine(startP.x, startP.y, endP.x, endP.y);
			g.drawPolygon(new int[] { (int) xp, (int) xl, (int) xk },
					new int[] { (int) yp, (int) yl, (int) yk }, 3);
			g.setColor(oldColor);
		}

		
		if(arc.qSimForcedArc && breaks == 0) {
			g.setColor(arc.qSimForcedColor);
			g.setStroke(new BasicStroke(4));
			g.drawLine(startP.x, startP.y, (int) xp, (int) yp);
			
			alfa = endP.x - startP.x + endP.y - startP.y == 0 ? 0 : Math.atan(((double) endP.y - (double) startP.y) / ((double) endP.x - (double) startP.x));
			alfaCos = Math.cos(alfa);
			alfaSin = Math.sin(alfa);
			//double sign = endP.x < arc.getStartLocation().getPosition().x ? 1 : -1;
			sign = endP.x < startP.x ? 1 : -1;
			M = 4 + incFactorM;
			xp = endP.x + endRadius * alfaCos * sign;
			yp = endP.y + endRadius * alfaSin * sign;
			xl = endP.x + (endRadius + 10 + incFactorRadius) * alfaCos * sign + M * alfaSin;
			yl = endP.y + (endRadius + 10 + incFactorRadius) * alfaSin * sign - M * alfaCos;
			xk = endP.x + (endRadius + 10 + incFactorRadius) * alfaCos * sign - M * alfaSin;
			yk = endP.y + (endRadius + 10 + incFactorRadius) * alfaSin * sign + M * alfaCos;
		}

		//STRZAŁKI
		//int leftRight = 0; //im wieksze, tym bardziej w prawo
		//int upDown = 0; //im większa, tym mocniej w dół
		g.setStroke(sizeStroke);
		
		if(arc.getArcType() == TypeOfArc.NORMAL || arc.getArcType() == TypeOfArc.READARC ) {
			//g.fillPolygon(new int[] { (int) xp+leftRight, (int) xl+leftRight, (int) xk+leftRight }, 
			//		new int[] { (int) yp+upDown, (int) yl+upDown, (int) yk+upDown }, 3);

			g.fillPolygon(new int[] { (int) xp, (int) xl, (int) xk }, 
					new int[] { (int) yp, (int) yl, (int) yk }, 3);
		} else if(arc.getArcType() == TypeOfArc.COLOR ) {

			g.fillPolygon(new int[] { (int) xp, (int) xl, (int) xk }, 
					new int[] { (int) yp, (int) yl, (int) yk }, 3);
			
		} else if (arc.getArcType() == TypeOfArc.INHIBITOR) {
			int xPos = (int) ((xl + xk)/2);
	    	int yPos = (int) ((yl + yk)/2);
	    	int xT = (int) ((xPos - xp)/3.14);
	    	int yT = (int) ((yPos - yp)/3.14);
	    	
	    	if(arc.qSimForcedArc) {
				g.setColor(arc.qSimForcedColor);
				g.setStroke(new BasicStroke(4));
				g.drawOval((int)(xPos-6-xT), (int)(yPos-6-yT), 12, 12);
	    	} else {
	    		g.drawOval((int)(xPos-5-xT), (int)(yPos-5-yT), 10, 10);
	    	}
	    	
		} else if (arc.getArcType() == TypeOfArc.RESET) {
			//g.fillPolygon(new int[] { (int) xp+leftRight, (int) xl+leftRight, (int) xk+leftRight }, 
			//		new int[] { (int) yp+upDown, (int) yl+upDown, (int) yk+upDown }, 3);
			g.fillPolygon(new int[] { (int) xp, (int) xl, (int) xk }, 
					new int[] { (int) yp, (int) yl, (int) yk }, 3);
			
			xl = endP.x + (endRadius + 30) * alfaCos * sign + M * alfaSin;
			yl = endP.y + (endRadius + 30) * alfaSin * sign - M * alfaCos;
			xk = endP.x + (endRadius + 30) * alfaCos * sign - M * alfaSin;
			yk = endP.y + (endRadius + 30) * alfaSin * sign + M * alfaCos;
			double newxp = endP.x - (endRadius-45) * alfaCos * sign;
			double newyp = endP.y - (endRadius-45) * alfaSin * sign;
			
			//g.fillPolygon(new int[] { (int) newxp, (int) xl+leftRight, (int) xk+leftRight }, 
			//		new int[] { (int) newyp, (int) yl+upDown, (int) yk+upDown }, 3);
			g.fillPolygon(new int[] { (int) newxp, (int) xl, (int) xk }, 
					new int[] { (int) newyp, (int) yl, (int) yk }, 3);
		} else if (arc.getArcType() == TypeOfArc.EQUAL) {
			int xPos = (int) ((xl + xk)/2);
	    	int yPos = (int) ((yl + yk)/2);
	    	int xT = (int) ((xPos - xp)/3.14);
	    	int yT = (int) ((yPos - yp)/3.14);
	    	
	    	g.fillOval((int)(xPos-4-xT), (int)(yPos-4-yT), 8, 8);
	    	
	    	xT = (int) ((xPos - xp));
	    	yT = (int) ((yPos - yp));
	    	
	    	g.fillOval((int)(xPos-4+xT), (int)(yPos-4+yT), 8, 8);
		} else if (arc.getArcType() == TypeOfArc.META_ARC) {
			double Mmeta = 6;
			double xpmeta = endP.x + (endRadius-10) * alfaCos * sign;
			double ypmeta = endP.y + (endRadius-10) * alfaSin * sign;
			double xlmeta = endP.x + (endRadius + 13) * alfaCos * sign + Mmeta * alfaSin;
			double ylmeta = endP.y + (endRadius + 13) * alfaSin * sign - Mmeta * alfaCos;
			double xkmeta = endP.x + (endRadius + 13) * alfaCos * sign - Mmeta * alfaSin;
			double ykmeta = endP.y + (endRadius + 13) * alfaSin * sign + Mmeta * alfaCos;
			
			g.setColor( new Color(30, 144, 255, 250));
			//g.fillPolygon(new int[] { (int) xpmeta+leftRight, (int) xlmeta+leftRight, (int) xkmeta+leftRight }, 
			//		new int[] { (int) ypmeta+upDown, (int) ylmeta+upDown, (int) ykmeta+upDown }, 3);
			g.fillPolygon(new int[] { (int) xpmeta, (int) xlmeta, (int) xkmeta }, 
					new int[] { (int) ypmeta, (int) ylmeta, (int) ykmeta }, 3);
		}
		
		
		//**********************************************
		//***********    LOKALIZACJA WAGI    ***********
		//**********************************************
		String wTxt = Integer.toString(arc.getWeight());
		if(arc.getArcType() == TypeOfArc.COLOR) {
			wTxt = arc.getColorWeight(0)+","+arc.getColorWeight(1)+","+arc.getColorWeight(2)+","+
					arc.getColorWeight(3)+","+arc.getColorWeight(4)+","+arc.getColorWeight(5);
		}
		
		if (arc.getWeight() > 1 || arc.getArcType() == TypeOfArc.COLOR) {
			if(arc.accessBreaks().size() > 0) {
				Point breakP = arc.accessBreaks().get(0);
				
				g.setFont(new Font("Tahoma", Font.PLAIN, 18));
				g.drawString(wTxt, breakP.x, breakP.y - 10);
			} else {
				int x_weight = (endP.x + startP.x) / 2;
				int y_weight = (endP.y + startP.y) / 2;
				
				//double atang = Math.atan2(p2.y-p1.y,p2.x-p1.x)*180.0/Math.PI;
				double atang = Math.toDegrees(Math.atan2(endP.y-startP.y,endP.x-startP.x));
				if(atang < 0){
					atang += 360;
			    }
				if(atang == 90 || atang == 270) { //pionowo
					x_weight = x_weight + 10;
				}
				atang = atang % 90;
				if(atang < 45.0) {
					y_weight = y_weight + 5;
					x_weight = x_weight - 5;
				} else {
					y_weight = y_weight - 15;
				}

				g.setFont(new Font("Tahoma", Font.PLAIN, 18));
				
				if(arc.getArcType() == TypeOfArc.COLOR) {
					//test długości ciągu wag:
					int aW0 = arc.getColorWeight(0);
					int aW1 = arc.getColorWeight(1);
					int aW2 = arc.getColorWeight(2);
					int aW3 = arc.getColorWeight(3);
					int aW4 = arc.getColorWeight(4);
					int aW5 = arc.getColorWeight(5);
					int defract = 0;
					if(aW0 > 0)
						defract += 7;
					if(aW1 > 0)
						defract += 7;
					if(aW2 > 0)
						defract += 7;
					if(aW3 > 0)
						defract += 7;
					if(aW4 > 0)
						defract += 7;
					if(aW5 > 0)
						defract += 7;
				
					x_weight-=defract;
					y_weight+=10;
					
					if(aW0 > 0) {
						g.setColor(cRed);
						g.drawString(""+aW0, x_weight, y_weight);
						x_weight += 11;
					}
					if(aW1 > 0) {
						g.setColor(cGreen);
						g.drawString(""+aW1, x_weight, y_weight);
						x_weight += 11;
					}
					if(aW2 > 0) {
						g.setColor(cBlue);
						g.drawString(""+aW2, x_weight, y_weight);
						x_weight += 11;
					}
					if(aW3 > 0) {
						g.setColor(cYellow);
						g.drawString(""+aW3, x_weight, y_weight);
						x_weight += 11;
					}
					if(aW4 > 0) {
						g.setColor(cGrey);
						g.drawString(""+aW4, x_weight, y_weight);
						x_weight += 11;
					}
					if(aW5 > 0) {
						g.setColor(cBlack);
						g.drawString(""+aW5, x_weight, y_weight);
						x_weight += 11;
					}
				} else {
					g.drawString(wTxt, x_weight, y_weight + 10);
				}
				
				
				
			}
		}
		return g;
	}

	/**
	 * Metoda rysuje łuk łamany.
	 * @param g Graphics2D - obiekt rysujący
	 * @param arc Arc
	 * @param startP Point - punkt startowy łuku
	 * @param endPx int - współrzędna x elementu docelowego łuku
	 * @param endPy int - współrzędna y elementu docelowego łuku
	 * @param breaksVector ArrayList[Point] - wektor punktów łąmiących
	 * @param breaks int - liczba punktów łamiących
	 */
	private static void drawBreaks(Graphics2D g, Arc arc, Point startP, int endPx, int endPy, ArrayList<Point> breaksVector, int breaks) {
		if(arc.qSimForcedArc) {
			g.setColor(arc.qSimForcedColor);
			g.setStroke(new BasicStroke(4));
		}
		
		g.drawLine(startP.x, startP.y, (int) breaksVector.get(0).x, (int) breaksVector.get(0).y);

		if(arc.isColorChanged()) {
			Color oldColor = g.getColor();
			g.setColor(arc.getArcNewColor());
			g.setStroke(EditorResources.glowStrokeArc);
			g.drawLine(startP.x, startP.y, (int) breaksVector.get(0).x, (int) breaksVector.get(0).y);
			g.setColor(oldColor);
		}

		if( arc.isGlowed_Sub()) {
			Color oldColor = g.getColor();
			g.setColor(EditorResources.glowMTCTransitonColorLevel3);
			g.setStroke(EditorResources.glowStrokeArc);
			g.drawLine(startP.x, startP.y, (int) breaksVector.get(0).x, (int) breaksVector.get(0).y);
			g.setColor(oldColor);
		}

		if(!arc.layers.isEmpty()) {
			int move = 0;
			for (Color color : arc.layers) {
				g.setColor(Color.black);
				Stroke backup = g.getStroke();
				g.setStroke(new BasicStroke(3));
				g.drawLine(startP.x+move, startP.y+move, (int) breaksVector.get(0).x+move, (int) breaksVector.get(0).y+move);
				g.setColor(color);
				g.setStroke(new BasicStroke(2));
				g.drawLine(startP.x + move, startP.y + move, (int) breaksVector.get(0).x + move, (int) breaksVector.get(0).y + move);
				g.drawLine(startP.x + move, startP.y + move, (int) breaksVector.get(0).x + move, (int) breaksVector.get(0).y + move);
				g.drawLine(startP.x + move, startP.y + move, (int) breaksVector.get(0).x + move, (int) breaksVector.get(0).y + move);
				g.setStroke(backup);
				move = move + 3;
			}
		}

		for(int b=1; b<breaks; b++) {
			Point breakPoint = breaksVector.get(b-1);
			g.drawLine(breakPoint.x, breakPoint.y, breaksVector.get(b).x, breaksVector.get(b).y);
			g.fillOval((int)(breakPoint.x-3), (int)(breakPoint.y-3), 6, 6);

			if(arc.isColorChanged() ) {
				Color oldColor = g.getColor();
				g.setColor(arc.getArcNewColor());
				g.setStroke(EditorResources.glowStrokeArc);
				g.drawLine(breakPoint.x, breakPoint.y, breaksVector.get(b).x, breaksVector.get(b).y);
				g.setColor(oldColor);
			}
			if(arc.isGlowed_Sub() ) {
				Color oldColor = g.getColor();
				g.setColor(EditorResources.glowMTCTransitonColorLevel3);
				g.setStroke(EditorResources.glowStrokeArc);
				g.drawLine(breakPoint.x, breakPoint.y, breaksVector.get(b).x, breaksVector.get(b).y);
				g.setColor(oldColor);
			}

			int move=0;
			for (Color color : arc.layers) {
				g.setColor(Color.black);
				Stroke backup = g.getStroke();
				g.setStroke(new BasicStroke(3));
				g.drawLine(breakPoint.x+move, breakPoint.y+move, (int) breaksVector.get(b).x+move, (int) breaksVector.get(b).y+move);
				g.setColor(color);
				g.setStroke(new BasicStroke(2));
				g.drawLine(breakPoint.x+move, breakPoint.y+move, (int) breaksVector.get(b).x+move, (int) breaksVector.get(b).y+move);
				g.drawLine(breakPoint.x+move, breakPoint.y+move, (int) breaksVector.get(b).x+move, (int) breaksVector.get(b).y+move);
				g.drawLine(breakPoint.x+move, breakPoint.y+move, (int) breaksVector.get(b).x+move, (int) breaksVector.get(b).y+move);
				g.setStroke(backup);
				move=move+3;

			}

		}
		Point lastPoint = breaksVector.get(breaks-1);
		g.drawLine(lastPoint.x, lastPoint.y, endPx, endPy);
		g.fillOval((int)(lastPoint.x-3), (int)(lastPoint.y-3), 6, 6);
		if(arc.isColorChanged()) {
			Color oldColor = g.getColor();
			g.setColor(arc.getArcNewColor());
			g.setStroke(EditorResources.glowStrokeArc);
			g.drawLine(lastPoint.x, lastPoint.y, endPx, endPy);
			g.setColor(oldColor);
		}
		if(arc.isGlowed_Sub()) {
			Color oldColor = g.getColor();
			g.setColor(EditorResources.glowMTCTransitonColorLevel3);
			g.setStroke(EditorResources.glowStrokeArc);
			g.drawLine(lastPoint.x, lastPoint.y, endPx, endPy);
			g.setColor(oldColor);
		}

		if(!arc.layers.isEmpty()) {
			int move = 0;
			for (Color color : arc.layers) {
				g.setColor(Color.black);
				Stroke backup = g.getStroke();
				g.setStroke(new BasicStroke(3));
				g.drawLine(lastPoint.x+move, lastPoint.y+move, (int) endPx+move, (int) endPy+move);
				g.setColor(color);
				g.setStroke(new BasicStroke(2));
				g.drawLine(lastPoint.x + move, lastPoint.y + move, (int) endPx + move, (int) endPy + move);
				g.drawLine(lastPoint.x + move, lastPoint.y + move, (int) endPx + move, (int) endPy + move);
				g.drawLine(lastPoint.x + move, lastPoint.y + move, (int) endPx + move, (int) endPy + move);
				g.setStroke(backup);
				move = move + 3;
			}
		}
	}

	/**
	 * Metoda rysująca tokeny w miejscu.
	 * @param g Graphics2D - obiekt rysujący
	 * @param place Place - obiekt miejsca
	 * @param nodeBounds Rectangle - zakres rysowania
	 */
	private static void drawTokens(Graphics2D g, Place place, Rectangle nodeBounds) {
		g.setColor(Color.black);
		g.setFont(new Font("TimesRoman", Font.BOLD, 14));
		
		if(place.isColored) {
				int posX = nodeBounds.x + nodeBounds.width / 2 - g.getFontMetrics().stringWidth("C") / 2 - 3;
				int posY = nodeBounds.y + nodeBounds.height / 2 + 8;
				Font old = g.getFont();
				Color oldC = g.getColor();
				g.setFont(new Font("Garamond", Font.BOLD, 22));
				g.setColor(Color.RED);
				g.drawString("C", posX, posY);	
				g.setFont(old);
				g.setColor(oldC);
			return;
		}
		
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
	
	private static Color getColor(int tokens) {
		long steps = GUIManager.getDefaultGUIManager().simSettings.currentStep;
		
		if(steps>10) {
			if(tokens < 10)
				return Color.white;

			if(tokens > steps * 0.5) {
				return new Color(255, 0, 0);
			} else if(tokens > steps * 0.4) {
				return new Color(255, 128, 0);
			} else if(tokens > steps * 0.25) {
				return new Color(255, 255, 0);
			} else if(tokens > steps * 0.1) {
				return new Color(0, 128, 0);
			} else {
				return new Color(0, 255, 0);
			}
		}
		
		return Color.white;
	}

	/**
	 * Rysowanie tokenów w ruchu podczas symulacji.
	 * @param g Graphics2D - obiekt rysujący
	 * @param sheetId int - indeks arkusza
	 * @param arc Arc - łuk
	 */
	public static void drawToken(Graphics2D g, int sheetId, Arc arc) {
		int STEP_COUNT = GUIManager.getDefaultGUIManager().simSettings.getArcDelay();
		int step = arc.getSimulationStep();
		int weight = 0; //arc.getWeight();
		
		if (!arc.isTransportingTokens || arc.getLocationSheetId() != sheetId  || step > STEP_COUNT) {
			return;
		} else {
		}

		weight = arc.getWeight();
		ArrayList<Point> breakPoints = null;
		int breaks = 0;
		breaks = (breakPoints=arc.accessBreaks()).size(); //pro...
		Point startPos = arc.getStartLocation().getPosition();
		Point endPos = arc.getEndLocation().getPosition();
		

		double arcWidth = 0;
		double stepSize = 0;
		if(breaks > 0) { //o żesz...
			handleBrokenArc(g, STEP_COUNT, step, breakPoints, breaks, startPos, endPos, arcWidth, weight, arc);
		} else {
			arcWidth = Math.hypot(startPos.x - endPos.x, startPos.y - endPos.y); //suma po breakach, potem wybrać odcinek, a potem jego dlugość
			stepSize = arcWidth / (double) STEP_COUNT;
			double a = 0;
			double b = 0;
			if (arc.isSimulationForwardDirection()) {
				a = startPos.x - stepSize * step * (startPos.x - endPos.x) / arcWidth;
				b = startPos.y - stepSize * step * (startPos.y - endPos.y) / arcWidth;
			} else {
				a = endPos.x + stepSize * step * (startPos.x - endPos.x) / arcWidth;
				b = endPos.y + stepSize * step * (startPos.y - endPos.y) / arcWidth;
			}
			
			g.setColor(EditorResources.tokenDefaultColor);
			g.fillOval((int) a - 5, (int) b - 5, 10, 10);
			g.setColor(Color.black);
			g.setStroke(EditorResources.tokenDefaultStroke);
			g.drawOval((int) a - 5, (int) b - 5, 10, 10);
			
			
			String wTxt = Integer.toString(weight);
			if(arc.getArcType() == TypeOfArc.COLOR) {
				wTxt = arc.getColorWeight(0)+","+arc.getColorWeight(1)+","+arc.getColorWeight(2)+","+
						arc.getColorWeight(3)+","+arc.getColorWeight(4)+","+arc.getColorWeight(5);
			}
			
			Font font1 = new Font("Tahoma", Font.BOLD, 14);
			Font font2 = new Font("Tahoma", Font.BOLD, 13);
			Font font3 = new Font("Tahoma", Font.PLAIN, 12);
			
			TextLayout textLayout1 = new TextLayout(wTxt, font1, g.getFontRenderContext());
			TextLayout textLayout2 = new TextLayout(wTxt, font2, g.getFontRenderContext());
			TextLayout textLayout3 = new TextLayout(wTxt, font3, g.getFontRenderContext());
			
			g.setColor(new Color(255, 255, 255, 70));
			textLayout1.draw(g, (int) a + 10, (int) b);
			g.setColor(new Color(255, 255, 255, 150));
			textLayout2.draw(g, (int) a + 10, (int) b);
			g.setColor(Color.black);
			textLayout3.draw(g, (int) a + 10, (int) b);
		}
	}
	
	/**
	 * Rysowanie tokenów po łuku łamanym. Euklides dziękuje i idzie w cholerę, bo ma dosyć.
	 * @param g Graphics2D - obiekt rysujący
	 * @param STEP_COUNT int - max. licznik kroków (faz rysowania)
	 * @param step int - aktualny krok
	 * @param breakPoints ArrayList[Point] - lista punktów łamiących łuku
	 * @param breaks int - liczba powyższych
	 * @param startPos Point - pozycja węzła startowego
	 * @param endPos Point - pozycja węzła końcowego łuku
	 * @param arcWidth int - szerokość kreski łuku
	 * @param weight int - waga łuku
	 * @param arc Arc - obiekt łuku
	 */
	private static void handleBrokenArc(Graphics2D g, int STEP_COUNT, int step, ArrayList<Point> breakPoints,
			int breaks, Point startPos, Point endPos, double arcWidth, int weight, Arc arc) {
		double stepSize;
		double tmp = 0;
		double a = 0;
		double b = 0;
		
		ArrayList<Double> segmentLengths = new ArrayList<Double>(); //dlugości odcinków
		ArrayList<Point> allPointsVector = new ArrayList<Point>(); //odcinki składające się na łuk
		tmp = Math.hypot(startPos.x - breakPoints.get(0).x, startPos.y - breakPoints.get(0).y);
		segmentLengths.add(tmp);
		allPointsVector.add(startPos);
		arcWidth += tmp;
		allPointsVector.add(breakPoints.get(0));
		for(int br=1; br<breaks; br++) {
			tmp = Math.hypot(breakPoints.get(br-1).x - breakPoints.get(br).x, breakPoints.get(br-1).y - breakPoints.get(br).y);
			segmentLengths.add(tmp);
			allPointsVector.add(breakPoints.get(br));
			arcWidth += tmp;
		}
		tmp = Math.hypot(breakPoints.get(breaks-1).x - endPos.x, breakPoints.get(breaks-1).y - endPos.y); //suma odcinków
		segmentLengths.add(tmp);
		allPointsVector.add(endPos);
		arcWidth += tmp;
		//koniec liczenia długości łuku

		stepSize = arcWidth / (double) STEP_COUNT;
		double endPoint = stepSize * step;
		
		double counter = segmentLengths.get(0);
		for(int i=1; i<segmentLengths.size()+1; i++) { //kosmos panie, kosmos
			if(counter > endPoint) {
				double distInCurrent = counter - endPoint;
				double tylePrzeszedl = segmentLengths.get(i-1) - distInCurrent;
				double proportion = tylePrzeszedl / segmentLengths.get(i-1);
				//proportion *= distances.get(i-1); //długość do przebycia
				
				Point startingPoint = allPointsVector.get(i-1);
				Point endingPoint = allPointsVector.get(i);
				int signX = 1;
				int signY = 1;
				if(startingPoint.x > endingPoint.x)
					signX = -1;
				if(startingPoint.y > endingPoint.y)
					signY = -1;
				
				int distX = Math.abs(startingPoint.x - endingPoint.x);
				int distY = Math.abs(startingPoint.y - endingPoint.y);
				
				a = startingPoint.x + (signX * proportion * distX);
				b = startingPoint.y + (signY * proportion * distY);
				//b = endPos.y + stepSize * step * (startPos.y - endPos.y) / arcWidth;
				
				break;
			}
			if(i < segmentLengths.size())
				counter += segmentLengths.get(i);
		}
		
		g.setColor(EditorResources.tokenDefaultColor);
		g.fillOval((int) a - 5, (int) b - 5, 10, 10);
		g.setColor(Color.black);
		g.setStroke(EditorResources.tokenDefaultStroke);
		g.drawOval((int) a - 5, (int) b - 5, 10, 10);
		
		String wTxt = Integer.toString(weight);
		if(arc.getArcType() == TypeOfArc.COLOR) {
			wTxt = arc.getColorWeight(0)+","+arc.getColorWeight(1)+","+arc.getColorWeight(2)+","+
					arc.getColorWeight(3)+","+arc.getColorWeight(4)+","+arc.getColorWeight(5);
		}

		Font font1 = new Font("Tahoma", Font.BOLD, 14);
		Font font2 = new Font("Tahoma", Font.BOLD, 13);
		Font font3 = new Font("Tahoma", Font.PLAIN, 12);
		TextLayout textLayout1 = new TextLayout(wTxt, font1, g.getFontRenderContext());
		TextLayout textLayout2 = new TextLayout(wTxt, font2, g.getFontRenderContext());
		TextLayout textLayout3 = new TextLayout(wTxt, font3, g.getFontRenderContext());
		
		g.setColor(new Color(255, 255, 255, 70));
		textLayout1.draw(g, (int) a + 10, (int) b);
		g.setColor(new Color(255, 255, 255, 150));
		textLayout2.draw(g, (int) a + 10, (int) b);
		g.setColor(Color.black);
		textLayout3.draw(g, (int) a + 10, (int) b);
	}
}
