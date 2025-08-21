package org.metavm.compiler.apigen;

import junit.framework.TestCase;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.metavm.util.Utils;

import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
public class ApiGeneratorTest extends TestCase {

    @SneakyThrows
    public void test() {
        var clazz = Mocks.createShoppingClasses();
        var text = new ApiGeneratorV3().generate(clazz);
        log.info(text);
    }

    // This test can't be written as an unit test because it's environment dependent
    @SneakyThrows
    public static void main(String[] args) {
        var clazz = Mocks.createShoppingClasses();
        var text = new ApiGeneratorV3().generate(clazz);
        Files.writeString(
                Path.of("/Users/leen/develop/page-works/1/src/api.ts"),
                text
        );
        var r = Utils.executeCommand(Path.of("/Users/leen/develop/page-works/1"), "npm", "run", "build");
        System.out.println(r.output());
        Utils.require(r.exitCode() == 0, "Build failed");
    }

}