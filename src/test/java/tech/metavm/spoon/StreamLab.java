package tech.metavm.spoon;


import java.util.Collection;
import java.util.stream.Stream;

public class StreamLab {

    public static <T> Stream<T> stream(Collection<T> collection) {
        return collection.stream();
    }

}
