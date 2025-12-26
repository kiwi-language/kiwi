package org.metavm.entity;

import junit.framework.TestCase;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.metavm.flow.Function;
import org.metavm.flow.Parameter;
import org.metavm.object.type.TypeVariable;

import java.util.List;

@Slf4j
public class StdIdGeneratorTest extends TestCase {

    public static final List<ModelIdentity> identities = List.of(
            ModelIdentity.create(Function.class, "functionToInstance"),
            ModelIdentity.create(Parameter.class, "functionToInstance.function"),
            ModelIdentity.create(TypeVariable.class, "functionToInstance.T")
    );

    public void test() {
        var id = new long[1];
        var generator = new StdIdGenerator(() ->  id[0]++);
        generator.generate();
        for (var identity : identities) {
            Assert.assertNotNull("ID not found for " + identity, generator.getId(identity));
        }
    }

}