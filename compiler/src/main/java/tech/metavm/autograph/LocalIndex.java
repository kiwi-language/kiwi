package tech.metavm.autograph;

import tech.metavm.entity.IndexOperator;
import tech.metavm.object.instance.persistence.IndexKeyPO;
import tech.metavm.util.InstanceInput;
import tech.metavm.util.InstanceOutput;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

public class LocalIndex {

//    private static final String PATH = CompilerConstants.INDEX_DIR + File.separator + "index";

//    public static final LocalIndex INSTANCE = new LocalIndex(PATH);

    public static final int NUM_COLS = 5;
    private final String path;
    private Map<IndexKeyPO, Long> indexMap = new HashMap<>();

    LocalIndex(String path) {
        this.path = path;
        load();
    }

    public void save() {
        try (var output = new FileOutputStream(path)) {
            var instOutput = new InstanceOutput(output);
            instOutput.writeInt(indexMap.size());
            indexMap.forEach((key, id) -> {
                instOutput.writeLong(key.getIndexId());
                for (int i = 0; i < NUM_COLS; i++) {
                    var bytes = key.getColumn(i);
                    instOutput.writeInt(bytes.length);
                    instOutput.write(bytes);
                }
                instOutput.writeLong(id);
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void load() {
        var indexFile = new File(path);
        if(!indexFile.exists())
            return;
        try (var input = new FileInputStream(path)) {
            var instInput = new InstanceInput(input);
            int size = instInput.readInt();
            var indexMap = new HashMap<IndexKeyPO, Long>(size);
            for (int i = 0; i < size; i++) {
                var key = new IndexKeyPO();
                key.setIndexId(instInput.readLong());
                for (int j = 0; j < NUM_COLS; j++) {
                    int n = instInput.readInt();
                    var bytes = new byte[n];
                    input.read(bytes);
                    key.setColumn(j, bytes);
                }
                long id = instInput.readLong();
                indexMap.put(key, id);
            }
            this.indexMap = indexMap;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void reset(Map<IndexKeyPO, Long> indexMap) {
        this.indexMap = new HashMap<>(indexMap);
        save();
    }

    public QueryResult query(Query query) {
        List<Long> ids = new ArrayList<>();
        this.indexMap.forEach((key, id) -> {
            if(query.match(key))
                ids.add(id);
        });
        if(query.desc)
            ids.sort((id1, id2) -> Long.compare(id2, id1));
        else
            Collections.sort(ids);
        Long last = null;
        var result = new ArrayList<Long>();
        for (Long l : ids) {
            if(!l.equals(last)) {
                result.add(l);
                last = l;
            }
        }
        return new QueryResult(
                result.subList(0, Math.min(result.size(), query.limit != null ? query.limit.intValue() : Integer.MAX_VALUE)),
                result.size()
        );
    }

    public long count(IndexKeyPO from, IndexKeyPO to) {
        if(from.getIndexId() != to.getIndexId())
            throw new RuntimeException("Can not count keys from different indexes");
        long count = 0;
        for (IndexKeyPO key : indexMap.keySet()) {
            if(key.getIndexId() == from.getIndexId() && key.compareTo(from) >= 0 && key.compareTo(to) <= 0)
                count++;
        }
        return count;
    }

    public List<Long> scan(IndexKeyPO from, IndexKeyPO to) {
        if(from.getIndexId() != to.getIndexId())
            throw new RuntimeException("Can not scan keys from different indexes");
        List<Long> ids = new ArrayList<>();
        for (var e : indexMap.entrySet()) {
            var key = e.getKey();
            if(key.getIndexId() == from.getIndexId() && key.compareTo(from) >= 0 && key.compareTo(to) <= 0)
                ids.add(e.getValue());
        }
        return ids;
    }

    public record Query(long indexId, List<QueryItem> items, boolean desc, Long limit) {

        boolean match(IndexKeyPO key) {
            if(indexId != key.getIndexId())
                return false;
            int i = 0;
            for (var item : items) {
                if(!item.operator.evaluate(key.getColumn(i++), item.value))
                    return false;
            }
            return true;
        }

    }

    public record QueryItem(IndexOperator operator, byte[] value) {
    }

    public record QueryResult(List<Long> ids, long total) {}

}
