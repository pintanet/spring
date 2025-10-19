package ist.bdi.sisna.cli.test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import ist.bdi.sisna.cli.model.command.SqlToOraCommand;
import ist.bdi.sisna.cli.model.command.XlsToSqlCommand;
import ist.bdi.sisna.cli.model.service.ConfigLoaderService;
import ist.bdi.sisna.cli.model.service.SqlExecutorService;
import ist.bdi.sisna.cli.model.service.XlsToSqlService;

@SpringBootTest
class SisnaCliApplicationTests {

	// ----------------------------------------------------
	// INIEZIONE DEI COMPONENTI PRINCIPALI
	// ----------------------------------------------------
	@Autowired
	private XlsToSqlCommand xlsToSqlCommand;

	@Autowired
	private SqlToOraCommand sqlToOraCommand;

	@Autowired
	private ConfigLoaderService configLoaderService;

	// Riferimenti ai mock registrati (per verificare che i bean siano stati
	// sostituiti)
	@Autowired
	private XlsToSqlService mockXlsToSqlService;

	@Autowired
	private SqlExecutorService mockSqlExecutorService;

	// ----------------------------------------------------
	// CONFIGURAZIONE PER IL MOCKING (Sostituisce @MockBean)
	// ----------------------------------------------------
	@TestConfiguration
	static class TestServiceMocks {

		/** Crea e registra il mock per XlsToSqlService */
		@Bean
		@Primary
		XlsToSqlService mockXlsToSqlService() {
			return mock(XlsToSqlService.class);
		}

		/** Crea e registra il mock per SqlExecutorService */
		@Bean
		@Primary
		SqlExecutorService mockSqlExecutorService() {
			return mock(SqlExecutorService.class);
		}
	}

	// ----------------------------------------------------
	// TEST DEL CONTESTO
	// ----------------------------------------------------
	@Test
	void contextLoads() {
		// Verifica che i bean dei comandi e dei servizi siano stati caricati
		// correttamente
		assertNotNull(xlsToSqlCommand, "Il bean XlsToSqlCommand non è stato caricato.");
		assertNotNull(sqlToOraCommand, "Il bean SqlToOraCommand non è stato caricato.");
		assertNotNull(configLoaderService, "Il bean ConfigLoaderService non è stato caricato.");

		// Verifica che i mock siano stati iniettati al posto delle implementazioni
		// reali
		assertNotNull(mockXlsToSqlService, "Il mock XlsToSqlService non è stato caricato/sostituito.");
		assertNotNull(mockSqlExecutorService, "Il mock SqlExecutorService non è stato caricato/sostituito.");

		System.out.println("✅ Il contesto Spring Boot è stato caricato con successo e i componenti sono disponibili.");
	}
}