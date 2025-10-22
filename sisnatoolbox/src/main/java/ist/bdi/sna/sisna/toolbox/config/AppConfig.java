package ist.bdi.sna.sisna.toolbox.config;

import org.jline.utils.AttributedString;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.shell.jline.PromptProvider;

import ist.bdi.sna.sisna.toolbox.model.entity.ConverterConfig;

@Configuration
public class AppConfig {

	@Bean
	public ConverterConfig converterConfig() {
		ConverterConfig config = new ConverterConfig();

		return config;
	}

	@Bean
	PromptProvider promptProvider() {
		return () -> new AttributedString("TheGrid:>");
	}

}
