package org.metavm.autograph;

import junit.framework.TestCase;
import org.junit.Assert;
import org.metavm.mocks.PipelineFoo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SubstitutorPipelineTest extends TestCase {

    public static final Logger logger = LoggerFactory.getLogger(SubstitutorPipelineTest.class);

    public void test() {
        var file = TranspileTestTools.getPsiJavaFile(PipelineFoo.class);
        var fooKlass = file.getClasses()[0];
        var fooExtKlass = file.getClasses()[1];
        var cmpKlass = fooKlass.getInterfaces()[0];

        var pipeline = TranspileUtils.findSubstitutorPipeline(TranspileUtils.createType(fooExtKlass), cmpKlass);
        Assert.assertNotNull(pipeline);
        Assert.assertEquals(3, pipeline.getDepth());
        var typeArg = pipeline.substitute(TranspileUtils.createType(cmpKlass.getTypeParameters()[0]));
        Assert.assertEquals(TranspileUtils.createType(fooKlass, TranspileUtils.createType(String.class)), typeArg);
    }

}