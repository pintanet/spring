package ist.bdi.sisna.cli.model.command;

import org.hibernate.validator.internal.util.stereotypes.Lazy;
import org.jline.reader.LineReader;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import ist.bdi.sisna.cli.model.entity.ConverterConfig;
import ist.bdi.sisna.cli.model.service.ConfigLoaderService;
import ist.bdi.sisna.cli.model.service.SqlExecutorService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ShellComponent
public class SqlToOraCommand implements AppCommand {

	@Lazy
	@Autowired
	private SqlExecutorService sqlExecutorService;

	@Autowired
	private ConfigLoaderService configLoaderService;

	// LineReader viene iniettato tramite ObjectProvider per evitare problemi
	// in contesti non Shell
	@Autowired
	private ObjectProvider<LineReader> lineReaderProvider;

	@Override
	public String getCommandName() {
		return "sql-to-ora";
	}

	@Override
	public String getCommandDescription() {
		return "Esegue i file SQL di un profilo nel database Oracle (con Dry Run opzionale).";
	}

	// --------------------------------------------------------------------------------
	// --- 1. MODALITÀ SHELL INTERATTIVA (@ShellMethod) ---
	// --------------------------------------------------------------------------------

	@ShellMethod(key = "sql-to-ora", value = "Esegue i file SQL di un profilo nel database Oracle.")
	public String executeShell(
			@ShellOption(help = "Nome del profilo", value = { "-p", "--profile" }) String profileName,
			@ShellOption(help = "Modalità Dry Run (default: true)", defaultValue = "true", value = { "-d",
					"--dry-run" }) boolean dryRun,
			@ShellOption(help = "Nome utente Oracle", defaultValue = ShellOption.NULL) String username,
			@ShellOption(help = "Password Oracle", defaultValue = ShellOption.NULL) String password) {

		return doExecutionShell(profileName, dryRun, username, password);
	}

	// --------------------------------------------------------------------------------
	// --- 2. MODALITÀ BATCH (AppCommand.execute) ---
	// --------------------------------------------------------------------------------

	@Override
	public void execute(ApplicationArguments args) throws Exception {
		// 1. Estrai il nome del profilo
		if (args.getNonOptionArgs().size() < 2) {
			throw new IllegalArgumentException(
					"Il comando 'sql-to-ora' in modalità BATCH richiede il nome del profilo come argomento.");
		}
		String profileName = args.getNonOptionArgs().get(1);

		// Caricamento della configurazione (necessario solo qui, non in doExecution)
		ConverterConfig config = configLoaderService.loadConfigurationManually(profileName);
		if (config == null) {
			throw new IllegalArgumentException(
					"Errore BATCH: File di configurazione non trovato per il profilo: " + profileName);
		}

		// 2. Estrai Dry Run
		boolean dryRun = args.containsOption("dry-run"); // Più semplice: true se l'opzione è presente
		if (args.containsOption("d")) {
			dryRun = true;
		}

		// 3. Estrai Utente e Password
		String username = args.containsOption("username") ? args.getOptionValues("username").get(0) : null;
		String password = args.containsOption("password") ? args.getOptionValues("password").get(0) : null;

		if (username == null || password == null) {
			throw new IllegalArgumentException(
					"Il comando 'sql-to-ora' in modalità BATCH richiede --username e --password.");
		}

		// 4. Esecuzione
		String result = doExecution(config, dryRun, username, password); // Passa l'oggetto config
		if (result.startsWith("❌")) {
			throw new Exception(result.substring(4));
		}
	}

	// --------------------------------------------------------------------------------
	// --- 3. HELPER METHODS ---
	// --------------------------------------------------------------------------------

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