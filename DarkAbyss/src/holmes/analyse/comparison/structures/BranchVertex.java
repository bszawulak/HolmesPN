package holmes.analyse.comparison.structures;

import holmes.petrinet.elements.Node;
import holmes.petrinet.elements.PetriNetElement;

import java.util.ArrayList;

public class BranchVertex {
    Node branchVertex ;
    //ArrayList<Node> endpoints ;
    public ArrayList<Node> inEndpoints;
    public ArrayList<Node> outEndpoints;

    public BranchVertex(Node bv, ArrayList<Node> ei, ArrayList<Node> eo)
    {
        branchVertex = bv;
        //endpoints = e;
        inEndpoints = ei;
        outEndpoints = eo;
    }

    public void addInEndpoint(Node n){
        inEndpoints.add(n);
    }



    public long getNumberOfInTransitions(){return inEndpoints.stream().filter(x->x.getType().equals(PetriNetElement.PetriNetElementType.TRANSITION)).count();}

    public long getNumberOfOutTransitions(){return outEndpoints.stream().filter(x->x.getType().equals(PetriNetElement.PetriNetElementType.TRANSITION)).count();}

    public long getNumberOfInPlace(){return inEndpoints.stream().filter(x->x.getType().equals(PetriNetElement.PetriNetElementType.PLACE)).count();}

    public long getNumberOfOutPlace(){return outEndpoints.stream().filter(x->x.getType().equals(PetriNetElement.PetriNetElementType.PLACE)).count();}

    public void addOutEndpoint(Node n){
        outEndpoints.add(n);
    }

    public int getDegreeOfBV(){
        return branchVertex.getOutInArcs().size();
    }

    public int getInDegreeOfBV(){
        return branchVertex.getInArcs().size();
    }

    public int getOutDegreeOfBV(){
        return branchVertex.getOutArcs().size();
    }

    public PetriNetElement.PetriNetElementType getTypeOfBV(){
        return branchVertex.getType();
    }


    public int getDegreeOfInEndpoint(int index){
        return inEndpoints.get(index).getOutInArcs().size();
    }

    public int getInDegreeOfInEndpoint(int index){
        return inEndpoints.get(index).getInArcs().size();
    }

    public int getOutDegreeOfInEndpoint(int index){
        return inEndpoints.get(index).getOutArcs().size();
    }

    public PetriNetElement.PetriNetElementType getTypeOfInEndpoint(int index){
        return inEndpoints.get(index).getType();
    }

    public int getDegreeOfOutEndpoint(int index){
        return outEndpoints.get(index).getOutInArcs().size();
    }

    public int getInDegreeOfOutEndpoint(int index){
        return outEndpoints.get(index).getInArcs().size();
    }

    public int getOutDegreeOfOutEndpoint(int index){
        return outEndpoints.get(index).getOutArcs().size();
    }

    public PetriNetElement.PetriNetElementType getTypeOfOutEndpoint(int index){
        return outEndpoints.get(index).getType();
    }
}
