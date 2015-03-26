package abyss.windows;

import java.awt.Color;
import java.awt.Dimension;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

import abyss.darkgui.GUIManager;
import abyss.utilities.Tools;

/**
 * Okno wewnętrznego notatnika programu.
 * @author MR
 *
 */
public class AbyssNotepad extends JFrame {
	private static final long serialVersionUID = 1694133455242675169L;

	private String newline = "\n";
	private StyledDocument doc; //
	private JTextPane textPane; //panel z tekstem -> paneScrollPane
	private JScrollPane paneScrollPane; //panel scrollbar -> editPanel
	
	/**
	 * regular, italic, bold, small, large, warning, error
	 */
	private String[] initStyles = { "regular", "italic", "bold", "small", "large", "warning", "error", "time", "node" };
	
	/**
	 * Główny konstruktor domyślny okna notatnika.
	 */
	private AbyssNotepad() {
		setTitle("Abyss Notepad");
    	try {
    		setIconImage(Tools.getImageFromIcon("/icons/blackhole.png"));
		} catch (Exception e ) {
			
		}

		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
	}
	
	/**
	 * Konstruktor tworzący nowe okno o zadanych rozmiarach.
	 * @param width int - szerokość okna
	 * @param height int - wysokość okna
	 */
	public AbyssNotepad(int width, int height) {
		this();
		setPreferredSize(new Dimension(width, height));
		setLocation(50,50);
		
		JPanel mainPanel = createEditor(width, height);
        setContentPane(mainPanel);
        pack();
        setVisible(false); 
	}

	/**
	 * Metoda pomocnica tworząca główne elementy okna notatnika.
	 * @param width int - szerokość okna
	 * @param height int - wysokość okna
	 * @return JPanel - panel główny notatnika
	 */
	private JPanel createEditor(int width, int height) {
		JPanel main = new JPanel();
		main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
		
		JPanel buttonPanel = new JPanel(null);
		buttonPanel.setMinimumSize(new Dimension(width, 40));
		buttonPanel.setPreferredSize(new Dimension(width,40));
		buttonPanel.setMaximumSize(new Dimension(3000, 40));
		buttonPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		main.add(buttonPanel);
		
		textPane = createTextPane();
        //textPane.setEditable(false);
        paneScrollPane = new JScrollPane(textPane);
        paneScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		
		//JPanel editorPanel = new JPanel(null);
		//editorPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		main.add(paneScrollPane);
		
		return main;
	}
	
	/**
	 * Metoda pomocnicza konstruktora, tworzy obiekt edytora.
	 * @return JTextPane - panel edytora
	 */
	private JTextPane createTextPane() {
		//String initString = "Console initiated"+newline;
	    JTextPane txtPane = new JTextPane();
	    doc = txtPane.getStyledDocument();
	    addStylesToDocument(doc);
	    try {
	        //doc.insertString(doc.getLength(), initString, doc.getStyle("regular"));
	    } catch (Exception e) {
	        GUIManager.getDefaultGUIManager().log("Couldn't insert initial text into text pane.","error", true);
	    }
	    return txtPane;
	}
	
	/**
	 * Metoda wpisuje nową linię do okna logów.
	 * @param text String - text do wpisania
	 * @param mode String - tryb pisania
	 * @param time boolean - true, jeśli ma być wyświetlony czas wpisu
	 * @param enter boolean - trye jeśli kończymy enterem
	 */
	public void addText(String text, String mode, boolean time, boolean enter) {
		int style = setWritingStyle(mode);
		
		String nL = "";
		if(enter)
			nL = newline;
		
		try {
			if(time) {
				String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
				doc.insertString(doc.getLength(), "["+timeStamp+"]   ", doc.getStyle("time"));
			}
	        doc.insertString(doc.getLength(), text+nL, doc.getStyle(initStyles[style]));
	    } catch (BadLocationException ble) {
	        GUIManager.getDefaultGUIManager().log("Couldn't insert initial text into text pane.", "error", true);
	    }
		
		int len = textPane.getDocument().getLength();
		textPane.setCaretPosition(len);
	}
	
