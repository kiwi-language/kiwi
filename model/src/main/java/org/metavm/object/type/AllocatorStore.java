package org.metavm.object.type;

import java.util.List;
import java.util.Properties;

public interface AllocatorStore {

    String getFileName(String code);

    List<String> getFileNames();

    long getNextId();

    void saveNextId(long nextId);

    Properties load(String fileName);

    boolean fileNameExists(String fileName);

    void saveFileNames(List<String> fileNames);

    void save(String fileName, Properties properties);

}
