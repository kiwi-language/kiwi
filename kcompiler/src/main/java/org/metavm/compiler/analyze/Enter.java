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
                log.trace("Entering class {}", classDecl.name().value());
            var clazz = new Clazz(
                    ClassTag.valueOf(classDecl.tag().name()),
                    classDecl.name().value(),
                    parseAccess(classDecl.mods()),
                    (ClassScope) currentElement()
            );
            if (clazz.isEnum())
                enterEnumMethods(clazz);
            classDecl.setElement(clazz);
            enterElement(clazz);
            super.visitClassDecl(classDecl);
            exitElement();
            return null;
        }

        private void enterEnumMethods(Clazz clazz) {
            assert clazz.isEnum();
            new Method(SymName.from("values"), Access.PUBLIC, true, false, false, clazz);
            var valueOfMethod = new Method(SymName.from("valueOf"), Access.PUBLIC, true, false, false, clazz);
            new Parameter(SymName.from("name"), Types.instance.getNullableString(), valueOfMethod);
        }

        @Override
        public Void visitFieldDecl(FieldDecl fieldDecl) {
            var mods = parseModifiers(fieldDecl.mods());
            var field = new Field(
                    fieldDecl.name().value(),
                    PrimitiveType.NEVER,
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
                    methodDecl.name().value(),
                    mods.access,
                    mods.static_,
                    mods.abstract_ || clazz.isInterface(),
                    methodDecl.name().value() == SymName.init(),
                    clazz
            );
            if (traceEntering)
                log.trace("Entering method {}", method.getQualifiedName());
            methodDecl.setElement(method);
            enterElement(method);
            super.visitMethodDecl(methodDecl);
            exitElement();
            return null;
        }

        @Override
        public Void visitEnumConstantDecl(EnumConstantDecl enumConstantDecl) {
            var clazz = (Clazz) currentElement();

            var ec = new EnumConstant(
                    enumConstantDecl.getName().value(),
                    clazz.getEnumConstants().size(),
                    clazz
            );
            if (traceEntering)
                log.trace("Entering enum constant {}", ec.getQualifiedName());
            enumConstantDecl.setElement(ec);
            super.visitEnumConstantDecl(enumConstantDecl);
            return null;
        }

        @Override
        public Void visitParamDecl(ParamDecl paramDecl) {
            var exe = (Executable) currentElement();
            var param = new Parameter(
                    paramDecl.name().value(),
                    PrimitiveType.NEVER,
                    exe
            );
            if (traceEntering) {
                log.trace("Entering parameter {}", param.getQualifiedName());
            }
            paramDecl.setElement(param);
            return super.visitParamDecl(paramDecl);
        }

        @Override
        public Void visitLocalVarDecl(LocalVarDecl localVarDecl) {
            var exe = currentExecutable();
            var code = Objects.requireNonNull(exe.getCode());
            var variable = new LocalVariable(localVarDecl.name().value(), PrimitiveType.NEVER, exe);
            localVarDecl.setElement(variable);
            if (traceEntering) {
                log.trace("Entering local variable {}", variable.getName());
            }
            return super.visitLocalVarDecl(localVarDecl);
        }

        @Override
        public Void visitTypeVariableDecl(TypeVariableDecl typeVariableDecl) {
            var typeVar = new TypeVariable(
                    typeVariableDecl.getName().value(),
                    PrimitiveType.ANY,
                    (GenericDeclaration) currentElement()
            );
            typeVariableDecl.setElement(typeVar);
            if (traceEntering) {
                log.trace("Entering type variable {}", typeVar.getName());
            }
            return super.visitTypeVariableDecl(typeVariableDecl);
        }

        @Override
        public Void visitLambdaExpr(LambdaExpr lambdaExpr) {
            var encl = (Executable) currentElement();
            var func = switch (encl) {
                case Func f -> f;
                case Lambda l -> l.getFunction();
                default -> throw new RuntimeException("Invalid enclosing element of lambda: " + encl);
            };
            var lambda = new Lambda(SymName.from("lambda" + func.getLambdas().size()), encl, func);
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
            if (tags.contains(ModifierTag.PUBLIC)) return Access.PUBLIC;
            if (tags.contains(ModifierTag.PROTECTED)) return Access.PROTECTED;
            if (tags.contains(ModifierTag.PRIVATE)) return Access.PRIVATE;
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
        var access = Access.PUBLIC;
        for (Modifier mod : mods) {
            switch (mod.tag()) {
                case PUBLIC -> access = Access.PUBLIC;
                case PRIVATE ->  access = Access.PRIVATE;
                case PROTECTED -> access = Access.PROTECTED;
                case STATIC -> static_ = true;
                case READONLY -> readonly = true;
                case ABSTRACT -> abstract_ = true;
                case DELETED ->  deleted = true;
            }
        }
        return new Modifiers(static_, abstract_, deleted, readonly, access);
    }


    private record Modifiers(
            boolean static_,
            boolean abstract_,
            boolean deleted,
            boolean readonly,
            Access access
    ) {

    }

}
