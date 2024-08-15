// Generated from MetaVMParser.g4 by ANTLR 4.13.1
package org.metavm.expression.antlr;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link MetaVMParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface MetaVMParserVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link MetaVMParser#primary}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrimary(MetaVMParser.PrimaryContext ctx);
	/**
	 * Visit a parse tree produced by {@link MetaVMParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpression(MetaVMParser.ExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link MetaVMParser#allMatch}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAllMatch(MetaVMParser.AllMatchContext ctx);
	/**
	 * Visit a parse tree produced by {@link MetaVMParser#list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitList(MetaVMParser.ListContext ctx);
	/**
	 * Visit a parse tree produced by {@link MetaVMParser#pattern}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPattern(MetaVMParser.PatternContext ctx);
	/**
	 * Visit a parse tree produced by {@link MetaVMParser#variableModifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVariableModifier(MetaVMParser.VariableModifierContext ctx);
	/**
	 * Visit a parse tree produced by {@link MetaVMParser#innerCreator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInnerCreator(MetaVMParser.InnerCreatorContext ctx);
	/**
	 * Visit a parse tree produced by {@link MetaVMParser#creator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreator(MetaVMParser.CreatorContext ctx);
	/**
	 * Visit a parse tree produced by {@link MetaVMParser#nonWildcardTypeArgumentsOrDiamond}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNonWildcardTypeArgumentsOrDiamond(MetaVMParser.NonWildcardTypeArgumentsOrDiamondContext ctx);
	/**
	 * Visit a parse tree produced by {@link MetaVMParser#explicitGenericInvocation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExplicitGenericInvocation(MetaVMParser.ExplicitGenericInvocationContext ctx);
	/**
	 * Visit a parse tree produced by {@link MetaVMParser#classCreatorRest}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitClassCreatorRest(MetaVMParser.ClassCreatorRestContext ctx);
	/**
	 * Visit a parse tree produced by {@link MetaVMParser#arrayCreatorRest}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArrayCreatorRest(MetaVMParser.ArrayCreatorRestContext ctx);
	/**
	 * Visit a parse tree produced by {@link MetaVMParser#arrayInitializer}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArrayInitializer(MetaVMParser.ArrayInitializerContext ctx);
	/**
	 * Visit a parse tree produced by {@link MetaVMParser#variableInitializer}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVariableInitializer(MetaVMParser.VariableInitializerContext ctx);
	/**
	 * Visit a parse tree produced by {@link MetaVMParser#createdName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreatedName(MetaVMParser.CreatedNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link MetaVMParser#typeArgumentsOrDiamond}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeArgumentsOrDiamond(MetaVMParser.TypeArgumentsOrDiamondContext ctx);
	/**
	 * Visit a parse tree produced by {@link MetaVMParser#methodCall}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMethodCall(MetaVMParser.MethodCallContext ctx);
	/**
	 * Visit a parse tree produced by {@link MetaVMParser#expressionList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpressionList(MetaVMParser.ExpressionListContext ctx);
	/**
	 * Visit a parse tree produced by {@link MetaVMParser#explicitGenericInvocationSuffix}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExplicitGenericInvocationSuffix(MetaVMParser.ExplicitGenericInvocationSuffixContext ctx);
	/**
	 * Visit a parse tree produced by {@link MetaVMParser#superSuffix}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSuperSuffix(MetaVMParser.SuperSuffixContext ctx);
	/**
	 * Visit a parse tree produced by {@link MetaVMParser#arguments}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArguments(MetaVMParser.ArgumentsContext ctx);
	/**
	 * Visit a parse tree produced by {@link MetaVMParser#elementValueArrayInitializer}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitElementValueArrayInitializer(MetaVMParser.ElementValueArrayInitializerContext ctx);
	/**
	 * Visit a parse tree produced by {@link MetaVMParser#literal}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLiteral(MetaVMParser.LiteralContext ctx);
	/**
	 * Visit a parse tree produced by {@link MetaVMParser#integerLiteral}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIntegerLiteral(MetaVMParser.IntegerLiteralContext ctx);
	/**
	 * Visit a parse tree produced by {@link MetaVMParser#floatLiteral}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFloatLiteral(MetaVMParser.FloatLiteralContext ctx);
	/**
	 * Visit a parse tree produced by {@link MetaVMParser#identifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIdentifier(MetaVMParser.IdentifierContext ctx);
	/**
	 * Visit a parse tree produced by {@link MetaVMParser#typeIdentifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeIdentifier(MetaVMParser.TypeIdentifierContext ctx);
	/**
	 * Visit a parse tree produced by {@link MetaVMParser#typeTypeOrVoid}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeTypeOrVoid(MetaVMParser.TypeTypeOrVoidContext ctx);
	/**
	 * Visit a parse tree produced by {@link MetaVMParser#elementValuePairs}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitElementValuePairs(MetaVMParser.ElementValuePairsContext ctx);
	/**
	 * Visit a parse tree produced by {@link MetaVMParser#elementValuePair}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitElementValuePair(MetaVMParser.ElementValuePairContext ctx);
	/**
	 * Visit a parse tree produced by {@link MetaVMParser#elementValue}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitElementValue(MetaVMParser.ElementValueContext ctx);
	/**
	 * Visit a parse tree produced by {@link MetaVMParser#qualifiedName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitQualifiedName(MetaVMParser.QualifiedNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link MetaVMParser#altAnnotationQualifiedName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAltAnnotationQualifiedName(MetaVMParser.AltAnnotationQualifiedNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link MetaVMParser#annotation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAnnotation(MetaVMParser.AnnotationContext ctx);
	/**
	 * Visit a parse tree produced by {@link MetaVMParser#typeArgument}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeArgument(MetaVMParser.TypeArgumentContext ctx);
	/**
	 * Visit a parse tree produced by {@link MetaVMParser#typeArguments}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeArguments(MetaVMParser.TypeArgumentsContext ctx);
	/**
	 * Visit a parse tree produced by {@link MetaVMParser#classOrInterfaceType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitClassOrInterfaceType(MetaVMParser.ClassOrInterfaceTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link MetaVMParser#typeType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeType(MetaVMParser.TypeTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link MetaVMParser#primitiveType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrimitiveType(MetaVMParser.PrimitiveTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link MetaVMParser#nonWildcardTypeArguments}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNonWildcardTypeArguments(MetaVMParser.NonWildcardTypeArgumentsContext ctx);
	/**
	 * Visit a parse tree produced by {@link MetaVMParser#typeList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeList(MetaVMParser.TypeListContext ctx);
}