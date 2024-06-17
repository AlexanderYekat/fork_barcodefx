package site.barsukov.barcodefx.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import site.barsukov.barcodefx.model.crpt.aggregation.UnitPack;

import javax.xml.bind.JAXBElement;
import java.io.IOException;

public class CrptKMAggregationSerializer extends StdSerializer<UnitPack.Document.PackContent> {

    public CrptKMAggregationSerializer() {
        this(null);
    }

    public CrptKMAggregationSerializer(Class<UnitPack.Document.PackContent> t) {
        super(t);
    }

    @Override
    public void serialize(UnitPack.Document.PackContent value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject();
        gen.writeObjectField("pack_code", value.getPackCode());
        for (JAXBElement<String> curKm : value.getCisOrSscc()) {
            gen.writeObjectField("cis", curKm.getValue());
        }
        gen.writeEndObject();
    }
}
