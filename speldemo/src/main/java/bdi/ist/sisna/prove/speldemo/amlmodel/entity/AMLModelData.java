package bdi.ist.sisna.prove.speldemo.amlmodel.entity;

import java.util.HashMap;
import java.util.Hashtable;

public class AMLModelData extends Hashtable<String, HashMap<String, Object>> {

	private static final long serialVersionUID = 1L;

	public AMLModelData(String dimension, HashMap<String, Object> values) {
		this.put(dimension, values);
	}
}
