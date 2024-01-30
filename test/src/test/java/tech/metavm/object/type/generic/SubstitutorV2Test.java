package tech.metavm.object.type.generic;

import junit.framework.TestCase;
import tech.metavm.entity.DummyGenericDeclaration;
import tech.metavm.entity.SerializeContext;
import tech.metavm.entity.mocks.MockEntityRepository;
import tech.metavm.expression.NodeExpression;
import tech.metavm.expression.PropertyExpression;
import tech.metavm.flow.*;
import tech.metavm.object.type.*;
import tech.metavm.object.type.mocks.TypeProviders;
import tech.metavm.util.InternalException;
import tech.metavm.util.Null;
import tech.metavm.util.TestUtils;

import java.util.List;

public class SubstitutorV2Test extends TestCase {

    public static final String JSON_FILE_PATH = "/Users/leen/workspace/object/test.json";

    public void test() {
        var nullType = new PrimitiveType(PrimitiveKind.NULL);
        var voidType = new PrimitiveType(PrimitiveKind.VOID);

        var typeVar = new TypeVariable(null, "E", "E", DummyGenericDeclaration.INSTANCE);
        ClassType foo = ClassTypeBuilder.newBuilder("Foo", "Foo")
                .typeParameters(typeVar)
                .build();

        var valueField = FieldBuilder.newBuilder("value", "value", foo, typeVar)
                .nullType(nullType)
                .build();
        {
            var getValueFlow = MethodBuilder.newBuilder(foo, "getValue", "getValue", null)
                    .type(new FunctionType(null, List.of(), typeVar))
                    .staticType(new FunctionType(null, List.of(foo), typeVar))
                    .returnType(typeVar)
                    .build();
            var selfNode = new SelfNode(null, "self", null, foo, null, getValueFlow.getRootScope());
            var valueNode = new ValueNode(
                    null, "value", null, typeVar, selfNode, getValueFlow.getRootScope(),
                    Values.expression(
                            new PropertyExpression(new NodeExpression(selfNode), valueField)
                    )
            );
            new ReturnNode(
                    null, "return", null, selfNode, getValueFlow.getRootScope(),
                    Values.expression(new NodeExpression(valueNode))
            );
        }

        {
            var flow = MethodBuilder.newBuilder(foo, "setValue", "setValue", null)
                    .type(new FunctionType(null, List.of(typeVar), voidType))
                    .staticType(new FunctionType(null, List.of(foo, typeVar), voidType))
                    .returnType(voidType)
                    .parameters(new Parameter(null, "value", "value", typeVar))
                    .build();
            var selfNode = new SelfNode(null, "self", null, foo, null, flow.getRootScope());
            var inputType = ClassTypeBuilder.newBuilder("setValueInput", "setValueInput")
                    .ephemeral(true)
                    .anonymous(true)
                    .build();
            var inputValueField = FieldBuilder.newBuilder("value", "value", inputType, typeVar)
                    .nullType(nullType)
                    .build();
            var inputNode = new InputNode(null, "input", null, inputType, selfNode, flow.getRootScope());
            var updateNode = new UpdateObjectNode(null, "update", null, inputNode, flow.getRootScope(),
                    Values.reference(new NodeExpression(selfNode)));
            updateNode.setUpdateField(
                    valueField, UpdateOp.SET,
                    Values.reference(new PropertyExpression(new NodeExpression(inputNode), inputValueField))
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

        stringType.initId(1);

        var typeProviders = new TypeProviders();

        var entityRepo = new MockEntityRepository();

        var compositeTypeFacade = new TypeProviders().createFacade();

        var subst = new SubstitutorV2(
                foo, List.of(typeVar), List.of(stringType),
                ResolutionStage.DECLARATION,
                entityRepo,
                compositeTypeFacade,
                typeProviders.parameterizedTypeProvider,
                typeProviders.parameterizedFlowProvider,
                new MockDTOProvider()
        );

        var pType = (ClassType) foo.accept(subst);
        try (var serContext = SerializeContext.enter()) {
            serContext.includingCode(true);
            serContext.writeType(pType);
            TestUtils.writeJson(JSON_FILE_PATH, serContext.getTypes());
        }
    }

}