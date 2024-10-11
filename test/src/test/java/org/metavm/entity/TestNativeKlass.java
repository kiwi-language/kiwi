package org.metavm.entity;

import junit.framework.TestCase;
import org.junit.Assert;
import org.metavm.api.entity.HttpCookie;
import org.metavm.flow.Flows;
import org.metavm.http.HttpRequestImpl;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.util.BootstrapUtils;
import org.metavm.util.ContextUtil;
import org.metavm.util.TestConstants;

import java.util.List;

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
            ContextUtil.setEntityContext(context);
            var request = new HttpRequestImpl(
                    "GET",
                    "/",
                    List.of(),
                    List.of(
                            new HttpCookie("token", "__token__")
                    )
            );
            context.bind(request);
            var inst = (ClassInstance) context.getInstance(request);
            var httpMethod = Flows.invokeVirtual(httpRequestGetMethod.get(), inst, List.of(), context);
            Assert.assertEquals(stringInstance("GET"), httpMethod);
            var token = Flows.invokeVirtual(httpRequestGetCookie.get(), inst, List.of(stringInstance("token")), context);
            Assert.assertEquals(stringInstance("__token__"), token);
        } finally {
            ContextUtil.setEntityContext(null);
        }
    }

}
