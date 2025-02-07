package org.metavm.object.type;

import org.metavm.annotation.NativeEntity;
import org.metavm.api.Entity;
import org.metavm.api.Generated;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.EntityRegistry;
import org.metavm.entity.IndexDef;
import org.metavm.entity.LocalKey;
import org.metavm.flow.CodeWriter;
import org.metavm.flow.Method;
import org.metavm.object.instance.IndexKeyRT;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.instance.core.Value;
import org.metavm.util.MvInput;
import org.metavm.util.MvOutput;
import org.metavm.util.StreamVisitor;
import org.metavm.util.Utils;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@NativeEntity(36)
@Entity
public class Index extends Constraint implements LocalKey, ITypeDef {

    @SuppressWarnings("unused")
    private static Klass __klass__;
    private int typeIndex;
    private boolean unique;
    private @Nullable Reference method;
    private transient IndexDef<?> indexDef;

    public Index(Klass declaringKlass, String name, String message, boolean unique, Type type,
                 @Nullable Method method) {
        super(declaringKlass, name, message);
        this.unique = unique;
        this.method = Utils.safeCall(method, Instance::getReference);
        this.typeIndex = declaringKlass.getConstantPool().addValue(type);
    }

    public Index(Long tmpId, Klass declaringKlass, String name, String message, boolean unique, int typeIndex) {
        super(declaringKlass, name, message);
        setTmpId(tmpId);
        this.unique = unique;
        this.typeIndex = typeIndex;
    }

    @Generated
    public static void visitBody(StreamVisitor visitor) {
        Constraint.visitBody(visitor);
        visitor.visitInt();
        visitor.visitBoolean();
        visitor.visitNullable(visitor::visitValue);
    }


    public IndexKeyRT createIndexKey(List<Value> values) {
        return new IndexKeyRT(this, values);
    }

    public boolean isUnique() {
        return unique;
    }

    @Override
    public String getDefaultMessage() {
        return "Duplicate field";
    }

    public Type getType() {
        return getType(getDeclaringType().getConstantPool());
    }

    public Type getType(TypeMetadata typeMetadata) {
        return typeMetadata.getType(typeIndex);
    }

    public int getTypeIndex() {
        return typeIndex;
    }

    public void setTypeIndex(int typeIndex) {
        this.typeIndex = typeIndex;
    }

    public void setType(Type type) {
        this.typeIndex = getDeclaringType().getConstantPool().addValue(type);
    }

    @Override
    public String getDesc() {
        return "Index " + getName();
    }

    public IndexDef<?> getIndexDef() {
        return indexDef;
    }

    public void setIndexDef(IndexDef<?> indexDef) {
        this.indexDef = indexDef;
    }

    @Override
    public String getTitle() {
        return getName();
    }

    public IndexRef getRef() {
        return new IndexRef(getDeclaringType().getType(), this);
    }


    public void setMethod(@Nullable Method method) {
        this.method = Utils.safeCall(method, Instance::getReference);
    }

    public void setMethod(@Nullable Reference method) {
        this.method = method;
    }

    public @Nullable Method getMethod() {
        return Utils.safeCall(method, m -> (Method) m.get());
    }

    public @Nullable Reference getMethodReference() {
        return method;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitIndex(this);
    }

    @Override
    public void acceptChildren(ElementVisitor<?> visitor) {
        super.acceptChildren(visitor);
    }

    public void writeCode(CodeWriter writer) {
        writer.writeln((unique ? "unique index " : "index ") + getName());
    }

    public void setUnique(boolean unique) {
        this.unique = unique;
    }

    @Override
    public void forEachReference(Consumer<Reference> action) {
        super.forEachReference(action);
        if (method != null) action.accept(method);
    }

    @Override
    public void buildJson(Map<String, Object> map) {
        map.put("unique", this.isUnique());
        map.put("defaultMessage", this.getDefaultMessage());
        map.put("type", this.getType().toJson());
        map.put("desc", this.getDesc());
        map.put("indexDef", this.getIndexDef());
        map.put("ref", this.getRef().toJson());
        var method = this.getMethod();
        if (method != null) map.put("method", method.getStringId());
        var methodReference = this.getMethodReference();
        if (methodReference != null) map.put("methodReference", methodReference.toJson());
        var message = this.getMessage();
        if (message != null) map.put("message", message);
        map.put("name", this.getName());
        map.put("qualifiedName", this.getQualifiedName());
        map.put("declaringType", this.getDeclaringType().getStringId());
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
        super.forEachChild(action);
    }

    @Override
    public int getEntityTag() {
        return EntityRegistry.TAG_Index;
    }

    @Generated
    @Override
    public void readBody(MvInput input, org.metavm.entity.Entity parent) {
        super.readBody(input, parent);
        this.typeIndex = input.readInt();
        this.unique = input.readBoolean();
        this.method = input.readNullable(() -> (Reference) input.readValue());
    }

    @Generated
    @Override
    public void writeBody(MvOutput output) {
        super.writeBody(output);
        output.writeInt(typeIndex);
        output.writeBoolean(unique);
        output.writeNullable(method, output::writeValue);
    }

    @Override
    protected void buildSource(Map<String, Value> source) {
        super.buildSource(source);
    }
}
