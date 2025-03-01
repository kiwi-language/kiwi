package org.metavm.compiler.syntax;

import lombok.extern.slf4j.Slf4j;
import org.metavm.compiler.element.Clazz;
import org.metavm.compiler.util.List;
import org.metavm.compiler.util.Traces;

import javax.annotation.Nullable;
import java.util.function.Consumer;

@Slf4j
public final class ClassDecl extends Decl<Clazz> {
    private final ClassTag tag;
    private final List<Modifier> mods;
    private List<Annotation> annotations;
    private final Ident name;
    @Nullable
    private TypeNode extends_;
    private List<TypeNode> implements_;
    private final List<TypeVariableDecl> typeParameters;
    private final List<EnumConstantDecl> enumConstants;
    private List<Node> members;

    public ClassDecl(
            ClassTag tag,
            List<Annotation> annotations,
            List<Modifier> mods,
            Ident name,
            @Nullable TypeNode ext,
            List<TypeNode> impls,
            List<TypeVariableDecl> typeParameters,
            List<EnumConstantDecl> enumConstants,
            List<Node> members
    ) {
        this.tag = tag;
        this.annotations = annotations;
        this.mods = mods;
        this.name = name;
        this.extends_ = ext;
        this.implements_ = impls;
        this.typeParameters = typeParameters;
        this.enumConstants = enumConstants;
        this.members = members;
        if (Traces.traceParsing) {
            log.trace("Created {}", this);
        }
    }

    @Override
    public void write(SyntaxWriter writer) {
        for (Modifier mod : mods) {
            writer.write(mod.tag().name().toLowerCase());
            writer.write(" ");
        }
        writer.write(tag.name().toLowerCase());
        writer.write(" ");
        name.write(writer);
        writer.write(" ");
        if (extends_ != null) {
            writer.write("extends ");
            extends_.write(writer);
        }
        if (!implements_.isEmpty()) {
            writer.write("implements ");
            writer.write(implements_);
        }
        writer.writeln(" {");
        writer.indent();
        for (var ec : enumConstants) {
            ec.write(writer);
        }
        for (Node member : members) {
            member.write(writer);
        }
        writer.deIndent();
        writer.writeln("}");
    }

    @Override
    public <R> R accept(NodeVisitor<R> visitor) {
        return visitor.visitClassDecl(this);
    }

    @Override
    public void forEachChild(Consumer<Node> action) {
        mods.forEach(action);
        action.accept(name);
        if (extends_ != null) action.accept(extends_);
        implements_.forEach(action);
        typeParameters.forEach(action);
        enumConstants.forEach(action);
        members.forEach(action);
    }

    public ClassTag tag() {
        return tag;
    }

    public List<Modifier> mods() {
        return mods;
    }

    public Ident name() {
        return name;
    }

    @Nullable
    public TypeNode getExtends() {
        return extends_;
    }

    public void setExtends(@Nullable TypeNode extends_) {
        this.extends_ = extends_;
    }

    public List<TypeNode> getImplements() {
        return implements_;
    }

    public void setImplements(List<TypeNode> impls) {
        this.implements_ = impls;
    }


    public List<TypeVariableDecl> getTypeParameters() {
        return typeParameters;
    }

    public List<EnumConstantDecl> enumConstants() {
        return enumConstants;
    }

    public List<Annotation> getAnnotations() {
        return annotations;
    }

    public List<Node> getMembers() {
        return members;
    }

    public void setMembers(List<Node> members) {
        this.members = members;
    }

    @Override
    public String toString() {
        return "ClassDecl[" +
                "tag=" + tag + ", " +
                "mods=" + mods + ", " +
                "name=" + name + ", " +
                "extends_=" + extends_ + ", " +
                "implementList=" + implements_ + ", " +
                "enumConstants=" + enumConstants + ", " +
                "members=" + members + ']';
    }

}
