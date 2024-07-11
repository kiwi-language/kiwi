package org.metavm.util;

import org.metavm.entity.TreeTags;

import java.io.InputStream;

public class MigrationTreeVisitor {

    private final InstanceInput input;

    public MigrationTreeVisitor(InputStream in) {
        this(new InstanceInput(in));
    }

    public MigrationTreeVisitor(InstanceInput input) {
        this.input = input;
    }

    public void visit() {
        var tag = input.read();
        assert tag == TreeTags.MIGRATED;
        visitTargetTreeId(input.readLong());
        int numForwards = input.readInt();
        for (int i = 0; i < numForwards; i++) {
            visitForwardingPointer(input.readLong(), input.readLong());
        }
    }

    public void visitTargetTreeId(long treeId) {

    }

    public void visitForwardingPointer(long sourceNodeId, long targetNodeId) {

    }

}
