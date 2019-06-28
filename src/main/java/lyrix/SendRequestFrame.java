package lyrix;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;

public class SendRequestFrame extends JFrame {
    private SendRequestPanel sendRequestPanel;
    private GetRequestPanel getRequestPanel;
    private JList<String> itemsList;
    private DefaultListModel<String> items;
    private HashMap<String, RequestObject> requests;
    private JButton deleteButton;
    private int counter = 1;

    public SendRequestFrame() {
        super("Отправить запрос");
        sendRequestPanel = new SendRequestPanel(this);
        getRequestPanel = new GetRequestPanel(this);

        setLayout(new BorderLayout());

        setSize(800, 600);
        JSplitPane centralSplitMenu = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(sendRequestPanel), new JScrollPane(getRequestPanel));
        centralSplitMenu.setDividerLocation(400);

        items = new DefaultListModel<>();
        itemsList = new JList<>(items);
        itemsList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                String selectedValue = itemsList.getSelectedValue();
                showRequestObject(requests.get(selectedValue));
            }
        });
        itemsList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        itemsList.setLayoutOrientation(JList.VERTICAL);

        requests = new HashMap<>();
        deleteButton = new JButton("Удалить");

        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                DefaultListModel model = (DefaultListModel) itemsList.getModel();
                int selectedIndex = itemsList.getSelectedIndex();
                if (selectedIndex != -1) {
                    String selectedValue = itemsList.getSelectedValue();
                    model.remove(selectedIndex);
                    requests.remove(selectedValue);
                    resetView();
                }
            }
        });

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(deleteButton);

        JSplitPane menuSplitPale = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(itemsList), centralSplitMenu);
        menuSplitPale.setDividerLocation(150);

        add(topPanel, BorderLayout.NORTH);
        add(menuSplitPale, BorderLayout.CENTER);
    }

    public void addRequestObject(RequestObject requestObject) {
        String actionName = requestObject.getActionName();
        String formattedActionName = actionName + " - " + counter++;
        requests.put(formattedActionName, requestObject);
        items.addElement(formattedActionName);
        showRequestObject(requestObject);
    }

    public void showRequestObject(RequestObject requestObject) {
        sendRequestPanel.showText(requestObject.getRequest());
        getRequestPanel.showText(requestObject.getResponse());
    }

    private void resetView() {
        sendRequestPanel.showText("");
        getRequestPanel.showText("");
    }
}
