package tech.metavm.object.meta;

import java.util.List;
import java.util.Properties;

public interface AllocatorStore {

    String createFile(String code);

    List<String> getFileNames();

    Properties load(String fileName);

    void save(String fileName, Properties properties);

}
