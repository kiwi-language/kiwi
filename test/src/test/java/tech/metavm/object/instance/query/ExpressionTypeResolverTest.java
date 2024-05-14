package tech.metavm.object.instance.query;

import junit.framework.TestCase;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.metavm.entity.MockStandardTypesInitializer;
import tech.metavm.entity.StandardTypes;
import tech.metavm.expression.AllMatchExpression;
import tech.metavm.expression.ExpressionParser;
import tech.metavm.expression.TypeParsingContext;
import tech.metavm.expression.VarType;
import tech.metavm.object.instance.core.InstanceProvider;
import tech.metavm.object.instance.core.mocks.MockInstanceRepository;
import tech.metavm.object.type.*;
import tech.metavm.object.type.mocks.MockTypeDefRepository;
import tech.metavm.util.TestUtils;

import java.util.List;

public class ExpressionTypeResolverTest extends TestCase {

    public static final Logger LOGGER = LoggerFactory.getLogger(ExpressionTypeResolverTest.class);

    private TypeDefRepository typeDefRepository;
    private InstanceProvider instanceProvider;

    @Override
    protected void setUp() throws Exception {
        typeDefRepository = new MockTypeDefRepository();
        instanceProvider = new MockInstanceRepository();
        MockStandardTypesInitializer.init();
    }

    public void testEq() {
        var fooType = ClassTypeBuilder.newBuilder("Foo", "Foo").build();
        TestUtils.initEntityIds(fooType);
        typeDefRepository.save(List.of(fooType));
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
        var fieldChildArrayType = new ArrayType(fieldType.getType(), ArrayKind.CHILD);
        FieldBuilder.newBuilder("可见字段", "visibleFields", listViewType, fieldChildArrayType).build();
        FieldBuilder.newBuilder("类型", "type", listViewType, classTypeType.getType()).build();
        FieldBuilder.newBuilder("所属类型", "declaringType", fieldType, classTypeType.getType()).build();
        TestUtils.initEntityIds(listViewType);
        typeDefRepository.save(List.of(listViewType, classTypeType, fieldType));

        String str = "allmatch(可见字段, 所属类型=this.类型)";
        var expression = ExpressionParser.parse(str, createTypeParsingContext(listViewType));
        Assert.assertTrue(expression instanceof AllMatchExpression);
        LOGGER.info(expression.build(VarType.NAME));
    }

    private TypeParsingContext createTypeParsingContext(Klass type) {
        return new TypeParsingContext(instanceProvider, typeDefRepository, type);
    }

}