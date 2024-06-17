package site.barsukov.barcodefx.model;

import java.util.Date;

public class ArchivedPickingOrder {
    private String fileName;
    private Date createDate;

    public ArchivedPickingOrder(String fileName, Date createDate) {
        this.fileName = fileName;
        this.createDate = createDate;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }
}
