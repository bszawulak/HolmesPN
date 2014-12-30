package abyss.utilities;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class CustomDialog extends JDialog {
	private static final long serialVersionUID = -2614492110805839328L;

	public CustomDialog(JFrame parent, String title, String message) {
		super(parent, title, true);
		if (parent != null) {
			Dimension parentSize = parent.getSize(); 
			Point p = parent.getLocation(); 
		    setLocation(p.x + parentSize.width / 4, p.y + parentSize.height / 4);
	    }
		this.setPreferredSize(new Dimension(300,200));
		
		JPanel messagePane = new JPanel();
		messagePane.setLayout(new BorderLayout());
		
		JTextArea area = new JTextArea();
		area.setLineWrap(true);
		JScrollPane scroll = new JScrollPane(area);
		messagePane.add(scroll, BorderLayout.CENTER);
		
		area.setEditable(false);
		area.setText(message);

		
		//messagePane.add(new JLabel(message));
		
		getContentPane().add(messagePane);
		
		 
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		pack(); 
		setVisible(true);
	}
}