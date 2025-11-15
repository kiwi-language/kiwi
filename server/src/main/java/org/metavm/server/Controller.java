package org.metavm.server;

import java.util.List;

public interface Controller {

    String getPath();

    List<RouteConfig> getRoutes();

}
