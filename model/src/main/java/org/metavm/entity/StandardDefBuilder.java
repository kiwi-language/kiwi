package org.metavm.entity;

import lombok.extern.slf4j.Slf4j;
import org.metavm.api.Index;
import org.metavm.api.Interceptor;
import org.metavm.api.entity.*;
import org.metavm.entity.natives.StdFunction;
import org.metavm.object.type.Klass;
import org.metavm.object.type.StringType;
import org.metavm.util.IteratorImpl;

import java.io.*;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

@Slf4j
public class StandardDefBuilder {

    private final SystemDefContext defContext;

    private final List<KlassDef<?>> defList = new ArrayList<>();

    public StandardDefBuilder(SystemDefContext defContext) {
        this.defContext = defContext;
    }

    public static final List<Class<?>> classes = List.of(
            Entity.class,
            String.class,
            Record.class,
            Exception.class,
            IOException.class,
            ObjectStreamException.class,
            InvalidObjectException.class,
            ReflectiveOperationException.class,
            ClassNotFoundException.class,
            RuntimeException.class,
            IllegalArgumentException.class,
            IllegalStateException.class,
            NullPointerException.class,
            ArithmeticException.class,
            UnsupportedOperationException.class,
            ConcurrentModificationException.class,
            ClassCastException.class,
            NoSuchElementException.class,
            IndexOutOfBoundsException.class,
            CloneNotSupportedException.class,
            ArrayIndexOutOfBoundsException.class,
            Error.class,
            VirtualMachineError.class,
            InternalError.class,
            OutOfMemoryError.class,
            Consumer.class,
            Predicate.class,
            Iterator.class,
            Iterable.class,
            Comparator.class,
            Comparable.class,
            Collection.class,
            IteratorImpl.class,
            Set.class,
            List.class,
            Map.class,
            InputStream.class,
            OutputStream.class,
            Number.class,
            ObjectInputStream.class,
            ObjectOutputStream.class,
            Byte.class,
            Short.class,
            Integer.class,
            Long.class,
            Float.class,
            Double.class,
            Character.class,
            Boolean.class,
            Enum.class,
            Throwable.class,
            ArrayList.class,
            HashSet.class,
            TreeSet.class,
            HashMap.class,
            StringBuffer.class,
            Index.class,
            Interceptor.class,
            HttpCookie.class,
            HttpHeader.class,
            HttpRequest.class,
            HttpResponse.class,
            MvObject.class
    );

    public void initRootTypes() {
        classes.forEach(this::parseKlass);
        initSystemFunctions();
        defList.forEach(defContext::afterDefInitialized);
    }

    private void initSystemFunctions() {
        StdFunction.defineSystemFunctions(defContext).forEach(defContext::writeEntity);
//        StandardStaticMethods.defineFunctions().forEach(defContext::writeEntity);
    }

    public void initUserFunctions() {
        StdFunction.defineUserFunctions(defContext).forEach(defContext::writeEntity);
    }

    Klass parseKlass(Class<?> javaClass) {
        if(javaClass == Class.class)
            return defContext.getKlass(Klass.class);
        var klass = defContext.tryGetKlass(javaClass);
        if(klass == null) {
            final KlassDef<?>[] def = new KlassDef<?>[1];
            var definer = new ReflectDefiner(javaClass, defContext.getTypeTag(javaClass), this::parseKlass,
                    (jk, k) -> {
                        if (jk == String.class) {
                            StdKlass.string.set(k);
                            k.setType(new StringType());
                        }
                        def[0] = new KlassDef<>(javaClass, k);
                        defContext.preAddDef(def[0]);
                    },
                    defContext::getModelId);
            var r = definer.defineClass();
            klass = r.klass();
            var sft = r.staticFieldTable();
            def[0].setStaticFieldTable(sft);
            defList.add(def[0]);
        }
        return klass;
    }

}
