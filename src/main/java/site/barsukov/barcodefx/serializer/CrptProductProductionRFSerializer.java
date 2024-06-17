package site.barsukov.barcodefx.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import site.barsukov.barcodefx.model.crpt.productionrf.IntroduceRf;

import java.io.IOException;

public class CrptProductProductionRFSerializer extends StdSerializer<IntroduceRf.ProductsList> {

    public CrptProductProductionRFSerializer() {
        this(null);
    }

    public CrptProductProductionRFSerializer(Class<IntroduceRf.ProductsList> t) {
        super(t);
    }

    @Override
    public void serialize(IntroduceRf.ProductsList value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject();
        for (IntroduceRf.ProductsList.Product curProduct : value.getProduct()) {
            gen.writeObjectField("product", curProduct);
        }
        gen.writeEndObject();
    }
}
