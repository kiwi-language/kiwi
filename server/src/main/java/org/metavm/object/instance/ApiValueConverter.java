package org.metavm.object.instance;

import lombok.extern.slf4j.Slf4j;
import org.metavm.api.dto.ClassTypeDTO;
import org.metavm.common.ErrorCode;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.PhysicalId;
import org.metavm.object.instance.rest.dto.*;
import org.metavm.util.ApiSearchResult;
import org.metavm.util.BusinessException;
import org.metavm.util.NamingUtils;
import org.metavm.util.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
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
            case DoubleValueDTO doubleValueDTO -> doubleValueDTO.value();
            case LongValueDTO longValueDTO -> longValueDTO.value();
            case ByteValueDTO byteValueDTO -> byteValueDTO.value();
            case CharValueDTO charValueDTO -> charValueDTO.value();
            case ShortValueDTO shortValueDTO -> shortValueDTO.value();
        };
    }

    public static Map<String, Object> toMap(ObjectDTO object) {
        var map = new HashMap<String, Object>();
        map.put(ObjectService.KEY_ID, object.id());
        map.put(ObjectService.KEY_TYPE, object.type().qualifiedName());
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

    public static SearchRequest buildSearchRequest(String className, Map<String, Object> query, int page, int pageSize) {
        var terms = new ArrayList<SearchTerm>();
        query.forEach((k, v) -> {
            if (k.equals(ObjectService.PAGE) || k.equals(ObjectService.PAGE_SIZE))
                return;
            var term = switch (v) {
                case List<?> list -> SearchTerm.ofRange(k, buildValue(list.getFirst()), buildValue(list.get(1)));
                default -> SearchTerm.of(k, buildValue(v));
            };
            terms.add(term);
        });
        return new SearchRequest(
                new ClassTypeDTO(className),
                terms,
                page,
                pageSize
        );
    }

    public static ValueDTO buildValue(Object o) {
        return switch (o) {
            case Byte b -> new ByteValueDTO(b);
            case Short s -> new ShortValueDTO(s);
            case Integer i -> new IntValueDTO(i);
            case Long l -> new LongValueDTO(l);
            case Float f -> new FloatValueDTO(f);
            case Double d -> new DoubleValueDTO(d);
            case Boolean z -> new BoolValueDTO(z);
            case Character c -> new CharValueDTO(c);
            case String str -> {
                if (str.startsWith("0")) {
                    try {
                        if (Id.parse(str) instanceof PhysicalId)
                            yield new ReferencedTO(str, null, null);
                    }
                    catch (Exception ignored) {}
                }
                yield new StringValueDTO(str);
            }
            case Map map -> buildObject(map);
            case List list -> buildArray(list);
            case null -> new NullDTO();
            default -> throw new BusinessException(ErrorCode.INVALID_REQUEST_BODY, o);
        };
    }

    public static ObjectDTO buildObject(Map map) {
        var typeExpr = map.get(ObjectService.KEY_TYPE) instanceof String s ? s : null;
        return buildObject(map, typeExpr);
    }

    public static ObjectDTO buildObject(Map map, String typeExpr) {
        var type = typeExpr != null ? new ClassTypeDTO(typeExpr) : null;
        var fields = new ArrayList<FieldDTO>();
        var children = new ArrayList<ObjectDTO>();
        map.forEach((k, v) -> {
            if (!(k instanceof String name) || name.isEmpty())
                throw invalidRequestBody(map);
            if (k.equals(ObjectService.KEY_ID) || k.equals(ObjectService.KEY_TYPE))
                return;
            if (Character.isUpperCase(name.charAt(0))) {
                if (!(v instanceof List list))
                    throw invalidRequestBody(map);
                for (Object e : list) {
                    if (!(e instanceof Map childMap))
                        throw invalidRequestBody(map);
                    children.add(buildObject(childMap, typeExpr + "." + name));
                }
            }
            else
                fields.add(new FieldDTO(name, buildValue(v)));
        });
        return new ObjectDTO(
                (String) map.get(ObjectService.KEY_ID),
                type,
                fields,
                children
        );
    }

    private static ArrayDTO buildArray(List list) {
        return new ArrayDTO(Utils.map(list, ApiValueConverter::buildValue));
    }

    private static BusinessException invalidRequestBody(Object o) {
        return new BusinessException(ErrorCode.INVALID_REQUEST_BODY, o);
    }

}
