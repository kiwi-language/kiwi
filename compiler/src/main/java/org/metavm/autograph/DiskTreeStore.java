package org.metavm.autograph;

import org.metavm.entity.Tree;
import org.metavm.object.instance.TreeSource;
import org.metavm.object.instance.core.IInstanceContext;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.PhysicalId;
import org.metavm.object.type.TypeOrTypeKey;
import org.metavm.util.InstanceInput;
import org.metavm.util.InstanceOutput;
import org.metavm.util.NncUtils;
import org.metavm.util.StreamVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;

public class DiskTreeStore implements TreeSource {

    public static final Logger logger = LoggerFactory.getLogger(DiskTreeStore.class);

    private final String path;
    private Map<Long, Tree> trees = new HashMap<>();

    DiskTreeStore(String path) {
        this.path = path;
        loadFromDisk();
    }

    public Collection<Id> getAllInstanceIds() {
        var ids = new ArrayList<Id>();
        for (Tree tree : trees.values()) {
            new StreamVisitor(new ByteArrayInputStream(tree.data())) {

                @Override
                public void visitInstanceBody(long oldTreeId, long oldNodeId, boolean useOldId, long treeId, long nodeId, TypeOrTypeKey typeOrTypeKey) {
                    ids.add(PhysicalId.of(treeId, nodeId, typeOrTypeKey));
                    super.visitInstanceBody(oldTreeId, oldNodeId, useOldId, treeId, nodeId, typeOrTypeKey);
                }
            }.visitGrove();
        }
        return ids;
    }

    @Override
    public void save(List<Tree> trees) {
    }

    public void load(List<Tree> trees, List<Long> removedIds) {
        NncUtils.forEach(removedIds, this.trees::remove);
        for (Tree tree : trees) {
            this.trees.put(tree.id(), tree);
        }
    }

    public boolean contains(long treeId) {
        return trees.containsKey(treeId);
    }

    public int size() {
        return trees.size();
    }

    @Override
    public List<Tree> load(Collection<Long> ids, IInstanceContext context) {
        return NncUtils.mapAndFilter(ids, trees::get, Objects::nonNull);
    }

    @Override
    public void remove(List<Long> ids) {
    }

    public void persist() {
        try (var output = new InstanceOutput(new FileOutputStream(path))) {
            output.writeInt(trees.size());
            for (Tree tree : trees.values()) {
                output.writeInt(tree.data().length);
                output.write(tree.data());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void loadFromDisk() {
        var file = new File(path);
        if(file.exists()) {
            try (var input = new InstanceInput(new FileInputStream(file))) {
                int numTrees = input.readInt();
                var trees = new HashMap<Long, Tree>(numTrees);
                for (int i = 0; i < numTrees; i++) {
                    int len = input.readInt();
                    byte[] bytes = new byte[len];
                    input.read(bytes);
                    var subInput = new InstanceInput(new ByteArrayInputStream(bytes));
                    var ref = new Object() {
                        long version;
                        long treeId;
                        long nextNodeId;
                    };
                    new StreamVisitor(subInput) {
                        @Override
                        public void visitVersion(long version) {
                            ref.version = version;
                            super.visitVersion(version);
                        }

                        @Override
                        public long readTreeId() {
                            return ref.treeId = super.readTreeId();
                        }

                        @Override
                        public void visitNextNodeId(long nextNodeId) {
                            ref.nextNodeId = nextNodeId;
                            super.visitNextNodeId(nextNodeId);
                        }
                    }.visitGrove();
                    trees.put(ref.treeId, new Tree(ref.treeId, ref.version, ref.nextNodeId, bytes));
                }
                this.trees = trees;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
