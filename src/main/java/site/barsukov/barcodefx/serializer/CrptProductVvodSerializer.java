package site.barsukov.barcodefx.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import site.barsukov.barcodefx.model.crpt.vvodostatkov.VvodOstatky;

import java.io.IOException;

public class CrptProductVvodSerializer extends StdSerializer<VvodOstatky.ProductsList> {

    public CrptProductVvodSerializer() {
        this(null);
    }

    public CrptProductVvodSerializer(Class<VvodOstatky.ProductsList> t) {
        super(t);
    }

    @Override
    public void serialize(VvodOstatky.ProductsList value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject();
        for (VvodOstatky.ProductsList.Product curProduct : value.getProduct()) {
            gen.writeObjectField("product", curProduct);
        }
        gen.writeEndObject();
    }
}
