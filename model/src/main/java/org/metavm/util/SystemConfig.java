package org.metavm.util;

import org.metavm.entity.*;
import org.metavm.entity.natives.DirectValueHolder;
import org.metavm.entity.natives.StdFunction;
import org.metavm.entity.natives.ThreadLocalValueHolder;
import org.metavm.entity.natives.ValueHolderOwner;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SystemConfig {

    private static final List<Class<? extends Enum<? extends ValueHolderOwner<?>>>> valueHolderEnums = List.of(
            StdKlass.class, StdMethod.class, StdFunction.class
    );

    public static void setThreadLocalMode() {
        ModelDefRegistry.setHolder(new ThreadLocalDefContextHolder());
        for (var valueHolderEnum : valueHolderEnums) {
            for (Enum<? extends ValueHolderOwner<?>> ec : valueHolderEnum.getEnumConstants()) {
                ((ValueHolderOwner<?>) ec).setValueHolder(new ThreadLocalValueHolder<>());
            }
        }
    }

    public static void setDefaultMode() {
        ModelDefRegistry.setHolder(new GlobalDefContextHolder());
        for (var valueHolderEnum : valueHolderEnums) {
            for (Enum<? extends ValueHolderOwner<?>> ec : valueHolderEnum.getEnumConstants()) {
                ((ValueHolderOwner<?>) ec).setValueHolder(new DirectValueHolder<>());
            }
        }
    }


}
