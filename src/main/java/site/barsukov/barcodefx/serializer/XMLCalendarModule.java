package site.barsukov.barcodefx.serializer;

import com.fasterxml.jackson.core.util.VersionUtil;
import com.fasterxml.jackson.databind.module.SimpleModule;

import javax.xml.datatype.XMLGregorianCalendar;

public class XMLCalendarModule extends SimpleModule {
    private static final String NAME = "CustomXMLCalendarModule";
    private static final VersionUtil VERSION_UTIL = new VersionUtil() {
    };

    public XMLCalendarModule() {
        super("CustomXMLCalendarModule", VERSION_UTIL.version());
        this.addSerializer(XMLGregorianCalendar.class, new XMLCalendarSerializer());
    }
}
