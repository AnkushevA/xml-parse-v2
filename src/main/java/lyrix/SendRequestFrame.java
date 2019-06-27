package lyrix;

import javax.swing.*;
import java.awt.*;

public class SendRequestFrame extends JFrame {
    private final JSplitPane centralSplitMenu;
    private SendRequestPanel sendRequestPanel;
    private GetRequestPanel getRequestPanel;

    public SendRequestFrame(String xmlString, String answer) {
        super("Отправить запрос");
        sendRequestPanel = new SendRequestPanel(this, xmlString);
        getRequestPanel = new GetRequestPanel(this, answer);

        setSize(800, 800);
        centralSplitMenu = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(sendRequestPanel), new JScrollPane(getRequestPanel));
        centralSplitMenu.setDividerLocation(300);
        add(centralSplitMenu, BorderLayout.CENTER);
    }

    public void showText(String xmlString, String answer) {
        sendRequestPanel.showText(xmlString);
        getRequestPanel.showText(answer);
    }
}
