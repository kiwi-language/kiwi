package tech.metavm.object.meta;

import tech.metavm.object.instance.ArrayType;

public class MetaTypeVisitor {

    public void visitType(Type type) {
        switch (type) {
            case ClassType classType -> visitClassType(classType);
            case TypeVariable typeVariable -> visitTypeVariable(typeVariable);
            case ArrayType arrayType -> visitArrayType(arrayType);
            default -> throw new IllegalStateException("Unexpected value: " + type);
        }
    }

    public void visitClassType(ClassType classType) {
        if(classType.getSuperType() != null) {
            visitType(classType.getSuperType());
        }
        classType.getInterfaces().forEach(this::visitType);
        classType.getTypeParameters().forEach(this::visitType);
    }

    public void visitTypeVariable(TypeVariable typeVariable) {

    }

    public void visitArrayType (ArrayType arrayType) {
        visitType(arrayType.getElementType());
    }

}
