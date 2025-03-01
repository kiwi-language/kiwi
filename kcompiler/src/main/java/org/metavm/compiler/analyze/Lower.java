package org.metavm.compiler.analyze;

import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.atn.BlockStartState;
import org.metavm.compiler.element.*;
import org.metavm.compiler.syntax.*;
import org.metavm.compiler.type.PrimitiveType;
import org.metavm.compiler.type.Types;
import org.metavm.compiler.util.List;
import org.metavm.compiler.util.Traces;

import static java.util.Objects.requireNonNull;

@Slf4j
public class Lower extends StructuralNodeVisitor {

    private final Project project;

    public Lower(Project project) {
        this.project = project;
    }

    @Override
    public Void visitClassDecl(ClassDecl classDecl) {
        var clazz = classDecl.getElement();
        super.visitClassDecl(classDecl);
        if (clazz.isEnum())
            lowerEnum(classDecl);
        var newMembers = List.<Node>builder();
        var classInits = List.<ClassInit>builder();
        for (Node member : classDecl.getMembers()) {
            if (member instanceof ClassInit classInit)
                classInits.append(classInit);
            else
                newMembers.append(member);
        }
        if(classInits.nonEmpty()) {
            var cinit = new Method(
                    SymNameTable.instance.cinit,
                    Access.PUBLIC,
                    true,
                    false,
                    false,
                    clazz
            );
            var cinitDecl = NodeMaker.makeMethodDecl(cinit, classInits.build().map(b -> new BlockStmt(b.block())));
            newMembers.append(cinitDecl);
        }
        classDecl.setMembers(newMembers.build());
        return null;
    }

    private void lowerEnum(ClassDecl classDecl) {
        var clazz = classDecl.getElement();
        if (Traces.traceLower)
            log.trace("Transforming enum class: {}", clazz.getName());
        classDecl.setExtends(
                project.getRootPackage().subPackage("java").subPackage("lang")
                        .getClass("Enum").getType(null, List.of(clazz.getType()))
                        .makeNode()
        );
        var newMembers = List.builder(classDecl.getMembers());
        for (var ecd : classDecl.enumConstants()) {
            var initDecl = makeEnumConstantInit(clazz, ecd);
            newMembers.append(initDecl);
        }
        newMembers.append(createEnumValuesMethod(clazz));
        newMembers.append(createValueOfMethod(clazz));
        classDecl.setMembers(newMembers.build());
    }

    private MethodDecl createEnumValuesMethod(Clazz clazz) {
        assert clazz.isEnum();
        return NodeMaker.makeMethodDecl(
                clazz.getMethod(SymNameTable.instance.values, List.nil()),
                List.of(
                        new ReturnStmt(
                                NodeMaker.makeNewArrayExpr(
                                        clazz.getType(),
                                        clazz.getEnumConstants().map(NodeMaker::makeStaticFieldRef)
                                )
                        )
                )
        );
    }

    private MethodDecl createValueOfMethod(Clazz clazz) {
        assert clazz.isEnum();
        var enumValueOf = project.getRootPackage().getFunction(SymName.from("enumValueOf"));
        var valuesMethod = clazz.getType().getMethod(SymNameTable.instance.values, List.nil());
        var method = clazz.getMethod(SymNameTable.instance.valueOf, List.of(Types.instance.getNullableString()));
        var nameParam = method.getParameters().getFirst();
        return NodeMaker.makeMethodDecl(
                method,
                List.of(
                        new ReturnStmt(
                                NodeMaker.makeCallExpr(
                                        NodeMaker.makeFuncRef(enumValueOf.getInstance(List.of(clazz.getType()))),
                                        List.of(clazz.getType()),
                                        List.of(
                                                NodeMaker.makeCallExpr(
                                                        NodeMaker.makeStaticMethodRef(valuesMethod),
                                                        List.nil(),
                                                        List.nil()
                                                ),
                                                NodeMaker.makeParamRef(nameParam)
                                        )
                                )
                        )
                )
        );
    }

    private MethodDecl makeEnumConstantInit(Clazz clazz, EnumConstantDecl ecd) {
        var ec = ecd.getElement();
        var initName = SymName.from("__init_" + ec.getName().toString() + "__");
        if (Traces.traceLower)
            log.trace("Creating enum constant initializer: {}", initName);
        var init = new Method(
                initName,
                Access.PRIVATE,
                true,
                false,
                false,
                clazz
        );
        init.setReturnType(clazz.getType());
        var stmts = List.<Stmt>builder();
        var constructor = (MethodInst) requireNonNull(clazz.getType().getTable().lookupFirst(
                SymName.init(), e -> e instanceof MethodInst
        ));
        var newExpr = NodeMaker.makeNewExpr(
                constructor,
                ecd.getArguments()
                        .prepend(new Literal(ec.getOrdinal()))
                        .prepend(new Literal(ec.getName().toString()))
        );
        newExpr.setElement(constructor);
        newExpr.setType(clazz.getType());
        stmts.append(new ReturnStmt(newExpr));
        var initDecl = new MethodDecl(
                List.of(
                        new Modifier(ModifierTag.PRIVATE),
                        new Modifier(ModifierTag.STATIC)
                ),
                List.nil(),
                new Ident(initName),
                List.nil(),
                new ClassTypeNode(new Ident(clazz.getName())),
                new Block(stmts.build())
        );
        initDecl.setElement(init);
        ec.setInitializer(init);
        return initDecl;
    }

    @Override
    public Void visitMethodDecl(MethodDecl methodDecl) {
        var method = methodDecl.getElement();
        var clazz = method.getDeclaringClass();
        if (method.isConstructor() && clazz.isEnum()) {
            if (Traces.traceLower)
                log.trace("Transforming initializer of enum class {}", clazz.getQualifiedName());
            var prevParams = method.getParameters();
            var nameParam = NodeMaker.makeParamDecl(
                    SymName.from("$enum$name"),
                    PrimitiveType.STRING,
                    method
            );
            var ordinalParam = NodeMaker.makeParamDecl(
                    SymName.from("$enum$ordinal"),
                    PrimitiveType.INT,
                    method
            );
            methodDecl.setParams(
                    methodDecl.getParams().prepend(ordinalParam).prepend(nameParam)
            );
            method.setParameters(
                    prevParams.prepend(ordinalParam.getElement())
                            .prepend(nameParam.getElement())
            );
            var body = requireNonNull(methodDecl.body());

            var enumType = project.getRootPackage().subPackage("java")
                    .subPackage("lang").getClass("Enum").getType(null, List.of(clazz.getType()));
            var enumInit = requireNonNull(enumType.getMethods().find(MethodInst::isConstructor));
            var superExpr = NodeMaker.makeRefExpr(
                    SymName.super_(),
                    enumInit
            );

            body.setStmts(body.getStmts().prepend(
                    new ExprStmt(
                        NodeMaker.makeCallExpr(
                                superExpr,
                                List.nil(),
                                List.of(
                                        NodeMaker.makeRefExpr(
                                                nameParam.name().value(),
                                                nameParam.getElement()
                                        ),
                                        NodeMaker.makeRefExpr(
                                                ordinalParam.name().value(),
                                                ordinalParam.getElement()
                                        )
                                )
                        )
                    )
            ));

        }
        return super.visitMethodDecl(methodDecl);
    }

}
