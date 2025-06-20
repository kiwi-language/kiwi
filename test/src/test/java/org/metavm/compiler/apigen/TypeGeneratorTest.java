package org.metavm.compiler.apigen;

import junit.framework.TestCase;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
public class TypeGeneratorTest extends TestCase {

    @SneakyThrows
    public void test() {
        var cls = Mocks.createShoppingClasses();
        var text = new TypeGenerator().generate(cls);
        log.debug("\n{}", text);
        Files.writeString(
                Path.of("/tmp/frontworks/1/src/types.ts"),
                text
        );
    }

}
