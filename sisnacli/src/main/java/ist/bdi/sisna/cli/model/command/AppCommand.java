package ist.bdi.sisna.cli.model.command;

import org.springframework.boot.ApplicationArguments;

public interface AppCommand {
	String getCommandName();

	String getCommandDescription();

	void execute(ApplicationArguments args) throws Exception;
}
