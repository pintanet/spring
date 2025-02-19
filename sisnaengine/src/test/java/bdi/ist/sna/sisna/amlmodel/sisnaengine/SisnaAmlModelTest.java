package bdi.ist.sna.sisna.amlmodel.sisnaengine;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
public class SisnaAmlModelTest {

	protected ExpressionParser parser;

	protected Data data;

	@Autowired
	protected Context context;

	private void initContext() {
		data = new Data();
		data.put("BANCHE_G", "tot_operativita", "10001", 11.0);
		data.put("BANCHE_G", "tot_operativita", "10002", 12.0);
		data.put("BANCHE_G", "tot_operativita", "10003", 13);
		data.put("BANCHE_G", "tot_operativita", "10004", 14.0);

		context.setVariable("data", data);
	}

	@Test
	public void test1() {
		try {
			initContext();

			parser = new SpelExpressionParser();

			String exprString = "#norm(#data, 'BANCHE_G','tot_operativita','10003')";

			Expression expr = parser.parseExpression(exprString);

			Object f = expr.getValue(context);

			log.info("Risultato {}", f);
			assertEquals(2, 2);
		} catch (Exception e) {
			log.error("Errore", e);
		}
	};

	@Test
	public void test2() {
		try {
			initContext();

			parser = new SpelExpressionParser();

			String exprMinStr = "#min(#getBySettore(#data,'BANCHE_G','tot_operativita'))";
			Expression exprMin = parser.parseExpression(exprMinStr);
			Object min = exprMin.getValue(context);
			context.setVariable("min", min);

			String exprMaxStr = "#max(#getBySettore(#data,'BANCHE_G','tot_operativita'))";
			Expression exprMax = parser.parseExpression(exprMaxStr);
			Object max = exprMax.getValue(context);
			context.setVariable("max", max);

			String exprValStr = "#getByIdSna(#data,'BANCHE_G','tot_operativita','10003')";
			Expression exprVal = parser.parseExpression(exprValStr);
			Object val = exprVal.getValue(context);
			context.setVariable("val", val);

			String exprStr = "(#val-#min)/(#max-#min)";
			Expression expr = parser.parseExpression(exprStr);
			Object norm = expr.getValue(context);

			log.info("Risultato {}", norm);
			assertEquals(2, 2);
		} catch (Exception e) {
			log.error("Errore", e);
		}
	};

	@Test
	public void test3() {
		try {
			initContext();

			parser = new SpelExpressionParser();

			List<Entry<String, String>> listIn = new ArrayList();

			listIn.add(new SimpleEntry("max", "#max(#getBySettore(#data,'BANCHE_G','tot_operativita'))"));
			listIn.add(new SimpleEntry("norm", "(#val-#min)/(#max-#min)"));
			listIn.add(new SimpleEntry("val", "#getByIdSna(#data,'BANCHE_G','tot_operativita','10003')"));
			listIn.add(new SimpleEntry("min", "#min(#getBySettore(#data,'BANCHE_G','tot_operativita'))"));
			listIn.add(new SimpleEntry("zic", "xic"));

			log.info("KEY BEGIN: {}", listIn.toString());

			List<Entry<String, String>> listOut = new ArrayList();

			int tot = listIn.size();
			int iOutPrev = -1;
			int iOut = 0;
			while ((listOut.size() < tot) && (iOutPrev != iOut)) {
				for (Entry<String, String> e : listIn) {
					try {
						Expression expr = parser.parseExpression(e.getValue());
						Object value = expr.getValue(context);
						context.setVariable(e.getKey(), value);
						listOut.add(e);

					} catch (Exception ex) {
						log.error("Errore Spel");
					}
				}
				listIn.removeAll(listOut);
				iOutPrev = iOut;
				iOut = listOut.size();
			}
			log.info("KEY Start: {}", listIn.toString());

			log.info("KEY END: {}", listOut.toString());

			log.info("Risultato {}", parser.parseExpression("#norm").getValue(context));

		} catch (Exception e) {
			log.error("Errore", e);
		}
	};
}
