package holmes.windows;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

import holmes.darkgui.GUIManager;
import holmes.utilities.HolmesFileView;
import holmes.utilities.Tools;

/**
 * Klasa ta tworzy okno konsoli programu, na którym pojawiają się informacje na temat wykonywania
 * różnych funkcji programu. Pozwala na zapis logu do pliku.
 */
public class HolmesConsole extends JFrame {
	@Serial
	private static final long serialVersionUID = -2286636544180010192L;
	private static final GUIManager overlord = GUIManager.getDefaultGUIManager();
	private String newline = "\n";
	private StyledDocument doc; //
	private JTextPane textPane; //panel z tekstem -> paneScrollPane
	/**
	 * regular, italic, bold, small, large, warning, error
	 */
	private String[] initStyles = { "regular", "italic", "bold", "small", "large", "warning", "error" };
	private boolean noWarnings = false;
	private boolean noErrors = false;
	
	/**
	 * Konstruktor domyślny obiektu klasy WindowConsole.
	 */
	public HolmesConsole() {
		setTitle("Holmes Status Console");
    	try {
    		setIconImage(Tools.getImageFromIcon("/icons/holmesicon.png"));
		} catch (Exception ex) {
			overlord.log("Error (316619924) | Exception:  "+ex.getMessage(), "error", true);
		}
    	setVisible(false);
		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		setMinimumSize(new Dimension(1000, 400));
		setMaximumSize(new Dimension(1000, 400));
		setResizable(false);

        //JPanel mainPanel = new JPanel();
       // mainPanel.setLayout(new BorderLayout());
        
        JPanel tablePanel = createEditor();
        tablePanel.setOpaque(true); 

        setContentPane(tablePanel);
        pack();
	}
	
	/**
	 * Metoda pomocnicza konstruktora okna konsoli.
	 * @return JPanel - główny panel okna
	 */
	private JPanel createEditor() {
		//główny panel okna
		JPanel editPanel = new JPanel();
		//this.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		editPanel.setLayout(new GridBagLayout());
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.FIRST_LINE_START;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.1;
        gbc.weighty = 1.0;
           
        textPane = createTextPane();
        textPane.setEditable(false);
		//panel scrollbar -> editPanel
		JScrollPane paneScrollPane = new JScrollPane(textPane);
        paneScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
      
        editPanel.add(paneScrollPane, gbc);
        gbc.insets = new Insets(5,10,5,10);  //top padding
        gbc.gridy = 1; 
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.fill = GridBagConstraints.SOUTH;
        
        JButton button = new JButton("Save log...");
        button.addActionListener(actionEvent -> saveDialog());
        editPanel.add(button, gbc);
        
        return editPanel;
	}
	
	/**
	 * Metoda pomocnicza konstruktora, tworzy obiekt edytora.
	 * @return JTextPane - obiekt edytora / konsoli
	 */
	private JTextPane createTextPane() {
		String initString = "Console initiated"+newline;
	    JTextPane txtPane = new JTextPane();
	    doc = txtPane.getStyledDocument();
	    addStylesToDocument(doc);
	    try {
	        doc.insertString(doc.getLength(), initString, doc.getStyle("regular"));
	    } catch (BadLocationException ble) {
	    	System.err.println("Couldn't initialize the console.");
	    }
	    return txtPane;
	}
	
	/**
	 * Metoda wpisuje nową linię do okna logów.
	 * @param text String - text do wpisania
	 * @param mode String - tryb pisania
	 * @param time boolean - true, jeśli ma być wyświetlony czas wpisu
	 */
	public void addText(String text, String mode, boolean time, boolean enter) {
		Style def = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);
		StyleConstants.setFontFamily(def, "SansSerif");
		StyleConstants.setFontSize(def, 12);
		
		String newLn = "";
		if(enter) newLn = newline;
			
