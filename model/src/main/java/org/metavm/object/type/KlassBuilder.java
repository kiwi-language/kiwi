package org.metavm.object.type;

import org.jetbrains.annotations.NotNull;
import org.metavm.entity.Attribute;
import org.metavm.object.instance.core.Id;
import org.metavm.util.Utils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class KlassBuilder {

    public static KlassBuilder newBuilder(Id id, String name, String qualifiedName) {
        return new KlassBuilder(id, name, qualifiedName);
    }

    private final Id id;
    private final String name;
    private final String qualifiedName;
    private ClassType superType;
    private ClassSource source = ClassSource.RUNTIME;
    private ClassKind kind = ClassKind.CLASS;
    private boolean anonymous;
    private boolean ephemeral;
    private boolean searchable;
    private boolean isAbstract;
    private boolean isTemplate;
    private boolean struct;
    private String desc;
    private List<ClassType> interfaces = new ArrayList<>();
    private boolean done;
    private List<TypeVariable> typeParameters = List.of();
    private long tag = TypeTags.DEFAULT;
    private Integer sourceTag;
    private final List<Attribute> attributes = new ArrayList<>();
    private int since = 0;
    private @Nullable KlassDeclaration scope;
    private boolean maintenanceDisabled;

    private KlassBuilder(Id id, String name, @Nullable String qualifiedName) {
        this.id = id;
        this.name = name;
        this.qualifiedName = qualifiedName;
    }

    public KlassBuilder superType(ClassType superType) {
        this.superType = superType;
        return this;
    }

    public KlassBuilder source(ClassSource source) {
        this.source = source;
        return this;
    }

    public KlassBuilder isTemplate(boolean isTemplate) {
        this.isTemplate = isTemplate;
        return this;
    }

    public KlassBuilder anonymous(boolean anonymous) {
        this.anonymous = anonymous;
        return this;
    }

    public KlassBuilder ephemeral(boolean ephemeral) {
        this.ephemeral = ephemeral;
        return this;
    }

    public KlassBuilder searchable(boolean searchable) {
        this.searchable = searchable;
        return this;
    }

    public KlassBuilder typeParameters(List<TypeVariable> typeParameters) {
        this.typeParameters = typeParameters;
        return this;
    }

    public KlassBuilder kind(ClassKind kind) {
        this.kind = kind;
        return this;
    }

    public KlassBuilder desc(String desc) {
        this.desc = desc;
        return this;
    }

    public KlassBuilder interfaces(ClassType... interfaces) {
        return interfaces(List.of(interfaces));
    }

    public KlassBuilder interfaces(List<ClassType> interfaces) {
        this.interfaces = interfaces;
        return this;
    }

    public KlassBuilder struct(boolean struct) {
        this.struct = struct;
        return this;
    }

    public KlassBuilder typeParameters(TypeVariable... typeParameters) {
        return typeParameters(List.of(typeParameters));
    }

    public KlassBuilder isAbstract(boolean isAbstract) {
        this.isAbstract = isAbstract;
        return this;
    }

    public KlassBuilder tag(long tag) {
        this.tag = tag;
        return this;
    }

    public KlassBuilder sourceTag(Integer sourceTag) {
        this.sourceTag = sourceTag;
        return this;
    }

    public KlassBuilder addAttribute(String name, String value) {
        attributes.removeIf(a -> a.name().equals(name));
        attributes.add(new Attribute(name, value));
        return this;
    }

    public KlassBuilder since(int since) {
        this.since = since;
        return this;
    }

    public KlassBuilder scope(@Nullable KlassDeclaration scope) {
        this.scope = scope;
        return this;
    }

    public KlassBuilder maintenanceDisabled() {
        this.maintenanceDisabled = true;
        return this;
    }

    public Klass build() {
        Utils.require(!done, "Build has already been invoked");
        done = true;
        return create();
    }

    @NotNull
    private Klass create() {
        if (Utils.isNotEmpty(typeParameters)) {
            isTemplate = true;
        }
        var klass = new Klass(
                id,
                name,
                qualifiedName,
                superType,
                interfaces,
                kind,
                source,
                anonymous,
                ephemeral,
                struct,
                searchable,
                desc,
                isAbstract,
                isTemplate,
                scope,
                typeParameters,
                tag,
                sourceTag,
                since,
                maintenanceDisabled);
        klass.setAttributes(attributes);
        return klass;
    }

}
