package tech.metavm.transpile;

import tech.metavm.util.NncUtils;
import tech.metavm.util.ReflectUtils;

import java.util.List;

public class SourceTypeUtil {

    public static SourceType fromClassName(String name) {
        return fromClass(ReflectUtils.classForName(name));
    }

    public static SourceType fromClass(Class<?> klass) {
        if(FunctionUtil.isFunctionalInterface(klass)) {
            return new FunctionalInterfaceType(klass);
        }
        else {
            return new ReflectSourceType(klass);
        }
    }

    public static SourceType getCompatibleType(List<SourceType> sourceTypes) {
        NncUtils.requireNotEmpty(sourceTypes);
        if(sourceTypes.size() == 1) {
            return sourceTypes.get(0);
        }
        return fromClass(
                ReflectUtils.getCompatibleType(
                        NncUtils.map(sourceTypes, SourceType::getReflectClass)
                )
        );
    }

}
