package org.metavm.object.type;

import org.metavm.context.Value;
import org.metavm.context.Bean;
import org.metavm.context.Configuration;

@Configuration
public class StdAllocatorsConfig {

    private final String cpRoot;

    public StdAllocatorsConfig(@Value("${metavm.resource-cp-root}") String cpRoot) {
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
