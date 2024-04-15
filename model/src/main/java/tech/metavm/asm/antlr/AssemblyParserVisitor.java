// Generated from AssemblyParser.g4 by ANTLR 4.13.1
package tech.metavm.asm.antlr;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link AssemblyParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface AssemblyParserVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link AssemblyParser#compilationUnit}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCompilationUnit(AssemblyParser.CompilationUnitContext ctx);
	/**
	 * Visit a parse tree produced by {@link AssemblyParser#typeDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeDeclaration(AssemblyParser.TypeDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link AssemblyParser#classDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitClassDeclaration(AssemblyParser.ClassDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link AssemblyParser#classBody}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitClassBody(AssemblyParser.ClassBodyContext ctx);
	/**
	 * Visit a parse tree produced by {@link AssemblyParser#typeList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeList(AssemblyParser.TypeListContext ctx);
	/**
	 * Visit a parse tree produced by {@link AssemblyParser#classBodyDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitClassBodyDeclaration(AssemblyParser.ClassBodyDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link AssemblyParser#enumDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEnumDeclaration(AssemblyParser.EnumDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link AssemblyParser#enumConstants}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEnumConstants(AssemblyParser.EnumConstantsContext ctx);
	/**
	 * Visit a parse tree produced by {@link AssemblyParser#enumConstant}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEnumConstant(AssemblyParser.EnumConstantContext ctx);
	/**
	 * Visit a parse tree produced by {@link AssemblyParser#enumBodyDeclarations}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEnumBodyDeclarations(AssemblyParser.EnumBodyDeclarationsContext ctx);
	/**
	 * Visit a parse tree produced by {@link AssemblyParser#interfaceDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInterfaceDeclaration(AssemblyParser.InterfaceDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link AssemblyParser#interfaceBody}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInterfaceBody(AssemblyParser.InterfaceBodyContext ctx);
	/**
	 * Visit a parse tree produced by {@link AssemblyParser#interfaceBodyDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInterfaceBodyDeclaration(AssemblyParser.InterfaceBodyDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link AssemblyParser#interfaceMemberDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInterfaceMemberDeclaration(AssemblyParser.InterfaceMemberDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link AssemblyParser#interfaceMethodDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInterfaceMethodDeclaration(AssemblyParser.InterfaceMethodDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link AssemblyParser#interfaceMethodModifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInterfaceMethodModifier(AssemblyParser.InterfaceMethodModifierContext ctx);
	/**
	 * Visit a parse tree produced by {@link AssemblyParser#genericInterfaceMethodDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGenericInterfaceMethodDeclaration(AssemblyParser.GenericInterfaceMethodDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link AssemblyParser#interfaceCommonBodyDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInterfaceCommonBodyDeclaration(AssemblyParser.InterfaceCommonBodyDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link AssemblyParser#memberDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMemberDeclaration(AssemblyParser.MemberDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link AssemblyParser#fieldDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFieldDeclaration(AssemblyParser.FieldDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link AssemblyParser#methodDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMethodDeclaration(AssemblyParser.MethodDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link AssemblyParser#genericMethodDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGenericMethodDeclaration(AssemblyParser.GenericMethodDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link AssemblyParser#constructorDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConstructorDeclaration(AssemblyParser.ConstructorDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link AssemblyParser#genericConstructorDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGenericConstructorDeclaration(AssemblyParser.GenericConstructorDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link AssemblyParser#typeParameters}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeParameters(AssemblyParser.TypeParametersContext ctx);
	/**
	 * Visit a parse tree produced by {@link AssemblyParser#qualifiedNameList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitQualifiedNameList(AssemblyParser.QualifiedNameListContext ctx);
	/**
	 * Visit a parse tree produced by {@link AssemblyParser#qualifiedName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitQualifiedName(AssemblyParser.QualifiedNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link AssemblyParser#typeParameter}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeParameter(AssemblyParser.TypeParameterContext ctx);
	/**
	 * Visit a parse tree produced by {@link AssemblyParser#formalParameters}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFormalParameters(AssemblyParser.FormalParametersContext ctx);
	/**
	 * Visit a parse tree produced by {@link AssemblyParser#receiverParameter}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReceiverParameter(AssemblyParser.ReceiverParameterContext ctx);
	/**
	 * Visit a parse tree produced by {@link AssemblyParser#formalParameterList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFormalParameterList(AssemblyParser.FormalParameterListContext ctx);
	/**
	 * Visit a parse tree produced by {@link AssemblyParser#formalParameter}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFormalParameter(AssemblyParser.FormalParameterContext ctx);
	/**
	 * Visit a parse tree produced by {@link AssemblyParser#methodBody}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMethodBody(AssemblyParser.MethodBodyContext ctx);
	/**
	 * Visit a parse tree produced by {@link AssemblyParser#block}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBlock(AssemblyParser.BlockContext ctx);
	/**
	 * Visit a parse tree produced by {@link AssemblyParser#labeledStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLabeledStatement(AssemblyParser.LabeledStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link AssemblyParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStatement(AssemblyParser.StatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link AssemblyParser#forControl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitForControl(AssemblyParser.ForControlContext ctx);
	/**
	 * Visit a parse tree produced by {@link AssemblyParser#loopVariableDeclarators}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLoopVariableDeclarators(AssemblyParser.LoopVariableDeclaratorsContext ctx);
	/**
	 * Visit a parse tree produced by {@link AssemblyParser#loopVariableDeclarator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLoopVariableDeclarator(AssemblyParser.LoopVariableDeclaratorContext ctx);
	/**
	 * Visit a parse tree produced by {@link AssemblyParser#loopVariableUpdates}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLoopVariableUpdates(AssemblyParser.LoopVariableUpdatesContext ctx);
	/**
	 * Visit a parse tree produced by {@link AssemblyParser#loopVariableUpdate}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLoopVariableUpdate(AssemblyParser.LoopVariableUpdateContext ctx);
	/**
	 * Visit a parse tree produced by {@link AssemblyParser#qualifiedFieldName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitQualifiedFieldName(AssemblyParser.QualifiedFieldNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link AssemblyParser#creator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreator(AssemblyParser.CreatorContext ctx);
	/**
	 * Visit a parse tree produced by {@link AssemblyParser#arrayCreatorRest}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArrayCreatorRest(AssemblyParser.ArrayCreatorRestContext ctx);
	/**
	 * Visit a parse tree produced by {@link AssemblyParser#arrayInitializer}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArrayInitializer(AssemblyParser.ArrayInitializerContext ctx);
	/**
	 * Visit a parse tree produced by {@link AssemblyParser#variableInitializer}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVariableInitializer(AssemblyParser.VariableInitializerContext ctx);
	/**
	 * Visit a parse tree produced by {@link AssemblyParser#createdName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreatedName(AssemblyParser.CreatedNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link AssemblyParser#classCreatorRest}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitClassCreatorRest(AssemblyParser.ClassCreatorRestContext ctx);
	/**
	 * Visit a parse tree produced by {@link AssemblyParser#catchClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCatchClause(AssemblyParser.CatchClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link AssemblyParser#catchFields}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCatchFields(AssemblyParser.CatchFieldsContext ctx);
	/**
	 * Visit a parse tree produced by {@link AssemblyParser#catchField}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCatchField(AssemblyParser.CatchFieldContext ctx);
	/**
	 * Visit a parse tree produced by {@link AssemblyParser#catchValue}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCatchValue(AssemblyParser.CatchValueContext ctx);
	/**
	 * Visit a parse tree produced by {@link AssemblyParser#branchCase}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBranchCase(AssemblyParser.BranchCaseContext ctx);
	/**
	 * Visit a parse tree produced by {@link AssemblyParser#switchLabel}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSwitchLabel(AssemblyParser.SwitchLabelContext ctx);
	/**
	 * Visit a parse tree produced by {@link AssemblyParser#parExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParExpression(AssemblyParser.ParExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link AssemblyParser#expressionList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpressionList(AssemblyParser.ExpressionListContext ctx);
	/**
	 * Visit a parse tree produced by {@link AssemblyParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpression(AssemblyParser.ExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link AssemblyParser#primary}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrimary(AssemblyParser.PrimaryContext ctx);
	/**
	 * Visit a parse tree produced by {@link AssemblyParser#explicitGenericInvocation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExplicitGenericInvocation(AssemblyParser.ExplicitGenericInvocationContext ctx);
	/**
	 * Visit a parse tree produced by {@link AssemblyParser#explicitGenericInvocationSuffix}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExplicitGenericInvocationSuffix(AssemblyParser.ExplicitGenericInvocationSuffixContext ctx);
	/**
	 * Visit a parse tree produced by {@link AssemblyParser#superSuffix}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSuperSuffix(AssemblyParser.SuperSuffixContext ctx);
	/**
	 * Visit a parse tree produced by {@link AssemblyParser#arguments}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArguments(AssemblyParser.ArgumentsContext ctx);
	/**
	 * Visit a parse tree produced by {@link AssemblyParser#classType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitClassType(AssemblyParser.ClassTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link AssemblyParser#methodCall}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMethodCall(AssemblyParser.MethodCallContext ctx);
	/**
	 * Visit a parse tree produced by {@link AssemblyParser#literal}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLiteral(AssemblyParser.LiteralContext ctx);
	/**
	 * Visit a parse tree produced by {@link AssemblyParser#integerLiteral}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIntegerLiteral(AssemblyParser.IntegerLiteralContext ctx);
	/**
	 * Visit a parse tree produced by {@link AssemblyParser#floatLiteral}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFloatLiteral(AssemblyParser.FloatLiteralContext ctx);
	/**
	 * Visit a parse tree produced by {@link AssemblyParser#typeTypeOrVoid}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeTypeOrVoid(AssemblyParser.TypeTypeOrVoidContext ctx);
	/**
	 * Visit a parse tree produced by {@link AssemblyParser#typeType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeType(AssemblyParser.TypeTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link AssemblyParser#arrayKind}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArrayKind(AssemblyParser.ArrayKindContext ctx);
	/**
	 * Visit a parse tree produced by {@link AssemblyParser#classOrInterfaceType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitClassOrInterfaceType(AssemblyParser.ClassOrInterfaceTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link AssemblyParser#typeArguments}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeArguments(AssemblyParser.TypeArgumentsContext ctx);
	/**
	 * Visit a parse tree produced by {@link AssemblyParser#primitiveType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrimitiveType(AssemblyParser.PrimitiveTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link AssemblyParser#modifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitModifier(AssemblyParser.ModifierContext ctx);
	/**
	 * Visit a parse tree produced by {@link AssemblyParser#classOrInterfaceModifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitClassOrInterfaceModifier(AssemblyParser.ClassOrInterfaceModifierContext ctx);
}