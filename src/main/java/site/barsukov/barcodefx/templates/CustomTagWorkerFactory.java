package site.barsukov.barcodefx.templates;

import com.itextpdf.html2pdf.attach.ITagWorker;
import com.itextpdf.html2pdf.attach.ProcessorContext;
import com.itextpdf.html2pdf.attach.impl.DefaultTagWorkerFactory;
import com.itextpdf.html2pdf.attach.impl.tags.ImgTagWorker;
import com.itextpdf.html2pdf.html.AttributeConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.styledxmlparser.jsoup.helper.StringUtil;
import com.itextpdf.styledxmlparser.node.IAttribute;
import com.itextpdf.styledxmlparser.node.IElementNode;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.log4j.Logger;
import site.barsukov.barcodefx.model.KM;
import site.barsukov.barcodefx.model.LabelInfo;
import site.barsukov.barcodefx.model.enums.DmEnconodation;

import java.util.List;
import java.util.Map;

import static com.itextpdf.html2pdf.html.TagConstants.IMG;

public class CustomTagWorkerFactory extends DefaultTagWorkerFactory {
    static final Logger logger = Logger.getLogger(CustomTagWorkerFactory.class);
    private List<KM> km;
    private LabelInfo labelInfo;
    private PdfDocument pdfDocument;
    private Map<String, ImgTagWorker> imgTagWorkerCash;

    public CustomTagWorkerFactory(List<KM> km, LabelInfo labelInfo, PdfDocument pdfDocument, Map<String, ImgTagWorker> imgTagWorkerCash) {
        this.km = km;
        this.labelInfo = labelInfo;
        this.pdfDocument = pdfDocument;
        this.imgTagWorkerCash = imgTagWorkerCash;
    }


    public ITagWorker getCustomTagWorker(IElementNode tag, ProcessorContext context) {
        int index = parseInteger(tag.getAttribute("km_number"), "Ошибка разбора атрибута km_number у тэга " + tag.name());
        int startString = parseInteger(tag.getAttribute("string_start"), "Ошибка разбора атрибута string_start у тэга " + tag.name());
        int endString = parseInteger(tag.getAttribute("string_end"), "Ошибка разбора атрибута string_end у тэга " + tag.name());

        if (startString < 0) {
            startString = 0;
        }

        if (km == null || index >= km.size() || km.isEmpty() || index == -1) {
            return null;
        }

        if ("gtin".equalsIgnoreCase(tag.name())) {
            String gtin = km.get(index).getGtin();
            if (BooleanUtils.toBoolean(tag.getAttribute("delete_ai"))) {
                gtin = gtin.substring(2);
            }
            if (endString <= 0 || endString > gtin.length()) {
                endString = gtin.length();
            }
            return new StringTagWorker(gtin.substring(startString, endString));
        }

        if ("serial".equalsIgnoreCase(tag.name())) {
            String serial = km.get(index).getSerial();
            if (BooleanUtils.toBoolean(tag.getAttribute("delete_ai"))) {
                serial = serial.substring(2);
            }
            if (endString <= 0 || endString > serial.length()) {
                endString = serial.length();
            }
            return new StringTagWorker(serial.substring(startString, endString));
        }

        if ("page_number".equalsIgnoreCase(tag.name())) {
            if (labelInfo.isPrintPageNumber()) {
                return new StringTagWorker(Integer.toString(labelInfo.getPageNumber()));
            } else {
                return null;
            }
        }

        if ("total_page_number".equalsIgnoreCase(tag.name())) {
            if (labelInfo.isPrintPageNumber()) {
                return new StringTagWorker(Integer.toString(labelInfo.getTotalPageNumber()));
            } else {
                return null;
            }
        }

        if ("user_label_text".equalsIgnoreCase(tag.name())) {
            if (endString <= 0 || endString > labelInfo.getUserLabelText().length()) {
                endString = labelInfo.getUserLabelText().length();
            }
            return new StringTagWorker(labelInfo.getUserLabelText().substring(startString, endString));
        }

        if ("datamatrix".equalsIgnoreCase(tag.name())) {
            Float height = parseFloat(tag.getAttribute(AttributeConstants.HEIGHT), "Ошибка разбора атрибута height у тэга datamatrix");
            Float width = parseFloat(tag.getAttribute(AttributeConstants.WIDTH), "Ошибка разбора атрибута width у тэга datamatrix");

            boolean printFnc = !BooleanUtils.toBoolean(tag.getAttribute("delete_fnc"));
            labelInfo.setDatamtrixHeight(height);
            labelInfo.setDatamtrixWidth(width);
            labelInfo.setDatamtrixMarginRight(parseFloatNoException(tag.getAttribute("margin_right")));
            labelInfo.setDatamtrixMarginLeft(parseFloatNoException(tag.getAttribute("margin_left")));
            labelInfo.setDatamtrixMarginBottom(parseFloatNoException(tag.getAttribute("margin_bottom")));
            labelInfo.setDatamtrixMarginTop(parseFloatNoException(tag.getAttribute("margin_top")));
            labelInfo.setDmEnconodation(parseEnconodationNoException(tag.getAttribute("enconodation")));
            return new DatamatrixTagWorker(km.get(index), labelInfo, printFnc, pdfDocument);
        }

        if (labelInfo.getCatalogElement(km.get(index).getGtin()).hasValue(tag.name().toUpperCase())) {
            return new CatalogTagWorker(tag.name(), labelInfo.getCatalogElement(km.get(index).getGtin()).getProperty(tag.name().toUpperCase()),
                    pdfDocument, tag.getAttributes());
        }

        if (IMG.equalsIgnoreCase(tag.name())) {
            String tagId = getUpHash(tag);
            if (!imgTagWorkerCash.containsKey(tagId)) {
                imgTagWorkerCash.put(tagId, new ImgTagWorker(tag, context));
            }
            return imgTagWorkerCash.get(tagId);
        }
        return null;

    }

    private String getUpHash(IElementNode tag) {
        StringBuilder builder = new StringBuilder();
        builder.append(tag.name());
        for (IAttribute atr : tag.getAttributes()) {
            builder.append(atr.getKey());
            builder.append(atr.getValue());
        }
        return builder.toString();
    }

    private DmEnconodation parseEnconodationNoException(String value) {
        if (StringUtil.isBlank(value)) {
            return null;
        }
        try {
            return DmEnconodation.valueOf(value);
        } catch (Exception e) {
            logger.error("Ошибка парсинга параметра " + value, e);
            return null;
        }
    }

    private Float parseFloatNoException(String value) {
        if (StringUtil.isBlank(value)) {
            return 0F;
        }
        try {
            return Float.parseFloat(value);
        } catch (Exception e) {
            logger.error("Ошибка парсинга параметра " + value, e);
            return 0F;
        }
    }

    private Float parseFloat(String value, String errorMessage) {
        try {
            return Float.parseFloat(value);
        } catch (Exception e) {
            logger.error("Ошибка парсинга параметра " + value, e);
            throw new IllegalArgumentException(errorMessage);
        }

    }

    private Integer parseInteger(String value, String errorMessage) {
        if (value == null && km.size() == 1) {
            return 0;
        } else if (value == null) {
            return -1;
        }
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            logger.error("Ошибка парсинга параметра " + value, e);
            throw new IllegalArgumentException(errorMessage);
        }

    }


}
