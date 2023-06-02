//package tech.metavm.transpile;
//
//import org.antlr.v4.runtime.RuleContext;
//import org.antlr.v4.runtime.tree.TerminalNode;
//import tech.metavm.util.InternalException;
//import tech.metavm.util.NncUtils;
//import tech.metavm.util.ReflectUtils;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Set;
//
//import static tech.metavm.transpile.JavaParser.*;
//import static tech.metavm.transpile.SourceTypeUtil.*;
//import static tech.metavm.util.ReflectUtils.*;
//
//public class ExpressionTypeResolver {
//
//    public static final SourceType VOID_TYPE = new VoidSourceType();
//
//    public static final Set<String> FIRST_TYPE_BINARY_OPS = Set.of(
//      "+=", "-=", "*=", "/=", "=", "&=", "|=","^=" ,">>=" , ">>>=" , "<<=" , "%="
//    );
//
//    public static final Set<String> TYPE_MERGING_BINARY_OPS = Set.of(
//            "+", "-", "*", "/", "&&", "||", "^", "&", "|", "%"
//    );
//
//    public static final Set<String> BOOL_RESULTING_OPS = Set.of(
//        ">", "<", "==", ">=", "<=", "!="
//    );
//
//    private final SymbolTable symbolTable;
//
//    public ExpressionTypeResolver(SymbolTable symbolTable) {
//        this.symbolTable = symbolTable;
//    }
//
//    public SourceType resolve(ExpressionContext expression) {
//        if(expression.primary() != null) {
//            return resolvePrimary(expression.primary());
//        }
//        if(expression.INSTANCEOF() != null) {
//            return fromClass(boolean.class);
//        }
//        if(expression.methodCall() != null) {
//            return resolveMethodFromCall(symbolTable.currentClass(), expression.methodCall()).type();
//        }
//        if(expression.creator() != null) {
//            return resolveCreatorType(expression.creator());
//        }
//        if(expression.prefix != null || expression.postfix != null) {
//            return resolve(expression.expression(0));
//        }
//        if(expression.lambdaExpression() != null) {
//            return resolveLambdaExpression(expression.lambdaExpression());
//        }
//        if(expression.switchExpression() != null) {
//            return resolveSwitchExpression(expression.switchExpression());
//        }
//        if(expression.bop != null) {
//            return resolveBopExpression(expression);
//        }
//        ChildrenStream stream = new ChildrenStream(expression);
//        if(stream.isNextExpression()) {
//            var firstOperand = stream.nextExpression();
//            var firstOperandType = resolve(firstOperand);
//            if(stream.isNextLiteral("[")) {
//                return resolveArrayElement(firstOperandType);
//            }
//        }
//        else if(stream.isNextLiteral("(")) {
//            stream.nextList(AnnotationContext.class);
//            return fromClass(parseRawClass(stream.nextTypeType()));
//        }
//        return null;
//    }
//
//    private SourceType resolveLambdaExpression(LambdaExpressionContext lambdaExpression) {
//        return LambdaTypeMatchUtil.getLambdaType(lambdaExpression);
//    }
//
//    private SourceType resolveSwitchExpression(SwitchExpressionContext switchExpression) {
//        return SwitchExpressionTypeResolver.resolve(switchExpression, this);
//    }
//
//    private SourceType resolveCreatorType(CreatorContext creator) {
//        StringBuilder type = new StringBuilder(resolveCreatedName(creator.createdName()));
//        if(creator.arrayCreatorRest() != null) {
//            var arrayCreatorRest = creator.arrayCreatorRest();
//            int dimensions = 0;
//            for (int i = 0; i < arrayCreatorRest.getChildCount(); i++) {
//                var child = arrayCreatorRest.getChild(i);
//                if(child instanceof TerminalNode terminalNode && terminalNode.getText().equals("[")) {
//                    dimensions++;
//                }
//            }
//            if(dimensions > 0) {
//                type.insert(0, "L");
//                for (int i = 0; i < dimensions; i++) {
//                    type.insert(0, "[");
//                }
//            }
//        }
//        return fromClassName(type.toString());
//    }
//
//    private String resolveCreatedName(CreatedNameContext createdName) {
//        if(createdName.primitiveType() != null) {
//            return parsePrimitiveType(createdName.primitiveType()).getName();
//        }
//        else {
//            return NncUtils.join(createdName.identifier(), RuleContext::getText, ".");
//        }
//    }
//
//    private SourceType resolveArrayElement(SourceType arrayType) {
//        return fromClass(arrayType.getReflectClass().getComponentType());
//    }
//
//    private SourceType resolveBopExpression(ExpressionContext expression) {
//        ChildrenStream stream = new ChildrenStream(expression);
//        var firstOperand = stream.nextExpression();
//        var firstOperandType = resolve(firstOperand);
//        String bop = stream.nextTerminal().getText();
//        var secondOperand = stream.nextExpression();
//        var secondOperandType = resolve(secondOperand);
//        if(bop.equals(".")) {
//            stream.next();
//            return parseDotExpression(firstOperandType, stream);
//        }
//        if(TYPE_MERGING_BINARY_OPS.contains(bop)) {
//            return getBinaryOpResultType(firstOperandType, secondOperandType);
//        }
//        if(BOOL_RESULTING_OPS.contains(bop)) {
//            return fromClass(boolean.class);
//        }
//        if(FIRST_TYPE_BINARY_OPS.contains(bop)) {
//            return firstOperandType;
//        }
//        throw new InternalException("Unrecognized operator '" + bop + "'");
//    }
//
//    private SourceType getBinaryOpResultType(SourceType firstType, SourceType secondType) {
//        Class<?> klass1 = ReflectUtils.unbox(firstType.getReflectClass()),
//                klass2 = ReflectUtils.unbox(secondType.getReflectClass());
//        if(klass1 == klass2) {
//            return fromClass(klass1);
//        }
//        if(klass1 == double.class || klass2 == double.class) {
//            return fromClass(double.class);
//        }
//        if(klass1 == float.class || klass2 == float.class) {
//            return fromClass(float.class);
//        }
//        if(klass1 == long.class || klass2 == long.class) {
//            return fromClass(long.class);
//        }
//        if(klass1 == int.class || klass2 == int.class) {
//            return fromClass(int.class);
//        }
//        if(klass1 == short.class || klass2 == short.class) {
//            return fromClass(short.class);
//        }
//        if(klass1 == byte.class || klass2 == byte.class) {
//            return fromClass(short.class);
//        }
//        return fromClass(klass1);
//    }
//
//    private SourceType parseDotExpression(SourceType firstOperandType, ChildrenStream stream) {
//        if(stream.isNextIdentifier()) {
//            Class<?> firstOpClass = firstOperandType.getReflectClass();
//            return fromClassName(
//                    getDeclaredFieldRecursively(firstOpClass, stream.nextIdentifier().getText()).getType().getName()
//            );
//        }
//        if(stream.isNextInstanceOf(MethodCallContext.class)) {
//            return resolveMethodFromCall(
//                    symbolTable.getClassScope(firstOperandType.getReflectClass().getName()),
//                    stream.next(MethodCallContext.class)
//            ).type();
//        }
//        if(stream.isNextTerminalOf(JavaLexer.THIS)) {
//            return firstOperandType;
//        }
//        if(stream.isNextTerminalOf(JavaLexer.NEW)) {
//            stream.next();
//            stream.skipIfNextInstanceOf(NonWildcardTypeArgumentsContext.class);
//            var innerCreator = stream.next(InnerCreatorContext.class);
//            return fromClassName(symbolTable.getQualifiedName(innerCreator.identifier().getText()));
//        }
//        if(stream.isNextTerminalOf(JavaLexer.SUPER)) {
//            stream.next();
//            var superSuffix = stream.next(SuperSuffixContext.class);
//            return resolveSuperSuffix(firstOperandType, superSuffix);
//        }
//        else {
//            return resolveExplicitGenericInvocation(stream.next(ExplicitGenericInvocationContext.class));
//        }
//    }
//
//    private SourceType resolveSuperSuffix(SourceType prefixType, SuperSuffixContext superSuffix) {
//        if(superSuffix.arguments() != null) {
//            return VOID_TYPE;
//        }
//        else {
//            return resolveMethodFromCall(
//                    symbolTable.getClassScope(prefixType.getReflectClassName()).getSuperClassRequired(),
//                    superSuffix.identifier(),
//                    superSuffix.arguments().expressionList()
//            ).type();
//        }
//    }
//
//    private SourceType resolveExplicitGenericInvocation(ExplicitGenericInvocationContext explicitGenericInvocation) {
//        var suffix = explicitGenericInvocation.explicitGenericInvocationSuffix();
//        if(suffix.SUPER() != null) {
//            return resolveSuperSuffix(symbolTable.currentClass().getSourceType(), suffix.superSuffix());
//        }
//        else {
//            return resolveMethodFromCall(
//                    symbolTable.currentClass(), suffix.identifier(), suffix.arguments().expressionList()
//            ).type();
//        }
//    }
//
//
//    public Symbol resolveMethodFromCall(MethodCallContext methodCall) {
//        return resolveMethodFromCall(symbolTable.currentClass(), methodCall);
//    }
//
//    public Symbol resolveMethodFromCall(ClassScope classScope, MethodCallContext methodCall) {
//        return resolveMethodFromCall(classScope, methodCall.identifier(), methodCall.expressionList());
//    }
//
//    public Symbol resolveMethodFromCall(ClassScope classScope, IdentifierContext identifier, ExpressionListContext expressionList) {
//        List<SourceType> paramTypes = resolveExpressionList(expressionList);
//        return classScope.resolveMethod(identifier.getText(), paramTypes);
//    }
//
//    private SourceType resolveLiteral(LiteralContext literal) {
//        if(literal.BOOL_LITERAL() != null) {
//            return fromClass(boolean.class);
//        }
//        if(literal.CHAR_LITERAL() != null) {
//            return fromClass(char.class);
//        }
//        if(literal.floatLiteral() != null) {
//            return fromClass(float.class);
//        }
//        if(literal.NULL_LITERAL() != null) {
//            return new NullSourceType();
//        }
//        if(literal.STRING_LITERAL() != null || literal.TEXT_BLOCK() != null) {
//            return fromClass(String.class);
//        }
//        if(literal.integerLiteral() != null) {
//            return fromClass(int.class);
//        }
//        throw new InternalException("Can not resolve literal '" + literal.getText() + "'");
//    }
//
//    private SourceType resolveIdentifier(IdentifierContext identifier) {
//        return symbolTable.resolveType(identifier.getText()).type();
//    }
//
//    private SourceType resolveTypeTypeOrVoid(TypeTypeOrVoidContext typeTypeOrVoid) {
//        if(typeTypeOrVoid.typeType() != null) {
//            return fromClass(parseRawClass(typeTypeOrVoid.typeType()));
//        }
//        else {
//            return VOID_TYPE;
//        }
//    }
//
//    private List<SourceType> resolveExpressionList(ExpressionListContext expressionList) {
//        if(expressionList == null) {
//            return List.of();
//        }
//        else {
//            return NncUtils.map(expressionList.expression(), this::resolve);
//        }
//    }
//
//    private SourceType resolveGenericInvocation(ChildrenStream stream) {
//        stream.next();
//        if(stream.isNextInstanceOf(ExplicitGenericInvocationSuffixContext.class)) {
//            var suffix = stream.next(ExplicitGenericInvocationSuffixContext.class);
//            if(suffix.identifier() != null) {
//                return symbolTable.resolveMethod(
//                        suffix.identifier().getText(),
//                        resolveExpressionList(suffix.arguments().expressionList())
//                ).type();
//            }
//            else {
//                return VOID_TYPE;
//            }
//        }
//        else {
//            return VOID_TYPE;
//        }
//    }
//
//    private SourceType resolvePrimary(PrimaryContext primary) {
//        ChildrenStream stream = new ChildrenStream(primary);
//        if(stream.isNextLiteral("(")) {
//            return resolve(stream.nextExpression());
//        }
//        if(stream.isNextTerminalOf(JavaLexer.THIS)) {
//            return fromClassName(symbolTable.currentClass().getName());
//        }
//        if(stream.isNextTerminalOf(JavaLexer.SUPER)) {
//            var superClass = NncUtils.requireNonNull(
//                    symbolTable.currentClass().getSuperClass(),
//                    "'" + symbolTable.currentClass().getName() + "' doesn't have a super class"
//            );
//            return fromClassName(superClass.getName());
//        }
//        if(stream.isNextInstanceOf(LiteralContext.class)) {
//            return resolveLiteral(stream.next(LiteralContext.class));
//        }
//        if(stream.isNextIdentifier()) {
//            return resolveIdentifier(stream.nextIdentifier());
//        }
//        if(stream.isNextInstanceOf(TypeTypeOrVoidContext.class)) {
//            return resolveTypeTypeOrVoid(stream.next(TypeTypeOrVoidContext.class));
//        }
//        else {
//            return resolveGenericInvocation(stream);
//        }
//    }
//
//    public Class<?> parsePrimitiveType(PrimitiveTypeContext primitiveType) {
//        var typeName = primitiveType.getText();
//        if(typeName.equals("int")) {
//            return int.class;
//        }
//        if(typeName.equals("long")) {
//            return long.class;
//        }
//        if(typeName.equals("byte")) {
//            return byte.class;
//        }
//        if(typeName.equals("short")) {
//            return short.class;
//        }
//        if(typeName.equals("float")) {
//            return float.class;
//        }
//        if(typeName.equals("double")) {
//            return double.class;
//        }
//        if(typeName.equals("boolean")) {
//            return boolean.class;
//        }
//        if(typeName.equals("char")) {
//            return char.class;
//        }
//        throw new InternalException("Unrecognized primitive type '" + typeName + "'");
//    }
//
//    public Class<?> parseRawClass(TypeTypeContext typeType) {
//        if(typeType.primitiveType() != null) {
//            return parsePrimitiveType(typeType.primitiveType());
//        }
//        else {
//            var path = new ArrayList<>(
//                    NncUtils.map(
//                            typeType.classOrInterfaceType().identifier(),
//                            RuleContext::getText
//                    )
//            );
//            path.add(typeType.classOrInterfaceType().typeIdentifier().getText());
//            int pkgEndIdx = 0;
//            for (String s : path) {
//                if(Character.isUpperCase(s.charAt(0))) {
//                    break;
//                }
//                pkgEndIdx++;
//            }
//            if(pkgEndIdx >= path.size()) {
//                throw new InternalException("Invalid class name '" + NncUtils.join(path, "."));
//            }
//            String qualifiedName = symbolTable.getQualifiedName(
//                    NncUtils.join(path.subList(0, pkgEndIdx + 1), ".")
//            );
//            var klass = classForName(qualifiedName);
//            for (int i = pkgEndIdx + 1; i < path.size(); i++) {
//                klass = getInnerClassRecursively(klass, path.get(i));
//            }
//            return klass;
//        }
//    }
//
//
//}
