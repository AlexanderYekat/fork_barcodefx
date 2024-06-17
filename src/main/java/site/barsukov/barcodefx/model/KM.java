package site.barsukov.barcodefx.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class KM {
    private String gtin;
    private String serial;
    private String suzString;
    private boolean sscc = false;

    public KM(String gtin, String serial) {
        this.gtin = gtin;
        this.serial = serial;
    }

    public String getShortGtin() {
        return gtin.substring(2);
    }

    @JsonIgnore
    public String getSscc() {
        if (!sscc) {
            throw new IllegalArgumentException("Код не является SSCC");
        }
        return suzString;
    }

    public boolean isSscc() {
        return sscc;
    }
}
