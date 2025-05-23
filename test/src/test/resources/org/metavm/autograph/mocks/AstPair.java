package org.metavm.autograph.mocks;

import org.metavm.api.Entity;

@Entity(compiled = true)
public class AstPair<K,V> {

    public AstPair(K first, V second) {
        this.first = first;
        this.second = second;
    }

    final K first;

    final V second;

}
