package bdi.ist.sisna.prove.speldemo;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import bdi.ist.sisna.prove.speldemo.amlmodel.entity.AMLModelData;
import bdi.ist.sisna.prove.speldemo.model.entity.TUUEntity;
import bdi.ist.sisna.prove.speldemo.model.entity.TUUEntityAMLModelDataMapper;
import bdi.ist.sisna.prove.speldemo.model.entity.TUUEntityAMLModelDataSet;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest(classes = SpelDemoApplication.class)
class SpelDemoApplicationTests {

	@Test
	void spel2() {
		TUUEntity en = TUUEntity.builder().vocesottvoc("0000").value1(0.0D).build();
		TUUEntityAMLModelDataMapper mapper = new TUUEntityAMLModelDataMapper();
		AMLModelData data = mapper.map(en);

		log.info(data.toString());
	}

	@Test
	void spel() {
		try {
			List<TUUEntity> list = buildData();

			TUUEntityAMLModelDataSet dataSet = new TUUEntityAMLModelDataSet();
			dataSet.load(list);

			EvaluationContext context = new StandardEvaluationContext();
			context.setVariable("data", dataSet);

			String expr = "#data.get('1111').get('value1')+ #data.get('2222').get('value1')";
			ExpressionParser expressionParser = new SpelExpressionParser();
			Expression expression = expressionParser.parseExpression(expr);

			String result = String.valueOf(expression.getValue(context));

			log.info(result);
		} catch (Exception ex) {
			log.error(ex.getLocalizedMessage());
		}
	}

	private List<TUUEntity> buildData() {
		List<TUUEntity> data = new ArrayList<>();

		data.add(TUUEntity.builder().vocesottvoc("0000").value1(0.0D).build());
		data.add(TUUEntity.builder().vocesottvoc("1111").value1(1.0D).build());
		data.add(TUUEntity.builder().vocesottvoc("2222").value1(2.0D).build());
		data.add(TUUEntity.builder().vocesottvoc("3333").value1(3.0D).build());
		data.add(TUUEntity.builder().vocesottvoc("4444").value1(4.0D).build());
		data.add(TUUEntity.builder().vocesottvoc("5555").value1(5.0D).build());
		data.add(TUUEntity.builder().vocesottvoc("6666").value1(6.0D).build());
		data.add(TUUEntity.builder().vocesottvoc("7777").value1(7.0D).build());
		data.add(TUUEntity.builder().vocesottvoc("8888").value1(8.0D).build());
		data.add(TUUEntity.builder().vocesottvoc("9999").value1(9.0D).build());

		return data;
	}
}
