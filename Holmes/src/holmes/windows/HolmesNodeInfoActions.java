package holmes.windows;

import java.util.ArrayList;
import java.util.Collections;

import javax.swing.JFrame;
import javax.swing.JProgressBar;

import holmes.analyse.InvariantsTools;
import holmes.darkgui.GUIManager;
import holmes.darkgui.LanguageManager;
import holmes.graphpanel.GraphPanel;
import holmes.petrinet.elements.Arc;
import holmes.petrinet.elements.Node;
import holmes.petrinet.elements.Place;
import holmes.petrinet.elements.Transition;
import holmes.utilities.Tools;

/**
 * Klasa użytkowa dla HolmesNodeInfo, zawiera metody wywoływane interfejsem opisanym w ramach HolmesNodeInfo.
 */
public class HolmesNodeInfoActions {
	private JFrame parentFrame;
	private static final GUIManager overlord = GUIManager.getDefaultGUIManager();
	private static final LanguageManager lang = GUIManager.getLanguageManager();
	
	public HolmesNodeInfoActions(JFrame parent) {
		parentFrame = parent;
	}

	/**
	 * Metoda zmieniająca nazwę dla miejsca sieci.
	 * @param place Place - obiekt miejsca
	 * @param newName String - nowa nazwa
	 */
	public void changeName(Node place, String newName) {
		place.setName(newName);
		repaintGraphPanel(place);
	}
	
	/**
	 * Metoda zmieniająca komentarz dla miejsca sieci.
	 * @param place Place - obiekt miejsca
	 * @param newComment String - nowa nazwa
	 */
	public void changeComment(Node place, String newComment) {
		place.setComment(newComment);
	}
	
	/**
	 * Metoda ustawia nową liczbę tokenów dla miejsca.
	 * @param place Place - obiekt miejsca
	 * @param tokens int - nowa liczba tokenów
	 */
	public void setTokens(Place place, int tokens) {
		place.setTokensNumber(tokens);
		repaintGraphPanel(place);
	}
	
	/**
	 * Metoda odpowiedzialna za przerysowanie grafu obrazu w arkuszu sieci.
	 */
	private void repaintGraphPanel(Node node) {
		int sheetIndex = overlord.IDtoIndex(node.getElementLocations().get(0).getSheetID());
		GraphPanel graphPanel = overlord.getWorkspace().getSheets().get(sheetIndex).getGraphPanel();
		graphPanel.repaint();
	}

	/**
	 * Metoda odpowiedzialna za aktualizację komórki tabeli wyświetlającej nazwę miejsca i tranzycji.
	 * @param parentFrame JFrame - obiekt okna wywołującego dla AnyssNodeInfo
	 * @param name String - nowa nazwa
	 */
	public void parentTableUpdate(JFrame parentFrame, String name) {
		if(parentFrame instanceof HolmesNetTables) {
			((HolmesNetTables)parentFrame).updateRow(name, 1);
			//jeśli macierzyste oknow dla HolmesNodeInfo to HolmesNetTables, wtedy jeszcze
			//wywołujemy metodę aktualizującą nazwę miejsca którą właśnie zmieniliśmy, ale
			//bez potrzeby przeładowywania całej tabeli danych
			
			//P.S. NIE, NIE MOŻNA ZASTĄPIĆ PRZEZ this.parentFrame! Nie ten obiekt!
		}
	}

