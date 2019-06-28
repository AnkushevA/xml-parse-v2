package lyrix;

public class RequestObject {
    private String request;
    private String response;
    private String actionName;

    public RequestObject(String request, String response, String actionName) {
        this.request = request;
        this.response = response;
        this.actionName = actionName;
    }

    public String getRequest() {
        return request;
    }

    public void setRequest(String request) {
        this.request = request;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public String getActionName() {
        return actionName;
    }

    public void setActionName(String actionName) {
        this.actionName = actionName;
    }

    @Override
    public String toString() {
        return "RequestObject{" +
                "request='" + request + '\'' +
                ", response='" + response + '\'' +
                ", actionName='" + actionName + '\'' +
                '}';
    }
}
