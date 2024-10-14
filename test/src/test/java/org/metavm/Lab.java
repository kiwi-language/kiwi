package org.metavm;

import org.metavm.autograph.TranspileTestTools;
import org.metavm.autograph.TranspileUtils;
import org.metavm.mocks.PipelineFoo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Objects;
import java.util.regex.Pattern;

public class Lab {

    public static final Logger logger = LoggerFactory.getLogger(Lab.class);

    public static final Pattern PTN = Pattern.compile("\\$(0|([1-9][0-9]*))");

    public static void main(String[] args) throws IOException, SQLException {
        System.out.println(PTN.matcher("$0").matches());
        System.out.println(PTN.matcher("$10").matches());
        System.out.println(PTN.matcher("$01").matches());

        var m = PTN.matcher("$10");
        if(m.matches())
            System.out.println(m.group(1));

        m = PTN.matcher("$0");
        if(m.matches())
            System.out.println(m.group(1));

    }


}
