package org.metavm.object.type;

import org.antlr.v4.runtime.tree.ParseTree;
import org.metavm.common.AbstractParseTreeTransformer;
import org.metavm.object.type.antlr.TypeParser;
import org.metavm.object.type.antlr.TypeParserVisitor;

public class TypeParserTransformer extends AbstractParseTreeTransformer implements TypeParserVisitor<ParseTree> {

    @Override
    public ParseTree visitUnit(TypeParser.UnitContext ctx) {
        var transformed = new TypeParser.UnitContext(getCurrent(), ctx.invokingState);
        transformChildren(ctx, transformed);
        return transformed;
    }

    @Override
    public TypeParser.TypeContext visitType(TypeParser.TypeContext ctx) {
        var transformed = new TypeParser.TypeContext(getCurrent(), ctx.invokingState);
        transformChildren(ctx, transformed);
        return transformed;
    }

    @Override
    public ParseTree visitMethodRef(TypeParser.MethodRefContext ctx) {
        var transformed = new TypeParser.MethodRefContext(getCurrent(), ctx.invokingState);
        transformChildren(ctx, transformed);
        return transformed;
    }

    @Override
    public ParseTree visitSimpleMethodRef(TypeParser.SimpleMethodRefContext ctx) {
        var transformed = new TypeParser.SimpleMethodRefContext(getCurrent(), ctx.invokingState);
        transformChildren(ctx, transformed);
        return transformed;
    }

    @Override
    public ParseTree visitArrayKind(TypeParser.ArrayKindContext ctx) {
        var transformed = new TypeParser.ArrayKindContext(getCurrent(), ctx.invokingState);
        transformChildren(ctx, transformed);
        return transformed;
    }

    @Override
    public ParseTree visitClassType(TypeParser.ClassTypeContext ctx) {
        var transformed = new TypeParser.ClassTypeContext(getCurrent(), ctx.invokingState);
        transformChildren(ctx, transformed);
        return transformed;
    }

    @Override
    public ParseTree visitVariableType(TypeParser.VariableTypeContext ctx) {
        var transformed = new TypeParser.VariableTypeContext(getCurrent(), ctx.invokingState);
        transformChildren(ctx, transformed);
        return transformed;
    }

    @Override
    public ParseTree visitTypeArguments(TypeParser.TypeArgumentsContext ctx) {
        var transformed = new TypeParser.TypeArgumentsContext(getCurrent(), ctx.invokingState);
        transformChildren(ctx, transformed);
        return transformed;
    }

    @Override
    public ParseTree visitPrimitiveType(TypeParser.PrimitiveTypeContext ctx) {
        var transformed = new TypeParser.PrimitiveTypeContext(getCurrent(), ctx.invokingState);
        transformChildren(ctx, transformed);
        return transformed;
    }

    @Override
    public ParseTree visitTypeList(TypeParser.TypeListContext ctx) {
        var transformed = new TypeParser.TypeListContext(getCurrent(), ctx.invokingState);
        transformChildren(ctx, transformed);
        return transformed;
    }

    @Override
    public ParseTree visitQualifiedName(TypeParser.QualifiedNameContext ctx) {
        var transformed = new TypeParser.QualifiedNameContext(getCurrent(), ctx.invokingState);
        transformChildren(ctx, transformed);
        return transformed;
    }

    @Override
    public ParseTree visitFunctionSignature(TypeParser.FunctionSignatureContext ctx) {
        var transformed = new TypeParser.FunctionSignatureContext(getCurrent(), ctx.invokingState);
        transformChildren(ctx, transformed);
        return transformed;
    }

    @Override
    public ParseTree visitParameterList(TypeParser.ParameterListContext ctx) {
        var transformed = new TypeParser.ParameterListContext(getCurrent(), ctx.invokingState);
        transformChildren(ctx, transformed);
        return transformed;
    }

    @Override
    public ParseTree visitParameter(TypeParser.ParameterContext ctx) {
        var transformed = new TypeParser.ParameterContext(getCurrent(), ctx.invokingState);
        transformChildren(ctx, transformed);
        return transformed;
    }

    @Override
    public ParseTree visitTypeParameterList(TypeParser.TypeParameterListContext ctx) {
        var transformed = new TypeParser.TypeParameterListContext(getCurrent(), ctx.invokingState);
        transformChildren(ctx, transformed);
        return transformed;
    }

    @Override
    public ParseTree visitTypeParameter(TypeParser.TypeParameterContext ctx) {
        var transformed = new TypeParser.TypeParameterContext(getCurrent(), ctx.invokingState);
        transformChildren(ctx, transformed);
        return transformed;
    }

}

