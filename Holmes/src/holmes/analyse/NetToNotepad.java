package holmes.analyse;

import holmes.darkgui.GUIManager;
import holmes.darkgui.LanguageManager;
import holmes.petrinet.elements.*;
import holmes.windows.HolmesNotepad;

import java.util.ArrayList;

public class NetToNotepad {
    private static final GUIManager overlord = GUIManager.getDefaultGUIManager();
    private static final LanguageManager lang = GUIManager.getLanguageManager();
    
    public void exportToNotepad() {
        HolmesNotepad notePad = null;
        notePad = new HolmesNotepad(900,600);
        notePad.addTextLineNL("Net structure:", "text");

        //for every transition if exists, show its name, in parenthesis t+index in transitions list, then its
        //eft and lft if it is TPN timed transition

        ArrayList<Transition> transitions = overlord.getWorkspace().getProject().getTransitions();
        ArrayList<Place> places = overlord.getWorkspace().getProject().getPlaces();
        
        if(transitions.isEmpty()) {
            notePad.addTextLineNL("No transitions in the net.", "text");
        } else {
            
            notePad.addTextLineNL("Transitions:", "text");  
            notePad.addTextLineNL("", "text");
            
            for (Transition t : transitions) {
                String transitionText = t.getName() + " (t" + transitions.indexOf(t) + ")";
                if (t.timeExtension.isTPN()) {
                    transitionText += " [eft: " + t.timeExtension.getEFT() + ", lft: " + t.timeExtension.getLFT() + "]";
                }
                notePad.addTextLineNL(transitionText, "text");
            }
        }
        notePad.addTextLineNL("", "text");
        notePad.addTextLineNL("Places:", "text");
        notePad.addTextLineNL("", "text");
        for(Place p : places) {
            String placeText = p.getName() + " (p" + places.indexOf(p) + ")";
            notePad.addTextLineNL(placeText, "text");
        }
        
        //list of arcs, in format tx (where x is index of transition in transitions list) -> py (where y is index of place in places list)
        // in same line, in parenthesis, show weight of the arc

        //łuki wyjściowe z tranzycji:
        notePad.addTextLineNL("", "text");
        notePad.addTextLineNL("Arcs:", "text");
        notePad.addTextLineNL("Output arcs from transitions:", "text");
        for(Transition t : transitions) {
            int elLocations = t.getElementLocations().size();
            for(int e=0; e<elLocations; e++) { //wszystkie lokalizacje tranzycji
                ElementLocation eLoc = t.getElementLocations().get(e);
                ArrayList<Arc> tmp_outgoingArcs = new ArrayList<>(eLoc.getOutArcs());
                
                for (Arc arc : tmp_outgoingArcs) { //wszystkie łuki wyjściowe
                    Node endNode = arc.getEndNode();
                    String startLoc = "t" + transitions.indexOf(t);// + "(" + e + ")";
                    Place endPlace = (Place) endNode;
                    int endNodeIndex = places.indexOf(endPlace);
                    String endLoc = "p" + endNodeIndex;// + "(" + endNodeLocationIndex + ")";
                    int weight = arc.getWeight();

                    String readArc="";
                    if(arc.getArcType() == Arc.TypeOfArc.READARC) {
                        readArc = " [read arc]";
                    }
                    String arcText = startLoc + " -> " + endLoc + " (weight: " + weight + ") "+readArc;
                    notePad.addTextLineNL(arcText, "text");
                }
            }
        }

        //łuki wyjściowe z miejsc:
        notePad.addTextLineNL("", "text");
        notePad.addTextLineNL("Output arcs from places:", "text");
        for(Place p : places) {
            int elLocations = p.getElementLocations().size();
            for(int e=0; e<elLocations; e++) { //wszystkie lokalizacje miejsca
                ElementLocation eLoc = p.getElementLocations().get(e);
                ArrayList<Arc> tmp_outgoingArcs = new ArrayList<>(eLoc.getOutArcs());

                for (Arc arc : tmp_outgoingArcs) { //wszystkie łuki wyjściowe
                    Node endNode = arc.getEndNode();
                    String startLoc = "p" + places.indexOf(p);// + "(" + e + ")";
                    Transition endTransition = (Transition) endNode;
                    int endNodeIndex = transitions.indexOf(endTransition);
                    String endLoc = "t" + endNodeIndex;// + "(" + endNodeLocationIndex + ")";
                    int weight = arc.getWeight();

                    String readArc="";
                    if(arc.getArcType() == Arc.TypeOfArc.READARC) {
                        readArc = " [read arc]";
                    }
                    String arcText = startLoc + " -> " + endLoc + " (weight: " + weight + ") "+readArc;
                    notePad.addTextLineNL(arcText, "text");
                }
            }
        }
        notePad.setVisible(true);
        notePad.addTextLineNL("", "text");
    }
}
