package holmes.analyse.comparison.structures;

import holmes.analyse.comparison.SubnetComparator;

import java.util.ArrayList;

public class GreatCommonSubnet {
    public ArrayList<SubnetComparator.PartialSubnetElements> psel = new ArrayList<>();
    public double gcsValue = -1;
    public int firstNetID = -1;
    public int secondNetID = -1;

    public int firstNetNodeSize = -1;
    public int secondNetNodeSize = -1;

    public GreatCommonSubnet(ArrayList<SubnetComparator.PartialSubnetElements> pse){
        this.psel = pse;
        if(pse.size()>0)
            this.gcsValue = pse.get(0).matchingValueFunction();
        else
            this.gcsValue = 0 ;
    }
}
