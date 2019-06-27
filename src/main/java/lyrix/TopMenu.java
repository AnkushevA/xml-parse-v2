package lyrix;

import javax.swing.*;
import java.awt.*;
import java.io.File;

import static lyrix.ButtonFactory.makeButton;

class TopMenu extends JPanel {
    private ICommand expandCommand;
    private ICommand collapseCommand;
    private final MainFrame mainFrame;

    TopMenu(final MainFrame mainFrame, ICommand expandCommand, ICommand collapseCommand) {
        this.mainFrame = mainFrame;
        this.expandCommand = expandCommand;
        this.collapseCommand = collapseCommand;
        JButton expandTreeButton = makeButton("Развернуть дерево", actionEvent -> expandCommand.execute());

        JButton collapseTreeButton = makeButton("Свернуть дерево", actionEvent -> collapseCommand.execute());

        JButton makeXMLButton = makeButton("Отправить запрос", actionEvent -> mainFrame.showXMLRequestWindow(mainFrame.makeXML()));

        setLayout(new FlowLayout(FlowLayout.LEFT));

        add(expandTreeButton);
        add(collapseTreeButton);
        add(makeXMLButton);
    }

    private void chooseFolder() {
        JFileChooser fileChooser = new JFileChooser();

        fileChooser.setDialogTitle("Выбор директории");
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        File workingDirectory = new File(System.getProperty("user.dir"));
        fileChooser.setCurrentDirectory(workingDirectory);

        int result = fileChooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            String path = fileChooser.getSelectedFile().getAbsolutePath();
            mainFrame.refreshList(path);
        }
    }
}
