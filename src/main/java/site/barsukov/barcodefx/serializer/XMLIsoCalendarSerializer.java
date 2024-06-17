package site.barsukov.barcodefx.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import javax.xml.datatype.XMLGregorianCalendar;
import java.io.IOException;
import java.time.temporal.ChronoUnit;

import static java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME;

public class XMLIsoCalendarSerializer extends StdSerializer<XMLGregorianCalendar> {
    public XMLIsoCalendarSerializer() {
        this((Class) null);
    }

    public XMLIsoCalendarSerializer(Class<XMLGregorianCalendar> t) {
        super(t);
    }

    public void serialize(XMLGregorianCalendar value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
        jgen.writeString(ISO_OFFSET_DATE_TIME.format(value.toGregorianCalendar().toZonedDateTime().truncatedTo(ChronoUnit.SECONDS)));
    }
}
