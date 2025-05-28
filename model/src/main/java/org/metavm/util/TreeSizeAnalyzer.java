package org.metavm.util;

import org.metavm.object.instance.core.IInstanceContext;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.PhysicalId;
import org.metavm.object.type.TypeOrTypeKey;
import org.metavm.object.type.rest.dto.TypeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TreeSizeAnalyzer extends StreamCopier {

    public static final Logger logger = LoggerFactory.getLogger(TreeSizeAnalyzer.class);

    private final ByteArrayOutputStream bout;
    private Entry entry;
    private Entry root;
    private int sizeBeforeRecord;
    private Long treeId;
    private Long nodeId;
    private int totalHeadSize;
    private int numReferences;
    private int totalReferenceSize;

    public TreeSizeAnalyzer(InputStream in) {
        super(in, new ByteArrayOutputStream());
        bout = (ByteArrayOutputStream) getOutput().getOut();
    }

    @Override
    public void visitValue() {
        sizeBeforeRecord = bout.size();
        super.visitValue();
    }

    @Override
    public void visitValueInstance() {
        treeId = null;
        nodeId = null;
        super.visitValueInstance();
    }

    @Override
    public void visitInstanceBody(long treeId, long nodeId, TypeOrTypeKey typeOrTypeKey, int refcount) {
        this.treeId = treeId;
        this.nodeId = nodeId;
        super.visitInstanceBody(treeId, nodeId, typeOrTypeKey, refcount);
    }

    @Override
    public void visitBody(TypeOrTypeKey typeOrTypeKey) {
        var id = treeId != null ? PhysicalId.of(treeId, nodeId) : null;
        var sizeBefore = bout.size();
        var headSize = sizeBefore - sizeBeforeRecord;
        totalHeadSize += headSize;
        entry = new Entry(id, (TypeKey) typeOrTypeKey, headSize, entry);
        if (entry.parent == null)
            root = entry;
        super.visitBody(typeOrTypeKey);
        entry.setBodySize(bout.size() - sizeBefore);
        entry = entry.parent;
    }

    @Override
    public void visitReference() {
        var beforeSize = bout.size();
        super.visitReference();
        totalReferenceSize += bout.size() - beforeSize;
        numReferences++;
    }

    public void printResult(IInstanceContext context) {
        var root = Objects.requireNonNull(this.root);
        logger.info("Tree size statistics, total size: {}, total head size: {}, number references:{}, total reference size: {}\n{}",
                root.retainedSize, totalHeadSize, numReferences, totalReferenceSize, root.toString(context));
    }

    private static class Entry {
        final @Nullable Id id;
        final TypeKey typeKey;
        final Entry parent;
        final List<Entry> children = new ArrayList<>();
        int headSize;
        int selfSize;
        int retainedSize;

        private Entry(@Nullable Id id, TypeKey typeKey, int headSize, Entry parent) {
            this.id = id;
            this.headSize = headSize;
            this.typeKey = typeKey;
            this.parent = parent;
            if (parent != null)
                parent.children.add(this);
        }

        void setBodySize(int bodySize) {
            this.retainedSize = headSize + bodySize;
            this.selfSize = headSize + bodySize - children.stream().mapToInt(c -> c.retainedSize).sum();
        }

        public String toString(IInstanceContext context) {
            var printer = new EntryPrinter();
            write(printer, context);
            return printer.toString();
        }

        private void write(EntryPrinter printer, IInstanceContext context) {
//            if(selfSize > 100) {
                var type = typeKey.toType(context.getTypeDefProvider());
                printer.writeLine(String.format("id: %s, type: %s, head size: %d, self size: %d, retained size: %d",
                        id, type.getTypeDesc(), headSize, selfSize, retainedSize));
//            }
            printer.indent();
            for (Entry child : children) {
                child.write(printer, context);
            }
            printer.deIndent();
        }

    }

    private static class EntryPrinter {
        private final StringBuilder stringBuilder = new StringBuilder();
        private int indent;

        public void indent() {
            indent++;
        }

        public void deIndent() {
            indent--;
        }

        public void writeLine(String line) {
            stringBuilder.append("  ".repeat(Math.max(0, indent)));
            stringBuilder.append(line).append('\n');
        }

        public String toString() {
            return stringBuilder.toString();
        }

    }
}
