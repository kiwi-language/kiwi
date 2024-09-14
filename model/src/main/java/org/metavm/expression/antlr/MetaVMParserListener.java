// Generated from MetaVMParser.g4 by ANTLR 4.13.2
package org.metavm.expression.antlr;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link MetaVMParser}.
 */
public interface MetaVMParserListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link MetaVMParser#primary}.
	 * @param ctx the parse tree
	 */
	void enterPrimary(MetaVMParser.PrimaryContext ctx);
	/**
	 * Exit a parse tree produced by {@link MetaVMParser#primary}.
	 * @param ctx the parse tree
	 */
	void exitPrimary(MetaVMParser.PrimaryContext ctx);
	/**
	 * Enter a parse tree produced by {@link MetaVMParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterExpression(MetaVMParser.ExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link MetaVMParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitExpression(MetaVMParser.ExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link MetaVMParser#allMatch}.
	 * @param ctx the parse tree
	 */
	void enterAllMatch(MetaVMParser.AllMatchContext ctx);
	/**
	 * Exit a parse tree produced by {@link MetaVMParser#allMatch}.
	 * @param ctx the parse tree
	 */
	void exitAllMatch(MetaVMParser.AllMatchContext ctx);
	/**
	 * Enter a parse tree produced by {@link MetaVMParser#list}.
	 * @param ctx the parse tree
	 */
	void enterList(MetaVMParser.ListContext ctx);
	/**
	 * Exit a parse tree produced by {@link MetaVMParser#list}.
	 * @param ctx the parse tree
	 */
	void exitList(MetaVMParser.ListContext ctx);
	/**
	 * Enter a parse tree produced by {@link MetaVMParser#pattern}.
	 * @param ctx the parse tree
	 */
	void enterPattern(MetaVMParser.PatternContext ctx);
	/**
	 * Exit a parse tree produced by {@link MetaVMParser#pattern}.
	 * @param ctx the parse tree
	 */
	void exitPattern(MetaVMParser.PatternContext ctx);
	/**
	 * Enter a parse tree produced by {@link MetaVMParser#variableModifier}.
	 * @param ctx the parse tree
	 */
	void enterVariableModifier(MetaVMParser.VariableModifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link MetaVMParser#variableModifier}.
	 * @param ctx the parse tree
	 */
	void exitVariableModifier(MetaVMParser.VariableModifierContext ctx);
	/**
	 * Enter a parse tree produced by {@link MetaVMParser#innerCreator}.
	 * @param ctx the parse tree
	 */
	void enterInnerCreator(MetaVMParser.InnerCreatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link MetaVMParser#innerCreator}.
	 * @param ctx the parse tree
	 */
	void exitInnerCreator(MetaVMParser.InnerCreatorContext ctx);
	/**
	 * Enter a parse tree produced by {@link MetaVMParser#creator}.
	 * @param ctx the parse tree
	 */
	void enterCreator(MetaVMParser.CreatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link MetaVMParser#creator}.
	 * @param ctx the parse tree
	 */
	void exitCreator(MetaVMParser.CreatorContext ctx);
	/**
	 * Enter a parse tree produced by {@link MetaVMParser#nonWildcardTypeArgumentsOrDiamond}.
	 * @param ctx the parse tree
	 */
	void enterNonWildcardTypeArgumentsOrDiamond(MetaVMParser.NonWildcardTypeArgumentsOrDiamondContext ctx);
	/**
	 * Exit a parse tree produced by {@link MetaVMParser#nonWildcardTypeArgumentsOrDiamond}.
	 * @param ctx the parse tree
	 */
	void exitNonWildcardTypeArgumentsOrDiamond(MetaVMParser.NonWildcardTypeArgumentsOrDiamondContext ctx);
	/**
	 * Enter a parse tree produced by {@link MetaVMParser#explicitGenericInvocation}.
	 * @param ctx the parse tree
	 */
	void enterExplicitGenericInvocation(MetaVMParser.ExplicitGenericInvocationContext ctx);
	/**
	 * Exit a parse tree produced by {@link MetaVMParser#explicitGenericInvocation}.
	 * @param ctx the parse tree
	 */
	void exitExplicitGenericInvocation(MetaVMParser.ExplicitGenericInvocationContext ctx);
	/**
	 * Enter a parse tree produced by {@link MetaVMParser#classCreatorRest}.
	 * @param ctx the parse tree
	 */
	void enterClassCreatorRest(MetaVMParser.ClassCreatorRestContext ctx);
	/**
	 * Exit a parse tree produced by {@link MetaVMParser#classCreatorRest}.
	 * @param ctx the parse tree
	 */
	void exitClassCreatorRest(MetaVMParser.ClassCreatorRestContext ctx);
	/**
	 * Enter a parse tree produced by {@link MetaVMParser#arrayCreatorRest}.
	 * @param ctx the parse tree
	 */
	void enterArrayCreatorRest(MetaVMParser.ArrayCreatorRestContext ctx);
	/**
	 * Exit a parse tree produced by {@link MetaVMParser#arrayCreatorRest}.
	 * @param ctx the parse tree
	 */
	void exitArrayCreatorRest(MetaVMParser.ArrayCreatorRestContext ctx);
	/**
	 * Enter a parse tree produced by {@link MetaVMParser#arrayInitializer}.
	 * @param ctx the parse tree
	 */
	void enterArrayInitializer(MetaVMParser.ArrayInitializerContext ctx);
	/**
	 * Exit a parse tree produced by {@link MetaVMParser#arrayInitializer}.
	 * @param ctx the parse tree
	 */
	void exitArrayInitializer(MetaVMParser.ArrayInitializerContext ctx);
	/**
	 * Enter a parse tree produced by {@link MetaVMParser#variableInitializer}.
	 * @param ctx the parse tree
	 */
	void enterVariableInitializer(MetaVMParser.VariableInitializerContext ctx);
	/**
	 * Exit a parse tree produced by {@link MetaVMParser#variableInitializer}.
	 * @param ctx the parse tree
	 */
	void exitVariableInitializer(MetaVMParser.VariableInitializerContext ctx);
	/**
	 * Enter a parse tree produced by {@link MetaVMParser#createdName}.
	 * @param ctx the parse tree
	 */
	void enterCreatedName(MetaVMParser.CreatedNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link MetaVMParser#createdName}.
	 * @param ctx the parse tree
	 */
	void exitCreatedName(MetaVMParser.CreatedNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link MetaVMParser#typeArgumentsOrDiamond}.
	 * @param ctx the parse tree
	 */
	void enterTypeArgumentsOrDiamond(MetaVMParser.TypeArgumentsOrDiamondContext ctx);
	/**
	 * Exit a parse tree produced by {@link MetaVMParser#typeArgumentsOrDiamond}.
	 * @param ctx the parse tree
	 */
	void exitTypeArgumentsOrDiamond(MetaVMParser.TypeArgumentsOrDiamondContext ctx);
	/**
	 * Enter a parse tree produced by {@link MetaVMParser#methodCall}.
	 * @param ctx the parse tree
	 */
	void enterMethodCall(MetaVMParser.MethodCallContext ctx);
	/**
	 * Exit a parse tree produced by {@link MetaVMParser#methodCall}.
	 * @param ctx the parse tree
	 */
	void exitMethodCall(MetaVMParser.MethodCallContext ctx);
	/**
	 * Enter a parse tree produced by {@link MetaVMParser#expressionList}.
	 * @param ctx the parse tree
	 */
	void enterExpressionList(MetaVMParser.ExpressionListContext ctx);
	/**
	 * Exit a parse tree produced by {@link MetaVMParser#expressionList}.
	 * @param ctx the parse tree
	 */
	void exitExpressionList(MetaVMParser.ExpressionListContext ctx);
	/**
	 * Enter a parse tree produced by {@link MetaVMParser#explicitGenericInvocationSuffix}.
	 * @param ctx the parse tree
	 */
	void enterExplicitGenericInvocationSuffix(MetaVMParser.ExplicitGenericInvocationSuffixContext ctx);
	/**
	 * Exit a parse tree produced by {@link MetaVMParser#explicitGenericInvocationSuffix}.
	 * @param ctx the parse tree
	 */
	void exitExplicitGenericInvocationSuffix(MetaVMParser.ExplicitGenericInvocationSuffixContext ctx);
	/**
	 * Enter a parse tree produced by {@link MetaVMParser#superSuffix}.
	 * @param ctx the parse tree
	 */
	void enterSuperSuffix(MetaVMParser.SuperSuffixContext ctx);
	/**
	 * Exit a parse tree produced by {@link MetaVMParser#superSuffix}.
	 * @param ctx the parse tree
	 */
	void exitSuperSuffix(MetaVMParser.SuperSuffixContext ctx);
	/**
	 * Enter a parse tree produced by {@link MetaVMParser#arguments}.
	 * @param ctx the parse tree
	 */
	void enterArguments(MetaVMParser.ArgumentsContext ctx);
	/**
	 * Exit a parse tree produced by {@link MetaVMParser#arguments}.
	 * @param ctx the parse tree
	 */
	void exitArguments(MetaVMParser.ArgumentsContext ctx);
	/**
	 * Enter a parse tree produced by {@link MetaVMParser#elementValueArrayInitializer}.
	 * @param ctx the parse tree
	 */
	void enterElementValueArrayInitializer(MetaVMParser.ElementValueArrayInitializerContext ctx);
	/**
	 * Exit a parse tree produced by {@link MetaVMParser#elementValueArrayInitializer}.
	 * @param ctx the parse tree
	 */
	void exitElementValueArrayInitializer(MetaVMParser.ElementValueArrayInitializerContext ctx);
	/**
	 * Enter a parse tree produced by {@link MetaVMParser#literal}.
	 * @param ctx the parse tree
	 */
	void enterLiteral(MetaVMParser.LiteralContext ctx);
	/**
	 * Exit a parse tree produced by {@link MetaVMParser#literal}.
	 * @param ctx the parse tree
	 */
	void exitLiteral(MetaVMParser.LiteralContext ctx);
	/**
	 * Enter a parse tree produced by {@link MetaVMParser#integerLiteral}.
	 * @param ctx the parse tree
	 */
	void enterIntegerLiteral(MetaVMParser.IntegerLiteralContext ctx);
	/**
	 * Exit a parse tree produced by {@link MetaVMParser#integerLiteral}.
	 * @param ctx the parse tree
	 */
	void exitIntegerLiteral(MetaVMParser.IntegerLiteralContext ctx);
	/**
	 * Enter a parse tree produced by {@link MetaVMParser#floatLiteral}.
	 * @param ctx the parse tree
	 */
	void enterFloatLiteral(MetaVMParser.FloatLiteralContext ctx);
	/**
	 * Exit a parse tree produced by {@link MetaVMParser#floatLiteral}.
	 * @param ctx the parse tree
	 */
	void exitFloatLiteral(MetaVMParser.FloatLiteralContext ctx);
	/**
	 * Enter a parse tree produced by {@link MetaVMParser#identifier}.
	 * @param ctx the parse tree
	 */
	void enterIdentifier(MetaVMParser.IdentifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link MetaVMParser#identifier}.
	 * @param ctx the parse tree
	 */
	void exitIdentifier(MetaVMParser.IdentifierContext ctx);
	/**
	 * Enter a parse tree produced by {@link MetaVMParser#typeIdentifier}.
	 * @param ctx the parse tree
	 */
	void enterTypeIdentifier(MetaVMParser.TypeIdentifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link MetaVMParser#typeIdentifier}.
	 * @param ctx the parse tree
	 */
	void exitTypeIdentifier(MetaVMParser.TypeIdentifierContext ctx);
	/**
	 * Enter a parse tree produced by {@link MetaVMParser#typeTypeOrVoid}.
	 * @param ctx the parse tree
	 */
	void enterTypeTypeOrVoid(MetaVMParser.TypeTypeOrVoidContext ctx);
	/**
	 * Exit a parse tree produced by {@link MetaVMParser#typeTypeOrVoid}.
	 * @param ctx the parse tree
	 */
	void exitTypeTypeOrVoid(MetaVMParser.TypeTypeOrVoidContext ctx);
	/**
	 * Enter a parse tree produced by {@link MetaVMParser#elementValuePairs}.
	 * @param ctx the parse tree
	 */
	void enterElementValuePairs(MetaVMParser.ElementValuePairsContext ctx);
	/**
	 * Exit a parse tree produced by {@link MetaVMParser#elementValuePairs}.
	 * @param ctx the parse tree
	 */
	void exitElementValuePairs(MetaVMParser.ElementValuePairsContext ctx);
	/**
	 * Enter a parse tree produced by {@link MetaVMParser#elementValuePair}.
	 * @param ctx the parse tree
	 */
	void enterElementValuePair(MetaVMParser.ElementValuePairContext ctx);
	/**
	 * Exit a parse tree produced by {@link MetaVMParser#elementValuePair}.
	 * @param ctx the parse tree
	 */
	void exitElementValuePair(MetaVMParser.ElementValuePairContext ctx);
	/**
	 * Enter a parse tree produced by {@link MetaVMParser#elementValue}.
	 * @param ctx the parse tree
	 */
	void enterElementValue(MetaVMParser.ElementValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link MetaVMParser#elementValue}.
	 * @param ctx the parse tree
	 */
	void exitElementValue(MetaVMParser.ElementValueContext ctx);
	/**
	 * Enter a parse tree produced by {@link MetaVMParser#qualifiedName}.
	 * @param ctx the parse tree
	 */
	void enterQualifiedName(MetaVMParser.QualifiedNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link MetaVMParser#qualifiedName}.
	 * @param ctx the parse tree
	 */
	void exitQualifiedName(MetaVMParser.QualifiedNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link MetaVMParser#altAnnotationQualifiedName}.
	 * @param ctx the parse tree
	 */
	void enterAltAnnotationQualifiedName(MetaVMParser.AltAnnotationQualifiedNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link MetaVMParser#altAnnotationQualifiedName}.
	 * @param ctx the parse tree
	 */
	void exitAltAnnotationQualifiedName(MetaVMParser.AltAnnotationQualifiedNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link MetaVMParser#annotation}.
	 * @param ctx the parse tree
	 */
	void enterAnnotation(MetaVMParser.AnnotationContext ctx);
	/**
	 * Exit a parse tree produced by {@link MetaVMParser#annotation}.
	 * @param ctx the parse tree
	 */
	void exitAnnotation(MetaVMParser.AnnotationContext ctx);
	/**
	 * Enter a parse tree produced by {@link MetaVMParser#typeArguments}.
	 * @param ctx the parse tree
	 */
	void enterTypeArguments(MetaVMParser.TypeArgumentsContext ctx);
	/**
	 * Exit a parse tree produced by {@link MetaVMParser#typeArguments}.
	 * @param ctx the parse tree
	 */
	void exitTypeArguments(MetaVMParser.TypeArgumentsContext ctx);
	/**
	 * Enter a parse tree produced by {@link MetaVMParser#classOrInterfaceType}.
	 * @param ctx the parse tree
	 */
	void enterClassOrInterfaceType(MetaVMParser.ClassOrInterfaceTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link MetaVMParser#classOrInterfaceType}.
	 * @param ctx the parse tree
	 */
	void exitClassOrInterfaceType(MetaVMParser.ClassOrInterfaceTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link MetaVMParser#typeType}.
	 * @param ctx the parse tree
	 */
	void enterTypeType(MetaVMParser.TypeTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link MetaVMParser#typeType}.
	 * @param ctx the parse tree
	 */
	void exitTypeType(MetaVMParser.TypeTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link MetaVMParser#arrayKind}.
	 * @param ctx the parse tree
	 */
	void enterArrayKind(MetaVMParser.ArrayKindContext ctx);
	/**
	 * Exit a parse tree produced by {@link MetaVMParser#arrayKind}.
	 * @param ctx the parse tree
	 */
	void exitArrayKind(MetaVMParser.ArrayKindContext ctx);
	/**
	 * Enter a parse tree produced by {@link MetaVMParser#primitiveType}.
	 * @param ctx the parse tree
	 */
	void enterPrimitiveType(MetaVMParser.PrimitiveTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link MetaVMParser#primitiveType}.
	 * @param ctx the parse tree
	 */
	void exitPrimitiveType(MetaVMParser.PrimitiveTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link MetaVMParser#nonWildcardTypeArguments}.
	 * @param ctx the parse tree
	 */
	void enterNonWildcardTypeArguments(MetaVMParser.NonWildcardTypeArgumentsContext ctx);
	/**
	 * Exit a parse tree produced by {@link MetaVMParser#nonWildcardTypeArguments}.
	 * @param ctx the parse tree
	 */
	void exitNonWildcardTypeArguments(MetaVMParser.NonWildcardTypeArgumentsContext ctx);
	/**
	 * Enter a parse tree produced by {@link MetaVMParser#typeList}.
	 * @param ctx the parse tree
	 */
	void enterTypeList(MetaVMParser.TypeListContext ctx);
	/**
	 * Exit a parse tree produced by {@link MetaVMParser#typeList}.
	 * @param ctx the parse tree
	 */
	void exitTypeList(MetaVMParser.TypeListContext ctx);
}