package org.metavm.util;

import org.metavm.entity.*;
import org.metavm.entity.natives.HybridValueHolder;
import org.metavm.entity.natives.StdFunction;
import org.metavm.entity.natives.ThreadLocalValueHolder;
import org.metavm.entity.natives.ValueHolderOwner;

import java.util.List;

public class SystemConfig {

    private static final List<Class<? extends Enum<? extends ValueHolderOwner<?>>>> valueHolderEnums = List.of(
            StdKlass.class, StdMethod.class, StdFunction.class, StdField.class
    );

    public static void setThreadLocalMode() {
        ModelDefRegistry.setHolder(new ThreadLocalDefContextHolder());
        for (var valueHolderEnum : valueHolderEnums) {
            for (Enum<? extends ValueHolderOwner<?>> ec : valueHolderEnum.getEnumConstants()) {
                ((ValueHolderOwner<?>) ec).setValueHolder(new ThreadLocalValueHolder<>());
            }
        }
    }

//    public static void setDefaultMode() {
//        ModelDefRegistry.setHolder(new GlobalDefContextHolder());
//        for (var valueHolderEnum : valueHolderEnums) {
//            for (Enum<? extends ValueHolderOwner<?>> ec : valueHolderEnum.getEnumConstants()) {
//                ((ValueHolderOwner<?>) ec).setValueHolder(new DirectValueHolder<>());
//            }
//        }
//    }

    public static void setHybridMode() {
        ModelDefRegistry.setHolder(new HybridDefContextHolder());
        for (var valueHolderEnum : valueHolderEnums) {
            for (Enum<? extends ValueHolderOwner<?>> ec : valueHolderEnum.getEnumConstants()) {
                ((ValueHolderOwner<?>) ec).setValueHolder(new HybridValueHolder<>());
            }
        }
    }

    public static void clearLocal() {
        ModelDefRegistry.clearLocal();
        for (var valueHolderEnum : valueHolderEnums) {
            for (Enum<? extends ValueHolderOwner<?>> ec : valueHolderEnum.getEnumConstants()) {
                ((ValueHolderOwner<?>) ec).getValueHolder().clearLocal();
            }
        }
    }

}
