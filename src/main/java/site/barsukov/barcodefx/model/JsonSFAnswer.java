package site.barsukov.barcodefx.model;


import com.fasterxml.jackson.annotation.JsonProperty;

public class JsonSFAnswer {
    @JsonProperty("platform_releases")
    private JsonSFPlatformReleases platformRelease;

    public JsonSFPlatformReleases getPlatformRelease() {
        return platformRelease;
    }

    public void setPlatformRelease(JsonSFPlatformReleases platformRelease) {
        this.platformRelease = platformRelease;
    }
}
