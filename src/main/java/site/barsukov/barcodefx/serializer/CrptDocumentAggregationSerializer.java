package site.barsukov.barcodefx.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import site.barsukov.barcodefx.model.crpt.aggregation.UnitPack;

import java.io.IOException;
import java.time.temporal.ChronoUnit;

import static java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME;

public class CrptDocumentAggregationSerializer extends StdSerializer<UnitPack.Document> {

    public CrptDocumentAggregationSerializer() {
        this(null);
    }

    public CrptDocumentAggregationSerializer(Class<UnitPack.Document> t) {
        super(t);
    }
    @Override
    public void serialize(UnitPack.Document value, JsonGenerator gen, SerializerProvider provider) throws IOException {

        ToXmlGenerator xmlGenerator = (ToXmlGenerator) gen;
        xmlGenerator.writeStartObject();
        xmlGenerator.setNextIsAttribute(true);
        gen.writeFieldName("document_number");
        gen.writeString(value.getDocumentNumber());

        xmlGenerator.setNextIsAttribute(true);
        gen.writeFieldName("operation_date_time");
        gen.writeString(ISO_OFFSET_DATE_TIME.format(value.getOperationDateTime().toGregorianCalendar().toZonedDateTime().truncatedTo(ChronoUnit.SECONDS)));

        xmlGenerator.writeObjectField("organisation", value.getOrganisation());
        for (UnitPack.Document.PackContent curKm : value.getPackContent()) {
            xmlGenerator.writeObjectField("pack_content", curKm);
        }
        gen.writeEndObject();
    }
}
