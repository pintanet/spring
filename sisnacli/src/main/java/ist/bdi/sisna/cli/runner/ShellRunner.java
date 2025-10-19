package ist.bdi.sisna.cli.runner;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import ist.bdi.sisna.cli.model.command.AppCommand;

@Component
public class ShellRunner implements ApplicationRunner {

	private final ApplicationContext applicationContext;
	private final Map<String, AppCommand> availableCommands;

	// Spring inietta tutti i bean di tipo AppCommand in una lista,
	// che convertiamo subito in una mappa per un accesso rapido per nome.
	public ShellRunner(ApplicationContext applicationContext, List<AppCommand> commands) {
		this.applicationContext = applicationContext;
		this.availableCommands = commands.stream()
				.collect(Collectors.toMap(AppCommand::getCommandName, command -> command));
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {
		List<String> nonOptionArgs = args.getNonOptionArgs();

		// 1. Nessun argomento BATCH: Avvia la shell interattiva (Spring Shell lo farà
		// automaticamente)
		if (nonOptionArgs.isEmpty()) {
			return;
		}

		// 2. Modalità BATCH: Esegui il comando

		// Il primo argomento è il nome del comando da eseguire (es. "convert")
		String commandName = nonOptionArgs.get(0).toLowerCase();

		AppCommand command = availableCommands.get(commandName);

		if (command == null) {
			System.err.println("❌ Errore BATCH: Comando non riconosciuto: " + commandName);
			System.err.println("Comandi disponibili: " + availableCommands.keySet());
			SpringApplication.exit(applicationContext, () -> 1);
			return;
		}

		try {
			// Esegui il comando trovato
			command.execute(args);
			System.out.println("✨ Esecuzione BATCH completata.");

		} catch (Exception e) {
			System.err.println("❌ Errore durante l'esecuzione del comando '" + commandName + "': " + e.getMessage());
			SpringApplication.exit(applicationContext, () -> 1);
			return;
		}

		// Forziamo la terminazione dopo l'esecuzione in modalità batch
		SpringApplication.exit(applicationContext, () -> 0);
	}
}