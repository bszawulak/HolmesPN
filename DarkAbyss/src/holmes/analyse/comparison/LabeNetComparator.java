package holmes.analyse.comparison;

import holmes.petrinet.data.PetriNetData;
import holmes.petrinet.elements.Arc;
import holmes.petrinet.elements.Node;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;

public class LabeNetComparator {


    public ArrayList<MatchedVertex> matchedVertices = new ArrayList<>();
    public PetriNetData fPND;
    public PetriNetData sPND;

    public LabeNetComparator(PetriNetData f, PetriNetData s) {
        fPND=f;
        sPND=s;
    }

    public void calcSimilarities(){

        for (int i = 0; i < fPND.nodes.size(); i++) {
            for (int j = 0; j < sPND.nodes.size(); j++) {
                if (fPND.nodes.get(i).getName().equals(sPND.nodes.get(j).getName())) {
                    matchedVertices.add(new MatchedVertex(fPND.nodes.get(i), sPND.nodes.get(j)));
                }
            }
        }
    }

    public ArrayList<String> getDifferencesFromFirstNet(){
        ArrayList<String> result = new ArrayList<>();

        result.add("-Nodes only in first net-\n");
        for (Node n : fPND.nodes) {
            if(matchedVertices.stream().filter(x->x.firstNetNode == n).collect(Collectors.toCollection(ArrayList::new)).size()<1)
            {
                result.add(n.getType() + " - ID : " + n.getID() + " " + n.getName());
            }
        }

        ArrayList<Arc> sumOfFirstNetArc = new ArrayList<>();
        ArrayList<MatchedArc> sumOfMatchedNetArc = new ArrayList<>();
        for (MatchedVertex mv :matchedVertices) {
            for (MatchedArc a: mv.arclist) {
                if(!sumOfFirstNetArc.contains(a.firstNetArc)) {
                    sumOfFirstNetArc.add(a.firstNetArc);
                    sumOfMatchedNetArc.add(a);
                }
            }
        }
        result.add("-Arc only in first net-\n");
        for (Arc a : fPND.arcs) {
            if(!sumOfFirstNetArc.contains(a))
            {
                result.add(a.getType() + " - ID : " + a.getID() + " From :" + a.getStartNode().getID() + "To :" + a.getEndNode().getID() );
            }
        }
        result.add("-Arc with different weight-\n");
        for (MatchedArc a : sumOfMatchedNetArc) {
            if(!a.isWehightEqual)
                result.add(a.firstNetArc.getType() + " - ID : " + a.firstNetArc.getID()+ " From :" + a.firstNetArc.getStartNode().getID() + "To :" + a.firstNetArc.getEndNode().getID()  + " Weight : "+a.firstNetArc.getWeight());

        }

        return result;
    }


    public ArrayList<String> getDifferencesFromSecondNet(){
        ArrayList<String> result = new ArrayList<>();

        result.add("-Nodes only in second net-\n");
        for (Node n : sPND.nodes) {
            if(matchedVertices.stream().filter(x->x.secondNetNode == n).collect(Collectors.toCollection(ArrayList::new)).size()<1)
            {
                result.add(n.getType() + " - ID : " + n.getID() + " " + n.getName());
            }
        }

        ArrayList<Arc> sumOfFirstNetArc = new ArrayList<>();
        ArrayList<MatchedArc> sumOfMatchedNetArc = new ArrayList<>();
        for (MatchedVertex mv :matchedVertices) {
            for (MatchedArc a: mv.arclist) {
                if(!sumOfFirstNetArc.contains(a.secondNetArc)) {
                    sumOfFirstNetArc.add(a.secondNetArc);
                    sumOfMatchedNetArc.add(a);
                }
            }
        }

        result.add("-Arc only in second net-\n");
        for (Arc a : sPND.arcs) {
            if(!sumOfFirstNetArc.contains(a))
            {
                result.add(a.getType() + " - ID : " + a.getID() + " From :" + a.getStartNode().getID() + "To :" + a.getEndNode().getID() );
            }
        }
        result.add("-Arc with different weight-\n");
        for (MatchedArc a : sumOfMatchedNetArc) {
            if(!a.isWehightEqual)
                result.add(a.secondNetArc.getType() + " - ID : " + a.secondNetArc.getID()+ " From :" + a.secondNetArc.getStartNode().getID() + "To :" + a.secondNetArc.getEndNode().getID() + " Weight : "+a.secondNetArc.getWeight());

        }

        return result;
    }

