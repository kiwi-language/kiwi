package tech.metavm.autograph;

import tech.metavm.entity.Tree;
import tech.metavm.object.instance.TreeSource;
import tech.metavm.object.instance.core.IInstanceContext;
import tech.metavm.object.instance.core.Id;
import tech.metavm.util.InstanceInput;
import tech.metavm.util.InstanceOutput;
import tech.metavm.util.NncUtils;
import tech.metavm.util.StreamVisitor;

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
                public void visitRecordBody(Id id) {
                    ids.add(id);
                    super.visitRecordBody(id);
                }
            }.visitMessage();
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
