package org.metavm.springconfig;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class ElasticSearchConfig {
    private final KiwiConfig kiwiConfig;

    public ElasticSearchConfig(KiwiConfig kiwiConfig) {
        this.kiwiConfig = kiwiConfig;
    }

    @Bean
    public RestHighLevelClient restHighLevelClient() {
        var esConfig = kiwiConfig.getEsConfig();
        log.info("Es config: {}", esConfig);
        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        if (esConfig.user() != null) {
            credentialsProvider.setCredentials(AuthScope.ANY,
                    new UsernamePasswordCredentials(esConfig.user(), esConfig.password()));
        }
        RestClientBuilder builder = RestClient.builder(new HttpHost(esConfig.host(), esConfig.port()))
                .setHttpClientConfigCallback(b -> b.setDefaultCredentialsProvider(credentialsProvider));

        return new RestHighLevelClient(builder);
    }

}
