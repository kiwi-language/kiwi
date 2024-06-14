package org.metavm.object.instance.persistence;

import javax.annotation.Nullable;

public record IndexQueryPO(long appId,
                           byte[] indexId,
                           @Nullable IndexKeyPO from,
                           @Nullable IndexKeyPO to,
                           boolean desc,
                           Long limit,
                           int lockMode) {

}
