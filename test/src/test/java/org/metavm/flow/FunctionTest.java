package org.metavm.flow;

import junit.framework.TestCase;
import org.junit.Assert;
import org.metavm.entity.DummyGenericDeclaration;
import org.metavm.entity.MockStandardTypesInitializer;
import org.metavm.object.type.TypeVariable;
import org.metavm.object.type.Types;
import org.metavm.util.TestUtils;

import java.util.List;

public class FunctionTest extends TestCase {

    @Override
    protected void setUp() throws Exception {
        TestUtils.ensureStringKlassInitialized();
        MockStandardTypesInitializer.init();
    }

    public void testGeneric() {
        var function = FunctionBuilder.newBuilder(TestUtils.nextRootId(), "test").build();
        var typeVar = new TypeVariable(function.nextChildId(), "T", function);
        function.setTypeParameters(List.of(typeVar));
        function.setParameters(List.of(new Parameter(function.nextChildId(), "p1", typeVar.getType(), function)));
        Assert.assertFalse(function.getTypeParameters().isEmpty());
        var parameterizedFunc = new FunctionRef(function, List.of(Types.getStringType()));
        Assert.assertSame(function, parameterizedFunc.getRawFlow());
        Assert.assertEquals(List.of(Types.getStringType()), parameterizedFunc.getTypeArguments());
        Assert.assertEquals(Types.getStringType(), parameterizedFunc.getParameterTypes().getFirst());
    }

}