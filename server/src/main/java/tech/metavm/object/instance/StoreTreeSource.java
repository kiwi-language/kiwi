package tech.metavm.object.instance;

import tech.metavm.entity.Tree;
import tech.metavm.object.instance.core.IInstanceContext;
import tech.metavm.object.instance.persistence.InstancePO;
import tech.metavm.util.InstanceOutput;
import tech.metavm.util.StreamCopier;
import tech.metavm.util.WireTypes;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.*;

public class StoreTreeSource implements TreeSource {

    private final IInstanceStore instanceStore;

    public StoreTreeSource(IInstanceStore instanceStore) {
        this.instanceStore = instanceStore;
    }

    @Override
    public void save(List<Tree> trees) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Tree> load(Collection<Long> ids, IInstanceContext context) {
        var instancePOs = new HashMap<Long, InstancePO>();
        for (InstancePO instancePO : instanceStore.loadForest(ids, context)) {
            instancePOs.put(instancePO.getId(), instancePO);
        }
        List<Tree> trees = new ArrayList<>();
        for (Long id : ids) {
            var root = instancePOs.get(id);
            if (root != null)
                trees.add(buildTree(root, instancePOs));
        }
        return trees;
    }

    @Override
    public void remove(List<Long> ids) {
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
