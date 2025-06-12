package org.metavm.compiler.syntax;

public abstract class AbstractNodeVisitor<R> implements NodeVisitor<R> {

    public abstract R visitNode(Node node);

    public R visitExpr(Expr expr) {
        return visitNode(expr);
    }

    public R visitStmt(Stmt stmt) {
        return visitNode(stmt);
    }

    public R visitDecl(Decl<?> decl) {
        return visitNode(decl);
    }

    public R visitFunctionTypeNode(FunctionTypeNode functionTypeNode) {
        return visitTypeNode(functionTypeNode);
    }

    public R visitUncertainTypeNode(UncertainTypeNode uncertainTypeNode) {
        return visitTypeNode(uncertainTypeNode);
    }

    public R visitPostfixExpr(PostfixExpr postfixExpr) {
        return visitExpr(postfixExpr);
    }

    public R visitPrefixExpr(PrefixExpr prefixExpr) {
        return visitExpr(prefixExpr);
    }

    public R visitNewArrayExpr(NewArrayExpr newArrayExpr) {
        return visitExpr(newArrayExpr);
    }

    public R visitModifier(Modifier modifier) {
        return visitNode(modifier);
    }

    public R visitMethodDecl(MethodDecl methodDecl) {
        return visitDecl(methodDecl);
    }

    public R visitArrayTypeNode(ArrayTypeNode arrayTypeNode) {
        return visitTypeNode(arrayTypeNode);
    }

    public R visitAssignExpr(AssignExpr assignExpr) {
        return visitExpr(assignExpr);
    }

    public R visitBinaryExpr(BinaryExpr binaryExpr) {
        return visitExpr(binaryExpr);
    }

    public R visitBlock(Block block) {
        return visitNode(block);
    }

    public R visitCaseClause(CaseClause caseClause) {
        return visitNode(caseClause);
    }

    public R visitCastExpr(CastExpr castExpr) {
        return visitExpr(castExpr);
    }

    public R visitClassDecl(ClassDecl classDecl) {
        return visitDecl(classDecl);
    }

    public R visitClassInit(ClassInit classInit) {
        return visitDecl(classInit);
    }

    public R visitEmptyStmt(EmptyStmt emptyStmt) {
        return visitStmt(emptyStmt);
    }

    public R visitEnumConstDecl(EnumConstDecl enumConstDecl) {
        return visitDecl(enumConstDecl);
    }

    public R visitExprStmt(ExprStmt exprStmt) {
        return visitStmt(exprStmt);
    }

    public R visitFieldDecl(FieldDecl fieldDecl) {
        return visitDecl(fieldDecl);
    }

    public R visitFile(File file) {
        return visitNode(file);
    }

    public R visitIfStmt(IfStmt ifStmt) {
        return visitStmt(ifStmt);
    }

    public R visitIndexExpr(IndexExpr indexExpr) {
        return visitExpr(indexExpr);
    }

    public R visitIsExpr(IsExpr isExpr) {
        return visitExpr(isExpr);
    }

    public R visitIntersectionTypeNode(IntersectionTypeNode intersectionTypeNode) {
        return visitTypeNode(intersectionTypeNode);
    }

    public R visitLambdaExpr(LambdaExpr lambdaExpr) {
        return visitExpr(lambdaExpr);
    }

    public R visitLiteral(Literal literal) {
        return visitExpr(literal);
    }

    public R visitParamDecl(ParamDecl paramDecl) {
        return visitDecl(paramDecl);
    }

    public R visitPrimitiveTypeNode(PrimitiveTypeNode primitiveTypeNode) {
        return visitTypeNode(primitiveTypeNode);
    }

    public R visitReturnStmt(RetStmt retStmt) {
        return visitStmt(retStmt);
    }

    public R visitSelectorExpr(SelectorExpr selectorExpr) {
        return visitExpr(selectorExpr);
    }

    public R visitSwitchStmt(SwitchStmt switchStmt) {
        return visitStmt(switchStmt);
    }

    public R visitThrowStmt(ThrowStmt throwStmt) {
        return visitStmt(throwStmt);
    }

    public R visitUnionTypeNode(UnionTypeNode unionTypeNode) {
        return visitTypeNode(unionTypeNode);
    }

    public R visitLocalVarDecl(LocalVarDecl localVarDecl) {
        return visitDecl(localVarDecl);
    }

    public R visitWhileStmt(WhileStmt whileStmt) {
        return visitStmt(whileStmt);
    }

    public R visitDeclStmt(DeclStmt declStmt) {
        return visitStmt(declStmt);
    }

    public R visitBlockStmt(BlockStmt blockStmt) {
        return visitStmt(blockStmt);
    }

    public R visitPackageDecl(PackageDecl packageDecl) {
        return visitNode(packageDecl);
    }

    public R visitTypeVariableDecl(TypeVariableDecl typeVariableDecl) {
        return visitDecl(typeVariableDecl);
    }

    public R visitClassTypeNode(ClassTypeNode classTypeNode) {
        return visitTypeNode(classTypeNode);
    }

    public R visitTypeNode(TypeNode typeNode) {
        return visitNode(typeNode);
    }

    public R visitImport(Import imp) {
        return visitNode(imp);
    }

    public R visitIdent(Ident ident) {
        return visitExpr(ident);
    }

    @Override
    public R visitCall(Call call) {
        return visitExpr(call);
    }

    @Override
    public R visitAnnotation(Annotation annotation) {
        return visitNode(annotation);
    }

    @Override
    public R visitCondExpr(CondExpr condExpr) {
        return visitExpr(condExpr);
    }

    @Override
    public R visitForeachStmt(ForeachStmt foreachStmt) {
        return visitStmt(foreachStmt);
    }

    @Override
    public R visitBreakStmt(BreakStmt breakStmt) {
        return visitStmt(breakStmt);
    }

    @Override
    public R visitContinueStmt(ContinueStmt continueStmt) {
        return visitStmt(continueStmt);
    }

    @Override
    public R visitLabeledStmt(LabeledStmt labeledStmt) {
        return visitStmt(labeledStmt);
    }

    @Override
    public R visitRangeExpr(RangeExpr rangeExpr) {
        return visitExpr(rangeExpr);
    }

    @Override
    public R visitDoWhileStmt(DoWhileStmt doWhileStmt) {
        return visitStmt(doWhileStmt);
    }

    @Override
    public R visitAnonClassExpr(AnonClassExpr anonClassExpr) {
        return visitExpr(anonClassExpr);
    }

    @Override
    public R visitCatcher(Catcher catcher) {
        return visitNode(catcher);
    }

    @Override
    public R visitTryStmt(TryStmt tryStmt) {
        return visitStmt(tryStmt);
    }

    @Override
    public R visitTypeApply(TypeApply typeApply) {
        return visitExpr(typeApply);
    }

    @Override
    public R visitClassParamDecl(ClassParamDecl classParamDecl) {
        return visitDecl(classParamDecl);
    }

    @Override
    public R visitInit(Init init) {
        return visitNode(init);
    }

    @Override
    public R visitExtend(Extend extend) {
        return visitNode(extend);
    }

    @Override
    public R visitDelStmt(DelStmt delStmt) {
        return visitStmt(delStmt);
    }

    @Override
    public R visitErrorExpr(ErrorExpr errorExpr) {
        return visitExpr(errorExpr);
    }

    @Override
    public R visitAttribute(Annotation.Attribute attribute) {
        return visitNode(attribute);
    }

    @Override
    public R visitErrorType(ErrorTypeNode errorTypeNode) {
        return visitTypeNode(errorTypeNode);
    }
}
