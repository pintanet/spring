package bdi.ist.sisna.prove.speldemo.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import bdi.ist.sisna.prove.speldemo.amlmodel.engine.AMLModelDataMapper;
import bdi.ist.sisna.prove.speldemo.amlmodel.entity.AMLModelDataSet;
import bdi.ist.sisna.prove.speldemo.model.entity.TUUEntity;
import bdi.ist.sisna.prove.speldemo.model.entity.TUUEntityAMLModelDataMapper;
import bdi.ist.sisna.prove.speldemo.model.entity.TUUEntityAMLModelDataSet;

@Configuration
public class AMLModelConfiguration {

	@Bean
	public AMLModelDataMapper<TUUEntity> buildMapper() {
		return new TUUEntityAMLModelDataMapper();
	}

	@Bean
	public AMLModelDataSet<TUUEntity> buildDataSet() {
		return new TUUEntityAMLModelDataSet();
	}
}
