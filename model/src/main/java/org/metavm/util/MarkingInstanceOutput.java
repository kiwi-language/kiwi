package org.metavm.util;

import org.metavm.object.instance.core.Value;

import javax.annotation.Nullable;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MarkingInstanceOutput extends InstanceOutput {

    private final List<Block> blocks = new ArrayList<>();
    private int lastValueEnd;
    private final ByteArrayOutputStream bout;
    private @Nullable Block lastBlock;
    private boolean defaultWriting;
    private int depth;


    public MarkingInstanceOutput() {
        this(new ByteArrayOutputStream());
    }

    private MarkingInstanceOutput(ByteArrayOutputStream bout) {
        super(bout);
        this.bout = bout;
    }

    @Override
    public void writeValue(Value value) {
        if(defaultWriting)
            super.writeValue(value);
        else {
            enterValue();
            super.writeValue(value);
            exitValue();
        }
    }

    @Override
    public void writeInstance(Value value) {
        if(defaultWriting)
            super.writeInstance(value);
        else {
            enterValue();
            super.writeInstance(value);
            exitValue();
        }
    }

    private void enterValue() {
        if (depth++ == 0) {
            insertBytesSectionIfRequired();
            if (lastBlock instanceof ValueBlock valueSection)
                valueSection.incrementCount();
            else
                addBlock(new ValueBlock());
        }
    }

    private void exitValue() {
        if (--depth == 0) lastValueEnd = size();
    }

    public void insertBytesSectionIfRequired() {
        var len = size() - lastValueEnd;
        if(len > 0)
            addBlock(new BytesBlock(len));
    }

    public byte[] toByteArray() {
        return bout.toByteArray();
    }

    public List<Block> getBlocks() {
        return Collections.unmodifiableList(blocks);
    }

    public int size() {
        return bout.size();
    }

    public void enterDefaultWriting() {
        insertBytesSectionIfRequired();
        addBlock(new DefaultWritingBlock());
        defaultWriting = true;
    }

    private void addBlock(Block block) {
        blocks.add(lastBlock = block);
    }

    public void exitingDefaultWriting() {
        lastValueEnd = size();
        defaultWriting = false;
    }

    public static abstract class Block {

        public static Block read(MvInput input) {
            var tag = input.read();
            return switch (tag) {
                case BytesBlock.TAG -> new BytesBlock(input.readInt());
                case ValueBlock.TAG_SINGLE -> new ValueBlock();
                case ValueBlock.TAG_MULTI -> new ValueBlock(input.readInt());
                case DefaultWritingBlock.TAG ->  new DefaultWritingBlock();
                default -> throw new IllegalStateException("Unrecognized section tag " + tag);
            };
        }

        public abstract int getTag();

        public abstract void write(MvOutput out);

        public abstract void visitBody(StreamVisitor visitor);

    }

    public static class BytesBlock extends Block {

        public static final int TAG = 0;

        private final int length;

        public BytesBlock(int length) {
            this.length = length;
        }

        @Override
        public int getTag() {
            return TAG;
        }

        @Override
        public void write(MvOutput out) {
            out.write(TAG);
            out.writeInt(length);
        }

        @Override
        public void visitBody(StreamVisitor visitor) {
            visitor.visitBytes(length);
        }
    }

    public static class ValueBlock extends Block {

        public static final int TAG_SINGLE = 1;
        public static final int TAG_MULTI = 2;

        private int count;

        public ValueBlock() {
            count = 1;
        }

        public ValueBlock(int count) {
            this.count = count;
        }

        @Override
        public int getTag() {
            return TAG_MULTI;
        }

        @Override
        public void write(MvOutput out) {
            if(count == 1)
                out.write(TAG_SINGLE);
            else {
                out.write(TAG_MULTI);
                out.writeInt(count);
            }
        }

        @Override
        public void visitBody(StreamVisitor visitor) {
            for (int i = 0; i < count; i++) {
                visitor.visitValue();
            }
        }

        private void incrementCount() {
            count++;
        }

    }

    public static class DefaultWritingBlock extends Block {

        public static final int TAG = 3;

        @Override
        public int getTag() {
            return TAG;
        }

        @Override
        public void write(MvOutput out) {
            out.write(TAG);
        }

        @Override
        public void visitBody(StreamVisitor visitor) {
            visitor.visitClassBody();
        }
    }

}
