package it.pinta.prove.springboot.data;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.history.Revisions;
import org.springframework.test.annotation.Rollback;

import it.pinta.prove.springboot.data.persistence.model.User;
import it.pinta.prove.springboot.data.persistence.repo.UserRepository;
import it.pinta.prove.springboot.data.persistence.repo.UserRevisionRepository;
import lombok.extern.slf4j.Slf4j;

@DataJpaTest(properties = { "spring.datasource.url=jdbc:mysql://localhost:3306/PINTA",
		"spring.jpa.hibernate.ddl-auto=update", "spring.datasource.username=root",
		"spring.datasource.password=frederik", "spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver",
		"spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect" })
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Slf4j
public class JpaAuditTest {

	@Autowired
	private UserRepository repo;

	@Autowired
	private UserRevisionRepository repoRev;

	@Test
	@Rollback(false)
	public void testCreateUser() {
		User user = User.builder().name("puppo").email("puppo@mail.com").build();

		repo.save(user);
	}

	@Test
	@Rollback(false)
	public void testUpdateUserSix() {
		User user = repo.findById(6).orElse(new User());

		user.setEmail(user.getEmail() + ".it");

		repo.save(user);
	}

	@Test
	@Rollback(false)
	public void testLogUserSixHistory() {
		Revisions<Integer, User> userRevisions = repoRev.findRevisions(6L);
		userRevisions.stream().map(r -> r.getEntity()).forEach(u -> log.info(u.toString()));
	}
}
