package tech.metavm.object.view;

import junit.framework.TestCase;
import org.junit.Assert;
import tech.metavm.entity.MockStandardTypesInitializer;
import tech.metavm.entity.StandardTypes;
import tech.metavm.entity.natives.mocks.MockNativeFunctionsInitializer;
import tech.metavm.flow.MethodBuilder;
import tech.metavm.flow.Nodes;
import tech.metavm.flow.Parameter;
import tech.metavm.flow.Values;
import tech.metavm.object.instance.core.*;
import tech.metavm.object.instance.core.mocks.MockInstanceRepository;
import tech.metavm.object.type.*;
import tech.metavm.object.type.mocks.MockArrayTypeProvider;
import tech.metavm.object.type.mocks.MockFunctionTypeProvider;
import tech.metavm.object.type.mocks.MockTypeRepository;
import tech.metavm.object.type.mocks.TypeProviders;
import tech.metavm.object.view.mocks.MockMappingRepository;
import tech.metavm.util.Instances;
import tech.metavm.util.TestUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static tech.metavm.entity.StandardTypes.getStringType;

public class MappingSaverTest extends TestCase {

    private InstanceRepository instanceRepository;
    private TypeProviders typeProviders;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        instanceRepository = new MockInstanceRepository();
        typeProviders = new TypeProviders();
        MockStandardTypesInitializer.init();
        MockNativeFunctionsInitializer.init(typeProviders.functionTypeProvider);
    }

    public void testSaveBuiltin() {
        var fooType = ClassTypeBuilder.newBuilder("Foo", "Foo")
                .build();
        var barType = ClassTypeBuilder.newBuilder("Bar", "Bar")
                .build();

        var barChildArrayType = typeProviders.arrayTypeProvider.getArrayType(barType, ArrayKind.CHILD);
        var barReadWriteArrayType = typeProviders.arrayTypeProvider.getArrayType(barType, ArrayKind.READ_WRITE);

        var fooBarsField = FieldBuilder.newBuilder("bars", "bars", fooType, barChildArrayType)
                .access(Access.PRIVATE)
                .isChild(true)
                .build();
        var fooNameField = FieldBuilder.newBuilder("name", "name", fooType, getStringType())
                .asTitle()
                .build();
        var barCodeField = FieldBuilder.newBuilder("code", "code", barType, getStringType())
                .asTitle()
                .build();

        // generate getBars method
        var getBarsMethod = MethodBuilder.newBuilder(fooType, "获取bars", "getBars", typeProviders.functionTypeProvider)
                .returnType(barReadWriteArrayType)
                .build();
        {
            var scope = getBarsMethod.getRootScope();
            var selfNode = Nodes.self("Self", null, fooType, scope);
            var barsNode = Nodes.newArray("NewArray", null, barReadWriteArrayType,
                    Values.nodeProperty(selfNode, fooBarsField), null, scope);
            Nodes.ret("Return", null, scope, Values.node(barsNode));
        }

        // generate setBars method
        var setBarsMethod = MethodBuilder.newBuilder(fooType, "设置bars", "setBars", typeProviders.functionTypeProvider)
                .parameters(new Parameter(null, "bars", "bars", barReadWriteArrayType))
                .build();
        {
            var scope = setBarsMethod.getRootScope();
            var selfNode = Nodes.self("Self", null, fooType, scope);
            var inputNode = Nodes.input(setBarsMethod);
            Nodes.clearArray("ClearBars", null, Values.nodeProperty(selfNode, fooBarsField), scope);
            Nodes.forEach(() -> Values.inputValue(inputNode, 0),
                    (bodyScope, element, index) -> {
                        Nodes.addElement("AddBar", null, Values.nodeProperty(selfNode, fooBarsField),
                                element.get(), bodyScope);
                    },
                    scope);
            Nodes.ret("Return", null, scope, null);
        }
        TestUtils.initEntityIds(fooType);

        var typeRepository = new MockTypeRepository();
        typeRepository.save(fooType);
        typeRepository.save(barType);
        var arrayMappingRepo = new MockArrayMappingRepository();
        var mappingProvider = new MockMappingRepository();
        MappingSaver saver = new MappingSaver(
                instanceRepository,
                typeRepository,
                new MockFunctionTypeProvider(),
                new MockArrayTypeProvider(),
                mappingProvider,
                arrayMappingRepo
        );

        var barMapping = saver.saveBuiltinMapping(barType, true);
        var fooMapping = saver.saveBuiltinMapping(fooType, true);
        var fooViewType = fooMapping.getTargetType();
        var barsFieldMapping = fooMapping.getFieldMappingByTargetField(fooViewType.getFieldByCode("bars"));
        var barArrayMapping = Objects.requireNonNull(barsFieldMapping.getNestedMapping());
//        fooMapping.initId(3001L);
//        barArrayMapping.initId(3002L);
//        barMapping.initId(3003L);

        var barInst = ClassInstanceBuilder.newBuilder(barType)
                .data(Map.of(
                        barCodeField,
                        new StringInstance("bar001", StandardTypes.getStringType())
                ))
                .build();

        var barChildArray = new ArrayInstance(barChildArrayType, List.of(barInst));

        var foo = ClassInstanceBuilder.newBuilder(fooType)
                .data(
                        Map.of(
                                fooNameField,
                                new StringInstance("foo", StandardTypes.getStringType()),
                                fooBarsField,
                                barChildArray
                        )
                )
                .build();

        TestUtils.initInstanceIds(foo);

        System.out.println(barArrayMapping.getMapper().getText());
        System.out.println(barArrayMapping.getUnmapper().getText());

        TestUtils.initEntityIds(fooType);

        // Mapping
        var fooView = (ClassInstance) fooMapping.mapRoot(foo, instanceRepository, typeProviders.parameterizedFlowProvider);

        Assert.assertSame(fooView.getType(), fooViewType);
        Assert.assertSame(foo, fooView.tryGetSource());
        var fooViewName = fooView.getField("name");
        Assert.assertEquals(foo.getField("name"), fooViewName);
        Assert.assertTrue(fooView.getId() instanceof ViewId);

        var fooViewBars = (ArrayInstance) fooView.getField("bars");
        Assert.assertTrue(fooViewBars.isChildArray());
        var barView = (ClassInstance) fooViewBars.get(0);
        var barViewType = barMapping.getTargetType();
        Assert.assertSame(barView.getType(), barViewType);
        Assert.assertEquals(barInst.getField("code"), barView.getField("code"));
        Assert.assertTrue(barView.getId() instanceof ChildViewId);

        // mapping
        System.out.println(fooMapping.getMapper().getText());
        System.out.println(fooMapping.getReadMethod().getText());

        // Unmapping
        System.out.println(fooMapping.getUnmapper().getText());
        System.out.println(fooMapping.getWriteMethod().getText());

        fooView.setField("name", Instances.stringInstance("foo2"));
        barView.setField("code", Instances.stringInstance("bar002"));

//        fooViewBars.addElement(
//                ClassInstanceBuilder.newBuilder(barMapping.getTargetType())
//                        .data(Map.of(
//                                barViewType.getFieldByCode("code"),
//                                Instances.stringInstance("bar002")
//                        ))
//                        .build()
//        );

        var unmappedFoo =
                fooMapping.unmap(fooView, instanceRepository, typeProviders.parameterizedFlowProvider);
        Assert.assertSame(foo, unmappedFoo);
        Assert.assertEquals(Instances.stringInstance("foo2"), foo.getField("name"));
        var bars = (ArrayInstance) foo.getField("bars");
        Assert.assertEquals(1, bars.size());
        var bar = (ClassInstance) bars.get(0);
        Assert.assertEquals(Instances.stringInstance("bar002"), bar.getField("code"));
    }

    public void testPathId() {
        var productType = ClassTypeBuilder.newBuilder("Product", "Product").build();
        var skuType = ClassTypeBuilder.newBuilder("Sku", "Sku").build();
        var skuChildArrayType = new ArrayType(null, skuType, ArrayKind.CHILD);
        var skuListField = FieldBuilder.newBuilder("skuList", "skuList", productType, skuChildArrayType)
                .isChild(true)
                .access(Access.PRIVATE)
                .build();
        var skuRwArrayType = new ArrayType(null, skuType, ArrayKind.READ_WRITE);
        var getSkuListMethod = MethodBuilder.newBuilder(productType, "getSkuList", "getSkuList", typeProviders.functionTypeProvider)
                .returnType(skuRwArrayType)
                .build();
        {
            var scope = getSkuListMethod.getRootScope();
            var self = Nodes.self("self", "self", productType, scope);
            Nodes.input(getSkuListMethod);
            var skuList = Nodes.newArray(
                    "skuList", "skuList", skuRwArrayType,
                    Values.nodeProperty(self, skuListField),
                    null, scope
            );
            Nodes.ret("Return", "Return", scope, Values.node(skuList));
        }

        var setSkuListMethod = MethodBuilder.newBuilder(productType, "setSkuList", "setSkuList", typeProviders.functionTypeProvider)
                .parameters(new Parameter(null, "skuList", "skuList", skuRwArrayType))
                .build();
        {
            var scope = setSkuListMethod.getRootScope();
            var self = Nodes.self("self", "self", productType, scope);
            var input = Nodes.input(setSkuListMethod);
            Nodes.clearArray("clearArray", null, Values.nodeProperty(self, skuListField), scope);
            Nodes.forEach(
                    () -> Values.inputValue(input, 0),
                    (bodyScope, getElement, getIndex) -> {
                        Nodes.addElement("addElement", null,
                                Values.nodeProperty(self, skuListField), getElement.get(), bodyScope);
                    },
                    scope
            );
        }
        var typeRepository = new MockTypeRepository();
        typeRepository.save(productType);
        typeRepository.save(skuType);
        var arrayMappingRepo = new MockArrayMappingRepository();
        var mappingProvider = new MockMappingRepository();
        MappingSaver saver = new MappingSaver(
                instanceRepository,
                typeRepository,
                new MockFunctionTypeProvider(),
                new MockArrayTypeProvider(),
                mappingProvider,
                arrayMappingRepo
        );
        saver.saveBuiltinMapping(skuType, true);
        var productMapping = saver.saveBuiltinMapping(productType, true);
        TestUtils.initEntityIds(productType);
        var sku = ClassInstanceBuilder.newBuilder(skuType)
                .data(Map.of())
                .build();
        var product = ClassInstanceBuilder.newBuilder(productType)
                .data(Map.of(
                        skuListField,
                        new ArrayInstance(skuChildArrayType, List.of(sku))
                ))
                .build();
        TestUtils.initInstanceIds(product);
        var viewSkuListField = productMapping.getTargetType().getFieldByCode("skuList");
        var productView = (ClassInstance) productMapping.mapRoot(product, instanceRepository, typeProviders.parameterizedFlowProvider);
        var skuListView = (ArrayInstance) productView.getField(viewSkuListField);
        Assert.assertTrue(productView.getId() instanceof ViewId);
        Assert.assertTrue(skuListView.getId() instanceof FieldViewId);
        Assert.assertTrue(skuListView.get(0).getId() instanceof ChildViewId);

        skuListView.removeElement(0);

        productMapping.unmap(productView, instanceRepository, typeProviders.parameterizedFlowProvider);
        var skuList = (ArrayInstance) product.getField(skuListField);
        Assert.assertTrue(skuList.isEmpty());

    }

}