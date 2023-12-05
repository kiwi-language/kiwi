package tech.metavm.util;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;

public class TmpIdDeserializer extends StdDeserializer<Long> {

    protected TmpIdDeserializer() {
        super(Long.class);
    }

    @Override
    public Long deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {
        return jsonParser.getLongValue();
    }

    @Override
    public Long getNullValue(DeserializationContext context) {
        return NncUtils.randomNonNegative();
    }
}
