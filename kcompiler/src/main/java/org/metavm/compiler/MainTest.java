package org.metavm.compiler;

import java.io.IOException;

public class MainTest {

    public static void main(String[] args) throws IOException {
        var main = new Main("/Users/leen/workspace/kiwi_test");
        main.initializeHttpClient();
        main.ensureLoggedIn();
        main.deploy();
    }

}
