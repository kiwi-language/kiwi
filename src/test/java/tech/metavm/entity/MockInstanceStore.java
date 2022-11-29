package tech.metavm.entity;

import tech.metavm.object.instance.IInstanceStore;
import tech.metavm.object.instance.Instance;
import tech.metavm.object.instance.persistence.IndexKeyPO;
import tech.metavm.object.instance.persistence.InstancePO;
import tech.metavm.util.ChangeList;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;
import tech.metavm.util.TestUtils;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

public class MockInstanceStore implements IInstanceStore {

    public static final String DIR = "/Users/leen/workspace/object/src/test/resources/instances";

    @Override
    public void save(ChangeList<InstancePO> diff) {
        for (InstancePO delete : diff.deletes()) {
            if(!getInstanceFile(delete.getId()).delete()) {
                throw new InternalException("Fail to delete instance " + delete.getId());
            }
        }
        for (InstancePO instancePO : diff.insertsOrUpdates()) {
            try (FileWriter fileWriter = new FileWriter(getInstanceFile(instancePO.getId()))){
                fileWriter.write(TestUtils.toJSONString(instancePO));
            } catch (IOException e) {
                throw new InternalException("Fail to save instance " + instancePO.getId(), e);
            }
        }
    }

    private File getInstanceFile(long id) {
        return new File(DIR + "/" + id + ".json");
    }

    @Override
    public List<Instance> selectByKey(IndexKeyPO key, InstanceContext context) {
        return List.of();
    }

    @Override
    public List<InstancePO> load(StoreLoadRequest request, InstanceContext context) {
        return NncUtils.map(request.ids(), this::readInstance);
    }

    private InstancePO readInstance(long id) {
        try(FileReader reader = new FileReader(getInstanceFile(id))) {
            return TestUtils.readJSON(InstancePO.class, reader);
        }
        catch (IOException e) {
            throw new InternalException("Fail to read instance " + id, e);
        }
    }

    @Override
    public List<InstancePO> getByTypeIds(Collection<Long> typeIds, InstanceContext context) {
        return List.of();
    }

    public void clear() {
        TestUtils.clearDir(DIR);
    }

}
