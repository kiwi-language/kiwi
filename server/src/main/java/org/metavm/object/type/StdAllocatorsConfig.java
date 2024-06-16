package org.metavm.object.type;

import org.metavm.object.type.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
    public ColumnStore columnStore() {
        return new FileColumnStore(cpRoot);
    }

    @Bean
    public TypeTagStore typeTagStore() {
        return new FileTypeTagStore(cpRoot);
    }

}
