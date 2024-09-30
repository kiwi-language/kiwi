package org.metavm.expression;

import org.antlr.v4.runtime.tree.ParseTree;
import org.metavm.common.AbstractParseTreeTransformer;
import org.metavm.expression.antlr.MetaVMParser;
import org.metavm.expression.antlr.MetaVMParserVisitor;

public class ExpressionParserTransformer extends AbstractParseTreeTransformer implements MetaVMParserVisitor<ParseTree> {

    @Override
    public ParseTree visitPrimary(MetaVMParser.PrimaryContext ctx) {
        var transformed = new MetaVMParser.PrimaryContext(getCurrent(), ctx.invokingState);
        transformChildren(ctx, transformed);
        return transformed;
    }

    @Override
    public ParseTree visitExpression(MetaVMParser.ExpressionContext ctx) {
        var transformed = new MetaVMParser.ExpressionContext(getCurrent(), ctx.invokingState);
        transformChildren(ctx, transformed);
        return transformed;
    }

    @Override
    public ParseTree visitAllMatch(MetaVMParser.AllMatchContext ctx) {
        var transformed = new MetaVMParser.AllMatchContext(getCurrent(), ctx.invokingState);
        transformChildren(ctx, transformed);
        return transformed;
    }

    @Override
    public ParseTree visitList(MetaVMParser.ListContext ctx) {
        var transformed = new MetaVMParser.ListContext(getCurrent(), ctx.invokingState);
        transformChildren(ctx, transformed);
        return transformed;
    }

    @Override
    public ParseTree visitPattern(MetaVMParser.PatternContext ctx) {
        var transformed = new MetaVMParser.PatternContext(getCurrent(), ctx.invokingState);
        transformChildren(ctx, transformed);
        return transformed;
    }

    @Override
    public ParseTree visitVariableModifier(MetaVMParser.VariableModifierContext ctx) {
        var transformed = new MetaVMParser.VariableModifierContext(getCurrent(), ctx.invokingState);
        transformChildren(ctx, transformed);
        return transformed;
    }

    @Override
    public ParseTree visitInnerCreator(MetaVMParser.InnerCreatorContext ctx) {
        var transformed = new MetaVMParser.InnerCreatorContext(getCurrent(), ctx.invokingState);
        transformChildren(ctx, transformed);
        return transformed;
    }

    @Override
    public ParseTree visitCreator(MetaVMParser.CreatorContext ctx) {
        var transformed = new MetaVMParser.CreatorContext(getCurrent(), ctx.invokingState);
        transformChildren(ctx, transformed);
        return transformed;
    }

    @Override
    public ParseTree visitNonWildcardTypeArgumentsOrDiamond(MetaVMParser.NonWildcardTypeArgumentsOrDiamondContext ctx) {
        var transformed = new MetaVMParser.NonWildcardTypeArgumentsOrDiamondContext(getCurrent(), ctx.invokingState);
        transformChildren(ctx, transformed);
        return transformed;
    }

    @Override
    public ParseTree visitExplicitGenericInvocation(MetaVMParser.ExplicitGenericInvocationContext ctx) {
        var transformed = new MetaVMParser.ExplicitGenericInvocationContext(getCurrent(), ctx.invokingState);
        transformChildren(ctx, transformed);
        return transformed;
    }

    @Override
    public ParseTree visitClassCreatorRest(MetaVMParser.ClassCreatorRestContext ctx) {
        var transformed = new MetaVMParser.ClassCreatorRestContext(getCurrent(), ctx.invokingState);
        transformChildren(ctx, transformed);
        return transformed;
    }

    @Override
    public ParseTree visitArrayCreatorRest(MetaVMParser.ArrayCreatorRestContext ctx) {
        var transformed = new MetaVMParser.ArrayCreatorRestContext(getCurrent(), ctx.invokingState);
        transformChildren(ctx, transformed);
        return transformed;
    }

    @Override
    public ParseTree visitArrayInitializer(MetaVMParser.ArrayInitializerContext ctx) {
        var transformed = new MetaVMParser.ArrayInitializerContext(getCurrent(), ctx.invokingState);
        transformChildren(ctx, transformed);
        return transformed;
    }

