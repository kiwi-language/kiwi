// Generated from KiwiParser.g4 by ANTLR 4.13.2
package org.metavm.compiler.antlr;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link KiwiParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface KiwiParserVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link KiwiParser#compilationUnit}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCompilationUnit(KiwiParser.CompilationUnitContext ctx);
	/**
	 * Visit a parse tree produced by {@link KiwiParser#packageDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPackageDeclaration(KiwiParser.PackageDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link KiwiParser#importDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitImportDeclaration(KiwiParser.ImportDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link KiwiParser#typeDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeDeclaration(KiwiParser.TypeDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link KiwiParser#classDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitClassDeclaration(KiwiParser.ClassDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link KiwiParser#classBody}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitClassBody(KiwiParser.ClassBodyContext ctx);
	/**
	 * Visit a parse tree produced by {@link KiwiParser#typeList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeList(KiwiParser.TypeListContext ctx);
	/**
	 * Visit a parse tree produced by {@link KiwiParser#classBodyDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitClassBodyDeclaration(KiwiParser.ClassBodyDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link KiwiParser#staticBlock}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStaticBlock(KiwiParser.StaticBlockContext ctx);
	/**
	 * Visit a parse tree produced by {@link KiwiParser#enumDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEnumDeclaration(KiwiParser.EnumDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link KiwiParser#enumConstants}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEnumConstants(KiwiParser.EnumConstantsContext ctx);
	/**
	 * Visit a parse tree produced by {@link KiwiParser#enumConstant}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEnumConstant(KiwiParser.EnumConstantContext ctx);
	/**
	 * Visit a parse tree produced by {@link KiwiParser#enumBodyDeclarations}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEnumBodyDeclarations(KiwiParser.EnumBodyDeclarationsContext ctx);
	/**
	 * Visit a parse tree produced by {@link KiwiParser#interfaceDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInterfaceDeclaration(KiwiParser.InterfaceDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link KiwiParser#interfaceBody}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInterfaceBody(KiwiParser.InterfaceBodyContext ctx);
	/**
	 * Visit a parse tree produced by {@link KiwiParser#interfaceBodyDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInterfaceBodyDeclaration(KiwiParser.InterfaceBodyDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link KiwiParser#interfaceMemberDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInterfaceMemberDeclaration(KiwiParser.InterfaceMemberDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link KiwiParser#interfaceMethodDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInterfaceMethodDeclaration(KiwiParser.InterfaceMethodDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link KiwiParser#interfaceMethodModifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInterfaceMethodModifier(KiwiParser.InterfaceMethodModifierContext ctx);
	/**
	 * Visit a parse tree produced by {@link KiwiParser#memberDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMemberDeclaration(KiwiParser.MemberDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link KiwiParser#fieldDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFieldDeclaration(KiwiParser.FieldDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link KiwiParser#methodDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMethodDeclaration(KiwiParser.MethodDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link KiwiParser#constructorDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConstructorDeclaration(KiwiParser.ConstructorDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link KiwiParser#typeParameters}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeParameters(KiwiParser.TypeParametersContext ctx);
	/**
	 * Visit a parse tree produced by {@link KiwiParser#qualifiedNameList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitQualifiedNameList(KiwiParser.QualifiedNameListContext ctx);
	/**
	 * Visit a parse tree produced by {@link KiwiParser#qualifiedName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitQualifiedName(KiwiParser.QualifiedNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link KiwiParser#typeParameter}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeParameter(KiwiParser.TypeParameterContext ctx);
	/**
	 * Visit a parse tree produced by {@link KiwiParser#formalParameters}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFormalParameters(KiwiParser.FormalParametersContext ctx);
	/**
	 * Visit a parse tree produced by {@link KiwiParser#formalParameterList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFormalParameterList(KiwiParser.FormalParameterListContext ctx);
	/**
	 * Visit a parse tree produced by {@link KiwiParser#formalParameter}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFormalParameter(KiwiParser.FormalParameterContext ctx);
	/**
	 * Visit a parse tree produced by {@link KiwiParser#methodBody}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMethodBody(KiwiParser.MethodBodyContext ctx);
	/**
	 * Visit a parse tree produced by {@link KiwiParser#block}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBlock(KiwiParser.BlockContext ctx);
	/**
	 * Visit a parse tree produced by {@link KiwiParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStatement(KiwiParser.StatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link KiwiParser#localVariableDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLocalVariableDeclaration(KiwiParser.LocalVariableDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link KiwiParser#forControl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitForControl(KiwiParser.ForControlContext ctx);
	/**
	 * Visit a parse tree produced by {@link KiwiParser#loopVariableDeclarators}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLoopVariableDeclarators(KiwiParser.LoopVariableDeclaratorsContext ctx);
	/**
	 * Visit a parse tree produced by {@link KiwiParser#loopVariableDeclarator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLoopVariableDeclarator(KiwiParser.LoopVariableDeclaratorContext ctx);
	/**
	 * Visit a parse tree produced by {@link KiwiParser#loopVariableUpdates}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLoopVariableUpdates(KiwiParser.LoopVariableUpdatesContext ctx);
	/**
	 * Visit a parse tree produced by {@link KiwiParser#loopVariableUpdate}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLoopVariableUpdate(KiwiParser.LoopVariableUpdateContext ctx);
	/**
	 * Visit a parse tree produced by {@link KiwiParser#newExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNewExpr(KiwiParser.NewExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link KiwiParser#newArray}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNewArray(KiwiParser.NewArrayContext ctx);
	/**
	 * Visit a parse tree produced by {@link KiwiParser#arrayInitializer}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArrayInitializer(KiwiParser.ArrayInitializerContext ctx);
	/**
	 * Visit a parse tree produced by {@link KiwiParser#variableInitializer}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVariableInitializer(KiwiParser.VariableInitializerContext ctx);
	/**
	 * Visit a parse tree produced by {@link KiwiParser#catchClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCatchClause(KiwiParser.CatchClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link KiwiParser#catchFields}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCatchFields(KiwiParser.CatchFieldsContext ctx);
	/**
	 * Visit a parse tree produced by {@link KiwiParser#catchField}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCatchField(KiwiParser.CatchFieldContext ctx);
	/**
	 * Visit a parse tree produced by {@link KiwiParser#catchValue}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCatchValue(KiwiParser.CatchValueContext ctx);
	/**
	 * Visit a parse tree produced by {@link KiwiParser#branchCase}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBranchCase(KiwiParser.BranchCaseContext ctx);
	/**
	 * Visit a parse tree produced by {@link KiwiParser#switchLabel}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSwitchLabel(KiwiParser.SwitchLabelContext ctx);
	/**
	 * Visit a parse tree produced by {@link KiwiParser#parExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParExpression(KiwiParser.ParExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link KiwiParser#expressionList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpressionList(KiwiParser.ExpressionListContext ctx);
	/**
	 * Visit a parse tree produced by {@link KiwiParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpression(KiwiParser.ExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link KiwiParser#assignment}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAssignment(KiwiParser.AssignmentContext ctx);
	/**
	 * Visit a parse tree produced by {@link KiwiParser#assignmentSuffix}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAssignmentSuffix(KiwiParser.AssignmentSuffixContext ctx);
	/**
	 * Visit a parse tree produced by {@link KiwiParser#ternary}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTernary(KiwiParser.TernaryContext ctx);
	/**
	 * Visit a parse tree produced by {@link KiwiParser#disjunction}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDisjunction(KiwiParser.DisjunctionContext ctx);
	/**
	 * Visit a parse tree produced by {@link KiwiParser#conjunction}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConjunction(KiwiParser.ConjunctionContext ctx);
	/**
	 * Visit a parse tree produced by {@link KiwiParser#bitor}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBitor(KiwiParser.BitorContext ctx);
	/**
	 * Visit a parse tree produced by {@link KiwiParser#bitand}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBitand(KiwiParser.BitandContext ctx);
	/**
	 * Visit a parse tree produced by {@link KiwiParser#bitxor}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBitxor(KiwiParser.BitxorContext ctx);
	/**
	 * Visit a parse tree produced by {@link KiwiParser#equality}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEquality(KiwiParser.EqualityContext ctx);
	/**
	 * Visit a parse tree produced by {@link KiwiParser#equalitySuffix}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEqualitySuffix(KiwiParser.EqualitySuffixContext ctx);
	/**
	 * Visit a parse tree produced by {@link KiwiParser#relational}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRelational(KiwiParser.RelationalContext ctx);
	/**
	 * Visit a parse tree produced by {@link KiwiParser#relationalSuffix}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRelationalSuffix(KiwiParser.RelationalSuffixContext ctx);
	/**
	 * Visit a parse tree produced by {@link KiwiParser#isExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIsExpr(KiwiParser.IsExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link KiwiParser#shift}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShift(KiwiParser.ShiftContext ctx);
	/**
	 * Visit a parse tree produced by {@link KiwiParser#shiftSuffix}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShiftSuffix(KiwiParser.ShiftSuffixContext ctx);
	/**
	 * Visit a parse tree produced by {@link KiwiParser#additive}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAdditive(KiwiParser.AdditiveContext ctx);
	/**
	 * Visit a parse tree produced by {@link KiwiParser#additiveSuffix}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAdditiveSuffix(KiwiParser.AdditiveSuffixContext ctx);
	/**
	 * Visit a parse tree produced by {@link KiwiParser#multiplicative}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMultiplicative(KiwiParser.MultiplicativeContext ctx);
	/**
	 * Visit a parse tree produced by {@link KiwiParser#multiplicativeSuffix}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMultiplicativeSuffix(KiwiParser.MultiplicativeSuffixContext ctx);
	/**
	 * Visit a parse tree produced by {@link KiwiParser#asExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAsExpr(KiwiParser.AsExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link KiwiParser#prefixExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrefixExpr(KiwiParser.PrefixExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link KiwiParser#prefixOp}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrefixOp(KiwiParser.PrefixOpContext ctx);
	/**
	 * Visit a parse tree produced by {@link KiwiParser#postfixExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPostfixExpr(KiwiParser.PostfixExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link KiwiParser#postfixSuffix}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPostfixSuffix(KiwiParser.PostfixSuffixContext ctx);
	/**
	 * Visit a parse tree produced by {@link KiwiParser#callSuffix}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCallSuffix(KiwiParser.CallSuffixContext ctx);
	/**
	 * Visit a parse tree produced by {@link KiwiParser#indexingSuffix}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIndexingSuffix(KiwiParser.IndexingSuffixContext ctx);
	/**
	 * Visit a parse tree produced by {@link KiwiParser#selectorSuffix}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelectorSuffix(KiwiParser.SelectorSuffixContext ctx);
	/**
	 * Visit a parse tree produced by {@link KiwiParser#primaryExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrimaryExpr(KiwiParser.PrimaryExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link KiwiParser#arguments}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArguments(KiwiParser.ArgumentsContext ctx);
	/**
	 * Visit a parse tree produced by {@link KiwiParser#identifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIdentifier(KiwiParser.IdentifierContext ctx);
	/**
	 * Visit a parse tree produced by {@link KiwiParser#methodCall}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMethodCall(KiwiParser.MethodCallContext ctx);
	/**
	 * Visit a parse tree produced by {@link KiwiParser#literal}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLiteral(KiwiParser.LiteralContext ctx);
	/**
	 * Visit a parse tree produced by {@link KiwiParser#integerLiteral}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIntegerLiteral(KiwiParser.IntegerLiteralContext ctx);
	/**
	 * Visit a parse tree produced by {@link KiwiParser#floatLiteral}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFloatLiteral(KiwiParser.FloatLiteralContext ctx);
	/**
	 * Visit a parse tree produced by {@link KiwiParser#typeOrVoid}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeOrVoid(KiwiParser.TypeOrVoidContext ctx);
	/**
	 * Visit a parse tree produced by {@link KiwiParser#type}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitType(KiwiParser.TypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link KiwiParser#arrayKind}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArrayKind(KiwiParser.ArrayKindContext ctx);
	/**
	 * Visit a parse tree produced by {@link KiwiParser#classOrInterfaceType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitClassOrInterfaceType(KiwiParser.ClassOrInterfaceTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link KiwiParser#typeArguments}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeArguments(KiwiParser.TypeArgumentsContext ctx);
	/**
	 * Visit a parse tree produced by {@link KiwiParser#primitiveType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrimitiveType(KiwiParser.PrimitiveTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link KiwiParser#modifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitModifier(KiwiParser.ModifierContext ctx);
	/**
	 * Visit a parse tree produced by {@link KiwiParser#classOrInterfaceModifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitClassOrInterfaceModifier(KiwiParser.ClassOrInterfaceModifierContext ctx);
	/**
	 * Visit a parse tree produced by {@link KiwiParser#lambdaExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLambdaExpression(KiwiParser.LambdaExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link KiwiParser#lambdaParameters}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLambdaParameters(KiwiParser.LambdaParametersContext ctx);
	/**
	 * Visit a parse tree produced by {@link KiwiParser#lambdaBody}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLambdaBody(KiwiParser.LambdaBodyContext ctx);
	/**
	 * Visit a parse tree produced by {@link KiwiParser#annotation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAnnotation(KiwiParser.AnnotationContext ctx);
	/**
	 * Visit a parse tree produced by {@link KiwiParser#elementValuePairs}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitElementValuePairs(KiwiParser.ElementValuePairsContext ctx);
	/**
	 * Visit a parse tree produced by {@link KiwiParser#elementValuePair}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitElementValuePair(KiwiParser.ElementValuePairContext ctx);
}