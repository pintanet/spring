package ist.bdi.sna.sisna.toolbox.model.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import ist.bdi.sna.sisna.toolbox.SisnaToolboxApplication;
import ist.bdi.sna.sisna.toolbox.model.entity.ColumnMapping;
import ist.bdi.sna.sisna.toolbox.model.entity.ConversionGroup;
import ist.bdi.sna.sisna.toolbox.model.entity.ConverterConfig;
import ist.bdi.sna.sisna.toolbox.model.entity.CustomValue;

@ActiveProfiles("test")
@SpringBootTest(classes = SisnaToolboxApplication.class, properties = { "spring.main.web-application-type=none",
		"spring.shell.interactive.enabled=false" })
class ConfigLoaderServiceTest {

	@Autowired
	private ConfigLoaderService configLoaderService;

	// ------------------------------------------
	// TEST 1: Caricamento Riuscito e Binding Completo
	// ------------------------------------------

	@Test
	void loadConfigurationManually_ShouldReturnConfig_WhenFileExistsAndIsValid() {
		// Corrisponde al file: src/test/resources/xlstosql/questionario-mapping.yml
		final String PROFILE_NAME = "questionario";

		// ACT
		ConverterConfig config = configLoaderService.loadConfigurationManually(PROFILE_NAME);

		// ASSERT
		assertNotNull(config, "La configurazione NON deve essere null.");

		// Verifica le proprietà di ConverterConfig
		assertEquals(PROFILE_NAME, config.getProfileName(),
				"Il 'profileName' del risultato deve corrispondere a quello del caricamento.");
		assertEquals("ROW", config.getProfileType(), "Il 'profileType' deve essere mappato.");
		assertEquals("dati/input.xlsx", config.getExcelFilePath(), "Il 'excelFilePath' deve essere mappato.");
		assertTrue(config.isHasHeaderRow(), "'hasHeaderRow' deve essere true.");

		// Verifica i Gruppi di Conversione
		assertNotNull(config.getConversionGroups(), "La lista conversionGroups NON deve essere null.");
		assertEquals(1, config.getConversionGroups().size(), "Deve esserci esattamente un ConversionGroup.");

		// Verifica il contenuto del primo gruppo (Group-Anagrafica)
		ConversionGroup group = config.getConversionGroups().get(0);
		assertEquals("Group-Anagrafica", group.getGroupName(), "Il 'groupName' deve essere corretto.");
		assertEquals("LIVE.ANAGRAFICA_CLIENTI", group.getOracleTableName(), "La tabella Oracle deve essere corretta.");
		assertEquals("output/anagrafica_inserts.sql", group.getSqlOutputFile(),
				"Il file SQL di output deve essere corretto.");

		// Verifica la Mappatura delle Colonne (mappings)
		List<ColumnMapping> mappings = group.getMappings();
		assertNotNull(mappings, "La lista di mappings NON deve essere null.");
		assertEquals(2, mappings.size(), "Devono essere mappate due colonne.");
		assertEquals("ID_PARTNER", mappings.get(0).getOracleColumn());
		assertEquals("A", mappings.get(0).getExcelColumn());

		// Verifica i Valori Personalizzati (customValues)
		List<CustomValue> customValues = group.getCustomValues();
		assertNotNull(customValues, "La lista customValues NON deve essere null.");
		assertEquals(2, customValues.size(), "Devono esserci due CustomValue.");
		assertEquals("DATA_CREAZIONE", customValues.get(1).getOracleColumn());
		assertEquals("EXPRESSION", customValues.get(1).getValueType());
	}

	// ------------------------------------------
	// TEST 2: File di Configurazione Non Trovato
	// ------------------------------------------

	@Test
	void loadConfigurationManually_ShouldReturnNull_WhenFileDoesNotExist() {
		// Profilo che punta a un file inesistente
		final String PROFILE_NAME = "nonEsistente";

		// ACT
		ConverterConfig config = configLoaderService.loadConfigurationManually(PROFILE_NAME);

		// ASSERT
		assertNull(config, "La configurazione deve essere null quando il file non è trovato.");
	}

	// ------------------------------------------
	// TEST 3: Fallimento del Binding (YAML con prefisso sbagliato)
	// ------------------------------------------

	@Test
	void loadConfigurationManually_ShouldReturnNull_WhenBindingFails() {
		// Profilo che corrisponde al file invalid-mapping.yml (con un prefisso diverso
		// da 'converter')
		final String PROFILE_NAME = "invalid";

		// ACT
		ConverterConfig config = configLoaderService.loadConfigurationManually(PROFILE_NAME);

		// ASSERT
		// Il servizio gestisce l'eccezione di binding e restituisce null.
		assertNull(config, "La configurazione deve essere null in caso di fallimento del binding.");
	}
}