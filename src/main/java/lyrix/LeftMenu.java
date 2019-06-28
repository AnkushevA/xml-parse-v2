package lyrix;

import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.support.wsdl.WsdlImporter;
import com.eviware.soapui.model.iface.Operation;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.swing.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;

class LeftMenu extends JPanel {
    private final MainFrame mainFrame;
    private JList<String> itemsList;
    private DefaultListModel<String> items;
    private HashMap<String, String> menuFiles;
    private JButton parseXmlsButton;
    private String wsdlLink = "";
    private String selectedAction;

    LeftMenu(final MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        menuFiles = new HashMap<>();
        setLayout(new BorderLayout());
        parseXmlsButton = new JButton("Отобразить методы");
        parseXmlsButton.addActionListener(actionEvent -> {
            wsdlLink = JOptionPane.showInputDialog(mainFrame, "Вставьте ссылку:");
            if (!wsdlLink.isEmpty()) {
                try {
                    createDirectoryForXmls();
                    createXmls(getWsdlInterface(wsdlLink));
                    refreshMenu("./XMLs/");
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(null, "Ошибка получения списка запросов.");
                }
            }
        });

        items = new DefaultListModel<>();
        itemsList = new JList<>(items);
        itemsList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                selectedAction = itemsList.getSelectedValue();
                String path = menuFiles.get(selectedAction);
                if (Files.notExists(Paths.get(path))) {
                    JOptionPane.showMessageDialog(null, String.format("%s не существует.", path));
                } else {
                    try {
                        mainFrame.setLeftMenuState(path);
                    } catch (ParserConfigurationException | SAXException | IOException e) {
                        JOptionPane.showMessageDialog(null, "Невозможно обновить дерево");
                    }
                }
            }
        });
        itemsList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        itemsList.setLayoutOrientation(JList.VERTICAL);

        add(new JScrollPane(itemsList), BorderLayout.CENTER);
        add(parseXmlsButton, BorderLayout.SOUTH);
    }

    public String getWsdlLink() {
        return wsdlLink;
    }

    public String getSelectedAction() {
        return selectedAction;
    }

    private WsdlInterface getWsdlInterface(String url) throws Exception {
        WsdlProject project = new WsdlProject();
        WsdlInterface[] wsdls = WsdlImporter.importWsdl(project, url);
        return wsdls[0];
    }

    private void createXmls(WsdlInterface wsdl) throws ParserConfigurationException, SAXException, IOException, TransformerException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        TransformerFactory tFactory = TransformerFactory.newInstance();
        Transformer transformer = tFactory.newTransformer();

        for (Operation operation : wsdl.getOperationList()) {
            WsdlOperation wsdlOperation = (WsdlOperation) operation;
            String request = wsdlOperation.createRequest(true);
            Document doc = builder.parse(new InputSource(new StringReader(request)));

            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File("./XMLs/" + wsdlOperation.getName() + ".xml"));
            transformer.transform(source, result);
        }
    }

    void refreshMenu(String path) {
        File folder = new File(path);
        File[] listOfFiles = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".xml"));
        items.removeAllElements();
        menuFiles.clear();

        if (listOfFiles != null) {
            for (File listOfFile : listOfFiles) {
                menuFiles.put(listOfFile.getName(), listOfFile.getAbsolutePath());
                items.addElement(listOfFile.getName());
            }
        }
    }

    private void createDirectoryForXmls() {
        File theDir = new File("XMLs");
        if (!theDir.exists()) {
            try {
                theDir.mkdir();
            } catch (SecurityException se) {
                JOptionPane.showMessageDialog(null, "Невозможно создать папку для файлов.");
            }

        }
    }
}
