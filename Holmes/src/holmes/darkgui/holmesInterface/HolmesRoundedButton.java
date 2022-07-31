package holmes.darkgui.holmesInterface;

import holmes.utilities.Tools;

import javax.swing.*;
import java.awt.*;

/**
 * Klasa nowych przycisków Holmesa, wymaga grafik dla: normalnego stanu, myszy nad przyciskiem oraz klikniętego.
 */
public class HolmesRoundedButton extends JButton {
    private ImageIcon normal;
    private ImageIcon hover;
    private ImageIcon clicked;

    private JLabel title;

    public HolmesRoundedButton(String text, String normalName, String howerName, String clickedName) {
        setLayout(new BorderLayout());
        Dimension size = getPreferredSize();
        size.width = size.height = Math.max(size.width, size.height);
        setPreferredSize(size);
        setContentAreaFilled(false);
        setFocusPainted(false);

        normal = Tools.getResIcon22("/buttons/"+normalName);
        hover = Tools.getResIcon22("/buttons/"+howerName);
        clicked = Tools.getResIcon22("/buttons/"+clickedName);

        title = new JLabel(text);
        title.setHorizontalAlignment(JLabel.CENTER);
        add(title, BorderLayout.CENTER);
    }

    public void setNewText(String text) {
        title.setText(text);
    }

    public void repaintBackground(String normalName, String howerName, String clickedName) {
        normal = Tools.getResIcon22("/buttons/"+normalName);
        hover = Tools.getResIcon22("/buttons/"+howerName);
        clicked = Tools.getResIcon22("/buttons/"+clickedName);
    }
    @Override
    protected void paintComponent(Graphics g) {
        //int offset = getInsets().left;
        int offset = 0;
        g.setColor(getBackground());
        if (getModel().isArmed()) {
            setIcon(resizeIcon(clicked, getWidth()-offset, getHeight()-offset));
        } else if (getModel().isRollover()) {
            setIcon(resizeIcon(hover, getWidth()-offset, getHeight()-offset));
        } else {
            setIcon(resizeIcon(normal, getWidth()-offset, getHeight()-offset));
        }
        super.paintComponent(g);
    }

    private static Icon resizeIcon(ImageIcon icon, int resizedWidth, int resizedHeight) {
        Image img = icon.getImage();
        Image resizedImage = img.getScaledInstance(resizedWidth, resizedHeight,  java.awt.Image.SCALE_SMOOTH);
        return new ImageIcon(resizedImage);
    }

    @Override
    protected void paintBorder(Graphics g) {
        g.setColor(getForeground());
    }
}