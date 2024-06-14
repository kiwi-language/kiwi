package org.metavm.object.instance.persistence.mappers;//package org.metavm.object.instance.persistence.mappers;
//
//import junit.framework.TestCase;
//import org.apache.ibatis.session.SqlSession;
//import org.apache.ibatis.session.SqlSessionFactory;
//import org.apache.ibatis.session.SqlSessionFactoryBuilder;
//import org.hamcrest.MatcherAssert;
//import org.metavm.object.instance.persistence.InstanceArrayPO;
//import org.metavm.util.H2DataSourceBuilder;
//import org.metavm.util.ListMatcher;
//import org.metavm.util.SQLStatements;
//
//import javax.sql.DataSource;
//import java.sql.Connection;
//import java.sql.Statement;
//import java.util.List;
//import java.util.Map;
//
//import static org.metavm.util.PersistenceUtil.convertForLoading;
//import static org.metavm.util.TestConstants.APP_ID;
//
//public class InstanceArrayMapperTest extends TestCase {
//
//    private SqlSessionFactory sqlSessionFactory;
//
//    @Override
//    protected void setUp() throws Exception {
//        DataSource dataSource = H2DataSourceBuilder.getH2DataSource();
//        try (Connection connection = dataSource.getConnection()) {
//            Statement statement = connection.createStatement();
//            statement.execute("drop table if exists instance_array");
//            statement.execute(SQLStatements.CREATE_INSTANCE_ARRAY);
//        }
//        sqlSessionFactory = new SqlSessionFactoryBuilder().build(
//                InstanceMapperTest.class.getResourceAsStream("/mybatis-config.xml")
//        );
//    }
//
//    public void test() {
//        InstanceArrayPO instanceArrayPO = new InstanceArrayPO(
//                1L, 100L, APP_ID, 3,
//                List.of(
//                        1,
//                        Map.of(
//                                "kind", 1,
//                                "value", 2L
//                        ),
//                        Map.of(
//                                "kind", 2,
//                                "typeId", 102L,
//                                "value",
//                                Map.of(
//                                        "s0", "Big Foo",
//                                        "o0",
//                                        Map.of(
//                                                "kind", 2,
//                                                "typeId", 103L,
//                                                "value",
//                                                Map.of(
//                                                        "s0", "Bar001"
//                                                )
//                                        )
//                                )
//                        ),
//                        Map.of(
//                                "kind", 3,
//                                "typeId", 101L,
//                                "value",
//                                List.of(
//                                        Map.of(
//                                                "kind", 2,
//                                                "typeId", 103L,
//                                                "value",
//                                                Map.of(
//                                                        "s0", "Bar002"
//                                                )
//                                        ),
//                                        Map.of(
//                                                "kind", 2,
//                                                "typeId", 103L,
//                                                "value",
//                                                Map.of(
//                                                        "s0", "Bar003"
//                                                )
//                                        )
//                                )
//                        )
//                ),
//                false,
//                0L,
//                0L
//        );
//
//        try (SqlSession session = sqlSessionFactory.openSession()) {
//            InstanceArrayMapper instanceArrayMapper = session.getMapper(InstanceArrayMapper.class);
//            instanceArrayMapper.batchInsert(List.of(instanceArrayPO));
//            List<InstanceArrayPO> loaded = instanceArrayMapper.selectByIds(APP_ID, List.of(instanceArrayPO.getId()));
//            MatcherAssert.assertThat(
//                    convertForLoading(loaded),
//                    ListMatcher.of(convertForLoading(instanceArrayPO))
//            );
//        }
//
//    }
//
//}