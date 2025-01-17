package org.metavm.object;

import org.hamcrest.MatcherAssert;
import org.junit.Assert;
import org.metavm.autograph.CompilerTestBase;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.MvClassInstance;
import org.metavm.object.instance.rest.LoadInstancesByPathsRequest;
import org.metavm.object.instance.rest.SelectRequest;
import org.metavm.util.Constants;
import org.metavm.util.InstanceDTOMatcher;
import org.metavm.util.Instances;
import org.metavm.util.TestUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class FooCompilingTest extends CompilerTestBase {

    public static final String FOO_SOURCE_ROOT = "/Users/leen/workspace/object/lab/src/main/foo";

    public void testLoadByPaths() {
        compile(FOO_SOURCE_ROOT);
        submit(() -> {
            var id = saveFoo();
            var result = instanceManager.loadByPaths(
                    new LoadInstancesByPathsRequest(
                            null,
                            List.of(
                                    Constants.ID_PREFIX + id + ".bar",
                                    Constants.ID_PREFIX + id + ".bar.code",
                                    Constants.ID_PREFIX + id + ".bazList.*.bars.0.code",
                                    Constants.ID_PREFIX + id + ".bazList.*.bars.1.code"
                            )
                    )
            );
            try (var context = entityContextFactory.newContext()) {
                context.loadKlasses();
                var foo = (MvClassInstance) context.get(id);
                Assert.assertEquals(
                        List.of(
                                foo.getField("bar").toDTO(),
                                Instances.createString("Bar001").toDTO(),
                                Instances.createString("Bar002").toDTO(),
                                Instances.createString("Bar004").toDTO(),
                                Instances.createString("Bar003").toDTO(),
                                Instances.createString("Bar005").toDTO()
                        ),
                        result
                );
            }
        });
    }

    public void testSelect() {
        compile(FOO_SOURCE_ROOT);
        submit(() -> {
            var id = saveFoo();
            TestUtils.waitForAllTasksDone(schedulerAndWorker);
            var fooKlassId = typeManager.getKlassId("Foo");
            var page = instanceManager.select(new SelectRequest(
                    Constants.ID_PREFIX + fooKlassId.toString(),
                    List.of(
                            "bar.code",
                            "qux"
                    ),
                    "name = \"Big Foo\"",
                    1,
                    20
            ));
            Assert.assertEquals(1, page.total());
            try (var context = entityContextFactory.newContext()) {
                context.loadKlasses();
                var foo = (MvClassInstance) context.get(id);
                MatcherAssert.assertThat(page.data().getFirst()[0],
                        InstanceDTOMatcher.of(Instances.createString("Bar001").toDTO()));
                Assert.assertEquals(page.data().getFirst()[1].id(), Objects.requireNonNull(foo.getField("qux")).getStringId());
            }
        });
    }

    private Id saveFoo() {
        return Id.parse(saveInstance("Foo", Map.of(
                "name", "Big Foo",
                "bar", Map.of(
                        "code", "Bar001"
                ),
                "qux", Map.of("amount", 100),
                "bazList", List.of(
                        Map.of(
                                "bars", List.of(
                                        Map.of("code", "Bar002"),
                                        Map.of("code", "Bar003")
                                )
                        ),
                        Map.of(
                                "bars", List.of(
                                        Map.of("code", "Bar004"),
                                        Map.of("code", "Bar005")
                                )
                        )
                )
        )));
    }


}
