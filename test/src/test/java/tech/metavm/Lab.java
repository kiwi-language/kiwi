package tech.metavm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.metavm.autograph.TranspileTestTools;
import tech.metavm.autograph.mocks.GenericOverrideFoo;

public class Lab {

    public static final Logger logger = LoggerFactory.getLogger(Lab.class);


    public static void main(String[] args) {
        var file = TranspileTestTools.getPsiJavaFile(GenericOverrideFoo.class);
        var baseClass = file.getClasses()[0];
        var method0 = baseClass.getMethods()[0];
        var typeParamList0 = method0.getTypeParameterList();
        logger.info("type parameters: {}",  typeParamList0 != null ? typeParamList0.getText() : "null");

        var subClass = file.getClasses()[1];
        var method = subClass.getMethods()[0];
        var typeParamList = method.getTypeParameterList();
        logger.info("type parameters: {}",  typeParamList != null ? typeParamList.getText() : "null");
    }

}
