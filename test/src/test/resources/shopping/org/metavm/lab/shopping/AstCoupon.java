package org.metavm.lab.shopping;

import org.metavm.api.Entity;

@Entity(compiled = true)
public interface AstCoupon {

    long use(int amount);

    long calc(int amount);

}
