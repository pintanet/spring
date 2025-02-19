package bdi.ist.sna.sisna.amlmodel.sisnaengine;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Data {

	private Map<String, Map<String, Map<String, Object>>> data;

	public void put(String settore, String variable, String idSna, Object value) {

		if (data == null)
			data = new HashMap();

		if (!data.containsKey(settore)) {
			Map<String, Object> values = new HashMap();
			values.put(idSna, value);

			Map<String, Map<String, Object>> vars = new HashMap<>();
			vars.put(variable, values);

			data.put(settore, vars);
		} else {
			if (!data.get(settore).containsKey(variable)) {
				Map<String, Object> values = new HashMap();
				values.put(idSna, value);

				data.get(settore).put(variable, values);
			} else {
				if (!data.get(settore).get(variable).containsKey(idSna)) {
					data.get(settore).get(variable).put(idSna, value);
				}
			}
		}
	}

	public Object get(String settore, String variable, String idSna) {
		Map<String, Object> values = getMapSettore(settore, variable);

		if ((values != null) && values.containsKey(idSna)) {
			return values.get(idSna);
		} else {
			return null;
		}
	}

	public Map<String, Object> getMapSettore(String settore, String variable) {
		if (data.containsKey(settore)) {
			if (data.get(settore).containsKey(variable)) {
				return data.get(settore).get(variable);
			}
		}

		return null;

	}

	public List<Object> getSettore(String settore, String variable) {
		Map<String, Object> map = getMapSettore(settore, variable);

		return (map != null) ? map.values().stream().collect(Collectors.toList()) : null;
	}
}
