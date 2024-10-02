package it.pinta.prove.springboot.data;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import it.pinta.prove.springboot.data.amlmodel.model.AmlModel;
import it.pinta.prove.springboot.data.amlmodel.model.Flusso;
import it.pinta.prove.springboot.data.amlmodel.repo.AmlModelRepo;
import lombok.extern.slf4j.Slf4j;

@SpringBootTest(classes = Main.class)
@Slf4j
public class AmlModelTest {

	@Autowired
	private AmlModelRepo repo;

	@Test
	public void testSaveAmlModel() {
		String yamlFile = "src/test/resources/aml.model.yaml";

		AmlModel model = AmlModel.builder().anno(2023).version(1).revision(1).time(LocalDateTime.now()).build();
		model.getFlusso().add(Flusso.builder().nome("pippo").condizione("condizione1").sorgente("sorgente1").build());
		model.getFlusso().add(Flusso.builder().nome("pluto").condizione("condizione2").sorgente("sorgente2").build());

		repo.write(yamlFile, model);
	}

	@Test
	public void testLoadAmlModel() {

		String yamlFile = "src/test/resources/aml.model.test.yaml";

		AmlModel model = repo.read(yamlFile);

		log.info(model.toString());
	}
}
