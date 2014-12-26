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

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

/**
 * Klasa u�ytkowa, odpowiednik klasy statycznej w normalnym j�zyku programowania
 * jak np. C#. testing testing
 * @author Rince
 *
 */
public final class Tools {
	/**
	 * Prywatny konstruktor. To powinno za�atwi� problem obiekt�w.
	 */
	private Tools() {
		int x;
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
	
	/**
	 * Okno dialogowe do wskazywania pliku.
	 * @param lastPath String - ostatnia otwarta lokalizacja
	 * @param filter FileFilter[] - filtry plik�w, pierwszy - domy�lny
	 * @param buttonText String - tekst na przycisku akceptacji
	 * @param buttonToolTip String - tekst wyja�nienia dla przycisku akceptacji
	 * @return String - �cie�ka do pliku
	 */
	public static String selectFileDialog(String lastPath, FileFilter[] filter, 
			String buttonText, String buttonToolTip) {
		String resultPath = "";
		JFileChooser fc;
		if(lastPath == null)
			fc = new JFileChooser();
		else
			fc = new JFileChooser(lastPath);

		for(int i=0; i<filter.length; i++) {
			fc.addChoosableFileFilter(filter[i]);
		}
		fc.setFileFilter(filter[0]);
		
		if(!buttonText.equals(""))
			fc.setApproveButtonText(buttonText);
		if(!buttonToolTip.equals(""))
			fc.setApproveButtonToolTipText(buttonToolTip);
		
		fc.setAcceptAllFileFilterUsed(false);
		int returnVal = fc.showSaveDialog(null);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File f = fc.getSelectedFile();
			resultPath = f.getPath();	
		}
		return resultPath;
	}
	
	/**
	 * Metoda wy�wietla okno dialogowe dla wskazania katalogu z plikami klastr�w
	 * @param lastPath String - ostatnia otwarta �cie�ka dost�pu
	 * @param buttonText String - tekst na przycisku akceptacji
	 * @param buttonToolTip String - tekst wyja�nienia dla przycisku akceptacji
	 * @return boolean - true, je�li wskazano jakikolwiek katalog, false w przeciwnym
	 * 		wypadku
	 */
	public static String selectDirectoryDialog(String lastPath, String buttonText, String buttonToolTip) {
		String resultDir = "";
		JFileChooser fc = null;
		if(lastPath == null)
			fc = new JFileChooser();
		else
			fc = new JFileChooser(lastPath);
		
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fc.setAcceptAllFileFilterUsed(false);
		
		if(!buttonText.equals(""))
			fc.setApproveButtonText(buttonText);
		if(!buttonToolTip.equals(""))
			fc.setApproveButtonToolTipText(buttonToolTip);
		
		int returnVal = fc.showDialog(fc, fc.getApproveButtonText());
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			resultDir = fc.getSelectedFile().getPath();
		} 
		return resultDir;
	}
}
