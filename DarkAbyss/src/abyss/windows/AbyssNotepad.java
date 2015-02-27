package abyss.windows;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JPanel;

import abyss.utilities.Tools;

public class AbyssNotepad extends JFrame {
	private static final long serialVersionUID = 1694133455242675169L;

	public AbyssNotepad() {
		setTitle("Abyss Notepad");
    	try {
    		setIconImage(Tools.getImageFromIcon("/icons/blackhole.png"));
		} catch (Exception e ) {
			
		}

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

	private JPanel createEditor() {
		// TODO Auto-generated method stub
		return null;
	}
}
