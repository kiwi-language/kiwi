package tech.metavm.entity;

import tech.metavm.object.instance.log.InstanceLog;
import tech.metavm.util.TypeReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class ContextAttributeKey<T> extends AttributeKey<T> {

    public static final  ContextAttributeKey<Map<Long, String>> INSTANCE_TITLES =
            new ContextAttributeKey<>(new TypeReference<>() {}, false, HashMap::new);

    public static final  ContextAttributeKey<List<InstanceLog>> CHANGE_LOGS =
            new ContextAttributeKey<>(new TypeReference<>() {}, false, ArrayList::new);

    public ContextAttributeKey(TypeReference<T> typeReference, boolean nullable, Supplier<T> defaultValueSupplier) {
        super(typeReference, nullable, defaultValueSupplier);
    }

}
