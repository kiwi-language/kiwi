package org.metavm.entity;

import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.metavm.flow.Function;
import org.metavm.flow.Parameter;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.PhysicalId;
import org.metavm.object.type.TypeVariable;
import org.metavm.object.type.antlr.TypeLexer;
import org.metavm.object.type.antlr.TypeParser;

import java.util.function.BiConsumer;

public class FunctionIdGenerator {

    public static void initFunctionId(String expression,
                                      long treeId,
                                      BiConsumer<ModelIdentity, Id> addId,
                                      BiConsumer<ModelIdentity, Long> addNextNodeId
    ) {
        var input = CharStreams.fromString(expression);
        var parser = new TypeParser(new CommonTokenStream(new TypeLexer(input)));
        parser.setErrorHandler(new BailErrorStrategy());
        var func = parser.functionSignature();
        long nextNodeId = 0;
        var funcName = func.IDENTIFIER().getText();
        addId.accept(
                ModelIdentity.create(Function.class, funcName),
                PhysicalId.of(treeId, nextNodeId++)
        );
        var typeParamList = func.typeParameterList();
        if (typeParamList != null) {
            for (var typeParam : typeParamList.typeParameter()) {
                addId.accept(
                        ModelIdentity.create(TypeVariable.class, funcName + "." + typeParam.IDENTIFIER().getText()),
                        PhysicalId.of(treeId, nextNodeId++)
                );
            }
        }
        var paramList = func.parameterList();
        if (paramList != null) {
            for (var param : paramList.parameter()) {
                addId.accept(
                        ModelIdentity.create(Parameter.class, funcName + "." + param.IDENTIFIER().getText()),
                        PhysicalId.of(treeId, nextNodeId++)
                );
            }
        }
        addNextNodeId.accept(ModelIdentity.create(Function.class, funcName), nextNodeId);
    }




}
