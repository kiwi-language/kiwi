package org.metavm;

import org.metavm.object.instance.core.Id;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;

public class Lab {

    public static final Logger logger = LoggerFactory.getLogger(Lab.class);


    public static void main(String[] args) throws IOException, SQLException {
        System.out.println(Id.parse("0290a8d6b90704ce02").getClass());
    }


}
