package tech.metavm.autograph;

import tech.metavm.entity.IndexOperator;
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
    private Map<Key, Long> indexMap = new HashMap<>();

    LocalIndex(String path) {
        this.path = path;
        load();
    }

    public void save() {
        try (var output = new FileOutputStream(path)) {
            var instOutput = new InstanceOutput(output);
            instOutput.writeInt(indexMap.size());
            indexMap.forEach((key, id) -> {
                instOutput.writeLong(key.constraintId);
                for (int i = 0; i < NUM_COLS; i++) {
                    var bytes = key.bytes[i];
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
            var indexMap = new HashMap<Key, Long>(size);
            for (int i = 0; i < size; i++) {
                long indexId = instInput.readLong();
                byte[][] bytes = new byte[NUM_COLS][];
                for (int j = 0; j < NUM_COLS; j++) {
                    int n = instInput.readInt();
                    bytes[j] = new byte[n];
                    input.read(bytes[j]);
                }
                long id = instInput.readLong();
                var key = new Key(indexId, bytes);
                indexMap.put(key, id);
            }
            this.indexMap = indexMap;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void reset(Map<Key, Long> indexMap) {
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

    public record Query(long indexId, List<QueryItem> items, boolean desc, Long limit) {

        boolean match(Key key) {
            if(indexId != key.constraintId)
                return false;
            int i = 0;
            for (var item : items) {
                if(!item.operator.evaluate(key.bytes[i++], item.value))
                    return false;
            }
            return true;
        }

    }

    public record QueryItem(IndexOperator operator, byte[] value) {
    }

    public record QueryResult(List<Long> ids, long total) {}

    public record Key(long constraintId, byte[][] bytes) {

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            var that = (Key) obj;
            return this.constraintId == that.constraintId &&
                    Arrays.deepEquals(this.bytes, that.bytes);
        }

        @Override
        public int hashCode() {
            return Objects.hash(constraintId, Arrays.deepHashCode(bytes));
        }

        @Override
        public String toString() {
            return "Key[" +
                    "constraintId=" + constraintId + ", " +
                    "bytes=" + Arrays.deepToString(bytes) + ']';
        }

    }

}
