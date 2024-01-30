package tech.metavm.autograph.mocks;

import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;

@EntityType(value = "AstPair", compiled = true)
public class AstPair<K,V> {

    public AstPair(K first, V second) {
        this.first = first;
        this.second = second;
    }

    @EntityField("first")
    final K first;

    @EntityField("second")
    final V second;

}
