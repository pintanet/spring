package ist.bdi.sna.sisna.toolbox.datarcv.command;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import ist.bdi.sna.sisna.toolbox.datarcv.model.entity.ConverterConfig;
import ist.bdi.sna.sisna.toolbox.datarcv.model.service.ConfigLoaderService;
import ist.bdi.sna.sisna.toolbox.datarcv.model.service.xlstosql.XlsToSqlExecutorService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ShellComponent
public class XlsToSqlCommand {

	@Autowired
	private ConfigLoaderService configLoaderService;

	@Autowired
	private XlsToSqlExecutorService xlsToSqlExecutorService;

	@ShellMethod(key = "xls-to-sql", value = "Genera file SQL di INSERT dal file Excel di un dato profilo.")
	public String execute(@ShellOption(help = "Nome del profilo (es. 'anagrafica')", value = { "-p",
			"--profile" }) String profileName) {

		ConverterConfig runtimeConfig = configLoaderService.loadConfigurationManually(profileName);

		if (runtimeConfig == null) {
			return "❌ Errore: File di configurazione non trovato per il profilo: " + profileName;
		}

		try {
			xlsToSqlExecutorService.execute(runtimeConfig, false, null, null);

			return String.format("✅ File SQL generati con successo per il profilo '%s' (Tipo: %s).", profileName,
					runtimeConfig.getProfileType());

		} catch (Exception e) {
			log.error("Errore durante la conversione.", e);
			return "❌ Errore durante la conversione: " + e.getMessage();
		}
	}
}