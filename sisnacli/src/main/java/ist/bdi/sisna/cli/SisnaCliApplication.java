package ist.bdi.sisna.cli;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SisnaCliApplication {

	public static void main(String[] args) {
//		SpringApplication.run(SisnaCliApplication.class, args);
		SpringApplication application = new SpringApplication(SisnaCliApplication.class);
		application.setWebApplicationType(WebApplicationType.NONE);
		application.run(args);
	}
}
