package site.barsukov.barcodefx.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import site.barsukov.barcodefx.model.crpt.withdraw.Withdrawal;

import java.io.IOException;

public class CrptProductWithdrawSerializer extends StdSerializer<Withdrawal.ProductsList> {

    public CrptProductWithdrawSerializer() {
        this(null);
    }

    public CrptProductWithdrawSerializer(Class<Withdrawal.ProductsList> t) {
        super(t);
    }

    @Override
    public void serialize(Withdrawal.ProductsList value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject();
        for (Withdrawal.ProductsList.Product curProduct : value.getProduct()) {
            gen.writeObjectField("product", curProduct);
        }
        gen.writeEndObject();
    }
}
