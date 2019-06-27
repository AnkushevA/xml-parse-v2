package lyrix;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.swing.*;
import javax.swing.tree.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.*;
import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.awt.*;
import java.io.*;
import java.util.Enumeration;
import java.util.Iterator;

class TreeMenu extends JPanel implements LeftMenuUpdateListener {
    private JTree tree;
    private final MainFrame mainFrame;

    TreeMenu(final MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        tree = new JTree(new DefaultTreeModel(new DefaultMutableTreeNode("Root")));
        CheckBoxNodeRenderer renderer = new CheckBoxNodeRenderer();
        tree.setCellRenderer(renderer);
        tree.setCellEditor(new CheckBoxNodeEditor(tree, this));
        tree.setEditable(true);

//        drawTree("C:\\Users\\BASS4x4\\IntelliJIDEAProjects\\xmlparse\\src\\main\\resources\\example1.xml");
        setLayout(new BorderLayout());
        add(tree, BorderLayout.CENTER);
    }

    @Override
    public void update(String xmlPath) {
        drawTree(xmlPath);
        mainFrame.setNodeEditTree(tree);
    }

    JTree getTree() {
        return tree;
    }

    public void showEditFields(DefaultMutableTreeNode node) {
        mainFrame.showEditFields(node);
    }

    void drawTree(String xmlPath) {
        try {
            DefaultMutableTreeNode node = getRootNode(xmlPath); //построить дерево
            DefaultTreeModel treeModel = ((DefaultTreeModel) tree.getModel());
            treeModel.setRoot(node);
            treeModel.reload();

        } catch (ParserConfigurationException | SAXException | IOException e) {
            JOptionPane.showMessageDialog(null, "Невозможно отобразить дерево!");
        }
    }

    private DefaultMutableTreeNode getRootNode(String xmlPath) throws ParserConfigurationException, SAXException, IOException {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode("XML"); //корень дерева

        DocumentBuilderFactory xmlBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = xmlBuilderFactory.newDocumentBuilder();
        xmlBuilderFactory.setIgnoringElementContentWhitespace(true);
        xmlBuilderFactory.setIgnoringComments(true);
        File xmlFile = new File(xmlPath);

        Document xmlDocument = documentBuilder.parse(xmlFile);
        Element documentElement = xmlDocument.getDocumentElement();

        if (documentElement.hasChildNodes()) {
            NodeList childNodes = documentElement.getChildNodes();
            for (int i = 0; i < childNodes.getLength(); i++) {
                Node child = childNodes.item(i);
                addNode(child, node);
            }
        }
        return node;
    }

    private void addNode(Node childNode, DefaultMutableTreeNode parentNode) {
        if (childNode.getNodeType() == Node.ELEMENT_NODE) {
            Element documentElement = (Element) childNode;
            DefaultMutableTreeNode node = new DefaultMutableTreeNode(new TextFieldNode(documentElement.getTagName(), "", true));
            parentNode.add(node);

            if (documentElement.hasChildNodes()) {
                NodeList list = documentElement.getChildNodes();
                for (int i = 0; i < list.getLength(); i++) {
                    addNode(list.item(i), node);
                }
            }
        }
    }

    void expandAll(boolean expand) {
        TreeNode root = (TreeNode) tree.getModel().getRoot();
        expandAll(new TreePath(root), expand);
    }

    private void expandAll(TreePath parent, boolean expand) {
        TreeNode node = (TreeNode) parent.getLastPathComponent();
        if (node.getChildCount() >= 0) {
            for (Enumeration e = node.children(); e.hasMoreElements(); ) {
                TreeNode n = (TreeNode) e.nextElement();
                TreePath path = parent.pathByAddingChild(n);
                expandAll(path, expand);
            }
        }
        if (expand) {
            tree.expandPath(parent);
        } else {
            tree.collapsePath(parent);
        }
    }

    String makeXML() {
        try {
            MessageFactory factory = MessageFactory.newInstance();
            SOAPMessage soapMsg = factory.createMessage();
            SOAPPart part = soapMsg.getSOAPPart();

            SOAPEnvelope envelope = part.getEnvelope();

            Iterator namespacePrefixes = envelope.getNamespacePrefixes();
            while (namespacePrefixes.hasNext()) {
                envelope.removeNamespaceDeclaration(((String) namespacePrefixes.next()));
            }

            SOAPBody body = envelope.getBody();

            soapMsg.getSOAPHeader().setPrefix("soapenv");
            body.setPrefix("soapenv");
            envelope.setPrefix("soapenv");
            envelope.addNamespaceDeclaration("car", "http://cardlibrary2.webservices.integration.css.aamsystems.com");
            DefaultMutableTreeNode root = (DefaultMutableTreeNode) tree.getModel().getRoot();
            int childCount = root.getChildCount();

            for (int i = 0; i < childCount; i++) {
                DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) (tree.getModel().getChild(root, i));
                TextFieldNode userObject = (TextFieldNode) treeNode.getUserObject();
                if (userObject.getAttribute().toLowerCase().contains("body")) { //узлы внутри body
                    TreeModel treeModel = tree.getModel();
                    int count = treeNode.getChildCount();
                    for (int j = 0; j < count; j++) {
                        DefaultMutableTreeNode bodyChild = (DefaultMutableTreeNode) (treeModel.getChild(treeNode, j));
                        addXMLChildNode(body, bodyChild, envelope);
                    }
                }
            }
            return getXmlFormattedString(soapMsg);
        } catch (SOAPException | IOException | TransformerException e) {
            JOptionPane.showMessageDialog(null, "Невозможно создать XML!");
        }
        return "";
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
/*
            transformer.transform(xmlInput,
                    new StreamResult(new FileOutputStream("C:\\Users\\BASS4x4\\IntelliJIDEAProjects\\xmlparse\\src\\main\\resources\\outputXML.xml")));
*/
        transformer.transform(xmlInput, streamResult);
        return streamResult.getWriter().toString();
    }

    private void addXMLChildNode(SOAPElement parent, DefaultMutableTreeNode childNode, SOAPEnvelope envelope) {
        TextFieldNode childTextFieldNode = (TextFieldNode) childNode.getUserObject();
        if (childTextFieldNode.isIncluded()) {
            SOAPElement soapElement = null;
            if (childNode.isLeaf()) {
                if (!childTextFieldNode.getText().isEmpty()) {
                    try {
                        soapElement = parent.addChildElement(childTextFieldNode.getAttribute(), "car");
                        soapElement.addTextNode(childTextFieldNode.getText());
                    } catch (SOAPException e) {
                        JOptionPane.showMessageDialog(null, "Ошибка добавления элемента в XML!");
                    }
                }
            } else {
                try {
                    if (parent instanceof SOAPBody) {
                        soapElement = ((SOAPBody) parent).addBodyElement(envelope.createName(childTextFieldNode.getAttribute(), "car", "http://cardlibrary2.webservices.integration.css.aamsystems.com"));
                    } else {
                        soapElement = parent.addChildElement(childTextFieldNode.getAttribute(), "car");
                    }
                } catch (SOAPException e) {
                    JOptionPane.showMessageDialog(null, "Ошибка добавления элемента в XML!");
                }

                TreeModel treeModel = tree.getModel();
                int childCount = childNode.getChildCount();
                for (int i = 0; i < childCount; i++) {
                    DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) (treeModel.getChild(childNode, i));
                    addXMLChildNode(soapElement, treeNode, envelope);
                }
            }
        }
    }

}
