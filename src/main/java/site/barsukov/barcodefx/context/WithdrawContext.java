package site.barsukov.barcodefx.context;

import lombok.Data;

import java.time.LocalDate;

@Data
public class WithdrawContext extends BaseContext {
    private String kktNumber;
    private String primaryDocumentCustomName;
    private LocalDate primaryDocumentDate;
    private String primaryDocumentNumber;
    private String primaryDocumentType;
    private String tradeParticipantInn;
    private String withdrawalType;
    private LocalDate withdrawalDate;
}
