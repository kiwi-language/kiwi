package tech.metavm.transpile;

import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.tree.TerminalNode;
import tech.metavm.transpile.ir.*;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;

import java.util.List;
import java.util.Set;

import static tech.metavm.transpile.IRTypeUtil.*;
import static tech.metavm.transpile.JavaParser.*;
import static tech.metavm.util.ReflectUtils.getDeclaredFieldRecursively;

public class ExpressionTypeResolverV2 {

    public static final Set<String> FIRST_TYPE_BINARY_OPS = Set.of(
      "+=", "-=", "*=", "/=", "=", "&=", "|=","^=" ,">>=" , ">>>=" , "<<=" , "%="
    );

    public static final Set<String> TYPE_MERGING_BINARY_OPS = Set.of(
            "+", "-", "*", "/", "&&", "||", "^", "&", "|", "%"
    );

    public static final Set<String> BOOL_RESULTING_OPS = Set.of(
        ">", "<", "==", ">=", "<=", "!="
    );

    private final SymbolTableV2 symbolTable;

    public ExpressionTypeResolverV2(SymbolTableV2 symbolTable) {
        this.symbolTable = symbolTable;
    }

    public IRType resolve(ExpressionContext expression) {
        if(expression.primary() != null) {
            return resolvePrimary(expression.primary());
        }
        if(expression.INSTANCEOF() != null) {
            return fromClass(boolean.class);
        }
        if(expression.methodCall() != null) {
            return resolveMethodFromCall(symbolTable.currentClass(), expression.methodCall()).returnType();
        }
        if(expression.creator() != null) {
            return resolveCreatorType(expression.creator());
        }
        if(expression.prefix != null || expression.postfix != null) {
            return resolve(expression.expression(0));
        }
        if(expression.lambdaExpression() != null) {
            return resolveLambdaExpression(expression.lambdaExpression());
        }
        if(expression.switchExpression() != null) {
            return resolveSwitchExpression(expression.switchExpression());
        }
        if(expression.bop != null) {
            return resolveBopExpression(expression);
        }
        ChildrenStream stream = new ChildrenStream(expression);
        if(stream.isNextExpression()) {
            var firstOperand = stream.nextExpression();
            var firstOperandType = resolve(firstOperand);
            if(stream.isNextLiteral("[")) {
                return resolveArrayElement((IRArrayType) firstOperandType);
            }
        }
        else if(stream.isNextLiteral("(")) {
            stream.nextList(AnnotationContext.class);
            return parseTypeType(stream.nextTypeType());
        }
        return null;
    }

    private IRType resolveLambdaExpression(LambdaExpressionContext lambdaExpression) {
        return LambdaTypeMatchUtil.getLambdaType(lambdaExpression);
    }

    private IRType resolveSwitchExpression(SwitchExpressionContext switchExpression) {
        return SwitchExpressionTypeResolverV2.resolve(switchExpression, this);
    }

    private IRType resolveCreatorType(CreatorContext creator) {
        StringBuilder type = new StringBuilder(resolveCreatedName(creator.createdName()));
        if(creator.arrayCreatorRest() != null) {
            var arrayCreatorRest = creator.arrayCreatorRest();
            int dimensions = 0;
            for (int i = 0; i < arrayCreatorRest.getChildCount(); i++) {
                var child = arrayCreatorRest.getChild(i);
                if(child instanceof TerminalNode terminalNode && terminalNode.getText().equals("[")) {
                    dimensions++;
                }
            }
            if(dimensions > 0) {
                type.insert(0, "L");
                for (int i = 0; i < dimensions; i++) {
                    type.insert(0, "[");
                }
            }
        }
        return classForName(type.toString());
    }

    private String resolveCreatedName(CreatedNameContext createdName) {
        if(createdName.primitiveType() != null) {
            return parsePrimitiveType(createdName.primitiveType()).getName();
        }
        else {
            return NncUtils.join(createdName.identifier(), RuleContext::getText, ".");
        }
    }

