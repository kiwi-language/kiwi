package org.metavm;

import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.PhysicalId;
import org.metavm.object.instance.core.TaggedPhysicalId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;

public class Lab {

    public static final Logger logger = LoggerFactory.getLogger(Lab.class);


    public static void main(String[] args) throws IOException, SQLException {
        System.out.println(Id.parse("0290a8d6b90704ce02").getClass());
    }

    private static void printId(PhysicalId id) {
        logger.debug("isArray: {}, treeId: {}, node:d: {}, type tag: {}",
                id.isArray(), id.getTreeId(), id.getNodeId(), id instanceof TaggedPhysicalId t ? t.getTypeTag() : 0);
    }


}
