package org.metavm.entity;

import org.metavm.object.instance.log.InstanceLog;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ContextAttributeKey<T> extends AttributeKey<T> {

    /** @noinspection rawtypes, unchecked */
    public static final  ContextAttributeKey<List<InstanceLog>> CHANGE_LOGS =
            new ContextAttributeKey<>((Class) List.class, false, ArrayList::new);

    public ContextAttributeKey(Class<T> clazz, boolean nullable, Supplier<T> defaultValueSupplier) {
        super(clazz, nullable, defaultValueSupplier);
    }

}
