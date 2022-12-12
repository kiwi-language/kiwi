package tech.metavm.object.meta;

import junit.framework.TestCase;
import org.junit.Assert;
import tech.metavm.entity.*;
import tech.metavm.mocks.Baz;
import tech.metavm.mocks.Foo;
import tech.metavm.object.instance.ArrayType;
import tech.metavm.object.instance.InstanceQueryService;
import tech.metavm.object.instance.MemInstanceSearchService;
import tech.metavm.object.meta.rest.dto.ColumnDTO;
import tech.metavm.object.meta.rest.dto.TableDTO;
import tech.metavm.object.meta.rest.dto.TitleFieldDTO;
import tech.metavm.util.MockIdProvider;

import java.util.List;

public class TableManagerTest extends TestCase {

    private TableManager tableManager;

    @Override
    protected void setUp() throws Exception {
        EntityIdProvider idProvider = new MockIdProvider();
        InstanceContextFactory instanceContextFactory = new InstanceContextFactory(new MemInstanceStore()).setIdService(idProvider);
        Bootstrap bootstrap = new Bootstrap(instanceContextFactory, new StdAllocators(new MemAllocatorStore()));
        bootstrap.bootAndSave();

        EntityQueryService entityQueryService =
                new EntityQueryService(new InstanceQueryService(new MemInstanceSearchService()));
        ClassTypeManager classTypeManager = new ClassTypeManager(instanceContextFactory, entityQueryService, null);
        tableManager = new TableManager(classTypeManager, instanceContextFactory);
    }

    public void testSmoking() {
        Type fooType = ModelDefRegistry.getType(Foo.class);

        TableDTO tableDTO = tableManager.get(fooType.getId());
        Assert.assertEquals(fooType.getId(), tableDTO.id());

        Field bazListField = ModelDefRegistry.getField(Foo.class, "bazList");
        Assert.assertTrue(bazListField.getType().isNullable());
        Type bazListType = bazListField.getType().getUnderlyingType();
        Assert.assertTrue(bazListType.isArray());
        ArrayType arrayType = (ArrayType) bazListType;
        Assert.assertEquals(ModelDefRegistry.getType(Baz.class), arrayType.getElementType());
    }

    public void testGet() {
        ClassType type = ModelDefRegistry.getClassType(Type.class);
        TableDTO tableDTO = tableManager.get(type.getId());
        Assert.assertNotNull(tableDTO.id());
        Assert.assertEquals(type.getFields().size(), tableDTO.fields().size());
        for (ColumnDTO field : tableDTO.fields()) {
            Assert.assertNotNull(field.id());
            Assert.assertEquals(field.name(), type.getField(field.id()).getName());
        }
    }

    public void testSave() {
        TableDTO tableDTO = new TableDTO(
                null, "Foo", null,
                false, false,
                new TitleFieldDTO(
                        "name", TableManager.ColumnType.STRING.code(),
                        false, null
                ),
                List.of(
                        ColumnDTO.createPrimitive(
                                "name", TableManager.ColumnType.STRING.code(),
                                true, false, true
                        )
                )
        );

        TableDTO saved = tableManager.save(tableDTO);
        Assert.assertNotNull(saved.id());
    }

}