package holmes.petrinet.subnets.dialogs;

import holmes.darkgui.GUIManager;
import holmes.utilities.Tools;

import javax.swing.*;

public abstract class BaseDialog {
    private JDialog dialog;

    protected BaseDialog(String title, int width, int height) {
        initDialog(title, width, height);
    }

    private void initDialog(String title, int width, int height) {
        GUIManager overlord = GUIManager.getDefaultGUIManager();
        dialog = new JDialog(overlord.getFrame(), title);
        dialog.setSize(width, height);
        dialog.setLocationRelativeTo(dialog.getParent());
        dialog.setResizable(false);
        try {
            dialog.setIconImage(Tools.getImageFromIcon("/icons/holmesicon.png"));
        } catch (Exception ex) {
            overlord.log("Error (830125713) | Exception:  " + ex.getMessage(), "error", true);
        }
        dialog.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                overlord.getFrame().setEnabled(true);
            }
        });
    }

    public void open() {
        JFrame frame = GUIManager.getDefaultGUIManager().getFrame();
        frame.setEnabled(false);
        dialog.setVisible(true);
    }

    public JDialog getDialog() {
        return dialog;
    }
}
