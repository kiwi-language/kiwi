package tech.metavm.object.instance;

import tech.metavm.object.meta.Type;
import tech.metavm.object.meta.TypeCategory;
import tech.metavm.object.meta.rest.dto.ArrayTypeParamDTO;

import java.util.Objects;

public class ArrayType extends Type {

    private final Type elementType;

    public ArrayType(Type elementType, boolean ephemeral) {
        super(getArrayTypeName(elementType), false, ephemeral, TypeCategory.ARRAY);
        this.elementType = elementType;
    }

    private static String getArrayTypeName(Type elementType) {
        return elementType.getName() + "[]";
    }

    @Override
    public Type getConcreteType() {
        return elementType.getConcreteType();
    }

    @Override
    public boolean isAssignableFrom(Type that) {
        if(that instanceof ArrayType arrayType) {
            return elementType.isAssignableFrom(arrayType.elementType);
        }
        else {
            return false;
        }
    }

    public Type getElementType() {
        return elementType;
    }

    @Override
    protected ArrayTypeParamDTO getParam() {
        return new ArrayTypeParamDTO(
                elementType.getId(),
                elementType.toDTO()
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ArrayType arrayType = (ArrayType) o;
        return Objects.equals(elementType, arrayType.elementType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(elementType);
    }
}
