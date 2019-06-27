package lyrix;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
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

        setLayout(new BorderLayout());


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
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
        attach(nodeEditMenu);
        attach(treeMenu);

    }

    public String getLeftMenuState() {
        return xmlPath;
    }

    public void setLeftMenuState(String xmlPath) {
        this.xmlPath = xmlPath;
        notifyAllObservers();
    }

    public void attach(LeftMenuUpdateListener leftMenuUpdateListener) {
        observers.add(leftMenuUpdateListener);
    }

    private void notifyAllObservers() {
        for (LeftMenuUpdateListener leftMenuUpdateListener : observers) {
            leftMenuUpdateListener.update(xmlPath);
        }
    }

    void setNodeEditTree(JTree nodeEditTree) {
        nodeEditMenu.setTree(nodeEditTree);
    }

    void expandTree(boolean expand) {
        treeMenu.expandAll(expand);
    }

    void refreshList(String path) {
        leftMenu.refreshMenu(path);
    }

    String makeXML() {
        return treeMenu.makeXML();
    }

    void redrawTree(String xmlPath) {
        treeMenu.drawTree(xmlPath);
    }

    void showXMLRequestWindow(String xmlString) {
        if (sendRequestFrame == null) {
            sendRequestFrame = new SendRequestFrame(xmlString);
            sendRequestFrame.pack();
        }
        sendRequestFrame.showText(xmlString);
        sendRequestFrame.setVisible(true);
    }

    void showEditFields(DefaultMutableTreeNode node) {
        nodeEditMenu.showEditFields(node);
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
}
