package tech.metavm.util;

import java.io.Serializable;
import java.util.function.Function;

public interface IndexMapper<T, K> extends Function<T, K>, Serializable {

}
