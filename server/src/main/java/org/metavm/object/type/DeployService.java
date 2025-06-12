package org.metavm.object.type;

import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;

public interface DeployService {
    @Transactional
    String deploy(InputStream in);
}
