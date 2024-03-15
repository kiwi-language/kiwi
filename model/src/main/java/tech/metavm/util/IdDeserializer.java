package tech.metavm.util;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import tech.metavm.object.instance.core.TmpId;

import java.io.IOException;

public class IdDeserializer extends StdDeserializer<String> {
    protected IdDeserializer() {
        super(String.class);
    }

    @Override
    public String deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {
        return jsonParser.getValueAsString();
    }

    @Override
    public String getNullValue(DeserializationContext ctxt) {
        return TmpId.of(NncUtils.randomNonNegative()).toString();
    }
}
