package site.barsukov.barcodefx.context;

import lombok.Data;

import java.time.LocalDate;

@Data
public class AcceptanceContext extends BaseContext {
    private String receiverINN;
    private String senderINN;
    private String transferDate;
    private LocalDate transferDateDate;
    private String moveDocDate;
    private String moveDocNum;
    private String acceptanceType;
    private String shipmentId;
}
