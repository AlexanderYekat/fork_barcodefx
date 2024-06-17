package site.barsukov.barcodefx.templates;

import com.itextpdf.html2pdf.attach.ITagWorker;
import com.itextpdf.html2pdf.attach.ProcessorContext;
import com.itextpdf.html2pdf.html.AttributeConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.layout.IPropertyContainer;
import com.itextpdf.layout.element.Text;
import com.itextpdf.styledxmlparser.jsoup.helper.StringUtil;
import com.itextpdf.styledxmlparser.node.IAttributes;
import com.itextpdf.styledxmlparser.node.IElementNode;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.DateUtil;
import site.barsukov.barcodefx.CreateMarkTableUtil;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CatalogTagWorker implements ITagWorker {
    static final Logger logger = Logger.getLogger(CatalogTagWorker.class);

    private String tagName;
    private String property;
    private IAttributes attributes;

    private PdfDocument pdfDocument;

    public CatalogTagWorker(String tagName, String property, PdfDocument pdfDocument, IAttributes attributes) {
        this.tagName = tagName;
        this.property = property;
        this.attributes = attributes;
        this.pdfDocument = pdfDocument;
    }

    @Override
    public void processEnd(IElementNode element, ProcessorContext context) {

    }

    @Override
    public boolean processContent(String content, ProcessorContext context) {
        return false;
    }

    @Override
    public boolean processTagChild(ITagWorker childTagWorker, ProcessorContext context) {
        return false;
    }

    @Override
    public IPropertyContainer getElementResult() {
        String result;
        String type = attributes.getAttribute(AttributeConstants.TYPE);
        if (StringUtils.isBlank(type) || "STRING".equalsIgnoreCase(type)) {
            result = property;
        } else if ("DATE".equalsIgnoreCase(type)) {
            try {
                Date date = DateUtil.getJavaDate(Double.parseDouble(property));
                String pattern = attributes.getAttribute("date_format");
                if (StringUtils.isBlank(pattern)) {
                    pattern = "dd.MM.yyyy";
                }
                DateFormat df = new SimpleDateFormat(pattern);
                result = df.format(date);
            } catch (Exception e) {
                logger.error("Ошибка обработки тега даты ", e);
                throw new IllegalArgumentException(String.format("Ошибка обработки тега %s с типом %s", tagName, type));
            }
        } else if ("DICT".equalsIgnoreCase(type)) {
            boolean useKey = BooleanUtils.toBoolean(attributes.getAttribute("use_key"));
            if (useKey) {
                result = getDictKey(property);
            } else {
                result = getDictValue(property);
            }

        } else if ("EAN-13".equalsIgnoreCase(type)) {
            var eanRotation = (double) parseFloatNoException(attributes.getAttribute("rotation"));
            var image = CreateMarkTableUtil.createEANImage(property, pdfDocument, eanRotation);
            image.setHeight(parseFloatNoException(attributes.getAttribute("height")));
            image.setWidth(parseFloatNoException(attributes.getAttribute("width")));
            image.setMarginRight(parseFloatNoException(attributes.getAttribute("margin_right")));
            image.setMarginLeft(parseFloatNoException(attributes.getAttribute("margin_left")));
            image.setMarginBottom(parseFloatNoException(attributes.getAttribute("margin_bottom")));
            image.setMarginTop(parseFloatNoException(attributes.getAttribute("margin_top")));
            image.setWidth(parseFloatNoException(attributes.getAttribute("width")));
            return image;
        }
        else if ("BARCODE-128".equalsIgnoreCase(type)) {
            var eanRotation = (double) parseFloatNoException(attributes.getAttribute("rotation"));
            var image = CreateMarkTableUtil.createBarcode128Image(property, pdfDocument, eanRotation);
            image.setHeight(parseFloatNoException(attributes.getAttribute("height")));
            image.setWidth(parseFloatNoException(attributes.getAttribute("width")));
            image.setMarginRight(parseFloatNoException(attributes.getAttribute("margin_right")));
            image.setMarginLeft(parseFloatNoException(attributes.getAttribute("margin_left")));
            image.setMarginBottom(parseFloatNoException(attributes.getAttribute("margin_bottom")));
            image.setMarginTop(parseFloatNoException(attributes.getAttribute("margin_top")));
            image.setWidth(parseFloatNoException(attributes.getAttribute("width")));
            return image;
        }
        else {
            throw new IllegalArgumentException(String.format("Неизвестный тип %s у тега %s", type, tagName));
        }

        return new Text(result);
    }

    private String getDictKey(String property) {
        try {
            int start = property.indexOf('<');
            int end = property.indexOf('>');
            return property.substring(start + 1, end);
        } catch (Exception e) {
            throw new IllegalArgumentException(String.format("Ошибка обработки ключа справочника в теге %s со значением %s", tagName, property));
        }

    }

    private String getDictValue(String property) {
        try {
            int start = property.indexOf('>');
            return property.substring(start + 1);
        } catch (Exception e) {
            throw new IllegalArgumentException(String.format("Ошибка обработки значения справочника в теге %s со значением %s", tagName, property));
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
}
