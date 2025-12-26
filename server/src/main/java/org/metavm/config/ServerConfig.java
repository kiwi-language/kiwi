package org.metavm.config;

import org.metavm.server.Controller;
import org.metavm.server.Filter;
import org.metavm.server.Server;
import org.metavm.context.Bean;
import org.metavm.context.Configuration;

import java.util.List;

@Configuration
public class ServerConfig {

    private final KiwiConfig.ServerConfig serverConfig;

    public ServerConfig(KiwiConfig config) {
        this.serverConfig = config.getServerConfig();
    }

    @Bean
    public Server server(List<Controller> controllers, List<Filter> filters) {
        return new Server(serverConfig.port(), controllers, filters);
    }

}
