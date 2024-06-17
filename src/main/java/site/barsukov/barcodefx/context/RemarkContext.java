package site.barsukov.barcodefx.context;

import lombok.Data;
import site.barsukov.barcodefx.model.enums.RemarkCause;

import java.time.LocalDate;

@Data
public class RemarkContext extends BaseContext {
    private String participantINN;
    private String remarkTnvd;
    private String remarkColor;
    private String remarkSize;
    private RemarkCause remarkCause;
    private LocalDate remarkDate;

}
