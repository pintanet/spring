package ist.bdi.sna.sisna.toolbox.test;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import ist.bdi.sna.sisna.toolbox.SisnaToolboxApplication;

@ActiveProfiles("test")
@SpringBootTest(classes = SisnaToolboxApplication.class, properties = { "spring.main.web-application-type=none",
		"spring.shell.interactive.enabled=false" })
class SisnaCliApplicationTests {

	@Test
	void contextLoads() {
	}
}