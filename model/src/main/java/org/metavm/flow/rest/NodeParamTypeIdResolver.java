package org.metavm.flow.rest;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DatabindContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import org.metavm.flow.NodeKind;

import java.io.IOException;

public class NodeParamTypeIdResolver implements TypeIdResolver {

    private JavaType baseType;

    @Override
    public void init(JavaType baseType) {
        this.baseType = baseType;
    }

    @Override
    public String idFromValue(Object value) {
        return NodeKind.getByParamClassRequired(value.getClass()).code() + "";
    }

    @Override
    public String idFromValueAndType(Object value, Class<?> suggestedType) {
        return idFromValue(value);
    }

    @Override
    public String idFromBaseType() {
        return "0";
    }

    @Override
    public JavaType typeFromId(DatabindContext context, String id) throws IOException {
        int code = Integer.parseInt(id);
        NodeKind type = NodeKind.getByCodeRequired(code);
        return context.resolveSubType(baseType, type.getParamKlass().getName());
    }

    @Override
    public String getDescForKnownTypeIds() {
        return "category code used as category objectId";
    }

    @Override
    public JsonTypeInfo.Id getMechanism() {
        return JsonTypeInfo.Id.NAME;
    }
}
