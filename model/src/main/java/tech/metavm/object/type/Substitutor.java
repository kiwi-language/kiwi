package tech.metavm.object.type;

import tech.metavm.entity.IEntityContext;
import tech.metavm.util.NncUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Substitutor extends StructuralTypeTransformer<Void> {

    private final Map<TypeVariable, Type> map = new HashMap<>();

    public Substitutor(IEntityContext entityContext, List<TypeVariable> typeVariables, List<Type> typeArguments) {
        super(entityContext);
        NncUtils.requireTrue(typeVariables.size() == typeArguments.size());
        NncUtils.biForEach(typeVariables, typeVariables, map::put);
    }

    @Override
    public Type visitTypeVariable(TypeVariable type, Void unused) {
        return NncUtils.requireNonNull(map.get(type), "Can not resolve type variable: " + type);
    }
}
