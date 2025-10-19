package ist.bdi.sisna.cli.model.command;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import ist.bdi.sisna.cli.model.entity.ConverterConfig;
import ist.bdi.sisna.cli.model.service.ConfigLoaderService;
import ist.bdi.sisna.cli.model.service.XlsToSqlService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ShellComponent
public class XlsToSqlCommand implements AppCommand {

	@Autowired
	private XlsToSqlService xlsToSqlService;
	@Autowired
	private ConfigLoaderService configLoaderService;

	@Override
	public String getCommandName() {
		return "xls-to-sql";
	}

	@Override
	public String getCommandDescription() {
		return "Genera file SQL di INSERT dal file Excel di un dato profilo.";
	}

	// --- METODO PER LA MODALITÀ SHELL INTERATTIVA (già esistente) ---
	@ShellMethod(key = "xls-to-sql", value = "Genera file SQL di INSERT dal file Excel di un dato profilo.")
	public String executeShell(@ShellOption(help = "Nome del profilo (es. 'anagrafica')", value = { "-p",
			"--profile" }) String profileName) {

		// La logica è la stessa, rinominiamo il metodo per evitare conflitti
		return doConversion(profileName);
	}

	// --- METODO PER LA MODALITÀ BATCH (Nuova implementazione) ---
	@Override
	public void execute(ApplicationArguments args) throws Exception {
		// In modalità Batch, il primo argomento non-option dopo il nome del comando è
		// il valore.
		// Esempio: java -jar app.jar xls-to-sql anagrafica
		if (args.getNonOptionArgs().size() < 2) {
			throw new IllegalArgumentException(
					"Il comando 'xls-to-sql' in modalità BATCH richiede il nome del profilo come argomento.");
		}
		String profileName = args.getNonOptionArgs().get(1); // Il 1° argomento dopo il comando

		String result = doConversion(profileName);
		if (result.startsWith("❌")) {
			throw new Exception(result.substring(4)); // Lancia eccezione per segnalare l'errore al Runner
		}
	}

	// Metodo helper per la logica comune
	private String doConversion(String profileName) {
		ConverterConfig runtimeConfig = configLoaderService.loadConfigurationManually(profileName);
		if (runtimeConfig == null) {
			return "❌ Errore: File di configurazione non trovato per il profilo: " + profileName;
		}
		try {
			xlsToSqlService.convert(runtimeConfig);
			return String.format("✅ File SQL generati con successo per il profilo: %s.", profileName);
		} catch (Exception e) {
			log.error("Errore durante la conversione Batch.", e);
			return "❌ Errore durante la conversione: " + e.getMessage();
		}
	}
}