package org.metavm.compiler;

import java.io.IOException;
import java.nio.file.Path;

public class MainTest {

    public static void main(String[] args) throws IOException {
        var main = new Main(Path.of("/Users/leen/workspace/kiwi_test"));
        main.initializeHttpClient();
        main.ensureLoggedIn();
        main.generateApi(false);
    }

}
