package site.barsukov.barcodefx.utils;

import javafx.scene.control.DatePicker;
import org.apache.commons.lang3.StringUtils;
import site.barsukov.barcodefx.exception.AppException;
import site.barsukov.barcodefx.props.IProp;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.GregorianCalendar;

public class Utils {
    public static final String TEMPLATES_FOLDER_PATH = "templates";
    public static final String DEFAULT_CLASSIC_INTERFACE = "CLASSIC";
    public static final String DEFAULT_SCANNER_INTERFACE = "SCANNER";

    public static String selectAI(String source) {
        StringBuilder builder = new StringBuilder();
        builder.append('(');
        builder.append(source.substring(0, 2));
        builder.append(')');
        builder.append(source.substring(2));
        return builder.toString();
    }

    public static String getStringDate(DatePicker datePicker, DateTimeFormatter dateTimeFormatter) {
        if (datePicker.getValue() != null) {
            return datePicker.getValue().format(dateTimeFormatter);
        } else if (StringUtils.isNotBlank(datePicker.getEditor().getText())) {
            dateTimeFormatter.parse(datePicker.getEditor().getText());
            return datePicker.getEditor().getText();
        } else {
            return null;
        }
    }

    public static String deleteBlankString(String source) {
        return StringUtils.isBlank(source) ? null : source;
    }

    public static LocalDate getLocalDate(DatePicker datePicker, DateTimeFormatter dateTimeFormatter) {
        if (datePicker.getValue() != null) {
            return datePicker.getValue();
        } else {
            if (StringUtils.isBlank(datePicker.getEditor().getText())) {
                return null;
            }
            return LocalDate.parse(datePicker.getEditor().getText(), dateTimeFormatter);
        }
    }

    public static LocalDate getLocalDate(DatePicker datePicker) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        return getLocalDate(datePicker, dateTimeFormatter);
    }

    public static void checkArgument(boolean condition, String errorMessage) {
        if (!condition) {
            throw new AppException(errorMessage);
        }
    }

    public static XMLGregorianCalendar getXMLGregorianCalendarNow() {
        GregorianCalendar gregorianCalendar = new GregorianCalendar();
        DatatypeFactory datatypeFactory = null;
        try {
            datatypeFactory = DatatypeFactory.newInstance();
        } catch (DatatypeConfigurationException e) {
            throw new AppException(e);
        }
        return datatypeFactory.newXMLGregorianCalendar(gregorianCalendar);
    }

    public static XMLGregorianCalendar toXMLGregorianCalendar(LocalDate localDate) {
        try {
            return DatatypeFactory.newInstance().newXMLGregorianCalendar(localDate.toString());
        } catch (DatatypeConfigurationException e) {
            return null;
        }
    }

    public static Float getFloatValue(IProp prop, String value) {
        try {
            return Float.parseFloat(value);
        } catch (Exception e) {
            return Float.parseFloat(prop.getDefaultValue());
        }
    }
}
