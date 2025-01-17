package org.metavm.util;

import java.io.File;

public class MetaVersionStore {

//    public static final MetaVersionStore INSTANCE = new MetaVersionStore();

//    private static final String VERSION_FILE = CompilerConstants.HOME_DIR + File.separator + "metaVersion";

    private final String versionFile;

    public MetaVersionStore(String versionFile) {
        this.versionFile = versionFile;
    }

    public long getMetaVersion() {
        var file = new File(versionFile);
        return file.exists() ? Utils.readLong(file) : -1L;
    }

    public void setMetaVersion(long l) {
        Utils.writeFile(versionFile, Long.toString(l));
    }

}
