package it.pinta.prove.springboot.data.persistence.repo;

import java.util.Collection;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import it.pinta.prove.springboot.data.persistence.model.User;

public interface UserRepository extends CrudRepository<User, Integer> {

	@Query(value = "SELECT u FROM User u where u.email like '%@%'")
	public Collection<User> getCustom();
}