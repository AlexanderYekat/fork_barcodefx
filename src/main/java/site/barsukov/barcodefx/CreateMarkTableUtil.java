package site.barsukov.barcodefx;

import com.itextpdf.barcodes.Barcode128;
import com.itextpdf.barcodes.BarcodeDataMatrix;
import com.itextpdf.barcodes.BarcodeEAN;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.xobject.PdfFormXObject;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.property.HorizontalAlignment;
import com.itextpdf.layout.property.VerticalAlignment;
import org.apache.commons.lang3.StringUtils;
import site.barsukov.barcodefx.context.DataMatrixContext;
import site.barsukov.barcodefx.model.KM;
import site.barsukov.barcodefx.model.enums.DmEnconodation;
import site.barsukov.barcodefx.props.IProp;
import site.barsukov.barcodefx.services.PropService;
import site.barsukov.barcodefx.utils.Utils;

import java.io.IOException;
import java.util.Map;

import static com.itextpdf.barcodes.Barcode128.FNC1;
import static com.itextpdf.barcodes.BarcodeDataMatrix.*;
import static site.barsukov.barcodefx.props.JsonPrintProps.PrintProp.*;

public class CreateMarkTableUtil {
    public static Table createMarkTableVertical(KM km, PdfDocument document, float height, float width, DataMatrixContext context, Integer number) throws IOException {
        PropService propservice = context.getPropService();
        Table result = new Table(1);

        PdfFont f1 = PdfFontFactory.createFont("TimesNewRoman.ttf",
                "CP1251", true);
        result.addCell(createDMImage(km, document, context.getPropService()));
        if (km.isSscc()) {
            return result;
        }
        result.startNewRow();

        Cell cell = new Cell();

        Paragraph phrase = new Paragraph();
        if (!StringUtils.isBlank(context.getUserLabelText())) {
            phrase.add(new Text(context.getUserLabelText() + "\n")
                    .setFont(f1)
                    .setFontSize(propservice.getFloatProp(USER_LABEL_FONT_SIZE))
                    .setBold());
        }


        phrase.add(new Text(Utils.selectAI(km.getGtin()) + "\n" + Utils.selectAI(km.getSerial()) + "\n")
                .setFont(f1)
                .setFontSize(propservice.getFloatProp(LABEL_FONT_SIZE)));


        if (context.getPrintMarkNumber()) {
            phrase.add(propservice.getProp(LABEL_COUNTER_PREFIX) + number)
                    .setFont(f1)
                    .setFontSize(propservice.getFloatProp(LABEL_FONT_SIZE));

        }
        phrase.setHorizontalAlignment(HorizontalAlignment.CENTER);
        phrase.setVerticalAlignment(VerticalAlignment.MIDDLE);
        phrase.setMarginLeft(propservice.getFloatProp(LABEL_A4_TEXT_MARGIN_LEFT));

        cell.setVerticalAlignment(VerticalAlignment.MIDDLE);
        cell.setHorizontalAlignment(HorizontalAlignment.LEFT);
        cell.add(phrase);
        cell.setWidth(width);
        float imageCellHeight = propservice.getFloatProp(DATAMATRIX_IMAGE_HEIGHT) + propservice.getFloatProp(DATAMATRIX_IMAGE_MARGIN_TOP) + propservice.getFloatProp(DATAMATRIX_IMAGE_MARGIN_BOTTOM);
        cell.setHeight(height - imageCellHeight);
        result.addCell(cell);
        return result;

    }