    private IRType resolveArrayElement(IRArrayType arrayType) {
        return arrayType.getElementType();
    }

    private IRType resolveBopExpression(ExpressionContext expression) {
        ChildrenStream stream = new ChildrenStream(expression);
        var firstOperand = stream.nextExpression();
        String bop = stream.nextTerminal().getText();
        var secondOperand = stream.nextExpression();
        var secondOperandType = resolve(secondOperand);
        if(bop.equals(".")) {
            stream.next();
            return parseDotExpression(firstOperand, stream);
        }

        var firstOperandType = resolve(firstOperand);
        if(TYPE_MERGING_BINARY_OPS.contains(bop)) {
            return getBinaryOpResultType(firstOperandType, secondOperandType);
        }
        if(BOOL_RESULTING_OPS.contains(bop)) {
            return fromClass(boolean.class);
        }
        if(FIRST_TYPE_BINARY_OPS.contains(bop)) {
            return firstOperandType;
        }
        throw new InternalException("Unrecognized operator '" + bop + "'");
    }

    private IRType getBinaryOpResultType(IRType firstType, IRType secondType) {
        IRClass klass1 = IRTypeUtil.unbox(IRUtil.getRawClass(firstType)),
                klass2 = IRTypeUtil.unbox(IRUtil.getRawClass(secondType));
        if(klass1 == klass2) {
            return klass1;
        }
        if(klass1 == doubleClass() || klass2 == doubleClass()) {
            return doubleClass();
        }
        if(klass1 == floatClass() || klass2 == floatClass()) {
            return floatClass();
        }
        if(klass1 == longClass() || klass2 == longClass()) {
            return longClass();
        }
        if(klass1 == intClass() || klass2 == intClass()) {
            return intClass();
        }
        if(klass1 == shortClass() || klass2 == shortClass()) {
            return shortClass();
        }
        if(klass1 == byteClass() || klass2 == byteClass()) {
            return byteClass();
        }
        return klass1;
    }

    private IRClass tryResolveClass(ExpressionContext expressionCtx) {
        if(expressionCtx.primary() != null && expressionCtx.primary().identifier() != null) {
            var symbol = symbolTable.resolveValueOrClass(expressionCtx.primary().identifier().getText());
            if(symbol instanceof IRClass klass) {
                return klass;
            }
        }
        return null;
    }

    private IRType parseDotExpression(ExpressionContext firstOperand, ChildrenStream stream) {
        var klass = tryResolveClass(firstOperand);
        if(klass != null) {
            if (stream.isNextIdentifier()) {
                return getDeclaredFieldRecursively(klass, stream.nextIdentifier().getText()).type();
            }
            if (stream.isNextInstanceOf(MethodCallContext.class)) {
                return resolveMethodFromCall(
                        klass,
                        stream.next(MethodCallContext.class)
                ).returnType();
            }
            if (stream.isNextTerminalOf(JavaLexer.THIS)) {
                return klass;
            }
            else {
                throw new InternalException("Malformed dot expression");
            }
        }
        else {
            var firstOperandType = resolve(firstOperand);
            if (stream.isNextIdentifier()) {
                var identifier = stream.nextIdentifier().getText();
                if(firstOperandType instanceof IRArrayType) {
                    if(identifier.equals("length")) {
                        return IRTypeUtil.intClass();
                    }
                    else {
                        throw new InternalException("Unexpected identifier '" + identifier + "'");
                    }
                }
                else {
                    IRClass firstOpClass = IRUtil.getRawClass(firstOperandType);
                    return getDeclaredFieldRecursively(firstOpClass, identifier).type();
                }
            }
            if (stream.isNextInstanceOf(MethodCallContext.class)) {
                return resolveMethodFromCall(
                        IRUtil.getRawClass(firstOperandType),
                        stream.next(MethodCallContext.class)
                ).returnType();
            }
            if (stream.isNextTerminalOf(JavaLexer.THIS)) {
                return firstOperandType;
            }
            if (stream.isNextTerminalOf(JavaLexer.NEW)) {
                stream.next();
                stream.skipIfNextInstanceOf(NonWildcardTypeArgumentsContext.class);
                var innerCreator = stream.next(InnerCreatorContext.class);
                return classForName(symbolTable.getQualifiedName(innerCreator.identifier().getText()));
            }
            if (stream.isNextTerminalOf(JavaLexer.SUPER)) {
                stream.next();
                var superSuffix = stream.next(SuperSuffixContext.class);
                return resolveSuperSuffix(firstOperandType, superSuffix);
            } else {
                return resolveExplicitGenericInvocation(stream.next(ExplicitGenericInvocationContext.class));
            }
        }
    }

