package tech.metavm.autograph.mocks;

public class LivenessFoo {

    public void test(int num) {
        // live: {num}
        String s = "Hello";
        if(/* live: {num, s} */ num > 0) {
            // live: {s}
            System.out.println(s);
            // live: {}
            s += " World";
        }
        // live: {s}
        System.out.println(s);
    }

}
