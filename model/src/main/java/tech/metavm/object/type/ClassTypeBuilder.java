package tech.metavm.object.type;

import org.jetbrains.annotations.NotNull;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class ClassTypeBuilder {

    public static ClassTypeBuilder newBuilder(String name, @Nullable String code) {
        return new ClassTypeBuilder(name, code);
    }

    private Long tmpId;
    private final String name;
    @Nullable
    private final String code;
    private ClassType superType;
    private TypeCategory category = TypeCategory.CLASS;
    private ClassSource source = ClassSource.RUNTIME;
    private boolean anonymous;
    private boolean ephemeral;
    private boolean isAbstract;
    private boolean isTemplate;
    private boolean struct;
    private String desc;
    private List<ClassType> interfaces = new ArrayList<>();
    private List<Type> typeArguments = new ArrayList<>();
    private ClassType existing;
    private boolean done;
    private Long suffix;
    private ClassType template;
    private List<ClassType> dependencies;
    private List<TypeVariable> typeParameters = List.of();

    private ClassTypeBuilder(String name, @Nullable String code) {
        this.name = name;
        this.code = code;
    }

    public ClassTypeBuilder superClass(ClassType superType) {
        this.superType = superType;
        return this;
    }

    public ClassTypeBuilder tmpId(Long tmpId) {
        this.tmpId = tmpId;
        return this;
    }

    public ClassTypeBuilder source(ClassSource source) {
        this.source = source;
        return this;
    }

    public ClassTypeBuilder isTemplate(boolean isTemplate) {
        this.isTemplate = isTemplate;
        return this;
    }

    public ClassTypeBuilder sourceClassName(String sourceClassName) {
        return this;
    }

    public ClassTypeBuilder anonymous(boolean anonymous) {
        this.anonymous = anonymous;
        return this;
    }

    public ClassTypeBuilder ephemeral(boolean ephemeral) {
        this.ephemeral = ephemeral;
        return this;
    }

    public ClassTypeBuilder temporary() {
        ephemeral = true;
        anonymous = true;
        return randomSuffix();
    }

    public ClassTypeBuilder template(ClassType template) {
        this.template = template;
        return this;
    }

    public ClassTypeBuilder dependencies(List<ClassType> dependencies) {
        this.dependencies = dependencies;
        return this;
    }

    public ClassTypeBuilder typeParameters(List<TypeVariable> typeParameters) {
        this.typeParameters = typeParameters;
        return this;
    }

    public ClassTypeBuilder category(TypeCategory category) {
        this.category = category;
        return this;
    }

    public ClassTypeBuilder desc(String desc) {
        this.desc = desc;
        return this;
    }

    public ClassTypeBuilder interfaces(ClassType... interfaces) {
        return interfaces(List.of(interfaces));
    }

    public ClassTypeBuilder interfaces(List<ClassType> interfaces) {
        this.interfaces = interfaces;
        return this;
    }

    public ClassTypeBuilder struct(boolean struct) {
        this.struct = struct;
        return this;
    }

    public ClassTypeBuilder randomSuffix() {
        return suffix(NncUtils.randomNonNegative());
    }

    public ClassTypeBuilder suffix(@Nullable Long suffix) {
        this.suffix = suffix;
        return this;
    }

    public ClassTypeBuilder typeParameters(TypeVariable... typeParameters) {
        return typeParameters(List.of(typeParameters));
    }

    public ClassTypeBuilder typeArguments(List<Type> typeArguments) {
        this.typeArguments = typeArguments;
        return this;
    }

    public ClassTypeBuilder isAbstract(boolean isAbstract) {
        this.isAbstract = isAbstract;
        return this;
    }

    public ClassTypeBuilder existing(ClassType existing) {
        this.existing = existing;
        return this;
    }

    public ClassType build() {
        NncUtils.requireFalse(done, "Build has already been invoked");
        done = true;
        return create();
    }

    @NotNull
    private ClassType create() {
        if (NncUtils.isNotEmpty(typeParameters)) {
            isTemplate = true;
            this.typeArguments = new ArrayList<>(typeParameters);
        }
        ClassType classType;
        String effectiveName = suffix != null ? name + "_" + suffix : name;
        String effectiveCode = code != null ? (suffix != null ? code + "_" + suffix : code) : null;
        if (existing == null) {
            classType = new ClassType(
                    tmpId,
                    effectiveName,
                    effectiveCode,
                    superType,
                    interfaces,
                    category,
                    source,
                    template,
                    anonymous,
                    ephemeral,
                    struct,
                    desc,
                    isAbstract,
                    isTemplate,
                    typeParameters,
                    typeArguments);
        } else {
            classType = existing;
            existing.setName(effectiveName);
            existing.setCode(effectiveCode);
            existing.setSuperClass(superType);
            existing.setInterfaces(interfaces);
            existing.setSource(source);
            existing.setAbstract(isAbstract);
            existing.setAnonymous(anonymous);
            existing.setDesc(desc);
            existing.setTypeParameters(typeParameters);
            existing.setTypeArguments(typeArguments);
            existing.setStruct(struct);
        }
        if (dependencies != null) {
            classType.setDependencies(dependencies);
        }
        return classType;
    }

}
