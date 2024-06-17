package site.barsukov.barcodefx.model;

import site.barsukov.barcodefx.services.PropService;

import java.util.Enumeration;
import java.util.ResourceBundle;

public class PickingServerResource extends ResourceBundle {
    public PickingServerResource(PropService propService) {
        this.propService = propService;
    }

    PropService propService;
    @Override
    protected Object handleGetObject(String key) {
        return propService;
    }

    @Override
    public Enumeration<String> getKeys() {
        return null;
    }
}
