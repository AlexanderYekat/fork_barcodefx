package site.barsukov.barcodefx.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import site.barsukov.barcodefx.model.crpt.vvodimportfts.IntroduceImportFts;

import java.io.IOException;

public class CrptProductVvodImportFtsSerializer extends StdSerializer<IntroduceImportFts.ProductsList> {

    public CrptProductVvodImportFtsSerializer() {
        this(null);
    }

    public CrptProductVvodImportFtsSerializer(Class<IntroduceImportFts.ProductsList> t) {
        super(t);
    }

    @Override
    public void serialize(IntroduceImportFts.ProductsList value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject();
        for (IntroduceImportFts.ProductsList.Product curProduct : value.getProduct()) {
            gen.writeObjectField("product", curProduct);
        }
        gen.writeEndObject();
    }
}
