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

import java.io.*;
import java.util.*;

public class DiskTreeStore implements TreeSource {

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
        for (Tree tree : trees) {
            this.trees.put(tree.id(), tree);
        }
    }

    @Override
    public List<Tree> load(Collection<Long> ids, IInstanceContext context) {
        return NncUtils.mapAndFilter(ids, trees::get, Objects::nonNull);
    }

    @Override
    public void remove(List<Long> ids) {
        NncUtils.forEach(ids, trees::remove);
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
                    var version = subInput.readLong();
                    var treeId = subInput.readTreeId();
                    var nextNodeId = subInput.readInt();
                    trees.put(treeId, new Tree(treeId, version, nextNodeId, bytes));
                }
                this.trees = trees;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
