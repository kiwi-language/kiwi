package tech.metavm.object.type;

import tech.metavm.util.NncUtils;

import java.util.List;
import java.util.Set;
import java.util.function.Function;

public class TypeTransformer<S> extends DefaultTypeVisitor<Type, S> implements Function<Type, Type> {

    public List<Type> visit(List<Type> types, S s) {
        return NncUtils.map(types, t -> visitType(t, s));
    }

    public Set<Type> visit(Set<Type> types, S s) {
        return NncUtils.mapUnique(types, t -> visitType(t, s));
    }

    @Override
    public Type visitType(Type type, S s) {
        return type;
    }

    @Override
    public Type apply(Type type) {
        return type.transform(this);
    }
}
