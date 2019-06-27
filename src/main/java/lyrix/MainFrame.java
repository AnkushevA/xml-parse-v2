package lyrix;

import org.xml.sax.SAXException;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;

class MainFrame extends JFrame {
    private SendRequestFrame sendRequestFrame;
    private TreeMenu treeMenu;
    private LeftMenu leftMenu;
    private TopMenu topMenu;
    private JSplitPane centralSplitMenu;
    private JScrollPane leftMenuScrollPane;
    private JScrollPane treeScrollPane;
    private NodeEditMenu nodeEditMenu;
    private JScrollPane nodeEditMenuScrollPane;
    private ArrayList<LeftMenuUpdateListener> observers = new ArrayList<>();
    private String xmlPath;


    MainFrame() {
        super("WSDL loader");
        setSize(1000, 700);
        setLayout(new BorderLayout());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        treeMenu = new TreeMenu(this);
        leftMenu = new LeftMenu(this);
        ExpandTreeCommand expandTreeCommand = new ExpandTreeCommand(treeMenu);
        CollapseTreeCommand collapseTreeCommand = new CollapseTreeCommand(treeMenu);
        nodeEditMenu = new NodeEditMenu(this, treeMenu.getTree());
        topMenu = new TopMenu(this, expandTreeCommand, collapseTreeCommand);

        addTopMenu();
        createTreeMenu();
        createNodeEditMenu();
        createLeftMenu();
        addSplitMenu();

        attachListener(nodeEditMenu);
        attachListener(treeMenu);

        setVisible(true);
    }

    public String getLeftMenuState() {
        return xmlPath;
    }

    public void setLeftMenuState(String xmlPath) throws ParserConfigurationException, SAXException, IOException {
        this.xmlPath = xmlPath;
        notifyAllObservers();
    }

    public void attachListener(LeftMenuUpdateListener leftMenuUpdateListener) {
        observers.add(leftMenuUpdateListener);
    }

    private void notifyAllObservers() throws IOException, SAXException, ParserConfigurationException {
        for (LeftMenuUpdateListener leftMenuUpdateListener : observers) {
            leftMenuUpdateListener.update(xmlPath);
        }
    }

    private void createNodeEditMenu() {
        nodeEditMenuScrollPane = new JScrollPane(nodeEditMenu);
        nodeEditMenuScrollPane.setBorder(BorderFactory.createTitledBorder("Info"));
    }

    private void addSplitMenu() {
        JSplitPane leftSplitMenu = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftMenuScrollPane, treeScrollPane);
        leftSplitMenu.setOneTouchExpandable(true);
        leftSplitMenu.setDividerLocation(300);

        centralSplitMenu = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftSplitMenu, nodeEditMenuScrollPane);
        centralSplitMenu.setDividerLocation(650);

        add(centralSplitMenu, BorderLayout.CENTER);
    }

    private void addTopMenu() {
        add(topMenu, BorderLayout.NORTH);
    }

    private void createLeftMenu() {
        leftMenuScrollPane = new JScrollPane(leftMenu);
        leftMenuScrollPane.setPreferredSize(new Dimension(150, getHeight()));
        leftMenuScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        leftMenuScrollPane.setBorder(BorderFactory.createTitledBorder(".XML"));
    }

    private void createTreeMenu() {
        treeScrollPane = new JScrollPane(treeMenu);
        treeScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        treeScrollPane.setBorder(BorderFactory.createTitledBorder("Tree view:"));
    }

    void setNodeEditTree(JTree nodeEditTree) {
        nodeEditMenu.setTree(nodeEditTree);
    }

    void expandTree(boolean expand) {
        treeMenu.expandAll(expand);
    }

    private SOAPMessage constructXMLFromTree() throws SOAPException {
        return treeMenu.constructXMLFromTree();
    }

    private SOAPMessage callSoapWebService(SOAPMessage soapMessage) {
        try {
            String soapEndpointUrl = leftMenu.getWsdlLink().replace("?wsdl", "");
            SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
            SOAPConnection soapConnection = soapConnectionFactory.createConnection();

            SOAPMessage soapResponse = soapConnection.call(soapMessage, soapEndpointUrl);

            soapConnection.close();
            return soapResponse;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Ошибка при отправке запроса.");
        }
        return null;
    }

    private String getXmlFormattedString(SOAPMessage soapMsg) throws SOAPException, IOException, TransformerException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        soapMsg.writeTo(out);
        String strMsg = new String(out.toByteArray());
        StreamResult streamResult = new StreamResult(new StringWriter());
        Source xmlInput = new StreamSource(new StringReader(strMsg));
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        transformer.transform(xmlInput, streamResult);
        return streamResult.getWriter().toString();
    }

    void showXMLRequestWindow() throws SOAPException, TransformerException, IOException {
        SOAPMessage message = constructXMLFromTree();
        String xmlFormattedString = getXmlFormattedString(message);
        SOAPMessage soapMsg = callSoapWebService(message);

        if (soapMsg != null) {
            String xmlFormattedRespond = getXmlFormattedString(soapMsg);
            if (sendRequestFrame == null) {
                sendRequestFrame = new SendRequestFrame(xmlFormattedString, xmlFormattedRespond);
                sendRequestFrame.pack();
            }
            sendRequestFrame.showText(xmlFormattedString, xmlFormattedRespond);
            sendRequestFrame.setVisible(true);
        }
    }

    void showEditFields(DefaultMutableTreeNode node) {
        nodeEditMenu.showEditFields(node);
    }


}
