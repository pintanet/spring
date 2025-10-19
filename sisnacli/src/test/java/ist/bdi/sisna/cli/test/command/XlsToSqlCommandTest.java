package ist.bdi.sisna.cli.test.command;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import ist.bdi.sisna.cli.model.command.XlsToSqlCommand;
import ist.bdi.sisna.cli.model.entity.ConverterConfig;
import ist.bdi.sisna.cli.model.service.ConfigLoaderService;
import ist.bdi.sisna.cli.model.service.SqlExecutorService;
import ist.bdi.sisna.cli.model.service.XlsToSqlService;

@SpringBootTest
class XlsToSqlCommandTest {

	@Autowired
	private XlsToSqlCommand xlsToSqlCommand;

	// Servizi iniettati come mock (per verificare le chiamate)
	@Autowired
	private XlsToSqlService mockXlsToSqlService;

	@Autowired
	private ConfigLoaderService mockConfigLoaderService;

	// ----------------------------------------------------
	// CONFIGURAZIONE DI MOCKING
	// ----------------------------------------------------
	@TestConfiguration
	static class TestServiceMocks {

		// Mock XlsToSqlService per verificarne la chiamata al metodo 'convert'
		@Bean
		@Primary
		XlsToSqlService mockXlsToSqlService() {
			return mock(XlsToSqlService.class);
		}

		// Mock ConfigLoaderService per simularne il successo/fallimento nel caricamento
		@Bean
		@Primary
		ConfigLoaderService mockConfigLoaderService() {
			return mock(ConfigLoaderService.class);
		}

		// Mock SqlExecutorService (necessario per il contesto completo di Spring Shell)
		@Bean
		@Primary
		SqlExecutorService mockSqlExecutorService() {
			return mock(SqlExecutorService.class);
		}
	}

	/**
	 * Resetta lo stato e lo storico dei mock prima di ogni test.
	 */
	@BeforeEach
	void setup() {
		// Resetta lo storico delle chiamate (interazioni)
		reset(mockConfigLoaderService);
		reset(mockXlsToSqlService);

		// Resetta il comportamento (i 'when')
		// In questo modo, l'istruzione 'when' nel test è l'unica attiva.
	}

	// ----------------------------------------------------
	// TEST CASE
	// ----------------------------------------------------

	/**
	 * Testa l'esecuzione di successo del comando con un profilo valido.
	 */
	@Test
	void execute_successfulConversion() throws Exception {
		final String profileName = "profilo-test";

		// 1. Setup Mock ConfigLoaderService (simula il caricamento di una
		// configurazione valida)
		ConverterConfig mockConfig = new ConverterConfig();
		mockConfig.setProfileName(profileName);
		when(mockConfigLoaderService.loadConfigurationManually(profileName)).thenReturn(mockConfig);

		// NOTA: Non è necessario mockare il comportamento di xlsToSqlService.convert(),
		// basta verificare che sia chiamato.

		// 2. Esecuzione del Comando
		String result = xlsToSqlCommand.executeShell(profileName);

		// 3. Asserzioni
		// Verifica che il risultato sia di successo
		assertTrue(result.startsWith("✅"), "Il comando dovrebbe restituire un messaggio di successo.");

		// Verifica che ConfigLoaderService sia stato chiamato
		verify(mockConfigLoaderService, times(1)).loadConfigurationManually(profileName);

		// Verifica che XlsToSqlService.convert sia stato chiamato una volta con la
		// configurazione corretta
		verify(mockXlsToSqlService, times(1)).convert(mockConfig);
	}

	/**
	 * Testa il fallimento quando il file di configurazione non viene trovato.
	 */
	@Test
	void execute_configurationNotFound() throws Exception {
		final String profileName = "profilo-non-esistente";

		// 1. Setup Mock ConfigLoaderService (simula il fallimento del caricamento)
		// L'istruzione 'when' viene eseguita dopo il reset.
		when(mockConfigLoaderService.loadConfigurationManually(profileName)).thenReturn(null);

		// 2. Esecuzione del Comando
		String result = xlsToSqlCommand.executeShell(profileName);

		// 3. Asserzioni
		assertTrue(result.startsWith("❌ Errore: File di configurazione non trovato"),
				"Il comando dovrebbe restituire un errore di configurazione.");

		// Verifica che XlsToSqlService NON sia stato chiamato
		// Questa asserzione è ora più affidabile grazie al reset.
		verify(mockXlsToSqlService, never()).convert(any());

		// Se il tuo test per il successo (execute_successfulConversion) falliva,
		// prova a riattivarlo. Il reset qui dovrebbe risolvere molti problemi di
		// contesto.
	}

	/**
	 * Testa il fallimento quando il servizio di conversione lancia un'eccezione
	 * (es. FileNotFound o errore Excel).
	 */
	@Test
	void execute_conversionFails() throws Exception {
		final String profileName = "profilo-con-errore";

		// 1. Setup Mock ConfigLoaderService
		ConverterConfig mockConfig = new ConverterConfig();
		when(mockConfigLoaderService.loadConfigurationManually(profileName)).thenReturn(mockConfig);

		// 2. Setup Mock XlsToSqlService (simula un errore IOException)
		doThrow(new java.io.IOException("Errore simulato di lettura file Excel")).when(mockXlsToSqlService)
				.convert(any());

		// 3. Esecuzione del Comando
		String result = xlsToSqlCommand.executeShell(profileName);

		// 4. Asserzioni
		// Verifica che il risultato indichi un errore di conversione
		assertTrue(result.startsWith("❌ Errore durante la conversione:"),
				"Il comando dovrebbe catturare l'eccezione e restituire un messaggio di errore.");

		// Verifica che il servizio sia stato comunque chiamato
		verify(mockXlsToSqlService, times(1)).convert(mockConfig);
	}
}