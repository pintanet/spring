package ist.bdi.sisna.cli.test.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import ist.bdi.sisna.cli.model.entity.ConverterConfig;
import ist.bdi.sisna.cli.model.service.ConfigLoaderService;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = { "converter.active-profile=test" })
class ConfigLoaderServiceTest {

	@Autowired
	private ConfigLoaderService configLoaderService;

	// ----------------------------------------------------
	// TEST CASE
	// ----------------------------------------------------

	/**
	 * Testa il caricamento di un file di configurazione YAML valido e verifica il
	 * binding.
	 */
	@Test
	void loadConfigurationManually_success() {
		final String profileName = "test"; // Corrisponde a test-mapping.yml

		// 1. Esecuzione del caricamento
		ConverterConfig config = configLoaderService.loadConfigurationManually(profileName);

		// 2. Asserzioni di base
		assertNotNull(config, "La configurazione non dovrebbe essere NULL.");

		// 3. Asserzioni sui valori mappati
		assertEquals("test", config.getProfileName(), "Il nome del profilo dovrebbe essere 'test'.");
		assertEquals("dati/test_input.xlsx", config.getExcelFilePath(), "Il percorso del file Excel non corrisponde.");
		assertFalse(config.isHasHeaderRow(), "hasHeaderRow dovrebbe essere false.");

		// 4. Asserzioni sulla lista (ConversionGroups)
		assertNotNull(config.getConversionGroups(), "La lista dei gruppi di conversione non dovrebbe essere NULL.");
		assertEquals(1, config.getConversionGroups().size(), "Dovrebbe esserci un solo gruppo di conversione.");

		// 5. Asserzioni sul primo gruppo
		assertEquals("T_ANAGRAFICA", config.getConversionGroups().get(0).getOracleTableName());
		assertEquals(1, config.getConversionGroups().get(0).getMappings().size());
		assertEquals("CODICE_UNIVOCO", config.getConversionGroups().get(0).getMappings().get(0).getOracleColumn());
	}

	/**
	 * Testa il fallimento quando il file di configurazione non esiste.
	 */
	@Test
	void loadConfigurationManually_fileNotFound() {
		final String profileName = "non-esistente";

		// 1. Esecuzione del caricamento
		ConverterConfig config = configLoaderService.loadConfigurationManually(profileName);

		// 2. Asserzione
		assertNull(config, "La configurazione dovrebbe essere NULL se il file non è stato trovato.");
	}

}