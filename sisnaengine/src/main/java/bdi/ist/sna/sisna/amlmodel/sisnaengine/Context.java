package bdi.ist.sna.sisna.amlmodel.sisnaengine;

import java.util.List;

import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class Context extends StandardEvaluationContext {

	public Context() {
		super();
		try {
			registerFunction("getBySettore",
					Functions.class.getDeclaredMethod("getBySettore", Data.class, String.class, String.class));
			registerFunction("getByIdSna", Functions.class.getDeclaredMethod("getByIdSna", Data.class, String.class,
					String.class, String.class));
			registerFunction("norm",
					Functions.class.getDeclaredMethod("norm", Data.class, String.class, String.class, String.class));
			registerFunction("min", Functions.class.getDeclaredMethod("min", List.class));
			registerFunction("max", Functions.class.getDeclaredMethod("max", List.class));
		} catch (NoSuchMethodException e) {
			log.error("Exception during Sisna AML Modle evaluation", e);
		}
	}
}