    private IRType resolveSuperSuffix(IRType prefixType, SuperSuffixContext superSuffix) {
        if(superSuffix.arguments() != null) {
            return fromClass(void.class);
        }
        else {
            return resolveMethodFromCall(
                    NncUtils.requireNonNull(IRUtil.getRawClass(prefixType).getRawSuperClass()),
                    superSuffix.identifier(),
                    superSuffix.arguments().expressionList()
            ).returnType();
        }
    }

    private IRType resolveExplicitGenericInvocation(ExplicitGenericInvocationContext explicitGenericInvocation) {
        var suffix = explicitGenericInvocation.explicitGenericInvocationSuffix();
        if(suffix.SUPER() != null) {
            return resolveSuperSuffix(symbolTable.currentClass(), suffix.superSuffix());
        }
        else {
            return resolveMethodFromCall(
                    symbolTable.currentClass(), suffix.identifier(), suffix.arguments().expressionList()
            ).returnType();
        }
    }

    public IRMethod resolveMethodFromCall(MethodCallContext methodCall) {
        return resolveMethodFromCall(symbolTable.currentClass(), methodCall);
    }

    public IRMethod resolveMethodFromCall(IRClass klass, MethodCallContext methodCall) {
        return resolveMethodFromCall(klass, methodCall.identifier(), methodCall.expressionList());
    }

    public IRMethod resolveMethodFromCall(IRClass klass, IdentifierContext identifier, ExpressionListContext expressionList) {
        List<IRType> paramTypes = resolveExpressionList(expressionList);
        return klass.getMethod(identifier.getText(), paramTypes);
    }

    private IRType resolveLiteral(LiteralContext literal) {
        if(literal.BOOL_LITERAL() != null) {
            return fromClass(boolean.class);
        }
        if(literal.CHAR_LITERAL() != null) {
            return fromClass(char.class);
        }
        if(literal.floatLiteral() != null) {
            return fromClass(float.class);
        }
        if(literal.NULL_LITERAL() != null) {
            return nullType();
        }
        if(literal.STRING_LITERAL() != null || literal.TEXT_BLOCK() != null) {
            return fromClass(String.class);
        }
        if(literal.integerLiteral() != null) {
            return fromClass(int.class);
        }
        throw new InternalException("Can not resolve literal '" + literal.getText() + "'");
    }

    private IRType resolveIdentifier(IdentifierContext identifier) {
        return symbolTable.resolveValue(identifier.getText()).type();
    }

    private IRType resolveTypeTypeOrVoid(TypeTypeOrVoidContext typeTypeOrVoid) {
        if(typeTypeOrVoid.typeType() != null) {
            return parseTypeType(typeTypeOrVoid.typeType());
        }
        else {
            return voidType();
        }
    }

    private List<IRType> resolveExpressionList(ExpressionListContext expressionList) {
        if(expressionList == null) {
            return List.of();
        }
        else {
            return NncUtils.map(expressionList.expression(), this::resolve);
        }
    }

