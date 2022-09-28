package tech.metavm.object.instance.query;

import tech.metavm.object.meta.Field;
import tech.metavm.object.meta.Type;

public class TypeParsingContext implements ParsingContext {

    private final Type type;

    public TypeParsingContext(Type type) {
        this.type = type;
    }

    @Override
    public Expression parseField(String fieldPath) {
        return getExpression(type, fieldPath);
    }

    public static FieldExpression getExpression(Type type, String fieldPath) {
        return new FieldExpression(fieldPath, getField(type, fieldPath));
    }

    private static Field getField(Type type, String fieldPath) {
        if(type == null) {
            return null;
        }
        String[] splits = fieldPath.split("\\.");
        Field field = null;
        for (String split : splits) {
            if(type == null) {
                throw new RuntimeException("Invalid field path: " + fieldPath);
            }
            field = type.getFieldNyName(split);
            if(field.isComposite()) {
                type = field.getConcreteType();
            }
            else {
                type = null;
            }
        }
        return field;
    }
}
