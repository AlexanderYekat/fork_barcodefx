package site.barsukov.barcodefx.model;

public class TabVisibleDto {
    private String id;
    private String name;
    private Boolean visible;

    public TabVisibleDto(String id, String name, boolean visible) {
        this.id = id;
        this.name = name;
        this.visible = visible;

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean isVisible() {
        return this.visible;
    }

    public void setVisible(Boolean visible) {
        this.visible = visible;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
