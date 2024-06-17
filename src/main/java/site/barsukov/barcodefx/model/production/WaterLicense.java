package site.barsukov.barcodefx.model.production;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class WaterLicense {
    public String number;
    public LocalDate date;
}
