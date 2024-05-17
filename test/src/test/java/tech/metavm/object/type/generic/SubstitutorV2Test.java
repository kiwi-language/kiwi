package tech.metavm.object.type.generic;

import junit.framework.TestCase;
import org.junit.Assert;
import tech.metavm.entity.DummyGenericDeclaration;
import tech.metavm.entity.MockStandardTypesInitializer;
import tech.metavm.entity.SerializeContext;
import tech.metavm.entity.StandardTypes;
import tech.metavm.entity.mocks.MockEntityRepository;
import tech.metavm.expression.NodeExpression;
import tech.metavm.expression.PropertyExpression;
import tech.metavm.flow.*;
import tech.metavm.object.instance.core.DefaultPhysicalId;
import tech.metavm.object.type.*;
import tech.metavm.object.type.mocks.TypeProviders;
import tech.metavm.util.InternalException;
import tech.metavm.util.Null;
import tech.metavm.util.TestUtils;

import java.util.List;

public class SubstitutorV2Test extends TestCase {

    public static final String JSON_FILE_PATH = "/Users/leen/workspace/object/test.json";

    @Override
    protected void setUp() throws Exception {
        MockStandardTypesInitializer.init();
    }

    public void test() {
        var nullType = new PrimitiveType(PrimitiveKind.NULL);
        var voidType = new PrimitiveType(PrimitiveKind.VOID);

        var typeVar = new TypeVariable(null, "E", "E", DummyGenericDeclaration.INSTANCE);
        Klass foo = ClassTypeBuilder.newBuilder("Foo", "Foo")
                .typeParameters(typeVar)
                .build();

        var valueField = FieldBuilder.newBuilder("value", "value", foo, typeVar.getType())
                .nullType(nullType)
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
            var inputType = ClassTypeBuilder.newBuilder("setValueInput", "setValueInput")
                    .ephemeral(true)
                    .anonymous(true)
                    .build();
            var inputValueField = FieldBuilder.newBuilder("value", "value", inputType, typeVar.getType())
                    .nullType(nullType)
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

        var stringType = new PrimitiveType(PrimitiveKind.STRING);

        var typeFactory = new DefaultTypeFactory(t -> {
            if (t == Null.class)
                return nullType;
            if (t == String.class)
                return stringType;
            throw new InternalException("Type not found: " + t.getTypeName());
        });

        stringType.initId(DefaultPhysicalId.ofObject(1L, 0L, TestUtils.mockClassType()));

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
        var fooType = ClassTypeBuilder.newBuilder("Foo", "Foo").build();
        var barMethod = MethodBuilder.newBuilder(fooType, "bar", "bar")
                .typeParameters(List.of(
                        new TypeVariable(null, "T", "T", DummyGenericDeclaration.INSTANCE)
                ))
                .build();
        var bar1 = barMethod.getParameterized(List.of(StandardTypes.getStringType()));
        var bar2 = barMethod.getParameterized(List.of(StandardTypes.getStringType()));
        Assert.assertSame(bar1, bar2);
    }



}