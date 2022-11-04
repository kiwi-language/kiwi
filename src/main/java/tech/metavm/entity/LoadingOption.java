package tech.metavm.entity;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

public enum LoadingOption {
    FIELDS_LAZY_LOADING,
    CONSTRAINTS_LAZY_LOADING,
    ENUM_CONSTANTS_LAZY_LOADING,

    ;

    public static Set<LoadingOption> none() {
        return EnumSet.noneOf(LoadingOption.class);
    }

    public static Set<LoadingOption> of(LoadingOption option1, LoadingOption... rest) {
        return EnumSet.of(option1, rest);
    }

    public static Set<LoadingOption> createSet() {
        return new HashSet<>();
    }

}
