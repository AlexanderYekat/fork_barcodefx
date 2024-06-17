package site.barsukov.barcodefx.model.printing;

import lombok.Data;

@Data
public class PdfPrintingProfile {
    private String name;
    private boolean vertical;
    private boolean horizontal;
    private boolean termoPrinter;
    private String height;
    private String templateFileName;
    private String width;
    private String userLabelText;
    private Boolean printMarkNumber;
    private int markNumberStart;
    private boolean printInPacks;
    private int packSize;
}
