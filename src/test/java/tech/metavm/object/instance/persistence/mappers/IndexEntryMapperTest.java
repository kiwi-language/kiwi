package tech.metavm.object.instance.persistence.mappers;

import junit.framework.TestCase;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.Assert;
import tech.metavm.entity.IndexQueryOperator;
import tech.metavm.entity.LockMode;
import tech.metavm.object.instance.persistence.IndexEntryPO;
import tech.metavm.object.instance.persistence.IndexKeyPO;
import tech.metavm.object.instance.persistence.IndexQueryPO;
import tech.metavm.util.H2DataSourceBuilder;
import tech.metavm.util.SQLStatements;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static tech.metavm.util.TestConstants.TENANT_ID;

public class IndexEntryMapperTest extends TestCase {

    private static final long CONSTRAINT_ID = 1001L;

    private SqlSessionFactory sqlSessionFactory;

    @Override
    protected void setUp() throws Exception {
        DataSource dataSource = H2DataSourceBuilder.getH2DataSource();
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            statement.execute("drop table if exists index_entry");
            statement.execute(SQLStatements.CREATE_INDEX_ENTRY);
        }
        sqlSessionFactory = new SqlSessionFactoryBuilder().build(
                IndexEntryMapperTest.class.getResourceAsStream("/mybatis-config.xml")
        );
    }

    public void testQuery() {
        try(SqlSession session = sqlSessionFactory.openSession()) {
            IndexEntryMapper indexEntryMapper = session.getMapper(IndexEntryMapper.class);
            long instanceIdBase = 10000L;
            List<IndexEntryPO> indexItems = new ArrayList<>();
            for (int i = 0; i < 10; i++) {
                IndexKeyPO k = new IndexKeyPO();
                k.setConstraintId(CONSTRAINT_ID);
                k.setColumn1(IndexKeyPO.getIndexColumn(1));
                k.setColumnXPresent(true);
                k.setColumnX((long) i);
                indexItems.add(new IndexEntryPO(TENANT_ID, k, instanceIdBase + i));
            }
            indexEntryMapper.batchInsert(indexItems);

            IndexKeyPO key = new IndexKeyPO();
            key.setConstraintId(CONSTRAINT_ID);
            key.setColumn1(IndexKeyPO.getIndexColumn(1));
            key.setColumnXPresent(true);
            key.setColumnX(5L);

            IndexQueryPO query = new IndexQueryPO(
                    TENANT_ID, CONSTRAINT_ID,
                    key,
                    IndexQueryOperator.GT,
                    true, 2, LockMode.NONE.code()
            );
            List<IndexEntryPO> result = indexEntryMapper.query(query);
            Assert.assertEquals(2, result.size());
            Assert.assertEquals(instanceIdBase + 9, result.get(0).getInstanceId());
            Assert.assertEquals(instanceIdBase + 8, result.get(1).getInstanceId());
        }
    }


}