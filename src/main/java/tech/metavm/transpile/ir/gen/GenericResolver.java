//package tech.metavm.transpile.ir.gen;
//
//import tech.metavm.transpile.ir.IRConstructor;
//import tech.metavm.transpile.ir.IRMethod;
//import tech.metavm.transpile.ir.IRType;
//import tech.metavm.transpile.ir.PType;
//import tech.metavm.util.NncUtils;
//
//import javax.annotation.Nullable;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.function.Function;
//
//public class GenericResolver {
//
//    public boolean resolveMethodCall(
//            @Nullable IRType instanceType,
//            IRMethod method,
//            List<IRType> arguments,
//            IRType returnValue) {
//
//        Map<IRType, IRType> xTypeMap = new HashMap<>(
//                NncUtils.toMap(method.typeParameters(), Function.identity(), XTypeImpl::new)
//        );
//        if(instanceType instanceof PType pInstanceType) {
//            xTypeMap.putAll(pInstanceType.getTypeArgumentMapRecursively());
//        }
//        var substitutedParamTypes = NncUtils.map(
//                method.parameterTypes(),
//                t -> t.substituteReferenceRecursively(xTypeMap)
//        );
//        var substitutedReturnType = method.returnType().substituteReferenceRecursively(xTypeMap);
//
//        TypeGraph graph = new TypeGraphImpl();
//
//        xTypeMap.forEach((p, x) -> {
//            graph.addSuper(x, p.getLowerBound());
//            graph.addExtension(x, p.getUpperBound());
//        });
//
//        for (int i = 0; i < arguments.size(); i++) {
//            graph.addSuper(substitutedParamTypes.get(i), arguments.get(i));
//        }
//        graph.addExtension(substitutedReturnType, returnValue);
//        return graph.isSolvable();
//    }
//
//    public void resolveConstructorCall(IRConstructor constructor, List<IRType> arguments, IRType instance) {
//
//    }
//
//}
