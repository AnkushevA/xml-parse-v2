package lyrix;

import javax.swing.*;
import javax.xml.soap.SOAPException;
import javax.xml.transform.TransformerException;
import java.awt.*;
import java.io.IOException;


class TopMenu extends JPanel {
    private final MainFrame mainFrame;

    TopMenu(final MainFrame mainFrame, ICommand expandCommand, ICommand collapseCommand) {
        this.mainFrame = mainFrame;

        JButton expandTreeButton = new JButton("Развернуть дерево");
        expandTreeButton.addActionListener(actionEvent -> expandCommand.execute());

        JButton collapseTreeButton = new JButton("Свернуть дерево");
        collapseTreeButton.addActionListener(actionEvent -> collapseCommand.execute());

        JButton showRequestsButton = new JButton("Отобразить запросы");
        showRequestsButton.addActionListener(actionEvent -> {
            mainFrame.showXmlRequest();
        });

        JButton makeXMLButton = new JButton("Отправить запрос");
        makeXMLButton.addActionListener(actionEvent -> {
            try {
                mainFrame.sendXmlRequest();
            } catch (SOAPException | TransformerException | IOException e) {
                JOptionPane.showMessageDialog(null, "Ошибка отправки сообщения");
            }
        });

        setLayout(new FlowLayout(FlowLayout.LEFT));

        add(expandTreeButton);
        add(collapseTreeButton);
        add(makeXMLButton);
        add(showRequestsButton);
    }

}
