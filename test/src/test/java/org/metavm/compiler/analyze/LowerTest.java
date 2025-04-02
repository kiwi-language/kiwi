package org.metavm.compiler.analyze;

import junit.framework.TestCase;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.metavm.compiler.CompilerTestUtils;
import org.metavm.compiler.element.Name;
import org.metavm.compiler.syntax.MethodDecl;

@Slf4j
public class LowerTest extends TestCase {

    public void test() {
        var source = "/Users/leen/workspace/object/test/src/test/resources/kiwi/enum.kiwi";
        var file = CompilerTestUtils.parse(source);
        var proj = CompilerTestUtils.attr(file);
        file.accept(new Lower(proj));

        var classDecl = file.getClassDeclarations().getFirst();
        var initDecl = classDecl.getMembers().find(e -> e instanceof MethodDecl m && m.name() == Name.init());
        Assert.assertNotNull(initDecl);

        log.debug("{}", file.getText());
    }

}
