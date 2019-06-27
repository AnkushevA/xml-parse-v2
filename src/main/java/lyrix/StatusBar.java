package lyrix;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;

public class StatusBar extends JPanel {

    private JLabel statusLabel;

    StatusBar() {
        setBorder(new BevelBorder(BevelBorder.LOWERED));
        setLayout(new FlowLayout(FlowLayout.LEFT));
        statusLabel = new JLabel("status");
        statusLabel.setForeground(Color.red);
        statusLabel.setHorizontalAlignment(SwingConstants.LEFT);
        add(statusLabel);
    }

    public void refreshStatus(String message) {
        statusLabel.setText(message);
    }

}
