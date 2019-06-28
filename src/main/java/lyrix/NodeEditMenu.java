package lyrix;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;


class NodeEditMenu extends JPanel implements LeftMenuUpdateListener {
    private final MainFrame mainFrame;
    private JLabel nameLabel;
    private JTextField dataField;
    private JCheckBox includeToOutput;
    private JButton okButton;
    private JButton addButton;
    private JButton removeButton;
    private JList<String> itemsList;
    private DefaultMutableTreeNode node;
    private JTree tree;
    private DefaultListModel<String> model;
    private HashMap<DefaultMutableTreeNode, JTextField> leafNodes;
    private boolean useHashMap;

    NodeEditMenu(MainFrame mainFrame, JTree tree) {
        this.tree = tree;
        this.mainFrame = mainFrame;
        nameLabel = new JLabel("Node name");
        nameLabel.setFont(nameLabel.getFont().deriveFont(20.0f));
        dataField = new JTextField(15);

        removeButton = new JButton("-");
        removeButton.addActionListener(actionEvent -> {
            int[] selectedItems = itemsList.getSelectedIndices();
            if (selectedItems.length == 1) {
                node.remove(selectedItems[0]);
                updateTreeModel();
                model.removeElementAt(selectedItems[0]);
            }
        });

        addButton = new JButton("+");
        addButton.addActionListener(actionEvent -> {
            if (node != null) {
                TextFieldNode textFieldNode = ((TextFieldNode) node.getUserObject());
                String attribute = textFieldNode.getAttribute();
                boolean isEnabled = ((TextFieldNode) node.getUserObject()).isIncluded();

                TextFieldNode fieldToAdd = new TextFieldNode("item", "", isEnabled);
                DefaultMutableTreeNode nodeToAdd = null;

                if (attribute.equals("fields")) {
                    nodeToAdd = makeFieldsNode(fieldToAdd, isEnabled);
                } else if (attribute.equals("accessLevels")) {
                    nodeToAdd = makeAccessLevelsNode(fieldToAdd, isEnabled);
                }

                if (nodeToAdd != null) {
                    DefaultTreeModel treeModel = (DefaultTreeModel) tree.getModel();
                    treeModel.insertNodeInto(nodeToAdd, node, node.getChildCount());
                    model.addElement(fieldToAdd.getDefaultString());
                }
            }
        });

        okButton = new JButton("OK");
        okButton.addActionListener(actionEvent -> {
            if (node != null) {
                if (!useHashMap) {
                    TextFieldNode textFieldNode = ((TextFieldNode) node.getUserObject());
                    if (shouldHaveEditFields(textFieldNode)) {
                        textFieldNode.setText(node.isLeaf() ? dataField.getText() : "");
                        textFieldNode.setIncluded(includeToOutput.isSelected());
                    }
                    makeNodeAndChildrenEnabled(node, includeToOutput.isSelected());
                } else{
                    for (Map.Entry<DefaultMutableTreeNode, JTextField> entry : leafNodes.entrySet()) {
                        DefaultMutableTreeNode key = entry.getKey();
                        JTextField value = entry.getValue();
                        TextFieldNode textFieldNode = ((TextFieldNode) key.getUserObject());
                        textFieldNode.setText(value.getText());
                    }
                }
                updateTreeModel();
            }
        });

        includeToOutput = new JCheckBox("Enabled");
        model = new DefaultListModel<>();
        itemsList = new JList<>(model);
        itemsList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        itemsList.setVisibleRowCount(-1);

        leafNodes = new HashMap<>();

        setLayout(new GridBagLayout());
        setLeafEditPanel();
    }

    private boolean shouldHaveEditFields(TextFieldNode textFieldNode) {
        return textFieldNode.getAttribute().equals("accessLevels") || textFieldNode.getAttribute().equals("fields") || node.isLeaf();
    }

    public void setTree(JTree tree) {
        this.tree = tree;
    }

    @Override
    public void update(String xmlPath) {
        setDefaultState();
    }

    private void setDefaultState() {
        setLeafEditPanel();
        nameLabel.setText("Node name");
        dataField.setText("");
        includeToOutput.setSelected(false);
        model.removeAllElements();
        leafNodes.clear();
        node = null;
    }

    private void updateTreeModel() {
        ((DefaultTreeModel) tree.getModel()).reload();
        mainFrame.expandTree(true);
    }

