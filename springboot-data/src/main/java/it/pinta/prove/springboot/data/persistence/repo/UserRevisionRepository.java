//https://satyacodes.gitlab.io/SpringBoot-Auditing-with-Hibernate-Envers-&-Spring-Data-JPA.html

package it.pinta.prove.springboot.data.persistence.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.history.RevisionRepository;

import it.pinta.prove.springboot.data.persistence.model.User;

public interface UserRevisionRepository extends RevisionRepository<User, Long, Integer>, JpaRepository<User, Long> {

}
