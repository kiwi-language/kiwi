package org.metavm.entity;

import junit.framework.TestCase;
import org.junit.Assert;
import org.metavm.flow.MethodBuilder;
import org.metavm.flow.Parameter;
import org.metavm.object.type.ClassSource;
import org.metavm.object.type.FieldBuilder;
import org.metavm.object.type.TypeVariable;
import org.metavm.object.type.Types;
import org.metavm.util.TestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.IdentityHashMap;
import java.util.List;

public class IdentityContextTest extends TestCase {

    public static final Logger logger = LoggerFactory.getLogger(IdentityContextTest.class);

    public void test() {
        IdentityContext identityContext = new IdentityContext();
        var fooKlass = TestUtils.newKlassBuilder("Foo", "org.metavm.Foo")
                .source(ClassSource.BUILTIN)
                .build();

        var fooNameField = FieldBuilder.newBuilder("name", fooKlass, Types.getStringType())
                .build();

        var method = MethodBuilder.newBuilder(fooKlass, "bar")
                .returnType(Types.getVoidType())
                .build();
        var typeVar = new TypeVariable(fooKlass.nextChildId(), "T", method);
        method.setTypeParameters(List.of(typeVar));
        method.setParameters(List.of(new Parameter(fooKlass.nextChildId(), "t", typeVar.getType(), method)));
        var identities = new IdentityHashMap<Object, ModelIdentity>();
        fooKlass.visitGraph(i -> {
            if (i instanceof Entity entity)
                identities.put(entity, identityContext.getModelId(entity));
            return true;
        });
        Assert.assertTrue(identities.containsKey(fooKlass));
        Assert.assertTrue(identities.containsKey(fooNameField));
        Assert.assertTrue(identities.containsKey(method));
        Assert.assertTrue(identities.containsKey(method.getParameters().getFirst()));
    }

}