	public void showTinvForPlace(Place place, JProgressBar progressBar) {
		ArrayList<ArrayList<Integer>> invariantsMatrix = overlord.getWorkspace().getProject().getT_InvMatrix();
		if(invariantsMatrix == null || invariantsMatrix.isEmpty())
			return;

		HolmesNotepad notepad = new HolmesNotepad(800, 600, true);
		ArrayList<Transition> transitions = overlord.getWorkspace().getProject().getTransitions();
		//ArrayList<Integer> invariant = invariantsMatrix.get(0);
		
		ArrayList<ArrayList<Integer>> invariantsSubMatrix = new ArrayList<>();
		
		//int invs = -1;
		for(ArrayList<Integer> invariant : invariantsMatrix) {
			//invs++;
			int position = -1;
			
			boolean sourcePlaceFound = false;
			for(int t : invariant) {
				position++;
				if(t == 0)
					continue;
				
				for(Arc arc : transitions.get(position).getInputArcs()) {
					if(arc.getStartNode().equals(place)) {
						sourcePlaceFound = true;
						break;
					}
				}
				if(sourcePlaceFound)
					break;
			}
			if(!sourcePlaceFound)
				continue; //nie ten inwariant
			
			position = -1;
			boolean targetPlaceFound = false;
			for(int t : invariant) {
				position++;
				if(t == 0)
					continue;
				
				for(Arc arc : transitions.get(position).getOutputArcs()) {
					if(arc.getEndNode().equals(place)) {
						targetPlaceFound = true;
						break;
					}
				}
				if(targetPlaceFound)
					break;
			}
			
			if(targetPlaceFound) {
				invariantsSubMatrix.add(invariant);
			}
		}

		ArrayList<ArrayList<Transition>> mctSets = overlord.getWorkspace().getProject().getMCTMatrix();
		
		progressBar.setMaximum(invariantsSubMatrix.size());
		progressBar.setMinimum(0);
	    progressBar.setValue(0);
	    int counter = 0;
		for(ArrayList<Integer> invariant : invariantsSubMatrix) {
			int index = invariantsMatrix.indexOf(invariant);
			showInvariantNotepadSimple(counter, index, notepad, invariant, transitions, mctSets);

			notepad.addTextLineNL("", "text");
			notepad.addTextLineNL("=====================================================================================", "text");
			notepad.addTextLineNL("", "text");
			
			progressBar.setValue(counter++);
			if(counter % 20 == 0)
				progressBar.update(progressBar.getGraphics());
		}
		
		notepad.addTextLineNL("", "text");
		notepad.addTextLineNL("=====================================================================================", "text");
		notepad.addTextLineNL("=====================================================================================", "text");
		notepad.addTextLineNL("=====================================================================================", "text");
		notepad.addTextLineNL("", "text");
		
		counter = 0;
		for(ArrayList<Integer> invariant : invariantsSubMatrix) {
			StringBuilder vector = new StringBuilder();
			for (int fireValue : invariant) {
				vector.append(fireValue).append(";");
				if (fireValue == 0)
					continue;
			}
			vector = new StringBuilder(vector.substring(0, vector.length() - 1));
			notepad.addTextLineNL(counter+";"+vector, "text");
			counter++;
		}

		notepad.setVisible(true);
	}

	public void showTinvForTransition(Transition transition, JProgressBar progressBar) {
		ArrayList<ArrayList<Integer>> invariantsMatrix = overlord.getWorkspace().getProject().getT_InvMatrix();
		if(invariantsMatrix == null || invariantsMatrix.isEmpty())
			return;
		
		HolmesNotepad notepad = new HolmesNotepad(800, 600, true);
		ArrayList<Transition> transitions = overlord.getWorkspace().getProject().getTransitions();
		//ArrayList<Integer> invariant = invariantsMatrix.get(0);
		
		ArrayList<ArrayList<Integer>> invariantsSubMatrix = InvariantsTools.returnT_invWithTransition(invariantsMatrix, transitions.indexOf(transition));
		ArrayList<ArrayList<Transition>> mctSets = overlord.getWorkspace().getProject().getMCTMatrix();
		
		progressBar.setMaximum(invariantsSubMatrix.size());
		progressBar.setMinimum(0);
	    progressBar.setValue(0);
	    int counter = 0;
		for(ArrayList<Integer> invariant : invariantsSubMatrix) {
			int index = invariantsMatrix.indexOf(invariant);
			showInvariantNotepadSimple(counter, index, notepad, invariant, transitions, mctSets);

			notepad.addTextLineNL("", "text");
			notepad.addTextLineNL("=====================================================================================", "text");
			notepad.addTextLineNL("", "text");
			
			progressBar.setValue(counter++);
			if(counter % 20 == 0)
				progressBar.update(progressBar.getGraphics());
		}
		
		notepad.addTextLineNL("", "text");
		notepad.addTextLineNL("=====================================================================================", "text");
		notepad.addTextLineNL("=====================================================================================", "text");
		notepad.addTextLineNL("=====================================================================================", "text");
		notepad.addTextLineNL("", "text");
		
		counter = 0;
		for(ArrayList<Integer> invariant : invariantsSubMatrix) {
			StringBuilder vector = new StringBuilder();
			for (int fireValue : invariant) {
				vector.append(fireValue).append(";");
				if (fireValue == 0)
					continue;
			}
			vector = new StringBuilder(vector.substring(0, vector.length() - 1));
			notepad.addTextLineNL(counter+";"+vector, "text");
			counter++;
		}

		notepad.setVisible(true);
	}
	
