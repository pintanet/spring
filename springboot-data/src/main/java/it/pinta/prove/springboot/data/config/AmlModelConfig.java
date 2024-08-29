package it.pinta.prove.springboot.data.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature;

@Configuration
public class AmlModelConfig {

	@Bean
	public ObjectMapper buildYamlObjectMapper() {
		YAMLFactory factory = new YAMLFactory();
		factory.disable(Feature.WRITE_DOC_START_MARKER);

		ObjectMapper out = new ObjectMapper(factory);
		out.findAndRegisterModules();
		out.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

		return out;
	}

}
