package bdi.ist.sna.sisna.amlmodel.sisnaengine;

import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

public class Functions {

	public static List<Object> getBySettore(Data data, String settore, String variable) {
		return data.getSettore(settore, variable);
	}

	public static Object getByIdSna(Data data, String settore, String variable, String idSna) {
		return data.get(settore, variable, idSna);
	}

	public static Double norm(Data data, String settore, String variable, String idSna) {
		Double min = data.getSettore(settore, variable).stream().filter(Double.class::isInstance)
				.map(Double.class::cast).min(Comparator.comparing(Double::doubleValue))
				.orElseThrow(NoSuchElementException::new);

		Double max = data.getSettore(settore, variable).stream().filter(Double.class::isInstance)
				.map(Double.class::cast).max(Comparator.comparing(Double::doubleValue))
				.orElseThrow(NoSuchElementException::new);

		Double val = Double.parseDouble(data.get(settore, variable, idSna).toString());

		return (val - min) / (max - min);
	}

	public static Double min(List<Double> values) {
		return values.stream().min(Comparator.comparing(Double::doubleValue)).orElseThrow(NoSuchElementException::new);
	}

	public static Double max(List<Double> values) {
		return values.stream().max(Comparator.comparing(Double::doubleValue)).orElseThrow(NoSuchElementException::new);
	}

}
