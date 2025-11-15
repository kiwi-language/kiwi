package org.metavm.compiler.syntax;

import org.metavm.compiler.element.Name;
import org.metavm.compiler.util.List;

public class ClassDeclBuilder {


    public static ClassDeclBuilder builder(Name name) {
        return new ClassDeclBuilder(name);
    }

    private List<Annotation> annotations = List.nil();
    private List<Modifier> mods = List.of();
    private ClassTag tag = ClassTag.CLASS;
    private final Name name;
    private List<TypeVariableDecl> typeParams = List.of();
    private List<Extend> extends_ = List.of();
    private List<ClassParamDecl> params = List.of();
    private List<EnumConstDecl> enumConsts = List.of();
    private List<Node> members = List.of();

    public ClassDeclBuilder(Name name) {
        this.name = name;
    }


    public ClassDeclBuilder tag(ClassTag tag) {
        this.tag = tag;
        return this;
    }

    public ClassDeclBuilder typeParams(List<TypeVariableDecl> typeParams) {
        this.typeParams = typeParams;
        return this;
    }

    public ClassDeclBuilder params(List<ClassParamDecl> params) {
        this.params = params;
        return this;
    }

    public ClassDeclBuilder extends_(List<Extend> extends_) {
        this.extends_ = extends_;
        return this;
    }

    public ClassDeclBuilder addExtend(Extend extend) {
        this.extends_ = this.extends_.append(extend);
        return this;
    }

    public ClassDeclBuilder annotations(List<Annotation> annotations) {
        this.annotations = annotations;
        return this;
    }

    public ClassDeclBuilder mods(List<Modifier> mods) {
        this.mods = mods;
        return this;
    }

    public ClassDeclBuilder enumConsts(List<EnumConstDecl> enumConsts) {
        this.enumConsts = enumConsts;
        return this;
    }

    public ClassDeclBuilder members(List<Node> members) {
        this.members = members;
        return this;
    }

    public ClassDeclBuilder addMember(Node member) {
        this.members = this.members.append(member);
        return this;
    }

    public ClassDeclBuilder addAnnotation(Annotation annotation) {
        this.annotations = this.annotations.append(annotation);
        return this;
    }

    public ClassDeclBuilder addModifier(Modifier modifier) {
        this.mods = this.mods.append(modifier);
        return this;
    }

    public ClassDeclBuilder addTypeParam(TypeVariableDecl typeParam) {
        this.typeParams = this.typeParams.append(typeParam);
        return this;
    }


    public ClassDecl build() {
        return new ClassDecl(
                tag,
                annotations,
                mods,
                name,
                null,
                extends_,
                typeParams,
                params,
                enumConsts,
                members
        );
    }


}