	public void addTextLine(String text, String mode) {
		int style = setWritingStyle(mode);
		try {
	        doc.insertString(doc.getLength(), text, doc.getStyle(initStyles[style]));
	    } catch (BadLocationException ble) {
	        GUIManager.getDefaultGUIManager().log("Couldn't insert initial text into text pane.", "error", true);
	    }
		int len = textPane.getDocument().getLength();
		textPane.setCaretPosition(len);
	}
	
	public void addTextLineNL(String text, String mode) {
		int style = setWritingStyle(mode);
		try {
	        doc.insertString(doc.getLength(), text+newline, doc.getStyle(initStyles[style]));
	    } catch (BadLocationException ble) {
	        GUIManager.getDefaultGUIManager().log("Couldn't insert initial text into text pane.", "error", true);
	    }
		int len = textPane.getDocument().getLength();
		textPane.setCaretPosition(len);
	}
	
	/**
	 * Dodaje pojedyńczą linię tekstu.
	 * @param text String - tekst do dodania
	 * @param mode String - tryb pisania
	 */
	public void addLine(String text, String mode) {
		int style = setWritingStyle(mode);
		
		try {
	        doc.insertString(doc.getLength(), text, doc.getStyle(initStyles[style]));
	    } catch (BadLocationException ble) {
	        System.err.println("Couldn't insert initial text into text pane.");
	    }
		
		int len = textPane.getDocument().getLength();
		textPane.setCaretPosition(len);
	}
	
	/**
	 * Metoda wewnętrzna definiująca styl po jego nazwie. Zwraca numer ID stylu.
	 * @param mode String - nazwa stylu pisania tekstu
	 * @return int - numer stylu
	 */
	private int setWritingStyle(String mode) {
		//Style def = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);
		//StyleConstants.setFontFamily(def, "monospaced");
		//StyleConstants.setFontSize(def, 10);

		int style = 0;
		
		if(mode.equals("text") || mode.equals("t")) {
			style = 0;
		} else if(mode.equals("italic") || mode.equals("i")) {
			style = 1;
		} else if(mode.equals("bold") || mode.equals("b")) {
			style = 2;
		} else if(mode.equals("small")) {
			style = 3;
		} else if(mode.equals("large")) {
			style = 4;
		} else if(mode.equals("warning")) {
			style = 5;
		} else if(mode.equals("error")) {
			style = 6;
		} else if(mode.equals("time")) {
			style = 7;
		} else if(mode.equals("nodeName")) {
			style = 8;
		} else {
			style = 1;
		}
		return style;
	}
	
	/**
	 * Metoda pomocnicza konstruktora klasy, tworzy style dla wypisywanych komunikatów.
	 * @param doc StyledDocument - obiekt dokumentu przechowującego style
	 */
	private void addStylesToDocument(StyledDocument doc) {
        Style def = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);
        StyleConstants.setFontFamily(def, "Consolas"); //Monospaced
        StyleConstants.setFontSize(def, 14);
        
        Style regular = doc.addStyle("regular", def); //0
        StyleConstants.setFontFamily(regular, "Consolas");
        StyleConstants.setFontSize(regular, 14);

        Style s = doc.addStyle("italic", regular); //1
        StyleConstants.setItalic(s, true);

        s = doc.addStyle("bold", regular); //2
        StyleConstants.setBold(s, true);

        s = doc.addStyle("small", regular); //3
        StyleConstants.setFontSize(s, 10);

        s = doc.addStyle("large", regular); //4
        StyleConstants.setFontSize(s, 18);
        
        s = doc.addStyle("warning", regular); //5
        StyleConstants.setForeground(s, Color.orange);
        
        s = doc.addStyle("error", regular); //6
        StyleConstants.setForeground(s, Color.red);
        
        s = doc.addStyle("time", regular); //7
        StyleConstants.setForeground(s, Color.darkGray);
        StyleConstants.setBold(s, true);
        
        s = doc.addStyle("node", regular); //8
        StyleConstants.setForeground(s, Color.darkGray);
        StyleConstants.setBold(s, true);
	}
}
