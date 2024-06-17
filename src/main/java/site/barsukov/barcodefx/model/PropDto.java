package site.barsukov.barcodefx.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import site.barsukov.barcodefx.model.enums.SysPropType;

@Data
@Builder
@Jacksonized
@AllArgsConstructor
public class PropDto {
    private Long id;
    private String name;
    private String descr;
    private String value;
    private SysPropType type;
}
