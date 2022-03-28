package holmes.analyse;

import holmes.analyse.comparison.SubnetComparator;
import holmes.analyse.comparison.structures.GreatCommonSubnet;
import holmes.files.io.IOprotocols;
import holmes.petrinet.data.IdGenerator;
import holmes.petrinet.elements.*;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SubnetComparatorModABATest {
    //TODO TESTS

    @Test
    void compare0_00() {
        SubnetComparator sc = new SubnetComparator(creatSubnet0(), creatSubnet0(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(2, gcs.gcsValue);
        assertEquals(2, gcs.psel.get(0).partialNodes.size());
        assertEquals(1, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare0_01() {
        SubnetComparator sc = new SubnetComparator(creatSubnet0(), creatSubnet1(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(1, gcs.gcsValue);
        assertEquals(1, gcs.psel.get(0).partialNodes.size());
        assertEquals(0, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare0_02() {
        SubnetComparator sc = new SubnetComparator(creatSubnet0(), creatSubnet2(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(2, gcs.gcsValue);
        assertEquals(2, gcs.psel.get(0).partialNodes.size());
        assertEquals(1, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare0_03() {
        SubnetComparator sc = new SubnetComparator(creatSubnet0(), creatSubnet3(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(2, gcs.gcsValue);
        assertEquals(2, gcs.psel.get(0).partialNodes.size());
        assertEquals(1, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare0_04() {
        SubnetComparator sc = new SubnetComparator(creatSubnet0(), creatSubnet4(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(2, gcs.gcsValue);
        assertEquals(2, gcs.psel.get(0).partialNodes.size());
        assertEquals(1, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare0_05() {
        SubnetComparator sc = new SubnetComparator(creatSubnet0(), creatSubnet5(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(2, gcs.gcsValue);
        assertEquals(2, gcs.psel.get(0).partialNodes.size());
        assertEquals(1, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare0_06() {
        SubnetComparator sc = new SubnetComparator(creatSubnet0(), creatSubnet6(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(2, gcs.gcsValue);
        assertEquals(2, gcs.psel.get(0).partialNodes.size());
        assertEquals(1, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare0_07() {
        SubnetComparator sc = new SubnetComparator(creatSubnet0(), creatSubnet7(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(2, gcs.gcsValue);
        assertEquals(2, gcs.psel.get(0).partialNodes.size());
        assertEquals(1, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare0_08() {
        SubnetComparator sc = new SubnetComparator(creatSubnet0(), creatSubnet8(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(2, gcs.gcsValue);
        assertEquals(2, gcs.psel.get(0).partialNodes.size());
        assertEquals(1, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare0_09() {
        SubnetComparator sc = new SubnetComparator(creatSubnet0(), creatSubnet9(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(2, gcs.gcsValue);
        assertEquals(2, gcs.psel.get(0).partialNodes.size());
        assertEquals(1, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare0_10() {
        SubnetComparator sc = new SubnetComparator(creatSubnet0(), creatSubnet10(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(2, gcs.gcsValue);
        assertEquals(2, gcs.psel.get(0).partialNodes.size());
        assertEquals(1, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare0_11() {
        SubnetComparator sc = new SubnetComparator(creatSubnet0(), creatSubnet11(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(2, gcs.gcsValue);
        assertEquals(2, gcs.psel.get(0).partialNodes.size());
        assertEquals(1, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare0_12() {
        SubnetComparator sc = new SubnetComparator(creatSubnet0(), creatSubnet12(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(2, gcs.gcsValue);
        assertEquals(2, gcs.psel.get(0).partialNodes.size());
        assertEquals(1, gcs.psel.get(0).partialArcs.size());
        //assertEquals(2, gcs.gcsValue);
        //assertEquals(2, gcs.psel.get(0).partialNodes.size());
        //assertEquals(1, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare0_13() {
        SubnetComparator sc = new SubnetComparator(creatSubnet0(), creatSubnet13(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(2, gcs.gcsValue);
        assertEquals(2, gcs.psel.get(0).partialNodes.size());
        assertEquals(1, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare0_14() {
        SubnetComparator sc = new SubnetComparator(creatSubnet0(), creatSubnet14(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(2, gcs.gcsValue);
        assertEquals(2, gcs.psel.get(0).partialNodes.size());
        assertEquals(1, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare0_15() {
        SubnetComparator sc = new SubnetComparator(creatSubnet0(), creatSubnet15(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(2, gcs.gcsValue);
        assertEquals(2, gcs.psel.get(0).partialNodes.size());
        assertEquals(1, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare0_16() {
        //saveSubnet(creatSubnet16());
        SubnetComparator sc = new SubnetComparator(creatSubnet0(), creatSubnet16(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(2, gcs.gcsValue);
        assertEquals(2, gcs.psel.get(0).partialNodes.size());
        assertEquals(1, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare0_17() {
        SubnetComparator sc = new SubnetComparator(creatSubnet0(), creatSubnet17(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(2, gcs.gcsValue);
        assertEquals(2, gcs.psel.get(0).partialNodes.size());
        assertEquals(1, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare0_18() {
        SubnetComparator sc = new SubnetComparator(creatSubnet0(), creatSubnet18(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(2, gcs.gcsValue);
        assertEquals(2, gcs.psel.get(0).partialNodes.size());
        assertEquals(1, gcs.psel.get(0).partialArcs.size());
    }

    //line 1

    @Test
    void compare1_00() {
        SubnetComparator sc = new SubnetComparator(creatSubnet1(), creatSubnet0(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(1, gcs.gcsValue);
        assertEquals(1, gcs.psel.get(0).partialNodes.size());
        assertEquals(0, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare1_01() {
        SubnetComparator sc = new SubnetComparator(creatSubnet1(), creatSubnet1(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(2, gcs.gcsValue);
        assertEquals(2, gcs.psel.get(0).partialNodes.size());
        assertEquals(1, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare1_02() {
        SubnetComparator sc = new SubnetComparator(creatSubnet1(), creatSubnet2(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(2, gcs.gcsValue);
        assertEquals(2, gcs.psel.get(0).partialNodes.size());
        assertEquals(1, gcs.psel.get(0).partialArcs.size());

    }

    @Test
    void compare1_03() {
        SubnetComparator sc = new SubnetComparator(creatSubnet1(), creatSubnet3(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(2, gcs.gcsValue);
        assertEquals(2, gcs.psel.get(0).partialNodes.size());
        assertEquals(1, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare1_04() {
        SubnetComparator sc = new SubnetComparator(creatSubnet1(), creatSubnet4(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(2, gcs.gcsValue);
        assertEquals(2, gcs.psel.get(0).partialNodes.size());
        assertEquals(1, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare1_05() {
        SubnetComparator sc = new SubnetComparator(creatSubnet1(), creatSubnet5(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(2, gcs.gcsValue);
        assertEquals(2, gcs.psel.get(0).partialNodes.size());
        assertEquals(1, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare1_06() {
        SubnetComparator sc = new SubnetComparator(creatSubnet1(), creatSubnet6(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(2, gcs.gcsValue);
        assertEquals(2, gcs.psel.get(0).partialNodes.size());
        assertEquals(1, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare1_07() {
        SubnetComparator sc = new SubnetComparator(creatSubnet1(), creatSubnet7(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(2, gcs.gcsValue);
        assertEquals(2, gcs.psel.get(0).partialNodes.size());
        assertEquals(1, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare1_08() {
        SubnetComparator sc = new SubnetComparator(creatSubnet1(), creatSubnet8(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(2, gcs.gcsValue);
        assertEquals(2, gcs.psel.get(0).partialNodes.size());
        assertEquals(1, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare1_09() {
        SubnetComparator sc = new SubnetComparator(creatSubnet1(), creatSubnet9(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(2, gcs.gcsValue);
        assertEquals(2, gcs.psel.get(0).partialNodes.size());
        assertEquals(1, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare1_10() {
        SubnetComparator sc = new SubnetComparator(creatSubnet1(), creatSubnet10(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(2, gcs.gcsValue);
        assertEquals(2, gcs.psel.get(0).partialNodes.size());
        assertEquals(1, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare1_11() {
        SubnetComparator sc = new SubnetComparator(creatSubnet1(), creatSubnet11(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(2, gcs.gcsValue);
        assertEquals(2, gcs.psel.get(0).partialNodes.size());
        assertEquals(1, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare1_12() {
        SubnetComparator sc = new SubnetComparator(creatSubnet1(), creatSubnet12(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(2, gcs.gcsValue);
        assertEquals(2, gcs.psel.get(0).partialNodes.size());
        assertEquals(1, gcs.psel.get(0).partialArcs.size());
        //assertEquals(2, gcs.gcsValue);
        //assertEquals(2, gcs.psel.get(0).partialNodes.size());
        //assertEquals(1, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare1_13() {
        SubnetComparator sc = new SubnetComparator(creatSubnet1(), creatSubnet13(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(2, gcs.gcsValue);
        assertEquals(2, gcs.psel.get(0).partialNodes.size());
        assertEquals(1, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare1_14() {
        SubnetComparator sc = new SubnetComparator(creatSubnet1(), creatSubnet14(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(2, gcs.gcsValue);
        assertEquals(2, gcs.psel.get(0).partialNodes.size());
        assertEquals(1, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare1_15() {
        SubnetComparator sc = new SubnetComparator(creatSubnet1(), creatSubnet15(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(2, gcs.gcsValue);
        assertEquals(2, gcs.psel.get(0).partialNodes.size());
        assertEquals(1, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare1_16() {
        SubnetComparator sc = new SubnetComparator(creatSubnet1(), creatSubnet16(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(2, gcs.gcsValue);
        assertEquals(2, gcs.psel.get(0).partialNodes.size());
        assertEquals(1, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare1_17() {
        SubnetComparator sc = new SubnetComparator(creatSubnet1(), creatSubnet17(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(2, gcs.gcsValue);
        assertEquals(2, gcs.psel.get(0).partialNodes.size());
        assertEquals(1, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare1_18() {
        SubnetComparator sc = new SubnetComparator(creatSubnet1(), creatSubnet18(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(2, gcs.gcsValue);
        assertEquals(2, gcs.psel.get(0).partialNodes.size());
        assertEquals(1, gcs.psel.get(0).partialArcs.size());
    }

    //line 2

    @Test
    void compare2_00() {
        SubnetComparator sc = new SubnetComparator(creatSubnet2(), creatSubnet0(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(2, gcs.gcsValue);
        assertEquals(2, gcs.psel.get(0).partialNodes.size());
        assertEquals(1, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare2_01() {
        SubnetComparator sc = new SubnetComparator(creatSubnet2(), creatSubnet1(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(2, gcs.gcsValue);
        assertEquals(2, gcs.psel.get(0).partialNodes.size());
        assertEquals(1, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare2_02() {
        SubnetComparator sc = new SubnetComparator(creatSubnet2(), creatSubnet2(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(4, gcs.gcsValue);
        assertEquals(4, gcs.psel.get(0).partialNodes.size());
        assertEquals(3, gcs.psel.get(0).partialArcs.size());

    }

    @Test
    void compare2_03() {
        SubnetComparator sc = new SubnetComparator(creatSubnet2(), creatSubnet3(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(4, gcs.gcsValue);
        assertEquals(4, gcs.psel.get(0).partialNodes.size());
        assertEquals(3, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare2_04() {
        SubnetComparator sc = new SubnetComparator(creatSubnet2(), creatSubnet4(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(4, gcs.gcsValue);
        assertEquals(4, gcs.psel.get(0).partialNodes.size());
        assertEquals(3, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare2_05() {
        SubnetComparator sc = new SubnetComparator(creatSubnet2(), creatSubnet5(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(3, gcs.gcsValue);
        assertEquals(3, gcs.psel.get(0).partialNodes.size());
        assertEquals(2, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare2_06() {
        SubnetComparator sc = new SubnetComparator(creatSubnet2(), creatSubnet6(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(4, gcs.gcsValue);
        assertEquals(4, gcs.psel.get(0).partialNodes.size());
        assertEquals(3, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare2_07() {
        SubnetComparator sc = new SubnetComparator(creatSubnet2(), creatSubnet7(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(4, gcs.gcsValue);
        assertEquals(4, gcs.psel.get(0).partialNodes.size());
        assertEquals(3, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare2_08() {
        SubnetComparator sc = new SubnetComparator(creatSubnet2(), creatSubnet8(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(4, gcs.gcsValue);
        assertEquals(4, gcs.psel.get(0).partialNodes.size());
        assertEquals(3, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare2_09() {
        SubnetComparator sc = new SubnetComparator(creatSubnet2(), creatSubnet9(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(4, gcs.gcsValue);
        assertEquals(4, gcs.psel.get(0).partialNodes.size());
        assertEquals(3, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare2_10() {
        SubnetComparator sc = new SubnetComparator(creatSubnet2(), creatSubnet10(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(4, gcs.gcsValue);
        assertEquals(4, gcs.psel.get(0).partialNodes.size());
        assertEquals(3, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare2_11() {
        SubnetComparator sc = new SubnetComparator(creatSubnet2(), creatSubnet11(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(4, gcs.gcsValue);
        assertEquals(4, gcs.psel.get(0).partialNodes.size());
        assertEquals(3, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare2_12() {
        SubnetComparator sc = new SubnetComparator(creatSubnet2(), creatSubnet12(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(4, gcs.gcsValue);
        assertEquals(4, gcs.psel.get(0).partialNodes.size());
        assertEquals(3, gcs.psel.get(0).partialArcs.size());
        //assertEquals(4, gcs.gcsValue);
        //assertEquals(4, gcs.psel.get(0).partialNodes.size());
        //assertEquals(3, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare2_13() {
        SubnetComparator sc = new SubnetComparator(creatSubnet2(), creatSubnet13(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(4, gcs.gcsValue);
        assertEquals(4, gcs.psel.get(0).partialNodes.size());
        assertEquals(3, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare2_14() {
        SubnetComparator sc = new SubnetComparator(creatSubnet2(), creatSubnet14(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(4, gcs.gcsValue);
        assertEquals(4, gcs.psel.get(0).partialNodes.size());
        assertEquals(3, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare2_15() {
        SubnetComparator sc = new SubnetComparator(creatSubnet2(), creatSubnet15(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(4, gcs.gcsValue);
        assertEquals(4, gcs.psel.get(0).partialNodes.size());
        assertEquals(3, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare2_16() {
        SubnetComparator sc = new SubnetComparator(creatSubnet2(), creatSubnet16(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(4, gcs.gcsValue);
        assertEquals(4, gcs.psel.get(0).partialNodes.size());
        assertEquals(3, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare2_17() {
        //TODO NOT SUDER IF 4 ot 3
        SubnetComparator sc = new SubnetComparator(creatSubnet2(), creatSubnet17(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(4, gcs.gcsValue);
        assertEquals(4, gcs.psel.get(0).partialNodes.size());
        assertEquals(3, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare2_18() {
        SubnetComparator sc = new SubnetComparator(creatSubnet2(), creatSubnet18(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(4, gcs.gcsValue);
        assertEquals(4, gcs.psel.get(0).partialNodes.size());
        assertEquals(3, gcs.psel.get(0).partialArcs.size());
    }

    //line 3

    @Test
    void compare3_00() {
        SubnetComparator sc = new SubnetComparator(creatSubnet3(), creatSubnet0(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(2, gcs.gcsValue);
        assertEquals(2, gcs.psel.get(0).partialNodes.size());
        assertEquals(1, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare3_01() {
        SubnetComparator sc = new SubnetComparator(creatSubnet3(), creatSubnet1(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(2, gcs.gcsValue);
        assertEquals(2, gcs.psel.get(0).partialNodes.size());
        assertEquals(1, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare3_02() {
        SubnetComparator sc = new SubnetComparator(creatSubnet3(), creatSubnet2(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(4, gcs.gcsValue);
        assertEquals(4, gcs.psel.get(0).partialNodes.size());
        assertEquals(3, gcs.psel.get(0).partialArcs.size());

    }

    @Test
    void compare3_03() {
        SubnetComparator sc = new SubnetComparator(creatSubnet3(), creatSubnet3(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(5, gcs.gcsValue);
        assertEquals(5, gcs.psel.get(0).partialNodes.size());
        assertEquals(4, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare3_04() {
        SubnetComparator sc = new SubnetComparator(creatSubnet3(), creatSubnet4(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(4, gcs.gcsValue);
        assertEquals(4, gcs.psel.get(0).partialNodes.size());
        assertEquals(3, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare3_05() {
        SubnetComparator sc = new SubnetComparator(creatSubnet3(), creatSubnet5(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(3, gcs.gcsValue);
        assertEquals(3, gcs.psel.get(0).partialNodes.size());
        assertEquals(2, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare3_06() {
        SubnetComparator sc = new SubnetComparator(creatSubnet3(), creatSubnet6(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(4, gcs.gcsValue);
        assertEquals(4, gcs.psel.get(0).partialNodes.size());
        assertEquals(3, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare3_07() {
        SubnetComparator sc = new SubnetComparator(creatSubnet3(), creatSubnet7(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(4, gcs.gcsValue);
        assertEquals(4, gcs.psel.get(0).partialNodes.size());
        assertEquals(3, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare3_08() {
        SubnetComparator sc = new SubnetComparator(creatSubnet3(), creatSubnet8(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(5, gcs.gcsValue);
        assertEquals(5, gcs.psel.get(0).partialNodes.size());
        assertEquals(4, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare3_09() {
        SubnetComparator sc = new SubnetComparator(creatSubnet3(), creatSubnet9(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(5, gcs.gcsValue);
        assertEquals(5, gcs.psel.get(0).partialNodes.size());
        assertEquals(4, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare3_10() {
        SubnetComparator sc = new SubnetComparator(creatSubnet3(), creatSubnet10(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        //saveSubnet(gcs);
        assertEquals(4, gcs.gcsValue);
        assertEquals(4, gcs.psel.get(0).partialNodes.size());
        assertEquals(3, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare3_11() {
        SubnetComparator sc = new SubnetComparator(creatSubnet3(), creatSubnet11(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(5, gcs.gcsValue);
        assertEquals(5, gcs.psel.get(0).partialNodes.size());
        assertEquals(4, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare3_12() {
        SubnetComparator sc = new SubnetComparator(creatSubnet3(), creatSubnet12(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(4, gcs.gcsValue);
        assertEquals(4, gcs.psel.get(0).partialNodes.size());
        assertEquals(3, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare3_13() {
        SubnetComparator sc = new SubnetComparator(creatSubnet3(), creatSubnet13(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(5, gcs.gcsValue);
        assertEquals(5, gcs.psel.get(0).partialNodes.size());
        assertEquals(4, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare3_14() {
        SubnetComparator sc = new SubnetComparator(creatSubnet3(), creatSubnet14(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(5, gcs.gcsValue);
        assertEquals(5, gcs.psel.get(0).partialNodes.size());
        assertEquals(4, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare3_15() {
        SubnetComparator sc = new SubnetComparator(creatSubnet3(), creatSubnet15(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(4, gcs.gcsValue);
        assertEquals(4, gcs.psel.get(0).partialNodes.size());
        assertEquals(3, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare3_16() {
        SubnetComparator sc = new SubnetComparator(creatSubnet3(), creatSubnet16(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(4, gcs.gcsValue);
        assertEquals(4, gcs.psel.get(0).partialNodes.size());
        assertEquals(3, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare3_17() {
        SubnetComparator sc = new SubnetComparator(creatSubnet3(), creatSubnet17(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(5, gcs.gcsValue);
        assertEquals(5, gcs.psel.get(0).partialNodes.size());
        assertEquals(4, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare3_18() {
        SubnetComparator sc = new SubnetComparator(creatSubnet3(), creatSubnet18(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(5, gcs.gcsValue);
        assertEquals(5, gcs.psel.get(0).partialNodes.size());
        assertEquals(4, gcs.psel.get(0).partialArcs.size());
    }

    //line 4

    @Test
    void compare4_00() {
        SubnetComparator sc = new SubnetComparator(creatSubnet4(), creatSubnet0(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(2, gcs.gcsValue);
        assertEquals(2, gcs.psel.get(0).partialNodes.size());
        assertEquals(1, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare4_01() {
        SubnetComparator sc = new SubnetComparator(creatSubnet4(), creatSubnet1(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(2, gcs.gcsValue);
        assertEquals(2, gcs.psel.get(0).partialNodes.size());
        assertEquals(1, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare4_02() {
        SubnetComparator sc = new SubnetComparator(creatSubnet4(), creatSubnet2(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(4, gcs.gcsValue);
        assertEquals(4, gcs.psel.get(0).partialNodes.size());
        assertEquals(3, gcs.psel.get(0).partialArcs.size());

    }

    @Test
    void compare4_03() {
        SubnetComparator sc = new SubnetComparator(creatSubnet4(), creatSubnet3(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(4, gcs.gcsValue);
        assertEquals(4, gcs.psel.get(0).partialNodes.size());
        assertEquals(3, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare4_04() {
        SubnetComparator sc = new SubnetComparator(creatSubnet4(), creatSubnet4(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(5, gcs.gcsValue);
        assertEquals(5, gcs.psel.get(0).partialNodes.size());
        assertEquals(4, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare4_05() {
        SubnetComparator sc = new SubnetComparator(creatSubnet4(), creatSubnet5(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(0, gcs.gcsValue);
        assertEquals(0, gcs.psel.size());
    }

    @Test
    void compare4_06() {
        SubnetComparator sc = new SubnetComparator(creatSubnet4(), creatSubnet6(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(0, gcs.gcsValue);
        assertEquals(0, gcs.psel.size());
    }

    @Test
    void compare4_07() {
        SubnetComparator sc = new SubnetComparator(creatSubnet4(), creatSubnet7(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(0, gcs.gcsValue);
        assertEquals(0, gcs.psel.size());
    }

    @Test
    void compare4_08() {
        SubnetComparator sc = new SubnetComparator(creatSubnet4(), creatSubnet8(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(0, gcs.gcsValue);
        assertEquals(0, gcs.psel.size());
    }

    @Test
    void compare4_09() {
        SubnetComparator sc = new SubnetComparator(creatSubnet4(), creatSubnet9(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(0, gcs.gcsValue);
        assertEquals(0, gcs.psel.size());
    }

    @Test
    void compare4_10() {
        SubnetComparator sc = new SubnetComparator(creatSubnet4(), creatSubnet10(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(0, gcs.gcsValue);
        assertEquals(0, gcs.psel.size());
    }

    @Test
    void compare4_11() {
        SubnetComparator sc = new SubnetComparator(creatSubnet4(), creatSubnet11(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(5, gcs.gcsValue);
        assertEquals(5, gcs.psel.get(0).partialNodes.size());
        assertEquals(4, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare4_12() {
        SubnetComparator sc = new SubnetComparator(creatSubnet4(), creatSubnet12(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(4, gcs.gcsValue);
        assertEquals(4, gcs.psel.get(0).partialNodes.size());
        assertEquals(3, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare4_13() {
        SubnetComparator sc = new SubnetComparator(creatSubnet4(), creatSubnet13(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(0, gcs.gcsValue);
        assertEquals(0, gcs.psel.size());
    }

    @Test
    void compare4_14() {
        SubnetComparator sc = new SubnetComparator(creatSubnet4(), creatSubnet14(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(0, gcs.gcsValue);
        assertEquals(0, gcs.psel.size());
    }

    @Test
    void compare4_15() {
        SubnetComparator sc = new SubnetComparator(creatSubnet4(), creatSubnet15(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(0, gcs.gcsValue);
        assertEquals(0, gcs.psel.size());
    }

    @Test
    void compare4_16() {
        SubnetComparator sc = new SubnetComparator(creatSubnet4(), creatSubnet16(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(0, gcs.gcsValue);
        assertEquals(0, gcs.psel.size());
    }

    @Test
    void compare4_17() {
        SubnetComparator sc = new SubnetComparator(creatSubnet4(), creatSubnet17(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(0, gcs.gcsValue);
        assertEquals(0, gcs.psel.size());
    }

    @Test
    void compare4_18() {
        SubnetComparator sc = new SubnetComparator(creatSubnet4(), creatSubnet18(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(0, gcs.gcsValue);
        assertEquals(0, gcs.psel.size());
    }

    //line 5

    @Test
    void compare5_00() {
        SubnetComparator sc = new SubnetComparator(creatSubnet5(), creatSubnet0(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(2, gcs.gcsValue);
        assertEquals(2, gcs.psel.get(0).partialNodes.size());
        assertEquals(1, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare5_01() {
        SubnetComparator sc = new SubnetComparator(creatSubnet5(), creatSubnet1(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(2, gcs.gcsValue);
        assertEquals(2, gcs.psel.get(0).partialNodes.size());
        assertEquals(1, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare5_02() {
        SubnetComparator sc = new SubnetComparator(creatSubnet5(), creatSubnet2(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(3, gcs.gcsValue);
        assertEquals(3, gcs.psel.get(0).partialNodes.size());
        assertEquals(2, gcs.psel.get(0).partialArcs.size());

    }

    @Test
    void compare5_03() {
        SubnetComparator sc = new SubnetComparator(creatSubnet5(), creatSubnet3(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(3, gcs.gcsValue);
        assertEquals(3, gcs.psel.get(0).partialNodes.size());
        assertEquals(2, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare5_04() {
        SubnetComparator sc = new SubnetComparator(creatSubnet5(), creatSubnet4(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(0, gcs.gcsValue);
        assertEquals(0, gcs.psel.size());
    }

    @Test
    void compare5_05() {
        SubnetComparator sc = new SubnetComparator(creatSubnet5(), creatSubnet5(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(7, gcs.gcsValue);
        assertEquals(7, gcs.psel.get(0).partialNodes.size());
        assertEquals(6, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare5_06() {
        SubnetComparator sc = new SubnetComparator(creatSubnet5(), creatSubnet6(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(7, gcs.gcsValue);
        assertEquals(7, gcs.psel.get(0).partialNodes.size());
        assertEquals(6, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare5_07() {
        SubnetComparator sc = new SubnetComparator(creatSubnet5(), creatSubnet7(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(7, gcs.gcsValue);
        assertEquals(7, gcs.psel.get(0).partialNodes.size());
        assertEquals(6, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare5_08() {
        SubnetComparator sc = new SubnetComparator(creatSubnet5(), creatSubnet8(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        //saveSubnet(gcs);
        assertEquals(6, gcs.gcsValue);
        assertEquals(6, gcs.psel.get(0).partialNodes.size());
        assertEquals(5, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare5_09() {
        SubnetComparator sc = new SubnetComparator(creatSubnet5(), creatSubnet9(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(6, gcs.gcsValue);
        assertEquals(6, gcs.psel.get(0).partialNodes.size());
        assertEquals(5, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare5_10() {
        //TODO ZA DUŻO ZNAJDUJE< WCHODZI W LOOPA
        SubnetComparator sc = new SubnetComparator(creatSubnet5(), creatSubnet10(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(7, gcs.gcsValue);
        assertEquals(7, gcs.psel.get(0).partialNodes.size());
        assertEquals(6, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare5_11() {
        SubnetComparator sc = new SubnetComparator(creatSubnet5(), creatSubnet11(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(0, gcs.gcsValue);
        assertEquals(0, gcs.psel.size());
    }

    @Test
    void compare5_12() {
        SubnetComparator sc = new SubnetComparator(creatSubnet5(), creatSubnet12(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(3, gcs.gcsValue);
        assertEquals(3, gcs.psel.get(0).partialNodes.size());
        assertEquals(2, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare5_13() {
        SubnetComparator sc = new SubnetComparator(creatSubnet5(), creatSubnet13(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(6, gcs.gcsValue);
        assertEquals(6, gcs.psel.get(0).partialNodes.size());
        assertEquals(5, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare5_14() {
        SubnetComparator sc = new SubnetComparator(creatSubnet5(), creatSubnet14(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(7, gcs.gcsValue);
        assertEquals(7, gcs.psel.get(0).partialNodes.size());
        assertEquals(6, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare5_15() {
        SubnetComparator sc = new SubnetComparator(creatSubnet5(), creatSubnet15(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(7, gcs.gcsValue);
        assertEquals(7, gcs.psel.get(0).partialNodes.size());
        assertEquals(6, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare5_16() {
        SubnetComparator sc = new SubnetComparator(creatSubnet5(), creatSubnet16(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        //saveSubnet(gcs);
        assertEquals(7, gcs.gcsValue);
        assertEquals(7, gcs.psel.get(0).partialNodes.size());
        assertEquals(6, gcs.psel.get(0).partialArcs.size());
    }

    //@Test
    void checkNet(){
        SubnetCalculator.SubNet  sn = creatSubnet18();;
        saveSubnet(sn);
    }

    @Test
    void compare5_17() {
        SubnetComparator sc = new SubnetComparator(creatSubnet5(), creatSubnet17(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(7, gcs.gcsValue);
        assertEquals(7, gcs.psel.get(0).partialNodes.size());
        assertEquals(6, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare5_18() {
        SubnetComparator sc = new SubnetComparator(creatSubnet5(), creatSubnet18(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(5, gcs.gcsValue);
        assertEquals(5, gcs.psel.get(0).partialNodes.size());
        assertEquals(4, gcs.psel.get(0).partialArcs.size());
    }

    //line 6

    @Test
    void compare6_00() {
        SubnetComparator sc = new SubnetComparator(creatSubnet6(), creatSubnet0(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(2, gcs.gcsValue);
        assertEquals(2, gcs.psel.get(0).partialNodes.size());
        assertEquals(1, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare6_01() {
        SubnetComparator sc = new SubnetComparator(creatSubnet6(), creatSubnet1(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(2, gcs.gcsValue);
        assertEquals(2, gcs.psel.get(0).partialNodes.size());
        assertEquals(1, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare6_02() {
        SubnetComparator sc = new SubnetComparator(creatSubnet6(), creatSubnet2(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(4, gcs.gcsValue);
        assertEquals(4, gcs.psel.get(0).partialNodes.size());
        assertEquals(3, gcs.psel.get(0).partialArcs.size());

    }

    @Test
    void compare6_03() {
        SubnetComparator sc = new SubnetComparator(creatSubnet6(), creatSubnet3(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(4, gcs.gcsValue);
        assertEquals(4, gcs.psel.get(0).partialNodes.size());
        assertEquals(3, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare6_04() {
        SubnetComparator sc = new SubnetComparator(creatSubnet6(), creatSubnet4(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(0, gcs.gcsValue);
        assertEquals(0, gcs.psel.size());
    }

    @Test
    void compare6_05() {
        SubnetComparator sc = new SubnetComparator(creatSubnet6(), creatSubnet5(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(7, gcs.gcsValue);
        assertEquals(7, gcs.psel.get(0).partialNodes.size());
        assertEquals(6, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare6_06() {
        SubnetComparator sc = new SubnetComparator(creatSubnet6(), creatSubnet6(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(10, gcs.gcsValue);
        assertEquals(10, gcs.psel.get(0).partialNodes.size());
        assertEquals(9, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare6_07() {
        SubnetComparator sc = new SubnetComparator(creatSubnet6(), creatSubnet7(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(9, gcs.gcsValue);
        assertEquals(9, gcs.psel.get(0).partialNodes.size());
        assertEquals(8, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare6_08() {
        SubnetComparator sc = new SubnetComparator(creatSubnet6(), creatSubnet8(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(8, gcs.gcsValue);
        assertEquals(8, gcs.psel.get(0).partialNodes.size());
        assertEquals(7, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare6_09() {
        SubnetComparator sc = new SubnetComparator(creatSubnet6(), creatSubnet9(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(8, gcs.gcsValue);
        assertEquals(8, gcs.psel.get(0).partialNodes.size());
        assertEquals(7, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare6_10() {
        SubnetComparator sc = new SubnetComparator(creatSubnet6(), creatSubnet10(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        //saveSubnet(gcs);
        assertEquals(10, gcs.gcsValue);
        assertEquals(10, gcs.psel.get(0).partialNodes.size());
        assertEquals(9, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare6_11() {
        SubnetComparator sc = new SubnetComparator(creatSubnet6(), creatSubnet11(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(0, gcs.gcsValue);
        assertEquals(0, gcs.psel.size());
    }

    @Test
    void compare6_12() {
        SubnetComparator sc = new SubnetComparator(creatSubnet6(), creatSubnet12(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(0, gcs.gcsValue);
        assertEquals(0, gcs.psel.size());
    }

    @Test
    void compare6_13() {
        SubnetComparator sc = new SubnetComparator(creatSubnet6(), creatSubnet13(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(7, gcs.gcsValue);
        assertEquals(7, gcs.psel.get(0).partialNodes.size());
        assertEquals(6, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare6_14() {
        SubnetComparator sc = new SubnetComparator(creatSubnet6(), creatSubnet14(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(9, gcs.gcsValue);
        assertEquals(9, gcs.psel.get(0).partialNodes.size());
        assertEquals(8, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare6_15() {
        SubnetComparator sc = new SubnetComparator(creatSubnet6(), creatSubnet15(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(9, gcs.gcsValue);
        assertEquals(9, gcs.psel.get(0).partialNodes.size());
        assertEquals(8, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare6_16() {
        SubnetComparator sc = new SubnetComparator(creatSubnet6(), creatSubnet16(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(8, gcs.gcsValue);
        assertEquals(8, gcs.psel.get(0).partialNodes.size());
        assertEquals(7, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare6_17() {
        SubnetComparator sc = new SubnetComparator(creatSubnet6(), creatSubnet17(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(10, gcs.gcsValue);
        assertEquals(10, gcs.psel.get(0).partialNodes.size());
        assertEquals(9, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare6_18() {
        SubnetComparator sc = new SubnetComparator(creatSubnet6(), creatSubnet18(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(6, gcs.gcsValue);
        assertEquals(6, gcs.psel.get(0).partialNodes.size());
        assertEquals(5, gcs.psel.get(0).partialArcs.size());
    }

    //linia 7

    @Test
    void compare7_00() {
        SubnetComparator sc = new SubnetComparator(creatSubnet7(), creatSubnet0(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(2, gcs.gcsValue);
        assertEquals(2, gcs.psel.get(0).partialNodes.size());
        assertEquals(1, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare7_01() {
        SubnetComparator sc = new SubnetComparator(creatSubnet7(), creatSubnet1(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(2, gcs.gcsValue);
        assertEquals(2, gcs.psel.get(0).partialNodes.size());
        assertEquals(1, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare7_02() {
        SubnetComparator sc = new SubnetComparator(creatSubnet7(), creatSubnet2(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(4, gcs.gcsValue);
        assertEquals(4, gcs.psel.get(0).partialNodes.size());
        assertEquals(3, gcs.psel.get(0).partialArcs.size());

    }

    @Test
    void compare7_03() {
        SubnetComparator sc = new SubnetComparator(creatSubnet7(), creatSubnet3(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(4, gcs.gcsValue);
        assertEquals(4, gcs.psel.get(0).partialNodes.size());
        assertEquals(3, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare7_04() {
        SubnetComparator sc = new SubnetComparator(creatSubnet7(), creatSubnet4(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(0, gcs.gcsValue);
        assertEquals(0, gcs.psel.size());
    }

    @Test
    void compare7_05() {
        SubnetComparator sc = new SubnetComparator(creatSubnet7(), creatSubnet5(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(7, gcs.gcsValue);
        assertEquals(7, gcs.psel.get(0).partialNodes.size());
        assertEquals(6, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare7_06() {
        SubnetComparator sc = new SubnetComparator(creatSubnet7(), creatSubnet6(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(9, gcs.gcsValue);
        assertEquals(9, gcs.psel.get(0).partialNodes.size());
        assertEquals(8, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare7_07() {
        SubnetComparator sc = new SubnetComparator(creatSubnet7(), creatSubnet7(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        //saveSubnet(gcs);
        assertEquals(15, gcs.gcsValue);
        assertEquals(15, gcs.psel.get(0).partialNodes.size());
        assertEquals(14, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare7_08() {
        SubnetComparator sc = new SubnetComparator(creatSubnet7(), creatSubnet8(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        printElements(gcs);
        assertEquals(8, gcs.gcsValue);
        assertEquals(8, gcs.psel.get(0).partialNodes.size());
        assertEquals(7, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare7_09() {
        SubnetComparator sc = new SubnetComparator(creatSubnet7(), creatSubnet9(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        printElements(gcs);
        assertEquals(8, gcs.gcsValue);
        assertEquals(8, gcs.psel.get(0).partialNodes.size());
        assertEquals(7, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare7_10() {
        SubnetComparator sc = new SubnetComparator(creatSubnet7(), creatSubnet10(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(9, gcs.gcsValue);
        assertEquals(9, gcs.psel.get(0).partialNodes.size());
        assertEquals(8, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare7_11() {
        SubnetComparator sc = new SubnetComparator(creatSubnet7(), creatSubnet11(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(0, gcs.gcsValue);
        assertEquals(0, gcs.psel.size());
    }

    @Test
    void compare7_12() {
        SubnetComparator sc = new SubnetComparator(creatSubnet7(), creatSubnet12(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(3, gcs.gcsValue);
        assertEquals(3, gcs.psel.get(0).partialNodes.size());
        assertEquals(2, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare7_13() {
        SubnetComparator sc = new SubnetComparator(creatSubnet7(), creatSubnet13(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        //saveSubnet(gcs);
        assertEquals(6, gcs.gcsValue);
        assertEquals(6, gcs.psel.get(0).partialNodes.size());
        assertEquals(5, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare7_14() {
        SubnetComparator sc = new SubnetComparator(creatSubnet7(), creatSubnet14(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(9, gcs.gcsValue);
        assertEquals(9, gcs.psel.get(0).partialNodes.size());
        assertEquals(8, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare7_15() {
        SubnetComparator sc = new SubnetComparator(creatSubnet7(), creatSubnet15(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(14, gcs.gcsValue);
        assertEquals(14, gcs.psel.get(0).partialNodes.size());
        assertEquals(13, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare7_16() {
        SubnetComparator sc = new SubnetComparator(creatSubnet7(), creatSubnet16(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        //saveSubnet(gcs);
        assertEquals(12, gcs.gcsValue);
        assertEquals(12, gcs.psel.get(0).partialNodes.size());
        assertEquals(11, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare7_17() {
        SubnetComparator sc = new SubnetComparator(creatSubnet7(), creatSubnet17(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        //saveSubnet(gcs);
        assertEquals(9, gcs.gcsValue);
        assertEquals(9, gcs.psel.get(0).partialNodes.size());
        assertEquals(8, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare7_18() {
        SubnetComparator sc = new SubnetComparator(creatSubnet7(), creatSubnet18(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        printElements(gcs);
        assertEquals(8, gcs.gcsValue);
        assertEquals(8, gcs.psel.get(0).partialNodes.size());
        assertEquals(7, gcs.psel.get(0).partialArcs.size());
    }

    //line 8

    @Test
    void compare8_00() {
        SubnetComparator sc = new SubnetComparator(creatSubnet8(), creatSubnet0(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(2, gcs.gcsValue);
        assertEquals(2, gcs.psel.get(0).partialNodes.size());
        assertEquals(1, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare8_01() {
        SubnetComparator sc = new SubnetComparator(creatSubnet8(), creatSubnet1(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(2, gcs.gcsValue);
        assertEquals(2, gcs.psel.get(0).partialNodes.size());
        assertEquals(1, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare8_02() {
        SubnetComparator sc = new SubnetComparator(creatSubnet8(), creatSubnet2(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(4, gcs.gcsValue);
        assertEquals(4, gcs.psel.get(0).partialNodes.size());
        assertEquals(3, gcs.psel.get(0).partialArcs.size());

    }

    @Test
    void compare8_03() {
        SubnetComparator sc = new SubnetComparator(creatSubnet8(), creatSubnet3(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(5, gcs.gcsValue);
        assertEquals(5, gcs.psel.get(0).partialNodes.size());
        assertEquals(4, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare8_04() {
        SubnetComparator sc = new SubnetComparator(creatSubnet8(), creatSubnet4(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(0, gcs.gcsValue);
        assertEquals(0, gcs.psel.size());
    }

    @Test
    void compare8_05() {
        SubnetComparator sc = new SubnetComparator(creatSubnet8(), creatSubnet5(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(6, gcs.gcsValue);
        assertEquals(6, gcs.psel.get(0).partialNodes.size());
        assertEquals(5, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare8_06() {
        SubnetComparator sc = new SubnetComparator(creatSubnet8(), creatSubnet6(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(8, gcs.gcsValue);
        assertEquals(8, gcs.psel.get(0).partialNodes.size());
        assertEquals(7, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare8_07() {
        SubnetComparator sc = new SubnetComparator(creatSubnet8(), creatSubnet7(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        printElements(gcs);
        assertEquals(8, gcs.gcsValue);
        assertEquals(8, gcs.psel.get(0).partialNodes.size());
        assertEquals(7, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare8_08() {
        SubnetComparator sc = new SubnetComparator(creatSubnet8(), creatSubnet8(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(10, gcs.gcsValue);
        assertEquals(10, gcs.psel.get(0).partialNodes.size());
        assertEquals(10, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare8_09() {
        SubnetComparator sc = new SubnetComparator(creatSubnet8(), creatSubnet9(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        printElements(gcs);
        //saveSubnet(gcs);
        assertEquals(10, gcs.gcsValue);
        assertEquals(10, gcs.psel.get(0).partialNodes.size());
        assertEquals(9, gcs.psel.get(0).partialArcs.size());
    }

    private void printElements(GreatCommonSubnet gcs) {
        System.out.println("---whole subnet---:");
        System.out.println("Node:");
        for (Node n : gcs.psel.get(0).partialNodes) {
            System.out.println(n.getName());
        }
        System.out.println("Arcs:");
        for (Arc a : gcs.psel.get(0).partialArcs) {
            System.out.println(a.getStartNode().getName() + "-->" + a.getEndNode().getName());
        }
    }

    @Test
    void compare8_10() {
        SubnetComparator sc = new SubnetComparator(creatSubnet8(), creatSubnet10(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        printElements(gcs);
        //saveSubnet(gcs);
        assertEquals(8, gcs.gcsValue);
        assertEquals(8, gcs.psel.get(0).partialNodes.size());
        assertEquals(7, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare8_11() {
        SubnetComparator sc = new SubnetComparator(creatSubnet8(), creatSubnet11(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(0, gcs.gcsValue);
        assertEquals(0, gcs.psel.size());
    }

    @Test
    void compare8_12() {
        SubnetComparator sc = new SubnetComparator(creatSubnet8(), creatSubnet12(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(4, gcs.gcsValue);
        assertEquals(4, gcs.psel.get(0).partialNodes.size());
        assertEquals(3, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare8_13() {
        SubnetComparator sc = new SubnetComparator(creatSubnet8(), creatSubnet13(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(7, gcs.gcsValue);
        assertEquals(7, gcs.psel.get(0).partialNodes.size());
        assertEquals(6, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare8_14() {
        SubnetComparator sc = new SubnetComparator(creatSubnet8(), creatSubnet14(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(8, gcs.gcsValue);
        assertEquals(8, gcs.psel.get(0).partialNodes.size());
        assertEquals(7, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare8_15() {
        SubnetComparator sc = new SubnetComparator(creatSubnet8(), creatSubnet15(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(8, gcs.gcsValue);
        assertEquals(8, gcs.psel.get(0).partialNodes.size());
        assertEquals(7, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare8_16() {
        SubnetComparator sc = new SubnetComparator(creatSubnet8(), creatSubnet16(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(7, gcs.gcsValue);
        assertEquals(7, gcs.psel.get(0).partialNodes.size());
        assertEquals(6, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare8_17() {
        SubnetComparator sc = new SubnetComparator(creatSubnet8(), creatSubnet17(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        //saveSubnet(gcs);
        assertEquals(9, gcs.gcsValue);
        assertEquals(9, gcs.psel.get(0).partialNodes.size());
        assertEquals(8, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare8_18() {
        SubnetComparator sc = new SubnetComparator(creatSubnet8(), creatSubnet18(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        //saveSubnet(gcs);
        assertEquals(10, gcs.gcsValue);
        assertEquals(10, gcs.psel.get(0).partialNodes.size());
        assertEquals(9, gcs.psel.get(0).partialArcs.size());
    }

    //line 9

    @Test
    void compare9_00() {
        SubnetComparator sc = new SubnetComparator(creatSubnet9(), creatSubnet0(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(2, gcs.gcsValue);
        assertEquals(2, gcs.psel.get(0).partialNodes.size());
        assertEquals(1, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare9_01() {
        SubnetComparator sc = new SubnetComparator(creatSubnet9(), creatSubnet1(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(2, gcs.gcsValue);
        assertEquals(2, gcs.psel.get(0).partialNodes.size());
        assertEquals(1, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare9_02() {
        SubnetComparator sc = new SubnetComparator(creatSubnet9(), creatSubnet2(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(4, gcs.gcsValue);
        assertEquals(4, gcs.psel.get(0).partialNodes.size());
        assertEquals(3, gcs.psel.get(0).partialArcs.size());

    }

    @Test
    void compare9_03() {
        SubnetComparator sc = new SubnetComparator(creatSubnet9(), creatSubnet3(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(5, gcs.gcsValue);
        assertEquals(5, gcs.psel.get(0).partialNodes.size());
        assertEquals(4, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare9_04() {
        SubnetComparator sc = new SubnetComparator(creatSubnet9(), creatSubnet4(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(0, gcs.gcsValue);
        assertEquals(0, gcs.psel.size());
    }

    @Test
    void compare9_05() {
        SubnetComparator sc = new SubnetComparator(creatSubnet9(), creatSubnet5(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(6, gcs.gcsValue);
        assertEquals(6, gcs.psel.get(0).partialNodes.size());
        assertEquals(5, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare9_06() {
        SubnetComparator sc = new SubnetComparator(creatSubnet9(), creatSubnet6(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(8, gcs.gcsValue);
        assertEquals(8, gcs.psel.get(0).partialNodes.size());
        assertEquals(7, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare9_07() {
        SubnetComparator sc = new SubnetComparator(creatSubnet9(), creatSubnet7(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(8, gcs.gcsValue);
        assertEquals(8, gcs.psel.get(0).partialNodes.size());
        assertEquals(7, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare9_08() {
        SubnetComparator sc = new SubnetComparator(creatSubnet9(), creatSubnet8(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        printElements(gcs);
        //saveSubnet(gcs);
        assertEquals(10, gcs.gcsValue);
        assertEquals(10, gcs.psel.get(0).partialNodes.size());
        assertEquals(9, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare9_09() {
        SubnetComparator sc = new SubnetComparator(creatSubnet9(), creatSubnet9(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        //saveSubnet(gcs);
        assertEquals(14, gcs.gcsValue);
        assertEquals(14, gcs.psel.get(0).partialNodes.size());
        assertEquals(14, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare9_10() {
        SubnetComparator sc = new SubnetComparator(creatSubnet9(), creatSubnet10(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(8, gcs.gcsValue);
        assertEquals(8, gcs.psel.get(0).partialNodes.size());
        assertEquals(7, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare9_11() {
        SubnetComparator sc = new SubnetComparator(creatSubnet9(), creatSubnet11(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(0, gcs.gcsValue);
        assertEquals(0, gcs.psel.size());
    }

    @Test
    void compare9_12() {
        SubnetComparator sc = new SubnetComparator(creatSubnet9(), creatSubnet12(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(4, gcs.gcsValue);
        assertEquals(4, gcs.psel.get(0).partialNodes.size());
        assertEquals(3, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare9_13() {
        SubnetComparator sc = new SubnetComparator(creatSubnet9(), creatSubnet13(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        //saveSubnet(gcs);
        assertEquals(7, gcs.gcsValue);
        assertEquals(7, gcs.psel.get(0).partialNodes.size());
        assertEquals(6, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare9_14() {
        SubnetComparator sc = new SubnetComparator(creatSubnet9(), creatSubnet14(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        //saveSubnet(gcs);
        assertEquals(8, gcs.gcsValue);
        assertEquals(8, gcs.psel.get(0).partialNodes.size());
        assertEquals(7, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare9_15() {
        SubnetComparator sc = new SubnetComparator(creatSubnet9(), creatSubnet15(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(8, gcs.gcsValue);
        assertEquals(8, gcs.psel.get(0).partialNodes.size());
        assertEquals(7, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare9_16() {
        SubnetComparator sc = new SubnetComparator(creatSubnet9(), creatSubnet16(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        //Subnet(gcs);
        assertEquals(7, gcs.gcsValue);
        assertEquals(7, gcs.psel.get(0).partialNodes.size());
        assertEquals(6, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare9_17() {
        SubnetComparator sc = new SubnetComparator(creatSubnet9(), creatSubnet17(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(10, gcs.gcsValue);
        assertEquals(10, gcs.psel.get(0).partialNodes.size());
        assertEquals(9, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare9_18() {
        SubnetComparator sc = new SubnetComparator(creatSubnet9(), creatSubnet18(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        //saveSubnet(gcs);
        assertEquals(12, gcs.gcsValue);
        assertEquals(12, gcs.psel.get(0).partialNodes.size());
        assertEquals(11, gcs.psel.get(0).partialArcs.size());
    }

    //line 10

    @Test
    void compare10_00() {
        SubnetComparator sc = new SubnetComparator(creatSubnet10(), creatSubnet0(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(2, gcs.gcsValue);
        assertEquals(2, gcs.psel.get(0).partialNodes.size());
        assertEquals(1, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare10_01() {
        SubnetComparator sc = new SubnetComparator(creatSubnet10(), creatSubnet1(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(2, gcs.gcsValue);
        assertEquals(2, gcs.psel.get(0).partialNodes.size());
        assertEquals(1, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare10_02() {
        SubnetComparator sc = new SubnetComparator(creatSubnet10(), creatSubnet2(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(4, gcs.gcsValue);
        assertEquals(4, gcs.psel.get(0).partialNodes.size());
        assertEquals(3, gcs.psel.get(0).partialArcs.size());

    }

    @Test
    void compare10_03() {
        SubnetComparator sc = new SubnetComparator(creatSubnet10(), creatSubnet3(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(4, gcs.gcsValue);
        assertEquals(4, gcs.psel.get(0).partialNodes.size());
        assertEquals(3, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare10_04() {
        SubnetComparator sc = new SubnetComparator(creatSubnet10(), creatSubnet4(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(0, gcs.gcsValue);
        assertEquals(0, gcs.psel.size());
    }

    @Test
    void compare10_05() {
        SubnetComparator sc = new SubnetComparator(creatSubnet10(), creatSubnet5(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        //saveSubnet(gcs);
        assertEquals(7, gcs.gcsValue);
        assertEquals(7, gcs.psel.get(0).partialNodes.size());
        assertEquals(6, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare10_06() {
        SubnetComparator sc = new SubnetComparator(creatSubnet10(), creatSubnet6(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(10, gcs.gcsValue);
        assertEquals(10, gcs.psel.get(0).partialNodes.size());
        assertEquals(9, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare10_07() {
        SubnetComparator sc = new SubnetComparator(creatSubnet10(), creatSubnet7(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(9, gcs.gcsValue);
        assertEquals(9, gcs.psel.get(0).partialNodes.size());
        assertEquals(8, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare10_08() {
        SubnetComparator sc = new SubnetComparator(creatSubnet10(), creatSubnet8(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        //saveSubnet(gcs);
        assertEquals(8, gcs.gcsValue);
        assertEquals(8, gcs.psel.get(0).partialNodes.size());
        assertEquals(7, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare10_09() {
        SubnetComparator sc = new SubnetComparator(creatSubnet10(), creatSubnet9(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(8, gcs.gcsValue);
        assertEquals(8, gcs.psel.get(0).partialNodes.size());
        assertEquals(7, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare10_10() {
        SubnetComparator sc = new SubnetComparator(creatSubnet10(), creatSubnet10(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        //saveSubnet(gcs);
        assertEquals(10, gcs.gcsValue);
        assertEquals(10, gcs.psel.get(0).partialNodes.size());
        assertEquals(10, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare10_11() {
        SubnetComparator sc = new SubnetComparator(creatSubnet10(), creatSubnet11(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(0, gcs.gcsValue);
        assertEquals(0, gcs.psel.size());
    }

    @Test
    void compare10_12() {
        SubnetComparator sc = new SubnetComparator(creatSubnet10(), creatSubnet12(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(4, gcs.gcsValue);
        assertEquals(4, gcs.psel.get(0).partialNodes.size());
        assertEquals(4, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare10_13() {
        SubnetComparator sc = new SubnetComparator(creatSubnet10(), creatSubnet13(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        //saveSubnet(gcs);
        assertEquals(7, gcs.gcsValue);
        assertEquals(7, gcs.psel.get(0).partialNodes.size());
        assertEquals(6, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare10_14() {
        SubnetComparator sc = new SubnetComparator(creatSubnet10(), creatSubnet14(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(9, gcs.gcsValue);
        assertEquals(9, gcs.psel.get(0).partialNodes.size());
        assertEquals(8, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare10_15() {
        SubnetComparator sc = new SubnetComparator(creatSubnet10(), creatSubnet15(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(9, gcs.gcsValue);
        assertEquals(9, gcs.psel.get(0).partialNodes.size());
        assertEquals(8, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare10_16() {
        SubnetComparator sc = new SubnetComparator(creatSubnet10(), creatSubnet16(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(8, gcs.gcsValue);
        assertEquals(8, gcs.psel.get(0).partialNodes.size());
        assertEquals(8, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare10_17() {
        SubnetComparator sc = new SubnetComparator(creatSubnet10(), creatSubnet17(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        //saveSubnet(gcs);
        assertEquals(10, gcs.gcsValue);
        assertEquals(10, gcs.psel.get(0).partialNodes.size());
        assertEquals(9, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare10_18() {
        SubnetComparator sc = new SubnetComparator(creatSubnet10(), creatSubnet18(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        //saveSubnet(gcs);
        assertEquals(8, gcs.gcsValue);
        assertEquals(8, gcs.psel.get(0).partialNodes.size());
        assertEquals(7, gcs.psel.get(0).partialArcs.size());
    }

    //line 11


    @Test
    void compare11_00() {
        SubnetComparator sc = new SubnetComparator(creatSubnet11(), creatSubnet0(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(2, gcs.gcsValue);
        assertEquals(2, gcs.psel.get(0).partialNodes.size());
        assertEquals(1, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare11_01() {
        SubnetComparator sc = new SubnetComparator(creatSubnet11(), creatSubnet1(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(2, gcs.gcsValue);
        assertEquals(2, gcs.psel.get(0).partialNodes.size());
        assertEquals(1, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare11_02() {
        SubnetComparator sc = new SubnetComparator(creatSubnet11(), creatSubnet2(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(4, gcs.gcsValue);
        assertEquals(4, gcs.psel.get(0).partialNodes.size());
        assertEquals(3, gcs.psel.get(0).partialArcs.size());

    }

    @Test
    void compare11_03() {
        SubnetComparator sc = new SubnetComparator(creatSubnet11(), creatSubnet3(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(5, gcs.gcsValue);
        assertEquals(5, gcs.psel.get(0).partialNodes.size());
        assertEquals(4, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare11_04() {
        SubnetComparator sc = new SubnetComparator(creatSubnet11(), creatSubnet4(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(5, gcs.gcsValue);
        assertEquals(5, gcs.psel.get(0).partialNodes.size());
        assertEquals(4, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare11_05() {
        SubnetComparator sc = new SubnetComparator(creatSubnet11(), creatSubnet5(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(0, gcs.gcsValue);
        assertEquals(0, gcs.psel.size());
    }

    @Test
    void compare11_06() {
        SubnetComparator sc = new SubnetComparator(creatSubnet11(), creatSubnet6(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(0, gcs.gcsValue);
        assertEquals(0, gcs.psel.size());
    }

    @Test
    void compare11_07() {
        SubnetComparator sc = new SubnetComparator(creatSubnet11(), creatSubnet7(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(0, gcs.gcsValue);
        assertEquals(0, gcs.psel.size());
    }

    @Test
    void compare11_08() {
        SubnetComparator sc = new SubnetComparator(creatSubnet11(), creatSubnet8(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(0, gcs.gcsValue);
        assertEquals(0, gcs.psel.size());
    }

    @Test
    void compare11_09() {
        SubnetComparator sc = new SubnetComparator(creatSubnet11(), creatSubnet9(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(0, gcs.gcsValue);
        assertEquals(0, gcs.psel.size());
    }

    @Test
    void compare11_10() {
        SubnetComparator sc = new SubnetComparator(creatSubnet11(), creatSubnet10(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(0, gcs.gcsValue);
        assertEquals(0, gcs.psel.size());
    }

    @Test
    void compare11_11() {
        SubnetComparator sc = new SubnetComparator(creatSubnet11(), creatSubnet11(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(7, gcs.gcsValue);
        assertEquals(7, gcs.psel.get(0).partialNodes.size());
        assertEquals(6, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare11_12() {
        SubnetComparator sc = new SubnetComparator(creatSubnet11(), creatSubnet12(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(4, gcs.gcsValue);
        assertEquals(4, gcs.psel.get(0).partialNodes.size());
        assertEquals(3, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare11_13() {
        SubnetComparator sc = new SubnetComparator(creatSubnet11(), creatSubnet13(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(0, gcs.gcsValue);
        assertEquals(0, gcs.psel.size());
    }

    @Test
    void compare11_14() {
        SubnetComparator sc = new SubnetComparator(creatSubnet11(), creatSubnet14(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(0, gcs.gcsValue);
        assertEquals(0, gcs.psel.size());
    }

    @Test
    void compare11_15() {
        SubnetComparator sc = new SubnetComparator(creatSubnet11(), creatSubnet15(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(0, gcs.gcsValue);
        assertEquals(0, gcs.psel.size());
    }

    @Test
    void compare11_16() {
        SubnetComparator sc = new SubnetComparator(creatSubnet11(), creatSubnet16(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(0, gcs.gcsValue);
        assertEquals(0, gcs.psel.size());
    }

    @Test
    void compare11_17() {
        SubnetComparator sc = new SubnetComparator(creatSubnet11(), creatSubnet17(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(0, gcs.gcsValue);
        assertEquals(0, gcs.psel.size());
    }

    @Test
    void compare11_18() {
        SubnetComparator sc = new SubnetComparator(creatSubnet11(), creatSubnet18(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(0, gcs.gcsValue);
        assertEquals(0, gcs.psel.size());
    }


    //line 12


    @Test
    void compare12_00() {
        SubnetComparator sc = new SubnetComparator(creatSubnet12(), creatSubnet0(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(2, gcs.gcsValue);
        assertEquals(2, gcs.psel.get(0).partialNodes.size());
        assertEquals(1, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare12_01() {
        SubnetComparator sc = new SubnetComparator(creatSubnet12(), creatSubnet1(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(2, gcs.gcsValue);
        assertEquals(2, gcs.psel.get(0).partialNodes.size());
        assertEquals(1, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare12_02() {
        SubnetComparator sc = new SubnetComparator(creatSubnet12(), creatSubnet2(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(4, gcs.gcsValue);
        assertEquals(4, gcs.psel.get(0).partialNodes.size());
        assertEquals(3, gcs.psel.get(0).partialArcs.size());

    }

    @Test
    void compare12_03() {
        SubnetComparator sc = new SubnetComparator(creatSubnet12(), creatSubnet3(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(4, gcs.gcsValue);
        assertEquals(4, gcs.psel.get(0).partialNodes.size());
        assertEquals(3, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare12_04() {
        SubnetComparator sc = new SubnetComparator(creatSubnet12(), creatSubnet4(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(4, gcs.gcsValue);
        assertEquals(4, gcs.psel.get(0).partialNodes.size());
        assertEquals(3, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare12_05() {
        SubnetComparator sc = new SubnetComparator(creatSubnet12(), creatSubnet5(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(3, gcs.gcsValue);
        assertEquals(3, gcs.psel.get(0).partialNodes.size());
        assertEquals(2, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare12_06() {
        SubnetComparator sc = new SubnetComparator(creatSubnet12(), creatSubnet6(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(0, gcs.gcsValue);
        assertEquals(0, gcs.psel.size());
    }

    @Test
    void compare12_07() {
        SubnetComparator sc = new SubnetComparator(creatSubnet12(), creatSubnet7(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(3, gcs.gcsValue);
        assertEquals(3, gcs.psel.get(0).partialNodes.size());
        assertEquals(2, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare12_08() {
        SubnetComparator sc = new SubnetComparator(creatSubnet12(), creatSubnet8(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(4, gcs.gcsValue);
        assertEquals(4, gcs.psel.get(0).partialNodes.size());
        assertEquals(3, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare12_09() {
        SubnetComparator sc = new SubnetComparator(creatSubnet12(), creatSubnet9(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(4, gcs.gcsValue);
        assertEquals(4, gcs.psel.get(0).partialNodes.size());
        assertEquals(3, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare12_10() {
        SubnetComparator sc = new SubnetComparator(creatSubnet12(), creatSubnet10(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(4, gcs.gcsValue);
        assertEquals(4, gcs.psel.get(0).partialNodes.size());
        assertEquals(4, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare12_11() {
        SubnetComparator sc = new SubnetComparator(creatSubnet12(), creatSubnet11(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(4, gcs.gcsValue);
        assertEquals(4, gcs.psel.get(0).partialNodes.size());
        assertEquals(3, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare12_12() {
        SubnetComparator sc = new SubnetComparator(creatSubnet12(), creatSubnet12(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(4, gcs.gcsValue);
        assertEquals(4, gcs.psel.get(0).partialNodes.size());
        assertEquals(4, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare12_13() {
        SubnetComparator sc = new SubnetComparator(creatSubnet12(), creatSubnet13(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(4, gcs.gcsValue);
        assertEquals(4, gcs.psel.get(0).partialNodes.size());
        assertEquals(3, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare12_14() {
        SubnetComparator sc = new SubnetComparator(creatSubnet12(), creatSubnet14(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(4, gcs.gcsValue);
        assertEquals(4, gcs.psel.get(0).partialNodes.size());
        assertEquals(3, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare12_15() {
        SubnetComparator sc = new SubnetComparator(creatSubnet12(), creatSubnet15(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(3, gcs.gcsValue);
        assertEquals(3, gcs.psel.get(0).partialNodes.size());
        assertEquals(2, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare12_16() {
        SubnetComparator sc = new SubnetComparator(creatSubnet12(), creatSubnet16(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(4, gcs.gcsValue);
        assertEquals(4, gcs.psel.get(0).partialNodes.size());
        assertEquals(4, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare12_17() {
        SubnetComparator sc = new SubnetComparator(creatSubnet12(), creatSubnet17(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(4, gcs.gcsValue);
        assertEquals(4, gcs.psel.get(0).partialNodes.size());
        assertEquals(4, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare12_18() {
        SubnetComparator sc = new SubnetComparator(creatSubnet12(), creatSubnet18(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(4, gcs.gcsValue);
        assertEquals(4, gcs.psel.get(0).partialNodes.size());
        assertEquals(3, gcs.psel.get(0).partialArcs.size());
    }


    //line 13


    @Test
    void compare13_00() {
        SubnetComparator sc = new SubnetComparator(creatSubnet13(), creatSubnet0(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(2, gcs.gcsValue);
        assertEquals(2, gcs.psel.get(0).partialNodes.size());
        assertEquals(1, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare13_01() {
        SubnetComparator sc = new SubnetComparator(creatSubnet13(), creatSubnet1(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(2, gcs.gcsValue);
        assertEquals(2, gcs.psel.get(0).partialNodes.size());
        assertEquals(1, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare13_02() {
        SubnetComparator sc = new SubnetComparator(creatSubnet13(), creatSubnet2(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(4, gcs.gcsValue);
        assertEquals(4, gcs.psel.get(0).partialNodes.size());
        assertEquals(3, gcs.psel.get(0).partialArcs.size());

    }

    @Test
    void compare13_03() {
        SubnetComparator sc = new SubnetComparator(creatSubnet13(), creatSubnet3(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(5, gcs.gcsValue);
        assertEquals(5, gcs.psel.get(0).partialNodes.size());
        assertEquals(4, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare13_04() {
        SubnetComparator sc = new SubnetComparator(creatSubnet13(), creatSubnet4(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(0, gcs.gcsValue);
        assertEquals(0, gcs.psel.size());
    }

    @Test
    void compare13_05() {
        SubnetComparator sc = new SubnetComparator(creatSubnet13(), creatSubnet5(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(6, gcs.gcsValue);
        assertEquals(6, gcs.psel.get(0).partialNodes.size());
        assertEquals(5, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare13_06() {
        SubnetComparator sc = new SubnetComparator(creatSubnet13(), creatSubnet6(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(7, gcs.gcsValue);
        assertEquals(7, gcs.psel.get(0).partialNodes.size());
        assertEquals(6, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare13_07() {
        SubnetComparator sc = new SubnetComparator(creatSubnet13(), creatSubnet7(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(6, gcs.gcsValue);
        assertEquals(6, gcs.psel.get(0).partialNodes.size());
        assertEquals(5, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare13_08() {
        SubnetComparator sc = new SubnetComparator(creatSubnet13(), creatSubnet8(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(7, gcs.gcsValue);
        assertEquals(7, gcs.psel.get(0).partialNodes.size());
        assertEquals(6, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare13_09() {
        SubnetComparator sc = new SubnetComparator(creatSubnet13(), creatSubnet9(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(7, gcs.gcsValue);
        assertEquals(7, gcs.psel.get(0).partialNodes.size());
        assertEquals(6, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare13_10() {
        SubnetComparator sc = new SubnetComparator(creatSubnet13(), creatSubnet10(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        //saveSubnet(gcs);
        assertEquals(7, gcs.gcsValue);
        assertEquals(7, gcs.psel.get(0).partialNodes.size());
        assertEquals(6, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare13_11() {
        SubnetComparator sc = new SubnetComparator(creatSubnet13(), creatSubnet11(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(0, gcs.gcsValue);
        assertEquals(0, gcs.psel.size());
    }

    @Test
    void compare13_12() {
        SubnetComparator sc = new SubnetComparator(creatSubnet13(), creatSubnet12(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(4, gcs.gcsValue);
        assertEquals(4, gcs.psel.get(0).partialNodes.size());
        assertEquals(3, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare13_13() {
        SubnetComparator sc = new SubnetComparator(creatSubnet13(), creatSubnet13(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        //saveSubnet(gcs);
        assertEquals(8, gcs.gcsValue);
        assertEquals(8, gcs.psel.get(0).partialNodes.size());
        assertEquals(8, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare13_14() {
        SubnetComparator sc = new SubnetComparator(creatSubnet13(), creatSubnet14(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        //saveSubnet(gcs);
        assertEquals(7, gcs.gcsValue);
        assertEquals(7, gcs.psel.get(0).partialNodes.size());
        assertEquals(6, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare13_15() {
        SubnetComparator sc = new SubnetComparator(creatSubnet13(), creatSubnet15(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(6, gcs.gcsValue);
        assertEquals(6, gcs.psel.get(0).partialNodes.size());
        assertEquals(5, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare13_16() {
        SubnetComparator sc = new SubnetComparator(creatSubnet13(), creatSubnet16(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        //saveSubnet(gcs);
        assertEquals(7, gcs.gcsValue);
        assertEquals(7, gcs.psel.get(0).partialNodes.size());
        assertEquals(6, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare13_17() {
        SubnetComparator sc = new SubnetComparator(creatSubnet13(), creatSubnet17(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(7, gcs.gcsValue);
        assertEquals(7, gcs.psel.get(0).partialNodes.size());
        assertEquals(6, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare13_18() {
        SubnetComparator sc = new SubnetComparator(creatSubnet13(), creatSubnet18(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(7, gcs.gcsValue);
        assertEquals(7, gcs.psel.get(0).partialNodes.size());
        assertEquals(6, gcs.psel.get(0).partialArcs.size());
    }


    //line 14


    @Test
    void compare14_00() {
        SubnetComparator sc = new SubnetComparator(creatSubnet14(), creatSubnet0(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(2, gcs.gcsValue);
        assertEquals(2, gcs.psel.get(0).partialNodes.size());
        assertEquals(1, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare14_01() {
        SubnetComparator sc = new SubnetComparator(creatSubnet14(), creatSubnet1(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(2, gcs.gcsValue);
        assertEquals(2, gcs.psel.get(0).partialNodes.size());
        assertEquals(1, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare14_02() {
        SubnetComparator sc = new SubnetComparator(creatSubnet14(), creatSubnet2(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(4, gcs.gcsValue);
        assertEquals(4, gcs.psel.get(0).partialNodes.size());
        assertEquals(3, gcs.psel.get(0).partialArcs.size());

    }

    @Test
    void compare14_03() {
        SubnetComparator sc = new SubnetComparator(creatSubnet14(), creatSubnet3(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(5, gcs.gcsValue);
        assertEquals(5, gcs.psel.get(0).partialNodes.size());
        assertEquals(4, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare14_04() {
        SubnetComparator sc = new SubnetComparator(creatSubnet14(), creatSubnet4(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(0, gcs.gcsValue);
        assertEquals(0, gcs.psel.size());
    }

    @Test
    void compare14_05() {
        SubnetComparator sc = new SubnetComparator(creatSubnet14(), creatSubnet5(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(7, gcs.gcsValue);
        assertEquals(7, gcs.psel.get(0).partialNodes.size());
        assertEquals(6, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare14_06() {
        SubnetComparator sc = new SubnetComparator(creatSubnet14(), creatSubnet6(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(9, gcs.gcsValue);
        assertEquals(9, gcs.psel.get(0).partialNodes.size());
        assertEquals(8, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare14_07() {
        SubnetComparator sc = new SubnetComparator(creatSubnet14(), creatSubnet7(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(9, gcs.gcsValue);
        assertEquals(9, gcs.psel.get(0).partialNodes.size());
        assertEquals(8, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare14_08() {
        SubnetComparator sc = new SubnetComparator(creatSubnet14(), creatSubnet8(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(8, gcs.gcsValue);
        assertEquals(8, gcs.psel.get(0).partialNodes.size());
        assertEquals(7, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare14_09() {
        SubnetComparator sc = new SubnetComparator(creatSubnet14(), creatSubnet9(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals( 8, gcs.gcsValue);
        assertEquals(8, gcs.psel.get(0).partialNodes.size());
        assertEquals(7, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare14_10() {
        SubnetComparator sc = new SubnetComparator(creatSubnet14(), creatSubnet10(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(9, gcs.gcsValue);
        assertEquals(9, gcs.psel.get(0).partialNodes.size());
        assertEquals(8, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare14_11() {
        SubnetComparator sc = new SubnetComparator(creatSubnet14(), creatSubnet11(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(0, gcs.gcsValue);
        assertEquals(0, gcs.psel.size());
    }

    @Test
    void compare14_12() {
        SubnetComparator sc = new SubnetComparator(creatSubnet14(), creatSubnet12(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(4, gcs.gcsValue);
        assertEquals(4, gcs.psel.get(0).partialNodes.size());
        assertEquals(3, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare14_13() {
        SubnetComparator sc = new SubnetComparator(creatSubnet14(), creatSubnet13(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(7, gcs.gcsValue);
        assertEquals(7, gcs.psel.get(0).partialNodes.size());
        assertEquals(6, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare14_14() {
        SubnetComparator sc = new SubnetComparator(creatSubnet14(), creatSubnet14(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        //saveSubnet(gcs);
        assertEquals(19, gcs.gcsValue);
        assertEquals(19, gcs.psel.get(0).partialNodes.size());
        assertEquals(18, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare14_15() {
        SubnetComparator sc = new SubnetComparator(creatSubnet14(), creatSubnet15(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        //Important
        //saveSubnet(gcs);
        assertEquals(12, gcs.gcsValue);
        assertEquals(12, gcs.psel.get(0).partialNodes.size());
        assertEquals(11, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare14_16() {
        SubnetComparator sc = new SubnetComparator(creatSubnet14(), creatSubnet16(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
       //aeSubnet(gcs);
        assertEquals(10, gcs.gcsValue);
        assertEquals(10, gcs.psel.get(0).partialNodes.size());
        assertEquals(9, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare14_17() {
        SubnetComparator sc = new SubnetComparator(creatSubnet14(), creatSubnet17(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(9, gcs.gcsValue);
        assertEquals(9, gcs.psel.get(0).partialNodes.size());
        assertEquals(8, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare14_18() {
        SubnetComparator sc = new SubnetComparator(creatSubnet14(), creatSubnet18(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(8, gcs.gcsValue);
        assertEquals(8, gcs.psel.get(0).partialNodes.size());
        assertEquals(7, gcs.psel.get(0).partialArcs.size());
    }


    //line 15


    @Test
    void compare15_00() {
        SubnetComparator sc = new SubnetComparator(creatSubnet15(), creatSubnet0(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(2, gcs.gcsValue);
        assertEquals(2, gcs.psel.get(0).partialNodes.size());
        assertEquals(1, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare15_01() {
        SubnetComparator sc = new SubnetComparator(creatSubnet15(), creatSubnet1(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(2, gcs.gcsValue);
        assertEquals(2, gcs.psel.get(0).partialNodes.size());
        assertEquals(1, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare15_02() {
        SubnetComparator sc = new SubnetComparator(creatSubnet15(), creatSubnet2(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(4, gcs.gcsValue);
        assertEquals(4, gcs.psel.get(0).partialNodes.size());
        assertEquals(3, gcs.psel.get(0).partialArcs.size());

    }

    @Test
    void compare15_03() {
        SubnetComparator sc = new SubnetComparator(creatSubnet15(), creatSubnet3(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(4, gcs.gcsValue);
        assertEquals(4, gcs.psel.get(0).partialNodes.size());
        assertEquals(3, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare15_04() {
        SubnetComparator sc = new SubnetComparator(creatSubnet15(), creatSubnet4(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(0, gcs.gcsValue);
        assertEquals(0, gcs.psel.size());
    }

    @Test
    void compare15_05() {
        SubnetComparator sc = new SubnetComparator(creatSubnet15(), creatSubnet5(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(7, gcs.gcsValue);
        assertEquals(7, gcs.psel.get(0).partialNodes.size());
        assertEquals(6, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare15_06() {
        SubnetComparator sc = new SubnetComparator(creatSubnet15(), creatSubnet6(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(9, gcs.gcsValue);
        assertEquals(9, gcs.psel.get(0).partialNodes.size());
        assertEquals(8, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare15_07() {
        SubnetComparator sc = new SubnetComparator(creatSubnet15(), creatSubnet7(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(14, gcs.gcsValue);
        assertEquals(14, gcs.psel.get(0).partialNodes.size());
        assertEquals(13, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare15_08() {
        SubnetComparator sc = new SubnetComparator(creatSubnet15(), creatSubnet8(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(8, gcs.gcsValue);
        assertEquals(8, gcs.psel.get(0).partialNodes.size());
        assertEquals(7, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare15_09() {
        SubnetComparator sc = new SubnetComparator(creatSubnet15(), creatSubnet9(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(8, gcs.gcsValue);
        assertEquals(8, gcs.psel.get(0).partialNodes.size());
        assertEquals(7, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare15_10() {
        SubnetComparator sc = new SubnetComparator(creatSubnet15(), creatSubnet10(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(9, gcs.gcsValue);
        assertEquals(9, gcs.psel.get(0).partialNodes.size());
        assertEquals(8, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare15_11() {
        SubnetComparator sc = new SubnetComparator(creatSubnet15(), creatSubnet11(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(0, gcs.gcsValue);
        assertEquals(0, gcs.psel.size());
    }

    @Test
    void compare15_12() {
        SubnetComparator sc = new SubnetComparator(creatSubnet15(), creatSubnet12(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(3, gcs.gcsValue);
        assertEquals(3, gcs.psel.get(0).partialNodes.size());
        assertEquals(2, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare15_13() {
        SubnetComparator sc = new SubnetComparator(creatSubnet15(), creatSubnet13(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(6, gcs.gcsValue);
        assertEquals(6, gcs.psel.get(0).partialNodes.size());
        assertEquals(5, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare15_14() {
        SubnetComparator sc = new SubnetComparator(creatSubnet15(), creatSubnet14(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(12, gcs.gcsValue);
        assertEquals(12, gcs.psel.get(0).partialNodes.size());
        assertEquals(11, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare15_15() {
        SubnetComparator sc = new SubnetComparator(creatSubnet15(), creatSubnet15(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(18, gcs.gcsValue);
        assertEquals(18, gcs.psel.get(0).partialNodes.size());
        assertEquals(17, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare15_16() {
        SubnetComparator sc = new SubnetComparator(creatSubnet15(), creatSubnet16(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(16, gcs.gcsValue);
        assertEquals(16, gcs.psel.get(0).partialNodes.size());
        assertEquals(15, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare15_17() {
        SubnetComparator sc = new SubnetComparator(creatSubnet15(), creatSubnet17(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        //saveSubnet(gcs);
        assertEquals(9, gcs.gcsValue);
        assertEquals(9, gcs.psel.get(0).partialNodes.size());
        assertEquals(8, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare15_18() {
        SubnetComparator sc = new SubnetComparator(creatSubnet15(), creatSubnet18(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        //saveSubnet(gcs);
        assertEquals(7, gcs.gcsValue);
        assertEquals(7, gcs.psel.get(0).partialNodes.size());
        assertEquals(6, gcs.psel.get(0).partialArcs.size());
    }


    //line 16


    @Test
    void compare16_00() {
        SubnetComparator sc = new SubnetComparator(creatSubnet16(), creatSubnet0(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(2, gcs.gcsValue);
        assertEquals(2, gcs.psel.get(0).partialNodes.size());
        assertEquals(1, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare16_01() {
        SubnetComparator sc = new SubnetComparator(creatSubnet16(), creatSubnet1(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(2, gcs.gcsValue);
        assertEquals(2, gcs.psel.get(0).partialNodes.size());
        assertEquals(1, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare16_02() {
        SubnetComparator sc = new SubnetComparator(creatSubnet16(), creatSubnet2(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(4, gcs.gcsValue);
        assertEquals(4, gcs.psel.get(0).partialNodes.size());
        assertEquals(3, gcs.psel.get(0).partialArcs.size());

    }

    @Test
    void compare16_03() {
        SubnetComparator sc = new SubnetComparator(creatSubnet16(), creatSubnet3(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(4, gcs.gcsValue);
        assertEquals(4, gcs.psel.get(0).partialNodes.size());
        assertEquals(3, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare16_04() {
        SubnetComparator sc = new SubnetComparator(creatSubnet16(), creatSubnet4(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(0, gcs.gcsValue);
        assertEquals(0, gcs.psel.size());
    }

    @Test
    void compare16_05() {
        SubnetComparator sc = new SubnetComparator(creatSubnet16(), creatSubnet5(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(7, gcs.gcsValue);
        assertEquals(7, gcs.psel.get(0).partialNodes.size());
        assertEquals(6, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare16_06() {
        SubnetComparator sc = new SubnetComparator(creatSubnet16(), creatSubnet6(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(8, gcs.gcsValue);
        assertEquals(8, gcs.psel.get(0).partialNodes.size());
        assertEquals(7, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare16_07() {
        SubnetComparator sc = new SubnetComparator(creatSubnet16(), creatSubnet7(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(12, gcs.gcsValue);
        assertEquals(12, gcs.psel.get(0).partialNodes.size());
        assertEquals(11, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare16_08() {
        SubnetComparator sc = new SubnetComparator(creatSubnet16(), creatSubnet8(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(7, gcs.gcsValue);
        assertEquals(7, gcs.psel.get(0).partialNodes.size());
        assertEquals(6, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare16_09() {
        SubnetComparator sc = new SubnetComparator(creatSubnet16(), creatSubnet9(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(7, gcs.gcsValue);
        assertEquals(7, gcs.psel.get(0).partialNodes.size());
        assertEquals(6, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare16_10() {
        SubnetComparator sc = new SubnetComparator(creatSubnet16(), creatSubnet10(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(8, gcs.gcsValue);
        assertEquals(8, gcs.psel.get(0).partialNodes.size());
        assertEquals(8, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare16_11() {
        SubnetComparator sc = new SubnetComparator(creatSubnet16(), creatSubnet11(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(0, gcs.gcsValue);
        assertEquals(0, gcs.psel.size());
    }

    @Test
    void compare16_12() {
        SubnetComparator sc = new SubnetComparator(creatSubnet16(), creatSubnet12(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(4, gcs.gcsValue);
        assertEquals(4, gcs.psel.get(0).partialNodes.size());
        assertEquals(4, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare16_13() {
        SubnetComparator sc = new SubnetComparator(creatSubnet16(), creatSubnet13(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        //saveSubnet(gcs);
        assertEquals(7, gcs.gcsValue);
        assertEquals(7, gcs.psel.get(0).partialNodes.size());
        assertEquals(6, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare16_14() {
        SubnetComparator sc = new SubnetComparator(creatSubnet16(), creatSubnet14(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        //saveSubnet(gcs);
        assertEquals(14, gcs.gcsValue);
        assertEquals(14, gcs.psel.get(0).partialNodes.size());
        assertEquals(13, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare16_15() {
        SubnetComparator sc = new SubnetComparator(creatSubnet16(), creatSubnet15(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(16, gcs.gcsValue);
        assertEquals(16, gcs.psel.get(0).partialNodes.size());
        assertEquals(15, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare16_16() {
        SubnetComparator sc = new SubnetComparator(creatSubnet16(), creatSubnet16(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        //     saveSubnet(gcs);
        assertEquals(16, gcs.gcsValue);
        assertEquals(16, gcs.psel.get(0).partialNodes.size());
        assertEquals(18, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare16_17() {
        SubnetComparator sc = new SubnetComparator(creatSubnet16(), creatSubnet17(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        //saveSubnet(gcs);


        System.out.println("---------------");

        for(Arc a : gcs.psel.get(0).partialArcs)
        {
            System.out.println(" Arc: " + a.getStartNode().getName() + " - > " + a.getEndNode().getName());
        }
        System.out.println("RRRRRRRRRRRRRRRRRRRRrr");

        //saveSubnet(gcs);
        assertEquals(8, gcs.gcsValue);
        assertEquals(8, gcs.psel.get(0).partialNodes.size());
        assertEquals(7, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare16_18() {
        SubnetComparator sc = new SubnetComparator(creatSubnet16(), creatSubnet18(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        //saveSubnet(gcs);
        assertEquals(7, gcs.gcsValue);
        assertEquals(7, gcs.psel.get(0).partialNodes.size());
        assertEquals(6, gcs.psel.get(0).partialArcs.size());
    }


    //line 17


    @Test
    void compare17_00() {
        SubnetComparator sc = new SubnetComparator(creatSubnet17(), creatSubnet0(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(2, gcs.gcsValue);
        assertEquals(2, gcs.psel.get(0).partialNodes.size());
        assertEquals(1, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare17_01() {
        SubnetComparator sc = new SubnetComparator(creatSubnet17(), creatSubnet1(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(2, gcs.gcsValue);
        assertEquals(2, gcs.psel.get(0).partialNodes.size());
        assertEquals(1, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare17_02() {
        SubnetComparator sc = new SubnetComparator(creatSubnet17(), creatSubnet2(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(4, gcs.gcsValue);
        assertEquals(4, gcs.psel.get(0).partialNodes.size());
        assertEquals(3, gcs.psel.get(0).partialArcs.size());

    }

    @Test
    void compare17_03() {
        SubnetComparator sc = new SubnetComparator(creatSubnet17(), creatSubnet3(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(5, gcs.gcsValue);
        assertEquals(5, gcs.psel.get(0).partialNodes.size());
        assertEquals(4, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare17_04() {
        SubnetComparator sc = new SubnetComparator(creatSubnet17(), creatSubnet4(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(0, gcs.gcsValue);
        assertEquals(0, gcs.psel.size());
    }

    @Test
    void compare17_05() {
        SubnetComparator sc = new SubnetComparator(creatSubnet17(), creatSubnet5(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(7, gcs.gcsValue);
        assertEquals(7, gcs.psel.get(0).partialNodes.size());
        assertEquals(6, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare17_06() {
        SubnetComparator sc = new SubnetComparator(creatSubnet17(), creatSubnet6(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(10, gcs.gcsValue);
        assertEquals(10, gcs.psel.get(0).partialNodes.size());
        assertEquals(9, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare17_07() {
        SubnetComparator sc = new SubnetComparator(creatSubnet17(), creatSubnet7(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(9, gcs.gcsValue);
        assertEquals(9, gcs.psel.get(0).partialNodes.size());
        assertEquals(8, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare17_08() {
        SubnetComparator sc = new SubnetComparator(creatSubnet17(), creatSubnet8(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        //saveSubnet(gcs);
        assertEquals(9, gcs.gcsValue);
        assertEquals(9, gcs.psel.get(0).partialNodes.size());
        assertEquals(8, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare17_09() {
        SubnetComparator sc = new SubnetComparator(creatSubnet17(), creatSubnet9(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(10, gcs.gcsValue);
        assertEquals(10, gcs.psel.get(0).partialNodes.size());
        assertEquals(9, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare17_10() {
        SubnetComparator sc = new SubnetComparator(creatSubnet17(), creatSubnet10(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(10, gcs.gcsValue);
        assertEquals(10, gcs.psel.get(0).partialNodes.size());
        assertEquals(9, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare17_11() {
        SubnetComparator sc = new SubnetComparator(creatSubnet17(), creatSubnet11(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(0, gcs.gcsValue);
        assertEquals(0, gcs.psel.size());
    }

    @Test
    void compare17_12() {
        SubnetComparator sc = new SubnetComparator(creatSubnet17(), creatSubnet12(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(4, gcs.gcsValue);
        assertEquals(4, gcs.psel.get(0).partialNodes.size());
        assertEquals(4, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare17_13() {
        SubnetComparator sc = new SubnetComparator(creatSubnet17(), creatSubnet13(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(7, gcs.gcsValue);
        assertEquals(7, gcs.psel.get(0).partialNodes.size());
        assertEquals(6, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare17_14() {
        SubnetComparator sc = new SubnetComparator(creatSubnet17(), creatSubnet14(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(9, gcs.gcsValue);
        assertEquals(9, gcs.psel.get(0).partialNodes.size());
        assertEquals(8, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare17_15() {
        SubnetComparator sc = new SubnetComparator(creatSubnet17(), creatSubnet15(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        //saveSubnet(gcs);
        assertEquals(6, gcs.gcsValue);
        assertEquals(6, gcs.psel.get(0).partialNodes.size());
        assertEquals(5, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare17_16() {
        SubnetComparator sc = new SubnetComparator(creatSubnet17(), creatSubnet16(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        //saveSubnet(gcs);
        assertEquals(8, gcs.gcsValue);
        assertEquals(8, gcs.psel.get(0).partialNodes.size());
        assertEquals(7, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare17_17() {
        SubnetComparator sc = new SubnetComparator(creatSubnet17(), creatSubnet17(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        //saveSubnet(gcs);
        assertEquals(18, gcs.gcsValue);
        assertEquals(18, gcs.psel.get(0).partialNodes.size());
        assertEquals(18, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare17_18() {
        SubnetComparator sc = new SubnetComparator(creatSubnet17(), creatSubnet18(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        //saveSubnet(gcs);
        assertEquals(8, gcs.gcsValue);
        assertEquals(8, gcs.psel.get(0).partialNodes.size());
        assertEquals(7, gcs.psel.get(0).partialArcs.size());
    }


    //line 18


    @Test
    void compare18_00() {
        SubnetComparator sc = new SubnetComparator(creatSubnet18(), creatSubnet0(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(2, gcs.gcsValue);
        assertEquals(2, gcs.psel.get(0).partialNodes.size());
        assertEquals(1, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare18_01() {
        SubnetComparator sc = new SubnetComparator(creatSubnet18(), creatSubnet1(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(2, gcs.gcsValue);
        assertEquals(2, gcs.psel.get(0).partialNodes.size());
        assertEquals(1, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare18_02() {
        SubnetComparator sc = new SubnetComparator(creatSubnet18(), creatSubnet2(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(4, gcs.gcsValue);
        assertEquals(4, gcs.psel.get(0).partialNodes.size());
        assertEquals(3, gcs.psel.get(0).partialArcs.size());

    }

    @Test
    void compare18_03() {
        SubnetComparator sc = new SubnetComparator(creatSubnet18(), creatSubnet3(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(5, gcs.gcsValue);
        assertEquals(5, gcs.psel.get(0).partialNodes.size());
        assertEquals(4, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare18_04() {
        SubnetComparator sc = new SubnetComparator(creatSubnet18(), creatSubnet4(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(0, gcs.gcsValue);
        assertEquals(0, gcs.psel.size());
    }

    @Test
    void compare18_05() {
        SubnetComparator sc = new SubnetComparator(creatSubnet18(), creatSubnet5(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(5, gcs.gcsValue);
        assertEquals(5, gcs.psel.get(0).partialNodes.size());
        assertEquals(4, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare18_06() {
        SubnetComparator sc = new SubnetComparator(creatSubnet18(), creatSubnet6(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(6, gcs.gcsValue);
        assertEquals(6, gcs.psel.get(0).partialNodes.size());
        assertEquals(5, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare18_07() {
        SubnetComparator sc = new SubnetComparator(creatSubnet18(), creatSubnet7(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(8, gcs.gcsValue);
        assertEquals(8, gcs.psel.get(0).partialNodes.size());
        assertEquals(7, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare18_08() {
        SubnetComparator sc = new SubnetComparator(creatSubnet18(), creatSubnet8(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        //saveSubnet(gcs);
        assertEquals(10, gcs.gcsValue);
        assertEquals(10, gcs.psel.get(0).partialNodes.size());
        assertEquals(9, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare18_09() {
        SubnetComparator sc = new SubnetComparator(creatSubnet18(), creatSubnet9(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        //saveSubnet(gcs);
        assertEquals(12, gcs.gcsValue);
        assertEquals(12, gcs.psel.get(0).partialNodes.size());
        assertEquals(11, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare18_10() {
        SubnetComparator sc = new SubnetComparator(creatSubnet18(), creatSubnet10(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        //saveSubnet(gcs);
        assertEquals(8, gcs.gcsValue);
        assertEquals(8, gcs.psel.get(0).partialNodes.size());
        assertEquals(7, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare18_11() {
        SubnetComparator sc = new SubnetComparator(creatSubnet18(), creatSubnet11(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(0, gcs.gcsValue);
        assertEquals(0, gcs.psel.size());
    }

    @Test
    void compare18_12() {
        SubnetComparator sc = new SubnetComparator(creatSubnet18(), creatSubnet12(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(4, gcs.gcsValue);
        assertEquals(4, gcs.psel.get(0).partialNodes.size());
        assertEquals(3, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare18_13() {
        SubnetComparator sc = new SubnetComparator(creatSubnet18(), creatSubnet13(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(7, gcs.gcsValue);
        assertEquals(7, gcs.psel.get(0).partialNodes.size());
        assertEquals(6, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare18_14() {
        SubnetComparator sc = new SubnetComparator(creatSubnet18(), creatSubnet14(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(8, gcs.gcsValue);
        assertEquals(8, gcs.psel.get(0).partialNodes.size());
        assertEquals(7, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare18_15() {
        SubnetComparator sc = new SubnetComparator(creatSubnet18(), creatSubnet15(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        //saveSubnet(gcs);
        assertEquals(7, gcs.gcsValue);
        assertEquals(7, gcs.psel.get(0).partialNodes.size());
        assertEquals(6, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare18_16() {
        SubnetComparator sc = new SubnetComparator(creatSubnet18(), creatSubnet16(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        //saveSubnet(gcs);
        assertEquals(7, gcs.gcsValue);
        assertEquals(7, gcs.psel.get(0).partialNodes.size());
        assertEquals(6, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare18_17() {
        SubnetComparator sc = new SubnetComparator(creatSubnet18(), creatSubnet17(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        assertEquals(8, gcs.gcsValue);
        assertEquals(8, gcs.psel.get(0).partialNodes.size());
        assertEquals(7, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare18_18() {
        SubnetComparator sc = new SubnetComparator(creatSubnet18(), creatSubnet18(),false,true,false);
        GreatCommonSubnet gcs = sc.compareTest();
        //saveSubnet(gcs);
        assertEquals(19, gcs.gcsValue);
        assertEquals(19, gcs.psel.get(0).partialNodes.size());
        assertEquals(18, gcs.psel.get(0).partialArcs.size());
    }


    SubnetCalculator.SubNet creatSubnet0() {
        ArrayList<Transition> tl = new ArrayList<>();
        Transition t1 = new Transition(IdGenerator.getNextTransitionId(), 0, new Point(1, 1));
        tl.add(t1);

        ArrayList<Place> pl = new ArrayList<>();
        Place p1 = new Place(IdGenerator.getNextId(), 0, new Point(1, 1));
        pl.add(p1);

        ArrayList<Node> nl = new ArrayList<>();
        nl.add(p1);
        nl.add(t1);

        Arc a1 = new Arc(IdGenerator.getNextId(), t1.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();

        al.add(a1);
        return new SubnetCalculator.SubNet(SubnetCalculator.SubNetType.TNET, tl, null, null, null, null);
    }

    SubnetCalculator.SubNet creatSubnet1() {
        ArrayList<Transition> tl = new ArrayList<>();
        Transition t1 = new Transition(IdGenerator.getNextTransitionId(), 0, new Point(1, 1));
        tl.add(t1);

        ArrayList<Place> pl = new ArrayList<>();
        Place p1 = new Place(IdGenerator.getNextId(), 0, new Point(1, 1));
        pl.add(p1);

        ArrayList<Node> nl = new ArrayList<>();
        nl.add(p1);
        nl.add(t1);

        Arc a1 = new Arc(IdGenerator.getNextId(), p1.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        return new SubnetCalculator.SubNet(SubnetCalculator.SubNetType.TNET, tl, null, null, null, null);
    }

    SubnetCalculator.SubNet creatSubnet2() {
        ArrayList<Transition> tl = new ArrayList<>();
        ArrayList<Place> pl = new ArrayList<>();
        ArrayList<Node> nl = new ArrayList<>();

        Transition t3 = new Transition(IdGenerator.getNextTransitionId(), 0, new Point(1, 1));
        Transition t4 = new Transition(IdGenerator.getNextTransitionId(), 0, new Point(1, 1));
        tl.add(t3);
        tl.add(t4);

        Place p3 = new Place(IdGenerator.getNextId(), 0, new Point(1, 1));
        Place p4 = new Place(IdGenerator.getNextId(), 0, new Point(1, 1));
        pl.add(p3);
        pl.add(p4);

        nl.add(p3);
        nl.add(p4);
        nl.add(t3);
        nl.add(t4);

        Arc a3 = new Arc(IdGenerator.getNextId(), p3.getElementLocations().get(0), t3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(IdGenerator.getNextId(), t3.getElementLocations().get(0), p4.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a5 = new Arc(IdGenerator.getNextId(), p4.getElementLocations().get(0), t4.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        return new SubnetCalculator.SubNet(SubnetCalculator.SubNetType.TNET, tl, null, null, null, null);
    }

    SubnetCalculator.SubNet creatSubnet3() {
        ArrayList<Transition> tl = new ArrayList<>();
        ArrayList<Place> pl = new ArrayList<>();

        Transition t5 = new Transition(IdGenerator.getNextTransitionId(), 0, new Point(1, 1));
        Transition t6 = new Transition(IdGenerator.getNextTransitionId(), 0, new Point(1, 1));
        Transition t7 = new Transition(IdGenerator.getNextTransitionId(), 0, new Point(1, 1));
        tl.add(t5);
        tl.add(t6);
        tl.add(t7);

        Place p5 = new Place(IdGenerator.getNextId(), 0, new Point(1, 1));
        Place p6 = new Place(IdGenerator.getNextId(), 0, new Point(1, 1));
        pl.add(p5);
        pl.add(p6);

        Arc a2 = new Arc(IdGenerator.getNextId(), t5.getElementLocations().get(0), p5.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(IdGenerator.getNextId(), p5.getElementLocations().get(0), t6.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(IdGenerator.getNextId(), t6.getElementLocations().get(0), p6.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a5 = new Arc(IdGenerator.getNextId(), p6.getElementLocations().get(0), t7.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        return new SubnetCalculator.SubNet(SubnetCalculator.SubNetType.TNET, tl, null, null, null, null);
    }

    SubnetCalculator.SubNet creatSubnet4() {
        ArrayList<Transition> tl = new ArrayList<>();
        ArrayList<Place> pl = new ArrayList<>();

        Transition t6 = new Transition(IdGenerator.getNextTransitionId(), 0, new Point(1, 1));
        Transition t7 = new Transition(IdGenerator.getNextTransitionId(), 0, new Point(1, 1));
        tl.add(t6);
        tl.add(t7);

        Place p5 = new Place(IdGenerator.getNextId(), 0, new Point(1, 1));
        Place p6 = new Place(IdGenerator.getNextId(), 0, new Point(1, 1));
        Place p7 = new Place(IdGenerator.getNextId(), 0, new Point(1, 1));
        pl.add(p5);
        pl.add(p6);
        pl.add(p7);


        Arc a3 = new Arc(IdGenerator.getNextId(), p5.getElementLocations().get(0), t6.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(IdGenerator.getNextId(), t6.getElementLocations().get(0), p6.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a5 = new Arc(IdGenerator.getNextId(), p6.getElementLocations().get(0), t7.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(IdGenerator.getNextId(), t7.getElementLocations().get(0), p7.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        return new SubnetCalculator.SubNet(SubnetCalculator.SubNetType.TNET, tl, null, null, null, null);
    }

    SubnetCalculator.SubNet creatSubnet5() {
        ArrayList<Transition> tl = new ArrayList<>();
        ArrayList<Place> pl = new ArrayList<>();
        ArrayList<Node> nl = new ArrayList<>();

        Transition t5 = new Transition(IdGenerator.getNextTransitionId(), 0, new Point(1, 1));
        Transition t6 = new Transition(IdGenerator.getNextTransitionId(), 0, new Point(1, 1));
        Transition t7 = new Transition(IdGenerator.getNextTransitionId(), 0, new Point(1, 1));
        Transition t8 = new Transition(IdGenerator.getNextTransitionId(), 0, new Point(1, 1));
        tl.add(t5);
        tl.add(t6);
        tl.add(t7);
        tl.add(t8);

        Place p5 = new Place(IdGenerator.getNextId(), 0, new Point(1, 1));
        Place p6 = new Place(IdGenerator.getNextId(), 0, new Point(1, 1));
        Place p7 = new Place(IdGenerator.getNextId(), 0, new Point(1, 1));
        pl.add(p5);
        pl.add(p6);
        pl.add(p7);

        Arc a2 = new Arc(IdGenerator.getNextId(), t5.getElementLocations().get(0), p5.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(IdGenerator.getNextId(), p5.getElementLocations().get(0), t6.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(IdGenerator.getNextId(), t6.getElementLocations().get(0), p6.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a5 = new Arc(IdGenerator.getNextId(), p6.getElementLocations().get(0), t7.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        Arc a6 = new Arc(IdGenerator.getNextId(), t6.getElementLocations().get(0), p7.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a7 = new Arc(IdGenerator.getNextId(), p7.getElementLocations().get(0), t8.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        return new SubnetCalculator.SubNet(SubnetCalculator.SubNetType.TNET, tl, null, null, null, null);
    }

    SubnetCalculator.SubNet creatSubnet6() {
        ArrayList<Transition> tl = new ArrayList<>();
        ArrayList<Place> pl = new ArrayList<>();
        ArrayList<Node> nl = new ArrayList<>();

        Transition t5 = new Transition(IdGenerator.getNextTransitionId(), 0, new Point(1, 1));
        Transition t6 = new Transition(IdGenerator.getNextTransitionId(), 0, new Point(1, 1));
        Transition t7 = new Transition(IdGenerator.getNextTransitionId(), 0, new Point(1, 1));
        Transition t8 = new Transition(IdGenerator.getNextTransitionId(), 0, new Point(1, 1));
        tl.add(t5);
        tl.add(t6);
        tl.add(t7);
        tl.add(t8);

        Place p5 = new Place(IdGenerator.getNextId(), 0, new Point(1, 1));
        Place p6 = new Place(IdGenerator.getNextId(), 0, new Point(1, 1));
        Place p7 = new Place(IdGenerator.getNextId(), 0, new Point(1, 1));
        Place p8 = new Place(IdGenerator.getNextId(), 0, new Point(1, 1));
        Place p9 = new Place(IdGenerator.getNextId(), 0, new Point(1, 1));
        Place p10 = new Place(IdGenerator.getNextId(), 0, new Point(1, 1));
        pl.add(p5);
        pl.add(p6);
        pl.add(p7);
        pl.add(p8);
        pl.add(p9);
        pl.add(p10);

        Arc a3 = new Arc(IdGenerator.getNextId(), p8.getElementLocations().get(0), t5.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(IdGenerator.getNextId(), t5.getElementLocations().get(0), p5.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a8 = new Arc(IdGenerator.getNextId(), p5.getElementLocations().get(0), t6.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(IdGenerator.getNextId(), t6.getElementLocations().get(0), p6.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a5 = new Arc(IdGenerator.getNextId(), p6.getElementLocations().get(0), t7.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a9 = new Arc(IdGenerator.getNextId(), t7.getElementLocations().get(0), p9.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        Arc a6 = new Arc(IdGenerator.getNextId(), t6.getElementLocations().get(0), p7.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a7 = new Arc(IdGenerator.getNextId(), p7.getElementLocations().get(0), t8.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a10 = new Arc(IdGenerator.getNextId(), t8.getElementLocations().get(0), p10.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        return new SubnetCalculator.SubNet(SubnetCalculator.SubNetType.TNET, tl, null, null, null, null);
    }

    SubnetCalculator.SubNet creatSubnet7() {
        ArrayList<Transition> tl = new ArrayList<>();
        ArrayList<Place> pl = new ArrayList<>();

        Transition t5 = new Transition(IdGenerator.getNextTransitionId(), 0, new Point(1, 1));
        t5.setName("T5");
        Transition t6 = new Transition(IdGenerator.getNextTransitionId(), 0, new Point(1, 1));
        t6.setName("T6");
        Transition t7 = new Transition(IdGenerator.getNextTransitionId(), 0, new Point(1, 1));
        t7.setName("T7");
        Transition t8 = new Transition(IdGenerator.getNextTransitionId(), 0, new Point(1, 1));
        t8.setName("T8");
        Transition t9 = new Transition(IdGenerator.getNextTransitionId(), 0, new Point(1, 1));
        t9.setName("T9");
        Transition t10 = new Transition(IdGenerator.getNextTransitionId(), 0, new Point(1, 1));
        t10.setName("T10");
        tl.add(t5);
        tl.add(t6);
        tl.add(t7);
        tl.add(t8);
        tl.add(t9);
        tl.add(t10);

        Place p5 = new Place(IdGenerator.getNextId(), 0, new Point(1, 1));
        p5.setName("P5");
        Place p6 = new Place(IdGenerator.getNextId(), 0, new Point(1, 1));
        p6.setName("P6");
        Place p7 = new Place(IdGenerator.getNextId(), 0, new Point(1, 1));
        p7.setName("P7");
        Place p8 = new Place(IdGenerator.getNextId(), 0, new Point(1, 1));
        p8.setName("P8");
        Place p9 = new Place(IdGenerator.getNextId(), 0, new Point(1, 1));
        p9.setName("P9");
        Place p10 = new Place(IdGenerator.getNextId(), 0, new Point(1, 1));
        p10.setName("P10");
        Place p11 = new Place(IdGenerator.getNextId(), 0, new Point(1, 1));
        p11.setName("P11");
        Place p12 = new Place(IdGenerator.getNextId(), 0, new Point(1, 1));
        p12.setName("P12");
        Place p13 = new Place(IdGenerator.getNextId(), 0, new Point(1, 1));
        p13.setName("P13");
        pl.add(p5);
        pl.add(p6);
        pl.add(p7);
        pl.add(p8);
        pl.add(p9);
        pl.add(p10);
        pl.add(p11);
        pl.add(p12);
        pl.add(p13);


        Arc a21 = new Arc(IdGenerator.getNextId(), p13.getElementLocations().get(0), t10.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a22 = new Arc(IdGenerator.getNextId(), t10.getElementLocations().get(0), p8.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        Arc a11 = new Arc(IdGenerator.getNextId(), p12.getElementLocations().get(0), t9.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a12 = new Arc(IdGenerator.getNextId(), t9.getElementLocations().get(0), p11.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a13 = new Arc(IdGenerator.getNextId(), p11.getElementLocations().get(0), t5.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        Arc a3 = new Arc(IdGenerator.getNextId(), p8.getElementLocations().get(0), t5.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(IdGenerator.getNextId(), t5.getElementLocations().get(0), p5.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a8 = new Arc(IdGenerator.getNextId(), p5.getElementLocations().get(0), t6.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(IdGenerator.getNextId(), t6.getElementLocations().get(0), p6.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a5 = new Arc(IdGenerator.getNextId(), p6.getElementLocations().get(0), t7.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a9 = new Arc(IdGenerator.getNextId(), t7.getElementLocations().get(0), p9.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        Arc a6 = new Arc(IdGenerator.getNextId(), t6.getElementLocations().get(0), p7.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a7 = new Arc(IdGenerator.getNextId(), p7.getElementLocations().get(0), t8.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a10 = new Arc(IdGenerator.getNextId(), t8.getElementLocations().get(0), p10.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        return new SubnetCalculator.SubNet(SubnetCalculator.SubNetType.TNET, tl, null, null, null, null);
    }

    SubnetCalculator.SubNet creatSubnet8() {
        ArrayList<Transition> tl = new ArrayList<>();
        ArrayList<Place> pl = new ArrayList<>();

        Transition t5 = new Transition(IdGenerator.getNextTransitionId(), 0, new Point(1, 1));
        t5.setName("T5");
        Transition t6 = new Transition(IdGenerator.getNextTransitionId(), 0, new Point(1, 1));
        t6.setName("T6");
        Transition t7 = new Transition(IdGenerator.getNextTransitionId(), 0, new Point(1, 1));
        t7.setName("T7");
        Transition t8 = new Transition(IdGenerator.getNextTransitionId(), 0, new Point(1, 1));
        t8.setName("T8");
        tl.add(t5);
        tl.add(t6);
        tl.add(t7);
        tl.add(t8);

        Place p5 = new Place(IdGenerator.getNextId(), 0, new Point(1, 1));
        p5.setName("P5");
        Place p6 = new Place(IdGenerator.getNextId(), 0, new Point(1, 1));
        p6.setName("P6");
        Place p7 = new Place(IdGenerator.getNextId(), 0, new Point(1, 1));
        p7.setName("P7");
        Place p8 = new Place(IdGenerator.getNextId(), 0, new Point(1, 1));
        p8.setName("P8");
        Place p9 = new Place(IdGenerator.getNextId(), 0, new Point(1, 1));
        p9.setName("P9");
        Place p10 = new Place(IdGenerator.getNextId(), 0, new Point(1, 1));
        p10.setName("P10");
        Place p11 = new Place(IdGenerator.getNextId(), 0, new Point(1, 1));
        p11.setName("P11");
        pl.add(p5);
        pl.add(p6);
        pl.add(p7);
        pl.add(p8);
        pl.add(p9);
        pl.add(p10);
        pl.add(p11);


        Arc a3 = new Arc(IdGenerator.getNextId(), p8.getElementLocations().get(0), t5.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(IdGenerator.getNextId(), t5.getElementLocations().get(0), p5.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a8 = new Arc(IdGenerator.getNextId(), p5.getElementLocations().get(0), t6.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(IdGenerator.getNextId(), t6.getElementLocations().get(0), p6.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a5 = new Arc(IdGenerator.getNextId(), p6.getElementLocations().get(0), t7.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a9 = new Arc(IdGenerator.getNextId(), t7.getElementLocations().get(0), p9.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);


        Arc a21 = new Arc(IdGenerator.getNextId(), t5.getElementLocations().get(0), p10.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a22 = new Arc(IdGenerator.getNextId(), p10.getElementLocations().get(0), t8.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a23 = new Arc(IdGenerator.getNextId(), t8.getElementLocations().get(0), p11.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a24 = new Arc(IdGenerator.getNextId(), p11.getElementLocations().get(0), t7.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        return new SubnetCalculator.SubNet(SubnetCalculator.SubNetType.TNET, tl, null, null, null, null);
    }

    SubnetCalculator.SubNet creatSubnet9() {
        ArrayList<Transition> tl = new ArrayList<>();
        ArrayList<Place> pl = new ArrayList<>();

        Transition t5 = new Transition(IdGenerator.getNextTransitionId(), 0, new Point(1, 1));
        Transition t6 = new Transition(IdGenerator.getNextTransitionId(), 0, new Point(1, 1));
        Transition t7 = new Transition(IdGenerator.getNextTransitionId(), 0, new Point(1, 1));
        Transition t8 = new Transition(IdGenerator.getNextTransitionId(), 0, new Point(1, 1));
        Transition t9 = new Transition(IdGenerator.getNextTransitionId(), 0, new Point(1, 1));
        Transition t10 = new Transition(IdGenerator.getNextTransitionId(), 0, new Point(1, 1));
        tl.add(t5);
        tl.add(t6);
        tl.add(t7);
        tl.add(t8);
        tl.add(t9);
        tl.add(t10);

        Place p5 = new Place(IdGenerator.getNextId(), 0, new Point(1, 1));
        Place p6 = new Place(IdGenerator.getNextId(), 0, new Point(1, 1));
        Place p7 = new Place(IdGenerator.getNextId(), 0, new Point(1, 1));
        Place p8 = new Place(IdGenerator.getNextId(), 0, new Point(1, 1));
        Place p9 = new Place(IdGenerator.getNextId(), 0, new Point(1, 1));
        Place p10 = new Place(IdGenerator.getNextId(), 0, new Point(1, 1));
        Place p11 = new Place(IdGenerator.getNextId(), 0, new Point(1, 1));
        Place p12 = new Place(IdGenerator.getNextId(), 0, new Point(1, 1));
        Place p13 = new Place(IdGenerator.getNextId(), 0, new Point(1, 1));
        pl.add(p5);
        pl.add(p6);
        pl.add(p7);
        pl.add(p8);
        pl.add(p9);
        pl.add(p10);
        pl.add(p11);
        pl.add(p12);
        pl.add(p13);


        Arc a3 = new Arc(IdGenerator.getNextId(), p8.getElementLocations().get(0), t5.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(IdGenerator.getNextId(), t5.getElementLocations().get(0), p5.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a8 = new Arc(IdGenerator.getNextId(), p5.getElementLocations().get(0), t6.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(IdGenerator.getNextId(), t6.getElementLocations().get(0), p6.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a5 = new Arc(IdGenerator.getNextId(), p6.getElementLocations().get(0), t9.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a14 = new Arc(IdGenerator.getNextId(), t9.getElementLocations().get(0), p12.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a15 = new Arc(IdGenerator.getNextId(), p12.getElementLocations().get(0), t10.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a16 = new Arc(IdGenerator.getNextId(), t10.getElementLocations().get(0), p13.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a17 = new Arc(IdGenerator.getNextId(), p13.getElementLocations().get(0), t7.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a9 = new Arc(IdGenerator.getNextId(), t7.getElementLocations().get(0), p9.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);


        Arc a21 = new Arc(IdGenerator.getNextId(), t5.getElementLocations().get(0), p10.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a22 = new Arc(IdGenerator.getNextId(), p10.getElementLocations().get(0), t8.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a23 = new Arc(IdGenerator.getNextId(), t8.getElementLocations().get(0), p11.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a24 = new Arc(IdGenerator.getNextId(), p11.getElementLocations().get(0), t7.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        return new SubnetCalculator.SubNet(SubnetCalculator.SubNetType.TNET, tl, null, null, null, null);
    }

    SubnetCalculator.SubNet creatSubnet10() {
        ArrayList<Transition> tl = new ArrayList<>();
        ArrayList<Place> pl = new ArrayList<>();
        ArrayList<Node> nl = new ArrayList<>();

        Transition t5 = new Transition(IdGenerator.getNextTransitionId(), 0, new Point(1, 1));
        Transition t6 = new Transition(IdGenerator.getNextTransitionId(), 0, new Point(1, 1));
        Transition t7 = new Transition(IdGenerator.getNextTransitionId(), 0, new Point(1, 1));
        Transition t8 = new Transition(IdGenerator.getNextTransitionId(), 0, new Point(1, 1));
        tl.add(t5);
        tl.add(t6);
        tl.add(t7);
        tl.add(t8);

        Place p5 = new Place(IdGenerator.getNextId(), 0, new Point(1, 1));
        Place p6 = new Place(IdGenerator.getNextId(), 0, new Point(1, 1));
        Place p7 = new Place(IdGenerator.getNextId(), 0, new Point(1, 1));
        Place p8 = new Place(IdGenerator.getNextId(), 0, new Point(1, 1));
        Place p9 = new Place(IdGenerator.getNextId(), 0, new Point(1, 1));
        Place p10 = new Place(IdGenerator.getNextId(), 0, new Point(1, 1));
        pl.add(p5);
        pl.add(p6);
        pl.add(p7);
        pl.add(p8);
        pl.add(p9);
        pl.add(p10);

        Arc a3 = new Arc(IdGenerator.getNextId(), p8.getElementLocations().get(0), t5.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(IdGenerator.getNextId(), t5.getElementLocations().get(0), p5.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a8 = new Arc(IdGenerator.getNextId(), p5.getElementLocations().get(0), t6.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(IdGenerator.getNextId(), t6.getElementLocations().get(0), p6.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a5 = new Arc(IdGenerator.getNextId(), p6.getElementLocations().get(0), t7.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a9 = new Arc(IdGenerator.getNextId(), t7.getElementLocations().get(0), p9.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        Arc a6 = new Arc(IdGenerator.getNextId(), t6.getElementLocations().get(0), p7.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a7 = new Arc(IdGenerator.getNextId(), p7.getElementLocations().get(0), t8.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a10 = new Arc(IdGenerator.getNextId(), t8.getElementLocations().get(0), p10.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        Arc a99 = new Arc(IdGenerator.getNextId(), p10.getElementLocations().get(0), t6.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        return new SubnetCalculator.SubNet(SubnetCalculator.SubNetType.TNET, tl, null, null, null, null);
    }


    SubnetCalculator.SubNet creatSubnet11() {
        ArrayList<Transition> tl = new ArrayList<>();
        ArrayList<Place> pl = new ArrayList<>();
        ArrayList<Node> nl = new ArrayList<>();

        Transition t5 = new Transition(IdGenerator.getNextTransitionId(), 0, new Point(1, 1));
        Transition t6 = new Transition(IdGenerator.getNextTransitionId(), 0, new Point(1, 1));
        Transition t7 = new Transition(IdGenerator.getNextTransitionId(), 0, new Point(1, 1));

        Transition t8 = new Transition(IdGenerator.getNextTransitionId(), 0, new Point(1, 1));
        Transition t9 = new Transition(IdGenerator.getNextTransitionId(), 0, new Point(1, 1));
        tl.add(t5);
        tl.add(t6);
        tl.add(t7);
        //tl.add(t8);

        Place p5 = new Place(IdGenerator.getNextId(), 0, new Point(1, 1));
        Place p6 = new Place(IdGenerator.getNextId(), 0, new Point(1, 1));
        Place p7 = new Place(IdGenerator.getNextId(), 0, new Point(1, 1));
        Place p8 = new Place(IdGenerator.getNextId(), 0, new Point(1, 1));
        Place p9 = new Place(IdGenerator.getNextId(), 0, new Point(1, 1));
        Place p10 = new Place(IdGenerator.getNextId(), 0, new Point(1, 1));
        pl.add(p5);
        pl.add(p6);
        //pl.add(p7);
        pl.add(p8);
        pl.add(p9);
        pl.add(p10);

        Arc a3 = new Arc(IdGenerator.getNextId(), p8.getElementLocations().get(0), t5.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(IdGenerator.getNextId(), t5.getElementLocations().get(0), p5.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a8 = new Arc(IdGenerator.getNextId(), p5.getElementLocations().get(0), t6.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(IdGenerator.getNextId(), t6.getElementLocations().get(0), p6.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a5 = new Arc(IdGenerator.getNextId(), p6.getElementLocations().get(0), t7.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a9 = new Arc(IdGenerator.getNextId(), t7.getElementLocations().get(0), p9.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        Arc a6 = new Arc(IdGenerator.getNextId(), p5.getElementLocations().get(0), t9.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a77 = new Arc(IdGenerator.getNextId(), t9.getElementLocations().get(0), p7.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a7 = new Arc(IdGenerator.getNextId(), p7.getElementLocations().get(0), t8.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a10 = new Arc(IdGenerator.getNextId(), t8.getElementLocations().get(0), p5.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);


        return new SubnetCalculator.SubNet(SubnetCalculator.SubNetType.TNET, tl, null, null, null, null);
    }

    SubnetCalculator.SubNet creatSubnet12() {
        ArrayList<Transition> tl = new ArrayList<>();
        ArrayList<Place> pl = new ArrayList<>();
        ArrayList<Node> nl = new ArrayList<>();

        Transition t5 = new Transition(IdGenerator.getNextTransitionId(), 0, new Point(1, 1));
        Transition t6 = new Transition(IdGenerator.getNextTransitionId(), 0, new Point(1, 1));
        Transition t7 = new Transition(IdGenerator.getNextTransitionId(), 0, new Point(1, 1));

        Transition t8 = new Transition(IdGenerator.getNextTransitionId(), 0, new Point(1, 1));
        Transition t9 = new Transition(IdGenerator.getNextTransitionId(), 0, new Point(1, 1));
        //      tl.add(t5);
        //      tl.add(t6);
        //      tl.add(t7);
        tl.add(t8);
        tl.add(t9);

        Place p5 = new Place(IdGenerator.getNextId(), 0, new Point(1, 1));
        Place p6 = new Place(IdGenerator.getNextId(), 0, new Point(1, 1));
        Place p7 = new Place(IdGenerator.getNextId(), 0, new Point(1, 1));
        Place p8 = new Place(IdGenerator.getNextId(), 0, new Point(1, 1));
        Place p9 = new Place(IdGenerator.getNextId(), 0, new Point(1, 1));
        Place p10 = new Place(IdGenerator.getNextId(), 0, new Point(1, 1));
        pl.add(p5);
        //     pl.add(p6);
        pl.add(p7);
        //   pl.add(p8);
        //   pl.add(p9);
        //   pl.add(p10);

        Arc a3 = new Arc(IdGenerator.getNextId(), p8.getElementLocations().get(0), t5.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(IdGenerator.getNextId(), t5.getElementLocations().get(0), p5.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a8 = new Arc(IdGenerator.getNextId(), p5.getElementLocations().get(0), t6.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(IdGenerator.getNextId(), t6.getElementLocations().get(0), p6.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a5 = new Arc(IdGenerator.getNextId(), p6.getElementLocations().get(0), t7.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a9 = new Arc(IdGenerator.getNextId(), t7.getElementLocations().get(0), p9.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        Arc a6 = new Arc(IdGenerator.getNextId(), p5.getElementLocations().get(0), t9.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a77 = new Arc(IdGenerator.getNextId(), t9.getElementLocations().get(0), p7.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a7 = new Arc(IdGenerator.getNextId(), p7.getElementLocations().get(0), t8.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a10 = new Arc(IdGenerator.getNextId(), t8.getElementLocations().get(0), p5.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);


        return new SubnetCalculator.SubNet(SubnetCalculator.SubNetType.TNET, tl, null, null, null, null);
    }

    SubnetCalculator.SubNet creatSubnet13() {
        ArrayList<Transition> tl = new ArrayList<>();
        ArrayList<Place> pl = new ArrayList<>();

        Transition t5 = new Transition(IdGenerator.getNextTransitionId(), 0, new Point(1, 1));
        Transition t6 = new Transition(IdGenerator.getNextTransitionId(), 0, new Point(1, 1));
        Transition t7 = new Transition(IdGenerator.getNextTransitionId(), 0, new Point(1, 1));
        Transition t8 = new Transition(IdGenerator.getNextTransitionId(), 0, new Point(1, 1));
        tl.add(t5);
        tl.add(t6);
        tl.add(t7);
        tl.add(t8);

        Place p5 = new Place(IdGenerator.getNextId(), 0, new Point(1, 1));
        Place p6 = new Place(IdGenerator.getNextId(), 0, new Point(1, 1));
        Place p7 = new Place(IdGenerator.getNextId(), 0, new Point(1, 1));
        Place p8 = new Place(IdGenerator.getNextId(), 0, new Point(1, 1));
        Place p9 = new Place(IdGenerator.getNextId(), 0, new Point(1, 1));
        Place p10 = new Place(IdGenerator.getNextId(), 0, new Point(1, 1));
        Place p11 = new Place(IdGenerator.getNextId(), 0, new Point(1, 1));
        pl.add(p5);
        pl.add(p6);
        pl.add(p7);
        pl.add(p8);
        pl.add(p9);
        pl.add(p10);
        pl.add(p11);


        Arc a3 = new Arc(IdGenerator.getNextId(), p8.getElementLocations().get(0), t5.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(IdGenerator.getNextId(), t5.getElementLocations().get(0), p5.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a8 = new Arc(IdGenerator.getNextId(), p5.getElementLocations().get(0), t6.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(IdGenerator.getNextId(), t6.getElementLocations().get(0), p6.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a5 = new Arc(IdGenerator.getNextId(), p6.getElementLocations().get(0), t7.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a9 = new Arc(IdGenerator.getNextId(), t7.getElementLocations().get(0), p9.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);


        Arc a21 = new Arc(IdGenerator.getNextId(), p11.getElementLocations().get(0), t5.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a24 = new Arc(IdGenerator.getNextId(), t7.getElementLocations().get(0), p11.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        return new SubnetCalculator.SubNet(SubnetCalculator.SubNetType.TNET, tl, null, null, null, null);
    }


    SubnetCalculator.SubNet creatSubnet14() {
        ArrayList<Transition> tl = new ArrayList<>();
        ArrayList<Place> pl = new ArrayList<>();

        Transition t5 = new Transition(IdGenerator.getNextTransitionId(), 0, new Point(1, 1));
        Transition t6 = new Transition(IdGenerator.getNextTransitionId(), 0, new Point(1, 1));
        Transition t7 = new Transition(IdGenerator.getNextTransitionId(), 0, new Point(1, 1));
        Transition t8 = new Transition(IdGenerator.getNextTransitionId(), 0, new Point(1, 1));
        Transition t9 = new Transition(IdGenerator.getNextTransitionId(), 0, new Point(1, 1));
        Transition t10 = new Transition(IdGenerator.getNextTransitionId(), 0, new Point(1, 1));
        Transition t11 = new Transition(IdGenerator.getNextTransitionId(), 0, new Point(1, 1));
        Transition t12 = new Transition(IdGenerator.getNextTransitionId(), 0, new Point(1, 1));
        tl.add(t5);
        tl.add(t6);
        tl.add(t7);
        tl.add(t8);
        tl.add(t9);
        tl.add(t10);
        tl.add(t11);
        tl.add(t12);

        Place p5 = new Place(IdGenerator.getNextId(), 0, new Point(1, 1));
        Place p6 = new Place(IdGenerator.getNextId(), 0, new Point(1, 1));
        Place p7 = new Place(IdGenerator.getNextId(), 0, new Point(1, 1));
        Place p8 = new Place(IdGenerator.getNextId(), 0, new Point(1, 1));
        Place p9 = new Place(IdGenerator.getNextId(), 0, new Point(1, 1));

        Place p11 = new Place(IdGenerator.getNextId(), 0, new Point(1, 1));
        Place p12 = new Place(IdGenerator.getNextId(), 0, new Point(1, 1));
        Place p13 = new Place(IdGenerator.getNextId(), 0, new Point(1, 1));

        Place p14 = new Place(IdGenerator.getNextId(), 0, new Point(1, 1));
        Place p15 = new Place(IdGenerator.getNextId(), 0, new Point(1, 1));
        Place p16 = new Place(IdGenerator.getNextId(), 0, new Point(1, 1));
        pl.add(p5);
        pl.add(p6);
        pl.add(p7);
        pl.add(p8);
        pl.add(p9);
        pl.add(p11);
        pl.add(p12);
        pl.add(p13);
        pl.add(p14);
        pl.add(p15);
        pl.add(p16);


        Arc a321 = new Arc(IdGenerator.getNextId(), p14.getElementLocations().get(0), t11.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a322 = new Arc(IdGenerator.getNextId(), t11.getElementLocations().get(0), p13.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        Arc a21 = new Arc(IdGenerator.getNextId(), p13.getElementLocations().get(0), t10.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a22 = new Arc(IdGenerator.getNextId(), p8.getElementLocations().get(0), t10.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);


        Arc a112 = new Arc(IdGenerator.getNextId(), t8.getElementLocations().get(0), p12.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a11 = new Arc(IdGenerator.getNextId(), p12.getElementLocations().get(0), t9.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a12 = new Arc(IdGenerator.getNextId(), t9.getElementLocations().get(0), p11.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a13 = new Arc(IdGenerator.getNextId(), p11.getElementLocations().get(0), t5.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        Arc a3 = new Arc(IdGenerator.getNextId(), t5.getElementLocations().get(0), p8.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(IdGenerator.getNextId(), p5.getElementLocations().get(0), t5.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a8 = new Arc(IdGenerator.getNextId(), t6.getElementLocations().get(0), p5.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(IdGenerator.getNextId(), p6.getElementLocations().get(0), t6.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a5 = new Arc(IdGenerator.getNextId(), t7.getElementLocations().get(0), p6.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a9 = new Arc(IdGenerator.getNextId(), p9.getElementLocations().get(0), t7.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        Arc a6 = new Arc(IdGenerator.getNextId(), t10.getElementLocations().get(0), p7.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);


        Arc a46 = new Arc(IdGenerator.getNextId(), t6.getElementLocations().get(0), p15.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a47 = new Arc(IdGenerator.getNextId(), p15.getElementLocations().get(0), t12.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a410 = new Arc(IdGenerator.getNextId(), t12.getElementLocations().get(0), p16.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        return new SubnetCalculator.SubNet(SubnetCalculator.SubNetType.TNET, tl, null, null, null, null);
    }


    SubnetCalculator.SubNet creatSubnet15() {
        ArrayList<Transition> tl = new ArrayList<>();
        ArrayList<Place> pl = new ArrayList<>();

        Transition t5 = new Transition(IdGenerator.getNextTransitionId(), 0, new Point(1, 1));
        Transition t6 = new Transition(IdGenerator.getNextTransitionId(), 0, new Point(1, 1));
        Transition t7 = new Transition(IdGenerator.getNextTransitionId(), 0, new Point(1, 1));
        Transition t8 = new Transition(IdGenerator.getNextTransitionId(), 0, new Point(1, 1));
        Transition t9 = new Transition(IdGenerator.getNextTransitionId(), 0, new Point(1, 1));
        Transition t10 = new Transition(IdGenerator.getNextTransitionId(), 0, new Point(1, 1));
        Transition t11 = new Transition(IdGenerator.getNextTransitionId(), 0, new Point(1, 1));
        Transition t12 = new Transition(IdGenerator.getNextTransitionId(), 0, new Point(1, 1));
        tl.add(t5);
        tl.add(t6);
        tl.add(t7);
        tl.add(t8);
        tl.add(t9);
        tl.add(t10);
        tl.add(t11);
        tl.add(t12);

        Place p5 = new Place(IdGenerator.getNextId(), 0, new Point(1, 1));
        Place p6 = new Place(IdGenerator.getNextId(), 0, new Point(1, 1));
        Place p7 = new Place(IdGenerator.getNextId(), 0, new Point(1, 1));
        Place p8 = new Place(IdGenerator.getNextId(), 0, new Point(1, 1));
        Place p9 = new Place(IdGenerator.getNextId(), 0, new Point(1, 1));

        Place p11 = new Place(IdGenerator.getNextId(), 0, new Point(1, 1));
        Place p12 = new Place(IdGenerator.getNextId(), 0, new Point(1, 1));
        Place p13 = new Place(IdGenerator.getNextId(), 0, new Point(1, 1));

        Place p14 = new Place(IdGenerator.getNextId(), 0, new Point(1, 1));
        Place p15 = new Place(IdGenerator.getNextId(), 0, new Point(1, 1));
        Place p16 = new Place(IdGenerator.getNextId(), 0, new Point(1, 1));
        pl.add(p5);
        pl.add(p6);
        pl.add(p7);
        pl.add(p8);
        pl.add(p9);
        pl.add(p11);
        pl.add(p12);
        pl.add(p13);
        pl.add(p14);
        pl.add(p15);
        pl.add(p16);


        Arc a321 = new Arc(IdGenerator.getNextId(), p14.getElementLocations().get(0), t11.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a322 = new Arc(IdGenerator.getNextId(), t11.getElementLocations().get(0), p13.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        Arc a21 = new Arc(IdGenerator.getNextId(), p13.getElementLocations().get(0), t10.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a22 = new Arc(IdGenerator.getNextId(), t10.getElementLocations().get(0), p8.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);


        Arc a11 = new Arc(IdGenerator.getNextId(), p12.getElementLocations().get(0), t9.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a12 = new Arc(IdGenerator.getNextId(), t9.getElementLocations().get(0), p11.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a13 = new Arc(IdGenerator.getNextId(), p11.getElementLocations().get(0), t5.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        Arc a3 = new Arc(IdGenerator.getNextId(), p8.getElementLocations().get(0), t5.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(IdGenerator.getNextId(), t5.getElementLocations().get(0), p5.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a8 = new Arc(IdGenerator.getNextId(), p5.getElementLocations().get(0), t6.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(IdGenerator.getNextId(), t6.getElementLocations().get(0), p6.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a5 = new Arc(IdGenerator.getNextId(), p6.getElementLocations().get(0), t7.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a9 = new Arc(IdGenerator.getNextId(), t7.getElementLocations().get(0), p9.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        Arc a6 = new Arc(IdGenerator.getNextId(), p7.getElementLocations().get(0), t10.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);


        Arc a46 = new Arc(IdGenerator.getNextId(), t6.getElementLocations().get(0), p15.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a47 = new Arc(IdGenerator.getNextId(), p15.getElementLocations().get(0), t12.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a410 = new Arc(IdGenerator.getNextId(), t12.getElementLocations().get(0), p16.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        return new SubnetCalculator.SubNet(SubnetCalculator.SubNetType.TNET, tl, null, null, null, null);
    }


    SubnetCalculator.SubNet creatSubnet16() {
        ArrayList<Transition> tl = new ArrayList<>();
        ArrayList<Place> pl = new ArrayList<>();
        ArrayList<Node> nl = new ArrayList<>();

        Transition t5 = new Transition(IdGenerator.getNextTransitionId(), 0, new Point(1, 1));
        Transition t6 = new Transition(IdGenerator.getNextTransitionId(), 0, new Point(1, 1));
        Transition t7 = new Transition(IdGenerator.getNextTransitionId(), 0, new Point(1, 1));
        Transition t8 = new Transition(IdGenerator.getNextTransitionId(), 0, new Point(1, 1));
        Transition t9 = new Transition(IdGenerator.getNextTransitionId(), 0, new Point(1, 1));
        Transition t10 = new Transition(IdGenerator.getNextTransitionId(), 0, new Point(1, 1));
        tl.add(t5);
        tl.add(t6);
        tl.add(t7);
        tl.add(t8);
        tl.add(t9);
        tl.add(t10);

        Place p5 = new Place(IdGenerator.getNextId(), 0, new Point(1, 1));
        Place p6 = new Place(IdGenerator.getNextId(), 0, new Point(1, 1));
        Place p7 = new Place(IdGenerator.getNextId(), 0, new Point(1, 1));
        Place p8 = new Place(IdGenerator.getNextId(), 0, new Point(1, 1));
        Place p9 = new Place(IdGenerator.getNextId(), 0, new Point(1, 1));
        Place p10 = new Place(IdGenerator.getNextId(), 0, new Point(1, 1));
        Place p11 = new Place(IdGenerator.getNextId(), 0, new Point(1, 1));
        Place p12 = new Place(IdGenerator.getNextId(), 0, new Point(1, 1));
        Place p13 = new Place(IdGenerator.getNextId(), 0, new Point(1, 1));
        Place p14 = new Place(IdGenerator.getNextId(), 0, new Point(1, 1));
        pl.add(p5);
        pl.add(p6);
        pl.add(p7);
        pl.add(p8);
        pl.add(p9);
        pl.add(p10);
        pl.add(p11);
        pl.add(p12);
        pl.add(p13);
        pl.add(p14);

        Arc a3 = new Arc(IdGenerator.getNextId(), p8.getElementLocations().get(0), t5.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(IdGenerator.getNextId(), t5.getElementLocations().get(0), p5.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a8 = new Arc(IdGenerator.getNextId(), p5.getElementLocations().get(0), t6.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(IdGenerator.getNextId(), t6.getElementLocations().get(0), p6.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a5 = new Arc(IdGenerator.getNextId(), p6.getElementLocations().get(0), t7.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a9 = new Arc(IdGenerator.getNextId(), t7.getElementLocations().get(0), p9.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        Arc a6 = new Arc(IdGenerator.getNextId(), t6.getElementLocations().get(0), p7.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a7 = new Arc(IdGenerator.getNextId(), p7.getElementLocations().get(0), t8.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a10 = new Arc(IdGenerator.getNextId(), t8.getElementLocations().get(0), p10.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a11 = new Arc(IdGenerator.getNextId(), p10.getElementLocations().get(0), t6.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);


        Arc a36 = new Arc(IdGenerator.getNextId(), t5.getElementLocations().get(0), p11.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a37 = new Arc(IdGenerator.getNextId(), p11.getElementLocations().get(0), t9.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a30 = new Arc(IdGenerator.getNextId(), t9.getElementLocations().get(0), p12.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a99 = new Arc(IdGenerator.getNextId(), p12.getElementLocations().get(0), t5.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);


        Arc a46 = new Arc(IdGenerator.getNextId(), t7.getElementLocations().get(0), p13.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a47 = new Arc(IdGenerator.getNextId(), p13.getElementLocations().get(0), t10.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a40 = new Arc(IdGenerator.getNextId(), t10.getElementLocations().get(0), p14.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a599 = new Arc(IdGenerator.getNextId(), p14.getElementLocations().get(0), t7.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);


        return new SubnetCalculator.SubNet(SubnetCalculator.SubNetType.TNET, tl, null, null, null, null);
    }


    SubnetCalculator.SubNet creatSubnet17() {
        ArrayList<Transition> tl = new ArrayList<>();
        ArrayList<Place> pl = new ArrayList<>();
        ArrayList<Node> nl = new ArrayList<>();

        Transition t5 = new Transition(IdGenerator.getNextTransitionId(), 0, new Point(1, 1));
        Transition t6 = new Transition(IdGenerator.getNextTransitionId(), 0, new Point(1, 1));
        Transition t7 = new Transition(IdGenerator.getNextTransitionId(), 0, new Point(1, 1));
        //Transition t8 = new Transition(IdGenerator.getNextTransitionId(), 0, new Point(1, 1));
        Transition t9 = new Transition(IdGenerator.getNextTransitionId(), 0, new Point(1, 1));
        //Transition t10 = new Transition(IdGenerator.getNextTransitionId(), 0, new Point(1, 1));
        Transition t11 = new Transition(IdGenerator.getNextTransitionId(), 0, new Point(1, 1));
        Transition t12 = new Transition(IdGenerator.getNextTransitionId(), 0, new Point(1, 1));
        Transition t13 = new Transition(IdGenerator.getNextTransitionId(), 0, new Point(1, 1));
        Transition t14 = new Transition(IdGenerator.getNextTransitionId(), 0, new Point(1, 1));
        tl.add(t5);
        tl.add(t6);
        tl.add(t7);
        //tl.add(t8);
        tl.add(t9);
        tl.add(t11);
        tl.add(t12);
        tl.add(t13);
        tl.add(t14);

        Place p5 = new Place(IdGenerator.getNextId(), 0, new Point(1, 1));
        Place p6 = new Place(IdGenerator.getNextId(), 0, new Point(1, 1));
        Place p8 = new Place(IdGenerator.getNextId(), 0, new Point(1, 1));
        Place p9 = new Place(IdGenerator.getNextId(), 0, new Point(1, 1));
        Place p11 = new Place(IdGenerator.getNextId(), 0, new Point(1, 1));
        Place p12 = new Place(IdGenerator.getNextId(), 0, new Point(1, 1));
        Place p13 = new Place(IdGenerator.getNextId(), 0, new Point(1, 1));
        Place p14 = new Place(IdGenerator.getNextId(), 0, new Point(1, 1));
        Place p15 = new Place(IdGenerator.getNextId(), 0, new Point(1, 1));
        Place p16 = new Place(IdGenerator.getNextId(), 0, new Point(1, 1));
        pl.add(p5);
        pl.add(p6);
        pl.add(p8);
        pl.add(p9);
        pl.add(p11);
        pl.add(p12);
        pl.add(p13);
        pl.add(p14);
        pl.add(p15);
        pl.add(p16);

        Arc a3 = new Arc(IdGenerator.getNextId(), p8.getElementLocations().get(0), t5.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(IdGenerator.getNextId(), t5.getElementLocations().get(0), p5.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a8 = new Arc(IdGenerator.getNextId(), p5.getElementLocations().get(0), t6.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(IdGenerator.getNextId(), t6.getElementLocations().get(0), p6.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a5 = new Arc(IdGenerator.getNextId(), p6.getElementLocations().get(0), t7.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a9 = new Arc(IdGenerator.getNextId(), t7.getElementLocations().get(0), p9.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);


        Arc a36 = new Arc(IdGenerator.getNextId(), t5.getElementLocations().get(0), p11.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a37 = new Arc(IdGenerator.getNextId(), p11.getElementLocations().get(0), t9.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a30 = new Arc(IdGenerator.getNextId(), t9.getElementLocations().get(0), p12.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a99 = new Arc(IdGenerator.getNextId(), p12.getElementLocations().get(0), t5.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);


        Arc a536 = new Arc(IdGenerator.getNextId(), t7.getElementLocations().get(0), p13.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a537 = new Arc(IdGenerator.getNextId(), p13.getElementLocations().get(0), t11.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a636 = new Arc(IdGenerator.getNextId(), t11.getElementLocations().get(0), p14.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a637 = new Arc(IdGenerator.getNextId(), p14.getElementLocations().get(0), t12.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        Arc a736 = new Arc(IdGenerator.getNextId(), t7.getElementLocations().get(0), p15.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a737 = new Arc(IdGenerator.getNextId(), p15.getElementLocations().get(0), t13.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a836 = new Arc(IdGenerator.getNextId(), t13.getElementLocations().get(0), p16.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a837 = new Arc(IdGenerator.getNextId(), p16.getElementLocations().get(0), t14.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        return new SubnetCalculator.SubNet(SubnetCalculator.SubNetType.TNET, tl, null, null, null, null);
    }

    SubnetCalculator.SubNet creatSubnet18() {
        ArrayList<Transition> tl = new ArrayList<>();
        ArrayList<Place> pl = new ArrayList<>();
        ArrayList<Node> nl = new ArrayList<>();

        Transition t5 = new Transition(IdGenerator.getNextTransitionId(), 0, new Point(1, 1));
        Transition t6 = new Transition(IdGenerator.getNextTransitionId(), 0, new Point(1, 1));
        Transition t7 = new Transition(IdGenerator.getNextTransitionId(), 0, new Point(1, 1));
        //Transition t9 = new Transition(IdGenerator.getNextTransitionId(), 0, new Point(1, 1));
        Transition t11 = new Transition(IdGenerator.getNextTransitionId(), 0, new Point(1, 1));
        Transition t12 = new Transition(IdGenerator.getNextTransitionId(), 0, new Point(1, 1));
        Transition t13 = new Transition(IdGenerator.getNextTransitionId(), 0, new Point(1, 1));
        Transition t14 = new Transition(IdGenerator.getNextTransitionId(), 0, new Point(1, 1));
        tl.add(t5);
        tl.add(t6);
        tl.add(t7);
        //tl.add(t9);
        tl.add(t11);
        tl.add(t12);
        tl.add(t13);
        tl.add(t14);

        Place p5 = new Place(IdGenerator.getNextId(), 0, new Point(1, 1));
        Place p6 = new Place(IdGenerator.getNextId(), 0, new Point(1, 1));
        Place p8 = new Place(IdGenerator.getNextId(), 0, new Point(1, 1));
        Place p9 = new Place(IdGenerator.getNextId(), 0, new Point(1, 1));
        Place p11 = new Place(IdGenerator.getNextId(), 0, new Point(1, 1));
        Place p12 = new Place(IdGenerator.getNextId(), 0, new Point(1, 1));
        Place p13 = new Place(IdGenerator.getNextId(), 0, new Point(1, 1));
        Place p14 = new Place(IdGenerator.getNextId(), 0, new Point(1, 1));
        Place p15 = new Place(IdGenerator.getNextId(), 0, new Point(1, 1));
        Place p16 = new Place(IdGenerator.getNextId(), 0, new Point(1, 1));
        Place p17 = new Place(IdGenerator.getNextId(), 0, new Point(1, 1));
        Place p18 = new Place(IdGenerator.getNextId(), 0, new Point(1, 1));
        pl.add(p5);
        pl.add(p6);
        pl.add(p8);
        pl.add(p9);
        pl.add(p11);
        pl.add(p12);
        pl.add(p13);
        pl.add(p14);
        pl.add(p15);
        pl.add(p16);
        pl.add(p17);
        pl.add(p18);

        Arc a3 = new Arc(IdGenerator.getNextId(), p8.getElementLocations().get(0), t5.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(IdGenerator.getNextId(), t5.getElementLocations().get(0), p5.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a8 = new Arc(IdGenerator.getNextId(), p5.getElementLocations().get(0), t6.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(IdGenerator.getNextId(), t6.getElementLocations().get(0), p6.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a5 = new Arc(IdGenerator.getNextId(), p6.getElementLocations().get(0), t7.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a9 = new Arc(IdGenerator.getNextId(), t7.getElementLocations().get(0), p9.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);


        Arc a36 = new Arc(IdGenerator.getNextId(), t5.getElementLocations().get(0), p11.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a99 = new Arc(IdGenerator.getNextId(), p12.getElementLocations().get(0), t5.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);


        Arc a536 = new Arc(IdGenerator.getNextId(), p13.getElementLocations().get(0), t7.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a537 = new Arc(IdGenerator.getNextId(), t11.getElementLocations().get(0), p13.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a636 = new Arc(IdGenerator.getNextId(), p14.getElementLocations().get(0), t11.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a637 = new Arc(IdGenerator.getNextId(), t12.getElementLocations().get(0), p14.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a538 = new Arc(IdGenerator.getNextId(), p17.getElementLocations().get(0), t12.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        Arc a736 = new Arc(IdGenerator.getNextId(), p15.getElementLocations().get(0), t7.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a737 = new Arc(IdGenerator.getNextId(), t13.getElementLocations().get(0), p15.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a836 = new Arc(IdGenerator.getNextId(), p16.getElementLocations().get(0), t13.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a837 = new Arc(IdGenerator.getNextId(), t14.getElementLocations().get(0), p16.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a530 = new Arc(IdGenerator.getNextId(), p18.getElementLocations().get(0), t14.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        return new SubnetCalculator.SubNet(SubnetCalculator.SubNetType.TNET, tl, null, null, null, null);
    }
/*
    void ceateTestNets(){

        //net 0
        ArrayList<Transition> tl = new ArrayList<>();
        Transition t1 = new Transition(0,0,new Point(1,1));
        tl.add(t1);

        ArrayList<Place> pl = new ArrayList<>();
        Place p1 = new Place(99,0,new Point(1,1));
        pl.add(p1);

        ArrayList<Node> nl = new ArrayList<>();
        nl.add(p1);
        nl.add(t1);

        Arc a1 = new Arc(50,t1.getElementLocations().get(0),p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();

        al.add(a1);

        //net 1
        Transition t2 = new Transition(2,0,new Point(1,1));
        tl.add(t2);

        Place p2 = new Place(99,0,new Point(1,1));
        pl.add(p2);

        nl.add(p2);
        nl.add(t2);

        Arc a2 = new Arc(51,p2.getElementLocations().get(0),t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        al.add(a1);

        //net 2
        Transition t3 = new Transition(3,0,new Point(1,1));
        Transition t4 = new Transition(4,0,new Point(1,1));
        tl.add(t3);
        tl.add(t4);

        Place p3 = new Place(103,0,new Point(1,1));
        Place p4 = new Place(104,0,new Point(1,1));
        pl.add(p3);
        pl.add(p4);

        nl.add(p3);
        nl.add(p4);
        nl.add(t3);
        nl.add(t4);

        Arc a3 = new Arc(53,p3.getElementLocations().get(0),t3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(54,t3.getElementLocations().get(0),p4.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a5 = new Arc(55,p4.getElementLocations().get(0),t4.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        al.add(a3);
        al.add(a4);
        al.add(a5);

        //nrt3
        Transition t5 = new Transition(5,0,new Point(1,1));
        Transition t6 = new Transition(6,0,new Point(1,1));
        Transition t7 = new Transition(7,0,new Point(1,1));
        tl.add(t5);
        tl.add(t6);
        tl.add(t7);

        Place p5 = new Place(103,0,new Point(1,1));
        Place p6 = new Place(104,0,new Point(1,1));
        pl.add(p5);
        pl.add(p6);

        nl.add(p5);
        nl.add(p6);
        nl.add(t5);
        nl.add(t6);
        nl.add(t7);

        Arc a3 = new Arc(53,p3.getElementLocations().get(0),t3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(54,t3.getElementLocations().get(0),p4.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a5 = new Arc(55,p4.getElementLocations().get(0),t4.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        al.add(a3);
        al.add(a4);
        al.add(a5);



        PetriNet pn = new PetriNet(nl,al);
        tc = new InvariantsCalculator(pl,tl,al,true);
        tc.generateInvariantsForTest(pn);
        SubnetCalculator.compileTestElements(tl,pl,nl,tc.getInvariants(true),tc.getInvariants(false));

    }





    void ceateTestNet(){
        ArrayList<Transition> tl = new ArrayList<>();
        Transition t1 = new Transition(0,0,new Point(1,1));
        tl.add(t1);
        Transition t2 = new Transition(1,0,new Point(1,2));
        tl.add(t2);
        Transition t3 = new Transition(2,0,new Point(1,3));
        tl.add(t3);

        ArrayList<Place> pl = new ArrayList<>();
        Place p1 = new Place(99,0,new Point(1,1));
        pl.add(p1);
        Place p2 = new Place(98,0,new Point(1,1));
        pl.add(p2);

        ArrayList<Node> nl = new ArrayList<>();
        nl.add(p1);
        nl.add(p2);
        nl.add(t2);
        nl.add(t3);
        nl.add(t1);

        Arc a1 = new Arc(50,t1.getElementLocations().get(0),p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(51,p1.getElementLocations().get(0),t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(52,t2.getElementLocations().get(0),p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(53,p2.getElementLocations().get(0),t3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();

        al.add(a1);
        al.add(a2);
        al.add(a3);
        al.add(a4);


        PetriNet pn = new PetriNet(nl,al);
        tc = new InvariantsCalculator(pl,tl,al,true);
        tc.generateInvariantsForTest(pn);
        SubnetCalculator.compileTestElements(tl,pl,nl,tc.getInvariants(true),tc.getInvariants(false));

    }
    */

    public void saveSubnet(GreatCommonSubnet gcs) {
        ArrayList<ElementLocation> listOfElements = new ArrayList<>();

        for (SubnetComparator.PartialSubnetElements pse : gcs.psel) {
            for (Arc a : pse.partialArcs
            ) {
                System.out.println("Arc : " + a.getStartNode().getType() + " " +a.getStartNode().getName() + " - > "  + a.getEndNode().getType() + " " +a.getEndNode().getName());
            }

            for (Node n : pse.partialNodes
            ) {
                System.out.println("Node " + n.getName());

                //for (ElementLocation el : n.getElementLocations()) {
                //if (el.isSelected()) {
                //   listOfElements.add(el);
                //}
                // }
            }
        }


        for (SubnetComparator.PartialSubnetElements pse : gcs.psel) {
            for (Arc a : pse.partialArcs
            ) {
                if(!listOfElements.contains(a.getStartLocation()))
                {
                    listOfElements.add(a.getStartLocation());
                }

                if(!listOfElements.contains(a.getEndLocation()))
                {
                    listOfElements.add(a.getEndLocation());
                }
            }
        }

        /*
        for (SubnetComparator.PartialSubnetElements pse : gcs.psel) {
            for (Node n : pse.partialNodes
            ) {
                for (ElementLocation el : n.getElementLocations())
                {
                    //if (el.isSelected()) {
                    listOfElements.add(el);
                    //}
                }
            }
        }
        */

        IOprotocols io = new IOprotocols();
        for (SubnetComparator.PartialSubnetElements pse: gcs.psel
        ) {
            io.exportSubnet(listOfElements,pse.partialArcs);
        }
        //io.exportSubnet(listOfElements,gcs.psel.get(0).partialArcs);
    }

    public void saveSubnet(SubnetCalculator.SubNet sn) {
        ArrayList<ElementLocation> listOfElements = new ArrayList<>();

        for (Arc a  : sn.getSubArcs()) {
            if(!listOfElements.contains(a.getStartLocation()))
            {
                listOfElements.add(a.getStartLocation());
            }

            if(!listOfElements.contains(a.getEndLocation()))
            {
                listOfElements.add(a.getEndLocation());
            }
        }


        /*
        for (SubnetComparator.PartialSubnetElements pse : gcs.psel) {
            for (Node n : pse.partialNodes
            ) {
                for (ElementLocation el : n.getElementLocations())
                {
                    //if (el.isSelected()) {
                    listOfElements.add(el);
                    //}
                }
            }
        }
        */

        IOprotocols io = new IOprotocols();
        io.exportSubnet(listOfElements,sn.subArcs);
    }
}

