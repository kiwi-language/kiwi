package org.metavm.autograph;

import com.intellij.psi.PsiExpression;
import com.intellij.psi.PsiTypeParameter;
import lombok.extern.slf4j.Slf4j;
import org.metavm.entity.GenericDeclaration;
import org.metavm.entity.StdKlass;
import org.metavm.flow.Method;
import org.metavm.flow.Node;
import org.metavm.object.type.*;
import org.metavm.util.CompilerException;

import java.util.*;

import static java.util.Objects.requireNonNull;

@Slf4j
public class CaptureProcessor {

    private final ExpressionResolver expressionResolver;
    private final TypeResolver typeResolver;
    private final MethodGenerator methodGenerator;
    private final Map<Node, Integer> captureVariables = new HashMap<>();

    public CaptureProcessor(MethodGenerator methodGenerator) {
        this.typeResolver = methodGenerator.getTypeResolver();
        expressionResolver = methodGenerator.getExpressionResolver();
        this.methodGenerator = methodGenerator;
    }

    public void constructType(Type type, MethodGenerator builder, Map<CapturedType, Integer> ctVars) {
        if(type.isCaptured()) {
            switch (type) {
                case CapturedType capturedType -> {
                   builder.createLoad(requireNonNull(ctVars.get(capturedType),
                        () -> "Cannot find variable for captured type " + capturedType.getTypeDesc() + ", variables: " + ctVars), StdKlass.type.type());
                }
                case KlassType klassType -> {
                    if (klassType.getOwner() instanceof KlassType ownerKt) {
                        constructType(ownerKt, builder, ctVars);
                        klassType.getTypeArguments().forEach(t -> constructType(t, builder, ctVars));
                        builder.createLoadInnerKlassType(klassType);
                    }
                    else if (klassType.isLocal()) {
                        klassType.getTypeArguments().forEach(t -> constructType(t, builder, ctVars));
                        builder.createLoadLocalKlassType(klassType);
                    } else {
                        klassType.getTypeArguments().forEach(t -> constructType(t, builder, ctVars));
                        builder.createLoadKlassType(klassType);
                    }
                }
                case ArrayType arrayType -> {
                    assert arrayType.getKind() == ArrayKind.READ_WRITE;
                    constructType(arrayType.getElementType(), builder, ctVars);
                    builder.createLoadArrayType();
                }
                case FunctionType functionType -> {
                    functionType.getParameterTypes().forEach(t -> constructType(t, builder, ctVars));
                    constructType(functionType.getReturnType(), builder, ctVars);
                    builder.createLoadFunctionType(functionType.getParameterTypes().size());
                }
                case UnionType unionType -> {
                    unionType.getMembers().forEach(t -> constructType(t, builder, ctVars));
                    builder.createLoadUnionType(unionType.getMembers().size());
                }
                case IntersectionType intersectionType -> {
                    intersectionType.getTypes().forEach(t -> constructType(t, builder, ctVars));
                    builder.createLoadIntersectionType(intersectionType.getTypes().size());
                }
                case UncertainType uncertainType -> {
                    constructType(uncertainType.getLowerBound(), builder, ctVars);
                    constructType(uncertainType.getUpperBound(), builder, ctVars);
                    builder.createLoadUncertainType();
                }
                default -> throw new CompilerException("Captured type is not allowed in " + type.getClass().getSimpleName());
            }
        }
        else
            builder.createLoadType(type);
    }

