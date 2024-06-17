package site.barsukov.barcodefx.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class WithdrawalProduct {
    private String ki;
    private String documentNumber;
    private String primaryDocumentCustomName;
    private String primaryDocumentType;
    private BigDecimal cost;
    private LocalDate primaryDocumentDate;

    public String getKi() {
        return ki;
    }

    public void setKi(String ki) {
        this.ki = ki;
    }

    public BigDecimal getCost() {
        return cost;
    }

    public void setCost(BigDecimal cost) {
        this.cost = cost;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }

    public String getPrimaryDocumentCustomName() {
        return primaryDocumentCustomName;
    }

    public void setPrimaryDocumentCustomName(String primaryDocumentCustomName) {
        this.primaryDocumentCustomName = primaryDocumentCustomName;
    }

    public String getPrimaryDocumentType() {
        return primaryDocumentType;
    }

    public void setPrimaryDocumentType(String primaryDocumentType) {
        this.primaryDocumentType = primaryDocumentType;
    }

    public LocalDate getPrimaryDocumentDate() {
        return primaryDocumentDate;
    }

    public void setPrimaryDocumentDate(LocalDate primaryDocumentDate) {
        this.primaryDocumentDate = primaryDocumentDate;
    }
}
