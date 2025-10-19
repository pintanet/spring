package ist.bdi.sisna.cli.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import ist.bdi.sisna.cli.model.entity.ConverterConfig;

@Configuration
public class AppConfig {

	@Bean
	public ConverterConfig converterConfig() {
		ConverterConfig config = new ConverterConfig();

		return config;
	}
}
