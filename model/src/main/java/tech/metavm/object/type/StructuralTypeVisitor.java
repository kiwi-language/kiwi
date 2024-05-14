package tech.metavm.object.type;

public class StructuralTypeVisitor extends DefaultTypeVisitor<Void, Void> {
    @Override
    public Void visitType(Type type, Void unused) {
        type.acceptComponents(this, unused);
        return null;
    }
}
