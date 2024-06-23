package org.metavm.common;

import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNodeImpl;
import org.metavm.expression.ExpressionParserTransformer;
import org.metavm.expression.antlr.MetaVMLexer;
import org.metavm.expression.antlr.MetaVMParser;
import org.metavm.object.type.TypeParserTransformer;
import org.metavm.object.type.antlr.TypeLexer;
import org.metavm.object.type.antlr.TypeParser;
import org.metavm.util.Constants;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface CopyContext {

    static CopyContext create(Map<String, String> idMap) {
        return idMap.isEmpty() ? EmptyCopyContext.INSTANCE : new DefaultCopyContext(idMap);
    }

    <T> T copy(T object);

    String mapId(String id);

    String mapType(String type);

    String mapExpression(String expression);

}

class EmptyCopyContext implements CopyContext {

    static EmptyCopyContext INSTANCE = new EmptyCopyContext();

    private EmptyCopyContext() {
    }

    @Override
    public <T> T copy(T object) {
        return object;
    }

    @Override
    public String mapId(String id) {
        return id;
    }

    @Override
    public String mapType(String type) {
        return type;
    }

    @Override
    public String mapExpression(String expression) {
        return expression;
    }
}

class DefaultCopyContext implements CopyContext {
    private final Map<String, String> idMap = new HashMap<>();

    DefaultCopyContext(Map<String, String> idMap) {
        this.idMap.putAll(idMap);
    }

    @Override
    public <T> T copy(T object) {
        return new DtoCopier(this).copy(object);
    }

    public String mapId(String id) {
        return idMap.getOrDefault(id, id);
    }

    @Override
    public String mapType(String type) {
        if(type == null)
            return null;
        var input = CharStreams.fromString(type);
        var parser = new org.metavm.object.type.antlr.TypeParser(new CommonTokenStream(new TypeLexer(input)));
        parser.setErrorHandler(new BailErrorStrategy());
        var typeCtx = parser.type().accept(new IdMappingTypeParserTransformer(this));
        return typeCtx.getText();
    }

    @Override
    public String mapExpression(String expression) {
        if(expression == null)
            return null;
        var input = CharStreams.fromString(expression);
        var parser = new MetaVMParser(new CommonTokenStream(new MetaVMLexer(input)));
        parser.setErrorHandler(new BailErrorStrategy());
        var expressionCtx = parser.expression().accept(new IdMappingExpressionTransformer(this));
        return expressionCtx.getText();
    }

}

class IdMappingExpressionTransformer extends ExpressionParserTransformer {

    private final CopyContext copyContext;

    IdMappingExpressionTransformer(CopyContext copyContext) {
        this.copyContext = copyContext;
    }

    @Override
    public ParseTree visitIdentifier(MetaVMParser.IdentifierContext ctx) {
        var text = ctx.getText();
        var prefixes = List.of("$$", "$");
        for (String prefix : prefixes) {
            if (text.startsWith(prefix)) {
                var id = copyContext.mapId(text.substring(prefix.length()));
                var copy = new MetaVMParser.IdentifierContext(getCurrent(), ctx.invokingState);
                copy.addChild(new TerminalNodeImpl(new CommonToken(MetaVMLexer.IDENTIFIER, prefix + id)));
                return copy;
            }
        }
        return super.visitIdentifier(ctx);
    }
}

class IdMappingTypeParserTransformer extends TypeParserTransformer {

    private final CopyContext copyContext;

    public IdMappingTypeParserTransformer(CopyContext copyContext) {
        this.copyContext = copyContext;
    }

    @Override
    public ParseTree visitQualifiedName(TypeParser.QualifiedNameContext ctx) {
        if(ctx.getChildCount() == 1) {
            var text = ctx.getText();
            if (Constants.isIdPrefixed(text)) {
                var copy = new TypeParser.QualifiedNameContext(getCurrent(), ctx.invokingState);
                var id = copyContext.mapId(Constants.removeIdPrefix(text));
                copy.addChild(new TerminalNodeImpl(new CommonToken(TypeParser.IDENTIFIER, Constants.addIdPrefix(id))));
                return copy;
            }
        }
        return super.visitQualifiedName(ctx);
    }

}
