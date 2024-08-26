package org.metavm.util;

import org.metavm.entity.GenericDeclaration;
import org.metavm.object.type.Klass;
import org.metavm.object.type.Type;

import java.util.List;
import java.util.function.BiConsumer;

public class ParameterizedStore {

    private static final ParameterizedMap sharedMap = new ParameterizedMap();
    private static final ThreadLocal<ParameterizedMap> TL = ThreadLocal.withInitial(ParameterizedMap::new);

    public static Object get(GenericDeclaration genericDeclaration, List<? extends Type> typeArguments) {
        var r =  map().get(genericDeclaration, typeArguments);
        return r != null ? r : sharedMap.get(genericDeclaration, typeArguments);
    }

    public static Object put(GenericDeclaration genericDeclaration, List<? extends Type> typeArguments, Object parameterized) {
        if(ThreadConfigs.sharedParameterizedElements())
            return sharedMap.put(genericDeclaration, typeArguments, parameterized);
        else
            return map().put(genericDeclaration, typeArguments, parameterized);
    }

    public static void forEach(Klass rawKlass, BiConsumer<List<? extends Type>, Object> action) {
        sharedMap.forEach(rawKlass, action);
        map().forEach(rawKlass, action);
    }

    public static void setMap(ParameterizedMap parameterizedMap) {
        TL.set(parameterizedMap);
    }

    public static ParameterizedMap getMap() {
        return map();
    }

    private static ParameterizedMap map() {
        return TL.get();
    }

}
