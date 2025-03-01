package org.metavm.compiler.element;

import org.jetbrains.annotations.NotNull;
import org.metavm.compiler.type.Type;
import org.metavm.compiler.type.Types;
import org.metavm.compiler.util.List;

import javax.annotation.Nullable;
import java.util.Objects;

public class FreeFunc extends Func implements Comparable<FreeFunc> {

    private @Nullable FreeFuncInst instance;

    public FreeFunc(SymName name) {
        super(name);
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitFunction(this);
    }

    @Override
    public SymName getQualifiedName() {
        return getName();
    }

    @Override
    public Object getInternalName(@Nullable Func current) {
        return getQualifiedName();
    }

    @Override
    public int compareTo(@NotNull FreeFunc o) {
        var r = getName().compareTo(o.getName());
        if (r != 0)
            return r;
        return Types.instance.compareTypes(getParameterTypes(), o.getParameterTypes());
    }

    public void initInstance() {
        assert instance == null;
        instance = new FreeFuncInst(
                this,
                List.into(getTypeParameters()),
                getParameterTypes(),
                getReturnType()
        );
    }

    public FreeFuncInst getInstance() {
        return Objects.requireNonNull(instance);
    }

    public FreeFuncInst getInstance(List<Type> typeArguments) {
        return getInstance().getInstance(typeArguments);
    }

    @Override
    public String toString() {
        return "FreeFunction " + getName();
    }
}
