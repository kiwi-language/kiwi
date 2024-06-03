// Generated from TypeParser.g4 by ANTLR 4.13.1
package tech.metavm.object.type.antlr;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link TypeParser}.
 */
public interface TypeParserListener extends ParseTreeListener {
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
}