package site.barsukov.barcodefx.context;

import lombok.Data;

import java.time.LocalDate;

@Data
public class VvodVOborotImportFtsContext extends BaseContext {
    private String userINN;
    private String declarationNum;
    private LocalDate declarationDate;
}
