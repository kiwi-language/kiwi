package org.manul.object.type;

import org.manul.context.sql.Transactional;

import java.io.InputStream;

public interface DeployService {

    @Transactional
    String deploy(InputStream in);

    String deploy(boolean noBackup, InputStream in);

}
