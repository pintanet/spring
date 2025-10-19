package ist.bdi.sisna.cli.model.entity;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Data
@Configuration
@ConfigurationProperties(prefix = "converter")
public class ConverterConfig {
	private String profileName;
	private String excelFilePath;
	private boolean hasHeaderRow = true;
	private List<ConversionGroup> conversionGroups;
}