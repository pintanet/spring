package ist.bdi.sna.sisna.toolbox.model.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Service;

import ist.bdi.sna.sisna.toolbox.model.entity.ConversionGroup;
import ist.bdi.sna.sisna.toolbox.model.entity.ConverterConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service("sqlExecutor")
@RequiredArgsConstructor
@Slf4j
public class SqlExecutorService {

	private final Environment env;

	/**
	 * Costruisce il DataSource a runtime utilizzando le credenziali passate e la
	 * configurazione da application.yaml.
	 */
	private DataSource getDataSource(String username, String password) {
		log.info("Configurazione connessione DB con utente: {}", username);
		try {
			return DataSourceBuilder.create().url(env.getProperty("spring.datasource.url"))
					.driverClassName(env.getProperty("spring.datasource.driver-class-name")).username(username)
					.password(password).build();
		} catch (Exception e) {
			log.error("Errore durante la creazione del DataSource: Verificare URL e Driver.", e);
			throw new RuntimeException("Impossibile connettersi al database.", e);
		}
	}

	/**
	 * Esegue i file SQL per il profilo specificato. * @param runtimeConfig
	 * Configurazione del profilo caricato.
	 * 
	 * @param dryRun   Se TRUE, simula l'esecuzione (no COMMIT).
	 * @param username Nome utente del DB.
	 * @param password Password del DB.
	 */
	public void execute(ConverterConfig runtimeConfig, boolean dryRun, String username, String password) {
		String profileName = runtimeConfig.getProfileName();
		log.info("Inizio esecuzione SQL per profilo: {} (Dry Run: {})", profileName, dryRun);

		DataSource dataSource = null;
		Connection conn = null;

		try {
			dataSource = getDataSource(username, password);
			// Ottiene una connessione e la lega al thread corrente (utile per Spring)
			conn = DataSourceUtils.getConnection(dataSource);

			// Disabilita l'auto-commit per gestire esplicitamente la transazione
			conn.setAutoCommit(false);
			log.info("Transazione avviata. AutoCommit disabilitato.");

			for (ConversionGroup group : runtimeConfig.getConversionGroups()) {
				executeSqlFile(conn, group.getSqlOutputFile(), dryRun);
			}

			// --- GESTIONE COMMIT/ROLLBACK ---
			if (dryRun) {
				conn.rollback();
				log.warn("Modalità DRY RUN attiva. Esecuzione simulata, è stato eseguito il ROLLBACK.");
			} else {
				conn.commit();
				log.info("Esecuzione completata. È stato eseguito il COMMIT sul database.");
			}

		} catch (Exception e) {
			log.error("Errore critico durante l'esecuzione del profilo {}. Eseguo ROLLBACK se la connessione è aperta.",
					profileName, e);
			try {
				if (conn != null) {
					conn.rollback();
					log.info("ROLLBACK eseguito a causa di un'eccezione.");
				}
			} catch (SQLException rollbackEx) {
				log.error("Errore durante il ROLLBACK.", rollbackEx);
			}
		} finally {
			// Rilascia la connessione in modo sicuro tramite Spring
			if (conn != null) {
				DataSourceUtils.releaseConnection(conn, dataSource);
			}
		}
	}

	/**
	 * Legge e esegue le istruzioni SQL da un singolo file.
	 */
	private void executeSqlFile(Connection conn, String sqlFilePath, boolean dryRun) throws IOException, SQLException {
		File sqlFile = new File(sqlFilePath);
		if (!sqlFile.exists()) {
			log.warn("File SQL non trovato: {}", sqlFilePath);
			return;
		}

		log.info("Elaborazione del file SQL: {}", sqlFilePath);
		int successfulUpdates = 0;
		int lineCount = 0;

		try (BufferedReader reader = new BufferedReader(new FileReader(sqlFile));
				Statement stmt = conn.createStatement()) {

			String line;
			while ((line = reader.readLine()) != null) {
				lineCount++;
				String sql = line.trim();

				if (sql.isEmpty() || sql.startsWith("--"))
					continue; // Salta righe vuote o commenti

				try {
					if (dryRun) {
						log.info("[DRY RUN] Simulata esecuzione riga {}: {}", lineCount,
								sql.substring(0, Math.min(sql.length(), 100)) + "...");
						successfulUpdates++;
					} else {
						// Esecuzione reale
						stmt.executeUpdate(sql);
						successfulUpdates++;
					}
				} catch (SQLException e) {
					log.error("Errore nell'esecuzione dell'istruzione SQL a riga {} del file {}: {}", lineCount,
							sqlFilePath, e.getMessage());
					// In caso di errore, lancia l'eccezione per forzare il rollback generale
					throw e;
				}
			}
			log.info("File {} elaborato. Righe eseguite/simulate con successo: {}", sqlFilePath, successfulUpdates);

		}
	}
}