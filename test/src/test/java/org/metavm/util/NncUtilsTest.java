package org.metavm.util;

import junit.framework.TestCase;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class NncUtilsTest extends TestCase {

    public static final Random random = new Random();

    public void testToBase64() {
        System.out.println(NncUtils.encodeBase64(1000000000));
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

    public void testRandomPassword() {
        System.out.println(NncUtils.randomPassword());
    }

    public void testTryParseLong() {
        Assert.assertEquals((Object) 111L, NncUtils.tryParseLong("000111"));
        Assert.assertEquals((Object) (-1L), NncUtils.tryParseLong("-1"));
        Assert.assertNull(NncUtils.tryParseLong("abc"));
    }

    public void testIsEmailAddress() {
        Assert.assertTrue(EmailUtils.isEmailAddress("15968879210@163.com"));
        Assert.assertTrue(EmailUtils.isEmailAddress("wizardleen@gmail.com"));
        Assert.assertFalse(EmailUtils.isEmailAddress("yang_li_gmail.com"));
        Assert.assertFalse(EmailUtils.isEmailAddress("wizardleen@gmail.com///"));
    }

}