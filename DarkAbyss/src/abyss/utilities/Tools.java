/**
 * 
 */
package abyss.utilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.swing.JOptionPane;

/**
 * Klasa u�ytkowa, odpowiednik klasy statycznej w normalnym j�zyku programowania
 * jak np. C#.
 * @author Rince
 *
 */
public final class Tools {
	/**
	 * Prywatny konstruktor. To powinno za�atwi� problem obiekt�w.
	 */
	private Tools() {

	}
	
	/**
	 * Metoda pomocnicza s�u��ca do kopiowania pliku z pierwszej lokalizacji w drug�.
	 * @param source String - �cie�ka do pliku kopiowanego
	 * @param target String - �cie�ka do miejsca gdzie kopiujemy
	 * @throws IOException - si� zepsu�o si�...
	 */
	public static void copyFileByPath(String source, String target) throws IOException{
    	InputStream inStream = null;
    	OutputStream outStream = null;
 
   	    File file1 =new File(source);
   	    File file2 =new File(target);
 
   	    inStream = new FileInputStream(file1);
   	    outStream = new FileOutputStream(file2);
 
   	    byte[] buffer = new byte[1024];
 
   	    int length;
   	    while ((length = inStream.read(buffer)) > 0){
   	    	outStream.write(buffer, 0, length);
   	    }
 
   	    if (inStream != null)inStream.close();
   	    if (outStream != null)outStream.close();
    }
	
	/**
	 * Metoda kopiuj�ca plik �r�d�owy do docelowego.
	 * @param source File - plik do kopiowania
	 * @param target File - plik kt�ry zast�pujemy kopiowanym
	 */
	public static void copyFileDirectly(File source, File target) {
    	InputStream inStream = null;
    	OutputStream outStream = null;
 
   	    try {
			inStream = new FileInputStream(source);
			outStream = new FileOutputStream(target);
			byte[] buffer = new byte[1024];
			 
	   	    int length;
	   	    while ((length = inStream.read(buffer)) > 0){
	   	    	outStream.write(buffer, 0, length);
	   	    }
	 
	   	    if (inStream != null)
	   	    	inStream.close();
	   	    if (outStream != null)
	   	    	outStream.close();
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null,
					"I/O operation failed for reason unknown. You can now start panicking.\nHave a nice day!",
					"Critical error", JOptionPane.ERROR_MESSAGE);
			return;
		}
    }
}
