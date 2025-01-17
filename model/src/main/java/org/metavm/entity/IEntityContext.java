package org.metavm.entity;

import org.metavm.ddl.Commit;
import org.metavm.event.EventQueue;
import org.metavm.flow.*;
import org.metavm.object.instance.core.Value;
import org.metavm.object.instance.core.*;
import org.metavm.object.type.*;
import org.metavm.util.Instances;
import org.metavm.util.Utils;
import org.metavm.util.ParameterizedMap;
import org.metavm.util.TypeReference;
import org.metavm.util.profile.Profiler;

import javax.annotation.Nullable;
import java.io.Closeable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

public interface IEntityContext extends Closeable, EntityRepository, TypeProvider, TypeDefProvider, RedirectStatusProvider, IInstanceContext {

    default void loadKlasses() {
        Klasses.loadKlasses(this);
    }

    @Override
    default <T extends Instance> T bind(T entity) {
        batchBind(List.of(entity));
        return entity;
    }

    default <T extends Entity> T getEntity(TypeReference<T> typeReference, Id id) {
        return getEntity(typeReference.getType(), id);
    }

    default <T extends Entity> T getEntity(TypeReference<T> typeReference, String id) {
        return getEntity(typeReference.getType(), id);
    }

    default <T> T getEntity(Class<T> klass, Instance instance) {
        return klass.cast(instance);
    }

    Profiler getProfiler();

    DefContext getDefContext();

    <T> List<T> getAllBufferedEntities(Class<T> entityClass);

    void close();

    <T> T getBufferedEntity(Class<T> entityType, Id id);

    <T> T getEntity(Class<T> entityType, Id id);

    default <T> T getEntity(Class<T> entityType, String id) {
        return id != null ? getEntity(entityType, Id.parse(id)) : null;
    }

    Type getType(Class<?> javaType);

    default Type getType(String id) {
        return getEntity(Type.class, id);
    }

    <T> T getRemoved(Class<T> entityClass, Id id);

    default Type getType(Id id) {
        return getEntity(Type.class, id);
    }

    default ITypeDef getTypeDef(Id id) {
        return getEntity(ITypeDef.class, id);
    }

    default TypeDef getTypeDef(String id) {
        return getEntity(TypeDef.class, id);
    }

    default Klass getKlass(Id id) {
        return getEntity(Klass.class, id);
    }

    default Klass getKlass(String id) {
        return getEntity(Klass.class, id);
    }

    @Nullable
    EventQueue getEventQueue();

    long getAppId();

    default Field getField(Id id) {
        return getEntity(Field.class, id);
    }

    default Field getField(String id) {
        return getEntity(Field.class, id);
    }

    default Commit getCommit(Id id) {
        return getEntity(Commit.class, id);
    }

    default Commit getCommit(String id) {
        return getEntity(Commit.class, id);
    }

    default TypeVariable getTypeVariable(Id id) {
        return getEntity(TypeVariable.class, id);
    }

    default TypeVariable getTypeVariable(String id) {
        return getEntity(TypeVariable.class, id);
    }

    default CapturedTypeVariable getCapturedTypeVariable(String id) {
        return getEntity(CapturedTypeVariable.class, id);
    }

    default Node getNode(Id id) {
        return getEntity(Node.class, id);
    }

    default Node getNode(String id) {
        return getEntity(Node.class, id);
    }

    default Flow getFlow(Id id) {
        return getEntity(Flow.class, id);
    }

    default Flow getFlow(String id) {
        return getEntity(Flow.class, id);
    }

    default Code getCode(Id id) {
        return getEntity(Code.class, id);
    }

    default Code getCode(String id) {
        return getEntity(Code.class, id);
    }

    default Method getMethod(Id id) {
        return getEntity(Method.class, id);
    }

    default Method getMethod(String id) {
        return getEntity(Method.class, id);
    }

    @Override
    default RedirectStatus getRedirectStatus(Id id) {
        return getEntity(RedirectStatus.class, id);
    }

    default Function getFunction(Id id) {
        return getEntity(Function.class, id);
    }

    default Function getFunction(String id) {
        return getEntity(Function.class, id);
    }

    boolean isFinished();

    void finish();

    <T extends Entity> List<T> query(EntityIndexQuery<T> query);

    long count(EntityIndexQuery<?> query);

    void flush();

    <T extends Entity> List<T> selectByKey(IndexDef<T> indexDef, Value... refValues);

    @Nullable
    default <T extends Entity> T selectFirstByKey(IndexDef<T> indexDef, Value... values) {
        return Utils.first(selectByKey(indexDef, values));
    }

    default @Nullable Klass findKlassByQualifiedName(String qualifiedName) {
        return selectFirstByKey(Klass.UNIQUE_QUALIFIED_NAME, Instances.stringInstance(qualifiedName));
    }

    default Klass getKlassByQualifiedName(String qualifiedName) {
        return Objects.requireNonNull(findKlassByQualifiedName(qualifiedName));
    }

    boolean containsUniqueKey(IndexDef<?> indexDef, Value... values);

    IEntityContext createSame(long appId);

    ScanResult scan(long start, long limit);

    ParameterizedMap getParameterizedMap();

    void setParameterizedMap(ParameterizedMap parameterizedMap);
}
