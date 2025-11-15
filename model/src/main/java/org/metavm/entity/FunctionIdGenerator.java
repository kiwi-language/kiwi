package org.metavm.entity;

import org.metavm.flow.Function;
import org.metavm.flow.Parameter;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.PhysicalId;
import org.metavm.object.type.TypeDefProvider;
import org.metavm.object.type.TypeParserImpl;
import org.metavm.object.type.TypeVariable;

import java.util.function.BiConsumer;

public class FunctionIdGenerator {

    public static void initFunctionId(String expression,
                                      long treeId,
                                      BiConsumer<ModelIdentity, Id> addId,
                                      BiConsumer<ModelIdentity, Long> addNextNodeId
    ) {
        var parser = new TypeParserImpl((TypeDefProvider) id -> null);
        var func = parser.parseFunctionSignature(expression);
        long nextNodeId = 0;
        var funcName = func.name();
        addId.accept(
                ModelIdentity.create(Function.class, funcName),
                PhysicalId.of(treeId, nextNodeId++)
        );
        var typeParamList = func.typeParameterNames();
        if (typeParamList != null) {
            for (var typeParam : typeParamList) {
                addId.accept(
                        ModelIdentity.create(TypeVariable.class, funcName + "." + typeParam),
                        PhysicalId.of(treeId, nextNodeId++)
                );
            }
        }
        var paramList = func.parameterNames();
        if (paramList != null) {
            for (var param : paramList) {
                addId.accept(
                        ModelIdentity.create(Parameter.class, funcName + "." + param),
                        PhysicalId.of(treeId, nextNodeId++)
                );
            }
        }
        addNextNodeId.accept(ModelIdentity.create(Function.class, funcName), nextNodeId);
    }

}
