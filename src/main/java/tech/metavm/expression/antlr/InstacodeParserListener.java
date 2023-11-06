package tech.metavm.expression.antlr;// Generated from InstacodeParser.g4 by ANTLR 4.13.1
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link InstacodeParser}.
 */
public interface InstacodeParserListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link InstacodeParser#primary}.
	 * @param ctx the parse tree
	 */
	void enterPrimary(InstacodeParser.PrimaryContext ctx);
	/**
	 * Exit a parse tree produced by {@link InstacodeParser#primary}.
	 * @param ctx the parse tree
	 */
	void exitPrimary(InstacodeParser.PrimaryContext ctx);
	/**
	 * Enter a parse tree produced by {@link InstacodeParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterExpression(InstacodeParser.ExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link InstacodeParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitExpression(InstacodeParser.ExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link InstacodeParser#allMatch}.
	 * @param ctx the parse tree
	 */
	void enterAllMatch(InstacodeParser.AllMatchContext ctx);
	/**
	 * Exit a parse tree produced by {@link InstacodeParser#allMatch}.
	 * @param ctx the parse tree
	 */
	void exitAllMatch(InstacodeParser.AllMatchContext ctx);
	/**
	 * Enter a parse tree produced by {@link InstacodeParser#list}.
	 * @param ctx the parse tree
	 */
	void enterList(InstacodeParser.ListContext ctx);
	/**
	 * Exit a parse tree produced by {@link InstacodeParser#list}.
	 * @param ctx the parse tree
	 */
	void exitList(InstacodeParser.ListContext ctx);
	/**
	 * Enter a parse tree produced by {@link InstacodeParser#pattern}.
	 * @param ctx the parse tree
	 */
	void enterPattern(InstacodeParser.PatternContext ctx);
	/**
	 * Exit a parse tree produced by {@link InstacodeParser#pattern}.
	 * @param ctx the parse tree
	 */
	void exitPattern(InstacodeParser.PatternContext ctx);
	/**
	 * Enter a parse tree produced by {@link InstacodeParser#variableModifier}.
	 * @param ctx the parse tree
	 */
	void enterVariableModifier(InstacodeParser.VariableModifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link InstacodeParser#variableModifier}.
	 * @param ctx the parse tree
	 */
	void exitVariableModifier(InstacodeParser.VariableModifierContext ctx);
	/**
	 * Enter a parse tree produced by {@link InstacodeParser#innerCreator}.
	 * @param ctx the parse tree
	 */
	void enterInnerCreator(InstacodeParser.InnerCreatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link InstacodeParser#innerCreator}.
	 * @param ctx the parse tree
	 */
	void exitInnerCreator(InstacodeParser.InnerCreatorContext ctx);
	/**
	 * Enter a parse tree produced by {@link InstacodeParser#creator}.
	 * @param ctx the parse tree
	 */
	void enterCreator(InstacodeParser.CreatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link InstacodeParser#creator}.
	 * @param ctx the parse tree
	 */
	void exitCreator(InstacodeParser.CreatorContext ctx);
	/**
	 * Enter a parse tree produced by {@link InstacodeParser#nonWildcardTypeArgumentsOrDiamond}.
	 * @param ctx the parse tree
	 */
	void enterNonWildcardTypeArgumentsOrDiamond(InstacodeParser.NonWildcardTypeArgumentsOrDiamondContext ctx);
	/**
	 * Exit a parse tree produced by {@link InstacodeParser#nonWildcardTypeArgumentsOrDiamond}.
	 * @param ctx the parse tree
	 */
	void exitNonWildcardTypeArgumentsOrDiamond(InstacodeParser.NonWildcardTypeArgumentsOrDiamondContext ctx);
	/**
	 * Enter a parse tree produced by {@link InstacodeParser#explicitGenericInvocation}.
	 * @param ctx the parse tree
	 */
	void enterExplicitGenericInvocation(InstacodeParser.ExplicitGenericInvocationContext ctx);
	/**
	 * Exit a parse tree produced by {@link InstacodeParser#explicitGenericInvocation}.
	 * @param ctx the parse tree
	 */
	void exitExplicitGenericInvocation(InstacodeParser.ExplicitGenericInvocationContext ctx);
	/**
	 * Enter a parse tree produced by {@link InstacodeParser#classCreatorRest}.
	 * @param ctx the parse tree
	 */
	void enterClassCreatorRest(InstacodeParser.ClassCreatorRestContext ctx);
	/**
	 * Exit a parse tree produced by {@link InstacodeParser#classCreatorRest}.
	 * @param ctx the parse tree
	 */
	void exitClassCreatorRest(InstacodeParser.ClassCreatorRestContext ctx);
	/**
	 * Enter a parse tree produced by {@link InstacodeParser#arrayCreatorRest}.
	 * @param ctx the parse tree
	 */
	void enterArrayCreatorRest(InstacodeParser.ArrayCreatorRestContext ctx);
	/**
	 * Exit a parse tree produced by {@link InstacodeParser#arrayCreatorRest}.
	 * @param ctx the parse tree
	 */
	void exitArrayCreatorRest(InstacodeParser.ArrayCreatorRestContext ctx);
	/**
	 * Enter a parse tree produced by {@link InstacodeParser#arrayInitializer}.
	 * @param ctx the parse tree
	 */
	void enterArrayInitializer(InstacodeParser.ArrayInitializerContext ctx);
	/**
	 * Exit a parse tree produced by {@link InstacodeParser#arrayInitializer}.
	 * @param ctx the parse tree
	 */
	void exitArrayInitializer(InstacodeParser.ArrayInitializerContext ctx);
	/**
	 * Enter a parse tree produced by {@link InstacodeParser#variableInitializer}.
	 * @param ctx the parse tree
	 */
	void enterVariableInitializer(InstacodeParser.VariableInitializerContext ctx);
	/**
	 * Exit a parse tree produced by {@link InstacodeParser#variableInitializer}.
	 * @param ctx the parse tree
	 */
	void exitVariableInitializer(InstacodeParser.VariableInitializerContext ctx);
	/**
	 * Enter a parse tree produced by {@link InstacodeParser#createdName}.
	 * @param ctx the parse tree
	 */
	void enterCreatedName(InstacodeParser.CreatedNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link InstacodeParser#createdName}.
	 * @param ctx the parse tree
	 */
	void exitCreatedName(InstacodeParser.CreatedNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link InstacodeParser#typeArgumentsOrDiamond}.
	 * @param ctx the parse tree
	 */
	void enterTypeArgumentsOrDiamond(InstacodeParser.TypeArgumentsOrDiamondContext ctx);
	/**
	 * Exit a parse tree produced by {@link InstacodeParser#typeArgumentsOrDiamond}.
	 * @param ctx the parse tree
	 */
	void exitTypeArgumentsOrDiamond(InstacodeParser.TypeArgumentsOrDiamondContext ctx);
	/**
	 * Enter a parse tree produced by {@link InstacodeParser#methodCall}.
	 * @param ctx the parse tree
	 */
	void enterMethodCall(InstacodeParser.MethodCallContext ctx);
	/**
	 * Exit a parse tree produced by {@link InstacodeParser#methodCall}.
	 * @param ctx the parse tree
	 */
	void exitMethodCall(InstacodeParser.MethodCallContext ctx);
	/**
	 * Enter a parse tree produced by {@link InstacodeParser#expressionList}.
	 * @param ctx the parse tree
	 */
	void enterExpressionList(InstacodeParser.ExpressionListContext ctx);
	/**
	 * Exit a parse tree produced by {@link InstacodeParser#expressionList}.
	 * @param ctx the parse tree
	 */
	void exitExpressionList(InstacodeParser.ExpressionListContext ctx);
	/**
	 * Enter a parse tree produced by {@link InstacodeParser#explicitGenericInvocationSuffix}.
	 * @param ctx the parse tree
	 */
	void enterExplicitGenericInvocationSuffix(InstacodeParser.ExplicitGenericInvocationSuffixContext ctx);
	/**
	 * Exit a parse tree produced by {@link InstacodeParser#explicitGenericInvocationSuffix}.
	 * @param ctx the parse tree
	 */
	void exitExplicitGenericInvocationSuffix(InstacodeParser.ExplicitGenericInvocationSuffixContext ctx);
	/**
	 * Enter a parse tree produced by {@link InstacodeParser#superSuffix}.
	 * @param ctx the parse tree
	 */
	void enterSuperSuffix(InstacodeParser.SuperSuffixContext ctx);
	/**
	 * Exit a parse tree produced by {@link InstacodeParser#superSuffix}.
	 * @param ctx the parse tree
	 */
	void exitSuperSuffix(InstacodeParser.SuperSuffixContext ctx);
	/**
	 * Enter a parse tree produced by {@link InstacodeParser#arguments}.
	 * @param ctx the parse tree
	 */
	void enterArguments(InstacodeParser.ArgumentsContext ctx);
	/**
	 * Exit a parse tree produced by {@link InstacodeParser#arguments}.
	 * @param ctx the parse tree
	 */
	void exitArguments(InstacodeParser.ArgumentsContext ctx);
	/**
	 * Enter a parse tree produced by {@link InstacodeParser#elementValueArrayInitializer}.
	 * @param ctx the parse tree
	 */
	void enterElementValueArrayInitializer(InstacodeParser.ElementValueArrayInitializerContext ctx);
	/**
	 * Exit a parse tree produced by {@link InstacodeParser#elementValueArrayInitializer}.
	 * @param ctx the parse tree
	 */
	void exitElementValueArrayInitializer(InstacodeParser.ElementValueArrayInitializerContext ctx);
	/**
	 * Enter a parse tree produced by {@link InstacodeParser#literal}.
	 * @param ctx the parse tree
	 */
	void enterLiteral(InstacodeParser.LiteralContext ctx);
	/**
	 * Exit a parse tree produced by {@link InstacodeParser#literal}.
	 * @param ctx the parse tree
	 */
	void exitLiteral(InstacodeParser.LiteralContext ctx);
	/**
	 * Enter a parse tree produced by {@link InstacodeParser#integerLiteral}.
	 * @param ctx the parse tree
	 */
	void enterIntegerLiteral(InstacodeParser.IntegerLiteralContext ctx);
	/**
	 * Exit a parse tree produced by {@link InstacodeParser#integerLiteral}.
	 * @param ctx the parse tree
	 */
	void exitIntegerLiteral(InstacodeParser.IntegerLiteralContext ctx);
	/**
	 * Enter a parse tree produced by {@link InstacodeParser#floatLiteral}.
	 * @param ctx the parse tree
	 */
	void enterFloatLiteral(InstacodeParser.FloatLiteralContext ctx);
	/**
	 * Exit a parse tree produced by {@link InstacodeParser#floatLiteral}.
	 * @param ctx the parse tree
	 */
	void exitFloatLiteral(InstacodeParser.FloatLiteralContext ctx);
	/**
	 * Enter a parse tree produced by {@link InstacodeParser#identifier}.
	 * @param ctx the parse tree
	 */
	void enterIdentifier(InstacodeParser.IdentifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link InstacodeParser#identifier}.
	 * @param ctx the parse tree
	 */
	void exitIdentifier(InstacodeParser.IdentifierContext ctx);
	/**
	 * Enter a parse tree produced by {@link InstacodeParser#typeIdentifier}.
	 * @param ctx the parse tree
	 */
	void enterTypeIdentifier(InstacodeParser.TypeIdentifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link InstacodeParser#typeIdentifier}.
	 * @param ctx the parse tree
	 */
	void exitTypeIdentifier(InstacodeParser.TypeIdentifierContext ctx);
	/**
	 * Enter a parse tree produced by {@link InstacodeParser#typeTypeOrVoid}.
	 * @param ctx the parse tree
	 */
	void enterTypeTypeOrVoid(InstacodeParser.TypeTypeOrVoidContext ctx);
	/**
	 * Exit a parse tree produced by {@link InstacodeParser#typeTypeOrVoid}.
	 * @param ctx the parse tree
	 */
	void exitTypeTypeOrVoid(InstacodeParser.TypeTypeOrVoidContext ctx);
	/**
	 * Enter a parse tree produced by {@link InstacodeParser#elementValuePairs}.
	 * @param ctx the parse tree
	 */
	void enterElementValuePairs(InstacodeParser.ElementValuePairsContext ctx);
	/**
	 * Exit a parse tree produced by {@link InstacodeParser#elementValuePairs}.
	 * @param ctx the parse tree
	 */
	void exitElementValuePairs(InstacodeParser.ElementValuePairsContext ctx);
	/**
	 * Enter a parse tree produced by {@link InstacodeParser#elementValuePair}.
	 * @param ctx the parse tree
	 */
	void enterElementValuePair(InstacodeParser.ElementValuePairContext ctx);
	/**
	 * Exit a parse tree produced by {@link InstacodeParser#elementValuePair}.
	 * @param ctx the parse tree
	 */
	void exitElementValuePair(InstacodeParser.ElementValuePairContext ctx);
	/**
	 * Enter a parse tree produced by {@link InstacodeParser#elementValue}.
	 * @param ctx the parse tree
	 */
	void enterElementValue(InstacodeParser.ElementValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link InstacodeParser#elementValue}.
	 * @param ctx the parse tree
	 */
	void exitElementValue(InstacodeParser.ElementValueContext ctx);
	/**
	 * Enter a parse tree produced by {@link InstacodeParser#qualifiedName}.
	 * @param ctx the parse tree
	 */
	void enterQualifiedName(InstacodeParser.QualifiedNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link InstacodeParser#qualifiedName}.
	 * @param ctx the parse tree
	 */
	void exitQualifiedName(InstacodeParser.QualifiedNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link InstacodeParser#altAnnotationQualifiedName}.
	 * @param ctx the parse tree
	 */
	void enterAltAnnotationQualifiedName(InstacodeParser.AltAnnotationQualifiedNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link InstacodeParser#altAnnotationQualifiedName}.
	 * @param ctx the parse tree
	 */
	void exitAltAnnotationQualifiedName(InstacodeParser.AltAnnotationQualifiedNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link InstacodeParser#annotation}.
	 * @param ctx the parse tree
	 */
	void enterAnnotation(InstacodeParser.AnnotationContext ctx);
	/**
	 * Exit a parse tree produced by {@link InstacodeParser#annotation}.
	 * @param ctx the parse tree
	 */
	void exitAnnotation(InstacodeParser.AnnotationContext ctx);
	/**
	 * Enter a parse tree produced by {@link InstacodeParser#typeArgument}.
	 * @param ctx the parse tree
	 */
	void enterTypeArgument(InstacodeParser.TypeArgumentContext ctx);
	/**
	 * Exit a parse tree produced by {@link InstacodeParser#typeArgument}.
	 * @param ctx the parse tree
	 */
	void exitTypeArgument(InstacodeParser.TypeArgumentContext ctx);
	/**
	 * Enter a parse tree produced by {@link InstacodeParser#typeArguments}.
	 * @param ctx the parse tree
	 */
	void enterTypeArguments(InstacodeParser.TypeArgumentsContext ctx);
	/**
	 * Exit a parse tree produced by {@link InstacodeParser#typeArguments}.
	 * @param ctx the parse tree
	 */
	void exitTypeArguments(InstacodeParser.TypeArgumentsContext ctx);
	/**
	 * Enter a parse tree produced by {@link InstacodeParser#classOrInterfaceType}.
	 * @param ctx the parse tree
	 */
	void enterClassOrInterfaceType(InstacodeParser.ClassOrInterfaceTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link InstacodeParser#classOrInterfaceType}.
	 * @param ctx the parse tree
	 */
	void exitClassOrInterfaceType(InstacodeParser.ClassOrInterfaceTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link InstacodeParser#typeType}.
	 * @param ctx the parse tree
	 */
	void enterTypeType(InstacodeParser.TypeTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link InstacodeParser#typeType}.
	 * @param ctx the parse tree
	 */
	void exitTypeType(InstacodeParser.TypeTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link InstacodeParser#primitiveType}.
	 * @param ctx the parse tree
	 */
	void enterPrimitiveType(InstacodeParser.PrimitiveTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link InstacodeParser#primitiveType}.
	 * @param ctx the parse tree
	 */
	void exitPrimitiveType(InstacodeParser.PrimitiveTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link InstacodeParser#nonWildcardTypeArguments}.
	 * @param ctx the parse tree
	 */
	void enterNonWildcardTypeArguments(InstacodeParser.NonWildcardTypeArgumentsContext ctx);
	/**
	 * Exit a parse tree produced by {@link InstacodeParser#nonWildcardTypeArguments}.
	 * @param ctx the parse tree
	 */
	void exitNonWildcardTypeArguments(InstacodeParser.NonWildcardTypeArgumentsContext ctx);
	/**
	 * Enter a parse tree produced by {@link InstacodeParser#typeList}.
	 * @param ctx the parse tree
	 */
	void enterTypeList(InstacodeParser.TypeListContext ctx);
	/**
	 * Exit a parse tree produced by {@link InstacodeParser#typeList}.
	 * @param ctx the parse tree
	 */
	void exitTypeList(InstacodeParser.TypeListContext ctx);
}