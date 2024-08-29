package it.pinta.prove.springboot.data.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.envers.repository.config.EnableEnversRepositories;
import org.springframework.data.envers.repository.support.EnversRevisionRepositoryFactoryBean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import it.pinta.prove.springboot.data.persistence.audit.AuditorAwareImpl;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = {
		"it.pinta.prove.springboot.data.persistence" }, repositoryFactoryBeanClass = EnversRevisionRepositoryFactoryBean.class)
@EnableEnversRepositories
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class PersistenceConfig {

	@Bean
	public AuditorAware<String> auditorProvider() {
		return new AuditorAwareImpl();
	}
}
