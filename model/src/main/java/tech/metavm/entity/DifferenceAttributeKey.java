package tech.metavm.entity;

import tech.metavm.object.instance.persistence.IndexEntryPO;
import tech.metavm.util.TypeReference;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class DifferenceAttributeKey<T> extends AttributeKey<T> {

    public static final DifferenceAttributeKey<List<IndexEntryPO>> OLD_INDEX_ITEMS
            = new DifferenceAttributeKey<>(new TypeReference<>() {}, false, ArrayList::new);

    public static final DifferenceAttributeKey<List<IndexEntryPO>> NEW_INDEX_ITEMS
            = new DifferenceAttributeKey<>(new TypeReference<>() {}, false, ArrayList::new);

    public DifferenceAttributeKey(TypeReference<T> typeReference, boolean nullable, Supplier<T> defaultValueSupplier) {
        super(typeReference, nullable, defaultValueSupplier);
    }

}
