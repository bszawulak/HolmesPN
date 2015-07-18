package abyss.graphpanel;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;

import javax.imageio.ImageIO;

import abyss.darkgui.GUIManager;
import abyss.petrinet.elements.Arc;
import abyss.petrinet.elements.Arc.TypesOfArcs;
import abyss.petrinet.elements.ElementLocation;
import abyss.petrinet.elements.MetaNode;
import abyss.petrinet.elements.Node;
import abyss.petrinet.elements.PetriNetElement.PetriNetElementType;
import abyss.petrinet.elements.Place;
import abyss.petrinet.elements.Transition;
import abyss.petrinet.elements.Transition.TransitionType;
import abyss.utilities.Tools;

/**
 * Klasa pomocnicza, odpowiedzialna za operacje rysowania grafiki w panelach dla sieci.
 * @author MR
 *
 */
public final class ElementDraw {
	private static Font f_plain = new Font("TimesRoman", Font.PLAIN, 10);
	private static Font f_bold = new Font("TimesRoman", Font.BOLD, 12);
	private static boolean view3d = false;
	/**
	 * Prywatny konstruktor. To powinno załatwić problem obiektów.
	 */
	private ElementDraw() {

	}
	
	/**
	 * Główna metoda statyczna odpowiedzialna za rysowanie węzłów sieci.
	 * @param node Node - obiekt węzła
	 * @param g Graphics2D - obiekt rysujący
	 * @param sheetId int - numer arkusza
	 * @return Graphics2D - obiekt rysujący
	 */
	public static Graphics2D drawElement(Node node, Graphics2D g, int sheetId) {
		if(GUIManager.getDefaultGUIManager().getSettingsManager().getValue("editor3Dview").equals("1")) {
			view3d = true;
		} else {
			view3d = false;
		}
		
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
				
				//g.setColor(Color.BLACK);
				if(view3d) {
					g.setColor(Color.BLACK);
					g.fillRect(nodeBounds.x+2, nodeBounds.y+2, nodeBounds.width, nodeBounds.height);
					g.fillRect(nodeBounds.x+3, nodeBounds.y+3, nodeBounds.width, nodeBounds.height);
				}
				
				if( ((Transition)node).getTransType() == TransitionType.TPN) {
					g.setColor(Color.gray);
				} else {
					g.setColor(new Color(224,224,224));
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
				
				
				if(view3d) { 
					if(el.isPortalSelected()) {
						g.setColor(EditorResources.selectionColorLevel3);
					} else {
						g.setColor(Color.BLACK);
					}
					
					g.fillOval(nodeBounds.x+1, nodeBounds.y+1, nodeBounds.width+1, nodeBounds.height+1);
					g.fillOval(nodeBounds.x+2, nodeBounds.y+2, nodeBounds.width+1, nodeBounds.height+1);
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
		} else if(node instanceof MetaNode) {
			MetaNode metanode = (MetaNode)node;
			for (ElementLocation el : metanode.getNodeLocations(sheetId)) {
				int radius = metanode.getRadius();
				//radius = 30;
				Rectangle nodeBounds = new Rectangle(el.getPosition().x - radius, el.getPosition().y - radius, radius * 2, radius * 2);
				
				g.setColor(new Color(224,224,224));
				g.setColor(Color.DARK_GRAY);
				g.setStroke(new BasicStroke(1.5F));
				
				//TODO
				if(view3d) {
					
					
					g.drawRect(nodeBounds.x, nodeBounds.y, nodeBounds.width, nodeBounds.height);
					g.drawRect(nodeBounds.x+1, nodeBounds.y+1, nodeBounds.width, nodeBounds.height);
					g.drawRect(nodeBounds.x+2, nodeBounds.y+2, nodeBounds.width, nodeBounds.height);
					g.setColor(Color.LIGHT_GRAY);
					g.fillRect(nodeBounds.x, nodeBounds.y, nodeBounds.width, nodeBounds.height);
				} else {
					g.setColor(Color.LIGHT_GRAY);
					g.fillRect(nodeBounds.x, nodeBounds.y, nodeBounds.width, nodeBounds.height);
					g.setColor(Color.DARK_GRAY);
					g.drawRect(nodeBounds.x, nodeBounds.y, nodeBounds.width, nodeBounds.height);
				}
				
				
				g.setStroke(new BasicStroke(1.5F));
				g.setColor(Color.RED);
				g.drawLine(nodeBounds.x + 10, nodeBounds.y + 9, nodeBounds.x + 20, nodeBounds.y + 9);
				g.drawLine(nodeBounds.x + 10, nodeBounds.y + 21, nodeBounds.x + 20, nodeBounds.y + 21);
				g.drawLine(nodeBounds.x + 10, nodeBounds.y + 9, nodeBounds.x + 10, nodeBounds.y + 21);
				g.drawLine(nodeBounds.x + 20, nodeBounds.y + 10, nodeBounds.x + 20, nodeBounds.y + 21);
				
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
	 * @return Graphics2D - obiekt rysujący
	 */
	public static Graphics2D drawArc(Arc arc, Graphics2D g, int sheetId, int zoom) {
		if (arc.getLocationSheetId() != sheetId)
			return g;
		
		Stroke sizeStroke = g.getStroke();
		
		if(arc.getStartLocation().getParentNode().getType() == PetriNetElementType.META || 
				arc.getEndLocation().getParentNode().getType() == PetriNetElementType.META ) {
			@SuppressWarnings("unused")
			int x=1;
		}
		
		Point p1 = new Point((Point)arc.getStartLocation().getPosition());
		Point p2 = new Point();
		int endRadius = 0;
		if (arc.getEndLocation() == null) {
			p2 = (Point)arc.getTempEndPoint();
		} else {
			p2 = (Point)arc.getEndLocation().getPosition().clone();
			endRadius = arc.getEndLocation().getParentNode().getRadius();// * zoom / 100;
		}
		
		int distX = Tools.absolute(p1.x - p2.x);
		int distY = Tools.absolute(p1.y - p2.y);
		
		if(distX == distY) {
			p1.setLocation(p1.x+1, p1.y);
		}
		
		double alfa = p2.x - p1.x + p2.y - p1.y == 0 ? 0 : Math.atan(((double) p2.y - (double) p1.y) / ((double) p2.x - (double) p1.x));
		double alfaCos = Math.cos(alfa);
		double alfaSin = Math.sin(alfa);
		double sign = p2.x < arc.getStartLocation().getPosition().x ? 1 : -1;
		double M = 4;
		double xp = p2.x + endRadius * alfaCos * sign;
		double yp = p2.y + endRadius * alfaSin * sign;
		// double xs = p1.x + startRadius * alfaCos * sign * -1;
		// double ys = p1.y + startRadius * alfaSin * sign * -1;
		double xl = p2.x + (endRadius + 10) * alfaCos * sign + M * alfaSin;
		double yl = p2.y + (endRadius + 10) * alfaSin * sign - M * alfaCos;
		double xk = p2.x + (endRadius + 10) * alfaCos * sign - M * alfaSin;
		double yk = p2.y + (endRadius + 10) * alfaSin * sign + M * alfaCos;
		if (arc.getSelected()) {
			g.setColor(EditorResources.selectionColorLevel3);
			g.setStroke(EditorResources.glowStrokeArc);
			g.drawLine(p1.x, p1.y, p2.x, p2.y);
			g.drawPolygon(new int[] { (int) xp, (int) xl, (int) xk },
					new int[] { (int) yp, (int) yl, (int) yk }, 3);
		}
		// this.starNodeEdgeIntersection = new Point((int) xs, (int) ys);
		// this.endNodeEdgeIntersection = new Point((int) xp, (int) yp);
		g.setStroke(new BasicStroke(1.0f));
		if (arc.getIsCorect())
			g.setColor(Color.darkGray);
		else
			g.setColor(new Color(176, 23, 31));

		int leftRight = 0; //im wieksze, tym bardziej w prawo
		int upDown = 0; //im większa, tym mocniej w dół

		
		g.setStroke(sizeStroke);
		if(arc.getArcType() == TypesOfArcs.NORMAL || arc.getArcType() == TypesOfArcs.READARC || 
				arc.getArcType() == TypesOfArcs.META_ARC) {
			g.fillPolygon(new int[] { (int) xp+leftRight, (int) xl+leftRight, (int) xk+leftRight }, 
					new int[] { (int) yp+upDown, (int) yl+upDown, (int) yk+upDown }, 3);
		} else if (arc.getArcType() == TypesOfArcs.INHIBITOR) {
			//g.fillOval((int)xp-4, (int)yp, 8, 8);
			int xPos = (int) ((xl + xk)/2);
	    	int yPos = (int) ((yl + yk)/2);
	    	int xT = (int) ((xPos - xp)/3.14);
	    	int yT = (int) ((yPos - yp)/3.14);
	    	
	    	g.drawOval((int)(xPos-5-xT), (int)(yPos-5-yT), 10, 10);
		} else if (arc.getArcType() == TypesOfArcs.RESET) {
			g.fillPolygon(new int[] { (int) xp+leftRight, (int) xl+leftRight, (int) xk+leftRight }, 
					new int[] { (int) yp+upDown, (int) yl+upDown, (int) yk+upDown }, 3);
			
			//int xPos = (int) ((xl + xk)/2);
	    	//int yPos = (int) ((yl + yk)/2);
			xl = p2.x + (endRadius + 30) * alfaCos * sign + M * alfaSin;
			yl = p2.y + (endRadius + 30) * alfaSin * sign - M * alfaCos;
			xk = p2.x + (endRadius + 30) * alfaCos * sign - M * alfaSin;
			yk = p2.y + (endRadius + 30) * alfaSin * sign + M * alfaCos;
			double newxp = p2.x - (endRadius-45) * alfaCos * sign;
			double newyp = p2.y - (endRadius-45) * alfaSin * sign;
			
			g.fillPolygon(new int[] { (int) newxp, (int) xl+leftRight, (int) xk+leftRight }, 
					new int[] { (int) newyp, (int) yl+upDown, (int) yk+upDown }, 3);
		} else if (arc.getArcType() == TypesOfArcs.EQUAL) {
			int xPos = (int) ((xl + xk)/2);
	    	int yPos = (int) ((yl + yk)/2);
	    	int xT = (int) ((xPos - xp)/3.14);
	    	int yT = (int) ((yPos - yp)/3.14);
	    	
	    	g.fillOval((int)(xPos-4-xT), (int)(yPos-4-yT), 8, 8);
	    	
	    	xT = (int) ((xPos - xp));
	    	yT = (int) ((yPos - yp));
	    	
	    	g.fillOval((int)(xPos-4+xT), (int)(yPos-4+yT), 8, 8);
		}
			
		if (arc.getPairedArc() == null || arc.isMainArcOfPair()) { 
			//czyli nie rysuje kreski tylko wtedy, jeśli to podrzędny łuk w ramach read-arc - żeby nie dublować
			g.drawLine(p1.x, p1.y, (int) xp, (int) yp);
		}
		
		int x_weight = (p2.x + p1.x) / 2;
		int y_weight = (p2.y + p1.y) / 2;
		
		//double atang = Math.atan2(p2.y-p1.y,p2.x-p1.x)*180.0/Math.PI;
		double atang = Math.toDegrees(Math.atan2(p2.y-p1.y,p2.x-p1.x));
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
		
		if (arc.getWeight() > 1) {
			g.setFont(new Font("Tahoma", Font.PLAIN, 18));
			g.drawString(Integer.toString(arc.getWeight()), x_weight, y_weight + 10);
		}
		return g;
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
