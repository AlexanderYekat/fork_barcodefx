package site.barsukov.barcodefx.model;

import lombok.Data;
import site.barsukov.barcodefx.model.enums.DmEnconodation;

import java.util.Map;

@Data
public class LabelInfo {
    private Float datamtrixHeight;
    private Float datamtrixWidth;
    private Float datamtrixMarginLeft;
    private Float datamtrixMarginRight;
    private Float datamtrixMarginTop;
    private Float datamtrixMarginBottom;
    private Float eanHeight;
    private Float eanWidth;
    private Float eanMarginLeft;
    private Float eanMarginRight;
    private Float eanMarginTop;
    private Float eanMarginBottom;
    private Double eanRotation;
    private int pageNumber;
    private int totalPageNumber;
    private boolean printPageNumber;
    private String userLabelText;
    private DmEnconodation dmEnconodation;
    private Map<String, String> printingProps;
    private Map<String, CatalogElement> catalogElement;

    public Float getDatamtrixHeight() {
        return datamtrixHeight;
    }

    public void setDatamtrixHeight(Float datamtrixHeight) {
        this.datamtrixHeight = datamtrixHeight;
    }

    public Float getDatamtrixWidth() {
        return datamtrixWidth;
    }

    public void setDatamtrixWidth(Float datamtrixWidth) {
        this.datamtrixWidth = datamtrixWidth;
    }

    public CatalogElement getCatalogElement(String gtin) {
        return catalogElement.getOrDefault(gtin, new CatalogElement(gtin));
    }

    public void setCatalogElement(Map<String, CatalogElement> catalogElement) {
        this.catalogElement = catalogElement;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public boolean isPrintPageNumber() {
        return printPageNumber;
    }

    public void setPrintPageNumber(boolean printPageNumber) {
        this.printPageNumber = printPageNumber;
    }

    public String getUserLabelText() {
        return userLabelText;
    }

    public void setUserLabelText(String userLabelText) {
        this.userLabelText = userLabelText;
    }

    public Map<String, String> getPrintingProps() {
        return printingProps;
    }

    public void setPrintingProps(Map<String, String> printingProps) {
        this.printingProps = printingProps;
    }

    public Float getDatamtrixMarginLeft() {
        return datamtrixMarginLeft;
    }

    public void setDatamtrixMarginLeft(Float datamtrixMarginLeft) {
        this.datamtrixMarginLeft = datamtrixMarginLeft;
    }

    public Float getDatamtrixMarginRight() {
        return datamtrixMarginRight;
    }

    public void setDatamtrixMarginRight(Float datamtrixMarginRight) {
        this.datamtrixMarginRight = datamtrixMarginRight;
    }

    public Float getDatamtrixMarginTop() {
        return datamtrixMarginTop;
    }

    public void setDatamtrixMarginTop(Float datamtrixMarginTop) {
        this.datamtrixMarginTop = datamtrixMarginTop;
    }

    public void setDmEnconodation(DmEnconodation dmEnconodation) {
        this.dmEnconodation = dmEnconodation;
    }

    public Float getDatamtrixMarginBottom() {
        return datamtrixMarginBottom;
    }

    public void setDatamtrixMarginBottom(Float datamtrixMarginBottom) {
        this.datamtrixMarginBottom = datamtrixMarginBottom;
    }

    public int getTotalPageNumber() {
        return totalPageNumber;
    }

    public void setTotalPageNumber(int totalPageNumber) {
        this.totalPageNumber = totalPageNumber;
    }
}
