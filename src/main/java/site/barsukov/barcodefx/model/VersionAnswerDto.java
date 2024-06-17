package site.barsukov.barcodefx.model;

import com.fasterxml.jackson.annotation.JsonRootName;

@JsonRootName(value = "platform_releases.windows")
public class VersionAnswerDto {
    private String filename;
    private String url;
    private String version;

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
