package tech.metavm.object.meta;

import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class ClassBuilder {

    public static ClassBuilder newBuilder(String name, @Nullable String code) {
        return new ClassBuilder( name, code);
    }

    private Long tmpId;
    private final String name;
    private final @Nullable String code;
    private ClassType superType;
    private TypeCategory category = TypeCategory.CLASS;
    private ClassSource source = ClassSource.RUNTIME;
    private boolean anonymous;
    private boolean ephemeral;
    private String desc;
    private List<ClassType> interfaces = new ArrayList<>();
    private List<Type> typeArguments = new ArrayList<>();
    private boolean done;
    private Long suffix;
    private String template;

    private ClassBuilder(String name, @Nullable String code) {
        this.name = name;
        this.code = code;
    }

    public ClassBuilder superType(ClassType superType) {
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

    public ClassBuilder category(TypeCategory category) {
        this.category = category;
        return this;
    }

    public ClassBuilder desc(String desc) {
        this.desc = desc;
        return this;
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

    public ClassBuilder template(String template) {
        this.template = template;
        return this;
    }

    public ClassBuilder typeArguments(List<Type> typeArguments) {
        this.typeArguments = typeArguments;
        return this;
    }

    public ClassType build() {
        NncUtils.requireFalse(done, "Build has already been invoked");
        done = true;
        return new ClassType(
                tmpId,
                suffix != null ? name + "_" + suffix : name,
                code != null ? (suffix != null ? code + "_" + suffix : code) : null,
                superType,
                interfaces,
                category,
                source,
                anonymous,
                ephemeral,
                desc,
                template,
                typeArguments
        );
    }
}
