package org.metavm;

import org.metavm.entity.mocks.MockEntityRepository;
import org.metavm.flow.*;
import org.metavm.object.instance.core.Id;
import org.metavm.object.type.Klass;
import org.metavm.object.type.KlassBuilder;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;


public class Lab implements Serializable {

    public static final String productFile = "/Users/leen/workspace/shopping/target/shopping/Product.mvclass";
    public static final String productStatusFile = "/Users/leen/workspace/shopping/target/shopping/ProductStatus.mvclass";

    public static void main(String[] args) throws IOException {

        System.out.println(Id.parse("01cc9c0104").getClass().getName());
        System.out.println(Id.parse("13c4b21f").getClass().getName());

        var entityRepo = new MockEntityRepository();
        var indexMapKlass = KlassBuilder.newBuilder("Index", "org.metavm.api.Index").build();
        indexMapKlass.initId(Id.parse("01f69d0100"));
        entityRepo.bind(indexMapKlass);

        var productKlass = new KlassInput(new FileInputStream(productFile), entityRepo).readEntity(Klass.class, null);
        var productStatusKlass = new KlassInput(new FileInputStream(productStatusFile), entityRepo).readEntity(Klass.class, null);
// 1396b21f
        var method = productStatusKlass.getMethod(m -> m.getName().equals("ProductStatus"));
        method.getCode().rebuildNodes();
//        System.out.println(method.getText());
    }

}