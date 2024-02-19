package tech.metavm.object.type;

import tech.metavm.util.InternalException;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

public class DirectoryAllocatorStore implements AllocatorStore {

    private static final String ID_FILE_DIR = "/id";

    private static final Pattern ID_FILE_NAME_PATTERN = Pattern.compile(".+\\.properties");

    private final String saveDir;

    public DirectoryAllocatorStore(String saveDir) {
        this.saveDir = saveDir;
    }

    @Override
    public String getFileName(String code) {
        return ID_FILE_DIR + "/" + code + ".properties";
    }

    public List<String> getFileNames() {
        List<String> fileNames = new ArrayList<>();
        try (InputStream input = StdAllocators.class.getResourceAsStream(ID_FILE_DIR + "/manifest")) {
            if (input == null) {
                return fileNames;
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            String line;
            while ((line = reader.readLine()) != null) {
                fileNames.add(line);
            }
            return fileNames;
        } catch (IOException e) {
            throw new InternalException("Fail to read id files", e);
        }
    }

    @Override
    public Properties load(String fileName) {
        Properties properties = new Properties();
        try (InputStream input = StdAllocator.class.getResourceAsStream(fileName)) {
            properties.load(input);
            return properties;
        } catch (IOException e) {
            throw new InternalException("fail to load id properties file: " + fileName);
        }
    }

    @Override
    public void saveFileNames(List<String> fileNames) {
        String manifestFile = saveDir + "/id/manifest";
        try (OutputStream out = new FileOutputStream(manifestFile)) {
            for (String fileName : fileNames) {
                out.write((fileName + "\n").getBytes());
            }
        } catch (IOException e) {
            throw new InternalException("Fail to save id file names", e);
        }
    }

    @Override
    public void save(String fileName, Properties properties) {
        String filePath = saveDir + fileName;
        try (OutputStream out = new FileOutputStream(filePath)) {
            properties.store(out, null);
        } catch (IOException e) {
            throw new InternalException("Fail to save properties to file: " + fileName, e);
        }
    }

    @Override
    public boolean fileNameExists(String fileName) {
        try (InputStream inputStream = StdAllocator.class.getResourceAsStream(fileName)) {
            return inputStream != null;
        } catch (IOException e) {
            throw new InternalException(e);
        }
    }

}
