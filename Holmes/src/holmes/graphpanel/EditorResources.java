package holmes.graphpanel;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Stroke;

/**
 * Klasa zasobów programu, ogólnie mówiac definiuje stałe związane z grafiką.
 */
@SuppressWarnings("unused")
public class EditorResources {
	public static final Color selectionColorLevel1 = new Color(30, 144, 255, 20);
	public static final Color selectionColorLevel2 = new Color(30, 144, 255, 50);
	public static final Color selectionColorLevel3 = new Color(30, 144, 255, 100);
	
	public static final Color glowPortalColorLevel1 = new Color(255, 230, 0, 50);
	public static final Color glowPortalColorLevel2 = new Color(255, 230, 0, 100);
	public static final Color glowPortalColorLevel3 = new Color(255, 230, 0, 140);
	
	public static final Color glowTransitonColorLevel1 = new Color(140, 190, 41, 50);
	public static final Color glowTransitonColorLevel2 = new Color(140, 190, 41, 100);
	public static final Color glowTransitonColorLevel3 = new Color(140, 190, 41, 140);
	
	public static final Color glowPlaceColorLevelBlue = new Color(0, 102, 204, 180);
	public static final Color glowPlaceColorLevelRed = new Color(255, 0, 0, 180);
	
	public static final Color glowMTCTransitonColorLevel1 = new Color(165, 0, 255, 50);
	public static final Color glowMTCTransitonColorLevel2 = new Color(165, 0, 255, 100);
	public static final Color glowMTCTransitonColorLevel3 = new Color(165, 0, 255, 140);
	
	public static final Color glowTransitonTextColor = new Color(0, 89, 0, 255);
	
	public static final Color graphPanelMeshColor = new Color(200, 200, 200, 50);
	
	public static final Stroke glowStrokeLevel1 = new BasicStroke(8);
	public static final Stroke glowStrokeLevel2 =  new BasicStroke(6);
	public static final Stroke glowStrokeLevel3 =  new BasicStroke(4);
	
	public static final Stroke glowStrokeArc =  new BasicStroke(2);
	
	public static final Stroke selectionRectStroke = new BasicStroke(0.8f);
	public static final Color selectionRectFill = new Color(61, 64, 67, 5);
	public static final Color selectionRectColor = new Color(61, 64, 67, 250);
	
	public static final Color tokenDefaultColor = new Color(206, 0, 0, 255);
	public static final Stroke tokenDefaultStroke = new BasicStroke(0.5f);
	
	public static final Color launchColorLevel1 = new Color(230, 20, 0, 20);
	public static final Color launchColorLevel2 = new Color(230, 20, 0, 50);
	public static final Color launchColorLevel3 = new Color(230, 20, 0, 100);


	public static final Color arcNeutralXTPNcolor = new Color(0, 0, 0, 255);
	public static final Color activationXTPNcolor = new Color(0, 51, 153, 255); //kolor łuku dla aktywacji
	public static final Color productionXTPNcolor = new Color(0, 200, 0, 255); //kolor łuku dla produkcji
	public static final Color inhibitorXTPNcolor = new Color(250, 51, 0, 255);

	public static final Color lightGray = new Color(174, 174, 174, 255);

	public static final Color lighterGray = new Color(192, 192, 192, 255);


	public static final Color lightSky = new Color(0, 135, 230);
	public static final Color lightSky2 = new Color(0, 185, 230);
	public static final Color lightSky3 = new Color(0, 215, 230);
	
	
	public static final Color stargate1 = new Color(0, 130, 255);
	public static final Color stargate2 = new Color(0, 150, 255);
	public static final Color stargate3 = new Color(0, 170, 255);
	public static final Color stargate4 = new Color(0, 190, 255);
	public static final Color stargate5 = new Color(0, 210, 255);
	public static final Color stargate6 = new Color(0, 230, 255);


	//Color-net:
	public static final Color cRed = Color.red;
	public static final Color cGreen = Color.green;
	public static final Color cBlue = Color.blue;
	public static final Color cYellow = new Color(255,155,0) ;
	public static final Color cGrey = Color.gray;
	public static final Color cBlack = Color.black;


	//oznaczania TPN / DPN:
	public static final Color tpnNOTdpn = new Color(51, 255, 51);
	public static final Color dpnNOTtpn = new Color(0,153,76);
	public static final Color tpnANDdpn = new Color(0,102,0);


	public static final Color placeDefColor = new Color(255,255,255);
	public static final Color transDefColor = new Color(224, 224, 224);

	//XTPN:
	public static final Color alphaColor = new Color(51, 102, 255);
	public static final Color betaColor = new Color(0, 153, 0);
	public static final Color gammaColor = new Color(60, 60, 60);
	public static final Color tauColor = new Color(255, 51, 0);
}
