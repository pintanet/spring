package it.pinta.demo.jpa.mysql.model.repository;

import org.springframework.data.repository.CrudRepository;

import it.pinta.demo.jpa.mysql.model.entity.User;

public interface UserRepository extends CrudRepository<User, Integer> {

}
