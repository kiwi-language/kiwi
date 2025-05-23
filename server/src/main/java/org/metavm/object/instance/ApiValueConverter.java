package org.metavm.object.instance;

import org.metavm.api.dto.ClassTypeDTO;
import org.metavm.common.ErrorCode;
import org.metavm.object.instance.rest.dto.*;
import org.metavm.util.BusinessException;
import org.metavm.util.NamingUtils;
import org.metavm.util.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings({"rawtypes", "unchecked"})
public class ApiValueConverter {

    public static Object toRaw(ValueDTO value) {
        return switch (value) {
            case ArrayDTO arrayDTO -> Utils.map(arrayDTO.elements(), ApiValueConverter::toRaw);
            case BeanDTO beanDTO -> beanDTO.name();
            case BoolValueDTO boolValueDTO -> boolValueDTO.value();
            case EnumConstantDTO enumConstantDTO -> enumConstantDTO.name();
            case FloatValueDTO floatValueDTO -> floatValueDTO.value();
            case IntValueDTO intValueDTO -> intValueDTO.value();
            case NullDTO ignored -> null;
            case ObjectDTO objectDTO -> toMap(objectDTO);
            case ReferencedTO referencedTO -> referencedTO.id();
            case StringValueDTO stringValueDTO -> stringValueDTO.value();
        };
    }

    private static Map<String, Object> toMap(ObjectDTO object) {
        var map = new HashMap<String, Object>();
        map.put("$id", object.id());
        map.put("$type", object.type().qualifiedName());
        for (FieldDTO field : object.fields()) {
            map.put(field.name(), toRaw(field.value()));
        }
        for (ObjectDTO child : object.children()) {
            var childClassName = NamingUtils.extractSimpleName(child.type().qualifiedName());
            var list = (List) map.computeIfAbsent(childClassName, k -> new ArrayList<>());
            list.add(toRaw(child));
        }
        return map;
    }

    public static ValueDTO buildValue(Object o) {
        return switch (o) {
            case Byte b -> new IntValueDTO(b);
            case Short s -> new IntValueDTO(s);
            case Integer i -> new IntValueDTO(i);
            case Long l -> new IntValueDTO(l);
            case Float f -> new FloatValueDTO(f);
            case Double d -> new FloatValueDTO(d);
            case Boolean z -> new BoolValueDTO(z);
            case Character c -> new StringValueDTO(c.toString());
            case String str -> new StringValueDTO(str);
            case Map map -> buildObject(map);
            case List list -> buildArray(list);
            case null -> new NullDTO();
            default -> throw new BusinessException(ErrorCode.INVALID_REQUEST_BODY);
        };
    }

    public static ObjectDTO buildObject(Map map) {
        var typeExpr = map.get("$type") instanceof String s ? s : null;
        return buildObject(map, typeExpr);
    }

    public static ObjectDTO buildObject(Map map, String typeExpr) {
        var type = typeExpr != null ? new ClassTypeDTO(typeExpr) : null;
        var fields = new ArrayList<FieldDTO>();
        var children = new ArrayList<ObjectDTO>();
        map.forEach((k, v) -> {
            if (!(k instanceof String name) || name.isEmpty())
                throw invalidRequestBody();
            if (!k.equals("$id") && !k.equals("$type"))
                return;
            if (Character.isUpperCase(name.charAt(0))) {
                if (!(v instanceof List list))
                    throw invalidRequestBody();
                for (Object e : list) {
                    if (!(e instanceof Map childMap))
                        throw invalidRequestBody();
                    children.add(buildObject(childMap));
                }
            }
            else
                fields.add(new FieldDTO(name, buildValue(v)));
        });
        return new ObjectDTO(
                (String) map.get("$id"),
                type,
                fields,
                children
        );
    }

    private static ArrayDTO buildArray(List list) {
        return new ArrayDTO(Utils.map(list, ApiValueConverter::buildValue));
    }

    private static BusinessException invalidRequestBody() {
        return new BusinessException(ErrorCode.INVALID_REQUEST_BODY);
    }

}
