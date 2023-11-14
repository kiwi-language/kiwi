package tech.metavm.util;

import org.jetbrains.annotations.NotNull;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Profiler {

    private final Entry root = new Entry("root", false);
    private final LinkedList<Entry> stack = new LinkedList<>();
    private boolean verbose;

    public Profiler() {
        stack.push(root);
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public boolean isVerbose() {
        return verbose;
    }

    @NotNull
    private Entry top() {
        //noinspection DataFlowIssue
        return stack.peek();
    }

    public Entry enter(String name) {
        //noinspection resource
        return enter(name, top().verbose);
    }

    public Entry enter(String name, boolean verbose) {
        var entry = new Entry(name, verbose);
        //noinspection resource
        top().addChild(entry);
        stack.push(entry);
        return entry;
    }

    public Result finish() {
        root.close();
        StringBuilder builder = new StringBuilder("Profiler result\n");
        root.print(builder, 1);
        return new Result(builder.toString(), root.duration());
    }

    public record Result(
            String output,
            long duration
    ) {
    }

    public class Entry implements Closeable {

        final String name;
        final long start;
        final boolean verbose;
        long end;
        final List<Entry> children = new ArrayList<>();
        final List<String> messages = new ArrayList<>();

        private Entry(String name, boolean verbose) {
            this.name = name;
            this.verbose = verbose;
            this.start = System.currentTimeMillis();
        }

        private void addChild(Entry child) {
            children.add(child);
        }

        public void addMessage(String name, Object value) {
            messages.add(String.format("%s: %s", name, value));
        }

        @Override
        public void close() {
            //noinspection resource
            stack.pop();
            end = System.currentTimeMillis();
        }

        long duration() {
            return this.end - this.start;
        }

        @Override
        public String toString() {
            String str = String.format("%s, duration: %d", name, duration());
            if (!messages.isEmpty()) {
                str += ", " + String.join(", ", messages);
            }
            return str;
        }

        public void print(StringBuilder buffer, int indent) {
            buffer.append("  ".repeat(Math.max(0, indent)));
            buffer.append(this).append('\n');
            for (Entry child : children) {
                child.print(buffer, indent + 1);
            }
        }

        public boolean isVerbose() {
            return verbose;
        }
    }

}
