package org.metavm.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.lang.reflect.Field;

public class ReflectFieldSerializer extends StdSerializer<Field> {

    protected ReflectFieldSerializer() {
        super(Field.class);
    }

    @Override
    public void serialize(Field value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            gen.writeString(value.getDeclaringClass().getName() + "." + value.getName());
    }
}
