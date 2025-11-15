package org.metavm.object.type;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.metavm.api.Entity;
import org.metavm.api.EntityField;
import org.metavm.entity.*;
import org.metavm.wire.*;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Reference;
import org.metavm.util.NamingUtils;

import javax.annotation.Nullable;
import java.util.function.Consumer;

@Wire(69)
@Entity
public abstract class Constraint extends org.metavm.entity.Entity implements  ClassMember, LocalKey, ITypeDef, Element {

    @Setter
    @Parent
    private Klass declaringType;
    @Getter
    @EntityField(asTitle = true)
    private String name;
    @Nullable
    private final String message;

    public Constraint(Id id, @NotNull Klass declaringType,
                      String name, @Nullable String message) {
        super(id);
        this.declaringType = declaringType;
        this.name =  NamingUtils.ensureValidName(name);
        this.message = message;
        declaringType.addConstraint((Index) this);
    }

    public @Nullable String getMessage() {
        return message;
    }

    public abstract String getDefaultMessage();

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
    public void forEachChild(Consumer<? super Instance> action) {
    }

}