    @Override
    public ParseTree visitVariableInitializer(MetaVMParser.VariableInitializerContext ctx) {
        var transformed = new MetaVMParser.VariableInitializerContext(getCurrent(), ctx.invokingState);
        transformChildren(ctx, transformed);
        return transformed;
    }

    @Override
    public ParseTree visitCreatedName(MetaVMParser.CreatedNameContext ctx) {
        var transformed = new MetaVMParser.CreatedNameContext(getCurrent(), ctx.invokingState);
        transformChildren(ctx, transformed);
        return transformed;
    }

    @Override
    public ParseTree visitTypeArgumentsOrDiamond(MetaVMParser.TypeArgumentsOrDiamondContext ctx) {
        var transformed = new MetaVMParser.TypeArgumentsOrDiamondContext(getCurrent(), ctx.invokingState);
        transformChildren(ctx, transformed);
        return transformed;
    }

    @Override
    public ParseTree visitMethodCall(MetaVMParser.MethodCallContext ctx) {
        var transformed = new MetaVMParser.MethodCallContext(getCurrent(), ctx.invokingState);
        transformChildren(ctx, transformed);
        return transformed;
    }

    @Override
    public ParseTree visitExpressionList(MetaVMParser.ExpressionListContext ctx) {
        var transformed = new MetaVMParser.ExpressionListContext(getCurrent(), ctx.invokingState);
        transformChildren(ctx, transformed);
        return transformed;
    }

    @Override
    public ParseTree visitExplicitGenericInvocationSuffix(MetaVMParser.ExplicitGenericInvocationSuffixContext ctx) {
        var transformed = new MetaVMParser.ExplicitGenericInvocationSuffixContext(getCurrent(), ctx.invokingState);
        transformChildren(ctx, transformed);
        return transformed;
    }

    @Override
    public ParseTree visitSuperSuffix(MetaVMParser.SuperSuffixContext ctx) {
        var transformed = new MetaVMParser.SuperSuffixContext(getCurrent(), ctx.invokingState);
        transformChildren(ctx, transformed);
        return transformed;
    }

    @Override
    public ParseTree visitArguments(MetaVMParser.ArgumentsContext ctx) {
        var transformed = new MetaVMParser.ArgumentsContext(getCurrent(), ctx.invokingState);
        transformChildren(ctx, transformed);
        return transformed;
    }

    @Override
    public ParseTree visitElementValueArrayInitializer(MetaVMParser.ElementValueArrayInitializerContext ctx) {
        var transformed = new MetaVMParser.ElementValueArrayInitializerContext(getCurrent(), ctx.invokingState);
        transformChildren(ctx, transformed);
        return transformed;
    }

    @Override
    public ParseTree visitLiteral(MetaVMParser.LiteralContext ctx) {
        var transformed = new MetaVMParser.LiteralContext(getCurrent(), ctx.invokingState);
        transformChildren(ctx, transformed);
        return transformed;
    }

    @Override
    public ParseTree visitIntegerLiteral(MetaVMParser.IntegerLiteralContext ctx) {
        var transformed = new MetaVMParser.IntegerLiteralContext(getCurrent(), ctx.invokingState);
        transformChildren(ctx, transformed);
        return transformed;
    }

    @Override
    public ParseTree visitFloatLiteral(MetaVMParser.FloatLiteralContext ctx) {
        var transformed = new MetaVMParser.FloatLiteralContext(getCurrent(), ctx.invokingState);
        transformChildren(ctx, transformed);
        return transformed;
    }

    @Override
    public ParseTree visitIdentifier(MetaVMParser.IdentifierContext ctx) {
        var transformed = new MetaVMParser.IdentifierContext(getCurrent(), ctx.invokingState);
        transformChildren(ctx, transformed);
        return transformed;
    }

    @Override
    public ParseTree visitTypeIdentifier(MetaVMParser.TypeIdentifierContext ctx) {
        var transformed = new MetaVMParser.TypeIdentifierContext(getCurrent(), ctx.invokingState);
        transformChildren(ctx, transformed);
        return transformed;
    }

