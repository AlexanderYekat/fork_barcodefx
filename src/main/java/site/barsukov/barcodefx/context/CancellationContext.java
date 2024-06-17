package site.barsukov.barcodefx.context;

import lombok.Data;

@Data
public class CancellationContext extends BaseContext {
    private String participantINN;
    private String reason;
    private String cancellationDate;
    private String cancellationDocNum;

}
