package site.barsukov.barcodefx.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import site.barsukov.barcodefx.model.crpt.acceptance.Acceptance;
import site.barsukov.barcodefx.model.crpt.remark.Remark;

import java.io.IOException;

public class CrptProductRemarkSerializer extends StdSerializer<Remark.ProductsList> {

    public CrptProductRemarkSerializer() {
        this(null);
    }

    public CrptProductRemarkSerializer(Class<Remark.ProductsList> t) {
        super(t);
    }

    @Override
    public void serialize(Remark.ProductsList value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject();
        for (Remark.ProductsList.Product curProduct : value.getProduct()) {
            gen.writeObjectField("product", curProduct);
        }
        gen.writeEndObject();
    }
}
