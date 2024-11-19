package org.metavm.entity;

import org.metavm.object.instance.core.Id;
import org.metavm.system.persistence.FileMapper;
import org.metavm.system.persistence.FilePO;
import org.metavm.util.InstanceInput;
import org.metavm.util.InstanceOutput;
import org.springframework.stereotype.Component;

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
    public void save(Map<String, Id> ids) {
        fileMapper.save(buildFile(ids));
    }

    @Override
    public Map<String, Id> load() {
        var file = fileMapper.selectByName(FILE_NAME);
        if(file != null)
            return buildIds(file.getContent());
        else
            return Map.of();
    }

    public static Map<String, Id> buildIds(byte[] content) {
        var input = new InstanceInput(new ByteArrayInputStream(content));
        var size = input.readInt();
        var ids = new HashMap<String, Id>(size);
        for (int i = 0; i < size; i++) {
            var key = input.readUTF();
            var value = input.readId();
            ids.put(key, value);
        }
        return ids;
    }

    public static FilePO buildFile(Map<String, Id> ids) {
        var bout = new ByteArrayOutputStream();
        var output = new InstanceOutput(bout);
        output.writeInt(ids.size());
        for (var entry : ids.entrySet()) {
            output.writeUTF(entry.getKey());
            output.writeId(entry.getValue());
        }
        return new FilePO(FILE_NAME, bout.toByteArray());
    }

}
