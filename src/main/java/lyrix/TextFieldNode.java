package lyrix;

public class TextFieldNode {
    private String attribute;
    private String text;
    private boolean include;

    TextFieldNode(String attribute, String text, boolean include) {
        this.attribute = attribute.substring(attribute.indexOf(":") + 1);
        this.text = text;
        this.include = include;
    }

    String getAttribute() {
        return attribute;
    }

    public void setAttribute(String attribute) {
        this.attribute = attribute;
    }

    boolean isIncluded() {
        return include;
    }

    void setIncluded(boolean include) {
        this.include = include;
    }

    String getText() {
        return text;
    }

    void setText(String text) {
        this.text = text;
    }

    String getDefaultString() {
        return String.format("[%s]:%s", attribute, text);
    }

    @Override
    public String toString() {
        String output = (include ? "<font color='green'>[" + attribute + "]:</font>" : "<font color='red'>[" + attribute + "]:</font>") + text;
        return "<html>" + output + "</html>";
    }


}
