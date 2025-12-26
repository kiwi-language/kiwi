package org.metavm.compiler.element;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.metavm.compiler.type.Type;
import org.metavm.compiler.type.TypeSubst;
import org.metavm.compiler.type.Types;
import org.metavm.compiler.util.List;
import org.metavm.compiler.generate.KlassOutput;
import org.metavm.compiler.generate.WireTypes;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class FreeFunc extends Func implements FreeFuncRef, Comparable<FreeFunc> {

    private final Package pkg;
    private final Map<List<Type>, FreeFuncInst> instances = new HashMap<>();

    public FreeFunc(Name name, Package pkg) {
        super(name);
        this.pkg = pkg;
        pkg.addFunction(this);
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitFunction(this);
    }

    @Override
    public Name getQualName() {
        return getName();
    }

    @Override
    public Object getInternalName(@Nullable Func current) {
        return getQualName();
    }

    public Package getPkg() {
        return pkg;
    }

    @Override
    public int compareTo(@NotNull FreeFunc o) {
        var r = getName().compareTo(o.getName());
        if (r != 0)
            return r;
        return Types.instance.compareTypes(getParamTypes(), o.getParamTypes());
    }

    public FreeFuncRef getInst(List<Type> typeArguments) {
        if (typeArguments.equals(getParamTypes()))
            return this;
        var subst = TypeSubst.create(getTypeParams(), typeArguments);
        return instances.computeIfAbsent(typeArguments, k ->
                new FreeFuncInst(this, typeArguments,
                        getParamTypes().map(t -> t.accept(subst)),
                        getRetType().accept(subst)
                ));
    }

    @Override
    public String toString() {
        return "FreeFunction " + getName();
    }

    @Override
    public void write(KlassOutput output) {
        output.write(WireTypes.FUNCTION_REF);
        Elements.writeReference(this, output);
        output.writeList(getTypeParams(), t -> t.write(output));
    }
}
