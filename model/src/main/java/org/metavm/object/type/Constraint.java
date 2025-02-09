package org.metavm.object.type;

import org.jetbrains.annotations.NotNull;
import org.metavm.annotation.NativeEntity;
import org.metavm.api.EntityField;
import org.metavm.api.Entity;
import org.metavm.api.Generated;
import org.metavm.entity.BuildKeyContext;
import org.metavm.entity.Element;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.EntityRegistry;
import org.metavm.entity.LocalKey;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Reference;
import org.metavm.util.MvInput;
import org.metavm.util.MvOutput;
import org.metavm.util.NamingUtils;
import org.metavm.util.StreamVisitor;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.function.Consumer;

@NativeEntity(69)
@Entity
public abstract class Constraint extends org.metavm.entity.Entity implements  ClassMember, LocalKey, ITypeDef, Element {

    @SuppressWarnings("unused")
    private static Klass __klass__;
    private Klass declaringType;
    @EntityField(asTitle = true)
    private String name;
    @Nullable
    private String message;

    public Constraint(Id id, @NotNull Klass declaringType,
                      String name, @Nullable String message) {
        super(id);
        this.declaringType = declaringType;
        this.name =  NamingUtils.ensureValidName(name);
        this.message = message;
        declaringType.addConstraint((Index) this);
    }

    @Generated
    public static void visitBody(StreamVisitor visitor) {
        visitor.visitUTF();
        visitor.visitNullable(visitor::visitUTF);
    }

    public @Nullable String getMessage() {
        return message;
    }

    public abstract String getDefaultMessage();

    public String getName() {
        return name;
    }

    public String getQualifiedName() {
        return declaringType.getTypeDesc() + "." + getName();
    }

    public void setName(String name) {
        this.name = NamingUtils.ensureValidName(name);
    }

    @Override
    public Klass getDeclaringType() {
        return declaringType;
    }

    public void setDeclaringType(Klass declaringType) {
        this.declaringType = declaringType;
    }

    public abstract String getDesc();

    @Override
    public boolean isValidLocalKey() {
        return true;
    }

    @Override
    public String getLocalKey(@NotNull BuildKeyContext context) {
        return name;
    }

    @Nullable
    @Override
    public org.metavm.entity.Entity getParentEntity() {
        return declaringType;
    }

    @Override
    public void acceptChildren(ElementVisitor<?> visitor) {
    }

    @Override
    public void forEachReference(Consumer<Reference> action) {
    }

    @Override
    public void buildJson(Map<String, Object> map) {
        var message = this.getMessage();
        if (message != null) map.put("message", message);
        map.put("defaultMessage", this.getDefaultMessage());
        map.put("name", this.getName());
        map.put("qualifiedName", this.getQualifiedName());
        map.put("declaringType", this.getDeclaringType().getStringId());
        map.put("desc", this.getDesc());
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
        return EntityRegistry.TAG_Constraint;
    }

    @Generated
    @Override
    public void readBody(MvInput input, org.metavm.entity.Entity parent) {
        this.declaringType = (Klass) parent;
        this.name = input.readUTF();
        this.message = input.readNullable(input::readUTF);
    }

    @Generated
    @Override
    public void writeBody(MvOutput output) {
        output.writeUTF(name);
        output.writeNullable(message, output::writeUTF);
    }

    @Override
    protected void buildSource(Map<String, org.metavm.object.instance.core.Value> source) {
    }
}
