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

    public static final Logger logger = LoggerFactory.getLogger(BasicCompileTest.class);

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
            logger.info("Utils error: {}", error.message());
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
        Assert.assertEquals("true", result.title());
    }

    private void processValueTypes() {
        var currencyKlass = getClassTypeByCode("valuetypes.Currency");
        Assert.assertEquals(ClassKind.VALUE.code(), currencyKlass.kind());
        var productKlass = getClassTypeByCode("valuetypes.Product");
        var pricesKlass = getClassTypeByCode("valuetypes.Price");
        var channelPriceKlass = getClassTypeByCode("valuetypes.ChannelPrice");
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
                                                TypeExpressions.getClassType(pricesKlass),
                                                List.of(
                                                        InstanceFieldDTO.create(
                                                                TestUtils.getFieldIdByCode(pricesKlass, "defaultPrice"),
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
                                                        ),
                                                        InstanceFieldDTO.create(
                                                                TestUtils.getFieldIdByCode(pricesKlass, "channelPrices"),
                                                                new ListFieldValue(
                                                                        null, false,
                                                                        List.of(
                                                                                InstanceFieldValue.of(
                                                                                        InstanceDTO.createClassInstance(
                                                                                                TypeExpressions.getClassType(channelPriceKlass),
                                                                                                List.of(
                                                                                                        InstanceFieldDTO.create(
                                                                                                                TestUtils.getFieldIdByCode(channelPriceKlass, "channel"),
                                                                                                                PrimitiveFieldValue.createString("mobile")
                                                                                                        ),
                                                                                                        InstanceFieldDTO.create(
                                                                                                                TestUtils.getFieldIdByCode(channelPriceKlass, "price"),
                                                                                                                InstanceFieldValue.of(
                                                                                                                        InstanceDTO.createClassInstance(
                                                                                                                                TypeExpressions.getClassType(currencyKlass),
                                                                                                                                List.of(
                                                                                                                                        InstanceFieldDTO.create(
                                                                                                                                                TestUtils.getFieldIdByCode(currencyKlass, "quantity"),
                                                                                                                                                PrimitiveFieldValue.createDouble(80.0)
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
                                                                                        )
                                                                                ),
                                                                                InstanceFieldValue.of(
                                                                                        InstanceDTO.createClassInstance(
                                                                                                TypeExpressions.getClassType(channelPriceKlass),
                                                                                                List.of(
                                                                                                        InstanceFieldDTO.create(
                                                                                                                TestUtils.getFieldIdByCode(channelPriceKlass, "channel"),
                                                                                                                PrimitiveFieldValue.createString("web")
                                                                                                        ),
                                                                                                        InstanceFieldDTO.create(
                                                                                                                TestUtils.getFieldIdByCode(channelPriceKlass, "price"),
                                                                                                                InstanceFieldValue.of(
                                                                                                                        InstanceDTO.createClassInstance(
                                                                                                                                TypeExpressions.getClassType(currencyKlass),
                                                                                                                                List.of(
                                                                                                                                        InstanceFieldDTO.create(
                                                                                                                                                TestUtils.getFieldIdByCode(currencyKlass, "quantity"),
                                                                                                                                                PrimitiveFieldValue.createDouble(95.0)
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
                                                                                        )
                                                                                )
                                                                        )
                                                                )
                                                        )
                                                )
                                        )
                                )
                        )
                )
        )));
        var product = instanceManager.get(productId, 2).instance();
        var price = product.getInstance("price");
        Assert.assertNull(price.id());
        // check default price
        var defaultPrice = price.getInstance("defaultPrice");
        Assert.assertNull(defaultPrice.id());
        Assert.assertEquals(100.0, defaultPrice.getPrimitiveValue("quantity"));
        Assert.assertEquals(currencyKindYuan.id(), defaultPrice.getReferenceId("kind"));
        // check channels
        var channelPrices = price.getInstance("channelPrices");
        Assert.assertNull(channelPrices.id());
        Assert.assertEquals(2, channelPrices.getListSize());
        // check mobile channel
        var mobileChannelPrice = channelPrices.getElementInstance(0);
        Assert.assertNull(mobileChannelPrice.id());
        Assert.assertEquals("mobile", mobileChannelPrice.getPrimitiveValue("channel"));
        var mobilePrice = mobileChannelPrice.getInstance("price");
        Assert.assertEquals(80.0, mobilePrice.getPrimitiveValue("quantity"));
        Assert.assertEquals(currencyKindYuan.id(), mobilePrice.getReferenceId("kind"));
        // check web channel
        var webChannelPrice = channelPrices.getElementInstance(1);
        Assert.assertNull(webChannelPrice.id());
        Assert.assertEquals("web", webChannelPrice.getPrimitiveValue("channel"));
        var webPrice = webChannelPrice.getInstance("price");
        Assert.assertEquals(95.0, webPrice.getPrimitiveValue("quantity"));
        Assert.assertEquals(currencyKindYuan.id(), webPrice.getReferenceId("kind"));
    }

}
