package tech.metavm.spoon;


import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class TryWithLab {

    public void test() {
        try(
                InputStream input = new FileInputStream("");
                InputStream input2 = new FileInputStream("");
        ) {
            System.out.println(input.read());
            System.out.println(input2.read());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
