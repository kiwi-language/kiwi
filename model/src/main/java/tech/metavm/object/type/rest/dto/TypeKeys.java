package tech.metavm.object.type.rest.dto;

import java.util.Map;

public class TypeKeys {

    public static TypeKey substitute(TypeKey typeKey, Map<VariableTypeKey, TypeKey> map) {
        return typeKey.accept(new TypeKeySubstitutor(map));
    }

}