	protected void showInvariantNotepad(int invNo, HolmesNotepad notepad, ArrayList<Integer> invariant, ArrayList<Transition> transitions) {
		//MCT:
		ArrayList<Integer> mcts = new ArrayList<Integer>();
		ArrayList<String> singleT = new ArrayList<String>();
		ArrayList<Integer> transMCTvector = overlord.getWorkspace().getProject().getMCTtransIndicesVector();
		int transNumber = 0;
		for(int t=0; t<invariant.size(); t++) {
			int fireValue = invariant.get(t);
			if(fireValue == 0)
				continue;
			
			transNumber++;
			int mctNo = transMCTvector.get(t);
			if(mctNo == -1) { 
				singleT.add("T"+t+"_"+transitions.get(t).getName());
			} else {
				if(!mcts.contains(mctNo)) {
					mcts.add(mctNo);
				}
			}
		}
		//Collections.sort(mcts);
		String description = overlord.getWorkspace().getProject().accessT_InvDescriptions().get(invNo);
		notepad.addTextLineNL(lang.getText("HNIAwin_entry001")+" "+(invNo+1), "text");
		notepad.addTextLineNL(lang.getText("HNIAwin_entry002")+" "+description, "text");
		notepad.addTextLineNL(lang.getText("HNIAwin_entry003")+" "+transNumber, "text");
		notepad.addTextLineNL(lang.getText("HNIAwin_entry004"), "text");
		for(int mct : mcts) {
			String MCTname = overlord.getWorkspace().getProject().getMCTname(mct);
			notepad.addTextLineNL("  [MCT: "+(mct+1)+"]: "+MCTname, "text");
		}
		for(String transName : singleT)
			notepad.addTextLineNL(transName, "text");
		//END OF STRUCTURE BLOCK
		
		notepad.addTextLineNL("", "text");
		notepad.addTextLineNL(lang.getText("HNIAwin_entry005") + (invNo+1)+":", "text");
		
		if(transitions.size() != invariant.size()) {
			transitions = overlord.getWorkspace().getProject().getTransitions();
			if(transitions == null || transitions.size() != invariant.size()) {
				overlord.log(lang.getText("LOGentry00484critErr"), "error", true);
				return;
			}
		}
		
		StringBuilder vector = new StringBuilder();
		for(int t=0; t<invariant.size(); t++) {
			int fireValue = invariant.get(t);
			vector.append(fireValue).append(";");
			if(fireValue == 0)
				continue;
			
			Transition realT = transitions.get(t);
			String t1 = Tools.setToSize("t"+t, 5, false);
			String t2 = Tools.setToSize("Fired: "+fireValue, 12, false);
			notepad.addTextLineNL(t1 + t2 + " ; "+realT.getName(), "text");
		}
		vector = new StringBuilder(vector.substring(0, vector.length() - 1));
		notepad.addTextLineNL("", "text");
		notepad.addTextLineNL(lang.getText("HNIAwin_entry006"), "text");
		notepad.addTextLineNL(vector.toString(), "text");

		//notepad.setCaretFirstLine();
	}
	
	protected void showInvariantNotepadSimple(int invNo, int index, HolmesNotepad notepad, ArrayList<Integer> invariant, ArrayList<Transition> transitions, ArrayList<ArrayList<Transition>> mctSets) {
		//MCT:
		ArrayList<Integer> mcts = new ArrayList<Integer>();
		ArrayList<String> singleT = new ArrayList<String>();
		ArrayList<Integer> transMCTvector = overlord.getWorkspace().getProject().getMCTtransIndicesVector();
		
		
		//int transNumber = 0;
		for(int t=0; t<invariant.size(); t++) {
			int fireValue = invariant.get(t);
			if(fireValue == 0)
				continue;
			
			//transNumber++;
			int mctNo = transMCTvector.get(t);
			if(mctNo == -1) { 
				singleT.add("t"+t+"_"+transitions.get(t).getName());
			} else {
				if(!mcts.contains(mctNo)) {
					mcts.add(mctNo);
				}
			}
		}
		Collections.sort(mcts);
		String strB = "err.";
		try {
			strB = String.format(lang.getText("HNIAwin_entry007"), invNo, index+1);
		} catch (Exception e) {
			overlord.log(lang.getText("LOGentryLNGexc")+" "+"HNIAwin_entry007", "error", true);
		}
		notepad.addTextLineNL(strB, "text");

		for(int mct : mcts) {
			StringBuilder transInMCT = new StringBuilder();
			for(Transition trans : mctSets.get(mct)) {
				transInMCT.append("t").append(transitions.indexOf(trans)).append("_").append(trans.getName()).append(",  ");
			}
			transInMCT = new StringBuilder(transInMCT.substring(0, transInMCT.length() - 3));
			notepad.addTextLineNL("  [MCT_"+(mct+1)+": "+transInMCT+"]", "text");
		}
		
		for(String transName : singleT)
			notepad.addTextLineNL(transName, "text");

		StringBuilder vector = new StringBuilder();
		for (int fireValue : invariant) {
			vector.append(fireValue).append(";");
			if (fireValue == 0)
				continue;

			//Transition realT = transitions.get(t);
			//String t1 = Tools.setToSize("t"+t, 5, false);
			//String t2 = Tools.setToSize("Fired: "+fireValue, 12, false);
			//notepad.addTextLineNL(t1 + t2 + " ; "+realT.getName(), "text");
		}
		vector = new StringBuilder(vector.substring(0, vector.length() - 1));
		notepad.addTextLineNL("", "text");
		notepad.addTextLineNL(lang.getText("HNIAwin_entry008"), "text");
		notepad.addTextLineNL(vector.toString(), "text");
	}
}