    @Override
    public ParseTree visitTypeTypeOrVoid(MetaVMParser.TypeTypeOrVoidContext ctx) {
        var transformed = new MetaVMParser.TypeTypeOrVoidContext(getCurrent(), ctx.invokingState);
        transformChildren(ctx, transformed);
        return transformed;
    }

    @Override
    public ParseTree visitElementValuePairs(MetaVMParser.ElementValuePairsContext ctx) {
        var transformed = new MetaVMParser.ElementValuePairsContext(getCurrent(), ctx.invokingState);
        transformChildren(ctx, transformed);
        return transformed;
    }

    @Override
    public ParseTree visitElementValuePair(MetaVMParser.ElementValuePairContext ctx) {
        var transformed = new MetaVMParser.ElementValuePairContext(getCurrent(), ctx.invokingState);
        transformChildren(ctx, transformed);
        return transformed;
    }

    @Override
    public ParseTree visitElementValue(MetaVMParser.ElementValueContext ctx) {
        var transformed = new MetaVMParser.ElementValueContext(getCurrent(), ctx.invokingState);
        transformChildren(ctx, transformed);
        return transformed;
    }

    @Override
    public ParseTree visitQualifiedName(MetaVMParser.QualifiedNameContext ctx) {
        var transformed = new MetaVMParser.QualifiedNameContext(getCurrent(), ctx.invokingState);
        transformChildren(ctx, transformed);
        return transformed;
    }

    @Override
    public ParseTree visitAltAnnotationQualifiedName(MetaVMParser.AltAnnotationQualifiedNameContext ctx) {
        var transformed = new MetaVMParser.AltAnnotationQualifiedNameContext(getCurrent(), ctx.invokingState);
        transformChildren(ctx, transformed);
        return transformed;
    }

    @Override
    public ParseTree visitAnnotation(MetaVMParser.AnnotationContext ctx) {
        var transformed = new MetaVMParser.AnnotationContext(getCurrent(), ctx.invokingState);
        transformChildren(ctx, transformed);
        return transformed;
    }

    @Override
    public ParseTree visitTypeArguments(MetaVMParser.TypeArgumentsContext ctx) {
        var transformed = new MetaVMParser.TypeArgumentsContext(getCurrent(), ctx.invokingState);
        transformChildren(ctx, transformed);
        return transformed;
    }

    @Override
    public ParseTree visitClassOrInterfaceType(MetaVMParser.ClassOrInterfaceTypeContext ctx) {
        var transformed = new MetaVMParser.ClassOrInterfaceTypeContext(getCurrent(), ctx.invokingState);
        transformChildren(ctx, transformed);
        return transformed;
    }

    @Override
    public ParseTree visitTypeType(MetaVMParser.TypeTypeContext ctx) {
        var transformed = new MetaVMParser.TypeTypeContext(getCurrent(), ctx.invokingState);
        transformChildren(ctx, transformed);
        return transformed;
    }

    @Override
    public ParseTree visitParType(MetaVMParser.ParTypeContext ctx) {
        var transformed = new MetaVMParser.ParTypeContext(getCurrent(), ctx.invokingState);
        transformChildren(ctx, transformed);
        return transformed;
    }

    @Override
    public ParseTree visitArrayKind(MetaVMParser.ArrayKindContext ctx) {
        var transformed = new MetaVMParser.ArrayKindContext(getCurrent(), ctx.invokingState);
        transformChildren(ctx, transformed);
        return transformed;
    }

    @Override
    public ParseTree visitPrimitiveType(MetaVMParser.PrimitiveTypeContext ctx) {
        var transformed = new MetaVMParser.PrimitiveTypeContext(getCurrent(), ctx.invokingState);
        transformChildren(ctx, transformed);
        return transformed;
    }

    @Override
    public ParseTree visitNonWildcardTypeArguments(MetaVMParser.NonWildcardTypeArgumentsContext ctx) {
        var transformed = new MetaVMParser.NonWildcardTypeArgumentsContext(getCurrent(), ctx.invokingState);
        transformChildren(ctx, transformed);
        return transformed;
    }

    @Override
    public ParseTree visitTypeList(MetaVMParser.TypeListContext ctx) {
        var transformed = new MetaVMParser.TypeListContext(getCurrent(), ctx.invokingState);
        transformChildren(ctx, transformed);
        return transformed;
    }

}
