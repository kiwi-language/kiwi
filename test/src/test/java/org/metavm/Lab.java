package org.metavm;

import org.metavm.autograph.TranspileTestTools;
import org.metavm.autograph.TranspileUtils;
import org.metavm.mocks.PipelineFoo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Objects;

public class Lab {

    public static final Logger logger = LoggerFactory.getLogger(Lab.class);


    public static void main(String[] args) throws IOException, SQLException {
        var file = TranspileTestTools.getPsiJavaFile(PipelineFoo.class);
        var klass = file.getClasses()[0];
        var extKlass = file.getClasses()[1];
        var cmpKlass = klass.getInterfaces()[0];

        var pipeline = TranspileUtils.findSubstitutorPipeline(TranspileUtils.createType(extKlass), cmpKlass);
        Objects.requireNonNull(pipeline);
        var typeArg = pipeline.substitute(TranspileUtils.createType(cmpKlass.getTypeParameters()[0]));
        System.out.println(typeArg);
    }


}
