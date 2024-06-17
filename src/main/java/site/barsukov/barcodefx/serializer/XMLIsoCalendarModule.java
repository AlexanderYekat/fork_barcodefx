package site.barsukov.barcodefx.serializer;

import com.fasterxml.jackson.core.util.VersionUtil;
import com.fasterxml.jackson.databind.module.SimpleModule;

import javax.xml.datatype.XMLGregorianCalendar;

public class XMLIsoCalendarModule extends SimpleModule {
    private static final String NAME = "CustomXMLIsoCalendarModule";
    private static final VersionUtil VERSION_UTIL = new VersionUtil() {
    };

    public XMLIsoCalendarModule() {
        super("CustomXMLCalendarModule", VERSION_UTIL.version());
        this.addSerializer(XMLGregorianCalendar.class, new XMLIsoCalendarSerializer());
    }
}
