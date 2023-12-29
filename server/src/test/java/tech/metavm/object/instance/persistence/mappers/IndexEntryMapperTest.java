package tech.metavm.object.instance.persistence.mappers;

import junit.framework.TestCase;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.Assert;
import tech.metavm.entity.IndexOperator;
import tech.metavm.entity.LockMode;
import tech.metavm.object.instance.persistence.IndexEntryPO;
import tech.metavm.object.instance.persistence.IndexKeyPO;
import tech.metavm.object.instance.persistence.IndexQueryItemPO;
import tech.metavm.object.instance.persistence.IndexQueryPO;
import tech.metavm.util.BytesUtils;
import tech.metavm.util.H2DataSourceBuilder;
import tech.metavm.util.Instances;
import tech.metavm.util.SQLStatements;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static tech.metavm.util.TestConstants.APP_ID;

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
                k.setIndexId(CONSTRAINT_ID);
                k.setColumn0(BytesUtils.toIndexBytes(Instances.longInstance(1L)));
                k.setColumn1(BytesUtils.toIndexBytes(Instances.longInstance(i)));
                indexItems.add(new IndexEntryPO(APP_ID, k, instanceIdBase + i));
            }
            indexEntryMapper.batchInsert(indexItems);

            IndexQueryPO query = new IndexQueryPO(
                    APP_ID, CONSTRAINT_ID,
                    List.of(
                            new IndexQueryItemPO(
                                    "column0", IndexOperator.EQ, BytesUtils.toIndexBytes(Instances.longInstance(1L))
                            ),
                            new IndexQueryItemPO(
                                    "column1", IndexOperator.EQ, BytesUtils.toIndexBytes(Instances.longInstance(5L))
                            )
                    ),
                    true, 2L, LockMode.NONE.code()
            );
            List<IndexEntryPO> result = indexEntryMapper.query(query);
            Assert.assertEquals(2, result.size());
            Assert.assertEquals(instanceIdBase + 9, result.get(0).getInstanceId());
            Assert.assertEquals(instanceIdBase + 8, result.get(1).getInstanceId());
        }
    }


}