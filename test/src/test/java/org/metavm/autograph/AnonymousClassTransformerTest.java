package org.metavm.autograph;

import junit.framework.TestCase;
import lombok.extern.slf4j.Slf4j;
import org.metavm.util.TestUtils;
import org.metavm.util.Utils;

import java.util.regex.Pattern;

@Slf4j
public class AnonymousClassTransformerTest extends TestCase {

    public static final Pattern PTN = Pattern.compile("test-classes/.+/(.+)/");

//    public void test() {
//        var source = "basics/anonymous_class/AnonymousClassFoo.java";
//        process(source);
//    }

//    public void testAnonymousTakingArgs() {
//        var src = "basics/anonymous_class/AnonymousClassWithArgs.java";
//        process(src);
//    }

    public void testLocalClass() {
        String src = "basics/local_class/LocalClassFoo.java";
        process(src);
    }

    public void testNestedLocalClass() {
        String src = "basics/local_class/LocalClassNameConflictFoo.java";
        process(src);
    }

//    public void testAnonymous2() {
//        var src = "basics/anonymous_class/SuperclassFieldFoo.java";
//        process(src);
//    }

    private void process(String source) {
        source = TestUtils.getResourcePath(source);
        var m = PTN.matcher(source);
        log.debug("{}", source);
        if(m.find()) {
            var pkgName = m.group(1);
            var newPkgName = pkgName + "2";
            var target = m.replaceAll("tmp/" + newPkgName + "/");
            log.debug("{}", target);
            var file = TranspileTestTools.getPsiJavaFile(source);
            TranspileTestTools.executeCommand(() -> {
                file.accept(new SyntheticClassNameTracker());
                file.accept(new AnonymousClassTransformer());
            });
            Utils.writeFile(target, file.getText().replace(pkgName, newPkgName));
        }
        else
            throw new IllegalArgumentException("Invalid source " + source);
    }

}