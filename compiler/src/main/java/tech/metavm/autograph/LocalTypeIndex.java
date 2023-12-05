package tech.metavm.autograph;

import tech.metavm.util.InstanceInput;
import tech.metavm.util.InstanceOutput;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

public class LocalTypeIndex {

//    private static final String PATH = CompilerConstants.INDEX_DIR + File.separator + "type_index";

//    public static LocalTypeIndex INSTANCE = new LocalTypeIndex(PATH);
    private final String path;
    private Map<Long, List<Long>> index;

    LocalTypeIndex(String path) {
        this.path = path;
        load();
    }

    public void reset(Map<Long, List<Long>> index) {
        this.index = new HashMap<>(index);
        save();
    }

    public void save() {
        try (var output = new InstanceOutput(new FileOutputStream(path))) {
            output.writeInt(index.size());
            index.forEach((typeId, ids) -> {
                output.writeLong(typeId);
                output.writeInt(ids.size());
                ids.forEach(output::writeLong);
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void load() {
        var indexFile = new File(path);
        if(!indexFile.exists())
            return;
        try (var input = new InstanceInput(new FileInputStream(indexFile))) {
            int size = input.readInt();
            var index = new HashMap<Long, List<Long>>(size);
            for (int i = 0; i < size; i++) {
                var typeId = input.readLong();
                int numIds = input.readInt();
                var ids = new ArrayList<Long>(numIds);
                for (int j = 0; j < numIds; j++) {
                    ids.add(input.readLong());
                }
                index.put(typeId, ids);
            }
            this.index = index;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Long> query(long typeId, long startId, long limit) {
        var ids = index.getOrDefault(typeId, List.of());
        var index = Collections.binarySearch(ids, startId);
        if (index < 0)
            index = -(index + 1);
        if (index < ids.size())
            return ids.subList(index, Math.min(ids.size(), index + (int) limit));
        else
            return List.of();
    }

}
