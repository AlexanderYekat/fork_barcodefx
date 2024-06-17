package site.barsukov.barcodefx.services.dto;

import lombok.Builder;
import lombok.Data;
import site.barsukov.barcodefx.model.enums.GoodCategory;

@Data
@Builder
public class AggregateMultiCreateDto {
    private final String inn;
    private final boolean ip;
    private final String orgName;
    private final String newStatus;
    private final boolean statusChanged;
    private final String status;
    private final String fileName;
    private final String resultPath;
    private final GoodCategory goodCategory;
}