    public Map<CapturedType, Integer> extractCapturedTypes(Collection<Type> types) {
        var capturedTypes = new HashSet<CapturedType>();
        var extractor = new StructuralTypeVisitor() {
            @Override
            public Void visitCapturedType(CapturedType type, Void unused) {
                capturedTypes.add(type);
                return null;
            }
        };
        types.forEach(t -> t.accept(extractor, null));
        if (capturedTypes.isEmpty())
            return Map.of();
        var variables = new HashMap<CapturedType, Integer>();
        for (CapturedType capturedType : capturedTypes) {
            var psiCt = typeResolver.getPsiCapturedType(capturedType);
            var ctx = Objects.requireNonNull(psiCt.getContext());
            if (ctx instanceof PsiExpression expression) {
                var exprType = typeResolver.resolveDeclaration(expression.getType());
                var typeVar = capturedType.getVariable().getTypeVariable();
                methodGenerator.createLoad(getCapturedExpressionVariable(expression), exprType);
                methodGenerator.createTypeOf();
                methodGenerator.createLoadAncestorType(((Klass) typeVar.getGenericDeclaration()).getType());
//            methodGenerator.createTypeCast(StdKlass.klassType.type());
                methodGenerator.createLoadTypeArgument(typeVar.getIndex());
                var idx = methodGenerator.nextVariableIndex();
                methodGenerator.createStore(idx);
                variables.put(capturedType, idx);
            }
            else if (ctx instanceof PsiTypeParameter contextTypeParam) {
                var contextTypeVar = typeResolver.resolveTypeVariable(contextTypeParam).getVariable();
                GenericDeclaration genDecl = methodGenerator.getMethod();
                methodGenerator.createLoadCurrentFlow();
                for(;;) {
                    if (contextTypeVar.getGenericDeclaration() == genDecl) {
                        methodGenerator.createLoadTypeArgument(contextTypeVar.getIndex());
                        var typeParam = typeResolver.resolveTypeVariable(psiCt.getTypeParameter()).getVariable();
                        var declaringKlass = (Klass) typeParam.getGenericDeclaration();
                        methodGenerator.createLoadAncestorType(declaringKlass.getType());
                        methodGenerator.createLoadTypeArgument(typeParam.getIndex());
                        var idx = methodGenerator.nextVariableIndex();
                        methodGenerator.createStore(idx);
                        variables.put(capturedType, idx);
                        break;
                    }
                    else {
                        if (genDecl instanceof Method method) {
                            methodGenerator.createLoadDeclaringType();
                            genDecl = method.getDeclaringType();
                        }
                        else if (genDecl instanceof Klass klass) {
                            if (klass.isInner()) {
                                methodGenerator.createLoadOwnerType();
                                genDecl = Objects.requireNonNull(klass.getScope());
                            }
                            else if (klass.isLocal()) {
                                methodGenerator.createLoadOwnerType();
                                genDecl = Objects.requireNonNull(klass.getScope());
                            }
                            else {
                                throw new IllegalStateException("Cannot find generic declaration for type variable: " + contextTypeVar.getTypeDesc());
                            }
                        }
                        else
                            throw new IllegalStateException("Cannot find generic declaration for type variable: " + contextTypeVar.getTypeDesc());
                    }
                }
            }
        }
        return variables;
    }

    private int getCapturedExpressionVariable(PsiExpression expression) {
        var node = Objects.requireNonNull(expressionResolver.getResolvedNode(expression));
        var idx = captureVariables.get(node);
        if (idx == null) {
            idx = methodGenerator.nextVariableIndex();
            captureVariables.put(node, idx);
            methodGenerator.recordValue(node, idx);
        }
        return idx;
    }

//    private record Path(List<PathItem> items) {
//
//        private Path {
//            items = new ArrayList<>(items);
//        }
//
//        @Override
//        public String toString() {
//            return NncUtils.join(items, Object::toString, ".");
//        }
//    }
//
//    private interface PathItem {
//
//        void generateExtraction(MethodGenerator builder);
//
//    }
//
//    private record ElementPathItem() implements PathItem {
//
//        @Override
//        public void generateExtraction(MethodGenerator builder) {
//            builder.createLoadElementType();
//        }
//
//        @Override
//        public String toString() {
//            return "element";
//        }
//    }
//
//    private record OwnerPathItem() implements PathItem {
//
//        @Override
//        public void generateExtraction(MethodGenerator builder) {
//            builder.createLoadOwnerType();
//        }
//
//        @Override
//        public String toString() {
//            return "owner";
//        }
//    }
//
//    private record TypeArgumentPathItem(int index) implements PathItem {
//
//        @Override
//        public void generateExtraction(MethodGenerator builder) {
//            builder.createLoadTypeArgument(index);
//        }
//
//        @Override
//        public String toString() {
//            return "typeArguments[" + index + "]";
//        }
//    }
//
//    private record UnderlyingPathItem() implements PathItem {
//
//        @Override
//        public void generateExtraction(MethodGenerator builder) {
//            builder.createLoadUnderlyingType();
//        }
//
//        @Override
//        public String toString() {
//            return "underlyingType";
//        }
//    }
//
//    private record ParameterTypePathItem(int index) implements PathItem {
//
//        @Override
//        public void generateExtraction(MethodGenerator builder) {
//            builder.createLoadParameterType(index);
//        }
//
//        @Override
//        public String toString() {
//            return "parameterType[" + index + "]";
//        }
//    }
//
//    private record ReturnTypePathItem() implements PathItem {
//
//        @Override
//        public void generateExtraction(MethodGenerator builder) {
//            builder.createLoadReturnType();
//        }
//
//        @Override
//        public String toString() {
//            return "returnType";
//        }
//    }

}
