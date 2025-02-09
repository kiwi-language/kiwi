package org.metavm.entity;

import com.intellij.psi.impl.PsiSuperMethodImplUtil;
import junit.framework.TestCase;
import org.elasticsearch.transport.RemoteConnectionInfo;
import org.junit.Assert;
import org.metavm.entity.natives.CallContext;
import org.metavm.flow.Method;
import org.metavm.http.HttpResponseImpl;
import org.metavm.object.instance.core.Value;
import org.metavm.object.type.Field;
import org.metavm.object.type.Index;
import org.metavm.object.type.Klass;
import org.metavm.object.type.TypeVariable;
import org.metavm.util.ReflectionUtils;

import java.lang.invoke.MethodHandles;
import java.security.Timestamp;
import java.security.cert.CertPath;
import java.util.*;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.stream.Stream;

public class ModelIdentitiesTest extends TestCase {

    public void test() {
        Assert.assertEquals("int", ModelIdentities.getModelName(int.class));
        Assert.assertEquals(
                ModelIdentity.create(Klass.class, "java.util.Map.Entry"),
                ModelIdentities.getIdentity(Map.Entry.class));
        Assert.assertEquals(
                ModelIdentity.create(Field.class, "java.util.Spliterator.ORDERED"),
                ModelIdentities.getIdentity(ReflectionUtils.getField(Spliterator.class, "ORDERED")));
        Assert.assertEquals("java.util.Collection<@java.util.Set.E>",
                ModelIdentities.getModelName(Set.class.getGenericInterfaces()[0]));
        Assert.assertEquals(
                ModelIdentity.create(Method.class, "java.util.Map.put(@java.util.Map.K|null,@java.util.Map.V|null)"),
                ModelIdentities.getIdentity(ReflectionUtils.getMethod(Map.class, "put", Object.class, Object.class))
        );
        Assert.assertEquals(
                ModelIdentity.create(Method.class, "java.util.List.toArray((@this.T|null)[]|null)"),
                ModelIdentities.getIdentity(ReflectionUtils.getMethod(List.class, "toArray", Object[].class))
        );
        Assert.assertEquals(
                ModelIdentity.create(Method.class, "java.util.List.get(int)"),
                ModelIdentities.getIdentity(ReflectionUtils.getMethod(List.class, "get", int.class))
        );
        Assert.assertEquals(
                ModelIdentity.create(Method.class, "java.util.ArrayList.ArrayList(java.util.Collection<[never,@java.util.ArrayList.E]>|null)"),
                ModelIdentities.getIdentity(ReflectionUtils.getConstructor(ArrayList.class, Collection.class))
        );
        Assert.assertEquals(
                ModelIdentity.create(Index.class, "org.metavm.object.type.Klass.UNIQUE_QUALIFIED_NAME"),
                ModelIdentities.getIdentity(ReflectionUtils.getField(Klass.class, "UNIQUE_QUALIFIED_NAME"))
        );
        Assert.assertEquals(
                ModelIdentity.create(Method.class, "java.util.Comparator.comparing(java.util.function.Function<[@this.T,any],[never,@this.U]>|null)"),
                ModelIdentities.getIdentity(ReflectionUtils.getMethod(Comparator.class, "comparing", Function.class))
        );
        Assert.assertEquals(
                ModelIdentity.create(TypeVariable.class, "java.util.Map.K"),
                ModelIdentities.getIdentity(Map.class.getTypeParameters()[0])
        );
        // public abstract java.lang.Object[] java.util.stream.Stream.toArray(java.util.function.IntFunction)
        Assert.assertEquals(
                ModelIdentity.create(Method.class, "java.util.stream.Stream.toArray(java.util.function.IntFunction<(@this.A|null)[]>|null)"),
                ModelIdentities.getIdentity(ReflectionUtils.getMethod(Stream.class, "toArray", IntFunction.class))
        );
        Assert.assertEquals(
                ModelIdentity.create(Method.class, "java.lang.invoke.MethodHandles.throwException(null|org.metavm.object.type.Klass,null|org.metavm.object.type.Klass)"),
                ModelIdentities.getIdentity(ReflectionUtils.getMethod(MethodHandles.class, "throwException", Class.class, Class.class))
        );
        Assert.assertEquals(
                ModelIdentity.create(Method.class, "java.security.Timestamp.Timestamp(null|time,java.security.cert.CertPath|null)"),
                ModelIdentities.getIdentity(ReflectionUtils.getConstructor(Timestamp.class, Date.class, CertPath.class))
        );
        Assert.assertEquals(
                ModelIdentity.create(Method.class, "org.metavm.http.HttpResponseImpl.addCookie(any|null,any|null,null|org.metavm.entity.natives.CallContext)"),
                ModelIdentities.getIdentity(ReflectionUtils.getMethod(HttpResponseImpl.class, "addCookie", Value.class, Value.class, CallContext.class))
        );

    }

}