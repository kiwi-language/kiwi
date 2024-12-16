package org.metavm.object.type;

import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.jetbrains.annotations.NotNull;
import org.metavm.entity.DummyGenericDeclaration;
import org.metavm.entity.GenericDeclaration;
import org.metavm.entity.GenericDeclarationRef;
import org.metavm.flow.*;
import org.metavm.object.instance.core.Id;
import org.metavm.object.type.antlr.TypeLexer;
import org.metavm.util.Constants;
import org.metavm.util.InternalException;
import org.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class TypeParserImpl implements TypeParser {

    private final ParserTypeDefProvider typeDefProvider;

    private final Map<String, TypeVariable> typeParameters = new HashMap<>();

    public TypeParserImpl(TypeDefProvider typeDefProvider) {
        this.typeDefProvider = name -> {
            if (name.startsWith(Constants.ID_PREFIX)) {
                var id = Constants.removeIdPrefix(name);
                return typeDefProvider.getTypeDef(Id.parse(id));
            } else
                throw new InternalException("Invalid id: " + name);
        };
    }

    public TypeParserImpl(ParserTypeDefProvider typeDefProvider) {
        this.typeDefProvider = typeDefProvider;
    }

    @Override
    public Type parseType(@NotNull String expression) {
        var parser = createAntlrParser(expression);
        try {
            return parseType(parser.unit().type());
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

    @Override
    public FunctionRef parseFunctionRef(String expression) {
        var parser = createAntlrParser(expression);
        try {
            return parseFunctionRef(parser.functionRef());
        } catch (Exception e) {
            throw new InternalException("Failed to parse function reference: " + expression, e);
        }
    }

    @Override
    public SimpleMethodRef parseSimpleMethodRef(String expression) {
        var parser = createAntlrParser(expression);
        try {
            return parseSimpleMethodRef(parser.simpleMethodRef());
        } catch (Exception e) {
            throw new InternalException("Failed to parse simple method reference: " + expression, e);
        }
    }

    public Function parseFunction(String expression) {
        var parser = createAntlrParser(expression);
        try {
           return parseFunction(parser.functionSignature());
        }
        catch (Exception e) {
            throw new InternalException("Failed to parse function: " + expression, e);
        }
    }

    public String getFunctionName(String expression) {
        var parser = createAntlrParser(expression);
        try {
            return parser.functionSignature().IDENTIFIER().getText();
        }
        catch (Exception e) {
            throw new InternalException("Failed to parse function: " + expression, e);
        }

    }

    private Function parseFunction(org.metavm.object.type.antlr.TypeParser.FunctionSignatureContext ctx) {
        var name = ctx.IDENTIFIER().getText();
        var func =  FunctionBuilder.newBuilder(name)
                .typeParameters(
                        ctx.typeParameterList() != null ?
                                NncUtils.map(ctx.typeParameterList().typeParameter(), this::parseTypeParameter) : List.of()
                )
                .build();
        func.setParameters(
                        ctx.parameterList() != null ?
                                NncUtils.map(ctx.parameterList().parameter(), p -> parseParameter(p, func)) : List.of()
                );
        func.setReturnType(parseType(ctx.type()));
        return func;
    }

    private Parameter parseParameter(org.metavm.object.type.antlr.TypeParser.ParameterContext ctx, Callable callable) {
        var name = ctx.IDENTIFIER().getText();
        return new Parameter(null, name, parseType(ctx.type()), callable);
    }

    private TypeVariable parseTypeParameter(org.metavm.object.type.antlr.TypeParser.TypeParameterContext ctx) {
        var name = ctx.IDENTIFIER().getText();
        var typeVar =  new TypeVariable(null, name, DummyGenericDeclaration.INSTANCE);
        typeParameters.put(name, typeVar);
        return typeVar;
    }

    private org.metavm.object.type.antlr.TypeParser createAntlrParser(String expression) {
        var input = CharStreams.fromString(expression);
        var parser = new org.metavm.object.type.antlr.TypeParser(new CommonTokenStream(new TypeLexer(input)));
        parser.setErrorHandler(new BailErrorStrategy());
        return parser;
    }

    private Type parseType(org.metavm.object.type.antlr.TypeParser.TypeContext ctx) {
        if(ctx.parType() != null)
            return parseType(ctx.parType().type());
        if (ctx.primitiveType() != null)
            return parsePrimitiveType(ctx.primitiveType());
        if (ctx.ANY() != null)
            return AnyType.instance;
        if (ctx.NEVER() != null)
            return NeverType.instance;
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

    public MethodRef parseMethodRef(org.metavm.object.type.antlr.TypeParser.MethodRefContext ctx) {
        var classType = (ClassType) parseClassType(ctx.classType());
        var rawMethod = (Method) typeDefProvider.getTypeDef(ctx.IDENTIFIER().getText());
        List<Type> typeArgs = ctx.typeArguments() != null ? parseTypeList(ctx.typeArguments().typeList()) : List.of();
        return new MethodRef(classType, rawMethod, typeArgs);
    }

    public FunctionRef parseFunctionRef(org.metavm.object.type.antlr.TypeParser.FunctionRefContext ctx) {
        var rawFunc = (Function) getTypeDef(ctx.IDENTIFIER().getText());
        List<Type> typeArgs = ctx.typeArguments() != null ? parseTypeList(ctx.typeArguments().typeList()) : List.of();
        return new FunctionRef(rawFunc, typeArgs);
    }

    private SimpleMethodRef parseSimpleMethodRef(org.metavm.object.type.antlr.TypeParser.SimpleMethodRefContext ctx) {
        return new SimpleMethodRef(
                ctx.IDENTIFIER().getText(),
                ctx.typeArguments() != null ? NncUtils.map(ctx.typeArguments().typeList().type(), this::parseType) : List.of()
        );
    }

    private VariableType parseVariableType(org.metavm.object.type.antlr.TypeParser.VariableTypeContext ctx) {
//        return new VariableType((TypeVariable) getTypeDef(ctx.qualifiedName().getText()), genericDeclarationRef, rawVariable);
        var rawTypeVariable = (TypeVariable) getTypeDef(ctx.IDENTIFIER().getText());
        return new VariableType(rawTypeVariable);
    }

    private GenericDeclarationRef parseGenericDeclarationRef(org.metavm.object.type.antlr.TypeParser.GenericDeclarationRefContext ctx) {
        if(ctx.classType() != null)
            return (ClassType) parseClassType(ctx.classType());
        else if(ctx.methodRef() != null)
            return parseMethodRef(ctx.methodRef());
        else if(ctx.functionRef() != null)
            return parseFunctionRef(ctx.functionRef());
        else
            throw new IllegalStateException("Failed to parse generic declaration ref: " + ctx.getText());
    }

    private PrimitiveType parsePrimitiveType(org.metavm.object.type.antlr.TypeParser.PrimitiveTypeContext ctx) {
        if (ctx.BOOLEAN() != null)
            return PrimitiveType.booleanType;
        if (ctx.STRING() != null)
            return PrimitiveType.stringType;
        if (ctx.INT() != null)
            return PrimitiveType.intType;
        if (ctx.LONG() != null)
            return PrimitiveType.longType;
        if (ctx.CHAR() != null)
            return PrimitiveType.charType;
        if (ctx.DOUBLE() != null)
            return PrimitiveType.doubleType;
        if (ctx.VOID() != null)
            return PrimitiveType.voidType;
        if (ctx.TIME() != null)
            return PrimitiveType.timeType;
        if (ctx.PASSWORD() != null)
            return PrimitiveType.passwordType;
        if (ctx.NULL() != null)
            return PrimitiveType.nullType;
        throw new IllegalArgumentException("Unknown primitive type: " + ctx.getText());
    }

    private FunctionType parseFunctionType(org.metavm.object.type.antlr.TypeParser.TypeContext ctx) {
        return new FunctionType(parseTypeList(ctx.typeList()), parseType(ctx.type(0)));
    }

    private ArrayType parseArrayType(org.metavm.object.type.antlr.TypeParser.TypeContext ctx) {
        return new ArrayType(parseType(ctx.elementType), parseArrayKind(ctx.arrayKind()));
    }

    private UncertainType parseUncertainType(org.metavm.object.type.antlr.TypeParser.TypeContext ctx) {
        return new UncertainType(parseType(ctx.type(0)), parseType(ctx.type(1)));
    }

    private UnionType parseUnionType(org.metavm.object.type.antlr.TypeParser.TypeContext ctx) {
        return new UnionType(NncUtils.mapUnique(ctx.type(), this::parseType)).flatten();
    }

    private IntersectionType parseIntersectionType(org.metavm.object.type.antlr.TypeParser.TypeContext ctx) {
        return new IntersectionType(NncUtils.mapUnique(ctx.type(), this::parseType)).flatten();
    }

    private Type parseClassType(org.metavm.object.type.antlr.TypeParser.ClassTypeContext ctx) {
        var name = ctx.qualifiedName().getText();
        var typeVar = typeParameters.get(name);
        if(typeVar != null)
            return typeVar.getType();
        var klass = (Klass) getTypeDef(name);
        if (ctx.typeArguments() != null) {
            return new KlassType(NncUtils.get(klass.getOwner(), GenericDeclaration::getRef),
                    klass, NncUtils.map(ctx.typeArguments().typeList().type(), this::parseType));
        } else
            return klass.getType();
    }

    private CapturedType parseCapturedType(org.metavm.object.type.antlr.TypeParser.TypeContext ctx) {
        var name = ctx.qualifiedName().getText();
        var typeDef = getTypeDef(name);
        return new CapturedType((CapturedTypeVariable) typeDef);
    }

    private ITypeDef getTypeDef(String name) {
        var typeDef = typeDefProvider.getTypeDef(name);
        if(typeDef == null)
            throw new NullPointerException("Failed to find a TypeDef for name: " + name);
        return typeDef;
    }

    private ArrayKind parseArrayKind(org.metavm.object.type.antlr.TypeParser.ArrayKindContext ctx) {
        if (ctx.LBRACK() != null)
            return ArrayKind.READ_WRITE;
        if (ctx.R() != null)
            return ArrayKind.READ_ONLY;
        if (ctx.C() != null)
            return ArrayKind.CHILD;
        if(ctx.V() != null)
            return ArrayKind.VALUE;
        throw new IllegalArgumentException("Unknown array kind: " + ctx.getText());
    }

    private List<Type> parseTypeList(@Nullable org.metavm.object.type.antlr.TypeParser.TypeListContext ctx) {
        if (ctx == null)
            return List.of();
        return NncUtils.map(ctx.type(), this::parseType);
    }
}
