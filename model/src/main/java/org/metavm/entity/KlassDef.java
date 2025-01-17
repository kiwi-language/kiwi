package org.metavm.entity;

import org.metavm.object.type.ClassType;
import org.metavm.object.type.Klass;
import org.metavm.object.type.StaticFieldTable;

import javax.annotation.Nullable;
import java.util.List;

public final class KlassDef<T> {
    final Klass klass;
    private final Class<T> javaClass;
    private boolean initialized;
    @Nullable
    private KlassParser<T> parser;
    private StaticFieldTable staticFieldTable;

    public KlassDef(Class<T> javaClass, Klass klass) {
        this.javaClass = javaClass;
        this.klass = klass;
    }

    public void setStaticFieldTable(StaticFieldTable staticFieldTable) {
        this.staticFieldTable = staticFieldTable;
    }

    public Klass getTypeDef() {
        return klass;
    }

    public ClassType getType() {
        return klass.getType();
    }

    public Klass getKlass() {
        return klass;
    }

    public List<Entity> getEntities() {
        if(staticFieldTable == null)
            return List.of(klass);
        else
            return List.of(klass, staticFieldTable);
    }

    public Class<T> getJavaClass() {
        return javaClass;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void setInitialized(boolean initialized) {
        this.initialized = initialized;
    }

    @Nullable
    public KlassParser<T> getParser() {
        return parser;
    }

    public void setParser(@Nullable KlassParser<T> parser) {
        this.parser = parser;
    }


}
