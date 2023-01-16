package tech.metavm.object.meta;

import java.util.List;
import java.util.Properties;

public interface AllocatorStore {

    String getFileName(String code);

    List<String> getFileNames();

    Properties load(String fileName);

    boolean fileNameExists(String fileName);

    void save(String fileName, Properties properties);

}
