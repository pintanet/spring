//https://www.baeldung.com/jackson-yaml
package it.pinta.prove.springboot.data.amlmodel.repo;

import it.pinta.prove.springboot.data.amlmodel.model.AmlModel;

public interface AmlModelRepo {
	public AmlModel read(String yamlFile);

	public void write(String yamlFile, AmlModel model);
}
