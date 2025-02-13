package org.metavm.object.type;

import org.metavm.entity.StdKlass;
import org.metavm.util.Utils;

import java.util.List;
import java.util.Set;

public class TypeExpressions {

    public static String getClassType(Klass klass) {
        return getClassType(klass.getStringId());
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
        return Utils.join(types, "|");
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
        return getParameterizedType(StdKlass.list.get().getStringId(), elementType);
    }

    public static String getArrayListType(String elementType) {
        return getParameterizedType(StdKlass.arrayList.get().getStringId(), elementType);
    }

}