    public static Table createMarkTableHorizontal(KM km, PdfDocument document, float height, float width, DataMatrixContext context, Integer number) throws IOException {
        PropService propService = context.getPropService();
        Float labelFontSize = propService.getFloatProp(LABEL_FONT_SIZE);

        Table result = new Table(2);

        PdfFont f1 = PdfFontFactory.createFont("TimesNewRoman.ttf",
                "CP1251", true);

        Cell cell = new Cell();
        Paragraph phrase = new Paragraph();
        if (!StringUtils.isBlank(context.getUserLabelText())) {
            phrase.add(new Text(context.getUserLabelText() + "\n")
                    .setFont(f1)
                    .setFontSize(propService.getFloatProp(USER_LABEL_FONT_SIZE))
                    .setBold());
        }
        if (km.isSscc()) {
            phrase.add(new Text(Utils.selectAI(km.getSuzString()))
                    .setFont(f1).setFontSize(labelFontSize));
        } else {
            phrase.add(new Text(Utils.selectAI(km.getGtin()) + "\n" + Utils.selectAI(km.getSerial()) + "\n")
                    .setFont(f1).setFontSize(labelFontSize));
        }

        if (context.getPrintMarkNumber()) {
            phrase.add(propService.getProp(LABEL_COUNTER_PREFIX) + number)
                    .setFont(f1).setFontSize(labelFontSize);

        }
        phrase.setHorizontalAlignment(HorizontalAlignment.LEFT);
        phrase.setVerticalAlignment(VerticalAlignment.MIDDLE);
        phrase.setMarginLeft(propService.getFloatProp(LABEL_A4_TEXT_MARGIN_LEFT));

        cell.add(phrase);
        float imageCellWidth = propService.getFloatProp(DATAMATRIX_IMAGE_WIDTH) + propService.getFloatProp(DATAMATRIX_IMAGE_MARGIN_LEFT) + propService.getFloatProp(DATAMATRIX_IMAGE_MARGIN_RIGHT);
        cell.setWidth(width - imageCellWidth);
        cell.setHeight(height);
        cell.setVerticalAlignment(VerticalAlignment.MIDDLE);

        if (!km.isSscc()) {
            result.addCell(cell);
        }

        Cell imageCell = new Cell();
        imageCell.add(createDMImage(km, document, propService));
        imageCell.setVerticalAlignment(VerticalAlignment.MIDDLE);

        result.addCell(imageCell);

        return result;

    }

    public static Image createEANImage(String value, PdfDocument document, Double rotation) {
        Image image = new Image(createEAN_13BarcodeObject(value, document));
        image.setRotationAngle(rotation);

        return image;
    }

    public static Image createBarcode128Image(String value, PdfDocument document, Double rotation) {
        Image image = new Image(createCode_128BarcodeObject(value, document));
        image.setRotationAngle(rotation);
        return image;
    }

    private static PdfFormXObject createEAN_13BarcodeObject(String dataString, PdfDocument document) {
        BarcodeEAN barcodeEAN = new BarcodeEAN(document);
        barcodeEAN.setAltText(dataString);
        barcodeEAN.setCode(dataString);
        return barcodeEAN.createFormXObject(null, null, document);
    }

    private static PdfFormXObject createCode_128BarcodeObject(String dataString, PdfDocument document) {
        Barcode128 barcode128 = new Barcode128(document);
        barcode128.setAltText(dataString);
        barcode128.setCode(dataString);
        return barcode128.createFormXObject(null, null, document);
    }

    public static Image createDMImage(KM km, PdfDocument document, PropService propService) {
        return createDMImage(km, document, propService, true);
    }

    public static Image createDMImage(KM km, PdfDocument document, PropService propService, boolean printFnc) {
        Image image;
        if (km.isSscc()) {
            image = new Image(createEANBarcodeObject(km.getSuzString(), document));
            image.setHeight(propService.getFloatProp(EAN_IMAGE_HEIGHT));
            image.setWidth(propService.getFloatProp(EAN_IMAGE_WIDTH));
            image.setHorizontalAlignment(HorizontalAlignment.CENTER);
            image.setMargins(
                    propService.getFloatProp(EAN_IMAGE_MARGIN_TOP),
                    propService.getFloatProp(EAN_IMAGE_MARGIN_RIGHT),
                    propService.getFloatProp(EAN_IMAGE_MARGIN_BOTTOM),
                    propService.getFloatProp(EAN_IMAGE_MARGIN_LEFT)
            );
        } else {
            image = new Image(createDMBarcodeObject(km.getSuzString(), document, printFnc, null));
            image.setHeight(propService.getFloatProp(DATAMATRIX_IMAGE_HEIGHT));
            image.setWidth(propService.getFloatProp(DATAMATRIX_IMAGE_WIDTH));
            image.setHorizontalAlignment(HorizontalAlignment.CENTER);
            image.setMargins(
                    propService.getFloatProp(DATAMATRIX_IMAGE_MARGIN_TOP),
                    propService.getFloatProp(DATAMATRIX_IMAGE_MARGIN_RIGHT),
                    propService.getFloatProp(DATAMATRIX_IMAGE_MARGIN_BOTTOM),
                    propService.getFloatProp(DATAMATRIX_IMAGE_MARGIN_LEFT)
            );
        }
        return image;
    }

