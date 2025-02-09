package org.metavm.user;

import org.metavm.annotation.NativeEntity;
import org.metavm.api.Entity;
import org.metavm.api.Generated;
import org.metavm.entity.EntityRegistry;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.InstanceVisitor;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.instance.core.Value;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.Klass;
import org.metavm.util.MvInput;
import org.metavm.util.MvOutput;
import org.metavm.util.StreamVisitor;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.function.Consumer;

@NativeEntity(22)
@Entity
public class SessionEntry extends org.metavm.entity.Entity {

    @SuppressWarnings("unused")
    private static Klass __klass__;
    private Session session;
    private String key;
    private Value value;

    public SessionEntry(Session session, String key, Value value) {
        super(session.nextChildId());
        this.session = session;
        this.key = key;
        this.value = value;
    }

    @Generated
    public static void visitBody(StreamVisitor visitor) {
        visitor.visitUTF();
        visitor.visitValue();
    }

    public String getKey() {
        return key;
    }

    public Value getValue() {
        return value;
    }

    public void setValue(Value value) {
        this.value = value;
    }

    @Override
    public String getTitle() {
        return null;
    }

    @Nullable
    @Override
    public org.metavm.entity.Entity getParentEntity() {
        return session;
    }

    @Override
    public void forEachReference(Consumer<Reference> action) {
        if (value instanceof Reference r) action.accept(r);
        else if (value instanceof org.metavm.object.instance.core.NativeValue t) t.forEachReference(action);
    }

    @Override
    public void buildJson(Map<String, Object> map) {
        map.put("key", this.getKey());
    }

    @Override
    public Klass getInstanceKlass() {
        return __klass__;
    }

    @Override
    public ClassType getInstanceType() {
        return __klass__.getType();
    }

    @Override
    public void forEachChild(Consumer<? super Instance> action) {
    }

    @Override
    public int getEntityTag() {
        return EntityRegistry.TAG_SessionEntry;
    }

    @Generated
    @Override
    public void readBody(MvInput input, org.metavm.entity.Entity parent) {
        this.session = (Session) parent;
        this.key = input.readUTF();
        this.value = input.readValue();
    }

    @Generated
    @Override
    public void writeBody(MvOutput output) {
        output.writeUTF(key);
        output.writeValue(value);
    }

    @Override
    protected void buildSource(Map<String, Value> source) {
    }
}
