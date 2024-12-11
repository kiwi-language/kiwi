package org.metavm.object.type;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.EntityField;
import org.metavm.api.Entity;
import org.metavm.entity.BuildKeyContext;
import org.metavm.entity.Element;
import org.metavm.entity.LocalKey;
import org.metavm.util.NamingUtils;

import javax.annotation.Nullable;

@Entity
public abstract class Constraint extends Element implements  ClassMember, LocalKey, ITypeDef {

    private Klass declaringType;
    @EntityField(asTitle = true)
    private String name;
    @Nullable
    private String message;
    private transient boolean visited;

    public Constraint(@NotNull Klass declaringType,
                      String name, @Nullable String message) {
        super(null);
        this.declaringType = declaringType;
        this.name =  NamingUtils.ensureValidName(name);
        this.message = message;
        declaringType.addConstraint(this);
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

    public boolean isVisited() {
        return visited;
    }

    public void setVisited(boolean visited) {
        this.visited = visited;
    }
}
