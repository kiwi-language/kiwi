package tech.metavm.object.instance.core;

import junit.framework.TestCase;
import org.junit.Assert;
import tech.metavm.entity.MockStandardTypesInitializer;
import tech.metavm.entity.StandardTypes;
import tech.metavm.flow.ParameterizedFlowProvider;
import tech.metavm.object.instance.IndexKeyRT;
import tech.metavm.object.type.ClassTypeBuilder;
import tech.metavm.object.type.FieldBuilder;
import tech.metavm.object.type.Index;
import tech.metavm.object.type.mocks.TypeProviders;
import tech.metavm.util.Instances;

import java.util.List;
import java.util.Map;

public class InstanceMemoryIndexTest extends TestCase {

    private ParameterizedFlowProvider parameterizedFlowProvider;

    @Override
    protected void setUp() throws Exception {
        var compositeTypeFacade = new TypeProviders();
        parameterizedFlowProvider = compositeTypeFacade.parameterizedFlowProvider;
        MockStandardTypesInitializer.init();
    }

    public void test() {
        var memIndex = new InstanceMemoryIndex(parameterizedFlowProvider);

        var fooType = ClassTypeBuilder.newBuilder("Foo", "Foo")
                        .build();
        var nameField = FieldBuilder.newBuilder("name", "name", fooType, StandardTypes.getStringType())
                        .build();

        var index = new Index(
                fooType, "idxName", "idxName", "name must be unique", true,
                List.of(nameField)
        );

        var name = Instances.stringInstance("foo001");

        var foo = ClassInstanceBuilder.newBuilder(fooType)
                .data(Map.of(nameField, name))
                .build();
        memIndex.save(foo);
        var result = memIndex.selectUnique(new IndexKeyRT(index, Map.of(index.getFieldByTypeField(nameField), name)));
        Assert.assertSame(foo, result);
    }

}