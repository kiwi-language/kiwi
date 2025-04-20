package org.metavm.compiler.analyze;

import lombok.extern.slf4j.Slf4j;
import org.metavm.compiler.element.*;
import org.metavm.compiler.element.ClassTag;
import org.metavm.compiler.syntax.*;
import org.metavm.compiler.type.PrimitiveType;
import org.metavm.compiler.type.Types;
import org.metavm.compiler.util.List;
import org.metavm.util.Utils;

import java.util.Objects;

import static org.metavm.compiler.util.Traces.traceEntering;

@Slf4j
public class Enter {

    private final Project project;
    private int nextLambdaId = 0;

    public Enter(Project project) {
        this.project = project;
    }

    public void enter(List<File> files) {
        var visitor = new EnterVisitor();
        files.forEach(f -> f.accept(visitor));
    }

    public void enter(Node node, Element parent) {
        var visitor = new EnterVisitor();
        visitor.elements = visitor.elements.prepend(parent);
        node.accept(visitor);
    }

    private class EnterVisitor extends StructuralNodeVisitor {

        private List<Element> elements = List.nil();

        @Override
        public Void visitFile(File file) {
            var pkg = file.getPackageDecl() != null ?
                    project.getOrCreatePackage(file.getPackageDecl().getName()) : project.getRootPackage();
            file.setPackage(pkg);
            elements = elements.prepend(pkg);
            super.visitFile(file);
            elements = elements.tail();
            return null;
        }

        @Override
        public Void visitClassDecl(ClassDecl classDecl) {
            if (traceEntering)
                log.trace("Entering class {}", classDecl.name());
            var scope = (ClassScope) currentElement();
            var name = classDecl.isAnonymous() ?
                    Name.from("$" + scope.getClasses().size()) : classDecl.name();
            var mods = parseModifiers(classDecl.mods());
            var clazz = new Clazz(
                    ClassTag.valueOf(classDecl.tag().name()),
                    name,
                    parseAccess(classDecl.mods()),
                    scope
            );
            if (mods.temp)
                clazz.setEphemeral(true);
            if (mods.static_)
                clazz.setStatic(true);
            if (clazz.isEnum())
                enterEnumMethods(clazz);
            classDecl.setElement(clazz);
            enterElement(clazz);
            super.visitClassDecl(classDecl);
            exitElement();
            if (!clazz.isInterface() && !classDecl.isAnonymous() && clazz.getMethods().nonMatch(Method::isInit)) {
                var initDecl = NodeMaker.methodDecl(
                        new Method(
                                Name.init(),
                                Access.PUBLIC,
                                false,
                                false,
                                true,
                                clazz
                        ),
                        List.of()
                );
                classDecl.setMembers(classDecl.getMembers().prepend(initDecl));
            }
            return null;
        }

        private void enterEnumMethods(Clazz clazz) {
            assert clazz.isEnum();
            new Method(Name.from("values"), Access.PUBLIC, true, false, false, clazz);
            var valueOfMethod = new Method(Name.from("valueOf"), Access.PUBLIC, true, false, false, clazz);
            new Param(Name.from("name"), Types.instance.getNullableString(), valueOfMethod);
        }

        @Override
        public Void visitFieldDecl(FieldDecl fieldDecl) {
            var mods = parseModifiers(fieldDecl.mods());
            var field = new Field(
                    fieldDecl.getName(),
                    DeferredType.instance,
                    mods.access,
                    mods.static_,
                    mods.deleted,
                    (Clazz) currentElement()
            );
            if (traceEntering)
                log.trace("Entering field {}", field.getQualifiedName());
            fieldDecl.setElement(field);
            return super.visitFieldDecl(fieldDecl);
        }

        @Override
        public Void visitMethodDecl(MethodDecl methodDecl) {
            var mods = parseModifiers(methodDecl.mods());
            var clazz = (Clazz) currentElement();
            var method = new Method(
                    methodDecl.name(),
                    mods.access,
                    mods.static_,
                    mods.abstract_ || clazz.isInterface(),
                    methodDecl.name() == Name.init(),
                    clazz
            );
            if (traceEntering)
                log.trace("Entering method {}", method.getQualName());
            methodDecl.setElement(method);
            enterElement(method);
            super.visitMethodDecl(methodDecl);
            exitElement();
            return null;
        }

