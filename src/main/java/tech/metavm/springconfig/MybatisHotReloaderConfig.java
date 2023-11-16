//package tech.metavm.object.springconfig;
//
//import org.apache.ibatis.session.SqlSessionFactory;
//import org.springframework.beans.BeansException;
//import org.springframework.context.ApplicationContext;
//import org.springframework.context.ApplicationContextAware;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import tech.metavm.util.MybatisHotReloader;
//
//import java.io.IOException;
//
////@Configuration
//public class MybatisHotReloaderConfig implements ApplicationContextAware {
//
//    private ApplicationContext applicationContext;
//
////    @Bean
//    public MybatisHotReloader getMybatisHotReloader(SqlSessionFactory sqlSessionFactory) throws IOException {
//        return new MybatisHotReloader(
//                applicationContext.getResources("classpath*:/mappers/*.xml"),
//                sqlSessionFactory.getConfiguration()
//        );
//    }
//
//    @Override
//    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
//        this.applicationContext = applicationContext;
//    }
//}
