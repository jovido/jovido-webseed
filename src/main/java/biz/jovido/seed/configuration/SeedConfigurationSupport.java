package biz.jovido.seed.configuration;

import biz.jovido.seed.content.url.AliasRequestMapping;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.session.MapSessionRepository;
import org.springframework.session.SessionRepository;
import org.springframework.session.config.annotation.web.http.EnableSpringHttpSession;

/**
 * @author Stephan Grundner
 */
@ComponentScan("biz.jovido.seed")
@EntityScan("biz.jovido.seed")
@EnableJpaRepositories("biz.jovido.seed")
@EnableJpaAuditing
@EnableSpringHttpSession
@EnableWebSecurity
public class SeedConfigurationSupport implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Bean
    public AliasRequestMapping aliasRequestMapping() {
        AliasRequestMapping requestMapping = new AliasRequestMapping();
        requestMapping.setOrder(0);
        return requestMapping;
    }
//
//    @Bean
//    public ViewNameResolver viewNameResolver() {
//        return new DefaultViewNameResolver();
//    }

    @Bean
    public SessionRepository<?> sessionRepository() {
        return new MapSessionRepository();
    }

}
