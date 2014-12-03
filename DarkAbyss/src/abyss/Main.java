package abyss;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import abyss.darkgui.GUIManager;

public class Main {

	public static GUIManager guiManager;

	public static void main(String[] args) {
		Runnable doCreateAndShowGUI = new Runnable() {
			public void run() {
				guiManager = new GUIManager(new JFrame("Abyss - Zintegrowane œrodowisko do edycji, symulacji i analizy Sieci Petriego"));
				guiManager.getSimulatorBox().createSimulatorProperties();
				guiManager.getInvSimBox().createInvSimulatorProperties();
			}
		};
		SwingUtilities.invokeLater(doCreateAndShowGUI);
	}
}
