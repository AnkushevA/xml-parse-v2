package lyrix;

import javax.swing.*;
import java.awt.*;

public class SendRequestPanel extends JPanel {
    private SendRequestFrame sendRequestFrame;
    private JTextArea requestTextArea;

    public SendRequestPanel(SendRequestFrame sendRequestFrame) {
        this.sendRequestFrame = sendRequestFrame;
        setLayout(new BorderLayout());
        requestTextArea = new JTextArea(30, 30);
        requestTextArea.setLineWrap(true);
        requestTextArea.setEditable(false);
        requestTextArea.setWrapStyleWord(true);
        requestTextArea.setBorder(BorderFactory.createTitledBorder("Отправленное сообщение"));

        add(requestTextArea, BorderLayout.CENTER);
    }

    public void showText(String xmlString) {
        requestTextArea.setText(xmlString);
    }
}
