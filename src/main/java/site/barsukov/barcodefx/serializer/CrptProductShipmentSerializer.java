package site.barsukov.barcodefx.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import site.barsukov.barcodefx.model.crpt.shipping.Shipment;

import java.io.IOException;

public class CrptProductShipmentSerializer extends StdSerializer<Shipment.ProductsList> {

    public CrptProductShipmentSerializer() {
        this(null);
    }

    public CrptProductShipmentSerializer(Class<Shipment.ProductsList> t) {
        super(t);
    }

    @Override
    public void serialize(Shipment.ProductsList value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject();
        for (Shipment.ProductsList.Product curProduct : value.getProduct()) {
            gen.writeObjectField("product", curProduct);
        }
        gen.writeEndObject();
    }
}
