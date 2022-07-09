package holmes.darkgui.holmesInterface;

import holmes.utilities.Tools;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;

public class HolmesRoundedButton extends JButton {
    private ImageIcon normal;
    private ImageIcon hover;
    private ImageIcon clicked;
    public HolmesRoundedButton(String text, String normalName, String howerName, String clickedName) {
        super(text);
        Dimension size = getPreferredSize();
        size.width = size.height = Math.max(size.width, size.height);
        setPreferredSize(size);
        setContentAreaFilled(false);

        normal = Tools.getResIcon22("/buttons/"+normalName);
        hover = Tools.getResIcon22("/buttons/"+howerName);
        clicked = Tools.getResIcon22("/buttons/"+clickedName);
    }

    @Override
    protected void paintComponent(Graphics g) {
        int offset = getInsets().left;
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