    private DefaultMutableTreeNode makeFieldsNode(TextFieldNode fieldToAdd, boolean isEnabled) {
        DefaultMutableTreeNode nodeToAdd = new DefaultMutableTreeNode(fieldToAdd);
        nodeToAdd.add(new DefaultMutableTreeNode(new TextFieldNode("MName", "", isEnabled)));
        nodeToAdd.add(new DefaultMutableTreeNode(new TextFieldNode("MLabel", "", isEnabled)));
        nodeToAdd.add(new DefaultMutableTreeNode(new TextFieldNode("MType", "", isEnabled)));
        nodeToAdd.add(new DefaultMutableTreeNode(new TextFieldNode("subType", "", isEnabled)));
        nodeToAdd.add(new DefaultMutableTreeNode(new TextFieldNode("MValue", "", isEnabled)));
        return nodeToAdd;
    }

    private DefaultMutableTreeNode makeAccessLevelsNode(TextFieldNode fieldToAdd, boolean isEnabled) {
        DefaultMutableTreeNode nodeToAdd = new DefaultMutableTreeNode(fieldToAdd);
        DefaultMutableTreeNode idNode = new DefaultMutableTreeNode(new TextFieldNode("id", "", isEnabled));
        idNode.add(new DefaultMutableTreeNode(new TextFieldNode("additionalID", "", isEnabled)));
        idNode.add(new DefaultMutableTreeNode(new TextFieldNode("primaryID", "", isEnabled)));
        idNode.add(new DefaultMutableTreeNode(new TextFieldNode("systemID", "", isEnabled)));
        nodeToAdd.add(idNode);
        nodeToAdd.add(new DefaultMutableTreeNode(new TextFieldNode("label", "", isEnabled)));
        return nodeToAdd;
    }

    void showEditFields(DefaultMutableTreeNode node) {
        this.node = node;
        TextFieldNode textFieldNode = (TextFieldNode) node.getUserObject();
        includeToOutput.setSelected(textFieldNode.isIncluded());
        nameLabel.setText(textFieldNode.getAttribute());
        useHashMap = false;
        if (isArrayType(textFieldNode)) {
            setListEditPanel();
            fillArrayTypeList(node);
        } else if (node.isLeaf()) {
            setLeafEditPanel();
            dataField.setText(textFieldNode.getText());
        } else if (ifNodeHasOnlyLeafs(node)) {
            useHashMap = true;
            setListOfLeafsPanel();
        } else {
            setDefaultEditPanel();
        }

    }

    private void setListOfLeafsPanel() {
        cleanPanel();
        leafNodes.clear();

        GridBagConstraints gc = new GridBagConstraints();

        TreeModel treeModel = tree.getModel();
        int counter = 0;

        gc.gridy = counter++;
        gc.weightx = 1;
        gc.anchor = GridBagConstraints.CENTER;
        gc.insets = new Insets(0, 0, 15, 0);
        add(nameLabel, gc);

        int childCount = treeModel.getChildCount(node);
        gc.insets = new Insets(0, 0, 0, 0);
        for (int i = 0; i < childCount; i++) {
            DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) (treeModel.getChild(node, i));
            TextFieldNode textFieldNode = (TextFieldNode) treeNode.getUserObject();

            gc.gridy = counter++;
            gc.weightx = 1;
            gc.anchor = GridBagConstraints.CENTER;
            add(new JLabel(textFieldNode.getAttribute()), gc);

            JTextField textField = new JTextField(textFieldNode.getText(), 15);
            gc.gridy = counter++;
            gc.weightx = 1;
            gc.anchor = GridBagConstraints.CENTER;
            add(textField, gc);
            leafNodes.put(treeNode, textField);
        }

