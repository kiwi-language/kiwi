/*package org.metavm.object.type;

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
        if(classType.getSuperClass() != null) {
            visitType(classType.getSuperClass());
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
*/