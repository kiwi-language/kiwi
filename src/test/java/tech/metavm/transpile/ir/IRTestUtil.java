package tech.metavm.transpile.ir;

import tech.metavm.util.TypeReference;

import java.util.List;
import java.util.Set;

public class IRTestUtil {

    private static final TypeStore TYPE_STORE = new TypeStore(List.of(), Set.of(), Set.of());

    public static IRType createType(TypeReference<?> typeRef) {
        return TYPE_STORE.fromType(typeRef.getGenericType());
    }

    public static IRClass createClass(Class<?> klass) {
        return TYPE_STORE.fromClass(klass);
    }

//    public static final Map<Class<?>, IRClass> CLASS_MAP = new HashMap<>();

//    public static IRClass createClass(Class<?> klass) {
//        var irClass = CLASS_MAP.get(klass);
//        if(irClass != null) {
//            return irClass;
//        }
//        irClass = new IRClass(
//                klass.getName(),
//                getClassKInd(klass),
//                createPackage(klass.getPackageName()),
//                List.of(),
//                List.of(),
//                null,
//                null
//        );
//
//        CLASS_MAP.put(klass, irClass);
//        if(klass.getSuperclass() != null) {
//            irClass.setSuperType(createClass(klass.getSuperclass()));
//        }
//        irClass.setInterfaces(
//                NncUtils.map(
//                        Arrays.asList(klass.getInterfaces()),
//                        IRTestUtil::createClass
//                )
//        );
//
//        return irClass;
//    }

//    private static IRClassKind getClassKInd(Class<?> klass) {
//        if(klass.isInterface()) {
//            return IRClassKind.INTERFACE;
//        }
//        if(klass.isEnum()) {
//            return IRClassKind.ENUM;
//        }
//        if(klass.isRecord()) {
//            return IRClassKind.RECORD;
//        }
//        return IRClassKind.CLASS;
//    }

//    public static IRPackage createPackage(String name) {
//        if("".equals(name)) {
//            return IRPackage.ROOT_PKG;
//        }
//        return new IRPackage(name);
//    }

}
