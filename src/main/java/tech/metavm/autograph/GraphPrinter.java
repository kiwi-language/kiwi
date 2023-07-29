package tech.metavm.autograph;

import java.util.*;

public class GraphPrinter {

    private static final int LINE_GAPS = 2;

    private static final int BOX_WIDTH = 40;

    public static String print(Graph graph) {
        return new GraphPrinter().print0(graph);
    }

    private String print0(Graph graph) {
        PrintNode printNode = createPrintNode(graph.entry(), new HashMap<>());
        printNode.dfs();
        printNode.calcWidth();
        printNode.calcPosition(0, 0);
        return print(printNode);
    }

    private String print(PrintNode root) {
        Queue<PrintNode> queue = new LinkedList<>();
        queue.offer(root);
        int canvasWidth = root.width;
        int x = 0, y = 0;
        StringBuilder builder = new StringBuilder();
        List<Integer> verticalBars = new ArrayList<>();
        while (!queue.isEmpty()) {
            var node = queue.poll();
            if (node.y > y) {
                builder.append('\n');
                for (int i = 0; i < LINE_GAPS; i++) {
                    for (int j = 0, k = 0; j < canvasWidth; j++) {
                        if (k < verticalBars.size() && verticalBars.get(k) == j) {
                            builder.append('|');
                            k++;
                        } else builder.append(' ');
                    }
                    builder.append('\n');
                }
                verticalBars.clear();
                x = 0;
                y = node.y;
            }
            for (int k = 0; x < node.end(); x++) {
                if (k < node.children.size() && node.children.get(k).boxCenter() == x) {
                    verticalBars.add(x);
                    k++;
                }
                if (x >= node.boxStart && x < node.boxEnd()) {
                    if (x >= node.textStart() && x < node.textEnd()) {
                        builder.append(node.text.charAt(x - node.textStart()));
                    } else builder.append(' ');
                } else {
                    if (node.children.size() > 1 && k > 0 && k < node.children.size()) builder.append('-');
                    else builder.append(' ');
                }
            }
            for (PrintNode child : node.children) queue.offer(child);
        }
        return builder.toString();
    }

    private PrintNode createPrintNode(CfgNode cfgNode, Map<CfgNode, PrintNode> map) {
        PrintNode printNode;
        if ((printNode = map.get(cfgNode)) != null) return printNode;
        printNode = new PrintNode(cfgNode);
        map.put(cfgNode, printNode);
        for (CfgNode next : cfgNode.getNext()) {
            printNode.connect(createPrintNode(next, map));
        }
        return printNode;
    }

    private static class PrintNode {
        @SuppressWarnings({"FieldCanBeLocal", "unused"})
        private final CfgNode cfgNode;
        private final String text;
        private int width = -1;
        private int x;
        private int y;
        private int boxStart;
        private int state;
        private final List<PrintNode> prev = new ArrayList<>();
        private final List<PrintNode> next = new ArrayList<>();
        private final List<PrintNode> children = new ArrayList<>();

        private PrintNode(CfgNode cfgNode) {
            this.cfgNode = cfgNode;
            text = cfgNode.getElement().toString();
        }

        void dfs() {
            if (state > 0) return;
            state = 1;
            for (PrintNode node : next) {
                if (node.state != 1) {
                    children.add(node);
                    node.dfs();
                }
            }
            state = 2;
        }

        void calcWidth() {
            if(width != -1) return;
            for (PrintNode child : children) {
                child.calcWidth();
                if(child.prev.size() == 1) width += child.width;
            }
            if (children.size() % 2 == 0) width += BOX_WIDTH;
        }

        void calcPosition(int x, int y) {
            this.x = x;
            this.y = y;
            if (children.isEmpty()) boxStart = x;
            else {
                for (int i = 0, childX = x; i < children.size(); i++) {
                    children.get(i).calcPosition(childX, y + 1);
                    childX += children.get(i).width;
                    if (children.size() % 2 == 0 && i == children.size() / 2 - 1) {
                        childX += BOX_WIDTH;
                    }
                }
                if (children.size() % 2 == 0) {
                    boxStart = children.get(children.size() / 2 - 1).end();
                } else {
                    boxStart = children.get(children.size() / 2).boxStart;
                }
            }
        }

        void connect(PrintNode node) {
            next.add(node);
            node.prev.add(this);
        }

        int textStart() {
            return boxStart + (BOX_WIDTH - text.length()) / 2;
        }

        int textEnd() {
            return textStart() + text.length();
        }

        int boxCenter() {
            return boxStart + BOX_WIDTH / 2;
        }

        int boxEnd() {
            return boxStart + BOX_WIDTH;
        }

        int end() {
            return x + width;
        }

    }

}
