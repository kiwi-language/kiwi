package tech.metavm.object.type.generic;

import junit.framework.TestCase;
import tech.metavm.entity.*;
import tech.metavm.expression.NodeExpression;
import tech.metavm.expression.PropertyExpression;
import tech.metavm.flow.*;
import tech.metavm.flow.Value;
import tech.metavm.object.type.*;
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
        ClassType foo = ClassBuilder.newBuilder("Foo", "Foo")
                .typeParameters(typeVar)
                .build();

        var valueField = FieldBuilder.newBuilder("value", "value", foo, typeVar)
                .nullType(nullType)
                .build();
        {
            var getValueFlow = FlowBuilder.newBuilder(foo, "getValue", "getValue", null)
                    .type(new FunctionType(null, List.of(), typeVar))
                    .staticType(new FunctionType(null, List.of(foo), typeVar))
                    .nullType(nullType)
                    .returnType(typeVar)
                    .build();
            var selfNode = new SelfNode(null, "self", foo, null, getValueFlow.getRootScope());
            var valueNode = new ValueNode(
                    null, "value", typeVar, selfNode, getValueFlow.getRootScope(),
                    Value.expression(
                            new PropertyExpression(new NodeExpression(selfNode), valueField)
                    )
            );
            var returnNode = new ReturnNode(
                    null, "return", selfNode, getValueFlow.getRootScope()
            );
            returnNode.setValue(Value.expression(new NodeExpression(valueNode)));
        }

        {
            var flow = FlowBuilder.newBuilder(foo, "setValue", "setValue", null)
                    .type(new FunctionType(null, List.of(typeVar), voidType))
                    .staticType(new FunctionType(null, List.of(foo, typeVar), voidType))
                    .returnType(voidType)
                    .parameters(new Parameter(null, "value", "value", typeVar))
                    .nullType(nullType)
                    .build();
            var selfNode = new SelfNode(null, "self", foo, null, flow.getRootScope());
            var inputType = ClassBuilder.newBuilder("setValueInput", "setValueInput")
                    .ephemeral(true)
                    .anonymous(true)
                    .build();
            var inputValueField = FieldBuilder.newBuilder("value", "value", inputType, typeVar)
                    .nullType(nullType)
                    .build();
            var inputNode = new InputNode(null, "input", inputType, selfNode, flow.getRootScope());
            var updateNode = new UpdateObjectNode(null, "update", inputNode, flow.getRootScope());
            updateNode.setObjectId(Value.reference(new NodeExpression(selfNode)));
            updateNode.setUpdateField(
                    valueField, UpdateOp.SET,
                    Value.reference(new PropertyExpression(new NodeExpression(inputNode), inputValueField))
            );
            new ReturnNode(null, "return", updateNode, flow.getRootScope());
        }

        IEntityContext entityContext = new EntityContext(new MemInstanceContext(), null) {
            @Override
            public boolean isBindSupported() {
                return false;
            }
        };

        var stringType = new PrimitiveType(PrimitiveKind.STRING);

        var typeFactory = new DefaultTypeFactory(t -> {
            if (t == Null.class)
                return nullType;
            if (t == String.class)
                return stringType;
            throw new InternalException("Type not found: " + t.getTypeName());
        });

        stringType.initId(1);
        var subst = new SubstitutorV2(foo, entityContext.getGenericContext(),
                List.of(typeVar), List.of(stringType), ResolutionStage.DECLARATION,
                SaveTypeBatch.empty(entityContext), typeFactory);

        var pType = (ClassType) foo.accept(subst);
        try (var serContext = SerializeContext.enter()) {
            serContext.setIncludingCode(true);
            serContext.writeType(pType);
            TestUtils.writeJson(JSON_FILE_PATH, serContext.getTypes());
        }
    }

}