package org.metavm.object.type.rest.dto;

import org.metavm.util.NncUtils;

import java.util.HashMap;
import java.util.Map;

public class TypeKeySubstitutor extends TypeKeyVisitorBase<TypeKey> {

    private final Map<VariableTypeKey, TypeKey> map;

    public TypeKeySubstitutor(Map<VariableTypeKey, TypeKey> map) {
        this.map = new HashMap<>(map);
    }

    @Override
    public TypeKey visitTypeKey(TypeKey typeKey) {
        return typeKey;
    }

    @Override
    public TypeKey visitVariableTypeKey(VariableTypeKey typeKey) {
        return map.getOrDefault(typeKey, typeKey);
    }

    @Override
    public TypeKey visitArrayTypeKey(ArrayTypeKey typeKey) {
        return new ArrayTypeKey(typeKey.kind(), typeKey.elementTypeKey().accept(this));
    }

    @Override
    public TypeKey visitFunctionTypeKey(FunctionTypeKey typeKey) {
        return new FunctionTypeKey(NncUtils.map(typeKey.parameterTypeKeys(), k -> k.accept(this)), typeKey.returnTypeKey().accept(this));
    }

    @Override
    public TypeKey visitUnionTypeKey(UnionTypeKey typeKey) {
        return new UnionTypeKey(NncUtils.mapUnique(typeKey.memberKeys(), k -> k.accept(this)));
    }

    @Override
    public TypeKey visitIntersectionTypeKey(IntersectionTypeKey typeKey) {
        return new IntersectionTypeKey(NncUtils.mapUnique(typeKey.typeKeys(), k -> k.accept(this)));
    }

    @Override
    public TypeKey visitUncertainTypeKey(UncertainTypeKey typeKey) {
        return new UncertainTypeKey(typeKey.lowerBoundKey().accept(this), typeKey.upperBoundKey().accept(this));
    }

    @Override
    public TypeKey visitParameterizedTypeKey(ParameterizedTypeKey typeKey) {
        return new ParameterizedTypeKey(
                NncUtils.get(typeKey.owner(), k -> k.accept(this)),
                typeKey.templateId(), NncUtils.map(typeKey.typeArgumentKeys(), k -> k.accept(this)));
    }

}
