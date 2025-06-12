package org.metavm.compiler.syntax;

import lombok.extern.slf4j.Slf4j;
import org.metavm.compiler.element.Clazz;
import org.metavm.compiler.element.Name;
import org.metavm.compiler.element.NameTable;
import org.metavm.compiler.util.List;
import org.metavm.compiler.util.Traces;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Consumer;

@Slf4j
public final class ClassDecl extends Decl<Clazz> {
    private final ClassTag tag;
    private final List<Modifier> mods;
    private final List<Annotation> annotations;
    private final Name name;
    @Nullable
    private TypeNode extends_;
    private List<Extend> implements_;
    private final List<TypeVariableDecl> typeParameters;
    private List<ClassParamDecl> params;
    private final List<EnumConstDecl> enumConstants;
    private List<Node> members;

    public ClassDecl(
            ClassTag tag,
            List<Annotation> annotations,
            List<Modifier> mods,
            Name name,
            @Nullable TypeNode ext,
            List<Extend> impls,
            List<TypeVariableDecl> typeParameters,
            List<ClassParamDecl> params,
            List<EnumConstDecl> enumConstants,
            List<Node> members
    ) {
        this.tag = tag;
        this.annotations = annotations;
        this.mods = mods;
        this.name = name;
        this.extends_ = ext;
        this.implements_ = impls;
        this.typeParameters = typeParameters;
        this.params = params;
        this.enumConstants = enumConstants;
        this.members = members;
        if (Traces.traceParsing) {
            log.trace("Created {}", this);
        }
    }

    public boolean isAnonymous() {
        return name == NameTable.instance.empty;
    }

    @Override
    public void write(SyntaxWriter writer) {
        for (Modifier mod : mods) {
            writer.write(mod.tag().name().toLowerCase());
            writer.write(" ");
        }
        writer.write(tag.name().toLowerCase());
        writer.write(" ");
        writer.write(name);
        if (params.nonEmpty()) {
            writer.writeln("(");
            writer.indent();
            var paramIt = params.iterator();
            while (paramIt.hasNext()) {
                writer.write(paramIt.next());
                if (paramIt.hasNext())
                    writer.write(",");
                writer.writeln();
            }
            writer.deIndent();
            writer.write(")");
        }
        if (implements_.nonEmpty()) {
            writer.write(": ");
            writeExtends(writer);
        }
        writeBody(writer);
    }

    public void writeExtends(SyntaxWriter writer) {
        writer.write(implements_);
    }

    public void writeBody(SyntaxWriter writer) {
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
        if (extends_ != null) action.accept(extends_);
        implements_.forEach(action);
        typeParameters.forEach(action);
        params.forEach(action);
        enumConstants.forEach(action);
        members.forEach(m -> {
            if (m instanceof FieldDecl fieldDecl)
                action.accept(fieldDecl);
        });
        members.forEach(m -> {
            if (!(m instanceof FieldDecl))
                action.accept(m);
        });
    }

    public ClassTag tag() {
        return tag;
    }

    public List<Modifier> mods() {
        return mods;
    }

    public Name name() {
        return name;
    }

    public void setExtends(@Nullable TypeNode extends_) {
        this.extends_ = extends_;
    }

    public List<Extend> getImplements() {
        return implements_;
    }

    public void setImplements(List<Extend> impls) {
        this.implements_ = impls;
    }


    public List<TypeVariableDecl> getTypeParameters() {
        return typeParameters;
    }

    public List<EnumConstDecl> enumConstants() {
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

    public List<ClassParamDecl> getParams() {
        return params;
    }

    public void setParams(List<ClassParamDecl> params) {
        this.params = params;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        ClassDecl classDecl = (ClassDecl) object;
        return tag == classDecl.tag && Objects.equals(mods, classDecl.mods) && Objects.equals(annotations, classDecl.annotations) && Objects.equals(name, classDecl.name) && Objects.equals(extends_, classDecl.extends_) && Objects.equals(implements_, classDecl.implements_) && Objects.equals(typeParameters, classDecl.typeParameters) && Objects.equals(params, classDecl.params) && Objects.equals(enumConstants, classDecl.enumConstants) && Objects.equals(members, classDecl.members);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tag, mods, annotations, name, extends_, implements_, typeParameters, params, enumConstants, members);
    }

    @Override
    public ClassDecl setPos(int pos) {
        return (ClassDecl) super.setPos(pos);
    }
}
