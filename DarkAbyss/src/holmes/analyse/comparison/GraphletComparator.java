package holmes.analyse.comparison;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

public class GraphletComparator {

    int orbNumber = 98;
    int testNetExtensionsNumber = 6;

    GraphletComparator()
    {

    }

    public GraphletComparator(int orb)
    {
        this.orbNumber = orb;
    }

    public  void compare()
    {
        for(int i = 0 ; i < 10 ; i ++)
        {
            for(int  j = 0 ; j < 10 ; j++)
            {
                for(int p = 0 ; p < 10 ; p++)
                {
                    double[][] comparisonTable = new double[testNetExtensionsNumber][testNetExtensionsNumber];
                    for(int variant = 0 ; variant < testNetExtensionsNumber ; variant++)
                    {
                        for(int variant2 = 0 ; variant2 < testNetExtensionsNumber ; variant2++)
                        {
                            if(variant!=variant2) {
                                comparisonTable[variant][variant2]=calcDGDDA(getPath(i,j,p,getName(variant)),getPath(i,j,p,getName(variant2)));
                            }
                            else
                            {
                                comparisonTable[variant][variant2]=-1;
                            }
                        }
                    }

                    try {
                        FileWriter myWriter = new FileWriter(getPath(i,j,p,"WYNIK"));
                        for (double[] line : comparisonTable) {
                            for(int k = 0 ; k < line.length ; k++) {
                                String str = String.valueOf(line[k]);
                                if(k+1<line.length)
                                {
                                    str+=",";
                                }
                                myWriter.write(str);
                            }
                            myWriter.write("\n");
                        }
                        myWriter.close();
                        System.out.println("Successfully wrote to the file - i:"+i + " j:" + j +" p:"+ p);
                    } catch (IOException e) {
                        System.out.println("An error occurred - i:"+i + " j:" + j +" p:"+ p);
                        e.printStackTrace();
                    }
                }
            }
        }

		/*
		GraphletComparator gc = new GraphletComparator();

		double result = gc.calcDGDDA("/home/Szavislav/Eksperyment/Wyniki/i0j0/i0j0p0/i0j0p0-BASE-DGDDA.txt","/home/Szavislav/Eksperyment/Wyniki/i0j0/i0j0p0/i0j0p0-S4VARIANT-DGDDA.txt");
		System.out.println("-------B-S4 " + result);
		result = gc.calcDGDDA("/home/Szavislav/Eksperyment/Wyniki/i0j0/i0j0p0/i0j0p0-BASE-DGDDA.txt","/home/Szavislav/Eksperyment/Wyniki/i0j0/i0j0p0/i0j0p0-K4LVARIANT-DGDDA.txt");
		System.out.println("-------B-S4L " + result);
		result = gc.calcDGDDA("/home/Szavislav/Eksperyment/Wyniki/i0j0/i0j0p0/i0j0p0-BASE-DGDDA.txt","/home/Szavislav/Eksperyment/Wyniki/i0j0/i0j0p0/i0j0p0-K4LkVARIANT-DGDDA.txt");
		System.out.println("-------B-S4Lk " + result);
		result = gc.calcDGDDA("/home/Szavislav/Eksperyment/Wyniki/i0j0/i0j0p0/i0j0p0-BASE-DGDDA.txt","/home/Szavislav/Eksperyment/Wyniki/i0j0/i0j0p0/i0j0p0-E2VARIANT-DGDDA.txt");
		System.out.println("-------B-E2 " + result);
		result = gc.calcDGDDA("/home/Szavislav/Eksperyment/Wyniki/i0j0/i0j0p0/i0j0p0-BASE-DGDDA.txt","/home/Szavislav/Eksperyment/Wyniki/i0j0/i0j0p0/i0j0p0-C6VARIANT-DGDDA.txt");
		System.out.println("-------B-C6 " + result);

		result = gc.calcDGDDA("/home/Szavislav/Eksperyment/Wyniki/i0j0/i0j0p0/i0j0p0-K4LVARIANT-DGDDA.txt","/home/Szavislav/Eksperyment/Wyniki/i0j0/i0j0p0/i0j0p0-K4LkVARIANT-DGDDA.txt");
		System.out.println("-------S4L-S4Lk " + result);
		*/

    }

    public String getPath(int i , int j, int p, String name)
    {
        return "/home/Szavislav/Eksperyment/Wyniki/i"+i+"j"+j+"/i"+i+"j"+j+"p"+p+"/i"+i+"j"+j+"p"+p+"-"+name+"-DGDDA.txt";
    }

    private String getName(int variant) {
        switch (variant){
            case 0: return "BASE";
            case 1: return "S4VARIANT";
            case 2: return "K4LVARIANT";
            case 3: return "K4LkVARIANT";
            case 4: return "E2VARIANT";
            case 5: return "C6VARIANT";
            default: return "BASE";
        }
    }

    public double calcDGDDA(String path1, String path2){

        double[][] nG = lodaN(path1);
        double[][] nH = lodaN(path2);

        int maxk = Math.max(nG[0].length,nH[0].length);

        double[] d = new double[orbNumber];

        for(int orb = 0 ; orb< orbNumber ; orb++)
        {
            double di = 0;

            for(int k = 0 ; k < maxk ; k++ )
            {
                if(k>=nG[orb].length)
                {
                    di += Math.pow(0-nH[orb][k],2);
                }
                else if(k>=nH[orb].length)
                {
                    di += Math.pow(nG[orb][k]-0,2);
                }
                else
                {
                    di += Math.pow(nG[orb][k]-nH[orb][k],2);
                }
            }

            d[orb] = 1 - (1/Math.sqrt(2)) * Math.sqrt(di);
        }

        return DoubleStream.of(d).sum()/orbNumber;
    }

    private double[][] lodaN(String path) {

        Scanner scanner = null;
        try {
            scanner = new Scanner(new File(path));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        boolean startRead = false;

        List<double[]> result = new ArrayList<>();

        while (scanner.hasNext()) {

            String line = scanner.nextLine();

            if(line.contains("-n"))
            {
                startRead = true;
                line = scanner.nextLine();
            }

            if(startRead)
            {
                line=line.replace("[","");
                line=line.replace("]","");
                //to test
                line=line.replace("NaN","0.0");

                String[] lin = line.split(",");

                double[] shortResult = new double[lin.length];

                for(int i = 0 ; i < lin.length ; i++)
                {
                    //System.out.println(lin[i]);
                    shortResult[i] = Double.parseDouble(lin[i]);
                }

                result.add(shortResult);
            }


        }
        scanner.close();

        double[][] matrix=new double[result.size()][];
        return result.toArray(matrix);

        //return (double[][]) result.toArray();
    }
}
