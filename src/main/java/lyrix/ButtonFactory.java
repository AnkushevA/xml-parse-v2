package lyrix;

import javax.swing.*;
import java.awt.event.ActionListener;

public class ButtonFactory {
    public static JButton makeButton(String buttonLabel, ActionListener actionListener) {
        JButton button = new JButton(buttonLabel);
        button.addActionListener(actionListener);
        return button;
    }
}
