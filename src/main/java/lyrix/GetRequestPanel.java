package lyrix;

import javax.swing.*;
import java.awt.*;

public class GetRequestPanel extends JPanel {
    private SendRequestFrame sendRequestFrame;
    private JTextArea requestTextArea;

    public GetRequestPanel(SendRequestFrame sendRequestFrame, String answer) {
        this.sendRequestFrame = sendRequestFrame;
        setLayout(new BorderLayout());
        requestTextArea = new JTextArea(30, 30);
        requestTextArea.setLineWrap(true);
        requestTextArea.setEditable(false);
        requestTextArea.setWrapStyleWord(true);
        requestTextArea.setBorder(BorderFactory.createTitledBorder("Полученное сообщение"));
        requestTextArea.setText(answer);
        add(requestTextArea, BorderLayout.CENTER);
    }

    public void showText(String xmlString) {
        requestTextArea.setText(xmlString);
    }

}
