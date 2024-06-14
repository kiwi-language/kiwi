package org.metavm.entity;

import org.metavm.object.instance.persistence.IndexEntryPO;
import org.metavm.util.TypeReference;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Supplier;

public class DifferenceAttributeKey<T> extends AttributeKey<T> {

    public static final DifferenceAttributeKey<Collection<IndexEntryPO>> OLD_INDEX_ITEMS
            = new DifferenceAttributeKey<>(new TypeReference<>() {}, false, ArrayList::new);

    public static final DifferenceAttributeKey<Collection<IndexEntryPO>> NEW_INDEX_ITEMS
            = new DifferenceAttributeKey<>(new TypeReference<>() {}, false, ArrayList::new);

    public DifferenceAttributeKey(TypeReference<T> typeReference, boolean nullable, Supplier<T> defaultValueSupplier) {
        super(typeReference, nullable, defaultValueSupplier);
    }

}
