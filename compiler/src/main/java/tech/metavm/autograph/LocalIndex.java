package tech.metavm.autograph;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.metavm.entity.IndexOperator;
import tech.metavm.object.instance.core.Id;
import tech.metavm.object.instance.persistence.IndexEntryPO;
import tech.metavm.object.instance.persistence.IndexKeyPO;
import tech.metavm.util.InstanceInput;
import tech.metavm.util.InstanceOutput;
import tech.metavm.util.NncUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class LocalIndex {

    public static final Logger logger = LoggerFactory.getLogger(LocalIndex.class);
    private static final byte[] MIN_ID = new byte[0];
    private static final byte[] MAX_ID = new byte[]{
            -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1
    };

    private final String path;
    private NavigableSet<IndexEntryPO> indexMap = new TreeSet<>();
    private final long appId;

    LocalIndex(long appId, String path) {
        this.appId = appId;
        this.path = path;
        load();
    }

    public void save() {
        try (var output = new FileOutputStream(path)) {
            var instOutput = new InstanceOutput(output);
            instOutput.writeInt(indexMap.size());
            indexMap.forEach(entry -> {
                var key = entry.getKey();
                instOutput.writeId(Id.fromBytes(key.getIndexId()));
                for (int i = 0; i < IndexKeyPO.MAX_KEY_COLUMNS; i++) {
                    var bytes = key.getColumn(i);
                    instOutput.writeInt(bytes.length);
                    instOutput.write(bytes);
                }
                instOutput.writeId(entry.getId());
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void load() {
        var indexFile = new File(path);
        if (!indexFile.exists())
            return;
        try (var input = new FileInputStream(path)) {
            var instInput = new InstanceInput(input);
            int size = instInput.readInt();
            var indexMap = new TreeSet<IndexEntryPO>();
            for (int i = 0; i < size; i++) {
                var key = new IndexKeyPO();
                key.setIndexId(instInput.readId().toBytes());
                for (int j = 0; j < IndexKeyPO.MAX_KEY_COLUMNS; j++) {
                    int n = instInput.readInt();
                    var bytes = new byte[n];
                    //noinspection ResultOfMethodCallIgnored
                    input.read(bytes);
                    key.setColumn(j, bytes);
                }
                var id = instInput.readId();
                indexMap.add(new IndexEntryPO(appId, key, id.toBytes()));
            }
            this.indexMap = indexMap;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void reset(Map<IndexKeyPO, String> indexMap) {
        this.indexMap = new TreeSet<>();
        indexMap.forEach((key, id) -> this.indexMap.add(new IndexEntryPO(appId, key, Id.parse(id).toBytes())));
        save();
    }

    public QueryResult query(Query query) {
        var entries = indexMap.subSet(new IndexEntryPO(appId, query.from, MIN_ID), true,
                new IndexEntryPO(appId, query.to, MAX_ID), true);
        var total = entries.stream().map(IndexEntryPO::getId).distinct().count();
        var pageIds = entries.stream()
                .map(IndexEntryPO::getId)
                .sorted(query.desc ? Collections.reverseOrder() : Comparator.naturalOrder())
                .distinct()
                .limit(NncUtils.orElse(query.limit, Long.MAX_VALUE))
                .map(Id::toString)
                .collect(Collectors.toList());
        return new QueryResult(pageIds, total);
    }

    public long count(IndexKeyPO from, IndexKeyPO to) {
        if (from.getIndexId() != to.getIndexId())
            throw new RuntimeException("Can not count keys from different indexes");
        return query(from, to).stream().distinct().count();
    }

    public List<String> scan(IndexKeyPO from, IndexKeyPO to) {
        if (from.getIndexId() != to.getIndexId())
            throw new RuntimeException("Can not scan keys from different indexes");
        return query(from, to).stream()
                .sorted()
                .distinct()
                .map(e -> e.getId().toString())
                .collect(Collectors.toList());
    }

    private Collection<IndexEntryPO> query(IndexKeyPO from, IndexKeyPO to) {
        return this.indexMap.subSet(new IndexEntryPO(appId, from, MIN_ID), true,
                new IndexEntryPO(appId, to, MAX_ID), true);
    }

    public record Query(Id indexId, IndexKeyPO from, IndexKeyPO to, boolean desc, Long limit) {
    }

    public record QueryItem(IndexOperator operator, byte[] value) {
    }

    public record QueryResult(List<String> ids, long total) {
    }

}
