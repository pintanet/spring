package bdi.ist.sisna.prove.speldemo.amlmodel.engine;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.HashMap;

import bdi.ist.sisna.prove.speldemo.amlmodel.annotations.AMLModelDimension;
import bdi.ist.sisna.prove.speldemo.amlmodel.annotations.AMLModelFact;
import bdi.ist.sisna.prove.speldemo.amlmodel.entity.AMLModelData;

public class AMLModelDataMapper<T> {

	private final Class<T> clazz = ((Class<T>) ((ParameterizedType) getClass().getGenericSuperclass())
			.getActualTypeArguments()[0]);

	public AMLModelData map(T obj) {

		boolean hasDimension = Arrays.stream(clazz.getDeclaredFields())
				.filter(f -> f.isAnnotationPresent(AMLModelDimension.class)).findAny().isPresent();
		boolean hasFact = Arrays.stream(clazz.getDeclaredFields())
				.filter(f -> f.isAnnotationPresent(AMLModelFact.class)).findAny().isPresent();
		if (!hasDimension || !hasFact)
			throw new RuntimeException(
					String.format("Flusso dati %s non valido per il modello AML", clazz.getSimpleName()));

		HashMap<String, Object> values = new HashMap<>();
		Arrays.stream(clazz.getDeclaredFields()).filter(f -> f.isAnnotationPresent(AMLModelFact.class)).forEach(f -> {
			try {
				String fact = f.getAnnotation(AMLModelFact.class).value().isBlank() ? f.getName()
						: f.getAnnotation(AMLModelFact.class).value();
				f.setAccessible(true);
				Object value = f.get(obj);
				values.put(fact, value);
			} catch (Exception ex) {
				throw new RuntimeException(ex);
			}
		});

		try {
			Field dimensionField = Arrays.stream(clazz.getDeclaredFields())
					.filter(f -> f.isAnnotationPresent(AMLModelDimension.class)).findFirst().orElseThrow();
			dimensionField.setAccessible(true);
			String dimension = dimensionField.get(obj).toString();
			return new AMLModelData(dimension, values);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}

	}
}
