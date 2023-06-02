package tech.metavm.transpile.ir;

import tech.metavm.transpile.TypeRange;
import tech.metavm.transpile.ir.gen.XType;

import java.util.List;
import java.util.Objects;

public class TypeVariable<D extends GenericDeclaration<D>> extends TypeRange {

    private final D genericDeclaration;
    private final String name;

    public TypeVariable(D genericDeclaration, String name, List<IRType> upperBounds) {
        super(name, upperBounds, List.of());
        this.genericDeclaration = genericDeclaration;
        this.name = name;
        //noinspection unchecked
        var declaration = (InternalGenericDeclaration<D>) genericDeclaration;
        declaration.addTypeParameter(this);
    }

    public D getGenericDeclaration() {
        return genericDeclaration;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean typeEquals(IRType type) {
        return equals(type);
    }

    @Override
    public List<XType> getVariables() {
        return List.of();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TypeVariable<?> that = (TypeVariable<?>) o;
        return Objects.equals(genericDeclaration, that.genericDeclaration) && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(genericDeclaration, name);
    }
}
