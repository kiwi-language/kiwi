package tech.metavm.flow;

import junit.framework.TestCase;
import org.junit.Assert;
import tech.metavm.entity.DummyGenericDeclaration;
import tech.metavm.entity.MockStandardTypesInitializer;
import tech.metavm.entity.StandardTypes;
import tech.metavm.object.type.ResolutionStage;
import tech.metavm.object.type.TypeVariable;
import tech.metavm.object.type.generic.SubstitutorV2;

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
                List.of(StandardTypes.getStringType()),
                ResolutionStage.INIT
        );
        var parameterizedFunc = (Function) function.accept(subst);
        Assert.assertSame(function, parameterizedFunc.getHorizontalTemplate());
        Assert.assertEquals(List.of(StandardTypes.getStringType()), parameterizedFunc.getTypeArguments());
        Assert.assertEquals(StandardTypes.getStringType(), parameterizedFunc.getParameter(0).getType());
    }

}