package tech.metavm.object.type.rest.dto;

public class StructuralTypeKeyVisitor extends TypeKeyVisitorBase<Void> {
    @Override
    public Void visitTypeKey(TypeKey typeKey) {
        typeKey.acceptChildren(this);
        return null;
    }
}
