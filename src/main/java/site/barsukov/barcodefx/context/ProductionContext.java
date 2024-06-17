package site.barsukov.barcodefx.context;

import lombok.Data;
import site.barsukov.barcodefx.model.production.WaterLicense;

import java.time.LocalDate;
import java.util.List;

@Data
public class ProductionContext extends BaseContext {
    private String productionOrder;
    private String certificateType;
    private LocalDate productDate;
    private String participantInn;
    private String producerInn;
    private String ownerInn;
    private String certificateNumber;
    private LocalDate certificateDate;
    private String tnvedCode;
    private String vsdNumber;
    private List<WaterLicense> waterLicenses;
}
