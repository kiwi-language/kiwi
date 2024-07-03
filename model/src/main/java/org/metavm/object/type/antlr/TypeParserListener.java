// Generated from TypeParser.g4 by ANTLR 4.13.1
package org.metavm.object.type.antlr;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link TypeParser}.
 */
public interface TypeParserListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link TypeParser#unit}.
	 * @param ctx the parse tree
	 */
	void enterUnit(TypeParser.UnitContext ctx);
	/**
	 * Exit a parse tree produced by {@link TypeParser#unit}.
	 * @param ctx the parse tree
	 */
	void exitUnit(TypeParser.UnitContext ctx);
	/**
	 * Enter a parse tree produced by {@link TypeParser#type}.
	 * @param ctx the parse tree
	 */
	void enterType(TypeParser.TypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link TypeParser#type}.
	 * @param ctx the parse tree
	 */
	void exitType(TypeParser.TypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link TypeParser#methodRef}.
	 * @param ctx the parse tree
	 */
	void enterMethodRef(TypeParser.MethodRefContext ctx);
	/**
	 * Exit a parse tree produced by {@link TypeParser#methodRef}.
	 * @param ctx the parse tree
	 */
	void exitMethodRef(TypeParser.MethodRefContext ctx);
	/**
	 * Enter a parse tree produced by {@link TypeParser#simpleMethodRef}.
	 * @param ctx the parse tree
	 */
	void enterSimpleMethodRef(TypeParser.SimpleMethodRefContext ctx);
	/**
	 * Exit a parse tree produced by {@link TypeParser#simpleMethodRef}.
	 * @param ctx the parse tree
	 */
	void exitSimpleMethodRef(TypeParser.SimpleMethodRefContext ctx);
	/**
	 * Enter a parse tree produced by {@link TypeParser#arrayKind}.
	 * @param ctx the parse tree
	 */
	void enterArrayKind(TypeParser.ArrayKindContext ctx);
	/**
	 * Exit a parse tree produced by {@link TypeParser#arrayKind}.
	 * @param ctx the parse tree
	 */
	void exitArrayKind(TypeParser.ArrayKindContext ctx);
	/**
	 * Enter a parse tree produced by {@link TypeParser#classType}.
	 * @param ctx the parse tree
	 */
	void enterClassType(TypeParser.ClassTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link TypeParser#classType}.
	 * @param ctx the parse tree
	 */
	void exitClassType(TypeParser.ClassTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link TypeParser#variableType}.
	 * @param ctx the parse tree
	 */
	void enterVariableType(TypeParser.VariableTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link TypeParser#variableType}.
	 * @param ctx the parse tree
	 */
	void exitVariableType(TypeParser.VariableTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link TypeParser#typeArguments}.
	 * @param ctx the parse tree
	 */
	void enterTypeArguments(TypeParser.TypeArgumentsContext ctx);
	/**
	 * Exit a parse tree produced by {@link TypeParser#typeArguments}.
	 * @param ctx the parse tree
	 */
	void exitTypeArguments(TypeParser.TypeArgumentsContext ctx);
	/**
	 * Enter a parse tree produced by {@link TypeParser#primitiveType}.
	 * @param ctx the parse tree
	 */
	void enterPrimitiveType(TypeParser.PrimitiveTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link TypeParser#primitiveType}.
	 * @param ctx the parse tree
	 */
	void exitPrimitiveType(TypeParser.PrimitiveTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link TypeParser#typeList}.
	 * @param ctx the parse tree
	 */
	void enterTypeList(TypeParser.TypeListContext ctx);
	/**
	 * Exit a parse tree produced by {@link TypeParser#typeList}.
	 * @param ctx the parse tree
	 */
	void exitTypeList(TypeParser.TypeListContext ctx);
	/**
	 * Enter a parse tree produced by {@link TypeParser#qualifiedName}.
	 * @param ctx the parse tree
	 */
	void enterQualifiedName(TypeParser.QualifiedNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link TypeParser#qualifiedName}.
	 * @param ctx the parse tree
	 */
	void exitQualifiedName(TypeParser.QualifiedNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link TypeParser#functionSignature}.
	 * @param ctx the parse tree
	 */
	void enterFunctionSignature(TypeParser.FunctionSignatureContext ctx);
	/**
	 * Exit a parse tree produced by {@link TypeParser#functionSignature}.
	 * @param ctx the parse tree
	 */
	void exitFunctionSignature(TypeParser.FunctionSignatureContext ctx);
	/**
	 * Enter a parse tree produced by {@link TypeParser#parameterList}.
	 * @param ctx the parse tree
	 */
	void enterParameterList(TypeParser.ParameterListContext ctx);
	/**
	 * Exit a parse tree produced by {@link TypeParser#parameterList}.
	 * @param ctx the parse tree
	 */
	void exitParameterList(TypeParser.ParameterListContext ctx);
	/**
	 * Enter a parse tree produced by {@link TypeParser#parameter}.
	 * @param ctx the parse tree
	 */
	void enterParameter(TypeParser.ParameterContext ctx);
	/**
	 * Exit a parse tree produced by {@link TypeParser#parameter}.
	 * @param ctx the parse tree
	 */
	void exitParameter(TypeParser.ParameterContext ctx);
	/**
	 * Enter a parse tree produced by {@link TypeParser#typeParameterList}.
	 * @param ctx the parse tree
	 */
	void enterTypeParameterList(TypeParser.TypeParameterListContext ctx);
	/**
	 * Exit a parse tree produced by {@link TypeParser#typeParameterList}.
	 * @param ctx the parse tree
	 */
	void exitTypeParameterList(TypeParser.TypeParameterListContext ctx);
	/**
	 * Enter a parse tree produced by {@link TypeParser#typeParameter}.
	 * @param ctx the parse tree
	 */
	void enterTypeParameter(TypeParser.TypeParameterContext ctx);
	/**
	 * Exit a parse tree produced by {@link TypeParser#typeParameter}.
	 * @param ctx the parse tree
	 */
	void exitTypeParameter(TypeParser.TypeParameterContext ctx);
}