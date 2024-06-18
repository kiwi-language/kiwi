package org.metavm.asm;

import junit.framework.TestCase;
import org.metavm.entity.MockStandardTypesInitializer;
import org.metavm.flow.FlowSavingContext;
import org.metavm.object.type.rest.dto.BatchSaveRequest;
import org.metavm.util.BootstrapUtils;
import org.metavm.util.ContextUtil;
import org.metavm.util.TestConstants;
import org.metavm.util.TestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class AssemblerTest extends TestCase {

    public static final Logger logger = LoggerFactory.getLogger(AssemblerTest.class);

    @Override
    protected void setUp() throws Exception {
        MockStandardTypesInitializer.init();
    }

    public void testParentChild() {
        deploy("/Users/leen/workspace/object/test/src/test/resources/asm/ParentChild.masm");
}

    public void testMyList() {
//        assemble(List.of(source));
        deploy("/Users/leen/workspace/object/test/src/test/resources/asm/List.masm");
    }

    public void testShopping() {
        deploy("/Users/leen/workspace/object/test/src/test/resources/asm/Shopping.masm");
    }

    public void testLivingBeing() {
        deploy("/Users/leen/workspace/object/test/src/test/resources/asm/LivingBeing.masm");
    }

    public void testUtils() {
        deploy("/Users/leen/workspace/object/test/src/test/resources/asm/Utils.masm");
    }

    public void testGenericOverloading() {
        deploy("/Users/leen/workspace/object/test/src/test/resources/asm/GenericOverloading.masm");
    }

    public void testLambda() {
        deploy("/Users/leen/workspace/object/test/src/test/resources/asm/Lambda.masm");
    }

    private void assemble(String source) {
        var assembler = AssemblerFactory.createWithStandardTypes();
        assemble(List.of(source), assembler);
    }

    private BatchSaveRequest assemble(List<String> sources, Assembler assembler) {
        assembler.assemble(sources);
        var request = new BatchSaveRequest(assembler.getAllTypeDefs(), List.of(), true);
        TestUtils.writeJson("/Users/leen/workspace/object/test.json", request);
        return request;
    }

    private void deploy(String source) {
        var bootResult = BootstrapUtils.bootstrap();
        var typeManager = TestUtils.createCommonManagers(bootResult).typeManager();
        FlowSavingContext.initConfig();
        var assembler = AssemblerFactory.createWithStandardTypes();
        var request = assemble(List.of(source), assembler);
//        DebugEnv.DEBUG_ON = true;
        ContextUtil.setAppId(TestConstants.APP_ID);
        TestUtils.doInTransaction(() -> typeManager.batchSave(request));
    }

}