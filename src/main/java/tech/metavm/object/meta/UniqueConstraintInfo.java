package tech.metavm.object.meta;

import tech.metavm.object.meta.persistence.ConstraintPO;
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
        ConstraintPO po = new ConstraintPO();
        po.setId(id);
        po.setKind(ConstraintKind.UNIQUE.code());
        po.setDeclaringTypeId(typeId);
        po.setMessage(message);
        po.setParam(NncUtils.toJSONString(getParam()));
        return po;
    }

    public UniqueConstraintParam getParam() {
        return new UniqueConstraintParam(
                NncUtils.map(items, UniqueConstraintItemInfo::toDTO)
        );
    }

}