    private IRType resolveGenericInvocation(ChildrenStream stream) {
        stream.next();
        if(stream.isNextInstanceOf(ExplicitGenericInvocationSuffixContext.class)) {
            var suffix = stream.next(ExplicitGenericInvocationSuffixContext.class);
            if(suffix.identifier() != null) {
                return symbolTable.resolveMethod(
                        suffix.identifier().getText(),
                        resolveExpressionList(suffix.arguments().expressionList())
                ).returnType();
            }
            else {
                return voidType();
            }
        }
        else {
            return voidType();
        }
    }

    private IRType resolvePrimary(PrimaryContext primary) {
        ChildrenStream stream = new ChildrenStream(primary);
        if(stream.isNextLiteral("(")) {
            return resolve(stream.nextExpression());
        }
        if(stream.isNextTerminalOf(JavaLexer.THIS)) {
            return classForName(symbolTable.currentClass().getName());
        }
        if(stream.isNextTerminalOf(JavaLexer.SUPER)) {
            var superClass = NncUtils.requireNonNull(
                    symbolTable.currentClass().getRawSuperClass(),
                    "'" + symbolTable.currentClass().getName() + "' doesn't have a super class"
            );
            return classForName(superClass.getName());
        }
        if(stream.isNextInstanceOf(LiteralContext.class)) {
            return resolveLiteral(stream.next(LiteralContext.class));
        }
        if(stream.isNextIdentifier()) {
            return resolveIdentifier(stream.nextIdentifier());
        }
        if(stream.isNextInstanceOf(TypeTypeOrVoidContext.class)) {
            return resolveTypeTypeOrVoid(stream.next(TypeTypeOrVoidContext.class));
        }
        else {
            return resolveGenericInvocation(stream);
        }
    }

    public Class<?> parsePrimitiveClass(PrimitiveTypeContext primitiveType) {
        var typeName = primitiveType.getText();
        if(typeName.equals("int")) {
            return int.class;
        }
        if(typeName.equals("long")) {
            return long.class;
        }
        if(typeName.equals("byte")) {
            return byte.class;
        }
        if(typeName.equals("short")) {
            return short.class;
        }
        if(typeName.equals("float")) {
            return float.class;
        }
        if(typeName.equals("double")) {
            return double.class;
        }
        if(typeName.equals("boolean")) {
            return boolean.class;
        }
        if(typeName.equals("char")) {
            return char.class;
        }
        throw new InternalException("Unrecognized primitive type '" + typeName + "'");
    }

    public IRClass parsePrimitiveType(PrimitiveTypeContext primitiveType) {
        return fromClass(parsePrimitiveClass(primitiveType));
    }

    private IRType parseTypeType(TypeTypeContext typeType) {
        return parseTypeType(typeType, 0);
    }

    private IRType parseTypeType(TypeTypeContext typeType, int extraDimensions) {
        var type = typeType.primitiveType() != null ?
                parsePrimitiveType(typeType.primitiveType())
                : parseClassOrInterface(typeType.classOrInterfaceType());
        ChildrenStream stream = new ChildrenStream(typeType);
        int dimensions = stream.countLiteral("[") + extraDimensions;
        for (int i = 0; i < dimensions; i++) {
            type = type.getArrayType();
        }
        return type;
    }


    private IRType parseClassOrInterface(ClassOrInterfaceTypeContext classOrInterface) {
        var namePrefix = NncUtils.join(
                classOrInterface.identifier(),
                RuleContext::getText,
                "."
        );
        var name = namePrefix.length() > 0 ?
                namePrefix + "." + classOrInterface.typeIdentifier().getText()
                : classOrInterface.typeIdentifier().getText();
        var klass = classForName(symbolTable.getQualifiedName(name));
        int nTypeArgs = classOrInterface.typeArguments().size();
        if(nTypeArgs == 0) {
            return klass;
        }
        else {
            var typeArgsContext = classOrInterface.typeArguments(nTypeArgs-1);
            List<IRType> typeArgs = NncUtils.map(
                    typeArgsContext.typeArgument(),
                    t -> parseTypeType(t.typeType())
            );
            return new PType(null, klass, typeArgs);
        }
    }

}
