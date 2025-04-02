package org.metavm.compiler.analyze;

import lombok.extern.slf4j.Slf4j;
import org.metavm.compiler.element.*;
import org.metavm.compiler.syntax.*;
import org.metavm.compiler.type.ClassType;
import org.metavm.compiler.type.PrimitiveType;
import org.metavm.compiler.type.Types;
import org.metavm.compiler.util.List;
import org.metavm.compiler.util.Traces;

import static java.util.Objects.requireNonNull;
import static org.metavm.compiler.syntax.NodeMaker.*;

@Slf4j
public class Lower extends AbstractNodeVisitor<Node> {

    private final Project project;
    private final Env env = new Env();

    public Lower(Project project) {
        this.project = project;
    }

    @Override
    public Node visitClassDecl(ClassDecl classDecl) {
        var clazz = classDecl.getElement();
        var members = List.<Node>builder();
        var primaryInits = List.<MethodDecl>nil();
        var initializedFields = List.<FieldDecl>of();
        var classInits = List.<ClassInit>builder();
        for (Node member : classDecl.getMembers()) {
            if (member instanceof ClassInit classInit)
                classInits.append(classInit);
            else
                members.append(member);
            if (member instanceof FieldDecl fieldDecl && fieldDecl.getInitial() != null)
                initializedFields = initializedFields.prepend(fieldDecl);
            if (member instanceof MethodDecl methodDecl && methodDecl.isInit()) {
                if (methodDecl.isPrimaryInit())
                    primaryInits = primaryInits.prepend(methodDecl);
            }
        }
        moveFieldInitializers(primaryInits, initializedFields, members);
        if(classInits.nonEmpty()) {
            var cinit = new Method(
                    NameTable.instance.cinit,
                    Access.PUBLIC,
                    true,
                    false,
                    false,
                    clazz
            );
            var cinitDecl = methodDecl(cinit, classInits.build().map(b -> new BlockStmt(b.block())));
            members.append(cinitDecl);
        }
        classDecl.setMembers(members.build());
        if (clazz.isEnum())
            lowerEnum(classDecl);
        try (var ignored = env.enterScope(classDecl)) {
            return super.visitClassDecl(classDecl);
        }
    }

    private void moveFieldInitializers(List<MethodDecl> primaryInits, List<FieldDecl> initializedFields, List.Builder<Node> members) {
        for (FieldDecl fieldDecl : initializedFields) {
            createFieldInit(fieldDecl, members);
        }
        for (var methodDecl : primaryInits) {
            var body = requireNonNull(methodDecl.body());
            var stmts = body.getStmts();
            Stmt head = null;
            if (stmts.nonEmpty() && isSuperCall(stmts.head())) {
                head = stmts.head();
                stmts = stmts.tail();
            }
            for (FieldDecl fieldDecl : initializedFields) {
                if (!fieldDecl.getElement().isStatic()) {
                    var initializer = requireNonNull(fieldDecl.getElement().getInitializer());
                    var field = fieldDecl.getElement();
                    var stmt = new ExprStmt(
                            makeAssignExpr(
                                    ref(field),
                                    callExpr(
                                            ref(initializer),
                                            List.nil()
                                    )
                            )
                    );
                    stmts = stmts.prepend(stmt);
                }
            }
            if (head != null)
                stmts = stmts.prepend(head);
            body.setStmts(stmts);
        }
    }

    private void createFieldInit(FieldDecl fieldDecl, List.Builder<Node> members) {
        var field = fieldDecl.getElement();
        if (isIndexField(fieldDecl))
            lowerIndexInit(fieldDecl, members);
        var clazz = field.getDeclClass();
        var table = clazz.getTable();
        var namePrefix = "__" + field.getName() + "__";
        var name = Name.from(namePrefix);
        for(var i = 0; table.lookupFirst(name) != null ; i++) {
            name = Name.from(namePrefix + i);
        }
        var method = new Method(
                name,
                Access.PRIVATE,
                field.isStatic(),
                false,
                false,
                clazz
        );
        method.setRetType(field.getType());
        clazz.onMethodAdded(method);
        field.setInitializer(method);
        var methodDecl = methodDecl(method, List.of(
                new RetStmt(requireNonNull(fieldDecl.getInitial()))
        ));
        members.append(methodDecl);
        fieldDecl.setInitial(null);
    }

