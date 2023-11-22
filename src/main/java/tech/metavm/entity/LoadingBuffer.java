package tech.metavm.entity;

import tech.metavm.common.ErrorCode;
import tech.metavm.object.instance.IInstanceStore;
import tech.metavm.object.instance.cache.Cache;
import tech.metavm.object.instance.core.InstanceContext;
import tech.metavm.object.instance.persistence.InstancePO;
import tech.metavm.object.instance.persistence.Version;
import tech.metavm.util.*;

import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.*;

public class LoadingBuffer {

    private final List<Long> bufferedIds = new ArrayList<>();
    private final Set<Long> visited = new HashSet<>();
    private final InstanceContext context;
    private final IInstanceStore instanceStore;
    private final @Nullable Cache cache;
    private final Map<Long, Tree> invertedIndex = new HashMap<>();

    public LoadingBuffer(InstanceContext context, @Nullable Cache cache) {
        this.context = context;
        instanceStore = context.getInstanceStore();
        this.cache = cache;
    }

    public boolean buffer(long id) {
        if (visited.add(id)) {
            bufferedIds.add(id);
            return true;
        } else
            return false;
    }

    public Tree getTree(long id) {
        return NncUtils.requireNonNull(
                tryGetTree(id),
                () -> new BusinessException(ErrorCode.INSTANCE_NOT_FOUND, id)
        );
    }

    public Tree tryGetTree(long id) {
        var tree = invertedIndex.get(id);
        if (tree != null)
            return tree;
        buffer(id);
        flush();
        tree = invertedIndex.get(id);
        return tree;
    }

    private void flush() {
        if (bufferedIds.isEmpty())
            return;
        loadForest(bufferedIds);
        bufferedIds.clear();
    }

    private void loadForest(List<Long> ids) {
        List<Long> misses;
        if (cache != null) {
            var rootVersions = instanceStore.getRootVersions(ids, context);
            var bytes = cache.batchGet(NncUtils.map(rootVersions, Version::getId));
            misses = new ArrayList<>();
            for (int i = 0; i < bytes.size(); i++) {
                var tree = bytes.get(i);
                var v = rootVersions.get(i);
                if (tree != null) {
                    if (ByteUtils.readFirstLong(tree) == v.getVersion()) {
                        addTree(new Tree(v.getId(), v.getVersion(), tree));
                        continue;
                    }
                }
                misses.add(v.getId());
            }
        } else
            misses = ids;
        if (!misses.isEmpty()) {
            var instancePOs = new HashMap<Long, InstancePO>();
            for (InstancePO instancePO : instanceStore.loadForest(misses, context)) {
                instancePOs.put(instancePO.getId(), instancePO);
            }
            for (Long id : misses) {
                var root = instancePOs.get(id);
                if (root != null)
                    addTree(buildTree(root, instancePOs));
            }
            if (cache != null) {
                List<KeyValue<Long, byte[]>> entries = new ArrayList<>();
                for (Long id : misses) {
                    var tree = invertedIndex.get(id);
                    if (tree != null)
                        entries.add(new KeyValue<>(id, tree.data()));
                }
                cache.batchAdd(entries);
            }
        }
    }

    private void addTree(Tree tree) {
        new StreamVisitor(new ByteArrayInputStream(tree.data())) {

            @Override
            public void visitRecordBody(long id) {
                invertedIndex.put(id, tree);
                super.visitRecordBody(id);
            }

        }.visitMessage();

    }

    private Tree buildTree(InstancePO root, Map<Long, InstancePO> instancePOs) {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        new MessageWriter(bout, instancePOs).writeMessage(root);
        return new Tree(root.getId(), root.getVersion(), bout.toByteArray());
    }

    private static class MessageWriter extends InstanceOutput {

        private long parentId;
        private long parentFieldId;
        private final Map<Long, InstancePO> instancePOs;

        public MessageWriter(OutputStream outputStream, Map<Long, InstancePO> instancePOs) {
            super(outputStream);
            this.instancePOs = instancePOs;
        }

        public void writeMessage(InstancePO root) {
            writeLong(root.getVersion());
            writeInstancePO(root);
        }

        public void writeInstancePO(InstancePO instancePO) {
            long oldParentId = parentId;
            long oldParentFieldId = parentFieldId;
            parentId = instancePO.getId();
            parentFieldId = -1L;
            new StreamCopier(new ByteArrayInputStream(instancePO.getData()), this) {

                @Override
                public void visitField() {
                    writeLong(parentFieldId = readLong());
                    visit();
                }

                @Override
                public void visitReference() {
                    long id = readLong();
                    var instancePO = instancePOs.get(id);
                    if (instancePO != null
                            && instancePO.getParentId() == parentId
                            && instancePO.getParentFieldId() == parentFieldId)
                        writeInstancePO(instancePO);
                    else {
                        write(WireTypes.REFERENCE);
                        writeLong(id);
                    }
                }

            }.visit();
            parentId = oldParentId;
            parentFieldId = oldParentFieldId;
        }

    }

}
