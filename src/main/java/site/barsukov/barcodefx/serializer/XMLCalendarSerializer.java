package site.barsukov.barcodefx.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import javax.xml.datatype.XMLGregorianCalendar;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class XMLCalendarSerializer extends StdSerializer<XMLGregorianCalendar> {
    public XMLCalendarSerializer() {
        this((Class) null);
    }

    public XMLCalendarSerializer(Class<XMLGregorianCalendar> t) {
        super(t);
    }

    public void serialize(XMLGregorianCalendar value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        Date date = value.toGregorianCalendar().getTime();
        dateFormat.setTimeZone(TimeZone.getTimeZone(value.getTimeZone(value.getTimezone()).getDisplayName()));
        jgen.writeString(dateFormat.format(date));

    }
}
