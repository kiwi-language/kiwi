package tech.metavm.object.meta;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tech.metavm.util.InternalException;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class DirectoryAllocatorStore implements AllocatorStore {

    private static final String ID_FILE_DIR = "/id";

    private static final Pattern ID_FILE_NAME_PTN = Pattern.compile("([^.\\s]+)\\.properties");

    private final String saveDir;

    public DirectoryAllocatorStore(@Value("${metavm.resource-cp-root}") String saveDir) {
        this.saveDir = saveDir;
    }

    @Override
    public String createFile(String code) {
        return ID_FILE_DIR + "/" + code + ".properties";
    }

    public List<String> getFileNames() {
        List<String> fileNames = new ArrayList<>();
        try(InputStream input = StdAllocators.class.getResourceAsStream(ID_FILE_DIR)) {
            if(input != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                String line;
                while ((line = reader.readLine()) != null) {
                    Matcher matcher = ID_FILE_NAME_PTN.matcher(line);
                    if (matcher.matches()) {
                        fileNames.add( ID_FILE_DIR + "/" + line);
                    }
                }
            }
            return fileNames;
        } catch (IOException e) {
            throw new InternalException("Fail to read id files", e);
        }
    }

    @Override
    public Properties load(String fileName) {
        Properties properties = new Properties();
        try(InputStream input = StdAllocator.class.getResourceAsStream(fileName)) {
            properties.load(input);
            return properties;
        }
        catch (IOException e) {
            throw new InternalException("fail to load id properties file: " + fileName);
        }
    }

    @Override
    public void save(String fileName, Properties properties) {
        String filePath = saveDir + fileName;
        try(OutputStream out = new FileOutputStream(filePath)) {
            properties.store(out, null);
        }
        catch (IOException e) {
            throw new InternalException("Fail to save properties to file: " + fileName);
        }
    }
}
