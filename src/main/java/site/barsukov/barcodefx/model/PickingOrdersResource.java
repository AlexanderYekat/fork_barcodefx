package site.barsukov.barcodefx.model;

import site.barsukov.barcodefx.model.yo.pickingorder.PickingOrder;

import java.util.Enumeration;
import java.util.List;
import java.util.ResourceBundle;

public class PickingOrdersResource extends ResourceBundle {
    public PickingOrdersResource(List<PickingOrder> pickingOrders) {
        this.pickingOrders = pickingOrders;
    }

    private List<PickingOrder> pickingOrders;
    @Override
    protected Object handleGetObject(String key) {
        return pickingOrders;
    }

    @Override
    public Enumeration<String> getKeys() {
        return null;
    }
}
