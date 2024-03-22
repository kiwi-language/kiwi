package tech.metavm.object.instance;

import javax.annotation.Nullable;

public record ScanQuery(@Nullable byte[] startId, long limit) {

}
