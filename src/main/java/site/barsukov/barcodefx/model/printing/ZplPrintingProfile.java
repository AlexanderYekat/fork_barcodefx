package site.barsukov.barcodefx.model.printing;

import lombok.Data;

@Data
public class ZplPrintingProfile {
    private String name;
    private String printerIp;
    private Integer printerPort;
    private String template;
}
