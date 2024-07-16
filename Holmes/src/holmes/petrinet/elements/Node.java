package holmes.petrinet.elements;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.Serial;
import java.util.ArrayList;

import holmes.darkgui.GUIManager;
import holmes.darkgui.settings.SettingsManager;
import holmes.graphpanel.EditorResources;
import holmes.graphpanel.ElementDrawSettings;
import holmes.utilities.Tools;

import static holmes.graphpanel.EditorResources.*;

/**
 * Klasa implementująca wierzchołek sieci Petriego. Dziedziczą po niej klasy
 * reprezentujące miejsca (Place) oraz tranzycje (Transition). Zapewnia
 * implementację wspólnych dla nich funkcjonalności, takie jak rysowanie ich na
 * odpowiednim arkuszu czy umożliwienie tworzenia portali (wielu odnośników
 * graficznych do jednego wierzchołka sieci).
 */
public abstract class Node extends PetriNetElement {
	@Serial
	private static final long serialVersionUID = -8569201372990876149L;
	private ArrayList<ElementLocation> elementLocations = new ArrayList<>();
	//lokalizacje napisów do oddzielnego przesuwania
	private ArrayList<ElementLocation> namesLocations = new ArrayList<>();
	private ArrayList<ElementLocation> alphaLocations = new ArrayList<>();
	private ArrayList<ElementLocation> betaLocations = new ArrayList<>();
	private ArrayList<ElementLocation> gammaLocations = new ArrayList<>();
	private ArrayList<ElementLocation> tauLocations = new ArrayList<>();
	private boolean isPortal = false;
	protected boolean invisible = false;
	private int radius = 20;


	public boolean qSimArcSign = false; //znacznik dła łuku - czy ma być wzmocniony pomiędzy węzłami które mają tu wartość true


	public Color branchColor = null;
	public ArrayList<Color> branchBorderColors = new ArrayList<>();
	private static final Font f_Big = new Font("TimesRoman", Font.BOLD, 14);
	private static final Font f_BigL = new Font("TimesRoman", Font.PLAIN, 14);
	private static final Font f_SmallL = new Font("TimesRoman", Font.BOLD, 12);
	//private static final Color darkGreen = new Color(0, 75, 0);
	
	/**
	 * Konstruktor obiektu klasy Node. Ustawia też początkowe wartości przesunięcia nazwy wierzchołka.
	 * @param sheetId (<b>int</b>) identyfikator arkusza.
	 * @param nodeId (<b>int</b>) identyfikator elementu sieci Petriego.
	 * @param nodePosition (<b>Point</b> punkt, w którym znajduje się lokalizacja tego wierzchołka na odpowiednim arkuszu.
	 * @param radius (<b>int</b>) promień okręgu, na którym opisana jest figura geometryczna reprezentująca obiekt w edytorze graficznym.
	 */
	public Node(int sheetId, int nodeId, Point nodePosition, int radius) {
		this.setRadius(radius);
		this.setID(nodeId);
		//lokalizacje graficzne:
		this.getNodeLocations().add(new ElementLocation(sheetId, nodePosition, this));
		//napis - przesunięcie startowe:
		this.getTextsLocations(GUIManager.locationMoveType.NAME).add(new ElementLocation(sheetId, new Point(0,0), this));
		this.getTextsLocations(GUIManager.locationMoveType.ALPHA).add(new ElementLocation(sheetId, new Point(0,0), this));
		this.getTextsLocations(GUIManager.locationMoveType.BETA).add(new ElementLocation(sheetId, new Point(0,0), this));
		this.getTextsLocations(GUIManager.locationMoveType.GAMMA).add(new ElementLocation(sheetId, new Point(0,0), this));
		this.getTextsLocations(GUIManager.locationMoveType.TAU).add(new ElementLocation(sheetId, new Point(0,0), this));
	}

	/**
	 * Konstruktor obiektu klasy Node. Jest wywoływany między innymi w czasie tworzenia portalu,
	 * tj. wtedy, kiedy jeden węzeł sieci ma 2 lub więcej lokalizacji.
	 * @param nodeId (<b>int</b>) identyfikator elementu sieci Petriego.
	 * @param elementLocations (<b>ArrayList[ElementLocation]</b>) lista lokalizacji elementu sieci Petriego.
	 * @param radius (<b>int</b>) promień okręgu, na którym opisana jest figura geometryczna reprezentująca obiekt w edytorze graficznym.
	 */
	public Node(int nodeId, ArrayList<ElementLocation> elementLocations, int radius) {
		this.setRadius(radius);
		this.setID(nodeId);
		/*
		 * Poniżej, dla wszystkich lokalizacji w elemenLocations a co za tym idzie
		 * dla wszystkich przesłanych wewnątrz tej tablicy węzłów, są one podmieniane
		 * na aktualnie tworzony konstruktorem - czyli np. jeśli przyszły tu dane 3
		 * starych tranzycji o id 1, 2, 3, są one podmieniane na właśnie konstruowany
		 * obiekt, o innym ID i innych danych - jest to ten sam portal.
		 */
		for (ElementLocation el : elementLocations)
			el.setParentNode(this);
		/*
		 * Tablica elementLocations zawiera kolekcje łuków wejściowych i wyjściowych, 
		 * tak więc powyższe podmienienie ParentNode nie wpływa na nie - zostają takie,
		 * jakie były dla starych kilku węzłów zmienianych w portal
		 */
		this.setElementLocations(elementLocations);
		if (elementLocations.size() > 1) { // oczywiście konstruktor może też tworzyć zwykły węzeł
			setPortal(true); //skoro po coś ta metoda w ogóle powstała...
		}
	}

