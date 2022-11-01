package tech.metavm.infra;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tech.metavm.infra.persistence.IdBulk;
import tech.metavm.infra.persistence.IdBulkMapper;

import java.util.ArrayList;
import java.util.List;

@Component
public class IdService {

    public static final int NUM_BULKS = 1;

    @Autowired
    private IdBulkMapper idGeneratorMapper;

    @Transactional
    public void initBulks(long initId) {
        List<IdBulk> bulks = new ArrayList<>();
        long bulkCap = Long.MAX_VALUE / NUM_BULKS;
        long nextId = initId;
        for (int i = 0; i < NUM_BULKS; i++) {
            bulks.add(new IdBulk(i, nextId));
            nextId += bulkCap;
        }
        idGeneratorMapper.batchInsert(bulks);
    }

    @Transactional
    public List<Long> allocateIds(long tenantId, int num) {
        int bulkNum = bulkNum(tenantId);
        IdBulk idBulk = idGeneratorMapper.selectForUpdate(bulkNum);
        idGeneratorMapper.increaseNextId(bulkNum, num);
        List<Long> results = new ArrayList<>();
        long id = idBulk.getNextId();
        for (int i = 0; i < num; i++) {
            results.add(id++);
        }
        return results;
    }

    public Long allocateId(long tenantId) {
        return allocateIds(tenantId, 1).get(0);
    }

    private int bulkNum(long tenantId) {
        return (int) (tenantId % NUM_BULKS);
    }

}
