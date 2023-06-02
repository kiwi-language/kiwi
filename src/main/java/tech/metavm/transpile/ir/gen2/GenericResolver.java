package tech.metavm.transpile.ir.gen2;

import tech.metavm.transpile.ObjectClass;
import tech.metavm.transpile.ir.*;
import tech.metavm.transpile.ir.gen.XType;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GenericResolver {

    public IRExpression resolve(IRExpression expression, @Nullable IRType returnType) {
        return switch (expression) {
            case UnresolvedMethodCall umc -> resolveMethodCall(umc, returnType);
            case UnresolvedCreator un -> resolveCreator(un, returnType);
            case BinaryIRExpression b -> resolveBinary(b);
            case UnaryExpression u -> resolveUnary(u);
            case ArrayExpression a -> resolveArray(a);
            case ArrayElementExpression e -> resolveArrayElement(e);
            case CastExpression cst -> resolveCast(cst);
            case ArrayLengthExpression al -> resolveArrayLength(al);
            default -> expression;
        };
    }

    private IRExpression resolveCreator(UnresolvedCreator un, @Nullable IRType returnType) {
        for (IRConstructor constructor : un.klass().getConstructors()) {
            PartiallyResolvedCreator result;
            if((result = tryResolveCreator(un, constructor)) != null) {
                return returnType != null ? fullyResolveCreator(result, returnType) : result;
            }
        }
        throw new InternalException("Can not resolve new expression " + un);
    }

    private CreatorExpression fullyResolveCreator(PartiallyResolvedCreator pn, IRType returnType) {
        var graph = new GraphImpl(pn.graph());
        var typeMap = getTypeVariableMap(pn);
        var substituteReturnType = typeMap.substituteType(pn, pn.klass().templateOrSelf());
        graph.addExtension(substituteReturnType, returnType);
        return createCreator(pn, graph.getSolution());
    }

    private CreatorExpression createCreator(PartiallyResolvedCreator pn, Map<XType, IRType> solution) {
        var resolvedArgs = NncUtils.map(pn.arguments(), arg -> createResolved(arg, solution));
        var typeMap = getTypeVariableMap(pn);
        var typeArguments = NncUtils.map(
                pn.constructor().typeParameters(),
                tp -> solution.get((XType) typeMap.get(pn, tp))
        );
        var instanceType = typeMap.substituteType(pn, pn.klass().templateOrSelf());
        return pn.toCreator(instanceType, typeArguments, resolvedArgs);
    }

    private @Nullable PartiallyResolvedCreator tryResolveCreator(UnresolvedCreator un, IRConstructor constructor) {
        var arguments = un.arguments();
        var typeMap = createTypeVariableMap(un, constructor.typeParameters());
        typeMap.merge(createTypeVariableMap(un, constructor.declaringClass().typeParameters()));

        var substitutedParamTypes = NncUtils.map(
                constructor.parameterTypes(),
                t -> typeMap.substituteType(un, t)
        );

        var graph = new GraphImpl();
        addBuiltinBounds(typeMap.getMap(un), graph);

        List<IRExpression> resolvedArgs = NncUtils.map(arguments, arg -> resolve(arg, null));
        IRExpression resolvedOwner = NncUtils.get(un.owner(), o -> resolve(o, ObjectClass.getInstance()));

        List<IRType> argTypes = NncUtils.map(
                resolvedArgs,
                arg -> typeMap.substituteType(arg, arg.type())
        );

        graph.batchAddExtensions(argTypes, substitutedParamTypes);
        if(graph.isSolvable()) {
            return un.toPartiallyResolved(constructor, resolvedOwner, resolvedArgs, graph);
        }
        else {
            return null;
        }
    }

    private IRExpression resolveUnary(UnaryExpression expression) {
        return new UnaryExpression(
                resolve(expression.operand(), ObjectClass.getInstance()),
                expression.operator()
        );
    }

    private IRExpression resolveCast(CastExpression castExpression) {
        return new CastExpression(
                resolve(castExpression.expression(), castExpression.type()),
                castExpression.type()
        );
    }

    private IRExpression resolveArrayLength(ArrayLengthExpression expression) {
        return new ArrayLengthExpression(
                resolve(expression.array(), ObjectClass.getInstance())
        );
    }

    private IRExpression resolveArrayElement(ArrayElementExpression expression) {
        return new ArrayElementExpression(
                resolve(expression.array(), ObjectClass.getInstance()),
                expression.index()
        );
    }

    private IRExpression resolveArray(ArrayExpression expression) {
        return new ArrayExpression(
                expression.type(),
                NncUtils.map(expression.elements(), e -> resolve(e, expression.type().getElementType()))
        );
    }

    private IRExpression resolveBinary(BinaryIRExpression expression) {
        return new BinaryIRExpression(
                resolve(expression.getFirst(), ObjectClass.getInstance()),
                expression.getOperator(),
                resolve(expression.getSecond(), ObjectClass.getInstance())
        );
    }

    public IRExpression resolveMethodCall(UnresolvedMethodCall umc, @Nullable IRType returnType) {
        for (IRMethod method : umc.methods()) {
            PartiallyResolvedMethodCall result;
            if((result = tryResolveMethodCall(umc, method)) != null) {
                return returnType != null ? fullyResolveMethodCall(result, returnType) : result;
            }
        }
        throw new InternalException("Can not resolve method call " + umc);
    }

    public MethodCall fullyResolveMethodCall(PartiallyResolvedMethodCall pmc, IRType returnType) {
        var graph = new GraphImpl(pmc.graph());
        var method = pmc.method();
        var typeMap = getTypeVariableMap(pmc);
        var substitutedReturnType = typeMap.substituteType(pmc, method.returnType());
        graph.addExtension(substitutedReturnType, returnType);
        return createMethodCall(pmc, graph.getSolution());
    }

    private IRExpression createResolved(IRExpression expression, Map<XType, IRType> solution) {
        if(expression instanceof PartiallyResolvedMethodCall pmc) {
            return createMethodCall(pmc, solution);
        }
        else if(expression instanceof PartiallyResolvedCreator pn) {
            return createCreator(pn, solution);
        }
        else {
            return expression;
        }
    }

    private MethodCall createMethodCall(PartiallyResolvedMethodCall pmc, Map<XType, IRType> solution) {
        var method = pmc.method();
        var typeMap = getTypeVariableMap(pmc);
        List<IRType> typeArguments = NncUtils.map(
                method.typeParameters(),
                tp -> solution.get(typeMap.getXType(pmc, tp))
        );
        var resolvedArgs = NncUtils.map(pmc.arguments(), arg -> createResolved(arg, solution));
        return pmc.toMethodCall(typeArguments, resolvedArgs);
    }

    public PartiallyResolvedMethodCall tryResolveMethodCall(UnresolvedMethodCall umc, IRMethod method) {
        var arguments = NncUtils.map(umc.arguments(), arg -> resolve(arg, null));
        var instance = NncUtils.get(umc.instance(), i -> resolve(i , ObjectClass.getInstance()));
        var typeMap = getTypeVariableMap(umc, method.typeParameters(), arguments, instance);
        var substitutedParamTypes = NncUtils.map(
                method.parameterTypes(),
                t -> t.substituteReferenceRecursively(typeMap.getMap(umc))
        );

        Graph graph = new GraphImpl();

        List<IRType> argumentTypes = new ArrayList<>();
        for (IRExpression argument : arguments) {
            argumentTypes.add(argument.type().substituteReferenceRecursively(typeMap.getMap(argument)));
        }

        List<PartiallyResolvedMethodCall> methodCallArgs = NncUtils.filterByType(arguments, PartiallyResolvedMethodCall.class);
        for (PartiallyResolvedMethodCall methodCallArg : methodCallArgs) {
            graph.merge(methodCallArg.graph());
        }

        addBuiltinBounds(typeMap.getMap(umc), graph);
        graph.batchAddExtensions(argumentTypes, substitutedParamTypes);
        if(!graph.isSolvable()) {
            return null;
        }

        return new PartiallyResolvedMethodCall(
                instance,
                method,
                arguments,
                graph);
    }

    private XTypeMap getTypeVariableMap(PartiallyResolvedMethodCall pmc) {
        return getTypeVariableMap(pmc, pmc.method().typeParameters(), pmc.arguments(), pmc.instance());
    }

    private XTypeMap getTypeVariableMap(PartiallyResolvedCreator pn) {
        return getTypeVariableMap(pn, pn.constructor().typeParameters(), pn.arguments(), null);
    }

    private XTypeMap getTypeVariableMap(
            IRExpression expression,
            List<? extends TypeVariable<?>> typeParameters,
            List<IRExpression> arguments,
            @Nullable IRExpression instance) {
        var typeVarMap = createTypeVariableMap(expression, typeParameters);
        if(NncUtils.get(instance, IRExpression::type) instanceof PType pInstanceType) {
            typeVarMap.putAll(expression, pInstanceType.getTypeArgumentMapRecursively());
        }
        for (IRExpression argument : arguments) {
            if(argument instanceof PartiallyResolvedMethodCall pmcArg) {
                typeVarMap.merge(getTypeVariableMap(pmcArg, pmcArg.method().typeParameters(), pmcArg.arguments(), pmcArg.instance()));
            }
            else if(argument instanceof PartiallyResolvedCreator pn) {
                typeVarMap.merge(getTypeVariableMap(pn, pn.constructor().typeParameters(), pn.arguments(), null));
            }
        }
        return typeVarMap;
    }

    private void addBuiltinBounds(Map<? extends IRType, ? extends IRType> xTypeMap, Graph graph) {
        xTypeMap.forEach((p, x) -> {
            graph.addExtension(p.getLowerBound().substituteReferenceRecursively(xTypeMap), x);
            graph.addExtension(x, p.getUpperBound().substituteReferenceRecursively(xTypeMap));
        });
    }

    private XTypeMap createTypeVariableMap(IRExpression expression, List<? extends TypeVariable<?>> typeVariables) {
        XTypeMap xTypeMap = new XTypeMap();
        xTypeMap.createAllXTypes(expression, typeVariables);
        return xTypeMap;
    }

}
