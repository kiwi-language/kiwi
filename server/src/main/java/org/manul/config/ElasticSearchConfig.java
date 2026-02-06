package org.manul.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.manul.context.Bean;
import org.manul.context.Configuration;

@Slf4j
@Configuration(module = "persistent")
public class ElasticSearchConfig {
    private final ManulConfig manulConfig;

    public ElasticSearchConfig(ManulConfig manulConfig) {
        this.manulConfig = manulConfig;
    }

    @Bean
    public RestClient restClient() {
        var esConfig = manulConfig.getSearchConfig();
        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        if (esConfig.user() != null) {
            credentialsProvider.setCredentials(AuthScope.ANY,
                    new UsernamePasswordCredentials(esConfig.user(), esConfig.password()));
        }
        RestClientBuilder builder = RestClient.builder(new HttpHost(esConfig.host(), esConfig.port()))
                .setHttpClientConfigCallback(b -> b.setDefaultCredentialsProvider(credentialsProvider));

        return builder.build();
    }

}