    public ArrayList<String> getSimilarities(){
        ArrayList<String> result = new ArrayList<>();
        for (MatchedVertex mv : matchedVertices) {
            if(fPND.nodes.contains(mv.firstNetNode))
            {
                result.add(mv.firstNetNode.getType() + " : " + mv.firstNetNode.getID() + " " + mv.firstNetNode.getName());
            }
        }

        ArrayList<Arc> sumOfFirstNetArc = new ArrayList<>();
        ArrayList<MatchedArc> sumOfMatchedNetArc = new ArrayList<>();
        for (MatchedVertex mv :matchedVertices) {
            for (MatchedArc a: mv.arclist) {
                if(!sumOfFirstNetArc.contains(a.firstNetArc)) {
                    sumOfFirstNetArc.add(a.firstNetArc);
                    sumOfMatchedNetArc.add(a);
                }
            }
        }

        for (MatchedArc a : sumOfMatchedNetArc) {
            if(a.isWehightEqual)
                result.add(a.firstNetArc.getType() + " : " + a.firstNetArc.getID()+"/"+a.secondNetArc.getID() + " From :" + a.firstNetArc.getStartNode().getID() + "To :" + a.firstNetArc.getEndNode().getID() );

        }

        return result;
    }

    public class MatchedVertex {
        public Node firstNetNode;
        public Node secondNetNode;
        public Boolean isNeighbourhoodTheSame = true;
        public ArrayList<MatchedArc> arclist = new ArrayList<>();

        MatchedVertex(Node n1, Node n2) {
            firstNetNode = n1;
            secondNetNode = n2;
            checkNeighbourhood();

        }

        void checkNeighbourhood() {

            for (int i = 0; i < firstNetNode.getInNodes().size(); i++) {
                boolean found = false;
                for (int j = 0; j < secondNetNode.getInNodes().size(); j++) {
                    if (firstNetNode.getInNodes().get(i).getName().equals(secondNetNode.getInNodes().get(j).getName())) {
                        found = true;
                        ArrayList<Arc> firstNetArc = firstNetNode.getInArcs();//.stream().filter(x -> x.getStartNode().getID() == firstNetNode.getID()).collect(Collectors.toCollection(ArrayList::new));
                        ArrayList<Arc> secondNetArc = secondNetNode.getInArcs();//.stream().filter(x -> x.getStartNode().getID() == secondNetNode.getID()).collect(Collectors.toCollection(ArrayList::new));


                        for (Arc a1 : firstNetArc) {
                            for(Arc a2 : secondNetArc)
                                if(a1.getStartNode().getName().equals(a2.getStartNode().getName()))
                                    arclist.add(new MatchedArc(a1, a2));
                        }
                    }
                }

                if (!found) {
                    isNeighbourhoodTheSame = false;
                }
            }

            for (int i = 0; i < firstNetNode.getOutNodes().size(); i++) {
                boolean found = false;
                for (int j = 0; j < secondNetNode.getOutNodes().size(); j++) {
                    if (firstNetNode.getOutNodes().get(i).getName().equals(secondNetNode.getOutNodes().get(j).getName())) {
                        found = true;
                        ArrayList<Arc> firstNetArc = firstNetNode.getOutArcs();//.stream().filter(x -> x.getEndNode().getID() == firstNetNode.getID()).collect(Collectors.toCollection(ArrayList::new));
                        ArrayList<Arc> secondNetArc = secondNetNode.getOutArcs();//.stream().filter(x -> x.getEndNode().getID() == secondNetNode.getID()).collect(Collectors.toCollection(ArrayList::new));

                        for (Arc a1 : firstNetArc) {
                            for(Arc a2 : secondNetArc)
                                if(a1.getEndNode().getName().equals(a2.getEndNode().getName()))
                                    arclist.add(new MatchedArc(a1, a2));
                        }

                        //if (firstNetArc.size() != 0 || secondNetArc.size() != 0)
                        //arclist.add(new MatchedArc(firstNetArc.get(0), secondNetArc.get(0)));
                    }
                }

                if (!found) {
                    isNeighbourhoodTheSame = false;
                }
            }

            //add arc check

        }
    }

    public class MatchedArc {
        Arc firstNetArc = null;
        Arc secondNetArc = null;
        Boolean isWehightEqual = true;
        int minimalWeight = -1;

        public MatchedArc(Arc a1, Arc a2) {
            firstNetArc = a1;
            secondNetArc = a2;
            if (firstNetArc.getWeight() != secondNetArc.getWeight()) {
                isWehightEqual = false;
                minimalWeight = Math.min(firstNetArc.getWeight(), secondNetArc.getWeight());
            }
        }
    }
}
