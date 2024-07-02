package org.metavm.flow;

import junit.framework.TestCase;
import org.junit.Assert;
import org.metavm.entity.DummyGenericDeclaration;
import org.metavm.entity.MockStandardTypesInitializer;
import org.metavm.object.type.ResolutionStage;
import org.metavm.object.type.TypeVariable;
import org.metavm.object.type.Types;
import org.metavm.object.type.generic.SubstitutorV2;

import java.util.List;

public class FunctionTest extends TestCase {

    @Override
    protected void setUp() throws Exception {
        MockStandardTypesInitializer.init();
    }

    public void testGeneric() {
        var typeVar = new TypeVariable(null, "T", null, DummyGenericDeclaration.INSTANCE);
        var function = FunctionBuilder.newBuilder("test", "test")
                .typeParameters(List.of(typeVar))
                .parameters(new Parameter(null, "p1", "p1", typeVar.getType()))
                .build();
        Assert.assertFalse(function.getTypeParameters().isEmpty());
        var subst = new SubstitutorV2(
                function,
                function.getTypeParameters(),
                List.of(Types.getStringType()),
                ResolutionStage.INIT
        );
        var parameterizedFunc = (Function) subst.copy(function);
        Assert.assertSame(function, parameterizedFunc.getHorizontalTemplate());
        Assert.assertEquals(List.of(Types.getStringType()), parameterizedFunc.getTypeArguments());
        Assert.assertEquals(Types.getStringType(), parameterizedFunc.getParameter(0).getType());
    }

}