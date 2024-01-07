package tech.metavm.object.view;

import junit.framework.TestCase;
import org.junit.Assert;
import tech.metavm.entity.MockStandardTypesInitializer;
import tech.metavm.entity.StandardTypes;
import tech.metavm.entity.mocks.MockEntityRepository;
import tech.metavm.entity.natives.mocks.MockNativeFunctionsInitializer;
import tech.metavm.flow.MethodBuilder;
import tech.metavm.flow.Nodes;
import tech.metavm.flow.Parameter;
import tech.metavm.flow.Values;
import tech.metavm.object.instance.core.*;
import tech.metavm.object.instance.core.mocks.MockInstanceRepository;
import tech.metavm.object.type.*;
import tech.metavm.object.type.mocks.TypeProviders;
import tech.metavm.object.type.mocks.MockArrayTypeProvider;
import tech.metavm.object.type.mocks.MockFunctionTypeProvider;
import tech.metavm.object.type.mocks.MockTypeRepository;
import tech.metavm.object.view.mocks.MockMappingRepository;
import tech.metavm.util.Instances;

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
        var fooType = ClassBuilder.newBuilder("Foo", "Foo")
                .tmpId(1L)
                .build();
        var barType = ClassBuilder.newBuilder("Bar", "Bar")
                .tmpId(2L)
                .build();

        var barChildArrayType = typeProviders.arrayTypeProvider.getArrayType(barType, ArrayKind.CHILD);
        var barReadWriteArrayType = typeProviders.arrayTypeProvider.getArrayType(barType, ArrayKind.READ_WRITE);

        var fooBarsField = FieldBuilder.newBuilder("bars", "bars", fooType, barChildArrayType)
                .tmpId(11L)
                .access(Access.PRIVATE)
                .isChild(true)
                .build();
        var fooNameField = FieldBuilder.newBuilder("name", "name", fooType, getStringType())
                .tmpId(12L)
                .asTitle()
                .build();
        var barCodeField = FieldBuilder.newBuilder("code", "code", barType, getStringType())
                .tmpId(13L)
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
        var typeRepository = new MockTypeRepository();
        typeRepository.save(fooType);
        typeRepository.save(barType);
        var arrayMappingRepo = new MockArrayMappingRepository();
        var mappingProvider = new MockMappingRepository();
        var entityRepository = new MockEntityRepository();
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
        fooMapping.initId(3001L);
        barArrayMapping.initId(3002L);
        barMapping.initId(3003L);

        var barInst = ClassInstanceBuilder.newBuilder(barType)
                .data(Map.of(
                        barCodeField,
                        new StringInstance("bar001", StandardTypes.getStringType())
                ))
                .id(TmpId.of(1003L))
                .build();

        var barChildArray = new ArrayInstance(barChildArrayType, List.of(barInst));
        barChildArray.initId(TmpId.of(1002L));

        var foo = ClassInstanceBuilder.newBuilder(fooType)
                .data(
                        Map.of(
                                fooNameField,
                                new StringInstance("foo", StandardTypes.getStringType()),
                                fooBarsField,
                                barChildArray
                        )
                )
                .id(TmpId.of(1001L))
                .build();


        System.out.println(barArrayMapping.getMapper().getText());
        System.out.println(barArrayMapping.getUnmapper().getText());

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

}