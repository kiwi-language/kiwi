package org.metavm.util;

import junit.framework.TestCase;

import java.util.List;

public class LinkedListTest extends TestCase {

    public void testListIterator() {
        List<Integer> list = new LinkedList<>();
        list = list.subList(0, 0);
        for (Integer v : list) {
            System.out.println(v);
        }
    }

}