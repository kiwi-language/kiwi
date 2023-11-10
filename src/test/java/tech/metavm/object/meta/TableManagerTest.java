package tech.metavm.object.meta;

import junit.framework.TestCase;
import org.junit.Assert;
import tech.metavm.entity.*;
import tech.metavm.task.TaskManager;
import tech.metavm.mocks.Baz;
import tech.metavm.mocks.Foo;
import tech.metavm.object.instance.InstanceQueryService;
import tech.metavm.object.instance.MemInstanceSearchService;
import tech.metavm.object.meta.rest.dto.ColumnDTO;
import tech.metavm.object.meta.rest.dto.TableDTO;
import tech.metavm.object.meta.rest.dto.TitleFieldDTO;
import tech.metavm.util.MockIdProvider;
import tech.metavm.util.MockTransactionOperations;

import java.util.List;

public class TableManagerTest extends TestCase {

    private TableManager tableManager;

    @Override
    protected void setUp() throws Exception {
        EntityIdProvider idProvider = new MockIdProvider();
        InstanceContextFactory instanceContextFactory = new InstanceContextFactory(new MemInstanceStore()).setIdService(idProvider);
        Bootstrap bootstrap = new Bootstrap(instanceContextFactory, new StdAllocators(new MemAllocatorStore()), new MemColumnStore());
        bootstrap.bootAndSave();

        TaskManager jobManager = new TaskManager(instanceContextFactory, new MockTransactionOperations());

        EntityQueryService entityQueryService =
                new EntityQueryService(new InstanceQueryService(new MemInstanceSearchService()));
        TypeManager typeManager = new TypeManager(instanceContextFactory, entityQueryService, jobManager,null);
        tableManager = new TableManager(typeManager, instanceContextFactory);
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
        Assert.assertEquals(type.getName(), tableDTO.name());
        Assert.assertEquals(type.getAllFields().size(), tableDTO.fields().size());
        for (ColumnDTO column : tableDTO.fields()) {
            Assert.assertNotNull(column.id());
            Field field = type.getField(column.id());
            Assert.assertNotNull(field);
            Assert.assertEquals(field.getName(), column.name());
            Assert.assertEquals(field.isUnique(), column.unique());
            Assert.assertEquals(field.isArray(), column.multiValued());
            Assert.assertEquals(field.isNotNull(), column.required());
        }
    }

    public void testSave() {
        TableDTO tableDTO = new TableDTO(
                null, "傻", "Foo", null,
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

    public void testMultiValuedField() {

        TableDTO bar = tableManager.save(new TableDTO(
                null, "Bar", "Bar", null,
                false, false,
                new TitleFieldDTO(
                        "code", TableManager.ColumnType.STRING.code(),
                        true, null
                ),
                List.of()
        ));


        TableDTO foo = tableManager.save(new TableDTO(
                null, "Foo", "Foo", null,
                false, false,
                new TitleFieldDTO(
                        "name", TableManager.ColumnType.STRING.code(),
                        false, null
                ),
                List.of(
                        new ColumnDTO(
                                null, "bars", TableManager.ColumnType.TABLE.code(),
                                Access.PUBLIC.code(), null,
                                bar.id(), null, true, true, false,
                                 false, null, null
                        )
                )
        ));

        TableDTO loadedFoo = tableManager.get(foo.id());

        Assert.assertEquals(2, loadedFoo.fields().size());
    }

}