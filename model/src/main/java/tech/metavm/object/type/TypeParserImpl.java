package tech.metavm.object.type;

import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.jetbrains.annotations.NotNull;
import tech.metavm.flow.MethodRef;
import tech.metavm.object.instance.core.Id;
import tech.metavm.object.type.antlr.TypeLexer;
import tech.metavm.util.Constants;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.List;

public class TypeParserImpl implements TypeParser {

    private final TypeDefProvider typeDefProvider;

    public TypeParserImpl(TypeDefProvider typeDefProvider) {
        this.typeDefProvider = typeDefProvider;
    }

    @Override
    public Type parseType(@NotNull String expression) {
        var parser = createAntlrParser(expression);
        try {
            return parseType(parser.type());
        } catch (Exception e) {
            throw new InternalException("Failed to parse type: " + expression, e);
        }
    }

    @Override
    public MethodRef parseMethodRef(String expression) {
        var parser = createAntlrParser(expression);
        try {
            return parseMethodRef(parser.methodRef());
        } catch (Exception e) {
            throw new InternalException("Failed to parse method reference: " + expression, e);
        }
    }

    private tech.metavm.object.type.antlr.TypeParser createAntlrParser(String expression) {
        var input = CharStreams.fromString(expression);
        var parser = new tech.metavm.object.type.antlr.TypeParser(new CommonTokenStream(new TypeLexer(input)));
        parser.setErrorHandler(new BailErrorStrategy());
        return parser;
    }

    private Type parseType(tech.metavm.object.type.antlr.TypeParser.TypeContext ctx) {
        if (ctx.primitiveType() != null)
            return parsePrimitiveType(ctx.primitiveType());
        if (ctx.ANY() != null)
            return new AnyType();
        if (ctx.NEVER() != null)
            return new NeverType();
        if (ctx.LPAREN() != null)
            return parseFunctionType(ctx);
        if (ctx.elementType != null)
            return parseArrayType(ctx);
        if (ctx.classType() != null)
            return parseClassType(ctx.classType());
        if (ctx.variableType() != null)
            return parseVariableType(ctx.variableType());
        if (ctx.LBRACK() != null)
            return parseUncertainType(ctx);
        if (ctx.NUM() != null)
            return parseCapturedType(ctx);
        if (!ctx.BITOR().isEmpty())
            return parseUnionType(ctx);
        if (!ctx.BITAND().isEmpty())
            return parseIntersectionType(ctx);
        throw new IllegalArgumentException("Unknown type: " + ctx.getText());
    }

    private MethodRef parseMethodRef(tech.metavm.object.type.antlr.TypeParser.MethodRefContext ctx) {
        var classType = parseClassType(ctx.classType());
        var rawMethod = classType.getKlass().getMethod(Id.parse(ctx.IDENTIFIER().getText().substring(Constants.CONSTANT_ID_PREFIX.length())));
        List<Type> typeArgs = ctx.typeArguments() != null ? parseTypeList(ctx.typeArguments().typeList()) : List.of();
        return new MethodRef(classType, rawMethod, typeArgs);
    }

    private VariableType parseVariableType(tech.metavm.object.type.antlr.TypeParser.VariableTypeContext ctx) {
        return new VariableType((TypeVariable) getTypeDef(ctx.IDENTIFIER().getText()));
    }

    private PrimitiveType parsePrimitiveType(tech.metavm.object.type.antlr.TypeParser.PrimitiveTypeContext ctx) {
        if (ctx.BOOLEAN() != null)
            return new PrimitiveType(PrimitiveKind.BOOLEAN);
        if (ctx.STRING() != null)
            return new PrimitiveType(PrimitiveKind.STRING);
        if (ctx.LONG() != null)
            return new PrimitiveType(PrimitiveKind.LONG);
        if (ctx.DOUBLE() != null)
            return new PrimitiveType(PrimitiveKind.DOUBLE);
        if (ctx.VOID() != null)
            return new PrimitiveType(PrimitiveKind.VOID);
        if (ctx.TIME() != null)
            return new PrimitiveType(PrimitiveKind.TIME);
        if (ctx.PASSWORD() != null)
            return new PrimitiveType(PrimitiveKind.PASSWORD);
        if (ctx.NULL() != null)
            return new PrimitiveType(PrimitiveKind.NULL);
        throw new IllegalArgumentException("Unknown primitive type: " + ctx.getText());
    }

    private FunctionType parseFunctionType(tech.metavm.object.type.antlr.TypeParser.TypeContext ctx) {
        return new FunctionType(parseTypeList(ctx.typeList()), parseType(ctx.type(0)));
    }

    private ArrayType parseArrayType(tech.metavm.object.type.antlr.TypeParser.TypeContext ctx) {
        return new ArrayType(parseType(ctx.elementType), parseArrayKind(ctx.arrayKind()));
    }

    private UncertainType parseUncertainType(tech.metavm.object.type.antlr.TypeParser.TypeContext ctx) {
        return new UncertainType(parseType(ctx.type(0)), parseType(ctx.type(1)));
    }

    private UnionType parseUnionType(tech.metavm.object.type.antlr.TypeParser.TypeContext ctx) {
        return new UnionType(NncUtils.mapUnique(ctx.type(), this::parseType));
    }

    private IntersectionType parseIntersectionType(tech.metavm.object.type.antlr.TypeParser.TypeContext ctx) {
        return new IntersectionType(NncUtils.mapUnique(ctx.type(), this::parseType));
    }

    private ClassType parseClassType(tech.metavm.object.type.antlr.TypeParser.ClassTypeContext ctx) {
        var name = ctx.qualifiedName().getText();
        var klass = (Klass) getTypeDef(name);
        if (ctx.typeArguments() != null) {
            return new ClassType(klass, NncUtils.map(ctx.typeArguments().typeList().type(), this::parseType));
        } else
            return klass.getType();
    }

    private CapturedType parseCapturedType(tech.metavm.object.type.antlr.TypeParser.TypeContext ctx) {
        var name = ctx.qualifiedName().getText();
        var typeDef = getTypeDef(name);
        return new CapturedType((CapturedTypeVariable) typeDef);
    }

    private TypeDef getTypeDef(String name) {
        if (name.startsWith(Constants.CONSTANT_ID_PREFIX)) {
            var id = name.substring(Constants.CONSTANT_ID_PREFIX.length());
            return typeDefProvider.getTypeDef(Id.parse(id));
        } else
            throw new InternalException("Invalid id: " + name);

    }

    private ArrayKind parseArrayKind(@Nullable tech.metavm.object.type.antlr.TypeParser.ArrayKindContext ctx) {
        if (ctx == null)
            return ArrayKind.READ_WRITE;
        if (ctx.R() != null)
            return ArrayKind.READ_ONLY;
        if (ctx.C() != null)
            return ArrayKind.CHILD;
        throw new IllegalArgumentException("Unknown array kind: " + ctx.getText());
    }

    private List<Type> parseTypeList(@Nullable tech.metavm.object.type.antlr.TypeParser.TypeListContext ctx) {
        if (ctx == null)
            return List.of();
        return NncUtils.map(ctx.type(), this::parseType);
    }


}
