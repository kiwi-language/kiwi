package org.metavm.object.instance.query;

import junit.framework.TestCase;
import org.junit.Assert;
import org.metavm.expression.ExpressionParser;
import org.metavm.expression.TypeParsingContext;
import org.metavm.expression.VarType;
import org.metavm.object.instance.core.InstanceProvider;
import org.metavm.object.instance.core.mocks.MockInstanceRepository;
import org.metavm.object.type.*;
import org.metavm.object.type.mocks.MockTypeDefRepository;
import org.metavm.util.TestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ExpressionTypeResolverTest extends TestCase {

    public static final Logger logger = LoggerFactory.getLogger(ExpressionTypeResolverTest.class);

    private TypeDefRepository typeDefRepository;
    private InstanceProvider instanceProvider;

    @Override
    protected void setUp() throws Exception {
        typeDefRepository = new MockTypeDefRepository();
        instanceProvider = new MockInstanceRepository();
    }

    public void testEq() {
        var fooType = TestUtils.newKlassBuilder("Foo", "Foo").build();
        typeDefRepository.save(List.of(fooType));
        FieldBuilder.newBuilder("name", fooType, Types.getStringType()).build();
        String exprString = "this.name = \"Big Foo\"";
        var expression = ExpressionParser.parse(exprString, createTypeParsingContext(fooType));
        Assert.assertNotNull(expression);
        Assert.assertEquals(exprString, expression.build(VarType.NAME));
    }

    private TypeParsingContext createTypeParsingContext(Klass type) {
        return new TypeParsingContext(instanceProvider, typeDefRepository, type);
    }

}