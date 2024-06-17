package site.barsukov.barcodefx.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import site.barsukov.barcodefx.model.crpt.productionrf.IntroduceRf;

import java.io.IOException;

public class CrptWaterLicenceSerializer extends StdSerializer<IntroduceRf.ProductsList.Product.LicencesList> {

    public CrptWaterLicenceSerializer() {
        this(null);
    }

    public CrptWaterLicenceSerializer(Class<IntroduceRf.ProductsList.Product.LicencesList> t) {
        super(t);
    }

    @Override
    public void serialize(IntroduceRf.ProductsList.Product.LicencesList value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject();
        for (IntroduceRf.ProductsList.Product.LicencesList.Licence currentLicence : value.getLicence()) {
            gen.writeObjectField("licence", currentLicence);
        }
        gen.writeEndObject();
    }
}
