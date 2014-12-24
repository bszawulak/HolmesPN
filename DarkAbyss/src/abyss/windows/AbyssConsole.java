package abyss.windows;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
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

/**
 * Klasa ta tworzy okno konsoli programu, na którym pojawiaj¹ siê informacje na temat wykonywania
 * ró¿nych funkcji. Pozwala na zapis logu do pliku.
 * @author MR
 *
 */
public class AbyssConsole extends JFrame {
	private static final long serialVersionUID = -2286636544180010192L;
	private String newline = "\n";
	private StyledDocument doc; //
	private JTextPane textPane; //panel z tekstem -> paneScrollPane
	private JPanel editPanel; //g³ówny panel okna
	private JScrollPane paneScrollPane; //panel scrollbar -> editPanel
	/**
	 * regular, italic, bold, small, large, warning, error
	 */
	private String[] initStyles = { "regular", "italic", "bold", "small", "large", "warning", "error" };
	private boolean noWarnings = false;
	private boolean noErrors = false;
	
	/**
	 * Konstruktor domyœlny obiektu klasy WindowConsole.
	 */
	public AbyssConsole() {
		this.setTitle("Abyss Status Console");

		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		setMinimumSize(new Dimension(1000, 400));
		setMaximumSize(new Dimension(1000, 400));
		setResizable(false);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        
        JPanel tablePanel = createEditor();
        tablePanel.setOpaque(true); 

        setContentPane(tablePanel);
        pack();
        setVisible(false); 
	}
	
	/**
	 * Metoda pomocnicza konstruktora okna konsoli.
	 * @return JPanel - g³ówny panel okna
	 */
	private JPanel createEditor() {
		editPanel = new JPanel();
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
        paneScrollPane = new JScrollPane(textPane);
        paneScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
      
        editPanel.add(paneScrollPane, gbc);
        gbc.insets = new Insets(5,10,5,10);  //top padding
        gbc.gridy = 1; 
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.fill = GridBagConstraints.SOUTH;
        
        JButton button = new JButton("Save log...");
        button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				saveDialog();
			}
		});
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
	        System.err.println("Couldn't insert initial text into text pane.");
	    }
	    return txtPane;
	}
	
	/**
	 * Metoda wpisuje now¹ liniê do okna logów.
	 * @param text String - text do wpisania
	 * @param mode String - tryb pisania
	 * @param time boolean - true, jeœli ma byæ wyœwietlony czas wpisu
	 */
	public void addText(String text, String mode, boolean time, boolean enter) {
		String newLn = "";
		if(enter) newLn = newline;
			
		int style = 0;
		if(mode.equals("warning")) {
			if(noWarnings) return;
			style = 5;
		} else if(mode.equals("error")) {
			if(noErrors) return;
			style = 6;
		} else if(mode.equals("text")) {
			style = 0;
		} else if(mode.equals("italic")) {
			style = 1;
		} else if(mode.equals("bold")) {
			style = 2;
		} else {
			style = 0;
		}
		if(time) {
			
		}
		if(style < 0 || style > 6)
			style = 0;
		
		try {
			if(time) {
				String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
				doc.insertString(doc.getLength(), "["+timeStamp+"]   ", doc.getStyle("time"));
			}
	        doc.insertString(doc.getLength(), text+newLn, doc.getStyle(initStyles[style]));
	    } catch (BadLocationException ble) {
	        System.err.println("Couldn't insert initial text into text pane.");
	    }
		
		int len = textPane.getDocument().getLength();
		textPane.setCaretPosition(len);
		
		//int pos = paneScrollPane.getVerticalScrollBar().getValue();
		//paneScrollPane.getVerticalScrollBar().setValue(pos);
		//editPanel.update(editPanel.getGraphics());
	}

	/**
	 * Metoda pomocnicza konstruktora klasy, tworzy style dla wypisywanych komunikatów.
	 * @param doc StyledDocument - obiekt dokumentu przechowuj¹cego style
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
        StyleConstants.setForeground(s, Color.orange);
        
        s = doc.addStyle("error", regular);
        StyleConstants.setForeground(s, Color.red);
        
        s = doc.addStyle("time", regular);
        StyleConstants.setForeground(s, Color.darkGray);
        StyleConstants.setBold(s, true);
	}
	
	/**
	 * Metoda ustawia flagê odpowiadaj¹c¹ za dopuszczenie do zapisywania ostrze¿eñ.
	 * @param value boolean - true, jeœli ostrze¿enia maj¹ byæ zapamiêtywane
	 */
	public void silenceWarnings(boolean value) {
		noWarnings = value;
	}
	
	/**
	 * Metoda ustawia flagê odpowiadaj¹c¹ za dopuszczenie do zapisywania b³êdów.
	 * @param value boolean - true, jeœli b³êdy maj¹ byæ zapamiêtywane
	 */
	public void silenceErrors(boolean value) {
		noErrors = value;
	}
	
	/**
	 * Metoda pytaj¹ca w formie dialogu gdzie zapisaæ plik loga.
	 */
	private void saveDialog()
	{
	    JFileChooser chooserSaveAs = new JFileChooser();
	    chooserSaveAs.setDialogTitle("Save as...");
	    if(chooserSaveAs.showSaveDialog(this) == JFileChooser.APPROVE_OPTION)
	    {
	    	File file = chooserSaveAs.getSelectedFile();
	        saveLogToFile(file);
	    }
	}

	/**
	 * Metoda zapisuje log do wybranego pliku lub do pliku domyœlnego.
	 * @param file File - plik do zapisu, jeœli == null, wtedy zapis domyœlny
	 */
	public void saveLogToFile(File file) {
		File saveFile = null;
		if(file == null) {
			saveFile = new File("log/log.txt");
		} else {
			saveFile = file;
		}
		
		try
        {
			PrintStream p = new PrintStream(new BufferedOutputStream(new FileOutputStream(saveFile, true)));
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
