package site.barsukov.barcodefx.model;

import java.util.Enumeration;
import java.util.ResourceBundle;

public class AboutResource extends ResourceBundle {
    public AboutResource(AboutTextEnum aboutTextEnum) {
        this.aboutTextEnum = aboutTextEnum;
    }

    AboutTextEnum aboutTextEnum;
    @Override
    protected Object handleGetObject(String key) {
        return aboutTextEnum;
    }

    @Override
    public Enumeration<String> getKeys() {
        return null;
    }
}
