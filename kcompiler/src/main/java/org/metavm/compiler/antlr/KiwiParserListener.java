// Generated from KiwiParser.g4 by ANTLR 4.13.2
package org.metavm.compiler.antlr;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link KiwiParser}.
 */
public interface KiwiParserListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link KiwiParser#compilationUnit}.
	 * @param ctx the parse tree
	 */
	void enterCompilationUnit(KiwiParser.CompilationUnitContext ctx);
	/**
	 * Exit a parse tree produced by {@link KiwiParser#compilationUnit}.
	 * @param ctx the parse tree
	 */
	void exitCompilationUnit(KiwiParser.CompilationUnitContext ctx);
	/**
	 * Enter a parse tree produced by {@link KiwiParser#packageDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterPackageDeclaration(KiwiParser.PackageDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link KiwiParser#packageDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitPackageDeclaration(KiwiParser.PackageDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link KiwiParser#importDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterImportDeclaration(KiwiParser.ImportDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link KiwiParser#importDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitImportDeclaration(KiwiParser.ImportDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link KiwiParser#topLevTypeDecl}.
	 * @param ctx the parse tree
	 */
	void enterTopLevTypeDecl(KiwiParser.TopLevTypeDeclContext ctx);
	/**
	 * Exit a parse tree produced by {@link KiwiParser#topLevTypeDecl}.
	 * @param ctx the parse tree
	 */
	void exitTopLevTypeDecl(KiwiParser.TopLevTypeDeclContext ctx);
	/**
	 * Enter a parse tree produced by {@link KiwiParser#typeDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterTypeDeclaration(KiwiParser.TypeDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link KiwiParser#typeDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitTypeDeclaration(KiwiParser.TypeDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link KiwiParser#classDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterClassDeclaration(KiwiParser.ClassDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link KiwiParser#classDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitClassDeclaration(KiwiParser.ClassDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link KiwiParser#classBody}.
	 * @param ctx the parse tree
	 */
	void enterClassBody(KiwiParser.ClassBodyContext ctx);
	/**
	 * Exit a parse tree produced by {@link KiwiParser#classBody}.
	 * @param ctx the parse tree
	 */
	void exitClassBody(KiwiParser.ClassBodyContext ctx);
	/**
	 * Enter a parse tree produced by {@link KiwiParser#typeList}.
	 * @param ctx the parse tree
	 */
	void enterTypeList(KiwiParser.TypeListContext ctx);
	/**
	 * Exit a parse tree produced by {@link KiwiParser#typeList}.
	 * @param ctx the parse tree
	 */
	void exitTypeList(KiwiParser.TypeListContext ctx);
	/**
	 * Enter a parse tree produced by {@link KiwiParser#classBodyDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterClassBodyDeclaration(KiwiParser.ClassBodyDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link KiwiParser#classBodyDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitClassBodyDeclaration(KiwiParser.ClassBodyDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link KiwiParser#staticBlock}.
	 * @param ctx the parse tree
	 */
	void enterStaticBlock(KiwiParser.StaticBlockContext ctx);
	/**
	 * Exit a parse tree produced by {@link KiwiParser#staticBlock}.
	 * @param ctx the parse tree
	 */
	void exitStaticBlock(KiwiParser.StaticBlockContext ctx);
	/**
	 * Enter a parse tree produced by {@link KiwiParser#enumDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterEnumDeclaration(KiwiParser.EnumDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link KiwiParser#enumDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitEnumDeclaration(KiwiParser.EnumDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link KiwiParser#enumConstants}.
	 * @param ctx the parse tree
	 */
	void enterEnumConstants(KiwiParser.EnumConstantsContext ctx);
	/**
	 * Exit a parse tree produced by {@link KiwiParser#enumConstants}.
	 * @param ctx the parse tree
	 */
	void exitEnumConstants(KiwiParser.EnumConstantsContext ctx);
	/**
	 * Enter a parse tree produced by {@link KiwiParser#enumConstant}.
	 * @param ctx the parse tree
	 */
	void enterEnumConstant(KiwiParser.EnumConstantContext ctx);
	/**
	 * Exit a parse tree produced by {@link KiwiParser#enumConstant}.
	 * @param ctx the parse tree
	 */
	void exitEnumConstant(KiwiParser.EnumConstantContext ctx);
	/**
	 * Enter a parse tree produced by {@link KiwiParser#enumBodyDeclarations}.
	 * @param ctx the parse tree
	 */
	void enterEnumBodyDeclarations(KiwiParser.EnumBodyDeclarationsContext ctx);
	/**
	 * Exit a parse tree produced by {@link KiwiParser#enumBodyDeclarations}.
	 * @param ctx the parse tree
	 */
	void exitEnumBodyDeclarations(KiwiParser.EnumBodyDeclarationsContext ctx);
	/**
	 * Enter a parse tree produced by {@link KiwiParser#interfaceDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterInterfaceDeclaration(KiwiParser.InterfaceDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link KiwiParser#interfaceDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitInterfaceDeclaration(KiwiParser.InterfaceDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link KiwiParser#interfaceBody}.
	 * @param ctx the parse tree
	 */
	void enterInterfaceBody(KiwiParser.InterfaceBodyContext ctx);
	/**
	 * Exit a parse tree produced by {@link KiwiParser#interfaceBody}.
	 * @param ctx the parse tree
	 */
	void exitInterfaceBody(KiwiParser.InterfaceBodyContext ctx);
	/**
	 * Enter a parse tree produced by {@link KiwiParser#interfaceBodyDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterInterfaceBodyDeclaration(KiwiParser.InterfaceBodyDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link KiwiParser#interfaceBodyDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitInterfaceBodyDeclaration(KiwiParser.InterfaceBodyDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link KiwiParser#interfaceMemberDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterInterfaceMemberDeclaration(KiwiParser.InterfaceMemberDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link KiwiParser#interfaceMemberDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitInterfaceMemberDeclaration(KiwiParser.InterfaceMemberDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link KiwiParser#interfaceMethodDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterInterfaceMethodDeclaration(KiwiParser.InterfaceMethodDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link KiwiParser#interfaceMethodDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitInterfaceMethodDeclaration(KiwiParser.InterfaceMethodDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link KiwiParser#interfaceMethodModifier}.
	 * @param ctx the parse tree
	 */
	void enterInterfaceMethodModifier(KiwiParser.InterfaceMethodModifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link KiwiParser#interfaceMethodModifier}.
	 * @param ctx the parse tree
	 */
	void exitInterfaceMethodModifier(KiwiParser.InterfaceMethodModifierContext ctx);
	/**
	 * Enter a parse tree produced by {@link KiwiParser#memberDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterMemberDeclaration(KiwiParser.MemberDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link KiwiParser#memberDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitMemberDeclaration(KiwiParser.MemberDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link KiwiParser#fieldDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterFieldDeclaration(KiwiParser.FieldDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link KiwiParser#fieldDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitFieldDeclaration(KiwiParser.FieldDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link KiwiParser#methodDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterMethodDeclaration(KiwiParser.MethodDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link KiwiParser#methodDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitMethodDeclaration(KiwiParser.MethodDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link KiwiParser#constructorDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterConstructorDeclaration(KiwiParser.ConstructorDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link KiwiParser#constructorDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitConstructorDeclaration(KiwiParser.ConstructorDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link KiwiParser#typeParameters}.
	 * @param ctx the parse tree
	 */
	void enterTypeParameters(KiwiParser.TypeParametersContext ctx);
	/**
	 * Exit a parse tree produced by {@link KiwiParser#typeParameters}.
	 * @param ctx the parse tree
	 */
	void exitTypeParameters(KiwiParser.TypeParametersContext ctx);
	/**
	 * Enter a parse tree produced by {@link KiwiParser#qualifiedNameList}.
	 * @param ctx the parse tree
	 */
	void enterQualifiedNameList(KiwiParser.QualifiedNameListContext ctx);
	/**
	 * Exit a parse tree produced by {@link KiwiParser#qualifiedNameList}.
	 * @param ctx the parse tree
	 */
	void exitQualifiedNameList(KiwiParser.QualifiedNameListContext ctx);
	/**
	 * Enter a parse tree produced by {@link KiwiParser#qualifiedName}.
	 * @param ctx the parse tree
	 */
	void enterQualifiedName(KiwiParser.QualifiedNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link KiwiParser#qualifiedName}.
	 * @param ctx the parse tree
	 */
	void exitQualifiedName(KiwiParser.QualifiedNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link KiwiParser#typeParameter}.
	 * @param ctx the parse tree
	 */
	void enterTypeParameter(KiwiParser.TypeParameterContext ctx);
	/**
	 * Exit a parse tree produced by {@link KiwiParser#typeParameter}.
	 * @param ctx the parse tree
	 */
	void exitTypeParameter(KiwiParser.TypeParameterContext ctx);
	/**
	 * Enter a parse tree produced by {@link KiwiParser#formalParameters}.
	 * @param ctx the parse tree
	 */
	void enterFormalParameters(KiwiParser.FormalParametersContext ctx);
	/**
	 * Exit a parse tree produced by {@link KiwiParser#formalParameters}.
	 * @param ctx the parse tree
	 */
	void exitFormalParameters(KiwiParser.FormalParametersContext ctx);
	/**
	 * Enter a parse tree produced by {@link KiwiParser#formalParameterList}.
	 * @param ctx the parse tree
	 */
	void enterFormalParameterList(KiwiParser.FormalParameterListContext ctx);
	/**
	 * Exit a parse tree produced by {@link KiwiParser#formalParameterList}.
	 * @param ctx the parse tree
	 */
	void exitFormalParameterList(KiwiParser.FormalParameterListContext ctx);
	/**
	 * Enter a parse tree produced by {@link KiwiParser#formalParameter}.
	 * @param ctx the parse tree
	 */
	void enterFormalParameter(KiwiParser.FormalParameterContext ctx);
	/**
	 * Exit a parse tree produced by {@link KiwiParser#formalParameter}.
	 * @param ctx the parse tree
	 */
	void exitFormalParameter(KiwiParser.FormalParameterContext ctx);
	/**
	 * Enter a parse tree produced by {@link KiwiParser#methodBody}.
	 * @param ctx the parse tree
	 */
	void enterMethodBody(KiwiParser.MethodBodyContext ctx);
	/**
	 * Exit a parse tree produced by {@link KiwiParser#methodBody}.
	 * @param ctx the parse tree
	 */
	void exitMethodBody(KiwiParser.MethodBodyContext ctx);
	/**
	 * Enter a parse tree produced by {@link KiwiParser#block}.
	 * @param ctx the parse tree
	 */
	void enterBlock(KiwiParser.BlockContext ctx);
	/**
	 * Exit a parse tree produced by {@link KiwiParser#block}.
	 * @param ctx the parse tree
	 */
	void exitBlock(KiwiParser.BlockContext ctx);
	/**
	 * Enter a parse tree produced by {@link KiwiParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterStatement(KiwiParser.StatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link KiwiParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitStatement(KiwiParser.StatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link KiwiParser#localVariableDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterLocalVariableDeclaration(KiwiParser.LocalVariableDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link KiwiParser#localVariableDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitLocalVariableDeclaration(KiwiParser.LocalVariableDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link KiwiParser#forControl}.
	 * @param ctx the parse tree
	 */
	void enterForControl(KiwiParser.ForControlContext ctx);
	/**
	 * Exit a parse tree produced by {@link KiwiParser#forControl}.
	 * @param ctx the parse tree
	 */
	void exitForControl(KiwiParser.ForControlContext ctx);
	/**
	 * Enter a parse tree produced by {@link KiwiParser#loopVariable}.
	 * @param ctx the parse tree
	 */
	void enterLoopVariable(KiwiParser.LoopVariableContext ctx);
	/**
	 * Exit a parse tree produced by {@link KiwiParser#loopVariable}.
	 * @param ctx the parse tree
	 */
	void exitLoopVariable(KiwiParser.LoopVariableContext ctx);
	/**
	 * Enter a parse tree produced by {@link KiwiParser#loopVariableDeclarators}.
	 * @param ctx the parse tree
	 */
	void enterLoopVariableDeclarators(KiwiParser.LoopVariableDeclaratorsContext ctx);
	/**
	 * Exit a parse tree produced by {@link KiwiParser#loopVariableDeclarators}.
	 * @param ctx the parse tree
	 */
	void exitLoopVariableDeclarators(KiwiParser.LoopVariableDeclaratorsContext ctx);
	/**
	 * Enter a parse tree produced by {@link KiwiParser#loopVariableDeclarator}.
	 * @param ctx the parse tree
	 */
	void enterLoopVariableDeclarator(KiwiParser.LoopVariableDeclaratorContext ctx);
	/**
	 * Exit a parse tree produced by {@link KiwiParser#loopVariableDeclarator}.
	 * @param ctx the parse tree
	 */
	void exitLoopVariableDeclarator(KiwiParser.LoopVariableDeclaratorContext ctx);
	/**
	 * Enter a parse tree produced by {@link KiwiParser#loopVariableUpdates}.
	 * @param ctx the parse tree
	 */
	void enterLoopVariableUpdates(KiwiParser.LoopVariableUpdatesContext ctx);
	/**
	 * Exit a parse tree produced by {@link KiwiParser#loopVariableUpdates}.
	 * @param ctx the parse tree
	 */
	void exitLoopVariableUpdates(KiwiParser.LoopVariableUpdatesContext ctx);
	/**
	 * Enter a parse tree produced by {@link KiwiParser#loopVariableUpdate}.
	 * @param ctx the parse tree
	 */
	void enterLoopVariableUpdate(KiwiParser.LoopVariableUpdateContext ctx);
	/**
	 * Exit a parse tree produced by {@link KiwiParser#loopVariableUpdate}.
	 * @param ctx the parse tree
	 */
	void exitLoopVariableUpdate(KiwiParser.LoopVariableUpdateContext ctx);
	/**
	 * Enter a parse tree produced by {@link KiwiParser#anonClassExpr}.
	 * @param ctx the parse tree
	 */
	void enterAnonClassExpr(KiwiParser.AnonClassExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link KiwiParser#anonClassExpr}.
	 * @param ctx the parse tree
	 */
	void exitAnonClassExpr(KiwiParser.AnonClassExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link KiwiParser#newArray}.
	 * @param ctx the parse tree
	 */
	void enterNewArray(KiwiParser.NewArrayContext ctx);
	/**
	 * Exit a parse tree produced by {@link KiwiParser#newArray}.
	 * @param ctx the parse tree
	 */
	void exitNewArray(KiwiParser.NewArrayContext ctx);
	/**
	 * Enter a parse tree produced by {@link KiwiParser#arrayInitializer}.
	 * @param ctx the parse tree
	 */
	void enterArrayInitializer(KiwiParser.ArrayInitializerContext ctx);
	/**
	 * Exit a parse tree produced by {@link KiwiParser#arrayInitializer}.
	 * @param ctx the parse tree
	 */
	void exitArrayInitializer(KiwiParser.ArrayInitializerContext ctx);
	/**
	 * Enter a parse tree produced by {@link KiwiParser#variableInitializer}.
	 * @param ctx the parse tree
	 */
	void enterVariableInitializer(KiwiParser.VariableInitializerContext ctx);
	/**
	 * Exit a parse tree produced by {@link KiwiParser#variableInitializer}.
	 * @param ctx the parse tree
	 */
	void exitVariableInitializer(KiwiParser.VariableInitializerContext ctx);
	/**
	 * Enter a parse tree produced by {@link KiwiParser#catchClause}.
	 * @param ctx the parse tree
	 */
	void enterCatchClause(KiwiParser.CatchClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link KiwiParser#catchClause}.
	 * @param ctx the parse tree
	 */
	void exitCatchClause(KiwiParser.CatchClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link KiwiParser#catchFields}.
	 * @param ctx the parse tree
	 */
	void enterCatchFields(KiwiParser.CatchFieldsContext ctx);
	/**
	 * Exit a parse tree produced by {@link KiwiParser#catchFields}.
	 * @param ctx the parse tree
	 */
	void exitCatchFields(KiwiParser.CatchFieldsContext ctx);
	/**
	 * Enter a parse tree produced by {@link KiwiParser#catchField}.
	 * @param ctx the parse tree
	 */
	void enterCatchField(KiwiParser.CatchFieldContext ctx);
	/**
	 * Exit a parse tree produced by {@link KiwiParser#catchField}.
	 * @param ctx the parse tree
	 */
	void exitCatchField(KiwiParser.CatchFieldContext ctx);
	/**
	 * Enter a parse tree produced by {@link KiwiParser#catchValue}.
	 * @param ctx the parse tree
	 */
	void enterCatchValue(KiwiParser.CatchValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link KiwiParser#catchValue}.
	 * @param ctx the parse tree
	 */
	void exitCatchValue(KiwiParser.CatchValueContext ctx);
	/**
	 * Enter a parse tree produced by {@link KiwiParser#branchCase}.
	 * @param ctx the parse tree
	 */
	void enterBranchCase(KiwiParser.BranchCaseContext ctx);
	/**
	 * Exit a parse tree produced by {@link KiwiParser#branchCase}.
	 * @param ctx the parse tree
	 */
	void exitBranchCase(KiwiParser.BranchCaseContext ctx);
	/**
	 * Enter a parse tree produced by {@link KiwiParser#switchLabel}.
	 * @param ctx the parse tree
	 */
	void enterSwitchLabel(KiwiParser.SwitchLabelContext ctx);
	/**
	 * Exit a parse tree produced by {@link KiwiParser#switchLabel}.
	 * @param ctx the parse tree
	 */
	void exitSwitchLabel(KiwiParser.SwitchLabelContext ctx);
	/**
	 * Enter a parse tree produced by {@link KiwiParser#parExpression}.
	 * @param ctx the parse tree
	 */
	void enterParExpression(KiwiParser.ParExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link KiwiParser#parExpression}.
	 * @param ctx the parse tree
	 */
	void exitParExpression(KiwiParser.ParExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link KiwiParser#expressionList}.
	 * @param ctx the parse tree
	 */
	void enterExpressionList(KiwiParser.ExpressionListContext ctx);
	/**
	 * Exit a parse tree produced by {@link KiwiParser#expressionList}.
	 * @param ctx the parse tree
	 */
	void exitExpressionList(KiwiParser.ExpressionListContext ctx);
	/**
	 * Enter a parse tree produced by {@link KiwiParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterExpression(KiwiParser.ExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link KiwiParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitExpression(KiwiParser.ExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link KiwiParser#assignment}.
	 * @param ctx the parse tree
	 */
	void enterAssignment(KiwiParser.AssignmentContext ctx);
	/**
	 * Exit a parse tree produced by {@link KiwiParser#assignment}.
	 * @param ctx the parse tree
	 */
	void exitAssignment(KiwiParser.AssignmentContext ctx);
	/**
	 * Enter a parse tree produced by {@link KiwiParser#assignmentSuffix}.
	 * @param ctx the parse tree
	 */
	void enterAssignmentSuffix(KiwiParser.AssignmentSuffixContext ctx);
	/**
	 * Exit a parse tree produced by {@link KiwiParser#assignmentSuffix}.
	 * @param ctx the parse tree
	 */
	void exitAssignmentSuffix(KiwiParser.AssignmentSuffixContext ctx);
	/**
	 * Enter a parse tree produced by {@link KiwiParser#ternary}.
	 * @param ctx the parse tree
	 */
	void enterTernary(KiwiParser.TernaryContext ctx);
	/**
	 * Exit a parse tree produced by {@link KiwiParser#ternary}.
	 * @param ctx the parse tree
	 */
	void exitTernary(KiwiParser.TernaryContext ctx);
	/**
	 * Enter a parse tree produced by {@link KiwiParser#disjunction}.
	 * @param ctx the parse tree
	 */
	void enterDisjunction(KiwiParser.DisjunctionContext ctx);
	/**
	 * Exit a parse tree produced by {@link KiwiParser#disjunction}.
	 * @param ctx the parse tree
	 */
	void exitDisjunction(KiwiParser.DisjunctionContext ctx);
	/**
	 * Enter a parse tree produced by {@link KiwiParser#conjunction}.
	 * @param ctx the parse tree
	 */
	void enterConjunction(KiwiParser.ConjunctionContext ctx);
	/**
	 * Exit a parse tree produced by {@link KiwiParser#conjunction}.
	 * @param ctx the parse tree
	 */
	void exitConjunction(KiwiParser.ConjunctionContext ctx);
	/**
	 * Enter a parse tree produced by {@link KiwiParser#range}.
	 * @param ctx the parse tree
	 */
	void enterRange(KiwiParser.RangeContext ctx);
	/**
	 * Exit a parse tree produced by {@link KiwiParser#range}.
	 * @param ctx the parse tree
	 */
	void exitRange(KiwiParser.RangeContext ctx);
	/**
	 * Enter a parse tree produced by {@link KiwiParser#bitor}.
	 * @param ctx the parse tree
	 */
	void enterBitor(KiwiParser.BitorContext ctx);
	/**
	 * Exit a parse tree produced by {@link KiwiParser#bitor}.
	 * @param ctx the parse tree
	 */
	void exitBitor(KiwiParser.BitorContext ctx);
	/**
	 * Enter a parse tree produced by {@link KiwiParser#bitand}.
	 * @param ctx the parse tree
	 */
	void enterBitand(KiwiParser.BitandContext ctx);
	/**
	 * Exit a parse tree produced by {@link KiwiParser#bitand}.
	 * @param ctx the parse tree
	 */
	void exitBitand(KiwiParser.BitandContext ctx);
	/**
	 * Enter a parse tree produced by {@link KiwiParser#bitxor}.
	 * @param ctx the parse tree
	 */
	void enterBitxor(KiwiParser.BitxorContext ctx);
	/**
	 * Exit a parse tree produced by {@link KiwiParser#bitxor}.
	 * @param ctx the parse tree
	 */
	void exitBitxor(KiwiParser.BitxorContext ctx);
	/**
	 * Enter a parse tree produced by {@link KiwiParser#equality}.
	 * @param ctx the parse tree
	 */
	void enterEquality(KiwiParser.EqualityContext ctx);
	/**
	 * Exit a parse tree produced by {@link KiwiParser#equality}.
	 * @param ctx the parse tree
	 */
	void exitEquality(KiwiParser.EqualityContext ctx);
	/**
	 * Enter a parse tree produced by {@link KiwiParser#equalitySuffix}.
	 * @param ctx the parse tree
	 */
	void enterEqualitySuffix(KiwiParser.EqualitySuffixContext ctx);
	/**
	 * Exit a parse tree produced by {@link KiwiParser#equalitySuffix}.
	 * @param ctx the parse tree
	 */
	void exitEqualitySuffix(KiwiParser.EqualitySuffixContext ctx);
	/**
	 * Enter a parse tree produced by {@link KiwiParser#relational}.
	 * @param ctx the parse tree
	 */
	void enterRelational(KiwiParser.RelationalContext ctx);
	/**
	 * Exit a parse tree produced by {@link KiwiParser#relational}.
	 * @param ctx the parse tree
	 */
	void exitRelational(KiwiParser.RelationalContext ctx);
	/**
	 * Enter a parse tree produced by {@link KiwiParser#relationalSuffix}.
	 * @param ctx the parse tree
	 */
	void enterRelationalSuffix(KiwiParser.RelationalSuffixContext ctx);
	/**
	 * Exit a parse tree produced by {@link KiwiParser#relationalSuffix}.
	 * @param ctx the parse tree
	 */
	void exitRelationalSuffix(KiwiParser.RelationalSuffixContext ctx);
	/**
	 * Enter a parse tree produced by {@link KiwiParser#isExpr}.
	 * @param ctx the parse tree
	 */
	void enterIsExpr(KiwiParser.IsExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link KiwiParser#isExpr}.
	 * @param ctx the parse tree
	 */
	void exitIsExpr(KiwiParser.IsExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link KiwiParser#isSuffix}.
	 * @param ctx the parse tree
	 */
	void enterIsSuffix(KiwiParser.IsSuffixContext ctx);
	/**
	 * Exit a parse tree produced by {@link KiwiParser#isSuffix}.
	 * @param ctx the parse tree
	 */
	void exitIsSuffix(KiwiParser.IsSuffixContext ctx);
	/**
	 * Enter a parse tree produced by {@link KiwiParser#typePtn}.
	 * @param ctx the parse tree
	 */
	void enterTypePtn(KiwiParser.TypePtnContext ctx);
	/**
	 * Exit a parse tree produced by {@link KiwiParser#typePtn}.
	 * @param ctx the parse tree
	 */
	void exitTypePtn(KiwiParser.TypePtnContext ctx);
	/**
	 * Enter a parse tree produced by {@link KiwiParser#shift}.
	 * @param ctx the parse tree
	 */
	void enterShift(KiwiParser.ShiftContext ctx);
	/**
	 * Exit a parse tree produced by {@link KiwiParser#shift}.
	 * @param ctx the parse tree
	 */
	void exitShift(KiwiParser.ShiftContext ctx);
	/**
	 * Enter a parse tree produced by {@link KiwiParser#shiftSuffix}.
	 * @param ctx the parse tree
	 */
	void enterShiftSuffix(KiwiParser.ShiftSuffixContext ctx);
	/**
	 * Exit a parse tree produced by {@link KiwiParser#shiftSuffix}.
	 * @param ctx the parse tree
	 */
	void exitShiftSuffix(KiwiParser.ShiftSuffixContext ctx);
	/**
	 * Enter a parse tree produced by {@link KiwiParser#additive}.
	 * @param ctx the parse tree
	 */
	void enterAdditive(KiwiParser.AdditiveContext ctx);
	/**
	 * Exit a parse tree produced by {@link KiwiParser#additive}.
	 * @param ctx the parse tree
	 */
	void exitAdditive(KiwiParser.AdditiveContext ctx);
	/**
	 * Enter a parse tree produced by {@link KiwiParser#additiveSuffix}.
	 * @param ctx the parse tree
	 */
	void enterAdditiveSuffix(KiwiParser.AdditiveSuffixContext ctx);
	/**
	 * Exit a parse tree produced by {@link KiwiParser#additiveSuffix}.
	 * @param ctx the parse tree
	 */
	void exitAdditiveSuffix(KiwiParser.AdditiveSuffixContext ctx);
	/**
	 * Enter a parse tree produced by {@link KiwiParser#multiplicative}.
	 * @param ctx the parse tree
	 */
	void enterMultiplicative(KiwiParser.MultiplicativeContext ctx);
	/**
	 * Exit a parse tree produced by {@link KiwiParser#multiplicative}.
	 * @param ctx the parse tree
	 */
	void exitMultiplicative(KiwiParser.MultiplicativeContext ctx);
	/**
	 * Enter a parse tree produced by {@link KiwiParser#multiplicativeSuffix}.
	 * @param ctx the parse tree
	 */
	void enterMultiplicativeSuffix(KiwiParser.MultiplicativeSuffixContext ctx);
	/**
	 * Exit a parse tree produced by {@link KiwiParser#multiplicativeSuffix}.
	 * @param ctx the parse tree
	 */
	void exitMultiplicativeSuffix(KiwiParser.MultiplicativeSuffixContext ctx);
	/**
	 * Enter a parse tree produced by {@link KiwiParser#asExpr}.
	 * @param ctx the parse tree
	 */
	void enterAsExpr(KiwiParser.AsExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link KiwiParser#asExpr}.
	 * @param ctx the parse tree
	 */
	void exitAsExpr(KiwiParser.AsExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link KiwiParser#prefixExpr}.
	 * @param ctx the parse tree
	 */
	void enterPrefixExpr(KiwiParser.PrefixExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link KiwiParser#prefixExpr}.
	 * @param ctx the parse tree
	 */
	void exitPrefixExpr(KiwiParser.PrefixExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link KiwiParser#prefixOp}.
	 * @param ctx the parse tree
	 */
	void enterPrefixOp(KiwiParser.PrefixOpContext ctx);
	/**
	 * Exit a parse tree produced by {@link KiwiParser#prefixOp}.
	 * @param ctx the parse tree
	 */
	void exitPrefixOp(KiwiParser.PrefixOpContext ctx);
	/**
	 * Enter a parse tree produced by {@link KiwiParser#postfixExpr}.
	 * @param ctx the parse tree
	 */
	void enterPostfixExpr(KiwiParser.PostfixExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link KiwiParser#postfixExpr}.
	 * @param ctx the parse tree
	 */
	void exitPostfixExpr(KiwiParser.PostfixExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link KiwiParser#postfixSuffix}.
	 * @param ctx the parse tree
	 */
	void enterPostfixSuffix(KiwiParser.PostfixSuffixContext ctx);
	/**
	 * Exit a parse tree produced by {@link KiwiParser#postfixSuffix}.
	 * @param ctx the parse tree
	 */
	void exitPostfixSuffix(KiwiParser.PostfixSuffixContext ctx);
	/**
	 * Enter a parse tree produced by {@link KiwiParser#callSuffix}.
	 * @param ctx the parse tree
	 */
	void enterCallSuffix(KiwiParser.CallSuffixContext ctx);
	/**
	 * Exit a parse tree produced by {@link KiwiParser#callSuffix}.
	 * @param ctx the parse tree
	 */
	void exitCallSuffix(KiwiParser.CallSuffixContext ctx);
	/**
	 * Enter a parse tree produced by {@link KiwiParser#indexingSuffix}.
	 * @param ctx the parse tree
	 */
	void enterIndexingSuffix(KiwiParser.IndexingSuffixContext ctx);
	/**
	 * Exit a parse tree produced by {@link KiwiParser#indexingSuffix}.
	 * @param ctx the parse tree
	 */
	void exitIndexingSuffix(KiwiParser.IndexingSuffixContext ctx);
	/**
	 * Enter a parse tree produced by {@link KiwiParser#selectorSuffix}.
	 * @param ctx the parse tree
	 */
	void enterSelectorSuffix(KiwiParser.SelectorSuffixContext ctx);
	/**
	 * Exit a parse tree produced by {@link KiwiParser#selectorSuffix}.
	 * @param ctx the parse tree
	 */
	void exitSelectorSuffix(KiwiParser.SelectorSuffixContext ctx);
	/**
	 * Enter a parse tree produced by {@link KiwiParser#primaryExpr}.
	 * @param ctx the parse tree
	 */
	void enterPrimaryExpr(KiwiParser.PrimaryExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link KiwiParser#primaryExpr}.
	 * @param ctx the parse tree
	 */
	void exitPrimaryExpr(KiwiParser.PrimaryExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link KiwiParser#arguments}.
	 * @param ctx the parse tree
	 */
	void enterArguments(KiwiParser.ArgumentsContext ctx);
	/**
	 * Exit a parse tree produced by {@link KiwiParser#arguments}.
	 * @param ctx the parse tree
	 */
	void exitArguments(KiwiParser.ArgumentsContext ctx);
	/**
	 * Enter a parse tree produced by {@link KiwiParser#identifier}.
	 * @param ctx the parse tree
	 */
	void enterIdentifier(KiwiParser.IdentifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link KiwiParser#identifier}.
	 * @param ctx the parse tree
	 */
	void exitIdentifier(KiwiParser.IdentifierContext ctx);
	/**
	 * Enter a parse tree produced by {@link KiwiParser#literal}.
	 * @param ctx the parse tree
	 */
	void enterLiteral(KiwiParser.LiteralContext ctx);
	/**
	 * Exit a parse tree produced by {@link KiwiParser#literal}.
	 * @param ctx the parse tree
	 */
	void exitLiteral(KiwiParser.LiteralContext ctx);
	/**
	 * Enter a parse tree produced by {@link KiwiParser#integerLiteral}.
	 * @param ctx the parse tree
	 */
	void enterIntegerLiteral(KiwiParser.IntegerLiteralContext ctx);
	/**
	 * Exit a parse tree produced by {@link KiwiParser#integerLiteral}.
	 * @param ctx the parse tree
	 */
	void exitIntegerLiteral(KiwiParser.IntegerLiteralContext ctx);
	/**
	 * Enter a parse tree produced by {@link KiwiParser#floatLiteral}.
	 * @param ctx the parse tree
	 */
	void enterFloatLiteral(KiwiParser.FloatLiteralContext ctx);
	/**
	 * Exit a parse tree produced by {@link KiwiParser#floatLiteral}.
	 * @param ctx the parse tree
	 */
	void exitFloatLiteral(KiwiParser.FloatLiteralContext ctx);
	/**
	 * Enter a parse tree produced by {@link KiwiParser#typeOrVoid}.
	 * @param ctx the parse tree
	 */
	void enterTypeOrVoid(KiwiParser.TypeOrVoidContext ctx);
	/**
	 * Exit a parse tree produced by {@link KiwiParser#typeOrVoid}.
	 * @param ctx the parse tree
	 */
	void exitTypeOrVoid(KiwiParser.TypeOrVoidContext ctx);
	/**
	 * Enter a parse tree produced by {@link KiwiParser#type}.
	 * @param ctx the parse tree
	 */
	void enterType(KiwiParser.TypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link KiwiParser#type}.
	 * @param ctx the parse tree
	 */
	void exitType(KiwiParser.TypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link KiwiParser#unionType}.
	 * @param ctx the parse tree
	 */
	void enterUnionType(KiwiParser.UnionTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link KiwiParser#unionType}.
	 * @param ctx the parse tree
	 */
	void exitUnionType(KiwiParser.UnionTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link KiwiParser#intersectionType}.
	 * @param ctx the parse tree
	 */
	void enterIntersectionType(KiwiParser.IntersectionTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link KiwiParser#intersectionType}.
	 * @param ctx the parse tree
	 */
	void exitIntersectionType(KiwiParser.IntersectionTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link KiwiParser#postfixType}.
	 * @param ctx the parse tree
	 */
	void enterPostfixType(KiwiParser.PostfixTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link KiwiParser#postfixType}.
	 * @param ctx the parse tree
	 */
	void exitPostfixType(KiwiParser.PostfixTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link KiwiParser#typeSuffix}.
	 * @param ctx the parse tree
	 */
	void enterTypeSuffix(KiwiParser.TypeSuffixContext ctx);
	/**
	 * Exit a parse tree produced by {@link KiwiParser#typeSuffix}.
	 * @param ctx the parse tree
	 */
	void exitTypeSuffix(KiwiParser.TypeSuffixContext ctx);
	/**
	 * Enter a parse tree produced by {@link KiwiParser#atomicType}.
	 * @param ctx the parse tree
	 */
	void enterAtomicType(KiwiParser.AtomicTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link KiwiParser#atomicType}.
	 * @param ctx the parse tree
	 */
	void exitAtomicType(KiwiParser.AtomicTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link KiwiParser#primitiveType}.
	 * @param ctx the parse tree
	 */
	void enterPrimitiveType(KiwiParser.PrimitiveTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link KiwiParser#primitiveType}.
	 * @param ctx the parse tree
	 */
	void exitPrimitiveType(KiwiParser.PrimitiveTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link KiwiParser#functionType}.
	 * @param ctx the parse tree
	 */
	void enterFunctionType(KiwiParser.FunctionTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link KiwiParser#functionType}.
	 * @param ctx the parse tree
	 */
	void exitFunctionType(KiwiParser.FunctionTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link KiwiParser#uncertainType}.
	 * @param ctx the parse tree
	 */
	void enterUncertainType(KiwiParser.UncertainTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link KiwiParser#uncertainType}.
	 * @param ctx the parse tree
	 */
	void exitUncertainType(KiwiParser.UncertainTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link KiwiParser#arrayKind}.
	 * @param ctx the parse tree
	 */
	void enterArrayKind(KiwiParser.ArrayKindContext ctx);
	/**
	 * Exit a parse tree produced by {@link KiwiParser#arrayKind}.
	 * @param ctx the parse tree
	 */
	void exitArrayKind(KiwiParser.ArrayKindContext ctx);
	/**
	 * Enter a parse tree produced by {@link KiwiParser#classType}.
	 * @param ctx the parse tree
	 */
	void enterClassType(KiwiParser.ClassTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link KiwiParser#classType}.
	 * @param ctx the parse tree
	 */
	void exitClassType(KiwiParser.ClassTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link KiwiParser#classTypePart}.
	 * @param ctx the parse tree
	 */
	void enterClassTypePart(KiwiParser.ClassTypePartContext ctx);
	/**
	 * Exit a parse tree produced by {@link KiwiParser#classTypePart}.
	 * @param ctx the parse tree
	 */
	void exitClassTypePart(KiwiParser.ClassTypePartContext ctx);
	/**
	 * Enter a parse tree produced by {@link KiwiParser#typeArguments}.
	 * @param ctx the parse tree
	 */
	void enterTypeArguments(KiwiParser.TypeArgumentsContext ctx);
	/**
	 * Exit a parse tree produced by {@link KiwiParser#typeArguments}.
	 * @param ctx the parse tree
	 */
	void exitTypeArguments(KiwiParser.TypeArgumentsContext ctx);
	/**
	 * Enter a parse tree produced by {@link KiwiParser#modifier}.
	 * @param ctx the parse tree
	 */
	void enterModifier(KiwiParser.ModifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link KiwiParser#modifier}.
	 * @param ctx the parse tree
	 */
	void exitModifier(KiwiParser.ModifierContext ctx);
	/**
	 * Enter a parse tree produced by {@link KiwiParser#classOrInterfaceModifier}.
	 * @param ctx the parse tree
	 */
	void enterClassOrInterfaceModifier(KiwiParser.ClassOrInterfaceModifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link KiwiParser#classOrInterfaceModifier}.
	 * @param ctx the parse tree
	 */
	void exitClassOrInterfaceModifier(KiwiParser.ClassOrInterfaceModifierContext ctx);
	/**
	 * Enter a parse tree produced by {@link KiwiParser#lambdaExpression}.
	 * @param ctx the parse tree
	 */
	void enterLambdaExpression(KiwiParser.LambdaExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link KiwiParser#lambdaExpression}.
	 * @param ctx the parse tree
	 */
	void exitLambdaExpression(KiwiParser.LambdaExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link KiwiParser#lambdaParameters}.
	 * @param ctx the parse tree
	 */
	void enterLambdaParameters(KiwiParser.LambdaParametersContext ctx);
	/**
	 * Exit a parse tree produced by {@link KiwiParser#lambdaParameters}.
	 * @param ctx the parse tree
	 */
	void exitLambdaParameters(KiwiParser.LambdaParametersContext ctx);
	/**
	 * Enter a parse tree produced by {@link KiwiParser#lambdaParameterList}.
	 * @param ctx the parse tree
	 */
	void enterLambdaParameterList(KiwiParser.LambdaParameterListContext ctx);
	/**
	 * Exit a parse tree produced by {@link KiwiParser#lambdaParameterList}.
	 * @param ctx the parse tree
	 */
	void exitLambdaParameterList(KiwiParser.LambdaParameterListContext ctx);
	/**
	 * Enter a parse tree produced by {@link KiwiParser#lambdaParameter}.
	 * @param ctx the parse tree
	 */
	void enterLambdaParameter(KiwiParser.LambdaParameterContext ctx);
	/**
	 * Exit a parse tree produced by {@link KiwiParser#lambdaParameter}.
	 * @param ctx the parse tree
	 */
	void exitLambdaParameter(KiwiParser.LambdaParameterContext ctx);
	/**
	 * Enter a parse tree produced by {@link KiwiParser#lambdaBody}.
	 * @param ctx the parse tree
	 */
	void enterLambdaBody(KiwiParser.LambdaBodyContext ctx);
	/**
	 * Exit a parse tree produced by {@link KiwiParser#lambdaBody}.
	 * @param ctx the parse tree
	 */
	void exitLambdaBody(KiwiParser.LambdaBodyContext ctx);
	/**
	 * Enter a parse tree produced by {@link KiwiParser#annotation}.
	 * @param ctx the parse tree
	 */
	void enterAnnotation(KiwiParser.AnnotationContext ctx);
	/**
	 * Exit a parse tree produced by {@link KiwiParser#annotation}.
	 * @param ctx the parse tree
	 */
	void exitAnnotation(KiwiParser.AnnotationContext ctx);
	/**
	 * Enter a parse tree produced by {@link KiwiParser#elementValuePairs}.
	 * @param ctx the parse tree
	 */
	void enterElementValuePairs(KiwiParser.ElementValuePairsContext ctx);
	/**
	 * Exit a parse tree produced by {@link KiwiParser#elementValuePairs}.
	 * @param ctx the parse tree
	 */
	void exitElementValuePairs(KiwiParser.ElementValuePairsContext ctx);
	/**
	 * Enter a parse tree produced by {@link KiwiParser#elementValuePair}.
	 * @param ctx the parse tree
	 */
	void enterElementValuePair(KiwiParser.ElementValuePairContext ctx);
	/**
	 * Exit a parse tree produced by {@link KiwiParser#elementValuePair}.
	 * @param ctx the parse tree
	 */
	void exitElementValuePair(KiwiParser.ElementValuePairContext ctx);
}