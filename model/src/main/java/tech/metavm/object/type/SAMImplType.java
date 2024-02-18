package tech.metavm.object.type;

import tech.metavm.expression.Expressions;
import tech.metavm.flow.MethodBuilder;
import tech.metavm.flow.Nodes;
import tech.metavm.flow.Parameter;
import tech.metavm.flow.Values;
import tech.metavm.object.instance.core.FunctionInstance;
import tech.metavm.util.NncUtils;

import java.util.List;

public class SAMImplType extends ClassType {

    public SAMImplType(ClassType samInterface, FunctionInstance function) {
        super(null, samInterface.getName() + "$" + NncUtils.randomNonNegative(),
                null, null, List.of(samInterface),
                TypeCategory.CLASS,
                ClassSource.RUNTIME, null,
                true, true, null, false,
                List.of(), List.of());
        setEphemeralEntity(true);
        var sam = samInterface.getSingleAbstractMethod();
//        Field functionField = FieldBuilder.newBuilder(
//                "function",
//                null,
//                this,
//                sam.getType()
//        ).build();
        var methodStaticType = new FunctionType(
                null,
                NncUtils.prepend(this, sam.getParameterTypes()),
                sam.getReturnType()
        );
        methodStaticType.setEphemeralEntity(true);
        var method = MethodBuilder.newBuilder(this, sam.getName(), null, null)
                .parameters(NncUtils.map(sam.getParameters(), Parameter::copy))
                .returnType(sam.getReturnType())
                .type(sam.getType())
                .staticType(methodStaticType)
                .build();
        var scope = method.getRootScope();
//        var self = Nodes.self("self", null, this, scope);
        var input = Nodes.input(method);
        var func = Nodes.function(
                "function",
                scope,
                Values.constant(Expressions.constant(function)),
                function.getType().getParameterTypes().isEmpty() ?
                        List.of() :
                        NncUtils.map(input.getType().getFields(), f -> Values.nodeProperty(input, f))
        );
        if (sam.getReturnType().isVoid())
            Nodes.ret("ret", scope, null);
        else
            Nodes.ret("ret", scope, Values.node(func));
    }

}
