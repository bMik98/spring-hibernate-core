package sorokin.java.course.config;

import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.service.ServiceRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import sorokin.java.course.account.Account;
import sorokin.java.course.user.User;
import sorokin.java.course.hibernate.TransactionRunner;

import java.util.Properties;

@Configuration
public class HibernateConfiguration {

    private static Properties hibernateProperties(Environment env) {
        Properties properties = new Properties();

        properties.put("hibernate.connection.driver_class", env.getRequiredProperty("db.driver"));
        properties.put("hibernate.connection.url", env.getRequiredProperty("db.url"));
        properties.put("hibernate.connection.username", env.getRequiredProperty("db.username"));
        properties.put("hibernate.connection.password", env.getRequiredProperty("db.password"));

        properties.put("hibernate.hbm2ddl.auto", env.getRequiredProperty("hibernate.hbm2ddl.auto"));
        properties.put("hibernate.show_sql", env.getRequiredProperty("hibernate.show_sql"));
        properties.put("hibernate.format_sql", env.getRequiredProperty("hibernate.format_sql"));

        properties.put("hibernate.current_session_context_class", "thread");
        properties.put("hibernate.connection.isolation", "2");

        return properties;
    }

    @Bean(destroyMethod = "close")
    public SessionFactory sessionFactory(Environment env) {
        org.hibernate.cfg.Configuration configuration = new org.hibernate.cfg.Configuration();

        configuration
                .addAnnotatedClass(User.class)
                .addAnnotatedClass(Account.class)
                .setProperties(hibernateProperties(env));

        ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
                .applySettings(configuration.getProperties())
                .build();

        return configuration.buildSessionFactory(serviceRegistry);
    }

    @Bean
    public TransactionRunner transactionRunner(SessionFactory sessionFactory) {
        return new TransactionRunner(sessionFactory);
    }
}
