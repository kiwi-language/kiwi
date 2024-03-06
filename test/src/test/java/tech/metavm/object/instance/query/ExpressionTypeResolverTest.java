package tech.metavm.object.instance.query;

import junit.framework.TestCase;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.metavm.entity.*;
import tech.metavm.expression.*;
import tech.metavm.object.instance.core.InstanceProvider;
import tech.metavm.object.instance.core.mocks.MockInstanceRepository;
import tech.metavm.object.type.*;
import tech.metavm.object.type.mocks.MockArrayTypeProvider;
import tech.metavm.object.type.mocks.MockTypeRepository;
import tech.metavm.object.type.mocks.MockUnionTypeProvider;
import tech.metavm.util.TestUtils;

import java.util.List;

public class ExpressionTypeResolverTest extends TestCase {

    public static final Logger LOGGER = LoggerFactory.getLogger(ExpressionTypeResolverTest.class);

    private TypeRepository typeRepository;
    private InstanceProvider instanceProvider;
    private ArrayTypeProvider arrayTypeProvider;
    private UnionTypeProvider unionTypeProvider;

    @Override
    protected void setUp() throws Exception {
        typeRepository = new MockTypeRepository();
        instanceProvider = new MockInstanceRepository();
        arrayTypeProvider = new MockArrayTypeProvider();
        unionTypeProvider = new MockUnionTypeProvider();
        MockStandardTypesInitializer.init();
    }

    public void testEq() {
        var fooType = ClassTypeBuilder.newBuilder("Foo", "Foo").build();
        typeRepository.save(List.of(fooType));
        FieldBuilder.newBuilder("名称", "name", fooType, StandardTypes.getStringType()).build();
        String exprString = "this.名称 = \"Big Foo\"";
        var expression = ExpressionParser.parse(exprString, createTypeParsingContext(fooType));
        Assert.assertNotNull(expression);
        Assert.assertEquals(exprString, expression.build(VarType.NAME));
    }

    public void testAllMatch() {
        var listViewType = ClassTypeBuilder.newBuilder("列表视图", "ListView").build();
        var classTypeType = ClassTypeBuilder.newBuilder("Class类型", "ClassType").build();
        var fieldType = ClassTypeBuilder.newBuilder("字段", "Field").build();
        var fieldChildArrayType = new ArrayType(null, fieldType, ArrayKind.CHILD);
        FieldBuilder.newBuilder("可见字段", "visibleFields", listViewType, fieldChildArrayType).build();
        FieldBuilder.newBuilder("类型", "type", listViewType, classTypeType).build();
        FieldBuilder.newBuilder("所属类型", "declaringType", fieldType, classTypeType).build();
        TestUtils.initEntityIds(listViewType);
        typeRepository.save(List.of(listViewType, classTypeType, fieldType));

        String str = "allmatch(可见字段, 所属类型=this.类型)";
        var expression = ExpressionParser.parse(str, createTypeParsingContext(listViewType));
        Assert.assertTrue(expression instanceof AllMatchExpression);
        LOGGER.info(expression.build(VarType.NAME));
    }

    private TypeParsingContext createTypeParsingContext(ClassType type) {
        return new TypeParsingContext(instanceProvider, typeRepository, arrayTypeProvider, unionTypeProvider, type);
    }

}