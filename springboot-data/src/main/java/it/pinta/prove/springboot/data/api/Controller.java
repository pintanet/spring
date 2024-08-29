package it.pinta.prove.springboot.data.api;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import it.pinta.prove.springboot.data.metamodel.config.MetaModelConfig;
import it.pinta.prove.springboot.data.metamodel.config.YamlPropertySourceFactory;
import it.pinta.prove.springboot.data.persistence.model.User;
import it.pinta.prove.springboot.data.persistence.repo.UserRepository;

@RestController
@RequestMapping(path = "/pinta")
@PropertySource(ignoreResourceNotFound = true, value = "classpath:meta-model-config.yaml", factory = YamlPropertySourceFactory.class)
public class Controller {

	@Autowired
	private UserRepository repo;

	@Autowired
	private MetaModelConfig meta;

	@GetMapping(path = "/pippo")
	public String getPinta() {
		User u = repo.findById(1).orElse(new User());
		return u.getEmail();
	}

	@GetMapping(path = "/all")
	public Map<String, String> getAllUsers() {
		HashMap<String, String> map = new HashMap<>();
		map.put("key", "value");
		map.put("foo", "bar");
		map.put("aa", "bb");
		return map;
	}

	@GetMapping(path = "/custom")
	public Collection<User> getCustom() {
		return repo.getCustom();
	}

	@GetMapping(path = "/meta")
	public String getMeta() {
		return meta.getCreazioneUtente();
	}

}