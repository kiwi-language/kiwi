package org.metavm.object.type;

import org.jetbrains.annotations.NotNull;
import org.metavm.entity.Attribute;
import org.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class KlassBuilder {

    public static KlassBuilder newBuilder(String name, @Nullable String code) {
        return new KlassBuilder(name, code);
    }

    private Long tmpId;
    private final String name;
    @Nullable
    private final String code;
    private ClassType superType;
    private ClassSource source = ClassSource.RUNTIME;
    private ClassKind kind = ClassKind.CLASS;
    private boolean anonymous;
    private boolean ephemeral;
    private boolean isAbstract;
    private boolean isTemplate;
    private boolean struct;
    private String desc;
    private List<ClassType> interfaces = new ArrayList<>();
    private List<? extends Type> typeArguments = new ArrayList<>();
    private Klass existing;
    private boolean done;
    private Long suffix;
    private Klass template;
    private List<Klass> dependencies;
    private List<TypeVariable> typeParameters = List.of();
    private long tag = TypeTags.DEFAULT;
    private Integer sourceCodeTag;
    private final List<Attribute> attributes = new ArrayList<>();
    private int since = 0;

    private KlassBuilder(String name, @Nullable String code) {
        this.name = name;
        this.code = code;
    }

    public KlassBuilder superType(ClassType superType) {
        this.superType = superType;
        return this;
    }

    public KlassBuilder tmpId(Long tmpId) {
        this.tmpId = tmpId;
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

    public KlassBuilder temporary() {
        ephemeral = true;
        anonymous = true;
        return randomSuffix();
    }

    public KlassBuilder template(Klass template) {
        this.template = template;
        return this;
    }

    public KlassBuilder dependencies(List<Klass> dependencies) {
        this.dependencies = dependencies;
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

    public KlassBuilder randomSuffix() {
        return suffix(NncUtils.randomNonNegative());
    }

    public KlassBuilder suffix(@Nullable Long suffix) {
        this.suffix = suffix;
        return this;
    }

    public KlassBuilder typeParameters(TypeVariable... typeParameters) {
        return typeParameters(List.of(typeParameters));
    }

    public KlassBuilder typeArguments(List<? extends Type> typeArguments) {
        this.typeArguments = typeArguments;
        return this;
    }

    public KlassBuilder isAbstract(boolean isAbstract) {
        this.isAbstract = isAbstract;
        return this;
    }

    public KlassBuilder tag(long tag) {
        this.tag = tag;
        return this;
    }

    public KlassBuilder sourceCodeTag(Integer sourceCodeTag) {
        this.sourceCodeTag = sourceCodeTag;
        return this;
    }

    public KlassBuilder existing(Klass existing) {
        this.existing = existing;
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

    public Klass build() {
        NncUtils.requireFalse(done, "Build has already been invoked");
        done = true;
        return create();
    }

    @NotNull
    private Klass create() {
        if (NncUtils.isNotEmpty(typeParameters)) {
            isTemplate = true;
            this.typeArguments = new ArrayList<>(NncUtils.map(typeParameters, TypeVariable::getType));
        }
        Klass klass;
        String effectiveName = suffix != null ? name + "_" + suffix : name;
        String effectiveCode = code != null ? (suffix != null ? code + "_" + suffix : code) : null;
        if (existing == null) {
            klass = new Klass(
                    tmpId,
                    effectiveName,
                    effectiveCode,
                    superType,
                    interfaces,
                    kind,
                    source,
                    template,
                    anonymous,
                    ephemeral,
                    struct,
                    desc,
                    isAbstract,
                    isTemplate,
                    typeParameters,
                    typeArguments,
                    tag,
                    sourceCodeTag,
                    since);
        } else {
            klass = existing;
            existing.setName(effectiveName);
            existing.setCode(effectiveCode);
            existing.setSuperType(superType);
            existing.setInterfaces(interfaces);
            existing.setSource(source);
            existing.setAbstract(isAbstract);
            existing.setAnonymous(anonymous);
            existing.setDesc(desc);
            existing.setTypeParameters(typeParameters);
            existing.setTypeArguments(typeArguments);
            existing.setStruct(struct);
        }
        klass.setAttributes(attributes);
        return klass;
    }

}
