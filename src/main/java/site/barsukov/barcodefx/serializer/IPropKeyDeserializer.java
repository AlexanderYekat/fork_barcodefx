package site.barsukov.barcodefx.serializer;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.KeyDeserializer;
import site.barsukov.barcodefx.props.*;

import java.io.IOException;
import java.util.Arrays;

public class IPropKeyDeserializer extends KeyDeserializer {
    @Override
    public Object deserializeKey(String key, DeserializationContext ctxt) throws IOException {
        if (Arrays.stream(JsonCatalogProps.CatalogProp.values()).anyMatch(s -> s.name().equals(key))) {
            return JsonCatalogProps.CatalogProp.valueOf(key);
        } else if (Arrays.stream(JsonPrintProps.PrintProp.values()).anyMatch(s -> s.name().equals(key))) {
            return JsonPrintProps.PrintProp.valueOf(key);
        } else if (Arrays.stream(JsonSsccProps.SsccProp.values()).anyMatch(s -> s.name().equals(key))) {
            return JsonSsccProps.SsccProp.valueOf(key);
        } else if (Arrays.stream(JsonUpdateProps.UpdateProp.values()).anyMatch(s -> s.name().equals(key))) {
            return JsonUpdateProps.UpdateProp.valueOf(key);
        } else if (Arrays.stream(JsonLastStateProps.LastStateProp.values()).anyMatch(s -> s.name().equals(key))) {
            return JsonLastStateProps.LastStateProp.valueOf(key);
        } else if (Arrays.stream(JsonSystemProps.SystemProp.values()).anyMatch(s -> s.name().equals(key))) {
            return JsonSystemProps.SystemProp.valueOf(key);
        } else {
            return null;
        }
    }
}
