package org.metavm.entity;

import org.metavm.entity.natives.NativeBase;
import org.metavm.entity.natives.ValueHolder;
import org.metavm.object.type.Klass;

import javax.annotation.Nullable;
import java.util.Objects;

public class BuiltinKlassDef {

    private final Class<?> javaClass;
    private final boolean autoDefine;
    private final @Nullable Class<? extends NativeBase> nativeClass;
    private ValueHolder<Klass> klassHolder;

    public BuiltinKlassDef(Class<?> javaClass, boolean autoDefine, @Nullable Class<? extends NativeBase> nativeClass, ValueHolder<Klass> klassHolder) {
        this.javaClass = javaClass;
        this.autoDefine = autoDefine;
        this.nativeClass = nativeClass;
        this.klassHolder = klassHolder;
    }

    public Class<?> getJavaClass() {
        return javaClass;
    }

    public boolean isAutoDefine() {
        return autoDefine;
    }

    @Nullable
    public Class<?> getNativeClass() {
        return nativeClass;
    }

    public Klass get() {
        return Objects.requireNonNull(klassHolder.get(), () -> "Builtin klass not initialized: " + javaClass.getName());
    }

    void set(Klass klass) {
        klass.setNativeClass(nativeClass);
        klassHolder.set(klass);
    }

    void setKlassHolder(ValueHolder<Klass> klassHolder) {
        this.klassHolder = klassHolder;
    }

    public void init(DefContext defContext) {
        var klass = defContext.getKlass(javaClass);
        set(klass);
    }
}
