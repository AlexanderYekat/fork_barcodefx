package site.barsukov.barcodefx.model.enums;

import com.itextpdf.barcodes.BarcodeDataMatrix;

public enum DmEnconodation {
    DM_AUTO(BarcodeDataMatrix.DM_AUTO),
    DM_ASCII(BarcodeDataMatrix.DM_ASCII),
    DM_TEXT(BarcodeDataMatrix.DM_TEXT),
    DM_C40(BarcodeDataMatrix.DM_C40),
    DM_B256(BarcodeDataMatrix.DM_B256),
    DM_X12(BarcodeDataMatrix.DM_X12);

    private int option;

    DmEnconodation(int option) {
        this.option = option;
    }

    public int getOption() {
        return option;
    }
}
