package site.barsukov.barcodefx.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import site.barsukov.barcodefx.model.crpt.acceptance.Acceptance;

import java.io.IOException;

public class CrptProductAcceptanceSerializer extends StdSerializer<Acceptance.ProductsList> {

    public CrptProductAcceptanceSerializer() {
        this(null);
    }

    public CrptProductAcceptanceSerializer(Class<Acceptance.ProductsList> t) {
        super(t);
    }

    @Override
    public void serialize(Acceptance.ProductsList value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject();
        for (Acceptance.ProductsList.Product curProduct : value.getProduct()) {
            gen.writeObjectField("product", curProduct);
        }
        gen.writeEndObject();
    }
}
