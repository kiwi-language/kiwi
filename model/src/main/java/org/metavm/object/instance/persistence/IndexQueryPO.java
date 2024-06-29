package org.metavm.object.instance.persistence;

import javax.annotation.Nullable;

public record IndexQueryPO(long appId,
                           byte[] indexId,
                           @Nullable IndexKeyPO from,
                           @Nullable IndexKeyPO to,
                           boolean desc,
                           Long limit,
                           int lockMode) {

    public boolean match(IndexKeyPO key) {
        return (from == null || from.compareTo(key) <= 0) && (to == null || to.compareTo(key) >= 0);
    }

}
