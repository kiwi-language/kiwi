package org.metavm.classfile;

import junit.framework.TestCase;
import org.junit.Assert;
import org.metavm.entity.StdKlass;
import org.metavm.entity.mocks.MockEntityRepository;
import org.metavm.flow.KlassInput;
import org.metavm.flow.KlassOutput;
import org.metavm.object.type.Types;
import org.metavm.util.MockUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class ClassFileIOTest extends TestCase {


    public void test() {
        var testKlasses = MockUtils.createTestKlasses();
        var fooKlass = testKlasses.getFirst();
        var supplierKlass = testKlasses.get(1);

        var bout = new ByteArrayOutputStream();
        var writer = new ClassFileWriter(new KlassOutput(bout));
        writer.write(fooKlass);
        writer.write(supplierKlass);

        var repo = new MockEntityRepository();
        repo.bind(StdKlass.index.get());

        var reader = new ClassFileReader(new KlassInput(new ByteArrayInputStream(bout.toByteArray()), repo), repo, null);
        var fooKlass1 = reader.read();
        var supplierKlass1 = reader.read();

        Assert.assertEquals(fooKlass.getName(), fooKlass1.getName());
        Assert.assertTrue(fooKlass1.isSearchable());

        var nameField = fooKlass1.getFieldByName("name");
        Assert.assertEquals("name", nameField.getName());

        var getComparatorMethod = fooKlass1.getMethodByName("getComparator");
        var lambda = getComparatorMethod.getLambdas().getFirst();
        Assert.assertEquals(Types.getLongType(), lambda.getReturnType());

        var nameIndex = fooKlass1.getIndices().getFirst();
        Assert.assertEquals("nameIdx", nameIndex.getName());
        Assert.assertEquals(Types.getStringType(), nameIndex.getType());

        var typeVariable = fooKlass1.getTypeParameters().getFirst();
        Assert.assertEquals("T", typeVariable.getName());

        Assert.assertEquals(supplierKlass.getName(), supplierKlass1.getName());

        var itKlass = fooKlass1.getInterfaces().getFirst().getKlass();
        Assert.assertSame(supplierKlass1, itKlass);

        var reader1 = new ClassFileReader(new KlassInput(new ByteArrayInputStream(bout.toByteArray()), repo), repo, null);
        var fooKlass2 = reader1.read();
        Assert.assertSame(fooKlass1, fooKlass2);
        var getComparatorMethod1 = fooKlass1.getMethodByName("getComparator");
        Assert.assertSame(getComparatorMethod, getComparatorMethod1);
        var nameField1 = fooKlass1.getFieldByName("name");
        Assert.assertSame(nameField, nameField1);
    }


}