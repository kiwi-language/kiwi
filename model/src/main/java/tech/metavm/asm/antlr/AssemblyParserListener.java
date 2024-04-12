// Generated from AssemblyParser.g4 by ANTLR 4.13.1
package tech.metavm.asm.antlr;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link AssemblyParser}.
 */
public interface AssemblyParserListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link AssemblyParser#compilationUnit}.
	 * @param ctx the parse tree
	 */
	void enterCompilationUnit(AssemblyParser.CompilationUnitContext ctx);
	/**
	 * Exit a parse tree produced by {@link AssemblyParser#compilationUnit}.
	 * @param ctx the parse tree
	 */
	void exitCompilationUnit(AssemblyParser.CompilationUnitContext ctx);
	/**
	 * Enter a parse tree produced by {@link AssemblyParser#typeDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterTypeDeclaration(AssemblyParser.TypeDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link AssemblyParser#typeDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitTypeDeclaration(AssemblyParser.TypeDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link AssemblyParser#classDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterClassDeclaration(AssemblyParser.ClassDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link AssemblyParser#classDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitClassDeclaration(AssemblyParser.ClassDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link AssemblyParser#classBody}.
	 * @param ctx the parse tree
	 */
	void enterClassBody(AssemblyParser.ClassBodyContext ctx);
	/**
	 * Exit a parse tree produced by {@link AssemblyParser#classBody}.
	 * @param ctx the parse tree
	 */
	void exitClassBody(AssemblyParser.ClassBodyContext ctx);
	/**
	 * Enter a parse tree produced by {@link AssemblyParser#typeList}.
	 * @param ctx the parse tree
	 */
	void enterTypeList(AssemblyParser.TypeListContext ctx);
	/**
	 * Exit a parse tree produced by {@link AssemblyParser#typeList}.
	 * @param ctx the parse tree
	 */
	void exitTypeList(AssemblyParser.TypeListContext ctx);
	/**
	 * Enter a parse tree produced by {@link AssemblyParser#classBodyDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterClassBodyDeclaration(AssemblyParser.ClassBodyDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link AssemblyParser#classBodyDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitClassBodyDeclaration(AssemblyParser.ClassBodyDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link AssemblyParser#enumDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterEnumDeclaration(AssemblyParser.EnumDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link AssemblyParser#enumDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitEnumDeclaration(AssemblyParser.EnumDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link AssemblyParser#enumConstants}.
	 * @param ctx the parse tree
	 */
	void enterEnumConstants(AssemblyParser.EnumConstantsContext ctx);
	/**
	 * Exit a parse tree produced by {@link AssemblyParser#enumConstants}.
	 * @param ctx the parse tree
	 */
	void exitEnumConstants(AssemblyParser.EnumConstantsContext ctx);
	/**
	 * Enter a parse tree produced by {@link AssemblyParser#enumConstant}.
	 * @param ctx the parse tree
	 */
	void enterEnumConstant(AssemblyParser.EnumConstantContext ctx);
	/**
	 * Exit a parse tree produced by {@link AssemblyParser#enumConstant}.
	 * @param ctx the parse tree
	 */
	void exitEnumConstant(AssemblyParser.EnumConstantContext ctx);
	/**
	 * Enter a parse tree produced by {@link AssemblyParser#enumBodyDeclarations}.
	 * @param ctx the parse tree
	 */
	void enterEnumBodyDeclarations(AssemblyParser.EnumBodyDeclarationsContext ctx);
	/**
	 * Exit a parse tree produced by {@link AssemblyParser#enumBodyDeclarations}.
	 * @param ctx the parse tree
	 */
	void exitEnumBodyDeclarations(AssemblyParser.EnumBodyDeclarationsContext ctx);
	/**
	 * Enter a parse tree produced by {@link AssemblyParser#interfaceDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterInterfaceDeclaration(AssemblyParser.InterfaceDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link AssemblyParser#interfaceDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitInterfaceDeclaration(AssemblyParser.InterfaceDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link AssemblyParser#interfaceBody}.
	 * @param ctx the parse tree
	 */
	void enterInterfaceBody(AssemblyParser.InterfaceBodyContext ctx);
	/**
	 * Exit a parse tree produced by {@link AssemblyParser#interfaceBody}.
	 * @param ctx the parse tree
	 */
	void exitInterfaceBody(AssemblyParser.InterfaceBodyContext ctx);
	/**
	 * Enter a parse tree produced by {@link AssemblyParser#interfaceBodyDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterInterfaceBodyDeclaration(AssemblyParser.InterfaceBodyDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link AssemblyParser#interfaceBodyDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitInterfaceBodyDeclaration(AssemblyParser.InterfaceBodyDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link AssemblyParser#interfaceMemberDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterInterfaceMemberDeclaration(AssemblyParser.InterfaceMemberDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link AssemblyParser#interfaceMemberDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitInterfaceMemberDeclaration(AssemblyParser.InterfaceMemberDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link AssemblyParser#interfaceMethodDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterInterfaceMethodDeclaration(AssemblyParser.InterfaceMethodDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link AssemblyParser#interfaceMethodDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitInterfaceMethodDeclaration(AssemblyParser.InterfaceMethodDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link AssemblyParser#interfaceMethodModifier}.
	 * @param ctx the parse tree
	 */
	void enterInterfaceMethodModifier(AssemblyParser.InterfaceMethodModifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link AssemblyParser#interfaceMethodModifier}.
	 * @param ctx the parse tree
	 */
	void exitInterfaceMethodModifier(AssemblyParser.InterfaceMethodModifierContext ctx);
	/**
	 * Enter a parse tree produced by {@link AssemblyParser#genericInterfaceMethodDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterGenericInterfaceMethodDeclaration(AssemblyParser.GenericInterfaceMethodDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link AssemblyParser#genericInterfaceMethodDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitGenericInterfaceMethodDeclaration(AssemblyParser.GenericInterfaceMethodDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link AssemblyParser#interfaceCommonBodyDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterInterfaceCommonBodyDeclaration(AssemblyParser.InterfaceCommonBodyDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link AssemblyParser#interfaceCommonBodyDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitInterfaceCommonBodyDeclaration(AssemblyParser.InterfaceCommonBodyDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link AssemblyParser#memberDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterMemberDeclaration(AssemblyParser.MemberDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link AssemblyParser#memberDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitMemberDeclaration(AssemblyParser.MemberDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link AssemblyParser#fieldDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterFieldDeclaration(AssemblyParser.FieldDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link AssemblyParser#fieldDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitFieldDeclaration(AssemblyParser.FieldDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link AssemblyParser#methodDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterMethodDeclaration(AssemblyParser.MethodDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link AssemblyParser#methodDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitMethodDeclaration(AssemblyParser.MethodDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link AssemblyParser#genericMethodDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterGenericMethodDeclaration(AssemblyParser.GenericMethodDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link AssemblyParser#genericMethodDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitGenericMethodDeclaration(AssemblyParser.GenericMethodDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link AssemblyParser#constructorDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterConstructorDeclaration(AssemblyParser.ConstructorDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link AssemblyParser#constructorDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitConstructorDeclaration(AssemblyParser.ConstructorDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link AssemblyParser#genericConstructorDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterGenericConstructorDeclaration(AssemblyParser.GenericConstructorDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link AssemblyParser#genericConstructorDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitGenericConstructorDeclaration(AssemblyParser.GenericConstructorDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link AssemblyParser#typeParameters}.
	 * @param ctx the parse tree
	 */
	void enterTypeParameters(AssemblyParser.TypeParametersContext ctx);
	/**
	 * Exit a parse tree produced by {@link AssemblyParser#typeParameters}.
	 * @param ctx the parse tree
	 */
	void exitTypeParameters(AssemblyParser.TypeParametersContext ctx);
	/**
	 * Enter a parse tree produced by {@link AssemblyParser#qualifiedNameList}.
	 * @param ctx the parse tree
	 */
	void enterQualifiedNameList(AssemblyParser.QualifiedNameListContext ctx);
	/**
	 * Exit a parse tree produced by {@link AssemblyParser#qualifiedNameList}.
	 * @param ctx the parse tree
	 */
	void exitQualifiedNameList(AssemblyParser.QualifiedNameListContext ctx);
	/**
	 * Enter a parse tree produced by {@link AssemblyParser#qualifiedName}.
	 * @param ctx the parse tree
	 */
	void enterQualifiedName(AssemblyParser.QualifiedNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link AssemblyParser#qualifiedName}.
	 * @param ctx the parse tree
	 */
	void exitQualifiedName(AssemblyParser.QualifiedNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link AssemblyParser#typeParameter}.
	 * @param ctx the parse tree
	 */
	void enterTypeParameter(AssemblyParser.TypeParameterContext ctx);
	/**
	 * Exit a parse tree produced by {@link AssemblyParser#typeParameter}.
	 * @param ctx the parse tree
	 */
	void exitTypeParameter(AssemblyParser.TypeParameterContext ctx);
	/**
	 * Enter a parse tree produced by {@link AssemblyParser#formalParameters}.
	 * @param ctx the parse tree
	 */
	void enterFormalParameters(AssemblyParser.FormalParametersContext ctx);
	/**
	 * Exit a parse tree produced by {@link AssemblyParser#formalParameters}.
	 * @param ctx the parse tree
	 */
	void exitFormalParameters(AssemblyParser.FormalParametersContext ctx);
	/**
	 * Enter a parse tree produced by {@link AssemblyParser#receiverParameter}.
	 * @param ctx the parse tree
	 */
	void enterReceiverParameter(AssemblyParser.ReceiverParameterContext ctx);
	/**
	 * Exit a parse tree produced by {@link AssemblyParser#receiverParameter}.
	 * @param ctx the parse tree
	 */
	void exitReceiverParameter(AssemblyParser.ReceiverParameterContext ctx);
	/**
	 * Enter a parse tree produced by {@link AssemblyParser#formalParameterList}.
	 * @param ctx the parse tree
	 */
	void enterFormalParameterList(AssemblyParser.FormalParameterListContext ctx);
	/**
	 * Exit a parse tree produced by {@link AssemblyParser#formalParameterList}.
	 * @param ctx the parse tree
	 */
	void exitFormalParameterList(AssemblyParser.FormalParameterListContext ctx);
	/**
	 * Enter a parse tree produced by {@link AssemblyParser#formalParameter}.
	 * @param ctx the parse tree
	 */
	void enterFormalParameter(AssemblyParser.FormalParameterContext ctx);
	/**
	 * Exit a parse tree produced by {@link AssemblyParser#formalParameter}.
	 * @param ctx the parse tree
	 */
	void exitFormalParameter(AssemblyParser.FormalParameterContext ctx);
	/**
	 * Enter a parse tree produced by {@link AssemblyParser#methodBody}.
	 * @param ctx the parse tree
	 */
	void enterMethodBody(AssemblyParser.MethodBodyContext ctx);
	/**
	 * Exit a parse tree produced by {@link AssemblyParser#methodBody}.
	 * @param ctx the parse tree
	 */
	void exitMethodBody(AssemblyParser.MethodBodyContext ctx);
	/**
	 * Enter a parse tree produced by {@link AssemblyParser#block}.
	 * @param ctx the parse tree
	 */
	void enterBlock(AssemblyParser.BlockContext ctx);
	/**
	 * Exit a parse tree produced by {@link AssemblyParser#block}.
	 * @param ctx the parse tree
	 */
	void exitBlock(AssemblyParser.BlockContext ctx);
	/**
	 * Enter a parse tree produced by {@link AssemblyParser#labeledStatement}.
	 * @param ctx the parse tree
	 */
	void enterLabeledStatement(AssemblyParser.LabeledStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link AssemblyParser#labeledStatement}.
	 * @param ctx the parse tree
	 */
	void exitLabeledStatement(AssemblyParser.LabeledStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link AssemblyParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterStatement(AssemblyParser.StatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link AssemblyParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitStatement(AssemblyParser.StatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link AssemblyParser#qualifiedFieldName}.
	 * @param ctx the parse tree
	 */
	void enterQualifiedFieldName(AssemblyParser.QualifiedFieldNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link AssemblyParser#qualifiedFieldName}.
	 * @param ctx the parse tree
	 */
	void exitQualifiedFieldName(AssemblyParser.QualifiedFieldNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link AssemblyParser#creator}.
	 * @param ctx the parse tree
	 */
	void enterCreator(AssemblyParser.CreatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link AssemblyParser#creator}.
	 * @param ctx the parse tree
	 */
	void exitCreator(AssemblyParser.CreatorContext ctx);
	/**
	 * Enter a parse tree produced by {@link AssemblyParser#arrayCreatorRest}.
	 * @param ctx the parse tree
	 */
	void enterArrayCreatorRest(AssemblyParser.ArrayCreatorRestContext ctx);
	/**
	 * Exit a parse tree produced by {@link AssemblyParser#arrayCreatorRest}.
	 * @param ctx the parse tree
	 */
	void exitArrayCreatorRest(AssemblyParser.ArrayCreatorRestContext ctx);
	/**
	 * Enter a parse tree produced by {@link AssemblyParser#arrayInitializer}.
	 * @param ctx the parse tree
	 */
	void enterArrayInitializer(AssemblyParser.ArrayInitializerContext ctx);
	/**
	 * Exit a parse tree produced by {@link AssemblyParser#arrayInitializer}.
	 * @param ctx the parse tree
	 */
	void exitArrayInitializer(AssemblyParser.ArrayInitializerContext ctx);
	/**
	 * Enter a parse tree produced by {@link AssemblyParser#variableInitializer}.
	 * @param ctx the parse tree
	 */
	void enterVariableInitializer(AssemblyParser.VariableInitializerContext ctx);
	/**
	 * Exit a parse tree produced by {@link AssemblyParser#variableInitializer}.
	 * @param ctx the parse tree
	 */
	void exitVariableInitializer(AssemblyParser.VariableInitializerContext ctx);
	/**
	 * Enter a parse tree produced by {@link AssemblyParser#createdName}.
	 * @param ctx the parse tree
	 */
	void enterCreatedName(AssemblyParser.CreatedNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link AssemblyParser#createdName}.
	 * @param ctx the parse tree
	 */
	void exitCreatedName(AssemblyParser.CreatedNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link AssemblyParser#classCreatorRest}.
	 * @param ctx the parse tree
	 */
	void enterClassCreatorRest(AssemblyParser.ClassCreatorRestContext ctx);
	/**
	 * Exit a parse tree produced by {@link AssemblyParser#classCreatorRest}.
	 * @param ctx the parse tree
	 */
	void exitClassCreatorRest(AssemblyParser.ClassCreatorRestContext ctx);
	/**
	 * Enter a parse tree produced by {@link AssemblyParser#catchClause}.
	 * @param ctx the parse tree
	 */
	void enterCatchClause(AssemblyParser.CatchClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link AssemblyParser#catchClause}.
	 * @param ctx the parse tree
	 */
	void exitCatchClause(AssemblyParser.CatchClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link AssemblyParser#catchFields}.
	 * @param ctx the parse tree
	 */
	void enterCatchFields(AssemblyParser.CatchFieldsContext ctx);
	/**
	 * Exit a parse tree produced by {@link AssemblyParser#catchFields}.
	 * @param ctx the parse tree
	 */
	void exitCatchFields(AssemblyParser.CatchFieldsContext ctx);
	/**
	 * Enter a parse tree produced by {@link AssemblyParser#catchField}.
	 * @param ctx the parse tree
	 */
	void enterCatchField(AssemblyParser.CatchFieldContext ctx);
	/**
	 * Exit a parse tree produced by {@link AssemblyParser#catchField}.
	 * @param ctx the parse tree
	 */
	void exitCatchField(AssemblyParser.CatchFieldContext ctx);
	/**
	 * Enter a parse tree produced by {@link AssemblyParser#catchValue}.
	 * @param ctx the parse tree
	 */
	void enterCatchValue(AssemblyParser.CatchValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link AssemblyParser#catchValue}.
	 * @param ctx the parse tree
	 */
	void exitCatchValue(AssemblyParser.CatchValueContext ctx);
	/**
	 * Enter a parse tree produced by {@link AssemblyParser#branchCase}.
	 * @param ctx the parse tree
	 */
	void enterBranchCase(AssemblyParser.BranchCaseContext ctx);
	/**
	 * Exit a parse tree produced by {@link AssemblyParser#branchCase}.
	 * @param ctx the parse tree
	 */
	void exitBranchCase(AssemblyParser.BranchCaseContext ctx);
	/**
	 * Enter a parse tree produced by {@link AssemblyParser#switchLabel}.
	 * @param ctx the parse tree
	 */
	void enterSwitchLabel(AssemblyParser.SwitchLabelContext ctx);
	/**
	 * Exit a parse tree produced by {@link AssemblyParser#switchLabel}.
	 * @param ctx the parse tree
	 */
	void exitSwitchLabel(AssemblyParser.SwitchLabelContext ctx);
	/**
	 * Enter a parse tree produced by {@link AssemblyParser#parExpression}.
	 * @param ctx the parse tree
	 */
	void enterParExpression(AssemblyParser.ParExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link AssemblyParser#parExpression}.
	 * @param ctx the parse tree
	 */
	void exitParExpression(AssemblyParser.ParExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link AssemblyParser#expressionList}.
	 * @param ctx the parse tree
	 */
	void enterExpressionList(AssemblyParser.ExpressionListContext ctx);
	/**
	 * Exit a parse tree produced by {@link AssemblyParser#expressionList}.
	 * @param ctx the parse tree
	 */
	void exitExpressionList(AssemblyParser.ExpressionListContext ctx);
	/**
	 * Enter a parse tree produced by {@link AssemblyParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterExpression(AssemblyParser.ExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link AssemblyParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitExpression(AssemblyParser.ExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link AssemblyParser#primary}.
	 * @param ctx the parse tree
	 */
	void enterPrimary(AssemblyParser.PrimaryContext ctx);
	/**
	 * Exit a parse tree produced by {@link AssemblyParser#primary}.
	 * @param ctx the parse tree
	 */
	void exitPrimary(AssemblyParser.PrimaryContext ctx);
	/**
	 * Enter a parse tree produced by {@link AssemblyParser#explicitGenericInvocation}.
	 * @param ctx the parse tree
	 */
	void enterExplicitGenericInvocation(AssemblyParser.ExplicitGenericInvocationContext ctx);
	/**
	 * Exit a parse tree produced by {@link AssemblyParser#explicitGenericInvocation}.
	 * @param ctx the parse tree
	 */
	void exitExplicitGenericInvocation(AssemblyParser.ExplicitGenericInvocationContext ctx);
	/**
	 * Enter a parse tree produced by {@link AssemblyParser#explicitGenericInvocationSuffix}.
	 * @param ctx the parse tree
	 */
	void enterExplicitGenericInvocationSuffix(AssemblyParser.ExplicitGenericInvocationSuffixContext ctx);
	/**
	 * Exit a parse tree produced by {@link AssemblyParser#explicitGenericInvocationSuffix}.
	 * @param ctx the parse tree
	 */
	void exitExplicitGenericInvocationSuffix(AssemblyParser.ExplicitGenericInvocationSuffixContext ctx);
	/**
	 * Enter a parse tree produced by {@link AssemblyParser#superSuffix}.
	 * @param ctx the parse tree
	 */
	void enterSuperSuffix(AssemblyParser.SuperSuffixContext ctx);
	/**
	 * Exit a parse tree produced by {@link AssemblyParser#superSuffix}.
	 * @param ctx the parse tree
	 */
	void exitSuperSuffix(AssemblyParser.SuperSuffixContext ctx);
	/**
	 * Enter a parse tree produced by {@link AssemblyParser#arguments}.
	 * @param ctx the parse tree
	 */
	void enterArguments(AssemblyParser.ArgumentsContext ctx);
	/**
	 * Exit a parse tree produced by {@link AssemblyParser#arguments}.
	 * @param ctx the parse tree
	 */
	void exitArguments(AssemblyParser.ArgumentsContext ctx);
	/**
	 * Enter a parse tree produced by {@link AssemblyParser#classType}.
	 * @param ctx the parse tree
	 */
	void enterClassType(AssemblyParser.ClassTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link AssemblyParser#classType}.
	 * @param ctx the parse tree
	 */
	void exitClassType(AssemblyParser.ClassTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link AssemblyParser#methodCall}.
	 * @param ctx the parse tree
	 */
	void enterMethodCall(AssemblyParser.MethodCallContext ctx);
	/**
	 * Exit a parse tree produced by {@link AssemblyParser#methodCall}.
	 * @param ctx the parse tree
	 */
	void exitMethodCall(AssemblyParser.MethodCallContext ctx);
	/**
	 * Enter a parse tree produced by {@link AssemblyParser#literal}.
	 * @param ctx the parse tree
	 */
	void enterLiteral(AssemblyParser.LiteralContext ctx);
	/**
	 * Exit a parse tree produced by {@link AssemblyParser#literal}.
	 * @param ctx the parse tree
	 */
	void exitLiteral(AssemblyParser.LiteralContext ctx);
	/**
	 * Enter a parse tree produced by {@link AssemblyParser#integerLiteral}.
	 * @param ctx the parse tree
	 */
	void enterIntegerLiteral(AssemblyParser.IntegerLiteralContext ctx);
	/**
	 * Exit a parse tree produced by {@link AssemblyParser#integerLiteral}.
	 * @param ctx the parse tree
	 */
	void exitIntegerLiteral(AssemblyParser.IntegerLiteralContext ctx);
	/**
	 * Enter a parse tree produced by {@link AssemblyParser#floatLiteral}.
	 * @param ctx the parse tree
	 */
	void enterFloatLiteral(AssemblyParser.FloatLiteralContext ctx);
	/**
	 * Exit a parse tree produced by {@link AssemblyParser#floatLiteral}.
	 * @param ctx the parse tree
	 */
	void exitFloatLiteral(AssemblyParser.FloatLiteralContext ctx);
	/**
	 * Enter a parse tree produced by {@link AssemblyParser#typeTypeOrVoid}.
	 * @param ctx the parse tree
	 */
	void enterTypeTypeOrVoid(AssemblyParser.TypeTypeOrVoidContext ctx);
	/**
	 * Exit a parse tree produced by {@link AssemblyParser#typeTypeOrVoid}.
	 * @param ctx the parse tree
	 */
	void exitTypeTypeOrVoid(AssemblyParser.TypeTypeOrVoidContext ctx);
	/**
	 * Enter a parse tree produced by {@link AssemblyParser#typeType}.
	 * @param ctx the parse tree
	 */
	void enterTypeType(AssemblyParser.TypeTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link AssemblyParser#typeType}.
	 * @param ctx the parse tree
	 */
	void exitTypeType(AssemblyParser.TypeTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link AssemblyParser#arrayKind}.
	 * @param ctx the parse tree
	 */
	void enterArrayKind(AssemblyParser.ArrayKindContext ctx);
	/**
	 * Exit a parse tree produced by {@link AssemblyParser#arrayKind}.
	 * @param ctx the parse tree
	 */
	void exitArrayKind(AssemblyParser.ArrayKindContext ctx);
	/**
	 * Enter a parse tree produced by {@link AssemblyParser#classOrInterfaceType}.
	 * @param ctx the parse tree
	 */
	void enterClassOrInterfaceType(AssemblyParser.ClassOrInterfaceTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link AssemblyParser#classOrInterfaceType}.
	 * @param ctx the parse tree
	 */
	void exitClassOrInterfaceType(AssemblyParser.ClassOrInterfaceTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link AssemblyParser#typeArguments}.
	 * @param ctx the parse tree
	 */
	void enterTypeArguments(AssemblyParser.TypeArgumentsContext ctx);
	/**
	 * Exit a parse tree produced by {@link AssemblyParser#typeArguments}.
	 * @param ctx the parse tree
	 */
	void exitTypeArguments(AssemblyParser.TypeArgumentsContext ctx);
	/**
	 * Enter a parse tree produced by {@link AssemblyParser#primitiveType}.
	 * @param ctx the parse tree
	 */
	void enterPrimitiveType(AssemblyParser.PrimitiveTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link AssemblyParser#primitiveType}.
	 * @param ctx the parse tree
	 */
	void exitPrimitiveType(AssemblyParser.PrimitiveTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link AssemblyParser#modifier}.
	 * @param ctx the parse tree
	 */
	void enterModifier(AssemblyParser.ModifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link AssemblyParser#modifier}.
	 * @param ctx the parse tree
	 */
	void exitModifier(AssemblyParser.ModifierContext ctx);
	/**
	 * Enter a parse tree produced by {@link AssemblyParser#classOrInterfaceModifier}.
	 * @param ctx the parse tree
	 */
	void enterClassOrInterfaceModifier(AssemblyParser.ClassOrInterfaceModifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link AssemblyParser#classOrInterfaceModifier}.
	 * @param ctx the parse tree
	 */
	void exitClassOrInterfaceModifier(AssemblyParser.ClassOrInterfaceModifierContext ctx);
}