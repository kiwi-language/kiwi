package org.metavm.springconfig;

import org.springframework.boot.web.server.ConfigurableWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServerConfig {

    private final KiwiConfig.ServerConfig serverConfig;

    public ServerConfig(KiwiConfig config) {
        this.serverConfig = config.getServerConfig();
    }

    @Bean
    public WebServerFactoryCustomizer<ConfigurableWebServerFactory> webServerFactoryCustomizer() {
        return factory -> factory.setPort(serverConfig.port());
    }

}
