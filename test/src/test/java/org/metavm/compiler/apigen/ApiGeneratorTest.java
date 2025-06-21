package org.metavm.compiler.apigen;

import junit.framework.TestCase;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
public class ApiGeneratorTest extends TestCase {

    @SneakyThrows
    public void test() {
        var clazz = Mocks.createShoppingClasses();
        var text = new ApiGenerator().generate(clazz);
        log.debug("\n{}", text);
        Files.writeString(
                Path.of("/tmp/pageworks/1/src/api.ts"),
                text
        );
    }

}