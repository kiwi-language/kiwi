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

    public R visitTypeApply(TypeApply typeApply) {
        return visitNode(typeApply);
    }

    public R visitNewExpr(NewExpr newExpr) {
        return visitExpr(newExpr);
    }

    public R visitNewArrayExpr(NewArrayExpr newArrayExpr) {
        return visitExpr(newArrayExpr);
    }

    public R visitName(Name name) {
        return visitNode(name);
    }

    public R visitIdent(Ident ident) {
        return visitName(ident);
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

    public R visitEnumConstantDecl(EnumConstantDecl enumConstantDecl) {
        return visitDecl(enumConstantDecl);
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

    public R visitReturnStmt(ReturnStmt returnStmt) {
        return visitStmt(returnStmt);
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

    public R visitRefExpr(RefExpr refExpr) {
        return visitExpr(refExpr);
    }

    public R visitQualifiedName(QualifiedName qualifiedName) {
        return visitName(qualifiedName);
    }

    @Override
    public R visitCallExpr(CallExpr callExpr) {
        return visitExpr(callExpr);
    }

    @Override
    public R visitAnnotation(Annotation annotation) {
        return visitNode(annotation);
    }

    @Override
    public R visitCondExpr(CondExpr condExpr) {
        return visitExpr(condExpr);
    }
}
