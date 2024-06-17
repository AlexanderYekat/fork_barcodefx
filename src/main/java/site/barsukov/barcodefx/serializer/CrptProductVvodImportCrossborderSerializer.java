package site.barsukov.barcodefx.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import site.barsukov.barcodefx.model.crpt.crossborder.VvodCrossborder;

import java.io.IOException;

public class CrptProductVvodImportCrossborderSerializer extends StdSerializer<VvodCrossborder.ProductsList> {

    public CrptProductVvodImportCrossborderSerializer() {
        this(null);
    }

    public CrptProductVvodImportCrossborderSerializer(Class<VvodCrossborder.ProductsList> t) {
        super(t);
    }

    @Override
    public void serialize(VvodCrossborder.ProductsList value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject();
        for (VvodCrossborder.ProductsList.Product curProduct : value.getProduct()) {
            gen.writeObjectField("product", curProduct);
        }
        gen.writeEndObject();
    }
}
