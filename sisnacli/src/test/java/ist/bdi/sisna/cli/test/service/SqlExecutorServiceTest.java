package ist.bdi.sisna.cli.test.service;

import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DataSourceUtils;

import ist.bdi.sisna.cli.model.entity.ConversionGroup;
import ist.bdi.sisna.cli.model.entity.ConverterConfig;
import ist.bdi.sisna.cli.model.service.SqlExecutorService;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SqlExecutorServiceUnitTest {

	@Mock
	private Environment mockEnv;

	// Usiamo @InjectMocks per iniettare mockEnv in sqlExecutorService
	@InjectMocks
	private SqlExecutorService sqlExecutorService;

	// Mock delle dipendenze JDBC/File
	@Mock
	private DataSource mockDataSource;
	@Mock
	private Connection mockConnection;
	@Mock
	private Statement mockStatement;

	@TempDir
	Path tempDir;

	private static final String DB_USER = "testUser";
	private static final String DB_PASS = "testPass";
	private static final String MOCK_SQL_URL = "jdbc:oracle:thin:@//mock:1521/MOCKPDB1";
	private static final String MOCK_DRIVER = "oracle.jdbc.OracleDriver";
	private Path sqlFilePath;

	@BeforeEach
	void setup() throws SQLException, IOException {
		// --- Setup del Mock Environment ---
		when(mockEnv.getProperty("spring.datasource.url")).thenReturn(MOCK_SQL_URL);
		when(mockEnv.getProperty("spring.datasource.driver-class-name")).thenReturn(MOCK_DRIVER);

		// --- Mocking di DataSourceUtils/Connection ---
		// Visto che DataSourceUtils.getConnection è un metodo statico,
		// usiamo Mockito.mockStatic per intercettare la chiamata.

		// Questo mock deve essere mockato PRIMA del test e chiuso DOPO.
		// Lo gestiremo all'interno dei test case per semplicità.

		// --- Setup del File SQL Temporaneo ---
		sqlFilePath = tempDir.resolve("insert_test.sql");
		String sqlContent = "INSERT INTO TABLE (C1) VALUES ('RIGA 1');\n"
				+ "INSERT INTO TABLE (C1) VALUES ('RIGA 2');\n";
		Files.writeString(sqlFilePath, sqlContent);
	}

	// -------------------------------------------------------------------------
	// TEST CASE
	// -------------------------------------------------------------------------

	@Test
	void execute_realRun_shouldCallCommit() throws SQLException {
		ConverterConfig config = createMockConfig(sqlFilePath.toString());

		// Mocking del contesto JDBC a runtime
		try (MockedStatic<DataSourceUtils> mockedDataSourceUtils = mockStatic(DataSourceUtils.class,
				CALLS_REAL_METHODS)) {
			// Forziamo il getDataSource a restituire il mockConnection
			mockedDataSourceUtils.when(() -> DataSourceUtils.getConnection(any(DataSource.class)))
					.thenReturn(mockConnection);

			when(mockConnection.createStatement()).thenReturn(mockStatement);
			when(mockStatement.executeUpdate(anyString())).thenReturn(1); // Simula l'inserimento di 1 riga

			// 1. Esecuzione (dryRun = false)
			sqlExecutorService.execute(config, false, DB_USER, DB_PASS);

			// 2. Assertions
			// Verifica che il commit sia stato chiamato
			verify(mockConnection, times(1)).commit();
			// Verifica che il rollback NON sia stato chiamato
			verify(mockConnection, never()).rollback();
			// Verifica che le due righe SQL siano state eseguite
			verify(mockStatement, times(2)).executeUpdate(anyString());
		}
	}

	@Test
	void execute_dryRun_shouldCallRollback() throws SQLException {
		ConverterConfig config = createMockConfig(sqlFilePath.toString());

		try (MockedStatic<DataSourceUtils> mockedDataSourceUtils = mockStatic(DataSourceUtils.class,
				CALLS_REAL_METHODS)) {

			mockedDataSourceUtils.when(() -> DataSourceUtils.getConnection(any(DataSource.class)))
					.thenReturn(mockConnection);

			when(mockConnection.createStatement()).thenReturn(mockStatement);

			// RIGA RIMOSSA: Non serve mockare executeUpdate in dry run!
			// when(mockStatement.executeUpdate(anyString())).thenReturn(1);

			// 1. Esecuzione (dryRun = true)
			sqlExecutorService.execute(config, true, DB_USER, DB_PASS);

			// 2. Assertions
			verify(mockStatement, never()).executeUpdate(anyString());
			verify(mockConnection, times(1)).rollback();
			verify(mockConnection, never()).commit();
		}
	}

	@Test
	void execute_sqlError_shouldCallRollback() throws SQLException {
		ConverterConfig config = createMockConfig(sqlFilePath.toString());

		try (MockedStatic<DataSourceUtils> mockedDataSourceUtils = mockStatic(DataSourceUtils.class,
				CALLS_REAL_METHODS)) {
			mockedDataSourceUtils.when(() -> DataSourceUtils.getConnection(any(DataSource.class)))
					.thenReturn(mockConnection);

			when(mockConnection.createStatement()).thenReturn(mockStatement);

			// Simula il successo della prima riga
			when(mockStatement.executeUpdate(contains("RIGA 1"))).thenReturn(1);
			// Simula un errore SQL alla seconda riga
			when(mockStatement.executeUpdate(contains("RIGA 2")))
					.thenThrow(new SQLException("Errore di sintassi simulato"));

			// 1. Esecuzione (dryRun = false)
			sqlExecutorService.execute(config, false, DB_USER, DB_PASS);

			// 2. Assertions
			// Verifica che la prima riga sia stata tentata e la seconda abbia fallito
			verify(mockStatement, times(2)).executeUpdate(anyString());

			// Verifica che il rollback sia stato chiamato a causa dell'eccezione
			verify(mockConnection, times(1)).rollback();
			// Verifica che il commit NON sia stato chiamato
			verify(mockConnection, never()).commit();
		}
	}

	// -------------------------------------------------------------------------
	// METODI DI UTILITÀ
	// -------------------------------------------------------------------------

	private ConverterConfig createMockConfig(String sqlPath) {
		ConversionGroup group = new ConversionGroup();
		group.setSqlOutputFile(sqlPath);

		ConverterConfig config = new ConverterConfig();
		config.setProfileName("test-sql");
		config.setConversionGroups(Collections.singletonList(group));
		return config;
	}
}