package ist.bdi.sna.sisna.toolbox.model.service;

import java.util.Properties;

import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import ist.bdi.sna.sisna.toolbox.model.entity.ConverterConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConfigLoaderService {

	private final ResourceLoader resourceLoader;

	// Metodo per caricare la configurazione di un profilo specifico
	public ConverterConfig loadConfigurationManually(String profileName) {
		String configPath = String.format("classpath:xlstosql/%s-mapping.yml", profileName);
		Resource resource = resourceLoader.getResource(configPath);

		if (!resource.exists()) {
			log.error("File di configurazione non trovato per il profilo: {}", profileName);
			return null;
		}

		try {
			// 1. Legge il file YAML in un oggetto Properties
			YamlPropertiesFactoryBean factory = new YamlPropertiesFactoryBean();
			factory.setResources(resource);
			Properties properties = factory.getObject();

			if (properties == null) {
				log.error("Impossibile leggere le proprietà dal file YAML: {}", configPath);
				return null;
			}

			// 2. Utilizza il Binder di Spring per mappare le proprietà caricate all'oggetto
			// ConverterConfig
			StandardEnvironment env = new StandardEnvironment();
			env.getPropertySources().addLast(new PropertiesPropertySource("runtimeConfig", properties));

			Binder binder = Binder.get(env);

			// Il binding avviene sul prefisso "converter" come definito in ConverterConfig
			ConverterConfig config = binder.bind("converter", ConverterConfig.class)
					.orElseThrow(() -> new IllegalStateException("Binding della configurazione fallito."));

			// Imposta il nome del profilo per coerenza
			config.setProfileName(profileName);
			return config;

		} catch (Exception e) {
			log.error("Errore critico durante il caricamento manuale della configurazione per {}: {}", profileName,
					e.getMessage(), e);
			return null;
		}
	}
}
