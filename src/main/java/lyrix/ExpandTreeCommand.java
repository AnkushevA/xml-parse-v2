package lyrix;

public class ExpandTreeCommand implements ICommand {
    TreeMenu treeMenu;

    public ExpandTreeCommand(TreeMenu treeMenu) {
        this.treeMenu = treeMenu;
    }

    @Override
    public void execute() {
        treeMenu.expandAll(true);
    }
}
