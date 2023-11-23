package tech.metavm.util;

import junit.framework.TestCase;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class NncUtilsTest extends TestCase {

    public static final Random random = new Random();

    public void testToBase64() {
        System.out.println(NncUtils.encondeBase64(1000000000));
    }

    public void testMerge() {
        for (int t = 0; t < 1000; t++) {
            var l1 = randomList(random.nextInt(100));
            var l2 = randomList(random.nextInt(100));
            var m = NncUtils.merge(l1, l2, false);
            for (int i = 1; i < m.size(); i++) {
                Assert.assertTrue(m.get(i) > m.get(i - 1));
            }
        }
    }

    private List<Integer> randomList(int n) {
        List<Integer> list = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            list.add(random.nextInt(1000));
        }
        return list;
    }

}