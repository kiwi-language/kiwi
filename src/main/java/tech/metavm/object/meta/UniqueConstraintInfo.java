package tech.metavm.object.meta;

import tech.metavm.object.meta.persistence.ConstraintPO;
import tech.metavm.util.NncUtils;

import java.util.List;

public record UniqueConstraintInfo(
        long id,
        List<Long> fieldIds
) {

    static UniqueConstraintInfo create(long id, List<Long> fieldIds) {
        return new UniqueConstraintInfo(id, fieldIds);
    }

    public ConstraintPO toPO(long typeId) {
        ConstraintPO po = new ConstraintPO();
        po.setId(id);
        po.setKind(ConstraintKind.UNIQUE.code());
        po.setTypeId(typeId);
        po.setParam(
                NncUtils.toJSONString(new UniqueConstraintParam(fieldIds))
        );
        return po;
    }
}
