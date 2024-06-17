package site.barsukov.barcodefx.model;

public class BufferDTO {
    private Integer availableCodes;
    private Integer unavailableCodes;
    private Integer totalCodes;
    private String orderId;
    private String bufferStatus;
    private String gtin;

    public Integer getAvailableCodes() {
        return availableCodes;
    }

    public void setAvailableCodes(Integer availableCodes) {
        this.availableCodes = availableCodes;
    }

    public Integer getUnavailableCodes() {
        return unavailableCodes;
    }

    public void setUnavailableCodes(Integer unavailableCodes) {
        this.unavailableCodes = unavailableCodes;
    }

    public Integer getTotalCodes() {
        return totalCodes;
    }

    public void setTotalCodes(Integer totalCodes) {
        this.totalCodes = totalCodes;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getBufferStatus() {
        return bufferStatus;
    }

    public void setBufferStatus(String bufferStatus) {
        this.bufferStatus = bufferStatus;
    }

    public String getGtin() {
        return gtin;
    }

    public void setGtin(String gtin) {
        this.gtin = gtin;
    }
}
