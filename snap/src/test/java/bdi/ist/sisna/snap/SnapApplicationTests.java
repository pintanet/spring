package bdi.ist.sisna.snap;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("h2")
@SpringBootTest(classes = SnapApplication.class)
class SnapApplicationTests {

	@Test
	void contextLoads() {
	}

}
