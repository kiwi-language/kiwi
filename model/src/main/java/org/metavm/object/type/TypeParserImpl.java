package org.metavm.object.type;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.metavm.entity.DummyGenericDeclaration;
import org.metavm.entity.GenericDeclaration;
import org.metavm.entity.ModelIdentity;
import org.metavm.entity.StdKlass;
import org.metavm.flow.*;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.TmpId;
import org.metavm.object.type.rest.dto.*;
import org.metavm.util.Constants;
import org.metavm.util.InternalException;

import java.util.*;

@Slf4j
public class TypeParserImpl implements TypeParser {

    private final ParserTypeDefProvider typeDefProvider;

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
        return createParser(expression).type();
    }

    @Override
    public TypeKey parseTypeKey(@NotNull String expression) {
        return createParser(expression).typeKey();
    }

    @Override
    public FunctionSignature parseFunctionSignature(String expression) {
        return createParser(expression).functionSignature();
    }

    @Override
    public SimpleMethodRef parseSimpleMethodRef(String expression) {
        return createParser(expression).methodRef();
    }

    public Function parseFunction(String expression, java.util.function.Function<ModelIdentity, Id> getId) {
        return createParser(expression).function(getId);
    }

    public String getFunctionName(String expression) {
        return createParser(expression).name();
    }

    private Parser createParser(String expression) {
        return new Parser(expression);
    }


    private class Parser {

        private final Tokenizer tokenizer;
        private Token token;
        private final Map<String, TypeVariable> typeParams = new HashMap<>();
        private final StringBuilder buf = new StringBuilder();

        private Parser(String s) {
            this.tokenizer = new Tokenizer(s);
            token = tokenizer.next();
        }

        Function function(java.util.function.Function<ModelIdentity, Id> getId) {
            var name = accept(TokenKind.IDENT).text;
            var id = getId.apply(ModelIdentity.create(Function.class, name));
            var func = FunctionBuilder.newBuilder(id, name).build();
            var typeParams = new ArrayList<TypeVariable>();
            if (is(TokenKind.LT)) {
                next();
                do {
                    typeParams.add(typeParam(func, getId));
                } while (skip(TokenKind.COMMA));
                accept(TokenKind.GT);
            }
            func.setTypeParameters(typeParams);
            accept(TokenKind.LPAREN);
            var params = new ArrayList<Parameter>();
            if (!is(TokenKind.RPAREN)) {
                do {
                    params.add(param(func, getId));
                } while (skip(TokenKind.COMMA));
            }
            accept(TokenKind.RPAREN);
            func.setParameters(params);
            accept(TokenKind.ARROW);
            func.setReturnType(type());
            return func;
        }

        public FunctionSignature functionSignature() {
            var name = name();
            var typeParamNames = new ArrayList<String>();
            if (is(TokenKind.LT)) {
                next();
                do {
                    var typeParamName = name();
                    if (skip(TokenKind.COLON))
                        typeKey();
                    this.typeParams.put(typeParamName, new TypeVariable(TmpId.random(), typeParamName, DummyGenericDeclaration.INSTANCE));
                    typeParamNames.add(typeParamName);
                } while (skip(TokenKind.COMMA));
                accept(TokenKind.GT);
            }
            accept(TokenKind.LPAREN);
            var paramNames = new ArrayList<String>();
            if (!is(TokenKind.RPAREN)) {
                do {
                    var paramName = name();
                    accept(TokenKind.COLON);
                    typeKey();
                    paramNames.add(paramName);
                } while (skip(TokenKind.COMMA));
                accept(TokenKind.RPAREN);
            }
            return new FunctionSignature(name, typeParamNames, paramNames);
        }

        private Parameter param(Function function, java.util.function.Function<ModelIdentity, Id> getId) {
            var name = accept(TokenKind.IDENT).text;
            accept(TokenKind.COLON);
            var type = type();
            return new Parameter(
                    getId.apply(ModelIdentity.create(Parameter.class, function.getName() + "." + name)),
                    name,
                    type,
                    function
            );
        }

        private TypeVariable typeParam(GenericDeclaration genericDecl, java.util.function.Function<ModelIdentity, Id> getId) {
            var name = accept(TokenKind.IDENT).text;
            var id = getId.apply(ModelIdentity.create(TypeVariable.class, genericDecl.getName() + "." + name));
            var typeVar = new TypeVariable(id, name, genericDecl);
            typeParams.put(name, typeVar);
            if (is(TokenKind.COLON)) {
                next();
                typeVar.setBounds(List.of(type()));
            }
            return typeVar;
        }

        SimpleMethodRef methodRef() {
            var name = name();
            List<Type> typeArgs = is(TokenKind.LT) ? typeArgs() : List.of();
            return new SimpleMethodRef(name, typeArgs);
        }

        private List<Type> typeArgs() {
            accept(TokenKind.LT);
            var typeArgs = new ArrayList<Type>();
            typeArgs.add(type());
            while (skip(TokenKind.COMMA)){
                typeArgs.add(type());
            }
            accept(TokenKind.GT);
            return typeArgs;
        }

        TypeKey typeKey() {
            return unionTypeKey();
        }

        TypeKey unionTypeKey() {
            var t = intersectionTypeKey();
            if (!is(TokenKind.OR))
                return t;
            var alts = new HashSet<TypeKey>();
            alts.add(t);
            do {
                next();
                alts.add(intersectionTypeKey());
            } while (is(TokenKind.OR));
            return new UnionTypeKey(alts);
        }

        TypeKey intersectionTypeKey() {
            var t = arrayTypeKey();
            if (!is(TokenKind.AND))
                return t;
            var bounds = new HashSet<TypeKey>();
            bounds.add(t);
            do {
                next();
                bounds.add(arrayTypeKey());
            } while (is(TokenKind.AND));
            return new IntersectionTypeKey(bounds);
        }

        private TypeKey arrayTypeKey() {
            var t = atomTypeKey();
            if (!is(TokenKind.LBRACKET))
                return t;
            do {
                t = new ArrayTypeKey(ArrayKind.DEFAULT.code(), t);
                next();
                accept(TokenKind.RBRACKET);
            } while (is(TokenKind.LBRACKET));
            return t;
        }

        private TypeKey atomTypeKey() {
            return switch (token.kind) {
                case LBRACKET -> {
                    next();
                    var lb = typeKey();
                    accept(TokenKind.COMMA);
                    var ub = typeKey();
                    accept(TokenKind.RBRACKET);
                    yield new UncertainTypeKey(lb, ub);
                }
                case LPAREN -> {
                    next();
                    if (skip(TokenKind.RPAREN)) {
                        accept(TokenKind.ARROW);
                        yield new FunctionTypeKey(List.of(), typeKey());
                    }
                    var t = typeKey();
                    if (is(TokenKind.COMMA)) {
                        next();
                        var paramTypes = new ArrayList<TypeKey>();
                        paramTypes.add(t);
                        do {
                            paramTypes.add(typeKey());
                        } while (skip(TokenKind.COMMA));
                        accept(TokenKind.RPAREN);
                        accept(TokenKind.ARROW);
                        yield new FunctionTypeKey(paramTypes, typeKey());
                    } else {
                        accept(TokenKind.RPAREN);
                        if (skip(TokenKind.ARROW))
                            yield new FunctionTypeKey(List.of(t), typeKey());
                        else
                            yield t;
                    }
                }
                case AT -> {
                    next();
                    var name = name();
                    yield new VariableTypeKey(Id.parse(name.substring(Constants.ID_PREFIX.length())));
                }
                case IDENT -> {
                    var text = token.text;
                    next();
                    yield switch (text) {
                        case "byte" -> new PrimitiveTypeKey(PrimitiveKind.BYTE.code());
                        case "short" -> new PrimitiveTypeKey(PrimitiveKind.SHORT.code());
                        case "int" -> new PrimitiveTypeKey(PrimitiveKind.INT.code());
                        case "long" -> new PrimitiveTypeKey(PrimitiveKind.LONG.code());
                        case "boolean" -> new PrimitiveTypeKey(PrimitiveKind.BOOLEAN.code());
                        case "string" -> new ClassTypeKey(StdKlass.string.get().getId());
                        case "char" -> new PrimitiveTypeKey(PrimitiveKind.CHAR.code());
                        case "double" -> new PrimitiveTypeKey(PrimitiveKind.DOUBLE.code());
                        case "float" -> new PrimitiveTypeKey(PrimitiveKind.FLOAT.code());
                        case "void" -> new PrimitiveTypeKey(PrimitiveKind.VOID.code());
                        case "time" -> new PrimitiveTypeKey(PrimitiveKind.TIME.code());
                        case "password" -> new PrimitiveTypeKey(PrimitiveKind.PASSWORD.code());
                        case "null" -> new NullTypeKey();
                        case "any" -> new AnyTypeKey();
                        case "never" -> new NeverTypeKey();
                        default -> {
                            buf.setLength(0);
                            buf.append(text);
                            while (is(TokenKind.DOT)) {
                                next();
                                buf.append('.').append(accept(TokenKind.IDENT).text);
                            }
                            var name = buf.toString();
                            if (typeParams.containsKey(name))
                                yield new VariableTypeKey(typeParams.get(name).getId());
                            var id = Id.parse(name.substring(Constants.ID_PREFIX.length()));
                            if (is(TokenKind.LT)) {
                                next();
                                var typeArgs = new ArrayList<TypeKey>();
                                do {
                                    typeArgs.add(typeKey());
                                } while (skip(TokenKind.COMMA));
                                accept(TokenKind.GT);
                                yield new ParameterizedTypeKey(null, id, typeArgs);
                            } else
                                yield new ClassTypeKey(id);
                        }
                    };
                }
                default -> throw new TypeNotPresentException("Unexpected token: " + token.kind, null);
            };
        }

        Type type() {
            return unionType();
        }

        Type unionType() {
            var t = intersectionType();
            if (!is(TokenKind.OR))
                return t;
            var alts = new HashSet<Type>();
            alts.add(t);
            do {
                next();
                alts.add(intersectionType());
            } while (is(TokenKind.OR));
            return new UnionType(alts);
        }

        String name() {
            return accept(TokenKind.IDENT).text;
        }

        Type intersectionType() {
            var t = arrayType();
            if (!is(TokenKind.AND))
                return t;
            var bounds = new HashSet<Type>();
            bounds.add(t);
            do {
                next();
                bounds.add(arrayType());
            } while (is(TokenKind.AND));
            return new IntersectionType(bounds);
        }

        Type arrayType() {
            var t = atomType();
            if (!is(TokenKind.LBRACKET))
                return t;
            do {
                t = Types.getArrayType(t);
                next();
                accept(TokenKind.RBRACKET);
            } while (is(TokenKind.LBRACKET));
            return t;
        }

        Type atomType() {
            return switch (token.kind) {
                case LBRACKET -> {
                    next();
                    var lb = type();
                    accept(TokenKind.COMMA);
                    var ub = type();
                    accept(TokenKind.RBRACKET);
                    yield new UncertainType(lb, ub);
                }
                case LPAREN -> {
                    next();
                    if (skip(TokenKind.RPAREN)) {
                        accept(TokenKind.ARROW);
                        yield new FunctionType(List.of(), type());
                    }
                    var t = type();
                    if (is(TokenKind.COMMA)) {
                        next();
                        var paramTypes = new ArrayList<Type>();
                        paramTypes.add(t);
                        do {
                            paramTypes.add(type());
                        } while (skip(TokenKind.COMMA));
                        accept(TokenKind.RPAREN);
                        accept(TokenKind.ARROW);
                        yield new FunctionType(paramTypes, type());
                    } else {
                        accept(TokenKind.RPAREN);
                        if (skip(TokenKind.ARROW))
                            yield new FunctionType(List.of(t), type());
                        else
                            yield t;
                    }
                }
                case AT -> {
                    next();
                    var name = name();
                    yield new VariableType((TypeVariable) typeDefProvider.getTypeDef(name));
                }
                case IDENT -> {
                    var text = token.text;
                    next();
                    yield switch (text) {
                        case "byte" -> PrimitiveType.byteType;
                        case "short" -> PrimitiveType.shortType;
                        case "int" -> PrimitiveType.intType;
                        case "long" -> PrimitiveType.longType;
                        case "boolean" -> PrimitiveType.booleanType;
                        case "string" -> StdKlass.string.type();
                        case "char" -> PrimitiveType.charType;
                        case "double" -> PrimitiveType.doubleType;
                        case "float" -> PrimitiveType.floatType;
                        case "void" -> PrimitiveType.voidType;
                        case "time" -> PrimitiveType.timeType;
                        case "password" -> PrimitiveType.passwordType;
                        case "null" -> NullType.instance;
                        case "any" -> AnyType.instance;
                        case "never" -> NeverType.instance;
                        default -> {
                            buf.setLength(0);
                            buf.append(text);
                            while (is(TokenKind.DOT)) {
                                next();
                                buf.append('.').append(accept(TokenKind.IDENT).text);
                            }
                            var name = buf.toString();
                            if (typeParams.containsKey(name))
                                yield typeParams.get(name).getType();
                            var klass = (Klass) typeDefProvider.getTypeDef(name);
                            if (is(TokenKind.LT)) {
                                next();
                                var typeArgs = new ArrayList<Type>();
                                do {
                                    typeArgs.add(type());
                                } while (skip(TokenKind.COMMA));
                                accept(TokenKind.GT);
                                yield KlassType.create(klass.getType().getOwner(), klass, typeArgs);
                            } else
                                yield klass.getType();
                        }
                    };
                }
                default -> throw new TypeParsingException("Unexpected token: " + token.kind);
            };
        }

        private void next() {
            token = tokenizer.next();
        }

        private boolean skip(TokenKind kind) {
            if (is(kind)) {
                next();
                return true;
            } else
                return false;
        }

        private boolean is(TokenKind kind) {
            return token.kind == kind;
        }

        private Token accept(TokenKind kind) {
            if (is(kind)) {
                var t = token;
                next();
                return t;
            }
            throw new TypeParsingException("Failed to parse expression: " + tokenizer.s + ". Expected token: " + kind + ", but found: " + token.kind);
        }

    }

    private static class Tokenizer {
        private final String s;
        private int pos;
        private final StringBuilder buf = new StringBuilder();

        public Tokenizer(String s) {
            this.s = s;
        }

        char current() {
            return s.charAt(pos);
        }

        Token next() {
            if (pos == s.length())
                return new Token(TokenKind.EOF, "");
            return switch (current()) {
                case 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
                     'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
                     'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
                     'N', 'O', 'P',  'Q',  'R',  'S',  'T',  'U',  'V',  'W',  'X',
                     'Y',  'Z', '_' ->
                    new Token(TokenKind.IDENT, scanIdent());
                case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' ->
                    throw new TypeParsingException("Numeric literals are not supported");
                case ',' -> {
                    pos++;
                    yield new Token(TokenKind.COMMA, ",");
                }
                case '.' -> {
                    pos++;
                    yield new Token(TokenKind.DOT, ".");
                }
                case '&' -> {
                    pos++;
                    yield new Token(TokenKind.AND, "&");
                }
                case '|' -> {
                    pos++;
                    yield new Token(TokenKind.OR, "|");
                }
                case '(' -> {
                    pos++;
                    yield new Token(TokenKind.LPAREN, "(");
                }
                case ')' -> {
                    pos++;
                    yield new Token(TokenKind.RPAREN, ")");
                }
                case '[' -> {
                    pos++;
                    yield new Token(TokenKind.LBRACKET, "[");
                }
                case ']' -> {
                    pos++;
                    yield new Token(TokenKind.RBRACKET, "]");
                }
                case '<' -> {
                    pos++;
                    yield new Token(TokenKind.LT, "<");
                }
                case '>' -> {
                    pos++;
                    yield new Token(TokenKind.GT, ">");
                }
                case '-' -> {
                    pos++;
                    if (current() == '>') {
                        next();
                        yield new Token(TokenKind.ARROW, "->");
                    } else
                        throw new TypeParsingException("Unexpected character: " + current());
                }
                case ':' -> {
                    pos++;
                    yield new Token(TokenKind.COLON, ":");
                }
                case '@' -> {
                    pos++;
                    yield new Token(TokenKind.AT, "@");
                }
                case ' ', '\t' -> {
                    skipWhitespaces();
                    yield next();
                }
                default -> {
                    if (Character.isJavaIdentifierPart(current()))
                        yield new Token(TokenKind.IDENT, scanIdent());
                    else
                        throw new TypeParsingException("Unexpected character: " + current());
                }
            };
        }

        private void skipWhitespaces() {
            while (!isEof() && Character.isWhitespace(current()))
                pos++;
        }

        private String scanIdent() {
            buf.setLength(0);
            while (!isEof() && Character.isJavaIdentifierPart(current())) {
                buf.append(current());
                pos++;
            }
            return buf.toString();
        }

        private boolean isEof() {
            return pos >= s.length();
        }


    }

    private record Token(TokenKind kind, String text) {}

    private enum TokenKind {
       IDENT, DOT, COMMA, COLON, ARROW, LPAREN, RPAREN, LBRACKET, RBRACKET, EOF, AND, OR, LT, GT, AT
    }


    public static class TypeParsingException extends RuntimeException {

        public TypeParsingException(String message) {
            super(message);
        }
    }

}
