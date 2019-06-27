package lyrix;

public class CollapseTreeCommand implements ICommand {
    TreeMenu treeMenu;

    public CollapseTreeCommand(TreeMenu treeMenu) {
        this.treeMenu = treeMenu;
    }

    @Override
    public void execute() {
        treeMenu.expandAll(false);
    }
}
