package it.pinta.prove.springboot.data.metamodel.config;

import java.time.LocalTime;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@ConfigurationProperties(prefix = "sna.meta.amlmodel")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Configuration
public class MetaModelConfig {

	private Integer anno;
	private Integer versione;
	private Integer revisione;

	private LocalTime creazioneTS;
	private String creazioneUtente;

}
