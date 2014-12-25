package abyss.windows;

import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class AbyssAbout extends JFrame {
	private static final long serialVersionUID = 6034143130559149651L;
	JFrame parentFrame;
	
	public AbyssAbout(JFrame parent) {
		this.parentFrame = parent;
		parentFrame.setEnabled(false);
		
		this.setLayout(null);
		
		setSize(new Dimension(500, 300));
		
		JTextArea area = new JTextArea();
		area.setLineWrap(true);
		area.setWrapStyleWord(true);
		area.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

		add(new JScrollPane(area));
		

		addWindowListener(new java.awt.event.WindowAdapter() {
		    @Override
		    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
		    	parentFrame.setEnabled(true);
		    }
		});
		
		setLocationRelativeTo(null);
		setVisible(true);
	}

}
