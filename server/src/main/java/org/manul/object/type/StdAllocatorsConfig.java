package org.manul.object.type;

import org.manul.context.Value;
import org.manul.context.Bean;
import org.manul.context.Configuration;

@Configuration
public class StdAllocatorsConfig {

    private final String cpRoot;

    public StdAllocatorsConfig(@Value("${manul.resource-cp-root}") String cpRoot) {
        this.cpRoot = cpRoot;
    }

    @Bean
    public StdAllocators stdAllocators() {
        return new StdAllocators(new DirectoryAllocatorStore(cpRoot));
    }

    @Bean
    public TypeTagStore typeTagStore() {
        return new FileTypeTagStore(cpRoot);
    }

}
