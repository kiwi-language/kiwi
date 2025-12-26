package org.metavm.server;

import java.util.function.Consumer;

public interface Filter {

    void filter(HttpRequest request, Consumer<HttpRequest> proceed);

    int order();

}
