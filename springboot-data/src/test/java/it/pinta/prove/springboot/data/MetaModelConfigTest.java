package it.pinta.prove.springboot.data;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.TestPropertySource;

import it.pinta.prove.springboot.data.metamodel.config.MetaModelConfig;
import it.pinta.prove.springboot.data.metamodel.config.YamlPropertySourceFactory;
import lombok.extern.slf4j.Slf4j;

@SpringBootTest(classes = Main.class)
@TestPropertySource(factory = YamlPropertySourceFactory.class, value = "classpath:meta-model-config-test.yaml")
@Slf4j
public class MetaModelConfigTest {

	@Autowired
	private MetaModelConfig meta;

	@Bean
	public static PropertySourcesPlaceholderConfigurer properties() {
		PropertySourcesPlaceholderConfigurer pspc = new PropertySourcesPlaceholderConfigurer();
		Resource[] resources = new ClassPathResource[] { new ClassPathResource("foo.properties") };
		pspc.setLocations(resources);
		pspc.setIgnoreUnresolvablePlaceholders(true);
		return pspc;
	}

	@Test
	void testConfig() {
		Assertions.assertEquals("pippo", meta.getCreazioneUtente());
		log.info("Pippo = {}", meta.getCreazioneUtente());
	}

}