	/**
	 * Konstruktor obiektu klasy Node. Ustawia też początkowe wartości przesunięcia nazwy wierzchołka.
	 * @param nodeId (<b>int</b>) identyfikator elementu sieci Petriego
	 * @param elementLocation (<b>ElementLocation</b>) lokalizacja elementu sieci Petriego
	 * @param radius (<b>int</b>) promień okręgu, na którym opisana jest figura geometryczna reprezentująca obiekt w edytorze graficznym
	 */
	public Node(int nodeId, ElementLocation elementLocation, int radius) {
		this.setRadius(radius);
		this.setID(nodeId);
		elementLocation.setParentNode(this);
		this.getElementLocations().add(elementLocation);
		//napis - przesunięcie startowe:
		this.getTextsLocations(GUIManager.locationMoveType.NAME).add(new ElementLocation(elementLocation.getSheetID(), new Point(0,0), this));
	}

	/**
	 * Metoda pozwala pobrać listę wszystkich punktów lokalizacji wierzchołka na arkuszu o określonym identyfikatorze.
	 * @param sheetId (<b>int</b>) identyfikator arkusza.
	 * @return (<b>ArrayList[Point]</b>) lista punktów lokalizacji wierzchołka na wybranym arkuszu.
	 */
	public ArrayList<Point> getNodePositions(int sheetId) {
		ArrayList<Point> returnPoints = new ArrayList<>();
		for (ElementLocation e : this.getNodeLocations())
			if (e.getSheetID() == sheetId)
				returnPoints.add(e.getPosition());
		return returnPoints;
	}
	
	/**
	 * Metoda pozwala pobrać listę wszystkich punktów lokalizacji nazwy wierzchołka na arkuszu o określonym identyfikatorze.
	 * @param sheetId (<b>int</b>) identyfikator arkusza
	 * @param nameType  (GUIManager.locationMoveType) - ALPHA, BETA, GAMMA, DELTA
	 * @return (<b>ArrayList[Point]</b>) - lista punktów lokalizacji nazwy wierzchołka na wybranym arkuszu
	 */
	public ArrayList<Point> getNodeNamePositions(int sheetId, GUIManager.locationMoveType nameType) {
		ArrayList<Point> returnPoints = new ArrayList<>();
		for (ElementLocation e : this.getTextsLocations(nameType))
			if (e.getSheetID() == sheetId)
				returnPoints.add(e.getPosition());
		return returnPoints;
	}

	/**
	 * Metoda pozwala pobrać listę wszystkich lokacji wierzchołka na arkuszu o określonym identyfikatorze
	 * @param sheetId (<b>int</b>) identyfikator arkusza.
	 * @return (<b>ArrayList[ElementLocation]</b>) - lista lokalizacji wierzchołka na wybranym arkuszu.
	 */
	public ArrayList<ElementLocation> getNodeLocations(int sheetId) {
		ArrayList<ElementLocation> returnPoints = new ArrayList<>();
		for (ElementLocation e : this.getElementLocations())
			if (e.getSheetID() == sheetId)
				returnPoints.add(e);
		return returnPoints;
	}

	/**
	 * Metoda pozwala pobrać ostatnią pośród dodanych dla tego wierzchołka lokację.
	 * @return (<b>ElementLocation</b>) - ostatnia dodana lokalizacja.
	 */
	public ElementLocation getLastLocation() {
		if (this.getNodeLocations().isEmpty())
			return null;
		return this.getNodeLocations().get(this.getNodeLocations().size() - 1);
	}

	/**
	 * Metoda pozwala narysować NAZWĘ wierzchołka sieci Petriego na odpowiednim arkuszu.
	 * @param g (<b>Graphics2D</b> obiekt grafiki.
	 * @param sheetId (<b>int</b>) identyfikator arkusza.
	 * @param eds (<b>ElementDrawSettings</b>) opcje rysowania.
	 */
	public void draw(Graphics2D g, int sheetId, ElementDrawSettings eds) {
		g.setColor(Color.black);
		g.setFont(new Font("Tahoma", Font.PLAIN, 11));
		int width = g.getFontMetrics().stringWidth(getName());
		for (Point p : this.getNodePositions(sheetId)) {
			g.drawString(getName(), p.x - width / 2, p.y + getRadius() + 15);
		}
	}

	/**
	 * Malowanie linii pomiędzy lokacjami węzła, jeśli jest to portal.
	 * @param g <b>Graphics2D</b>
	 * @param sheetId <b>int</b>
	 * @param eds <b>ElementDrawSettings</b>
	 */
	public void drawPortalLines(Graphics2D g, int sheetId, ElementDrawSettings eds) {
		g.setColor(EditorResources.selectionColorLevel2);
		g.setStroke(EditorResources.glowStrokeLevel2);
		if(this instanceof Place) {
			Place p = (Place)this;
			if(p.isPortal()) {
				ArrayList<ElementLocation> nodeLocations = this.getNodeLocations(sheetId);
				//iterate nodeLocation to find the selected one
				for(int i=0; i<nodeLocations.size(); i++) { //po wszystkich węzłach
					if(nodeLocations.get(i).isSelected()) { //czy jakiś jest w ogóle klinięty
						Point nodePoint = nodeLocations.get(i).getPosition(); //znajdź lokację
                        for (ElementLocation nodeLocation : nodeLocations) { //iteruj od nowa po wszystkich
                            if (nodeLocations.get(i) == nodeLocation) //z wyjątkiem klikniętego
                                continue;
                            Point nodePoint2 = nodeLocation.getPosition(); //znajdź lokację drugiego
                            //g.setColor(Color.black);
                            g.drawLine(nodePoint.x, nodePoint.y, nodePoint2.x, nodePoint2.y); //narysuj linię
                        }
						return;
					}
				}
			}
		} else if (this instanceof Transition) {
			Transition t = (Transition)this;
			ArrayList<ElementLocation> nodeLocations = this.getNodeLocations(sheetId);
			//iterate nodeLocation to find the selected one
			for(int i=0; i<nodeLocations.size(); i++) { //po wszystkich węzłach
				if(nodeLocations.get(i).isSelected()) { //czy jakiś jest w ogóle klinięty
					Point nodePoint = nodeLocations.get(i).getPosition(); //znajdź lokację
                    for (ElementLocation nodeLocation : nodeLocations) { //iteruj od nowa po wszystkich
                        if (nodeLocations.get(i) == nodeLocation) //z wyjątkiem klikniętego
                            continue;
                        Point nodePoint2 = nodeLocation.getPosition(); //znajdź lokację drugiego
                        //g.setColor(Color.black);
                        g.drawLine(nodePoint.x, nodePoint.y, nodePoint2.x, nodePoint2.y); //narysuj linię
                    }
					return;
				}
			}
		}
	}
	
