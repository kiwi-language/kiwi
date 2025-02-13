package org.metavm.entity;

import junit.framework.TestCase;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.metavm.flow.Function;
import org.metavm.flow.Parameter;
import org.metavm.object.type.IndexRef;
import org.metavm.object.type.Klass;
import org.metavm.object.type.TypeVariable;
import org.metavm.util.ReflectionUtils;

import java.io.ObjectStreamConstants;
import java.io.SerializablePermission;
import java.lang.invoke.MethodHandles;
import java.net.InterfaceAddress;
import java.util.List;
import java.util.Spliterator;

@Slf4j
public class StdIdGeneratorTest extends TestCase {

    public static final List<Class<?>> testClasses = List.of(
            Entity.class, MethodHandles.class, Spliterator.OfDouble.class,
            InterfaceAddress.class, ObjectStreamConstants.class, SerializablePermission.class,
            IndexRef.class
    );

    public static final List<ModelIdentity> identities = List.of(
            ModelIdentity.create(Function.class, "functionToInstance"),
            ModelIdentity.create(Parameter.class, "functionToInstance.function"),
            ModelIdentity.create(TypeVariable.class, "functionToInstance.T")
    );

    public void test() {
        var id = new long[1];
        var generator = new StdIdGenerator(() ->  id[0]++);
        generator.generate();
        for (Class<?> testClass : testClasses) {
            Assert.assertNotNull("ID not found for " + testClass, generator.getId(testClass));
        }
        for (var identity : identities) {
            Assert.assertNotNull("ID not found for " + identity, generator.getId(identity));
        }
        for (Class<?> k : EntityUtils.getModelClasses()) {
            Assert.assertNotNull(generator.getId(k));
        }
        var indexId = generator.getId(ReflectionUtils.getField(Klass.class, "UNIQUE_QUALIFIED_NAME"));
        Assert.assertNotNull(indexId);
    }

}