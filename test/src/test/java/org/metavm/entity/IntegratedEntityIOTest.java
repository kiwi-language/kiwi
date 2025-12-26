package org.metavm.entity;

import junit.framework.TestCase;
import lombok.extern.slf4j.Slf4j;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.metavm.flow.MethodBuilder;
import org.metavm.flow.NameAndType;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.instance.core.*;
import org.metavm.object.type.FieldBuilder;
import org.metavm.object.type.Klass;
import org.metavm.object.type.KlassBuilder;
import org.metavm.object.type.Types;
import org.metavm.util.*;
import org.metavm.wire.AdapterRegistry;
import org.metavm.wire.WireAdapter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/** @noinspection resource*/
@Slf4j
public class IntegratedEntityIOTest extends TestCase {

    public void testKlass() {
        var klass = KlassBuilder.newBuilder(
                TmpId.random(),
                "Foo",
                "Foo"
        ).build();
        FieldBuilder.newBuilder("name", klass, Types.getStringType()).build();

        MethodBuilder.newBuilder(klass, "bar")
                .isAbstract(true)
                .parameters(new NameAndType("o", Types.getAnyType()))
                .build();

        var klass1 = recover(klass);
        assertEquals(klass.getId(), klass1.getId());
        assertEquals(klass.getName(), klass1.getName());
        assertEquals(1, klass1.getFields().size());
        var field = klass1.getFields().getFirst();
        assertEquals("name", field.getName());
        assertEquals(Types.getStringType(), field.getType());
        assertEquals(1, klass1.getMethods().size());
        var method = klass1.getMethods().getFirst();
        assertEquals("bar", method.getName());
        assertEquals(1, method.getParameters().size());
        assertEquals("o", method.getParameters().getFirst().getName());
        assertTrue(method.isAbstract());
        assertEquals(Types.getAnyType(), method.getParameters().getFirst().getType());
    }

    public void testCompatibility() {
        var klass = KlassBuilder.newBuilder(
                PhysicalId.of(10000, 0),
                "Foo",
                "Foo"
        ).build();
        FieldBuilder.newBuilder("name", klass, Types.getStringType()).build();

        MethodBuilder.newBuilder(klass, "bar")
                .isAbstract(true)
                .parameters(new NameAndType("o", Types.getAnyType()))
                .build();

        var bout = new ByteArrayOutputStream();
        var out = new InstanceOutput(bout);
        klass.writeTo(out);
        var bytes = bout.toByteArray();

        var in = createInput(bytes);
        var o = in.readTree();
        MatcherAssert.assertThat(o, CoreMatchers.instanceOf(Klass.class));
        var k = (Klass) o;
        assertEquals(klass.getName(), k.getName());

        var ref = new Object() {
          int entityCount;
        };
        var visitor = new StreamVisitor(new ByteArrayInputStream(bytes)) {

            @Override
            public void visitEntity0(WireAdapter<?> adapter) {
                if (adapter.getTag() != -1)
                    ref.entityCount++;
                super.visitEntity0(adapter);
            }
        };
        visitor.visitTree();
        assertEquals(4, ref.entityCount);

        var bout1 = new ByteArrayOutputStream();
        var copier = new StreamCopier(new ByteArrayInputStream(bytes), bout1);
        copier.visitTree();

        var in1 = createInput(bout1.toByteArray());
        var klass1 = (Klass) in1.readTree();
        assertEquals(klass.getName(), klass1.getName());
    }

    private InstanceInput createInput(byte[] bytes) {
        return new InstanceInput(
                new ByteArrayInputStream(bytes),
                InstanceInput.UNSUPPORTED_RESOLVER,
                i -> {},
                InstanceInput.UNSUPPORTED_REDIRECTION_SIGNAL_PROVIDER
        );
    }

    public void testValue() {
        var ref = new EntityReference(TmpId.random(), () -> {
            throw new UnsupportedOperationException();
        });
        var ref1 = recover(ref, EntityReference.class);
        assertEquals(ref, ref1);
    }

    private <T> T recover(T o) {
        return recover(o, Object.class);
    }

    /** @noinspection SameParameterValue*/
    private <T> T recover(T o, Class<? super T> cls) {
        var adapter = AdapterRegistry.instance.getAdapter(cls);
        var bout = new ByteArrayOutputStream();
        var out = new MvOutput(bout) {};
        out.writeEntity(o, adapter);
        var bin = new ByteArrayInputStream(bout.toByteArray());
        var in = new MyInput(bin);
        //noinspection unchecked
        return (T) in.readEntity(adapter, null);
    }

    private static class MyInput extends MvInput {
        public MyInput(InputStream in) {
            super(in);
        }

        @Override
        public Message readTree() {
            return null;
        }

        @Override
        public Value readRemovingInstance() {
            return null;
        }

        @Override
        public Value readValueInstance() {
            return null;
        }

        @Override
        public Value readInstance() {
            return null;
        }

        @Override
        public Reference readReference() {
            return new EntityReference(
                    readId(),
                    () -> {
                        throw new UnsupportedOperationException();
                    }
            );
        }

        @Override
        public Entity readEntityMessage() {
            return null;
        }
    }

}