    public static Image createDMImage(KM km, PdfDocument document, Map<String, String> props, boolean printFnc) {
        return createDMImage(km, document, props, printFnc, null);
    }

    public static Image createDMImage(KM km, PdfDocument document, Map<String, String> props, boolean printFnc, DmEnconodation dmEnconodation) {
        Image image;
        if (km.isSscc()) {
            image = new Image(createEANBarcodeObject(km.getSuzString(), document));
            image.setHeight(getPropFromMap(props, EAN_IMAGE_HEIGHT));
            image.setWidth(getPropFromMap(props, EAN_IMAGE_WIDTH));
            image.setHorizontalAlignment(HorizontalAlignment.CENTER);
            image.setMargins(
                    getPropFromMap(props, EAN_IMAGE_MARGIN_TOP),
                    getPropFromMap(props, EAN_IMAGE_MARGIN_RIGHT),
                    getPropFromMap(props, EAN_IMAGE_MARGIN_BOTTOM),
                    getPropFromMap(props, EAN_IMAGE_MARGIN_LEFT)
            );
        } else {
            image = new Image(createDMBarcodeObject(km.getSuzString(), document, printFnc, dmEnconodation));
            image.setHeight(getPropFromMap(props, DATAMATRIX_IMAGE_HEIGHT));
            image.setWidth(getPropFromMap(props, DATAMATRIX_IMAGE_WIDTH));
            image.setHorizontalAlignment(HorizontalAlignment.CENTER);
            image.setMargins(
                    getPropFromMap(props, DATAMATRIX_IMAGE_MARGIN_TOP),
                    getPropFromMap(props, DATAMATRIX_IMAGE_MARGIN_RIGHT),
                    getPropFromMap(props, DATAMATRIX_IMAGE_MARGIN_BOTTOM),
                    getPropFromMap(props, DATAMATRIX_IMAGE_MARGIN_LEFT)
            );
        }
        return image;
    }

    private static float getPropFromMap(Map<String, String> props, IProp prop) {
        return Float.parseFloat(props.get(prop.name()));
    }


    private static PdfFormXObject createDMBarcodeObject(String dataString, PdfDocument document, boolean printFnc, DmEnconodation dmEnconodation) {
        BarcodeDataMatrix dataMatrix = new BarcodeDataMatrix();
        int textOption = dmEnconodation == null ? DM_TEXT : dmEnconodation.getOption();
        if (printFnc) {
            dataMatrix.setOptions(DM_EXTENSION + textOption);
            dataMatrix.setCode("f." + dataString);
        } else {
            dataMatrix.setOptions(textOption);
            dataMatrix.setCode(dataString);
        }

        if (dmEnconodation == null) {
            dataMatrix.setOptions(DM_AUTO);
        } else {
            dataMatrix.setOptions(dmEnconodation.getOption());
        }
        return dataMatrix.createFormXObject(null, 1, document);
    }

    private static PdfFormXObject createEANBarcodeObject(String dataString, PdfDocument document) {
        Barcode128 dataMatrix = new Barcode128(document);
        dataMatrix.setAltText(Utils.selectAI(dataString));
        dataMatrix.setCode(FNC1 + dataString);
        return dataMatrix.createFormXObject(null, null, document);
    }
}