        gc.gridy = counter;
        gc.weighty = 1;
        gc.weightx = 1;
        gc.anchor = GridBagConstraints.PAGE_START;
        gc.insets = new Insets(10, 0, 0, 0);
        add(okButton, gc);
        repaint();
    }

    private boolean ifNodeHasOnlyLeafs(DefaultMutableTreeNode node) {
        TreeModel treeModel = tree.getModel();
        int childCount = treeModel.getChildCount(node);
        for (int i = 0; i < childCount; i++) {
            DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) (treeModel.getChild(node, i));
            if (!treeNode.isLeaf()) {
                return false;
            }
        }

        return true;
    }

    private void fillArrayTypeList(DefaultMutableTreeNode node) {
        TreeModel treeModel = tree.getModel();
        model.removeAllElements();
        int childCount = treeModel.getChildCount(node);
        for (int i = 0; i < childCount; i++) {
            DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) (treeModel.getChild(node, i));
            TextFieldNode childNode = (TextFieldNode) treeNode.getUserObject();
            model.addElement(childNode.getDefaultString());
        }
    }

    private boolean isArrayType(TextFieldNode textFieldNode) {
        return textFieldNode.getAttribute().equals("accessLevels") || textFieldNode.getAttribute().equals("fields");
    }

    private void makeNodeAndChildrenEnabled(DefaultMutableTreeNode node, boolean enable) {
        TextFieldNode leafNode = (TextFieldNode) node.getUserObject();
        leafNode.setIncluded(enable);
        if (!node.isLeaf()) {
            TreeModel treeModel = tree.getModel();
            int childCount = treeModel.getChildCount(node);
            for (int i = 0; i < childCount; i++) {
                makeNodeAndChildrenEnabled((DefaultMutableTreeNode) (treeModel.getChild(node, i)), enable);
            }
        }
    }

    public void cleanPanel() {
        for (Component component : getComponents()) {
            remove(component);
            revalidate();
        }
    }

    private void setDefaultEditPanel() {
        cleanPanel();
        GridBagConstraints gc = new GridBagConstraints();

        gc.gridy = 0;
        gc.weightx = 1;
        gc.anchor = GridBagConstraints.CENTER;
        add(nameLabel, gc);

        gc.gridy = 1;
        gc.weightx = 1;
        gc.anchor = GridBagConstraints.CENTER;
        add(includeToOutput, gc);

        gc.gridy = 2;
        gc.weighty = 1;
        gc.weightx = 1;
        gc.anchor = GridBagConstraints.PAGE_START;
        add(okButton, gc);
        repaint();
    }

    private void setLeafEditPanel() {
        cleanPanel();
        GridBagConstraints gc = new GridBagConstraints();

        gc.gridx = 0;
        gc.anchor = GridBagConstraints.CENTER;
        add(nameLabel, gc);

        gc.gridy = 1;
        gc.anchor = GridBagConstraints.CENTER;
        add(includeToOutput, gc);

        gc.gridy = 2;
        gc.anchor = GridBagConstraints.CENTER;
        add(dataField, gc);

        gc.weighty = 1;

        gc.gridy = 3;
        gc.anchor = GridBagConstraints.PAGE_START;
        gc.insets = new Insets(10, 0, 0, 0);
        add(okButton, gc);
        repaint();
    }

    private void setListEditPanel() {
        cleanPanel();
        GridBagConstraints gc = new GridBagConstraints();

        gc.gridwidth = 6;
        gc.gridy = 0;
        gc.anchor = GridBagConstraints.CENTER;
        add(nameLabel, gc);

        gc.gridwidth = 6;
        gc.anchor = GridBagConstraints.CENTER;
        gc.gridy = 1;
        add(includeToOutput, gc);

        gc.gridwidth = 1;
        gc.gridx = 0;
        gc.gridy = 2;
        gc.fill = GridBagConstraints.BOTH;
        gc.insets = new Insets(0, 5, 5, 5);
        add(addButton, gc);

        gc.gridwidth = 1;
        gc.gridx = 0;
        gc.gridy = 3;
        gc.fill = GridBagConstraints.BOTH;
        gc.insets = new Insets(0, 5, 0, 5);
        add(removeButton, gc);


        gc.weightx = 0.4;
        gc.gridheight = 3;
        gc.gridx = 1;
        gc.gridy = 2;
        gc.fill = GridBagConstraints.HORIZONTAL;
        JScrollPane scrollPane = new JScrollPane(itemsList);
        add(scrollPane, gc);
        scrollPane.setPreferredSize(new Dimension(200, 120));

        gc.gridwidth = 1;
        gc.weighty = 1;
        gc.gridy = 6;
        gc.anchor = GridBagConstraints.PAGE_START;
        gc.fill = GridBagConstraints.NONE;
        gc.insets = new Insets(5, 0, 0, 0);
        add(okButton, gc);
        repaint();
    }
}
