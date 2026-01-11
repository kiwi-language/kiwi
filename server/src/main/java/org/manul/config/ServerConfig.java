package org.manul.config;

import org.manul.server.Controller;
import org.manul.server.Filter;
import org.manul.server.Server;
import org.manul.context.Bean;
import org.manul.context.Configuration;

import java.util.List;

@Configuration
public class ServerConfig {

    private final ManulConfig.ServerConfig serverConfig;

    public ServerConfig(ManulConfig config) {
        this.serverConfig = config.getServerConfig();
    }

    @Bean
    public Server server(List<Controller> controllers, List<Filter> filters) {
        return new Server(serverConfig.port(), controllers, filters);
    }

}
