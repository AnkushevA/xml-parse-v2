package lyrix;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.EventObject;


class CheckBoxNodeEditor extends AbstractCellEditor implements TreeCellEditor {
    private JTree tree;
    private CheckBoxNodeRenderer renderer = new CheckBoxNodeRenderer();
    private TreeMenu parentPanel;


    public CheckBoxNodeEditor(JTree tree, TreeMenu parentPanel) {
        this.tree = tree;
        this.parentPanel = parentPanel;
    }

    public Object getCellEditorValue() {
        return renderer.getObject();
    }

    public boolean isCellEditable(EventObject event) {
        if (event instanceof MouseEvent) {
            MouseEvent mouseEvent = (MouseEvent) event;
            TreePath path = tree.getPathForLocation(mouseEvent.getX(), mouseEvent.getY());
            if (path != null) {
                Object node = path.getLastPathComponent();
                if (node instanceof DefaultMutableTreeNode) {
                    DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) node;
                    if (treeNode.isRoot() || tree.getModel().getIndexOfChild(tree.getModel().getRoot(), treeNode) != -1) {
                        return false;
                    } else {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public Component getTreeCellEditorComponent(final JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row) {
        Component editor = renderer.getTreeCellRendererComponent(tree, value, true, expanded, leaf, row, true);
        if (editor instanceof JLabel) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
            parentPanel.showEditFields(node);
        }
        return editor;
    }
}