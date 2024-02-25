package tech.metavm.entity;

import org.springframework.stereotype.Component;
import tech.metavm.system.persistence.FileMapper;
import tech.metavm.system.persistence.FilePO;
import tech.metavm.util.InstanceInput;
import tech.metavm.util.InstanceOutput;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

@Component
public class DatabaseStdIdStore implements StdIdStore {

    public static final String FILE_NAME = "std_ids";

    private final FileMapper fileMapper;

    public DatabaseStdIdStore(FileMapper fileMapper) {
        this.fileMapper = fileMapper;
    }

    @Override
    public void save(Map<String, Long> ids) {
        fileMapper.save(buildFile(ids));
    }

    @Override
    public Map<String, Long> load() {
        var file = fileMapper.selectByName(FILE_NAME);
        if(file != null)
            return buildIds(file.getContent());
        else
            return Map.of();
    }

    private Map<String, Long> buildIds(byte[] content) {
        var input = new InstanceInput(new ByteArrayInputStream(content));
        var size = input.readInt();
        var ids = new HashMap<String, Long>(size);
        for (int i = 0; i < size; i++) {
            var key = input.readString();
            var value = input.readLong();
            ids.put(key, value);
        }
        return ids;
    }

    private FilePO buildFile(Map<String, Long> ids) {
        var bout = new ByteArrayOutputStream();
        var output = new InstanceOutput(bout);
        output.writeInt(ids.size());
        for (var entry : ids.entrySet()) {
            output.writeString(entry.getKey());
            output.writeLong(entry.getValue());
        }
        return new FilePO(FILE_NAME, bout.toByteArray());
    }

}
