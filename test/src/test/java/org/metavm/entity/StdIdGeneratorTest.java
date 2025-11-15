package org.metavm.entity;

import junit.framework.TestCase;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.metavm.api.Index;
import org.metavm.api.Interceptor;
import org.metavm.api.entity.HttpCookie;
import org.metavm.api.entity.HttpRequest;
import org.metavm.flow.Function;
import org.metavm.flow.Parameter;
import org.metavm.http.HttpRequestImpl;
import org.metavm.object.type.TypeVariable;

import java.util.List;
import java.util.ServiceLoader;

@Slf4j
public class StdIdGeneratorTest extends TestCase {

    public static final List<Class<?>> testClasses = List.of(
            Index.class, Interceptor.class, HttpRequestImpl.class, HttpRequest.class, HttpCookie.class
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
        var builders = ServiceLoader.load(StdKlassBuilder.class, StdIdGeneratorTest.class.getClassLoader());
        for (var b : builders) {
            Assert.assertNotNull(generator.getId(b.getJavaClass()));
        }
        var indexId = generator.getId(ModelIdentity.create(org.metavm.object.type.Index.class, "org.metavm.object.type.Klass.UNIQUE_QUALIFIED_NAME"));
        Assert.assertNotNull(indexId);
    }

}