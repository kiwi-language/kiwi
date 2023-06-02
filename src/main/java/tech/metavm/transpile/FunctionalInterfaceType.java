package tech.metavm.transpile;

import java.lang.reflect.Type;

public class FunctionalInterfaceType extends ReflectSourceType {

    public FunctionalInterfaceType(Type type) {
        super(type);
    }

    @Override
    public boolean isAssignableFrom(SourceType that) {
        if(getReflectClass().isAssignableFrom(that.getReflectClass())) {
            return true;
        }
        if(that instanceof LambdaType lambdaType) {
            return LambdaTypeMatchUtil.match(lambdaType, type);
        }
        return false;
    }

}