    private boolean isIndexField(FieldDecl fieldDecl) {
        return fieldDecl.getElement().isStatic()
                && fieldDecl.getElement().getType() instanceof ClassType ct
                && ct.getName() == NameTable.instance.index
                && fieldDecl.getInitial() instanceof Call call
                && call.getElement() instanceof MethodRef methodRef && methodRef.isInit()
                && Nodes.getRefName(call.getFunc()) == NameTable.instance.index;
    }

    private boolean isSuperCall(Stmt stmt) {
        if (stmt instanceof ExprStmt exprStmt && exprStmt.expr() instanceof Call call)
            return call.getFunc() instanceof Ident ident && ident.getName() == Name.super_();
        else
            return false;
    }

    private void lowerEnum(ClassDecl classDecl) {
        var clazz = classDecl.getElement();
        if (Traces.traceLower)
            log.trace("Transforming enum class: {}", clazz.getName());
        classDecl.setExtends(
                project.getRootPackage().subPackage("java").subPackage("lang")
                        .getClass("Enum").getInst(null, List.of(clazz))
                        .makeNode()
        );
        var newMembers = List.builder(classDecl.getMembers());
        for (var ecd : classDecl.enumConstants()) {
            var initDecl = makeEnumConstInit(clazz, ecd);
            newMembers.append(initDecl);
        }
        newMembers.append(createValuesMethod(clazz));
        newMembers.append(createValueOfMethod(clazz));
        classDecl.setMembers(newMembers.build());
    }

    private MethodDecl createValuesMethod(Clazz clazz) {
        assert clazz.isEnum();
        return methodDecl(
                clazz.getMethod(NameTable.instance.values, List.nil()),
                List.of(
                        new RetStmt(
                                makeNewArrayExpr(
                                        clazz,
                                        clazz.getEnumConstants().map(NodeMaker::ref)
                                )
                        )
                )
        );
    }

    private MethodDecl createValueOfMethod(Clazz clazz) {
        assert clazz.isEnum();
        var enumValueOf = project.getRootPackage().getFunction(Name.from("enumValueOf"));
        var valuesMethod = clazz.getMethod(NameTable.instance.values, List.nil());
        var method = clazz.getMethod(NameTable.instance.valueOf, List.of(Types.instance.getNullableString()));
        var nameParam = method.getParams().getFirst();
        return methodDecl(
                method,
                List.of(
                        new RetStmt(
                                callExpr(
                                        ref(enumValueOf.getInst(List.of(clazz))),
                                        List.of(
                                                callExpr(
                                                        ref(valuesMethod),
                                                        List.nil()
                                                ),
                                                ref(nameParam)
                                        )
                                )
                        )
                )
        );
    }

    private MethodDecl makeEnumConstInit(Clazz clazz, EnumConstDecl ecd) {
        var ec = ecd.getElement();
        var initName = Name.from("__init_" + ec.getName().toString() + "__");
        if (Traces.traceLower)
            log.trace("Creating enum constant initializer: {}", initName);
        var ecInit = new Method(
                initName,
                Access.PRIVATE,
                true,
                false,
                false,
                clazz
        );
        ecInit.setRetType(clazz);
        var stmts = List.<Stmt>builder();
        var init = ecd.getInit();
        var newExpr = makeNewExpr(
                init,
                ecd.getArguments()
                        .prepend(new Literal(ec.getOrdinal()))
                        .prepend(new Literal(ec.getName().toString()))
        );
        newExpr.setElement(init);
        newExpr.setType(clazz);
        stmts.append(new RetStmt(newExpr));
        var ecInitDecl = methodDecl(
                ecInit,
                stmts.build()
        );
        ecInitDecl.setElement(ecInit);
        ec.setInitializer(ecInit);
        return ecInitDecl;
    }

    public Node visitNode(Node node) {
        node.forEachChild(c -> c.accept(this));
        return node;
    }

    @Override
    public Node visitMethodDecl(MethodDecl methodDecl) {
        var method = methodDecl.getElement();
        var clazz = method.getDeclClass();
        if (method.isInit()) {
            if (clazz.isEnum() || clazz.getSuper() != null && clazz.getSuper().isEnum())
                lowerEnumInit(methodDecl);
            else
                lowerInit(methodDecl);
        }
        return super.visitMethodDecl(methodDecl);
    }

