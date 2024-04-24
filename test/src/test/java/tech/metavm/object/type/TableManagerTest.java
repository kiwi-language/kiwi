package tech.metavm.object.type;

import junit.framework.TestCase;
import org.junit.Assert;
import tech.metavm.entity.*;
import tech.metavm.mocks.Baz;
import tech.metavm.mocks.Foo;
import tech.metavm.object.instance.InstanceQueryService;
import tech.metavm.object.instance.core.Id;
import tech.metavm.object.instance.core.TmpId;
import tech.metavm.object.type.rest.dto.ColumnDTO;
import tech.metavm.object.type.rest.dto.TableDTO;
import tech.metavm.object.type.rest.dto.TitleFieldDTO;
import tech.metavm.task.TaskManager;
import tech.metavm.util.*;

import java.util.List;

public class TableManagerTest extends TestCase {

    private TableManager tableManager;

    @Override
    protected void setUp() throws Exception {
        var bootResult = BootstrapUtils.bootstrap();
        var entityContextFactory = bootResult.entityContextFactory();
        TaskManager jobManager = new TaskManager(entityContextFactory, new MockTransactionOperations());
        EntityQueryService entityQueryService =
                new EntityQueryService(new InstanceQueryService(bootResult.instanceSearchService()));
        TypeManager typeManager = new TypeManager(entityContextFactory, entityQueryService, jobManager, new MockTransactionOperations());
        tableManager = new TableManager(entityContextFactory, typeManager);
        ContextUtil.setAppId(TestConstants.APP_ID);
    }

    @Override
    protected void tearDown() {
        tableManager = null;
    }

    public void testSmoking() {
        Type fooType = ModelDefRegistry.getType(Foo.class);

        TableDTO tableDTO = tableManager.get(fooType.getStringId());
        Assert.assertEquals(fooType.getStringId(), tableDTO.id());

        Field bazListField = ModelDefRegistry.getField(Foo.class, "bazList");
        Assert.assertTrue(bazListField.getType().isNullable());
        Type bazListType = bazListField.getType().getUnderlyingType();
        Assert.assertTrue(bazListType.isArray());
        ArrayType arrayType = (ArrayType) bazListType;
        Assert.assertEquals(ModelDefRegistry.getType(Baz.class), arrayType.getElementType());
    }

    public void testGet() {
        Klass type = ModelDefRegistry.getClassType(Type.class).resolve();
        TableDTO tableDTO = tableManager.get(type.getStringId());
        Assert.assertNotNull(tableDTO.id());
        Assert.assertEquals(type.getName(), tableDTO.name());
        Assert.assertEquals(type.getAllFields().size(), tableDTO.fields().size());
        for (ColumnDTO column : tableDTO.fields()) {
            Assert.assertNotNull(column.id());
            Field field = type.getField(Id.parse(column.id()));
            Assert.assertNotNull(field);
            Assert.assertEquals(field.getName(), column.name());
            Assert.assertEquals(field.isUnique(), column.unique());
            Assert.assertEquals(field.isArray(), column.multiValued());
            Assert.assertEquals(field.isNotNull(), column.required());
        }
    }

    public void testSave() {
        TableDTO tableDTO = new TableDTO(
                TmpId.random().toString(), "傻", "Foo", null,
                false, false,
                new TitleFieldDTO(
                        NncUtils.randomNonNegative(),
                        "name", TableManager.ColumnType.STRING.code(),
                        false, null
                ),
                List.of(
                        ColumnDTO.createPrimitive(
                                NncUtils.randomNonNegative(),
                                "code", TableManager.ColumnType.STRING.code(),
                                true, false
                        )
                )
        );

        TableDTO saved = save(tableDTO);
        Assert.assertNotNull(saved.id());
    }

    public void testMultiValuedField() {
        TableDTO bar = save(new TableDTO(
                TmpId.random().toString(),  "Bar", "Bar", null,
                false, false,
                new TitleFieldDTO(
                        NncUtils.randomNonNegative(),
                        "code", TableManager.ColumnType.STRING.code(),
                        true, null
                ),
                List.of()
        ));


        TableDTO foo = save(new TableDTO(
                TmpId.random().toString(), "Foo", "Foo", null,
                false, false,
                new TitleFieldDTO(
                        NncUtils.randomNonNegative(),
                        "name", TableManager.ColumnType.STRING.code(),
                        false, null
                ),
                List.of(
                        new ColumnDTO(
                                TmpId.random().toString(),
                                "bars", TableManager.ColumnType.TABLE.code(),
                                Access.PUBLIC.code(), null,
                                bar.id(), null, true, true, false,
                                  null, null
                        )
                )
        ));

        TableDTO loadedFoo = tableManager.get(foo.id());

        Assert.assertEquals(2, loadedFoo.fields().size());
    }

    private TableDTO save(TableDTO tableDTO) {
        return TestUtils.doInTransaction(() -> tableManager.save(tableDTO));
    }

}