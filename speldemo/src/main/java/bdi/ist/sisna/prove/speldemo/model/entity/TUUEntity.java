package bdi.ist.sisna.prove.speldemo.model.entity;

import bdi.ist.sisna.prove.speldemo.amlmodel.annotations.AMLModelFact;
import bdi.ist.sisna.prove.speldemo.amlmodel.annotations.AMLModelDimension;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TUUEntity {

	@AMLModelDimension
	private String vocesottvoc;

	@AMLModelFact
	private Double value1;
	@AMLModelFact
	private Integer value2;
	@AMLModelFact
	private String value3;
}
