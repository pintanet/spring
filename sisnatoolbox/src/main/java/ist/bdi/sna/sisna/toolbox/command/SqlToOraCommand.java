package ist.bdi.sna.sisna.toolbox.command;

import org.hibernate.validator.internal.util.stereotypes.Lazy;
import org.jline.reader.LineReader;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import ist.bdi.sna.sisna.toolbox.model.entity.ConverterConfig;
import ist.bdi.sna.sisna.toolbox.model.service.ConfigLoaderService;
import ist.bdi.sna.sisna.toolbox.model.service.SqlExecutorService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ShellComponent
public class SqlToOraCommand {

	@Lazy
	@Autowired
	private SqlExecutorService sqlExecutorService;

	@Autowired
	private ConfigLoaderService configLoaderService;

	@Autowired
	private ObjectProvider<LineReader> lineReaderProvider;

	@ShellMethod(key = "sql-to-ora", value = "Esegue i file SQL di un profilo nel database Oracle.")
	public String execute(@ShellOption(help = "Nome del profilo", value = { "-p", "--profile" }) String profileName,
			@ShellOption(help = "Modalità Dry Run (default: true)", defaultValue = "true", value = { "-d",
					"--dry-run" }) boolean dryRun,
			@ShellOption(help = "Nome utente Oracle", defaultValue = ShellOption.NULL) String username,
			@ShellOption(help = "Password Oracle", defaultValue = ShellOption.NULL) String password) {

		return doExecutionShell(profileName, dryRun, username, password);
	}

	/**
	 * Logica di esecuzione core, riceve la configurazione già caricata. EVITA IL
	 * DOPPIO CARICAMENTO.
	 */
	private String doExecution(ConverterConfig config, boolean dryRun, String username, String password) {

		// La config è già stata caricata dal chiamante (execute o doExecutionShell)
		if (config == null) {
			return "❌ Errore interno: Configurazione mancante.";
		}

		try {
			sqlExecutorService.execute(config, dryRun, username, password);
			return String.format("✅ Esecuzione SQL completata per il profilo %s (Dry Run: %s).",
					config.getProfileName(), dryRun);
		} catch (Exception e) {
			log.error("Errore durante l'esecuzione.", e);
			return "❌ Errore durante l'esecuzione: " + e.getMessage();
		}
	}

	/**
	 * Logica di interazione Shell che carica la configurazione e gestisce l'input.
	 */
	private String doExecutionShell(String profileName, boolean dryRun, String username, String password) {
		// 1. Caricamento della configurazione (UNA VOLTA SOLA in tutto il flusso)
		ConverterConfig runtimeConfig = configLoaderService.loadConfigurationManually(profileName);

		if (runtimeConfig == null) {
			return "❌ Errore: File di configurazione non trovato per il profilo: " + profileName;
		}

		// 2. Logica di input interattiva
		LineReader lineReader = lineReaderProvider.getObject();

		if (lineReader == null) {
			log.error("Impossibile ottenere LineReader.");
			return "❌ Errore interno: Interfaccia CLI non disponibile.";
		}

		if (username == null || username.trim().isEmpty()) {
			username = lineReader.readLine("Inserisci Utente Oracle: ");
		}

		if (password == null || password.trim().isEmpty()) {
			char mask = '*';
			password = lineReader.readLine("Inserisci Password Oracle (nascosta): ", mask);
		}

		// 3. Esecuzione finale (passa la configurazione caricata)
		return doExecution(runtimeConfig, dryRun, username, password);
	}
}