package ist.bdi.sisna.cli.test.command;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyChar;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.jline.reader.LineReader;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import ist.bdi.sisna.cli.model.command.SqlToOraCommand;
import ist.bdi.sisna.cli.model.entity.ConverterConfig;
import ist.bdi.sisna.cli.model.service.ConfigLoaderService;
import ist.bdi.sisna.cli.model.service.SqlExecutorService;
import ist.bdi.sisna.cli.model.service.XlsToSqlService;

@SpringBootTest
class SqlToOraCommandTest {

	@Autowired
	private SqlToOraCommand sqlToOraCommand;

	// Servizi iniettati come mock
	@Autowired
	private SqlExecutorService mockSqlExecutorService;

	@Autowired
	private ConfigLoaderService mockConfigLoaderService;

	@Autowired
	private LineReader mockLineReader;

	// ----------------------------------------------------
	// CONFIGURAZIONE DI MOCKING PER I SERVIZI
	// ----------------------------------------------------
	@TestConfiguration
	static class TestServiceMocks {

		// Mockiamo anche XlsToSqlService per mantenere il contesto pulito
		@Bean
		@Primary
		XlsToSqlService mockXlsToSqlService() {
			return mock(XlsToSqlService.class);
		}

		@Bean
		@Primary
		SqlExecutorService mockSqlExecutorService() {
			return mock(SqlExecutorService.class);
		}

		// Mockiamo ConfigLoaderService per controllare il caricamento della
		// configurazione
		@Bean
		@Primary
		ConfigLoaderService mockConfigLoaderService() {
			return mock(ConfigLoaderService.class);
		}

		// Mockiamo LineReader per simulare l'input interattivo
		@Bean
		@Primary
		LineReader mockLineReader() {
			return mock(LineReader.class);
		}
	}

	// ----------------------------------------------------
	// TEST CASE
	// ----------------------------------------------------

	/**
	 * Testa l'esecuzione del comando in modalità dry-run, simulando l'input
	 * interattivo per username e password.
	 */
	@Test
	void execute_interactiveCredentials_dryRunMode() throws Exception {
		final String profileName = "profilo-test";
		final String expectedUser = "TEST_USER";
		final String expectedPass = "SECRET_PASS";

		// 1. Setup Mock ConfigLoaderService
		ConverterConfig mockConfig = new ConverterConfig();
		mockConfig.setProfileName(profileName);
		when(mockConfigLoaderService.loadConfigurationManually(profileName)).thenReturn(mockConfig);

		// 2. Setup Mock LineReader (Simulazione Input Interattivo)
		// Quando il comando chiede l'username, restituisci expectedUser
		when(mockLineReader.readLine(eq("Inserisci Utente Oracle: "))).thenReturn(expectedUser);

		// Quando il comando chiede la password (con maschera), restituisci expectedPass
		when(mockLineReader.readLine(eq("Inserisci Password Oracle (nascosta): "), eq('*'))).thenReturn(expectedPass);

		// 3. Esecuzione del Comando
		// Chiamiamo il comando senza username e password, forzando l'interazione con
		// LineReader
		String result = sqlToOraCommand.executeShell(profileName, // profileName
				true, // dryRun = true
				null, // username = null (richiede input)
				null // password = null (richiede input)
		);

		// 4. Asserzioni
		// Verifica che il risultato sia di successo
		assertTrue(result.startsWith("✅"), "Il comando dovrebbe restituire un messaggio di successo.");

		// Verifica che ConfigLoaderService sia stato chiamato
		verify(mockConfigLoaderService, times(2)).loadConfigurationManually(profileName);

		// Verifica che LineReader sia stato chiamato per l'username e la password
		verify(mockLineReader, times(1)).readLine(eq("Inserisci Utente Oracle: "));
		verify(mockLineReader, times(1)).readLine(eq("Inserisci Password Oracle (nascosta): "), eq('*'));

		// Verifica che SqlExecutorService sia stato chiamato con i parametri corretti
		verify(mockSqlExecutorService, times(1)).execute(eq(mockConfig), eq(true), // dryRun = true
				eq(expectedUser), eq(expectedPass));
	}

	/**
	 * Testa l'esecuzione quando le credenziali sono fornite come opzioni (non
	 * interattivo).
	 */
	@Test
	void execute_optionsCredentials_realRunMode() throws Exception {
		final String profileName = "profilo-test";
		final String testUser = "ADMIN";
		final String testPass = "ADMIN_PASS";

		ConverterConfig mockConfig = new ConverterConfig();
		when(mockConfigLoaderService.loadConfigurationManually(profileName)).thenReturn(mockConfig);

		// Esecuzione del Comando passando tutti i parametri
		String result = sqlToOraCommand.executeShell(profileName, false, // dryRun = false
				testUser, // username fornito
				testPass // password fornita
		);

		// Verifica che il risultato sia di successo
		assertTrue(result.startsWith("✅"));

		// Verifica che LineReader NON sia stato chiamato (credanziali fornite)
		verify(mockLineReader, never()).readLine(anyString());
		verify(mockLineReader, never()).readLine(anyString(), anyChar());

		// Verifica che SqlExecutorService sia stato chiamato correttamente
		verify(mockSqlExecutorService, times(1)).execute(eq(mockConfig), eq(false), // dryRun = false
				eq(testUser), eq(testPass));
	}
}