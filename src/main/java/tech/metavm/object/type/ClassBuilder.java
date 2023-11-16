package tech.metavm.object.type;

import org.jetbrains.annotations.NotNull;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class ClassBuilder {

    public static ClassBuilder newBuilder(String name, @Nullable String code) {
        return new ClassBuilder(name, code);
    }

    private Long tmpId;
    private final String name;
    private final @Nullable String code;
    private ClassType superType;
    private TypeCategory category = TypeCategory.CLASS;
    private ClassSource source = ClassSource.RUNTIME;
    private boolean anonymous;
    private boolean ephemeral;
    private boolean isTemplate;
    private String desc;
    private List<ClassType> interfaces = new ArrayList<>();
    private List<Type> typeArguments = new ArrayList<>();
    private ClassType existing;
    private boolean done;
    private Long suffix;
    private ClassType template;
    private List<ClassType> dependencies;
    private List<TypeVariable> typeParameters = List.of();

    private ClassBuilder(String name, @Nullable String code) {
        this.name = name;
        this.code = code;
    }

    public ClassBuilder superClass(ClassType superType) {
        this.superType = superType;
        return this;
    }

    public ClassBuilder tmpId(Long tmpId) {
        this.tmpId = tmpId;
        return this;
    }

    public ClassBuilder source(ClassSource source) {
        this.source = source;
        return this;
    }

    public ClassBuilder isTemplate(boolean isTemplate) {
        this.isTemplate = isTemplate;
        return this;
    }

    public ClassBuilder anonymous(boolean anonymous) {
        this.anonymous = anonymous;
        return this;
    }

    public ClassBuilder ephemeral(boolean ephemeral) {
        this.ephemeral = ephemeral;
        return this;
    }

    public ClassBuilder temporary() {
        ephemeral = true;
        anonymous = true;
        return randomSuffix();
    }

    public ClassBuilder template(ClassType template) {
        this.template = template;
        return this;
    }

    public ClassBuilder dependencies(List<ClassType> dependencies) {
        this.dependencies = dependencies;
        return this;
    }

    public ClassBuilder typeParameters(List<TypeVariable> typeParameters) {
        this.typeParameters = typeParameters;
        return this;
    }

    public ClassBuilder category(TypeCategory category) {
        this.category = category;
        return this;
    }

    public ClassBuilder desc(String desc) {
        this.desc = desc;
        return this;
    }

    public ClassBuilder interfaces(ClassType... interfaces) {
        return interfaces(List.of(interfaces));
    }

    public ClassBuilder interfaces(List<ClassType> interfaces) {
        this.interfaces = interfaces;
        return this;
    }

    public ClassBuilder randomSuffix() {
        return suffix(NncUtils.random());
    }

    public ClassBuilder suffix(@Nullable Long suffix) {
        this.suffix = suffix;
        return this;
    }

    public ClassBuilder typeParameters(TypeVariable... typeParameters) {
        return typeParameters(List.of(typeParameters));
    }

    public ClassBuilder typeArguments(List<Type> typeArguments) {
        this.typeArguments = typeArguments;
        return this;
    }


    public ClassBuilder existing(ClassType existing) {
        this.existing = existing;
        return this;
    }

    public ClassType build() {
        NncUtils.requireFalse(done, "Build has already been invoked");
        done = true;
        var classType = create();
        for (TypeVariable typeParameter : typeParameters) {
            typeParameter.setGenericDeclaration(classType);
        }
        return classType;
    }

    @NotNull
    private ClassType create() {
        if(NncUtils.isNotEmpty(typeParameters)) {
            isTemplate = true;
            NncUtils.requireEmpty(typeArguments, "Can not add type arguments to a template class");
        }
        ClassType classType;
        String effectiveName = suffix != null ? name + "_" + suffix : name;
        String effectiveCode = code != null ? (suffix != null ? code + "_" + suffix : code) : null;
        if(existing == null) {
            classType = new ClassType(
                    tmpId,
                    effectiveName,
                    effectiveCode,
                    superType,
                    interfaces,
                    category,
                    source,
                    anonymous,
                    ephemeral,
                    desc,
                    isTemplate,
                    template,
                    typeArguments
            );
        }
        else {
            classType = existing;
            existing.setName(effectiveName);
            existing.setCode(effectiveCode);
            existing.setSuperClass(superType);
            existing.setInterfaces(interfaces);
            existing.setSource(source);
            existing.setAnonymous(anonymous);
            existing.setDesc(desc);
            existing.setTypeArguments(typeArguments);
        }
        if(dependencies != null) {
            classType.setDependencies(dependencies);
        }
        return classType;
    }

}
