package site.barsukov.barcodefx.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import site.barsukov.barcodefx.model.crpt.cancellation.KmCancellation;

import java.io.IOException;

public class CrptKMCancellationSerializer extends StdSerializer<KmCancellation.KmList> {

    public CrptKMCancellationSerializer() {
        this(null);
    }

    public CrptKMCancellationSerializer(Class<KmCancellation.KmList> t) {
        super(t);
    }

    @Override
    public void serialize(KmCancellation.KmList value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject();
        for (KmCancellation.KmList.Km curKm : value.getKm()) {
            gen.writeObjectField("km", curKm);
        }
        gen.writeEndObject();
    }
}
