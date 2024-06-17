package site.barsukov.barcodefx;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.xml.sax.SAXException;
import site.barsukov.barcodefx.exception.AppException;
import site.barsukov.barcodefx.model.KM;
import site.barsukov.barcodefx.model.enums.GoodCategory;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Validator {
    public final static String XSD_PATH = "xsd/all.xsd";
    final static Logger logger = Logger.getLogger(Validator.class);

    public static void validateINN(String inn) {
        if (!isInnValid(inn)) {
            throw new AppException("Ошибка в ИНН пользователя");
        }
    }

    public static boolean isInnValid(String inn) {
        String regex = "(([0-9]{9})|([0-9]{1}[1-9]{1}|[1-9]{1}[0-9]{1})[0-9]{8})|(([0-9]{1}[1-9]{1}|[1-9]{1}[0-9]{1})[0-9]{10})";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(inn);
        if (!matcher.matches()) {
            return false;
        }
        return true;
    }

    public static void validateDtNum(String dtNum) {
        String regex = "[0-9]{8}\\/[0-9]{6}\\/[0-9]{7}";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(dtNum);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Ошибка в номере ДТ");
        }
    }

    public static boolean isAtpKMValid(KM curKM) {
        if (curKM.getSuzString().length() != 29) {
            logger.warn("Неверная длина кода АТП " + curKM.getSuzString());
        }
        return curKM.getSuzString().length() == 29;
    }

    public static boolean isKMTotalLengthValid(KM curKM, GoodCategory category) {
        if (!curKM.isSscc()) {
            logger.warn("Ошибка полной длины кода маркировки:" + curKM.getSuzString());
            return curKM.getSuzString().length() == category.getTotalLength();
        } else {
            return true;
        }
    }

    public static boolean isKMValid(KM curKM, GoodCategory category) {
        boolean result = true;
        if (StringUtils.isBlank(curKM.getGtin()) && !curKM.isSscc()) {
            logger.warn("Не удалось выделить gtin у КМ:" + curKM.getSuzString());
            result = false;
        }

        if (StringUtils.isBlank(curKM.getSerial()) && !curKM.isSscc()) {
            logger.warn("Не удалось выделить серийный номер у КМ:" + curKM.getSuzString());
            result = false;
        }
        if (curKM.getGtin() != null && !curKM.getGtin().startsWith("01")) {
            logger.warn("У gtin AI отличный от 01 в КМ:" + curKM.getSuzString());
            result = false;
        }

        if (curKM.getSerial() != null && !curKM.getSerial().startsWith("21")) {
            logger.warn("У серийника AI отличный от 21 в КМ:" + curKM.getSuzString());
            result = false;
        }

        if (curKM.getSerial() != null && !curKM.isSscc() && curKM.getSerial().length() != 2 + category.getSerialLength()) {
            logger.warn(String.format("Размер серийника не равен %d символов для категории %s в КМ: %s", category.getSerialLength(),
                    category.getShortName(),
                    curKM.getSuzString()));
            result = false;
        }

        if (curKM.getGtin() != null) {
            Pattern gtinPattern = Pattern.compile("\\d+");
            Matcher gtinMatcher = gtinPattern.matcher(curKM.getGtin());
            if (!curKM.isSscc() && !gtinMatcher.matches()) {
                logger.warn("Недопустимые символы в gtin у КМ:" + curKM.getSuzString());
                result = false;
            }
        }
        if (curKM.getSerial() != null) {
            for (int i = 0; i < curKM.getSerial().length(); i++) {
                if (!curKM.isSscc() && Character.UnicodeBlock.of(curKM.getSerial().charAt(i)).equals(Character.UnicodeBlock.CYRILLIC)) {
                    logger.warn("Кириллические символы в серийнике у КМ:" + curKM.getSuzString());
                    result = false;
                }
            }
        }

        return result;
    }

    public static boolean isKMAIValid(String km, GoodCategory goodCategory) {
        boolean result = true;
        String[] splitStrings = km.split("\\u001D");
        if (splitStrings.length < 3 && goodCategory.getTailLength() > 0) {
            logger.warn("Менее двух GS разделителей в КМ: " + km);
            result = false;
        }
        if (splitStrings.length < 1 && goodCategory.getTailLength() == 0) {
            logger.warn("Менее одного GS разделителей в КМ: " + km);
            result = false;
        }
        if (!splitStrings[1].startsWith(goodCategory.getCheckKeyAI())) {
            logger.warn("Недопустимый идентификатор применения у ключа проверки в КМ:"
                    + km);
            result = false;
        }
        if (goodCategory.getTailLength() != 0 && splitStrings.length < 3) {
            logger.warn("Отсутствует крипто-часть в КМ:"
                    + km);
            result = false;
        } else if (goodCategory.getCryptoAI() != null && !splitStrings[2].startsWith(goodCategory.getCryptoAI())) {
            logger.warn("Недопустимый идентификатор применения у кода проверки в КМ:"
                    + km);
            result = false;
        }

        return result;
    }

    public static boolean isXmlValid(File xmlFile) {
        try {
            SchemaFactory factory =
                    SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = factory.newSchema(new File(XSD_PATH));
            javax.xml.validation.Validator validator = schema.newValidator();
            validator.validate(new StreamSource(xmlFile));
        } catch (IOException | SAXException e) {
            return false;
        }
        return true;
    }

    public static String validateXml(File xmlFile) {
        try {
            SchemaFactory factory =
                    SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = factory.newSchema(new File(XSD_PATH));
            javax.xml.validation.Validator validator = schema.newValidator();
            validator.validate(new StreamSource(xmlFile));
        } catch (IOException | SAXException e) {
            return e.getMessage();
        }
        return "Файл соответствует XSD схеме";
    }


}
