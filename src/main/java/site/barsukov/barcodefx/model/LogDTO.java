package site.barsukov.barcodefx.model;

public class LogDTO {

    private String clientId;
    private String sessionId;
    private String appVersion;
    private String serviceName;
    private String categoryName;
    private long linesProcessed;

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public long getLinesProcessed() {
        return linesProcessed;
    }

    public void setLinesProcessed(long linesProcessed) {
        this.linesProcessed = linesProcessed;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }
}
