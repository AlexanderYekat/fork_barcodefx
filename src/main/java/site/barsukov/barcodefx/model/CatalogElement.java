package site.barsukov.barcodefx.model;

import java.util.HashMap;
import java.util.Map;

public class CatalogElement {
    private String gtin;
    private Map<String, String> values = new HashMap<>();

    public CatalogElement() {
    }

    public CatalogElement(String gtin) {
        this.gtin = gtin;
    }

    public String getGtin() {
        return gtin;
    }

    public void setGtin(String gtin) {
        this.gtin = gtin;
    }

    public boolean hasValue(String key) {
        return values.containsKey(key);
    }

    public void addValue(String key, String value) {
        values.put(key, value);
    }

    public String getProperty(String key) {
        return values.get(key);
    }
}