		int style;
		switch (mode) {
			case "warning" -> {
				if (noWarnings) return;
				style = 5;
			}
			case "error" -> {
				if (noErrors) return;
				style = 6;
			}
			case "text" -> style = 0;
			case "italic" -> style = 1;
			case "bold" -> style = 2;
			default -> style = 0;
		}

		//if(style < 0 || style > 6)
		//	style = 0;
		
		try {
			if(time) {
				String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
				doc.insertString(doc.getLength(), "["+timeStamp+"]   ", doc.getStyle("time"));
			}
	        doc.insertString(doc.getLength(), text+newLn, doc.getStyle(initStyles[style]));
	    } catch (BadLocationException ble) {
	    	System.err.println("Couldn't insert text into console.");
	    }
		
		int len = textPane.getDocument().getLength();
		textPane.setCaretPosition(len);

	}

	/**
	 * Metoda pomocnicza konstruktora klasy, tworzy style dla wypisywanych komunikatów.
	 * @param doc StyledDocument - obiekt dokumentu przechowującego style
	 */
	private void addStylesToDocument(StyledDocument doc) {
        Style def = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);

        Style regular = doc.addStyle("regular", def);
        StyleConstants.setFontFamily(def, "SansSerif");

        Style s = doc.addStyle("italic", regular);
        StyleConstants.setItalic(s, true);

        s = doc.addStyle("bold", regular);
        StyleConstants.setBold(s, true);

        s = doc.addStyle("small", regular);
        StyleConstants.setFontSize(s, 10);

        s = doc.addStyle("large", regular);
        StyleConstants.setFontSize(s, 16);
        
        s = doc.addStyle("warning", regular);
        StyleConstants.setForeground(s, Color.magenta);
        
        s = doc.addStyle("error", regular);
        StyleConstants.setForeground(s, Color.red);
        
        s = doc.addStyle("time", regular);
        StyleConstants.setForeground(s, Color.darkGray);
        StyleConstants.setBold(s, true);
	}
	
	/**
	 * Metoda ustawia flagę odpowiadającą za dopuszczenie do zapisywania ostrzeżeń.
	 * @param value boolean - true, jeśli ostrzeżenia mają być zapamiętywane
	 */
	public void silenceWarnings(boolean value) {
		noWarnings = value;
	}
	
	/**
	 * Metoda ustawia flagę odpowiadającą za dopuszczenie do zapisywania błędów.
	 * @param value boolean - true, jeśli błędy mają być zapamiętywane
	 */
	public void silenceErrors(boolean value) {
		noErrors = value;
	}
	
	/**
	 * Metoda pytająca w formie dialogu gdzie zapisać plik logów programu.
	 */
	private void saveDialog()
	{
	    JFileChooser chooserSaveAs = new JFileChooser();
	    chooserSaveAs.setFileView(new HolmesFileView());
	    chooserSaveAs.setDialogTitle("Save as...");
	    if(chooserSaveAs.showSaveDialog(this) == JFileChooser.APPROVE_OPTION)
	    {
	    	File file = chooserSaveAs.getSelectedFile();
	        saveLogToFile(file);
	    }
	}

	/**
	 * Metoda zapisuje log do wybranego pliku lub do pliku domyślnego.
	 * @param file File - plik do zapisu, jeśli == null, wtedy zapis domyślny
	 */
	public void saveLogToFile(File file) {
		File saveFile;
		if(file == null) {
			saveFile = new File("log/log.txt");
		} else {
			saveFile = file;
		}
		
		try
        {
			PrintStream p = new PrintStream(new BufferedOutputStream(new FileOutputStream(saveFile, true))); // (•_•)
		    p.println("*********************************************************************");
		    p.close();
		      
			DefaultEditorKit kit = new DefaultEditorKit();
			BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(saveFile, true));
			kit.write(out, doc, doc.getStartPosition().getOffset(), doc.getLength());
			out.close();
        }
        catch(IOException | BadLocationException ex)
        {
            //Logger.getLogger(CFrameMain.class.getName()).log(Level.SEVERE, null, ex);
        }
	}
}
