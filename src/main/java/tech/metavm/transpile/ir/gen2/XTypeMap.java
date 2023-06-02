package tech.metavm.transpile.ir.gen2;

import tech.metavm.transpile.ir.IRExpression;
import tech.metavm.transpile.ir.IRType;
import tech.metavm.transpile.ir.TypeVariable;
import tech.metavm.transpile.ir.gen.ExprXType;
import tech.metavm.transpile.ir.gen.XType;
import tech.metavm.util.InternalException;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class XTypeMap {

    private final Map<IRExpression, Map<TypeVariable<?>, IRType>> map = new HashMap<>();

    IRType substituteType(IRExpression expression, IRType type) {
        return type.substituteReferenceRecursively(getMap(expression));
    }

    XType getXType(IRExpression expression, TypeVariable<?> typeVariable) {
        return (XType) get(expression, typeVariable);
    }

    IRType get(IRExpression expression, TypeVariable<?> typeVariable) {
        var subMap = map.get(expression);
        IRType result;
        if(subMap != null && (result = subMap.get(typeVariable)) != null) {
            return result;
        }
        throw new InternalException("Can not find XType for " + expression + "." + typeVariable);
    }

    Map<TypeVariable<?>, IRType> getMap(IRExpression expression) {
        return Collections.unmodifiableMap(map.computeIfAbsent(expression, k -> new HashMap<>()));
    }

    void createXType(IRExpression expression, TypeVariable<?> typeVariable) {
        put(expression, typeVariable, new ExprXType(expression, typeVariable));
    }

    void createAllXTypes(IRExpression expression, List<? extends TypeVariable<?>> typeVariables) {
        for (TypeVariable<?> typeVariable : typeVariables) {
            createXType(expression, typeVariable);
        }
    }

    void put(IRExpression expression, TypeVariable<?> typeVariable, IRType type) {
        var subMap = map.computeIfAbsent(expression, k -> new HashMap<>());
        if(subMap.containsKey(typeVariable)) {
            throw new InternalException("XType type for " + expression + "." + typeVariable + " already exists" );
        }
        subMap.put(typeVariable, type);
    }

    void putAll(IRExpression expression, Map<? extends TypeVariable<?>, ? extends IRType> map) {
        map.forEach((k,v) -> put(expression, k, v));
    }

    void merge(XTypeMap typeMap) {
        typeMap.map.forEach(this::putAll);
    }

}
