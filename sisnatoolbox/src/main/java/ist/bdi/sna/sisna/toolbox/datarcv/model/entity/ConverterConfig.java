package ist.bdi.sna.sisna.toolbox.datarcv.model.entity;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Data
@Configuration
@ConfigurationProperties(prefix = "converter")
public class ConverterConfig {
	private String profileName;
	private String profileType; // Valori: "ROW", "SHEET"
	private String excelFilePath; // Nome del singolo file (per ROW/SHEET)
	private String excelDirectoryPath; // NUOVO: Directory dei file (per SHEET)
	private boolean hasHeaderRow = true;
	private List<ConversionGroup> conversionGroups;
}