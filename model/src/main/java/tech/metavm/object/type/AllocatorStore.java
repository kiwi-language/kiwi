package tech.metavm.object.type;

import java.util.List;
import java.util.Properties;

public interface AllocatorStore {

    String getFileName(String code);

    List<String> getFileNames();

    Properties load(String fileName);

    boolean fileNameExists(String fileName);

    void saveFileNames(List<String> fileNames);

    void save(String fileName, Properties properties);

}
