package org.metavm.common;

import junit.framework.TestCase;
import org.metavm.object.instance.core.TmpId;
import org.metavm.object.instance.rest.*;
import org.metavm.util.Constants;

import java.util.List;
import java.util.Map;

public class DtoCopierTest extends TestCase {

    public void test() {
        var dtoCopier = new DtoCopier(CopyContext.create(Map.of()));
        var type = Constants.addIdPrefix(TmpId.randomString());
        var fieldId = TmpId.randomString();
        var fieldId2 = TmpId.randomString();
        var instanceFieldValue = new InstanceFieldValue("Shoes", InstanceDTO.createClassInstance(
                type,
                List.of(
                        InstanceFieldDTO.create(
                                fieldId,
                                PrimitiveFieldValue.createString("Shoes")
                        ),
                        InstanceFieldDTO.create(
                                fieldId2,
                                ReferenceFieldValue.create(TmpId.randomString())
                        )
                )
        ));
        var copy = dtoCopier.copy(instanceFieldValue);
        assertEquals(instanceFieldValue, copy);
    }

}