package org.metavm.entity;

import junit.framework.TestCase;
import org.junit.Assert;
import org.metavm.flow.Flows;
import org.metavm.http.HttpCookieImpl;
import org.metavm.http.HttpRequestImpl;
import org.metavm.util.BootstrapUtils;
import org.metavm.util.Instances;
import org.metavm.util.TestConstants;

import java.util.List;
import java.util.Objects;

import static org.metavm.entity.StdMethod.httpRequestGetCookie;
import static org.metavm.entity.StdMethod.httpRequestGetMethod;
import static org.metavm.util.Instances.stringInstance;

public class TestNativeKlass extends TestCase {

    private EntityContextFactory entityContextFactory;

    @Override
    protected void setUp() throws Exception {
        var bootResult = BootstrapUtils.bootstrap();
        entityContextFactory = bootResult.entityContextFactory();
    }

    @Override
    protected void tearDown() throws Exception {
        entityContextFactory = null;
    }

    public void test() {
        try(var context = entityContextFactory.newContext(TestConstants.APP_ID)) {
            var request = new HttpRequestImpl(
                    "GET",
                    "/",
                    List.of(),
                    List.of(
                            new HttpCookieImpl("token", "__token__")
                    )
            );
            context.bind(request);
            var httpMethod = Flows.invokeVirtual(httpRequestGetMethod.get().getRef(), request, List.of(), context);
            Assert.assertEquals("GET", Instances.toJavaString(Objects.requireNonNull(httpMethod)));
            var token = Flows.invokeVirtual(httpRequestGetCookie.get().getRef(), request, List.of(stringInstance("token")), context);
            Assert.assertEquals("__token__", Instances.toJavaString(Objects.requireNonNull(token)));
        }
    }

}
