package abyss.windows;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

public class WindowConsole extends JPanel {
	private static final long serialVersionUID = -2286636544180010192L;
	private JTextArea textArea;
	private String newline = "\n";
	private StyledDocument doc;
	private String[] initStyles = { "regular", "italic", "bold", "small", "large" };
	
	public WindowConsole() {
		//this.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		this.setLayout(new GridBagLayout());
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.FIRST_LINE_START;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.1;
        gbc.weighty = 1.0;
        
        //textArea = new JTextArea ("Test");   
        //JScrollPane scroll = new JScrollPane (textArea, 
        //   JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
           
        JTextPane textPane = createTextPane();
        JScrollPane paneScrollPane = new JScrollPane(textPane);
        paneScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        //paneScrollPane.setPreferredSize(new Dimension(250, 155));
        //paneScrollPane.setMinimumSize(new Dimension(10, 10));
        add(paneScrollPane, gbc);
        
        //this.add(textPane, gbc);

        gbc.insets = new Insets(5,10,5,10);  //top padding
        gbc.gridy = 1; 
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        
        gbc.fill = GridBagConstraints.SOUTH;
        
        JButton button = new JButton("Napis");
        this.add(button, gbc);
	}
	
	private JTextPane createTextPane() {
		String initString = "Console initiated"+newline;
	    JTextPane textPane = new JTextPane();
	    doc = textPane.getStyledDocument();
	    addStylesToDocument(doc);

	    try {
	        doc.insertString(doc.getLength(), initString, doc.getStyle("regular"));
	        //doc.insertString(doc.getLength(), "\ndupa dupa dupa", doc.getStyle("regular"));
	    } catch (BadLocationException ble) {
	        System.err.println("Couldn't insert initial text into text pane.");
	    }
	    return textPane;
	}
	
	public void addText(String text, int style) {
		if(style < 0 || style > 4)
			style = 0;
		
		try {
	        doc.insertString(doc.getLength(), text, doc.getStyle(initStyles[style]));
	    } catch (BadLocationException ble) {
	        System.err.println("Couldn't insert initial text into text pane.");
	    }
	}

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
	}

	public void addText(String line) {
		textArea.append(line+"\n");
	}
}
