package bdi.ist.sisna.prove.speldemo.amlmodel.entity;

import java.util.ArrayList;
import java.util.List;

import bdi.ist.sisna.prove.speldemo.amlmodel.engine.AMLModelDataMapper;

public class AMLModelDataSet<T> extends ArrayList<AMLModelData> {

	private static final long serialVersionUID = 8557742210101293918L;

	private AMLModelDataMapper<T> mapper = new AMLModelDataMapper<>();

	public AMLModelDataSet<T> load(List<T> data) {
		if (data != null) {
			data.stream().forEach(d -> {
				this.add(mapper.map(d));
			});
		}

		return this;
	}

}
