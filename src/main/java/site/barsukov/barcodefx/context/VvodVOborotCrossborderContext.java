package site.barsukov.barcodefx.context;

import lombok.Data;

import java.time.LocalDate;

@Data
public class VvodVOborotCrossborderContext extends BaseContext {
    private String userINN;
    private String senderINN;
    private String exporterName;
    private String countryOksm;
    private String vsdNumber;
    private LocalDate importDate;
    private String primaryDocNum;
    private LocalDate primaryDocDate;
}
