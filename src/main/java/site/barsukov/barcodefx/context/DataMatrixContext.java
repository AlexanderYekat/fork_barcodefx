package site.barsukov.barcodefx.context;

import lombok.Data;

@Data
public class DataMatrixContext extends BaseContext {
    private boolean vertical;
    private boolean horizontal;
    private boolean termoPrinter;
    private String height;
    private String templateName;
    private String width;
    private String userLabelText;
    private Boolean printMarkNumber;
    private int markNumberStart;
    private int totalPageNumber;

    public float getHeight() {
        try {
            return Float.parseFloat(height.trim());
        } catch (Exception e) {
            throw new IllegalArgumentException("Ошибка определения высоты этикетки.");
        }
    }

    public float getWidth() {
        try {
            return Float.parseFloat(width.trim());
        } catch (Exception e) {
            throw new IllegalArgumentException("Ошибка определения ширины этикетки.");
        }
    }

}
