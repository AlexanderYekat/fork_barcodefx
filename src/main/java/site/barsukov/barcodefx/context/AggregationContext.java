package site.barsukov.barcodefx.context;

import lombok.Data;

@Data
public class AggregationContext extends BaseContext {
    private String documentNumber;
    private String participantINN;
    private String orgName;
    private String sscc;
    private boolean ip; //является ли ИП-шником
}
