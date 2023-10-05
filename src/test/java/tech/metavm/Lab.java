package tech.metavm;

import java.io.Serializable;

public abstract class Lab {

    public static void main(String[] args) {
        try {
            throw new Throwable();
        }
        catch (Exception e) {
            throw new RuntimeException("Rethrown", e);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        } finally {
            System.out.println("finally done");
        }

    }

    public static void test(Object value) {
        if(value instanceof CharSequence charSeq || value instanceof Serializable ser) {

        }
    }
}