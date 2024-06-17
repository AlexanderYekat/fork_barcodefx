package site.barsukov.barcodefx.context;

import lombok.Data;

@Data
public class ShipmentContext extends BaseContext {
    private String receiverINN;
    private String senderINN;
    private String transferDate;
    private String moveDocDate;
    private String moveDocNum;
    private String shipmentType;
    private String withdrawType;
    private String withdrawDate;
    private boolean shippingToParticipant;

}
