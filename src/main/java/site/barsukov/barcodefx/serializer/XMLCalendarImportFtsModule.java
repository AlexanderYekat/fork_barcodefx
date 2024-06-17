package site.barsukov.barcodefx.serializer;

import com.fasterxml.jackson.core.util.VersionUtil;
import com.fasterxml.jackson.databind.module.SimpleModule;

import javax.xml.datatype.XMLGregorianCalendar;

public class XMLCalendarImportFtsModule extends SimpleModule {
    private static final String NAME = "CustomXMLCalendarImportFtsModule";
    private static final VersionUtil VERSION_UTIL = new VersionUtil() {
    };

    public XMLCalendarImportFtsModule() {
        super("CustomXMLCalendarImportFtsModule", VERSION_UTIL.version());
        this.addSerializer(XMLGregorianCalendar.class, new XMLCalendarImportFtsSerializer());
    }
}
