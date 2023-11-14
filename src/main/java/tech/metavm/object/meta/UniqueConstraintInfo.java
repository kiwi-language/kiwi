package tech.metavm.object.meta;

import tech.metavm.object.meta.persistence.ConstraintPO;
import tech.metavm.util.ContextUtil;
import tech.metavm.util.NncUtils;

import java.util.List;

public record UniqueConstraintInfo(
        long id,
        List<UniqueConstraintItemInfo> items,
        String message
) {

    static UniqueConstraintInfo create(long id, List<UniqueConstraintItemInfo> items, String message) {
        return new UniqueConstraintInfo(id, items, message);
    }

    public ConstraintPO toPO(long typeId) {
        return new ConstraintPO(
            id, ContextUtil.getTenantId(), typeId,
                ConstraintKind.UNIQUE.code(),
                message,
                NncUtils.toJSONString(getParam())
        );
    }

    public IndexParam getParam() {
        return new IndexParam(
                NncUtils.map(items, UniqueConstraintItemInfo::toDTO)
        );
    }

}