        @Override
        public Void visitEnumConstDecl(EnumConstDecl enumConstDecl) {
            var clazz = (Clazz) currentElement();
            var type = clazz;
            super.visitEnumConstDecl(enumConstDecl);
            if (enumConstDecl.getDecl() != null) {
                var anonClass = enumConstDecl.getDecl().getElement();
                anonClass.setInterfaces(List.of(clazz));
                anonClass.setStatic(true);
                type = anonClass;
            }
            var ec = new EnumConst(
                    enumConstDecl.getName(),
                    clazz.getEnumConstants().size(),
                    clazz,
                    type
            );
            if (traceEntering)
                log.trace("Entering enum constant {}", ec.getQualifiedName());
            enumConstDecl.setElement(ec);
            return null;
        }

        @Override
        public Void visitParamDecl(ParamDecl paramDecl) {
            var exe = (Executable) currentElement();
            var param = new Param(
                    paramDecl.getName(),
                    PrimitiveType.NEVER,
                    exe
            );
            if (traceEntering) {
                log.trace("Entering parameter {}", param.getQualName());
            }
            paramDecl.setElement(param);
            return super.visitParamDecl(paramDecl);
        }

        @Override
        public Void visitLocalVarDecl(LocalVarDecl localVarDecl) {
            var exe = currentExecutable();
            var variable = new LocalVar(localVarDecl.getName(), DeferredType.instance, exe);
            localVarDecl.setElement(variable);
            if (traceEntering) {
                log.trace("Entering local variable {}", variable.getName());
            }
            return super.visitLocalVarDecl(localVarDecl);
        }

        @Override
        public Void visitTypeVariableDecl(TypeVariableDecl typeVariableDecl) {
            var typeVar = new TypeVar(
                    typeVariableDecl.getName(),
                    PrimitiveType.ANY,
                    (GenericDecl) currentElement()
            );
            typeVariableDecl.setElement(typeVar);
            if (traceEntering) {
                log.trace("Entering type variable {}", typeVar.getName());
            }
            return super.visitTypeVariableDecl(typeVariableDecl);
        }

        @Override
        public Void visitLambdaExpr(LambdaExpr lambdaExpr) {
            var lambda = new Lambda(Name.from("lambda" + nextLambdaId++));
            lambdaExpr.setElement(lambda);
            enterElement(lambda);
            super.visitLambdaExpr(lambdaExpr);
            exitElement();
            return null;
        }

        private Element currentElement() {
            return Objects.requireNonNull(elements.head(), "Not inside any element");
        }

        private Executable currentExecutable() {
            for (Element element : elements) {
                if (element instanceof Executable exe)
                    return exe;
            }
            throw new RuntimeException("Not inside any executable");
        }

        private Access parseAccess(List<Modifier> modifiers) {
            var tags = Utils.mapToSet(modifiers, Modifier::tag);
            if (tags.contains(ModifierTag.PUB)) return Access.PUBLIC;
            if (tags.contains(ModifierTag.PROT)) return Access.PROTECTED;
            if (tags.contains(ModifierTag.PRIV)) return Access.PRIVATE;
            return Access.PACKAGE;
        }

        private void enterElement(Element element) {
            elements = elements.prepend(element);
        }

        private void exitElement() {
            elements = elements.tail();
        }

    }

    private Modifiers parseModifiers(List<Modifier> mods) {
        var static_ = false;
        var abstract_ = false;
        var deleted = false;
        var readonly = false;
        var temp = false;
        var access = Access.PUBLIC;
        for (Modifier mod : mods) {
            switch (mod.tag()) {
                case PUB -> access = Access.PUBLIC;
                case PRIV ->  access = Access.PRIVATE;
                case PROT -> access = Access.PROTECTED;
                case STATIC -> static_ = true;
                case READONLY -> readonly = true;
                case ABSTRACT -> abstract_ = true;
                case DELETED ->  deleted = true;
                case TEMP ->  temp = true;
            }
        }
        return new Modifiers(static_, abstract_, deleted, readonly, temp, access);
    }


    private record Modifiers(
            boolean static_,
            boolean abstract_,
            boolean deleted,
            boolean readonly,
            boolean temp,
            Access access
    ) {

    }

}
