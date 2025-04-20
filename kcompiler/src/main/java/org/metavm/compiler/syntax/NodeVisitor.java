package org.metavm.compiler.syntax;

public interface NodeVisitor<R> {
    
    R visitNode(Node node);
    
    R visitExpr(Expr expr);
    
    R visitStmt(Stmt stmt);
    
    R visitDecl(Decl<?> decl);
    
    R visitFunctionTypeNode(FunctionTypeNode functionTypeNode);

    R visitUncertainTypeNode(UncertainTypeNode uncertainTypeNode);

    R visitPostfixExpr(PostfixExpr postfixExpr);

    R visitPrefixExpr(PrefixExpr prefixExpr);

    R visitNewArrayExpr(NewArrayExpr newArrayExpr);

    R visitModifier(Modifier modifier);

    R visitMethodDecl(MethodDecl methodDecl);

    R visitArrayTypeNode(ArrayTypeNode arrayTypeNode);

    R visitAssignExpr(AssignExpr assignExpr);

    R visitBinaryExpr(BinaryExpr binaryExpr);

    R visitBlock(Block block);

    R visitCaseClause(CaseClause caseClause);

    R visitCastExpr(CastExpr castExpr);

    R visitClassDecl(ClassDecl classDecl);

    R visitClassInit(ClassInit classInit);

    R visitEmptyStmt(EmptyStmt emptyStmt);

    R visitEnumConstDecl(EnumConstDecl enumConstDecl);

    R visitExprStmt(ExprStmt exprStmt);

    R visitFieldDecl(FieldDecl fieldDecl);

    R visitFile(File file);

    R visitIfStmt(IfStmt ifStmt);

    R visitIndexExpr(IndexExpr indexExpr);

    R visitIsExpr(IsExpr isExpr);

    R visitIntersectionTypeNode(IntersectionTypeNode intersectionTypeNode);

    R visitLambdaExpr(LambdaExpr lambdaExpr);

    R visitLiteral(Literal literal);

    R visitParamDecl(ParamDecl paramDecl);

    R visitPrimitiveTypeNode(PrimitiveTypeNode primitiveTypeNode);

    R visitReturnStmt(RetStmt retStmt);

    R visitSelectorExpr(SelectorExpr selectorExpr);

    R visitSwitchStmt(SwitchStmt switchStmt);

    R visitThrowStmt(ThrowStmt throwStmt);

    R visitUnionTypeNode(UnionTypeNode unionTypeNode);

    R visitLocalVarDecl(LocalVarDecl localVarDecl);

    R visitWhileStmt(WhileStmt whileStmt);

    R visitDeclStmt(DeclStmt declStmt);

    R visitBlockStmt(BlockStmt blockStmt);

    R visitPackageDecl(PackageDecl packageDecl);

    R visitTypeVariableDecl(TypeVariableDecl typeVariableDecl);

    R visitClassTypeNode(ClassTypeNode classTypeNode);

    R visitTypeNode(TypeNode typeNode);

    R visitImport(Import imp);

    R visitIdent(Ident ident);

    R visitCall(Call call);

    R visitAnnotation(Annotation annotation);

    R visitCondExpr(CondExpr condExpr);

    R visitForeachStmt(ForeachStmt foreachStmt);

    R visitBreakStmt(BreakStmt breakStmt);

    R visitContinueStmt(ContinueStmt continueStmt);

    R visitLabeledStmt(LabeledStmt labeledStmt);

    R visitRangeExpr(RangeExpr rangeExpr);

    R visitDoWhileStmt(DoWhileStmt doWhileStmt);

    R visitAnonClassExpr(AnonClassExpr anonClassExpr);

    R visitTryStmt(TryStmt tryStmt);

    R visitCatcher(Catcher catcher);

    R visitTypeApply(TypeApply typeApply);
}
