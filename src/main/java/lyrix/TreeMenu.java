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
import java.awt.*;
import java.io.File;
import java.io.IOException;
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

        setLayout(new BorderLayout());
        add(tree, BorderLayout.CENTER);
    }

    @Override
    public void update(String xmlPath) throws ParserConfigurationException, SAXException, IOException {
        drawTree(xmlPath);
        mainFrame.setNodeEditTree(tree);
    }

    JTree getTree() {
        return tree;
    }

    public void showEditFields(DefaultMutableTreeNode node) {
        mainFrame.showEditFields(node);
    }

    void drawTree(String xmlPath) throws IOException, SAXException, ParserConfigurationException {
        DefaultMutableTreeNode node = getRootNode(xmlPath);
        DefaultTreeModel treeModel = ((DefaultTreeModel) tree.getModel());
        treeModel.setRoot(node);
        treeModel.reload();
    }

    private DefaultMutableTreeNode getRootNode(String xmlPath) throws ParserConfigurationException, SAXException, IOException {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode("XML"); //корень дерева

        DocumentBuilder documentBuilder = getDocumentBuilder();
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

    private DocumentBuilder getDocumentBuilder() throws ParserConfigurationException {
        DocumentBuilderFactory xmlBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = xmlBuilderFactory.newDocumentBuilder();
        xmlBuilderFactory.setIgnoringElementContentWhitespace(true);
        xmlBuilderFactory.setIgnoringComments(true);
        return documentBuilder;
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

    SOAPMessage constructXMLFromTree() throws SOAPException {
        MessageFactory factory = MessageFactory.newInstance();
        SOAPMessage soapMsg = factory.createMessage();
        SOAPPart part = soapMsg.getSOAPPart();

        SOAPEnvelope envelope = part.getEnvelope();

        Iterator namespacePrefixes = envelope.getNamespacePrefixes();
        removeNamespaces(envelope, namespacePrefixes);

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
        return soapMsg;
    }

    private void addXMLChildNode(SOAPElement parent, DefaultMutableTreeNode childNode, SOAPEnvelope envelope) throws SOAPException {
        TextFieldNode childTextFieldNode = (TextFieldNode) childNode.getUserObject();
        if (childTextFieldNode.isIncluded()) {
            SOAPElement soapElement = null;
            if (childNode.isLeaf()) {
                if (!childTextFieldNode.getText().isEmpty()) {
                    soapElement = parent.addChildElement(childTextFieldNode.getAttribute(), "car");
                    soapElement.addTextNode(childTextFieldNode.getText());

                }
            } else {
                if (parent instanceof SOAPBody) {
                    soapElement = ((SOAPBody) parent).addBodyElement(envelope.createName(childTextFieldNode.getAttribute(), "car", "http://cardlibrary2.webservices.integration.css.aamsystems.com"));
                } else {
                    soapElement = parent.addChildElement(childTextFieldNode.getAttribute(), "car");
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

    private void removeNamespaces(SOAPEnvelope envelope, Iterator namespacePrefixes) {
        while (namespacePrefixes.hasNext()) {
            envelope.removeNamespaceDeclaration(((String) namespacePrefixes.next()));
        }
    }



}
