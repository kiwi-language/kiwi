package org.metavm.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

public class ReflectClassSerializer extends StdSerializer<Class<?>> {

    protected ReflectClassSerializer() {
        super(new TypeReference<Class<?>>(){}.getType());
    }

    @Override
    public void serialize(Class<?> value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            gen.writeString(value.getName());
    }
}
