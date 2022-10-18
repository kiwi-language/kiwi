package tech.metavm.entity;

import java.util.EnumSet;

public enum LoadingOption {
    FIELDS_LAZY_LOADING;

    public static EnumSet<LoadingOption> none() {
        return EnumSet.noneOf(LoadingOption.class);
    }

    public static EnumSet<LoadingOption> of(LoadingOption option1, LoadingOption... rest) {
        return EnumSet.of(option1, rest);
    }


}
