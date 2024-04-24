package tech.metavm.object.type;

import tech.metavm.entity.StandardTypes;
import tech.metavm.object.type.rest.dto.*;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TypeExpressions {

    /**
     *
     * @param type to be substituted
     * @param variableMap key: variable id, value: substitution type
     * @return the substituted type
     */
    public static String substitute(String type, Map<String, String> variableMap) {
        var typeKey = TypeKey.fromExpression(type);
        var typeKeyMap = new HashMap<VariableTypeKey, TypeKey>();
        variableMap.forEach((variableId, substType) -> typeKeyMap.put(new VariableTypeKey(variableId), TypeKey.fromExpression(substType)));
        return TypeKeys.substitute(typeKey, typeKeyMap).toTypeExpression();
    }

    public static String extractKlassId(String type) {
        var typeKey = TypeKey.fromExpression(type);
        if(typeKey instanceof ClassTypeKey classTypeKey)
            return classTypeKey.id();
        if(typeKey instanceof ParameterizedTypeKey parameterizedTypeKey)
            return parameterizedTypeKey.templateId();
        throw new InternalException("Can not extract klass id from type: " + type);
    }

    public static String getClassType(Klass klass) {
        return getClassType(klass.getStringId());
    }

    public static String getClassType(TypeDTO typeDTO) {
        return getClassType(typeDTO.id());
    }

    public static String getClassType(String id) {
        return "$$" + id;
    }

    public static String getNullableType(String type) {
        return getUnionType(Set.of(type, "null"));
    }

    public static String getUnionType(String...types) {
        return getUnionType(Set.of(types));
    }

    public static String getUnionType(Set<String> types) {
        return NncUtils.join(types, "|");
    }

    public static String getParameterizedType(String templateId, String...typeArguments) {
        return getParameterizedType(templateId, List.of(typeArguments));
    }

    public static String getParameterizedType(String templateId, List<String> typeArguments) {
        var buf = new StringBuilder(getClassType(templateId));
        if(!typeArguments.isEmpty())
            buf.append('<').append(java.lang.String.join(",", typeArguments)).append(">");
        return buf.toString();
    }

    public static String getReadWriteArrayType(String elementType) {
        return String.format("%s[]", elementType);
    }

    public static String getChildArrayType(String elementType) {
        return String.format("%s[c]", elementType);
    }

    public static String getListType(String elementType) {
        return getParameterizedType(StandardTypes.getListType().getStringId(), elementType);
    }

    public static String getReadWriteListType(String elementType) {
        return getParameterizedType(StandardTypes.getReadWriteListType().getStringId(), elementType);
    }

    public static String getChildListType(String elementType) {
        return getParameterizedType(StandardTypes.getChildListType().getStringId(), elementType);
    }

}
