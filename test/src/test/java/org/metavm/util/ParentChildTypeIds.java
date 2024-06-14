package org.metavm.util;

public record ParentChildTypeIds(
        String parentTypeId,
        String parentChildrenFieldId,
        String parentConstructorId,
        String parentTestMethodId,
        String childTypeId,
        String childNameFieldId,
        String childNextFieldId
) {

}
