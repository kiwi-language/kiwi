// Generated from TypeParser.g4 by ANTLR 4.13.1
package org.metavm.object.type.antlr;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link TypeParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface TypeParserVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link TypeParser#type}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitType(TypeParser.TypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link TypeParser#methodRef}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMethodRef(TypeParser.MethodRefContext ctx);
	/**
	 * Visit a parse tree produced by {@link TypeParser#simpleMethodRef}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSimpleMethodRef(TypeParser.SimpleMethodRefContext ctx);
	/**
	 * Visit a parse tree produced by {@link TypeParser#arrayKind}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArrayKind(TypeParser.ArrayKindContext ctx);
	/**
	 * Visit a parse tree produced by {@link TypeParser#classType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitClassType(TypeParser.ClassTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link TypeParser#variableType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVariableType(TypeParser.VariableTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link TypeParser#typeArguments}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeArguments(TypeParser.TypeArgumentsContext ctx);
	/**
	 * Visit a parse tree produced by {@link TypeParser#primitiveType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrimitiveType(TypeParser.PrimitiveTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link TypeParser#typeList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeList(TypeParser.TypeListContext ctx);
	/**
	 * Visit a parse tree produced by {@link TypeParser#qualifiedName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitQualifiedName(TypeParser.QualifiedNameContext ctx);
}