	/**
	 * Metoda odpowiedzialna za rysowanie nazwy wierzchołka na obrazie sieci.
	 * @param g (<b>Graphics2D</b>) obiekt rysujący.
	 * @param sheetId (<b>int</b>) identyfikator arkusza.
	 * @param places (<b>ArrayList[Place]</b>) wektor miejsc.
	 * @param transitions (<b>ArrayList[Transition]</b>) wektor tranzycji.
	 * @param timeTransitions (<b>ArrayList[Transition]</b>) wektor tranzycji czasowych.
	 * @param metanodes (<b>ArrayList[MetaNode]</b>) wektor metawęzłów.
	 */
	@SuppressWarnings("all")
	public void drawName(Graphics2D g, int sheetId, ArrayList<Node> places, ArrayList<Node> transitions, ArrayList<Node> timeTransitions,
			 ArrayList<Node> metanodes) {
		SettingsManager sm = GUIManager.getDefaultGUIManager().getSettingsManager();

		boolean lowerIndexID = sm.getValue("editorShortNameLowerIndex").equals("1");

		String name = getName();
		String xtpnID = "";
		int add_transY = 0;
		if(sm.getValue("editorShowShortNames").equals("1")) {
			if(this instanceof Place) {
				int x = places.indexOf(this);
				if(lowerIndexID) {
					xtpnID = x+"";
					name = "p";
				} else {
					name = "p"+x;
				}
			} else if (this instanceof Transition) {
				int x = transitions.indexOf(this);
				if(lowerIndexID) {
					xtpnID = x+"";
					name = "t";
				} else {
					name = "t"+x;
				}

				/*
				if(((Transition)this).getTransType() == TransitionType.PN) {
					name = "t"+x;
				} else if(this instanceof TransitionXTPN) {
					xtpnID = x+"";
					name = "t";
				}
				 */
			} else {
				int x = metanodes.indexOf(this);
				name = "M"+x;
			}
		}
		if (this instanceof Transition) {
			add_transY = 3;
		}
		g.setColor(Color.black);
		if(sm.getValue("editorGraphFontBold").equals("0")) {
			g.setFont(new Font("Tahoma", Font.PLAIN, Integer.parseInt(sm.getValue("editorGraphFontSize"))));
		} else {
			g.setFont(new Font("Tahoma", Font.BOLD, Integer.parseInt(sm.getValue("editorGraphFontSize"))));
		}
		
		int name_width = g.getFontMetrics().stringWidth(name);

		// Node może mieć wiele EL (portal):
		ArrayList<Point> nodePoints = this.getNodePositions(sheetId);
		ArrayList<Point> namesPoints = getNodeNamePositions(sheetId, GUIManager.locationMoveType.NAME); // lokalizacja przesunięcia nazwy
		for (int i=0; i<nodePoints.size(); i++) {
			Point nodePoint = nodePoints.get(i);
			Point namePoint = namesPoints.get(i);
			
			int drawX = (nodePoint.x - name_width / 2) + namePoint.x;
			int drawY =  (nodePoint.y + getRadius() + 15) + namePoint.y;
			
			if(drawX < 0 )
				drawX = (nodePoint.x - name_width / 2); //oryginalny kod
			if(drawY < 0 )
				drawY = nodePoint.y + getRadius() + 15; //oryginalny kod

			if(lowerIndexID) {
				int size = Integer.parseInt(sm.getValue("editorGraphFontSize"));
				int offset = 0;
				if(size > 16)
					offset += 2;
				if(size > 19)
					offset += 2;

				if(this instanceof Transition) { //inaczej dla tranzycji
					g.drawString(name, drawX, drawY+add_transY);
					//g.setFont(new Font("Tahoma", Font.BOLD, size - 2));
					g.drawString(xtpnID, drawX+8+offset, drawY+add_transY+5);

				} else if(this instanceof Place) { //inaczej dla miejsc
					g.drawString(name, drawX, drawY+add_transY);
					//g.setFont(new Font("Tahoma", Font.BOLD, size - 2));
					g.drawString(xtpnID, drawX+10+offset, drawY+add_transY+5);
				}
			} else {
				g.drawString(name, drawX, drawY+add_transY);
			}
		}

		if(this instanceof Place) {
			if( this instanceof PlaceXTPN ) {
				//ArrayList<Point> gammaPoints = getNodeNamePositions(sheetId, GUIManager.locationMoveType.GAMMA); //XTPN, jw
				for (int i=0; i<gammaLocations.size(); i++) {
					if(gammaLocations.get(i).getSheetID() != sheetId) //tylko dla danego arkusza
						continue;

					Point nodePoint = nodePoints.get(i);
					Point gammaPoint = gammaLocations.get(i).getPosition();
					int drawX = (nodePoint.x) + gammaPoint.x - 50;
					int drawY =  (nodePoint.y) + gammaPoint.y - 24;

					if(((PlaceXTPN)this).isGammaModeActive()) {
						if( ((PlaceXTPN)this).isGammaRangeVisible() ) {
							int franctionDigits = ((PlaceXTPN)this).getFractionForPlaceXTPN();
							g.setColor(gammaColor);
							g.setFont(f_BigL);
							double gamma = ((PlaceXTPN)this).getGammaMaxValue();
							//String gammaMaxVal = "\u221E";
							String gammaMaxVal = "∞";
							if(gamma < Integer.MAX_VALUE-2) {
								gammaMaxVal = Tools.cutValueExt(((PlaceXTPN)this).getGammaMaxValue(), franctionDigits);
							}

							//String gammaStr = "\u03B3: [" + Tools.cutValueExt(((PlaceXTPN)this).getGammaMinValue(), franctionDigits) + ", " + gammaMaxVal+"]";
							String gammaStr = "γ: [" + Tools.cutValueExt(((PlaceXTPN)this).getGammaMinValue(), franctionDigits) + ", " + gammaMaxVal+"]";

							g.drawString(gammaStr, drawX, drawY);
						}
					} else {
						g.setColor(Color.GRAY);
						g.setFont(f_SmallL);
						g.drawString("(NORMAL PLACE)", drawX, drawY);
					}
				}
			}

		} else if (this instanceof Transition) {
			if(this instanceof TransitionXTPN ) {
				TransitionXTPN transXTPN = (TransitionXTPN)this;

				if(alphaLocations.size() + betaLocations.size() - tauLocations.size() - namesLocations.size() != 0) {
					//error, impossible
					GUIManager.getDefaultGUIManager().log("Error: alpha, beta, tau and name arrays size do not match. "
							+ "Node drawName method.", "error", true);
					return;
				}
				if(transXTPN.qSimXTPN.showQSimXTPN) {
					for (int i=0; i<alphaLocations.size(); i++) { // ==
						if (namesLocations.get(i).getSheetID() != sheetId) //tylko dla danego arkusza
							continue;

						Point nodePoint = nodePoints.get(i);
						int drawX = (nodePoint.x) + transXTPN.qSimXTPN.xOff - 40;
						int drawY =  (nodePoint.y) + transXTPN.qSimXTPN.yOff;

						g.drawString(transXTPN.qSimXTPN.text1, drawX, drawY);
						g.drawString(transXTPN.qSimXTPN.text2, drawX, drawY+15);
						g.drawString(transXTPN.qSimXTPN.text3, drawX, drawY+30);
						g.drawString(transXTPN.qSimXTPN.text4, drawX, drawY+45);
					}
				} else { //jeśli nie qSIM, pokaż alfa/beta

					for (int i=0; i<alphaLocations.size(); i++) { // ==
						if(namesLocations.get(i).getSheetID() != sheetId) //tylko dla danego arkusza
							continue;

						Point nodePoint = nodePoints.get(i);
						if(transXTPN.isAlphaModeActive() && transXTPN.isAlphaRangeVisible()) {
							Point alphaPoint = alphaLocations.get(i).getPosition();

							int drawX = (nodePoint.x) + alphaPoint.x - 40;
							int drawY =  (nodePoint.y) + alphaPoint.y - 28;

							if(transXTPN.isAlphaModeActive()) {
								g.setColor(alphaColor);
								g.setFont(f_BigL);

								/*
								int moveDown = 0;
								if(transXTPN.isBetaModeActive()) { //jak nie ma bety, to alfa bliżej kwadratu tranzycji
									moveDown = -16;
								}
								String alphaStr = "\u03B1: [";
								g.drawString(alphaStr, drawX, drawY+moveDown);
								int width = g.getFontMetrics().stringWidth(alphaStr);
								g.setFont(f_BigL);
								alphaStr = Tools.cutValueExt(transXTPN.getAlphaMinValue(), transXTPN.getFraction_xTPN()) + ", "
										+ Tools.cutValueExt(transXTPN.getAlphaMaxValue(), transXTPN.getFraction_xTPN());
								int width2 = g.getFontMetrics().stringWidth(alphaStr);
								g.drawString(alphaStr, drawX+width, drawY+moveDown);
								g.setFont(f_Big);
								alphaStr = "]";
								g.drawString(alphaStr, drawX+width+width2, drawY+moveDown);
								 */

								//String alfa = "\u03B1:" + Tools.cutValueExt(transXTPN.getAlphaMinValue(), transXTPN.getFraction_xTPN()) + " / "
								//		+ Tools.cutValueExt(transXTPN.getAlphaMaxValue(), transXTPN.getFraction_xTPN());
								//String alfa = "\u03B1: [" + Tools.cutValueExt(transXTPN.getAlphaMinValue(), transXTPN.getFraction_xTPN()) + ", "
								//		+ Tools.cutValueExt(transXTPN.getAlphaMaxValue(), transXTPN.getFraction_xTPN())+"]";
								String alfa = "α: [" + Tools.cutValueExt(transXTPN.getAlphaMinValue(), transXTPN.getFraction_xTPN()) + ", "
										+ Tools.cutValueExt(transXTPN.getAlphaMaxValue(), transXTPN.getFraction_xTPN())+"]";

								if(!transXTPN.isBetaModeActive()) { //jak nie ma bety, to alfa bliżej kwadratu tranzycji
									g.drawString(alfa,drawX, drawY);
								} else {
									g.drawString(alfa, drawX, drawY - 16);
								}
							}
						}

						if(transXTPN.isBetaModeActive() && transXTPN.isBetaRangeVisible()) {
							Point betaPoint = betaLocations.get(i).getPosition();
							int drawX = (nodePoint.x) + betaPoint.x - 40;
							int drawY =  (nodePoint.y) + betaPoint.y - 28;
							if(transXTPN.isBetaModeActive()) {
								g.setFont(f_BigL);
								g.setColor(betaColor);

								//String beta = "\u03B2: [" + Tools.cutValueExt(transXTPN.getBetaMinValue(), transXTPN.getFraction_xTPN()) + ", "
								//		+ Tools.cutValueExt(transXTPN.getBetaMaxValue(), transXTPN.getFraction_xTPN())+"]";
								String beta = "β: [" + Tools.cutValueExt(transXTPN.getBetaMinValue(), transXTPN.getFraction_xTPN()) + ", "
										+ Tools.cutValueExt(transXTPN.getBetaMaxValue(), transXTPN.getFraction_xTPN())+"]";
								g.drawString(beta, drawX, drawY);

							}
						}
					}
				}
				//wartości tau niezależnie od qSIM
				for (int i=0; i<alphaLocations.size(); i++) {
					if(namesLocations.get(i).getSheetID() != sheetId) //tylko dla danego arkusza
						continue;

					Point nodePoint = nodePoints.get(i);

					if(transXTPN.isTauTimerVisible() ) {
						Point tauPoint = tauLocations.get(i).getPosition();
						int drawX = (nodePoint.x) + tauPoint.x-5;
						int drawY =  (nodePoint.y) + tauPoint.y-15;

						g.setFont(f_BigL);
						if(transXTPN.isTauTimerVisible()) {
							double alphaTime = transXTPN.getTauAlphaValue();
							double betaTime = transXTPN.getTauBetaValue();
							double u_alfaTime = transXTPN.getTimerAlfaValue();
							double v_betaTime = transXTPN.getTimerBetaValue();
							boolean isAlpha = transXTPN.isAlphaModeActive();
							boolean isBeta = transXTPN.isBetaModeActive();

							String timerA;
							String timerB;
							g.setColor(tauColor);
							if(alphaTime < 0 && betaTime < 0) {
								if(isAlpha) {
									//timerA = "\u03C4(\u03B1): #\u279F#";
									timerA = "τ(α): #➟#";
									g.drawString(timerA, drawX + 40, drawY + 12);
								}
								if(isBeta) {
									//timerB = "\u03C4(\u03B2): #\u279F#";
									timerB = "τ(β): #➟#";
									g.drawString(timerB, drawX + 40, drawY + 26);
								}
							} else if(alphaTime < 0) {
								if(isAlpha) {
									//timerA = "\u03C4(\u03B1): #\u279F#";
									timerA = "τ(α): #➟#";
									g.drawString(timerA, drawX + 40, drawY + 12);
								}
								if(isBeta) {
									//timerB = "\u03C4(\u03B2): " + Tools.cutValueExt(v_betaTime, transXTPN.getFraction_xTPN()) + "\u279F"
									//		+ Tools.cutValueExt(betaTime, transXTPN.getFraction_xTPN());
									timerB = "τ(β): " + Tools.cutValueExt(v_betaTime, transXTPN.getFraction_xTPN()) + "\u279F"
											+ Tools.cutValueExt(betaTime, transXTPN.getFraction_xTPN());
									g.drawString(timerB, drawX + 40, drawY + 26);
								}
							} else if(betaTime < 0) {
								if(isAlpha) {
									//timerA = "\u03C4(\u03B1): " + Tools.cutValueExt(u_alfaTime, transXTPN.getFraction_xTPN()) + "\u279F"
									//		+ Tools.cutValueExt(alphaTime, transXTPN.getFraction_xTPN());
									timerA = "τ(α): " + Tools.cutValueExt(u_alfaTime, transXTPN.getFraction_xTPN()) + "\u279F"
											+ Tools.cutValueExt(alphaTime, transXTPN.getFraction_xTPN());
									g.drawString(timerA, drawX + 40, drawY + 12);
								}
								if(isBeta) {
									//timerB = "\u03C4(\u03B2): #\u279F#";
									timerB = "\u03C4(\u03B2): #\u279F#";
									g.drawString(timerB, drawX + 40, drawY + 26);
								}
							} else {
								if(isAlpha) {
									//timerA = "\u03C4(\u03B1): " + Tools.cutValueExt(u_alfaTime, transXTPN.getFraction_xTPN()) + "\u279F"
									//		+ Tools.cutValueExt(alphaTime, transXTPN.getFraction_xTPN());
									timerA = "τ(α): " + Tools.cutValueExt(u_alfaTime, transXTPN.getFraction_xTPN()) + "\u279F"
											+ Tools.cutValueExt(alphaTime, transXTPN.getFraction_xTPN());
									g.drawString(timerA, drawX + 40, drawY + 12);
								}
								if(isBeta) {
									//timerB = "\u03C4(\u03B2): " + Tools.cutValueExt(v_betaTime, transXTPN.getFraction_xTPN()) + "\u279F"
									//		+ Tools.cutValueExt(betaTime, transXTPN.getFraction_xTPN());
									timerB = "τ(β): " + Tools.cutValueExt(v_betaTime, transXTPN.getFraction_xTPN()) + "\u279F"
											+ Tools.cutValueExt(betaTime, transXTPN.getFraction_xTPN());
									g.drawString(timerB, drawX + 40, drawY + 26);
								}
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Metoda pozwala sprawdzić, czy dany punkt na danym arkuszu zawiera się
	 * w obszarze rysowania tego wierzchołka sieci Petriego.
	 * @param point (<b>Point</b>) sprawdzany punkt.
	 * @param sheetId (<b>int</b>) identyfikator arkusza.
	 * @return (<b>boolean</b>) - true, jeśli lokalizacja faktycznie zawiera się w obszarze
	 * 	rysowania tego wierzchołka sieci Petriego; false w przeciwnym wypadku.
	 */
	public boolean contains(Point point, int sheetId) {
		for (Point p : this.getNodePositions(sheetId))
			if (p.x - getRadius() < point.x && p.y - getRadius() < point.y
					&& p.x + getRadius() > point.x && p.y + getRadius() > point.y)
				return true;
		return false;
	}

	/**
	 * Metoda pozwala pobrać lokalizację wierzchołka sieci Petriego, której 
	 * obszar rysowania zawiera dany punkt na danym arkuszu.
	 * @param point (<b>Point</b>) sprawdzany punkt.
	 * @param sheetId (<b>int</b>) identyfikator arkusza.
	 * @param additionalRange (<b>int</b>) [2022-07] dodatkowy zakres wyszukiwania w pixelach, do tej pory
	 *                        tego nie było, więc tak jakby 0 dla działania domyślnego.
	 * @return (<b>ElementLocation</b>) - lokalizacja wierzchołka sieci Petriego,
	 * 		której obszar rysowania zawiera wybrany punkt na wybranym arkuszu.
	 */
	public ElementLocation getLocationWhichContains(Point point, int sheetId, int additionalRange) {
		for (ElementLocation e : this.getNodeLocations(sheetId)) {
			if (e.getPosition().x - getRadius() - additionalRange < point.x && e.getPosition().y - getRadius() - additionalRange < point.y
					&& e.getPosition().x + getRadius() + additionalRange > point.x && e.getPosition().y + getRadius() + additionalRange > point.y)
				return e;
		}
		return null;
	}

	/**
	 * Metoda pozwala pobrać listę lokacji, które zawierają się w danym
	 * prostokątnym obszarze na danym arkuszu.
	 * @param rectangle (<b>Rectangle</b>) prostokątny obszar.
	 * @param sheetId (<b>int</b>) identyfikator arkusza.
	 * @return (<b>ArrayList[ElementLocation]</b>) - lista lokalizacji, które
	 * 			zawierają się w wybranym prostokątnym obszarze na wybranym arkuszu.
	 */
	public ArrayList<ElementLocation> getLocationsWhichAreContained(Rectangle rectangle, int sheetId) {
		ArrayList<ElementLocation> returnElementLocations = new ArrayList<>();
		for (ElementLocation el : this.getNodeLocations(sheetId))
			if (rectangle.contains(el.getPosition()))
				returnElementLocations.add(el);
		return returnElementLocations;
	}

	/**
	 * Wykonanie tej metody oznacza wszystkie lokacje tego wierzchołka sieci Petriego jako wybrane.
	 */
	public void selectAllPortals() {
		if (!isPortal())
			return;
		for (ElementLocation el : this.getNodeLocations())
			if (!el.isSelected())
				el.setPortalSelected(true);
	}

	/**
	 * Wykonanie tej metody oznacza wszystkie lokacje tego wierzchołka sieci Petriego jako nie wybrane.
	 */
	public void deselectAllPortals() {
		if (!isPortal())
			return;
		for (ElementLocation el : this.getNodeLocations())
			el.setPortalSelected(false);
	}

	@SuppressWarnings("unused")
	public void forceDeselection() {
		if (!isPortal())
			return;
		for (ElementLocation el : this.getNodeLocations()) {
			el.setPortalSelected(false);
			el.setSelected(false);
		}
	}

	/**
	 * Metoda pozwala sprawdzić, czy wszystkie lokacje tego wierzchołka sieci Petriego są oznaczone jako wybrane.
	 * @return (<b>boolean</b>) - true jeśli wierzchołek jest portalem; false w przeciwnym wypadku.
	 */
	public boolean checkAllPortalsSelection() {
		if (!isPortal())
			return false;
		for (ElementLocation el : this.getNodeLocations())
			if (el.isSelected())
				return true;
		return false;
	}

	/**
	 * Metoda sprawdza, czy wierzchołek sieci Petriego jest portalem (czy ma więcej niż jedną lokację).
	 * @return (<b>boolean</b>) - true, jeśli wierzchołek jest portalem; false w przeciwnym wypadku.
	 */
	public boolean isPortal() {
		return isPortal;
	}

	/**
	 * Metoda pozwala oznaczyć, czy wierzchołek jest portalem (czy ma więcej niż jedną lokację).
	 * @param isPortal (<b>boolean</b>) - wartość określająca, czy wierzchołek ma być portalem.
	 */
	public void setPortal(boolean isPortal) {
		this.isPortal = isPortal;
	}

	/**
	 * Metoda pozwala pobrać promień okręgu, na którym opisana jest figura
	 * geometryczna reprezentująca obiekt w edytorze graficznym. 
	 * @return (<b>int</b>) - promień okręgu, na którym opisana jest figura
	 * 		geometryczna reprezentująca obiekt w edytorze graficznym
	 */
	public int getRadius() {
		return radius;
	}

	/**
	 * Metoda pozwala ustawić promień okręgu, na którym opisana jest figura
	 * geometryczna reprezentująca obiekt w edytorze graficznym. 
	 * @param radius (<b>int</b>) promień okręgu.
	 */
	public void setRadius(int radius) {
		this.radius = radius;
	}

	/**
	 * Metoda pozwala pobrać listę wszystkich lokacji wierzchołka.
	 * @return (<b>ArrayList[ElementLocation]</b>) - lista wszystkich lokalizacji wierzchołka.
	 */
	public ArrayList<ElementLocation> getNodeLocations() {
		return getElementLocations();
	}

	/**
	 * Metoda pozwala usunąć lokalizację z listy wszystkich lokalizacji wierzchołka.
	 * @param el (<b>ElementLocation</b>) lokalizacja do usunięcia.
	 * @return (<b>boolean</b>) - false, jeśli usunięto jedyną (czyli ostatnią) lokalizację wierzchołka,
	 * 		true, jeżeli wierzchołek ma jeszcze inne lokalizacje
	 */
	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	public boolean removeElementLocation(ElementLocation el) {
		int nodeElLocIndex = this.getNodeLocations().indexOf(el);
		this.getTextsLocations(GUIManager.locationMoveType.NAME).remove(nodeElLocIndex);

		if(el.getParentNode() instanceof PlaceXTPN || el.getParentNode() instanceof TransitionXTPN) {
			this.getTextsLocations(GUIManager.locationMoveType.ALPHA).remove(nodeElLocIndex); //XTPN lokalizacja
			this.getTextsLocations(GUIManager.locationMoveType.BETA).remove(nodeElLocIndex); //XTPN lokalizacja
			this.getTextsLocations(GUIManager.locationMoveType.GAMMA).remove(nodeElLocIndex); //XTPN lokalizacja
			this.getTextsLocations(GUIManager.locationMoveType.TAU).remove(nodeElLocIndex); //XTPN lokalizacja
		}
		this.getNodeLocations().remove(el);
		
		int subNet = el.getSheetID();
		if(subNet > 0) {
			boolean found = false;
			for(ElementLocation element : getElementLocations()) {
				if(element.getSheetID() == subNet) {
					found = true;
					break;
				}
			}
			if(!found) {
				GUIManager.getDefaultGUIManager().subnetsHQ.clearAllMetaArcs(this, subNet);
			}
		}
		return (!this.getNodeLocations().isEmpty());
			//false, jeżeli usunięto jedyną (czyli ostatnią) lokalizację wierzchołka
	}

	/**
	 * Metoda pozwala pobrać listę łuków wejściowych.
	 * @return (ArrayList[<b>Arc</b>]) - lista łuków wejściowych.
	 */
	public ArrayList<Arc> getInputArcs() {
		ArrayList<Arc> totalInArcs = new ArrayList<>();
		for (ElementLocation location : getNodeLocations()) {
			totalInArcs.addAll(location.getInArcs());
		}
		return totalInArcs;
	}

	/**
	 * Metoda pozwala pobrać listę WSZYSTKICH łuków wyjściowych wierzchołka.
	 * @return (ArrayList[<b>Arc</b>]) - lista łuków wyjściowych
	 */
	public ArrayList<Arc> getOutputArcs() {
		ArrayList<Arc> totalOutArcs = new ArrayList<>();
		for (ElementLocation location : getNodeLocations()) {
			totalOutArcs.addAll(location.getOutArcs());
		}
		return totalOutArcs;
	}

	/**
	 * Metoda pozwala pobrać listę WSZYSTKICH wierzchołków wejściowych wierzchołka.
	 * @return (ArrayList[<b>Node</b>]) - lista wierzchołków wejściowych.
	 */
	public ArrayList<Node> getInputNodes() {
		ArrayList<Node> totalInNodes = new ArrayList<>();
		for (Arc arc : getInputArcs()) {
			totalInNodes.add(arc.getStartNode());
		}
		return totalInNodes;
	}

	/**
	 * Metoda pozwala pobrać listę wierzchołków wyjściowych.
	 * @return (ArrayList[<b>Node</b>]) - lista wierzchołków wyjściowych.
	 */
	public ArrayList<Node> getOutputNodes() {
		ArrayList<Node> totalOutNodes = new ArrayList<>();
		for (Arc arc : getOutputArcs()) {
			totalOutNodes.add(arc.getEndNode());
		}
		return totalOutNodes;
	}

	/**
	 * Metoda pozwala pobrać listę wszystkich sąsiadujących wierzchołków..
	 * @return (ArrayList[<b>Node</b>]) - lista wierzchołków sąsiadujących.
	 */
	public ArrayList<Node> getNeighborsNodes() {
		ArrayList<Node> totalNodes = new ArrayList<>();
		for (Arc arc : getOutputArcs()) {
			totalNodes.add(arc.getEndNode());
		}
		for (Arc arc : getInputArcs()) {
			totalNodes.add(arc.getStartNode());
		}
		return totalNodes;
	}

	/**
	 * Zwraca wartość identyfikatora węzła.
	 * @return (<b>String</b>) - łańcuch znaków ID
	 */
	public String toString() {
		String type;
		if(this instanceof Place)
			type = "(P)";
		else if(this instanceof Transition)
			type = "(T)";
		else if(this instanceof MetaNode)
			type = "(M)";
		else
			type = "(?)";
		return "ID: " + this.getID() +type;
	}

	/**
	 * Metoda zwraca wektor lokalizacji wierzchołka.
	 * @return (ArrayList[<b>ElementLocation</b>]) - tablica lokalizacji
	 */
	public ArrayList<ElementLocation> getElementLocations() {
		return elementLocations;
	}

	/**
	 * Metoda pozwala ustawić listę wszystkich lokalizacji wierzchołka.
	 * @param elementLocations (ArrayList[<b>ElementLocation</b>]) wektor lokalizacji wierzchołka
	 */
	public void setElementLocations(ArrayList<ElementLocation> elementLocations) {
		this.elementLocations = elementLocations;
	}
	
	/**
	 * Metoda zwraca wektor lokalizacji nazwy wierzchołka.
	 * @param dataType (<b>GUIManager.locationMoveType</b>) NAME, ALPHA, BETA, GAMMA, TAU
	 * @return (ArrayList[<b>ElementLocation</b>]) - odpowiedni wektor lokalizacji nazw.
	 */
	public ArrayList<ElementLocation> getTextsLocations(GUIManager.locationMoveType dataType) {
		return switch (dataType) {
			case ALPHA -> alphaLocations;
			case BETA -> betaLocations;
			case GAMMA -> gammaLocations;
			case TAU -> tauLocations;
			default -> namesLocations;
		};
	}

	/**
	 * Metoda ustawia nowy wektor lokalizacji nazw odpowiedniego typu.
	 * @param namesLocations (ArrayList[<b>ElementLocation</b>]) wektor lokalizacji nazw.
	 * @param nameType (<b>GUIManager.locationMoveType</b>) NAME, ALPHA, BETA, GAMMA, TAU
	 */
	public void setTextsLocations(ArrayList<ElementLocation> namesLocations, GUIManager.locationMoveType nameType) {
		switch(nameType) {
			case ALPHA -> this.alphaLocations = namesLocations;
			case BETA -> this.betaLocations = namesLocations;
			case GAMMA -> this.gammaLocations = namesLocations;
			case TAU -> this.tauLocations = namesLocations;
			default -> this.namesLocations = namesLocations;
		}
	}
	
	/**
	 * Metoda zwraca pozycję X nazwy wierzchołka o pozycji danej jako argument.
	 * @param index (<b>int</b>) indeks w wektorze ElementLocations.
	 * @param nameType (<b>GUIManager.locationMoveType</b>) NAME, ALPHA, BETA, GAMMA, TAU.
	 * @return (<b>int</b>) - pozycja X napisu.
	 */
	public int getTextLocation_X(int index, GUIManager.locationMoveType nameType) {
		switch (nameType) {
			case NAME -> {
				if (index >= namesLocations.size()) {
					GUIManager.getDefaultGUIManager().log("Internal error: invalid index for name location (X position). Node: " + getName(), "error", true);
					return 0;
				}
				return namesLocations.get(index).getPosition().x;
			}
			case ALPHA -> {
				if (index >= alphaLocations.size()) {
					GUIManager.getDefaultGUIManager().log("Internal error: invalid index for alpha location (X position). Node: " + getName(), "error", true);
					return 0;
				}
				return alphaLocations.get(index).getPosition().x;
			}
			case BETA -> {
				if (index >= betaLocations.size()) {
					GUIManager.getDefaultGUIManager().log("Internal error: invalid index for beta location (X position). Node: " + getName(), "error", true);
					return 0;
				}
				return betaLocations.get(index).getPosition().x;
			}
			case GAMMA -> {
				if (index >= gammaLocations.size()) {
					GUIManager.getDefaultGUIManager().log("Internal error: invalid index for gamma location (X position). Node: " + getName(), "error", true);
					return 0;
				}
				return gammaLocations.get(index).getPosition().x;
			}
			case TAU -> {
				if (index >= tauLocations.size()) {
					GUIManager.getDefaultGUIManager().log("Internal error: invalid index for tau location (X position). Node: " + getName(), "error", true);
					return 0;
				}
				return tauLocations.get(index).getPosition().x;
			}
			default -> {
				if (index >= namesLocations.size()) {
					GUIManager.getDefaultGUIManager().log("Internal error: invalid index for nama location (X position). Node: " + getName(), "error", true);
					return 0;
				}
				return namesLocations.get(index).getPosition().x;
			}
		}
	}
	
	/**
	 * Metoda zwraca pozycję Y nazwy wierzchołka o pozycji danej jako argument.
	 * @param index (<b>int</b>) indeks w wektorze ElementLocations.
	 * @param nameType (<b>GUIManager.locationMoveType</b>) NAME, ALPHA, BETA, GAMMA, TAU.
	 * @return (<b>int</b>) - pozycja Y napisu..
	 */
	public int getTextLocation_Y(int index, GUIManager.locationMoveType nameType) {
		switch (nameType) {
			case ALPHA -> {
				if (index >= alphaLocations.size()) {
					GUIManager.getDefaultGUIManager().log("Internal error: invalid index for alpha location (Y position). Node: " + getName(), "error", true);
					return 0;
				}
				return alphaLocations.get(index).getPosition().y;
			}
			case BETA -> {
				if (index >= betaLocations.size()) {
					GUIManager.getDefaultGUIManager().log("Internal error: invalid index for beta location (Y position). Node: " + getName(), "error", true);
					return 0;
				}
				return betaLocations.get(index).getPosition().y;
			}
			case GAMMA -> {
				if (index >= gammaLocations.size()) {
					GUIManager.getDefaultGUIManager().log("Internal error: invalid index for gamma location (Y position). Node: " + getName(), "error", true);
					return 0;
				}
				return gammaLocations.get(index).getPosition().y;
			}
			case TAU -> {
				if (index >= tauLocations.size()) {
					GUIManager.getDefaultGUIManager().log("Internal error: invalid index for tau location (Y position). Node: " + getName(), "error", true);
					return 0;
				}
				return tauLocations.get(index).getPosition().y;
			}
			default -> {
				if (index >= namesLocations.size()) {
					GUIManager.getDefaultGUIManager().log("Internal error: invalid index for name location (Y position). Node: " + getName(), "error", true);
					return 0;
				}
				return namesLocations.get(index).getPosition().y;
			}
		}
	}
	
	/**
	 * Metoda ustawia status niewidzialności tranzycji dla generatora inwariantów.
	 * @param status (<b>boolean</b>) true, jeśli ma być niewidzialna.
	 */
	public void setInvisibility(boolean status) {
		invisible = status;
	}
	
	/**
	 * Metoda zwraca status niewidzialności tranzycji dla generatora inwariantów.
	 * @return (<b>boolean</b>) - true, jeśli tranzycja jest niewidzialna.
	 */
	public boolean isInvisible() {
		return invisible;
	}

	public ArrayList<Arc> getNeighborsArcs(){
		ArrayList<Arc> totalInArcs = new ArrayList<>();

		for (ElementLocation location : getNodeLocations()) {
			totalInArcs.addAll(location.getInArcs());
			totalInArcs.addAll(location.getOutArcs());
		}
		return totalInArcs;
	}
}
