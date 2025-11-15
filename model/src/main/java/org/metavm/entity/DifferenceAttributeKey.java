package org.metavm.entity;

import org.metavm.object.instance.persistence.IndexEntryPO;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Supplier;

/** @noinspection rawtypes, unchecked */
public class DifferenceAttributeKey<T> extends AttributeKey<T> {

    public static final DifferenceAttributeKey<Collection<IndexEntryPO>> OLD_INDEX_ITEMS
            = new DifferenceAttributeKey<>((Class) Collection.class, false, ArrayList::new);

    public static final DifferenceAttributeKey<Collection<IndexEntryPO>> NEW_INDEX_ITEMS
            = new DifferenceAttributeKey<>((Class) Collection.class, false, ArrayList::new);

    public DifferenceAttributeKey(Class<T> clazz, boolean nullable, Supplier<T> defaultValueSupplier) {
        super(clazz, nullable, defaultValueSupplier);
    }

}
