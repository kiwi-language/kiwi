package org.metavm.common;

import org.metavm.entity.DescStore;
import org.metavm.util.NncUtils;
import org.metavm.util.ReflectionUtils;

import java.lang.reflect.RecordComponent;
import java.util.*;

public class DtoCopier {

    private static final Set<Class<?>> atomicClasses = Set.of(
            Byte.class, Short.class, Integer.class, Long.class, Float.class, Double.class, Boolean.class, Character.class, Date.class
    );

    public DtoCopier(CopyContext context) {
        this.context = context;
    }

    private enum Hint {
        ID,
        EXPRESSION,
        TYPE,
        NONE
    }

    private final CopyContext context;

    public <T> T copy(Object object) {
        //noinspection unchecked
        return (T) copy(object, Hint.NONE);
    }

    private Object copy(Object object, Hint hint) {
        if (object == null || atomicClasses.contains(object.getClass()))
            return object;
        if (object instanceof String string) {
            return switch (hint) {
                case ID -> context.mapId(string);
                case EXPRESSION -> context.mapExpression(string);
                case TYPE ->  context.mapType(string);
                case NONE -> string;
            };
        }
        return switch (object) {
            case Record record -> copyRecord(record, hint);
            case Map<?, ?> map -> copyMap(map, hint);
            case List<?> list -> copyList(list, hint);
            case Set<?> set -> copySet(set, hint);
            default -> copyObject(object);
        };
    }

    private Object copySet(Set<?> set, Hint hint) {
        return new HashSet<>(NncUtils.map(set, e -> copy(e, hint)));
    }

    private Object copyList(List<?> list, Hint hint) {
        return new ArrayList<>(NncUtils.map(list, e -> copy(e, hint)));
    }

    private Object copyMap(Map<?, ?> map, Hint hint) {
        var copy = new HashMap<>(map.size());
        map.forEach((k, v) -> copy.put(copy(k, hint), copy(v, hint)));
        return copy;
    }

    private Object copyObject(Object object) {
        var copy = ReflectionUtils.allocateInstance(object.getClass());
        DescStore.get(object.getClass()).forEachNonTransientProp(prop ->
                prop.set(copy, copy(prop.get(object), getHint(prop.getName())))
        );
        return copy;
    }

    private Object copyRecord(Record record, Hint hint) {
        var klass = record.getClass();
        var components = klass.getRecordComponents();
        var constructor = ReflectionUtils.getConstructor(
                klass,
                Arrays.stream(components)
                        .map(RecordComponent::getType)
                        .toArray(Class[]::new)
        );
        var componentValues = new Object[components.length];
        for (int i = 0; i < components.length; i++) {
            componentValues[i] = copy(ReflectionUtils.invoke(
                    record,
                    ReflectionUtils.getMethod(klass, components[i].getName())
            ), getHint(components[i].getName()));
        }
        return ReflectionUtils.invokeConstructor(constructor, componentValues);
    }

    private Hint getHint(String propName) {
        if(propName.equals("id") || propName.endsWith("Id") || propName.endsWith("Ids"))
            return Hint.ID;
        else if(propName.equals("expression") || propName.equals("expressions"))
            return Hint.EXPRESSION;
        else if(propName.equals("type") || propName.equals("typeArguments") || propName.endsWith("Type") || propName.endsWith("Types"))
            return Hint.TYPE;
        else
            return Hint.NONE;
    }

}
