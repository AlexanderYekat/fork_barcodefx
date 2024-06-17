package site.barsukov.barcodefx.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import javax.xml.datatype.XMLGregorianCalendar;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class XMLCalendarImportFtsSerializer extends StdSerializer<XMLGregorianCalendar> {
    public XMLCalendarImportFtsSerializer() {
        this((Class) null);
    }

    public XMLCalendarImportFtsSerializer(Class<XMLGregorianCalendar> t) {
        super(t);
    }

    public void serialize(XMLGregorianCalendar value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date = value.toGregorianCalendar().getTime();
        jgen.writeString(dateFormat.format(date));

    }
}
