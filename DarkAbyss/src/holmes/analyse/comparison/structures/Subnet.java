package holmes.analyse.comparison.structures;

import java.util.ArrayList;
import java.util.HashMap;

public class Subnet<ArrayLisy> {
    ArrayList<SubnetVariant> variants;

    public static class SubnetVariant{
        public int[][] incidenceMatrix;
        public ArrayList<VertexMapping> vertexMaps;
    }

    public static class VertexMapping{
        public HashMap<Integer, Integer> columnMap = new HashMap<>();
        public HashMap<Integer, Integer> rowMap = new HashMap<>();
    }

    public static class BranchesType{
        ArrayList<Integer> types;
        ArrayList<Object> branch;
        int[][] IncidenceMatrixFragment;
        VertexMapping map;
    }
}
