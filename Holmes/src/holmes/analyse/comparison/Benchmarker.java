package holmes.analyse.comparison;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class Benchmarker {

    HashMap<String, Date> map = new HashMap<>();

    public Benchmarker(String path){
        getTimestamps(path);

        listFilesForFolder(new File("/home/bszawulak/Dokumenty/Eksperyment/Wyniki/i0j0"));
    }

    public Benchmarker(){

        for(int i = 0 ; i < 45 ;i=i+5) {
            listFilesForFolder(new File("/home/bszawulak/Dokumenty/Eksperyment/Wyniki/i" +i+"j"+i));
        }

        for(int i = 0 ; i < 45 ;i=i+5) {
            for(int p = 0 ; p < 99 ; p++)
            {
                int r = p+1;
                Date n1 = map.get("i" +i+"j"+i+"p"+p+"-BASE-DGDDA.txt");
                Date n2 = map.get("i" +i+"j"+i+"p"+r+"-BASE-DGDDA.txt");

                long diffInMillies = Math.abs(n1.getTime() - n2.getTime());
                System.out.println("-> i" + i + "j" +i+ "p"+r +"   ===== "+ diffInMillies);
            }
        }

    }


    public void listFilesForFolder(final File folder) {
        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                listFilesForFolder(fileEntry);
            } else {

                if(fileEntry.getName().contains("-BASE-DGDDA.txt")) {
                    System.out.print(fileEntry.getName()+ " -> ");
                    map.put(fileEntry.getName(),getTimestamps(fileEntry.getPath()));
                }
            }
        }
    }

    private Date getTimestamps(String pat) {

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Path path = Paths.get(pat);
        BasicFileAttributes attr;
        try {
            // read file's attribute as a bulk operation
            attr = Files.readAttributes(path, BasicFileAttributes.class);
            // File creation time
            //System.out.println("File creation time - "
            //        + sdf.format(attr.creationTime().toMillis()));
            // File last modified date

            System.out.println("File modified time - "
                    + sdf.format(attr.lastModifiedTime().toMillis()));

            attr.lastModifiedTime().toMillis();

        } catch (IOException e ) {
            System.out.println("Error while reading file attributes " + e.getMessage());
        }
        return new Date();
    }


}
