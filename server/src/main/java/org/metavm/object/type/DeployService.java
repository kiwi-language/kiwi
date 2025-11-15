package org.metavm.object.type;

import org.metavm.context.sql.Transactional;

import java.io.InputStream;

public interface DeployService {

    @Transactional
    String deploy(InputStream in);

    String deploy(boolean noBackup, InputStream in);

}
