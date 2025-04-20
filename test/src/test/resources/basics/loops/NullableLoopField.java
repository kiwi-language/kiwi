package loops;

import java.util.List;

public class NullableLoopField {

    private Node head;
    private Node tail;

    public NullableLoopField(List<Integer> values) {
        values.forEach(this::add);
    }

    public void add(int value) {
        if(head == null)
            head = tail = new Node(value);
        else
            tail = tail.next = new Node(value);
    }

    public int sum() {
        var sum = 0;
        for(var x = head; x != null; x = x.next)
            sum += x.value;
        return sum;
    }

    public static class Node {
        final int value;
        Node next;

        public Node(int value) {
            this.value = value;
        }
    }

}
