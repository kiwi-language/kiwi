package tech.metavm.entity;

import tech.metavm.util.NncUtils;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public class ValueSupplier {

    public static <T> Map<Long, List<ValueSupplier>> toMap(List<T> list,
                                                           Function<T, Long> keyMapper,
                                                           Function<T, ValueSupplier> supplierMapper) {
        return NncUtils.toMultiMap(
                list,
                keyMapper,
                supplierMapper
        );
    }

    private final Supplier<Value> supplier;

    public ValueSupplier(Supplier<Value> supplier) {
        this.supplier = supplier;
    }
}