    private void lowerInit(MethodDecl methodDecl) {
        var method = methodDecl.getElement();
        var clazz = method.getDeclClass();
        var superType = clazz.getSuper();
        if (superType != null) {
            var stmts = requireNonNull(methodDecl.body()).getStmts();
            if (stmts.isEmpty() || (!Nodes.isSelfInitCall(stmts.head()) && !Nodes.isSuperInitCall(stmts.head()))) {
                var superInit = (MethodRef) superType.getTable().lookupFirst(
                        Name.init(), e -> e instanceof MethodRef m && m.getParamTypes().isEmpty()
                );
                if (superInit == null) {
                    throw new AnalysisException("Missing super call in init: " + method.getQualName());
                }
                methodDecl.body().setStmts(
                        stmts.prepend(
                                exprStmt(
                                        callExpr(
                                                ref(superInit), List.nil()
                                        )
                                )
                        )
                );
            }
        }
    }

    private void lowerEnumInit(MethodDecl methodDecl) {
        var method = methodDecl.getElement();
        var clazz = method.getDeclClass();
        if (Traces.traceLower)
            log.trace("Transforming initializer of enum class {}", clazz.getQualName());
        var prevParams = method.getParams();
        var nameParam = paramDecl(
                NameTable.instance.enumName,
                Types.instance.getStringType(),
                method
        );
        var ordinalParam = paramDecl(
                NameTable.instance.enumOrdinal,
                PrimitiveType.INT,
                method
        );
        methodDecl.setParams(
                methodDecl.getParams().prepend(ordinalParam).prepend(nameParam)
        );
        method.setParams(
                prevParams.prepend(ordinalParam.getElement())
                        .prepend(nameParam.getElement())
        );
        var body = requireNonNull(methodDecl.body());
        var superType = requireNonNull(clazz.getSuper());
        if (superType.isEnum()) {
            var expr = (Call) ((ExprStmt) body.getStmts().getFirst()).expr();
            expr.setArguments(
                    expr.getArguments().prepend(ref(ordinalParam.getElement()))
                            .prepend(ref(nameParam.getElement()))
            );
        }
        else {
            var enumInit = requireNonNull(superType.getTable().lookupFirst(Name.init()));
            body.setStmts(body.getStmts().prepend(
                    new ExprStmt(
                            callExpr(
                                    NodeMaker.ref(enumInit),
                                    List.of(
                                            NodeMaker.ref(nameParam.getElement()),
                                            NodeMaker.ref(ordinalParam.getElement())
                                    )
                            )
                    )
            ));
        }
    }

    private void lowerIndexInit(FieldDecl fieldDecl, List.Builder<Node> members) {
        var field = fieldDecl.getElement();
        var callExpr = (Call) requireNonNull(fieldDecl.getInitial());
        var indexType = (ClassType) field.getType();
        var init = indexType.getTable().lookupFirst(NameTable.instance.init, e -> e instanceof MethodRef m && m.getParamTypes().size() == 3);
        callExpr.setElement(init);
        callExpr.getFunc().setElement(init);
        var args = List.<Expr>builder();
        args.append(NodeMaker.literal(field.getName().toString()));
        args.append(callExpr.getArguments().head());
        var l = (LambdaExpr) callExpr.getArguments().tail().head();
        var m = new Method(
                field.getName().concat("$compute"),
                Access.PRIVATE,
                true,
                false,
                false,
                field.getDeclClass()
        );
        var p = l.getElement().getParams().head();
        var p1 = new Param(p.getName(), p.getType(), m);
        m.setRetType(l.getElement().getRetType());
        l.body().accept(new StructuralNodeVisitor() {

            @Override
            public Void visitIdent(Ident ident) {
                if (ident.getElement() == p)
                    ident.setElement(p1);
                return null;
            }
        });
        members.append(
                NodeMaker.methodDecl(
                    m,
                    l.body() instanceof Expr expr ? List.of(NodeMaker.retStmt(expr)) : ((Block) l.body()).getStmts()
                )
        );
        var l2 = new Lambda(Name.from("lambda0"));
        var p2 = new Param(p.getName(), p.getType(), l2);
        l2.setRetType(l.getElement().getRetType());
        args.append(NodeMaker.lambdaExpr(l2, NodeMaker.callExpr(
                NodeMaker.ref(m),
                List.of(NodeMaker.ref(p2))
        )));
        callExpr.setArguments(args.build());
    }

}
