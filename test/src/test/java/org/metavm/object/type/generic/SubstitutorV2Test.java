package org.metavm.object.type.generic;

import junit.framework.TestCase;
import org.junit.Assert;
import org.metavm.entity.DummyGenericDeclaration;
import org.metavm.entity.MockStandardTypesInitializer;
import org.metavm.entity.SerializeContext;
import org.metavm.entity.mocks.MockEntityRepository;
import org.metavm.expression.NodeExpression;
import org.metavm.expression.PropertyExpression;
import org.metavm.flow.*;
import org.metavm.object.instance.core.PhysicalId;
import org.metavm.object.type.*;
import org.metavm.object.type.mocks.TypeProviders;
import org.metavm.util.InternalException;
import org.metavm.util.Null;
import org.metavm.util.TestUtils;

import java.util.List;

public class SubstitutorV2Test extends TestCase {

    public static final String JSON_FILE_PATH = "/Users/leen/workspace/object/test.json";

    @Override
    protected void setUp() throws Exception {
        MockStandardTypesInitializer.init();
    }

    public void test() {
        var nullType = PrimitiveType.nullType;
        var voidType = PrimitiveType.voidType;

        var typeVar = new TypeVariable(null, "E", "E", DummyGenericDeclaration.INSTANCE);
        Klass foo = KlassBuilder.newBuilder("Foo", "Foo")
                .typeParameters(typeVar)
                .build();

        var valueField = FieldBuilder.newBuilder("value", "value", foo, typeVar.getType())
                .build();
        {
            var getValueFlow = MethodBuilder.newBuilder(foo, "getValue", "getValue")
                    .type(new FunctionType(List.of(), typeVar.getType()))
                    .staticType(new FunctionType(List.of(foo.getType()), typeVar.getType()))
                    .returnType(typeVar.getType())
                    .build();
            var selfNode = new SelfNode(null, "self", null, foo.getType(), null, getValueFlow.getRootScope());
            var valueNode = new ValueNode(
                    null, "value", null, typeVar.getType(), selfNode, getValueFlow.getRootScope(),
                    Values.expression(
                            new PropertyExpression(new NodeExpression(selfNode), valueField.getRef())
                    )
            );
            new ReturnNode(
                    null, "return", null, selfNode, getValueFlow.getRootScope(),
                    Values.expression(new NodeExpression(valueNode))
            );
        }

        {
            var flow = MethodBuilder.newBuilder(foo, "setValue", "setValue")
                    .type(new FunctionType(List.of(typeVar.getType()), voidType))
                    .staticType(new FunctionType(List.of(foo.getType(), typeVar.getType()), voidType))
                    .returnType(voidType)
                    .parameters(new Parameter(null, "value", "value", typeVar.getType()))
                    .build();
            var selfNode = new SelfNode(null, "self", null, foo.getType(), null, flow.getRootScope());
            var inputType = KlassBuilder.newBuilder("setValueInput", "setValueInput")
                    .ephemeral(true)
                    .anonymous(true)
                    .build();
            var inputValueField = FieldBuilder.newBuilder("value", "value", inputType, typeVar.getType())
                    .build();
            var inputNode = new InputNode(null, "input", null, inputType, selfNode, flow.getRootScope());
            var updateNode = new UpdateObjectNode(null, "update", null, inputNode, flow.getRootScope(),
                    Values.reference(new NodeExpression(selfNode)), List.of());
            updateNode.setUpdateField(
                    valueField, UpdateOp.SET,
                    Values.reference(new PropertyExpression(new NodeExpression(inputNode), inputValueField.getRef()))
            );
            new ReturnNode(null, "return", null, updateNode, flow.getRootScope(), null);
        }

        var stringType = PrimitiveType.stringType;

        var typeFactory = new DefaultTypeFactory(t -> {
            if (t == Null.class)
                return nullType;
            if (t == String.class)
                return stringType;
            throw new InternalException("Type not found: " + t.getTypeName());
        });

        stringType.initId(PhysicalId.of(1L, 0L, TestUtils.mockClassType()));

        var typeProviders = new TypeProviders();

        var entityRepo = new MockEntityRepository(typeProviders.typeRegistry);

        var subst = new SubstitutorV2(
                foo, List.of(typeVar), List.of(stringType),
                ResolutionStage.DECLARATION
        );

        var pType = (Klass) foo.accept(subst);
        try (var serContext = SerializeContext.enter()) {
            serContext.includingCode(true);
            serContext.writeTypeDef(pType);
            TestUtils.writeJson(JSON_FILE_PATH, serContext.getTypes());
        }
    }

    public void testFlow() {
        var fooType = KlassBuilder.newBuilder("Foo", "Foo").build();
        var barMethod = MethodBuilder.newBuilder(fooType, "bar", "bar")
                .typeParameters(List.of(
                        new TypeVariable(null, "T", "T", DummyGenericDeclaration.INSTANCE)
                ))
                .build();
        var bar1 = barMethod.getParameterized(List.of(Types.getStringType()));
        var bar2 = barMethod.getParameterized(List.of(Types.getStringType()));
        Assert.assertSame(bar1, bar2);
    }



}