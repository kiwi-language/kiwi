package tech.metavm.autograph;

import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.metavm.common.ErrorDTO;
import tech.metavm.entity.StandardTypes;
import tech.metavm.flow.rest.FlowExecutionRequest;
import tech.metavm.flow.rest.MethodRefDTO;
import tech.metavm.object.instance.rest.*;
import tech.metavm.object.type.ClassKind;
import tech.metavm.object.type.TypeExpressions;
import tech.metavm.util.TestUtils;

import java.util.List;

public class BasicCompileTest extends CompilerTestBase {

    public static final Logger LOGGER = LoggerFactory.getLogger(BasicCompileTest.class);

    public static final String SOURCE_ROOT = "/Users/leen/workspace/object/lab/src/main/basics";

    public void test() {
        compile(SOURCE_ROOT);
        compile(SOURCE_ROOT);
        submit(() -> {
            processCapturedType();
            processGenericOverride();
            processValueTypes();
        });
    }

    private void processCapturedType() {
//            DebugEnv.debugLogger_ON = true;
        var utilsType = getClassTypeByCode("capturedtypes.CtUtils");
        for (ErrorDTO error : utilsType.getClassParam().errors()) {
            LOGGER.info("Utils error: {}", error.message());
        }
        Assert.assertEquals(0, utilsType.getClassParam().errors().size());

        var labType = getClassTypeByCode("capturedtypes.CtLab");
        var fooType = getClassTypeByCode("capturedtypes.CtFoo");
        var labFoosFieldId = TestUtils.getFieldIdByCode(labType, "foos");
        var fooNameFieldId = TestUtils.getFieldIdByCode(fooType, "name");

        var labId = TestUtils.doInTransaction(() ->
                instanceManager.create(InstanceDTO.createClassInstance(
                        TypeExpressions.getClassType(labType.id()),
                        List.of(
                                InstanceFieldDTO.create(
                                        labFoosFieldId,
                                        new ListFieldValue(
                                                null,
                                                true,
                                                List.of(
                                                        InstanceFieldValue.of(
                                                                InstanceDTO.createClassInstance(
                                                                        TypeExpressions.getClassType(fooType.id()),
                                                                        List.of(
                                                                                InstanceFieldDTO.create(
                                                                                        fooNameFieldId,
                                                                                        PrimitiveFieldValue.createString("foo001")
                                                                                )
                                                                        )
                                                                )
                                                        ),
                                                        InstanceFieldValue.of(
                                                                InstanceDTO.createClassInstance(
                                                                        TypeExpressions.getClassType(fooType.id()),
                                                                        List.of(
                                                                                InstanceFieldDTO.create(
                                                                                        fooNameFieldId,
                                                                                        PrimitiveFieldValue.createString("foo002")
                                                                                )
                                                                        )
                                                                )
                                                        ),
                                                        InstanceFieldValue.of(
                                                                InstanceDTO.createClassInstance(
                                                                        TypeExpressions.getClassType(fooType.id()),
                                                                        List.of(
                                                                                InstanceFieldDTO.create(
                                                                                        fooNameFieldId,
                                                                                        PrimitiveFieldValue.createString("foo003")
                                                                                )
                                                                        )
                                                                )
                                                        )
                                                )
                                        )
                                )
                        )
                ))
        );
        var lab = instanceManager.get(labId, 2).instance();
        var foos = ((InstanceFieldValue) lab.getFieldValue(labFoosFieldId)).getInstance();
        Assert.assertEquals(3, foos.getElements().size());
        var foo002 = ((InstanceFieldValue) foos.getElements().get(1)).getInstance();

        var foundFoo = TestUtils.doInTransaction(() ->
                flowExecutionService.execute(
                        new FlowExecutionRequest(
                                TestUtils.getMethodRefByCode(labType, "getFooByName"),
                                labId,
                                List.of(
                                        PrimitiveFieldValue.createString("foo002")
                                )
                        )
                )
        );
        Assert.assertEquals(foo002.id(), foundFoo.id());
    }

    private void processGenericOverride() {
        var baseType = getClassTypeByCode("genericoverride.Base");
        var subType = getClassTypeByCode("genericoverride.Sub");
        var subId = TestUtils.doInTransaction(() -> instanceManager.create(
                InstanceDTO.createClassInstance(
                        TypeExpressions.getClassType(subType.id()),
                        List.of()
                )
        ));
        var containsAnyMethodId = TestUtils.getMethodIdByCode(baseType, "containsAny");
        var stringListType = StandardTypes.getReadWriteListKlass().getParameterized(List.of(StandardTypes.getStringType()));
        var result = TestUtils.doInTransaction(() -> flowExecutionService.execute(new FlowExecutionRequest(
                new MethodRefDTO(
                        TypeExpressions.getClassType(baseType),
                        containsAnyMethodId,
                        List.of("string")
                ),
                subId,
                List.of(
                        InstanceFieldValue.of(
                                InstanceDTO.createListInstance(
                                        stringListType.getType().toExpression(),
                                        false,
                                        List.of(
                                                PrimitiveFieldValue.createString("a"),
                                                PrimitiveFieldValue.createString("b"),
                                                PrimitiveFieldValue.createString("c")
                                        )
                                )
                        ),
                        InstanceFieldValue.of(
                                InstanceDTO.createListInstance(
                                        stringListType.getType().toExpression(),
                                        false,
                                        List.of(
                                                PrimitiveFieldValue.createString("c"),
                                                PrimitiveFieldValue.createString("d")
                                        )
                                )
                        )
                )
        )));
        Assert.assertEquals("是", result.title());
    }

    private void processValueTypes() {
        var currencyKlass = getClassTypeByCode("valuetypes.Currency");
        Assert.assertEquals(ClassKind.VALUE.code(), currencyKlass.kind());
        var productKlass = getClassTypeByCode("valuetypes.Product");
        var currencyKindKlass = getClassTypeByCode("valuetypes.CurrencyKind");
        var currencyKindYuan = TestUtils.getEnumConstantByName(currencyKindKlass, "YUAN");

        var productId = TestUtils.doInTransaction(() -> instanceManager.create(InstanceDTO.createClassInstance(
                TypeExpressions.getClassType(productKlass),
                List.of(
                        InstanceFieldDTO.create(
                                TestUtils.getFieldIdByCode(productKlass, "name"),
                                PrimitiveFieldValue.createString("shoes")
                        ),
                        InstanceFieldDTO.create(
                                TestUtils.getFieldIdByCode(productKlass, "price"),
                                InstanceFieldValue.of(
                                        InstanceDTO.createClassInstance(
                                                TypeExpressions.getClassType(currencyKlass),
                                                List.of(
                                                        InstanceFieldDTO.create(
                                                               TestUtils.getFieldIdByCode(currencyKlass, "quantity"),
                                                               PrimitiveFieldValue.createDouble(100.0)
                                                        ),
                                                        InstanceFieldDTO.create(
                                                                TestUtils.getFieldIdByCode(currencyKlass, "kind"),
                                                                ReferenceFieldValue.create(currencyKindYuan)
                                                        )
                                                )
                                        )
                                )
                        )
                )
        )));
        var product = instanceManager.get(productId, 2).instance();
        var currency = product.getInstance("price");
        Assert.assertNull(currency.id());
        Assert.assertEquals(100.0, currency.getPrimitiveValue("quantity"));
        Assert.assertEquals(currencyKindYuan.id(), currency.getReferenceId("kind"));
    }

}
