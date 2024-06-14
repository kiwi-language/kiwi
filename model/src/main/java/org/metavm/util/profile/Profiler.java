package org.metavm.util.profile;

import org.jetbrains.annotations.NotNull;

import java.io.Closeable;
import java.util.*;
import java.util.function.Consumer;

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
        return finish(true, false);
    }

    public Result finish(boolean withTraces, boolean withHisto) {
        root.close();
        StringBuilder builder = new StringBuilder("Profiler result\n");
        builder.append("Total duration: ").append(root.duration()).append(" us\n");
        if (withTraces)
            root.print(builder, 1, root.start, root.duration());
        if (withHisto)
            buildHisto(root.duration()).print(builder);
        return new Result(builder.toString(), root.duration());
    }

    private Histo buildHisto(long totalDuration) {
        Map<String, HistoEntryBuilder> builderMap = new HashMap<>();
        forEachEntry(entry -> {
            HistoEntryBuilder builder = builderMap.computeIfAbsent(entry.name, HistoEntryBuilder::new);
            builder.add(entry.selfTime());
        });
        List<HistoEntry> entries = new ArrayList<>();
        builderMap.forEach((name, builder) -> entries.add(builder.build(totalDuration)));
        entries.sort(Comparator.comparingLong(HistoEntry::totalDuration).reversed());
        return new Histo(entries);
    }

    private void forEachEntry(Consumer<Entry> consumer) {
        forEachEntry(root, consumer);
    }

    private void forEachEntry(Entry entry, Consumer<Entry> consumer) {
        consumer.accept(entry);
        for (Entry child : entry.children) {
            forEachEntry(child, consumer);
        }
    }

    public record Result(
            String output,
            long duration
    ) {
    }

    private record Histo(
            List<HistoEntry> entries
    ) {

        public void print(StringBuilder buf) {
            buf.append("Histo\n");
            for (HistoEntry entry : entries) {
                buf.append(entry.name).append(": ").append(entry.count).append(", ").append(entry.totalDuration)
                        .append(", ").append(entry.getFormattedPercent()).append('%').append('\n');
            }
        }

    }

    private static class HistoEntryBuilder {
        private final String name;
        private long count;
        private long durable;

        private HistoEntryBuilder(String name) {
            this.name = name;
        }

        public void add(long duration) {
            count++;
            durable += duration;
        }

        public HistoEntry build(long totalDurable) {
            return new HistoEntry(name, count, durable, (double) durable / totalDurable);
        }
    }

    private record HistoEntry(
            String name,
            long count,
            long totalDuration,
            double percent
    ) {

        public String getFormattedPercent() {
            return String.format("%.2f", percent * 100);
        }

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
            this.start = System.nanoTime();
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
            end = System.nanoTime();
        }

        long duration() {
            return (this.end - this.start) / 1000;
        }

        long selfTime() {
            long selfTime = duration();
            for (Entry child : children) {
                selfTime -= child.duration();
            }
            return selfTime;
        }

        public String getOutput(long start, long totalDuration) {
            String str = String.format("%s, start: %d, duration: %d, percent: %.2f", name,
                    (this.start - start) / 1000L, duration(),
                    (double) duration() / totalDuration * 100) + "%";
            if (!messages.isEmpty()) {
                str += ", " + String.join(", ", messages);
            }
            return str;
        }

        public void print(StringBuilder buffer, int indent, long start, long totalDuration) {
            buffer.append("  ".repeat(Math.max(0, indent)));
            buffer.append(getOutput(start, totalDuration)).append('\n');
            for (Entry child : children) {
                child.print(buffer, indent + 1, start, totalDuration);
            }
        }

        public boolean isVerbose() {
            return verbose;
        }
    }

}
