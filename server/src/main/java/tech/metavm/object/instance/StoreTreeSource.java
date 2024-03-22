package tech.metavm.object.instance;

import tech.metavm.entity.Tree;
import tech.metavm.object.instance.core.IInstanceContext;
import tech.metavm.object.instance.core.Id;
import tech.metavm.object.instance.persistence.InstancePO;
import tech.metavm.util.InstanceOutput;
import tech.metavm.util.NncUtils;
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
    public List<Tree> load(Collection<Id> ids, IInstanceContext context) {
        var instancePOs = new HashMap<Id, InstancePO>();
        for (InstancePO instancePO : instanceStore.loadForest(ids, context)) {
            instancePOs.put(instancePO.getInstanceId(), instancePO);
        }
        List<Tree> trees = new ArrayList<>();
        for (var id : ids) {
            var root = instancePOs.get(id);
            if (root != null)
                trees.add(buildTree(root, instancePOs));
        }
        return trees;
    }

    @Override
    public void remove(List<Id> ids) {
    }

    private Tree buildTree(InstancePO root, Map<Id, InstancePO> instancePOs) {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        new MessageWriter(bout, instancePOs).writeMessage(root);
        return new Tree(root.getInstanceId(), root.getVersion(), bout.toByteArray());
    }

    private static class MessageWriter extends InstanceOutput {

        private Id parentId;
        private Id parentFieldId;
        private final Map<Id, InstancePO> instancePOs;

        public MessageWriter(OutputStream outputStream, Map<Id, InstancePO> instancePOs) {
            super(outputStream);
            this.instancePOs = instancePOs;
        }

        public void writeMessage(InstancePO root) {
            writeLong(root.getVersion());
            writeInstancePO(root);
        }

        public void writeInstancePO(InstancePO instancePO) {
            var oldParentId = parentId;
            var oldParentFieldId = parentFieldId;
            parentId = instancePO.getInstanceId();
            parentFieldId = null;
            new StreamCopier(new ByteArrayInputStream(instancePO.getData()), this) {

                @Override
                public void visitField() {
                    writeId(parentFieldId = readId());
                    visit();
                }

                @Override
                public void visitReference() {
                    var id = readId();
                    var instancePO = instancePOs.get(id);
                    if (instancePO != null
                            && NncUtils.bytesEquals(instancePO.getParentId(), NncUtils.get(parentId, Id::toBytes))
                            && NncUtils.bytesEquals(instancePO.getParentFieldId(), NncUtils.get(parentFieldId, Id::toBytes)))
                        writeInstancePO(instancePO);
                    else {
                        write(WireTypes.REFERENCE);
                        writeId(id);
                    }
                }

            }.visit();
            parentId = oldParentId;
            parentFieldId = oldParentFieldId;
        }

    }
}
