package tech.metavm.autograph;

import tech.metavm.util.NncUtils;

import java.util.*;

class NameTracker {

    private final LinkedList<MethodInfo> methodInfoStack = new LinkedList<>();

    void addName(String name) {
        currentMethodInfo().addName(name);
    }

    void enterBlock() {
        currentMethodInfo().enterBlock();
    }

    void exitBlock() {
        currentMethodInfo().exitBlock();
    }

    void rename(String from, String to) {
        currentMethodInfo().rename(from, to);
    }

    boolean isVisible(String name) {
        return currentMethodInfo().isVisible(name);
    }

    String getMappedName(String name) {
        return currentMethodInfo().getMappedName(name);
    }

    String nextName(String name) {
        return currentMethodInfo().nextName(name);
    }

    void enterMethod() {
        methodInfoStack.push(new MethodInfo());
        enterBlock();
    }

    void exitMethod() {
        exitBlock();
        methodInfoStack.pop();
    }

    private MethodInfo currentMethodInfo() {
        return NncUtils.requireNonNull(methodInfoStack.peek());
    }

    private static class MethodInfo {
        private final Set<String> names = new HashSet<>();
        private BlockInfo blockInfo;

        String nextName(String prefix) {
            String name = prefix;
            int num = 1;
            while (names.contains(name)) {
                name = prefix + "_" + num++;
            }
            addName(name);
            return name;
        }

        void rename(String from, String to) {
            currentBlock().nameMap.put(from, to);
        }

        private BlockInfo currentBlock() {
            return NncUtils.requireNonNull(blockInfo);
        }

        String getMappedName(String name) {
            return currentBlock().getMappedName(name);
        }

        void addName(String name) {
            currentBlock().names.add(name);
            names.add(name);
        }

        public boolean isVisible(String name) {
            return names.contains(name);
        }

        public void enterBlock() {
            blockInfo = new BlockInfo(blockInfo);
        }

        public void exitBlock() {
            var block = currentBlock();
            names.removeAll(block.names);
            blockInfo = block.parent;
        }
    }

    private static class BlockInfo {
        private final BlockInfo parent;
        private final Set<String> names = new HashSet<>();
        private final Map<String, String> nameMap = new HashMap<>();

        private BlockInfo(BlockInfo parent) {
            this.parent = parent;
        }

        public String getMappedName(String name) {
            String mapped;
            if ((mapped = nameMap.get(name)) != null) {
                return mapped;
            }
            if (parent != null) {
                return parent.getMappedName(name);
            } else {
                return null;
            }
        }
    }

}
