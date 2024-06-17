package site.barsukov.barcodefx.services.dto;

import lombok.Builder;
import lombok.Data;
import site.barsukov.barcodefx.model.KM;

import java.util.Set;

@Data
@Builder
public class AggregateMultiCreateValidationResult {
    private final boolean error;
    private final String fileName;
    private final Set<KM> markingCodes;
}
